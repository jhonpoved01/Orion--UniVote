/*==============================================================*/
/* PROYECTO : Plataforma de Votación Universitaria Digital       */
/* BASE     : votacion_universitaria                            */
/* MOTOR    : MySQL 8.x                                         */
/* LENGUAJE : Java + JDBC                                       */
/*==============================================================*/

DROP DATABASE IF EXISTS votacion_universitaria;

CREATE DATABASE votacion_universitaria
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE votacion_universitaria;


/*==============================================================*/
/* TABLA: roles                                                  */
/*==============================================================*/

CREATE TABLE roles
(
    id_rol INT AUTO_INCREMENT,

    nombre VARCHAR(50) NOT NULL,
    descripcion VARCHAR(255),
    estado BOOLEAN NOT NULL DEFAULT TRUE,
    
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

	fecha_actualizacion DATETIME NULL,

    PRIMARY KEY (id_rol),

    CONSTRAINT uq_roles_nombre UNIQUE (nombre),

    CONSTRAINT chk_roles_estado
        CHECK (estado IN (0,1))

) ENGINE=InnoDB
COMMENT='Almacena los diferentes roles del sistema';


/*==============================================================*/
/* TABLA: permisos                                               */
/*==============================================================*/

CREATE TABLE permisos
(
    id_permiso INT AUTO_INCREMENT,

    nombre VARCHAR(80) NOT NULL,
    descripcion VARCHAR(255),
    estado BOOLEAN NOT NULL DEFAULT TRUE,
    
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

	fecha_actualizacion DATETIME NULL,

    PRIMARY KEY (id_permiso),

    CONSTRAINT uq_permisos_nombre UNIQUE (nombre),

    CONSTRAINT chk_permisos_estado
        CHECK (estado IN (0,1))

) ENGINE=InnoDB
COMMENT='Permisos disponibles dentro del sistema';


/*==============================================================*/
/* TABLA: roles_permisos                                         */
/*==============================================================*/

CREATE TABLE roles_permisos
(
    id_rol_permiso INT AUTO_INCREMENT,

    id_rol INT NOT NULL,
    id_permiso INT NOT NULL,

    PRIMARY KEY (id_rol_permiso),

    CONSTRAINT uq_roles_permisos
        UNIQUE(id_rol,id_permiso),

    CONSTRAINT fk_roles_permisos_roles
        FOREIGN KEY (id_rol)
        REFERENCES roles(id_rol)
        ON UPDATE CASCADE
        ON DELETE CASCADE,

    CONSTRAINT fk_roles_permisos_permisos
        FOREIGN KEY (id_permiso)
        REFERENCES permisos(id_permiso)
        ON UPDATE CASCADE
        ON DELETE CASCADE

) ENGINE=InnoDB
COMMENT='Relaciona los roles con los permisos del sistema';


/*==============================================================*/
/* TABLA: usuarios                                               */
/*==============================================================*/

CREATE TABLE usuarios
(
    id_usuario INT AUTO_INCREMENT,

    id_rol INT NOT NULL,

    documento VARCHAR(20) NOT NULL,

    nombres VARCHAR(100) NOT NULL,

    apellidos VARCHAR(100) NOT NULL,

    correo VARCHAR(120) NOT NULL,

    password_hash VARCHAR(255) NOT NULL,

    telefono VARCHAR(20),

    estado BOOLEAN NOT NULL DEFAULT TRUE,

    fecha_registro DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

	fecha_actualizacion DATETIME NULL,

    PRIMARY KEY (id_usuario),

    CONSTRAINT uq_usuario_documento
        UNIQUE(documento),

    CONSTRAINT uq_usuario_correo
        UNIQUE(correo),

    CONSTRAINT chk_usuario_estado
        CHECK (estado IN (0,1)),

    CONSTRAINT fk_usuario_rol
        FOREIGN KEY (id_rol)
        REFERENCES roles(id_rol)
        ON UPDATE CASCADE
        ON DELETE RESTRICT

) ENGINE=InnoDB
COMMENT='Usuarios registrados dentro de la plataforma';


/*==============================================================*/
/* ÍNDICES                                                       */
/*==============================================================*/

CREATE INDEX idx_usuario_nombre
ON usuarios(nombres);

CREATE INDEX idx_usuario_apellido
ON usuarios(apellidos);

CREATE INDEX idx_usuario_estado
ON usuarios(estado);


/*==============================================================*/
/* TABLA: elecciones                                             */
/*==============================================================*/

CREATE TABLE elecciones
(
    id_eleccion INT AUTO_INCREMENT,

    nombre_eleccion VARCHAR(120) NOT NULL,

    descripcion_eleccion TEXT,

    fecha_inicio DATETIME NOT NULL,

    fecha_fin DATETIME NOT NULL,

    estado VARCHAR(20) NOT NULL DEFAULT 'PROGRAMADA',
    
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

	fecha_actualizacion DATETIME NULL,

    PRIMARY KEY (id_eleccion),

    CONSTRAINT chk_fechas_eleccion
        CHECK (fecha_fin > fecha_inicio),

    CONSTRAINT chk_estado_eleccion
        CHECK (estado IN ('PROGRAMADA','ACTIVA','FINALIZADA','CANCELADA'))
        
        

) ENGINE=InnoDB
COMMENT='Procesos electorales registrados en la plataforma';


/*==============================================================*/
/* TABLA: candidatos                                             */
/*==============================================================*/

