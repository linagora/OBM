--/////////////////////////////////////////////////////////////////////////////
--// OBM - File : create_obmdb_2.1.pgsql.sql                                 //
--//     - Desc : PostgreSQL Database 2.1 creation script                    //
--// 2007-04-22 Pierre Baudracco                                             //
--/////////////////////////////////////////////////////////////////////////////
-- $Id$
--/////////////////////////////////////////////////////////////////////////////

BEGIN;


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
  obmsession_timeupdate  timestamp,
  obmsession_name        varchar(32) NOT NULL default '',
  obmsession_data        text,
  PRIMARY KEY (obmsession_sid, obmsession_name)
);

--
-- Table structure for table 'ActiveUserObm'
--
CREATE TABLE ActiveUserObm (
  activeuserobm_sid            varchar(32) NOT NULL DEFAULT '',
  activeuserobm_session_name   varchar(32) NOT NULL DEFAULT '',
  activeuserobm_userobm_id     integer DEFAULT NULL,
  activeuserobm_timeupdate     timestamp,
  activeuserobm_timecreate     timestamp,
  activeuserobm_nb_connexions  integer NOT NULL DEFAULT 0,
  activeuserobm_lastpage       varchar(64) NOT NULL DEFAULT '0',
  activeuserobm_ip             varchar(32) NOT NULL DEFAULT '0',
  PRIMARY KEY (activeuserobm_sid)
);


--
-- Table structure for table 'UserObm_SessionLog'
--
CREATE TABLE UserObm_SessionLog (
  userobm_sessionlog_sid            varchar(32) NOT NULL DEFAULT '',
  userobm_sessionlog_session_name   varchar(32) NOT NULL DEFAULT '',
  userobm_sessionlog_userobm_id     integer DEFAULT NULL,
  userobm_sessionlog_timeupdate     timestamp,
  userobm_sessionlog_timecreate     timestamp,
  userobm_sessionlog_nb_connexions  integer NOT NULL DEFAULT 0,
  userobm_sessionlog_lastpage       varchar(32) NOT NULL DEFAULT '0',
  userobm_sessionlog_ip             varchar(32) NOT NULL DEFAULT '0',
  PRIMARY KEY (userobm_sessionlog_sid)
);


--
-- Table structure for table 'UserObm'
--
CREATE TABLE UserObm (
  userobm_id                  serial,
  userobm_domain_id           integer DEFAULT 0,
  userobm_timeupdate          timestamp,
  userobm_timecreate          timestamp,
  userobm_userupdate          integer,
  userobm_usercreate          integer,
  userobm_local               integer DEFAULT 1,
  userobm_ext_id              varchar(16),
  userobm_system              integer default 0,
  userobm_archive             char(1) not null DEFAULT '0',
  userobm_timelastaccess      timestamp,
  userobm_login               varchar(32) DEFAULT '' NOT NULL,
  userobm_password_type       varchar(6) DEFAULT 'PLAIN' NOT NULL,
  userobm_password            varchar(64) DEFAULT '' NOT NULL,
  userobm_perms               varchar(254),
  userobm_calendar_version    timestamp,
  userobm_uid                 integer,
  userobm_gid                 integer,
  userobm_datebegin           date,
  userobm_lastname            varchar(32) DEFAULT '',
  userobm_firstname           varchar(48) DEFAULT '',
  userobm_title               varchar(64) DEFAULT '',
  userobm_sound               varchar(48),
  userobm_service             varchar(64),
  userobm_address1            varchar(64),
  userobm_address2            varchar(64),
  userobm_address3            varchar(64),
  userobm_zipcode             varchar(14),
  userobm_town                varchar(64),
  userobm_expresspostal       varchar(16),
  userobm_country_iso3166     char(2) DEFAULT '0',
  userobm_phone               varchar(32) DEFAULT '',
  userobm_phone2              varchar(32) DEFAULT '',
  userobm_mobile              varchar(32) DEFAULT '',
  userobm_fax                 varchar(32) DEFAULT '',
  userobm_fax2                varchar(32) DEFAULT '',
  userobm_web_perms           integer default NULL,
  userobm_web_list 	      text default NULL,  
  userobm_web_all	      integer default 0,
  userobm_mail_perms          integer default NULL,
  userobm_mail_ext_perms      integer default NULL,
  userobm_email               text DEFAULT '',
  userobm_mail_server_id      integer default NULL,
  userobm_mail_quota          integer default 0,
  userobm_nomade_perms        integer default 0,
  userobm_nomade_enable       integer default 0,
  userobm_nomade_local_copy   integer default 0,
  userobm_email_nomade        varchar(64) default '',
  userobm_vacation_enable     integer default 0,
  userobm_vacation_message    text default '',
  userobm_samba_perms         integer default 0,
  userobm_samba_home          varchar(255) default '',
  userobm_samba_home_drive    char(2) default '',
  userobm_samba_logon_script  varchar(128) default '',
  userobm_host_id             integer default 0,
  userobm_description         varchar(255),
  userobm_location            varchar(255),
  userobm_education           varchar(255),
  PRIMARY KEY (userobm_id)
);
CREATE UNIQUE INDEX k_login_user_UserObm_index ON UserObm (userobm_login);
CREATE UNIQUE INDEX k_uid_user_UserObm_index ON UserObm (userobm_uid);


--
-- Table structure for table 'UserObmPref'
--
CREATE TABLE UserObmPref (
  userobmpref_user_id  integer DEFAULT 0 NOT NULL,
  userobmpref_option   varchar(50) NOT NULL,
  userobmpref_value    varchar(50) NOT NULL
);


--
-- New table 'DisplayPref'
--
CREATE TABLE DisplayPref (
  display_user_id     integer NOT NULL DEFAULT 0,
  display_entity      varchar(32) NOT NULL DEFAULT '',
  display_fieldname   varchar(64) NOT NULL DEFAULT '',
  display_fieldorder  integer DEFAULT NULL,
  display_display     integer NOT NULL DEFAULT 1,
  PRIMARY KEY(display_user_id, display_entity, display_fieldname)
);
create INDEX DisplayPref_user_id_index ON DisplayPref (display_user_id);
create INDEX DisplayPref_entity_index ON DisplayPref (display_entity);


--
-- Table structure for table 'Category'
--
CREATE TABLE Category (
  category_id          serial,
  category_domain_id   integer NOT NULL DEFAULT 0,
  category_timeupdate  TIMESTAMP,
  category_timecreate  TIMESTAMP,
  category_userupdate  integer,
  category_usercreate  integer,
  category_category    varchar(24) NOT NULL default '',
  category_code        varchar(10) NOT NULL default '',
  category_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (category_id)
);
CREATE INDEX cat_idx_cat ON Category (category_category);


