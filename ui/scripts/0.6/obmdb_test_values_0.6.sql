--/////////////////////////////////////////////////////////////////////////////
-- OBM - File : obmdb_test_values.sql                                        //
--     - Desc : Insertion of Test values (database independant)              //
-- 2002-12-17 Pierre Baudracco                                               //
--/////////////////////////////////////////////////////////////////////////////
-- $Id$
--/////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Default User creation
-------------------------------------------------------------------------------

INSERT INTO UserObm (userobm_login, userobm_password,userobm_perms, userobm_lastname, userobm_firstname) VALUES ('uadmin','padmin','admin', 'Admin Lastname', 'Admin Firstname');

INSERT INTO UserObm (userobm_login,userobm_password,userobm_perms) VALUES ('ueditor','peditor','editor');

INSERT INTO UserObm (userobm_login,userobm_password,userobm_perms) VALUES ('uuser','puser','user');


-------------------------------------------------------------------------------
-- User 1 Preferences (should be replaced by a script which import default)
-------------------------------------------------------------------------------

INSERT INTO UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (1,'set_theme','aliacom');
INSERT INTO UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (1,'set_lang','en');
INSERT INTO UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (1,'set_rows',10);
INSERT INTO UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (1,'set_display','no');
INSERT INTO UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (1,'set_debug',0);
INSERT INTO UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (1,'last_company',0); 
INSERT INTO UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (1,'last_deal',0); 
INSERT INTO UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (1,'last_contact',0); 
INSERT INTO UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (1,'last_contract', '0');
INSERT INTO UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (1,'last_incident', '0');
INSERT INTO UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (1,'last_account',0); 
INSERT INTO UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (1,'last_invoice', '0');
INSERT INTO UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (1,'last_payment', '0');
INSERT INTO UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (1,'order_servicecomputer','service_port');
INSERT INTO UserObmPref (userobmpref_user_id,userobmpref_option,userobmpref_value) VALUES (1,'set_day_weekstart','monday');


--
-- uadmin (user1) uservalues for the table 'DisplayPref'
--

-- module 'contact'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'contact','contact_lastname',1,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'contact','contact_firstname',2,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'contact','contact_function',3,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'contact','contact_company_name',4,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'contact','contact_phone',5,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'contact','contact_homephone',6,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'contact','contact_mobilephone',7,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'contact','contact_email',8,1);

-- module 'company'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'company','company_name',1,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'company','company_contacts',2,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'company','company_new_contact',3,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'company','companytype_label',4,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'company','company_address1',5,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'company','company_phone',6,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'company','company_fax',7,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'company','company_email',8,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'company','company_web',9,1);

-- module 'deal'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'deal','deal_label',1,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'deal','deal_company_name',2,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'deal','deal_company_zipcode',3,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'deal','dealtype_label',4,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'deal','tasktype_label',5,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'deal','dealstatus_label',6,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'deal','deal_marketingmanager',7,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'deal','deal_amount',8,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'deal','deal_hitrate',9,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'deal','deal_archive',10,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'deal','deal_todo',11,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'deal','deal_datealarm',12,2);

-- module 'parentdeal'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'parentdeal','parentdeal_label',1,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'parentdeal','parentdeal_marketing_lastname',2,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (1,'parentdeal','parentdeal_technical_lastname',3,1);

-- module 'list'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'list', 'list_name', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'list', 'list_subject', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'list', 'list_nb_contact', 3, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'list', 'usercreate', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'list', 'timecreate', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'list', 'userupdate', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'list', 'timeupdate', 7, 1);

-- module 'list_contact'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'list_contact', 'list_contact_lastname', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'list_contact', 'list_contact_firstname', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'list_contact', 'list_contact_function', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'list_contact', 'list_contact_company', 4, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'list_contact', 'list_contact_town', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'list_contact', 'list_contact_phone', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'list_contact', 'list_contact_mobilephone', 7, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'list_contact', 'list_contact_email', 8, 1);

-- module 'computer'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'computer','computer_ip', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'computer','computer_user', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'computer','computer_usercreate', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'computer','computer_timecreate', 4, 1);   
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'computer','computer_userupdate', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'computer','computer_timeupdate', 6, 1);       
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'computer','computer_auth_scan', 7, 1); 
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'computer','computer_date_lastscan', 8, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'computer','computer_comments', 9,1);      
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'computer','service_name', 1, 2);    
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'computer','service_port', 2, 2);      
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'computer','service_proto', 3, 2);        
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'computer','service_desc', 4, 1);  
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'computer','service_status', 5, 2);      
-- module 'account'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'account','account_label', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'account','account_bank', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'account','account_number', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'account','account_today', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'account','account_balance', 5, 1);

-- module 'invoice'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'invoice','invoice_number', 1, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'invoice','invoice_date', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'invoice','invoice_label', 3, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'invoice','invoice_amount_HT', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'invoice','invoice_amount_TTC', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'invoice','invoice_status', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'invoice','invoice_company', 7, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'invoice','invoice_deal', 8, 1);

-- module 'payment'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'payment','payment_label', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'payment','payment_number', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'payment','payment_amount', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'payment','payment_expect_date', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'payment','payment_date', 5, 1);
-- sub module 'entrytemp'
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'entrytemp','entrytemp_date',1,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'entrytemp','entrytemp_type',2,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'entrytemp','entrytemp_amount',3,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'entrytemp','entrytemp_realdate',4,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'entrytemp','entrytemp_label',5,2);

--modules timemanager

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'time','task_date',1,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'time','task_company_name',2,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'time','task_deal_label',3,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'time','task_label',4,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'time','tasktype_label',5,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'time','task_length',6,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'time','task_id',7,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'time_day','task_date',1,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'time_day','task_totallength',2,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'time_deal','task_totallength',4,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'time_deal','task_deal_label',3,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1,'time_deal','task_company_name',2,1);

--module 'contract'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'contract', 'contract_label', 1, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'contract', 'contract_number', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'contract', 'contract_company_name', 3, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'contract', 'contracttype_label', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'contract', 'contract_dateexp', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'contract', 'contract_techmanager', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'contract', 'contract_marketmanager', 7, 1);

--module 'incident'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'incident', 'incident_label', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'incident', 'incident_company_name', 2, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'incident', 'incident_priority', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'incident', 'incident_etat', 6, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'incident', 'incident_date', 7, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'incident', 'incident_owner_lastname', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'incident', 'incident_logger_lastname', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (1, 'incident', 'timeupdate', 8, 1);
