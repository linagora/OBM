--
-- New table for trusted logins
--
BEGIN;

CREATE TABLE TrustToken (
  id int(8) NOT NULL PRIMARY KEY AUTO_INCREMENT,
  token char(36) NOT NULL UNIQUE,
  login varchar(80) NOT NULL,
  time_created timestamp
);

COMMIT;
