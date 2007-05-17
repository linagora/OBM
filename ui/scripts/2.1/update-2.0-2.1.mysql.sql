-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM MySQL Database from 2.0 to 2.1                              //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Global Information table
-------------------------------------------------------------------------------
UPDATE ObmInfo set obminfo_value='2.1' where obminfo_name='db_version';


-------------------------------------------------------------------------------
-- Update Lead table
-------------------------------------------------------------------------------
-- Add contact link
ALTER TABLE Lead ADD COLUMN lead_contact_id int(8) NOT NULL DEFAULT 0 AFTER lead_company_id;


-------------------------------------------------------------------------------
-- Update UserObm table
-------------------------------------------------------------------------------
-- Add delegation field (for delegated admin)
ALTER TABLE UserObm ADD COLUMN userobm_delegation_target varchar(64) DEFAULT '' AFTER userobm_perms;
ALTER TABLE UserObm ADD COLUMN userobm_delegation varchar(64) DEFAULT '' AFTER userobm_delegation;


-------------------------------------------------------------------------------
-- Tables needed for Automate work
-------------------------------------------------------------------------------
--
-- Table structure for the table 'Deleted'
--
CREATE TABLE Deleted (
  deleted_id         int(8) auto_increment,
  deleted_domain_id  int(8),
  deleted_entity     varchar(32),
  deleted_entity_id  int(8),
  deleted_user_id    int(8),
  deleted_timestamp  timestamp(14),
  PRIMARY KEY (deleted_id)
);


--
-- Table structure for the table 'Updated'
--
CREATE TABLE Updated (
  updated_id         int(8) auto_increment,
  updated_domain_id  int(8),
  updated_entity     varchar(32),
  updated_entity_id  int(8),
  updated_user_id    int(8),
  updated_type       char(1),
  PRIMARY KEY (updated_id)
);
