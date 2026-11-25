# Seguridapp — Segurity App

Aplicación móvil Android para registrar, consultar y dar seguimiento a situaciones de seguridad comunal. MVP académico de DSY1105 — Desarrollo de Aplicaciones Móviles, Duoc UC, caso Municipalidad de San Joaquín. Solo se utilizan datos sintéticos; no existe integración con sistemas municipales reales.

## Integrantes

- Tomás Antonio Huiriqueo Godoy
- Maximiliano Antonio Mora Castro

## Funcionalidades

- Inicio de sesión y registro con validaciones, sesión persistente y cuatro perfiles (residente, funcionario, supervisor y administrador).
- Dashboard, búsqueda y filtros por estado, categoría y prioridad.
- Registro de eventos con ubicación referencial y coordenadas opcionales (GPS o mapa).
- Consulta de detalles, observaciones, historial, derivación a una unidad simulada, avance de estado y cancelación con motivo, según permisos del perfil.
- Administración de roles de usuarios y consulta de publicaciones de JSONPlaceholder.
- La cámara se implementó como recurso nativo, pero el servidor actual no admite fotografías: para registrar un evento debe quedar sin foto. La cancelación conserva el historial; no existe una operación HTTP DELETE en el cliente actual.

## Arquitectura

Kotlin, Jetpack Compose, MVVM, StateFlow, Retrofit/Gson, Room y DataStore. `data/model` contiene DTO y reglas de dominio; `repository` coordina datos y permisos; `viewmodel` administra el estado; `ui` contiene navegación y pantallas. Los usuarios se persisten en Room y la sesión en DataStore. Aunque Room conserva definiciones de eventos y seguimientos, el flujo actual de eventos consulta el servidor mediante Retrofit y mantiene una caché en memoria. La API externa usa un cliente independiente.

## Endpoints consumidos por Android

Backend de eventos: `http://10.0.2.2:8081/` (dirección del host vista desde el emulador Android estándar). El servicio debe estar escuchando en el equipo anfitrión en el puerto 8081 y satisfacer este contrato.

| Método | Ruta | Finalidad |
|---|---|---|
| GET | `/api/eventos` | Listar eventos |
| GET | `/api/eventos/{id}` | Obtener detalle |
| GET | `/api/eventos/{id}/seguimientos` | Consultar historial |
| POST | `/api/eventos` | Registrar evento |
| POST | `/api/eventos/{id}/observaciones` | Añadir observación |
| PATCH | `/api/eventos/{id}/avanzar` | Avanzar estado |
| PATCH | `/api/eventos/{id}/asignar` | Derivar a unidad responsable |
| PATCH | `/api/eventos/{id}/cancelar` | Cancelar con motivo |

API externa: `https://jsonplaceholder.typicode.com/posts` (`GET`). Se muestra en la pantalla de publicaciones mediante Retrofit.

## Ejecución

1. Clonar el repositorio de la aplicación: `https://github.com/TomasHuiri/Seguridapp`.
2. Abrir la raíz del proyecto en Android Studio, sincronizar Gradle y utilizar un SDK compatible con la configuración del proyecto (compileSdk/targetSdk 37, minSdk 24, AGP 9.4.1 y wrapper Gradle 9.6.0 según los archivos entregados).
3. Iniciar el backend real del equipo siguiendo las instrucciones de su repositorio. Si se incluye junto al proyecto Android, usar `microservicios/eventos-api/` (con su propio `pom.xml`, `src/` y wrapper Maven si existe). No copiar ese código dentro de `app/`. **Pendiente de completar antes de la entrega:** enlace al repositorio del backend, directorios, comando de inicio y configuración de la base de datos. El ZIP Android por sí solo no contiene los servidores.
4. Confirmar desde PowerShell: `Invoke-RestMethod http://localhost:8081/api/eventos`. Después iniciar el emulador Android y ejecutar la aplicación. En un teléfono físico, `10.0.2.2` no funciona como dirección del PC; sería necesario configurar otra URL y recompilar.
5. Opcionalmente, agregar `MAPS_API_KEY=su_clave` al archivo `local.properties` (no versionar). Sin una clave válida el mapa puede no mostrar cartografía. No subir credenciales personales.
6. Para la demostración utilizar cuentas ficticias: `usuario@segurityapp.cl`, `funcionario@segurityapp.cl`, `supervisor@segurityapp.cl` y `admin@segurityapp.cl`, todas con contraseña de prueba `Segurity123`.

## Pruebas y cobertura

JUnit, Kotest, MockK y kotlinx-coroutines-test. El proyecto activa cobertura de pruebas unitarias de debug. Desde PowerShell en la raíz:

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:createDebugUnitTestCoverageReport --rerun-tasks
```

Abrir `app/build/reports/tests/testDebugUnitTest/index.html` y `app/build/reports/coverage/test/debug/index.html`. El script `herramientas/GENERAR_EVIDENCIAS.ps1` automatiza la ejecución y calcula cobertura de líneas combinada de todos los paquetes `repository`, `viewmodel` y `data/model` mediante los contadores de JaCoCo, sin promediar porcentajes ni eliminar clases sin pruebas. Se conserva además el informe global. La meta de la pauta es 80% de lógica; el resultado actual debe comprobarse ejecutando las pruebas, no por el número de tests escritos.

## APK firmada

- APK entregada: `apk/Seguridapp-release.apk`.
- Almacén de claves académico: `app/seguridapp.jks`.
- Configuración: `app/build.gradle.kts`, `signingConfigs.release`.
- Para verificar la APK, usar `apksigner verify --verbose --print-certs` con Android SDK Build Tools. Para regenerarla, usar Android Studio > Build > Generate Signed Bundle / APK, o `:app:assembleRelease` con la configuración válida.
- No publicar contraseñas ni utilizar este almacén de claves para una aplicación productiva. La configuración original contiene credenciales de firma en texto plano: se recomienda migrarlas a propiedades locales y usar una clave distinta para cualquier publicación real.
- Pendiente de incorporar captura de la APK firmada y del archivo JKS (sin mostrar contraseñas).

Para revisar la firma y consultar el endpoint local sin modificar sus datos, ejecutar `powershell -ExecutionPolicy Bypass -File .\herramientas\VERIFICAR_ENTREGA.ps1`. El resultado se guarda en `evidencias/generadas/verificacion-entrega.txt`.

## Trabajo colaborativo y evidencias

Repositorio Android: https://github.com/TomasHuiri/Seguridapp

Pendiente de completar: enlace al tablero Trello real, capturas de tareas y responsables, repositorio de microservicios y resultados actuales de pruebas, cobertura y ejecución de la APK. Consultar `evidencias/GUIA_EVIDENCIAS.md`.

## Limitaciones del MVP

La red usa HTTP local para el backend de prueba y la autenticación/permisos se implementan del lado Android; no se acredita autenticación ni autorización del servidor sin revisar su código. No usar datos personales, fotografías reales ni credenciales municipales. Este MVP no está preparado para producción.
