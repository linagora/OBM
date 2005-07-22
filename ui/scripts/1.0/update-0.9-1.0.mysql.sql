-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM MySQL Database from 0.9 to 1.0                              //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Global Information table
-------------------------------------------------------------------------------
UPDATE ObmInfo set obminfo_value='1.0.0' where obminfo_name='db_version';


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
