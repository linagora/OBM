--/////////////////////////////////////////////////////////////////////////////
--// OBM - File : create_obmdb_0.5.mysql.sql                                 //
--//     - Desc : MySQL Database 0.5 creation script                         //
--// 2001-07-27 ALIACOM                                                      //
--/////////////////////////////////////////////////////////////////////////////
-- $Id$ //
--/////////////////////////////////////////////////////////////////////////////

-------------------------------------------------------------------------------
-- User, Preferences tables
-- 1 user (uadmin/padmin) is created with admin rights
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
  userobm_email             varchar(60),
  userobm_timelastaccess    timestamp(14),
  userobm_contact_id        int(8),
  PRIMARY KEY (userobm_id),
  UNIQUE k_login_user (userobm_login)
);

-- Dumping data for table 'UserObm'
INSERT INTO UserObm (userobm_login, userobm_password,userobm_perms, userobm_lastname, userobm_firstname, userobm_contact_id) VALUES ('uadmin','padmin','admin', 'Admin Lastname', 'Admin Firstname','1');
INSERT INTO UserObm (userobm_login,userobm_password,userobm_perms) VALUES ('ueditor','peditor','editor');
INSERT INTO UserObm (userobm_login,userobm_password,userobm_perms) VALUES ('uuser','puser','user');


--
-- Table structure for table 'UserObmPref'
--
CREATE TABLE UserObmPref (
   userobmpref_user_id int(8) DEFAULT '0' NOT NULL,
   userobmpref_option varchar(50) NOT NULL,
   userobmpref_value varchar(50) NOT NULL
);


INSERT INTO UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (1,'set_theme','aliacom');
INSERT INTO UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (1,'set_lang','en');
INSERT INTO UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (1,'set_rows',10);
INSERT INTO UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (1,'set_display','no');
INSERT INTO UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (1,'set_debug',0);
INSERT INTO UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (1,'last_company',0); 
INSERT INTO UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (1,'last_deal',0); 
INSERT INTO UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (1,'last_contact',0); 
INSERT INTO UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (1,'last_contract', '0');
INSERT INTO UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (1,'last_incident', '0');
INSERT INTO UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (1,'last_account',0); 
INSERT INTO UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (1,'last_invoice', '0');
INSERT INTO UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (1,'last_payment', '0');
INSERT INTO UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (1,'order_contactlist','list_contact_lastname');
INSERT INTO UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (1,'order_servicecomputer','service_port');
INSERT INTO UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (1,'set_day_weekstart','monday');


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
-- uadmin (user1) uservalues for the table 'DisplayPref'
--

-- module 'contact'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'contact','contact_lastname',1,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'contact','contact_firstname',2,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'contact','contact_function',3,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'contact','contact_company_name',4,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'contact','contact_phone',5,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'contact','contact_homephone',6,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'contact','contact_mobilephone',7,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'contact','contact_email',8,1);

-- module 'company'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'company','company_name',1,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'company','company_contacts',2,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'company','company_new_contact',3,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'company','companytype_label',4,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'company','company_address1',5,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'company','company_phone',6,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'company','company_fax',7,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'company','company_email',8,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'company','company_web',9,1);

-- module 'deal'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'deal','deal_label',1,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'deal','deal_company_name',2,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'deal','dealtype_label',3,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'deal','deal_tasktype_label',4,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'deal','dealstatus_label',5,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'deal','deal_todo',6,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'deal','deal_datealarm',7,2);

-- module 'parentdeal'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'parentdeal','parentdeal_label',1,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'parentdeal','parentdeal_marketing_lastname',2,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'parentdeal','parentdeal_technical_lastname',3,1);

-- module 'list'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'list', 'list_name', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'list', 'list_subject', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'list', 'list_nb_contact', 3, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'list', 'usercreate', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'list', 'timecreate', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'list', 'userupdate', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'list', 'timeupdate', 7, 1);

-- module 'list_contact'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'list_contact', 'list_contact_lastname', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'list_contact', 'list_contact_firstname', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'list_contact', 'list_contact_function', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'list_contact', 'list_contact_company', 4, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'list_contact', 'list_contact_town', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'list_contact', 'list_contact_phone', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'list_contact', 'list_contact_mobilephone', 7, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'list_contact', 'list_contact_email', 8, 1);

