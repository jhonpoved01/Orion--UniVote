# Base de seguridad de UniVote

## Estado del bootstrap

La infraestructura JDBC existe, pero no se conecta automáticamente a MySQL y no implementa autenticación ni votación. `application.properties` contiene exclusivamente metadatos visuales no sensibles.

## Configuración

- Los secretos nunca se incluirán en `src/main/resources` porque quedarían empaquetados.
- La configuración local reside en `config/application-local.properties`, ignorado por Git.
- `config/application.example.properties` contiene marcadores y documenta las claves esperadas.
- Las variables de entorno `UNIVOTE_DB_*` sobrescriben el archivo local.
- La propiedad JVM `univote.config.path` tiene prioridad sobre `UNIVOTE_CONFIG_PATH` para seleccionar otro archivo.
- La contraseña se entrega a `DriverManager` mediante `Properties`; nunca forma parte de la URL JDBC ni de `toString`.
- Cada operación obtiene su propia conexión cerrable. No existe conexión estática, singleton ni pool.
- SSL queda en modo `PREFERRED` por defecto y solo admite modos reconocidos por Connector/J.

## Reglas para las siguientes fases

- Usar `PreparedStatement` y `CallableStatement` con `try-with-resources`.
- Emitir votos únicamente mediante `sp_registrar_voto`.
- No registrar contraseñas, hashes, códigos de voto ni la relación votante-candidatura.
- Autorizar operaciones en Service, además de adaptar la navegación visual.
- Mostrar errores seguros sin detalles internos de SQL o del entorno.
- Aplicar mínimo privilegio al usuario MySQL de la aplicación.
- No habilitar `allowPublicKeyRetrieval` ni desactivar SSL automáticamente.
