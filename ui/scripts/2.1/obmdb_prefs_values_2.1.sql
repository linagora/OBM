--/////////////////////////////////////////////////////////////////////////////
-- OBM - File : obm_prefs_values_2.1.sql                                     //
--     - Desc : Insertion of Preferences values (database independant)       //
-- 2007-04-23 Pierre Baudracco                                               //
--/////////////////////////////////////////////////////////////////////////////
-- $Id:$
--/////////////////////////////////////////////////////////////////////////////


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
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','set_theme','default');

-- Menu
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','set_menu','both');

-- Auto-Display list results
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','set_display','no');

-- # Rows displayed in a result page
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','set_rows','20');

-- Default Data Source
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','set_dsrc','0');

-- Date Input
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','set_date_upd','m/d/Y');

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

-- Calendar start hour
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','set_cal_first_hour','8');

-- Calendar last hour
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','set_cal_last_hour','20');

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
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_aka',2,0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_archive',3,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_contact_number',4,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_new_contact',5,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_deal_number',6,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_number',7,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_vat',8,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_siret',9,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','type_label',10,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','activity_label',11,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','nafcode_code',12,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_address',13,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_address1',14,0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_address2',15,0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_address3',16,0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_zipcode',17,0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_town',18,0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_expresspostal',19,0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','country_name',20,0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_phone',21,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_fax',22,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_email',23,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_web',24,1);

-- module 'contact'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_lastname',1,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_firstname',2,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_aka',3,0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_archive',4,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','function_label',5,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_title',6,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'contact', 'kind_minilabel', 7, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'contact', 'kind_header', 8, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'contact', 'kind_lang', 9, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','company_name',10,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','company_aka',11,0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_address',12,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'contact', 'contact_service', 13, 0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'contact', 'contact_address1', 14, 0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'contact', 'contact_address2', 15, 0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'contact', 'contact_address3', 16, 0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'contact', 'contact_zipcode', 17, 0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'contact', 'contact_town', 18, 0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'contact', 'contact_expresspostal', 19, 0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'contact', 'country_name', 20, 0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_phone',21,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_homephone',22,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_mobilephone',23,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_fax',24,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_email',25,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_date',26,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_mailing_ok',27,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_newsletter',28,1);

-- module 'lead'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'lead','lead_name',1,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'lead','company_name',2,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'lead','company_zipcode',3,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'lead','company_phone',4,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'lead','leadsource_label',5,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'lead','date',6,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'lead','manager',7,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'lead','datealarm',8,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'lead','lead_todo',9,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'lead','leadstatus_label',10,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'lead','lead_archive',11,1);

-- module 'deal'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','deal_label',1,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','deal_number',2,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','deal_company_name',3,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','deal_company_zipcode',4,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','dealtype_label',5,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','region_label',6,0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','tasktype_label',7,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','dealstatus_label',8,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','deal_marketingmanager',9,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','deal_source',10,0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','deal_commission',11,0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','deal_amount',12,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','deal_margin',13,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','deal_hitrate',14,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','deal_datebegin',15,0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','deal_dateproposal',16,0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','deal_dateexpected',17,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','expected_quarter',18,0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','sale_delay',19,0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','deal_dateend',20,0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','end_quarter',21,0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','deal_todo',22,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','deal_archive',23,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','deal_datealarm',24,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','deal_relation',25,0);

-- module 'parentdeal'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'parentdeal','parentdeal_label',1,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'parentdeal','parentdeal_marketing_lastname',2,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'parentdeal','parentdeal_technical_lastname',3,1);

-- module 'list'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list', 'list_name', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list', 'list_subject', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list', 'list_nb_contact', 3, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list', 'list_info_publication', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list', 'list_mode', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list', 'usercreate', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list', 'timecreate', 7, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list', 'userupdate', 8, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list', 'timeupdate', 9, 1);

-- module 'list_contact'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'contact_lastname', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'contact_firstname', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'function_label', 3, 1);
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
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'incident_reference', 2, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'incident_company_name', 3, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'contract_label', 4, 0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'incident_logger_lastname', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'incident_owner_lastname', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'incident_priority', 7, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'incident_status', 8, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'resolutiontype_label', 9, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'incident_date', 10, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'incident_duration', 11, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'timeupdate', 12, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'incident_archive', 13, 1);

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
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'invoice','invoice_credit_memo', 9, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'invoice','invoice_amount_ht', 10, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'invoice','invoice_amount_ttc', 11, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'invoice','invoice_status', 12, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'invoice','invoice_deal', 13, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'invoice','invoice_project', 14, 1);

