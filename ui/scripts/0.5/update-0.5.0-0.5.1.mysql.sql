-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM Database from 0.5.0 to 0.5.1	                             //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$ //
-- ////////////////////////////////////////////////////////////////////////////


--
-- Update (y/n -> 1/0) or create invoice_archive
--

-- Drop column invoice_archive from table Invoice
-- If the column doesn't exist, comment this line
ALTER table Invoice drop column invoice_archive;

-- Add column invoice_archive from table Invoice
ALTER table Invoice add column invoice_archive char(1) NOT NULL default '0';


--
-- Update payment_paid (y/n -> 1/0)
--

ALTER table Payment CHANGE payment_paid payment_paid char(1) NOT NULL DEFAULT '0';


--
-- Update (y/n -> 1/0) or create payment_checked
--

-- Drop column payment_checked from table Payment
-- If the column doesn't exist, comment this line
ALTER table Payment drop column payment_checked;

-- Add column invoice_archive from table Invoice
ALTER table Payment add column payment_checked char(1) NOT NULL default '0';


--
-- Update Size of email address fields
--

ALTER table Company CHANGE company_email company_email varchar(128);
ALTER table Contact CHANGE contact_email contact_email varchar(128);


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
-- Update some DisplayPref values
-------------------------------------------------------------------------------
-- move list_contact_company_id to list_contact_company (to allow order)
UPDATE DisplayPref set display_fieldname='list_contact_company' where display_fieldname='list_contact_company_id';

-- move list_timecreate and list_timeupdate to timecreate and timeupdate
-- to allow formatting of these date fields
UPDATE DisplayPref set display_fieldname='timeupdate' where display_entity='list' and display_fieldname='list_timeupdate';
UPDATE DisplayPref set display_fieldname='timecreate' where display_entity='list' and display_fieldname='list_timecreate';

-- move list_usercreate and list_userupdate to usercreate and userupdate
-- to allow printing of user name
UPDATE DisplayPref set display_fieldname='usercreate' where display_entity='list' and display_fieldname='list_usercreate';
UPDATE DisplayPref set display_fieldname='userupdate' where display_entity='list' and display_fieldname='list_userupdate';

-------------------------------------------------------------------------------
-- Support tables
-------------------------------------------------------------------------------
------ IF EXISTS DROP TABLE Contrat;
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
  incident_archive int(2) NOT NULL default '0',
  PRIMARY KEY  (incident_id)
) TYPE=MyISAM;


--module 'contract'
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'contract', 'contract_label', 1, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'contract', 'contract_numero', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'contract', 'contract_company_name', 3, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'contract', 'contracttype_label', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'contract', 'contract_expiration', 5, 1);

--module 'incident'
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'incident_label', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'incident_priority', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'incident_etat', 5, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'incident_date', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'incident_owner_lastname', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'incident_logger_lastname', 3, 1);
    
--module 'contract'
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'contract', 'contract_label', 1, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'contract', 'contract_numero', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'contract', 'contract_compagny_name', 3, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'contract', 'contracttype_label', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'contract', 'contract_expiration', 5, 1);

--module 'incident'
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'incident', 'incident_label', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'incident', 'incident_priority', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'incident', 'incident_etat', 5, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'incident', 'incident_date', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'incident', 'incident_owner_lastname', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'incident', 'incident_logger_lastname', 3, 1);




