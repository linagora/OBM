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
ALTER table CalendarSegment change column calendarsegment_date calendarsegment_date timestamp(14)  NOT NULL;
ALTER TABLE CalendarEvent CHANGE calendarevent_endrepeat calendarevent_endrepeat VARCHAR( 14 ) NOT NULL; 
UPDATE CalendarEvent SET calendarevent_endrepeat = CONCAT(calendarevent_endrepeat,"00");
ALTER table CalendarEvent change column calendarevent_endrepeat calendarevent_endrepeat timestamp(14);
ALTER table CalendarEvent change column calendarevent_length calendarevent_endrepeat INT(14);


--
-- Table structure for table 'ContactCategory1'
--
CREATE TABLE ContactCategory1 (
  contactcategory1_id          int(8) NOT NULL auto_increment,
  contactcategory1_timeupdate  timestamp(14),
  contactcategory1_timecreate  timestamp(14),
  contactcategory1_userupdate  int(8) default NULL,
  contactcategory1_usercreate  int(8) default NULL,
  contactcategory1_label       varchar(255) default NULL,
  PRIMARY KEY (contactcategory1_id)
);


--
-- Table structure for table 'ContactCategory2'
--
CREATE TABLE ContactCategory2 (
  contactcategory2_id          int(8) NOT NULL auto_increment,
  contactcategory2_timeupdate  timestamp(14) NOT NULL,
  contactcategory2_timecreate  timestamp(14) NOT NULL,
  contactcategory2_userupdate  int(8) default NULL,
  contactcategory2_usercreate  int(8) default NULL,
  contactcategory2_label       varchar(255) default NULL,
  PRIMARY KEY (contactcategory2_id)
);


--
-- Table structure for table 'ContactCategory1Link'
--
CREATE TABLE ContactCategory1Link (
  contactcategory1link_category_id  int(8) NOT NULL default '0',
  contactcategory1link_company_id   int(8) NOT NULL default '0',
  PRIMARY KEY (contactcategory1link_category_id,contactcategory1link_company_id)
);


--
-- Table structure for table 'ContactCategory2Link'
--
CREATE TABLE ContactCategory2Link (
  contactcategory2link_category_id  int(8) NOT NULL default '0',
  contactcategory2link_company_id   int(8) NOT NULL default '0',
  PRIMARY KEY (contactcategory2link_category_id,contactcategory2link_company_id)
);