--
-- Table structure for table 'CategoryLink'
--
CREATE TABLE CategoryLink (
  categorylink_category_id integer,
  categorylink_entity_id   integer,
  categorylink_category    varchar(24) NOT NULL default '',
  categorylink_entity      varchar(32) NOT NULL default '',
  PRIMARY KEY (categorylink_category_id, categorylink_entity_id)
);
CREATE INDEX catl_idx_entid ON CategoryLink (categorylink_entity_id);
CREATE INDEX catl_idx_cat ON CategoryLink (categorylink_category);


-------------------------------------------------------------------------------
-- References Tables
-------------------------------------------------------------------------------
--
-- Table structure for the table  'DataSource'
--
CREATE TABLE DataSource (
  datasource_id          serial,
  datasource_domain_id   integer default 0,
  datasource_timeupdate  timestamp,
  datasource_timecreate  timestamp,
  datasource_userupdate  integer,
  datasource_usercreate  integer,
  datasource_name        varchar(64),
  PRIMARY KEY (datasource_id)
);


--
-- Table structure for the table  'Country'
--
CREATE TABLE Country (
  country_domain_id   integer default 0,
  country_timeupdate  timestamp,
  country_timecreate  timestamp,
  country_userupdate  integer,
  country_usercreate  integer,
  country_iso3166     char(2) NOT NULL,
  country_name        varchar(64),
  country_lang        char(2) NOT NULL,
  country_phone       varchar(4),
  PRIMARY KEY (country_iso3166, country_lang)
);


-- 
-- Table structure for table 'Region'
--
CREATE TABLE Region (
  region_id          serial,
  region_domain_id   integer default 0,
  region_timeupdate  timestamp,
  region_timecreate  timestamp,
  region_userupdate  integer,
  region_usercreate  integer,
  region_code        varchar(10) NOT NULL default '',
  region_label       varchar(64),
  PRIMARY KEY (region_id)
);


-------------------------------------------------------------------------------
-- Company module tables
-------------------------------------------------------------------------------
-- 
-- Table structure for table 'CompanyType'
--
CREATE TABLE CompanyType (
  companytype_id          serial,
  companytype_domain_id   integer default 0,
  companytype_timeupdate  timestamp,
  companytype_timecreate  timestamp,
  companytype_userupdate  integer,
  companytype_usercreate  integer,
  companytype_code        varchar(10) NOT NULL default '',
  companytype_label       char(12),
  PRIMARY KEY (companytype_id)
);


-- 
-- Table structure for table 'CompanyActivity'
--
CREATE TABLE CompanyActivity (
  companyactivity_id          serial,
  companyactivity_domain_id   integer default 0,
  companyactivity_timeupdate  timestamp,
  companyactivity_timecreate  timestamp,
  companyactivity_userupdate  integer,
  companyactivity_usercreate  integer,
  companyactivity_code        varchar(10) NOT NULL default '',
  companyactivity_label       varchar(64),
  PRIMARY KEY (companyactivity_id)
);


-- 
-- Table structure for table 'CompanyNafCode'
--
CREATE TABLE CompanyNafCode (
  companynafcode_id          serial,
  companynafcode_domain_id   integer default 0,
  companynafcode_timeupdate  timestamp,
  companynafcode_timecreate  timestamp,
  companynafcode_userupdate  integer,
  companynafcode_usercreate  integer,
  companynafcode_title       integer NOT NULL DEFAULT 0,
  companynafcode_code        varchar(4),
  companynafcode_label       varchar(128),
  PRIMARY KEY (companynafcode_id)
);


--
-- Table structure for table 'Company'
--
CREATE TABLE Company (
  company_id                   serial,
  company_domain_id            integer default 0,
  company_timeupdate           timestamp,
  company_timecreate           timestamp,
  company_userupdate           integer,
  company_usercreate           integer,
  company_datasource_id        integer DEFAULT 0,
  company_number               varchar(32),
  company_vat                  varchar(20),
  company_siret                varchar(14) DEFAULT '',
  company_archive              char(1) DEFAULT '0' NOT NULL,
  company_name                 varchar(96) DEFAULT '' NOT NULL,
  company_aka                  varchar(255),
  company_sound                varchar(48),
  company_type_id              integer,
  company_activity_id          integer, 
  company_nafcode_id           integer, 
  company_marketingmanager_id  integer,
  company_address1             varchar(64),
  company_address2             varchar(64),
  company_address3             varchar(64),
  company_zipcode              varchar(14),
  company_town                 varchar(64),
  company_expresspostal        varchar(16),
  company_country_iso3166      char(2) DEFAULT '0',
  company_phone                varchar(32),
  company_fax                  varchar(32),
  company_web                  varchar(64),
  company_email                varchar(64),
  company_contact_number       integer DEFAULT 0 NOT NULL,
  company_deal_number          integer DEFAULT 0 NOT NULL,
  company_deal_total           integer DEFAULT 0 NOT NULL,
  company_comment              text,
  PRIMARY KEY (company_id)
);


-------------------------------------------------------------------------------
-- Contact module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'Contact'
--
CREATE TABLE Contact (
  contact_id                   serial,
  contact_domain_id            integer default 0,
  contact_timeupdate           timestamp,
  contact_timecreate           timestamp,
  contact_userupdate           integer,
  contact_usercreate           integer,
  contact_datasource_id        integer DEFAULT 0,
  contact_company_id           integer,
  contact_company              varchar(64),
  contact_kind_id              integer,
  contact_marketingmanager_id  integer,
  contact_lastname             varchar(64) DEFAULT '' NOT NULL,
  contact_firstname            varchar(64),
  contact_aka                  varchar(255),
  contact_sound                varchar(48),
  contact_service              varchar(64),
  contact_address1             varchar(64),
  contact_address2             varchar(64),
  contact_address3             varchar(64),
  contact_zipcode              varchar(14),
  contact_town                 varchar(64),
  contact_expresspostal        varchar(16),
  contact_country_iso3166      char(2) DEFAULT '0',
  contact_function_id          integer,
  contact_title                varchar(64),
  contact_phone                varchar(32),
  contact_homephone            varchar(32),
  contact_mobilephone          varchar(32),
  contact_fax                  varchar(32),
  contact_email                varchar(128),
  contact_email2               varchar(128),
  contact_mailing_ok           char(1) DEFAULT '0',
  contact_newsletter           char(1) DEFAULT '0',
  contact_archive              char(1) DEFAULT '0',
  contact_privacy              integer DEFAULT 0,
  contact_date                 TIMESTAMP,
  contact_comment              text,
  contact_comment2             text,
  contact_comment3             text,
  PRIMARY KEY (contact_id)
);


