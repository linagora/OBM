--/////////////////////////////////////////////////////////////////////////////
--// OBM - File : create_obmdb_2.2.pgsql.sql                                 //
--//     - Desc : PostgreSQL Database 2.2 creation script                    //
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
  activeuserobm_sid            varchar(32) NOT NULL default '',
  activeuserobm_session_name   varchar(32) NOT NULL default '',
  activeuserobm_userobm_id     integer default NULL,
  activeuserobm_timeupdate     timestamp,
  activeuserobm_timecreate     timestamp,
  activeuserobm_nb_connexions  integer NOT NULL default 0,
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
  userobm_sessionlog_userobm_id     integer default NULL,
  userobm_sessionlog_timeupdate     timestamp,
  userobm_sessionlog_timecreate     timestamp,
  userobm_sessionlog_nb_connexions  integer NOT NULL default 0,
  userobm_sessionlog_lastpage       varchar(32) NOT NULL default '0',
  userobm_sessionlog_ip             varchar(32) NOT NULL default '0',
  PRIMARY KEY (userobm_sessionlog_sid)
);


--
-- Table structure for table 'UserObm'
--
CREATE TABLE UserObm (
  userobm_id                  serial,
  userobm_domain_id           integer NOT NULL,
  userobm_timeupdate          timestamp,
  userobm_timecreate          timestamp,
  userobm_userupdate          integer,
  userobm_usercreate          integer,
  userobm_local               integer default 1,
  userobm_ext_id              varchar(16),
  userobm_system              integer default 0,
  userobm_archive             smallint not null default 0,
  userobm_timelastaccess      timestamp,
  userobm_login               varchar(32) default '' NOT NULL,
  userobm_nb_login_failed     integer default 0,
  userobm_password_type       varchar(6) default 'PLAIN' NOT NULL,
  userobm_password            varchar(64) default '' NOT NULL,
  userobm_password_dateexp    date,
  userobm_account_dateexp     date,
  userobm_perms               varchar(254),
  userobm_delegation_target   varchar(64) default '',
  userobm_delegation          varchar(64) default '',
  userobm_calendar_version    timestamp,
  userobm_uid                 integer,
  userobm_gid                 integer,
  userobm_datebegin           date,
  userobm_hidden              integer default 0,
  userobm_kind                varchar(12),
  userobm_lastname            varchar(64) default '',
  userobm_firstname           varchar(64) default '',
  userobm_title               varchar(64) default '',
  userobm_sound               varchar(64),
  userobm_company             varchar(64),
  userobm_direction           varchar(64),
  userobm_service             varchar(64),
  userobm_address1            varchar(64),
  userobm_address2            varchar(64),
  userobm_address3            varchar(64),
  userobm_zipcode             varchar(14),
  userobm_town                varchar(64),
  userobm_expresspostal       varchar(16),
  userobm_country_iso3166     char(2) default '0',
  userobm_phone               varchar(32) default '',
  userobm_phone2              varchar(32) default '',
  userobm_mobile              varchar(32) default '',
  userobm_fax                 varchar(32) default '',
  userobm_fax2                varchar(32) default '',
  userobm_web_perms           integer default 0,
  userobm_web_list 	      text default NULL,  
  userobm_web_all	      integer default 0,
  userobm_mail_perms          integer default 0,
  userobm_mail_ext_perms      integer default 0,
  userobm_email               text default '',
  userobm_mail_server_id      integer default NULL,
  userobm_mail_quota          integer default 0,
  userobm_mail_quota_use      integer default 0,
  userobm_mail_login_date     timestamp,
  userobm_nomade_perms        integer default 0,
  userobm_nomade_enable       integer default 0,
  userobm_nomade_local_copy   integer default 0,
  userobm_nomade_datebegin    timestamp,
  userobm_nomade_dateend      timestamp,
  userobm_email_nomade        varchar(64) default '',
  userobm_vacation_enable     integer default 0,
  userobm_vacation_datebegin  timestamp,
  userobm_vacation_dateend    timestamp,
  userobm_vacation_message    text default '',
  userobm_samba_perms         integer default 0,
  userobm_samba_home          varchar(255) default '',
  userobm_samba_home_drive    char(2) default '',
  userobm_samba_logon_script  varchar(128) default '',
  userobm_host_id             integer default NULL,
  userobm_description         varchar(255),
  userobm_location            varchar(255),
  userobm_education           varchar(255),
  userobm_photo_id            integer,
  PRIMARY KEY (userobm_id)
);
CREATE INDEX k_login_user_UserObm_index ON UserObm (userobm_login);
CREATE UNIQUE INDEX k_uid_user_UserObm_index ON UserObm (userobm_uid);


--
-- Table structure for table 'UserObmPref'
--
CREATE TABLE UserObmPref (
  userobmpref_id       serial,
  userobmpref_user_id  integer default NULL,
  userobmpref_option   varchar(50) NOT NULL,
  userobmpref_value    varchar(50) NOT NULL,
  PRIMARY KEY(userobmpref_id),
  CONSTRAINT userobmpref_key UNIQUE (userobmpref_user_id, userobmpref_option)
);


--
-- New table 'DisplayPref'
--
CREATE TABLE DisplayPref (
  display_id          serial,
  display_user_id     integer default NULL,
  display_entity      varchar(32) NOT NULL default '',
  display_fieldname   varchar(64) NOT NULL default '',
  display_fieldorder  integer default NULL,
  display_display     integer NOT NULL default 1,
  PRIMARY KEY(display_id),
  CONSTRAINT displaypref_key UNIQUE (display_user_id, display_entity, display_fieldname)
);
create INDEX DisplayPref_user_id_index ON DisplayPref (display_user_id);
create INDEX DisplayPref_entity_index ON DisplayPref (display_entity);


