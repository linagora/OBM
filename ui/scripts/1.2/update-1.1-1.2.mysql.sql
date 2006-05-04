-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM MySQL Database from 1.1 to 1.2                              //
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
ALTER TABLE List CHANGE list_name list_name varchar(64);


-------------------------------------------------------------------------------
-- Update Contract table
-------------------------------------------------------------------------------
-- contract_label length
ALTER TABLE Contract CHANGE contract_label contract_label varchar(128);


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
-- Re-create Payment table
-------------------------------------------------------------------------------
DROP TABLE IF EXISTS Payment;

CREATE TABLE Payment (
  payment_id              int(8) auto_increment,
  payment_timeupdate      timestamp(14),
  payment_timecreate      timestamp(14),
  payment_userupdate      int(8),
  payment_usercreate      int(8),
  payment_company_id      int(8) NOT NULL,
  payment_account_id      int(8),
  payment_paymentkind_id  int(8) NOT NULL,
  payment_amount          double(10,2) DEFAULT '0.0' NOT NULL,
  payment_date            date,
  payment_inout           char(1) NOT NULL DEFAULT '+',
  payment_number          varchar(24) DEFAULT '',
  payment_checked         char(1) NOT NULL DEFAULT '0',
  payment_comment         text,
  PRIMARY KEY (payment_id)
);


-------------------------------------------------------------------------------
-- Update PaymentKind table
-------------------------------------------------------------------------------
ALTER TABLE PaymentKind ADD COLUMN paymentkind_label varchar(40) NOT NULL DEFAULT '';
UPDATE PaymentKind SET paymentkind_label = paymentkind_longlabel;
ALTER TABLE PaymentKind DROP COLUMN paymentkind_longlabel;


-------------------------------------------------------------------------------
-- Drop deprecated tables
-------------------------------------------------------------------------------
DROP TABLE IF EXISTS EntryTemp;
DROP TABLE IF EXISTS PaymentTemp;