-- module 'payment'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'payment','payment_date', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'payment','company_name', 2, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'payment','payment_amount', 3, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'payment','payment_number', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'payment','paymentkind_label', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'payment','payment_comment', 6, 1);

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
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'project', 'project_shortname', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'project', 'project_company', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'project', 'project_type', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'project', 'project_tasktype', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'project', 'project_code', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'project', 'project_soldtime', 7, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'project', 'project_estimatedtime', 8, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'project', 'project_datebegin', 9, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'project', 'project_dateend', 10, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'project', 'project_archive', 11, 1);

-- modules 'document'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'document','document_title',1,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'document','document_name',2,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'document','document_size',3,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'document','document_author',4,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'document','timecreate',5,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'document','timeupdate',6,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'document','documentmimetype_label',9,1);

-- module 'userobm'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_login', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_archive', 2, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_system', 3, 0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'domain_label', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_delegation', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_local', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_ext_id', 7, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_lastname', 8, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_firstname', 9, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_title', 10, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_perms', 11, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'datebegin', 12, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'timelastaccess', 13, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_phone', 14, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_phone2', 15, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_mobile', 16, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_fax', 17, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_fax2', 18, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_email', 19, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_mail_quota', 20, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_mail_login_date', 21, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_nomade', 22, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_web_perms', 23, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_samba_perms', 24, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_description', 25, 1);

-- module 'group'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'group_name', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'group_delegation', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'group_gid', 3, 0);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'group_system', 4, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'group_privacy', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'group_local', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'group_ext_id', 7, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'group_samba', 8, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'group_desc', 9, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'group_email', 10, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'group_mailing', 11, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'group_nb_user', 12, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'usercreate', 13, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'timecreate', 14, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'userupdate', 15, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'timeupdate', 16, 1);

-- module 'group_user'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group_user', 'group_user_login', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group_user', 'group_user_lastname', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group_user', 'group_user_firstname', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group_user', 'group_user_phone', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group_user', 'group_user_email', 5, 1);

-- module 'group_group'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group_group', 'group_name', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group_group', 'group_desc', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group_group', 'group_email', 3, 1);

-- module 'host'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'host', 'host_name', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'host', 'host_delegation', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'host', 'host_ip', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'host', 'host_samba', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'host', 'host_description', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'host', 'usercreate', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'host', 'timecreate', 7, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'host', 'userupdate', 8, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'host', 'timeupdate', 9, 1);

-- module 'mailshare'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'mailshare', 'mailshare_name', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'mailshare', 'mailshare_delegation', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'mailshare', 'mailshare_quota', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'mailshare', 'mailshare_email', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'mailshare', 'mailshare_description', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'mailshare', 'usercreate', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'mailshare', 'timecreate', 7, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'mailshare', 'userupdate', 8, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'mailshare', 'timeupdate', 9, 1);

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
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resource', 'resourcetype_label', 4, 1);

-- module 'resourcegroup'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup', 'rgroup_name', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup', 'rgroup_privacy', 2, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup', 'rgroup_desc', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup', 'rgroup_nb_resource', 4, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup', 'usercreate', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup', 'timecreate', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup', 'userupdate', 7, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup', 'timeupdate', 8, 1);


-- module 'resourcegroup_resource'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup_resource', 'resourcegroup_resource_name', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup_resource', 'resourcegroup_resource_desc', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'resourcegroup_resource', 'resourcegroup_resource_qty', 3, 1);


-- module 'domain'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'domain', 'domain_label', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'domain', 'domain_description', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'domain', 'domain_name', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'domain', 'domain_alias', 4, 1);


-- module 'mailserver'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'mailserver', 'host_name', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'mailserver', 'host_ip', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'mailserver', 'mailserver_imap', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'mailserver', 'mailserver_smtp_in', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'mailserver', 'mailserver_smtp_out', 5, 1);


-- module cv

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'cv', 'cv_title', 1, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'cv', 'lastname', 2, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'cv', 'firstname', 3, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'cv', 'timeupdate', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'cv', 'timecreate', 5, 1);

-- module cv_reference

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'cv_reference', 'project_name', 1, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'cv_reference', 'projectcv_role', 2, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'cv_reference', 'project_reference_date', 3, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'cv_reference', 'project_reference_duration', 4, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'cv_reference', 'project_reference_desc', 5, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'cv_reference', 'project_reference_tech', 6, 2);

-- module organizationalchart
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'organizationalchart', 'organizationalchart_name', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'organizationalchart', 'organizationalchart_description', 2, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'organizationalchart', 'organizationalchart_archive', 3, 2);
