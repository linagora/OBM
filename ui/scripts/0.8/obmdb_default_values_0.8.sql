--/////////////////////////////////////////////////////////////////////////////
-- OBM - File : obm_default_values_0.8.sql                                   //
--     - Desc : Insertion of Default values (database independant)           //
-- 2003-12-29 Pierre Baudracco                                               //
--/////////////////////////////////////////////////////////////////////////////
-- $Id$
--/////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Default Global preferences values (table GlobaPref)
-------------------------------------------------------------------------------

-- Delete current default values
DELETE FROM GlobalPref;

-- Session lifetime
INSERT INTO GlobalPref VALUES ('lifetime', '14400');
INSERT INTO GlobalPref VALUES ('session_cookie', '1');
INSERT INTO GlobalPref VALUES ('document_path', '/var/documents');


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

-- Company bookmark
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','last_company','0');

-- Contact bookmark
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','last_contact','0');

-- Parent Deal bookmark
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','last_parentdeal','0');

-- Deal bookmark
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','last_deal','0');

-- Project bookmark
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','last_project','0');

-- List bookmark
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','last_list','0');

-- Contract bookmark
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','last_contract','0');

-- Incident bookmark
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','last_incident','0');

-- Account bookmark
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','last_account','0');

-- Invoice bookmark
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','last_invoice','0');

-- Payment bookmark
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','last_payment','0');

-- User bookmark
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','last_user','0');

-- Group bookmark
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','last_group','0');

-- Day the week start 
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','set_day_weekstart','monday');

-- Todo sort order
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','set_todo','priority');

-- Todo top list
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','todo_1_id','0');
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','todo_2_id','0');
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','todo_3_id','0');
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','todo_4_id','0');
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values ('0','todo_5_id','0');

-------------------------------------------------------------------------------
-- Default values for the table 'DisplayPref'
-- user 0 represent default values (affected to new users for ex:)
--

-- Delete current default values
DELETE FROM DisplayPref where display_user_id='0';

-- module 'contact'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_lastname',1,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_firstname',2,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_archive',3,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_function',4,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_company_name',5,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_phone',6,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_homephone',7,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_mobilephone',8,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'contact','contact_email',9,1);

-- module 'company'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_name',1,2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_archive',2,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_contact_number',3,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_new_contact',4,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_deal_number',5,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','companytype_label',6,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','companyactivity_label',7,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_address1',8,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_phone',9,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_fax',10,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_email',11,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'company','company_web',12,1);

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
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','deal_archive',10,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','deal_todo',11,1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) values (0,'deal','deal_datealarm',12,2);

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

-- module 'todo'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'todo', 'todo_title', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'todo', 'todo_priority', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'todo', 'date_todo', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'todo', 'date_deadline', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'todo', 'todo_id', 5, 2);

-- module 'list_contact'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'contact_lastname', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'contact_firstname', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'contact_function', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'company_name', 4, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'company_address1', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'company_zipcode', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'company_town', 7, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'contact_phone', 8, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'contact_mobilephone', 9, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'list_contact', 'contact_email', 10, 1);

-- module 'contract'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'contract', 'contract_label', 1, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'contract', 'contract_number', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'contract', 'contract_company_name', 3, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'contract', 'contracttype_label', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'contract', 'contract_dateexp', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'contract', 'contract_techmanager', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'contract', 'contract_marketmanager', 7, 1);

-- module 'incident'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'incident_label', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'incident_company_name', 2, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'incident_logger_lastname', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'incident_owner_lastname', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'incident_priority', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'incident_status', 6, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'incident_date', 7, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0, 'incident', 'timeupdate', 8, 1);

-- module 'account'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'account','account_label', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'account','account_bank', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'account','account_number', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'account','account_today', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'account','account_balance', 5, 1);

-- module 'invoice'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'invoice','invoice_number', 1, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'invoice','invoice_date', 2, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'invoice','invoice_label', 3, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'invoice','invoice_amount_HT', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'invoice','invoice_amount_TTC', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'invoice','invoice_status', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'invoice','invoice_company', 7, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'invoice','invoice_deal', 8, 1);

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
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_lastname', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_firstname', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_perms', 5, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_timelastaccess', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_phone', 7, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'user', 'userobm_email', 8, 1);

-- module 'group'

INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'group_name', 1, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'group_system', 2, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'group_desc', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'group_email', 4, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'group_nb_user', 5, 2);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'usercreate', 6, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'timecreate', 7, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'userupdate', 8, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'group', 'timeupdate', 9, 1);

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
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'import', 'import_separator', 3, 1);
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (0,'import', 'import_enclosed', 4, 1);
