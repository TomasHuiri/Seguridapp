# Evidencias reales para DSY1105 — Evaluación Parcial 4, EA3

No presentar esta guía como evidencia de ejecución. Solo completar con capturas y resultados reales. No cambiar el repositorio después de la fecha límite de entrega.

## IE 3.1.2 y 3.1.3 — Backend y comunicación (30%)

- Incluir el código fuente del backend que el equipo utiliza realmente (no el antiguo ejemplo de dos servicios si no coincide con el cliente actual). Registrar enlace del repositorio, commit, estructura, versión Java, dependencias, comando de inicio, puerto, configuración de H2/otra base y rutas reales.
- En el PC de demostración abrir PowerShell: `Invoke-RestMethod http://localhost:8081/api/eventos`. Si no responde, iniciar el backend; no intentar resolverlo reescribiendo Android.
- Mostrar ambos servicios si existen, sus controladores y base de datos. Comprobar que un registro permanece al reiniciar el servidor.
- Instalar/abrir la app en el emulador, iniciar sesión con el residente de prueba, registrar un incidente ficticio **sin fotografía**, mostrarlo en el listado, detalle y GET del backend; guardar su ID.
- Desde otro perfil, agregar observación; con supervisor/administrador, asignar a unidad simulada y avanzar PENDIENTE → EN_PROCESO → CERRADO. Ver historial y comprobar el JSON. Registrar otro caso para demostrar cancelación con motivo antes de cerrarlo. La cancelación no equivale automáticamente a DELETE; confirmar aceptación docente o implementar eliminación explícita si es requisito estricto y aún se permite modificar.
- Comprobar API externa en pantalla de publicaciones, carga y manejo de errores.

## IE 3.2.1 — Pruebas y cobertura (15%)

1. Copiar `herramientas/` a la raíz del repositorio Android.
2. Ejecutar `powershell -ExecutionPolicy Bypass -File .\herramientas\GENERAR_EVIDENCIAS.ps1` en la raíz (solo ejecutar scripts revisados).
3. Revisar `evidencias/generadas/resumen.txt`, `cobertura_por_paquete.csv`, `pruebas.csv`, `jacoco-report.xml` y el HTML de JaCoCo. Abrir también `app/build/reports/tests/testDebugUnitTest/index.html`.
4. Guardar capturas de BUILD SUCCESSFUL, resultados de tests y cobertura. Registrar alcance exacto (repository, viewmodel, data/model) y cobertura global. No seleccionar solo los paquetes con mejor resultado.
5. Si está bajo 80%, usar `clases_por_lineas_no_cubiertas.csv` para decidir qué pruebas faltan. No inventar ni redondear hacia arriba para acreditar el umbral. El informe mide líneas; la pauta habla de código lógico y el criterio de medición debe poder explicarse al docente.

## IE 3.3.1 — APK firmada (10%)

- Capturar los archivos `apk/Seguridapp-release.apk` y `app/seguridapp.jks` en el explorador o Android Studio, sin exponer contraseñas.
- En PowerShell, ubicar `apksigner.bat` en Android SDK Build Tools y ejecutar con su ruta real: `& 'RUTA_SDK\build-tools\VERSION\apksigner.bat' verify --verbose --print-certs .\apk\Seguridapp-release.apk`.
- Abrir un emulador compatible. Instalar con `adb install -r .\apk\Seguridapp-release.apk` (o desde Android Studio, arrastrando el APK al emulador). Si aparece INSTALL_FAILED_UPDATE_INCOMPATIBLE, la instalación anterior tiene otra firma: desinstalar solo con autorización y teniendo en cuenta que elimina los datos locales de prueba. Verificar login, creación sin foto y API externa con backend activo.
- Registrar captura de verificación y ejecución. No subir la salida de herramientas que muestre secretos.

## IE 3.3.2 — GitHub/Trello (15%)

- En el repositorio real (no ZIP de GitHub), ejecutar `git shortlog -sn --all` y `git log --all --format="%h %ad %an %s" --date=short`. Capturar el historial de ambos integrantes, incluyendo el commit de firma y los del backend. La cantidad de commits por sí sola no demuestra colaboración efectiva.
- Abrir GitHub en modo de lectura y capturar Commits, Contributors y enlaces a repositorios del código móvil y backend; comprobar que el docente tiene acceso.
- Capturar el tablero Trello real con tareas existentes, responsables, fechas y estados. No recrear retrospectivamente actividad como si hubiera ocurrido antes. Si alguna tarea no quedó registrada, explicar esa limitación.
- Añadir al README enlaces y capturas reales, sin publicar datos personales ni credenciales.

## Antes de entregar

- Código móvil y código fuente backend, README completo, APK y JKS académico, resultados de tests y cobertura, evidencia de Trello y GitHub. Verificar entrega grupal AVA y activación individual según pauta.
- No modificar el repositorio después del cierre: la pauta sanciona los cambios tardíos con 1,0.
