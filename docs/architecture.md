# Arquitectura de UniVote

UniVote inicia como una aplicación Maven de un solo módulo sobre Java 25 y JavaFX 25.0.2. La interfaz se declara con FXML y su presentación se concentra en hojas CSS externas.

## Flujo previsto

```text
View → Controller → Service → DAO → JDBC → MySQL
```

El bootstrap contiene la aplicación, configuración visual no sensible y una vista estática. La fase 2A añade configuración JDBC externa, una fábrica de conexiones por operación y un health check aislado. No contiene controladores funcionales, servicios, DAO ni modelos de base de datos.

## Infraestructura JDBC

`DatabaseConfigLoader` compone la configuración cuando una operación la solicita. La selección del archivo externo sigue este orden: propiedad JVM `univote.config.path`, variable `UNIVOTE_CONFIG_PATH` y, finalmente, `config/application-local.properties`.

Los valores de conexión aplican esta prioridad:

1. Variables de entorno `UNIVOTE_DB_*`.
2. Propiedades del archivo externo UTF-8.
3. Valores seguros no secretos definidos por la aplicación.

`DatabaseConnectionFactory` recibe una configuración inmutable y crea una conexión nueva mediante `DriverManager` por cada llamada. No conserva conexiones ni se ejecuta desde `UniVoteApplication`. `DatabaseHealthCheck` abre y cierra todos los recursos para ejecutar exclusivamente `SELECT 1`.

## Responsabilidades futuras

- **View:** presentación FXML, componentes y validación visual.
- **Controller:** eventos de interfaz y coordinación con servicios.
- **Service:** reglas de negocio, validación y autorización.
- **DAO:** persistencia JDBC parametrizada y procedimientos almacenados.
- **Config:** carga y validación de configuración.
- **Security:** autenticación, sesión y permisos.
- **Database:** creación acotada de conexiones y comprobación de disponibilidad.

Las dependencias se construirán explícitamente sin framework de inyección. La aplicación permanecerá inicialmente sin `module-info.java` para mantener sencillo el uso de FXML.
