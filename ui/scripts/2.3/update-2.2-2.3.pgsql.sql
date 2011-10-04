-- Write that the 2.2->2.3 has started
UPDATE ObmInfo SET obminfo_value='2.2.x->2.3.0' WHERE obminfo_name='db_version';
-- -----------------------------------------------------------------------------

--
-- Table structure for table `addressbook`
--
CREATE TABLE addressbook (
  id         integer NOT NULL,
  domain_id  integer NOT NULL,
  timeupdate timestamp without time zone,
  timecreate timestamp without time zone,
  userupdate integer default NULL,
  usercreate integer default NULL,
  origin     varchar(255) NOT NULL,
  owner      integer default NULL,
  name       varchar(64) NOT NULL,
  is_default    boolean default false,
  syncable   boolean default true
);

--
-- Addressbook id sequence
--
CREATE SEQUENCE addressbook_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER SEQUENCE addressbook_id_seq OWNED BY addressbook.id;
ALTER TABLE addressbook ALTER COLUMN id SET DEFAULT nextval('addressbook_id_seq'::regclass);

--
-- addressbook indexes
--
ALTER TABLE ONLY addressbook
    ADD CONSTRAINT addressbook_pkey PRIMARY KEY (id);
CREATE INDEX addressbook_domain_id_fkey ON addressbook (domain_id);
CREATE INDEX addressbook_userupdate_fkey ON addressbook (userupdate);
CREATE INDEX addressbook_usercreate_fkey ON addressbook (usercreate);
CREATE INDEX addressbook_owner_fkey ON addressbook (owner);

