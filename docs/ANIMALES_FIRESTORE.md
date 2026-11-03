# Animales en Firestore

## Alcance actual

El ciudadano autenticado puede registrar un animal y consultar únicamente los animales cuyo `mainResponsibleId` coincide con su UID de Firebase Authentication.

Colección:

```text
animals/{animalId}
```

Campos actuales:

- `animalId`
- `name`
- `species`
- `breed`
- `color`
- `sex`
- `approximateAge`
- `size`
- `sterilized`
- `addressType`
- `municipality`
- `neighborhoodOrVillage`
- `addressOrLocation`
- `referencePoint`
- `mainResponsibleId`
- `createdBy`
- `origin = citizen`
- `status = active`
- `createdAt`
- `updatedAt`

## Seguridad

El frontend no envía ni decide el UID del propietario. `FirebaseAnimalGateway` toma siempre `FirebaseAuth.currentUser.uid`.

Las reglas actuales permiten:

- crear un animal únicamente con `mainResponsibleId` y `createdBy` iguales al UID autenticado;
- leer únicamente animales propios;
- no editar ni borrar todavía.

La edición, transferencia de responsable, cuidadores adicionales, fotos, GPS, offline y eliminación requieren flujos específicos posteriores.

## Prueba manual

1. Publicar `firestore.rules` en Firebase Console.
2. Iniciar sesión con un ciudadano.
3. Registrar un animal desde la app.
4. Confirmar `animals/{animalId}` en Firestore.
5. Volver al portal y comprobar que aparece en “Mis animales registrados”.
6. Repetir en Android 7/API 24 y Android moderno con la misma variante `demoDebug`.
