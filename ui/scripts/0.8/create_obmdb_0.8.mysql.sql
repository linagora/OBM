--/////////////////////////////////////////////////////////////////////////////
--// OBM - File : create_obmdb_0.7.mysql.sql                                 //
--//     - Desc : MySQL Database 0.7 creation script                         //
--// 2003-07-22 ALIACOM                                                      //
--/////////////////////////////////////////////////////////////////////////////
-- $Id$
--/////////////////////////////////////////////////////////////////////////////

-------------------------------------------------------------------------------
-- Database creation (old one is deleted !)
-------------------------------------------------------------------------------

drop database IF EXISTS obm;

create database obm;

use obm;

-------------------------------------------------------------------------------
-- Global Information table
-------------------------------------------------------------------------------

--
-- Table structure for table 'ObmInfo'
--
CREATE TABLE ObmInfo (
  obminfo_name   varchar(32) NOT NULL default '',
  obminfo_value  varchar(255) default '',
  PRIMARY KEY (obminfo_name)
);


-------------------------------------------------------------------------------
-- User, Preferences tables
-------------------------------------------------------------------------------

--
-- Table structure for table 'ObmSession'
--
CREATE TABLE ObmSession (
  obmsession_sid         varchar(32) NOT NULL default '',
  obmsession_timeupdate  timestamp(14),
  obmsession_name        varchar(32) NOT NULL default '',
  obmsession_data        text,
  PRIMARY KEY (obmsession_sid, obmsession_name)
);


--
-- Table structure for table 'ActiveUserObm'
--
CREATE TABLE ActiveUserObm (
  activeuserobm_sid            varchar(32) NOT NULL default '',
  activeuserobm_session_name   varchar(32) NOT NULL default '',
  activeuserobm_userobm_id     int(11) default NULL,
  activeuserobm_timeupdate     timestamp(14),
  activeuserobm_timecreate     timestamp(14),
  activeuserobm_nb_connexions  int(11) NOT NULL default '0',
  activeuserobm_lastpage       varchar(64) NOT NULL default '0',
  activeuserobm_ip             varchar(32) NOT NULL default '0',
  PRIMARY KEY (activeuserobm_sid)
);


--
-- Table structure for table 'UserObm_SessionLog'
--
CREATE TABLE UserObm_SessionLog (
  userobm_sessionlog_sid            varchar(32) NOT NULL default '',
  userobm_sessionlog_session_name   varchar(32) NOT NULL default '',
  userobm_sessionlog_userobm_id     int(11) default NULL,
  userobm_sessionlog_timeupdate     timestamp(14),
  userobm_sessionlog_timecreate     timestamp(14),
  userobm_sessionlog_nb_connexions  int(11) NOT NULL default '0',
  userobm_sessionlog_lastpage       varchar(32) NOT NULL default '0',
  userobm_sessionlog_ip             varchar(32) NOT NULL default '0',
  PRIMARY KEY (userobm_sessionlog_sid)
);


--
-- Table structure for table 'UserObm'
--
CREATE TABLE UserObm (
  userobm_id              int(8) DEFAULT '0' NOT NULL auto_increment,
  userobm_timeupdate      timestamp(14),
  userobm_timecreate      timestamp(14),
  userobm_userupdate      int(8),
  userobm_usercreate      int(8),
  userobm_login           varchar(32) DEFAULT '' NOT NULL,
  userobm_password        varchar(32) DEFAULT '' NOT NULL,
  userobm_perms           varchar(254),
  userobm_datebegin       date,
  userobm_archive         char(1) not null default '0',
  userobm_lastname        varchar(32),
  userobm_firstname       varchar(32),
  userobm_phone           varchar(32),
  userobm_email           varchar(60),
  userobm_timelastaccess  timestamp(14),
  PRIMARY KEY (userobm_id),
  UNIQUE k_login_user (userobm_login)
);


--
-- Table structure for table 'UserObmPref'
--
CREATE TABLE UserObmPref (
   userobmpref_user_id  int(8) DEFAULT '0' NOT NULL,
   userobmpref_option   varchar(50) NOT NULL,
   userobmpref_value    varchar(50) NOT NULL
);


--
-- New table 'DisplayPref'
--
CREATE TABLE DisplayPref (
  display_user_id     int(8) NOT NULL default '0',
  display_entity      varchar(32) NOT NULL default '',
  display_fieldname   varchar(64) NOT NULL default '',
  display_fieldorder  int(3) unsigned default NULL,
  display_display     int(1) unsigned NOT NULL default '1',
  PRIMARY KEY (display_user_id, display_entity, display_fieldname),
  INDEX idx_user (display_user_id),
  INDEX idx_entity (display_entity)
) TYPE=MyISAM;


--
-- Table structure for table `GlobalPref`
--
CREATE TABLE GlobalPref (
  globalpref_option  varchar(255) NOT NULL default '',
  globalpref_value   varchar(255) NOT NULL default '',
  PRIMARY KEY (globalpref_option),
  UNIQUE KEY globalpref_option (globalpref_option)
) TYPE=MyISAM;


