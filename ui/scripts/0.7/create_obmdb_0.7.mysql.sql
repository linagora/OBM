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
-- User, Preferences tables
-------------------------------------------------------------------------------

--
-- Table structure for table 'ActiveUserObm'
--
CREATE TABLE ActiveUserObm (
  activeuserobm_sid		varchar(32) NOT NULL default '',
  activeuserobm_session_name	varchar(32) NOT NULL default '',
  activeuserobm_userobm_id	int(11) default NULL,
  activeuserobm_timeupdate	varchar(14) NOT NULL default '',
  activeuserobm_timecreate	varchar(14) NOT NULL default '0',
  activeuserobm_nb_connexions	int(11) NOT NULL default '0',
  activeuserobm_lastpage	varchar(32) NOT NULL default '0',
  activeuserobm_ip		varchar(32) NOT NULL default '0',
  PRIMARY KEY  (activeuserobm_sid)
);


--
-- Table structure for table 'UserObm_SessionLog'
--
CREATE TABLE UserObm_SessionLog (
  userobm_sessionlog_sid varchar(32) NOT NULL default '',
  userobm_sessionlog_session_name varchar(32) NOT NULL default '',
  userobm_sessionlog_userobm_id int(11) default NULL,
  userobm_sessionlog_timeupdate varchar(14) NOT NULL default '',
  userobm_sessionlog_timecreate varchar(14) NOT NULL default '0',
  userobm_sessionlog_nb_connexions int(11) NOT NULL default '0',
  userobm_sessionlog_lastpage varchar(32) NOT NULL default '0',
  userobm_sessionlog_ip varchar(32) NOT NULL default '0',
  PRIMARY KEY  (userobm_sessionlog_sid)
);

--
-- Table structure for table 'UserObm'
--
CREATE TABLE UserObm (
  userobm_id                int(8) DEFAULT '0' NOT NULL auto_increment,
  userobm_timeupdate        timestamp(14),
  userobm_timecreate        timestamp(14),
  userobm_userupdate        int(8),
  userobm_usercreate        int(8),
  userobm_login             varchar(32) DEFAULT '' NOT NULL,
  userobm_password          varchar(32) DEFAULT '' NOT NULL,
  userobm_perms             varchar(254),
  userobm_archive           char(1) not null default '0',
  userobm_lastname          varchar(32),
  userobm_firstname         varchar(32),
  userobm_phone             varchar(20),
  userobm_email             varchar(60),
  userobm_timelastaccess    timestamp(14),
  PRIMARY KEY (userobm_id),
  UNIQUE k_login_user (userobm_login)
);


--
-- Table structure for table 'UserObmPref'
--
CREATE TABLE UserObmPref (
   userobmpref_user_id int(8) DEFAULT '0' NOT NULL,
   userobmpref_option varchar(50) NOT NULL,
   userobmpref_value varchar(50) NOT NULL
);


--
-- New table 'DisplayPref'
--
CREATE TABLE DisplayPref (
  display_user_id int(8) NOT NULL default '0',
  display_entity varchar(32) NOT NULL default '',
  display_fieldname varchar(64) NOT NULL default '',
  display_fieldorder int(3) unsigned default NULL,
  display_display int(1) unsigned NOT NULL default '1',
  PRIMARY KEY(display_user_id, display_entity, display_fieldname),
  INDEX idx_user (display_user_id),
  INDEX idx_entity (display_entity)
) TYPE=MyISAM;


--
-- Table structure for table `GlobalPref`
--
CREATE TABLE GlobalPref (
  globalpref_option varchar(255) NOT NULL default '',
  globalpref_value varchar(255) NOT NULL default '',
  PRIMARY KEY  (globalpref_option),
  UNIQUE KEY globalpref_option (globalpref_option)
) TYPE=MyISAM;


-------------------------------------------------------------------------------
-- Company module tables
-------------------------------------------------------------------------------
-- 
-- Table structure for table 'CompanyType'
--
CREATE TABLE CompanyType (
  companytype_id int(8) DEFAULT '0' NOT NULL auto_increment,
  companytype_timeupdate timestamp(14),
  companytype_timecreate timestamp(14),
  companytype_userupdate int(8),
  companytype_usercreate int(8),
  companytype_label char(12),
  PRIMARY KEY (companytype_id)
);


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


