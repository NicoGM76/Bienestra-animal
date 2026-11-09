(() => {
  const screens = [...document.querySelectorAll('.screen')];
  let currentRole = '';
  const protectedScreenRoles = {
    home: 'citizen',
    register: 'citizen',
    location: 'citizen',
    report: 'citizen',
    adoptions: 'citizen',
    'vet-home': 'veterinarian',
    'vet-care': 'veterinarian',
    'admin-home': 'administrator',
    'admin-case': 'administrator'
  };

  const canOpenScreen = (name) => !protectedScreenRoles[name] || protectedScreenRoles[name] === currentRole;

  const go = (name) => {
    const safeName = canOpenScreen(name) ? name : 'login';
    screens.forEach(s => s.classList.toggle('active', s.dataset.screen === safeName));
    window.scrollTo({ top: 0, behavior: 'instant' });
    history.pushState({ screen: safeName }, '', `#${safeName}`);
  };

  const toast = (message) => {
    if (window.Android && typeof window.Android.showToast === 'function') {
      Android.showToast(message);
    } else {
      alert(message);
    }
  };

  const loginButton = document.getElementById('loginButton');
  const signupButton = document.getElementById('signupButton');
  const authMessage = document.getElementById('authMessage');
  const signupMessage = document.getElementById('signupMessage');
  const saveAnimalButton = document.getElementById('saveAnimal');
  const animalMessage = document.getElementById('animalMessage');
  const animalList = document.getElementById('animalList');
  const saveCaseButton = document.getElementById('saveCase');
  const caseMessage = document.getElementById('caseMessage');
  const caseList = document.getElementById('caseList');
  const citizenCaseSearch = document.getElementById('citizenCaseSearch');
  const citizenStatusFilter = document.getElementById('citizenStatusFilter');
  const logoutButton = document.getElementById('logoutButton');
  const vetLogoutButton = document.getElementById('vetLogoutButton');
  const adminLogoutButton = document.getElementById('adminLogoutButton');
  const vetCaseList = document.getElementById('vetCaseList');
  const vetMetrics = document.getElementById('vetMetrics');
  const vetCareTitle = document.getElementById('vetCareTitle');
  const vetCareSummary = document.getElementById('vetCareSummary');
  const vetCareStatus = document.getElementById('vetCareStatus');
  const vetCareCondition = document.getElementById('vetCareCondition');
  const vetCareLocation = document.getElementById('vetCareLocation');
  const vetCareDescription = document.getElementById('vetCareDescription');
  const vetDiagnosis = document.getElementById('vetDiagnosis');
  const vetObservations = document.getElementById('vetObservations');
  const vetTreatment = document.getElementById('vetTreatment');
  const vetRecommendations = document.getElementById('vetRecommendations');
  const vetCareMessage = document.getElementById('vetCareMessage');
  const saveVetCareButton = document.getElementById('saveVetCareButton');
  const adminMetrics = document.getElementById('adminMetrics');
  const adminRoleMetrics = document.getElementById('adminRoleMetrics');
  const adminCaseMetrics = document.getElementById('adminCaseMetrics');
  const adminRecentList = document.getElementById('adminRecentList');
  const adminAssignmentList = document.getElementById('adminAssignmentList');
  const adminAssignmentMessage = document.getElementById('adminAssignmentMessage');
  const citizenNotificationList = document.getElementById('citizenNotificationList');
  const vetNotificationList = document.getElementById('vetNotificationList');
  const vetClinicalHistory = document.getElementById('vetClinicalHistory');
  const vetCaseSearch = document.getElementById('vetCaseSearch');
  const vetStatusFilter = document.getElementById('vetStatusFilter');
  const adminCaseList = document.getElementById('adminCaseList');
  const adminCaseSearch = document.getElementById('adminCaseSearch');
  const adminStatusFilter = document.getElementById('adminStatusFilter');
  const adminPriorityFilter = document.getElementById('adminPriorityFilter');
  const adminTypeFilter = document.getElementById('adminTypeFilter');
  const adminCaseTitle = document.getElementById('adminCaseTitle');
  const adminCaseSummary = document.getElementById('adminCaseSummary');
  const adminCaseStatus = document.getElementById('adminCaseStatus');
  const adminCasePriority = document.getElementById('adminCasePriority');
  const adminCaseVet = document.getElementById('adminCaseVet');
  const adminCaseLocation = document.getElementById('adminCaseLocation');
  const adminCaseDescription = document.getElementById('adminCaseDescription');
  const adminLegacyNotice = document.getElementById('adminLegacyNotice');
  const adminAiPriority = document.getElementById('adminAiPriority');
  const adminAiReason = document.getElementById('adminAiReason');
  const adminPrioritySelect = document.getElementById('adminPrioritySelect');
  const useAiPriorityButton = document.getElementById('useAiPriorityButton');
  const saveAdminPriorityButton = document.getElementById('saveAdminPriorityButton');
  const adminReassignPanel = document.getElementById('adminReassignPanel');
  const adminReassignVetSelect = document.getElementById('adminReassignVetSelect');
  const adminReassignButton = document.getElementById('adminReassignButton');
  const adminCaseMessage = document.getElementById('adminCaseMessage');
  const reportTitle = document.getElementById('reportTitle');
  const reportHeader = document.getElementById('reportHeader');
  const reportLead = document.getElementById('reportLead');
  const protectedIdentityNotice = document.getElementById('protectedIdentityNotice');
  let lastAuthAction = 'login';
  let currentReportType = 'lost';
  let currentCitizenCases = [];
  let currentVetCases = [];
  let currentVetCaseId = '';
  let currentAdminCases = [];
  let currentAdminUsers = [];
  let currentAdminCaseId = '';

  const clearNode = (node) => {
    if (!node) return;
    while (node.firstChild) node.removeChild(node.firstChild);
  };

  const clearSessionData = () => {
    currentRole = '';
    currentCitizenCases = [];
    currentVetCases = [];
    currentVetCaseId = '';
    currentAdminCases = [];
    currentAdminUsers = [];
    currentAdminCaseId = '';
    clearNode(animalList);
    clearNode(caseList);
    clearNode(vetCaseList);
    clearNode(adminRecentList);
    clearNode(adminAssignmentList);
    clearNode(adminCaseList);
    clearNode(citizenNotificationList);
    clearNode(vetNotificationList);
    clearNode(vetClinicalHistory);
    if (adminAssignmentMessage) setMessage(adminAssignmentMessage, '');
    if (adminCaseMessage) setMessage(adminCaseMessage, '');

    [vetMetrics, adminMetrics, adminRoleMetrics, adminCaseMetrics].forEach(container => {
      if (!container) return;
      container.querySelectorAll('.metric-card strong').forEach(value => {
        value.textContent = '—';
      });
    });

    [vetDiagnosis, vetObservations, vetTreatment, vetRecommendations].forEach(field => {
      if (field) {
        field.value = '';
        field.disabled = false;
      }
    });
    if (saveVetCareButton) saveVetCareButton.disabled = false;
    if (vetCareMessage) vetCareMessage.textContent = '';

    ['signedInUser', 'vetSignedInUser', 'adminSignedInUser'].forEach(id => {
      const label = document.getElementById(id);
      if (label) label.textContent = 'Sesión cerrada.';
    });
  };

  const setMessage = (element, message, type = '') => {
    if (!element) return;
    element.textContent = message || '';
    element.className = `form-message${type ? ` ${type}` : ''}`;
  };

  const setAuthBusy = (busy, action = lastAuthAction) => {
    lastAuthAction = action || lastAuthAction;
    if (loginButton) {
      loginButton.disabled = busy;
      loginButton.classList.toggle('loading', busy && lastAuthAction === 'login');
      loginButton.textContent = busy && lastAuthAction === 'login' ? 'Ingresando…' : 'Iniciar Sesión';
    }
    if (signupButton) {
      signupButton.disabled = busy;
      signupButton.classList.toggle('loading', busy && lastAuthAction === 'register');
      signupButton.textContent = busy && lastAuthAction === 'register' ? 'Creando cuenta…' : 'Crear cuenta';
    }
  };

  const setAnimalBusy = (busy) => {
    if (!saveAnimalButton) return;
    saveAnimalButton.disabled = busy;
    saveAnimalButton.classList.toggle('loading', busy);
    saveAnimalButton.textContent = busy ? 'Guardando…' : 'Guardar animal';
  };

  const requestMyAnimals = () => {
    if (window.Android && typeof window.Android.loadMyAnimals === 'function') {
      Android.loadMyAnimals();
    }
  };

  const setCaseBusy = (busy) => {
    if (!saveCaseButton) return;
    saveCaseButton.disabled = busy;
    saveCaseButton.classList.toggle('loading', busy);
    saveCaseButton.textContent = busy ? 'Enviando…' : 'Enviar reporte';
  };

  const requestMyCases = () => {
    if (window.Android && typeof window.Android.loadMyCases === 'function') {
      Android.loadMyCases();
    }
  };

  const requestVeterinarianCases = () => {
    if (window.Android && typeof window.Android.loadAssignedCases === 'function') {
      Android.loadAssignedCases();
    }
  };

  const requestAdminDashboard = () => {
    if (window.Android && typeof window.Android.loadAdminDashboard === 'function') {
      Android.loadAdminDashboard();
    }
  };

  const requestNotifications = () => {
    if (window.Android && typeof window.Android.loadNotifications === 'function') {
      Android.loadNotifications();
    }
  };

  const requestClinicalHistory = (caseId) => {
    if (caseId && window.Android && typeof window.Android.loadClinicalHistory === 'function') {
      Android.loadClinicalHistory(caseId);
    }
  };

  const openPortalForRole = (data) => {
    const role = data && data.activeRole ? data.activeRole : '';
    const displayName = data && data.name ? data.name : (data && data.email ? data.email : 'Usuario');
    currentRole = role;

    if (role === 'citizen') {
      const signedInUser = document.getElementById('signedInUser');
      if (signedInUser) signedInUser.textContent = `Sesión: ${displayName}`;
      go('home');
      requestMyAnimals();
      requestMyCases();
      requestNotifications();
      return;
    }

    if (role === 'veterinarian') {
      const vetSignedInUser = document.getElementById('vetSignedInUser');
      if (vetSignedInUser) vetSignedInUser.textContent = `Sesión: ${displayName}`;
      go('vet-home');
      requestVeterinarianCases();
      requestNotifications();
      return;
    }

    if (role === 'administrator') {
      const adminSignedInUser = document.getElementById('adminSignedInUser');
      if (adminSignedInUser) adminSignedInUser.textContent = `Sesión: ${displayName}`;
      go('admin-home');
      requestAdminDashboard();
      return;
    }

    currentRole = '';
    setMessage(authMessage, 'El rol de esta cuenta no está habilitado.', 'error');
    if (window.Android && typeof window.Android.logout === 'function') Android.logout();
  };

  const reportLabels = {
    lost: { title: 'Reportar pérdida', lead: 'Registra dónde y cuándo identificaste la pérdida. El caso quedará recibido para seguimiento.' },
    found: { title: 'Reportar hallazgo', lead: 'Registra el animal encontrado y su ubicación para facilitar la búsqueda de su responsable.' },
    abuse: { title: 'Reportar presunto maltrato', lead: 'Describe la situación observada. Este reporte usa identidad protegida para el denunciante.' }
  };

  const configureReport = (type) => {
    currentReportType = reportLabels[type] ? type : 'lost';
    const label = reportLabels[currentReportType];
    if (reportTitle) reportTitle.textContent = label.title;
    if (reportHeader) reportHeader.textContent = label.title;
    if (reportLead) reportLead.textContent = label.lead;
    if (protectedIdentityNotice) protectedIdentityNotice.hidden = currentReportType !== 'abuse';
    setMessage(caseMessage, '');
    go('report');
  };

  const typeLabel = (type) => ({ lost: 'Pérdida', found: 'Hallazgo', abuse: 'Presunto maltrato' }[type] || 'Reporte');
  const priorityLabel = (priority) => ({ low: 'Baja', medium: 'Media', high: 'Alta', critical: 'Crítica', pending_review: 'Pendiente de revisión' }[priority] || priority || 'Pendiente');
  const statusLabel = (status) => ({ received: 'Recibido', under_review: 'En revisión', assigned: 'Asignado', in_progress: 'En atención', completed: 'Realizado' }[status] || status || 'Recibido');

  const renderCaseList = (cases, preserveSource = false) => {
    if (!caseList) return;
    if (!preserveSource) currentCitizenCases = Array.isArray(cases) ? cases : [];

    const query = (citizenCaseSearch ? citizenCaseSearch.value : '').trim().toLowerCase();
    const statusFilter = citizenStatusFilter ? citizenStatusFilter.value : '';
    const visibleCases = currentCitizenCases.filter(item => {
      const haystack = [typeLabel(item.type), item.neighborhoodOrVillage, item.finalPriority]
        .filter(Boolean).join(' ').toLowerCase();
      return (!query || haystack.includes(query))
        && (!statusFilter || item.status === statusFilter);
    });

    clearNode(caseList);

    if (visibleCases.length === 0) {
      const empty = document.createElement('div');
      empty.className = 'empty-state';
      const title = document.createElement('strong');
      title.textContent = currentCitizenCases.length ? 'No hay reportes con estos filtros' : 'Aún no has enviado reportes';
      const detail = document.createElement('span');
      detail.textContent = currentCitizenCases.length
        ? 'Prueba con otra búsqueda o estado.'
        : 'Cuando envíes uno podrás consultar aquí su estado general.';
      empty.append(title, detail);
      caseList.appendChild(empty);
      return;
    }

    const grid = document.createElement('div');
    grid.className = 'case-grid';
    visibleCases.forEach(item => {
      const card = document.createElement('article');
      card.className = 'case-card';

      const top = document.createElement('div');
      top.className = 'case-card-top';
      const name = document.createElement('strong');
      name.textContent = typeLabel(item.type);
      const priority = document.createElement('em');
      priority.className = item.finalPriority === 'critical' || item.finalPriority === 'high' ? 'status review' : 'status ok';
      priority.textContent = priorityLabel(item.finalPriority);
      top.append(name, priority);

      const location = document.createElement('span');
      location.textContent = item.neighborhoodOrVillage || 'Zipaquirá';
      const status = document.createElement('b');
      status.textContent = `Estado: ${statusLabel(item.status)}`;
      card.append(top, location, status);
      grid.appendChild(card);
    });
    caseList.appendChild(grid);
  };


  const renderVeterinarianMetrics = (cases) => {
    if (!vetMetrics) return;
    const values = vetMetrics.querySelectorAll('.metric-card strong');
    const list = Array.isArray(cases) ? cases : [];
    const assigned = list.filter(item => item.status === 'assigned').length;
    const inProgress = list.filter(item => item.status === 'in_progress').length;
    const completed = list.filter(item => item.status === 'completed').length;
    if (values[0]) values[0].textContent = String(assigned);
    if (values[1]) values[1].textContent = String(inProgress);
    if (values[2]) values[2].textContent = String(completed);
  };

  const filteredVeterinarianCases = () => {
    const query = (vetCaseSearch ? vetCaseSearch.value : '').trim().toLowerCase();
    const status = vetStatusFilter ? vetStatusFilter.value : '';

    return currentVetCases.filter(item => {
      const haystack = [
        item.type,
        item.description,
        item.neighborhoodOrVillage,
        item.addressOrLocation,
        item.referencePoint,
        item.animalCondition,
        item.finalPriority
      ].filter(Boolean).join(' ').toLowerCase();

      return (!query || haystack.includes(query))
        && (!status || item.status === status);
    });
  };

  const renderVeterinarianCases = (cases, preserveSource = false) => {
    if (!vetCaseList) return;
    if (!preserveSource) {
      currentVetCases = Array.isArray(cases) ? cases : [];
      renderVeterinarianMetrics(currentVetCases);
    }

    const visibleCases = preserveSource ? filteredVeterinarianCases() : filteredVeterinarianCases();
    clearNode(vetCaseList);

    if (visibleCases.length === 0) {
      const empty = document.createElement('div');
      empty.className = 'empty-state';
      const title = document.createElement('strong');
      title.textContent = currentVetCases.length ? 'No hay resultados con estos filtros' : 'No tienes casos asignados';
      const detail = document.createElement('span');
      detail.textContent = currentVetCases.length
        ? 'Prueba con otro texto o estado.'
        : 'Cuando se asigne un caso a tu cuenta aparecerá aquí.';
      empty.append(title, detail);
      vetCaseList.appendChild(empty);
      return;
    }

    const grid = document.createElement('div');
    grid.className = 'case-grid';
    visibleCases.forEach(item => {
      const card = document.createElement('article');
      card.className = 'case-card';

      const top = document.createElement('div');
      top.className = 'case-card-top';
      const name = document.createElement('strong');
      name.textContent = typeLabel(item.type);
      const priority = document.createElement('em');
      priority.className = item.finalPriority === 'critical' || item.finalPriority === 'high' ? 'status review' : 'status ok';
      priority.textContent = priorityLabel(item.finalPriority);
      top.append(name, priority);

      const location = document.createElement('span');
      location.textContent = [item.neighborhoodOrVillage, item.addressOrLocation].filter(Boolean).join(' · ') || 'Zipaquirá';

      const condition = document.createElement('span');
      condition.textContent = item.animalCondition ? `Condición: ${item.animalCondition}` : 'Condición no registrada';

      const status = document.createElement('b');
      status.textContent = `Estado: ${statusLabel(item.status)}`;

      const summary = document.createElement('span');
      const rawDescription = item.description || '';
      summary.textContent = rawDescription
        ? (rawDescription.length > 115 ? rawDescription.slice(0, 112) + '…' : rawDescription)
        : 'Sin descripción adicional.';

      card.append(top, location, condition, status, summary);

      if (item.legacy) {
        const legacy = document.createElement('span');
        legacy.className = 'legacy-pill';
        legacy.textContent = 'Registro legado compatible';
        card.appendChild(legacy);
      }

      const detailAction = document.createElement('button');
      detailAction.className = 'btn ghost block';
      detailAction.dataset.vetOpenCaseId = item.id;
      detailAction.textContent = item.status === 'completed' ? 'Ver ficha clínica' : 'Abrir ficha de atención';
      card.appendChild(detailAction);

      const nextStatus = item.status === 'assigned'
        ? 'in_progress'
        : (item.status === 'in_progress' ? 'completed' : '');

      if (nextStatus) {
        const action = document.createElement('button');
        action.className = 'btn secondary block';
        action.dataset.vetCaseId = item.id;
        action.dataset.vetNextStatus = nextStatus;
        action.textContent = nextStatus === 'in_progress' ? 'Iniciar atención' : 'Marcar como realizado';
        card.appendChild(action);
      }

      grid.appendChild(card);
    });

    vetCaseList.appendChild(grid);
  };

  const openVeterinarianCase = (caseId) => {
    const item = currentVetCases.find(candidate => candidate.id === caseId);
    if (!item) {
      toast('No fue posible abrir el caso seleccionado.');
      return;
    }

    currentVetCaseId = item.id;
    if (vetCareTitle) vetCareTitle.textContent = typeLabel(item.type);
    if (vetCareSummary) vetCareSummary.textContent = `Caso ${item.id.slice(0, 8)} · ${item.animalCount || 1} animal(es)`;
    if (vetCareStatus) vetCareStatus.textContent = statusLabel(item.status);
    if (vetCareCondition) vetCareCondition.textContent = item.animalCondition || 'No registrada';
    if (vetCareLocation) {
      vetCareLocation.textContent = [item.neighborhoodOrVillage, item.addressOrLocation, item.referencePoint]
        .filter(Boolean)
        .join(' · ') || 'Zipaquirá';
    }
    if (vetCareDescription) vetCareDescription.textContent = item.description || 'Sin descripción adicional.';

    if (vetDiagnosis) vetDiagnosis.value = item.veterinaryDiagnosis || '';
    if (vetObservations) vetObservations.value = item.veterinaryObservations || '';
    if (vetTreatment) vetTreatment.value = item.veterinaryTreatment || '';
    if (vetRecommendations) vetRecommendations.value = item.veterinaryRecommendations || '';

    const editable = item.status === 'in_progress';
    [vetDiagnosis, vetObservations, vetTreatment, vetRecommendations].forEach(field => {
      if (field) field.disabled = !editable;
    });
    if (saveVetCareButton) saveVetCareButton.disabled = !editable;

    if (editable) {
      setMessage(vetCareMessage, 'Ficha habilitada para registrar o actualizar la atención.');
    } else if (item.status === 'assigned') {
      setMessage(vetCareMessage, 'Primero inicia la atención desde el portal veterinario.', 'error');
    } else if (item.status === 'completed') {
      setMessage(vetCareMessage, 'Caso finalizado. La ficha se muestra en modo de consulta.', 'success');
    } else {
      setMessage(vetCareMessage, 'La ficha no está habilitada para este estado.');
    }

    go('vet-care');
    requestClinicalHistory(item.id);
  };

  const renderClinicalHistory = (records) => {
    if (!vetClinicalHistory) return;
    clearNode(vetClinicalHistory);

    if (!Array.isArray(records) || records.length === 0) {
      const empty = document.createElement('div');
      empty.className = 'empty-state';
      const title = document.createElement('strong');
      title.textContent = 'Aún no hay atenciones registradas';
      const detail = document.createElement('span');
      detail.textContent = 'Cuando guardes una ficha aparecerá aquí sin sobrescribir las anteriores.';
      empty.append(title, detail);
      vetClinicalHistory.appendChild(empty);
      return;
    }

    records.forEach((record, index) => {
      const card = document.createElement('article');
      card.className = 'clinical-record';
      const title = document.createElement('strong');
      title.textContent = record.source === 'legacy_case_snapshot'
        ? 'Registro clínico legado'
        : `Atención #${records.length - index}`;

      const diagnosis = document.createElement('span');
      diagnosis.textContent = `Diagnóstico: ${record.diagnosis || 'No registrado'}`;
      const treatment = document.createElement('span');
      treatment.textContent = `Tratamiento: ${record.treatment || 'No registrado'}`;
      const observations = document.createElement('span');
      observations.textContent = `Observaciones: ${record.observations || 'Sin observaciones'}`;
      const recommendations = document.createElement('span');
      recommendations.textContent = `Seguimiento: ${record.recommendations || 'Sin recomendaciones'}`;
      const date = document.createElement('small');
      date.textContent = record.createdAtMillis
        ? new Date(record.createdAtMillis).toLocaleString('es-CO')
        : 'Fecha no disponible';

      card.append(title, diagnosis, observations, treatment, recommendations, date);
      vetClinicalHistory.appendChild(card);
    });
  };

  const renderNotifications = (notifications) => {
    const target = currentRole === 'veterinarian'
      ? vetNotificationList
      : (currentRole === 'citizen' ? citizenNotificationList : null);
    if (!target) return;
    clearNode(target);

    const list = Array.isArray(notifications) ? notifications : [];
    if (list.length === 0) {
      const empty = document.createElement('div');
      empty.className = 'empty-state';
      const title = document.createElement('strong');
      title.textContent = 'No tienes notificaciones';
      const detail = document.createElement('span');
      detail.textContent = 'Las novedades importantes de tus casos aparecerán aquí.';
      empty.append(title, detail);
      target.appendChild(empty);
      return;
    }

    list.forEach(item => {
      const card = document.createElement('article');
      card.className = `notification-card${item.read ? '' : ' unread'}`;

      const top = document.createElement('div');
      top.className = 'notification-top';
      const title = document.createElement('strong');
      title.textContent = item.title || 'Notificación';
      const status = document.createElement('span');
      status.textContent = item.read ? 'Leída' : 'Nueva';
      top.append(title, status);

      const body = document.createElement('span');
      body.textContent = item.body || '';
      card.append(top, body);

      if (item.createdAtMillis) {
        const date = document.createElement('small');
        date.textContent = new Date(item.createdAtMillis).toLocaleString('es-CO');
        card.appendChild(date);
      }

      if (!item.read) {
        const button = document.createElement('button');
        button.className = 'btn ghost block';
        button.dataset.notificationReadId = item.id;
        button.textContent = 'Marcar como leída';
        card.appendChild(button);
      }

      target.appendChild(card);
    });
  };


  const filteredAdminCases = () => {
    const query = (adminCaseSearch ? adminCaseSearch.value : '').trim().toLowerCase();
    const status = adminStatusFilter ? adminStatusFilter.value : '';
    const priority = adminPriorityFilter ? adminPriorityFilter.value : '';
    const type = adminTypeFilter ? adminTypeFilter.value : '';

    return currentAdminCases.filter(item => {
      const haystack = [
        item.type,
        item.description,
        item.municipality,
        item.neighborhoodOrVillage,
        item.addressOrLocation,
        item.referencePoint,
        item.animalCondition,
        item.assignedToName
      ].filter(Boolean).join(' ').toLowerCase();

      return (!query || haystack.includes(query))
        && (!status || item.status === status)
        && (!priority || item.finalPriority === priority)
        && (!type || item.type === type);
    });
  };

  const renderAdminCaseManagement = () => {
    if (!adminCaseList) return;
    clearNode(adminCaseList);
    const cases = filteredAdminCases();

    if (cases.length === 0) {
      const empty = document.createElement('div');
      empty.className = 'empty-state';
      const title = document.createElement('strong');
      title.textContent = currentAdminCases.length ? 'No hay casos con estos filtros' : 'No hay casos registrados';
      const detail = document.createElement('span');
      detail.textContent = currentAdminCases.length ? 'Ajusta los filtros de búsqueda.' : 'Los nuevos reportes aparecerán aquí.';
      empty.append(title, detail);
      adminCaseList.appendChild(empty);
      return;
    }

    cases.forEach(item => {
      const card = document.createElement('article');
      card.className = 'admin-case-card';

      const top = document.createElement('div');
      top.className = 'case-card-top';
      const title = document.createElement('strong');
      title.textContent = typeLabel(item.type);
      const priority = document.createElement('em');
      priority.className = item.finalPriority === 'critical' || item.finalPriority === 'high' ? 'status review' : 'status ok';
      priority.textContent = priorityLabel(item.finalPriority);
      top.append(title, priority);

      const line = document.createElement('span');
      line.textContent = [
        statusLabel(item.status),
        item.neighborhoodOrVillage || item.municipality || 'Zipaquirá',
        item.animalCondition || 'Condición no registrada',
        item.assignedToName || 'Sin asignar'
      ].join(' · ');

      const summary = document.createElement('span');
      const text = item.description || 'Sin descripción adicional.';
      summary.textContent = text.length > 120 ? text.slice(0, 117) + '…' : text;

      card.append(top, line, summary);
      if (item.legacy) {
        const legacy = document.createElement('span');
        legacy.className = 'legacy-pill';
        legacy.textContent = 'Legado';
        card.appendChild(legacy);
      }

      const button = document.createElement('button');
      button.className = 'btn ghost block';
      button.dataset.adminOpenCaseId = item.id;
      button.textContent = 'Abrir expediente';
      card.appendChild(button);
      adminCaseList.appendChild(card);
    });
  };

  const openAdminCase = (caseId) => {
    const item = currentAdminCases.find(candidate => candidate.id === caseId);
    if (!item) {
      toast('No fue posible abrir el expediente.');
      return;
    }

    currentAdminCaseId = item.id;
    if (adminCaseTitle) adminCaseTitle.textContent = typeLabel(item.type);
    if (adminCaseSummary) adminCaseSummary.textContent = `Caso ${item.id.slice(0, 8)} · ${item.animalCount || 1} animal(es)`;
    if (adminCaseStatus) adminCaseStatus.textContent = statusLabel(item.status);
    if (adminCasePriority) adminCasePriority.textContent = priorityLabel(item.finalPriority);
    if (adminCaseVet) adminCaseVet.textContent = item.assignedToName || 'Sin asignar';
    if (adminCaseLocation) {
      adminCaseLocation.textContent = [item.municipality, item.neighborhoodOrVillage, item.addressOrLocation, item.referencePoint]
        .filter(Boolean).join(' · ') || 'Ubicación no registrada';
    }
    if (adminCaseDescription) {
      adminCaseDescription.textContent = [
        item.animalCondition ? `Condición: ${item.animalCondition}.` : '',
        item.description || 'Sin descripción adicional.'
      ].filter(Boolean).join(' ');
    }
    if (adminLegacyNotice) adminLegacyNotice.hidden = !item.legacy;

    const aiAvailable = item.aiSuggestedPriority && item.aiSuggestedPriority !== 'pending_review';
    if (adminAiPriority) {
      adminAiPriority.textContent = aiAvailable
        ? `Sugerencia: ${priorityLabel(item.aiSuggestedPriority)}`
        : 'Sin sugerencia disponible';
    }
    if (adminAiReason) {
      adminAiReason.textContent = aiAvailable
        ? (item.aiPriorityReason || 'Sugerencia generada por el asistente beta.')
        : 'Este caso no tiene análisis IA, normalmente porque pertenece a una versión anterior.';
    }

    const currentPriority = ['low', 'medium', 'high', 'critical'].includes(item.finalPriority)
      ? item.finalPriority
      : (aiAvailable ? item.aiSuggestedPriority : 'medium');
    if (adminPrioritySelect) adminPrioritySelect.value = currentPriority;

    if (adminReassignVetSelect) {
      clearNode(adminReassignVetSelect);
      currentAdminUsers
        .filter(user => user.activeRole === 'veterinarian' && user.status === 'active')
        .forEach(veterinarian => {
          const option = document.createElement('option');
          option.value = veterinarian.uid;
          option.textContent = veterinarian.name || veterinarian.email || 'Veterinario';
          option.selected = veterinarian.uid === item.assignedToUid;
          adminReassignVetSelect.appendChild(option);
        });
    }

    const canReassign = item.status === 'assigned';
    if (adminReassignPanel) adminReassignPanel.hidden = !canReassign;
    if (saveAdminPriorityButton) saveAdminPriorityButton.disabled = item.status === 'completed';
    if (useAiPriorityButton) useAiPriorityButton.disabled = !aiAvailable || item.status === 'completed';
    setMessage(adminCaseMessage, '');
    go('admin-case');
  };


  const renderAdminAssignments = (cases, users) => {
    if (!adminAssignmentList) return;
    clearNode(adminAssignmentList);

    const receivedCases = (Array.isArray(cases) ? cases : []).filter(item => item.status === 'received');
    const veterinarians = (Array.isArray(users) ? users : []).filter(
      user => user.activeRole === 'veterinarian' && user.status === 'active'
    );

    if (receivedCases.length === 0) {
      const empty = document.createElement('div');
      empty.className = 'empty-state';
      const title = document.createElement('strong');
      title.textContent = 'No hay casos pendientes de asignación';
      const detail = document.createElement('span');
      detail.textContent = 'Cuando llegue un nuevo caso en estado recibido aparecerá aquí.';
      empty.append(title, detail);
      adminAssignmentList.appendChild(empty);
      return;
    }

    if (veterinarians.length === 0) {
      const empty = document.createElement('div');
      empty.className = 'empty-state';
      const title = document.createElement('strong');
      title.textContent = 'No hay veterinarios activos';
      const detail = document.createElement('span');
      detail.textContent = 'Se necesita al menos una cuenta veterinaria activa para asignar casos.';
      empty.append(title, detail);
      adminAssignmentList.appendChild(empty);
      return;
    }

    receivedCases.forEach(item => {
      const card = document.createElement('article');
      card.className = 'admin-assignment-card';

      const title = document.createElement('strong');
      title.textContent = typeLabel(item.type);

      const detail = document.createElement('span');
      detail.textContent = [
        statusLabel(item.status),
        item.neighborhoodOrVillage || 'Zipaquirá',
        item.animalCondition || 'Condición no registrada',
        priorityLabel(item.finalPriority)
      ].filter(Boolean).join(' · ');

      const select = document.createElement('select');
      select.dataset.adminVetSelect = 'true';
      veterinarians.forEach(veterinarian => {
        const option = document.createElement('option');
        option.value = veterinarian.uid;
        option.textContent = veterinarian.name || veterinarian.email || 'Veterinario';
        select.appendChild(option);
      });

      const button = document.createElement('button');
      button.className = 'btn primary block';
      button.dataset.adminAssignCaseId = item.id;
      button.textContent = 'Asignar caso';

      card.append(title, detail, select, button);
      adminAssignmentList.appendChild(card);
    });
  };


  const renderAdminDashboard = (data) => {
    currentAdminCases = Array.isArray(data.cases) ? data.cases : [];
    currentAdminUsers = Array.isArray(data.users) ? data.users : [];
    renderAdminCaseManagement();
    renderAdminAssignments(currentAdminCases, currentAdminUsers);
    if (adminAssignmentMessage) setMessage(adminAssignmentMessage, '');

    if (adminMetrics) {
      const cards = adminMetrics.querySelectorAll('.metric-card strong');
      if (cards[0]) cards[0].textContent = String(data.userCount || 0);
      if (cards[1]) cards[1].textContent = String(data.animalCount || 0);
      if (cards[2]) cards[2].textContent = String(data.caseCount || 0);
      if (cards[3]) cards[3].textContent = String(data.activeCaseCount || 0);
    }

    if (adminRoleMetrics) {
      const cards = adminRoleMetrics.querySelectorAll('.metric-card strong');
      if (cards[0]) cards[0].textContent = String(data.citizenCount || 0);
      if (cards[1]) cards[1].textContent = String(data.veterinarianCount || 0);
      if (cards[2]) cards[2].textContent = String(data.administratorCount || 0);
    }

    if (adminCaseMetrics) {
      const cards = adminCaseMetrics.querySelectorAll('.metric-card strong');
      if (cards[0]) cards[0].textContent = String(data.receivedCaseCount || 0);
      if (cards[1]) cards[1].textContent = String(data.assignedCaseCount || 0);
      if (cards[2]) cards[2].textContent = String(data.inProgressCaseCount || 0);
      if (cards[3]) cards[3].textContent = String(data.completedCaseCount || 0);
    }

    if (!adminRecentList) return;
    while (adminRecentList.firstChild) adminRecentList.removeChild(adminRecentList.firstChild);

    const roleLabel = role => {
      if (role === 'administrator') return 'Administrador';
      if (role === 'veterinarian') return 'Veterinario';
      if (role === 'citizen') return 'Ciudadano';
      return role || 'Sin rol';
    };

    const sections = [
      {
        title: 'Cuentas recientes',
        items: data.users || [],
        describe: item => [item.name || item.email, roleLabel(item.activeRole), item.status === 'active' ? 'Activa' : item.status].filter(Boolean).join(' · ')
      },
      {
        title: 'Animales registrados',
        items: data.animals || [],
        describe: item => [item.name, item.species, item.status === 'active' ? 'Activo' : item.status].filter(Boolean).join(' · ')
      },
      {
        title: 'Casos recientes',
        items: data.cases || [],
        describe: item => [
          typeLabel(item.type),
          statusLabel(item.status),
          item.neighborhoodOrVillage,
          item.animalCondition,
          item.assignedToName
        ].filter(Boolean).join(' · ')
      }
    ];

    sections.forEach(section => {
      const group = document.createElement('div');
      group.className = 'admin-section';
      const heading = document.createElement('strong');
      heading.className = 'admin-section-title';
      heading.textContent = section.title;
      group.appendChild(heading);

      if (!section.items.length) {
        const empty = document.createElement('span');
        empty.textContent = 'Sin registros para mostrar.';
        group.appendChild(empty);
      } else {
        section.items.forEach(item => {
          const row = document.createElement('span');
          row.className = 'admin-row';
          row.textContent = section.describe(item);
          group.appendChild(row);
        });
      }

      adminRecentList.appendChild(group);
    });
  };

  const resetCaseForm = () => {
    ['reportDescription', 'reportNeighborhoodOrVillage', 'reportAddressOrLocation', 'reportReferencePoint', 'reportSuspectedResponsible'].forEach(id => {
      const input = document.getElementById(id);
      if (input) input.value = '';
    });
    const count = document.getElementById('reportAnimalCount');
    const priority = document.getElementById('reportPriority');
    const condition = document.getElementById('reportAnimalCondition');
    const known = document.getElementById('reportResponsibleKnown');
    if (count) count.value = '1';
    if (priority) priority.value = 'system';
    if (condition) condition.value = '';
    if (known) known.value = 'unknown';
  };

  const renderAnimalList = (animals) => {
    if (!animalList) return;
    while (animalList.firstChild) animalList.removeChild(animalList.firstChild);

    if (!Array.isArray(animals) || animals.length === 0) {
      const empty = document.createElement('div');
      empty.className = 'empty-state';
      const title = document.createElement('strong');
      title.textContent = 'Aún no tienes animales registrados';
      const detail = document.createElement('span');
      detail.textContent = 'Agrega tu primer animal para verlo aquí.';
      empty.append(title, detail);
      animalList.appendChild(empty);
      return;
    }

    const grid = document.createElement('div');
    grid.className = 'animal-grid';
    animals.forEach(animal => {
      const card = document.createElement('article');
      card.className = 'animal-card';

      const art = document.createElement('div');
      art.className = 'animal-art';
      art.textContent = animal.species || 'Animal';

      const body = document.createElement('div');
      const name = document.createElement('strong');
      name.textContent = animal.name || 'Sin nombre';
      const description = document.createElement('span');
      description.textContent = [animal.species, animal.breed].filter(Boolean).join(' · ') || 'Animal registrado';
      const status = document.createElement('em');
      status.className = 'status ok';
      status.textContent = animal.status === 'active' ? 'Activo' : (animal.status || 'Registrado');

      body.append(name, description, status);
      card.append(art, body);
      grid.appendChild(card);
    });
    animalList.appendChild(grid);
  };

  const resetAnimalForm = () => {
    const name = document.getElementById('animalName');
    const breed = document.getElementById('animalBreed');
    const color = document.getElementById('animalColor');
    const age = document.getElementById('animalAge');
    const neighborhood = document.getElementById('animalNeighborhoodOrVillage');
    const address = document.getElementById('animalAddressOrLocation');
    const reference = document.getElementById('animalReferencePoint');
    [name, breed, color, age, neighborhood, address, reference].forEach(input => {
      if (input) input.value = '';
    });
  };

  window.BAZNative = {
    onNativeEvent(eventName, payload) {
      const data = payload || {};
      if (eventName === 'auth:loading') {
        const action = data.action === 'register' ? 'register' : 'login';
        setAuthBusy(true, action);
        setMessage(action === 'register' ? signupMessage : authMessage, 'Conectando con el servicio…');
        return;
      }

      if (eventName === 'auth:error') {
        setAuthBusy(false);
        setMessage(lastAuthAction === 'register' ? signupMessage : authMessage, data.message || 'No fue posible completar la operación.', 'error');
        return;
      }

      if (eventName === 'auth:signedout') {
        clearSessionData();
        toast('Sesión cerrada.');
        go('login');
        history.replaceState({ screen: 'login' }, '', '#login');
        return;
      }

      if (eventName === 'auth:success' || eventName === 'auth:registered') {
        setAuthBusy(false);
        setMessage(authMessage, '');
        setMessage(signupMessage, '');
        if (eventName === 'auth:registered') toast('Cuenta ciudadana creada correctamente.');
        openPortalForRole(data);
        return;
      }

      if (eventName === 'animals:loading') {
        if (data.action === 'save') {
          setAnimalBusy(true);
          setMessage(animalMessage, 'Guardando el animal…');
        }
        return;
      }

      if (eventName === 'animals:error') {
        setAnimalBusy(false);
        setMessage(animalMessage, data.message || 'No fue posible completar la operación con el animal.', 'error');
        toast(data.message || 'No fue posible completar la operación con el animal.');
        return;
      }

      if (eventName === 'animals:saved') {
        setAnimalBusy(false);
        setMessage(animalMessage, 'Animal registrado correctamente.', 'success');
        toast('Animal registrado correctamente.');
        resetAnimalForm();
        go('home');
        requestMyAnimals();
        return;
      }

      if (eventName === 'animals:list') {
        renderAnimalList(data.animals || []);
      }


      if (eventName === 'cases:loading') {
        if (data.action === 'save') {
          setCaseBusy(true);
          setMessage(caseMessage, 'Enviando el reporte…');
        }
        return;
      }

      if (eventName === 'cases:error') {
        setCaseBusy(false);
        setMessage(caseMessage, data.message || 'No fue posible completar el reporte.', 'error');
        toast(data.message || 'No fue posible completar el reporte.');
        return;
      }

      if (eventName === 'cases:saved') {
        setCaseBusy(false);
        setMessage(caseMessage, 'Reporte recibido correctamente.', 'success');
        toast('Reporte recibido correctamente.');
        resetCaseForm();
        go('home');
        requestMyCases();
        return;
      }

      if (eventName === 'cases:list') {
        renderCaseList(data.cases || []);
        return;
      }

      if (eventName === 'vet:cases') {
        renderVeterinarianCases(data.cases || []);
        return;
      }

      if (eventName === 'vet:case-updated') {
        toast('Estado del caso actualizado.');
        requestVeterinarianCases();
        requestNotifications();
        return;
      }

      if (eventName === 'vet:care-saved') {
        if (saveVetCareButton) saveVetCareButton.disabled = false;
        setMessage(vetCareMessage, 'Ficha de atención guardada correctamente.', 'success');
        toast('Ficha veterinaria guardada.');
        requestVeterinarianCases();
        requestClinicalHistory(data.caseId || currentVetCaseId);
        return;
      }

      if (eventName === 'vet:history') {
        if (!currentVetCaseId || data.caseId === currentVetCaseId) {
          renderClinicalHistory(data.records || []);
        }
        return;
      }

      if (eventName === 'vet:error') {
        if (saveVetCareButton && currentVetCaseId) saveVetCareButton.disabled = false;
        setMessage(vetCareMessage, data.message || 'No fue posible completar la operación veterinaria.', 'error');
        toast(data.message || 'No fue posible completar la operación veterinaria.');
        return;
      }

      if (eventName === 'admin:dashboard') {
        renderAdminDashboard(data);
        return;
      }

      if (eventName === 'admin:case-assigned') {
        document.querySelectorAll('[data-admin-assign-case-id]').forEach(button => {
          button.disabled = false;
        });
        setMessage(
          adminAssignmentMessage,
          `Caso asignado correctamente a ${data.veterinarianName || 'veterinario'}.`,
          'success'
        );
        toast('Caso asignado correctamente.');
        requestAdminDashboard();
        return;
      }

      if (eventName === 'admin:case-changed') {
        if (data.action === 'priority_updated') {
          toast('Prioridad institucional actualizada.');
        } else if (data.action === 'reassigned') {
          toast(`Caso reasignado a ${data.veterinarianName || 'veterinario'}.`);
        } else {
          toast('Caso actualizado.');
        }
        requestAdminDashboard();
        go('admin-home');
        return;
      }

      if (eventName === 'notifications:list') {
        renderNotifications(data.notifications || []);
        return;
      }

      if (eventName === 'notifications:read') {
        requestNotifications();
        return;
      }

      if (eventName === 'notifications:error') {
        toast(data.message || 'No fue posible cargar las notificaciones.');
        return;
      }

      if (eventName === 'admin:error') {
        document.querySelectorAll('[data-admin-assign-case-id]').forEach(button => {
          button.disabled = false;
        });
        if (currentRole === 'administrator') {
          setMessage(adminAssignmentMessage, data.message || 'No fue posible completar la operación administrativa.', 'error');
          setMessage(adminCaseMessage, data.message || 'No fue posible completar la operación administrativa.', 'error');
        }
        toast(data.message || 'No fue posible cargar el panel administrativo.');
        return;
      }
    }
  };

  document.addEventListener('click', (event) => {
    const nav = event.target.closest('[data-go]');
    if (nav) {
      event.preventDefault();
      go(nav.dataset.go);
      return;
    }

    const reportAction = event.target.closest('[data-report-type]');
    if (reportAction) {
      event.preventDefault();
      configureReport(reportAction.dataset.reportType);
      return;
    }

    const adminOpen = event.target.closest('[data-admin-open-case-id]');
    if (adminOpen) {
      event.preventDefault();
      openAdminCase(adminOpen.dataset.adminOpenCaseId);
      return;
    }

    const notificationRead = event.target.closest('[data-notification-read-id]');
    if (notificationRead) {
      event.preventDefault();
      if (window.Android && typeof window.Android.markNotificationRead === 'function') {
        Android.markNotificationRead(notificationRead.dataset.notificationReadId);
      }
      return;
    }

    const adminAssign = event.target.closest('[data-admin-assign-case-id]');
    if (adminAssign) {
      event.preventDefault();
      const card = adminAssign.closest('.admin-assignment-card');
      const select = card ? card.querySelector('[data-admin-vet-select]') : null;
      const veterinarianUid = select ? select.value : '';
      const caseId = adminAssign.dataset.adminAssignCaseId || '';

      if (!caseId || !veterinarianUid) {
        setMessage(adminAssignmentMessage, 'Selecciona un veterinario válido.', 'error');
        return;
      }

      if (!window.Android || typeof window.Android.assignCaseToVeterinarian !== 'function') {
        setMessage(adminAssignmentMessage, 'La asignación requiere ejecutar la app Android.', 'error');
        return;
      }

      adminAssign.disabled = true;
      setMessage(adminAssignmentMessage, 'Asignando caso…');
      Android.assignCaseToVeterinarian(caseId, veterinarianUid);
      return;
    }

    const vetOpen = event.target.closest('[data-vet-open-case-id]');
    if (vetOpen) {
      event.preventDefault();
      openVeterinarianCase(vetOpen.dataset.vetOpenCaseId);
      return;
    }

    const vetAction = event.target.closest('[data-vet-case-id]');
    if (vetAction) {
      event.preventDefault();
      if (window.Android && typeof window.Android.updateAssignedCaseStatus === 'function') {
        Android.updateAssignedCaseStatus(vetAction.dataset.vetCaseId, vetAction.dataset.vetNextStatus);
      }
      return;
    }
  });

  [citizenCaseSearch, citizenStatusFilter].forEach(control => {
    if (control) control.addEventListener('input', () => renderCaseList(currentCitizenCases, true));
    if (control && control.tagName === 'SELECT') {
      control.addEventListener('change', () => renderCaseList(currentCitizenCases, true));
    }
  });

  [vetCaseSearch, vetStatusFilter].forEach(control => {
    if (control) control.addEventListener('input', () => renderVeterinarianCases(currentVetCases, true));
    if (control && control.tagName === 'SELECT') {
      control.addEventListener('change', () => renderVeterinarianCases(currentVetCases, true));
    }
  });

  [adminCaseSearch, adminStatusFilter, adminPriorityFilter, adminTypeFilter].forEach(control => {
    if (control) control.addEventListener('input', renderAdminCaseManagement);
    if (control && control.tagName === 'SELECT') {
      control.addEventListener('change', renderAdminCaseManagement);
    }
  });

  if (useAiPriorityButton) useAiPriorityButton.addEventListener('click', () => {
    const item = currentAdminCases.find(candidate => candidate.id === currentAdminCaseId);
    if (!item || !adminPrioritySelect) return;
    if (['low', 'medium', 'high', 'critical'].includes(item.aiSuggestedPriority)) {
      adminPrioritySelect.value = item.aiSuggestedPriority;
      setMessage(adminCaseMessage, 'Sugerencia aplicada al selector. Aún debes guardar la prioridad.');
    }
  });

  if (saveAdminPriorityButton) saveAdminPriorityButton.addEventListener('click', () => {
    if (!currentAdminCaseId || !adminPrioritySelect) return;
    if (!window.Android || typeof window.Android.updateAdminCasePriority !== 'function') {
      setMessage(adminCaseMessage, 'La actualización requiere ejecutar la app Android.', 'error');
      return;
    }
    saveAdminPriorityButton.disabled = true;
    setMessage(adminCaseMessage, 'Actualizando prioridad…');
    Android.updateAdminCasePriority(currentAdminCaseId, adminPrioritySelect.value);
  });

  if (adminReassignButton) adminReassignButton.addEventListener('click', () => {
    if (!currentAdminCaseId || !adminReassignVetSelect) return;
    if (!window.Android || typeof window.Android.reassignCaseToVeterinarian !== 'function') {
      setMessage(adminCaseMessage, 'La reasignación requiere ejecutar la app Android.', 'error');
      return;
    }
    adminReassignButton.disabled = true;
    setMessage(adminCaseMessage, 'Reasignando caso…');
    Android.reassignCaseToVeterinarian(currentAdminCaseId, adminReassignVetSelect.value);
  });

  if (saveVetCareButton) saveVetCareButton.addEventListener('click', () => {
    setMessage(vetCareMessage, '');
    if (!currentVetCaseId) {
      setMessage(vetCareMessage, 'Selecciona un caso antes de guardar.', 'error');
      return;
    }
    if (!window.Android || typeof window.Android.saveVeterinaryCare !== 'function') {
      setMessage(vetCareMessage, 'El registro clínico requiere ejecutar la app Android.', 'error');
      return;
    }

    const diagnosis = vetDiagnosis ? vetDiagnosis.value.trim() : '';
    const observations = vetObservations ? vetObservations.value.trim() : '';
    const treatment = vetTreatment ? vetTreatment.value.trim() : '';
    const recommendations = vetRecommendations ? vetRecommendations.value.trim() : '';

    if (diagnosis.length < 3 || treatment.length < 3) {
      setMessage(vetCareMessage, 'Completa al menos diagnóstico y tratamiento.', 'error');
      return;
    }

    saveVetCareButton.disabled = true;
    setMessage(vetCareMessage, 'Guardando ficha clínica…');
    Android.saveVeterinaryCare(currentVetCaseId, diagnosis, observations, treatment, recommendations);
  });

  if (loginButton) loginButton.addEventListener('click', () => {
    const email = document.getElementById('email').value.trim();
    const password = document.getElementById('password').value;
    setMessage(authMessage, '');
    lastAuthAction = 'login';

    if (!window.Android || typeof window.Android.login !== 'function') {
      setMessage(authMessage, 'El servicio de autenticación requiere ejecutar la app Android.', 'error');
      return;
    }
    Android.login(email, password);
  });

  if (signupButton) signupButton.addEventListener('click', () => {
    const name = document.getElementById('signupName').value.trim();
    const email = document.getElementById('signupEmail').value.trim();
    const password = document.getElementById('signupPassword').value;
    setMessage(signupMessage, '');
    lastAuthAction = 'register';

    if (!window.Android || typeof window.Android.registerCitizen !== 'function') {
      setMessage(signupMessage, 'El servicio de registro requiere ejecutar la app Android.', 'error');
      return;
    }
    Android.registerCitizen(name, email, password);
  });

  document.querySelectorAll('.species').forEach(button => {
    button.addEventListener('click', () => {
      document.querySelectorAll('.species').forEach(b => b.classList.remove('selected'));
      button.classList.add('selected');
    });
  });

  document.querySelectorAll('.direction').forEach(button => {
    button.addEventListener('click', () => {
      document.querySelectorAll('.direction').forEach(b => b.classList.remove('selected'));
      button.classList.add('selected');
    });
  });

  document.querySelectorAll('[data-sterilized]').forEach(button => {
    button.addEventListener('click', () => {
      document.querySelectorAll('[data-sterilized]').forEach(b => b.classList.remove('selected-choice'));
      button.classList.add('selected-choice');
    });
  });

  if (saveAnimalButton) saveAnimalButton.addEventListener('click', () => {
    setMessage(animalMessage, '');
    const selectedSpecies = document.querySelector('.species.selected');
    const selectedDirection = document.querySelector('.direction.selected');
    const selectedSterilized = document.querySelector('[data-sterilized].selected-choice');

    const payload = {
      name: document.getElementById('animalName').value.trim(),
      species: selectedSpecies ? selectedSpecies.dataset.species : '',
      breed: document.getElementById('animalBreed').value.trim(),
      color: document.getElementById('animalColor').value.trim(),
      sex: document.getElementById('animalSex').value,
      approximateAge: document.getElementById('animalAge').value.trim(),
      size: document.getElementById('animalSize').value,
      sterilized: selectedSterilized ? selectedSterilized.dataset.sterilized === 'true' : false,
      addressType: selectedDirection ? selectedDirection.dataset.direction : '',
      municipality: document.getElementById('animalMunicipality').value.trim(),
      neighborhoodOrVillage: document.getElementById('animalNeighborhoodOrVillage').value.trim(),
      addressOrLocation: document.getElementById('animalAddressOrLocation').value.trim(),
      referencePoint: document.getElementById('animalReferencePoint').value.trim()
    };

    if (!window.Android || typeof window.Android.saveAnimal !== 'function') {
      setMessage(animalMessage, 'El registro de animales requiere ejecutar la app Android.', 'error');
      return;
    }
    Android.saveAnimal(JSON.stringify(payload));
  });


  if (saveCaseButton) saveCaseButton.addEventListener('click', () => {
    setMessage(caseMessage, '');
    const payload = {
      type: currentReportType,
      description: document.getElementById('reportDescription').value.trim(),
      userPriority: document.getElementById('reportPriority').value,
      animalCount: Number(document.getElementById('reportAnimalCount').value || 1),
      animalCondition: document.getElementById('reportAnimalCondition').value,
      municipality: document.getElementById('reportMunicipality').value.trim(),
      neighborhoodOrVillage: document.getElementById('reportNeighborhoodOrVillage').value.trim(),
      addressOrLocation: document.getElementById('reportAddressOrLocation').value.trim(),
      referencePoint: document.getElementById('reportReferencePoint').value.trim(),
      responsibleKnown: document.getElementById('reportResponsibleKnown').value,
      suspectedResponsible: document.getElementById('reportSuspectedResponsible').value.trim()
    };

    if (!window.Android || typeof window.Android.saveCase !== 'function') {
      setMessage(caseMessage, 'El envío de reportes requiere ejecutar la app Android.', 'error');
      return;
    }
    Android.saveCase(JSON.stringify(payload));
  });




  const logout = () => {
    if (window.Android && typeof window.Android.logout === 'function') {
      Android.logout();
    } else {
      toast('Cerrar sesión requiere ejecutar la app Android.');
    }
  };

  if (logoutButton) logoutButton.addEventListener('click', logout);
  if (vetLogoutButton) vetLogoutButton.addEventListener('click', logout);
  if (adminLogoutButton) adminLogoutButton.addEventListener('click', logout);


  window.addEventListener('popstate', (event) => {
    const rawRequested = (event.state && event.state.screen) || location.hash.replace('#', '') || 'splash';
    const requested = canOpenScreen(rawRequested) ? rawRequested : 'login';
    screens.forEach(s => s.classList.toggle('active', s.dataset.screen === requested));
    if (requested !== rawRequested) {
      history.replaceState({ screen: requested }, '', `#${requested}`);
    }
  });

  const rawInitial = location.hash.replace('#', '') || 'splash';
  const initial = canOpenScreen(rawInitial) ? rawInitial : 'login';
  screens.forEach(s => s.classList.toggle('active', s.dataset.screen === initial));
})();