-------------------------------------------------------------------------------
-- References Tables
-------------------------------------------------------------------------------
--
-- Table structure for the table  'DataSource'
--
CREATE TABLE DataSource (
  datasource_id          int(8) DEFAULT '0' NOT NULL auto_increment,
  datasource_timeupdate  timestamp(14),
  datasource_timecreate  timestamp(14),
  datasource_userupdate  int(8),
  datasource_usercreate  int(8),
  datasource_name        varchar(64),
  PRIMARY KEY (datasource_id)
);


--
-- Table structure for the table  'Country'
--
CREATE TABLE Country (
  country_timeupdate  timestamp(14),
  country_timecreate  timestamp(14),
  country_userupdate  int(8),
  country_usercreate  int(8),
  country_iso3166     char(2) NOT NULL,
  country_name        varchar(64),
  country_lang        char(2) NOT NULL,
  country_phone       varchar(4),
  PRIMARY KEY (country_iso3166, country_lang)
);


-------------------------------------------------------------------------------
-- Company module tables
-------------------------------------------------------------------------------
-- 
-- Table structure for table 'CompanyType'
--
CREATE TABLE CompanyType (
  companytype_id          int(8) DEFAULT '0' NOT NULL auto_increment,
  companytype_timeupdate  timestamp(14),
  companytype_timecreate  timestamp(14),
  companytype_userupdate  int(8),
  companytype_usercreate  int(8),
  companytype_label       char(12),
  PRIMARY KEY (companytype_id)
);


-- 
-- Table structure for table 'CompanyActivity'
--
CREATE TABLE CompanyActivity (
  companyactivity_id          int(8) DEFAULT '0' NOT NULL auto_increment,
  companyactivity_timeupdate  timestamp(14),
  companyactivity_timecreate  timestamp(14),
  companyactivity_userupdate  int(8),
  companyactivity_usercreate  int(8),
  companyactivity_label       varchar(64),
  PRIMARY KEY (companyactivity_id)
);


-- 
-- Table structure for table 'CompanyNafCode'
--
CREATE TABLE CompanyNafCode (
  companynafcode_id          int(8) DEFAULT '0' NOT NULL auto_increment,
  companynafcode_timeupdate  timestamp(14),
  companynafcode_timecreate  timestamp(14),
  companynafcode_userupdate  int(8),
  companynafcode_usercreate  int(8),
  companynafcode_title       int(1) NOT NULL DEFAULT '0',
  companynafcode_code        varchar(4),
  companynafcode_label       varchar(128),
  PRIMARY KEY (companynafcode_id)
);


--
-- Table structure for table 'Company'
--
CREATE TABLE Company (
  company_id                   int(8) DEFAULT '0' NOT NULL auto_increment,
  company_timeupdate           timestamp(14),
  company_timecreate           timestamp(14),
  company_userupdate           int(8),
  company_usercreate           int(8),
  company_datasource_id        int(8),
  company_number               varchar(32),
  company_vat                  varchar(20),
  company_archive              char(1) DEFAULT '0' NOT NULL,
  company_name                 varchar(96) DEFAULT '' NOT NULL,
  company_aka                  varchar(255),
  company_sound                varchar(48),
  company_type_id              int(8),
  company_activity_id          int(8),
  company_nafcode_id           int(8),
  company_marketingmanager_id  int(8),
  company_address1             varchar(64),
  company_address2             varchar(64),
  company_address3             varchar(64),
  company_zipcode              varchar(14),
  company_town                 varchar(64),
  company_expresspostal        varchar(16),
  company_country_iso3166      char(2) DEFAULT '',
  company_phone                varchar(32),
  company_fax                  varchar(32),
  company_web                  varchar(64),
  company_email                varchar(64),
  company_contact_number       int(5) DEFAULT '0' NOT NULL,
  company_deal_number          int(5) DEFAULT '0' NOT NULL,
  company_deal_total           int(5) DEFAULT '0' NOT NULL,
  company_comment              text,
  PRIMARY KEY (company_id)
);


--
-- Table structure for table 'CompanyCategory'
--
CREATE TABLE CompanyCategory (
  companycategory_id          int(8) NOT NULL auto_increment,
  companycategory_timeupdate  timestamp(14),
  companycategory_timecreate  timestamp(14),
  companycategory_userupdate  int(8) NOT NULL default '0',
  companycategory_usercreate  int(8) NOT NULL default '0',
  companycategory_code        varchar(10) NOT NULL default '',
  companycategory_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (companycategory_id)
);


--
-- Table structure for table 'CompanyCategoryLink'
--
CREATE TABLE CompanyCategoryLink (
  companycategorylink_category_id  int(8) NOT NULL default '0',
  companycategorylink_company_id   int(8) NOT NULL default '0',
  PRIMARY KEY (companycategorylink_category_id,companycategorylink_company_id)
);


