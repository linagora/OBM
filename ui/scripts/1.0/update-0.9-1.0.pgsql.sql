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
  resource_name              varchar(32) DEFAULT '' NOT NULL,
  resource_description       varchar(255),
  resource_qty               integer DEFAULT 0,
  PRIMARY KEY (resource_id),
  UNIQUE (resource_name)
);
CREATE UNIQUE INDEX k_label_resource_Resource_index ON Resource (resource_name);

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
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resource', 'resource_name', 1, 2);
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
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup_resource', 'resourcegroup_resource_name', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup_resource', 'resourcegroup_resource_desc', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup_resource', 'resourcegroup_resource_qty', 3, 1);


-------------------------------------------------------------------------------
-- Update CalendarEvent table (support for location)
-------------------------------------------------------------------------------
-- Add column location
ALTER TABLE CalendarEvent ADD COLUMN calendarevent_location varchar(100);


-------------------------------------------------------------------------------
-- Create EventEntity table (support for entity Calendar)
-------------------------------------------------------------------------------
-- Create table EventEntity
CREATE TABLE EventEntity (
  evententity_timeupdate   timestamp,
  evententity_timecreate   timestamp,
  evententity_userupdate   integer default NULL,
  evententity_usercreate   integer default NULL,
  evententity_event_id     integer NOT NULL default 0,
  evententity_entity_id    integer NOT NULL default 0,
  evententity_entity       varchar(32) NOT NULL default '',
  evententity_state        char(1) NOT NULL default '',
  evententity_required     integer NOT NULL default 0,
  PRIMARY KEY (evententity_event_id,evententity_entity_id,evententity_entity)
);

-- Fill table EventEntity
INSERT INTO EventEntity (
  evententity_timeupdate,
  evententity_timecreate,
  evententity_userupdate,
  evententity_usercreate,
  evententity_event_id,
  evententity_entity_id,
  evententity_entity,
  evententity_state,
  evententity_required
)
SELECT
  calendaruser_timeupdate,
  calendaruser_timecreate,
  calendaruser_userupdate,
  calendaruser_usercreate,
  calendaruser_event_id,
  calendaruser_user_id,
  'user',
  calendaruser_state,
  calendaruser_required
FROM CalendarUser;

-- Drop table CalendarUser 
DROP TABLE CalendarUser;


-------------------------------------------------------------------------------
-- Create table 'CalendarEntityRight'
-------------------------------------------------------------------------------
CREATE TABLE EntityRight (
  entityright_entity       varchar(32) NOT NULL DEFAULT '',
  entityright_entity_id    integer NOT NULL DEFAULT 0,
  entityright_consumer     varchar(32) NOT NULL DEFAULT '',
  entityright_consumer_id  integer NOT NULL DEFAULT 0,
  entityright_read         integer NOT NULL DEFAULT 0,
  entityright_write        integer NOT NULL DEFAULT 0,
  PRIMARY KEY (entityright_entity, entityright_entity_id, entityright_consumer, entityright_consumer_id)
);
CREATE INDEX entright_idx_ent_id ON EntityRight (entityright_entity_id);
CREATE INDEX entright_idx_ent ON EntityRight (entityright_entity);
CREATE INDEX entright_idx_con_id ON EntityRight (entityright_consumer_id);
CREATE INDEX entright_idx_con ON EntityRight (entityright_consumer);


-- Update table EntityRight 
INSERT INTO EntityRight (
  entityright_entity,
  entityright_entity_id,
  entityright_consumer,
  entityright_consumer_id,
  entityright_read,
  entityright_write
)
SELECT 
  'calendar',
  calendarright_ownerid,
  'user',
  calendarright_customerid,
  calendarright_read,
  calendarright_write
FROM CalendarRight;


-- Drop table CalendarRight
DROP TABLE CalendarRight;


-------------------------------------------------------------------------------
-- InvoiceStatus table update
-------------------------------------------------------------------------------
-- add invoicestatus_created field
ALTER TABLE InvoiceStatus ADD COLUMN invoicestatus_created integer;
UPDATE InvoiceStatus SET invoicestatus_created=1;
ALTER TABLE InvoiceStatus ALTER COLUMN invoicestatus_created SET NOT NULL;
ALTER TABLE InvoiceStatus ALTER COLUMN invoicestatus_created SET DEFAULT 0;
UPDATE InvoiceStatus SET invoicestatus_created=0 WHERE invoicestatus_label ilike '%to create%' OR invoicestatus_label ilike '%A c%';


-------------------------------------------------------------------------------
-- Update Document table
-------------------------------------------------------------------------------
-- correctness : _mimetype -> mimetype_id
ALTER TABLE Document ADD COLUMN document_mimetype_id integer;
UPDATE Document SET document_mimetype_id = 0;
ALTER TABLE Document ALTER COLUMN document_mimetype_id SET DEFAULT 0;
ALTER TABLE Document DROP COLUMN document_mimetype;

-- Add ACL column
ALTER TABLE Document ADD COLUMN document_acl text;

-- Correct MIMETYPE extension case
UPDATE DocumentMimeType SET documentmimetype_extension='jpg' WHERE documentmimetype_extension='JPG';


-------------------------------------------------------------------------------
-- ContactCategory tables updates
-------------------------------------------------------------------------------
-- update _code to varchar(10)
ALTER TABLE ContactCategory1 ADD COLUMN temp_code VARCHAR(10);
UPDATE ContactCategory1 SET temp_code = contactcategory1_code;
ALTER TABLE ContactCategory1 DROP COLUMN contactcategory1_code;
ALTER TABLE ContactCategory1 RENAME COLUMN temp_code TO contactcategory1_code;

ALTER TABLE ContactCategory2 ADD COLUMN temp_code VARCHAR(10);
UPDATE ContactCategory2 SET temp_code = contactcategory2_code;
ALTER TABLE ContactCategory2 DROP COLUMN contactcategory2_code;
ALTER TABLE ContactCategory2 RENAME COLUMN temp_code TO contactcategory2_code;


-------------------------------------------------------------------------------
-- DealCategory table update
-------------------------------------------------------------------------------
-- update _code to varchar(10)
ALTER TABLE DealCategory ADD COLUMN temp_code VARCHAR(10);
UPDATE DealCategory SET temp_code = dealcategory_code;
ALTER TABLE DealCategory DROP COLUMN dealcategory_code;
ALTER TABLE DealCategory RENAME COLUMN temp_code TO dealcategory_code;


-------------------------------------------------------------------------------
-- Company and Contact Category Link tables index for performance
-------------------------------------------------------------------------------
CREATE INDEX compcat_idx_comp ON CompanyCategoryLink (companycategorylink_company_id);
CREATE INDEX contcat1_idx_cont ON ContactCategory1Link (contactcategory1link_contact_id);
CREATE INDEX contcat2_idx_cont ON ContactCategory2Link (contactcategory2link_contact_id);


-------------------------------------------------------------------------------
-- Add missing primary key
-------------------------------------------------------------------------------
ALTER TABLE UserObmGroup ADD PRIMARY KEY (userobmgroup_group_id, userobmgroup_userobm_id);
ALTER TABLE GroupGroup ADD PRIMARY KEY (groupgroup_parent_id, groupgroup_child_id);


-------------------------------------------------------------------------------
-- Deal table update
-------------------------------------------------------------------------------
-- add _dateexpected
ALTER TABLE Deal ADD COLUMN deal_dateexpected date;


-------------------------------------------------------------------------------
-- Drop Deprecated tables
-------------------------------------------------------------------------------
DROP TABLE RepeatKind;
DROP TABLE CalendarEventData;
