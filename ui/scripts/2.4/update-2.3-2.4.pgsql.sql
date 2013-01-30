-- Write that the 2.3->2.4 has started
UPDATE ObmInfo SET obminfo_value='2.3.x->2.4.0' WHERE obminfo_name='db_version';
-- -----------------------------------------------------------------------------

--
-- Table structure for `calendarcolor`
--
DROP TABLE IF EXISTS calendarcolor;
CREATE TABLE calendarcolor (
  user_id INTEGER NOT NULL,
  entity_id INTEGER NOT NULL,
  eventowner INTEGER DEFAULT NULL
);

ALTER TABLE calendarcolor 
  ADD CONSTRAINT pkey PRIMARY KEY (user_id,entity_id);

CREATE INDEX user_id_fkey ON calendarcolor(user_id);
CREATE INDEX entity_id_fkey ON calendarcolor(entity_id);

ALTER TABLE calendarcolor 
  ADD CONSTRAINT user_id_user_id_fkey FOREIGN KEY (user_id)
  REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE calendarcolor 
  ADD CONSTRAINT entity_id_entity_id_fkey FOREIGN KEY (entity_id)
  REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;

--
-- Update Resource
--
ALTER TABLE resource
ALTER resource_delegation TYPE character varying(256);

--
-- Update deleted
--
ALTER TABLE deleted
ALTER deleted_delegation TYPE character varying(256);

--
-- Update host
--
ALTER TABLE host
ALTER host_delegation TYPE character varying(256);

--
-- Update p_host
--
ALTER TABLE p_host
ALTER host_delegation TYPE character varying(256);

--
-- Update mailshare
--
ALTER TABLE mailshare
ALTER mailshare_delegation TYPE character varying(256);

--
-- Update p_mailshare
--
ALTER TABLE p_mailshare
ALTER mailshare_delegation TYPE character varying(256);

--
-- Update ugroup
--
ALTER TABLE ugroup
ALTER group_delegation TYPE character varying(256);

--
-- Update p_ugroup
--
ALTER TABLE p_ugroup
ALTER group_delegation TYPE character varying(256);

--
-- Update updated
--
ALTER TABLE updated
ALTER updated_delegation TYPE character varying(256);

--
-- Update updatedlinks
--
ALTER TABLE updatedlinks
ALTER updatedlinks_delegation TYPE character varying(256);

--
-- Update userobm
--
ALTER TABLE userobm
ADD COLUMN userobm_commonname varchar(256) default '', 
ALTER userobm_delegation TYPE character varying(256),
ALTER userobm_delegation_target TYPE character varying(256),
ALTER userobm_login TYPE character varying(80),
ALTER userobm_kind TYPE character varying(64),
ALTER userobm_title TYPE character varying(256);
--
-- Update contact
--
ALTER TABLE contact
ADD COLUMN contact_commonname varchar(256) default '';
--
-- Update p_userobm
--
ALTER TABLE p_userobm 
ADD COLUMN userobm_commonname varchar(256) default '', 
ALTER userobm_delegation TYPE character varying(256),
ALTER userobm_delegation_target TYPE character varying(256),
ALTER userobm_login TYPE character varying(80),
ALTER userobm_kind TYPE character varying(64),
ALTER userobm_title TYPE character varying(256);


--
-- Update obmbookmarkproperty
--
ALTER TABLE obmbookmarkproperty
ALTER obmbookmarkproperty_value TYPE character varying(256);



--
-- Table structure for table `userpattern`
--
CREATE TABLE userpattern (
  id          integer NOT NULL,
  domain_id   integer NOT NULL,
  timeupdate  timestamp without time zone,
  timecreate  timestamp without time zone,
  userupdate  integer default NULL,
  usercreate  integer default NULL,
  title       varchar(255) NOT NULL,
  description text default NULL
);

--
-- Table structure for table `userpattern_property`
--
CREATE TABLE userpattern_property (
  userpattern_id integer NOT NULL,
  attribute      varchar(255) NOT NULL,
  value          text default NULL
);

--
-- userpattern id sequence
--
CREATE SEQUENCE userpattern_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER SEQUENCE userpattern_id_seq OWNED BY userpattern.id;
ALTER TABLE userpattern ALTER COLUMN id SET DEFAULT nextval('userpattern_id_seq'::regclass);

--
-- userpattern indexes
--
ALTER TABLE ONLY userpattern
    ADD CONSTRAINT userpattern_pkey PRIMARY KEY (id);
CREATE INDEX userpattern_domain_id_domain_id_fkey ON userpattern (domain_id);
CREATE INDEX userpattern_userupdate_userobm_id_fkey ON userpattern (userupdate);
CREATE INDEX userpattern_usercreate_userobm_id_fkey ON userpattern (usercreate);

