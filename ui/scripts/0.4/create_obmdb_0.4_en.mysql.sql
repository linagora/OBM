#//////////////////////////////////////////////////////////////////////////////
#// OBM - File  : create_obm_db.mysql.sql                                 //
#//     - Desc  : English MySQL Database creation script                      //
#// 1999-03-19 ALIACOM - Last Update : 2001-06-07                            //
#//////////////////////////////////////////////////////////////////////////////

# Table structure for table 'ActiveSessions'
CREATE TABLE ActiveSessions (
  sid				varchar(32) DEFAULT '' NOT NULL,
  name				varchar(32) DEFAULT '' NOT NULL,
  val				text,
  changed			varchar(14) DEFAULT '' NOT NULL,
  PRIMARY KEY (name,sid),
  KEY changed (changed)
);

# Table structure for table 'auth_user'
CREATE TABLE UserObm (
  userobm_id			int(8) DEFAULT '0' NOT NULL auto_increment,
  userobm_timeupdate		timestamp(14),
  userobm_timecreate		timestamp(14),
  userobm_userupdate		int(8),
  userobm_usercreate		int(8),
  userobm_username		varchar(32) DEFAULT '' NOT NULL,
  userobm_password		varchar(32) DEFAULT '' NOT NULL,
  userobm_perms			varchar(254),
  userobm_email			varchar(60),
  userobm_timelastaccess	timestamp(14),
  userobm_contact_id		int(8),
  PRIMARY KEY (userobm_id),
  UNIQUE k_username_user (userobm_username)
);

# Dumping data for table 'User'
INSERT INTO UserObm (userobm_username,userobm_password,userobm_perms) VALUES ('uadmin','padmin','admin');


#
# Table structure for table 'UserObmPref'
#

CREATE TABLE UserObmPref (
   userobmpref_id int(8) DEFAULT '0' NOT NULL,
   userobmpref_option varchar(50) NOT NULL,
   userobmpref_choice varchar(50) NOT NULL
);




#
# Table structure for table 'ParentDeal'
#

CREATE TABLE ParentDeal (
   parentdeal_id int(8) NOT NULL auto_increment,
   parentdeal_timeupdate timestamp(14),
   parentdeal_timecreate timestamp(14),
   parentdeal_userupdate int(8),
   parentdeal_usercreate int(8),
   parentdeal_label varchar(128) NOT NULL,
   parentdeal_marketingmanager_id int(8),
   parentdeal_technicalmanager_id int(8),
   parentdeal_archive int(2) DEFAULT '0',
   parentdeal_comment text,
   PRIMARY KEY (parentdeal_id)
);



#
# Table structure for table 'Deal'
#
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
  deal_category_id int(8),
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
  deal_archive int(2) DEFAULT '0',
  deal_todo varchar(128),
  deal_visibility int(2) DEFAULT '0',
  PRIMARY KEY (deal_id)
);

#
# Table structure for table 'DealCategory'
#
CREATE TABLE DealCategory (
  dealcategory_id int(8) DEFAULT '0' NOT NULL auto_increment,
  dealcategory_timeupdate timestamp(14),
  dealcategory_timecreate timestamp(14),
  dealcategory_userupdate int(8),
  dealcategory_usercreate int(8),
  dealcategory_minilabel char(12),
  dealcategory_label char(30),
  PRIMARY KEY (dealcategory_id)
);

#
# Dumping data for table 'DealCategory'
#
INSERT INTO DealCategory (dealcategory_timeupdate, dealcategory_timecreate, dealcategory_userupdate, dealcategory_usercreate, dealcategory_minilabel, dealcategory_label) VALUES (null,null,2,null,'tech. sup.','Technical support');
INSERT INTO DealCategory (dealcategory_timeupdate, dealcategory_timecreate, dealcategory_userupdate, dealcategory_usercreate, dealcategory_minilabel, dealcategory_label) VALUES (null,null,2,null,'Products','Products');
INSERT INTO DealCategory (dealcategory_timeupdate, dealcategory_timecreate, dealcategory_userupdate, dealcategory_usercreate, dealcategory_minilabel, dealcategory_label) VALUES (null,null,2,null,'Hardware','Hardware');
INSERT INTO DealCategory (dealcategory_timeupdate, dealcategory_timecreate, dealcategory_userupdate, dealcategory_usercreate, dealcategory_minilabel, dealcategory_label) VALUES (null,null,2,null,'consulting','Consulting');


#
# Table structure for table 'Contact'
#
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
  contact_address1 varchar(30),
  contact_address2 varchar(30),
  contact_zipcode varchar(14),
  contact_town varchar(24),
  contact_expresspostal varchar(8),
  contact_country varchar(24),
  contact_function varchar(32),
  contact_phone varchar(16),
  contact_homephone varchar(16),
  contact_mobilephone varchar(16),
  contact_fax varchar(16),
  contact_email varchar(52),
  contact_comment text,
  contact_visibility int(2) DEFAULT '0',
  PRIMARY KEY (contact_id)
);


