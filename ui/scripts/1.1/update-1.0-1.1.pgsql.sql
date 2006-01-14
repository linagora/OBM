-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM PostgreSQL Database from 0.9 to 1.0                         //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////

-------------------------------------------------------------------------------
-- Global Information table
-------------------------------------------------------------------------------
UPDATE ObmInfo set obminfo_value='1.1' where obminfo_name='db_version';

-------------------------------------------------------------------------------
-- Insert Display Prefs
-------------------------------------------------------------------------------
-- Contact module
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_date',24,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_mailing_ok',25,1);

-------------------------------------------------------------------------------
-- Update Contact table
-------------------------------------------------------------------------------
-- Add columns comment2, comment3
ALTER TABLE Contact ADD COLUMN contact_comment2 text;
ALTER TABLE Contact ADD COLUMN contact_comment3 text;
ALTER TABLE Contact ADD COLUMN contact_date TIMESTAMP;
ALTER TABLE Contact ADD COLUMN contact_category5_id integer;
ALTER TABLE Contact ALTER COLUMN contact_category5_id SET DEFAULT 0;
ALTER TABLE Contact ADD COLUMN contact_aka varchar(255);
ALTER TABLE Contact ADD COLUMN contact_sound varchar(48);


-------------------------------------------------------------------------------
-- Tables needed for Contact Module
-------------------------------------------------------------------------------

--
-- Table structure for table 'ContactCategory3'
--
CREATE TABLE ContactCategory3 (
  contactcategory3_id          serial,
  contactcategory3_timeupdate  timestamp,
  contactcategory3_timecreate  timestamp,
  contactcategory3_userupdate  integer default 0,
  contactcategory3_usercreate  integer default 0,
  contactcategory3_code        varchar(10) default '',
  contactcategory3_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (contactcategory3_id)
);


--
-- Table structure for table 'ContactCategory3Link'
--
CREATE TABLE ContactCategory3Link (
  contactcategory3link_category_id  integer NOT NULL default 0,
  contactcategory3link_contact_id   integer NOT NULL default 0,
  PRIMARY KEY (contactcategory3link_category_id,contactcategory3link_contact_id)
);
CREATE INDEX contcat3_idx_cont ON ContactCategory3Link (contactcategory3link_contact_id);


--
-- Table structure for table 'ContactCategory4'
--
CREATE TABLE ContactCategory4 (
  contactcategory4_id          serial,
  contactcategory4_timeupdate  timestamp,
  contactcategory4_timecreate  timestamp,
  contactcategory4_userupdate  integer default 0,
  contactcategory4_usercreate  integer default 0,
  contactcategory4_code        varchar(10) default '',
  contactcategory4_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (contactcategory4_id)
);

--
-- Table structure for table 'ContactCategory4Link'
--
CREATE TABLE ContactCategory4Link (
  contactcategory4link_category_id  integer NOT NULL default 0,
  contactcategory4link_contact_id   integer NOT NULL default 0,
  PRIMARY KEY (contactcategory4link_category_id,contactcategory4link_contact_id)
);
CREATE INDEX contcat4_idx_cont ON ContactCategory4Link (contactcategory4link_contact_id);


--
-- Table structure for table 'ContactCategory5'
--
CREATE TABLE ContactCategory5 (
  contactcategory5_id          serial,
  contactcategory5_timeupdate  timestamp,
  contactcategory5_timecreate  timestamp,
  contactcategory5_userupdate  integer default 0,
  contactcategory5_usercreate  integer default 0,
  contactcategory5_code        varchar(10) default '',
  contactcategory5_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (contactcategory5_id)
);


-------------------------------------------------------------------------------
-- Create table structure for table 'ContactFunction'
-------------------------------------------------------------------------------
-- Table structure for table 'ContactFunction'
CREATE TABLE ContactFunction (
  contactfunction_id          serial,
  contactfunction_timeupdate  TIMESTAMP,
  contactfunction_timecreate  TIMESTAMP,
  contactfunction_userupdate  integer,
  contactfunction_usercreate  integer NOT NULL default 0,
  contactfunction_code        varchar(10) NOT NULL default '',
  contactfunction_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (contactfunction_id)
);

