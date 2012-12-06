--
-- Adds a new preference 'set_allday_opacity'
--
-- Add Table: deletedeventlink: In order to save deleted users from an event 
--

BEGIN;

INSERT INTO UserObmPref (userobmpref_option, userobmpref_value)
SELECT 'set_allday_opacity', 'TRANSPARENT'
WHERE NOT EXISTS (SELECT 1 FROM UserObmPref WHERE userobmpref_option='set_allday_opacity');

-- TABLE deletedeventlink;

CREATE TABLE deletedeventlink
(
  deletedeventlink_id serial NOT NULL,
  deletedeventlink_userobm_id integer NOT NULL,
  deletedeventlink_event_id integer NOT NULL,
  deletedeventlink_event_ext_id character varying(300) NOT NULL,
  deletedeventlink_time_removed timestamp without time zone NOT NULL DEFAULT now(),
  CONSTRAINT deletedeventlink_deletedeventlink_id_pkey PRIMARY KEY (deletedeventlink_id ),
  CONSTRAINT deletedeventlink_event_id_event_id_fkey FOREIGN KEY (deletedeventlink_event_id)
      REFERENCES event (event_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE,
  CONSTRAINT deletedeventlink_userobm_id_userobm_id_fkey FOREIGN KEY (deletedeventlink_userobm_id)
      REFERENCES userobm (userobm_id) MATCH SIMPLE
      ON UPDATE CASCADE ON DELETE CASCADE
)
WITH (
  OIDS=FALSE
);
ALTER TABLE deletedeventlink
  OWNER TO obm;

-- Index: fki_deletedeventlink_event_id_event_id_fkey
-- DROP INDEX fki_deletedeventlink_event_id_event_id_fkey;

CREATE INDEX fki_deletedeventlink_event_id_event_id_fkey
  ON deletedeventlink
  USING btree
  (deletedeventlink_event_id );

-- Index: fki_deletedeventlink_userobm_id_userobm_id_fkey
-- DROP INDEX fki_deletedeventlink_userobm_id_userobm_id_fkey;

CREATE INDEX fki_deletedeventlink_userobm_id_userobm_id_fkey
  ON deletedeventlink
  USING btree
  (deletedeventlink_userobm_id );

-- Sequence: deletedeventlink_deletedeventlink_id_seq
-- DROP SEQUENCE deletedeventlink_deletedeventlink_id_seq;

CREATE SEQUENCE deletedeventlink_deletedeventlink_id_seq
  INCREMENT 1
  MINVALUE 1
  MAXVALUE 9223372036854775807
  START 13
  CACHE 1;
ALTER TABLE deletedeventlink_deletedeventlink_id_seq
  OWNER TO obm;

ALTER TABLE deletedeventlink ALTER COLUMN deletedeventlink_id SET DEFAULT nextval('deletedeventlink_deletedeventlink_id_seq'::regclass);

--
-- Those tables drops are done because emails sync states are now managed out of the SQL database
--
DROP TABLE opush_sync_deleted_mail, opush_sync_mail;

COMMIT;