CREATE TABLE candidatos
(
    id_candidato INT AUTO_INCREMENT,

    id_usuario INT NOT NULL,

    titulo_propuesta VARCHAR(150) NOT NULL,
	
    propuesta TEXT NOT NULL,

    foto VARCHAR(255),

    estado BOOLEAN NOT NULL DEFAULT TRUE,
    
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

	fecha_actualizacion DATETIME NULL,

    PRIMARY KEY (id_candidato),

    CONSTRAINT uq_candidato_usuario
        UNIQUE(id_usuario),

    CONSTRAINT chk_candidato_estado
        CHECK (estado IN (0,1)),

    CONSTRAINT fk_candidato_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario)
        ON UPDATE CASCADE
        ON DELETE RESTRICT

) ENGINE=InnoDB
COMMENT='Usuarios inscritos como candidatos';


/*==============================================================*/
/* TABLA: candidaturas                                           */
/*==============================================================*/

CREATE TABLE candidaturas
(
    id_candidatura INT AUTO_INCREMENT,

    id_candidato INT NOT NULL,

    id_eleccion INT NOT NULL,

    numero_lista INT NOT NULL,

    estado BOOLEAN NOT NULL DEFAULT TRUE,
    
    fecha_inscripcion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id_candidatura),

    CONSTRAINT uq_candidatura
        UNIQUE(id_candidato,id_eleccion),

    CONSTRAINT uq_numero_lista
        UNIQUE(id_eleccion,numero_lista),

    CONSTRAINT chk_numero_lista
        CHECK (numero_lista > 0),

    CONSTRAINT chk_estado_candidatura
        CHECK (estado IN (0,1)),

    CONSTRAINT fk_candidatura_candidato
        FOREIGN KEY (id_candidato)
        REFERENCES candidatos(id_candidato)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_candidatura_eleccion
        FOREIGN KEY (id_eleccion)
        REFERENCES elecciones(id_eleccion)
        ON UPDATE CASCADE
        ON DELETE RESTRICT

) ENGINE=InnoDB
COMMENT='Relaciona candidatos con procesos electorales';


/*==============================================================*/
/* TABLA: votos                                                  */
/*==============================================================*/

CREATE TABLE votos
(
    id_voto INT AUTO_INCREMENT,

    id_candidatura INT NOT NULL,

    fecha_voto DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    codigo_verificacion CHAR(64) NOT NULL,

    PRIMARY KEY (id_voto),

    CONSTRAINT uq_codigo_verificacion
        UNIQUE(codigo_verificacion),

    CONSTRAINT fk_voto_candidatura
        FOREIGN KEY (id_candidatura)
        REFERENCES candidaturas(id_candidatura)
        ON UPDATE CASCADE
        ON DELETE RESTRICT

) ENGINE=InnoDB
COMMENT='Almacena los votos de manera anónima';


/*==============================================================*/
/* TABLA: control_votacion                                       */
/*==============================================================*/

CREATE TABLE control_votacion
(
    id_control INT AUTO_INCREMENT,

    id_usuario INT NOT NULL,

    id_eleccion INT NOT NULL,

    fecha_voto DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id_control),

    CONSTRAINT uq_voto_unico
        UNIQUE(id_usuario,id_eleccion),

    CONSTRAINT fk_control_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario)
        ON UPDATE CASCADE
        ON DELETE RESTRICT,

    CONSTRAINT fk_control_eleccion
        FOREIGN KEY (id_eleccion)
        REFERENCES elecciones(id_eleccion)
        ON UPDATE CASCADE
        ON DELETE RESTRICT

) ENGINE=InnoDB
COMMENT='Garantiza que un estudiante solo vote una vez por elección';


/*==============================================================*/
/* TABLA: sesiones                                               */
/*==============================================================*/

CREATE TABLE sesiones
(
    id_sesion INT AUTO_INCREMENT,

    id_usuario INT NOT NULL,

    fecha_inicio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    fecha_fin DATETIME NULL,

    direccion_ip VARCHAR(45) NOT NULL,

    dispositivo VARCHAR(150),

    navegador VARCHAR(100),

    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVA',

    PRIMARY KEY (id_sesion),

    CONSTRAINT chk_estado_sesion
        CHECK (estado IN ('ACTIVA','CERRADA','EXPIRADA')),

    CONSTRAINT fk_sesion_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario)
        ON UPDATE CASCADE
        ON DELETE RESTRICT

) ENGINE=InnoDB
COMMENT='Registro de sesiones iniciadas por los usuarios';


/*==============================================================*/
/* TABLA: auditorias                                             */
/*==============================================================*/

CREATE TABLE auditorias
(
    id_auditoria INT AUTO_INCREMENT,

    id_usuario INT NOT NULL,

    accion VARCHAR(120) NOT NULL,

    descripcion TEXT NOT NULL,

    fecha_hora DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    direccion_ip VARCHAR(45),

    PRIMARY KEY (id_auditoria),

    CONSTRAINT fk_auditoria_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario)
        ON UPDATE CASCADE
        ON DELETE RESTRICT

) ENGINE=InnoDB
COMMENT='Registro histórico de acciones realizadas dentro del sistema';


/*==============================================================*/
/* TABLA: notificaciones                                         */
/*==============================================================*/

CREATE TABLE notificaciones
(
    id_notificacion INT AUTO_INCREMENT,

    id_usuario INT NOT NULL,

    titulo VARCHAR(150) NOT NULL,

    mensaje TEXT NOT NULL,

    fecha_envio DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    leida BOOLEAN NOT NULL DEFAULT FALSE,

    PRIMARY KEY (id_notificacion),

    CONSTRAINT chk_notificacion_leida
        CHECK (leida IN (0,1)),

    CONSTRAINT fk_notificacion_usuario
        FOREIGN KEY (id_usuario)
        REFERENCES usuarios(id_usuario)
        ON UPDATE CASCADE
        ON DELETE CASCADE

) ENGINE=InnoDB
COMMENT='Notificaciones enviadas a los usuarios del sistema';