-- Update table 'ContactFunction'
INSERT INTO ContactFunction (
  contactfunction_id,
  contactfunction_timeupdate,
  contactfunction_timecreate,
  contactfunction_userupdate,
  contactfunction_usercreate,
  contactfunction_label
)
SELECT
  function_id,
  function_timeupdate,
  function_timecreate,
  function_userupdate,
  function_usercreate,
  function_label
FROM
  Function;

-- DROP table Function
DROP TABLE Function;


-------------------------------------------------------------------------------
-- Create table structure for table 'CompanyCategory1'
-------------------------------------------------------------------------------
-- Create table 'CompanyCategory1'
CREATE TABLE CompanyCategory1 (
  companycategory1_id          serial,
  companycategory1_timeupdate  TIMESTAMP,
  companycategory1_timecreate  TIMESTAMP,
  companycategory1_userupdate  integer,
  companycategory1_usercreate  integer NOT NULL default 0,
  companycategory1_code        varchar(10) NOT NULL default '',
  companycategory1_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (companycategory1_id)
);

-- Update table 'CompanyCategory1'
INSERT INTO CompanyCategory1 (
  companycategory1_id,
  companycategory1_timeupdate,
  companycategory1_timecreate,
  companycategory1_userupdate,
  companycategory1_usercreate,
  companycategory1_code,
  companycategory1_label
)
SELECT
  companycategory_id,
  companycategory_timeupdate,
  companycategory_timecreate,
  companycategory_userupdate,
  companycategory_usercreate,
  companycategory_code,
  companycategory_label
FROM
  CompanyCategory;

-- DROP table CompanyCategory
DROP TABLE CompanyCategory;

-------------------------------------------------------------------------------
-- Create table structure for table 'CompanyCategory1Link'
-------------------------------------------------------------------------------
-- Create table 'CompanyCategory1Link'
CREATE TABLE CompanyCategory1Link (
  companycategory1link_category_id  integer NOT NULL default 0,
  companycategory1link_company_id   integer NOT NULL default 0,
  PRIMARY KEY (companycategory1link_category_id,companycategory1link_company_id)
);
CREATE INDEX compcat1_idx_comp ON CompanyCategory1Link (companycategory1link_company_id);

-- Update table 'CompanyCategory1'
INSERT INTO CompanyCategory1Link (
  companycategory1link_category_id,
  companycategory1link_company_id
)
SELECT
  companycategorylink_category_id,
  companycategorylink_company_id
FROM
  CompanyCategoryLink;

-- DROP table CompanyCategoryLink
DROP TABLE CompanyCategoryLink;


-------------------------------------------------------------------------------
-- Update CompanyActivity table
-------------------------------------------------------------------------------
ALTER TABLE CompanyActivity ADD COLUMN companyactivity_code varchar(10);
UPDATE CompanyActivity SET companyactivity_code = '' where companyactivity_code is null;
ALTER TABLE CompanyActivity ALTER COLUMN companyactivity_code SET DEFAULT ''; 
ALTER TABLE CompanyActivity ALTER COLUMN companyactivity_code SET NOT NULL; 


-------------------------------------------------------------------------------
-- Update CompanyType table
-------------------------------------------------------------------------------
ALTER TABLE CompanyType ADD COLUMN companytype_code varchar(10); 
UPDATE CompanyType SET companytype_code = '' where companytype_code is null;
ALTER TABLE CompanyType ALTER COLUMN companytype_code SET DEFAULT ''; 
ALTER TABLE CompanyType ALTER COLUMN companytype_code SET NOT NULL; 


