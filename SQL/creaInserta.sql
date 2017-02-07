-- Schema: bd_ftp

-- DROP SCHEMA bd_ftp;

CREATE SCHEMA bd_ftp
  AUTHORIZATION amromero;
 
-
- Table: bd_ftp.users

-- DROP TABLE bd_ftp.users;

CREATE TABLE bd_ftp.users
(
  usuario character varying,
  contrasenia character varying
)
WITH (
  OIDS=FALSE
);
ALTER TABLE bd_ftp.users
  OWNER TO amromero;
  
-- Usuarios de prueba

INSERT INTO bd_ftp.users(
            usuario, contrasenia)
    VALUES ('marco', 'marco');
    
    
INSERT INTO bd_ftp.users(
            usuario, contrasenia)
    VALUES ('admin', 'admin');