--
-- Table structure for table 'Kind'
--
CREATE TABLE Kind (
  kind_id          serial,
  kind_domain_id   integer default 0,
  kind_timeupdate  timestamp,
  kind_timecreate  timestamp,
  kind_userupdate  integer,
  kind_usercreate  integer,
  kind_minilabel   varchar(64),
  kind_header      varchar(64),
  kind_lang        char(2),
  kind_default     integer NOT NULL DEFAULT 0,
  PRIMARY KEY (kind_id)
);


--
-- Table structure for the table 'ContactFunction'
--
CREATE TABLE ContactFunction (
  contactfunction_id          serial,
  contactfunction_domain_id   integer default 0,
  contactfunction_timeupdate  timestamp,
  contactfunction_timecreate  timestamp,
  contactfunction_userupdate  integer,
  contactfunction_usercreate  integer,
  contactfunction_code        varchar(10) default '',
  contactfunction_label       varchar(64),
  PRIMARY KEY (contactfunction_id)
);


-------------------------------------------------------------------------------
-- Lead module tables
-------------------------------------------------------------------------------
--
-- Table structure for the table 'LeadSource'
--
CREATE TABLE LeadSource (
  leadsource_id          serial,
  leadsource_domain_id   integer default 0,
  leadsource_timeupdate  timestamp,
  leadsource_timecreate  timestamp,
  leadsource_userupdate  integer default 0,
  leadsource_usercreate  integer default 0,
  leadsource_code        varchar(10) default '',
  leadsource_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (leadsource_id)
);


--
-- Table structure for the table 'Lead'
--
CREATE TABLE Lead (
  lead_id          serial,
  lead_domain_id   integer default 0,
  lead_timeupdate  timestamp,
  lead_timecreate  timestamp,
  lead_userupdate  integer default 0,
  lead_usercreate  integer default 0,
  lead_source_id   integer default 0,
  lead_manager_id  integer default 0,
  lead_company_id  integer NOT NULL DEFAULT 0,
  lead_contact_id  integer NOT NULL DEFAULT 0,
  lead_privacy     integer DEFAULT 0,
  lead_name        varchar(64),
  lead_date        date,
  lead_datealarm   date,
  lead_archive     char(1) DEFAULT '0',
  lead_todo        varchar(128),
  lead_comment     text,
  PRIMARY KEY (lead_id)
);


-------------------------------------------------------------------------------
-- Deal module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'ParentDeal'
--
CREATE TABLE ParentDeal (
  parentdeal_id                   serial,
  parentdeal_domain_id            integer default 0,
  parentdeal_timeupdate           TIMESTAMP,
  parentdeal_timecreate           TIMESTAMP,
  parentdeal_userupdate           integer,
  parentdeal_usercreate           integer,
  parentdeal_label                varchar(128) NOT NULL,
  parentdeal_marketingmanager_id  integer,
  parentdeal_technicalmanager_id  integer,
  parentdeal_archive              char(1) DEFAULT '0',
  parentdeal_comment              text,
  PRIMARY KEY (parentdeal_id)
);


--
-- Table structure for table 'Deal'
--
CREATE TABLE Deal (
  deal_id                   serial,
  deal_domain_id            integer default 0,
  deal_timeupdate           timestamp,
  deal_timecreate           timestamp,
  deal_userupdate           integer,
  deal_usercreate           integer,
  deal_number               varchar(32),
  deal_label                varchar(128),
  deal_datebegin            date,
  deal_parentdeal_id        integer,
  deal_type_id              integer,
  deal_region_id            integer DEFAULT 0 NOT NULL,
  deal_tasktype_id          integer,
  deal_company_id           integer DEFAULT 0 NOT NULL,
  deal_contact1_id          integer,
  deal_contact2_id          integer,
  deal_marketingmanager_id  integer,
  deal_technicalmanager_id  integer,
  deal_dateproposal         date,
  deal_dateexpected         date,
  deal_datealarm            date,
  deal_dateend              date,
  deal_amount               decimal(12,2),
  deal_commission           decimal(4,2) DEFAULT 0,
  deal_hitrate              integer DEFAULT 0,
  deal_status_id            integer,
  deal_archive              char(1) DEFAULT '0',
  deal_todo                 varchar(128),
  deal_privacy              integer DEFAULT 0,
  deal_comment              text,
  PRIMARY KEY (deal_id)
);


--
-- Table structure for table 'DealStatus'
--
CREATE TABLE DealStatus (
  dealstatus_id          serial,
  dealstatus_domain_id   integer default 0,
  dealstatus_timeupdate  timestamp,
  dealstatus_timecreate  timestamp,
  dealstatus_userupdate  integer,
  dealstatus_usercreate  integer,
  dealstatus_label       varchar(24),
  dealstatus_order       integer,
  dealstatus_hitrate     char(3),
  PRIMARY KEY (dealstatus_id)
);


--
-- Table structure for table 'DealType'
--
CREATE TABLE DealType (
  dealtype_id          serial,
  dealtype_domain_id   integer default 0,
  dealtype_timeupdate  timestamp,
  dealtype_timecreate  timestamp,
  dealtype_userupdate  integer,
  dealtype_usercreate  integer,
  dealtype_label       varchar(16),
  dealtype_inout       varchar(1) DEFAULT '-',
  PRIMARY KEY (dealtype_id)
);


--
-- Table structure for the table  'DealCompanyRole'
--
CREATE TABLE DealCompanyRole (
  dealcompanyrole_id          serial,
  dealcompanyrole_domain_id   integer default 0,
  dealcompanyrole_timeupdate  timestamp,
  dealcompanyrole_timecreate  timestamp,
  dealcompanyrole_userupdate  integer default 0,
  dealcompanyrole_usercreate  integer default 0,
  dealcompanyrole_code        varchar(10) default '',
  dealcompanyrole_label       varchar(64) NOT NULL default '',
  PRIMARY KEY (dealcompanyrole_id)
);


--
-- Table structure for the table  'DealCompany'
--
CREATE TABLE DealCompany (
  dealcompany_id          serial,
  dealcompany_timeupdate  timestamp,
  dealcompany_timecreate  timestamp,
  dealcompany_userupdate  integer default 0,
  dealcompany_usercreate  integer default 0,
  dealcompany_deal_id     integer NOT NULL default 0,
  dealcompany_company_id  integer NOT NULL default 0,
  dealcompany_role_id     integer NOT NULL default 0,
  PRIMARY KEY (dealcompany_id)
);
CREATE INDEX dealcompany_idx_deal ON DealCompany (dealcompany_deal_id);