/*==============================================================*/
/* TABLA: configuracion_sistema                                  */
/*==============================================================*/

CREATE TABLE configuracion_sistema
(
    id_configuracion INT AUTO_INCREMENT,

    nombre_parametro VARCHAR(100) NOT NULL,

    valor_parametro VARCHAR(255) NOT NULL,

    descripcion_parametro TEXT,
    
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

	fecha_actualizacion DATETIME NULL,

    PRIMARY KEY (id_configuracion),

    CONSTRAINT uq_nombre_parametro
        UNIQUE(nombre_parametro)

) ENGINE=InnoDB
COMMENT='Parámetros generales de configuración de la plataforma';


/*==============================================================*/
/* ÍNDICES                                                       */
/*==============================================================*/

CREATE INDEX idx_sesion_usuario
ON sesiones(id_usuario);

CREATE INDEX idx_sesion_estado
ON sesiones(estado);

CREATE INDEX idx_auditoria_usuario
ON auditorias(id_usuario);

CREATE INDEX idx_auditoria_fecha
ON auditorias(fecha_hora);

CREATE INDEX idx_notificacion_usuario
ON notificaciones(id_usuario);

CREATE INDEX idx_notificacion_leida
ON notificaciones(leida);


/*==============================================================*/
/* VISTA: vw_resultados                                         */
/*==============================================================*/

DROP VIEW IF EXISTS vw_resultados;

CREATE VIEW vw_resultados AS
SELECT
    e.id_eleccion,
    e.nombre_eleccion,
    e.estado AS estado_eleccion,
	ca.id_candidatura,
    ca.numero_lista,
	c.id_candidato,
    u.id_usuario,
	CONCAT_WS(' ', u.nombres, u.apellidos) AS nombre_candidato,
	c.titulo_propuesta,
	COUNT(v.id_voto) AS total_votos,
	COALESCE(
        ROUND(
            COUNT(v.id_voto) * 100.0
            / NULLIF(resumen.total_votos_eleccion, 0),
            2
		),
        0.00
    ) AS porcentaje_votos

FROM elecciones e

INNER JOIN candidaturas ca
    ON ca.id_eleccion = e.id_eleccion

INNER JOIN candidatos c
    ON c.id_candidato = ca.id_candidato

INNER JOIN usuarios u
    ON u.id_usuario = c.id_usuario

LEFT JOIN votos v
    ON v.id_candidatura = ca.id_candidatura

LEFT JOIN
(
    SELECT
        ca_total.id_eleccion,
        COUNT(v_total.id_voto) AS total_votos_eleccion

    FROM candidaturas ca_total

    LEFT JOIN votos v_total
        ON v_total.id_candidatura = ca_total.id_candidatura

    WHERE ca_total.estado = TRUE

    GROUP BY ca_total.id_eleccion

) AS resumen
    ON resumen.id_eleccion = e.id_eleccion

WHERE ca.estado = TRUE

GROUP BY
    e.id_eleccion,
    e.nombre_eleccion,
    e.estado,
    ca.id_candidatura,
    ca.numero_lista,
    c.id_candidato,
    u.id_usuario,
    u.nombres,
    u.apellidos,
    c.titulo_propuesta,
    resumen.total_votos_eleccion;
    
 
 /*
SELECT *
FROM vw_resultados;
*/

/*
SELECT *
FROM vw_resultados
WHERE id_eleccion = 1
ORDER BY total_votos DESC,
         numero_lista ASC;
*/

         
/*==============================================================*/
/* PROCEDIMIENTO: sp_registrar_voto                             */
/*==============================================================*/

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_registrar_voto$$

CREATE PROCEDURE sp_registrar_voto
(
    IN p_id_usuario INT,
    IN p_id_eleccion INT,
    IN p_id_candidatura INT,
    OUT p_codigo_verificacion CHAR(64)
)
BEGIN

    DECLARE v_usuario_valido INT DEFAULT 0;
    DECLARE v_eleccion_valida INT DEFAULT 0;
    DECLARE v_candidatura_valida INT DEFAULT 0;
    DECLARE v_usuario_ya_voto INT DEFAULT 0;

    /*
     * Si se produce cualquier error, se deshacen
     * todas las operaciones de la transacción.
     */
    DECLARE EXIT HANDLER FOR SQLEXCEPTION
    BEGIN
        ROLLBACK;
        RESIGNAL;
    END;

    START TRANSACTION;

    /*----------------------------------------------------------*/
    /* Validar que el usuario exista y esté activo              */
    /*----------------------------------------------------------*/

    SELECT COUNT(*)
    INTO v_usuario_valido
    FROM usuarios
    WHERE id_usuario = p_id_usuario
      AND estado = TRUE;

    IF v_usuario_valido = 0 THEN

        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT =
            'El usuario no existe o se encuentra inactivo';

    END IF;

    /*----------------------------------------------------------*/
    /* Validar que la elección esté disponible                  */
    /*----------------------------------------------------------*/

    SELECT COUNT(*)
    INTO v_eleccion_valida
    FROM elecciones
    WHERE id_eleccion = p_id_eleccion
      AND estado = 'ACTIVA'
      AND CURRENT_TIMESTAMP BETWEEN fecha_inicio AND fecha_fin;

    IF v_eleccion_valida = 0 THEN

        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT =
            'La elección no está activa o está fuera del periodo de votación';

    END IF;

    /*----------------------------------------------------------*/
    /* Validar candidatura, candidato y usuario asociado        */
    /*----------------------------------------------------------*/

    SELECT COUNT(*)
    INTO v_candidatura_valida
    FROM candidaturas ca

    INNER JOIN candidatos c
        ON c.id_candidato = ca.id_candidato

    INNER JOIN usuarios u
        ON u.id_usuario = c.id_usuario

    WHERE ca.id_candidatura = p_id_candidatura
      AND ca.id_eleccion = p_id_eleccion
      AND ca.estado = TRUE
      AND c.estado = TRUE
      AND u.estado = TRUE;

    IF v_candidatura_valida = 0 THEN

        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT =
            'La candidatura no pertenece a la elección o está inhabilitada';

    END IF;

    /*----------------------------------------------------------*/
    /* Verificar que el usuario no haya votado previamente      */
    /*----------------------------------------------------------*/

    SELECT COUNT(*)
    INTO v_usuario_ya_voto
    FROM control_votacion
    WHERE id_usuario = p_id_usuario
      AND id_eleccion = p_id_eleccion;

    IF v_usuario_ya_voto > 0 THEN

        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT =
            'El usuario ya registró su voto en esta elección';

    END IF;

    /*----------------------------------------------------------*/
    /* Registrar primero el control de participación            */
    /*----------------------------------------------------------*/

    INSERT INTO control_votacion
    (
        id_usuario,
        id_eleccion,
        fecha_voto
    )
    VALUES
    (
        p_id_usuario,
        p_id_eleccion,
        CURRENT_TIMESTAMP
    );

    /*----------------------------------------------------------*/
    /* Generar código de verificación anónimo                   */
    /*----------------------------------------------------------*/

    SET p_codigo_verificacion =
        LOWER(HEX(RANDOM_BYTES(32)));

    /*----------------------------------------------------------*/
    /* Registrar el voto                                        */
    /*----------------------------------------------------------*/

    INSERT INTO votos
    (
        id_candidatura,
        fecha_voto,
        codigo_verificacion
    )
    VALUES
    (
        p_id_candidatura,
        CURRENT_TIMESTAMP,
        p_codigo_verificacion
    );

    COMMIT;

