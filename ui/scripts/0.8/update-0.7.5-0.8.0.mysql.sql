-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM MySQL Database from 0.7.5 to 0.8.0                          //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Update User preferences
-------------------------------------------------------------------------------
-- Update preference : todo_order
UPDATE UserObmPref set userobmpref_option='set_todo', userobmpref_value='priority' where userobmpref_option='todo_order';


-------------------------------------------------------------------------------
-- Update Calendar tables
-------------------------------------------------------------------------------
-- Change column : calendarsegment_date from varchar to timestamp
 
ALTER TABLE CalendarSegment CHANGE calendarsegment_date calendarsegment_date VARCHAR( 14 ) NOT NULL ;
UPDATE CalendarSegment SET calendarsegment_date =CONCAT(calendarsegment_date,"00");
ALTER table CalendarSegment change column calendarsegment_date calendarsegment_date timestamp(14) NOT NULL;

ALTER TABLE CalendarEvent CHANGE calendarevent_endrepeat calendarevent_endrepeat VARCHAR( 14 ) NOT NULL; 
UPDATE CalendarEvent SET calendarevent_endrepeat = CONCAT(calendarevent_endrepeat,"00");
ALTER table CalendarEvent change column calendarevent_endrepeat calendarevent_endrepeat timestamp(14);

ALTER table CalendarEvent change column calendarevent_length calendarevent_length INT(14);

-------------------------------------------------------------------------------
-- Update List tables
-------------------------------------------------------------------------------
-- added column : list_structure

ALTER TABLE List ADD COLUMN list_structure text;
 

-------------------------------------------------------------------------------
-- Import module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'Import'
--
CREATE TABLE Import (
  import_id             int(8) DEFAULT '0' NOT NULL auto_increment,
  import_timeupdate     timestamp(14),
  import_timecreate     timestamp(14),
  import_userupdate     int(8),
  import_usercreate     int(8),
  import_name           varchar(64) NOT NULL,
  import_datasource_id  int(8),
  import_format         varchar(128),
  import_desc           text,
  PRIMARY KEY (import_id),
  UNIQUE (import_name)
);

-- module 'import'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'import', 'import_name', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'import', 'import_datasource', 2, 2);

-- module 'Contact'
--
-- Table structure for table 'ContactCategory1'
--
ALTER TABLE ContactCategory1 CHANGE COLUMN contactcategory1_order contactcategory1_code int(4) default '0';
--
-- Table structure for table 'ContactCategory2'
--
ALTER TABLE ContactCategory2 CHANGE COLUMN contactcategory2_order contactcategory2_code int(4) default '0';