-------------------------------------------------------------------------------
-- Create table structure for table 'DealCategory1'
-------------------------------------------------------------------------------
-- Table structure for table 'DealCategory1'
CREATE TABLE DealCategory1 (
  dealcategory1_id          serial,
  dealcategory1_timeupdate  timestamp,
  dealcategory1_timecreate  timestamp,
  dealcategory1_userupdate  integer default 0,
  dealcategory1_usercreate  integer default 0,
  dealcategory1_code        varchar(10) default '',
  dealcategory1_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (dealcategory1_id)
);

-- Update table 'DealCategory1'
INSERT INTO DealCategory1 (
  dealcategory1_id,
  dealcategory1_timeupdate,
  dealcategory1_timecreate,
  dealcategory1_userupdate,
  dealcategory1_usercreate,
  dealcategory1_code,
  dealcategory1_label
)
SELECT
  dealcategory_id,
  dealcategory_timeupdate,
  dealcategory_timecreate,
  dealcategory_userupdate,
  dealcategory_usercreate,
  dealcategory_code,
  dealcategory_label
FROM
  DealCategory;

-- DROP table DealCategory
DROP TABLE DealCategory;

-------------------------------------------------------------------------------
-- Table structure for table 'DealCategory1Link'
-------------------------------------------------------------------------------
-- Create table structure for table 'DealCategory1Link'
CREATE TABLE DealCategory1Link (
  dealcategory1link_category_id  integer NOT NULL default 0,
  dealcategory1link_deal_id      integer NOT NULL default 0,
  PRIMARY KEY (dealcategory1link_category_id,dealcategory1link_deal_id)
);
CREATE INDEX dealcat1_idx_deal ON DealCategory1Link (dealcategory1link_deal_id);

-- Update table 'DealCategory1Link'
INSERT INTO DealCategory1Link (
  dealcategory1link_category_id,
  dealcategory1link_deal_id
)
SELECT
  dealcategorylink_category_id,
  dealcategorylink_deal_id
FROM
  DealCategoryLink;

-- DROP table DealCategoryLink
DROP TABLE DealCategoryLink;


-------------------------------------------------------------------------------
-- Table structure for table 'CalendarCategory1'
-------------------------------------------------------------------------------
CREATE TABLE CalendarCategory1 (
  calendarcategory1_id          serial,
  calendarcategory1_timeupdate  timestamp,
  calendarcategory1_timecreate  timestamp,
  calendarcategory1_userupdate  integer DEFAULT NULL,
  calendarcategory1_usercreate  integer DEFAULT NULL,
  calendarcategory1_code        varchar(10) default '',
  calendarcategory1_label       varchar(128) DEFAULT NULL,
  PRIMARY KEY (calendarcategory1_id)
);

-- Update table 'CalendarCategory1'
INSERT INTO CalendarCategory1 (
  calendarcategory1_id,
  calendarcategory1_timeupdate,
  calendarcategory1_timecreate,
  calendarcategory1_userupdate,
  calendarcategory1_usercreate,
  calendarcategory1_label
)
SELECT
  calendarcategory_id,
  calendarcategory_timeupdate,
  calendarcategory_timecreate,
  calendarcategory_userupdate,
  calendarcategory_usercreate,
  calendarcategory_label
FROM
  CalendarCategory;

-- DROP table CalendarCategory
DROP TABLE CalendarCategory;


-------------------------------------------------------------------------------
-- Update CalendarEvent table
-------------------------------------------------------------------------------
-- correctness : _category_id -> _category1_id
ALTER TABLE CalendarEvent ADD COLUMN temp_calendarevent_category1_id integer;
UPDATE CalendarEvent SET temp_calendarevent_category1_id = calendarevent_category_id;
ALTER TABLE CalendarEvent DROP COLUMN calendarevent_category_id;
ALTER TABLE CalendarEvent RENAME COLUMN temp_calendarevent_category1_id TO calendarevent_category1_id;

-------------------------------------------------------------------------------
-- Update Incident table
-------------------------------------------------------------------------------
-- correctness : _cat1_id -> _category1_id
ALTER TABLE Incident ADD COLUMN temp_incident_category1_id integer;
UPDATE Incident SET temp_incident_category1_id = incident_cat1_id;
ALTER TABLE Incident DROP COLUMN incident_cat1_id;
ALTER TABLE Incident RENAME COLUMN temp_incident_category1_id TO incident_category1_id;

