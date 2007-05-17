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
ALTER TABLE Lead ADD COLUMN lead_contact_id integer;
ALTER TABLE Lead ALTER COLUMN lead_contact_id SET DEFAULT 0;


-------------------------------------------------------------------------------
-- Update UserObm table
-------------------------------------------------------------------------------
-- Add delegation fields (for delegated admin)
ALTER TABLE UserObm ADD COLUMN userobm_delegation_target varchar(64);
ALTER TABLE UserObm ALTER COLUMN userobm_delegation_target SET DEFAULT '';
ALTER TABLE UserObm ADD COLUMN userobm_delegation varchar(64);
ALTER TABLE UserObm ALTER COLUMN userobm_delegation SET DEFAULT '';


-------------------------------------------------------------------------------
-- Tables needed for Automate work
-------------------------------------------------------------------------------
--
-- Table structure for the table 'Deleted'
--
CREATE TABLE Deleted (
  deleted_id         serial,
  deleted_domain_id  integer,
  deleted_entity     varchar(32),
  deleted_entity_id  integer,
  deleted_user_id    integer,
  deleted_timestamp  timestamp,
  PRIMARY KEY (deleted_id)
);


--
-- Table structure for the table 'Updated'
--
CREATE TABLE Updated (
  updated_id         serial,
  updated_domain_id  integer,
  updated_entity     varchar(32),
  updated_entity_id  integer,
  updated_user_id    integer,
  updated_type       char(1),
  PRIMARY KEY (updated_id)
);
