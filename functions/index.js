const { onDocumentCreated, onDocumentWritten } = require("firebase-functions/v2/firestore");
const admin = require("firebase-admin");

admin.initializeApp();

const db = admin.firestore();
const messaging = admin.messaging();

exports.sendPushForNotification = onDocumentCreated(
  "notifications/{notificationId}",
  async (event) => {
    const notification = event.data?.data();
    if (!notification) return;

    const recipientUid = notification.recipientUid;
    if (!recipientUid) return;

    const devices = await db
      .collection("users")
      .doc(recipientUid)
      .collection("devices")
      .where("enabled", "==", true)
      .get();

    const tokens = devices.docs
      .map((doc) => doc.get("token"))
      .filter((token) => typeof token === "string" && token.length > 0);

    if (!tokens.length) return;

    const title = String(notification.title || "Bienestar Animal Zipaquirá");
    const body = String(notification.body || "Tienes una actualización.");
    const caseId = String(notification.caseId || "");
    const type = String(notification.type || "case_update");

    const response = await messaging.sendEachForMulticast({
      tokens,
      notification: { title, body },
      data: {
        title,
        body,
        caseId,
        type,
      },
      android: {
        priority: "high",
        notification: {
          channelId: "case_updates",
        },
      },
    });

    const invalidCodes = new Set([
      "messaging/registration-token-not-registered",
      "messaging/invalid-registration-token",
    ]);

    const cleanup = [];
    response.responses.forEach((result, index) => {
      if (!result.success && invalidCodes.has(result.error?.code)) {
        const token = tokens[index];
        const match = devices.docs.find((doc) => doc.get("token") === token);
        if (match) cleanup.push(match.ref.delete());
      }
    });
    await Promise.all(cleanup);
  }
);

exports.auditCaseWrite = onDocumentWritten(
  "cases/{caseId}",
  async (event) => {
    const before = event.data?.before?.exists ? event.data.before.data() : null;
    const after = event.data?.after?.exists ? event.data.after.data() : null;
    const caseId = event.params.caseId;

    let action = "case_updated";
    if (!before && after) action = "case_created";
    if (before && !after) action = "case_deleted";

    const changed = [];
    if (before && after) {
      const tracked = [
        "status",
        "finalPriority",
        "assignedTo",
        "evidenceUrls",
        "veterinaryDiagnosis",
        "veterinaryTreatment",
      ];
      for (const key of tracked) {
        const previous = JSON.stringify(before[key] ?? null);
        const current = JSON.stringify(after[key] ?? null);
        if (previous !== current) changed.push(key);
      }

      if (changed.includes("assignedTo")) action = "case_assigned_or_reassigned";
      else if (changed.includes("finalPriority")) action = "priority_updated";
      else if (changed.includes("status")) action = "status_updated";
      else if (changed.includes("evidenceUrls")) action = "evidence_added";
      else if (changed.includes("veterinaryDiagnosis") || changed.includes("veterinaryTreatment")) {
        action = "clinical_snapshot_updated";
      }
    }

    const source = after || before || {};
    let actorUid = "system";
    if (action === "priority_updated") {
      actorUid = source.priorityReviewedBy || "system";
    } else if (action === "case_assigned_or_reassigned") {
      actorUid = source.assignedBy || "system";
    } else if (action === "status_updated") {
      actorUid = source.assignedTo || "system";
    } else if (action === "evidence_added") {
      actorUid = source.createdBy || "system";
    } else if (action === "clinical_snapshot_updated") {
      actorUid = source.veterinaryUpdatedBy || source.assignedTo || "system";
    } else {
      actorUid = source.createdBy || "system";
    }

    await db.collection("auditLogs").add({
      caseId,
      action,
      actorUid,
      changedFields: changed,
      before: before
        ? {
            status: before.status ?? null,
            finalPriority: before.finalPriority ?? null,
            assignedTo: before.assignedTo ?? null,
            evidenceCount: Array.isArray(before.evidenceUrls) ? before.evidenceUrls.length : 0,
          }
        : null,
      after: after
        ? {
            status: after.status ?? null,
            finalPriority: after.finalPriority ?? null,
            assignedTo: after.assignedTo ?? null,
            evidenceCount: Array.isArray(after.evidenceUrls) ? after.evidenceUrls.length : 0,
          }
        : null,
      createdAt: admin.firestore.FieldValue.serverTimestamp(),
    });
  }
);
