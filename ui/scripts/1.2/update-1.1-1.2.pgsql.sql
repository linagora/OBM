-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM PostgreSQL Database from 1.1 to 1.2                         //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////

-------------------------------------------------------------------------------
-- Global Information table
-------------------------------------------------------------------------------
UPDATE ObmInfo set obminfo_value='1.2' where obminfo_name='db_version';


-------------------------------------------------------------------------------
-- Update List table
-------------------------------------------------------------------------------
-- list_name length
ALTER TABLE List ADD COLUMN temp_name varchar(64);
UPDATE List SET temp_name = list_name;
ALTER TABLE List DROP COLUMN list_name;
ALTER TABLE List RENAME COLUMN temp_name TO list_name;

-------------------------------------------------------------------------------
-- Update Contract table
-------------------------------------------------------------------------------
-- contract_label length
ALTER TABLE Contract ADD COLUMN temp_label varchar(128);
UPDATE Contract SET temp_label = contract_label;
ALTER TABLE Contract DROP COLUMN contract_label;
ALTER TABLE Contract RENAME COLUMN temp_label TO contract_label;


-------------------------------------------------------------------------------
-- Clean some tables content
-------------------------------------------------------------------------------
UPDATE ProjectUser SET projectuser_manager=0 WHERE projectuser_manager is null;
UPDATE Contact SET contact_address1='' WHERE contact_address1 is null;
UPDATE Contact SET contact_address2='' WHERE contact_address2 is null;
UPDATE Contact SET contact_address3='' WHERE contact_address3 is null;
UPDATE Contact SET contact_zipcode='' WHERE contact_zipcode is null;
UPDATE Contact SET contact_town='' WHERE contact_town is null;
UPDATE Contact SET contact_expresspostal='' WHERE contact_expresspostal is null;
UPDATE Company SET company_zipcode='' WHERE company_zipcode is null;


-------------------------------------------------------------------------------
-- Clean UserObm table
-------------------------------------------------------------------------------
-- set lastname and firstname to default '' instead of null (cause of concat)
UPDATE UserObm SET userobm_lastname = '' where userobm_lastname is null;
ALTER TABLE UserObm ALTER COLUMN userobm_lastname SET DEFAULT '';
UPDATE UserObm SET userobm_firstname = '' where userobm_firstname is null;
ALTER TABLE UserObm ALTER COLUMN userobm_firstname SET DEFAULT '';


-------------------------------------------------------------------------------
-- Re-create Payment table
-------------------------------------------------------------------------------
DROP TABLE Payment;

CREATE TABLE Payment (
  payment_id              serial,
  payment_timeupdate      timestamp,
  payment_timecreate      timestamp,
  payment_userupdate      integer,
  payment_usercreate      integer,
  payment_company_id      integer NOT NULL,
  payment_account_id      integer,
  payment_paymentkind_id  integer NOT NULL,
  payment_amount          decimal(10,2) DEFAULT '0.0' NOT NULL,
  payment_date            date,
  payment_inout           char(1) NOT NULL DEFAULT '+',
  payment_number          varchar(24) NOT NULL DEFAULT '',
  payment_checked         char(1) NOT NULL DEFAULT '0',
  payment_comment         text,
  PRIMARY KEY (payment_id)
);


-------------------------------------------------------------------------------
-- Update PaymentKind table
-------------------------------------------------------------------------------
ALTER TABLE PaymentKind ADD COLUMN paymentkind_label varchar(40);
ALTER TABLE PaymentKind ALTER COLUMN paymentkind_label SET DEFAULT '';
ALTER TABLE PaymentKind ALTER COLUMN paymentkind_label SET NOT NULL;
UPDATE PaymentKind SET paymentkind_label = paymentkind_longlabel;
ALTER TABLE PaymentKind DROP COLUMN paymentkind_longlabel;