--
-- Table structure for table 'Company'
--
CREATE TABLE Company (
  company_id int(8) DEFAULT '0' NOT NULL auto_increment,
  company_timeupdate timestamp(14),
  company_timecreate timestamp(14),
  company_userupdate int(8),
  company_usercreate int(8),
  company_number varchar(32),
  company_archive char(1) DEFAULT '0' NOT NULL,
  company_name varchar(50) DEFAULT '' NOT NULL,
  company_type_id int(8),
  company_activity_id int(8),
  company_marketingmanager_id int(8),
  company_address1 varchar(64),
  company_address2 varchar(64),
  company_zipcode varchar(14),
  company_town varchar(64),
  company_expresspostal varchar(8),
  company_country varchar(24),
  company_phone varchar(16),
  company_fax varchar(16),
  company_web varchar(64),
  company_email varchar(64),
  company_contact_number int(5) DEFAULT '0' NOT NULL,
  company_deal_number int(5) DEFAULT '0' NOT NULL,
  company_deal_total int(5) DEFAULT '0' NOT NULL,
  company_comment text,
  PRIMARY KEY (company_id)
);


-------------------------------------------------------------------------------
-- Contact module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'Contact'
--
CREATE TABLE Contact (
  contact_id int(8) DEFAULT '0' NOT NULL auto_increment,
  contact_timeupdate timestamp(14),
  contact_timecreate timestamp(14),
  contact_userupdate int(8),
  contact_usercreate int(8),
  contact_company_id int(8),
  contact_kind_id int(8),
  contact_lastname varchar(24) DEFAULT '' NOT NULL,
  contact_firstname varchar(24),
  contact_address1 varchar(50),
  contact_address2 varchar(50),
  contact_zipcode varchar(14),
  contact_town varchar(24),
  contact_expresspostal varchar(8),
  contact_country varchar(24),
  contact_function varchar(50),
  contact_phone varchar(16),
  contact_homephone varchar(16),
  contact_mobilephone varchar(16),
  contact_fax varchar(16),
  contact_email varchar(52),
  contact_mailing_ok char(1) DEFAULT '0',
  contact_archive char(1) DEFAULT '0',
  contact_visibility int(2) DEFAULT '0',
  contact_comment text,
  PRIMARY KEY (contact_id)
);


--
-- Table structure for table 'Kind'
--
CREATE TABLE Kind (
  kind_id int(8) DEFAULT '0' NOT NULL auto_increment,
  kind_timeupdate timestamp(14),
  kind_timecreate timestamp(14),
  kind_userupdate int(8),
  kind_usercreate int(8),
  kind_minilabel char(5),
  kind_label char(20),
  PRIMARY KEY (kind_id)
);

-------------------------------------------------------------------------------
-- Deal module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'ParentDeal'
--
CREATE TABLE ParentDeal (
   parentdeal_id int(8) NOT NULL auto_increment,
   parentdeal_timeupdate timestamp(14),
   parentdeal_timecreate timestamp(14),
   parentdeal_userupdate int(8),
   parentdeal_usercreate int(8),
   parentdeal_label varchar(128) NOT NULL,
   parentdeal_marketingmanager_id int(8),
   parentdeal_technicalmanager_id int(8),
   parentdeal_archive char(1) DEFAULT '0',
   parentdeal_comment text,
   PRIMARY KEY (parentdeal_id)
);


