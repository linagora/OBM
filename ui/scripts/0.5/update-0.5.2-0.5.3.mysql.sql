-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM Database from 0.5.2 to 0.5.3	                             //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$ //
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Table structure for table `GlobalPref`
-------------------------------------------------------------------------------

CREATE TABLE GlobalPref (
  globalpref_option varchar(255) NOT NULL default '',
  globalpref_value varchar(255) NOT NULL default '',
  PRIMARY KEY  (globalpref_option),
  UNIQUE KEY globalpref_option (globalpref_option)
) TYPE=MyISAM;

-- Dumping data for table `GlobalPref`
--
INSERT INTO GlobalPref VALUES ('lifetime', '14400');
INSERT INTO GlobalPref VALUES ('session_cookie', '1');


-------------------------------------------------------------------------------
-- UserObm Update
-------------------------------------------------------------------------------
-- change userobm_username name to userobm_login
ALTER table UserObm change column userobm_username userobm_login varchar(32);

-- add archive columns
ALTER table UserObm add column userobm_archive char(1) not null default '0' after userobm_perms;

-- add lastname and firstname columns
ALTER table UserObm add column userobm_lastname varchar(32) after userobm_archive;
ALTER table UserObm add column userobm_firstname varchar(32) after userobm_lastname;


-------------------------------------------------------------------------------
-- Company Update
-------------------------------------------------------------------------------
-- add company_userobm_manager
ALTER table Company add column company_marketingmanager_id int(8) after company_type_id;


-------------------------------------------------------------------------------
-- Get rid of old deprecated tables
-------------------------------------------------------------------------------
-- Drop deprecated table ActiveSessions
Drop table IF EXISTS ActiveSessions;
-- Drop deprecated Display tables
Drop table IF EXISTS CompanyDisplay;
Drop table IF EXISTS ComputerDisplay;
Drop table IF EXISTS ContactDisplay;
Drop table IF EXISTS DealDisplay;
Drop table IF EXISTS ListDisplay;
Drop table IF EXISTS ParentDealDisplay;


-------------------------------------------------------------------------------
-- Contract Update
-------------------------------------------------------------------------------
-- drop column contract_typedeal
ALTER table Contract drop column contract_typedeal;

-- update many columns names
ALTER table Contract change column contract_numero contract_number varchar(20);
ALTER table Contract change column contract_debut contract_datebegin date;
ALTER table Contract change column contract_expiration contract_dateexp date;
ALTER table Contract change column contract_responsable_client_id contract_contact1_id int(8);
ALTER table Contract change column contract_responsable_client2_id contract_contact2_id int(8);
ALTER table Contract change column contract_responsable_tech_id contract_techmanager_id int(8);
ALTER table Contract change column contract_responsable_com_id contract_marketmanager_id int(8);

-- move text columns at the row end
ALTER table Contract add column tmp text after contract_marketmanager_id;
UPDATE Contract set tmp=contract_clause;
ALTER table Contract drop column contract_clause;
ALTER table Contract change tmp contract_clause text;

ALTER table Contract add column tmp text after contract_clause;
UPDATE Contract set tmp=contract_comment;
ALTER table Contract drop column contract_comment;
ALTER table Contract change tmp contract_comment text;
