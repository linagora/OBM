--
-- New table for trusted logins
--
BEGIN;

CREATE TABLE TrustToken (
  token varchar(255) NOT NULL,
  time_created timestamp NULL default CURRENT_TIMESTAMP
);
INSERT INTO TrustToken (token) VALUES (UUID());

COMMIT;