-------------------------------------------------------------------------------
-- Contact module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'Contact'
--
CREATE TABLE Contact (
  contact_id                   int(8) DEFAULT '0' NOT NULL auto_increment,
  contact_timeupdate           timestamp(14),
  contact_timecreate           timestamp(14),
  contact_userupdate           int(8),
  contact_usercreate           int(8),
  contact_datasource_id        int(8),
  contact_company_id           int(8),
  contact_kind_id              int(8),
  contact_marketingmanager_id  int(8),
  contact_lastname             varchar(64) DEFAULT '' NOT NULL,
  contact_firstname            varchar(64),
  contact_service              varchar(64),
  contact_address1             varchar(64),
  contact_address2             varchar(64),
  contact_address3             varchar(64),
  contact_zipcode              varchar(14),
  contact_town                 varchar(64),
  contact_expresspostal        varchar(16),
  contact_country_iso3166      char(2) DEFAULT '',
  contact_function_id          int(8),
  contact_title                varchar(64),
  contact_phone                varchar(32),
  contact_homephone            varchar(32),
  contact_mobilephone          varchar(32),
  contact_fax                  varchar(32),
  contact_email                varchar(128),
  contact_email2               varchar(128),
  contact_mailing_ok           char(1) DEFAULT '0',
  contact_archive              char(1) DEFAULT '0',
  contact_privacy              int(2) NOT NULL DEFAULT '0',
  contact_comment              text,
  PRIMARY KEY (contact_id)
);


