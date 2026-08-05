# Arquitectura de UniVote

UniVote es una aplicación Maven de un módulo sobre Java 25 y JavaFX 25.0.2. Usa MVC con capas Service y DAO, FXML/CSS para presentación y JDBC directo para MySQL.

## Capas reales

```text
JavaFX/FXML → Controller → Service → DAO → DatabaseConnectionFactory → MySQL
```

- **View:** seis vistas FXML y una hoja de tema compartida.
- **Controller:** eventos, estado visual, creación de `Task` y navegación; no contiene SQL.
- **Service:** autenticación, permisos, disponibilidad electoral, validación de candidatura y publicación de resultados.
- **DAO:** consultas parametrizadas, mapeo y `CallableStatement`.
- **Model/Security:** records inmutables para usuario, sesión, elecciones, candidaturas y comprobantes.
- **Config/Database:** carga externa, fábrica de conexiones por operación y health check.

## Composición y navegación

`UniVoteApplication` carga `AppConfig`, crea `ApplicationServices`, un único executor daemon y `SceneNavigator`. `ApplicationServices` construye una fábrica JDBC compartida y servicios explícitos; construirlos no abre conexiones. `SceneNavigator` reutiliza el `Stage` y la `Scene`, carga recursos por classpath e inyecta controladores mediante `controllerFactory`. No existe Service Locator, Singleton ni estado global.

## Flujos

### Autenticación

El login crea un `Task`; `AuthenticationService` normaliza el identificador, consulta usuario/rol/permisos en una operación, verifica BCrypt y devuelve `UserSession`. Los fallos de credenciales comparten un mensaje genérico y el arreglo de contraseña se limpia en `finally`.

### Flujo electoral

`VotingService` exige `EMITIR_VOTO`, revalida elección activa y candidatura, y llama una sola vez a `JdbcVoteDao`. El DAO usa exclusivamente `sp_registrar_voto`; el comprobante resultante no contiene usuario ni candidatura.

### Verificación

Una sesión autenticada puede comprobar un código hexadecimal de 64 caracteres. `JdbcVoteVerificationDao` usa exclusivamente `sp_verificar_voto`; el modelo expone únicamente que el registro existe y su fecha.

### Resultados

`ElectionResultsService` exige `CONSULTAR_RESULTADOS`, consulta primero la elección y solo llama a `sp_consultar_resultados` cuando el estado es `FINALIZADA` y `fecha_fin` ya pasó. Los porcentajes se mantienen como `BigDecimal`.

## Concurrencia

Existe un solo `ExecutorService` de un hilo daemon. Login, lecturas y procedimientos JDBC se ejecutan en `Task`; sus handlers actualizan JavaFX en el hilo de UI. El executor se cierra en `Application.stop()`. No hay esperas activas, reintentos automáticos ni un executor por controlador.

## Persistencia y privacidad

Cada DAO abre y cierra una conexión con try-with-resources. `votos` conserva candidatura y comprobante; `control_votacion` conserva usuario y elección. Java no consulta directamente esas tablas: registrar, verificar y consultar resultados pasa por procedimientos almacenados.

## Límites del MVP

No incluye administración, recuperación de contraseña, persistencia de sesiones, notificaciones completas, auditoría avanzada, exportación, migraciones ni despliegue. Es un MVP académico/demostrativo, no una plataforma certificada para elecciones de alta criticidad.
