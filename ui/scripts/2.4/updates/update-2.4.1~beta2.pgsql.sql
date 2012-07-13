--
-- New table for trusted logins
--
BEGIN;

CREATE TABLE trusttoken
(
  token character varying(255),
  time_created timestamp without time zone default current_timestamp
);
INSERT INTO trusttoken (token) VALUES (UUID());

COMMIT;