--
-- addressbook fkey
--
ALTER TABLE ONLY addressbook
    ADD CONSTRAINT addressbook_domain_id_domain_id_fkey FOREIGN KEY (domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE ONLY addressbook
    ADD CONSTRAINT addressbook_userupdate_userobm_id_fkey FOREIGN KEY (userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;
ALTER TABLE ONLY addressbook
    ADD CONSTRAINT addressbook_usercreate_userobm_id_fkey FOREIGN KEY (usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;
ALTER TABLE ONLY addressbook
    ADD CONSTRAINT addressbook_owner_userobm_id_fkey FOREIGN KEY (owner) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


--
-- Table structure for table `addressbookentity`
--
CREATE TABLE addressbookentity (
  addressbookentity_entity_id      integer NOT NULL,
  addressbookentity_addressbook_id integer NOT NULL
);

--
-- Contact update
--
ALTER TABLE Contact ADD COLUMN contact_addressbook_id integer default NULL;
CREATE INDEX contact_addressbook_id_addressbook_id_fkey ON contact (contact_addressbook_id);

ALTER TABLE Contact ADD CONSTRAINT contact_addressbook_id_addressbook_id_fkey FOREIGN KEY (contact_addressbook_id) REFERENCES addressbook(id) ON UPDATE CASCADE ON DELETE CASCADE;

--
-- Migrate Contact
--

-- --
-- -- Create Default Address books
-- --

INSERT INTO addressbook (domain_id, timecreate, usercreate, origin, owner, name, is_default, syncable) 
SELECT userobm_domain_id, NOW(), userobm_id, 'obm-storage-migration-2.3', userobm_id, 'contacts', true, false 
FROM UserObm INNER JOIN Domain ON userobm_domain_id = domain_id WHERE domain_global = false;

INSERT INTO addressbook (domain_id, timecreate, usercreate, origin, owner, name, is_default, syncable) 
SELECT userobm_domain_id, NOW(), userobm_id, 'obm-storage-migration-2.3', userobm_id, 'collected_contacts', true, true
FROM UserObm INNER JOIN Domain ON userobm_domain_id = domain_id WHERE domain_global = false;

INSERT INTO addressbook (domain_id, timecreate, usercreate, origin, owner, name, is_default, syncable)
SELECT domain_id, NOW(), MIN(userobm_id), 'obm-storage-migration-2.3', MIN(userobm_id), 'public_contacts', true, false
FROM Domain INNER JOIN UserObm ON userobm_domain_id = domain_id WHERE domain_global = false AND userobm_perms = 'admin'
GROUP BY domain_id;

INSERT INTO addressbookentity (addressbookentity_addressbook_id, addressbookentity_entity_id) SELECT id, nextval('entity_entity_id_seq') from addressbook;

INSERT INTO entity (entity_id , entity_mailing) SELECT addressbookentity_entity_id, true FROM addressbookentity ;

-- setting default right on public_contacts address book
INSERT INTO EntityRight (entityright_entity_id, entityright_consumer_id, entityright_access, entityright_read, entityright_write, entityright_admin)
SELECT AddressbookEntity.addressbookentity_entity_id, NULL, 0, 1, 0, 0
FROM AddressbookEntity INNER JOIN AddressBook
ON AddressBook.id=AddressbookEntity.addressbookentity_addressbook_id
WHERE AddressBook.name='public_contacts';

UPDATE contact SET contact_addressbook_id = (SELECT id from addressbook WHERE owner = contact_usercreate and name = 'contacts') WHERE contact_privacy = 1 AND contact_collected = false;
UPDATE contact SET contact_addressbook_id = (SELECT id from addressbook WHERE owner = contact_usercreate and name = 'collected_contacts') WHERE contact_privacy = 1 AND contact_collected = true;
UPDATE contact SET contact_addressbook_id = (SELECT id from addressbook WHERE domain_id = contact_domain_id and name = 'public_contacts') WHERE contact_privacy = 0;

-- --
-- -- Add constraints
-- --

ALTER TABLE ONLY addressbookentity
    ADD CONSTRAINT addressbookentity_pkey PRIMARY KEY (addressbookentity_entity_id, addressbookentity_addressbook_id);

--
-- addressbookentity indexes
--
CREATE INDEX addressbookentity_addressbook_id_addressbook_id_fkey ON addressbookentity (addressbookentity_addressbook_id);
CREATE INDEX addressbookentity_entity_id_entity_id_fkey ON addressbookentity (addressbookentity_entity_id);

--
-- addressbookentity fkey
--
ALTER TABLE ONLY addressbookentity
    ADD CONSTRAINT addressbookentity_addressbook_id_addressbook_id_fkey FOREIGN KEY (addressbookentity_addressbook_id) REFERENCES addressbook(id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE ONLY addressbookentity
    ADD CONSTRAINT addressbookentity_entity_id_entity_id_fkey FOREIGN KEY (addressbookentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;



ALTER TABLE Contact DROP COLUMN contact_privacy;


--
-- Table structure for table `DeletedAddressbook`
--
CREATE TABLE deletedaddressbook (
  addressbook_id integer NOT NULL,
  user_id        integer NOT NULL,
  timestamp      timestamp without time zone,
  origin         varchar(255) NOT NULL
);


--
-- Table structure for table `DeletedContact`
--
DROP TABLE deletedcontact;
CREATE TABLE deletedcontact (
  deletedcontact_contact_id integer NOT NULL,
  deletedcontact_addressbook_id    integer,
  deletedcontact_timestamp  timestamp without time zone,
  deletedcontact_origin     varchar(255) NOT NULL
);
--
-- deletedaddressbook indexes
--
ALTER TABLE ONLY deletedaddressbook
  ADD CONSTRAINT deletedaddressbook_pkey PRIMARY KEY (addressbook_id);


--
-- Table structure for table SyncedAddressbook
--
CREATE TABLE syncedaddressbook (
  user_id        integer NOT NULL,
  addressbook_id integer NOT NULL,
  timestamp      timestamp without time zone NOT NULL DEFAULT now()
);


INSERT INTO syncedaddressbook (user_id, addressbook_id, timestamp)
SELECT owner, id, now() FROM addressbook WHERE name = 'contacts' AND is_default = true;
--
-- addressbook indexes
--
ALTER TABLE ONLY syncedaddressbook
  ADD CONSTRAINT syncedaddressbook_pkey PRIMARY KEY (user_id, addressbook_id);

ALTER TABLE ONLY syncedaddressbook
    ADD CONSTRAINT syncedaddressbook_user_id_userobm_id_fkey FOREIGN KEY (user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE ONLY syncedaddressbook
    ADD CONSTRAINT syncedaddressbook_addressbook_id_addressbook_id_fkey FOREIGN KEY (addressbook_id) REFERENCES addressbook(id) ON UPDATE CASCADE ON DELETE CASCADE;


DROP TABLE IF EXISTS SynchedContact;

-- EventTag
CREATE TABLE eventtag ( 
  eventtag_id integer NOT NULL, 
  eventtag_user_id integer NOT NULL, 
  eventtag_label character varying(128) DEFAULT ''::character varying,
  eventtag_color character(7) default NULL 
);

CREATE SEQUENCE eventtag_eventtag_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER SEQUENCE eventtag_eventtag_id_seq OWNED BY eventtag.eventtag_id;
ALTER TABLE eventtag ALTER COLUMN eventtag_id SET DEFAULT nextval('eventtag_eventtag_id_seq'::regclass);
ALTER TABLE ONLY eventtag ADD CONSTRAINT eventtag_pkey PRIMARY KEY (eventtag_id);
ALTER TABLE event ADD COLUMN event_tag_id integer default NULL;
ALTER TABLE ONLY event ADD CONSTRAINT event_tag_id_eventtag_id_fkey FOREIGN KEY (event_tag_id) REFERENCES eventtag(eventtag_id) ON UPDATE CASCADE ON DELETE SET NULL;
ALTER TABLE ONLY eventtag ADD CONSTRAINT eventtag_user_id_userobm_id_fkey FOREIGN KEY (eventtag_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- EventTemplate
CREATE TABLE eventtemplate (
    eventtemplate_id integer NOT NULL,
    eventtemplate_domain_id integer NOT NULL,
    eventtemplate_timeupdate timestamp without time zone,
    eventtemplate_timecreate timestamp without time zone DEFAULT now(),
    eventtemplate_userupdate integer DEFAULT NULL,
    eventtemplate_usercreate integer DEFAULT NULL,
    eventtemplate_owner integer,
    eventtemplate_name character varying(255) DEFAULT NULL::character varying,
    eventtemplate_title character varying(255) DEFAULT NULL::character varying,
    eventtemplate_location character varying(100) DEFAULT NULL::character varying,
    eventtemplate_category1_id integer,
    eventtemplate_priority integer,
    eventtemplate_privacy integer,
    eventtemplate_date timestamp without time zone,
    eventtemplate_duration integer DEFAULT 0 NOT NULL,
    eventtemplate_allday boolean DEFAULT false,
    eventtemplate_repeatkind character varying(20) DEFAULT 'none' NOT NULL,
    eventtemplate_repeatfrequence integer,
    eventtemplate_repeatdays character varying(7) DEFAULT NULL::character varying,
    eventtemplate_endrepeat timestamp without time zone,
    eventtemplate_allow_documents boolean DEFAULT false,
    eventtemplate_alert integer DEFAULT 0 NOT NULL,
    eventtemplate_description text,
    eventtemplate_properties text,
    eventtemplate_tag_id integer default NULL,
    eventtemplate_user_ids text default NULL,
    eventtemplate_contact_ids text default NULL,
    eventtemplate_resource_ids text default NULL,
    eventtemplate_document_ids text default NULL,
    eventtemplate_group_ids text default NULL
);

CREATE SEQUENCE eventtemplate_eventtemplate_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;
    
ALTER SEQUENCE eventtemplate_eventtemplate_id_seq OWNED BY eventtemplate.eventtemplate_id;
ALTER TABLE eventtemplate ALTER COLUMN eventtemplate_id SET DEFAULT nextval('eventtemplate_eventtemplate_id_seq'::regclass);
ALTER TABLE ONLY eventtemplate ADD CONSTRAINT eventtemplate_pkey PRIMARY KEY (eventtemplate_id);
CREATE INDEX eventtemplate_category1_id_fkey ON eventtemplate (eventtemplate_category1_id);
CREATE INDEX eventtemplate_domain_id_fkey ON eventtemplate (eventtemplate_domain_id);
CREATE INDEX eventtemplate_owner_fkey ON eventtemplate (eventtemplate_owner);
CREATE INDEX eventtemplate_usercreate_fkey ON eventtemplate (eventtemplate_usercreate);
CREATE INDEX eventtemplate_userupdate_fkey ON eventtemplate (eventtemplate_userupdate);
ALTER TABLE ONLY eventtemplate ADD CONSTRAINT eventtemplate_category1_id_eventcategory1_id_fkey FOREIGN KEY (eventtemplate_category1_id) REFERENCES eventcategory1(eventcategory1_id) ON UPDATE CASCADE ON DELETE SET NULL;
ALTER TABLE ONLY eventtemplate ADD CONSTRAINT eventtemplate_domain_id_domain_id_fkey FOREIGN KEY (eventtemplate_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE ONLY eventtemplate ADD CONSTRAINT eventtemplate_owner_userobm_id_fkey FOREIGN KEY (eventtemplate_owner) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE ONLY eventtemplate ADD CONSTRAINT eventtemplate_usercreate_userobm_id_fkey FOREIGN KEY (eventtemplate_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;
ALTER TABLE ONLY eventtemplate ADD CONSTRAINT eventtemplate_userupdate_userobm_id_fkey FOREIGN KEY (eventtemplate_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;
ALTER TABLE ONLY eventtemplate ADD CONSTRAINT eventtemplate_tag_id_eventtag_id_fkey FOREIGN KEY (eventtemplate_tag_id) REFERENCES eventtag(eventtag_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Event's document attachments
ALTER TABLE event ADD COLUMN event_allow_documents boolean default false;
ALTER TABLE documentlink ADD COLUMN documentlink_usercreate integer DEFAULT NULL;
CREATE INDEX documentlink_usercreate_fkey ON documentlink (documentlink_usercreate);
ALTER TABLE ONLY documentlink ADD CONSTRAINT documentlink_usercreate_userobm_id_fkey FOREIGN KEY (documentlink_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

--
-- Event exceptions : drop parent_id from Event and add parent_id/child_id in EventException
--
ALTER TABLE eventexception DROP CONSTRAINT eventexception_event_id_event_id_fkey;
DROP INDEX eventexception_usercreate_fkey;

ALTER TABLE eventexception RENAME COLUMN eventexception_event_id TO eventexception_parent_id;
CREATE INDEX eventexception_parent_id_fkey ON eventexception (eventexception_parent_id);
ALTER TABLE ONLY eventexception
    ADD CONSTRAINT eventexception_parent_id_event_id_fkey FOREIGN KEY (eventexception_parent_id) REFERENCES event(event_id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE eventexception ADD COLUMN eventexception_child_id INTEGER DEFAULT NULL;
CREATE INDEX eventexception_child_id_fkey ON eventexception (eventexception_child_id);
ALTER TABLE ONLY eventexception
    ADD CONSTRAINT eventexception_child_id_event_id_fkey FOREIGN KEY (eventexception_child_id) REFERENCES event(event_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- useless because event_parent_id was not used
-- UPDATE EventException ee
-- SET ee.eventexception_child_id = e.event_id
-- FROM Event e
-- WHERE e.event_parent_id = ee.eventexception_parent_id;

ALTER TABLE event DROP COLUMN event_parent_id;
CREATE INDEX event_ext_id_idx ON event (event_ext_id);
ALTER TABLE event ALTER COLUMN event_ext_id TYPE varchar(300);


--
-- Table structure for table plannedtask
--
CREATE TYPE taskperiod AS ENUM (
  'MORNING',
  'AFTERNOON',
  'ALLDAY'
);

CREATE TABLE plannedtask (
  plannedtask_id integer NOT NULL,
  plannedtask_domain_id integer default 0,
  plannedtask_timeupdate timestamp without time zone,
  plannedtask_timecreate timestamp without time zone DEFAULT now(),
  plannedtask_userupdate integer default NULL,
  plannedtask_usercreate integer default NULL,
  plannedtask_user_id integer default NULL,
  plannedtask_datebegin date,
  plannedtask_dateend date,
  plannedtask_period taskperiod DEFAULT 'MORNING'::taskperiod,
  plannedtask_project_id integer default NULL,
  plannedtask_tasktype_id integer default NULL,
  plannedtask_overrun boolean DEFAULT false,
  plannedtask_comment text
);

--
-- Name: plannedtask_plannedtask_id_seq; Type: SEQUENCE; Schema: public; Owner: -
--
CREATE SEQUENCE plannedtask_plannedtask_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER SEQUENCE plannedtask_plannedtask_id_seq OWNED BY plannedtask.plannedtask_id;
ALTER TABLE plannedtask ALTER COLUMN plannedtask_id SET DEFAULT nextval('plannedtask_plannedtask_id_seq'::regclass);
ALTER TABLE ONLY plannedtask
    ADD CONSTRAINT plannedtask_pkey PRIMARY KEY (plannedtask_id);
CREATE INDEX plannedtask_domain_id_fkey ON plannedtask (plannedtask_domain_id);
CREATE INDEX plannedtask_user_id_fkey ON plannedtask (plannedtask_user_id);
CREATE INDEX plannedtask_datebegin_fkey ON plannedtask (plannedtask_datebegin);
CREATE INDEX plannedtask_dateend_fkey ON plannedtask (plannedtask_dateend);
ALTER TABLE ONLY plannedtask
    ADD CONSTRAINT plannedtask_domain_id_domain_id_fkey FOREIGN KEY (plannedtask_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE ONLY plannedtask
    ADD CONSTRAINT plannedtask_userupdate_userobm_id_fkey FOREIGN KEY (plannedtask_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;
ALTER TABLE ONLY plannedtask
    ADD CONSTRAINT plannedtask_usercreate_userobm_id_fkey FOREIGN KEY (plannedtask_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;
ALTER TABLE ONLY plannedtask
    ADD CONSTRAINT plannedtask_project_id_project_id_fkey FOREIGN KEY (plannedtask_project_id) REFERENCES project(project_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE ONLY plannedtask
    ADD CONSTRAINT plannedtask_tasktype_id_tasktype_id_fkey FOREIGN KEY (plannedtask_tasktype_id) REFERENCES tasktype(tasktype_id) ON UPDATE CASCADE ON DELETE SET NULL;
ALTER TABLE ONLY plannedtask
    ADD CONSTRAINT plannedtask_user_id_userobm_id_fkey FOREIGN KEY (plannedtask_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

--
-- Table structure for table TaskTypeGroup
--
CREATE TABLE tasktypegroup (
  tasktypegroup_id integer NOT NULL,
  tasktypegroup_domain_id integer NOT NULL,
  tasktypegroup_timeupdate timestamp without time zone,
  tasktypegroup_timecreate timestamp without time zone DEFAULT now(),
  tasktypegroup_userupdate integer default NULL,
  tasktypegroup_usercreate integer default NULL,
  tasktypegroup_label varchar(32),
  tasktypegroup_code varchar(20),
  tasktypegroup_bgcolor varchar(7) default NULL,
  tasktypegroup_fgcolor varchar(7) default NULL
);

CREATE SEQUENCE tasktypegroup_tasktypegroup_id_seq
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER SEQUENCE tasktypegroup_tasktypegroup_id_seq OWNED BY tasktypegroup.tasktypegroup_id;
ALTER TABLE tasktypegroup ALTER COLUMN tasktypegroup_id SET DEFAULT nextval('tasktypegroup_tasktypegroup_id_seq'::regclass);
ALTER TABLE ONLY tasktypegroup
    ADD CONSTRAINT tasktypegroup_pkey PRIMARY KEY (tasktypegroup_id);
CREATE INDEX tasktypegroup_domain_id_fkey ON tasktypegroup (tasktypegroup_domain_id);
CREATE INDEX tasktypegroup_userupdate_fkey ON tasktypegroup (tasktypegroup_userupdate);
CREATE INDEX tasktypegroup_usercreate_fkey ON tasktypegroup (tasktypegroup_usercreate);

ALTER TABLE tasktype ADD tasktype_tasktypegroup_id integer;
CREATE INDEX tasktype_tasktypegroup_id_fkey ON tasktype (tasktype_tasktypegroup_id);
ALTER TABLE ONLY tasktype
  ADD CONSTRAINT tasktype_tasktypegroup_id_tasktypegroup_id_fkey FOREIGN KEY (tasktype_tasktypegroup_id) REFERENCES tasktypegroup (tasktypegroup_id) ON DELETE SET NULL ON UPDATE CASCADE;



CREATE TABLE opush_device (
       id 		SERIAL PRIMARY KEY,
       identifier 	VARCHAR(255) NOT NULL,
       owner		INTEGER REFERENCES userobm(userobm_id) ON DELETE CASCADE,
       type		VARCHAR(64) NOT NULL
);

CREATE TABLE opush_folder_mapping (
       id		SERIAL PRIMARY KEY,
       device_id	INTEGER NOT NULL REFERENCES opush_device(id) ON DELETE CASCADE,
       collection	VARCHAR(255) NOT NULL
);

-- store last sync dates
CREATE TABLE opush_sync_state (
       sync_key		VARCHAR(64) UNIQUE NOT NULL,
       collection_id	INTEGER NOT NULL REFERENCES opush_folder_mapping(id) ON DELETE CASCADE,
       device_id	INTEGER NOT NULL REFERENCES opush_device(id) ON DELETE CASCADE,
       last_sync	TIMESTAMP NOT NULL
);
ALTER TABLE ONLY opush_sync_state ADD CONSTRAINT 
unique_opush_col_dev UNIQUE (collection_id, device_id);

CREATE TABLE opush_sec_policy (
       id				SERIAL PRIMARY KEY,
       device_password_enabled		BOOLEAN DEFAULT FALSE
       -- add other fields...
);

-- A row exists if a user is allowed to Sync.
CREATE TABLE opush_sync_perms (
       owner		INTEGER REFERENCES userobm(userobm_id) ON DELETE CASCADE,
       device_id	INTEGER NOT NULL REFERENCES opush_device(id) ON DELETE CASCADE,
       -- add not null later
       policy		INTEGER REFERENCES opush_sec_policy(id) ON DELETE SET NULL
);

CREATE TABLE opush_sync_mail (
        collection_id   INTEGER NOT NULL REFERENCES opush_folder_mapping(id) ON DELETE CASCADE,
        device_id       INTEGER NOT NULL REFERENCES opush_device(id) ON DELETE CASCADE,
        mail_uid        INTEGER NOT NULL
);



INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'people', 'userobm_direction', 11, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'people', 'userobm_service', 12, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'people', 'userobm_address', 13, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'people', 'userobm_town', 14, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'people', 'userobm_zipcode', 15, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'user', 'userobm_direction', 26, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'user', 'userobm_service', 27, 1);

-- ----------------------------------------------------------------------------
-- Adding needed system users
-- obmSatellite
DELETE FROM UserSystem WHERE usersystem_login='obmsatelliterequest';
INSERT INTO UserSystem (usersystem_login, usersystem_password, usersystem_uid, usersystem_gid,usersystem_homedir, usersystem_lastname, usersystem_firstname, usersystem_shell) VALUES ( 'obmsatelliterequest', 'PgpTWb7x', 201, 65534, '/', 'OBM Satellite', 'HTTP auth request', '/bin/false' );

-- Reset all contacts display prefs
DELETE FROM DisplayPref WHERE display_user_id IS NOT NULL and display_entity = 'contact';

-- ----------------------------------------------------------------------------
-- Delete unused domain properties
DELETE FROM DomainProperty WHERE domainproperty_key='last_public_contact_export';
DELETE FROM DomainPropertyValue WHERE domainpropertyvalue_property_key='last_public_contact_export';

-- ----------------------------------------------------------------------------
-- Obm product ID
INSERT INTO ObmInfo SELECT 'product_id', LPAD(MD5(FLOOR(EXTRACT(EPOCH FROM TIMESTAMP 'NOW()')*RANDOM())::text), 24);
UPDATE Event SET event_ext_id = (select obminfo_value from obminfo where obminfo_name = 'prod_id') || MD5(FLOOR(EXTRACT(EPOCH FROM TIMESTAMP 'NOW()')*RANDOM())::text) || MD5(FLOOR(EXTRACT(EPOCH FROM TIMESTAMP 'NOW()')*RANDOM())::text) || MD5(FLOOR(EXTRACT(EPOCH FROM TIMESTAMP 'NOW()')*RANDOM())::text) || MD5(FLOOR(EXTRACT(EPOCH FROM TIMESTAMP 'NOW()')*RANDOM())::text) || MD5(FLOOR(EXTRACT(EPOCH FROM TIMESTAMP 'NOW()')*RANDOM())::text)
WHERE event_ext_id IS NULL OR event_ext_id = '';


UPDATE addressbook SET timecreate=NOW() WHERE timecreate IS NULL;
ALTER TABLE addressbook ALTER COLUMN timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_addressbook_change() RETURNS trigger AS '
BEGIN
new.timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_addressbook_create() RETURNS trigger AS '
BEGIN
new.timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER addressbook_created BEFORE INSERT ON addressbook FOR EACH ROW EXECUTE PROCEDURE on_addressbook_create();
CREATE TRIGGER addressbook_changed BEFORE UPDATE ON addressbook FOR EACH ROW EXECUTE PROCEDURE on_addressbook_change();

UPDATE plannedtask SET plannedtask_timecreate=NOW() WHERE plannedtask_timecreate IS NULL;
ALTER TABLE plannedtask ALTER COLUMN plannedtask_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_plannedtask_change() RETURNS trigger AS '
BEGIN
new.plannedtask_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_plannedtask_create() RETURNS trigger AS '
BEGIN
new.plannedtask_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER plannedtask_created BEFORE INSERT ON plannedtask FOR EACH ROW EXECUTE PROCEDURE on_plannedtask_create();
CREATE TRIGGER plannedtask_changed BEFORE UPDATE ON plannedtask FOR EACH ROW EXECUTE PROCEDURE on_plannedtask_change();

UPDATE eventtemplate SET eventtemplate_timecreate=NOW() WHERE eventtemplate_timecreate IS NULL;
ALTER TABLE eventtemplate ALTER COLUMN eventtemplate_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_eventtemplate_change() RETURNS trigger AS '
BEGIN
new.eventtemplate_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_eventtemplate_create() RETURNS trigger AS '
BEGIN
new.eventtemplate_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER eventtemplate_created BEFORE INSERT ON eventtemplate FOR EACH ROW EXECUTE PROCEDURE on_eventtemplate_create();
CREATE TRIGGER eventtemplate_changed BEFORE UPDATE ON eventtemplate FOR EACH ROW EXECUTE PROCEDURE on_eventtemplate_change();


UPDATE tasktypegroup SET tasktypegroup_timecreate=NOW() WHERE tasktypegroup_timecreate IS NULL;
ALTER TABLE tasktypegroup ALTER COLUMN tasktypegroup_timecreate SET DEFAULT NOW();

CREATE OR REPLACE FUNCTION on_tasktypegroup_change() RETURNS trigger AS '
BEGIN
new.tasktypegroup_timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_tasktypegroup_create() RETURNS trigger AS '
BEGIN
new.tasktypegroup_timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER tasktypegroup_created BEFORE INSERT ON tasktypegroup FOR EACH ROW EXECUTE PROCEDURE on_tasktypegroup_create();
CREATE TRIGGER tasktypegroup_changed BEFORE UPDATE ON tasktypegroup FOR EACH ROW EXECUTE PROCEDURE on_tasktypegroup_change();

-- -----------------------------------------------------------------------------
-- Write that the 2.2->2.3 is completed
UPDATE ObmInfo SET obminfo_value='2.3.0' WHERE obminfo_name='db_version';