#
# Table structure for table 'DealStatus'
#
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


#
# Dumping data for table 'DealStatus'
#
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label,dealstatus_order) VALUES (null,null,2,null, 'CONTACT',1);
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label,dealstatus_order) VALUES (null,null,2,null, 'Waiting for Proposal',2);
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label,dealstatus_order) VALUES (null,null,2,null, 'PROPOSITION',3);
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label,dealstatus_order) VALUES (null,null,2,null, 'SIGNED',4);
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label,dealstatus_order) VALUES (null,null,2,null, 'LOST',5);
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label,dealstatus_order) VALUES (null,null,2,null,'DONE',6);
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label,dealstatus_order) VALUES (null,null,2,null,'INVOICE',7);
INSERT INTO DealStatus (dealstatus_timeupdate, dealstatus_timecreate, dealstatus_userupdate, dealstatus_usercreate, dealstatus_label,dealstatus_order) VALUES (null,null,2,null,'CLOSED',8);


#
# Table structure for table 'Kind'
#
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

#
# Dumping data for table 'Kind'
#
INSERT INTO Kind (kind_timeupdate, kind_timecreate, kind_userupdate, kind_usercreate, kind_minilabel, kind_label) VALUES (null,null,2,null,'Mr','Mister');
INSERT INTO Kind (kind_timeupdate, kind_timecreate, kind_userupdate, kind_usercreate, kind_minilabel, kind_label) VALUES (null,null,2,null,'Mrs','Madam');
INSERT INTO Kind (kind_timeupdate, kind_timecreate, kind_userupdate, kind_usercreate, kind_minilabel, kind_label) VALUES (null,null,2,null,'Miss','Miss');



#
# Table structure for table 'Company'
#
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
  company_mailing int(2),
  company_comment text,
  PRIMARY KEY (company_id)
);

#
# Dumping data for table 'Company'
#
INSERT INTO Company (company_timeupdate, company_timecreate, company_userupdate, company_usercreate, company_number,company_state,company_name, company_type_id, company_address1, company_address2, company_zipcode, company_town, company_expresspostal, company_country, company_phone, company_fax, company_web, company_email, company_mailing, company_comment) VALUES (null,null,2,0,'MyNumber123',1,'MyCOMPANY',3,'my adress l1','my adress l2','31520','MyTOWN','','MyCountry','00 11 22 33 44','44 33 22 11 00','www.myweb.fr','info@mydomain.fr',0,NULL);


#
# Table structure for table 'DealType'
#
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