-------------------------------------------------------------------------------
-- Update DocumentCategory1 table
-------------------------------------------------------------------------------
ALTER TABLE DocumentCategory1 ADD COLUMN documentcategory1_code varchar(10); 
UPDATE DocumentCategory1 SET documentcategory1_code = '' where documentcategory1_code is null;
ALTER TABLE DocumentCategory1 ALTER COLUMN documentcategory1_code SET DEFAULT ''; 
ALTER TABLE DocumentCategory1 ALTER COLUMN documentcategory1_code SET NOT NULL; 

-------------------------------------------------------------------------------
-- Update DocumentCategory2 table
-------------------------------------------------------------------------------
ALTER TABLE DocumentCategory2 ADD COLUMN documentcategory2_code varchar(10);
UPDATE DocumentCategory2 SET documentcategory2_code = '' where documentcategory2_code is null;
ALTER TABLE DocumentCategory2 ALTER COLUMN documentcategory2_code SET DEFAULT ''; 
ALTER TABLE DocumentCategory2 ALTER COLUMN documentcategory2_code SET NOT NULL; 

-------------------------------------------------------------------------------
-- Update IncidentCategory1 table
-------------------------------------------------------------------------------
ALTER TABLE IncidentCategory1 ADD COLUMN incidentcategory1_code varchar(10);
UPDATE IncidentCategory1 SET incidentcategory1_code = '' where incidentcategory1_code is null;
ALTER TABLE IncidentCategory1 ALTER COLUMN incidentcategory1_code SET default '';
ALTER TABLE IncidentCategory1 ALTER COLUMN incidentcategory1_code SET NOT NULL;

-- Update table 'IncidentCategory1'
UPDATE IncidentCategory1 set incidentcategory1_code = incidentcategory1_order;

-- Update table 'IncidentCategory1'
ALTER TABLE IncidentCategory1 DROP COLUMN incidentcategory1_order;


-------------------------------------------------------------------------------
-- Update IncidentStatus table
-------------------------------------------------------------------------------
ALTER TABLE IncidentStatus ADD COLUMN incidentstatus_code varchar(10);
UPDATE IncidentStatus SET incidentstatus_code = '' where incidentstatus_code is null;
ALTER TABLE IncidentStatus ALTER COLUMN incidentstatus_code SET DEFAULT '';
ALTER TABLE IncidentStatus ALTER COLUMN incidentstatus_code SET NOT NULL;

-- Update table 'IncidentStatus'
UPDATE IncidentStatus set incidentstatus_code = incidentstatus_order;

-- Update table 'IncidentStatus'
ALTER TABLE IncidentStatus DROP COLUMN incidentstatus_order;


-------------------------------------------------------------------------------
-- Update IncidentPriority table
-------------------------------------------------------------------------------
ALTER TABLE IncidentPriority ADD COLUMN incidentpriority_code varchar(10);
UPDATE IncidentPriority SET incidentpriority_code = '' where incidentpriority_code is null;
ALTER TABLE IncidentPriority ALTER COLUMN incidentpriority_code SET DEFAULT '';
ALTER TABLE IncidentPriority ALTER COLUMN incidentpriority_code SET NOT NULL;

-- Update table 'IncidentPriority'
UPDATE IncidentPriority set incidentpriority_code = incidentpriority_order;

-- Update table 'IncidentPriority'
ALTER TABLE IncidentPriority DROP COLUMN incidentpriority_order;


-------------------------------------------------------------------------------
-- Update PublicationType table
-------------------------------------------------------------------------------
ALTER TABLE PublicationType ADD COLUMN publicationtype_code varchar(10);
UPDATE PublicationType SET publicationtype_code = '' where publicationtype_code is null;
ALTER TABLE PublicationType ALTER COLUMN publicationtype_code SET DEFAULT '';
ALTER TABLE PublicationType ALTER COLUMN publicationtype_code SET NOT NULL;