END$$

DELIMITER ;


/*
SET @codigo_generado = NULL;

CALL sp_registrar_voto
(
    1,                   ID del usuario votante 
    1,                   ID de la elección 
    1,                   ID de la candidatura 
    @codigo_generado     Código devuelto 
);
*/

SELECT
    @codigo_generado AS codigo_verificacion;
    

/*==============================================================*/
/* PROCEDIMIENTO: sp_consultar_resultados                       */
/*==============================================================*/

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_consultar_resultados$$

CREATE PROCEDURE sp_consultar_resultados
(
    IN p_id_eleccion INT
)
BEGIN

    DECLARE v_eleccion_existe INT DEFAULT 0;

    SELECT COUNT(*)
    INTO v_eleccion_existe
    FROM elecciones
    WHERE id_eleccion = p_id_eleccion;

    IF v_eleccion_existe = 0 THEN

        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT =
            'La elección indicada no existe';

    END IF;

    SELECT
        id_eleccion,
        nombre_eleccion,
        estado_eleccion,
        id_candidatura,
        numero_lista,
        id_candidato,
        nombre_candidato,
        titulo_propuesta,
        total_votos,
        porcentaje_votos

    FROM vw_resultados

    WHERE id_eleccion = p_id_eleccion

    ORDER BY
        total_votos DESC,
        numero_lista ASC;

END$$

DELIMITER ;


/*==============================================================*/
/* PROCEDIMIENTO: sp_verificar_voto                             */
/*==============================================================*/

DELIMITER $$

DROP PROCEDURE IF EXISTS sp_verificar_voto$$

CREATE PROCEDURE sp_verificar_voto
(
    IN p_codigo_verificacion CHAR(64)
)
BEGIN

    DECLARE v_voto_existe INT DEFAULT 0;

    SELECT COUNT(*)
    INTO v_voto_existe
    FROM votos
    WHERE codigo_verificacion = p_codigo_verificacion;

    IF v_voto_existe = 0 THEN

        SIGNAL SQLSTATE '45000'
        SET MESSAGE_TEXT =
            'No existe un voto asociado al código proporcionado';

    END IF;

    SELECT
        'VOTO REGISTRADO' AS resultado,
        fecha_voto AS fecha_registro

    FROM votos

    WHERE codigo_verificacion = p_codigo_verificacion;

END$$

DELIMITER ;


/*==============================================================*/
/* MÓDULO 5: TRIGGERS Y PROTECCIÓN DE DATOS                     */
/*==============================================================*/

USE votacion_universitaria;

DELIMITER $$

/*==============================================================*/
/* TRIGGER: actualización de roles                              */
/*==============================================================*/

DROP TRIGGER IF EXISTS trg_roles_fecha_actualizacion$$

CREATE TRIGGER trg_roles_fecha_actualizacion
BEFORE UPDATE ON roles
FOR EACH ROW
BEGIN
    SET NEW.fecha_actualizacion = CURRENT_TIMESTAMP;
END$$


/*==============================================================*/
/* TRIGGER: actualización de permisos                           */
/*==============================================================*/

DROP TRIGGER IF EXISTS trg_permisos_fecha_actualizacion$$

CREATE TRIGGER trg_permisos_fecha_actualizacion
BEFORE UPDATE ON permisos
FOR EACH ROW
BEGIN
    SET NEW.fecha_actualizacion = CURRENT_TIMESTAMP;
END$$


/*==============================================================*/
/* TRIGGER: actualización de usuarios                           */
/*==============================================================*/

DROP TRIGGER IF EXISTS trg_usuarios_fecha_actualizacion$$

CREATE TRIGGER trg_usuarios_fecha_actualizacion
BEFORE UPDATE ON usuarios
FOR EACH ROW
BEGIN
    SET NEW.fecha_actualizacion = CURRENT_TIMESTAMP;
