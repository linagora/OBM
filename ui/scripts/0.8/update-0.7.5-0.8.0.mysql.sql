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

-------------------------------------------------------------------------------
-- Publication module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'Publication'
--
CREATE TABLE Publication (
  publication_id             int(8) DEFAULT '0' NOT NULL auto_increment,
  publication_timeupdate     timestamp(14),
  publication_timecreate     timestamp(14),
  publication_userupdate     int(8),
  publication_usercreate     int(8),
  publication_title          varchar(64) NOT NULL,
  publication_type_id        int(8),
  publication_year           int(4),
  publication_lang           char(2),
  publication_desc           text,
  PRIMARY KEY (publication_id)
);

--
-- Table structure for table 'PublicationType'
--
CREATE TABLE PublicationType (
  publicationtype_id          int(8) DEFAULT '0' NOT NULL auto_increment,
  publicationtype_timeupdate  timestamp(14),
  publicationtype_timecreate  timestamp(14),
  publicationtype_userupdate  int(8),
  publicationtype_usercreate  int(8),
  publicationtype_label       char(12),
  PRIMARY KEY (publicationtype_id)
);


-- 
-- Publication Display Preferences
--
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'publication', 'publication_title', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'publication', 'publicationtype_label', 2, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'publication', 'publication_year', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'publication', 'publication_lang', 4, 1);

--
-- Subscription Tables
--

--
-- Table structure for table 'Subscription'
--
CREATE TABLE Subscription (
  subscription_publication_id 	int(8) NOT NULL,
  subscription_contact_id       int(8) NOT NULL,
  subscription_timeupdate       timestamp(14),
  subscription_timecreate       timestamp(14),
  subscription_userupdate       int(8),
  subscription_usercreate       int(8),
  subscription_quantity       	int(8),
  subscription_renewal_id       int(8) DEFAULT '0' NOT NULL,
  subscription_reception_id     int(8) DEFAULT '0' NOT NULL,
  subscription_date_begin       timestamp(14),
  subscription_date_end         timestamp(14),
  PRIMARY KEY (subscription_publication_id,subscription_contact_id)
);

--
-- Table structure for table 'SubscriptionRenewal'
--
CREATE TABLE SubscriptionRenewal (
  subscriptionrenewal_id          int(8) DEFAULT '0' NOT NULL auto_increment,
  subscriptionrenewal_timeupdate  timestamp(14),
  subscriptionrenewal_timecreate  timestamp(14),
  subscriptionrenewal_userupdate  int(8),
  subscriptionrenewal_usercreate  int(8),
  subscriptionrenewal_label       char(12),
  PRIMARY KEY (subscriptionrenewal_id)
);

--
-- Table structure for table 'SubscriptionReception'
--
CREATE TABLE SubscriptionReception ( 
  subscriptionreception_id          int(8) DEFAULT '0' NOT NULL auto_increment,
  subscriptionreception_timeupdate  timestamp(14),
  subscriptionreception_timecreate  timestamp(14),
  subscriptionreception_userupdate  int(8),
  subscriptionreception_usercreate  int(8),
  subscriptionreception_label       char(12),
  PRIMARY KEY (subscriptionreception_id)
);