#
# Dumping data for table 'DealType'
#
INSERT INTO DealType (dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES (null,null,2,null,'SALE','+');
INSERT INTO DealType (dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES (null,null,2,null,'PURCHASE','-');
INSERT INTO DealType (dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES (null,null,2,null,'MEDIA','-');
INSERT INTO DealType (dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES (null,null,2,null,'SOCIAL','-');
INSERT INTO DealType (dealtype_timeupdate, dealtype_timecreate, dealtype_userupdate, dealtype_usercreate, dealtype_label,dealtype_inout) VALUES (null,null,2,null,'JURIDICAL','-');


# 
# Table structure for table 'CompanyType'
#
CREATE TABLE CompanyType (
  companytype_id int(8) DEFAULT '0' NOT NULL auto_increment,
  companytype_timeupdate timestamp(14),
  companytype_timecreate timestamp(14),
  companytype_userupdate int(8),
  companytype_usercreate int(8),
  companytype_label char(12),
  PRIMARY KEY (companytype_id)
);


#
# Dumping data for table 'CompanyType'
#
INSERT INTO CompanyType (companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES (null,null,2,null,'Customer');
INSERT INTO CompanyType (companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES (null,null,2,null,'Supplier');
INSERT INTO CompanyType (companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES (null,null,2,null,'Partner');
INSERT INTO CompanyType (companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES (null,null,2,null,'Prospect');
INSERT INTO CompanyType (companytype_timeupdate, companytype_timecreate, companytype_userupdate, companytype_usercreate, companytype_label) VALUES (null,null,2,null,'Media');



#
# Table structure for table 'List'
#
CREATE TABLE List (
   list_id int(8) NOT NULL auto_increment,
   list_timeupdate timestamp(14),
   list_timecreate timestamp(14),
   list_userupdate int(8),
   list_usercreate int(8),
   list_name varchar(32) NOT NULL,
   list_subject varchar(70),
   list_auth_usermail tinyint(2) DEFAULT '0' NOT NULL,
   PRIMARY KEY (list_id),
   UNIQUE list_name (list_name)
);

#
# Table structure for table 'ListDisplay'
#
CREATE TABLE ListDisplay (
   display_user_id int(8) DEFAULT '0' NOT NULL,
   display_fieldname varchar(40) NOT NULL,
   display_fieldorder tinyint(3) unsigned,
   display_display tinyint(1) unsigned DEFAULT '1' NOT NULL
);


#
# Table structure for table 'ContactList'
#

CREATE TABLE ContactList (
   ContactList_listid int(8) DEFAULT '0' NOT NULL,
   ContactList_contactid int(8) DEFAULT '0' NOT NULL
);





#
# Table structure for table 'Computer'
#
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

#
# Table structure for table 'ServiceComputer'
#
CREATE TABLE ServiceComputer (
   service_computer_id int(8),
   service_name varchar(30),
   service_port smallint(5),
   service_proto varchar(5),
   service_desc varchar(70),
   service_status tinyint(3)
);


#
# Table structure for table 'ComputerDisplay'
#
CREATE TABLE ComputerDisplay (
   display_user_id int(8) DEFAULT '0' NOT NULL,
   display_fieldname varchar(40) NOT NULL,
   display_fieldorder tinyint(3) unsigned,
   display_display tinyint(1) unsigned DEFAULT '1' NOT NULL
);

#
# Table structure for table 'CompanyDisplay'
#
CREATE TABLE CompanyDisplay (
   display_user_id int(8) DEFAULT '0' NOT NULL,
   display_fieldname varchar(40) NOT NULL,
   display_fieldorder tinyint(3) unsigned,
   display_display tinyint(1) unsigned DEFAULT '1' NOT NULL
);


#
# Table structure for table 'ParentDealDisplay'
#

CREATE TABLE ParentDealDisplay (
   display_user_id int(8) DEFAULT '0' NOT NULL,
   display_fieldname varchar(40) NOT NULL,
   display_fieldorder tinyint(3) unsigned,
   display_display tinyint(1) unsigned DEFAULT '1' NOT NULL
);


#
# Table structure for table 'DealDisplay'
#

CREATE TABLE DealDisplay (
   display_user_id int(8) DEFAULT '0' NOT NULL,
   display_fieldname varchar(40) NOT NULL,
   display_fieldorder tinyint(3) unsigned,
   display_display tinyint(1) unsigned DEFAULT '1' NOT NULL
);

#
# Table structure for table 'ContactDisplay'
#

CREATE TABLE ContactDisplay (
   display_user_id int(8) DEFAULT '0' NOT NULL,
   display_fieldname varchar(40) NOT NULL,
   display_fieldorder tinyint(3) unsigned,
   display_display tinyint(1) unsigned DEFAULT '1' NOT NULL
);



#----------------------------------------------------------
# Preferences profile for the user uadmin : 
#----------------------------------------------------------


#----------------------------------------------------------
#
# Dump for the table 'UserObmPref'
#

INSERT INTO UserObmPref (userobmpref_id,userobmpref_option,userobmpref_choice) VALUES (1,"set_theme","standard");
INSERT INTO UserObmPref (userobmpref_id,userobmpref_option,userobmpref_choice) VALUES (1,"set_lang","en");
INSERT INTO UserObmPref (userobmpref_id,userobmpref_option,userobmpref_choice) VALUES (1,"set_rows",10);
INSERT INTO UserObmPref (userobmpref_id,userobmpref_option,userobmpref_choice) VALUES (1,"set_display","no");
INSERT INTO UserObmPref (userobmpref_id,userobmpref_option,userobmpref_choice) VALUES (1,"set_debug",0);
INSERT INTO UserObmPref (userobmpref_id,userobmpref_option,userobmpref_choice) VALUES (1,"last_company",0); 
INSERT INTO UserObmPref (userobmpref_id,userobmpref_option,userobmpref_choice) VALUES (1,"last_deal",0); 
INSERT INTO UserObmPref (userobmpref_id,userobmpref_option,userobmpref_choice) VALUES (1,"last_contact",0); 
INSERT INTO UserObmPref (userobmpref_id,userobmpref_option,userobmpref_choice) VALUES (1,"order_contactlist","list_contact_lastname"); 
INSERT INTO UserObmPref (userobmpref_id,userobmpref_option,userobmpref_choice) VALUES (1,"order_servicecomputer","service_port");


#----------------------------------------------------------
#
# Dump for the table 'ParentDealDisplay'
#

INSERT INTO ParentDealDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) values (1,"parentdeal_label",1,2);
INSERT INTO ParentDealDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) values (1,"parentdeal_marketing_lastname",2,1);
INSERT INTO ParentDealDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) values (1,"parentdeal_technical_lastname",3,1);



#--------------------------------------------------------------------------
#
# Dump for the table "DealDisplay"
#

INSERT INTO DealDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) values (1,"deal_label",1,2);
INSERT INTO DealDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) values (1,"deal_company_name",2,2);
INSERT INTO DealDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) values (1,"dealtype_label",3,1);
INSERT INTO DealDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) values (1,"dealcategory_minilabel",4,1);
INSERT INTO DealDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) values (1,"dealstatus_label",5,1);
INSERT INTO DealDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) values (1,"deal_todo",6,1);
INSERT INTO DealDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) values (1,"deal_datealarm",7,2);




