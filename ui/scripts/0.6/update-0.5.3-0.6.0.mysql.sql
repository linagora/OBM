-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM Database from 0.5.3 to 0.6.0	                             //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$ //
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Company Update
-------------------------------------------------------------------------------
-- change address1, address2, town, mail and web (to varchar(64))
ALTER table Company change column company_address1 company_address1 varchar(64);
ALTER table Company change column company_address2 company_address2 varchar(64);
ALTER table Company change column company_town company_town varchar(64);
ALTER table Company change column company_web company_web varchar(64);
ALTER table Company change column company_email company_email varchar(64);

-- Add new column : company_deal_total
ALTER table Company add column company_deal_total int(5) not null default 0 AFTER company_deal_number;

-- Add new column : company_activity_id
ALTER table Company add column company_activity_id int(8) AFTER company_type_id;

-- 
-- Table structure for table 'CompanyActivity'
--
CREATE TABLE CompanyActivity (
  companyactivity_id int(8) DEFAULT '0' NOT NULL auto_increment,
  companyactivity_timeupdate timestamp(14),
  companyactivity_timecreate timestamp(14),
  companyactivity_userupdate int(8),
  companyactivity_usercreate int(8),
  companyactivity_label varchar(64),
  PRIMARY KEY (companyactivity_id)
);


-------------------------------------------------------------------------------
-- Deal Update
-------------------------------------------------------------------------------
-- Add new column : deal_hitrate
ALTER table Deal add column deal_hitrate char(3) default 0 AFTER deal_amount;

-------------------------------------------------------------------------------
-- Calendar module tables
-------------------------------------------------------------------------------

DROP TABLE IF EXISTS CalendarEvent;
DROP TABLE IF EXISTS CalendarLayer;
DROP TABLE IF EXISTS EventUser;
DROP TABLE IF EXISTS CalendarCategory;
--
-- Table structure for the table  'CalendarSegment'
--
CREATE TABLE CalendarSegment (
  calendarsegment_eventid int(8) 	NOT NULL default '0',
  calendarsegment_customerid int(8) 	NOT NULL default '0',
  calendarsegment_date varchar(12) 	NOT NULL default '',
  calendarsegment_flag varchar(5) 	NOT NULL default '',
  calendarsegment_type varchar(5) 	NOT NULL default '',
  calendarsegment_state char(1) 	NOT NULL default '''',
  PRIMARY KEY  (calendarsegment_eventid,calendarsegment_customerid,calendarsegment_date,calendarsegment_flag,calendarsegment_type)
);

--
-- Table structure for the table  'CalendarEvent'
--
CREATE TABLE CalendarEvent (
  calendarevent_id int(8)   NOT NULL auto_increment,
  calendarevent_timeupdate  timestamp(14) NOT NULL,
  calendarevent_timecreate  timestamp(14) NOT NULL,
  calendarevent_userupdate  int(8) default NULL,
  calendarevent_usercreate  int(8) default NULL,
  calendarevent_title       varchar(255) default NULL,
  calendarevent_description text,
  calendarevent_category_id int(8) default NULL,
  calendarevent_priority    int(2) default NULL,
  calendarevent_privacy     int(2) default NULL,
  calendarevent_length      varchar(14) NOT NULL default '',
  calendarevent_repeatkind  varchar(20) default NULL,
  calendarevent_repeatdays  varchar(7) default NULL,
  calendarevent_endrepeat   timestamp(14) NOT NULL,
  PRIMARY KEY (calendarevent_id)
);

    
--
-- Table structure for the table  'CalendarCategory'
--
CREATE TABLE CalendarCategory (
  calendarcategory_id         int(8) NOT NULL auto_increment,
  calendarcategory_timeupdate timestamp(14) NOT NULL,
  calendarcategory_timecreate timestamp(14) NOT NULL,
  calendarcategory_userupdate int(8) default NULL,
  calendarcategory_usercreate int(8) default NULL,
  calendarcategory_label      varchar(128) default NULL,
  PRIMARY KEY (calendarcategory_id)
);

--
-- dump for table 'CalendarCategory'
--
INSERT INTO CalendarCategory (calendarcategory_timeupdate, calendarcategory_timecreate, calendarcategory_userupdate, calendarcategory_usercreate, calendarcategory_label) VALUES (null,null,null,1,'RDV');
INSERT INTO CalendarCategory (calendarcategory_timeupdate, calendarcategory_timecreate, calendarcategory_userupdate, calendarcategory_usercreate, calendarcategory_label) VALUES (null,null,null,1,'Formation');
INSERT INTO CalendarCategory (calendarcategory_timeupdate, calendarcategory_timecreate, calendarcategory_userupdate, calendarcategory_usercreate, calendarcategory_label) VALUES (null,null,null,1,'Commercial');
INSERT INTO CalendarCategory (calendarcategory_timeupdate, calendarcategory_timecreate, calendarcategory_userupdate, calendarcategory_usercreate, calendarcategory_label) VALUES (null,null,null,1,'Reunion');
INSERT INTO CalendarCategory (calendarcategory_timeupdate, calendarcategory_timecreate, calendarcategory_userupdate, calendarcategory_usercreate, calendarcategory_label) VALUES (null,null,null,1,'Appel tel.');
INSERT INTO CalendarCategory (calendarcategory_timeupdate, calendarcategory_timecreate, calendarcategory_userupdate, calendarcategory_usercreate, calendarcategory_label) VALUES (null,null,null,1,'Support');
INSERT INTO CalendarCategory (calendarcategory_timeupdate, calendarcategory_timecreate, calendarcategory_userupdate, calendarcategory_usercreate, calendarcategory_label) VALUES (null,null,null,1,'Developpement');
INSERT INTO CalendarCategory (calendarcategory_timeupdate, calendarcategory_timecreate, calendarcategory_userupdate, calendarcategory_usercreate, calendarcategory_label) VALUES (null,null,null,1,'Personnel');

