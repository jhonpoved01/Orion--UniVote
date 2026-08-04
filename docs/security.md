# Base de seguridad de UniVote

## Estado del bootstrap

Esta fase no se conecta a MySQL, no procesa credenciales y no implementa autenticación ni votación. `application.properties` contiene exclusivamente metadatos visuales no sensibles.

## Configuración

- Los secretos nunca se incluirán en `src/main/resources` porque quedarían empaquetados.
- La futura configuración local residirá en `config/application-local.properties`, ignorado por Git.
- `config/application.example.properties` contiene marcadores y documenta las claves esperadas.
- Las variables de entorno podrán sobrescribir secretos en una fase posterior.

## Reglas para las siguientes fases

- Usar `PreparedStatement` y `CallableStatement` con `try-with-resources`.
- Emitir votos únicamente mediante `sp_registrar_voto`.
- No registrar contraseñas, hashes, códigos de voto ni la relación votante-candidatura.
- Autorizar operaciones en Service, además de adaptar la navegación visual.
- Mostrar errores seguros sin detalles internos de SQL o del entorno.
- Aplicar mínimo privilegio al usuario MySQL de la aplicación.
