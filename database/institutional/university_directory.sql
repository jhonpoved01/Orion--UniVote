/*==============================================================*/
/* SISTEMA INSTITUCIONAL SIMULADO PARA UNIVOTE                  */
/* BASE: universidad_institucional                              */
/* ADVERTENCIA: script destructivo para entorno local de demo   */
/*==============================================================*/

DROP DATABASE IF EXISTS universidad_institucional;

CREATE DATABASE universidad_institucional
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE universidad_institucional;

CREATE TABLE estudiantes_institucionales
(
    codigo_estudiante VARCHAR(20) NOT NULL,
    documento VARCHAR(20) NOT NULL,
    nombres VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    correo_institucional VARCHAR(120) NOT NULL,
    programa VARCHAR(150) NOT NULL,
    estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    habilitado_votacion BOOLEAN NOT NULL DEFAULT TRUE,
    fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,

    PRIMARY KEY (codigo_estudiante),

    CONSTRAINT uq_estudiante_institucional_documento UNIQUE (documento),
    CONSTRAINT uq_estudiante_institucional_correo UNIQUE (correo_institucional),

    CONSTRAINT chk_estudiante_institucional_estado
        CHECK (estado IN ('ACTIVO','INACTIVO','GRADUADO','RETIRADO')),

    CONSTRAINT chk_estudiante_institucional_habilitado
        CHECK (habilitado_votacion IN (0,1))
) ENGINE=InnoDB
COMMENT='Directorio académico simulado, externo a UniVote';

CREATE INDEX idx_estudiante_institucional_estado
ON estudiantes_institucionales(estado, habilitado_votacion);

INSERT INTO estudiantes_institucionales
(
    codigo_estudiante,
    documento,
    nombres,
    apellidos,
    correo_institucional,
    programa,
    estado,
    habilitado_votacion
)
VALUES
('20260001','1001001001','Ana María','Torres Rojas','ana.torres@universidad.test','Ingeniería de Sistemas','ACTIVO',TRUE),
('20260002','1001001002','Mateo','Rojas Silva','mateo.rojas@universidad.test','Administración de Empresas','ACTIVO',TRUE),
('20260003','1001001003','Valentina','Gómez Pérez','valentina.gomez@universidad.test','Derecho','ACTIVO',TRUE),
('20260004','1001001004','Samuel','Díaz Castro','samuel.diaz@universidad.test','Ingeniería Industrial','INACTIVO',FALSE),
('20260005','1001001005','Luciana','Martínez León','luciana.martinez@universidad.test','Psicología','ACTIVO',FALSE);

/*
Usuario técnico sugerido (crear localmente con una contraseña propia):
  GRANT SELECT ON universidad_institucional.estudiantes_institucionales
  TO '<usuario_api>'@'localhost';
*/
