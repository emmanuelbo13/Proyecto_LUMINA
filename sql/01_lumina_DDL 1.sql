-- ==========================================================
-- PROYECTO LUMINA - Sistema Local de Gestión de Biblioteca Multimedia
-- 01 - DDL (Data Definition Language)
-- Creación de la base de datos y de las tablas según el
-- modelo relacional. MySQL 8.
-- ==========================================================

DROP DATABASE IF EXISTS lumina;
CREATE DATABASE lumina CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE lumina;

-- ----------------------------------------------------------
-- Tablas de apoyo
-- ----------------------------------------------------------

CREATE TABLE rol (
  id_rol      INT          NOT NULL AUTO_INCREMENT,
  nombre      VARCHAR(30)  NOT NULL,
  descripcion VARCHAR(150) NULL,
  CONSTRAINT pk_rol PRIMARY KEY (id_rol),
  CONSTRAINT uk_rol_nombre UNIQUE (nombre)
);

CREATE TABLE tipo_documento (
  id_tipo_documento INT         NOT NULL AUTO_INCREMENT,
  codigo            VARCHAR(5)  NOT NULL,
  nombre            VARCHAR(60) NOT NULL,
  CONSTRAINT pk_tipo_documento PRIMARY KEY (id_tipo_documento),
  CONSTRAINT uk_tipo_documento_codigo UNIQUE (codigo)
);

CREATE TABLE grado (
  id_grado INT         NOT NULL AUTO_INCREMENT,
  nombre   VARCHAR(30) NOT NULL,
  CONSTRAINT pk_grado PRIMARY KEY (id_grado),
  CONSTRAINT uk_grado_nombre UNIQUE (nombre)
);

CREATE TABLE categoria (
  id_categoria INT         NOT NULL AUTO_INCREMENT,
  nombre       VARCHAR(60) NOT NULL,
  CONSTRAINT pk_categoria PRIMARY KEY (id_categoria),
  CONSTRAINT uk_categoria_nombre UNIQUE (nombre)
);

-- ----------------------------------------------------------
-- Especialización PERSONA -> ESTUDIANTE / FUNCIONARIO
-- ----------------------------------------------------------

CREATE TABLE persona (
  id_persona        INT          NOT NULL AUTO_INCREMENT,
  id_tipo_documento INT          NOT NULL,
  numero_documento  VARCHAR(20)  NOT NULL,
  nombres           VARCHAR(60)  NOT NULL,
  apellidos         VARCHAR(60)  NOT NULL,
  clave_hash        VARCHAR(100) NOT NULL,   -- clave/PIN cifrado desde Java (PBKDF2)
  tipo_persona      ENUM('ESTUDIANTE','FUNCIONARIO') NOT NULL,
  estado            ENUM('ACTIVO','INACTIVO') NOT NULL DEFAULT 'ACTIVO',
  fecha_registro    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_persona PRIMARY KEY (id_persona),
  CONSTRAINT uk_persona_documento UNIQUE (id_tipo_documento, numero_documento),
  CONSTRAINT fk_persona_tipo_documento FOREIGN KEY (id_tipo_documento)
    REFERENCES tipo_documento (id_tipo_documento)
);

CREATE TABLE estudiante (
  id_persona INT NOT NULL,
  id_grado   INT NOT NULL,
  CONSTRAINT pk_estudiante PRIMARY KEY (id_persona),
  CONSTRAINT fk_estudiante_persona FOREIGN KEY (id_persona)
    REFERENCES persona (id_persona),
  CONSTRAINT fk_estudiante_grado FOREIGN KEY (id_grado)
    REFERENCES grado (id_grado)
);

CREATE TABLE funcionario (
  id_persona INT NOT NULL,
  id_rol     INT NOT NULL,
  CONSTRAINT pk_funcionario PRIMARY KEY (id_persona),
  CONSTRAINT fk_funcionario_persona FOREIGN KEY (id_persona)
    REFERENCES persona (id_persona),
  CONSTRAINT fk_funcionario_rol FOREIGN KEY (id_rol)
    REFERENCES rol (id_rol)
);

-- ----------------------------------------------------------
-- Especialización MATERIAL -> LIBRO_FISICO / EBOOK / AUDIOLIBRO
-- ----------------------------------------------------------

