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
ALTER TABLE Payment ADD COLUMN payment_invoice_id integer;
ALTER TABLE Payment ALTER COLUMN payment_invoice_id SET DEFAULT 0;
ALTER TABLE Payment ALTER COLUMN payment_invoice_id SET NOT NULL;
ALTER TABLE Payment ADD COLUMN payment_company_id integer DEFAULT NULL;

UPDATE Payment set payment_invoice_id=(select paymentinvoice_invoice_id from PaymentInvoice where paymentinvoice_payment_id=payment_id);
UPDATE Payment set payment_company_id=(select invoice_company_id from Invoice where invoice_id=payment_invoice_id);

DROP Table PaymentInvoice;
