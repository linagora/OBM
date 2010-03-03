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
ALTER userobm_delegation TYPE character varying(256),
ALTER userobm_delegation_target TYPE character varying(256);

--
-- Update p_userobm
--
ALTER TABLE p_userobm 
ALTER userobm_delegation TYPE character varying(256),
ALTER userobm_delegation_target TYPE character varying(256);

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
INSERT INTO P__contactgroup SELECT * FROM _contactgroup;


--
-- Domain Property
--
INSERT INTO DomainProperty VALUES ('mailshares_quota_default','integer','0','0');
INSERT INTO DomainProperty VALUES ('mailshares_quota_max','integer','0','0');

-- 
-- Contact group migration
--
CREATE TABLE tmp_groupemails (id serial, email  varchar(255),  gid integer);

CREATE TABLE tmp_groupcontacts (id integer, email  varchar(255), gid  integer, cid integer);

CREATE TABLE tmp_contactsgroup ( email  varchar(255), gid  integer, cid integer);
 
INSERT INTO tmp_groupemails (email, gid) SELECT  (regexp_split_to_table(group_contacts, E'\\r\\n')), group_id FROM UGroup WHERE group_contacts != '' AND group_contacts IS NOT NULL ;

INSERT INTO tmp_groupcontacts (id, cid, gid, email) 
SELECT tmp_groupemails.id, contactentity_contact_id, gid, email 
FROM ContactEntity 
INNER JOIN Email ON contactentity_entity_id = email_entity_id 
INNER JOIN tmp_groupemails ON trim(email) = trim(email_address)
INNER JOIN UGroup ON group_id = gid
INNER JOIN Contact ON contact_id = contactentity_contact_id 
INNER JOIN Addressbook ON Addressbook.id = contact_addressbook_id 
WHERE Addressbook.domain_id = group_domain_id 
AND Addressbook.is_default = 1 AND Addressbook.name = 'public_contacts'; 

DELETE FROM tmp_groupemails WHERE id IN (SELECT id FROM tmp_groupcontacts);

INSERT INTO Contact (contact_domain_id, contact_timecreate, contact_usercreate, contact_addressbook_id, contact_lastname, contact_collected, contact_origin)
SELECT group_domain_id, NOW(), group_usercreate, Addressbook.id, email, true, 'obm-storage-migration-2.4'
FROM tmp_groupemails
INNER JOIN UGroup ON group_id = gid
INNER JOIN Addressbook ON Addressbook.domain_id = group_domain_id 
WHERE Addressbook.is_default = 1 AND Addressbook.name = 'public_contacts';  

ALTER TABLE ContactEntity DISABLE TRIGGER ALL;

INSERT INTO ContactEntity (contactentity_contact_id, contactentity_entity_id) 
SELECT contact_id, nextval('entity_entity_id_seq') 
FROM Contact  
LEFT JOIN ContactEntity ON contactentity_contact_id = contact_id 
WHERE contactentity_contact_id IS NULL;

ALTER TABLE Email DISABLE TRIGGER ALL;

INSERT INTO Email (email_entity_id, email_label, email_address) 
SELECT contactentity_entity_id, 'INTERNET;X-OBM-Ref1', contact_lastname 
FROM Contact 
INNER JOIN ContactEntity ON contactentity_contact_id = contact_id 
LEFT JOIN Entity ON entity_id = contactentity_entity_id 
WHERE entity_id IS NULL;

INSERT INTO Entity (entity_id , entity_mailing) 
SELECT contactentity_entity_id, true 
FROM ContactEntity 
LEFT JOIN Entity ON contactentity_entity_id = entity_id 
WHERE entity_id IS NULL ;

ALTER TABLE ContactEntity ENABLE TRIGGER ALL;

ALTER TABLE Email ENABLE TRIGGER ALL;


INSERT INTO tmp_groupcontacts (id, cid, gid, email) 
SELECT id, contactentity_contact_id, gid, email 
FROM ContactEntity 
INNER JOIN Email ON contactentity_entity_id = email_entity_id 
INNER JOIN tmp_groupemails ON trim(email) = trim(email_address);

DELETE FROM tmp_groupemails WHERE id IN (SELECT id FROM tmp_groupcontacts);

INSERT INTO tmp_contactsgroup (email, gid, cid) 
SELECT email, gid, MAX(cid) 
FROM tmp_groupcontacts 
GROUP BY email, gid;

INSERT INTO contactgroup (contact_id, group_id) 
SELECT cid, gid 
FROM tmp_contactsgroup;


DROP TABLE tmp_groupemails;

DROP TABLE tmp_groupcontacts;

DROP TABLE tmp_contactsgroup;

ALTER TABLE Contact DROP COLUMN contact_privacy;


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

--
-- Name: field_field_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: -
--
CREATE SEQUENCE field_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;

ALTER SEQUENCE field_id_seq OWNED BY field.id;
ALTER TABLE field ALTER COLUMN id SET DEFAULT nextval('field_id_seq'::regclass);

--
-- field fkey
--
ALTER TABLE ONLY field
    ADD CONSTRAINT field_entity_id_fkey FOREIGN KEY (entity_id) REFERENCES Entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;




------------------------------------------------------------------------
-- Write that the 2.3->2.4 is completed
UPDATE ObmInfo SET obminfo_value='2.4.0' WHERE obminfo_name='db_version';

