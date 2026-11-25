# Incorporar el backend real sin alterar Android

El ZIP actual contiene el cliente Retrofit, no el servidor Spring Boot. No usar el ejemplo antiguo de puertos 8080/8081: el cliente actual espera todas las operaciones de eventos e historial en el puerto 8081. Recuperar el proyecto que ya se utilizó durante las pruebas H2.

Estructura sugerida si el equipo decide entregar todo junto:

```
Seguridapp/
  app/
  apk/
  herramientas/
  evidencias/
  microservicios/
    eventos-api/
      pom.xml
      src/
      mvnw (si existe)
      mvnw.cmd (si existe)
      .mvn/ (si existe)
  README.md
  gradlew.bat
```

Conservar el nombre real del proyecto si es diferente. No incluir `target/`, bases con datos personales, secretos ni credenciales. Si existen otros microservicios reales, incluir sus proyectos completos también. El repositorio separado también puede ser válido si se adjunta su código y se documenta su acceso según la pauta.

El nombre exacto, dependencias y estructura del backend real no están disponibles en el ZIP Android. No se inventaron ni reemplazaron. Confirmar que el servicio escucha en 8081, que sus DTO coinciden con `EventoRemoto`, `EventoSolicitud`, `SeguimientoRemoto` y `AccionEvento`, y que expone las ocho rutas documentadas en README.md.

Una vez incorporado, actualizar el README con su repositorio, directorio, comando Maven real, configuración de base de datos y forma de comprobar la persistencia. No afirmar que hay más de un microservicio si el equipo solo desarrolló uno.
