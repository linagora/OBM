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
  resource_id                int(8) NOT NULL auto_increment,
  resource_timeupdate        timestamp(14),
  resource_timecreate        timestamp(14),
  resource_userupdate        int(8),
  resource_usercreate        int(8),
  resource_name              varchar(32) DEFAULT '' NOT NULL,
  resource_description       varchar(255),
  resource_qty               int(8) DEFAULT 0,
  PRIMARY KEY (resource_id),
  UNIQUE k_label_resource (resource_name)
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
-- Table structure for table 'ResourceGroup'
--
CREATE TABLE ResourceGroup (
  resourcegroup_rgroup_id    int(8) DEFAULT 0 NOT NULL,
  resourcegroup_resource_id  int(8) DEFAULT 0 NOT NULL
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
ALTER TABLE CalendarEvent ADD COLUMN calendarevent_location varchar(100) after calendarevent_title;


-------------------------------------------------------------------------------
-- Create EventEntity table (support for entity Calendar)
-------------------------------------------------------------------------------
-- Create table EventEntity
CREATE TABLE EventEntity (
  evententity_timeupdate   timestamp(14),
  evententity_timecreate   timestamp(14),
  evententity_userupdate   int(8) default NULL,
  evententity_usercreate   int(8) default NULL,
  evententity_event_id     int(8) NOT NULL default 0,
  evententity_entity_id    int(8) NOT NULL default 0,
  evententity_entity       varchar(32) NOT NULL default 0,
  evententity_state        char(1) NOT NULL default 0,
  evententity_required     int(1) NOT NULL default 0,
  PRIMARY KEY (evententity_event_id,evententity_entity_id,evententity_entity)
);

-- Update table EventEntity
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

-- DROP table CalendarUser
DROP TABLE IF EXISTS CalendarUser;


-------------------------------------------------------------------------------
-- Create table 'EntityRight'
-------------------------------------------------------------------------------
CREATE TABLE EntityRight (
  entityright_entity        varchar(32) NOT NULL default '',
  entityright_entity_id     int(8) NOT NULL default 0,
  entityright_consumer      varchar(32) NOT NULL default '',
  entityright_consumer_id   int(8) NOT NULL default 0,
  entityright_read          int(1) NOT NULL default 0,
  entityright_write         int(1) NOT NULL default 0,
  PRIMARY KEY (entityright_entity, entityright_entity_id, entityright_consumer, entityright_consumer_id),
  INDEX entright_idx_ent_id (entityright_entity_id),
  INDEX entright_idx_ent (entityright_entity),
  INDEX entright_idx_con_id (entityright_consumer_id),
  INDEX entright_idx_con (entityright_consumer)
);


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


-- DROP table CalendarRight 
DROP TABLE IF EXISTS CalendarRight;


-------------------------------------------------------------------------------
-- Update Document table
-------------------------------------------------------------------------------
-- correctness : _mimetype -> mimetype_id
ALTER TABLE Document CHANGE document_mimetype document_mimetype_id int(8) not null default 0;

-- Add ACL column
ALTER TABLE Document ADD COLUMN document_acl text;

-- Correct MIMETYPE extension case
UPDATE DocumentMimeType SET documentmimetype_extension='jpg' WHERE documentmimetype_extension='JPG';


-------------------------------------------------------------------------------
-- InvoiceStatus table update
-------------------------------------------------------------------------------
-- add invoicestatus_created field
ALTER TABLE InvoiceStatus ADD COLUMN invoicestatus_created int(1) DEFAULT 0 NOT NULL after invoicestatus_payment;
UPDATE InvoiceStatus SET invoicestatus_created=1;
UPDATE InvoiceStatus SET invoicestatus_created=0 WHERE invoicestatus_label like '%to create%' OR invoicestatus_label like '%A c%';


-------------------------------------------------------------------------------
-- Subscription MySQL table was missing auto_increment
-------------------------------------------------------------------------------
DROP TABLE Subscription;
--
-- Table structure for table 'Subscription'
--
CREATE TABLE Subscription (
  subscription_id               int(8) NOT NULL auto_increment,
  subscription_publication_id 	int(8) NOT NULL,
  subscription_contact_id       int(8) NOT NULL,
  subscription_timeupdate       timestamp(14),
  subscription_timecreate       timestamp(14),
  subscription_userupdate       int(8),
  subscription_usercreate       int(8),
  subscription_quantity       	int(8),
  subscription_renewal          int(1) DEFAULT 0 NOT NULL,
  subscription_reception_id     int(8) DEFAULT 0 NOT NULL,
  subscription_date_begin       timestamp(14),
  subscription_date_end         timestamp(14),
  PRIMARY KEY (subscription_id)
);


-------------------------------------------------------------------------------
-- ContactCategory tables updates
-------------------------------------------------------------------------------
-- 
ALTER TABLE ContactCategory1 MODIFY COLUMN contactcategory1_code varchar(10) DEFAULT '';
ALTER TABLE ContactCategory2 MODIFY COLUMN contactcategory2_code varchar(10) DEFAULT '';


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
-- Drop Deprecated tables
-------------------------------------------------------------------------------
DROP TABLE IF EXISTS RepeatKind;
DROP TABLE IF EXISTS CalendarEventData;

