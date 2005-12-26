--/////////////////////////////////////////////////////////////////////////////
-- OBM - File : obm_default_values_1.0.sql                                   //
--     - Desc : Insertion of Default values (database independant)           //
-- 2005-06-08 Pierre Baudracco                                               //
--/////////////////////////////////////////////////////////////////////////////
-- $Id$
--/////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Default Information (table ObmInfo)
-------------------------------------------------------------------------------

-- Update DB version
DELETE FROM ObmInfo where obminfo_name='db_version';
INSERT INTO ObmInfo (obminfo_name, obminfo_value) VALUES ('db_version', '1.1.0');


-------------------------------------------------------------------------------
-- Default User preferences values (table UserObmPref)
-------------------------------------------------------------------------------
-- user 0 represent default values (affected to new users for ex:)
--

-- Delete current default values
DELETE FROM UserObmPref where userobmpref_user_id='0';

-- Language
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','set_lang','en');

-- Theme
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','set_theme','standard');

-- Menu
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','set_menu','both');

-- Auto-Display list results
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','set_display','no');

-- # Rows displayed in a result page
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','set_rows','12');

-- Default Data Source
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','set_dsrc','0');

-- Date
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','set_date','Y-m-d');

-- Comment display order
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','set_commentorder','0');

-- Calendar Interval
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','set_cal_interval','2');

-- CSV Export separator
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','set_csv_sep',';');

-- Debug Level
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','set_debug','0');

-- Mail enabled
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','set_mail','yes');

-- Day the week start 
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','set_day_weekstart','monday');

-- Todo sort order
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','set_todo','todo_priority');


-------------------------------------------------------------------------------
-- Default values for the table 'DisplayPref'
-- user 0 represent default values (affected to new users for ex:)
--

-- Delete current default values
DELETE FROM DisplayPref where display_user_id='0';


-- module 'company'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_name',1,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_archive',2,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_contact_number',3,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_new_contact',4,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_deal_number',5,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_number',6,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_vat',7,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','companytype_label',8,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','companyactivity_label',9,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','companynafcode_code',10,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_address',11,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_address1',12,0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_address2',13,0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_address3',14,0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_zipcode',15,0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_town',16,0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_expresspostal',17,0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','country_name',18,0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_phone',19,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_fax',20,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_email',21,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_web',22,1);

-- module 'contact'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_lastname',1,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_firstname',2,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_archive',3,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_function',4,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_title',5,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'contact', 'kind_minilabel', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'contact', 'kind_header', 7, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'contact', 'kind_lang', 8, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','company_name',9,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_address',10,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'contact', 'contact_service', 11, 0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'contact', 'contact_address1', 12, 0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'contact', 'contact_address2', 13, 0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'contact', 'contact_address3', 14, 0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'contact', 'contact_zipcode', 15, 0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'contact', 'contact_town', 16, 0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'contact', 'contact_expresspostal', 17, 0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'contact', 'country_name', 18, 0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_phone',19,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_homephone',20,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_mobilephone',21,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_fax',22,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_email',23,1);

-- module 'deal'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','deal_label',1,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','deal_company_name',2,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','deal_company_zipcode',3,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','dealtype_label',4,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','tasktype_label',5,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','dealstatus_label',6,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','deal_marketingmanager',7,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','deal_amount',8,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','deal_hitrate',9,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','deal_dateexpected',10,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','deal_todo',11,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','deal_archive',12,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','deal_datealarm',13,2);

-- module 'parentdeal'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'parentdeal','parentdeal_label',1,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'parentdeal','parentdeal_marketing_lastname',2,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'parentdeal','parentdeal_technical_lastname',3,1);

-- module 'list'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list', 'list_name', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list', 'list_subject', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list', 'list_nb_contact', 3, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list', 'usercreate', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list', 'timecreate', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list', 'userupdate', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list', 'timeupdate', 7, 1);

-- module 'list_contact'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'contact_lastname', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'contact_firstname', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'contactfunction_label', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'contact_title', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'kind_minilabel', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'kind_header', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'kind_lang', 7, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'company_name', 8, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'address', 9, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'service', 10, 0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'address1', 11, 0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'address2', 12, 0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'address3', 13, 0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'zipcode', 14, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'town', 15, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'expresspostal', 16, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'country_name', 17, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'contact_phone', 18, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'contact_homephone', 19, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'contact_mobilephone', 20, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'contact_fax', 21, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'publication_lang', 22, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'publication_title', 23, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'subscription_quantity', 24, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'contact_email', 25, 1);

-- module 'todo'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'todo', 'todo_title', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'todo', 'todo_priority', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'todo', 'todo_percent', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'todo', 'date_todo', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'todo', 'date_deadline', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'todo', 'todo_update', 6, 2);