-------------------------------------------------------------------------------
-- List module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'List'
--
CREATE TABLE List (
  list_id          	 serial,
  list_domain_id         integer default 0,
  list_timeupdate  	 timestamp,
  list_timecreate  	 timestamp,
  list_userupdate  	 integer,
  list_usercreate  	 integer,
  list_privacy     	 integer DEFAULT 0,
  list_name        	 varchar(64) NOT NULL,
  list_subject     	 varchar(128),
  list_email       	 varchar(128),
  list_mode       	 integer DEFAULT 0,
  list_mailing_ok  	 integer DEFAULT 0,
  list_contact_archive	 integer DEFAULT 0,
  list_info_publication  integer DEFAULT 0,
  list_static_nb   	 integer DEFAULT 0,
  list_query_nb    	 integer DEFAULT 0,
  list_query       	 text,
  list_structure   	 text,
  PRIMARY KEY (list_id),
  UNIQUE (list_name)
);


--
-- Table structure for table 'ContactList'
--
CREATE TABLE ContactList (
  contactlist_list_id     integer DEFAULT 0 NOT NULL,
  contactlist_contact_id  integer DEFAULT 0 NOT NULL
);


-------------------------------------------------------------------------------
-- Calendar module tables
-------------------------------------------------------------------------------
--
-- Table structure for the table  'CalendarEvent'
--
CREATE TABLE CalendarEvent (
  calendarevent_id           serial,
  calendarevent_domain_id    integer default 0,
  calendarevent_timeupdate   timestamp,
  calendarevent_timecreate   timestamp,
  calendarevent_userupdate   integer,
  calendarevent_usercreate   integer,
  calendarevent_owner        integer default NULL,    
  calendarevent_title        varchar(255) default NULL,
  calendarevent_location     varchar(100) default NULL,
  calendarevent_category1_id integer default 0,
  calendarevent_priority     integer,
  calendarevent_privacy      integer,
  calendarevent_date         timestamp NOT NULL,
  calendarevent_duration     integer NOT NULL default 0,
  calendarevent_allday       integer NOT NULL default 0,
  calendarevent_repeatkind   varchar(20) default NULL,
  calendarevent_repeatfrequence  integer default NULL,
  calendarevent_repeatdays   varchar(7) default NULL,
  calendarevent_endrepeat    timestamp NOT NULL,
  calendarevent_description  text,
  PRIMARY KEY (calendarevent_id)
);


--
-- Table structure for the table  'EventEntity'
--
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

--
-- Table structure for the table  'CalendarException'
--
CREATE TABLE CalendarException (
  calendarexception_timeupdate   timestamp,
  calendarexception_timecreate   timestamp,
  calendarexception_userupdate   integer default NULL,
  calendarexception_usercreate   integer default NULL,
  calendarexception_event_id     serial,
  calendarexception_date         timestamp NOT NULL,
  PRIMARY KEY (calendarexception_event_id,calendarexception_date)
);


--
-- Table structure for table 'CalendarCategory1'
--
CREATE TABLE CalendarCategory1 (
  calendarcategory1_id          serial,
  calendarcategory1_domain_id   integer default 0,
  calendarcategory1_timeupdate  timestamp,
  calendarcategory1_timecreate  timestamp,
  calendarcategory1_userupdate  integer DEFAULT NULL,
  calendarcategory1_usercreate  integer DEFAULT NULL,
  calendarcategory1_code        varchar(10) default '',
  calendarcategory1_label       varchar(128) DEFAULT NULL,
  PRIMARY KEY (calendarcategory1_id)
);


--
-- Table structure for table 'EntityRight'
--
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


-------------------------------------------------------------------------------
-- Todo
-------------------------------------------------------------------------------
--
-- New table 'Todo'
--
CREATE TABLE Todo (
  todo_id          serial,
  todo_domain_id   integer default 0,
  todo_timeupdate  timestamp,
  todo_timecreate  timestamp,
  todo_userupdate  integer,
  todo_usercreate  integer,
  todo_user        integer,
  todo_privacy     integer NOT NULL DEFAULT 0,
  todo_date        timestamp,
  todo_deadline    timestamp,
  todo_dateend     timestamp,
  todo_priority    integer DEFAULT NULL,
  todo_percent     integer,
  todo_title       varchar(80) DEFAULT NULL,
  todo_status      varchar(32),
  todo_webpage     varchar(255),
  todo_content     text DEFAULT NULL,
  PRIMARY KEY (todo_id)
);


-------------------------------------------------------------------------------
-- Publication module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'Publication'
--

CREATE TABLE Publication (
  publication_id             serial,
  publication_domain_id      integer default 0,
  publication_timeupdate     timestamp,
  publication_timecreate     timestamp,
  publication_userupdate     integer,
  publication_usercreate     integer,
  publication_title          varchar(64) NOT NULL,
  publication_type_id        integer,
  publication_year           integer,
  publication_lang           varchar(30),
  publication_desc           text,
  PRIMARY KEY (publication_id)
);

--
-- Table structure for table 'PublicationType'
--
CREATE TABLE PublicationType (
  publicationtype_id          serial,
  publicationtype_domain_id   integer default 0,
  publicationtype_timeupdate  timestamp,
  publicationtype_timecreate  timestamp,
  publicationtype_userupdate  integer,
  publicationtype_usercreate  integer,
  publicationtype_code        varchar(10) NOT NULL default '',
  publicationtype_label       varchar(64),
  PRIMARY KEY (publicationtype_id)
);


--
-- Table structure for table 'Subscription'
--
CREATE TABLE Subscription (
  subscription_id		serial,
  subscription_domain_id        integer default 0,
  subscription_publication_id 	integer NOT NULL,
  subscription_contact_id       integer NOT NULL,
  subscription_timeupdate       timestamp,
  subscription_timecreate       timestamp,
  subscription_userupdate       integer,
  subscription_usercreate       integer,
  subscription_quantity       	integer,
  subscription_renewal          integer NOT NULL,
  subscription_reception_id     integer NOT NULL,
  subscription_date_begin       timestamp,
  subscription_date_end         timestamp,
  PRIMARY KEY (subscription_id)
);