#--------------------------------------------------------------------------
#
# Dump for the table "CompanyDisplay"
#

INSERT INTO CompanyDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) values (1,"company_name",1,2);
INSERT INTO CompanyDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) values (1,"company_contacts",2,1);
INSERT INTO CompanyDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) values (1,"company_new_contact",3,1);
INSERT INTO CompanyDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) values (1,"companytype_label",4,1);
INSERT INTO CompanyDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) values (1,"company_address1",5,1);
INSERT INTO CompanyDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) values (1,"company_phone",6,1);
INSERT INTO CompanyDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) values (1,"company_fax",7,1);
INSERT INTO CompanyDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) values (1,"company_email",8,1);
INSERT INTO CompanyDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) values (1,"company_web",9,1);





#--------------------------------------------------------------------------
#
# Dump for the table "ContactDisplay"
#

INSERT INTO ContactDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) values (1,"contact_lastname",1,2);
INSERT INTO ContactDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) values (1,"contact_firstname",2,1);
INSERT INTO ContactDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) values (1,"contact_function",3,1);
INSERT INTO ContactDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) values (1,"contact_company_name",4,2);
INSERT INTO ContactDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) values (1,"contact_phone",5,1);
INSERT INTO ContactDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) values (1,"contact_homephone",6,1);
INSERT INTO ContactDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) values (1,"contact_mobilephone",7,1);
INSERT INTO ContactDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) values (1,"contact_email",8,1);


#--------------------------------------------------------------------------
#
# Dump for th table "ListDisplay"
#

INSERT INTO ListDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) VALUES ( 1, 'list_subject', '1', '2');
INSERT INTO ListDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) VALUES ( 1, 'list_nb_contact', '2', '2');
INSERT INTO ListDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) VALUES ( 1, 'list_usercreate', '3', '1');
INSERT INTO ListDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) VALUES ( 1, 'list_timecreate', '4', '1');
INSERT INTO ListDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) VALUES ( 1, 'list_userupdate', '5', '1');
INSERT INTO ListDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) VALUES ( 1, 'list_timeupdate', '6', '1');
INSERT INTO ListDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) VALUES ( 1, 'list_contact_lastname', '1', '2');
INSERT INTO ListDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) VALUES ( 1, 'list_contact_firstname', '2', '1');
INSERT INTO ListDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) VALUES ( 1, 'list_contact_function', '3', '1');
INSERT INTO ListDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) VALUES ( 1, 'list_contact_company_id', '4', '2');
INSERT INTO ListDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) VALUES ( 1, 'list_contact_town', '5', '1');
INSERT INTO ListDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) VALUES ( 1, 'list_contact_phone', '6', '1');
INSERT INTO ListDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) VALUES ( 1, 'list_contact_mobilephone', '7', '1');
INSERT INTO ListDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) VALUES ( 1, 'list_contact_email', '8', '1');



#--------------------------------------------------------------------------
#
# Dump for table "ComputerDisplay"
#

INSERT INTO ComputerDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) VALUES ( 1,'computer_ip', '1', '2');
INSERT INTO ComputerDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) VALUES ( 1,'computer_user', '2', '1');
INSERT INTO ComputerDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) VALUES ( 1,'computer_usercreate', '3', '1');
INSERT INTO ComputerDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) VALUES ( 1,'computer_timecreate', '4', '1');   
INSERT INTO ComputerDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) VALUES ( 1,'computer_userupdate', '5', '1');
INSERT INTO ComputerDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) VALUES ( 1,'computer_timeupdate', '6', '1');       
INSERT INTO ComputerDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) VALUES ( 1,'computer_auth_scan', '7', '1'); 
INSERT INTO ComputerDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) VALUES ( 1,'computer_date_lastscan', '8', '1');
INSERT INTO ComputerDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) VALUES ( 1,'computer_comments', '9', '1');      
INSERT INTO ComputerDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) VALUES ( 1,'service_name', '1', '2');    
INSERT INTO ComputerDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) VALUES ( 1,'service_port', '2', '2');      
INSERT INTO ComputerDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) VALUES ( 1,'service_proto', '3', '2');        
INSERT INTO ComputerDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) VALUES ( 1,'service_desc', '4', '1');  
INSERT INTO ComputerDisplay (display_user_id,display_fieldname,display_fieldorder,display_display) VALUES ( 1,'service_status', '5', '2');      
   