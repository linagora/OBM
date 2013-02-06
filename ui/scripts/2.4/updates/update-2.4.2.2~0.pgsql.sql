BEGIN;

UPDATE ObmInfo SET obminfo_value = '2.4.2.2-pre' WHERE obminfo_name = 'db_version';

ALTER TABLE opush_sync_perms ADD COLUMN pending_accept BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE opush_sync_perms ALTER pending_accept DROP DEFAULT;

--
-- Add Table: commitedoperation: In order to map client_id (as SHA1) to entity_id on elements persisted
--

BEGIN;

--
-- Name: vkind; Type: TYPE; Schema: public; Owner: -
--

CREATE TYPE vkind AS ENUM (
    'VEVENT',
    'VCONTACT'
);

--
-- Name: commitedoperation; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE commitedoperation (
    commitedoperation_hash_client_id varchar(44) NOT NULL PRIMARY KEY,
    commitedoperation_entity_id integer NOT NULL, 
    commitedoperation_kind vkind NOT NULL,
    CONSTRAINT commitedoperation_entity_id_fkey FOREIGN KEY (commitedoperation_entity_id) REFERENCES entity(entity_id) ON DELETE CASCADE
);

UPDATE ObmInfo SET obminfo_value = '2.4.2.2' WHERE obminfo_name = 'db_version';
COMMIT;