END$$


/*==============================================================*/
/* TRIGGER: actualización de elecciones                         */
/*==============================================================*/

DROP TRIGGER IF EXISTS trg_elecciones_fecha_actualizacion$$

CREATE TRIGGER trg_elecciones_fecha_actualizacion
BEFORE UPDATE ON elecciones
FOR EACH ROW
BEGIN
    SET NEW.fecha_actualizacion = CURRENT_TIMESTAMP;
END$$


/*==============================================================*/
/* TRIGGER: actualización de candidatos                         */
/*==============================================================*/

DROP TRIGGER IF EXISTS trg_candidatos_fecha_actualizacion$$

CREATE TRIGGER trg_candidatos_fecha_actualizacion
BEFORE UPDATE ON candidatos
FOR EACH ROW
BEGIN
    SET NEW.fecha_actualizacion = CURRENT_TIMESTAMP;
END$$


/*==============================================================*/
/* TRIGGER: actualización de configuración                      */
/*==============================================================*/

DROP TRIGGER IF EXISTS trg_configuracion_fecha_actualizacion$$

CREATE TRIGGER trg_configuracion_fecha_actualizacion
BEFORE UPDATE ON configuracion_sistema
FOR EACH ROW
BEGIN
    SET NEW.fecha_actualizacion = CURRENT_TIMESTAMP;
END$$


/*==============================================================*/
/* TRIGGER: impedir modificación de votos                       */
/*==============================================================*/

DROP TRIGGER IF EXISTS trg_votos_impedir_actualizacion$$

CREATE TRIGGER trg_votos_impedir_actualizacion
BEFORE UPDATE ON votos
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT =
        'Los votos registrados no pueden ser modificados';
END$$


/*==============================================================*/
/* TRIGGER: impedir eliminación de votos                        */
/*==============================================================*/

DROP TRIGGER IF EXISTS trg_votos_impedir_eliminacion$$

CREATE TRIGGER trg_votos_impedir_eliminacion
BEFORE DELETE ON votos
FOR EACH ROW
BEGIN
    SIGNAL SQLSTATE '45000'
    SET MESSAGE_TEXT =
        'Los votos registrados no pueden ser eliminados';
END$$

DELIMITER ;


/*
SELECT
    id_eleccion,
    nombre_eleccion,
    numero_lista,
    id_candidato,
    nombre_candidato,
    titulo_propuesta,
    total_votos,
    porcentaje_votos
FROM vw_resultados
WHERE id_eleccion = 1
	AND total_votos =
	(
		SELECT MAX(total_votos)
		FROM vw_resultados
		WHERE id_eleccion = 1
	);
*/
  

/*
  SELECT
    id_voto,
    id_candidatura,
    fecha_voto,
    codigo_verificacion
FROM votos;
*/


/*
SELECT
    id_control,
    id_usuario,
    id_eleccion,
    fecha_voto
FROM control_votacion;
*/


/*==============================================================*/
/* DATOS INICIALES: roles                                       */
/*==============================================================*/

INSERT INTO roles
(
    nombre,
    descripcion,
    estado
)
VALUES
(
    'ADMINISTRADOR',
    'Administra usuarios, elecciones, configuraciones y demás componentes del sistema',
    TRUE
),
(
    'ESTUDIANTE',
    'Usuario habilitado para participar en los procesos electorales',
    TRUE
),
(
    'CANDIDATO',
    'Usuario inscrito como candidato en uno o varios procesos electorales',
    TRUE
),
(
    'AUDITOR',
    'Usuario autorizado para revisar la trazabilidad y transparencia del sistema',
    TRUE
);


/*==============================================================*/
/* DATOS INICIALES: permisos                                    */
/*==============================================================*/

INSERT INTO permisos
(
    nombre,
    descripcion,
    estado
)
VALUES
(
    'GESTIONAR_USUARIOS',
    'Permite crear, consultar, actualizar y desactivar usuarios',
    TRUE
),
(
    'GESTIONAR_ROLES',
    'Permite administrar los roles del sistema',
    TRUE
),
(
    'GESTIONAR_PERMISOS',
    'Permite administrar los permisos disponibles',
    TRUE
),
(
    'GESTIONAR_ELECCIONES',
    'Permite crear y administrar procesos electorales',
    TRUE
),
(
    'GESTIONAR_CANDIDATURAS',
    'Permite registrar y administrar candidaturas',
    TRUE
),
(
    'EMITIR_VOTO',
    'Permite participar mediante la emisión de un voto',
    TRUE
),
(
    'CONSULTAR_RESULTADOS',
    'Permite consultar los resultados electorales disponibles',
    TRUE
),
(
    'GESTIONAR_NOTIFICACIONES',
    'Permite crear y enviar notificaciones',
    TRUE
),
(
    'CONSULTAR_NOTIFICACIONES',
    'Permite consultar las notificaciones recibidas',
    TRUE
),
(
    'CONSULTAR_AUDITORIAS',
    'Permite consultar los registros de auditoría',
    TRUE
),
(
    'AUDITAR_PROCESOS',
    'Permite revisar la trazabilidad de los procesos electorales',
    TRUE
),
(
    'GESTIONAR_CONFIGURACION',
    'Permite modificar los parámetros generales del sistema',
    TRUE
);


/*==============================================================*/
/* PERMISOS DEL ADMINISTRADOR                                   */
/*==============================================================*/

INSERT INTO roles_permisos
(
    id_rol,
    id_permiso
)
SELECT
    r.id_rol,
    p.id_permiso

FROM roles r

CROSS JOIN permisos p