--
-- Table structure for table 'SubscriptionReception'
--
CREATE TABLE SubscriptionReception ( 
  subscriptionreception_id          serial,
  subscriptionreception_domain_id   integer default 0,
  subscriptionreception_timeupdate  timestamp,
  subscriptionreception_timecreate  timestamp,
  subscriptionreception_userupdate  integer,
  subscriptionreception_usercreate  integer,
  subscriptionreception_code        varchar(10) NOT NULL default '',
  subscriptionreception_label       char(12),
  PRIMARY KEY (subscriptionreception_id)
);


-------------------------------------------------------------------------------
-- Document module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'Document'
--
CREATE TABLE Document (
  document_id            serial,
  document_domain_id     integer default 0,
  document_timeupdate    timestamp,
  document_timecreate    timestamp,
  document_userupdate  	 integer DEFAULT NULL,
  document_usercreate  	 integer DEFAULT NULL,
  document_title       	 varchar(255) DEFAULT NULL,
  document_name        	 varchar(255) DEFAULT NULL,
  document_kind        	 integer DEFAULT NULL,
  document_mimetype_id	 integer NOT NULL DEFAULT 0,
  document_privacy     	 integer NOT NULL DEFAULT 0,
  document_size        	 integer DEFAULT NULL,
  document_author      	 varchar(255) DEFAULT NULL,
  document_path        	 text DEFAULT NULL,
  document_acl        	 text DEFAULT NULL,
  PRIMARY KEY (document_id)
);


--
-- Table structure for table 'DocumentMimeType'
--
CREATE TABLE DocumentMimeType (
  documentmimetype_id          serial,
  documentmimetype_domain_id   integer default 0,
  documentmimetype_timeupdate  timestamp,
  documentmimetype_timecreate  timestamp,
  documentmimetype_userupdate  integer DEFAULT NULL,
  documentmimetype_usercreate  integer DEFAULT NULL,
  documentmimetype_label       varchar(255) DEFAULT NULL,
  documentmimetype_extension   varchar(10) DEFAULT NULL,
  documentmimetype_mime        varchar(255) DEFAULT NULL,
  PRIMARY KEY (documentmimetype_id)
);


--
-- Table structure for table 'DocumentEntity'
--
CREATE TABLE DocumentEntity (
  documententity_document_id  integer NOT NULL,
  documententity_entity_id    integer NOT NULL,
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
  project_id             serial,
  project_domain_id      integer default 0,
  project_timeupdate     timestamp,
  project_timecreate     timestamp,
  project_userupdate     integer,
  project_usercreate     integer,
  project_name           varchar(128),
  project_shortname      varchar(10),
  project_tasktype_id    integer,
  project_company_id     integer,
  project_deal_id        integer,
  project_soldtime       integer DEFAULT NULL,
  project_estimatedtime  integer DEFAULT NULL,
  project_datebegin      date,
  project_dateend        date,
  project_archive        char(1) DEFAULT '0',
  project_comment        text,
  project_reference_date varchar(32),
  project_reference_duration varchar(16),
  project_reference_desc text,
  project_reference_tech text,
  PRIMARY KEY (project_id)
);
create INDEX project_idx_comp ON Project (project_company_id);
create INDEX project_idx_deal ON Project (project_deal_id);


--
-- Table structure for table 'ProjectTask'
--
CREATE TABLE ProjectTask (
  projecttask_id             serial,
  projecttask_project_id     integer NOT NULL,
  projecttask_timeupdate     timestamp,
  projecttask_timecreate     timestamp,
  projecttask_userupdate     integer default NULL,
  projecttask_usercreate     integer default NULL,
  projecttask_label          varchar(128) default NULL,
  projecttask_parenttask_id  integer default 0,
  projecttask_rank           integer default NULL,
  PRIMARY KEY (projecttask_id)
);
create INDEX pt_idx_pro ON ProjectTask (projecttask_project_id);


--
-- Table structure for table 'ProjectReferenceTask'
--
CREATE TABLE ProjectRefTask (
  projectreftask_id           serial,
  projectreftask_timeupdate   timestamp,
  projectreftask_timecreate   timestamp,
  projectreftask_userupdate   integer default NULL,
  projectreftask_usercreate   integer default NULL,
  projectreftask_tasktype_id  integer,
  projectreftask_code         varchar(10) default '',
  projectreftask_label        varchar(128) default NULL,
  PRIMARY KEY (projectreftask_id)
);


--
-- Table structure for table 'ProjectUser'
--
CREATE TABLE ProjectUser (
  projectuser_id              serial,
  projectuser_project_id      integer NOT NULL,
  projectuser_user_id         integer NOT NULL,
  projectuser_projecttask_id  integer,
  projectuser_timeupdate      timestamp,
  projectuser_timecreate      timestamp,
  projectuser_userupdate      integer default NULL,
  projectuser_usercreate      integer default NULL,
  projectuser_projectedtime   integer default NULL,
  projectuser_missingtime     integer default NULL,
  projectuser_validity        timestamp,
  projectuser_soldprice       integer default NULL,
  projectuser_manager         integer default NULL,
  PRIMARY KEY (projectuser_id)
);
create INDEX pu_idx_pro ON ProjectUser (projectuser_project_id);
create INDEX pu_idx_user ON ProjectUser (projectuser_user_id);
create INDEX pu_idx_pt ON ProjectUser (projectuser_projecttask_id);


--
-- Table structure for table 'ProjectStat'
--
CREATE TABLE ProjectStat (
  projectstat_project_id     integer NOT NULL,
  projectstat_usercreate     integer NOT NULL,
  projectstat_date           timestamp NOT NULL,
  projectstat_useddays       integer default NULL,
  projectstat_remainingdays  integer default NULL,
  PRIMARY KEY (projectstat_project_id, projectstat_usercreate, projectstat_date)
);


----------------------------------------------------------------------------
-- Create CV table
----------------------------------------------------------------------------
CREATE TABLE CV (
  cv_id              serial,
  cv_domain_id       integer default 0,
  cv_timeupdate      TIMESTAMP,
  cv_timecreate      TIMESTAMP,
  cv_userupdate      integer,
  cv_usercreate      integer,
  cv_userobm_id      integer,
  cv_title           varchar(255),
  cv_additionnalrefs text,
  cv_comment         text,
  PRIMARY KEY(cv_id)
);


----------------------------------------------------------------------------
-- Create ProjectCV table
----------------------------------------------------------------------------
CREATE TABLE ProjectCV (
  projectcv_project_id integer NOT NULL,
  projectcv_cv_id      integer NOT NULL,
  projectcv_role       varchar(128),
  PRIMARY KEY(projectcv_project_id, projectcv_cv_id)
);


