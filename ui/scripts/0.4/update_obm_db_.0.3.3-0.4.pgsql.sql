-- //////////////////////////////////////////////////////
-- // Update OBM PostgreSql Database from 0.3.3 to 0.4 //
-- //////////////////////////////////////////////////////
-- // 2001-06-08    Francois Bloque                    //
-- //////////////////////////////////////////////////////



--
-- The table 'active_sessions' is now called 'ActiveSessions'
--
ALTER TABLE active_sessions RENAME TO ActiveSessions;



--
-- Table 'DealOrigin' is no longer used
--
DROP TABLE DealOrigin;


--
-- Table 'ListDisplay' have to be empty
--
DELETE FROM ListDisplay;


--
-- Table structure for table 'UserObmPref'
--
CREATE TABLE UserObmPref (
   userobmpref_id int8 DEFAULT '0' NOT NULL,
   userobmpref_option varchar(50) NOT NULL,
   userobmpref_choice varchar(50) NOT NULL
);


--
-- Table structure for table 'ParentDeal'
--
CREATE TABLE ParentDeal (
   parentdeal_id SERIAL,
   parentdeal_timeupdate datetime,
   parentdeal_timecreate datetime,
   parentdeal_userupdate int8,
   parentdeal_usercreate int8,
   parentdeal_label varchar(128) NOT NULL,
   parentdeal_marketingmanager_id int8,
   parentdeal_technicalmanager_id int8,
   parentdeal_archive int2 DEFAULT '0',
   parentdeal_comment text,
   PRIMARY KEY (parentdeal_id)
);


--
-- Table structure for table 'DealDisplay'
--
CREATE TABLE DealDisplay (
   display_user_id int8 DEFAULT '0' NOT NULL,
   display_fieldname varchar(40) NOT NULL,
   display_fieldorder int4,
   display_display int2 DEFAULT '1' NOT NULL
);



--
-- Table structure for table 'ParentDealDisplay'
--
CREATE TABLE ParentDealDisplay (
   display_user_id int8 DEFAULT '0' NOT NULL,
   display_fieldname varchar(40) NOT NULL,
   display_fieldorder int4,
   display_display int2 DEFAULT '1' NOT NULL
);



--
-- Table structure for table 'CompanyDisplay'
--
CREATE TABLE CompanyDisplay (
   display_user_id int8 DEFAULT '0' NOT NULL,
   display_fieldname varchar(40) NOT NULL,
   display_fieldorder int4,
   display_display int2 DEFAULT '1' NOT NULL
);



--
-- Table structure for table 'ContactDisplay'
--
CREATE TABLE ContactDisplay (
   display_user_id int8 DEFAULT '0' NOT NULL,
   display_fieldname varchar(40) NOT NULL,
   display_fieldorder int4,
   display_display int2 DEFAULT '1' NOT NULL
);


--
-- Table structure for table 'Computer'
--
CREATE TABLE Computer (
   computer_id SERIAL,
   computer_timeupdate datetime,
   computer_timecreate datetime,
   computer_userupdate int8,
   computer_usercreate int8,
   computer_name varchar(32) NOT NULL,
   computer_domain varchar(70),
   computer_ip varchar(19),
   computer_user varchar(50) NOT NULL,
   computer_comments text NOT NULL,
   computer_date_lastscan datetime,
   computer_auth_scan int2 DEFAULT '0' NOT NULL,
   PRIMARY KEY (computer_id)
);



--
-- Table structure for table 'ComputerDisplay'
--
CREATE TABLE ComputerDisplay (
   display_user_id int8 DEFAULT '0' NOT NULL,
   display_fieldname varchar(40) NOT NULL,
   display_fieldorder int4,
   display_display int2 DEFAULT '1' NOT NULL
);
 


--
-- Table structure for table 'ServiceComputer'
--
CREATE TABLE ServiceComputer (
   service_computer_id int8,
   service_name varchar(30),
   service_port int8,
   service_proto varchar(5),
   service_desc varchar(70),
   service_status int4
);