WHERE r.nombre = 'ADMINISTRADOR';


/*==============================================================*/
/* PERMISOS DEL ESTUDIANTE                                      */
/*==============================================================*/

INSERT INTO roles_permisos
(
    id_rol,
    id_permiso
)
SELECT
    r.id_rol,
    p.id_permiso

FROM roles r

INNER JOIN permisos p
    ON p.nombre IN
    (
        'EMITIR_VOTO',
        'CONSULTAR_RESULTADOS',
        'CONSULTAR_NOTIFICACIONES'
    )

WHERE r.nombre = 'ESTUDIANTE';


/*==============================================================*/
/* PERMISOS DEL CANDIDATO                                       */
/*==============================================================*/

INSERT INTO roles_permisos
(
    id_rol,
    id_permiso
)
SELECT
    r.id_rol,
    p.id_permiso

FROM roles r

INNER JOIN permisos p
    ON p.nombre IN
    (
        'EMITIR_VOTO',
        'CONSULTAR_RESULTADOS',
        'CONSULTAR_NOTIFICACIONES'
    )

WHERE r.nombre = 'CANDIDATO';


/*==============================================================*/
/* PERMISOS DEL AUDITOR                                         */
/*==============================================================*/

INSERT INTO roles_permisos
(
    id_rol,
    id_permiso
)
SELECT
    r.id_rol,
    p.id_permiso

FROM roles r

INNER JOIN permisos p
    ON p.nombre IN
    (
        'CONSULTAR_RESULTADOS',
        'CONSULTAR_AUDITORIAS',
        'AUDITAR_PROCESOS'
    )

WHERE r.nombre = 'AUDITOR';


/*==============================================================*/
/* DATOS INICIALES: usuarios                                    */
/* Contraseña temporal de prueba: Temporal123*                  */
/*==============================================================*/

INSERT INTO usuarios
(
    id_rol,
    documento,
    nombres,
    apellidos,
    correo,
    password_hash,
    telefono,
    estado
)
VALUES
(
    (SELECT id_rol FROM roles WHERE nombre = 'ADMINISTRADOR'),
    '1000000001',
    'Laura',
    'Martínez',
    'laura.martinez@universidad.edu.co',
    '$2a$12$rL2IE4qivVNntozgTz5YmO/L7ONXs0biW8a9FbQ6.RHNhI9mW6cSS',
    '3000000001',
    TRUE
),
(
    (SELECT id_rol FROM roles WHERE nombre = 'AUDITOR'),
    '1000000002',
    'Andrés',
    'Ruiz',
    'andres.ruiz@universidad.edu.co',
    '$2a$12$rL2IE4qivVNntozgTz5YmO/L7ONXs0biW8a9FbQ6.RHNhI9mW6cSS',
    '3000000002',
    TRUE
),
(
    (SELECT id_rol FROM roles WHERE nombre = 'CANDIDATO'),
    '1000000003',
    'Camila',
    'Torres',
    'camila.torres@universidad.edu.co',
    '$2a$12$rL2IE4qivVNntozgTz5YmO/L7ONXs0biW8a9FbQ6.RHNhI9mW6cSS',
    '3000000003',
    TRUE
),
(
    (SELECT id_rol FROM roles WHERE nombre = 'CANDIDATO'),
    '1000000004',
    'Daniel',
    'Rojas',
    'daniel.rojas@universidad.edu.co',
    '$2a$12$rL2IE4qivVNntozgTz5YmO/L7ONXs0biW8a9FbQ6.RHNhI9mW6cSS',
    '3000000004',
    TRUE
),
(
    (SELECT id_rol FROM roles WHERE nombre = 'CANDIDATO'),
    '1000000005',
    'Valentina',
    'Gómez',
    'valentina.gomez@universidad.edu.co',
    '$2a$12$rL2IE4qivVNntozgTz5YmO/L7ONXs0biW8a9FbQ6.RHNhI9mW6cSS',
    '3000000005',
    TRUE
),
(
    (SELECT id_rol FROM roles WHERE nombre = 'ESTUDIANTE'),
    '1000000006',
    'Santiago',
    'Pérez',
    'santiago.perez@universidad.edu.co',
    '$2a$12$rL2IE4qivVNntozgTz5YmO/L7ONXs0biW8a9FbQ6.RHNhI9mW6cSS',
    '3000000006',
    TRUE
),
(
    (SELECT id_rol FROM roles WHERE nombre = 'ESTUDIANTE'),
    '1000000007',
    'Mariana',
    'López',
    'mariana.lopez@universidad.edu.co',
    '$2a$12$rL2IE4qivVNntozgTz5YmO/L7ONXs0biW8a9FbQ6.RHNhI9mW6cSS',
    '3000000007',
    TRUE
),
(
    (SELECT id_rol FROM roles WHERE nombre = 'ESTUDIANTE'),
    '1000000008',
    'Juan',
    'Herrera',
    'juan.herrera@universidad.edu.co',
    '$2a$12$rL2IE4qivVNntozgTz5YmO/L7ONXs0biW8a9FbQ6.RHNhI9mW6cSS',
    '3000000008',
    TRUE
),
(
    (SELECT id_rol FROM roles WHERE nombre = 'ESTUDIANTE'),
    '1000000009',
    'Natalia',
    'Castro',
    'natalia.castro@universidad.edu.co',
    '$2a$12$rL2IE4qivVNntozgTz5YmO/L7ONXs0biW8a9FbQ6.RHNhI9mW6cSS',
    '3000000009',
    TRUE
),
(
    (SELECT id_rol FROM roles WHERE nombre = 'ESTUDIANTE'),
    '1000000010',
    'Felipe',
    'Mendoza',
    'felipe.mendoza@universidad.edu.co',
    '$2a$12$rL2IE4qivVNntozgTz5YmO/L7ONXs0biW8a9FbQ6.RHNhI9mW6cSS',
    '3000000010',
    TRUE
);