-------------------------------------------------------------------------------
-- Update SubscriptionReception table
-------------------------------------------------------------------------------
ALTER TABLE SubscriptionReception ADD COLUMN subscriptionreception_code varchar(10);
UPDATE SubscriptionReception SET subscriptionreception_code = '' where subscriptionreception_code is null;
ALTER TABLE SubscriptionReception ALTER COLUMN subscriptionreception_code SET DEFAULT '';
ALTER TABLE SubscriptionReception ALTER COLUMN subscriptionreception_code SET NOT NULL;


-------------------------------------------------------------------------------
-- Update ContractType table
-------------------------------------------------------------------------------
ALTER TABLE ContractType ADD COLUMN contracttype_code varchar(10);
UPDATE ContractType SET contracttype_code = '' where contracttype_code is null;
ALTER TABLE ContractType ALTER COLUMN contracttype_code SET DEFAULT '';
ALTER TABLE ContractType ALTER COLUMN contracttype_code SET NOT NULL;


-------------------------------------------------------------------------------
-- Update ContractPriority table
-------------------------------------------------------------------------------
ALTER TABLE ContractPriority ADD COLUMN contractpriority_code varchar(10);
UPDATE ContractPriority SET contractpriority_code = '' where contractpriority_code is null;
ALTER TABLE ContractPriority ALTER COLUMN contractpriority_code SET DEFAULT '';
ALTER TABLE ContractPriority ALTER COLUMN contractpriority_code SET NOT NULL;

-- Update table 'ContractPriority'
UPDATE ContractPriority set contractpriority_code = contractpriority_order;

-- Update table 'ContractPriority'
ALTER TABLE ContractPriority DROP COLUMN contractpriority_order;


-------------------------------------------------------------------------------
-- Update ContractStatus table
-------------------------------------------------------------------------------
ALTER TABLE ContractStatus ADD COLUMN contractstatus_code varchar(10);
UPDATE ContractStatus SET contractstatus_code = '' where contractstatus_code is null;
ALTER TABLE ContractStatus ALTER COLUMN contractstatus_code SET DEFAULT '';
ALTER TABLE ContractStatus ALTER COLUMN contractstatus_code SET NOT NULL;

-- Update table 'ContractStatus'
UPDATE ContractStatus set contractstatus_code = contractstatus_order;

-- Update table 'ContractStatus'
ALTER TABLE ContractStatus DROP COLUMN contractstatus_order;


-------------------------------------------------------------------------------
-- Update UserObm table
-------------------------------------------------------------------------------
-- set lastname and firstname to default '' instead of null (cause of concat)
UPDATE UserObm SET userobm_lastname = '' where userobm_lastname is null;
ALTER TABLE UserObm ALTER COLUMN userobm_lastname SET DEFAULT '';
UPDATE UserObm SET userobm_firstname = '' where userobm_firstname is null;
ALTER TABLE UserObm ALTER COLUMN userobm_firstname SET DEFAULT '';

-------------------------------------------------------------------------------
-- Update List table
-------------------------------------------------------------------------------
-- add column list type
ALTER TABLE List ADD COLUMN list_mode integer;
ALTER TABLE List ALTER COLUMN list_mode SET DEFAULT 0;


-------------------------------------------------------------------------------
-- Update Connector tables
-------------------------------------------------------------------------------
-- DROP table DeletedCalendarEvent
DROP TABLE DeletedCalendarEvent;
--
-- Table structure for the table 'DeletedCalendarEvent'
--
CREATE TABLE DeletedCalendarEvent (
  deletedcalendarevent_id         serial,
  deletedcalendarevent_event_id   integer,
  deletedcalendarevent_user_id    integer,
  deletedcalendarevent_timestamp  timestamp
);
create INDEX idx_dce_event_id ON DeletedCalendarEvent (deletedcalendarevent_event_id);
create INDEX idx_dce_user_id ON DeletedCalendarEvent (deletedcalendarevent_user_id);