--
-- Table structure for table 'Deal'
--
CREATE TABLE Deal (
  deal_id int(8) DEFAULT '0' NOT NULL auto_increment,
  deal_timeupdate timestamp(14),
  deal_timecreate timestamp(14),
  deal_userupdate int(8),
  deal_usercreate int(8),
  deal_number varchar(32),
  deal_label varchar(128),
  deal_datebegin date,
  deal_parentdeal_id int(8),
  deal_type_id int(8),
  deal_tasktype_id int(8),
  deal_company_id int(8) DEFAULT '0' NOT NULL,
  deal_contact1_id int(8),
  deal_contact2_id int(8),
  deal_marketingmanager_id int(8),
  deal_technicalmanager_id int(8),
  deal_dateproposal date,
  deal_amount decimal(12,2),
  deal_hitrate char(3) DEFAULT '0',
  deal_status_id int(2),
  deal_datealarm date,
  deal_comment text,
  deal_archive char(1) DEFAULT '0',
  deal_todo varchar(128),
  deal_visibility int(2) DEFAULT '0',
  deal_soldtime int(8) DEFAULT NULL,
  deal_state int(1) DEFAULT 0,
  PRIMARY KEY (deal_id)
);

--
-- Table structure for table 'DealStatus'
--
CREATE TABLE DealStatus (
  dealstatus_id int(2) DEFAULT '0' NOT NULL auto_increment,
  dealstatus_timeupdate timestamp(14),
  dealstatus_timecreate timestamp(14),
  dealstatus_userupdate int(8),
  dealstatus_usercreate int(8),
  dealstatus_label varchar(20),
  dealstatus_order int(2),
  dealstatus_hitrate char(3),
  PRIMARY KEY (dealstatus_id)
);

--
-- Table structure for table 'DealType'
--
CREATE TABLE DealType (
  dealtype_id int(8) DEFAULT '0' NOT NULL auto_increment,
  dealtype_timeupdate timestamp(14),
  dealtype_timecreate timestamp(14),
  dealtype_userupdate int(8),
  dealtype_usercreate int(8),
  dealtype_label varchar(16),
  dealtype_inout varchar(1) DEFAULT '-',
  PRIMARY KEY (dealtype_id)
);

-------------------------------------------------------------------------------
-- Document module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'Document'
--


CREATE TABLE Document (
  document_timeupdate timestamp(14) NOT NULL,
  document_timecreate timestamp(14) NOT NULL,
  document_userupdate int(8) default NULL,
  document_usercreate int(8) default NULL,
  document_id int(8) NOT NULL auto_increment,
  document_title varchar(255) default NULL,
  document_name varchar(255) default NULL,
  document_mimetype varchar(255) default NULL,
  document_category1 varchar(255) default NULL,
  document_category2 varchar(255) default NULL,
  document_author varchar(255) default NULL,
  document_private int(1) default NULL,
  document_path text default NULL,
  document_size int(15) default NULL,
  PRIMARY KEY (document_id)
);

--
-- Table structure for table 'DocumentCategory1'
--
CREATE TABLE DocumentCategory1 (
  documentcategory1_timeupdate timestamp(14) NOT NULL,
  documentcategory1_timecreate timestamp(14) NOT NULL,
  documentcategory1_userupdate int(8) default NULL,
  documentcategory1_usercreate int(8) default NULL,
  documentcategory1_id int(8) NOT NULL auto_increment,
  documentcategory1_label varchar(255) default NULL,
  PRIMARY KEY (documentcategory1_id)
);

--
-- Table structure for table 'DocumentCategory2'
--
CREATE TABLE DocumentCategory2 (
  documentcategory2_timeupdate timestamp(14) NOT NULL,
  documentcategory2_timecreate timestamp(14) NOT NULL,
  documentcategory2_userupdate int(8) default NULL,
  documentcategory2_usercreate int(8) default NULL,
  documentcategory2_id int(8) NOT NULL auto_increment,
  documentcategory2_label varchar(255) default NULL,
  PRIMARY KEY (documentcategory2_id)
);


--
-- Table structure for table 'DocumentMimeType'
--
CREATE TABLE DocumentMimeType (
  documentmimetype_timeupdate timestamp(14) NOT NULL,
  documentmimetype_timecreate timestamp(14) NOT NULL,
  documentmimetype_userupdate int(8) default NULL,
  documentmimetype_usercreate int(8) default NULL,
  documentmimetype_id int(8) NOT NULL auto_increment,
  documentmimetype_label varchar(255) default NULL,
  documentmimetype_extension varchar(10) default NULL,
  documentmimetype_mime varchar(255) default NULL,
  PRIMARY KEY (documentmimetype_id)
);

