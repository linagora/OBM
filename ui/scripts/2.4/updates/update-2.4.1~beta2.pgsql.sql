--
-- New table for trusted logins
--
BEGIN;

CREATE TABLE trusttoken
(
  id integer NOT NULL PRIMARY KEY,
  token character(36) NOT NULL UNIQUE,
  login varchar(80) NOT NULL,
  time_created timestamp without time zone DEFAULT current_timestamp
);

CREATE SEQUENCE trusttoken_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER SEQUENCE trusttoken_id_seq OWNED BY trusttoken.id;
ALTER TABLE trusttoken ALTER COLUMN id SET DEFAULT nextval('trusttoken_id_seq'::regclass);

COMMIT;