--
-- Table structure for table 'Kind'
--
CREATE TABLE Kind (
  kind_id          int(8) DEFAULT '0' NOT NULL auto_increment,
  kind_timeupdate  timestamp(14),
  kind_timecreate  timestamp(14),
  kind_userupdate  int(8),
  kind_usercreate  int(8),
  kind_minilabel   varchar(64),
  kind_header      varchar(64),
  kind_lang        char(2),
  kind_default     int(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (kind_id)
);


--
-- Table structure for the table 'Function'
--
CREATE TABLE Function (
  function_id          int(8) DEFAULT '0' NOT NULL auto_increment,
  function_timeupdate  timestamp(14),
  function_timecreate  timestamp(14),
  function_userupdate  int(8),
  function_usercreate  int(8),
  function_label       varchar(64),
  PRIMARY KEY (function_id)
);


--
-- Table structure for table 'ContactCategory1'
--
CREATE TABLE ContactCategory1 (
  contactcategory1_id          int(8) NOT NULL auto_increment,
  contactcategory1_timeupdate  timestamp(14),
  contactcategory1_timecreate  timestamp(14),
  contactcategory1_userupdate  int(8) default '0',
  contactcategory1_usercreate  int(8) default '0',
  contactcategory1_code        int(8) default '0',
  contactcategory1_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (contactcategory1_id)
);


--
-- Table structure for table 'ContactCategory1Link'
--
CREATE TABLE ContactCategory1Link (
  contactcategory1link_category_id  int(8) NOT NULL default '0',
  contactcategory1link_contact_id   int(8) NOT NULL default '0',
  PRIMARY KEY (contactcategory1link_category_id,contactcategory1link_contact_id)
);


--
-- Table structure for table 'ContactCategory2'
--
CREATE TABLE ContactCategory2 (
  contactcategory2_id          int(8) NOT NULL auto_increment,
  contactcategory2_timeupdate  timestamp(14),
  contactcategory2_timecreate  timestamp(14),
  contactcategory2_userupdate  int(8) default '0',
  contactcategory2_usercreate  int(8) default '0',
  contactcategory2_code        int(8) default '0',
  contactcategory2_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (contactcategory2_id)
);


--
-- Table structure for table 'ContactCategory2Link'
--
CREATE TABLE ContactCategory2Link (
  contactcategory2link_category_id  int(8) NOT NULL default '0',
  contactcategory2link_contact_id   int(8) NOT NULL default '0',
  PRIMARY KEY (contactcategory2link_category_id,contactcategory2link_contact_id)
);


-------------------------------------------------------------------------------
-- Deal module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'ParentDeal'
--
CREATE TABLE ParentDeal (
  parentdeal_id                   int(8) NOT NULL auto_increment,
  parentdeal_timeupdate           timestamp(14),
  parentdeal_timecreate           timestamp(14),
  parentdeal_userupdate           int(8),
  parentdeal_usercreate           int(8),
  parentdeal_label                varchar(128) NOT NULL,
  parentdeal_marketingmanager_id  int(8),
  parentdeal_technicalmanager_id  int(8),
  parentdeal_archive              char(1) DEFAULT '0',
  parentdeal_comment              text,
  PRIMARY KEY (parentdeal_id)
);


--
-- Table structure for table 'Deal'
--
CREATE TABLE Deal (
  deal_id                   int(8) DEFAULT '0' NOT NULL auto_increment,
  deal_timeupdate           timestamp(14),
  deal_timecreate           timestamp(14),
  deal_userupdate           int(8),
  deal_usercreate           int(8),
  deal_number               varchar(32),
  deal_label                varchar(128),
  deal_datebegin            date,
  deal_parentdeal_id        int(8),
  deal_type_id              int(8),
  deal_tasktype_id          int(8),
  deal_company_id           int(8) DEFAULT '0' NOT NULL,
  deal_contact1_id          int(8),
  deal_contact2_id          int(8),
  deal_marketingmanager_id  int(8),
  deal_technicalmanager_id  int(8),
  deal_dateproposal         date,
  deal_amount               decimal(12,2),
  deal_hitrate              int(3) DEFAULT 0,
  deal_status_id            int(2),
  deal_datealarm            date,
  deal_archive              char(1) DEFAULT '0',
  deal_todo                 varchar(128),
  deal_privacy              int(2) NOT NULL DEFAULT '0',
  deal_comment              text,
  PRIMARY KEY (deal_id)
);


--
-- Table structure for table 'DealStatus'
--
CREATE TABLE DealStatus (
  dealstatus_id          int(2) DEFAULT '0' NOT NULL auto_increment,
  dealstatus_timeupdate  timestamp(14),
  dealstatus_timecreate  timestamp(14),
  dealstatus_userupdate  int(8),
  dealstatus_usercreate  int(8),
  dealstatus_label       varchar(24),
  dealstatus_order       int(2),
  dealstatus_hitrate     char(3),
  PRIMARY KEY (dealstatus_id)
);


--
-- Table structure for table 'DealType'
--
CREATE TABLE DealType (
  dealtype_id          int(8) DEFAULT '0' NOT NULL auto_increment,
  dealtype_timeupdate  timestamp(14),
  dealtype_timecreate  timestamp(14),
  dealtype_userupdate  int(8),
  dealtype_usercreate  int(8),
  dealtype_label       varchar(16),
  dealtype_inout       varchar(1) DEFAULT '-',
  PRIMARY KEY (dealtype_id)
);


--
-- Table structure for table 'DealCategory'
--
CREATE TABLE DealCategory (
  dealcategory_id          int(8) NOT NULL auto_increment,
  dealcategory_timeupdate  timestamp(14),
  dealcategory_timecreate  timestamp(14),
  dealcategory_userupdate  int(8) default '0',
  dealcategory_usercreate  int(8) default '0',
  dealcategory_code        int(8) default '0',
  dealcategory_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (dealcategory_id)
);


-- Table structure for table 'DealCategoryLink'
--
CREATE TABLE DealCategoryLink (
  dealcategorylink_category_id  int(8) NOT NULL default '0',
  dealcategorylink_deal_id      int(8) NOT NULL default '0',
  PRIMARY KEY (dealcategorylink_category_id,dealcategorylink_deal_id)
);


-------------------------------------------------------------------------------
-- List module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'List'
--
CREATE TABLE List (
  list_id          int(8) NOT NULL auto_increment,
  list_timeupdate  timestamp(14),
  list_timecreate  timestamp(14),
  list_userupdate  int(8),
  list_usercreate  int(8),
  list_privacy     int(2) NOT NULL DEFAULT '0',
  list_name        varchar(64) NOT NULL,
  list_subject     varchar(128),
  list_email       varchar(128),
  list_mailing_ok  int(1),
  list_static_nb   int(10) DEFAULT 0,
  list_query_nb    int(10) DEFAULT 0,
  list_query       text,
  list_structure   text,
  PRIMARY KEY (list_id),
  UNIQUE list_name (list_name)
);


--
-- Table structure for table 'ContactList'
--
CREATE TABLE ContactList (
  contactlist_list_id     int(8) DEFAULT '0' NOT NULL,
  contactlist_contact_id  int(8) DEFAULT '0' NOT NULL
);


-------------------------------------------------------------------------------
-- Calendar module tables
-------------------------------------------------------------------------------
--
-- Table structure for the table  'CalendarSegment'
--
CREATE TABLE CalendarSegment (
  calendarsegment_eventid     int(8) NOT NULL default '0',
  calendarsegment_customerid  int(8) NOT NULL default '0',
  calendarsegment_date        timestamp(14) NOT NULL,
  calendarsegment_flag        varchar(5) NOT NULL default '',
  calendarsegment_type        varchar(5) NOT NULL default '',
  calendarsegment_state       char(1) NOT NULL default '',
  PRIMARY KEY (calendarsegment_eventid,calendarsegment_customerid,calendarsegment_date,calendarsegment_flag,calendarsegment_type)
);


--
-- Table structure for the table  'CalendarEvent'
--
CREATE TABLE CalendarEvent (
  calendarevent_id int(8)    NOT NULL auto_increment,
  calendarevent_timeupdate   timestamp(14),
  calendarevent_timecreate   timestamp(14),
  calendarevent_userupdate   int(8) default NULL,
  calendarevent_usercreate   int(8) default NULL,
  calendarevent_title        varchar(255) default NULL,
  calendarevent_description  text,
  calendarevent_category_id  int(8) default NULL,
  calendarevent_priority     int(2) default NULL,
  calendarevent_privacy      int(2) NOT NULL default '0',
  calendarevent_length       int(4) NOT NULL default '',
  calendarevent_repeatkind   varchar(20) default NULL,
  calendarevent_repeatdays   varchar(7) default NULL,
  calendarevent_endrepeat    timestamp(14) NOT NULL,
  PRIMARY KEY (calendarevent_id)
);

    
--
-- Table structure for table 'CalendarCategory'
--
CREATE TABLE CalendarCategory (
  calendarcategory_id          int(8) NOT NULL auto_increment,
  calendarcategory_timeupdate  timestamp(14),
  calendarcategory_timecreate  timestamp(14),
  calendarcategory_userupdate  int(8) default NULL,
  calendarcategory_usercreate  int(8) default NULL,
  calendarcategory_label       varchar(128) default NULL,
  PRIMARY KEY (calendarcategory_id)
);


--
-- Table structure for table 'CalendarRight'
--
CREATE TABLE CalendarRight (
  calendarright_ownerid     int(8) NOT NULL default '0',
  calendarright_customerid  int(8) NOT NULL default '0',
  calendarright_write       int(1) NOT NULL default '0',
  calendarright_read        int(1) NOT NULL default '0',
  PRIMARY KEY (calendarright_ownerid,calendarright_customerid)
);


--
-- structure fot table 'RepeatKind'
--
CREATE TABLE RepeatKind (
  repeatkind_id          int(8) NOT NULL auto_increment,
  repeatkind_timeupdate  timestamp(14),
  repeatkind_timecreate  timestamp(14),
  repeatkind_userupdate  int(8),
  repeatkind_usercreate  int(8),
  repeatkind_label       varchar(128),
  PRIMARY KEY(repeatkind_id)	
);


-------------------------------------------------------------------------------
-- Todo
-------------------------------------------------------------------------------
-- Create new table
CREATE TABLE Todo (
  todo_id          int(8) DEFAULT '0' NOT NULL auto_increment,
  todo_timeupdate  timestamp(14),
  todo_timecreate  timestamp(14),
  todo_userupdate  int(8),
  todo_usercreate  int(8),
  todo_user        int(8),
  todo_date        timestamp(14) default NULL,
  todo_deadline    timestamp(14) default NULL,
  todo_priority    int(8) default NULL,
  todo_title       varchar(80) default NULL,
  todo_content     text default NULL,
  PRIMARY KEY (todo_id)
);


-------------------------------------------------------------------------------
-- Document module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'Document'
--
CREATE TABLE Document (
  document_id          	 int(8) NOT NULL auto_increment,
  document_timeupdate  	 timestamp(14),
  document_timecreate  	 timestamp(14),
  document_userupdate  	 int(8) default NULL,
  document_usercreate  	 int(8) default NULL,
  document_title       	 varchar(255) default NULL,
  document_name        	 varchar(255) default NULL,
  document_kind        	 int(2) default NULL,
  document_mimetype    	 varchar(255) default NULL,
  document_category1_id  int(8) not null default '0',
  document_category2_id  int(8) not null default '0',
  document_author      	 varchar(255) default NULL,
  document_privacy     	 int(2) not null default '0',
  document_path        	 text default NULL,
  document_size        	 int(15) default NULL,
  PRIMARY KEY (document_id)
);


--
-- Table structure for table 'DocumentCategory1'
--
CREATE TABLE DocumentCategory1 (
  documentcategory1_id          int(8) NOT NULL auto_increment,
  documentcategory1_timeupdate  timestamp(14),
  documentcategory1_timecreate  timestamp(14),
  documentcategory1_userupdate  int(8) default NULL,
  documentcategory1_usercreate  int(8) default NULL,
  documentcategory1_label       varchar(255) default NULL,
  PRIMARY KEY (documentcategory1_id)
);


--
-- Table structure for table 'DocumentCategory2'
--
CREATE TABLE DocumentCategory2 (
  documentcategory2_id          int(8) NOT NULL auto_increment,
  documentcategory2_timeupdate  timestamp(14),
  documentcategory2_timecreate  timestamp(14),
  documentcategory2_userupdate  int(8) default NULL,
  documentcategory2_usercreate  int(8) default NULL,
  documentcategory2_label       varchar(255) default NULL,
  PRIMARY KEY (documentcategory2_id)
);


--
-- Table structure for table 'DocumentMimeType'
--
CREATE TABLE DocumentMimeType (
  documentmimetype_id          int(8) NOT NULL auto_increment,
  documentmimetype_timeupdate  timestamp(14),
  documentmimetype_timecreate  timestamp(14),
  documentmimetype_userupdate  int(8) default NULL,
  documentmimetype_usercreate  int(8) default NULL,
  documentmimetype_label       varchar(255) default NULL,
  documentmimetype_extension   varchar(10) default NULL,
  documentmimetype_mime        varchar(255) default NULL,
  PRIMARY KEY (documentmimetype_id)
);


--
-- Table structure for table 'DocumentEntity'
--
CREATE TABLE DocumentEntity (
  documententity_document_id  int(8) NOT NULL,
  documententity_entity_id    int(8) NOT NULL,
  documententity_entity       varchar(255) NOT NULL,
  PRIMARY KEY (documententity_document_id, documententity_entity_id, documententity_entity)
);


-------------------------------------------------------------------------------
-- Project module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'Project'
--
CREATE TABLE Project (
  project_id             int(8) DEFAULT '0' NOT NULL auto_increment,
  project_timeupdate     timestamp(14),
  project_timecreate     timestamp(14),
  project_userupdate     int(8),
  project_usercreate     int(8),
  project_name           varchar(128),
  project_tasktype_id    int(8),
  project_company_id     int(8),
  project_deal_id        int(8),
  project_soldtime       int(8) DEFAULT NULL,
  project_estimatedtime  int(8) DEFAULT NULL,
  project_datebegin      date,
  project_dateend        date,
  project_archive        char(1) DEFAULT '0',
  project_comment        text,
  PRIMARY KEY (project_id),
  INDEX project_idx_comp (project_company_id),
  INDEX project_idx_deal (project_deal_id)
);


--
-- Table structure for table 'ProjectTask'
--
CREATE TABLE ProjectTask (
  projecttask_id             int(8) DEFAULT '0' NOT NULL auto_increment,
  projecttask_project_id     int(8) NOT NULL,
  projecttask_timeupdate     timestamp(14),
  projecttask_timecreate     timestamp(14),
  projecttask_userupdate     int(8) default NULL,
  projecttask_usercreate     int(8) default NULL,
  projecttask_label          varchar(128) default NULL,
  projecttask_parenttask_id  int(8) default 0,
  projecttask_rank           int(8) default NULL,
  PRIMARY KEY (projecttask_id),
  INDEX pt_idx_pro (projecttask_project_id)
);


--
-- Table structure for table 'ProjectUser'
--
CREATE TABLE ProjectUser (
  projectuser_id              int(8) DEFAULT '0' NOT NULL auto_increment,
  projectuser_project_id      int(8) NOT NULL,
  projectuser_user_id         int(8) NOT NULL,
  projectuser_projecttask_id  int(8),
  projectuser_timeupdate      timestamp(14),
  projectuser_timecreate      timestamp(14),
  projectuser_userupdate      int(8) default NULL,
  projectuser_usercreate      int(8) default NULL,
  projectuser_projectedtime   int(8) default NULL,
  projectuser_missingtime     int(8) default NULL,
  projectuser_validity        timestamp(14) default NULL,
  projectuser_soldprice       int(8) default NULL,
  projectuser_manager         int(1) default NULL,
  PRIMARY KEY (projectuser_id),
  INDEX pu_idx_pro (projectuser_project_id),
  INDEX pu_idx_user (projectuser_user_id),
  INDEX pu_idx_pt (projectuser_projecttask_id)
);


--
-- Table structure for table 'ProjectStat'
--
CREATE TABLE ProjectStat (
  projectstat_project_id     int(8) NOT NULL,
  projectstat_usercreate     int(8) NOT NULL,
  projectstat_date           timestamp(14) NOT NULL,
  projectstat_useddays       int(8) default NULL,
  projectstat_remainingdays  int(8) default NULL,
  PRIMARY KEY (projectstat_project_id, projectstat_usercreate, projectstat_date)
);


-------------------------------------------------------------------------------
-- Timemanagement tables
-------------------------------------------------------------------------------
--
-- Task table
--
CREATE TABLE TimeTask (
  timetask_id              int(8) NOT NULL auto_increment,
  timetask_timeupdate      timestamp(14),
  timetask_timecreate      timestamp(14),
  timetask_userupdate      int(8) default NULL,
  timetask_usercreate      int(8) default NULL,
  timetask_user_id         int(8) default NULL,
  timetask_date            timestamp(14) NOT NULL,
  timetask_projecttask_id  int(8) default NULL,
  timetask_length          int(2) default NULL,
  timetask_tasktype_id     int(8) default NULL,
  timetask_label           varchar(255) default NULL,
  timetask_status          int(1) default NULL,
  PRIMARY KEY (timetask_id),
  INDEX tt_idx_pt (timetask_projecttask_id)
) TYPE=MyISAM;


--
-- TaskType table
--
CREATE TABLE TaskType (
  tasktype_id          int(8) NOT NULL auto_increment,
  tasktype_timeupdate  timestamp(14),
  tasktype_timecreate  timestamp(14),
  tasktype_userupdate  int(8) default NULL,
  tasktype_usercreate  int(8) default NULL,
  tasktype_internal    int(1) NOT NULL,
  tasktype_label       varchar(32) default NULL,
  PRIMARY KEY (tasktype_id)
) TYPE=MyISAM;


-------------------------------------------------------------------------------
-- Support tables
-------------------------------------------------------------------------------
--
-- New table 'Contract'
--
CREATE TABLE Contract (
  contract_id                int(8) NOT NULL auto_increment,
  contract_timeupdate        timestamp(14),
  contract_timecreate        timestamp(14),
  contract_userupdate        int(8) default NULL,
  contract_usercreate        int(8) default NULL,
  contract_label             varchar(64) default NULL,
  contract_deal_id           int(8) default NULL,
  contract_company_id        int(8) default NULL,
  contract_number            varchar(20) default NULL,
  contract_datebegin         date default NULL,
  contract_dateexp           date default NULL,
  contract_type_id           int(8) default NULL,
  contract_contact1_id       int(8) default NULL,
  contract_contact2_id       int(8) default NULL,
  contract_techmanager_id    int(8) default NULL,
  contract_marketmanager_id  int(8) default NULL,
  contract_clause            text,
  contract_comment           text,
  contract_archive           int(1) default 0,
  PRIMARY KEY (contract_id)
) TYPE=MyISAM;


--
-- New table 'ContractType'
--
CREATE TABLE ContractType (
  contracttype_id          int(8) NOT NULL auto_increment,
  contracttype_timeupdate  timestamp(14),
  contracttype_timecreate  timestamp(14),
  contracttype_userupdate  int(8) default NULL,
  contracttype_usercreate  int(8) default NULL,
  contracttype_label       varchar(40) default NULL,
  PRIMARY KEY (contracttype_id)
) TYPE=MyISAM;


--
-- New table 'Incident'
--
CREATE TABLE Incident (
  incident_id           int(8) NOT NULL auto_increment,
  incident_timeupdate   timestamp(14),
  incident_timecreate   timestamp(14),
  incident_userupdate   int(8) default NULL,
  incident_usercreate   int(8) default NULL,
  incident_contract_id  int(8) NOT NULL,
  incident_label        varchar(100) default NULL,
  incident_date         date default NULL,
  incident_priority_id  int(8) default NULL,
  incident_status_id    int(8) default NULL,
  incident_logger       int(8) default NULL,
  incident_owner        int(8) default NULL,
  incident_duration     char(4) default '0',
  incident_archive      char(1) NOT NULL default '0',
  incident_description  text,
  incident_resolution   text,
  PRIMARY KEY (incident_id)
) TYPE=MyISAM;


--
-- New table 'IncidentPriority'
--
CREATE TABLE IncidentPriority (
  incidentpriority_id          int(8) NOT NULL auto_increment,
  incidentpriority_timeupdate  timestamp(14),
  incidentpriority_timecreate  timestamp(14),
  incidentpriority_userupdate  int(8) default NULL,
  incidentpriority_usercreate  int(8) default NULL,
  incidentpriority_order       int(2),
  incidentpriority_color       char(6),
  incidentpriority_label       varchar(32) default NULL,
  PRIMARY KEY (incidentpriority_id)
) TYPE=MyISAM;


--
-- New table 'IncidentStatus'
--
CREATE TABLE IncidentStatus (
  incidentstatus_id          int(8) NOT NULL auto_increment,
  incidentstatus_timeupdate  timestamp(14),
  incidentstatus_timecreate  timestamp(14),
  incidentstatus_userupdate  int(8) default NULL,
  incidentstatus_usercreate  int(8) default NULL,
  incidentstatus_order       int(2),
  incidentstatus_label       varchar(32) default NULL,
  PRIMARY KEY (incidentstatus_id)
) TYPE=MyISAM;


-------------------------------------------------------------------------------
-- Accounting Section tables
-------------------------------------------------------------------------------
--
-- New table 'Invoice'
--
CREATE TABLE Invoice ( 
  invoice_id                int(8) NOT NULL auto_increment,
  invoice_timeupdate        timestamp(14),
  invoice_timecreate        timestamp(14),
  invoice_userupdate        int(8),
  invoice_usercreate        int(8),
  invoice_company_id        int(8) NOT NULL,
  invoice_deal_id           int(8) default NULL,
  invoice_project_id        int(8) default NULL,
  invoice_number            varchar(10) DEFAULT '0',
  invoice_label             varchar(40) NOT NULL DEFAULT '',
  invoice_amount_ht         double(10,2),
  invoice_amount_ttc        double(10,2),
  invoice_status_id         int(4) DEFAULT '0' NOT NULL,
  invoice_date              date not NULL DEFAULT '0000-00-00',
  invoice_payment_date      date,
  invoice_inout             char(1),
  invoice_archive           char(1) NOT NULL DEFAULT '0',
  invoice_comment           text,
  PRIMARY KEY (invoice_id)
);


--
-- New table 'InvoiceStatus'
--
CREATE TABLE InvoiceStatus (
  invoicestatus_id       int(8) NOT NULL auto_increment,
  invoicestatus_payment  int(1) DEFAULT '0' NOT NULL,
  invoicestatus_archive  int(1) DEFAULT '0' NOT NULL,
  invoicestatus_label    varchar(24) default '' NOT NULL,
  PRIMARY KEY (invoicestatus_id)
);


--
-- New table 'Payment'
--
CREATE TABLE  Payment (
  payment_id              int(8) NOT NULL auto_increment,
  payment_timeupdate      timestamp(14),
  payment_timecreate      timestamp(14),
  payment_userupdate      int(8),
  payment_usercreate      int(8),
  payment_company_id      int(8) NOT NULL,
  payment_number          int(10) default null,
  payment_date            date,
  payment_expected_date   date,		
  payment_amount          double(10,2) DEFAULT '0.0' NOT NULL,
  payment_label           varchar(40) NOT NULL DEFAULT '',
  payment_paymentkind_id  int(8),
  payment_account_id      int(8),
  payment_inout           char(1) NOT NULL,
  payment_paid            char(1) NOT NULL DEFAULT '0',
  payment_checked         char(1) NOT NULL DEFAULT '0',
  payment_comment         text,
  PRIMARY KEY (payment_id)
);


--
-- New table 'PaymentKind'
--
CREATE TABLE PaymentKind (
  paymentkind_id          int(8) NOT NULL auto_increment,
  paymentkind_shortlabel  varchar(3) NOT NULL DEFAULT '',
  paymentkind_longlabel   varchar(40) NOT NULL DEFAULT '',
  PRIMARY KEY (paymentkind_id)
);


--
-- New table 'PaymentInvoice'
--
CREATE TABLE PaymentInvoice (
  paymentinvoice_invoice_id  int(8) NOT NULL,
  paymentinvoice_payment_id  int(8) NOT NULL,
  paymentinvoice_timeupdate  timestamp(14),
  paymentinvoice_timecreate  timestamp(14),
  paymentinvoice_userupdate  int(8),
  paymentinvoice_usercreate  int(8),
  paymentinvoice_amount      double (10,2) NOT NULL DEFAULT '0',
  PRIMARY KEY (paymentinvoice_invoice_id,paymentinvoice_payment_id)
);


--
-- New table 'Account'
--
CREATE TABLE Account (
  account_id	      int(8) DEFAULT '0' NOT NULL auto_increment,
  account_timeupdate  timestamp(14),
  account_timecreate  timestamp(14),
  account_userupdate  int(8),
  account_usercreate  int(8),
  account_bank	      varchar(60) DEFAULT '' NOT NULL,
  account_number      varchar(11) DEFAULT '0' NOT NULL,
  account_balance     double(15,2) DEFAULT '0.00' NOT NULL,
  account_today	      double(15,2) DEFAULT '0.00' NOT NULL,
  account_comment     varchar(100),
  account_label	      varchar(40) NOT NULL DEFAULT '',
  PRIMARY KEY (account_id)
);


--
-- EntryTemp and PaymentTemp are used when importing data from the bank files
--

--
-- New table 'EntryTemp'
--
CREATE TABLE EntryTemp (
  entrytemp_id        int(8) not null default '0' auto_increment,
  entrytemp_label     varchar(40),
  entrytemp_amount    double(10,2) not null default '0.00',
  entrytemp_type      varchar(100),
  entrytemp_date      date not null default '0000-00-00',
  entrytemp_realdate  date not null default '0000-00-00',
  entrytemp_comment   varchar(100),
  entrytemp_checked   char(1) not null default '0',
  PRIMARY KEY (entrytemp_id)
);


--
-- New table 'PaymentTemp'
--
CREATE TABLE  PaymentTemp (
  paymenttemp_id              int(8) NOT NULL auto_increment,
  paymenttemp_timeupdate      timestamp(14),
  paymenttemp_timecreate      timestamp(14),
  paymenttemp_usercreate      int(8),
  paymenttemp_userupdate      int(8),
  paymenttemp_number          int(10) default null,
  paymenttemp_date            date,
  paymenttemp_expected_date   date,		
  paymenttemp_amount          double(10,2) DEFAULT '0.0' NOT NULL,
  paymenttemp_label           varchar(40) NOT NULL DEFAULT '',
  paymenttemp_paymentkind_id  int(8),
  paymenttemp_account_id      int(8),
  paymenttemp_comment         text,
  paymenttemp_inout           char(1) NOT NULL,
  paymenttemp_paid            char(1) NOT NULL DEFAULT '0',
  paymenttemp_checked         char(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (paymenttemp_id)
);


-------------------------------------------------------------------------------
-- Group module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'UGroup' (cause Group is a reserved keyword)
--
CREATE TABLE UGroup (
  group_id          int(8) NOT NULL auto_increment,
  group_timeupdate  timestamp(14),
  group_timecreate  timestamp(14),
  group_userupdate  int(8),
  group_usercreate  int(8),
  group_system      int(1) DEFAULT '0',
  group_name        varchar(32) NOT NULL,
  group_desc        varchar(128),
  group_email       varchar(128),
  PRIMARY KEY (group_id),
  UNIQUE group_name (group_name)
);


--
-- Table structure for table 'UserObmGroup'
--
CREATE TABLE UserObmGroup (
  userobmgroup_group_id    int(8) DEFAULT '0' NOT NULL,
  userobmgroup_userobm_id  int(8) DEFAULT '0' NOT NULL
);


--
-- Table structure for table 'GroupGroup'
--
CREATE TABLE GroupGroup (
  groupgroup_parent_id  int(8) DEFAULT '0' NOT NULL,
  groupgroup_child_id   int(8) DEFAULT '0' NOT NULL
);


-------------------------------------------------------------------------------
-- Import module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'Import'
--
CREATE TABLE Import (
  import_id                   int(8) DEFAULT '0' NOT NULL auto_increment,
  import_timeupdate           timestamp(14),
  import_timecreate           timestamp(14),
  import_userupdate           int(8),
  import_usercreate           int(8),
  import_name                 varchar(64) NOT NULL,
  import_datasource_id        int(8),
  import_marketingmanager_id  int(8),
  import_separator            varchar(3),
  import_enclosed             char(1),
  import_desc                 text,
  PRIMARY KEY (import_id),
  UNIQUE (import_name)
);


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
  publication_lang           varchar(30),
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
  publicationtype_label       varchar(64),
  PRIMARY KEY (publicationtype_id)
);
        


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
  subscription_renewal          int(1) DEFAULT '0' NOT NULL,
  subscription_reception_id     int(8) DEFAULT '0' NOT NULL,
  subscription_date_begin       timestamp(14),
  subscription_date_end         timestamp(14),
  PRIMARY KEY (subscription_publication_id,subscription_contact_id)
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