-- module 'computer'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'computer','computer_ip', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'computer','computer_user', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'computer','computer_usercreate', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'computer','computer_timecreate', 4, 1);   
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'computer','computer_userupdate', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'computer','computer_timeupdate', 6, 1);       
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'computer','computer_auth_scan', 7, 1); 
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'computer','computer_date_lastscan', 8, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'computer','computer_comments', 9,1);      
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'computer','service_name', 1, 2);    
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'computer','service_port', 2, 2);      
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'computer','service_proto', 3, 2);        
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'computer','service_desc', 4, 1);  
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'computer','service_status', 5, 2);      
-- module 'account'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'account','account_label', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'account','account_bank', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'account','account_number', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'account','account_today', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'account','account_balance', 5, 1);

-- module 'invoice'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'invoice','invoice_number', 1, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'invoice','invoice_date', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'invoice','invoice_label', 3, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'invoice','invoice_amount_HT', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'invoice','invoice_amount_TTC', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'invoice','invoice_status', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'invoice','invoice_company', 7, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'invoice','invoice_deal', 8, 1);

-- module 'payment'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'payment','payment_label', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'payment','payment_number', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'payment','payment_amount', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'payment','payment_expect_date', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'payment','payment_date', 5, 1);
-- sub module 'entrytemp'
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'entrytemp','entrytemp_date',1,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'entrytemp','entrytemp_type',2,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'entrytemp','entrytemp_amount',3,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'entrytemp','entrytemp_realdate',4,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'entrytemp','entrytemp_label',5,2);

--modules timemanager
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'time','task_date',1,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'time','task_company_name',2,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'time','task_deal_label',3,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'time','task_label',4,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'time','tasktype_label',5,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'time','task_length',6,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'time','task_id',7,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'time_day','task_date',1,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'time_day','task_totallength',2,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'time_deal','task_totallength',4,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'time_deal','task_deal_label',3,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'time_deal','task_company_name',2,1);

--module 'contract'
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'contract', 'contract_label', 1, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'contract', 'contract_numero', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'contract', 'contract_company_name', 3, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'contract', 'contracttype_label', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'contract', 'contract_expiration', 5, 1);

--module 'incident'
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'incident', 'incident_label', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'incident', 'incident_company_name', 2, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'incident', 'incident_priority', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'incident', 'incident_etat', 6, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'incident', 'incident_date', 7, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'incident', 'incident_owner_lastname', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'incident', 'incident_logger_lastname', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'incident', 'timeupdate', 8, 1);