----------------------------------------------------------------------------
-- Create DefaultOdtTemplate table
----------------------------------------------------------------------------
CREATE TABLE DefaultOdtTemplate (
  defaultodttemplate_id           serial,
  defaultodttemplate_domain_id    integer DEFAULT 0,
  defaultodttemplate_entity       varchar(32),
  defaultodttemplate_document_id  integer NOT NULL,
  defaultodttemplate_label        varchar(64) DEFAULT '',
  PRIMARY KEY(defaultodttemplate_id)
);


-------------------------------------------------------------------------------
-- Timemanagement tables
-------------------------------------------------------------------------------
--
-- Task table
--
CREATE TABLE TimeTask (
  timetask_id              serial,
  timetask_timeupdate      timestamp,
  timetask_timecreate      timestamp,
  timetask_userupdate      integer DEFAULT NULL,
  timetask_usercreate      integer DEFAULT NULL,
  timetask_user_id         integer DEFAULT NULL,
  timetask_date            timestamp NOT NULL,
  timetask_projecttask_id  integer DEFAULT NULL,
  timetask_length          integer DEFAULT NULL,
  timetask_tasktype_id     integer DEFAULT NULL,
  timetask_label           varchar(255) DEFAULT NULL,
  timetask_status          integer DEFAULT NULL,
  PRIMARY KEY (timetask_id)
);
create INDEX tt_idx_pt ON TimeTask (timetask_projecttask_id);


--
-- TaskType table
--
CREATE TABLE TaskType (
  tasktype_id          serial,
  tasktype_domain_id   integer default 0,
  tasktype_timeupdate  timestamp,
  tasktype_timecreate  timestamp,
  tasktype_userupdate  integer DEFAULT NULL,
  tasktype_usercreate  integer DEFAULT NULL,
  tasktype_internal    integer NOT NULL,
  tasktype_label       varchar(32) DEFAULT NULL,
  PRIMARY KEY (tasktype_id)
);


-------------------------------------------------------------------------------
-- Support tables
-------------------------------------------------------------------------------
--
-- New table 'Contract'
--
CREATE TABLE Contract (
  contract_id                serial,
  contract_domain_id         integer default 0,
  contract_timeupdate        timestamp,
  contract_timecreate        timestamp,
  contract_userupdate        integer DEFAULT NULL,
  contract_usercreate        integer DEFAULT NULL,
  contract_deal_id           integer DEFAULT NULL,
  contract_company_id        integer DEFAULT NULL,
  contract_label             varchar(128) DEFAULT NULL,
  contract_number            varchar(20) DEFAULT NULL,
  contract_datesignature     date DEFAULT NULL,
  contract_datebegin         date DEFAULT NULL,
  contract_dateexp           date DEFAULT NULL,
  contract_daterenew         date DEFAULT NULL,
  contract_datecancel        date DEFAULT NULL,
  contract_type_id           integer DEFAULT NULL,
  contract_priority_id       integer DEFAULT 0 NOT NULL,
  contract_status_id         integer DEFAULT 0 NOT NULL,
  contract_kind              integer DEFAULT 0 NULL,
  contract_format            integer DEFAULT 0 NULL,
  contract_ticketnumber      integer DEFAULT 0 NULL,
  contract_duration          integer DEFAULT 0 NULL,
  contract_autorenewal       integer DEFAULT 0 NULL,
  contract_contact1_id       integer DEFAULT NULL,
  contract_contact2_id       integer DEFAULT NULL,
  contract_techmanager_id    integer DEFAULT NULL,
  contract_marketmanager_id  integer DEFAULT NULL,
  contract_privacy           integer DEFAULT 0 NULL,
  contract_archive           integer DEFAULT 0,
  contract_clause            text,
  contract_comment           text,
  PRIMARY KEY (contract_id)
);


--
-- New table 'ContractType'
--
CREATE TABLE ContractType (
  contracttype_id          serial,
  contracttype_domain_id   integer default 0,
  contracttype_timeupdate  timestamp,
  contracttype_timecreate  timestamp,
  contracttype_userupdate  integer DEFAULT NULL,
  contracttype_usercreate  integer DEFAULT NULL,
  contracttype_code        varchar(10) default '',
  contracttype_label       varchar(64) DEFAULT NULL,
  PRIMARY KEY (contracttype_id)
);


--
-- New table 'ContractPriority'
--
CREATE TABLE ContractPriority (
  contractpriority_id          serial,
  contractpriority_domain_id   integer default 0,
  contractpriority_timeupdate  timestamp,
  contractpriority_timecreate  timestamp,
  contractpriority_userupdate  integer DEFAULT NULL,
  contractpriority_usercreate  integer DEFAULT NULL,
  contractpriority_color       varchar(6) DEFAULT NULL,
  contractpriority_code        varchar(10) default '',
  contractpriority_label       varchar(64) DEFAULT NULL,
  PRIMARY KEY (contractpriority_id)
);


--
-- New table 'ContractStatus'
--
CREATE TABLE ContractStatus (
  contractstatus_id          serial,
  contractstatus_domain_id   integer default 0,
  contractstatus_timeupdate  timestamp,
  contractstatus_timecreate  timestamp,
  contractstatus_userupdate  integer DEFAULT	NULL,
  contractstatus_usercreate  integer DEFAULT	NULL,
  contractstatus_code        varchar(10) default '',
  contractstatus_label       varchar(64) DEFAULT NULL,
PRIMARY KEY (contractstatus_id)
);


--
-- New table 'Incident'
--
CREATE TABLE Incident (
  incident_id                 serial,
  incident_domain_id          integer default 0,
  incident_timeupdate         timestamp,
  incident_timecreate         timestamp,
  incident_userupdate         integer DEFAULT NULL,
  incident_usercreate         integer DEFAULT NULL,
  incident_contract_id        integer NOT NULL,
  incident_label              varchar(100) DEFAULT NULL,
  incident_reference          varchar(32) default NULL,
  incident_date               timestamp,
  incident_priority_id        integer DEFAULT 0,
  incident_status_id          integer DEFAULT 0,
  incident_resolutiontype_id  integer DEFAULT 0,
  incident_logger             integer DEFAULT NULL,
  incident_owner              integer DEFAULT NULL,
  incident_duration           char(4) DEFAULT '0',
  incident_archive            char(1) NOT NULL DEFAULT '0',
  incident_comment            text,
  incident_resolution         text,
  PRIMARY KEY (incident_id)
);


--
-- New table 'IncidentPriority'
--
CREATE TABLE IncidentPriority (
  incidentpriority_id          serial,
  incidentpriority_domain_id   integer default 0,
  incidentpriority_timeupdate  timestamp,
  incidentpriority_timecreate  timestamp,
  incidentpriority_userupdate  integer DEFAULT NULL,
  incidentpriority_usercreate  integer DEFAULT NULL,
  incidentpriority_code        varchar(10) default '',
  incidentpriority_label       varchar(32) DEFAULT NULL,
  incidentpriority_color       char(6),
  PRIMARY KEY (incidentpriority_id)
);


