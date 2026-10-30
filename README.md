# Segurity App

Aplicación móvil Android (Kotlin + Jetpack Compose) para registrar, consultar y dar seguimiento a situaciones de seguridad comunal.
MVP académico de la asignatura DSY1105 – Desarrollo de Aplicaciones Móviles (Duoc UC). Caso: Municipalidad de San Joaquín.
Todos los datos usados son ficticios.

## Integrantes

- Tomás Antonio Huiriqueo Godoy
- Maximiliano Antonio Mora Castro

## Funcionalidades implementadas

- Inicio, login y registro con validación por campo (ícono y mensaje de error) y sesión persistente con DataStore.
- Dashboard con resumen de eventos por estado y eventos recientes.
- Lista de eventos con búsqueda y filtros por estado, categoría y prioridad.
- Creación de eventos con ubicación referencial, ubicación por GPS, selector de ubicación en Google Maps y evidencia con cámara (opcional).
- Detalle del evento con información, historial de seguimiento, observaciones, derivación a un responsable, cambio de estado y cancelación con motivo.
- Perfiles de usuario: residente, funcionario, supervisor y administrador, cada uno con permisos distintos. El administrador gestiona los roles de los usuarios.
- Persistencia local con Room (usuarios, eventos y seguimiento) y DataStore (sesión).
- Animaciones: entrada del logo, transiciones entre pantallas, mensajes de error, chips de estado, contadores y botones con indicador de carga.
- Recursos nativos: ubicación (permisos en tiempo de ejecución) y cámara (FileProvider).

## Arquitectura (MVVM)

- `model`: enums y reglas de negocio (estados, prioridades, categorías, roles y permisos).
- `data`: Room (entidades, DAOs, base de datos), DataStore y repositorios.
- `viewmodel`: estado de cada pantalla con StateFlow y validaciones.
- `ui`: tema, componentes reutilizables, navegación y pantallas en Compose.
- `util`: validadores, formato de fechas y acceso a recursos nativos.

## Cómo ejecutar

1. Clonar el repositorio y abrirlo en Android Studio.
2. Sincronizar Gradle y ejecutar en un emulador o dispositivo Android.
3. Para ver el mapa de Google, agregar en el archivo `local.properties` (no se sube al repositorio) la línea `MAPS_API_KEY=su_clave`.
   Sin la clave la aplicación funciona igual, pero el mapa se muestra en gris.
4. Cuentas de prueba (contraseña `Segurity123`): `usuario@segurityapp.cl`, `funcionario@segurityapp.cl`, `supervisor@segurityapp.cl` y `admin@segurityapp.cl`.

## Datos y restricciones del caso

La aplicación usa solo datos ficticios. No se capturan datos personales reales ni se conecta a sistemas municipales.