--
-- Update the structure of table "ListDisplay" 
--
-- display_fieldname is now on 40 characters
--
CREATE TABLE Temp AS SELECT * FROM ListDisplay;
DROP TABLE ListDisplay;
CREATE TABLE ListDisplay (
   display_user_id int8 DEFAULT '0' NOT NULL,
   display_fieldname varchar(40) NOT NULL,
   display_fieldorder int4,
   display_display int2 DEFAULT '1' NOT NULL
);
INSERT INTO ListDisplay(display_user_id,display_fieldname,display_fieldorder,display_display) SELECT * FROM Temp;
DROP TABLE Temp;




--
-- Update the structure of table "Company" 
--
-- New fields : company_number, company_state
-- Modification : company_zipcode is now on 14 char
-- 
CREATE TABLE Temp AS SELECT * FROM Company;

DROP TABLE Company;
DROP SEQUENCE company_company_id_seq;

CREATE TABLE Company (
  company_id SERIAL,
  company_timeupdate datetime,
  company_timecreate datetime,
  company_userupdate int8,
  company_usercreate int8,
  company_number varchar(32),
  company_state int2 DEFAULT '0',
  company_name varchar(50) DEFAULT '' NOT NULL,
  company_type_id int8,
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
  company_mailing int2,
  company_comment text,
  PRIMARY KEY (company_id)
);

INSERT INTO Company (company_id,company_timeupdate,company_timecreate,company_userupdate,company_usercreate,company_name,company_type_id,company_address1,company_address2,company_zipcode,company_town,company_expresspostal,company_country,company_phone,company_fax,company_web,company_email,company_mailing,company_comment) SELECT * FROM Temp;

DROP TABLE Temp;
--DROP SEQUENCE temp_company_id_seq;  -- ?



--
-- Update the structure of table "Contact"
--
-- New field ; contact_visibility
-- Modification : contact_zipcode is now on 14 char.
-- 
CREATE TABLE Temp AS SELECT * FROM Contact;
DROP TABLE Contact;
DROP SEQUENCE contact_contact_id_seq;

CREATE TABLE Contact (
  contact_id SERIAL,
  contact_timeupdate datetime,
  contact_timecreate datetime,
  contact_userupdate int8,
  contact_usercreate int8,
  contact_company_id int8,
  contact_kind_id int8,
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
  contact_visibility int2 DEFAULT '0',
  PRIMARY KEY (contact_id)
);

INSERT INTO Contact (contact_id,contact_timeupdate,contact_timecreate,contact_userupdate,contact_usercreate,contact_company_id,contact_kind_id,contact_lastname,contact_firstname,contact_address1,contact_address2,contact_zipcode,contact_town,contact_expresspostal,contact_country,contact_function,contact_phone,contact_homephone,contact_mobilephone,contact_fax,contact_email,contact_comment) SELECT * FROM Temp;

DROP TABLE Temp;
---DROP SEQUENCE temp_contact_id_seq; --?



--
-- Update the structure of table "Deal"
--
-- New fields : deal_number, deal_parentdeal_id, deal_todo, deal_visibility
-- Modification : deal_label is now on 128 characters
-- Drop : deal_origin, deal_manager and deal_proposal
-- 
CREATE TABLE Temp AS SELECT deal_id,deal_timeupdate,deal_timecreate,deal_userupdate,deal_usercreate,deal_label,deal_datebegin,deal_type_id,deal_category_id,deal_company_id,deal_contact1_id,deal_contact2_id,deal_marketingmanager_id,deal_technicalmanager_id,deal_dateproposal,deal_amount,deal_status_id,deal_datealarm,deal_comment,deal_archive FROM Deal;

DROP TABLE Deal;    
DROP SEQUENCE deal_deal_id_seq;
 
CREATE TABLE Deal (
  deal_id SERIAL,
  deal_timeupdate datetime,
  deal_timecreate datetime,
  deal_userupdate int8,
  deal_usercreate int8,
  deal_number varchar(32),
  deal_label varchar(128),
  deal_datebegin date,
  deal_parentdeal_id int8,
  deal_type_id int8,
  deal_category_id int8,
  deal_company_id int8 DEFAULT '0' NOT NULL,
  deal_contact1_id int8,
  deal_contact2_id int8,
  deal_marketingmanager_id int8,
  deal_technicalmanager_id int8,
  deal_dateproposal date,
  deal_amount decimal(12,2),
  deal_status_id int2,
  deal_datealarm date,
  deal_comment text,
  deal_archive int2 DEFAULT '0',
  deal_todo varchar(128),
  deal_visibility int2 DEFAULT '0',
  PRIMARY KEY (deal_id)
);