--
-- New table 'IncidentStatus'
--
CREATE TABLE IncidentStatus (
  incidentstatus_id          serial,
  incidentstatus_domain_id   integer default 0,
  incidentstatus_timeupdate  timestamp,
  incidentstatus_timecreate  timestamp,
  incidentstatus_userupdate  integer DEFAULT NULL,
  incidentstatus_usercreate  integer DEFAULT NULL,
  incidentstatus_code        varchar(10) default '',
  incidentstatus_label       varchar(32) DEFAULT NULL,
  PRIMARY KEY (incidentstatus_id)
);


--
-- New table 'IncidentResolutionType'
--
CREATE TABLE IncidentResolutionType (
  incidentresolutiontype_id          serial,
  incidentresolutiontype_domain_id   integer default 0,
  incidentresolutiontype_timeupdate  timestamp,
  incidentresolutiontype_timecreate  timestamp,
  incidentresolutiontype_userupdate  integer DEFAULT NULL,
  incidentresolutiontype_usercreate  integer DEFAULT NULL,
  incidentresolutiontype_code        varchar(10) default '',
  incidentresolutiontype_label       varchar(32) DEFAULT NULL,
  PRIMARY KEY (incidentresolutiontype_id)
);


-------------------------------------------------------------------------------
-- Accounting Section tables
-------------------------------------------------------------------------------
--
-- New table 'Invoice'
--
CREATE TABLE Invoice ( 
  invoice_id               serial,
  invoice_domain_id        integer default 0,
  invoice_timeupdate       timestamp,
  invoice_timecreate       timestamp,
  invoice_userupdate       integer,
  invoice_usercreate       integer,
  invoice_company_id       integer NOT NULL,
  invoice_deal_id          integer default NULL,
  invoice_project_id       integer default NULL,
  invoice_number           varchar(10) DEFAULT '0',
  invoice_label            varchar(40) NOT NULL DEFAULT '',
  invoice_amount_ht        DECIMAL(10,2),
  invoice_amount_ttc       DECIMAL(10,2),
  invoice_status_id        integer DEFAULT 0 NOT NULL,
  invoice_date             date,
  invoice_expiration_date  date,
  invoice_payment_date     date,
  invoice_inout            char(1),
  invoice_archive          char(1) NOT NULL DEFAULT '0',
  invoice_comment          text,
  PRIMARY KEY (invoice_id)
);


--
-- New table 'InvoiceStatus'
--
CREATE TABLE InvoiceStatus (
  invoicestatus_id         serial,
  invoicestatus_domain_id  integer default 0,
  invoicestatus_payment    integer default 0 not null,
  invoicestatus_created    integer default 0 not null,
  invoicestatus_archive    integer default 0 not null,
  invoicestatus_label      varchar(24) default '' not null,
  PRIMARY KEY (invoicestatus_id)
);


--
-- New table 'Payment'
--
CREATE TABLE Payment (
  payment_id              serial,
  payment_domain_id       integer default 0,
  payment_timeupdate      timestamp,
  payment_timecreate      timestamp,
  payment_userupdate      integer,
  payment_usercreate      integer,
  payment_company_id      integer NOT NULL,
  payment_account_id      integer,
  payment_paymentkind_id  integer NOT NULL,
  payment_amount          decimal(10,2) DEFAULT '0.0' NOT NULL,
  payment_date            date,
  payment_inout           char(1) NOT NULL DEFAULT '+',
  payment_number          varchar(24) NOT NULL DEFAULT '',
  payment_checked         char(1) NOT NULL DEFAULT '0',
  payment_comment         text,
  PRIMARY KEY (payment_id)
);


--
-- New table 'PaymentKind'
--
CREATE TABLE PaymentKind (
  paymentkind_id          serial,
  paymentkind_domain_id   integer default 0,
  paymentkind_shortlabel  varchar(3) NOT NULL DEFAULT '',
  paymentkind_label       varchar(40) NOT NULL DEFAULT '',
  PRIMARY KEY (paymentkind_id)
);


--
-- New table 'PaymentInvoice'
--
CREATE TABLE PaymentInvoice (
  paymentinvoice_invoice_id  integer NOT NULL,
  paymentinvoice_payment_id  integer NOT NULL,
  paymentinvoice_timeupdate  timestamp,
  paymentinvoice_timecreate  timestamp,
  paymentinvoice_userupdate  integer,
  paymentinvoice_usercreate  integer,
  paymentinvoice_amount      decimal (10,2) NOT NULL DEFAULT '0',
  PRIMARY KEY (paymentinvoice_invoice_id,paymentinvoice_payment_id)
);


--
-- New table 'Account'
--
CREATE TABLE Account (
  account_id          serial,
  account_domain_id   integer default 0,
  account_timeupdate  timestamp,
  account_timecreate  timestamp,
  account_userupdate  integer,
  account_usercreate  integer,
  account_bank	      varchar(60) DEFAULT '' NOT NULL,
  account_number      varchar(11) DEFAULT '0' NOT NULL,
  account_balance     DECIMAL(15,2) DEFAULT '0.00' NOT NULL,
  account_today	      DECIMAL(15,2) DEFAULT '0.00' NOT NULL,
  account_comment     varchar(100),
  account_label	      varchar(40) NOT NULL DEFAULT '',
  PRIMARY KEY (account_id)
);


-------------------------------------------------------------------------------
-- Group module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'UGroup' (cause Group is a reserved keyword)
--
CREATE TABLE UGroup (
  group_id          serial,
  group_domain_id   integer default 0,
  group_timeupdate  timestamp,
  group_timecreate  timestamp,
  group_userupdate  integer,
  group_usercreate  integer,
  group_system      integer DEFAULT 0,
  group_privacy     integer DEFAULT 0,
  group_local       integer DEFAULT 1,
  group_ext_id      integer,
  group_samba       integer DEFAULT 0,
  group_gid         integer,
  group_mailing     integer DEFAULT 0,
  group_name        varchar(32) NOT NULL,
  group_desc        varchar(128),
  group_email       varchar(128),
  group_contacts    text,
  PRIMARY KEY (group_id),
  UNIQUE (group_gid)
);