/*==============================================================*/
/* DATOS INICIALES: elecciones                                  */
/*==============================================================*/

INSERT INTO elecciones
(
    nombre_eleccion,
    descripcion_eleccion,
    fecha_inicio,
    fecha_fin,
    estado
)
VALUES
(
    'Elección de Representante Estudiantil 2026',
    'Proceso electoral para elegir al representante general de los estudiantes',
    DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 1 DAY),
    DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 7 DAY),
    'ACTIVA'
),
(
    'Elección del Consejo Académico 2027',
    'Proceso programado para elegir representantes ante el consejo académico',
    DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 30 DAY),
    DATE_ADD(CURRENT_TIMESTAMP, INTERVAL 37 DAY),
    'PROGRAMADA'
);


/*==============================================================*/
/* DATOS INICIALES: candidatos                                  */
/*==============================================================*/

INSERT INTO candidatos
(
    id_usuario,
    titulo_propuesta,
    propuesta,
    foto,
    estado
)
VALUES
(
    (
        SELECT id_usuario
        FROM usuarios
        WHERE documento = '1000000003'
    ),
    'Universidad participativa y transparente',
    'Fortalecer la participación estudiantil, publicar informes de gestión y crear espacios periódicos de diálogo con los aprendices.',
    'camila_torres.jpg',
    TRUE
),
(
    (
        SELECT id_usuario
        FROM usuarios
        WHERE documento = '1000000004'
    ),
    'Bienestar y acompañamiento estudiantil',
    'Crear estrategias de bienestar, acompañamiento académico y apoyo a estudiantes en riesgo de deserción.',
    'daniel_rojas.jpg',
    TRUE
),
(
    (
        SELECT id_usuario
        FROM usuarios
        WHERE documento = '1000000005'
    ),
    'Innovación y transformación digital',
    'Promover herramientas digitales, formación tecnológica y canales modernos de comunicación entre estudiantes y directivos.',
    'valentina_gomez.jpg',
    TRUE
);


/*==============================================================*/
/* DATOS INICIALES: candidaturas                                */
/*==============================================================*/

INSERT INTO candidaturas
(
    id_candidato,
    id_eleccion,
    numero_lista,
    estado
)
SELECT
    c.id_candidato,
    e.id_eleccion,
    1,
    TRUE

FROM candidatos c

INNER JOIN usuarios u
    ON u.id_usuario = c.id_usuario

CROSS JOIN elecciones e

WHERE u.documento = '1000000003'
  AND e.nombre_eleccion =
      'Elección de Representante Estudiantil 2026';
      
INSERT INTO candidaturas
(
    id_candidato,
    id_eleccion,
    numero_lista,
    estado
)
SELECT
    c.id_candidato,
    e.id_eleccion,
    2,
    TRUE

FROM candidatos c

INNER JOIN usuarios u
    ON u.id_usuario = c.id_usuario

CROSS JOIN elecciones e

WHERE u.documento = '1000000004'
  AND e.nombre_eleccion =
      'Elección de Representante Estudiantil 2026';
      
INSERT INTO candidaturas
(
    id_candidato,
    id_eleccion,
    numero_lista,
    estado
)
SELECT
    c.id_candidato,
    e.id_eleccion,
    3,
    TRUE

FROM candidatos c

INNER JOIN usuarios u
    ON u.id_usuario = c.id_usuario

CROSS JOIN elecciones e

WHERE u.documento = '1000000005'
  AND e.nombre_eleccion =
      'Elección de Representante Estudiantil 2026';
      
      
/*==============================================================*/
/* DATOS INICIALES: configuracion_sistema                       */
/*==============================================================*/

INSERT INTO configuracion_sistema
(
    nombre_parametro,
    valor_parametro,
    descripcion_parametro
)
VALUES
(
    'NOMBRE_INSTITUCION',
    'Universidad de los Sabios',
    'Nombre de la institución que utiliza la plataforma'
),
(
    'MAX_INTENTOS_LOGIN',
    '5',
    'Cantidad máxima de intentos fallidos de inicio de sesión'
),
(
    'DURACION_SESION_MINUTOS',
    '30',
    'Duración máxima de una sesión sin actividad'
),
(
    'RESULTADOS_DURANTE_ELECCION',
    'FALSE',
    'Determina si los resultados pueden verse antes del cierre de la elección'
),
(
    'DOMINIO_CORREO_INSTITUCIONAL',
    'universidad.edu.co',
    'Dominio institucional permitido para los usuarios'
),
(
    'MANTENIMIENTO_ACTIVO',
    'FALSE',
    'Indica si la plataforma se encuentra en modo de mantenimiento'
);


/*==============================================================*/
/* DATOS INICIALES: notificaciones                              */
/*==============================================================*/

INSERT INTO notificaciones
(
    id_usuario,
    titulo,
    mensaje,
    leida
)
SELECT
    id_usuario,
    'Bienvenido a la plataforma',
    'Su cuenta fue registrada correctamente en la Plataforma de Votación Universitaria Digital.',
    FALSE

FROM usuarios

WHERE documento IN
(
    '1000000006',
    '1000000007',
    '1000000008',
    '1000000009',
    '1000000010'
);

INSERT INTO notificaciones
(
    id_usuario,
    titulo,
    mensaje,
    leida
)
SELECT
    id_usuario,
    'Elección disponible',
    'La Elección de Representante Estudiantil 2026 se encuentra activa.',
    FALSE

FROM usuarios