-- module 'contract'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'contract', 'contract_label', 1, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'contract', 'contract_number', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'contract', 'contract_company_name', 3, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'contract', 'contracttype_label', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'contract', 'contract_priority', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'contract', 'contract_kind', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'contract', 'contract_format', 7, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'contract', 'contract_datebegin', 8, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'contract', 'contract_dateexp', 9, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'contract', 'contract_techmanager', 10, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'contract', 'contract_marketmanager', 11, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'contract', 'contract_archive', 12, 1);


-- module 'incident'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'incident_label', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'incident_company_name', 2, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'incident_logger_lastname', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'incident_owner_lastname', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'incident_priority', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'incident_status', 6, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'incident_date', 7, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'incident_duration', 8, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'timeupdate', 9, 1);

-- module 'account'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'account','account_label', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'account','account_bank', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'account','account_number', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'account','account_today', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'account','account_balance', 5, 1);

-- module 'invoice'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'invoice','invoice_label', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'invoice','invoice_archive', 2, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'invoice','invoice_number', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'invoice','invoice_date', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'invoice','invoice_expiration_date', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'invoice','invoice_payment_date', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'invoice','invoice_company', 7, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'invoice','invoice_inout', 8, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'invoice','invoice_amount_ht', 9, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'invoice','invoice_amount_ttc', 10, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'invoice','invoice_status', 11, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'invoice','invoice_deal', 12, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'invoice','invoice_project', 13, 1);

-- module 'payment'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'payment','payment_label', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'payment','payment_number', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'payment','payment_amount', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'payment','payment_expect_date', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'payment','payment_date', 5, 1);
-- sub module 'entrytemp'
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'entrytemp','entrytemp_date',1,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'entrytemp','entrytemp_type',2,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'entrytemp','entrytemp_amount',3,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'entrytemp','entrytemp_realdate',4,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'entrytemp','entrytemp_label',5,2);

-- modules 'time'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'time','date_task',1,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'time','timetask_company_name',2,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'time','tasktype_label',3,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'time','timetask_project_name',4,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'time','projecttask_label',5,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'time','timetask_label',6,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'time','timetask_length',7,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'time','timetask_id',8,1);

-- module 'project stat'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'time_proj','project_name',1,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'time_proj','company_name',2,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'time_proj','total_length',3,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'time_proj','total_before',4,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'time_proj','total_after',5,1);

-- ttype

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'time_tt','tasktype_label',1,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'time_tt','total_length',2,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'time_tt','total_before',3,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'time_tt','total_after',4,1);

-- modules 'project'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'project', 'project_name', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'project', 'project_company', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'project', 'project_tasktype', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'project', 'project_soldtime', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'project', 'project_estimatedtime', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'project', 'project_datebegin', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'project', 'project_dateend', 7, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'project', 'project_archive', 8, 1);

-- modules 'document'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'document','document_title',1,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'document','document_name',2,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'document','document_size',3,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'document','document_author',4,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'document','timecreate',5,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'document','timeupdate',6,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'document','documentcategory1_label',7,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'document','documentcategory2_label',8,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'document','documentmimetype_label',9,1);

-- module 'userobm'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_login', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_archive', 2, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_local', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_ext_id', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_lastname', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_firstname', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_perms', 7, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'datebegin', 8, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'timelastaccess', 9, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_phone', 10, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_phone2', 11, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_fax', 12, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_fax2', 13, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_email', 14, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_description', 15, 1);

-- module 'group'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'group_name', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'group_system', 2, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'group_privacy', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'group_local', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'group_ext_id', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'group_desc', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'group_email', 7, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'group_nb_user', 8, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'usercreate', 9, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'timecreate', 10, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'userupdate', 11, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'timeupdate', 12, 1);

-- module 'group_user'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group_user', 'group_user_lastname', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group_user', 'group_user_firstname', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group_user', 'group_user_phone', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group_user', 'group_user_email', 4, 1);

-- module 'group_group'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group_group', 'group_name', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group_group', 'group_desc', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group_group', 'group_email', 3, 1);

-- module 'import'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'import', 'import_name', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'import', 'import_datasource', 2, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'import', 'import_market', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'import', 'import_separator', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'import', 'import_enclosed', 5, 1);

-- module 'publication'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'publication', 'publication_title', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'publication', 'publicationtype_label', 2, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'publication', 'publication_year', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'publication', 'publication_lang', 4, 1);


-- module 'resource'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resource', 'resource_name', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resource', 'resource_description', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resource', 'resource_qty', 3, 1);

-- module 'resourcegroup'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup', 'rgroup_name', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup', 'rgroup_desc', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup', 'rgroup_nb_resource', 3, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup', 'usercreate', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup', 'timecreate', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup', 'userupdate', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup', 'timeupdate', 7, 1);


-- module 'resourcegroup_resource'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup_resource', 'resourcegroup_resource_name', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup_resource', 'resourcegroup_resource_desc', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup_resource', 'resourcegroup_resource_qty', 3, 1);

