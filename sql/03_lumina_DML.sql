-- ==========================================================
-- PROYECTO LUMINA
-- 03 - DML (Data Manipulation Language)
-- Datos iniciales y de prueba. Ejecutar después del DDL y DCL.
-- ==========================================================

USE lumina;

-- ----------------------------------------------------------
-- DATOS INICIALES
-- ----------------------------------------------------------

INSERT INTO rol (nombre, descripcion) VALUES
('ADMINISTRADOR', 'Administra el sistema y los funcionarios'),
('BIBLIOTECARIO', 'Gestiona materiales, estudiantes y reservas');

INSERT INTO tipo_documento (codigo, nombre) VALUES
('RC', 'Registro civil'),
('TI', 'Tarjeta de identidad'),
('CC', 'Cédula de ciudadanía'),
('CE', 'Cédula de extranjería'),
('PA', 'Pasaporte'),
('PPT', 'Permiso por protección temporal');

INSERT INTO grado (nombre) VALUES
('Transición'), ('Primero'), ('Segundo'), ('Tercero'), ('Cuarto'), ('Quinto'),
('Sexto'), ('Séptimo'), ('Octavo'), ('Noveno'), ('Décimo'), ('Undécimo');

INSERT INTO categoria (nombre) VALUES
('Literatura infantil'), ('Literatura juvenil'), ('Novela'),
('Ciencias naturales'), ('Matemáticas'), ('Historia y sociales');

-- ----------------------------------------------------------
-- DATOS DE PRUEBA
-- Claves de prueba. Se guardan ya cifradas, tal como las cifra Java
-- con SeguridadClave.cifrar() (SHA-256):
--   Administrador CC 1000000001 -> Lumina2026*
--   Bibliotecaria CC 1000000002 -> Biblio2026*
--   Estudiantes                 -> PIN 1234
-- ----------------------------------------------------------

INSERT INTO persona (id_persona, id_tipo_documento, numero_documento, nombres, apellidos, clave_hash, tipo_persona) VALUES
(1, 3, '1000000001', 'Administrador', 'Lumina',        '0af73c469e79b35deefb62c1311af4256f8484be05290eb1d41821e330ff8299', 'FUNCIONARIO'),
(2, 3, '1000000002', 'Ana María', 'Rodríguez Peña',   '2921062e68e0ef435161c508c5e72a86931f1f3ac512471780a4ebd0637bd66d', 'FUNCIONARIO'),
(3, 2, '1072000101', 'Juan Pablo', 'Gómez Ruiz',      '03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'ESTUDIANTE'),
(4, 2, '1072000102', 'Laura Sofía', 'Martínez Castro','03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4', 'ESTUDIANTE');

INSERT INTO funcionario (id_persona, id_rol) VALUES (1, 1), (2, 2);
INSERT INTO estudiante  (id_persona, id_grado) VALUES (3, 10), (4, 8);

INSERT INTO material (id_material, titulo, autor, num_paginas, fecha_publicacion, id_categoria, tipo_material) VALUES
(1, 'Cien años de soledad', 'Gabriel García Márquez', 471, '1967-05-30', 3, 'LIBRO'),
(2, 'El principito', 'Antoine de Saint-Exupéry', 96, '1943-04-06', 1, 'LIBRO'),
(3, 'María', 'Jorge Isaacs', 320, '1867-01-01', 3, 'EBOOK'),
(4, 'Matemáticas básicas', 'Equipo editorial', 150, '2020-01-01', 5, 'EBOOK'),
(5, 'La vorágine', 'José Eustasio Rivera', 290, '1924-11-25', 3, 'AUDIOLIBRO');

INSERT INTO libro_fisico (id_material, isbn, ubicacion_estante) VALUES
(1, '978-0307474728', 'A-01'),
(2, '978-9584203243', 'B-03');

INSERT INTO ejemplar (id_material, numero_ejemplar, estado, fecha_ingreso) VALUES
(1, 1, 'RESERVADO',  '2024-02-10'),
(1, 2, 'DISPONIBLE', '2024-02-10'),
(1, 3, 'DISPONIBLE', '2025-08-15'),
(2, 1, 'DISPONIBLE', '2025-03-03');

INSERT INTO ebook (id_material, formato_archivo, tamano_mb, total_licencias) VALUES
(3, 'EPUB', 1.80, 3),
(4, 'PDF', 12.50, 1);

INSERT INTO audiolibro (id_material, duracion_min, narrador, formato_audio, total_licencias) VALUES
(5, 540, 'Narrador de prueba', 'MP3', 2);

INSERT INTO reserva (id_estudiante, id_material, numero_ejemplar, fecha_vencimiento) VALUES
(3, 1, 1, DATE_ADD(CURDATE(), INTERVAL 3 DAY)),
(4, 4, NULL, DATE_ADD(CURDATE(), INTERVAL 3 DAY));
