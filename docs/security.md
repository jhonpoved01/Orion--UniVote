# Seguridad y privacidad de UniVote

## Modelo de amenazas básico

El MVP considera robo de credenciales, enumeración de usuarios, filtración de configuración, abuso de privilegios JDBC, doble voto, asociación entre identidad e intención, publicación anticipada y exposición de códigos o detalles SQL. No cubre compromiso del host, administrador MySQL malicioso, coacción del votante, ataques físicos ni anonimato criptográfico verificable.

## Secretos y configuración

- Los secretos no se empaquetan en recursos ni se registran.
- `config/application-local.properties` está ignorado y debe conservar permisos restrictivos.
- Variables `UNIVOTE_DB_*` prevalecen sobre el archivo externo.
- La contraseña se entrega mediante `Properties`, fuera de la URL JDBC, y se redacta en `toString()`.
- Se crea una conexión cerrable por operación; no existe conexión global.

## Autenticación y sesión

Los hashes se verifican con BCrypt 0.10.2 sin regeneración ni comparación manual. Usuario ausente, inactivo, hash inválido y contraseña incorrecta producen el mismo mensaje público. El `char[]` se limpia siempre. `AuthenticatedUser` y `UserSession` no contienen hash; la sesión vive solo en memoria y no es global ni persistida.

## Autorización

Los servicios usan `UserSession.hasPermission()` y no el nombre del rol. Las acciones de voto y resultados se protegen con `EMITIR_VOTO` y `CONSULTAR_RESULTADOS`. La UI oculta acciones no autorizadas, pero el servicio vuelve a validar.

## Procedimientos y privilegios mínimos

- `sp_registrar_voto`: única vía de escritura electoral.
- `sp_verificar_voto`: única vía para comprobar códigos.
- `sp_consultar_resultados`: única vía para obtener conteos publicados.

El usuario técnico necesita `SELECT` por columnas para autenticación/elecciones y `EXECUTE` sobre procedimientos. No necesita `INSERT`, `UPDATE`, `DELETE`, acceso administrativo ni lectura directa de `votos` o `control_votacion`, asumiendo procedimientos con seguridad de definidor.

## Separación identidad/intención

`control_votacion` impide el segundo voto mediante usuario+elección, mientras `votos` almacena candidatura y código sin usuario. El procedimiento los escribe en una transacción. `VoteReceipt` no conserva usuario/candidatura y `VoteVerification` no revela intención. No deben registrarse parámetros, sesiones, candidaturas ni códigos.

## Resultados

Aunque el procedimiento permite consultar una elección existente, `ElectionResultsService` bloquea resultados salvo estado `FINALIZADA` y fecha de cierre alcanzada. La autorización requiere `CONSULTAR_RESULTADOS`; los porcentajes usan `BigDecimal`.

## Manejo de errores y concurrencia

Los DAO conservan `SQLException` como causa interna bajo mensajes seguros. JavaFX nunca muestra SQL, host, URL o causas MySQL. Un executor daemon serializa tareas JDBC, se cierra al terminar y evita dobles envíos desde login/voto.

## Riesgos residuales

- La privacidad depende también de privilegios, configuración MySQL y controles operativos.
- `sslMode=PREFERRED` puede degradar el cifrado; entornos no locales deben usar verificación TLS adecuada.
- El portapapeles conserva el código hasta ser reemplazado por el sistema o usuario.
- Sin rate limiting, MFA, rotación integrada, auditoría segura avanzada o protección del endpoint físico.
- Los datos demostrativos del SQL deben considerarse públicos y no reutilizarse.

## Limitaciones

Este MVP no garantiza anonimato criptográfico, secreto frente a un administrador de base comprometido ni idoneidad para elecciones nacionales/alta criticidad. Requiere evaluación institucional, pruebas operativas y endurecimiento antes de producción.
