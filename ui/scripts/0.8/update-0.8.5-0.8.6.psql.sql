-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM PostgreSQL Database from 0.8.5 to 0.8.6                     //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Global Information table
-------------------------------------------------------------------------------
--
-- Table structure for table 'ObmInfo'
--
CREATE TABLE ObmInfo (
  obminfo_name   varchar(32) NOT NULL default '',
  obminfo_value  varchar(255) default '',
  PRIMARY KEY (obminfo_name)
);

INSERT INTO ObmInfo (obminfo_name, obminfo_value) values ('db_version', '0.8.6');


-------------------------------------------------------------------------------
-- Update Country relations : key is now : iso3166 + lang
-------------------------------------------------------------------------------
-- Company Table

ALTER TABLE Company ADD COLUMN company_country_iso3166 char(2) null;
ALTER TABLE Company ALTER COLUMN company_country_iso3166 SET DEFAULT '';
UPDATE Company set company_country_iso3166=(select country_iso3166 from Country where country_id=company_country_id);
ALTER TABLE Company DROP COLUMN company_country_id;

-- Contact Table

ALTER TABLE Contact ADD COLUMN contact_country_iso3166 char(2) null;
ALTER TABLE Contact ALTER COLUMN contact_country_iso3166 SET DEFAULT '';
UPDATE Contact set contact_country_iso3166=(select country_iso3166 from Country where country_id=contact_country_id);
ALTER TABLE Contact DROP COLUMN contact_country_id;

-- Country table

ALTER TABLE Country DROP CONSTRAINT country_pkey;
ALTER TABLE Country ADD CONSTRAINT country_pkey PRIMARY KEY (country_iso3166, country_lang);
ALTER TABLE Country DROP COLUMN Country_id;


-------------------------------------------------------------------------------
-- Update Invoice table
-------------------------------------------------------------------------------
ALTER TABLE Invoice ADD COLUMN invoice_company_id integer;
ALTER TABLE Invoice ALTER COLUMN invoice_company_id SET DEFAULT 0;
ALTER TABLE Invoice ALTER COLUMN invoice_company_id SET NOT NULL;
ALTER TABLE Invoice ADD COLUMN invoice_deal_id integer DEFAULT NULL;
ALTER TABLE Invoice ADD COLUMN invoice_project_id integer DEFAULT NULL;

UPDATE Invoice set invoice_deal_id=(select dealinvoice_deal_id from DealInvoice where dealinvoice_invoice_id=invoice_id);
UPDATE Invoice set invoice_company_id=(select deal_company_id from Deal where deal_id=invoice_deal_id);

DROP Table DealInvoice;


-------------------------------------------------------------------------------
-- Update Payment table
-------------------------------------------------------------------------------
ALTER TABLE Payment ADD COLUMN payment_company_id integer;
ALTER TABLE Payment ALTER COLUMN payment_company_id SET NOT NULL;


-------------------------------------------------------------------------------
-- Update DocumentEntity table for conformance and content
-------------------------------------------------------------------------------
ALTER TABLE DocumentEntity RENAME COLUMN documententity_documentid TO documententity_document_id;
ALTER TABLE DocumentEntity RENAME COLUMN documententity_entityid TO documententity_entity_id;

UPDATE DocumentEntity set documententity_entity = 'company' where documententity_entity='Company';
UPDATE DocumentEntity set documententity_entity = 'contact' where documententity_entity='Contact';
UPDATE DocumentEntity set documententity_entity = 'deal' where documententity_entity='Deal';


-------------------------------------------------------------------------------
-- Update Contact table
-------------------------------------------------------------------------------
ALTER TABLE Contact ADD COLUMN town_temp varchar(64);
UPDATE Contact set town_temp=contact_town;
ALTER TABLE Contact DROP COLUMN contact_town;
ALTER TABLE Contact RENAME COLUMN town_temp TO contact_town;


-------------------------------------------------------------------------------
-- Replace InvoiceStatus table
-------------------------------------------------------------------------------
DROP TABLE InvoiceStatus;

CREATE TABLE InvoiceStatus (
  invoicestatus_id       serial,
  invoicestatus_payment  integer DEFAULT '0' NOT NULL,
  invoicestatus_archive  integer DEFAULT '0' NOT NULL,
  invoicestatus_label    varchar(24) DEFAULT '' NOT NULL,
  PRIMARY KEY (invoicestatus_id)
);