INSERT INTO Deal (deal_id,deal_timeupdate,deal_timecreate,deal_userupdate,deal_usercreate,deal_label,deal_datebegin,deal_type_id,deal_category_id,deal_company_id,deal_contact1_id,deal_contact2_id,deal_marketingmanager_id,deal_technicalmanager_id,deal_dateproposal,deal_amount,deal_status_id,deal_datealarm,deal_comment,deal_archive) SELECT * FROM Temp;

DROP TABLE Temp;
--DROP SEQUENCE temp_deal_id_seq; 




--
-- Update the structure of table "DealType"
--
ALTER TABLE DealType ADD COLUMN dealtype_inout varchar(1) DEFAULT '-';


--
-- Update the structure of table "DealStatus"
--
ALTER TABLE DealStatus ADD COLUMN dealstatus_order int2;
 

--
-- Update the structure of table "UserObm"
--
-- Modification : userobm_perms is now on 254 char.
-- New field : userobm_contact_id
--
CREATE TABLE Temp as SELECT * FROM Userobm;
DROP TABLE Userobm;
DROP SEQUENCE userobm_userobm_id_seq;

CREATE TABLE UserObm (
  userobm_id			SERIAL,
  userobm_timeupdate		datetime,
  userobm_timecreate		datetime,
  userobm_userupdate		int8,
  userobm_usercreate		int8,
  userobm_username		varchar(32) DEFAULT '' NOT NULL,
  userobm_password		varchar(32) DEFAULT '' NOT NULL,
  userobm_perms			varchar(254),
  userobm_email			varchar(60),
  userobm_timelastaccess	datetime,
  userobm_contact_id		int8,
  PRIMARY KEY (userobm_id),
  UNIQUE (userobm_username)
);

INSERT INTO UserObm (userobm_id, userobm_timeupdate,userobm_timecreate,userobm_userupdate,userobm_usercreate,userobm_username,userobm_password,userobm_perms,userobm_email,userobm_timelastaccess) SELECT * FROM Temp;
DROP TABLE Temp;



---------

--
-- The company of id "1" is "internal" 
-- the others are "external"
--
UPDATE Company set company_state=0;	
UPDATE Company set company_state=1 where company_id=1;


--
-- All existing contacts and deals are set to "public" 
--
UPDATE Contact set contact_visibility = 0;
UPDATE Deal set deal_visibility = 0;


--
-- The Deal types are set to '-' (out) 
-- "ventes" is set to "+" ("in")
--
UPDATE DealType set dealtype_inout='-';
UPDATE DealType set dealtype_inout='+' where dealtype_label='VENTE' or dealtype_label='SALE';


--
-- The Deal status are ordered 
--
UPDATE DealStatus set dealstatus_order=1 where dealstatus_label='CONTACT';
UPDATE DealStatus set dealstatus_order=1 where dealstatus_label='RDV' or dealstatus_label='Appointment';
UPDATE DealStatus set dealstatus_order=2 where dealstatus_label='ATTENTE PROP.' or dealstatus_label='Waiting for Proposal' or dealstatus_label like 'Attente de propositi%';
UPDATE DealStatus set dealstatus_order=3 where dealstatus_label='PROPOSITION';
UPDATE DealStatus set dealstatus_order=4 where dealstatus_label='SIGNEE' or dealstatus_label='SIGNED';
UPDATE DealStatus set dealstatus_order=5 where dealstatus_label='PERDUE' or dealstatus_label='LOST';
UPDATE DealStatus set dealstatus_order=6 where dealstatus_label='REALISEE' or dealstatus_label='DONE';
UPDATE DealStatus set dealstatus_order=7 where dealstatus_label='FACTURE';
UPDATE DealStatus set dealstatus_order=7 where dealstatus_label='FACTUREE' or dealstatus_label='FACTURE' or dealstatus_label='INVOICE';
UPDATE DealStatus set dealstatus_order=8 where dealstatus_label='PAYEE';
UPDATE DealStatus set dealstatus_order=8 where dealstatus_label='SOLDEE' or dealstatus_label='CLOSED'; 