CREATE TABLE material (
  id_material       INT          NOT NULL AUTO_INCREMENT,
  titulo            VARCHAR(200) NOT NULL,
  autor             VARCHAR(150) NOT NULL,
  num_paginas       INT          NOT NULL,
  fecha_publicacion DATE         NULL,
  id_categoria      INT          NOT NULL,
  tipo_material     ENUM('LIBRO','EBOOK','AUDIOLIBRO') NOT NULL,
  estado            ENUM('ACTIVO','RETIRADO') NOT NULL DEFAULT 'ACTIVO',
  fecha_registro    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  CONSTRAINT pk_material PRIMARY KEY (id_material),
  CONSTRAINT fk_material_categoria FOREIGN KEY (id_categoria)
    REFERENCES categoria (id_categoria),
  CONSTRAINT ck_material_paginas CHECK (num_paginas > 0)
);

CREATE TABLE libro_fisico (
  id_material       INT         NOT NULL,
  isbn              VARCHAR(17) NULL,
  ubicacion_estante VARCHAR(30) NOT NULL,
  CONSTRAINT pk_libro_fisico PRIMARY KEY (id_material),
  CONSTRAINT uk_libro_fisico_isbn UNIQUE (isbn),
  CONSTRAINT fk_libro_fisico_material FOREIGN KEY (id_material)
    REFERENCES material (id_material)
);

-- Entidad débil: su PK combina la PK del libro y su clave parcial
CREATE TABLE ejemplar (
  id_material     INT  NOT NULL,
  numero_ejemplar INT  NOT NULL,
  estado          ENUM('DISPONIBLE','RESERVADO','DANADO') NOT NULL DEFAULT 'DISPONIBLE',
  fecha_ingreso   DATE NOT NULL,
  CONSTRAINT pk_ejemplar PRIMARY KEY (id_material, numero_ejemplar),
  CONSTRAINT fk_ejemplar_libro_fisico FOREIGN KEY (id_material)
    REFERENCES libro_fisico (id_material)
);

CREATE TABLE ebook (
  id_material     INT          NOT NULL,
  formato_archivo VARCHAR(10)  NOT NULL,
  tamano_mb       DECIMAL(7,2) NOT NULL,
  total_licencias INT          NOT NULL,
  CONSTRAINT pk_ebook PRIMARY KEY (id_material),
  CONSTRAINT fk_ebook_material FOREIGN KEY (id_material)
    REFERENCES material (id_material),
  CONSTRAINT ck_ebook_licencias CHECK (total_licencias > 0)
);

CREATE TABLE audiolibro (
  id_material     INT          NOT NULL,
  duracion_min    INT          NOT NULL,
  narrador        VARCHAR(120) NOT NULL,
  formato_audio   VARCHAR(10)  NOT NULL,
  total_licencias INT          NOT NULL,
  CONSTRAINT pk_audiolibro PRIMARY KEY (id_material),
  CONSTRAINT fk_audiolibro_material FOREIGN KEY (id_material)
    REFERENCES material (id_material),
  CONSTRAINT ck_audiolibro_duracion CHECK (duracion_min > 0),
  CONSTRAINT ck_audiolibro_licencias CHECK (total_licencias > 0)
);

-- ----------------------------------------------------------
-- Reservas
-- ----------------------------------------------------------

CREATE TABLE reserva (
  id_reserva        INT      NOT NULL AUTO_INCREMENT,
  id_estudiante     INT      NOT NULL,
  id_material       INT      NOT NULL,
  numero_ejemplar   INT      NULL,     -- solo libros físicos; NULL en ebook y audiolibro
  fecha_reserva     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_vencimiento DATE     NOT NULL,
  estado            ENUM('ACTIVA','CANCELADA','FINALIZADA','VENCIDA') NOT NULL DEFAULT 'ACTIVA',
  CONSTRAINT pk_reserva PRIMARY KEY (id_reserva),
  CONSTRAINT fk_reserva_estudiante FOREIGN KEY (id_estudiante)
    REFERENCES estudiante (id_persona),
  CONSTRAINT fk_reserva_material FOREIGN KEY (id_material)
    REFERENCES material (id_material),
  CONSTRAINT fk_reserva_ejemplar FOREIGN KEY (id_material, numero_ejemplar)
    REFERENCES ejemplar (id_material, numero_ejemplar)
);