-------------------------------------------------------------------------------
-- Company module tables
-------------------------------------------------------------------------------
---- 
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
-- Table structure for table 'Company'
--
CREATE TABLE Company (
  company_id int(8) DEFAULT '0' NOT NULL auto_increment,
  company_timeupdate timestamp(14),
  company_timecreate timestamp(14),
  company_userupdate int(8),
  company_usercreate int(8),
  company_number varchar(32),
  company_state int(2) DEFAULT '0',
  company_name varchar(50) DEFAULT '' NOT NULL,
  company_type_id int(8),
  company_address1 varchar(30),
  company_address2 varchar(30),
  company_zipcode varchar(14),
  company_town varchar(24),
  company_expresspostal varchar(8),
  company_country varchar(24),
  company_phone varchar(16),
  company_fax varchar(16),
  company_web varchar(52),
  company_email varchar(52),
  company_contact_number int(5) DEFAULT '0' NOT NULL,
  company_deal_number int(5) DEFAULT '0' NOT NULL,
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
  contact_comment text,
  contact_visibility int(2) DEFAULT '0',
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
  deal_status_id int(2),
  deal_datealarm date,
  deal_comment text,
  deal_archive char(1) DEFAULT '0',
  deal_todo varchar(128),
  deal_visibility int(2) DEFAULT '0',
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
-- Table structure for the table  'EventUser'
--
CREATE TABLE EventUser (
       eventuser_event_id	int(8) NOT NULL,
       eventuser_contact_id	int(8),
       eventuser_group_id	int(8),
       eventuser_state		char(1)
);


--
-- Table structure for the table  'CalendarLayer'
--
CREATE TABLE CalendarLayer (
       calendarlayer_contact_id	int(8),
       calendarlayer_group_id	int(8),
       calendarlayer_color	char(7)
);


--
-- Table structure for the table  'CalendarEvent'
--
CREATE TABLE CalendarEvent (
       calendarevent_id			int(8) NOT NULL auto_increment,
       calendarevent_timeupdate		timestamp(14),
       calendarevent_timecreate		timestamp(14),
       calendarevent_userupdate		int(8),
       calendarevent_usercreate		int(8),
       calendarevent_origin_id		int(8),
       calendarevent_title		varchar(255),
       calendarevent_description	text,
       calendarevent_category_id	int(8),
       calendarevent_priority		int(2),
       calendarevent_privacy		int(2),
       calendarevent_datebegin		timestamp(14),
       calendarevent_dateend		timestamp(14),
       calendarevent_occupied_day	int(2),
       calendarevent_repeatkind		varchar(20),
       calendarevent_repeat_interval	int(2),
       calendarevent_repeatdays		char(7),
       calendarevent_endrepeat		timestamp(14),
       PRIMARY KEY(calendarevent_id)
);


--
-- Table structure for the table  'EventCategory'
--
CREATE TABLE EventCategory (
       eventcategory_id			int(8) NOT NULL auto_increment,
       eventcategory_timeupdate		timestamp(14),
       eventcategory_timecreate		timestamp(14),
       eventcategory_userupdate		int(8),
       eventcategory_usercreate		int(8),
       eventcategory_label		varchar(128),
       PRIMARY KEY(eventcategory_id)    
);


--
-- structure fot table 'RepeatKind'
--
CREATE TABLE RepeatKind (
       repeatkind_id			int(8) NOT NULL auto_increment,
       repeatkind_timeupdate		timestamp(14),
       repeatkind_timecreate		timestamp(14),
       repeatkind_userupdate		int(8),
       repeatkind_usercreate		int(8),
       repeatkind_label			varchar(128),
       PRIMARY KEY(repeatkind_id)	
);


-------------------------------------------------------------------------------
-- Computer module tables
-------------------------------------------------------------------------------
--
-- Table structure for table 'Computer'
--
CREATE TABLE Computer (
   computer_id int(8) NOT NULL auto_increment,
   computer_timeupdate timestamp(14),
   computer_timecreate timestamp(14),
   computer_userupdate int(8),
   computer_usercreate int(8),
   computer_name varchar(32) NOT NULL,
   computer_domain varchar(70),
   computer_ip varchar(19),
   computer_user varchar(50) NOT NULL,
   computer_comments text NOT NULL,
   computer_date_lastscan timestamp(14),
   computer_auth_scan tinyint(2) DEFAULT '0' NOT NULL,
   PRIMARY KEY (computer_id)
);

--
-- Table structure for table 'ServiceComputer'
--
CREATE TABLE ServiceComputer (
   service_computer_id int(8),
   service_name varchar(30),
   service_port smallint(5),
   service_proto varchar(5),
   service_desc varchar(70),
   service_status tinyint(3)
);


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
  contract_company_id int(8) default NULL,
  contract_numero varchar(20) default NULL,
  contract_clause text,
  contract_debut date default NULL,
  contract_expiration date default NULL,
  contract_type_id int(8) default NULL,
  contract_comment text,
  contract_responsable_client_id int(8) default NULL,
  contract_responsable_client2_id int(8) default NULL,
  contract_responsable_tech_id int(8) default NULL,
  contract_responsable_com_id int(8) default NULL,
  contract_typedeal int(8) default NULL,
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
  incident_contract_id int(8) default NULL,
  incident_timeupdate timestamp(14) NOT NULL,
  incident_timecreate timestamp(14) NOT NULL,
  incident_userupdate int(8) default NULL,
  incident_usercreate int(8) default NULL,
  incident_label varchar(100) default NULL,
  incident_date date default NULL,
  incident_description text,
  incident_priority enum('REDHOT','HOT','NORMAL','LOW') default NULL,
  incident_etat enum('OPEN','CALL','WAITCALL','PAUSED','CLOSED') default NULL,
  incident_logger int(8) default NULL,
  incident_owner int(8) default NULL,
  incident_resolution text,
  incident_archive char(1) NOT NULL default '0',
  PRIMARY KEY  (incident_id)
) TYPE=MyISAM;

    

--
-- Timemanagement module
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

#
# Table structure for table `GlobalPref`
#

CREATE TABLE GlobalPref (
  globalpref_option varchar(255) NOT NULL default '',
  globalpref_value varchar(255) NOT NULL default '',
  PRIMARY KEY  (globalpref_option),
  UNIQUE KEY globalpref_option (globalpref_option)
) TYPE=MyISAM;


