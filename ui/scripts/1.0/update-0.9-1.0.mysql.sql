-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM MySQL Database from 0.9 to 1.0                              //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Global Information table
-------------------------------------------------------------------------------
UPDATE ObmInfo set obminfo_value='1.0' where obminfo_name='db_version';


-------------------------------------------------------------------------------
-- Update Display Prefs (should already been done)
-------------------------------------------------------------------------------
UPDATE DisplayPref SET display_fieldname='company_name' WHERE display_entity='contact' AND display_fieldname='contact_company_name';


-------------------------------------------------------------------------------
-- Update Todo table
-------------------------------------------------------------------------------
-- Add columns privacy, dateend, percent, status, webpage
ALTER TABLE Todo ADD COLUMN todo_privacy int(2) NOT NULL DEFAULT 0 after todo_user;
ALTER TABLE Todo ADD COLUMN todo_dateend timestamp(14) after todo_deadline;
ALTER TABLE Todo ADD COLUMN todo_percent int(8) after todo_priority;
ALTER TABLE Todo ADD COLUMN todo_status varchar(32) after todo_title;
ALTER TABLE Todo ADD COLUMN todo_webpage varchar(255) after todo_status;


-------------------------------------------------------------------------------
-- Tables needed for Connectors sync
-------------------------------------------------------------------------------
--
-- Table structure for the table 'DeletedCalendarEvent'
--
CREATE TABLE DeletedCalendarEvent (
  deletedcalendarevent_event_id   int(8),
  deletedcalendarevent_user_id    int(8),
  deletedcalendarevent_timestamp  timestamp(14),
  INDEX idx_dce_event (deletedcalendarevent_event_id),
  INDEX idx_dce_user (deletedcalendarevent_user_id)
);


--
-- Table structure for the table 'DeletedContact'
--
CREATE TABLE DeletedContact (
  deletedcontact_contact_id  int(8),
  deletedcontact_timestamp   timestamp(14),
  PRIMARY KEY (deletedcontact_contact_id)
);


--
-- Table structure for the table 'DeletedUser'
--
CREATE TABLE DeletedUser (
  deleteduser_user_id    int(8),
  deleteduser_timestamp  timestamp(14),
  PRIMARY KEY (deleteduser_user_id)
);


--
-- Table structure for the table 'DeletedTodo'
--
CREATE TABLE DeletedTodo (
  deletedtodo_todo_id    int(8),
  deletedtodo_timestamp  timestamp(14),
  PRIMARY KEY (deletedtodo_todo_id)
);


-------------------------------------------------------------------------------
-- Tables needed for Resources module
-------------------------------------------------------------------------------

--
-- Table structure for table 'Resource'
--
CREATE TABLE Resource (
  resource_id                int(8) DEFAULT 0 NOT NULL auto_increment,
  resource_timeupdate        timestamp(14),
  resource_timecreate        timestamp(14),
  resource_userupdate        int(8),
  resource_usercreate        int(8),
  resource_label             varchar(32) DEFAULT '' NOT NULL,
  resource_description       varchar(255),
  resource_qty               int(8) DEFAULT 0 NOT NULL,
  PRIMARY KEY (resource_id),
  UNIQUE k_label_resource (resource_label)
);

--
-- Table structure for table 'RGroup'
--
CREATE TABLE RGroup (
  rgroup_id          int(8) NOT NULL auto_increment,
  rgroup_timeupdate  timestamp(14),
  rgroup_timecreate  timestamp(14),
  rgroup_userupdate  int(8),
  rgroup_usercreate  int(8),
  rgroup_privacy     int(2) NULL DEFAULT 0,
  rgroup_name        varchar(32) NOT NULL,
  rgroup_desc        varchar(128),
  PRIMARY KEY (rgroup_id)
);

--
-- New table 'RGroupKind'
--
CREATE TABLE RGroupKind (
  rgroupkind_id          int(8) NOT NULL auto_increment,
  rgroupkind_shortlabel  varchar(3) NOT NULL DEFAULT '',
  rgroupkind_longlabel   varchar(40) NOT NULL DEFAULT '',
  PRIMARY KEY (rgroupkind_id)
);

--
-- Table structure for table 'ResourceGroup'
--
CREATE TABLE ResourceGroup (
  resourcegroup_rgroup_id    int(8) DEFAULT 0 NOT NULL,
  resourcegroup_resource_id  int(8) DEFAULT 0 NOT NULL
);

-------------------------------------------------------------------------------
-- Inserrt Display Prefs (Resource modules)
-------------------------------------------------------------------------------

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resource', 'resource_label', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resource', 'resource_description', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resource', 'resource_qty', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resource', 'resource_id', 2, 0);

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup', 'rgroup_name', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup', 'rgroup_desc', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup', 'rgroup_nb_resource', 3, 2);

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup_resource', 'resourcegroup_resource_label', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup_resource', 'resourcegroup_resource_desc', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup_resource', 'resourcegroup_resource_qty', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup_resource', 'resourcegroup_resource_nb_resource', 4, 2);

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup_resourcegroup', 'rgroup_name', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup_resourcegroup', 'rgroup_desc', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup_resourcegroup', 'rgroup_email', 3, 1);



