-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM PostgreSQL Database from 0.9 to 1.0                         //
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
ALTER TABLE Todo ADD COLUMN todo_privacy integer;
UPDATE Todo set todo_privacy=0;
ALTER TABLE Todo ALTER COLUMN todo_privacy SET NOT NULL;
ALTER TABLE Todo ALTER COLUMN todo_privacy SET DEFAULT 0;
ALTER TABLE Todo ADD COLUMN todo_dateend timestamp;
ALTER TABLE Todo ADD COLUMN todo_percent integer;
ALTER TABLE Todo ADD COLUMN todo_status varchar(32);
ALTER TABLE Todo ADD COLUMN todo_webpage varchar(255);


-------------------------------------------------------------------------------
-- Tables needed for Connectors sync
-------------------------------------------------------------------------------
--
-- Table structure for the table 'DeletedCalendarEvent'
--
CREATE TABLE DeletedCalendarEvent (
  deletedcalendarevent_event_id   integer,
  deletedcalendarevent_user_id    integer,
  deletedcalendarevent_timestamp  timestamp
);
create INDEX idx_dce_event_id ON DeletedCalendarEvent (deletedcalendarevent_event_id);
create INDEX idx_dce_user_id ON DeletedCalendarEvent (deletedcalendarevent_user_id);


--
-- Table structure for the table 'DeletedContact'
--
CREATE TABLE DeletedContact (
  deletedcontact_contact_id  integer,
  deletedcontact_timestamp   timestamp,
  PRIMARY KEY (deletedcontact_contact_id)
);


--
-- Table structure for the table 'DeletedUser'
--
CREATE TABLE DeletedUser (
  deleteduser_user_id    integer,
  deleteduser_timestamp  timestamp,
  PRIMARY KEY (deleteduser_user_id)
);


--
-- Table structure for the table 'DeletedTodo'
--
CREATE TABLE DeletedTodo (
  deletedtodo_todo_id    integer,
  deletedtodo_timestamp  timestamp,
  PRIMARY KEY (deletedtodo_todo_id)
);


-------------------------------------------------------------------------------
-- Tables needed for Resources module
-------------------------------------------------------------------------------

--
-- Table structure for table 'Resource'
--
CREATE TABLE Resource (
  resource_id                serial, 
  resource_timeupdate        timestamp,
  resource_timecreate        timestamp,
  resource_userupdate        integer,
  resource_usercreate        integer,
  resource_label             varchar(32) DEFAULT '' NOT NULL,
  resource_description       varchar(255),
  resource_qty               integer DEFAULT 0 NOT NULL,
  PRIMARY KEY (resource_id),
  UNIQUE (resource_label)
);
CREATE UNIQUE INDEX k_label_resource_Resource_index ON Resource (resource_label);

--
-- Table structure for table 'RGroup'
--
CREATE TABLE RGroup (
  rgroup_id          serial, 
  rgroup_timeupdate  timestamp,
  rgroup_timecreate  timestamp,
  rgroup_userupdate  integer,
  rgroup_usercreate  integer,
  rgroup_privacy     integer NULL DEFAULT 0,
  rgroup_name        varchar(32) NOT NULL,
  rgroup_desc        varchar(128),
  PRIMARY KEY (rgroup_id)
);

--
-- Table structure for table 'ResourceGroup'
--
CREATE TABLE ResourceGroup (
  resourcegroup_rgroup_id    integer DEFAULT 0 NOT NULL,
  resourcegroup_resource_id  integer DEFAULT 0 NOT NULL
);

-------------------------------------------------------------------------------
-- Insert Display Prefs (Resource modules)
-------------------------------------------------------------------------------
-- module 'resource'
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resource', 'resource_label', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resource', 'resource_description', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resource', 'resource_qty', 3, 1);

-- module 'resourcegroup'
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup', 'rgroup_name', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup', 'rgroup_desc', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup', 'rgroup_nb_resource', 3, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup', 'usercreate', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup', 'timecreate', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup', 'userupdate', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup', 'timeupdate', 7, 1);

-- module 'resourcegroup_resource'
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup_resource', 'resourcegroup_resource_label', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup_resource', 'resourcegroup_resource_desc', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup_resource', 'resourcegroup_resource_qty', 3, 1);


-------------------------------------------------------------------------------
-- Update CalendarEvent table (support for location)
-------------------------------------------------------------------------------
-- Add column location
ALTER TABLE CalendarEvent ADD COLUMN calendarevent_location varchar(100);