WHERE documento IN
(
    '1000000003',
    '1000000004',
    '1000000005',
    '1000000006',
    '1000000007',
    '1000000008',
    '1000000009',
    '1000000010'
);


/*==============================================================*/
/* DATOS INICIALES: auditorias                                  */
/*==============================================================*/

INSERT INTO auditorias
(
    id_usuario,
    accion,
    descripcion,
    direccion_ip
)
VALUES
(
    (
        SELECT id_usuario
        FROM usuarios
        WHERE documento = '1000000001'
    ),
    'CREACION_INICIAL',
    'Creación de los datos iniciales de la plataforma',
    '127.0.0.1'
),
(
    (
        SELECT id_usuario
        FROM usuarios
        WHERE documento = '1000000001'
    ),
    'CREACION_ELECCION',
    'Creación de la elección de representante estudiantil 2026',
    '127.0.0.1'
);


/*==============================================================*/
/* DATOS INICIALES: sesiones                                    */
/*==============================================================*/

INSERT INTO sesiones
(
    id_usuario,
    direccion_ip,
    dispositivo,
    navegador,
    estado
)
VALUES
(
    (
        SELECT id_usuario
        FROM usuarios
        WHERE documento = '1000000001'
    ),
    '127.0.0.1',
    'Equipo administrativo',
    'Mozilla Firefox',
    'ACTIVA'
);


/*
SET @id_eleccion_activa =
(
    SELECT id_eleccion
    FROM elecciones
    WHERE nombre_eleccion =
        'Elección de Representante Estudiantil 2026'
);

SET @id_candidatura_1 =
(
    SELECT id_candidatura
    FROM candidaturas
    WHERE id_eleccion = @id_eleccion_activa
      AND numero_lista = 1
);

SET @id_candidatura_2 =
(
    SELECT id_candidatura
    FROM candidaturas
    WHERE id_eleccion = @id_eleccion_activa
      AND numero_lista = 2
);

SET @id_candidatura_3 =
(
    SELECT id_candidatura
    FROM candidaturas
    WHERE id_eleccion = @id_eleccion_activa
      AND numero_lista = 3
);

SET @id_estudiante_1 =
(
    SELECT id_usuario
    FROM usuarios
    WHERE documento = '1000000006'
);

SET @id_estudiante_2 =
(
    SELECT id_usuario
    FROM usuarios
    WHERE documento = '1000000007'
);

SET @id_estudiante_3 =
(
    SELECT id_usuario
    FROM usuarios
    WHERE documento = '1000000008'
);

SET @id_estudiante_4 =
(
    SELECT id_usuario
    FROM usuarios
    WHERE documento = '1000000009'
);

SET @id_estudiante_5 =
(
    SELECT id_usuario
    FROM usuarios
    WHERE documento = '1000000010'
);
*/

/*
SET @codigo_voto_1 = NULL;

CALL sp_registrar_voto
(
    @id_estudiante_1,
    @id_eleccion_activa,
    @id_candidatura_1,
    @codigo_voto_1
);

SELECT
    @codigo_voto_1 AS comprobante_estudiante_1;
*/

/*
SET @codigo_voto_2 = NULL;

CALL sp_registrar_voto
(
    @id_estudiante_2,
    @id_eleccion_activa,
    @id_candidatura_1,
    @codigo_voto_2
);

SELECT
    @codigo_voto_2 AS comprobante_estudiante_2;
*/

/*
SET @codigo_voto_3 = NULL;

CALL sp_registrar_voto
(
    @id_estudiante_3,
    @id_eleccion_activa,
    @id_candidatura_2,
    @codigo_voto_3
);

SELECT
    @codigo_voto_3 AS comprobante_estudiante_3;
*/

/*
SET @codigo_voto_4 = NULL;

CALL sp_registrar_voto
(
    @id_estudiante_4,
    @id_eleccion_activa,
    @id_candidatura_2,
    @codigo_voto_4
);

SELECT
    @codigo_voto_4 AS comprobante_estudiante_4;
*/

/*
SET @codigo_voto_5 = NULL;

CALL sp_registrar_voto
(
    @id_estudiante_5,
    @id_eleccion_activa,
    @id_candidatura_3,
    @codigo_voto_5
);

SELECT
    @codigo_voto_5 AS comprobante_estudiante_5;
*/

/*
CALL sp_consultar_resultados(@id_eleccion_activa);
*/

/*
SELECT *
FROM vw_resultados
WHERE id_eleccion = @id_eleccion_activa
ORDER BY total_votos DESC,
         numero_lista ASC;
*/

/*
CALL sp_verificar_voto(@codigo_voto_1);
*/

/*
CALL sp_registrar_voto
(
    @id_estudiante_1,
    @id_eleccion_activa,
    @id_candidatura_2,
    @codigo_duplicado
);
*/

/*
SET @id_eleccion_programada =
(
    SELECT id_eleccion
    FROM elecciones
    WHERE nombre_eleccion =
        'Elección del Consejo Académico 2027'
);

CALL sp_registrar_voto
(
    @id_estudiante_1,
    @id_eleccion_programada,
    @id_candidatura_1,
    @codigo_eleccion_inactiva
);
*/

/*
SELECT * FROM roles;

SELECT * FROM permisos;

SELECT * FROM roles_permisos;

SELECT * FROM usuarios;

SELECT * FROM elecciones;

SELECT * FROM candidatos;

SELECT * FROM candidaturas;

SELECT * FROM votos;

SELECT * FROM control_votacion;

SELECT * FROM sesiones;

SELECT * FROM auditorias;

SELECT * FROM notificaciones;

SELECT * FROM configuracion_sistema;

SELECT * FROM vw_resultados;
*/