-------------------------------------------------------------------------------
-- Project module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'ProjectStat'
--
CREATE TABLE ProjectStat (
  projectstat_deal_id int(8) NOT NULL,
  projectstat_date timestamp(14) NOT NULL,
  projectstat_timeupdate timestamp(14) NOT NULL,
  projectstat_timecreate timestamp(14) NOT NULL,
  projectstat_userupdate int(8) default NULL,
  projectstat_usercreate int(8) default NULL,
  projectstat_useddays int(8) default NULL,
  projectstat_remainingdays int(8) default NULL,
  PRIMARY KEY (projectstat_deal_id, projectstat_date)
);

--
-- Table structure for table 'ProjectUser'
--
CREATE TABLE ProjectUser (
  projectuser_deal_id int(8) NOT NULL,
  projectuser_userobm_id int(8) NOT NULL,
  projectuser_timeupdate timestamp(14) NOT NULL,
  projectuser_timecreate timestamp(14) NOT NULL,
  projectuser_userupdate int(8) default NULL,
  projectuser_usercreate int(8) default NULL,
  projectuser_projectedtime int(8) default NULL,
  projectuser_missingtime int(8) default NULL,
  projectuser_validity timestamp(14) default NULL,
  projectuser_soldprice int(8) default NULL,
  projectuser_manager int(1) default NULL,
  PRIMARY KEY (projectuser_deal_id, projectuser_userobm_id)
);

-------------------------------------------------------------------------------
-- List module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'List'
--
CREATE TABLE List (
   list_id int(8) NOT NULL auto_increment,
   list_timeupdate timestamp(14),
   list_timecreate timestamp(14),
   list_userupdate int(8),
   list_usercreate int(8),
   list_name varchar(32) NOT NULL,
   list_subject varchar(70),
   list_email varchar(128),
   PRIMARY KEY (list_id),
   UNIQUE list_name (list_name)
);


--
-- Table structure for table 'ContactList'
--
CREATE TABLE ContactList (
   ContactList_listid int(8) DEFAULT '0' NOT NULL,
   ContactList_contactid int(8) DEFAULT '0' NOT NULL
);


