# ////////////////////////////////////////////////////////////
# // Update OBM Database from 0.3.3 to 0.4	            //
# //                                                        //
# // To use when the database already contains the tables   //
# // 'Userobm_pref', 'Computer', 'Service Computer',         //
# // 'ComputerDisplay'                                      // 
# //                                                        //
# ////////////////////////////////////////////////////////////
# // 2001-05-30    Francois Bloque                          //
# ////////////////////////////////////////////////////////////


#-----------------------------------------------------
#
# The table 'Userobm' is now called 'UserObm'
#

ALTER TABLE Userobm RENAME UserObm;


#-----------------------------------------------------
#
# The table 'Userobm_pref' is now called 'UserObmPref'
# 

ALTER TABLE Userobm_pref  RENAME UserObmPref;


#-----------------------------------------------------
#
# The table 'active_sessions' is now called 'ActiveSessions'
#

ALTER TABLE active_sessions RENAME ActiveSessions;


#-----------------------------------------------------
#
# Table 'DealOrigin' is no longer used
#

DROP TABLE IF EXISTS DealOrigin;


#----------------------------------------------------
#
# Table 'ListDisplay' have to be empty
#
DELETE FROM ListDisplay;

#----------------------------------------------------
#
# Table 'UserObmPref' have to be empty
#
DELETE FROM UserObmPref;


#----------------------------------------------------
#
# Table 'ComputerDisplay' have to be empty
#
DELETE FROM ComputerDisplay;


#-----------------------------------------------------
#
# Structure modifications for table 'UserObmPref'
#

ALTER TABLE UserObmPref CHANGE userobm_pref_id userobmpref_id int(8) DEFAULT '0' NOT NULL;
ALTER TABLE UserObmPref CHANGE userobm_pref_option userobmpref_option varchar(50) NOT NULL;
ALTER TABLE UserObmPref CHANGE userobm_pref_choice userobmpref_choice varchar(50) NOT NULL;


#-----------------------------------------------------
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



#-----------------------------------------------------
#
# Table structure for table 'DealDisplay'
#

CREATE TABLE DealDisplay (
   display_user_id int(8) DEFAULT '0' NOT NULL,
   display_fieldname varchar(40) NOT NULL,
   display_fieldorder tinyint(3) unsigned,
   display_display tinyint(1) unsigned DEFAULT '1' NOT NULL
);




#-----------------------------------------------------
#
# Table structure for table 'ParentDealDisplay'
#

CREATE TABLE ParentDealDisplay (
   display_user_id int(8) DEFAULT '0' NOT NULL,
   display_fieldname varchar(40) NOT NULL,
   display_fieldorder tinyint(3) unsigned,
   display_display tinyint(1) unsigned DEFAULT '1' NOT NULL
);



#-----------------------------------------------------
#
# Table structure for table 'CompanyDisplay'
#

CREATE TABLE CompanyDisplay (
   display_user_id int(8) DEFAULT '0' NOT NULL,
   display_fieldname varchar(40) NOT NULL,
   display_fieldorder tinyint(3) unsigned,
   display_display tinyint(1) unsigned DEFAULT '1' NOT NULL
);

#--------------------------------------------------
#
# Table structure for table 'ContactDisplay'
#

CREATE TABLE ContactDisplay (
   display_user_id int(8) DEFAULT '0' NOT NULL,
   display_fieldname varchar(40) NOT NULL,
   display_fieldorder tinyint(3) unsigned,
   display_display tinyint(1) unsigned DEFAULT '1' NOT NULL
);



#-----------------------------------------------------
#
# Update the structure of table "ListDisplay" 
#
ALTER TABLE ListDisplay CHANGE display_fieldname display_fieldname varchar(40) NOT NULL;


#-----------------------------------------------------
#
# Update the structure of table "ComputerDisplay" 
#
ALTER TABLE ComputerDisplay CHANGE display_fieldname display_fieldname varchar(40) NOT NULL;


#-----------------------------------------------------
#
# Update the structuure of table "Company" 
#

