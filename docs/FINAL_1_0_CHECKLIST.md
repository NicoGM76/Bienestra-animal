# Checklist de entrega — Bienestar Animal Zipaquirá 1.0

## Código y UX
- [x] Rama `final/1.0` separada de la APK estable de pre-sustentación.
- [x] Dashboard ciudadano segmentado.
- [x] Tarjetas en lugar de recuadros genéricos.
- [x] Flujo ciudadano de tres pasos visible.
- [x] Diversidad de especies destacada.
- [x] Icono de la aplicación visible en portales por rol.
- [x] Ficha/edición segura del animal.
- [x] Adopciones simuladas e interactivas.
- [x] Evidencia fotográfica con Firebase Storage.
- [x] Galería de evidencias para Admin y Veterinario.
- [x] Modo offline con caché Firestore y estado de conectividad.
- [x] FCM registrado en Android.
- [x] Cloud Function para push.
- [x] Cloud Function para auditoría.
- [x] Auditoría reciente visible en Admin.

## Firebase
- [ ] Publicar `firestore.rules`.
- [ ] Publicar `storage.rules`.
- [ ] Confirmar Storage habilitado en el proyecto Firebase.
- [ ] Confirmar Cloud Messaging habilitado.
- [ ] Instalar dependencias de `functions/`.
- [ ] Desplegar Cloud Functions.
- [ ] Validar al menos un push real en dispositivo/emulador compatible.

## Pruebas
- [ ] CI `Final 1.0 CI` en verde.
- [ ] Smoke test API 26 en verde.
- [ ] Smoke test API 33 en verde.
- [ ] Smoke test API 35 en verde.
- [ ] Login/logout probado en los tres roles.
- [ ] Registro y edición de animal probados.
- [ ] Reporte online probado.
- [ ] Reporte con pérdida temporal de conexión probado.
- [ ] Sincronización posterior probada.
- [ ] Foto de evidencia subida y visible en Admin/Vet.
- [ ] Asignación/reasignación probada.
- [ ] IA Beta + prioridad final probadas.
- [ ] Historial clínico con dos entradas probado.
- [ ] Notificación interna probada.
- [ ] Auditoría generada tras cambios de caso.

## Release
- [ ] Corregir cualquier fallo de CI.
- [ ] Cambiar `1.0.0-rc1` a `1.0.0` solo cuando los checks estén verdes.
- [ ] Generar APK final.
- [ ] Instalar APK desde cero en un dispositivo limpio.
- [ ] Hacer recorrido completo Ciudadano → Admin → Veterinario.
- [ ] Merge de `final/1.0` a `main`.
- [ ] Crear tag/release `v1.0.0`.

## Presentación
- [ ] Portada azul.
- [ ] Quitar referencia “PI”.
- [ ] Máximo de 23 palabras por bloque/diapositiva según feedback docente.
- [ ] Icono de la app en la parte inferior de cada usuario/rol.
- [ ] Explicar claramente el sistema ágil del ciudadano.
- [ ] Mostrar tablero de control, segmentos y tarjetas.
- [ ] Destacar gama de colores y variedad de mascotas.
- [ ] Mostrar adopciones como simulación, no backend productivo.
- [ ] Mostrar offline como requisito implementado y explicar sus límites reales.

## Documento final
- [ ] Usar la plantilla final suministrada.
- [ ] Resumen / Abstract / Palabras clave / Keywords.
- [ ] Introducción.
- [ ] Método.
- [ ] Desarrollo.
- [ ] Resultados y pruebas.
- [ ] Discusión.
- [ ] Conclusiones.
- [ ] Referencias.
- [ ] Figuras/capturas finales.