--
-- userpattern fkey
--
ALTER TABLE ONLY userpattern
    ADD CONSTRAINT userpattern_domain_id_domain_id_fkey FOREIGN KEY (domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE ONLY userpattern
    ADD CONSTRAINT userpattern_userupdate_userobm_id_fkey FOREIGN KEY (userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;
ALTER TABLE ONLY userpattern
    ADD CONSTRAINT userpattern_usercreate_userobm_id_fkey FOREIGN KEY (usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

--
-- userpattern_property indexes
--
ALTER TABLE ONLY userpattern_property
    ADD CONSTRAINT userpattern_property_pkey PRIMARY KEY (userpattern_id,attribute);
CREATE INDEX userpattern_property_userpattern_id_userpattern_id_fkey ON userpattern_property (userpattern_id);

--
-- userpattern_property fkey
--
ALTER TABLE ONLY userpattern_property
    ADD CONSTRAINT userpattern_property_userpattern_id_userpattern_id_fkey FOREIGN KEY (userpattern_id) REFERENCES userpattern(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Display Prefs
--
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'userpattern','title',1,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'userpattern','description',2,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'userpattern','timecreate',3,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'userpattern','timeupdate',4,1);

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'group_contact', 'group_contact_lastname', 1, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'group_contact', 'group_contact_firstname', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'group_contact', 'group_contact_phone', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'group_contact', 'group_contact_email', 4, 1);


--
-- Triggers
--
CREATE OR REPLACE FUNCTION on_userpattern_change() RETURNS trigger AS '
BEGIN
new.timeupdate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE OR REPLACE FUNCTION on_userpattern_create() RETURNS trigger AS '
BEGIN
new.timecreate := current_timestamp;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER userpattern_created BEFORE INSERT ON userpattern FOR EACH ROW EXECUTE PROCEDURE on_userpattern_create();
CREATE TRIGGER userpattern_changed BEFORE UPDATE ON userpattern FOR EACH ROW EXECUTE PROCEDURE on_userpattern_change();


--
-- Fix trigger userobm_changed
--
DROP TRIGGER userobm_changed ON userobm;
CREATE OR REPLACE FUNCTION on_userobm_change() RETURNS trigger AS '
BEGIN
IF new.userobm_timelastaccess = old.userobm_timelastaccess THEN
	new.userobm_timeupdate := current_timestamp;
END IF;
RETURN new;
END
' LANGUAGE plpgsql;
CREATE TRIGGER userobm_changed BEFORE UPDATE ON userobm FOR EACH ROW EXECUTE PROCEDURE on_userobm_change();


--
-- Token
--
DROP TABLE IF EXISTS token;
CREATE TABLE token (
  token varchar(300) NOT NULL, 
  property varchar(255) NOT NULL, 
  value varchar(255) NOT NULL
);

--
-- Table structure for table `contactgroup`
--

DROP TABLE IF EXISTS contactgroup;
CREATE TABLE contactgroup (
  contact_id integer NOT NULL,
  group_id integer NOT NULL
);

ALTER TABLE ONLY contactgroup
    ADD CONSTRAINT contactgroup_pkey PRIMARY KEY (contact_id, group_id);

CREATE INDEX contactgroup_contact_id_contact_id_fkey ON contactgroup (contact_id);
CREATE INDEX contactgroup_group_id_group_id_fkey ON contactgroup (group_id);

ALTER TABLE ONLY contactgroup
    ADD CONSTRAINT contactgroup_contact_id_contact_id_fkey FOREIGN KEY (contact_id) REFERENCES Contact(contact_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE ONLY contactgroup
    ADD CONSTRAINT contactgroup_group_id_group_id_fkey FOREIGN KEY (group_id) REFERENCES UGroup(group_id) ON UPDATE CASCADE ON DELETE CASCADE;

--
-- Table structure for table `_contactgroup`
--

DROP TABLE IF EXISTS _contactgroup;
CREATE TABLE _contactgroup (
  contact_id integer NOT NULL,
  group_id integer NOT NULL
);

ALTER TABLE ONLY _contactgroup
    ADD CONSTRAINT _contactgroup_pkey PRIMARY KEY (contact_id, group_id);

CREATE INDEX _contactgroup_contact_id_contact_id_fkey ON _contactgroup (contact_id);
CREATE INDEX _contactgroup_group_id_group_id_fkey ON _contactgroup (group_id);    


ALTER TABLE ONLY _contactgroup
    ADD CONSTRAINT _contactgroup_contact_id_contact_id_fkey FOREIGN KEY (contact_id) REFERENCES Contact(contact_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE ONLY contactgroup
    ADD CONSTRAINT _contactgroup_group_id_group_id_fkey FOREIGN KEY (group_id) REFERENCES UGroup(group_id) ON UPDATE CASCADE ON DELETE CASCADE;

--
-- Table structure for table `P__contactgroup`
--

DROP TABLE IF EXISTS P__contactgroup;
CREATE TABLE P__contactgroup (LIKE _contactgroup);


--
-- Table structure for table `P_CategoryLink`
--

DROP TABLE IF EXISTS P_CategoryLink;
CREATE TABLE P_CategoryLink (LIKE CategoryLink);


--
-- Domain Property
--
INSERT INTO DomainProperty VALUES ('mailshares_quota_default','integer','0','0');
INSERT INTO DomainProperty VALUES ('mailshares_quota_max','integer','0','0');


--
-- Table structure for table `field`
--
DROP TABLE IF EXISTS field;
CREATE TABLE field (
  id            integer NOT NULL,
  entity_id     integer NOT NULL,
  field         varchar(255),
  value         text
);

CREATE SEQUENCE field_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER SEQUENCE field_id_seq OWNED BY field.id;
ALTER TABLE field ALTER COLUMN id SET DEFAULT nextval('field_id_seq'::regclass);

ALTER TABLE ONLY field
    ADD CONSTRAINT field_id_pkey PRIMARY KEY (id);
ALTER TABLE ONLY field
    ADD CONSTRAINT field_entity_id_fkey FOREIGN KEY (entity_id) REFERENCES Entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;

--
-- Table structure for table `P_field`
--

DROP TABLE IF EXISTS P_field;
CREATE TABLE P_field (LIKE field);


--
-- Resource group delegation
--
ALTER TABLE RGroup ADD COLUMN rgroup_delegation varchar(256) default '';

--
-- update category_code length to 20
-- add unicity constraint
--
ALTER TABLE Category ALTER COLUMN category_code TYPE character varying(100);
ALTER TABLE Category ADD CONSTRAINT categorycategory_categorycode_uniquekey UNIQUE (category_domain_id,category_category,category_code,category_label);

--
-- possibility to save the forced insertion state into an event template
--
ALTER TABLE EventTemplate ADD COLUMN eventtemplate_force_insertion boolean DEFAULT false;
ALTER TABLE EventTemplate ADD COLUMN eventtemplate_opacity vopacity DEFAULT 'OPAQUE'::vopacity;
ALTER TABLE EventTemplate ADD COLUMN eventtemplate_show_user_calendar boolean DEFAULT false;
ALTER TABLE EventTemplate ADD COLUMN eventtemplate_show_resource_calendar boolean DEFAULT false;

-- 
-- empty sync_state table to force initial sync for all client devices
-- and add a id column
--
DELETE FROM opush_sync_state;
ALTER TABLE opush_sync_state ADD COLUMN id SERIAL PRIMARY KEY;

CREATE TABLE opush_synced_item (
       id		SERIAL PRIMARY KEY,
       sync_state_id	INTEGER NOT NULL REFERENCES opush_sync_state(id) ON DELETE CASCADE,
       item_id		INTEGER NOT NULL
);

CREATE TABLE opush_event_mapping (
       id               SERIAL PRIMARY KEY,
       device_id        INTEGER NOT NULL REFERENCES opush_device(id) ON DELETE CASCADE,
       event_id         INTEGER NOT NULL REFERENCES event(event_id) ON DELETE CASCADE,
       event_uid        VARCHAR(300) NOT NULL,
       UNIQUE (device_id, event_id)
);

DELETE FROM opush_invitation_mapping;
ALTER TABLE opush_invitation_mapping DROP COLUMN event_uid;
ALTER TABLE opush_invitation_mapping ADD COLUMN event_uid INTEGER NOT NULL REFERENCES event(event_id) ON DELETE CASCADE;

CREATE OR REPLACE FUNCTION UUID()
  RETURNS uuid AS
$BODY$
 SELECT CAST(md5(current_database()|| user ||current_timestamp ||random()) as uuid)
$BODY$
  LANGUAGE 'sql' VOLATILE;

UPDATE event SET event_ext_id=UUID() WHERE event_ext_id IS NULL;
ALTER TABLE event ALTER event_ext_id SET NOT NULL;

ALTER TABLE opush_synced_item ADD COLUMN addition BOOLEAN;
UPDATE opush_synced_item SET addition='1';
ALTER TABLE opush_synced_item ALTER addition SET NOT NULL;

------------------------------------------------------------------------
-- Write that the 2.3->2.4 is completed
UPDATE ObmInfo SET obminfo_value='2.4.0' WHERE obminfo_name='db_version';

