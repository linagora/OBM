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
    ADD CONSTRAINT userpattern_property_userpattern_id_userpattern_id_fkey FOREIGN KEY (userpattern_id) REFERENCES userpattern(userpattern_id) ON UPDATE CASCADE ON DELETE CASCADE;


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
-- Token
--
DROP TABLE IF EXISTS token;
CREATE TABLE token (
  token varchar(300) NOT NULL, 
  property varchar(255) NOT NULL, 
  value varchar(255) NOT NULL
);

------------------------------------------------------------------------
-- Write that the 2.3->2.4 is completed
UPDATE ObmInfo SET obminfo_value='2.4.0' WHERE obminfo_name='db_version';