# ZipCode is now on 14 characters
ALTER TABLE Company CHANGE company_zipcode company_zipcode varchar(14);

# New fields 
ALTER TABLE Company ADD company_number varchar(32) AFTER company_usercreate;
ALTER TABLE Company ADD company_state int(2) DEFAULT '0' AFTER company_number;



#-----------------------------------------------------
#
# Update the structure of table "Contact"
#

# ZipCode is now on 14 characters
ALTER TABLE Contact CHANGE contact_zipcode contact_zipcode varchar(14);

# New field
ALTER TABLE Contact ADD contact_visibility int(2) DEFAULT '0';



#-----------------------------------------------------
#
# Update the structure of table "Deal"
#

# ZipCode is now on 14 characters
ALTER TABLE Deal CHANGE deal_label deal_label varchar(128);

# New fields
ALTER TABLE Deal ADD deal_number varchar(32) AFTER deal_usercreate;
ALTER TABLE Deal ADD deal_parentdeal_id int(8) AFTER deal_datebegin;
ALTER TABLE Deal ADD deal_todo varchar(128); 
ALTER TABLE Deal ADD deal_visibility int(2) DEFAULT '0';

# old fields 
ALTER TABLE Deal DROP deal_origin_id;
ALTER TABLE Deal DROP deal_manager_id;
ALTER TABLE Deal DROP deal_proposal;



#-----------------------------------------------------
#
# Update the structure of table "DealType"
#

# New field
ALTER TABLE DealType ADD dealtype_inout varchar(1) DEFAULT '-';



#-----------------------------------------------------
#
# Update the structure of table "DealStatus"
#

# New field
ALTER TABLE DealStatus ADD dealstatus_order int(2);


#-----------------------------------------------------
#
# Update the structure of table "UserObm"
#

# perms are now stored on 254 characters
ALTER TABLE UserObm CHANGE userobm_perms userobm_perms varchar(254);

# New field
ALTER TABLE UserObm ADD userobm_contact_id int(8);



#---------------------------------------------------------------------
#
# The company of id "1" is "internal" 
# the others are "external"
#

UPDATE Company set company_state=0;	
UPDATE Company set company_state=1 where company_id=1;


#----------------------------------------------------------------------
# All existing contacts and deals are set to "public" 
#

UPDATE Contact set contact_visibility = 0;
UPDATE Deal set deal_visibility = 0;


#---------------------------------------------------------------------
# The Deal types are set to '-' (out) 
#

UPDATE DealType set dealtype_inout="-";
# "ventes" is set to "+" ("in")
UPDATE DealType set dealtype_inout="+" where dealtype_label="VENTE" or dealtype_label="SALE";


#---------------------------------------------------------------------
# The Deal status are ordered 
#

UPDATE DealStatus set dealstatus_order=1 where dealstatus_label="CONTACT";

UPDATE DealStatus set dealstatus_order=1 where dealstatus_label="RDV" or "Appointment";
UPDATE DealStatus set dealstatus_order=2 where dealstatus_label="ATTENTE PROP." or dealstatus_label="Waiting for Proposal" or dealstatus_label like "Attente de propositi%";
UPDATE DealStatus set dealstatus_order=3 where dealstatus_label="PROPOSITION";
UPDATE DealStatus set dealstatus_order=4 where dealstatus_label="SIGNEE" or dealstatus_label="SIGNED";
UPDATE DealStatus set dealstatus_order=5 where dealstatus_label="PERDUE" or dealstatus_label="LOST";
UPDATE DealStatus set dealstatus_order=6 where dealstatus_label="REALISEE" or dealstatus_label="DONE";
UPDATE DealStatus set dealstatus_order=7 where dealstatus_label="FACTURE";
UPDATE DealStatus set dealstatus_order=7 where dealstatus_label="FACTUREE" or dealstatus_label="FACTURE" or dealstatus_label="INVOICE";
UPDATE DealStatus set dealstatus_order=8 where dealstatus_label="PAYEE";
UPDATE DealStatus set dealstatus_order=8 where dealstatus_label="SOLDEE" or dealstatus_label="CLOSED"; 