--
-- Table structure for table 'UserObmGroup'
--
CREATE TABLE UserObmGroup (
  userobmgroup_group_id    integer DEFAULT 0 NOT NULL,
  userobmgroup_userobm_id  integer DEFAULT 0 NOT NULL,
  PRIMARY KEY (userobmgroup_group_id, userobmgroup_userobm_id)
);


--
-- Table structure for table 'GroupGroup'
--
CREATE TABLE GroupGroup (
  groupgroup_parent_id  integer DEFAULT 0 NOT NULL,
  groupgroup_child_id   integer DEFAULT 0 NOT NULL,
  PRIMARY KEY (groupgroup_parent_id, groupgroup_child_id)
);


--
-- Table structure for table 'of_usergroup'
--
CREATE TABLE of_usergroup (
  of_usergroup_group_id    integer DEFAULT 0 NOT NULL,
  of_usergroup_userobm_id  integer DEFAULT 0 NOT NULL,
  PRIMARY KEY (of_usergroup_group_id, of_usergroup_userobm_id)
);


-------------------------------------------------------------------------------
-- Import module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'Import'
--
CREATE TABLE Import (
  import_id                   serial,
  import_domain_id            integer default 0,
  import_timeupdate           timestamp,
  import_timecreate           timestamp,
  import_userupdate           integer,
  import_usercreate           integer,
  import_name                 varchar(64) NOT NULL,
  import_datasource_id        integer DEFAULT 0,
  import_marketingmanager_id  integer,
  import_separator            varchar(3),
  import_enclosed             char(1),
  import_desc                 text,
  PRIMARY KEY (import_id),
  UNIQUE (import_name)
);


-------------------------------------------------------------------------------
-- Tables needed for Connectors sync
-------------------------------------------------------------------------------
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
-- Resource module tables 
-------------------------------------------------------------------------------
--
-- Table structure for table 'Resource'
--
CREATE TABLE Resource (
  resource_id                serial,
  resource_domain_id         integer default 0,
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
CREATE UNIQUE INDEX k_name_resource_Resource_index ON Resource (resource_name);

--
-- Table structure for table 'RGroup'
--
CREATE TABLE RGroup (
  rgroup_id          serial,
  rgroup_domain_id   integer default 0,
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
-- Tables needed for Domain module
-------------------------------------------------------------------------------
--
-- Table structure for table 'Domain'
--
CREATE TABLE Domain (
  domain_id             serial,
  domain_timeupdate     timestamp,
  domain_timecreate     timestamp,
  domain_usercreate     integer,
  domain_userupdate     integer,
  domain_label          varchar(32) NOT NULL,
  domain_description    varchar(255),
  domain_name           varchar(128),
  domain_alias          text,
  domain_mail_server_id integer DEFAULT NULL,
  PRIMARY KEY (domain_id)
);


-------------------------------------------------------------------------------
-- OBM-Mail, OBM-LDAP tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'Host'
--
CREATE TABLE Host (
  host_id               serial,
  host_domain_id        integer default 0,
  host_timeupdate       timestamp,
  host_timecreate       timestamp,
  host_userupdate       integer,
  host_usercreate       integer,
  host_uid              integer,
  host_gid              integer,
  host_samba            integer DEFAULT 0,
  host_name             varchar(32) NOT NULL,
  host_ip               varchar(16),
  host_description      varchar(128),
  host_web_perms        integer default 0,
  host_web_list         text default '',
  host_web_all		integer default 0,
  host_ftp_perms        integer default 0,
  host_firewall_perms   varchar(128),
  PRIMARY KEY (host_id),
  UNIQUE (host_name),
  UNIQUE (host_uid)
);


--
-- Storage for stats
--
CREATE TABLE Stats (
  stats_name   varchar(32) NOT NULL default '',
  stats_value  varchar(255) NOT NULL default '',
  PRIMARY KEY (stats_name)
);


--
-- Samba parameters table
--
CREATE TABLE Samba (
  samba_domain_id  integer default 0,
  samba_name       varchar(255) NOT NULL default '',
  samba_value      varchar(255) NOT NULL default ''
);


--
-- Shared bals table
--
CREATE TABLE MailShare (
  mailshare_id             serial,
  mailshare_domain_id      integer default 0,
  mailshare_timeupdate     timestamp,
  mailshare_timecreate     timestamp,
  mailshare_userupdate     integer,
  mailshare_usercreate     integer,
  mailshare_name           varchar(32),
  mailshare_quota          varchar(8) default '0' NOT NULL,
  mailshare_mail_server_id integer default 0,
  mailshare_description    varchar(255),
  mailshare_email          text default NULL,
  PRIMARY KEY (mailshare_id)
);


CREATE TABLE UserSystem (
  usersystem_id         serial,
  usersystem_login      varchar(32) NOT NULL default '',
  usersystem_password   varchar(32) NOT NULL default '',
  usersystem_uid        varchar(6) default NULL,
  usersystem_gid        varchar(6) default NULL,
  usersystem_homedir    varchar(32) NOT NULL default '/tmp',
  usersystem_lastname   varchar(32) default NULL,
  usersystem_firstname  varchar(32) default NULL,
  usersystem_shell      varchar(32) default NULL,
  PRIMARY KEY (usersystem_id),
  UNIQUE (usersystem_login)
);


-----------------------------------------------------------------------------
-- Mail server declaration table
-----------------------------------------------------------------------------
CREATE TABLE MailServer (
  mailserver_id            serial,
  mailserver_host_id       integer NOT NULL default 0,
  mailserver_relayhost_id  integer default NULL,
  PRIMARY KEY (mailserver_id)
);


-----------------------------------------------------------------------------
-- Mail server network declaration table
-----------------------------------------------------------------------------
CREATE TABLE MailServerNetwork (
  mailservernetwork_host_id  integer NOT NULL default 0,
  mailservernetwork_ip       varchar(16) NOT NULL default ''
);


-------------------------------------------------------------------------------
-- OBM-Mail, OBM-LDAP Production tables (used by automate)
-------------------------------------------------------------------------------
CREATE TABLE P_Domain (like Domain);
CREATE TABLE P_UserObm (like UserObm);
CREATE TABLE P_UGroup (like UGroup);
CREATE TABLE P_UserObmGroup (like UserObmGroup);
CREATE TABLE P_GroupGroup (like GroupGroup);
CREATE TABLE P_of_usergroup (like UserObmGroup);
CREATE TABLE P_Host (like Host);
CREATE TABLE P_Samba (like Samba);
CREATE TABLE P_MailServer (like MailServer);
CREATE TABLE P_MailServerNetwork (like MailServerNetwork);
CREATE TABLE P_MailShare (like MailShare);
CREATE TABLE P_EntityRight (like EntityRight);

COMMIT;