-------------------------------------------------------------------------------
-- Calendar module tables
-------------------------------------------------------------------------------
--
-- Table structure for the table  'CalendarSegment'
--
CREATE TABLE CalendarSegment (
  calendarsegment_eventid int(8)    NOT NULL default '0',
  calendarsegment_customerid int(8) NOT NULL default '0',
  calendarsegment_date varchar(12)  NOT NULL default '',
  calendarsegment_flag varchar(5)   NOT NULL default '',
  calendarsegment_type varchar(5)   NOT NULL default '',
  calendarsegment_state char(1)     NOT NULL default '''',
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
  calendarevent_endrepeat   varchar(12) NOT NULL,
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


CREATE TABLE CalendarRight (
  calendarright_ownerid int(8) NOT NULL default '0',
  calendarright_customerid int(8) NOT NULL default '0',
  calendarright_write int(1) NOT NULL default '0',
  calendarright_read int(1) NOT NULL default '0',
  PRIMARY KEY  (calendarright_ownerid,calendarright_customerid)
);


--
-- structure fot table 'RepeatKind'
--
CREATE TABLE RepeatKind (
  repeatkind_id         int(8) NOT NULL auto_increment,
  repeatkind_timeupdate timestamp(14),
  repeatkind_timecreate timestamp(14),
  repeatkind_userupdate int(8),
  repeatkind_usercreate int(8),
  repeatkind_label      varchar(128),
  PRIMARY KEY(repeatkind_id)	
);


-------------------------------------------------------------------------------
-- Timemanagement tables
-------------------------------------------------------------------------------
--
-- Task table
--
CREATE TABLE Task (
  task_id int(8) NOT NULL auto_increment,
  task_timeupdate timestamp(14) NOT NULL,
  task_timecreate timestamp(14) NOT NULL,
  task_userupdate int(8) default NULL,
  task_usercreate int(8) default NULL,
  task_user_id int(8) default NULL,
  task_date timestamp(14) NOT NULL,
  task_deal_id int(8) default NULL,
  task_length int(2) default NULL,
  task_tasktype_id int(8) default NULL,
  task_label varchar(255) default NULL,
  task_status int(1) default NULL,
  PRIMARY KEY  (task_id)
) TYPE=MyISAM;


--
-- TaskType table
--
CREATE TABLE TaskType (
  tasktype_id int(8) NOT NULL auto_increment,
  tasktype_timeupdate timestamp(14) NOT NULL,
  tasktype_timecreate timestamp(14) NOT NULL,
  tasktype_userupdate int(8) default NULL,
  tasktype_usercreate int(8) default NULL,
  tasktype_internal int(1) NOT NULL,
  tasktype_label varchar(32) default NULL,
  PRIMARY KEY  (tasktype_id)
) TYPE=MyISAM;


-------------------------------------------------------------------------------
-- Project module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'ProjectStat'
--
CREATE TABLE ProjectStat (
  projectstat_deal_id int(8) NOT NULL,
  projectstat_date timestamp(14) NOT NULL,
  projectstat_timeupdate timestamp(14) NOT NULL,
  projectstat_timecreate timestamp(14) NOT NULL,
  projectstat_userupdate int(8) default NULL,
  projectstat_usercreate int(8) default NULL,
  projectstat_useddays int(8) default NULL,
  projectstat_remainingdays int(8) default NULL,
  PRIMARY KEY (projectstat_deal_id, projectstat_date)
);

--
-- Table structure for table 'ProjectUser'
--
CREATE TABLE ProjectUser (
  projectuser_deal_id int(8) NOT NULL,
  projectuser_userobm_id int(8) NOT NULL,
  projectuser_timeupdate timestamp(14) NOT NULL,
  projectuser_timecreate timestamp(14) NOT NULL,
  projectuser_userupdate int(8) default NULL,
  projectuser_usercreate int(8) default NULL,
  projectuser_projectedtime int(8) default NULL,
  projectuser_missingtime int(8) default NULL,
  projectuser_validity timestamp(14) default NULL,
  projectuser_soldprice int(8) default NULL,
  projectuser_manager int(1) default NULL,
  PRIMARY KEY (projectuser_deal_id, projectuser_userobm_id)
);


-------------------------------------------------------------------------------
-- Support tables
-------------------------------------------------------------------------------
--
-- New table 'Contract'
--
CREATE TABLE Contract (
  contract_id int(8) NOT NULL auto_increment,
  contract_timeupdate timestamp(14) NOT NULL,
  contract_timecreate timestamp(14) NOT NULL,
  contract_userupdate int(8) default NULL,
  contract_usercreate int(8) default NULL,
  contract_label varchar(40) default NULL,
  contract_deal_id int(8) default NULL,
  contract_company_id int(8) default NULL,
  contract_number varchar(20) default NULL,
  contract_datebegin date default NULL,
  contract_dateexp date default NULL,
  contract_type_id int(8) default NULL,
  contract_contact1_id int(8) default NULL,
  contract_contact2_id int(8) default NULL,
  contract_techmanager_id int(8) default NULL,
  contract_marketmanager_id int(8) default NULL,
  contract_clause text,
  contract_comment text,
  PRIMARY KEY  (contract_id)
) TYPE=MyISAM;


--
-- New table 'ContractType'
--

CREATE TABLE ContractType (
  contracttype_id int(8) NOT NULL auto_increment,
  contracttype_timeupdate timestamp(14) NOT NULL,
  contracttype_timecreate timestamp(14) NOT NULL,
  contracttype_userupdate int(8) default NULL,
  contracttype_usercreate int(8) default NULL,
  contracttype_label varchar(40) default NULL,
  PRIMARY KEY  (contracttype_id)
) TYPE=MyISAM;


--
-- New table 'Incident'
--
CREATE TABLE Incident (
  incident_id int(8) NOT NULL auto_increment,
  incident_timeupdate timestamp(14) NOT NULL,
  incident_timecreate timestamp(14) NOT NULL,
  incident_userupdate int(8) default NULL,
  incident_usercreate int(8) default NULL,
  incident_contract_id int(8) NOT NULL,
  incident_label varchar(100) default NULL,
  incident_date date default NULL,
  incident_priority_id int(8) default NULL,
  incident_status_id int(8) default NULL,
  incident_logger int(8) default NULL,
  incident_owner int(8) default NULL,
  incident_duration char(4) default '0',
  incident_archive char(1) NOT NULL default '0',
  incident_description text,
  incident_resolution text,
  PRIMARY KEY  (incident_id)
) TYPE=MyISAM;


--
-- New table 'IncidentPriority'
--
CREATE TABLE IncidentPriority (
  incidentpriority_id int(8) NOT NULL auto_increment,
  incidentpriority_timeupdate timestamp(14) NOT NULL,
  incidentpriority_timecreate timestamp(14) NOT NULL,
  incidentpriority_userupdate int(8) default NULL,
  incidentpriority_usercreate int(8) default NULL,
  incidentpriority_order int(2),
  incidentpriority_color char(6),
  incidentpriority_label varchar(32) default NULL,
  PRIMARY KEY (incidentpriority_id)
) TYPE=MyISAM;


--
-- New table 'IncidentStatus'
--
CREATE TABLE IncidentStatus (
  incidentstatus_id int(8) NOT NULL auto_increment,
  incidentstatus_timeupdate timestamp(14) NOT NULL,
  incidentstatus_timecreate timestamp(14) NOT NULL,
  incidentstatus_userupdate int(8) default NULL,
  incidentstatus_usercreate int(8) default NULL,
  incidentstatus_order int(2),
  incidentstatus_label varchar(32) default NULL,
  PRIMARY KEY (incidentstatus_id)
) TYPE=MyISAM;
    

-------------------------------------------------------------------------------
-- Accounting Section tables
-------------------------------------------------------------------------------
--
-- New table 'Invoice'
--
CREATE TABLE Invoice ( 
	invoice_id		int(8) NOT NULL auto_increment,
	invoice_number		varchar(10) DEFAULT '0',
	invoice_label		varchar(40) NOT NULL DEFAULT '',
	invoice_amount_HT	double(10,2),
	invoice_amount_TTC	double(10,2),
	invoice_invoicestatus_id	int(4) DEFAULT '0' NOT NULL,
	invoice_usercreate	int(8),
	invoice_userupdate	int(8),
	invoice_timeupdate	timestamp(14),
	invoice_timecreate	timestamp(14),
	invoice_comment		text,
	invoice_date		date not NULL DEFAULT '0000-00-00' ,
	invoice_inout		char(1),
	invoice_archive		char(1) NOT NULL DEFAULT '0',
	PRIMARY KEY(invoice_id)
);


--
-- New table 'InvoiceStatus'
--
CREATE TABLE InvoiceStatus (
	invoicestatus_id	int (8) NOT NULL auto_increment,
	invoicestatus_label	varchar(10) default '' NOT NULL,
	PRIMARY KEY(invoicestatus_id)
);


--
-- New table 'DealInvoice'
--
CREATE TABLE DealInvoice (
	dealinvoice_deal_id 	int(8) NOT NULL,
	dealinvoice_invoice_id	int(8) NOT NULL,
	dealinvoice_timeupdate	timestamp(14),
	dealinvoice_timecreate	timestamp(14),
	dealinvoice_usercreate	int(8),
	dealinvoice_userupdate 	int(8),
	PRIMARY KEY(dealinvoice_deal_id, dealinvoice_invoice_id)
);


--
-- New table 'Payment'
--
CREATE TABLE  Payment (
	payment_id			int(8) NOT NULL	auto_increment,
	payment_timeupdate		timestamp(14),
	payment_timecreate		timestamp(14),
	payment_usercreate		int(8),
	payment_userupdate 		int(8),
	payment_number			int(10) default null,
	payment_date			date,
	payment_expected_date 		date,		
	payment_amount			double(10,2) DEFAULT '0.0' NOT NULL,
	payment_label			varchar(40) NOT NULL DEFAULT '',
	payment_paymentkind_id 		int(8),
	payment_account_id		int(8),
	payment_comment			text,
	payment_inout			char(1) NOT NULL,
	payment_paid			char(1) NOT NULL DEFAULT '0',
	payment_checked			char(1) NOT NULL DEFAULT '0',
	PRIMARY KEY(payment_id)
);


--
-- New table 'PaymentKind'
--
CREATE TABLE PaymentKind (
	paymentkind_id	 	int(8) NOT NULL auto_increment,
	paymentkind_shortlabel	varchar(3) NOT NULL DEFAULT '',
	paymentkind_longlabel	varchar(40) NOT NULL DEFAULT '',
	PRIMARY KEY(paymentkind_id)
);


--
-- New table 'PaymentInvoice'
--
CREATE TABLE PaymentInvoice (
	paymentinvoice_invoice_id 	int(8) NOT NULL,
	paymentinvoice_payment_id	int(8) NOT NULL,
	paymentinvoice_amount		double (10,2) NOT NULL DEFAULT '0',
	paymentinvoice_timeupdate	timestamp(14),
	paymentinvoice_timecreate	timestamp(14),
	paymentinvoice_usercreate	int(8),
	paymentinvoice_userupdate 	int(8),
	PRIMARY KEY(paymentinvoice_invoice_id,paymentinvoice_payment_id)
);


--
-- New table 'Account'
--
CREATE TABLE Account (
  account_id	     int(8) DEFAULT '0' NOT NULL auto_increment,
  account_bank	     varchar(60) DEFAULT '' NOT NULL,
  account_number     varchar(11) DEFAULT '0' NOT NULL,
  account_balance    double(15,2) DEFAULT '0.00' NOT NULL,
  account_today	     double(15,2) DEFAULT '0.00' NOT NULL,
  account_comment    varchar(100),
  account_label	     varchar(40) NOT NULL DEFAULT '',
  account_timeupdate timestamp(14),
  account_timecreate timestamp(14),
  account_usercreate int(8),
  account_userupdate int(8),
  PRIMARY KEY (account_id)
);


--
-- EntryTemp and PaymentTemp are used when importing data from the bank files
--

--
-- New table 'EntryTemp'
--
CREATE TABLE EntryTemp (
	entrytemp_id		int(8) not null default '0' auto_increment,
	entrytemp_label		varchar(40),
  	entrytemp_amount	double(10,2) not null default '0.00',
	entrytemp_type		varchar(100),
	entrytemp_date		date not null default '0000-00-00',
	entrytemp_realdate	date not null default '0000-00-00',
	entrytemp_comment	varchar(100),
	entrytemp_checked	char(1) not null default '0',
	PRIMARY	KEY (entrytemp_id)
);


-------------------------------------------------------------------------------
--
-- New table 'PaymentTemp'
--
CREATE TABLE  PaymentTemp (
	paymenttemp_id			int(8) NOT NULL	auto_increment,
	paymenttemp_timeupdate		timestamp(14),
	paymenttemp_timecreate		timestamp(14),
	paymenttemp_usercreate		int(8),
	paymenttemp_userupdate 		int(8),
	paymenttemp_number		int(10) default null,
	paymenttemp_date		date,
	paymenttemp_expected_date	date,		
	paymenttemp_amount		double(10,2) DEFAULT '0.0' NOT NULL,
	paymenttemp_label		varchar(40) NOT NULL DEFAULT '',
	paymenttemp_paymentkind_id 	int(8),
	paymenttemp_account_id		int(8),
	paymenttemp_comment		text,
	paymenttemp_inout		char(1) NOT NULL,
	paymenttemp_paid		char(1) NOT NULL DEFAULT '0',
	paymenttemp_checked		char(1) NOT NULL DEFAULT '0',
	PRIMARY KEY(paymenttemp_id)
);


-------------------------------------------------------------------------------
-- Group module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'UGroup' (cause Group is a reserved keyword)
--
CREATE TABLE UGroup (
   group_id int(8) NOT NULL auto_increment,
   group_timeupdate timestamp(14),
   group_timecreate timestamp(14),
   group_userupdate int(8),
   group_usercreate int(8),
   group_name varchar(32) NOT NULL,
   group_desc varchar(128),
   group_email varchar(128),
   PRIMARY KEY (group_id),
   UNIQUE group_name (group_name)
);


--
-- Table structure for table 'UserObmGroup'
--
CREATE TABLE UserObmGroup (
   userobmgroup_groupid int(8) DEFAULT '0' NOT NULL,
   userobmgroup_userobmid int(8) DEFAULT '0' NOT NULL
);


--
-- Table structure for table 'GroupGroup'
--
CREATE TABLE Incident (
  incident_id int(8) NOT NULL auto_increment,
  incident_timeupdate timestamp(14) NOT NULL,
  incident_timecreate timestamp(14) NOT NULL,
  incident_userupdate int(8) default NULL,
  incident_usercreate int(8) default NULL,
  incident_contract_id int(8) NOT NULL,
  incident_label varchar(100) default NULL,
  incident_date date default NULL,
  incident_priority_id int(8) default NULL,
  incident_status_id int(8) default NULL,
  incident_logger int(8) default NULL,
  incident_owner int(8) default NULL,
  incident_duration char(4) default '0',
  incident_archive char(1) NOT NULL default '0',
  incident_description text,
  incident_resolution text,
  PRIMARY KEY  (incident_id)
) TYPE=MyISAM;


--
-- New table 'IncidentPriority'
--
CREATE TABLE IncidentPriority (
  incidentpriority_id int(8) NOT NULL auto_increment,
  incidentpriority_timeupdate timestamp(14) NOT NULL,
  incidentpriority_timecreate timestamp(14) NOT NULL,
  incidentpriority_userupdate int(8) default NULL,
  incidentpriority_usercreate int(8) default NULL,
  incidentpriority_order int(2),
  incidentpriority_color char(6),
  incidentpriority_label varchar(32) default NULL,
  PRIMARY KEY (incidentpriority_id)
) TYPE=MyISAM;


--
-- New table 'IncidentStatus'
--
CREATE TABLE IncidentStatus (
  incidentstatus_id int(8) NOT NULL auto_increment,
  incidentstatus_timeupdate timestamp(14) NOT NULL,
  incidentstatus_timecreate timestamp(14) NOT NULL,
  incidentstatus_userupdate int(8) default NULL,
  incidentstatus_usercreate int(8) default NULL,
  incidentstatus_order int(2),
  incidentstatus_label varchar(32) default NULL,
  PRIMARY KEY (incidentstatus_id)
) TYPE=MyISAM;
    

-------------------------------------------------------------------------------
-- Timemanagement tables
-------------------------------------------------------------------------------
--
-- Task table
--
CREATE TABLE Task (
  task_id int(8) NOT NULL auto_increment,
  task_timeupdate timestamp(14) NOT NULL,
  task_timecreate timestamp(14) NOT NULL,
  task_userupdate int(8) default NULL,
  task_usercreate int(8) default NULL,
  task_user_id int(8) default NULL,
  task_date timestamp(14) NOT NULL,
  task_deal_id int(8) default NULL,
  task_length int(2) default NULL,
  task_tasktype_id int(8) default NULL,
  task_label varchar(255) default NULL,
  task_status int(1) default NULL,
  PRIMARY KEY  (task_id)
) TYPE=MyISAM;


--
-- TaskType table
--
CREATE TABLE TaskType (
  tasktype_id int(8) NOT NULL auto_increment,
  tasktype_timeupdate timestamp(14) NOT NULL,
  tasktype_timecreate timestamp(14) NOT NULL,
  tasktype_userupdate int(8) default NULL,
  tasktype_usercreate int(8) default NULL,
  tasktype_internal int(1) NOT NULL,
  tasktype_label varchar(32) default NULL,
  PRIMARY KEY  (tasktype_id)
) TYPE=MyISAM;


CREATE TABLE GroupGroup (
   groupgroup_parentid int(8) DEFAULT '0' NOT NULL,
   groupgroup_childid int(8) DEFAULT '0' NOT NULL
);