--
-- Table structure for table 'Category'
--
CREATE TABLE Category (
  category_id          serial,
  category_domain_id   integer NOT NULL,
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


--
-- Table structure for table 'ObmBookmark'
--
CREATE TABLE ObmBookmark (
  obmbookmark_id          serial,
  obmbookmark_user_id     integer NOT NULL,
  obmbookmark_label       varchar(48) NOT NULL default '',
  obmbookmark_entity      varchar(24) NOT NULL default '',
  PRIMARY KEY (obmbookmark_id)
);
CREATE INDEX bkm_idx_user ON ObmBookmark (obmbookmark_user_id);


--
-- Table structure for table 'ObmBookmarkProperty'
--
CREATE TABLE ObmBookmarkProperty (
  obmbookmarkproperty_id           serial,
  obmbookmarkproperty_bookmark_id  integer NOT NULL,
  obmbookmarkproperty_property     varchar(64) NOT NULL default '',
  obmbookmarkproperty_value        varchar(64) NOT NULL default '',
  PRIMARY KEY (obmbookmarkproperty_id)
);
CREATE INDEX bkmprop_idx_bkm ON ObmBookmarkProperty (obmbookmarkproperty_bookmark_id);


-------------------------------------------------------------------------------
-- References Tables
-------------------------------------------------------------------------------
--
-- Table structure for the table  'DataSource'
--
CREATE TABLE DataSource (
  datasource_id          serial,
  datasource_domain_id   integer NOT NULL,
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
  country_domain_id   integer NOT NULL,
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
  region_domain_id   integer NOT NULL,
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
  companytype_domain_id   integer NOT NULL,
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
  companyactivity_domain_id   integer NOT NULL,
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
  companynafcode_domain_id   integer NOT NULL,
  companynafcode_timeupdate  timestamp,
  companynafcode_timecreate  timestamp,
  companynafcode_userupdate  integer,
  companynafcode_usercreate  integer,
  companynafcode_title       integer NOT NULL default 0,
  companynafcode_code        varchar(4),
  companynafcode_label       varchar(128),
  PRIMARY KEY (companynafcode_id)
);


--
-- Table structure for table 'Company'
--
CREATE TABLE Company (
  company_id                   serial,
  company_domain_id            integer NOT NULL,
  company_timeupdate           timestamp,
  company_timecreate           timestamp,
  company_userupdate           integer,
  company_usercreate           integer,
  company_datasource_id        integer default NULL,
  company_number               varchar(32),
  company_vat                  varchar(20),
  company_siret                varchar(14) default '',
  company_archive              smallint default 0 NOT NULL,
  company_name                 varchar(96) default '' NOT NULL,
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
  company_country_iso3166      char(2) default '0',
  company_phone                varchar(32),
  company_fax                  varchar(32),
  company_web                  varchar(64),
  company_email                varchar(64),
  company_contact_number       integer default 0 NOT NULL,
  company_deal_number          integer default 0 NOT NULL,
  company_deal_total           integer default 0 NOT NULL,
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
  contact_domain_id            integer NOT NULL,
  contact_timeupdate           timestamp,
  contact_timecreate           timestamp,
  contact_userupdate           integer,
  contact_usercreate           integer,
  contact_datasource_id        integer default NULL,
  contact_company_id           integer,
  contact_company              varchar(64),
  contact_kind_id              integer,
  contact_marketingmanager_id  integer,
  contact_lastname             varchar(64) default '' NOT NULL,
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
  contact_country_iso3166      char(2) default '0',
  contact_function_id          integer,
  contact_title                varchar(64),
  contact_phone                varchar(32),
  contact_homephone            varchar(32),
  contact_mobilephone          varchar(32),
  contact_fax                  varchar(32),
  contact_email                varchar(128),
  contact_email2               varchar(128),
  contact_mailing_ok           smallint default 0,
  contact_newsletter           smallint default 0,
  contact_archive              smallint default 0,
  contact_privacy              integer default 0,
  contact_date                 TIMESTAMP,
  contact_comment              text,
  contact_comment2             text,
  contact_comment3             text,
  contact_birthday_id          integer,
  contact_collected            boolean default FALSE, 
  contact_origin               varchar(255) not null, 
  PRIMARY KEY (contact_id)
);


--
-- Table structure for table 'Kind'
--
CREATE TABLE Kind (
  kind_id          serial,
  kind_domain_id   integer NOT NULL,
  kind_timeupdate  timestamp,
  kind_timecreate  timestamp,
  kind_userupdate  integer,
  kind_usercreate  integer,
  kind_minilabel   varchar(64),
  kind_header      varchar(64),
  kind_lang        char(2),
  kind_default     integer NOT NULL default 0,
  PRIMARY KEY (kind_id)
);


--
-- Table structure for the table 'ContactFunction'
--
CREATE TABLE ContactFunction (
  contactfunction_id          serial,
  contactfunction_domain_id   integer NOT NULL,
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
  leadsource_domain_id   integer NOT NULL,
  leadsource_timeupdate  timestamp,
  leadsource_timecreate  timestamp,
  leadsource_userupdate  integer default 0,
  leadsource_usercreate  integer default 0,
  leadsource_code        varchar(10) default '',
  leadsource_label       varchar(100) NOT NULL default '',
  PRIMARY KEY (leadsource_id)
);


--
-- Table structure for table 'LeadStatus'
--
CREATE TABLE LeadStatus (
  leadstatus_id          serial,
  leadstatus_domain_id   integer NOT NULL,
  leadstatus_timeupdate  timestamp,
  leadstatus_timecreate  timestamp,
  leadstatus_userupdate  integer,
  leadstatus_usercreate  integer,
  leadstatus_code        varchar(10),
  leadstatus_label       varchar(24),
  PRIMARY KEY (leadstatus_id)
);


--
-- Table structure for the table 'Lead'
--
CREATE TABLE Lead (
  lead_id          serial,
  lead_domain_id   integer NOT NULL,
  lead_timeupdate  timestamp,
  lead_timecreate  timestamp,
  lead_userupdate  integer default 0,
  lead_usercreate  integer default 0,
  lead_source_id   integer default NULL,
  lead_manager_id  integer default NULL,
  lead_company_id  integer NOT NULL,
  lead_contact_id  integer default NULL,
  lead_privacy     integer default 0,
  lead_name        varchar(64),
  lead_date        date,
  lead_datealarm   date,
  lead_status_id   integer,
  lead_archive     smallint default 0,
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
  parentdeal_domain_id            integer NOT NULL,
  parentdeal_timeupdate           TIMESTAMP,
  parentdeal_timecreate           TIMESTAMP,
  parentdeal_userupdate           integer,
  parentdeal_usercreate           integer,
  parentdeal_label                varchar(128) NOT NULL,
  parentdeal_marketingmanager_id  integer,
  parentdeal_technicalmanager_id  integer,
  parentdeal_archive              smallint default 0,
  parentdeal_comment              text,
  PRIMARY KEY (parentdeal_id)
);


--
-- Table structure for table 'Deal'
--
CREATE TABLE Deal (
  deal_id                   serial,
  deal_domain_id            integer NOT NULL,
  deal_timeupdate           timestamp,
  deal_timecreate           timestamp,
  deal_userupdate           integer,
  deal_usercreate           integer,
  deal_number               varchar(32),
  deal_label                varchar(128),
  deal_datebegin            date,
  deal_parentdeal_id        integer,
  deal_type_id              integer,
  deal_region_id            integer default NULL,
  deal_tasktype_id          integer,
  deal_company_id           integer NOT NULL,
  deal_contact1_id          integer,
  deal_contact2_id          integer,
  deal_marketingmanager_id  integer,
  deal_technicalmanager_id  integer,
  deal_source_id            integer default NULL,
  deal_source               varchar(64),
  deal_dateproposal         date,
  deal_dateexpected         date,
  deal_datealarm            date,
  deal_dateend              date,
  deal_amount               decimal(12,2),
  deal_margin               decimal(12,2),
  deal_commission           decimal(4,2) default 0,
  deal_hitrate              integer default 0,
  deal_status_id            integer,
  deal_archive              smallint default 0,
  deal_todo                 varchar(128),
  deal_privacy              integer default 0,
  deal_comment              text,
  PRIMARY KEY (deal_id)
);


--
-- Table structure for table 'DealStatus'
--
CREATE TABLE DealStatus (
  dealstatus_id          serial,
  dealstatus_domain_id   integer NOT NULL,
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
  dealtype_domain_id   integer NOT NULL,
  dealtype_timeupdate  timestamp,
  dealtype_timecreate  timestamp,
  dealtype_userupdate  integer,
  dealtype_usercreate  integer,
  dealtype_inout       varchar(1) default '-',
  dealtype_code        varchar(10),
  dealtype_label       varchar(16),
  PRIMARY KEY (dealtype_id)
);


--
-- Table structure for the table  'DealCompanyRole'
--
CREATE TABLE DealCompanyRole (
  dealcompanyrole_id          serial,
  dealcompanyrole_domain_id   integer NOT NULL,
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
  dealcompany_deal_id     integer NOT NULL,
  dealcompany_company_id  integer NOT NULL,
  dealcompany_role_id     integer default NULL,
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
  list_domain_id         integer NOT NULL,
  list_timeupdate  	 timestamp,
  list_timecreate  	 timestamp,
  list_userupdate  	 integer,
  list_usercreate  	 integer,
  list_privacy     	 integer default 0,
  list_name        	 varchar(64) NOT NULL,
  list_subject     	 varchar(128),
  list_email       	 varchar(128),
  list_mode       	 integer default 0,
  list_mailing_ok  	 integer default 0,
  list_contact_archive	 integer default 0,
  list_info_publication  integer default 0,
  list_static_nb   	 integer default 0,
  list_query_nb    	 integer default 0,
  list_query       	 text,
  list_structure   	 text,
  PRIMARY KEY (list_id),
  UNIQUE (list_name)
);


--
-- Table structure for table 'ContactList'
--
CREATE TABLE ContactList (
  contactlist_list_id     integer NOT NULL,
  contactlist_contact_id  integer NOT NULL
);


-------------------------------------------------------------------------------
-- Calendar module tables
-------------------------------------------------------------------------------
--
-- Table structure for the table 'Event'
--
CREATE TYPE vcomponent AS ENUM ('VEVENT', 'VTODO', 'VJOURNAL', 'VFREEBUSY');
CREATE TYPE vopacity AS ENUM ('OPAQUE', 'TRANSPARENT');
CREATE TABLE Event (
  event_id           	serial,
  event_domain_id    	integer NOT NULL,
  event_timeupdate   	timestamp,
  event_timecreate   	timestamp,
  event_userupdate   	integer,
  event_usercreate   	integer,
  event_ext_id       	varchar(255) default '', 
  event_type            vcomponent default 'VEVENT',
  event_origin          varchar(255) NOT NULL default '',
  event_owner           integer default NULL,    
  event_timezone        varchar(255) default 'GMT',    
  event_opacity         vopacity default 'OPAQUE',
  event_title           varchar(255) default NULL,
  event_location        varchar(100) default NULL,
  event_category1_id    integer default NULL,
  event_priority        integer,
  event_privacy         integer,
  event_date            timestamp NULL,
  event_duration        integer NOT NULL default 0,
  event_allday          BOOLEAN default FALSE,
  event_repeatkind      varchar(20) default NULL,
  event_repeatfrequence integer default NULL,
  event_repeatdays      varchar(7) default NULL,
  event_endrepeat       timestamp default NULL,
  event_color           varchar(7),
  event_completed       timestamp,
  event_url             text,
  event_description     text,
  event_properties      text,
  PRIMARY KEY (event_id)
);


--
-- Table structure for the table  'EventEntity'
--
CREATE TYPE vpartstat AS ENUM ('NEEDS-ACTION', 'ACCEPTED', 'DECLINED', 'TENTATIVE', 'DELEGATED', 'COMPLETED', 'IN-PROGRESS');
CREATE TYPE vrole AS ENUM ('CHAIR', 'REQ', 'OPT', 'NON');
CREATE TABLE EventEntity (
  evententity_timeupdate timestamp,
  evententity_timecreate timestamp,
  evententity_userupdate integer default NULL,
  evententity_usercreate integer default NULL,
  evententity_event_id   integer NOT NULL,
  evententity_entity_id  integer NOT NULL,
  evententity_entity     varchar(32) NOT NULL default '',
  evententity_state      vpartstat default 'NEEDS-ACTION',
  evententity_required   vrole default 'REQ',
  evententity_percent    float default 0,
  PRIMARY KEY (evententity_event_id,evententity_entity_id,evententity_entity)
);

--
-- Table structure for the table  'CalendarException'
--
CREATE TABLE EventException (
  eventexception_timeupdate   timestamp,
  eventexception_timecreate   timestamp,
  eventexception_userupdate   integer default NULL,
  eventexception_usercreate   integer default NULL,
  eventexception_event_id     integer,
  eventexception_date         timestamp NOT NULL,
  PRIMARY KEY (eventexception_event_id,eventexception_date)
);


--
-- Table structure for table 'EventAlert'
--
CREATE TABLE EventAlert (
  eventalert_timeupdate  timestamp,
  eventalert_timecreate  timestamp,
  eventalert_userupdate  integer default NULL,
  eventalert_usercreate  integer default NULL,
  eventalert_event_id    integer,
  eventalert_user_id     integer,
  eventalert_duration    integer NOT NULL default 0
);
CREATE INDEX idx_eventalert_user ON EventAlert (eventalert_user_id);


--
-- Table structure for table 'CalendarCategory1'
--
CREATE TABLE CalendarCategory1 (
  calendarcategory1_id          serial,
  calendarcategory1_domain_id   integer NOT NULL,
  calendarcategory1_timeupdate  timestamp,
  calendarcategory1_timecreate  timestamp,
  calendarcategory1_userupdate  integer default NULL,
  calendarcategory1_usercreate  integer default NULL,
  calendarcategory1_code        varchar(10) default '',
  calendarcategory1_label       varchar(128) default NULL,
  calendarcategory1_color       char(6),
  PRIMARY KEY (calendarcategory1_id)
);


--
-- Table structure for table 'EntityRight'
--
CREATE TABLE EntityRight (
  entityright_entity       varchar(32) NOT NULL default '',
  entityright_entity_id    integer NOT NULL,
  entityright_consumer     varchar(32) NOT NULL default '',
  entityright_consumer_id  integer NOT NULL,
  entityright_access         integer NOT NULL default 0,
  entityright_read         integer NOT NULL default 0,
  entityright_write        integer NOT NULL default 0,
  entityright_admin        integer NOT NULL default 0,
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
  todo_domain_id   integer NOT NULL,
  todo_timeupdate  timestamp,
  todo_timecreate  timestamp,
  todo_userupdate  integer,
  todo_usercreate  integer,
  todo_user        integer,
  todo_privacy     integer NOT NULL default 0,
  todo_date        timestamp,
  todo_deadline    timestamp,
  todo_dateend     timestamp,
  todo_priority    integer default NULL,
  todo_percent     integer,
  todo_title       varchar(80) default NULL,
  todo_status      varchar(32),
  todo_webpage     varchar(255),
  todo_content     text default NULL,
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
  publication_domain_id      integer NOT NULL,
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
  publicationtype_domain_id   integer NOT NULL,
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
  subscription_domain_id        integer NOT NULL,
  subscription_publication_id 	integer NOT NULL,
  subscription_contact_id       integer NOT NULL,
  subscription_timeupdate       timestamp,
  subscription_timecreate       timestamp,
  subscription_userupdate       integer,
  subscription_usercreate       integer,
  subscription_quantity       	integer,
  subscription_renewal          integer NOT NULL,
  subscription_reception_id     integer default NULL,
  subscription_date_begin       timestamp,
  subscription_date_end         timestamp,
  PRIMARY KEY (subscription_id)
);


--
-- Table structure for table 'SubscriptionReception'
--
CREATE TABLE SubscriptionReception ( 
  subscriptionreception_id          serial,
  subscriptionreception_domain_id   integer NOT NULL,
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
  document_domain_id     integer NOT NULL,
  document_timeupdate    timestamp,
  document_timecreate    timestamp,
  document_userupdate  	 integer default NULL,
  document_usercreate  	 integer default NULL,
  document_title       	 varchar(255) default NULL,
  document_name        	 varchar(255) default NULL,
  document_kind        	 integer default NULL,
  document_mimetype_id	 integer default NULL,
  document_privacy     	 integer NOT NULL default 0,
  document_size        	 integer default NULL,
  document_author      	 varchar(255) default NULL,
  document_path        	 text default NULL,
  document_acl        	 text default NULL,
  PRIMARY KEY (document_id)
);


--
-- Table structure for table 'DocumentMimeType'
--
CREATE TABLE DocumentMimeType (
  documentmimetype_id          serial,
  documentmimetype_domain_id   integer NOT NULL,
  documentmimetype_timeupdate  timestamp,
  documentmimetype_timecreate  timestamp,
  documentmimetype_userupdate  integer default NULL,
  documentmimetype_usercreate  integer default NULL,
  documentmimetype_label       varchar(255) default NULL,
  documentmimetype_extension   varchar(10) default NULL,
  documentmimetype_mime        varchar(255) default NULL,
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
  project_domain_id      integer NOT NULL,
  project_timeupdate     timestamp,
  project_timecreate     timestamp,
  project_userupdate     integer,
  project_usercreate     integer,
  project_name           varchar(128),
  project_shortname      varchar(10),
  project_type_id        integer,
  project_tasktype_id    integer,
  project_company_id     integer,
  project_deal_id        integer,
  project_soldtime       integer default NULL,
  project_estimatedtime  integer default NULL,
  project_datebegin      date,
  project_dateend        date,
  project_archive        smallint default 0,
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
  projecttask_parenttask_id  integer default NULL,
  projecttask_rank           integer default NULL,
  projecttask_datebegin      date,
  projecttask_dateend        date,
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
  projectuser_projectedtime   float default NULL,
  projectuser_missingtime     float default NULL,
  projectuser_validity        timestamp,
  projectuser_soldprice       integer default NULL,
  projectuser_manager         integer default NULL,
  PRIMARY KEY (projectuser_id)
);
create INDEX pu_idx_pro ON ProjectUser (projectuser_project_id);
create INDEX pu_idx_user ON ProjectUser (projectuser_user_id);
create INDEX pu_idx_pt ON ProjectUser (projectuser_projecttask_id);


--
-- Table structure for table 'ProjectClosing'
--
CREATE TABLE ProjectClosing (
  projectclosing_id           serial,
  projectclosing_project_id   integer NOT NULL,
  projectclosing_timeupdate   timestamp,
  projectclosing_timecreate   timestamp,
  projectclosing_userupdate   integer,
  projectclosing_usercreate   integer default NULL,
  projectclosing_date         timestamp NOT NULL,
  projectclosing_used         integer NOT NULL,
  projectclosing_remaining    integer NOT NULL,
  projectclosing_type         integer,
  projectclosing_comment      text,
  PRIMARY KEY (projectclosing_id)
);


----------------------------------------------------------------------------
-- Create CV table
----------------------------------------------------------------------------
CREATE TABLE CV (
  cv_id              serial,
  cv_domain_id       integer NOT NULL,
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
  defaultodttemplate_domain_id    integer NOT NULL,
  defaultodttemplate_entity       varchar(32),
  defaultodttemplate_document_id  integer NOT NULL,
  defaultodttemplate_label        varchar(64) default '',
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
  timetask_userupdate      integer default NULL,
  timetask_usercreate      integer default NULL,
  timetask_user_id         integer default NULL,
  timetask_date            timestamp NOT NULL,
  timetask_projecttask_id  integer default NULL,
  timetask_length          float default NULL,
  timetask_tasktype_id     integer default NULL,
  timetask_label           varchar(255) default NULL,
  timetask_status          integer default NULL,
  PRIMARY KEY (timetask_id)
);
create INDEX tt_idx_pt ON TimeTask (timetask_projecttask_id);


--
-- TaskType table
--
CREATE TABLE TaskType (
  tasktype_id          serial,
  tasktype_domain_id   integer NOT NULL,
  tasktype_timeupdate  timestamp,
  tasktype_timecreate  timestamp,
  tasktype_userupdate  integer default NULL,
  tasktype_usercreate  integer default NULL,
  tasktype_internal    integer NOT NULL,
  tasktype_code        varchar(10),
  tasktype_label       varchar(32) default NULL,
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
  contract_domain_id         integer NOT NULL,
  contract_timeupdate        timestamp,
  contract_timecreate        timestamp,
  contract_userupdate        integer default NULL,
  contract_usercreate        integer default NULL,
  contract_deal_id           integer default NULL,
  contract_company_id        integer default NULL,
  contract_label             varchar(128) default NULL,
  contract_number            varchar(20) default NULL,
  contract_datesignature     date default NULL,
  contract_datebegin         date default NULL,
  contract_dateexp           date default NULL,
  contract_daterenew         date default NULL,
  contract_datecancel        date default NULL,
  contract_type_id           integer default NULL,
  contract_priority_id       integer default NULL,
  contract_status_id         integer default NULL,
  contract_kind              integer default 0 NULL,
  contract_format            integer default 0 NULL,
  contract_ticketnumber      integer default 0 NULL,
  contract_duration          integer default 0 NULL,
  contract_autorenewal       integer default 0 NULL,
  contract_contact1_id       integer default NULL,
  contract_contact2_id       integer default NULL,
  contract_techmanager_id    integer default NULL,
  contract_marketmanager_id  integer default NULL,
  contract_privacy           integer default 0 NULL,
  contract_archive           integer default 0,
  contract_clause            text,
  contract_comment           text,
  PRIMARY KEY (contract_id)
);


--
-- New table 'ContractType'
--
CREATE TABLE ContractType (
  contracttype_id          serial,
  contracttype_domain_id   integer NOT NULL,
  contracttype_timeupdate  timestamp,
  contracttype_timecreate  timestamp,
  contracttype_userupdate  integer default NULL,
  contracttype_usercreate  integer default NULL,
  contracttype_code        varchar(10) default '',
  contracttype_label       varchar(64) default NULL,
  PRIMARY KEY (contracttype_id)
);


--
-- New table 'ContractPriority'
--
CREATE TABLE ContractPriority (
  contractpriority_id          serial,
  contractpriority_domain_id   integer NOT NULL,
  contractpriority_timeupdate  timestamp,
  contractpriority_timecreate  timestamp,
  contractpriority_userupdate  integer default NULL,
  contractpriority_usercreate  integer default NULL,
  contractpriority_color       varchar(6) default NULL,
  contractpriority_code        varchar(10) default '',
  contractpriority_label       varchar(64) default NULL,
  PRIMARY KEY (contractpriority_id)
);


--
-- New table 'ContractStatus'
--
CREATE TABLE ContractStatus (
  contractstatus_id          serial,
  contractstatus_domain_id   integer NOT NULL,
  contractstatus_timeupdate  timestamp,
  contractstatus_timecreate  timestamp,
  contractstatus_userupdate  integer default	NULL,
  contractstatus_usercreate  integer default	NULL,
  contractstatus_code        varchar(10) default '',
  contractstatus_label       varchar(64) default NULL,
PRIMARY KEY (contractstatus_id)
);


--
-- New table 'Incident'
--
CREATE TABLE Incident (
  incident_id                 serial,
  incident_domain_id          integer NOT NULL,
  incident_timeupdate         timestamp,
  incident_timecreate         timestamp,
  incident_userupdate         integer default NULL,
  incident_usercreate         integer default NULL,
  incident_contract_id        integer NOT NULL,
  incident_label              varchar(100) default NULL,
  incident_reference          varchar(32) default NULL,
  incident_date               timestamp,
  incident_priority_id        integer default NULL,
  incident_status_id          integer default NULL,
  incident_resolutiontype_id  integer default NULL,
  incident_logger             integer default NULL,
  incident_owner              integer default NULL,
  incident_duration           char(4) default '0',
  incident_archive            smallint NOT NULL default 0,
  incident_comment            text,
  incident_resolution         text,
  PRIMARY KEY (incident_id)
);


--
-- New table 'IncidentPriority'
--
CREATE TABLE IncidentPriority (
  incidentpriority_id          serial,
  incidentpriority_domain_id   integer NOT NULL,
  incidentpriority_timeupdate  timestamp,
  incidentpriority_timecreate  timestamp,
  incidentpriority_userupdate  integer default NULL,
  incidentpriority_usercreate  integer default NULL,
  incidentpriority_code        varchar(10) default '',
  incidentpriority_label       varchar(32) default NULL,
  incidentpriority_color       char(6),
  PRIMARY KEY (incidentpriority_id)
);


--
-- New table 'IncidentStatus'
--
CREATE TABLE IncidentStatus (
  incidentstatus_id          serial,
  incidentstatus_domain_id   integer NOT NULL,
  incidentstatus_timeupdate  timestamp,
  incidentstatus_timecreate  timestamp,
  incidentstatus_userupdate  integer default NULL,
  incidentstatus_usercreate  integer default NULL,
  incidentstatus_code        varchar(10) default '',
  incidentstatus_label       varchar(32) default NULL,
  PRIMARY KEY (incidentstatus_id)
);


--
-- New table 'IncidentResolutionType'
--
CREATE TABLE IncidentResolutionType (
  incidentresolutiontype_id          serial,
  incidentresolutiontype_domain_id   integer NOT NULL,
  incidentresolutiontype_timeupdate  timestamp,
  incidentresolutiontype_timecreate  timestamp,
  incidentresolutiontype_userupdate  integer default NULL,
  incidentresolutiontype_usercreate  integer default NULL,
  incidentresolutiontype_code        varchar(10) default '',
  incidentresolutiontype_label       varchar(32) default NULL,
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
  invoice_domain_id        integer NOT NULL,
  invoice_timeupdate       timestamp,
  invoice_timecreate       timestamp,
  invoice_userupdate       integer,
  invoice_usercreate       integer,
  invoice_company_id       integer NOT NULL,
  invoice_deal_id          integer default NULL,
  invoice_project_id       integer default NULL,
  invoice_number           varchar(10) default '0',
  invoice_label            varchar(40) NOT NULL default '',
  invoice_amount_ht        DECIMAL(10,2),
  invoice_amount_ttc       DECIMAL(10,2),
  invoice_status_id        integer NOT NULL,
  invoice_date             date,
  invoice_expiration_date  date,
  invoice_payment_date     date,
  invoice_inout            char(1),
  invoice_credit_memo      integer NOT NULL default 0,
  invoice_archive          smallint NOT NULL default 0,
  invoice_comment          text,
  PRIMARY KEY (invoice_id)
);


--
-- New table 'Payment'
--
CREATE TABLE Payment (
  payment_id              serial,
  payment_domain_id       integer NOT NULL,
  payment_timeupdate      timestamp,
  payment_timecreate      timestamp,
  payment_userupdate      integer,
  payment_usercreate      integer,
  payment_company_id      integer default NULL,
  payment_account_id      integer,
  payment_paymentkind_id  integer default NULL,
  payment_amount          decimal(10,2) default '0.0' NOT NULL,
  payment_date            date,
  payment_inout           char(1) NOT NULL default '+',
  payment_number          varchar(24) NOT NULL default '',
  payment_checked         smallint NOT NULL default 0,
  payment_gap             decimal(10,2) default '0.0' NOT NULL,
  payment_comment         text,
  PRIMARY KEY (payment_id)
);


--
-- New table 'PaymentKind'
--
CREATE TABLE PaymentKind (
  paymentkind_id          serial,
  paymentkind_domain_id   integer NOT NULL,
  paymentkind_shortlabel  varchar(3) NOT NULL default '',
  paymentkind_label       varchar(40) NOT NULL default '',
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
  paymentinvoice_amount      decimal (10,2) NOT NULL default '0',
  PRIMARY KEY (paymentinvoice_invoice_id,paymentinvoice_payment_id)
);


--
-- New table 'Account'
--
CREATE TABLE Account (
  account_id          serial,
  account_domain_id   integer NOT NULL,
  account_timeupdate  timestamp,
  account_timecreate  timestamp,
  account_userupdate  integer,
  account_usercreate  integer,
  account_bank	      varchar(60) default '' NOT NULL,
  account_number      varchar(11) default '0' NOT NULL,
  account_balance     DECIMAL(15,2) default '0.00' NOT NULL,
  account_today	      DECIMAL(15,2) default '0.00' NOT NULL,
  account_comment     varchar(100),
  account_label	      varchar(40) NOT NULL default '',
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
  group_domain_id   integer NOT NULL,
  group_timeupdate  timestamp,
  group_timecreate  timestamp,
  group_userupdate  integer,
  group_usercreate  integer,
  group_system      integer default 0,
  group_privacy     integer default 0,
  group_local       integer default 1,
  group_ext_id      integer,
  group_samba       integer default 0,
  group_gid         integer,
  group_mailing     integer default 0,
  group_delegation  varchar(64) default '',
  group_manager_id  integer default NULL,
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
  userobmgroup_group_id    integer NOT NULL,
  userobmgroup_userobm_id  integer NOT NULL,
  PRIMARY KEY (userobmgroup_group_id, userobmgroup_userobm_id)
);


--
-- Table structure for table 'GroupGroup'
--
CREATE TABLE GroupGroup (
  groupgroup_parent_id  integer NOT NULL,
  groupgroup_child_id   integer NOT NULL,
  PRIMARY KEY (groupgroup_parent_id, groupgroup_child_id)
);


--
-- Table structure for table 'of_usergroup'
--
CREATE TABLE of_usergroup (
  of_usergroup_group_id    integer NOT NULL,
  of_usergroup_user_id  integer NOT NULL,
  PRIMARY KEY (of_usergroup_group_id, of_usergroup_user_id)
);


--
-- Table structure for the table 'OrganizationalChart'
--
CREATE TABLE OrganizationalChart (
  organizationalchart_id			      serial,
  organizationalchart_domain_id     integer NOT NULL,
  organizationalchart_timeupdate    timestamp,
  organizationalchart_timecreate		timestamp,
  organizationalchart_userupdate    integer,
  organizationalchart_usercreate    integer,
  organizationalchart_name          varchar(32) not null,
  organizationalchart_description   varchar(64),
  organizationalchart_archive       integer not null default 0,
  PRIMARY KEY (organizationalchart_id)
);


--
-- Table structure for the table 'OGroup'
--
CREATE TABLE OGroup (
  ogroup_id					               serial,
  ogroup_domain_id                 integer NOT NULL,
  ogroup_timeupdate	             	 timestamp,
  ogroup_timecreate	             	 timestamp,
  ogroup_userupdate                integer,
  ogroup_usercreate                integer,
  ogroup_organizationalchart_id    integer not null,
  ogroup_parent_id                 integer,
  ogroup_name                      varchar(32) not null,
  ogroup_level                     varchar(16),
  PRIMARY KEY (ogroup_id)
);


--
-- Table structure for the table 'OGroupEntity'
--
CREATE TABLE OGroupEntity (
  ogroupentity_id                  serial,
  ogroupentity_domain_id           integer NOT NULL,
  ogroupentity_timeupdate          timestamp,
  ogroupentity_timecreate          timestamp,
  ogroupentity_userupdate          integer,
  ogroupentity_usercreate          integer,
  ogroupentity_ogroup_id           integer not null,
  ogroupentity_entity_id           integer not null,
  ogroupentity_entity              varchar(32) not null,
  PRIMARY KEY (ogroupentity_id)
);


-------------------------------------------------------------------------------
-- Import module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'Import'
--
CREATE TABLE Import (
  import_id                   serial,
  import_domain_id            integer NOT NULL,
  import_timeupdate           timestamp,
  import_timecreate           timestamp,
  import_userupdate           integer,
  import_usercreate           integer,
  import_name                 varchar(64) NOT NULL,
  import_datasource_id        integer default NULL,
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
-- Table structure for the table 'DeletedEvent'
--
CREATE TABLE DeletedEvent (
  deletedevent_id         serial,
  deletedevent_event_id   integer,
  deletedevent_user_id    integer,
  deletedevent_timestamp  timestamp
);
create INDEX idx_dce_event_id ON DeletedEvent (deletedevent_event_id);
create INDEX idx_dce_user_id ON DeletedEvent (deletedevent_user_id);


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
  resource_domain_id         integer NOT NULL,
  resource_rtype_id          integer,
  resource_timeupdate        timestamp,
  resource_timecreate        timestamp,
  resource_userupdate        integer,
  resource_usercreate        integer,
  resource_name              varchar(32) default '' NOT NULL,
  resource_description       varchar(255),
  resource_qty               integer default 0,
  PRIMARY KEY (resource_id),
  UNIQUE (resource_name)
);
CREATE UNIQUE INDEX k_name_resource_Resource_index ON Resource (resource_name);

--
-- Table structure for table 'RGroup'
--
CREATE TABLE RGroup (
  rgroup_id          serial,
  rgroup_domain_id   integer NOT NULL,
  rgroup_timeupdate  timestamp,
  rgroup_timecreate  timestamp,
  rgroup_userupdate  integer,
  rgroup_usercreate  integer,
  rgroup_privacy     integer NULL default 0,
  rgroup_name        varchar(32) NOT NULL,
  rgroup_desc        varchar(128),
  PRIMARY KEY (rgroup_id)
);

--
-- Table structure for table 'ResourceGroup'
--
CREATE TABLE ResourceGroup (
  resourcegroup_rgroup_id    integer NOT NULL,
  resourcegroup_resource_id  integer NOT NULL
);

--
-- Table structure for the table 'ResourceType'
--
CREATE TABLE ResourceType (
  resourcetype_id		serial,
  resourcetype_domain_id	integer NOT NULL,
  resourcetype_label		varchar(32) NOT NULL,
  resourcetype_property		varchar(32),
  resourcetype_pkind		integer default 0 NOT NULL,
  PRIMARY KEY (resourcetype_id)
);

--
-- Table structure for the table 'ResourceItem'
--
CREATE TABLE ResourceItem (
  resourceitem_id		serial,
  resourceitem_domain_id	integer NOT NULL,
  resourceitem_label		varchar(32) NOT NULL,
  resourceitem_resourcetype_id	integer NOT NULL,
  resourceitem_description	text,
  PRIMARY KEY (resourceitem_id)
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
  domain_mail_server_auto integer default NULL,
  domain_global         boolean default false,
  PRIMARY KEY (domain_id)
);


--
-- Table structure for table 'DomainProperty'
--
CREATE TABLE DomainProperty (
  domainproperty_key       varchar(255) NOT NULL,
  domainproperty_type      varchar(32),
  domainproperty_default   varchar(64),
  domainproperty_readonly          integer default 0,
  PRIMARY KEY (domainproperty_key)
);

--
-- Table structure for table 'DomainPropertyValue'
--
CREATE TABLE DomainPropertyValue (
  domainpropertyvalue_domain_id    integer NOT NULL,
  domainpropertyvalue_property_key varchar(255) NOT NULL,
  domainpropertyvalue_value        varchar(255) NOT NULL,
  PRIMARY KEY (domainpropertyvalue_domain_id, domainpropertyvalue_property_key)
);


-------------------------------------------------------------------------------
-- OBM-Mail, OBM-LDAP tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'Host'
--
CREATE TABLE Host (
  host_id               serial,
  host_domain_id        integer NOT NULL,
  host_timeupdate       timestamp,
  host_timecreate       timestamp,
  host_userupdate       integer,
  host_usercreate       integer,
  host_uid              integer,
  host_gid              integer,
  host_samba            integer default 0,
  host_name             varchar(32) NOT NULL,
  host_ip               varchar(16),
  host_delegation       varchar(64) default '',
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
  samba_domain_id  integer NOT NULL,
  samba_name       varchar(255) NOT NULL default '',
  samba_value      varchar(255) NOT NULL default ''
);


--
-- Shared bals table
--
CREATE TABLE MailShare (
  mailshare_id             serial,
  mailshare_domain_id      integer NOT NULL,
  mailshare_timeupdate     timestamp,
  mailshare_timecreate     timestamp,
  mailshare_userupdate     integer,
  mailshare_usercreate     integer,
  mailshare_name           varchar(32),
  mailshare_archive        integer not null default 0,
  mailshare_quota          varchar(8) default '0' NOT NULL,
  mailshare_mail_server_id integer default NULL,
  mailshare_delegation     varchar(64) default '',
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
  mailserver_timeupdate    timestamp,
  mailserver_timecreate    timestamp,
  mailserver_userupdate    integer,
  mailserver_usercreate    integer,
  mailserver_host_id       integer NOT NULL,
  mailserver_relayhost_id  integer default NULL,
  mailserver_imap          integer default 0,
  mailserver_smtp_in       integer default 0,
  mailserver_smtp_out      integer default 0,
  PRIMARY KEY (mailserver_id)
);


--
-- Domain - Mail server link table
--
CREATE TABLE DomainMailServer (
  domainmailserver_domain_id      integer NOT NULL,
  domainmailserver_mailserver_id  integer NOT NULL,
  domainmailserver_role           varchar(16) NOT NULL default 'imap'
);


-----------------------------------------------------------------------------
-- Mail server network declaration table
-----------------------------------------------------------------------------
CREATE TABLE MailServerNetwork (
  mailservernetwork_host_id  integer NOT NULL,
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
CREATE TABLE P_of_usergroup (like of_usergroup);
CREATE TABLE P_Host (like Host);
CREATE TABLE P_Samba (like Samba);
CREATE TABLE P_MailServer (like MailServer);
CREATE TABLE P_MailServerNetwork (like MailServerNetwork);
CREATE TABLE P_MailShare (like MailShare);
CREATE TABLE P_EntityRight (like EntityRight);


-------------------------------------------------------------------------------
-- Tables needed for Automate work
-------------------------------------------------------------------------------
--
-- Table structure for the table 'Deleted'
--
CREATE TABLE Deleted (
  deleted_id         serial,
  deleted_domain_id  integer,
  deleted_user_id    integer,
  deleted_delegation varchar(64) default '',
  deleted_table      varchar(32),
  deleted_entity_id  integer,
  deleted_timestamp  timestamp,
  PRIMARY KEY (deleted_id)
);


--
-- Table structure for the table 'Updated'
--
CREATE TABLE Updated (
  updated_id         serial,
  updated_domain_id  integer,
  updated_user_id    integer,
  updated_delegation varchar(64) default '',
  updated_table      varchar(32),
  updated_entity_id  integer,
  updated_type       char(1),
  PRIMARY KEY (updated_id)
);


--
-- Table structure for the table 'Updatedlinks'
--
CREATE TABLE Updatedlinks (
  updatedlinks_id         serial,
  updatedlinks_domain_id  integer,
  updatedlinks_user_id    integer,
  updatedlinks_delegation varchar(64),
  updatedlinks_table      varchar(32),
  updatedlinks_entity     varchar(32),
  updatedlinks_entity_id  integer,
  PRIMARY KEY (updatedlinks_id)
);


--
-- Table structure for table 'Profile'
--

CREATE TABLE Profile (
	profile_id			serial,
	profile_domain_id	integer NOT NULL,
	profile_timeupdate	timestamp,
	profile_timecreate	timestamp,
	profile_userupdate	integer default null,
	profile_usercreate  integer default null,
	profile_name		varchar(64) default null,
	PRIMARY KEY (profile_id)
);

--
-- Table structure for table 'ProfileModule'
--

CREATE TABLE ProfileModule (
	profilemodule_id			serial,
	profilemodule_domain_id		integer NOT NULL,
	profilemodule_profile_id	integer default NULL,
	profilemodule_module_name	varchar(64) NOT NULL default '',
	profilemodule_right			integer default NULL,
	PRIMARY KEY (profilemodule_id)
);

--
-- Table structure for table `ProfileSection`
--

CREATE TABLE ProfileSection (
	profilesection_id			serial,
	profilesection_domain_id	integer NOT NULL,
	profilesection_profile_id	integer default NULL,
	profilesection_section_name	varchar(64) NOT NULL default '',
	profilesection_show			smallint default NULL,
	PRIMARY KEY (profilesection_id)
);

--
-- Table structure for table `ProfileProperty`
--

CREATE TABLE ProfileProperty (
	profileproperty_id 			serial,
	profileproperty_type		varchar(32) default NULL,
	profileproperty_default		text default NULL,
	profileproperty_readonly	smallint default 0,
	profileproperty_name		varchar(32) NOT NULL default '',
	PRIMARY KEY (profileproperty_id)
);

--
-- Table structure for table `ProfilePropertyValue`
--

CREATE TABLE ProfilePropertyValue (
	profilepropertyvalue_id				serial,
	profilepropertyvalue_profile_id		integer default NULL,
	profilepropertyvalue_property_id	integer default NULL,
	profilepropertyvalue_property_value	text NOT NULL default '',
	PRIMARY KEY (profilepropertyvalue_id)
);

--
-- Table structure for table `Service`
--

CREATE TABLE Service (
  service_id                                    serial,
  service_key                                   varchar(255),
  UNIQUE (service_key),
  PRIMARY KEY (service_id)
);

--
-- Table structure for table `ServiceProperty`
--
CREATE TABLE ServiceProperty (
  serviceproperty_id                            serial,
  serviceproperty_service_id                    integer NOT NULL,
  serviceproperty_key                           varchar(255),
  serviceproperty_default                       text,
  serviceproperty_type                          varchar(255),
  serviceproperty_min                           smallint,
  serviceproperty_max                           smallint,
  UNIQUE (serviceproperty_key),
  PRIMARY KEY (serviceproperty_id),
  CONSTRAINT serviceproperty_service_id_service_id_fkey FOREIGN KEY (serviceproperty_service_id) REFERENCES Service (service_id) ON DELETE CASCADE ON UPDATE CASCADE
);

--
-- Table structure for table `ServicePropertyDomain`
--
CREATE TABLE ServicePropertyDomain (
  servicepropertydomain_id                      integer NOT NULL,
  PRIMARY KEY (servicepropertydomain_id),
  CONSTRAINT servicepropertydomain_id_serviceproperty_id_fkey FOREIGN KEY (servicepropertydomain_id) REFERENCES ServiceProperty (serviceproperty_id) ON DELETE CASCADE ON UPDATE CASCADE
);

--
-- Table structure for table `ServicePropertyHost`
--
CREATE TABLE ServicePropertyHost (
  servicepropertyhost_id                        integer NOT NULL,
  PRIMARY KEY (servicepropertyhost_id),
  CONSTRAINT servicepropertyhost_id_serviceproperty_id_fkey FOREIGN KEY (servicepropertyhost_id) REFERENCES ServiceProperty (serviceproperty_id) ON DELETE CASCADE ON UPDATE CASCADE
);

--
-- Table structure for table `DomainServicePropertyValue`
--
CREATE TABLE DomainServiceValue (
  domainservicevalue_serviceproperty_id integer NOT NULL,
  domainservicevalue_domain_id          integer NOT NULL,
  domainservicevalue_value              text,
  PRIMARY KEY (domainservicevalue_serviceproperty_id,domainservicevalue_domain_id),
  CONSTRAINT domainservicevalue_serviceproperty_id_serviceproperty_id_fkey FOREIGN KEY (domainservicevalue_serviceproperty_id) REFERENCES ServicePropertyDomain (servicepropertydomain_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT domainservicevalue_domain_id_domain_id_fkey FOREIGN KEY (domainservicevalue_domain_id) REFERENCES Domain (domain_id) ON DELETE CASCADE ON UPDATE CASCADE
);

--
-- Table structure for table `HostServicePropertyValue`
--
CREATE TABLE HostServiceValue (
  hostservicevalue_serviceproperty_id   integer,
  hostservicevalue_host_id              integer,
  hostservicevalue_value                text,
  PRIMARY KEY (hostservicevalue_serviceproperty_id,hostservicevalue_host_id),
  CONSTRAINT hostservicevalue_serviceproperty_id_serviceproperty_id_fkey FOREIGN KEY (hostservicevalue_serviceproperty_id) REFERENCES ServicePropertyHost (servicepropertyhost_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT hostservicevalue_host_id_host_id_fkey FOREIGN KEY (hostservicevalue_host_id) REFERENCES Host (host_id) ON DELETE CASCADE ON UPDATE CASCADE
);

--
-- Table structure for table `ServiceDomain`
--
CREATE TABLE ServiceDomain (
  servicedomain_id                              integer,
  PRIMARY KEY (servicedomain_id),
  CONSTRAINT servicedomain_id_service_id_fkey FOREIGN KEY (servicedomain_id) REFERENCES Service (service_id) ON DELETE CASCADE ON UPDATE CASCADE
);

--
-- Table structure for table `ServiceHost`
--
CREATE TABLE ServiceHost (
  servicehost_id                                integer,
  PRIMARY KEY (servicehost_id),
  CONSTRAINT servicehost_id_service_id_fkey FOREIGN KEY (servicehost_id) REFERENCES Service (service_id) ON DELETE CASCADE ON UPDATE CASCADE
);

--
-- Table structure for table `DomainService`
--
CREATE TABLE DomainService (
  domainservice_service_id                      integer,
  domainservice_domain_id                       integer,
  PRIMARY KEY (domainservice_service_id,domainservice_domain_id),
  CONSTRAINT domainservice_service_id_domainservice_domain_id_fkey FOREIGN KEY (domainservice_service_id) REFERENCES ServiceDomain (servicedomain_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT domainservice_domain_id_domain_id_fkey FOREIGN KEY (domainservice_domain_id) REFERENCES Domain (domain_id) ON DELETE CASCADE ON UPDATE CASCADE
);

--
-- Table structure for table `DomainService`
--
CREATE TABLE HostService (
  hostservice_service_id                        integer,
  hostservice_host_id                           integer,
  PRIMARY KEY (hostservice_service_id,hostservice_host_id),
  CONSTRAINT hostservice_service_id_hostservice_host_id_fkey FOREIGN KEY (hostservice_service_id) REFERENCES ServiceHost (servicehost_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT hostservice_host_id_host_id_fkey FOREIGN KEY (hostservice_host_id) REFERENCES Host (host_id) ON DELETE CASCADE ON UPDATE CASCADE
);



---
--- Address
---
CREATE TABLE Address (
  address_id                                    serial,
  address_street1                               varchar(255),
  address_street2                               varchar(255),
  address_street3                               varchar(2555),
  address_zipcode                               varchar(14),
  address_town                                  varchar(128),
  address_expresspostal                         varchar(16),
  address_country                               char(2),
  address_im                                    varchar(255),
  address_label                                 varchar(255),
  PRIMARY KEY (address_id)
);

---
--- Phone
---
CREATE TABLE Phone (
  phone_id                                      serial,
  phone_label                                   varchar(255) NOT NULL,
  phone_number                                  varchar(32),
  PRIMARY KEY (phone_id)
);

---
--- Website
---
CREATE TABLE Website (
  website_id                                    serial,
  website_label                                 varchar(255) NOT NULL,
  website_number                                varchar(32),
  PRIMARY KEY (website_id)
);

---
--- Email
---
CREATE TABLE Email (
  email_id                                      serial,
  email_label                                   varchar(255) NOT NULL,
  email_address                                 varchar(255),
  PRIMARY KEY (email_id)
);

---
--- IM
---
CREATE TABLE IM (
  im_id                                         serial,
  im_label                                      varchar(255) NOT NULL,
  im_address                                    varchar(255),
  PRIMARY KEY (im_id)
);

---
--- ContactAddress
---
CREATE TABLE ContactAddress (
  contactaddress_address_id                     integer NOT NULL,
  contactaddress_contact_id                     integer NOT NULL,
  PRIMARY KEY(contactaddress_contact_id,contactaddress_address_id),
  CONSTRAINT contactaddress_address_id_address_id_fkey FOREIGN KEY (contactaddress_address_id) REFERENCES Address (address_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT contactaddress_contact_id_contact_id_fkey FOREIGN KEY (contactaddress_contact_id) REFERENCES Contact (contact_id) ON DELETE CASCADE ON UPDATE CASCADE
);

---
--- ContactPhone
---
CREATE TABLE ContactPhone (
  contactphone_phone_id                         integer NOT NULL,
  contactphone_contact_id                       integer NOT NULL,
  PRIMARY KEY(contactphone_contact_id,contactphone_phone_id),
  CONSTRAINT contactphone_phone_id_phone_id_fkey FOREIGN KEY (contactphone_phone_id) REFERENCES Phone (phone_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT contactphone_contact_id_contact_id_fkey FOREIGN KEY (contactphone_contact_id) REFERENCES Contact (contact_id) ON DELETE CASCADE ON UPDATE CASCADE
);

---
--- ContactWebsite
---
CREATE TABLE ContactWebsite (
  contactwebsite_website_id                     integer NOT NULL,
  contactwebsite_contact_id                     integer NOT NULL,
  PRIMARY KEY(contactwebsite_contact_id,contactwebsite_website_id),
  CONSTRAINT contactwebsite_website_id_website_id_fkey FOREIGN KEY (contactwebsite_website_id) REFERENCES Website (website_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT contactwebsite_contact_id_contact_id_fkey FOREIGN KEY (contactwebsite_contact_id) REFERENCES Contact (contact_id) ON DELETE CASCADE ON UPDATE CASCADE
);

---
--- ContactEmail
---
CREATE TABLE ContactEmail (
  contactemail_email_id                         integer NOT NULL,
  contactemail_contact_id                       integer NOT NULL,
  PRIMARY KEY(contactemail_contact_id,contactemail_email_id),
  CONSTRAINT contactemail_email_id_email_id_fkey FOREIGN KEY (contactemail_email_id) REFERENCES Email (email_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT contactemail_contact_id_contact_id_fkey FOREIGN KEY (contactemail_contact_id) REFERENCES Contact (contact_id) ON DELETE CASCADE ON UPDATE CASCADE
);

---
--- ContactIM
---
CREATE TABLE ContactIM (
  contactim_im_id                               integer NOT NULL,
  contactim_contact_id                          integer NOT NULL,
  PRIMARY KEY(contactim_contact_id,contactim_im_id),
  CONSTRAINT contactim_im_id_im_id_fkey FOREIGN KEY (contactim_im_id) REFERENCES IM (im_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT contactim_contact_id_contact_id_fkey FOREIGN KEY (contactim_contact_id) REFERENCES Contact (contact_id) ON DELETE CASCADE ON UPDATE CASCADE
);

---
--- CompanyAddress
---
CREATE TABLE CompanyAddress (
  companyaddress_address_id                     integer NOT NULL,
  companyaddress_company_id                     integer NOT NULL,
  PRIMARY KEY(companyaddress_company_id,companyaddress_address_id),
  CONSTRAINT companyaddress_address_id_address_id_fkey FOREIGN KEY (companyaddress_address_id) REFERENCES Address (address_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT companyaddress_company_id_company_id_fkey FOREIGN KEY (companyaddress_company_id) REFERENCES Company (company_id) ON DELETE CASCADE ON UPDATE CASCADE
);

---
--- CompanyPhone
---
CREATE TABLE CompanyPhone (
  companyphone_phone_id                         integer NOT NULL,
  companyphone_company_id                       integer NOT NULL,
  PRIMARY KEY(companyphone_company_id,companyphone_phone_id),
  CONSTRAINT companyphone_phone_id_phone_id_fkey FOREIGN KEY (companyphone_phone_id) REFERENCES Phone (phone_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT companyphone_company_id_company_id_fkey FOREIGN KEY (companyphone_company_id) REFERENCES Company (company_id) ON DELETE CASCADE ON UPDATE CASCADE
);

---
--- CompanyWebsite
---
CREATE TABLE CompanyWebsite (
  companywebsite_website_id                     integer NOT NULL,
  companywebsite_company_id                     integer NOT NULL,
  PRIMARY KEY(companywebsite_company_id,companywebsite_website_id),
  CONSTRAINT companywebsite_website_id_website_id_fkey FOREIGN KEY (companywebsite_website_id) REFERENCES Website (website_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT companywebsite_company_id_company_id_fkey FOREIGN KEY (companywebsite_company_id) REFERENCES Company (company_id) ON DELETE CASCADE ON UPDATE CASCADE
);

---
--- CompanyEmail
---
CREATE TABLE CompanyEmail (
  companyemail_email_id                         integer NOT NULL,
  companyemail_company_id                       integer NOT NULL,
  PRIMARY KEY(companyemail_company_id,companyemail_email_id),
  CONSTRAINT companyemail_email_id_email_id_fkey FOREIGN KEY (companyemail_email_id) REFERENCES Email (email_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT companyemail_company_id_company_id_fkey FOREIGN KEY (companyemail_company_id) REFERENCES Company (company_id) ON DELETE CASCADE ON UPDATE CASCADE
);
--
-- Foreign Keys
--

-- Foreign key from account_domain_id to domain_id
ALTER TABLE Account ADD CONSTRAINT account_domain_id_domain_id_fkey FOREIGN KEY (account_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from account_usercreate to userobm_id
ALTER TABLE Account ADD CONSTRAINT account_usercreate_userobm_id_fkey FOREIGN KEY (account_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from account_userupdate to userobm_id
ALTER TABLE Account ADD CONSTRAINT account_userupdate_userobm_id_fkey FOREIGN KEY (account_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from activeuserobm_userobm_id to userobm_id
ALTER TABLE ActiveUserObm ADD CONSTRAINT activeuserobm_userobm_id_userobm_id_fkey FOREIGN KEY (activeuserobm_userobm_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from cv_domain_id to domain_id
ALTER TABLE CV ADD CONSTRAINT cv_domain_id_domain_id_fkey FOREIGN KEY (cv_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from cv_userobm_id to userobm_id
ALTER TABLE CV ADD CONSTRAINT cv_userobm_id_userobm_id_fkey FOREIGN KEY (cv_userobm_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from cv_userupdate to userobm_id
ALTER TABLE CV ADD CONSTRAINT cv_userupdate_userobm_id_fkey FOREIGN KEY (cv_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from cv_usercreate to userobm_id
ALTER TABLE CV ADD CONSTRAINT cv_usercreate_userobm_id_fkey FOREIGN KEY (cv_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from eventalert_event_id to event_id
ALTER TABLE EventAlert ADD CONSTRAINT eventalert_event_id_event_id_fkey FOREIGN KEY (eventalert_event_id) REFERENCES Event(event_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from eventalert_user_id to userobm_id
ALTER TABLE EventAlert ADD CONSTRAINT eventalert_user_id_userobm_id_fkey FOREIGN KEY (eventalert_user_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from eventalert_userupdate to userobm_id
ALTER TABLE EventAlert ADD CONSTRAINT eventalert_userupdate_userobm_id_fkey FOREIGN KEY (eventalert_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from eventalert_usercreate to userobm_id
ALTER TABLE EventAlert ADD CONSTRAINT eventalert_usercreate_userobm_id_fkey FOREIGN KEY (eventalert_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from calendarcategory1_domain_id to domain_id
ALTER TABLE CalendarCategory1 ADD CONSTRAINT calendarcategory1_domain_id_domain_id_fkey FOREIGN KEY (calendarcategory1_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from calendarcategory1_userupdate to userobm_id
ALTER TABLE CalendarCategory1 ADD CONSTRAINT calendarcategory1_userupdate_userobm_id_fkey FOREIGN KEY (calendarcategory1_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from calendarcategory1_usercreate to userobm_id
ALTER TABLE CalendarCategory1 ADD CONSTRAINT calendarcategory1_usercreate_userobm_id_fkey FOREIGN KEY (calendarcategory1_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from event_domain_id to domain_id
ALTER TABLE Event ADD CONSTRAINT event_domain_id_domain_id_fkey FOREIGN KEY (event_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from event_owner to userobm_id
ALTER TABLE Event ADD CONSTRAINT event_owner_userobm_id_fkey FOREIGN KEY (event_owner) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from event_userupdate to userobm_id
ALTER TABLE Event ADD CONSTRAINT event_userupdate_userobm_id_fkey FOREIGN KEY (event_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from event_usercreate to userobm_id
ALTER TABLE Event ADD CONSTRAINT event_usercreate_userobm_id_fkey FOREIGN KEY (event_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from event_category1_id to calendarcategory1_id
ALTER TABLE Event ADD CONSTRAINT event_category1_id_calendarcategory1_id_fkey FOREIGN KEY (event_category1_id) REFERENCES CalendarCategory1(calendarcategory1_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from eventexception_event_id to event_id
ALTER TABLE EventException ADD CONSTRAINT eventexception_event_id_event_id_fkey FOREIGN KEY (eventexception_event_id) REFERENCES Event(event_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from eventexception_userupdate to userobm_id
ALTER TABLE EventException ADD CONSTRAINT eventexception_userupdate_userobm_id_fkey FOREIGN KEY (eventexception_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from eventexception_usercreate to userobm_id
ALTER TABLE EventException ADD CONSTRAINT eventexception_usercreate_userobm_id_fkey FOREIGN KEY (eventexception_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from category_domain_id to domain_id
ALTER TABLE Category ADD CONSTRAINT category_domain_id_domain_id_fkey FOREIGN KEY (category_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from category_userupdate to userobm_id
ALTER TABLE Category ADD CONSTRAINT category_userupdate_userobm_id_fkey FOREIGN KEY (category_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from category_usercreate to userobm_id
ALTER TABLE Category ADD CONSTRAINT category_usercreate_userobm_id_fkey FOREIGN KEY (category_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from categorylink_category_id to category_id
ALTER TABLE CategoryLink ADD CONSTRAINT categorylink_category_id_category_id_fkey FOREIGN KEY (categorylink_category_id) REFERENCES Category(category_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from company_domain_id to domain_id
ALTER TABLE Company ADD CONSTRAINT company_domain_id_domain_id_fkey FOREIGN KEY (company_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from company_userupdate to userobm_id
ALTER TABLE Company ADD CONSTRAINT company_userupdate_userobm_id_fkey FOREIGN KEY (company_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from company_usercreate to userobm_id
ALTER TABLE Company ADD CONSTRAINT company_usercreate_userobm_id_fkey FOREIGN KEY (company_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from company_datasource_id to datasource_id
ALTER TABLE Company ADD CONSTRAINT company_datasource_id_datasource_id_fkey FOREIGN KEY (company_datasource_id) REFERENCES DataSource(datasource_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from company_type_id to companytype_id
ALTER TABLE Company ADD CONSTRAINT company_type_id_companytype_id_fkey FOREIGN KEY (company_type_id) REFERENCES CompanyType(companytype_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from company_activity_id to companyactivity_id
ALTER TABLE Company ADD CONSTRAINT company_activity_id_companyactivity_id_fkey FOREIGN KEY (company_activity_id) REFERENCES CompanyActivity(companyactivity_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from company_nafcode_id to companynafcode_id
ALTER TABLE Company ADD CONSTRAINT company_nafcode_id_companynafcode_id_fkey FOREIGN KEY (company_nafcode_id) REFERENCES CompanyNafCode(companynafcode_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from company_marketingmanager_id to userobm_id
ALTER TABLE Company ADD CONSTRAINT company_marketingmanager_id_userobm_id_fkey FOREIGN KEY (company_marketingmanager_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from companyactivity_domain_id to domain_id
ALTER TABLE CompanyActivity ADD CONSTRAINT companyactivity_domain_id_domain_id_fkey FOREIGN KEY (companyactivity_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from companyactivity_userupdate to userobm_id
ALTER TABLE CompanyActivity ADD CONSTRAINT companyactivity_userupdate_userobm_id_fkey FOREIGN KEY (companyactivity_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from companyactivity_usercreate to userobm_id
ALTER TABLE CompanyActivity ADD CONSTRAINT companyactivity_usercreate_userobm_id_fkey FOREIGN KEY (companyactivity_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from companynafcode_domain_id to domain_id
ALTER TABLE CompanyNafCode ADD CONSTRAINT companynafcode_domain_id_domain_id_fkey FOREIGN KEY (companynafcode_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from companynafcode_userupdate to userobm_id
ALTER TABLE CompanyNafCode ADD CONSTRAINT companynafcode_userupdate_userobm_id_fkey FOREIGN KEY (companynafcode_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from companynafcode_usercreate to userobm_id
ALTER TABLE CompanyNafCode ADD CONSTRAINT companynafcode_usercreate_userobm_id_fkey FOREIGN KEY (companynafcode_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from companytype_domain_id to domain_id
ALTER TABLE CompanyType ADD CONSTRAINT companytype_domain_id_domain_id_fkey FOREIGN KEY (companytype_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from companytype_userupdate to userobm_id
ALTER TABLE CompanyType ADD CONSTRAINT companytype_userupdate_userobm_id_fkey FOREIGN KEY (companytype_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from companytype_usercreate to userobm_id
ALTER TABLE CompanyType ADD CONSTRAINT companytype_usercreate_userobm_id_fkey FOREIGN KEY (companytype_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contact_domain_id to domain_id
ALTER TABLE Contact ADD CONSTRAINT contact_domain_id_domain_id_fkey FOREIGN KEY (contact_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contact_company_id to company_id
ALTER TABLE Contact ADD CONSTRAINT contact_company_id_company_id_fkey FOREIGN KEY (contact_company_id) REFERENCES Company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contact_userupdate to userobm_id
ALTER TABLE Contact ADD CONSTRAINT contact_userupdate_userobm_id_fkey FOREIGN KEY (contact_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contact_usercreate to userobm_id
ALTER TABLE Contact ADD CONSTRAINT contact_usercreate_userobm_id_fkey FOREIGN KEY (contact_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contact_datasource_id to datasource_id
ALTER TABLE Contact ADD CONSTRAINT contact_datasource_id_datasource_id_fkey FOREIGN KEY (contact_datasource_id) REFERENCES DataSource(datasource_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contact_kind_id to kind_id
ALTER TABLE Contact ADD CONSTRAINT contact_kind_id_kind_id_fkey FOREIGN KEY (contact_kind_id) REFERENCES Kind(kind_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contact_marketingmanager_id to userobm_id
ALTER TABLE Contact ADD CONSTRAINT contact_marketingmanager_id_userobm_id_fkey FOREIGN KEY (contact_marketingmanager_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contact_function_id to contactfunction_id
ALTER TABLE Contact ADD CONSTRAINT contact_function_id_contactfunction_id_fkey FOREIGN KEY (contact_function_id) REFERENCES ContactFunction(contactfunction_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contact_birthday_id to event_id
ALTER TABLE Contact ADD CONSTRAINT contact_birthday_id_event_id_fkey FOREIGN KEY (contact_birthday_id) REFERENCES Event(event_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contactfunction_domain_id to domain_id
ALTER TABLE ContactFunction ADD CONSTRAINT contactfunction_domain_id_domain_id_fkey FOREIGN KEY (contactfunction_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contactfunction_userupdate to userobm_id
ALTER TABLE ContactFunction ADD CONSTRAINT contactfunction_userupdate_userobm_id_fkey FOREIGN KEY (contactfunction_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contactfunction_usercreate to userobm_id
ALTER TABLE ContactFunction ADD CONSTRAINT contactfunction_usercreate_userobm_id_fkey FOREIGN KEY (contactfunction_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contactlist_list_id to list_id
ALTER TABLE ContactList ADD CONSTRAINT contactlist_list_id_list_id_fkey FOREIGN KEY (contactlist_list_id) REFERENCES List(list_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contactlist_contact_id to contact_id
ALTER TABLE ContactList ADD CONSTRAINT contactlist_contact_id_contact_id_fkey FOREIGN KEY (contactlist_contact_id) REFERENCES Contact(contact_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contract_domain_id to domain_id
ALTER TABLE Contract ADD CONSTRAINT contract_domain_id_domain_id_fkey FOREIGN KEY (contract_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contract_deal_id to deal_id
ALTER TABLE Contract ADD CONSTRAINT contract_deal_id_deal_id_fkey FOREIGN KEY (contract_deal_id) REFERENCES Deal(deal_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contract_company_id to company_id
ALTER TABLE Contract ADD CONSTRAINT contract_company_id_company_id_fkey FOREIGN KEY (contract_company_id) REFERENCES Company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contract_userupdate to userobm_id
ALTER TABLE Contract ADD CONSTRAINT contract_userupdate_userobm_id_fkey FOREIGN KEY (contract_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contract_usercreate to userobm_id
ALTER TABLE Contract ADD CONSTRAINT contract_usercreate_userobm_id_fkey FOREIGN KEY (contract_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contract_type_id to contracttype_id
ALTER TABLE Contract ADD CONSTRAINT contract_type_id_contracttype_id_fkey FOREIGN KEY (contract_type_id) REFERENCES ContractType(contracttype_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contract_priority_id to contractpriority_id
ALTER TABLE Contract ADD CONSTRAINT contract_priority_id_contractpriority_id_fkey FOREIGN KEY (contract_priority_id) REFERENCES ContractPriority(contractpriority_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contract_status_id to contractstatus_id
ALTER TABLE Contract ADD CONSTRAINT contract_status_id_contractstatus_id_fkey FOREIGN KEY (contract_status_id) REFERENCES ContractStatus(contractstatus_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contract_contact1_id to contact_id
ALTER TABLE Contract ADD CONSTRAINT contract_contact1_id_contact_id_fkey FOREIGN KEY (contract_contact1_id) REFERENCES Contact(contact_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contract_contact2_id to contact_id
ALTER TABLE Contract ADD CONSTRAINT contract_contact2_id_contact_id_fkey FOREIGN KEY (contract_contact2_id) REFERENCES Contact(contact_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contract_techmanager_id to userobm_id
ALTER TABLE Contract ADD CONSTRAINT contract_techmanager_id_userobm_id_fkey FOREIGN KEY (contract_techmanager_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contract_marketmanager_id to userobm_id
ALTER TABLE Contract ADD CONSTRAINT contract_marketmanager_id_userobm_id_fkey FOREIGN KEY (contract_marketmanager_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contractpriority_domain_id to domain_id
ALTER TABLE ContractPriority ADD CONSTRAINT contractpriority_domain_id_domain_id_fkey FOREIGN KEY (contractpriority_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contractpriority_userupdate to userobm_id
ALTER TABLE ContractPriority ADD CONSTRAINT contractpriority_userupdate_userobm_id_fkey FOREIGN KEY (contractpriority_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contractpriority_usercreate to userobm_id
ALTER TABLE ContractPriority ADD CONSTRAINT contractpriority_usercreate_userobm_id_fkey FOREIGN KEY (contractpriority_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contractstatus_domain_id to domain_id
ALTER TABLE ContractStatus ADD CONSTRAINT contractstatus_domain_id_domain_id_fkey FOREIGN KEY (contractstatus_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contractstatus_userupdate to userobm_id
ALTER TABLE ContractStatus ADD CONSTRAINT contractstatus_userupdate_userobm_id_fkey FOREIGN KEY (contractstatus_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contractstatus_usercreate to userobm_id
ALTER TABLE ContractStatus ADD CONSTRAINT contractstatus_usercreate_userobm_id_fkey FOREIGN KEY (contractstatus_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contracttype_domain_id to domain_id
ALTER TABLE ContractType ADD CONSTRAINT contracttype_domain_id_domain_id_fkey FOREIGN KEY (contracttype_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contracttype_userupdate to userobm_id
ALTER TABLE ContractType ADD CONSTRAINT contracttype_userupdate_userobm_id_fkey FOREIGN KEY (contracttype_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contracttype_usercreate to userobm_id
ALTER TABLE ContractType ADD CONSTRAINT contracttype_usercreate_userobm_id_fkey FOREIGN KEY (contracttype_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from country_domain_id to domain_id
ALTER TABLE Country ADD CONSTRAINT country_domain_id_domain_id_fkey FOREIGN KEY (country_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from country_userupdate to userobm_id
ALTER TABLE Country ADD CONSTRAINT country_userupdate_userobm_id_fkey FOREIGN KEY (country_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from country_usercreate to userobm_id
ALTER TABLE Country ADD CONSTRAINT country_usercreate_userobm_id_fkey FOREIGN KEY (country_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from datasource_domain_id to domain_id
ALTER TABLE DataSource ADD CONSTRAINT datasource_domain_id_domain_id_fkey FOREIGN KEY (datasource_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from datasource_userupdate to userobm_id
ALTER TABLE DataSource ADD CONSTRAINT datasource_userupdate_userobm_id_fkey FOREIGN KEY (datasource_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from datasource_usercreate to userobm_id
ALTER TABLE DataSource ADD CONSTRAINT datasource_usercreate_userobm_id_fkey FOREIGN KEY (datasource_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from deal_domain_id to domain_id
ALTER TABLE Deal ADD CONSTRAINT deal_domain_id_domain_id_fkey FOREIGN KEY (deal_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from deal_parentdeal_id to parentdeal_id
ALTER TABLE Deal ADD CONSTRAINT deal_parentdeal_id_parentdeal_id_fkey FOREIGN KEY (deal_parentdeal_id) REFERENCES ParentDeal(parentdeal_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from deal_company_id to company_id
ALTER TABLE Deal ADD CONSTRAINT deal_company_id_company_id_fkey FOREIGN KEY (deal_company_id) REFERENCES Company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from deal_userupdate to userobm_id
ALTER TABLE Deal ADD CONSTRAINT deal_userupdate_userobm_id_fkey FOREIGN KEY (deal_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from deal_usercreate to userobm_id
ALTER TABLE Deal ADD CONSTRAINT deal_usercreate_userobm_id_fkey FOREIGN KEY (deal_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from deal_type_id to dealtype_id
ALTER TABLE Deal ADD CONSTRAINT deal_type_id_dealtype_id_fkey FOREIGN KEY (deal_type_id) REFERENCES DealType(dealtype_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from deal_region_id to region_id
ALTER TABLE Deal ADD CONSTRAINT deal_region_id_region_id_fkey FOREIGN KEY (deal_region_id) REFERENCES Region(region_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from deal_tasktype_id to tasktype_id
ALTER TABLE Deal ADD CONSTRAINT deal_tasktype_id_tasktype_id_fkey FOREIGN KEY (deal_tasktype_id) REFERENCES TaskType(tasktype_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from deal_contact1_id to contact_id
ALTER TABLE Deal ADD CONSTRAINT deal_contact1_id_contact_id_fkey FOREIGN KEY (deal_contact1_id) REFERENCES Contact(contact_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from deal_contact2_id to contact_id
ALTER TABLE Deal ADD CONSTRAINT deal_contact2_id_contact_id_fkey FOREIGN KEY (deal_contact2_id) REFERENCES Contact(contact_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from deal_marketingmanager_id to userobm_id
ALTER TABLE Deal ADD CONSTRAINT deal_marketingmanager_id_userobm_id_fkey FOREIGN KEY (deal_marketingmanager_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from deal_technicalmanager_id to userobm_id
ALTER TABLE Deal ADD CONSTRAINT deal_technicalmanager_id_userobm_id_fkey FOREIGN KEY (deal_technicalmanager_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from deal_source_id to leadsource_id
ALTER TABLE Deal ADD CONSTRAINT deal_source_id_leadsource_id_fkey FOREIGN KEY (deal_source_id) REFERENCES LeadSource(leadsource_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from dealcompany_deal_id to deal_id
ALTER TABLE DealCompany ADD CONSTRAINT dealcompany_deal_id_deal_id_fkey FOREIGN KEY (dealcompany_deal_id) REFERENCES Deal(deal_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from dealcompany_company_id to company_id
ALTER TABLE DealCompany ADD CONSTRAINT dealcompany_company_id_company_id_fkey FOREIGN KEY (dealcompany_company_id) REFERENCES Company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from dealcompany_role_id to dealcompanyrole_id
ALTER TABLE DealCompany ADD CONSTRAINT dealcompany_role_id_dealcompanyrole_id_fkey FOREIGN KEY (dealcompany_role_id) REFERENCES DealCompanyRole(dealcompanyrole_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from dealcompany_userupdate to userobm_id
ALTER TABLE DealCompany ADD CONSTRAINT dealcompany_userupdate_userobm_id_fkey FOREIGN KEY (dealcompany_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from dealcompany_usercreate to userobm_id
ALTER TABLE DealCompany ADD CONSTRAINT dealcompany_usercreate_userobm_id_fkey FOREIGN KEY (dealcompany_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from dealcompanyrole_domain_id to domain_id
ALTER TABLE DealCompanyRole ADD CONSTRAINT dealcompanyrole_domain_id_domain_id_fkey FOREIGN KEY (dealcompanyrole_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from dealcompanyrole_userupdate to userobm_id
ALTER TABLE DealCompanyRole ADD CONSTRAINT dealcompanyrole_userupdate_userobm_id_fkey FOREIGN KEY (dealcompanyrole_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from dealcompanyrole_usercreate to userobm_id
ALTER TABLE DealCompanyRole ADD CONSTRAINT dealcompanyrole_usercreate_userobm_id_fkey FOREIGN KEY (dealcompanyrole_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from dealstatus_domain_id to domain_id
ALTER TABLE DealStatus ADD CONSTRAINT dealstatus_domain_id_domain_id_fkey FOREIGN KEY (dealstatus_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from dealstatus_userupdate to userobm_id
ALTER TABLE DealStatus ADD CONSTRAINT dealstatus_userupdate_userobm_id_fkey FOREIGN KEY (dealstatus_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from dealstatus_usercreate to userobm_id
ALTER TABLE DealStatus ADD CONSTRAINT dealstatus_usercreate_userobm_id_fkey FOREIGN KEY (dealstatus_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from dealtype_domain_id to domain_id
ALTER TABLE DealType ADD CONSTRAINT dealtype_domain_id_domain_id_fkey FOREIGN KEY (dealtype_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from dealtype_userupdate to userobm_id
ALTER TABLE DealType ADD CONSTRAINT dealtype_userupdate_userobm_id_fkey FOREIGN KEY (dealtype_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from dealtype_usercreate to userobm_id
ALTER TABLE DealType ADD CONSTRAINT dealtype_usercreate_userobm_id_fkey FOREIGN KEY (dealtype_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from defaultodttemplate_domain_id to domain_id
ALTER TABLE DefaultOdtTemplate ADD CONSTRAINT defaultodttemplate_domain_id_domain_id_fkey FOREIGN KEY (defaultodttemplate_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from defaultodttemplate_document_id to document_id
ALTER TABLE DefaultOdtTemplate ADD CONSTRAINT defaultodttemplate_document_id_document_id_fkey FOREIGN KEY (defaultodttemplate_document_id) REFERENCES Document(document_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from deleted_user_id to userobm_id
ALTER TABLE Deleted ADD CONSTRAINT deleted_user_id_userobm_id_fkey FOREIGN KEY (deleted_user_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from deleted_domain_id to domain_id
ALTER TABLE Deleted ADD CONSTRAINT deleted_domain_id_domain_id_fkey FOREIGN KEY (deleted_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from display_user_id to userobm_id;
ALTER TABLE DisplayPref ADD CONSTRAINT display_user_id_userobm_id_fkey FOREIGN KEY (display_user_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from document_domain_id to domain_id
ALTER TABLE Document ADD CONSTRAINT document_domain_id_domain_id_fkey FOREIGN KEY (document_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from document_userupdate to userobm_id
ALTER TABLE Document ADD CONSTRAINT document_userupdate_userobm_id_fkey FOREIGN KEY (document_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from document_usercreate to userobm_id
ALTER TABLE Document ADD CONSTRAINT document_usercreate_userobm_id_fkey FOREIGN KEY (document_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from document_mimetype_id to documentmimetype_id
ALTER TABLE Document ADD CONSTRAINT document_mimetype_id_documentmimetype_id_fkey FOREIGN KEY (document_mimetype_id) REFERENCES DocumentMimeType(documentmimetype_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from documententity_document_id to document_id
ALTER TABLE DocumentEntity ADD CONSTRAINT documententity_document_id_document_id_fkey FOREIGN KEY (documententity_document_id) REFERENCES Document(document_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from documentmimetype_domain_id to domain_id
ALTER TABLE DocumentMimeType ADD CONSTRAINT documentmimetype_domain_id_domain_id_fkey FOREIGN KEY (documentmimetype_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from documentmimetype_userupdate to userobm_id
ALTER TABLE DocumentMimeType ADD CONSTRAINT documentmimetype_userupdate_userobm_id_fkey FOREIGN KEY (documentmimetype_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from documentmimetype_usercreate to userobm_id
ALTER TABLE DocumentMimeType ADD CONSTRAINT documentmimetype_usercreate_userobm_id_fkey FOREIGN KEY (documentmimetype_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from domain_userupdate to userobm_id
ALTER TABLE Domain ADD CONSTRAINT domain_userupdate_userobm_id_fkey FOREIGN KEY (domain_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from domain_usercreate to userobm_id
ALTER TABLE Domain ADD CONSTRAINT domain_usercreate_userobm_id_fkey FOREIGN KEY (domain_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from domainmailserver_domain_id to domain_id
ALTER TABLE DomainMailServer ADD CONSTRAINT domainmailserver_domain_id_domain_id_fkey FOREIGN KEY (domainmailserver_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from domainmailserver_mailserver_id to mailserver_id
ALTER TABLE DomainMailServer ADD CONSTRAINT domainmailserver_mailserver_id_mailserver_id_fkey FOREIGN KEY (domainmailserver_mailserver_id) REFERENCES MailServer(mailserver_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from domainpropertyvalue_domain_id to domain_id
ALTER TABLE DomainPropertyValue ADD CONSTRAINT domainpropertyvalue_domain_id_domain_id_fkey FOREIGN KEY (domainpropertyvalue_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from evententity_event_id to event_id
ALTER TABLE EventEntity ADD CONSTRAINT evententity_event_id_event_id_fkey FOREIGN KEY (evententity_event_id) REFERENCES Event(event_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from evententity_userupdate to userobm_id
ALTER TABLE EventEntity ADD CONSTRAINT evententity_userupdate_userobm_id_fkey FOREIGN KEY (evententity_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from evententity_usercreate to userobm_id
ALTER TABLE EventEntity ADD CONSTRAINT evententity_usercreate_userobm_id_fkey FOREIGN KEY (evententity_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from groupgroup_parent_id to group_id
ALTER TABLE GroupGroup ADD CONSTRAINT groupgroup_parent_id_group_id_fkey FOREIGN KEY (groupgroup_parent_id) REFERENCES UGroup(group_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from groupgroup_child_id to group_id
ALTER TABLE GroupGroup ADD CONSTRAINT groupgroup_child_id_group_id_fkey FOREIGN KEY (groupgroup_child_id) REFERENCES UGroup(group_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from host_domain_id to domain_id
ALTER TABLE Host ADD CONSTRAINT host_domain_id_domain_id_fkey FOREIGN KEY (host_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from host_userupdate to userobm_id
ALTER TABLE Host ADD CONSTRAINT host_userupdate_userobm_id_fkey FOREIGN KEY (host_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from host_usercreate to userobm_id
ALTER TABLE Host ADD CONSTRAINT host_usercreate_userobm_id_fkey FOREIGN KEY (host_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from import_domain_id to domain_id
ALTER TABLE Import ADD CONSTRAINT import_domain_id_domain_id_fkey FOREIGN KEY (import_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from import_userupdate to userobm_id
ALTER TABLE Import ADD CONSTRAINT import_userupdate_userobm_id_fkey FOREIGN KEY (import_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from import_usercreate to userobm_id
ALTER TABLE Import ADD CONSTRAINT import_usercreate_userobm_id_fkey FOREIGN KEY (import_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from import_datasource_id to datasource_id
ALTER TABLE Import ADD CONSTRAINT import_datasource_id_datasource_id_fkey FOREIGN KEY (import_datasource_id) REFERENCES DataSource(datasource_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from import_marketingmanager_id to userobm_id
ALTER TABLE Import ADD CONSTRAINT import_marketingmanager_id_userobm_id_fkey FOREIGN KEY (import_marketingmanager_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incident_domain_id to domain_id
ALTER TABLE Incident ADD CONSTRAINT incident_domain_id_domain_id_fkey FOREIGN KEY (incident_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from incident_contract_id to contract_id
ALTER TABLE Incident ADD CONSTRAINT incident_contract_id_contract_id_fkey FOREIGN KEY (incident_contract_id) REFERENCES Contract(contract_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from incident_userupdate to userobm_id
ALTER TABLE Incident ADD CONSTRAINT incident_userupdate_userobm_id_fkey FOREIGN KEY (incident_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incident_usercreate to userobm_id
ALTER TABLE Incident ADD CONSTRAINT incident_usercreate_userobm_id_fkey FOREIGN KEY (incident_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incident_priority_id to incidentpriority_id
ALTER TABLE Incident ADD CONSTRAINT incident_priority_id_incidentpriority_id_fkey FOREIGN KEY (incident_priority_id) REFERENCES IncidentPriority(incidentpriority_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incident_status_id to incidentstatus_id
ALTER TABLE Incident ADD CONSTRAINT incident_status_id_incidentstatus_id_fkey FOREIGN KEY (incident_status_id) REFERENCES IncidentStatus(incidentstatus_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incident_resolutiontype_id to incidentresolutiontype_id
ALTER TABLE Incident ADD CONSTRAINT incident_resolutiontype_id_incidentresolutiontype_id_fkey FOREIGN KEY (incident_resolutiontype_id) REFERENCES IncidentResolutionType(incidentresolutiontype_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incident_logger to userobm_id
ALTER TABLE Incident ADD CONSTRAINT incident_logger_userobm_id_fkey FOREIGN KEY (incident_logger) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incident_owner to userobm_id
ALTER TABLE Incident ADD CONSTRAINT incident_owner_userobm_id_fkey FOREIGN KEY (incident_owner) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incidentpriority_domain_id to domain_id
ALTER TABLE IncidentPriority ADD CONSTRAINT incidentpriority_domain_id_domain_id_fkey FOREIGN KEY (incidentpriority_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from incidentpriority_userupdate to userobm_id
ALTER TABLE IncidentPriority ADD CONSTRAINT incidentpriority_userupdate_userobm_id_fkey FOREIGN KEY (incidentpriority_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incidentpriority_usercreate to userobm_id
ALTER TABLE IncidentPriority ADD CONSTRAINT incidentpriority_usercreate_userobm_id_fkey FOREIGN KEY (incidentpriority_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incidentresolutiontype_domain_id to domain_id
ALTER TABLE IncidentResolutionType ADD CONSTRAINT incidentresolutiontype_domain_id_domain_id_fkey FOREIGN KEY (incidentresolutiontype_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from incidentresolutiontype_userupdate to userobm_id
ALTER TABLE IncidentResolutionType ADD CONSTRAINT incidentresolutiontype_userupdate_userobm_id_fkey FOREIGN KEY (incidentresolutiontype_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incidentresolutiontype_usercreate to userobm_id
ALTER TABLE IncidentResolutionType ADD CONSTRAINT incidentresolutiontype_usercreate_userobm_id_fkey FOREIGN KEY (incidentresolutiontype_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incidentstatus_domain_id to domain_id
ALTER TABLE IncidentStatus ADD CONSTRAINT incidentstatus_domain_id_domain_id_fkey FOREIGN KEY (incidentstatus_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from incidentstatus_userupdate to userobm_id
ALTER TABLE IncidentStatus ADD CONSTRAINT incidentstatus_userupdate_userobm_id_fkey FOREIGN KEY (incidentstatus_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incidentstatus_usercreate to userobm_id
ALTER TABLE IncidentStatus ADD CONSTRAINT incidentstatus_usercreate_userobm_id_fkey FOREIGN KEY (incidentstatus_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from invoice_domain_id to domain_id
ALTER TABLE Invoice ADD CONSTRAINT invoice_domain_id_domain_id_fkey FOREIGN KEY (invoice_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from invoice_company_id to company_id
ALTER TABLE Invoice ADD CONSTRAINT invoice_company_id_company_id_fkey FOREIGN KEY (invoice_company_id) REFERENCES Company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from invoice_project_id to project_id
ALTER TABLE Invoice ADD CONSTRAINT invoice_project_id_project_id_fkey FOREIGN KEY (invoice_project_id) REFERENCES Project(project_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from invoice_deal_id to deal_id
ALTER TABLE Invoice ADD CONSTRAINT invoice_deal_id_deal_id_fkey FOREIGN KEY (invoice_deal_id) REFERENCES Deal(deal_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from invoice_userupdate to userobm_id
ALTER TABLE Invoice ADD CONSTRAINT invoice_userupdate_userobm_id_fkey FOREIGN KEY (invoice_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from invoice_usercreate to userobm_id
ALTER TABLE Invoice ADD CONSTRAINT invoice_usercreate_userobm_id_fkey FOREIGN KEY (invoice_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from kind_domain_id to domain_id
ALTER TABLE Kind ADD CONSTRAINT kind_domain_id_domain_id_fkey FOREIGN KEY (kind_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from kind_userupdate to userobm_id
ALTER TABLE Kind ADD CONSTRAINT kind_userupdate_userobm_id_fkey FOREIGN KEY (kind_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from kind_usercreate to userobm_id
ALTER TABLE Kind ADD CONSTRAINT kind_usercreate_userobm_id_fkey FOREIGN KEY (kind_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from lead_domain_id to domain_id
ALTER TABLE Lead ADD CONSTRAINT lead_domain_id_domain_id_fkey FOREIGN KEY (lead_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from lead_company_id to company_id
ALTER TABLE Lead ADD CONSTRAINT lead_company_id_company_id_fkey FOREIGN KEY (lead_company_id) REFERENCES Company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from lead_userupdate to userobm_id
ALTER TABLE Lead ADD CONSTRAINT lead_userupdate_userobm_id_fkey FOREIGN KEY (lead_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from lead_usercreate to userobm_id
ALTER TABLE Lead ADD CONSTRAINT lead_usercreate_userobm_id_fkey FOREIGN KEY (lead_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from lead_source_id to leadsource_id
ALTER TABLE Lead ADD CONSTRAINT lead_source_id_leadsource_id_fkey FOREIGN KEY (lead_source_id) REFERENCES LeadSource(leadsource_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from lead_manager_id to userobm_id
ALTER TABLE Lead ADD CONSTRAINT lead_manager_id_userobm_id_fkey FOREIGN KEY (lead_manager_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from lead_contact_id to contact_id
ALTER TABLE Lead ADD CONSTRAINT lead_contact_id_contact_id_fkey FOREIGN KEY (lead_contact_id) REFERENCES Contact(contact_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from lead_status_id to leadstatus_id
ALTER TABLE Lead ADD CONSTRAINT lead_status_id_leadstatus_id_fkey FOREIGN KEY (lead_status_id) REFERENCES LeadStatus(leadstatus_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from leadsource_domain_id to domain_id
ALTER TABLE LeadSource ADD CONSTRAINT leadsource_domain_id_domain_id_fkey FOREIGN KEY (leadsource_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from leadsource_userupdate to userobm_id
ALTER TABLE LeadSource ADD CONSTRAINT leadsource_userupdate_userobm_id_fkey FOREIGN KEY (leadsource_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from leadsource_usercreate to userobm_id
ALTER TABLE LeadSource ADD CONSTRAINT leadsource_usercreate_userobm_id_fkey FOREIGN KEY (leadsource_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from leadstatus_domain_id to domain_id
ALTER TABLE LeadStatus ADD CONSTRAINT leadstatus_domain_id_domain_id_fkey FOREIGN KEY (leadstatus_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from leadstatus_userupdate to userobm_id
ALTER TABLE LeadStatus ADD CONSTRAINT leadstatus_userupdate_userobm_id_fkey FOREIGN KEY (leadstatus_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from leadstatus_usercreate to userobm_id
ALTER TABLE LeadStatus ADD CONSTRAINT leadstatus_usercreate_userobm_id_fkey FOREIGN KEY (leadstatus_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from list_domain_id to domain_id
ALTER TABLE List ADD CONSTRAINT list_domain_id_domain_id_fkey FOREIGN KEY (list_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from list_userupdate to userobm_id
ALTER TABLE List ADD CONSTRAINT list_userupdate_userobm_id_fkey FOREIGN KEY (list_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from list_usercreate to userobm_id
ALTER TABLE List ADD CONSTRAINT list_usercreate_userobm_id_fkey FOREIGN KEY (list_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from mailserver_host_id to host_id
ALTER TABLE MailServer ADD CONSTRAINT mailserver_host_id_host_id_fkey FOREIGN KEY (mailserver_host_id) REFERENCES Host(host_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from mailserver_userupdate to userobm_id
ALTER TABLE MailServer ADD CONSTRAINT mailserver_userupdate_userobm_id_fkey FOREIGN KEY (mailserver_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from mailserver_usercreate to userobm_id
ALTER TABLE MailServer ADD CONSTRAINT mailserver_usercreate_userobm_id_fkey FOREIGN KEY (mailserver_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from mailserver_relayhost_id to Host
ALTER TABLE MailServer ADD CONSTRAINT mailserver_relayhost_id_host_id_fkey FOREIGN KEY (mailserver_relayhost_id) REFERENCES Host(host_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from mailshare_domain_id to domain_id
ALTER TABLE MailShare ADD CONSTRAINT mailshare_domain_id_domain_id_fkey FOREIGN KEY (mailshare_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from mailshare_mail_server_id to mailserver_id
ALTER TABLE MailShare ADD CONSTRAINT mailshare_mail_server_id_mailserver_id_fkey FOREIGN KEY (mailshare_mail_server_id) REFERENCES MailServer(mailserver_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from mailshare_userupdate to userobm_id
ALTER TABLE MailShare ADD CONSTRAINT mailshare_userupdate_userobm_id_fkey FOREIGN KEY (mailshare_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from mailshare_usercreate to userobm_id
ALTER TABLE MailShare ADD CONSTRAINT mailshare_usercreate_userobm_id_fkey FOREIGN KEY (mailshare_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from ogroup_domain_id to domain_id
ALTER TABLE OGroup ADD CONSTRAINT ogroup_domain_id_domain_id_fkey FOREIGN KEY (ogroup_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from ogroup_organizationalchart_id to organizationalchart_id
ALTER TABLE OGroup ADD CONSTRAINT ogroup_organizationalchart_id_organizationalchart_id_fkey FOREIGN KEY (ogroup_organizationalchart_id) REFERENCES OrganizationalChart(organizationalchart_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from ogroup_parent_id to ogroup_id
ALTER TABLE OGroup ADD CONSTRAINT ogroup_parent_id_ogroup_id_fkey FOREIGN KEY (ogroup_parent_id) REFERENCES OGroup(ogroup_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from ogroup_userupdate to userobm_id
ALTER TABLE OGroup ADD CONSTRAINT ogroup_userupdate_userobm_id_fkey FOREIGN KEY (ogroup_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from ogroup_usercreate to userobm_id
ALTER TABLE OGroup ADD CONSTRAINT ogroup_usercreate_userobm_id_fkey FOREIGN KEY (ogroup_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from ogroupentity_ogroup_id to ogroup_id
ALTER TABLE OGroupEntity ADD CONSTRAINT ogroupentity_ogroup_id_ogroup_id_fkey FOREIGN KEY (ogroupentity_ogroup_id) REFERENCES OGroup(ogroup_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from ogroupentity_domain_id to domain_id
ALTER TABLE OGroupEntity ADD CONSTRAINT ogroupentity_domain_id_domain_id_fkey FOREIGN KEY (ogroupentity_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from ogroupentity_userupdate to userobm_id
ALTER TABLE OGroupEntity ADD CONSTRAINT ogroupentity_userupdate_userobm_id_fkey FOREIGN KEY (ogroupentity_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from ogroupentity_usercreate to userobm_id
ALTER TABLE OGroupEntity ADD CONSTRAINT ogroupentity_usercreate_userobm_id_fkey FOREIGN KEY (ogroupentity_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from obmbookmark_user_id to userobm_id
ALTER TABLE ObmBookmark ADD CONSTRAINT obmbookmark_user_id_userobm_id_fkey FOREIGN KEY (obmbookmark_user_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from obmbookmarkproperty_bookmark_id to obmbookmark_id
ALTER TABLE ObmBookmarkProperty ADD CONSTRAINT obmbookmarkproperty_bookmark_id_obmbookmark_id_fkey FOREIGN KEY (obmbookmarkproperty_bookmark_id) REFERENCES ObmBookmark(obmbookmark_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from organizationalchart_domain_id to domain_id
ALTER TABLE OrganizationalChart ADD CONSTRAINT organizationalchart_domain_id_domain_id_fkey FOREIGN KEY (organizationalchart_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from organizationalchart_userupdate to userobm_id
ALTER TABLE OrganizationalChart ADD CONSTRAINT organizationalchart_userupdate_userobm_id_fkey FOREIGN KEY (organizationalchart_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from organizationalchart_usercreate to userobm_id
ALTER TABLE OrganizationalChart ADD CONSTRAINT organizationalchart_usercreate_userobm_id_fkey FOREIGN KEY (organizationalchart_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from parentdeal_domain_id to domain_id
ALTER TABLE ParentDeal ADD CONSTRAINT parentdeal_domain_id_domain_id_fkey FOREIGN KEY (parentdeal_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from parentdeal_userupdate to userobm_id
ALTER TABLE ParentDeal ADD CONSTRAINT parentdeal_userupdate_userobm_id_fkey FOREIGN KEY (parentdeal_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from parentdeal_usercreate to userobm_id
ALTER TABLE ParentDeal ADD CONSTRAINT parentdeal_usercreate_userobm_id_fkey FOREIGN KEY (parentdeal_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from parentdeal_marketingmanager_id to userobm_id
ALTER TABLE ParentDeal ADD CONSTRAINT parentdeal_marketingmanager_id_userobm_id_fkey FOREIGN KEY (parentdeal_marketingmanager_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from parentdeal_technicalmanager_id to userobm_id
ALTER TABLE ParentDeal ADD CONSTRAINT parentdeal_technicalmanager_id_userobm_id_fkey FOREIGN KEY (parentdeal_technicalmanager_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from payment_domain_id to domain_id
ALTER TABLE Payment ADD CONSTRAINT payment_domain_id_domain_id_fkey FOREIGN KEY (payment_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from payment_account_id to account_id
ALTER TABLE Payment ADD CONSTRAINT payment_account_id_account_id_fkey FOREIGN KEY (payment_account_id) REFERENCES Account(account_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from payment_userupdate to userobm_id
ALTER TABLE Payment ADD CONSTRAINT payment_userupdate_userobm_id_fkey FOREIGN KEY (payment_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from payment_usercreate to userobm_id
ALTER TABLE Payment ADD CONSTRAINT payment_usercreate_userobm_id_fkey FOREIGN KEY (payment_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from payment_company_id to company_id
ALTER TABLE Payment ADD CONSTRAINT payment_company_id_company_id_fkey FOREIGN KEY (payment_company_id) REFERENCES Company(company_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from payment_paymentkind_id to paymentkind_id
ALTER TABLE Payment ADD CONSTRAINT payment_paymentkind_id_paymentkind_id_fkey FOREIGN KEY (payment_paymentkind_id) REFERENCES PaymentKind(paymentkind_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from paymentinvoice_invoice_id to invoice_id
ALTER TABLE PaymentInvoice ADD CONSTRAINT paymentinvoice_invoice_id_invoice_id_fkey FOREIGN KEY (paymentinvoice_invoice_id) REFERENCES Invoice(invoice_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from paymentinvoice_payment_id to payment_id
ALTER TABLE PaymentInvoice ADD CONSTRAINT paymentinvoice_payment_id_payment_id_fkey FOREIGN KEY (paymentinvoice_payment_id) REFERENCES Payment(payment_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from paymentinvoice_usercreate to userobm_id
ALTER TABLE PaymentInvoice ADD CONSTRAINT paymentinvoice_usercreate_userobm_id_fkey FOREIGN KEY (paymentinvoice_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from paymentinvoice_userupdate to userobm_id
ALTER TABLE PaymentInvoice ADD CONSTRAINT paymentinvoice_userupdate_userobm_id_fkey FOREIGN KEY (paymentinvoice_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from paymentkind_domain_id to domain_id
ALTER TABLE PaymentKind ADD CONSTRAINT paymentkind_domain_id_domain_id_fkey FOREIGN KEY (paymentkind_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from project_domain_id to domain_id
ALTER TABLE Project ADD CONSTRAINT project_domain_id_domain_id_fkey FOREIGN KEY (project_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from project_deal_id to deal_id
ALTER TABLE Project ADD CONSTRAINT project_deal_id_deal_id_fkey FOREIGN KEY (project_deal_id) REFERENCES Deal(deal_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from project_company_id to company_id
ALTER TABLE Project ADD CONSTRAINT project_company_id_company_id_fkey FOREIGN KEY (project_company_id) REFERENCES Company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from project_userupdate to userobm_id
ALTER TABLE Project ADD CONSTRAINT project_userupdate_userobm_id_fkey FOREIGN KEY (project_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from project_usercreate to userobm_id
ALTER TABLE Project ADD CONSTRAINT project_usercreate_userobm_id_fkey FOREIGN KEY (project_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from project_tasktype_id to tasktype_id
ALTER TABLE Project ADD CONSTRAINT project_tasktype_id_tasktype_id_fkey FOREIGN KEY (project_tasktype_id) REFERENCES TaskType(tasktype_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from project_type_id to dealtype_id
ALTER TABLE Project ADD CONSTRAINT project_type_id_dealtype_id_fkey FOREIGN KEY (project_type_id) REFERENCES DealType(dealtype_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from projectcv_project_id to project_id
ALTER TABLE ProjectCV ADD CONSTRAINT projectcv_project_id_project_id_fkey FOREIGN KEY (projectcv_project_id) REFERENCES Project(project_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from projectcv_cv_id to cv_id
ALTER TABLE ProjectCV ADD CONSTRAINT projectcv_cv_id_cv_id_fkey FOREIGN KEY (projectcv_cv_id) REFERENCES CV(cv_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from projectclosing_project_id to project_id
ALTER TABLE ProjectClosing ADD CONSTRAINT projectclosing_project_id_project_id_fkey FOREIGN KEY (projectclosing_project_id) REFERENCES Project(project_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from projectclosing_userupdate to userobm_id
ALTER TABLE ProjectClosing ADD CONSTRAINT projectclosing_userupdate_userobm_id_fkey FOREIGN KEY (projectclosing_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from projectclosing_usercreate to userobm_id
ALTER TABLE ProjectClosing ADD CONSTRAINT projectclosing_usercreate_userobm_id_fkey FOREIGN KEY (projectclosing_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from projectreftask_tasktype_id to tasktype_id
ALTER TABLE ProjectRefTask ADD CONSTRAINT projectreftask_tasktype_id_tasktype_id_fkey FOREIGN KEY (projectreftask_tasktype_id) REFERENCES TaskType(tasktype_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from projectreftask_userupdate to userobm_id
ALTER TABLE ProjectRefTask ADD CONSTRAINT projectreftask_userupdate_userobm_id_fkey FOREIGN KEY (projectreftask_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from projectreftask_usercreate to userobm_id
ALTER TABLE ProjectRefTask ADD CONSTRAINT projectreftask_usercreate_userobm_id_fkey FOREIGN KEY (projectreftask_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from projecttask_project_id to project_id
ALTER TABLE ProjectTask ADD CONSTRAINT projecttask_project_id_project_id_fkey FOREIGN KEY (projecttask_project_id) REFERENCES Project(project_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from projecttask_parenttask_id to projecttask_id
ALTER TABLE ProjectTask ADD CONSTRAINT projecttask_parenttask_id_projecttask_id_fkey FOREIGN KEY (projecttask_parenttask_id) REFERENCES ProjectTask(projecttask_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from projecttask_userupdate to userobm_id
ALTER TABLE ProjectTask ADD CONSTRAINT projecttask_userupdate_userobm_id_fkey FOREIGN KEY (projecttask_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from projecttask_usercreate to userobm_id
ALTER TABLE ProjectTask ADD CONSTRAINT projecttask_usercreate_userobm_id_fkey FOREIGN KEY (projecttask_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from projectuser_project_id to project_id
ALTER TABLE ProjectUser ADD CONSTRAINT projectuser_project_id_project_id_fkey FOREIGN KEY (projectuser_project_id) REFERENCES Project(project_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from projectuser_user_id to userobm_id
ALTER TABLE ProjectUser ADD CONSTRAINT projectuser_user_id_userobm_id_fkey FOREIGN KEY (projectuser_user_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from projectuser_userupdate to userobm_id
ALTER TABLE ProjectUser ADD CONSTRAINT projectuser_userupdate_userobm_id_fkey FOREIGN KEY (projectuser_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from projectuser_usercreate to userobm_id
ALTER TABLE ProjectUser ADD CONSTRAINT projectuser_usercreate_userobm_id_fkey FOREIGN KEY (projectuser_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from publication_domain_id to domain_id
ALTER TABLE Publication ADD CONSTRAINT publication_domain_id_domain_id_fkey FOREIGN KEY (publication_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from publication_userupdate to userobm_id
ALTER TABLE Publication ADD CONSTRAINT publication_userupdate_userobm_id_fkey FOREIGN KEY (publication_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from publication_usercreate to userobm_id
ALTER TABLE Publication ADD CONSTRAINT publication_usercreate_userobm_id_fkey FOREIGN KEY (publication_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from publication_type_id to publicationtype_id
ALTER TABLE Publication ADD CONSTRAINT publication_type_id_publicationtype_id_fkey FOREIGN KEY (publication_type_id) REFERENCES PublicationType(publicationtype_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from publicationtype_domain_id to domain_id
ALTER TABLE PublicationType ADD CONSTRAINT publicationtype_domain_id_domain_id_fkey FOREIGN KEY (publicationtype_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from publicationtype_userupdate to userobm_id
ALTER TABLE PublicationType ADD CONSTRAINT publicationtype_userupdate_userobm_id_fkey FOREIGN KEY (publicationtype_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from publicationtype_usercreate to userobm_id
ALTER TABLE PublicationType ADD CONSTRAINT publicationtype_usercreate_userobm_id_fkey FOREIGN KEY (publicationtype_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from rgroup_domain_id to domain_id
ALTER TABLE RGroup ADD CONSTRAINT rgroup_domain_id_domain_id_fkey FOREIGN KEY (rgroup_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from rgroup_userupdate to userobm_id
ALTER TABLE RGroup ADD CONSTRAINT rgroup_userupdate_userobm_id_fkey FOREIGN KEY (rgroup_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from rgroup_usercreate to userobm_id
ALTER TABLE RGroup ADD CONSTRAINT rgroup_usercreate_userobm_id_fkey FOREIGN KEY (rgroup_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from region_domain_id to domain_id
ALTER TABLE Region ADD CONSTRAINT region_domain_id_domain_id_fkey FOREIGN KEY (region_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from region_userupdate to userobm_id
ALTER TABLE Region ADD CONSTRAINT region_userupdate_userobm_id_fkey FOREIGN KEY (region_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from region_usercreate to userobm_id
ALTER TABLE Region ADD CONSTRAINT region_usercreate_userobm_id_fkey FOREIGN KEY (region_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from resource_domain_id to domain_id
ALTER TABLE Resource ADD CONSTRAINT resource_domain_id_domain_id_fkey FOREIGN KEY (resource_domain_id) REFERENCES Domain (domain_id) ON DELETE CASCADE ON UPDATE CASCADE;

-- Foreign key from resource_userupdate to userobm_id
ALTER TABLE Resource ADD CONSTRAINT resource_userupdate_userobm_id_fkey FOREIGN KEY (resource_userupdate) REFERENCES UserObm (userobm_id) ON DELETE SET NULL ON UPDATE CASCADE;

-- Foreign key from resource_usercreate to userobm_id
ALTER TABLE Resource ADD CONSTRAINT resource_usercreate_userobm_id_fkey FOREIGN KEY (resource_usercreate) REFERENCES UserObm (userobm_id) ON DELETE SET NULL ON UPDATE CASCADE;

-- Foreign key from resource_rtype_id to resourcetype_id
ALTER TABLE Resource ADD CONSTRAINT resource_rtype_id_resourcetype_id_fkey FOREIGN KEY (resource_rtype_id) REFERENCES ResourceType (resourcetype_id) ON DELETE SET NULL ON UPDATE CASCADE;

-- Foreign key from resourcegroup_rgroup_id to rgroup_id
ALTER TABLE ResourceGroup ADD CONSTRAINT resourcegroup_rgroup_id_rgroup_id_fkey FOREIGN KEY (resourcegroup_rgroup_id) REFERENCES RGroup(rgroup_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from resourcegroup_resource_id to resource_id
ALTER TABLE ResourceGroup ADD CONSTRAINT resourcegroup_resource_id_resource_id_fkey FOREIGN KEY (resourcegroup_resource_id) REFERENCES Resource(resource_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from resourceitem_domain_id to domain_id
ALTER TABLE ResourceItem ADD CONSTRAINT resourceitem_domain_id_domain_id_fkey FOREIGN KEY (resourceitem_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from resourceitem_resourcetype_id to resourcetype_id
ALTER TABLE ResourceItem ADD CONSTRAINT resourceitem_resourcetype_id_resourcetype_id_fkey FOREIGN KEY (resourceitem_resourcetype_id) REFERENCES ResourceType(resourcetype_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from resourcetype_domain_id to domain_id
ALTER TABLE ResourceType ADD CONSTRAINT resourcetype_domain_id_domain_id_fkey FOREIGN KEY (resourcetype_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from samba_domain_id to domain_id
ALTER TABLE Samba ADD CONSTRAINT samba_domain_id_domain_id_fkey FOREIGN KEY (samba_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from subscription_domain_id to domain_id
ALTER TABLE Subscription ADD CONSTRAINT subscription_domain_id_domain_id_fkey FOREIGN KEY (subscription_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from subscription_publication_id to publication_id
ALTER TABLE Subscription ADD CONSTRAINT subscription_publication_id_publication_id_fkey FOREIGN KEY (subscription_publication_id) REFERENCES Publication(publication_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from subscription_contact_id to contact_id
ALTER TABLE Subscription ADD CONSTRAINT subscription_contact_id_contact_id_fkey FOREIGN KEY (subscription_contact_id) REFERENCES Contact(contact_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from subscription_userupdate to userobm_id
ALTER TABLE Subscription ADD CONSTRAINT subscription_userupdate_userobm_id_fkey FOREIGN KEY (subscription_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from subscription_usercreate to userobm_id
ALTER TABLE Subscription ADD CONSTRAINT subscription_usercreate_userobm_id_fkey FOREIGN KEY (subscription_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from subscription_reception_id to subscriptionreception_id
ALTER TABLE Subscription ADD CONSTRAINT subscription_reception_id_subscriptionreception_id_fkey FOREIGN KEY (subscription_reception_id) REFERENCES SubscriptionReception(subscriptionreception_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from subscriptionreception_domain_id to domain_id
ALTER TABLE SubscriptionReception ADD CONSTRAINT subscriptionreception_domain_id_domain_id_fkey FOREIGN KEY (subscriptionreception_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from subscriptionreception_userupdate to userobm_id
ALTER TABLE SubscriptionReception ADD CONSTRAINT subscriptionreception_userupdate_userobm_id_fkey FOREIGN KEY (subscriptionreception_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from subscriptionreception_usercreate to userobm_id
ALTER TABLE SubscriptionReception ADD CONSTRAINT subscriptionreception_usercreate_userobm_id_fkey FOREIGN KEY (subscriptionreception_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from tasktype_domain_id to domain_id
ALTER TABLE TaskType ADD CONSTRAINT tasktype_domain_id_domain_id_fkey FOREIGN KEY (tasktype_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from tasktype_userupdate to userobm_id
ALTER TABLE TaskType ADD CONSTRAINT tasktype_userupdate_userobm_id_fkey FOREIGN KEY (tasktype_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from tasktype_usercreate to userobm_id
ALTER TABLE TaskType ADD CONSTRAINT tasktype_usercreate_userobm_id_fkey FOREIGN KEY (tasktype_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from timetask_user_id to userobm_id
ALTER TABLE TimeTask ADD CONSTRAINT timetask_user_id_userobm_id_fkey FOREIGN KEY (timetask_user_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from timetask_projecttask_id to projecttask_id
ALTER TABLE TimeTask ADD CONSTRAINT timetask_projecttask_id_projecttask_id_fkey FOREIGN KEY (timetask_projecttask_id) REFERENCES ProjectTask(projecttask_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from timetask_tasktype_id to tasktype_id
ALTER TABLE TimeTask ADD CONSTRAINT timetask_tasktype_id_tasktype_id_fkey FOREIGN KEY (timetask_tasktype_id) REFERENCES TaskType(tasktype_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from timetask_userupdate to userobm_id
ALTER TABLE TimeTask ADD CONSTRAINT timetask_userupdate_userobm_id_fkey FOREIGN KEY (timetask_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from timetask_usercreate to userobm_id
ALTER TABLE TimeTask ADD CONSTRAINT timetask_usercreate_userobm_id_fkey FOREIGN KEY (timetask_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from todo_domain_id to domain_id
ALTER TABLE Todo ADD CONSTRAINT todo_domain_id_domain_id_fkey FOREIGN KEY (todo_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from todo_user to userobm_id
ALTER TABLE Todo ADD CONSTRAINT todo_user_userobm_id_fkey FOREIGN KEY (todo_user) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from todo_userupdate to userobm_id
ALTER TABLE Todo ADD CONSTRAINT todo_userupdate_userobm_id_fkey FOREIGN KEY (todo_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from todo_usercreate to userobm_id
ALTER TABLE Todo ADD CONSTRAINT todo_usercreate_userobm_id_fkey FOREIGN KEY (todo_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from group_domain_id to domain_id
ALTER TABLE UGroup ADD CONSTRAINT group_domain_id_domain_id_fkey FOREIGN KEY (group_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from group_userupdate to userobm_id
ALTER TABLE UGroup ADD CONSTRAINT group_userupdate_userobm_id_fkey FOREIGN KEY (group_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from group_usercreate to userobm_id
ALTER TABLE UGroup ADD CONSTRAINT group_usercreate_userobm_id_fkey FOREIGN KEY (group_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from group_manager_id to userobm_id
ALTER TABLE UGroup ADD CONSTRAINT group_manager_id_userobm_id_fkey FOREIGN KEY (group_manager_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from updated_domain_id to domain_id
ALTER TABLE Updated ADD CONSTRAINT updated_domain_id_domain_id_fkey FOREIGN KEY (updated_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from updated_user_id to userobm_id
ALTER TABLE Updated ADD CONSTRAINT updated_user_id_userobm_id_fkey FOREIGN KEY (updated_user_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from updatedlinks_domain_id to domain_id
ALTER TABLE Updatedlinks ADD CONSTRAINT updatedlinks_domain_id_domain_id_fkey FOREIGN KEY (updatedlinks_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from updatedlinks_user_id to userobm_id
ALTER TABLE Updatedlinks ADD CONSTRAINT updatedlinks_user_id_userobm_id_fkey FOREIGN KEY (updatedlinks_user_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from userobm_domain_id to domain_id
ALTER TABLE UserObm ADD CONSTRAINT userobm_domain_id_domain_id_fkey FOREIGN KEY (userobm_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from userobm_userupdate to userobm_id
ALTER TABLE UserObm ADD CONSTRAINT userobm_userupdate_userobm_id_fkey FOREIGN KEY (userobm_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from userobm_usercreate to userobm_id
ALTER TABLE UserObm ADD CONSTRAINT userobm_usercreate_userobm_id_fkey FOREIGN KEY (userobm_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from userobm_mail_server_id to mailserver_id
ALTER TABLE UserObm ADD CONSTRAINT userobm_mail_server_id_mailserver_id_fkey FOREIGN KEY (userobm_mail_server_id) REFERENCES MailServer(mailserver_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from userobm_host_id to host_id
ALTER TABLE UserObm ADD CONSTRAINT userobm_host_id_host_id_fkey FOREIGN KEY (userobm_host_id) REFERENCES Host(host_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from userobm_photo_id to document_id
ALTER TABLE UserObm ADD CONSTRAINT userobm_photo_id_document_id_fkey FOREIGN KEY (userobm_photo_id) REFERENCES Document(document_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from userobmgroup_group_id to group_id
ALTER TABLE UserObmGroup ADD CONSTRAINT userobmgroup_group_id_group_id_fkey FOREIGN KEY (userobmgroup_group_id) REFERENCES UGroup(group_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from userobmgroup_userobm_id to userobm_id
ALTER TABLE UserObmGroup ADD CONSTRAINT userobmgroup_userobm_id_userobm_id_fkey FOREIGN KEY (userobmgroup_userobm_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from userobmpref_user_id to userobm_id
ALTER TABLE UserObmPref ADD CONSTRAINT userobmpref_user_id_userobm_id_fkey FOREIGN KEY (userobmpref_user_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from userobm_sessionlog_userobm_id to userobm_id
ALTER TABLE UserObm_SessionLog ADD CONSTRAINT userobm_sessionlog_userobm_id_userobm_id_fkey FOREIGN KEY (userobm_sessionlog_userobm_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from of_usergroup_group_id to group_id
ALTER TABLE of_usergroup ADD CONSTRAINT of_usergroup_group_id_group_id_fkey FOREIGN KEY (of_usergroup_group_id) REFERENCES UGroup(group_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from of_usergroup_user_id to userobm_id
ALTER TABLE of_usergroup ADD CONSTRAINT of_usergroup_user_id_userobm_id_fkey FOREIGN KEY (of_usergroup_user_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from profilemodule_profile_id to profile_id
ALTER TABLE ProfileModule ADD CONSTRAINT profilemodule_profile_id_profile_id_fkey FOREIGN KEY (profilemodule_profile_id) REFERENCES Profile(profile_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from profilesection_profile_id to profile_id
ALTER TABLE ProfileSection ADD CONSTRAINT profilesection_profile_id_profile_id_fkey FOREIGN KEY (profilesection_profile_id) REFERENCES Profile(profile_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from profilepropertyvalue_profile_id to profile_id
ALTER TABLE ProfilePropertyValue ADD CONSTRAINT profilepropertyvalue_profile_id_profile_id_fkey FOREIGN KEY (profilepropertyvalue_profile_id) REFERENCES Profile(profile_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from profilepropertyvalue_property_id to profileproperty_id
ALTER TABLE ProfilePropertyValue ADD CONSTRAINT profilepropertyvalue_profileproperty_id_profileproperty_id_fkey FOREIGN KEY (profilepropertyvalue_property_id) REFERENCES ProfileProperty(profileproperty_id) ON UPDATE CASCADE ON DELETE CASCADE;


COMMIT;
