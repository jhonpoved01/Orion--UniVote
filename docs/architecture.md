# Arquitectura de UniVote

UniVote inicia como una aplicación Maven de un solo módulo sobre Java 25 y JavaFX 25.0.2. La interfaz se declara con FXML y su presentación se concentra en hojas CSS externas.

## Flujo previsto

```text
View → Controller → Service → DAO → JDBC → MySQL
```

El bootstrap actual solo contiene la aplicación, configuración no sensible y una vista estática. No contiene controladores funcionales, servicios, DAO, modelos de base de datos ni conexión JDBC.

## Responsabilidades futuras

- **View:** presentación FXML, componentes y validación visual.
- **Controller:** eventos de interfaz y coordinación con servicios.
- **Service:** reglas de negocio, validación y autorización.
- **DAO:** persistencia JDBC parametrizada y procedimientos almacenados.
- **Config:** carga y validación de configuración.
- **Security:** autenticación, sesión y permisos.

Las dependencias se construirán explícitamente sin framework de inyección. La aplicación permanecerá inicialmente sin `module-info.java` para mantener sencillo el uso de FXML.
