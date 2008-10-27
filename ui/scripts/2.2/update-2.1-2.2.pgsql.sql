--
-- Set integer to boolean when necessary
--

-- Domain
ALTER TABLE Domain ADD COLUMN domain_global BOOLEAN DEFAULT FALSE;
ALTER TABLE Domain DROP COLUMN domain_mail_server_id;
ALTER TABLE Domain ADD COLUMN domain_mail_server_auto integer default NULL;

-- Global Domain
INSERT INTO Domain (domain_timecreate,domain_label,domain_description,domain_name,domain_global) VALUES  (NOW(), 'Global Domain', 'Virtual domain for managing domains', 'global.virt', TRUE);
UPDATE UserObm SET userobm_domain_id = (SELECT domain_id FROM Domain WHERE domain_global = TRUE) WHERE userobm_domain_id = 0;
UPDATE Host SET host_domain_id = (SELECT domain_id FROM Domain WHERE domain_global = TRUE) WHERE host_domain_id = 0;

-- P_Domain
ALTER TABLE P_Domain ADD COLUMN domain_global BOOLEAN DEFAULT FALSE;
-- Global Domain
INSERT INTO P_Domain (domain_timecreate,domain_label,domain_description,domain_name,domain_global) VALUES  (NOW(), 'Global Domain', 'Virtual domain for managing domains', 'global.virt', TRUE);
UPDATE P_UserObm SET userobm_domain_id = (SELECT domain_id FROM Domain WHERE domain_global = TRUE) WHERE userobm_domain_id = 0;
UPDATE P_Host SET host_domain_id = (SELECT domain_id FROM Domain WHERE domain_global = TRUE) WHERE host_domain_id = 0;

-- OGroup
ALTER TABLE OGroup ALTER COLUMN ogroup_parent_id DROP NOT NULL;

-- Contact
ALTER TABLE Contact ADD COLUMN contact_birthday_id INTEGER default NULL;

-- CalendarEvent

ALTER TABLE calendarevent ALTER COLUMN calendarevent_allday DROP DEFAULT;
ALTER TABLE calendarevent ALTER COLUMN calendarevent_allday TYPE BOOLEAN USING CASE calendarevent_allday WHEN 1 THEN TRUE ELSE FALSE END;
ALTER TABLE calendarevent ALTER COLUMN calendarevent_allday SET DEFAULT FALSE;
ALTER TABLE calendarevent ALTER COLUMN calendarevent_endrepeat SET DEFAULT NULL;
ALTER TABLE calendarevent ADD COLUMN calendarevent_timezone VARCHAR(255) DEFAULT 'GMT';
ALTER TABLE evententity ALTER COLUMN evententity_required DROP DEFAULT;
ALTER TABLE evententity ALTER COLUMN evententity_required TYPE BOOLEAN USING CASE evententity_required WHEN 1 THEN TRUE ELSE FALSE END;
ALTER TABLE evententity ALTER COLUMN evententity_required SET DEFAULT FALSE;

-- Preferences
ALTER TABLE DisplayPref DROP CONSTRAINT displaypref_pkey;
ALTER TABLE DisplayPref ADD CONSTRAINT displaypref_key  UNIQUE (display_user_id, display_entity, display_fieldname);
ALTER TABLE DisplayPref ADD COLUMN display_id serial PRIMARY KEY;

-- NOT NULL to NULL Convertion
ALTER TABLE UserObm ALTER COLUMN userobm_domain_id SET NOT NULL;
ALTER TABLE UserObmPref ALTER COLUMN userobmpref_user_id DROP NOT NULL;
ALTER TABLE UserObmPref ALTER COLUMN userobmpref_user_id SET default NULL;
ALTER TABLE DataSource ALTER COLUMN datasource_domain_id SET NOT NULL;
ALTER TABLE Country ALTER COLUMN country_domain_id SET NOT NULL;
ALTER TABLE Region ALTER COLUMN region_domain_id SET NOT NULL;
ALTER TABLE CompanyType ALTER COLUMN companytype_domain_id SET NOT NULL;
ALTER TABLE CompanyActivity ALTER COLUMN companyactivity_domain_id SET NOT NULL;
ALTER TABLE CompanyNafCode ALTER COLUMN companynafcode_domain_id SET NOT NULL;
ALTER TABLE Company ALTER COLUMN company_domain_id SET NOT NULL;
ALTER TABLE Company ALTER COLUMN company_datasource_id SET default NULL;
ALTER TABLE Contact ALTER COLUMN contact_domain_id SET NOT NULL;
ALTER TABLE Contact ALTER COLUMN contact_datasource_id SET default NULL;
ALTER TABLE EntityRight ADD COLUMN entityright_access INTEGER not null DEFAULT 0;
ALTER TABLE Kind ALTER COLUMN kind_domain_id SET NOT NULL;
ALTER TABLE ContactFunction ALTER COLUMN contactfunction_domain_id SET NOT NULL;
ALTER TABLE LeadSource ALTER COLUMN leadsource_domain_id SET NOT NULL;
ALTER TABLE LeadStatus ALTER COLUMN leadstatus_domain_id SET NOT NULL;
ALTER TABLE Lead ALTER COLUMN lead_domain_id SET NOT NULL;
ALTER TABLE Lead ALTER COLUMN lead_source_id SET default NULL;
ALTER TABLE Lead ALTER COLUMN lead_manager_id SET default NULL;
ALTER TABLE ParentDeal ALTER COLUMN parentdeal_domain_id SET NOT NULL;
ALTER TABLE Deal ALTER COLUMN deal_domain_id SET NOT NULL;
ALTER TABLE DealStatus ALTER COLUMN dealstatus_domain_id SET NOT NULL;
ALTER TABLE DealType ALTER COLUMN dealtype_domain_id SET NOT NULL;
ALTER TABLE DealCompanyRole ALTER COLUMN dealcompanyrole_domain_id SET NOT NULL;
ALTER TABLE List ALTER COLUMN list_domain_id SET NOT NULL;
ALTER TABLE CalendarEvent ALTER COLUMN calendarevent_domain_id SET NOT NULL;
ALTER TABLE CalendarEvent ALTER COLUMN calendarevent_category1_id SET default NULL;
ALTER TABLE CalendarCategory1 ALTER COLUMN calendarcategory1_domain_id SET NOT NULL;
ALTER TABLE Todo ALTER COLUMN todo_domain_id SET NOT NULL;
ALTER TABLE Publication ALTER COLUMN publication_domain_id SET NOT NULL;
ALTER TABLE PublicationType ALTER COLUMN publicationtype_domain_id SET NOT NULL;
ALTER TABLE Subscription ALTER COLUMN subscription_domain_id SET NOT NULL;
ALTER TABLE Document ALTER COLUMN document_domain_id SET NOT NULL;
ALTER TABLE DocumentMimeType ALTER COLUMN documentmimetype_domain_id SET NOT NULL;
ALTER TABLE Project ALTER COLUMN project_domain_id SET NOT NULL;
ALTER TABLE ProjectTask ALTER COLUMN projecttask_parenttask_id SET default NULL;
ALTER TABLE CV ALTER COLUMN cv_domain_id SET NOT NULL;
ALTER TABLE DefaultOdtTemplate ALTER COLUMN defaultodttemplate_domain_id SET NOT NULL;
ALTER TABLE TaskType ALTER COLUMN tasktype_domain_id SET NOT NULL;
ALTER TABLE Contract ALTER COLUMN contract_domain_id SET NOT NULL;
ALTER TABLE ContractType ALTER COLUMN contracttype_domain_id SET NOT NULL;
ALTER TABLE ContractPriority ALTER COLUMN contractpriority_domain_id SET NOT NULL;
ALTER TABLE ContractStatus ALTER COLUMN contractstatus_domain_id SET NOT NULL;
ALTER TABLE Incident ALTER COLUMN incident_domain_id SET NOT NULL;
ALTER TABLE Incident ALTER COLUMN incident_priority_id SET default NULL;
ALTER TABLE Incident ALTER COLUMN incident_status_id SET default NULL;
ALTER TABLE Incident ALTER COLUMN incident_resolutiontype_id SET default NULL;
ALTER TABLE IncidentPriority ALTER COLUMN incidentpriority_domain_id SET NOT NULL;
ALTER TABLE IncidentStatus ALTER COLUMN incidentstatus_domain_id SET NOT NULL;
ALTER TABLE IncidentResolutionType ALTER COLUMN incidentresolutiontype_domain_id SET NOT NULL;
ALTER TABLE Payment ALTER COLUMN payment_domain_id SET NOT NULL;
ALTER TABLE PaymentKind ALTER COLUMN paymentkind_domain_id SET NOT NULL;
ALTER TABLE Account ALTER COLUMN account_domain_id SET NOT NULL;
ALTER TABLE UGroup ALTER COLUMN group_domain_id SET NOT NULL;
ALTER TABLE OrganizationalChart ALTER COLUMN organizationalchart_domain_id SET NOT NULL;
ALTER TABLE OGroup ALTER COLUMN ogroup_domain_id SET NOT NULL;
ALTER TABLE OGroupEntity ALTER COLUMN ogroupentity_domain_id SET NOT NULL;
ALTER TABLE Import ALTER COLUMN import_domain_id SET NOT NULL;
ALTER TABLE Import ALTER COLUMN import_datasource_id SET default NULL;
ALTER TABLE Resource ALTER COLUMN resource_domain_id SET NOT NULL;
ALTER TABLE RGroup ALTER COLUMN rgroup_domain_id SET NOT NULL;
ALTER TABLE Host ALTER COLUMN host_domain_id SET NOT NULL;
ALTER TABLE Samba ALTER COLUMN samba_domain_id SET NOT NULL;
ALTER TABLE MailShare ALTER COLUMN mailshare_domain_id SET NOT NULL;
ALTER TABLE MailShare ALTER COLUMN mailshare_mail_server_id SET default NULL;
ALTER TABLE deal ALTER COLUMN deal_region_id DROP DEFAULT;
ALTER TABLE deal ALTER COLUMN deal_region_id DROP NOT NULL;
ALTER TABLE deal ALTER COLUMN deal_region_id SET DEFAULT NULL;
ALTER TABLE deal ALTER COLUMN deal_source_id DROP DEFAULT;
ALTER TABLE deal ALTER COLUMN deal_source_id DROP NOT NULL;
ALTER TABLE deal ALTER COLUMN deal_source_id SET DEFAULT NULL;
ALTER TABLE dealcompany ALTER COLUMN dealcompany_role_id DROP DEFAULT;
ALTER TABLE dealcompany ALTER COLUMN dealcompany_role_id DROP NOT NULL;
ALTER TABLE dealcompany ALTER COLUMN dealcompany_role_id SET DEFAULT NULL;
ALTER TABLE contract ALTER COLUMN contract_priority_id DROP DEFAULT;
ALTER TABLE contract ALTER COLUMN contract_priority_id DROP NOT NULL;
ALTER TABLE contract ALTER COLUMN contract_priority_id SET DEFAULT NULL;
ALTER TABLE contract ALTER COLUMN contract_status_id DROP DEFAULT;
ALTER TABLE contract ALTER COLUMN contract_status_id DROP NOT NULL;
ALTER TABLE contract ALTER COLUMN contract_status_id SET DEFAULT NULL;
ALTER TABLE document ALTER COLUMN document_mimetype_id DROP DEFAULT;
ALTER TABLE document ALTER COLUMN document_mimetype_id DROP NOT NULL;
ALTER TABLE document ALTER COLUMN document_mimetype_id SET DEFAULT NULL;
ALTER TABLE lead ALTER COLUMN lead_contact_id DROP DEFAULT;
ALTER TABLE lead ALTER COLUMN lead_contact_id DROP NOT NULL;
ALTER TABLE lead ALTER COLUMN lead_contact_id SET DEFAULT NULL;
ALTER TABLE payment ALTER COLUMN payment_company_id DROP DEFAULT;
ALTER TABLE payment ALTER COLUMN payment_company_id DROP NOT NULL;
ALTER TABLE payment ALTER COLUMN payment_company_id SET DEFAULT NULL;
ALTER TABLE payment ALTER COLUMN payment_paymentkind_id DROP DEFAULT;
ALTER TABLE payment ALTER COLUMN payment_paymentkind_id DROP NOT NULL;
ALTER TABLE payment ALTER COLUMN payment_paymentkind_id SET DEFAULT NULL;
ALTER TABLE projectclosing ALTER COLUMN projectclosing_usercreate DROP DEFAULT;
ALTER TABLE projectclosing ALTER COLUMN projectclosing_usercreate DROP NOT NULL;
ALTER TABLE projectclosing ALTER COLUMN projectclosing_usercreate SET DEFAULT NULL;
ALTER TABLE subscription ALTER COLUMN subscription_reception_id DROP DEFAULT;
ALTER TABLE subscription ALTER COLUMN subscription_reception_id DROP NOT NULL;
ALTER TABLE subscription ALTER COLUMN subscription_reception_id SET DEFAULT NULL;
ALTER TABLE userobm ALTER COLUMN userobm_host_id DROP DEFAULT;
ALTER TABLE userobm ALTER COLUMN userobm_host_id SET DEFAULT NULL;
ALTER TABLE displaypref ALTER COLUMN display_user_id DROP DEFAULT;
ALTER TABLE displaypref ALTER COLUMN display_user_id DROP NOT NULL;
ALTER TABLE displaypref ALTER COLUMN display_user_id SET DEFAULT NULL;
ALTER TABLE UGroup ALTER COLUMN group_manager_id DROP DEFAULT;
ALTER TABLE UGroup ALTER COLUMN group_manager_id SET DEFAULT NULL;
ALTER TABLE P_EntityRight ADD COLUMN entityright_acces INTEGER not null DEFAULT 0;

--
-- Add tables structures around Profiles
--

--
-- Table structure for table 'Profile'
--

CREATE TABLE Profile (
	profile_id		serial,
	profile_domain_id	integer NOT NULL,
	profile_timeupdate	timestamp,
	profile_timecreate	timestamp,
	profile_userupdate	integer default null,
	profile_usercreate      integer default null,
	profile_name		varchar(64) default null,
	PRIMARY KEY (profile_id)
);

--
-- Table structure for table 'ProfileModule'
--

CREATE TABLE ProfileModule (
	profilemodule_id		serial,
	profilemodule_domain_id		integer NOT NULL,
	profilemodule_profile_id	integer default NULL,
	profilemodule_module_name	varchar(64) NOT NULL default '',
	profilemodule_right		integer default NULL,
	PRIMARY KEY (profilemodule_id)
);

--
-- Table structure for table `ProfileSection`
--

CREATE TABLE ProfileSection (
	profilesection_id			serial,
	profilesection_domain_id	integer NOT NULL,
	profilesection_profile_id	integer default NULL,
	profilesection_section_name	varchar(64) NOT NULL default '',
	profilesection_show			smallint default NULL,
	PRIMARY KEY (profilesection_id)
);

--
-- Table structure for table `ProfileProperty`
--

CREATE TABLE ProfileProperty (
	profileproperty_id 			serial,
	profileproperty_type		varchar(32) default NULL,
	profileproperty_default		text default NULL,
	profileproperty_readonly	smallint default 0,
	profileproperty_name		varchar(32) NOT NULL default '',
	PRIMARY KEY (profileproperty_id)
);

--
-- Table structure for table `ProfilePropertyValue`
--

CREATE TABLE ProfilePropertyValue (
	profilepropertyvalue_id				serial,
	profilepropertyvalue_profile_id		integer default NULL,
	profilepropertyvalue_property_id	integer default NULL,
	profilepropertyvalue_property_value	text NOT NULL default '',
	PRIMARY KEY (profilepropertyvalue_id)
);

-------------------------------------------------------------------------------
-- Default Profile properties
-------------------------------------------------------------------------------
INSERT INTO ProfileProperty (profileproperty_name, profileproperty_type, profileproperty_default, profileproperty_readonly) VALUES ('update_state', 'integer', 1, 1);
INSERT INTO ProfileProperty (profileproperty_name, profileproperty_type, profileproperty_default) VALUES ('level', 'integer', 3);
INSERT INTO ProfileProperty (profileproperty_name, profileproperty_type, profileproperty_default) VALUES ('level_managepeers', 'integer', 0);
INSERT INTO ProfileProperty (profileproperty_name, profileproperty_type, profileproperty_default) VALUES ('access_restriction', 'text', 'ALLOW_ALL');
INSERT INTO ProfileProperty (profileproperty_name, profileproperty_type, profileproperty_default) VALUES ('admin_realm', 'text', '');
INSERT INTO ProfileProperty (profileproperty_name, profileproperty_type, profileproperty_default, profileproperty_readonly) VALUES ('last_public_contact_export', 'timestamp', 0, 1);


-- Foreign key from account_domain_id to domain_id
DELETE FROM Account WHERE account_domain_id NOT IN (SELECT domain_id FROM Domain) AND account_domain_id IS NOT NULL;
ALTER TABLE Account ADD CONSTRAINT account_domain_id_domain_id_fkey FOREIGN KEY (account_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from account_usercreate to userobm_id
UPDATE Account SET account_usercreate = NULL WHERE account_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND account_usercreate IS NOT NULL;
ALTER TABLE Account ADD CONSTRAINT account_usercreate_userobm_id_fkey FOREIGN KEY (account_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from account_userupdate to userobm_id
UPDATE Account SET account_userupdate = NULL WHERE account_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND account_userupdate IS NOT NULL;
ALTER TABLE Account ADD CONSTRAINT account_userupdate_userobm_id_fkey FOREIGN KEY (account_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from activeuserobm_userobm_id to userobm_id
DELETE FROM ActiveUserObm WHERE activeuserobm_userobm_id NOT IN (SELECT userobm_id FROM UserObm) AND activeuserobm_userobm_id IS NOT NULL;
ALTER TABLE ActiveUserObm ADD CONSTRAINT activeuserobm_userobm_id_userobm_id_fkey FOREIGN KEY (activeuserobm_userobm_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from cv_domain_id to domain_id
DELETE FROM CV WHERE cv_domain_id NOT IN (SELECT domain_id FROM Domain) AND cv_domain_id IS NOT NULL;
ALTER TABLE CV ADD CONSTRAINT cv_domain_id_domain_id_fkey FOREIGN KEY (cv_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from cv_userobm_id to userobm_id
DELETE FROM CV WHERE cv_userobm_id NOT IN (SELECT userobm_id FROM UserObm) AND cv_userobm_id IS NOT NULL;
ALTER TABLE CV ADD CONSTRAINT cv_userobm_id_userobm_id_fkey FOREIGN KEY (cv_userobm_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from cv_userupdate to userobm_id
UPDATE CV SET cv_userupdate = NULL WHERE cv_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND cv_userupdate IS NOT NULL;
ALTER TABLE CV ADD CONSTRAINT cv_userupdate_userobm_id_fkey FOREIGN KEY (cv_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from cv_usercreate to userobm_id
UPDATE CV SET cv_usercreate = NULL WHERE cv_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND cv_usercreate IS NOT NULL;
ALTER TABLE CV ADD CONSTRAINT cv_usercreate_userobm_id_fkey FOREIGN KEY (cv_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from calendaralert_event_id to calendarevent_id
DELETE FROM CalendarAlert WHERE calendaralert_event_id NOT IN (SELECT calendarevent_id FROM CalendarEvent) AND calendaralert_event_id IS NOT NULL;
ALTER TABLE CalendarAlert ADD CONSTRAINT calendaralert_event_id_calendarevent_id_fkey FOREIGN KEY (calendaralert_event_id) REFERENCES CalendarEvent(calendarevent_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from calendaralert_user_id to userobm_id
DELETE FROM CalendarAlert WHERE calendaralert_user_id NOT IN (SELECT userobm_id FROM UserObm) AND calendaralert_user_id IS NOT NULL;
ALTER TABLE CalendarAlert ADD CONSTRAINT calendaralert_user_id_userobm_id_fkey FOREIGN KEY (calendaralert_user_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from calendaralert_userupdate to userobm_id
UPDATE CalendarAlert SET calendaralert_userupdate = NULL WHERE calendaralert_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND calendaralert_userupdate IS NOT NULL;
ALTER TABLE CalendarAlert ADD CONSTRAINT calendaralert_userupdate_userobm_id_fkey FOREIGN KEY (calendaralert_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from calendaralert_usercreate to userobm_id
UPDATE CalendarAlert SET calendaralert_usercreate = NULL WHERE calendaralert_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND calendaralert_usercreate IS NOT NULL;
ALTER TABLE CalendarAlert ADD CONSTRAINT calendaralert_usercreate_userobm_id_fkey FOREIGN KEY (calendaralert_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from calendarcategory1_domain_id to domain_id
DELETE FROM CalendarCategory1 WHERE calendarcategory1_domain_id NOT IN (SELECT domain_id FROM Domain) AND calendarcategory1_domain_id IS NOT NULL;
ALTER TABLE CalendarCategory1 ADD CONSTRAINT calendarcategory1_domain_id_domain_id_fkey FOREIGN KEY (calendarcategory1_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from calendarcategory1_userupdate to userobm_id
UPDATE CalendarCategory1 SET calendarcategory1_userupdate = NULL WHERE calendarcategory1_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND calendarcategory1_userupdate IS NOT NULL;
ALTER TABLE CalendarCategory1 ADD CONSTRAINT calendarcategory1_userupdate_userobm_id_fkey FOREIGN KEY (calendarcategory1_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from calendarcategory1_usercreate to userobm_id
UPDATE CalendarCategory1 SET calendarcategory1_usercreate = NULL WHERE calendarcategory1_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND calendarcategory1_usercreate IS NOT NULL;
ALTER TABLE CalendarCategory1 ADD CONSTRAINT calendarcategory1_usercreate_userobm_id_fkey FOREIGN KEY (calendarcategory1_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from calendarevent_domain_id to domain_id
DELETE FROM CalendarEvent WHERE calendarevent_domain_id NOT IN (SELECT domain_id FROM Domain) AND calendarevent_domain_id IS NOT NULL;
ALTER TABLE CalendarEvent ADD CONSTRAINT calendarevent_domain_id_domain_id_fkey FOREIGN KEY (calendarevent_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from calendarevent_owner to userobm_id
DELETE FROM CalendarEvent WHERE calendarevent_owner NOT IN (SELECT userobm_id FROM UserObm) AND calendarevent_owner IS NOT NULL;
ALTER TABLE CalendarEvent ADD CONSTRAINT calendarevent_owner_userobm_id_fkey FOREIGN KEY (calendarevent_owner) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from calendarevent_userupdate to userobm_id
UPDATE CalendarEvent SET calendarevent_userupdate = NULL WHERE calendarevent_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND calendarevent_userupdate IS NOT NULL;
ALTER TABLE CalendarEvent ADD CONSTRAINT calendarevent_userupdate_userobm_id_fkey FOREIGN KEY (calendarevent_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from calendarevent_usercreate to userobm_id
UPDATE CalendarEvent SET calendarevent_usercreate = NULL WHERE calendarevent_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND calendarevent_usercreate IS NOT NULL;
ALTER TABLE CalendarEvent ADD CONSTRAINT calendarevent_usercreate_userobm_id_fkey FOREIGN KEY (calendarevent_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from calendarevent_category1_id to calendarcategory1_id
UPDATE CalendarEvent SET calendarevent_category1_id = NULL WHERE calendarevent_category1_id NOT IN (SELECT calendarcategory1_id FROM CalendarCategory1) AND calendarevent_category1_id IS NOT NULL;
ALTER TABLE CalendarEvent ADD CONSTRAINT calendarevent_category1_id_calendarcategory1_id_fkey FOREIGN KEY (calendarevent_category1_id) REFERENCES CalendarCategory1(calendarcategory1_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from calendarexception_event_id to calendarevent_id
DELETE FROM CalendarException WHERE calendarexception_event_id NOT IN (SELECT calendarevent_id FROM CalendarEvent) AND calendarexception_event_id IS NOT NULL;
ALTER TABLE CalendarException ADD CONSTRAINT calendarexception_event_id_calendarevent_id_fkey FOREIGN KEY (calendarexception_event_id) REFERENCES CalendarEvent(calendarevent_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from calendarexception_userupdate to userobm_id
UPDATE CalendarException SET calendarexception_userupdate = NULL WHERE calendarexception_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND calendarexception_userupdate IS NOT NULL;
ALTER TABLE CalendarException ADD CONSTRAINT calendarexception_userupdate_userobm_id_fkey FOREIGN KEY (calendarexception_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from calendarexception_usercreate to userobm_id
UPDATE CalendarException SET calendarexception_usercreate = NULL WHERE calendarexception_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND calendarexception_usercreate IS NOT NULL;
ALTER TABLE CalendarException ADD CONSTRAINT calendarexception_usercreate_userobm_id_fkey FOREIGN KEY (calendarexception_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from category_domain_id to domain_id
DELETE FROM Category WHERE category_domain_id NOT IN (SELECT domain_id FROM Domain) AND category_domain_id IS NOT NULL;
ALTER TABLE Category ADD CONSTRAINT category_domain_id_domain_id_fkey FOREIGN KEY (category_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from category_userupdate to userobm_id
UPDATE Category SET category_userupdate = NULL WHERE category_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND category_userupdate IS NOT NULL;
ALTER TABLE Category ADD CONSTRAINT category_userupdate_userobm_id_fkey FOREIGN KEY (category_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from category_usercreate to userobm_id
UPDATE Category SET category_usercreate = NULL WHERE category_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND category_usercreate IS NOT NULL;
ALTER TABLE Category ADD CONSTRAINT category_usercreate_userobm_id_fkey FOREIGN KEY (category_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from categorylink_category_id to category_id
DELETE FROM CategoryLink WHERE categorylink_category_id NOT IN (SELECT category_id FROM Category) AND categorylink_category_id IS NOT NULL;
ALTER TABLE CategoryLink ADD CONSTRAINT categorylink_category_id_category_id_fkey FOREIGN KEY (categorylink_category_id) REFERENCES Category(category_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from company_domain_id to domain_id
DELETE FROM Company WHERE company_domain_id NOT IN (SELECT domain_id FROM Domain) AND company_domain_id IS NOT NULL;
ALTER TABLE Company ADD CONSTRAINT company_domain_id_domain_id_fkey FOREIGN KEY (company_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from company_userupdate to userobm_id
UPDATE Company SET company_userupdate = NULL WHERE company_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND company_userupdate IS NOT NULL;
ALTER TABLE Company ADD CONSTRAINT company_userupdate_userobm_id_fkey FOREIGN KEY (company_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from company_usercreate to userobm_id
UPDATE Company SET company_usercreate = NULL WHERE company_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND company_usercreate IS NOT NULL;
ALTER TABLE Company ADD CONSTRAINT company_usercreate_userobm_id_fkey FOREIGN KEY (company_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from company_datasource_id to datasource_id
UPDATE Company SET company_datasource_id = NULL WHERE company_datasource_id NOT IN (SELECT datasource_id FROM DataSource) AND company_datasource_id IS NOT NULL;
ALTER TABLE Company ADD CONSTRAINT company_datasource_id_datasource_id_fkey FOREIGN KEY (company_datasource_id) REFERENCES DataSource(datasource_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from company_type_id to companytype_id
UPDATE Company SET company_type_id = NULL WHERE company_type_id NOT IN (SELECT companytype_id FROM CompanyType) AND company_type_id IS NOT NULL;
ALTER TABLE Company ADD CONSTRAINT company_type_id_companytype_id_fkey FOREIGN KEY (company_type_id) REFERENCES CompanyType(companytype_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from company_activity_id to companyactivity_id
UPDATE Company SET company_activity_id = NULL WHERE company_activity_id NOT IN (SELECT companyactivity_id FROM CompanyActivity) AND company_activity_id IS NOT NULL;
ALTER TABLE Company ADD CONSTRAINT company_activity_id_companyactivity_id_fkey FOREIGN KEY (company_activity_id) REFERENCES CompanyActivity(companyactivity_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from company_nafcode_id to companynafcode_id
UPDATE Company SET company_nafcode_id = NULL WHERE company_nafcode_id NOT IN (SELECT companynafcode_id FROM CompanyNafCode) AND company_nafcode_id IS NOT NULL;
ALTER TABLE Company ADD CONSTRAINT company_nafcode_id_companynafcode_id_fkey FOREIGN KEY (company_nafcode_id) REFERENCES CompanyNafCode(companynafcode_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from company_marketingmanager_id to userobm_id
UPDATE Company SET company_marketingmanager_id = NULL WHERE company_marketingmanager_id NOT IN (SELECT userobm_id FROM UserObm) AND company_marketingmanager_id IS NOT NULL;
ALTER TABLE Company ADD CONSTRAINT company_marketingmanager_id_userobm_id_fkey FOREIGN KEY (company_marketingmanager_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from companyactivity_domain_id to domain_id
DELETE FROM CompanyActivity WHERE companyactivity_domain_id NOT IN (SELECT domain_id FROM Domain) AND companyactivity_domain_id IS NOT NULL;
ALTER TABLE CompanyActivity ADD CONSTRAINT companyactivity_domain_id_domain_id_fkey FOREIGN KEY (companyactivity_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from companyactivity_userupdate to userobm_id
UPDATE CompanyActivity SET companyactivity_userupdate = NULL WHERE companyactivity_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND companyactivity_userupdate IS NOT NULL;
ALTER TABLE CompanyActivity ADD CONSTRAINT companyactivity_userupdate_userobm_id_fkey FOREIGN KEY (companyactivity_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from companyactivity_usercreate to userobm_id
UPDATE CompanyActivity SET companyactivity_usercreate = NULL WHERE companyactivity_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND companyactivity_usercreate IS NOT NULL;
ALTER TABLE CompanyActivity ADD CONSTRAINT companyactivity_usercreate_userobm_id_fkey FOREIGN KEY (companyactivity_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from companynafcode_domain_id to domain_id
DELETE FROM CompanyNafCode WHERE companynafcode_domain_id NOT IN (SELECT domain_id FROM Domain) AND companynafcode_domain_id IS NOT NULL;
ALTER TABLE CompanyNafCode ADD CONSTRAINT companynafcode_domain_id_domain_id_fkey FOREIGN KEY (companynafcode_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from companynafcode_userupdate to userobm_id
UPDATE CompanyNafCode SET companynafcode_userupdate = NULL WHERE companynafcode_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND companynafcode_userupdate IS NOT NULL;
ALTER TABLE CompanyNafCode ADD CONSTRAINT companynafcode_userupdate_userobm_id_fkey FOREIGN KEY (companynafcode_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from companynafcode_usercreate to userobm_id
UPDATE CompanyNafCode SET companynafcode_usercreate = NULL WHERE companynafcode_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND companynafcode_usercreate IS NOT NULL;
ALTER TABLE CompanyNafCode ADD CONSTRAINT companynafcode_usercreate_userobm_id_fkey FOREIGN KEY (companynafcode_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from companytype_domain_id to domain_id
DELETE FROM CompanyType WHERE companytype_domain_id NOT IN (SELECT domain_id FROM Domain) AND companytype_domain_id IS NOT NULL;
ALTER TABLE CompanyType ADD CONSTRAINT companytype_domain_id_domain_id_fkey FOREIGN KEY (companytype_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from companytype_userupdate to userobm_id
UPDATE CompanyType SET companytype_userupdate = NULL WHERE companytype_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND companytype_userupdate IS NOT NULL;
ALTER TABLE CompanyType ADD CONSTRAINT companytype_userupdate_userobm_id_fkey FOREIGN KEY (companytype_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from companytype_usercreate to userobm_id
UPDATE CompanyType SET companytype_usercreate = NULL WHERE companytype_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND companytype_usercreate IS NOT NULL;
ALTER TABLE CompanyType ADD CONSTRAINT companytype_usercreate_userobm_id_fkey FOREIGN KEY (companytype_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contact_domain_id to domain_id
DELETE FROM Contact WHERE contact_domain_id NOT IN (SELECT domain_id FROM Domain) AND contact_domain_id IS NOT NULL;
ALTER TABLE Contact ADD CONSTRAINT contact_domain_id_domain_id_fkey FOREIGN KEY (contact_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contact_company_id to company_id
DELETE FROM Contact WHERE contact_company_id NOT IN (SELECT company_id FROM Company) AND contact_company_id IS NOT NULL;
ALTER TABLE Contact ADD CONSTRAINT contact_company_id_company_id_fkey FOREIGN KEY (contact_company_id) REFERENCES Company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contact_userupdate to userobm_id
UPDATE Contact SET contact_userupdate = NULL WHERE contact_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND contact_userupdate IS NOT NULL;
ALTER TABLE Contact ADD CONSTRAINT contact_userupdate_userobm_id_fkey FOREIGN KEY (contact_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contact_usercreate to userobm_id
UPDATE Contact SET contact_usercreate = NULL WHERE contact_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND contact_usercreate IS NOT NULL;
ALTER TABLE Contact ADD CONSTRAINT contact_usercreate_userobm_id_fkey FOREIGN KEY (contact_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contact_datasource_id to datasource_id
UPDATE Contact SET contact_datasource_id = NULL WHERE contact_datasource_id NOT IN (SELECT datasource_id FROM DataSource) AND contact_datasource_id IS NOT NULL;
ALTER TABLE Contact ADD CONSTRAINT contact_datasource_id_datasource_id_fkey FOREIGN KEY (contact_datasource_id) REFERENCES DataSource(datasource_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contact_kind_id to kind_id
UPDATE Contact SET contact_kind_id = NULL WHERE contact_kind_id NOT IN (SELECT kind_id FROM Kind) AND contact_kind_id IS NOT NULL;
ALTER TABLE Contact ADD CONSTRAINT contact_kind_id_kind_id_fkey FOREIGN KEY (contact_kind_id) REFERENCES Kind(kind_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contact_marketingmanager_id to userobm_id
UPDATE Contact SET contact_marketingmanager_id = NULL WHERE contact_marketingmanager_id NOT IN (SELECT userobm_id FROM UserObm) AND contact_marketingmanager_id IS NOT NULL;
ALTER TABLE Contact ADD CONSTRAINT contact_marketingmanager_id_userobm_id_fkey FOREIGN KEY (contact_marketingmanager_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contact_function_id to contactfunction_id
UPDATE Contact SET contact_function_id = NULL WHERE contact_function_id NOT IN (SELECT contactfunction_id FROM ContactFunction) AND contact_function_id IS NOT NULL;
ALTER TABLE Contact ADD CONSTRAINT contact_function_id_contactfunction_id_fkey FOREIGN KEY (contact_function_id) REFERENCES ContactFunction(contactfunction_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contactfunction_domain_id to domain_id
DELETE FROM ContactFunction WHERE contactfunction_domain_id NOT IN (SELECT domain_id FROM Domain) AND contactfunction_domain_id IS NOT NULL;
ALTER TABLE ContactFunction ADD CONSTRAINT contactfunction_domain_id_domain_id_fkey FOREIGN KEY (contactfunction_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contactfunction_userupdate to userobm_id
UPDATE ContactFunction SET contactfunction_userupdate = NULL WHERE contactfunction_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND contactfunction_userupdate IS NOT NULL;
ALTER TABLE ContactFunction ADD CONSTRAINT contactfunction_userupdate_userobm_id_fkey FOREIGN KEY (contactfunction_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contactfunction_usercreate to userobm_id
UPDATE ContactFunction SET contactfunction_usercreate = NULL WHERE contactfunction_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND contactfunction_usercreate IS NOT NULL;
ALTER TABLE ContactFunction ADD CONSTRAINT contactfunction_usercreate_userobm_id_fkey FOREIGN KEY (contactfunction_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contactlist_list_id to list_id
DELETE FROM ContactList WHERE contactlist_list_id NOT IN (SELECT list_id FROM List) AND contactlist_list_id IS NOT NULL;
ALTER TABLE ContactList ADD CONSTRAINT contactlist_list_id_list_id_fkey FOREIGN KEY (contactlist_list_id) REFERENCES List(list_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contactlist_contact_id to contact_id
DELETE FROM ContactList WHERE contactlist_contact_id NOT IN (SELECT contact_id FROM Contact) AND contactlist_contact_id IS NOT NULL;
ALTER TABLE ContactList ADD CONSTRAINT contactlist_contact_id_contact_id_fkey FOREIGN KEY (contactlist_contact_id) REFERENCES Contact(contact_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contract_domain_id to domain_id
DELETE FROM Contract WHERE contract_domain_id NOT IN (SELECT domain_id FROM Domain) AND contract_domain_id IS NOT NULL;
ALTER TABLE Contract ADD CONSTRAINT contract_domain_id_domain_id_fkey FOREIGN KEY (contract_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contract_deal_id to deal_id
DELETE FROM Contract WHERE contract_deal_id NOT IN (SELECT deal_id FROM Deal) AND contract_deal_id IS NOT NULL;
ALTER TABLE Contract ADD CONSTRAINT contract_deal_id_deal_id_fkey FOREIGN KEY (contract_deal_id) REFERENCES Deal(deal_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contract_company_id to company_id
DELETE FROM Contract WHERE contract_company_id NOT IN (SELECT company_id FROM Company) AND contract_company_id IS NOT NULL;
ALTER TABLE Contract ADD CONSTRAINT contract_company_id_company_id_fkey FOREIGN KEY (contract_company_id) REFERENCES Company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contract_userupdate to userobm_id
UPDATE Contract SET contract_userupdate = NULL WHERE contract_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND contract_userupdate IS NOT NULL;
ALTER TABLE Contract ADD CONSTRAINT contract_userupdate_userobm_id_fkey FOREIGN KEY (contract_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contract_usercreate to userobm_id
UPDATE Contract SET contract_usercreate = NULL WHERE contract_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND contract_usercreate IS NOT NULL;
ALTER TABLE Contract ADD CONSTRAINT contract_usercreate_userobm_id_fkey FOREIGN KEY (contract_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contract_type_id to contracttype_id
UPDATE Contract SET contract_type_id = NULL WHERE contract_type_id NOT IN (SELECT contracttype_id FROM ContractType) AND contract_type_id IS NOT NULL;
ALTER TABLE Contract ADD CONSTRAINT contract_type_id_contracttype_id_fkey FOREIGN KEY (contract_type_id) REFERENCES ContractType(contracttype_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contract_priority_id to contractpriority_id
UPDATE Contract SET contract_priority_id = NULL WHERE contract_priority_id NOT IN (SELECT contractpriority_id FROM ContractPriority) AND contract_priority_id IS NOT NULL;
ALTER TABLE Contract ADD CONSTRAINT contract_priority_id_contractpriority_id_fkey FOREIGN KEY (contract_priority_id) REFERENCES ContractPriority(contractpriority_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contract_status_id to contractstatus_id
UPDATE Contract SET contract_status_id = NULL WHERE contract_status_id NOT IN (SELECT contractstatus_id FROM ContractStatus) AND contract_status_id IS NOT NULL;
ALTER TABLE Contract ADD CONSTRAINT contract_status_id_contractstatus_id_fkey FOREIGN KEY (contract_status_id) REFERENCES ContractStatus(contractstatus_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contract_contact1_id to contact_id
UPDATE Contract SET contract_contact1_id = NULL WHERE contract_contact1_id NOT IN (SELECT contact_id FROM Contact) AND contract_contact1_id IS NOT NULL;
ALTER TABLE Contract ADD CONSTRAINT contract_contact1_id_contact_id_fkey FOREIGN KEY (contract_contact1_id) REFERENCES Contact(contact_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contract_contact2_id to contact_id
UPDATE Contract SET contract_contact2_id = NULL WHERE contract_contact2_id NOT IN (SELECT contact_id FROM Contact) AND contract_contact2_id IS NOT NULL;
ALTER TABLE Contract ADD CONSTRAINT contract_contact2_id_contact_id_fkey FOREIGN KEY (contract_contact2_id) REFERENCES Contact(contact_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contract_techmanager_id to userobm_id
UPDATE Contract SET contract_techmanager_id = NULL WHERE contract_techmanager_id NOT IN (SELECT userobm_id FROM UserObm) AND contract_techmanager_id IS NOT NULL;
ALTER TABLE Contract ADD CONSTRAINT contract_techmanager_id_userobm_id_fkey FOREIGN KEY (contract_techmanager_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contract_marketmanager_id to userobm_id
UPDATE Contract SET contract_marketmanager_id = NULL WHERE contract_marketmanager_id NOT IN (SELECT userobm_id FROM UserObm) AND contract_marketmanager_id IS NOT NULL;
ALTER TABLE Contract ADD CONSTRAINT contract_marketmanager_id_userobm_id_fkey FOREIGN KEY (contract_marketmanager_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contractpriority_domain_id to domain_id
DELETE FROM ContractPriority WHERE contractpriority_domain_id NOT IN (SELECT domain_id FROM Domain) AND contractpriority_domain_id IS NOT NULL;
ALTER TABLE ContractPriority ADD CONSTRAINT contractpriority_domain_id_domain_id_fkey FOREIGN KEY (contractpriority_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contractpriority_userupdate to userobm_id
UPDATE ContractPriority SET contractpriority_userupdate = NULL WHERE contractpriority_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND contractpriority_userupdate IS NOT NULL;
ALTER TABLE ContractPriority ADD CONSTRAINT contractpriority_userupdate_userobm_id_fkey FOREIGN KEY (contractpriority_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contractpriority_usercreate to userobm_id
UPDATE ContractPriority SET contractpriority_usercreate = NULL WHERE contractpriority_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND contractpriority_usercreate IS NOT NULL;
ALTER TABLE ContractPriority ADD CONSTRAINT contractpriority_usercreate_userobm_id_fkey FOREIGN KEY (contractpriority_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contractstatus_domain_id to domain_id
DELETE FROM ContractStatus WHERE contractstatus_domain_id NOT IN (SELECT domain_id FROM Domain) AND contractstatus_domain_id IS NOT NULL;
ALTER TABLE ContractStatus ADD CONSTRAINT contractstatus_domain_id_domain_id_fkey FOREIGN KEY (contractstatus_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contractstatus_userupdate to userobm_id
UPDATE ContractStatus SET contractstatus_userupdate = NULL WHERE contractstatus_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND contractstatus_userupdate IS NOT NULL;
ALTER TABLE ContractStatus ADD CONSTRAINT contractstatus_userupdate_userobm_id_fkey FOREIGN KEY (contractstatus_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contractstatus_usercreate to userobm_id
UPDATE ContractStatus SET contractstatus_usercreate = NULL WHERE contractstatus_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND contractstatus_usercreate IS NOT NULL;
ALTER TABLE ContractStatus ADD CONSTRAINT contractstatus_usercreate_userobm_id_fkey FOREIGN KEY (contractstatus_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contracttype_domain_id to domain_id
DELETE FROM ContractType WHERE contracttype_domain_id NOT IN (SELECT domain_id FROM Domain) AND contracttype_domain_id IS NOT NULL;
ALTER TABLE ContractType ADD CONSTRAINT contracttype_domain_id_domain_id_fkey FOREIGN KEY (contracttype_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contracttype_userupdate to userobm_id
UPDATE ContractType SET contracttype_userupdate = NULL WHERE contracttype_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND contracttype_userupdate IS NOT NULL;
ALTER TABLE ContractType ADD CONSTRAINT contracttype_userupdate_userobm_id_fkey FOREIGN KEY (contracttype_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contracttype_usercreate to userobm_id
UPDATE ContractType SET contracttype_usercreate = NULL WHERE contracttype_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND contracttype_usercreate IS NOT NULL;
ALTER TABLE ContractType ADD CONSTRAINT contracttype_usercreate_userobm_id_fkey FOREIGN KEY (contracttype_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from country_domain_id to domain_id
DELETE FROM Country WHERE country_domain_id NOT IN (SELECT domain_id FROM Domain) AND country_domain_id IS NOT NULL;
ALTER TABLE Country ADD CONSTRAINT country_domain_id_domain_id_fkey FOREIGN KEY (country_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from country_userupdate to userobm_id
UPDATE Country SET country_userupdate = NULL WHERE country_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND country_userupdate IS NOT NULL;
ALTER TABLE Country ADD CONSTRAINT country_userupdate_userobm_id_fkey FOREIGN KEY (country_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from country_usercreate to userobm_id
UPDATE Country SET country_usercreate = NULL WHERE country_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND country_usercreate IS NOT NULL;
ALTER TABLE Country ADD CONSTRAINT country_usercreate_userobm_id_fkey FOREIGN KEY (country_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from datasource_domain_id to domain_id
DELETE FROM DataSource WHERE datasource_domain_id NOT IN (SELECT domain_id FROM Domain) AND datasource_domain_id IS NOT NULL;
ALTER TABLE DataSource ADD CONSTRAINT datasource_domain_id_domain_id_fkey FOREIGN KEY (datasource_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from datasource_userupdate to userobm_id
UPDATE DataSource SET datasource_userupdate = NULL WHERE datasource_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND datasource_userupdate IS NOT NULL;
ALTER TABLE DataSource ADD CONSTRAINT datasource_userupdate_userobm_id_fkey FOREIGN KEY (datasource_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from datasource_usercreate to userobm_id
UPDATE DataSource SET datasource_usercreate = NULL WHERE datasource_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND datasource_usercreate IS NOT NULL;
ALTER TABLE DataSource ADD CONSTRAINT datasource_usercreate_userobm_id_fkey FOREIGN KEY (datasource_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from deal_domain_id to domain_id
DELETE FROM Deal WHERE deal_domain_id NOT IN (SELECT domain_id FROM Domain) AND deal_domain_id IS NOT NULL;
ALTER TABLE Deal ADD CONSTRAINT deal_domain_id_domain_id_fkey FOREIGN KEY (deal_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from deal_parentdeal_id to parentdeal_id
DELETE FROM Deal WHERE deal_parentdeal_id NOT IN (SELECT parentdeal_id FROM ParentDeal) AND deal_parentdeal_id IS NOT NULL;
ALTER TABLE Deal ADD CONSTRAINT deal_parentdeal_id_parentdeal_id_fkey FOREIGN KEY (deal_parentdeal_id) REFERENCES ParentDeal(parentdeal_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from deal_company_id to company_id
DELETE FROM Deal WHERE deal_company_id NOT IN (SELECT company_id FROM Company) AND deal_company_id IS NOT NULL;
ALTER TABLE Deal ADD CONSTRAINT deal_company_id_company_id_fkey FOREIGN KEY (deal_company_id) REFERENCES Company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from deal_userupdate to userobm_id
UPDATE Deal SET deal_userupdate = NULL WHERE deal_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND deal_userupdate IS NOT NULL;
ALTER TABLE Deal ADD CONSTRAINT deal_userupdate_userobm_id_fkey FOREIGN KEY (deal_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from deal_usercreate to userobm_id
UPDATE Deal SET deal_usercreate = NULL WHERE deal_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND deal_usercreate IS NOT NULL;
ALTER TABLE Deal ADD CONSTRAINT deal_usercreate_userobm_id_fkey FOREIGN KEY (deal_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from deal_type_id to dealtype_id
UPDATE Deal SET deal_type_id = NULL WHERE deal_type_id NOT IN (SELECT dealtype_id FROM DealType) AND deal_type_id IS NOT NULL;
ALTER TABLE Deal ADD CONSTRAINT deal_type_id_dealtype_id_fkey FOREIGN KEY (deal_type_id) REFERENCES DealType(dealtype_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from deal_region_id to region_id
UPDATE Deal SET deal_region_id = NULL WHERE deal_region_id NOT IN (SELECT region_id FROM Region) AND deal_region_id IS NOT NULL;
ALTER TABLE Deal ADD CONSTRAINT deal_region_id_region_id_fkey FOREIGN KEY (deal_region_id) REFERENCES Region(region_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from deal_tasktype_id to tasktype_id
UPDATE Deal SET deal_tasktype_id = NULL WHERE deal_tasktype_id NOT IN (SELECT tasktype_id FROM TaskType) AND deal_tasktype_id IS NOT NULL;
ALTER TABLE Deal ADD CONSTRAINT deal_tasktype_id_tasktype_id_fkey FOREIGN KEY (deal_tasktype_id) REFERENCES TaskType(tasktype_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from deal_contact1_id to contact_id
UPDATE Deal SET deal_contact1_id = NULL WHERE deal_contact1_id NOT IN (SELECT contact_id FROM Contact) AND deal_contact1_id IS NOT NULL;
ALTER TABLE Deal ADD CONSTRAINT deal_contact1_id_contact_id_fkey FOREIGN KEY (deal_contact1_id) REFERENCES Contact(contact_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from deal_contact2_id to contact_id
UPDATE Deal SET deal_contact2_id = NULL WHERE deal_contact2_id NOT IN (SELECT contact_id FROM Contact) AND deal_contact2_id IS NOT NULL;
ALTER TABLE Deal ADD CONSTRAINT deal_contact2_id_contact_id_fkey FOREIGN KEY (deal_contact2_id) REFERENCES Contact(contact_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from deal_marketingmanager_id to userobm_id
UPDATE Deal SET deal_marketingmanager_id = NULL WHERE deal_marketingmanager_id NOT IN (SELECT userobm_id FROM UserObm) AND deal_marketingmanager_id IS NOT NULL;
ALTER TABLE Deal ADD CONSTRAINT deal_marketingmanager_id_userobm_id_fkey FOREIGN KEY (deal_marketingmanager_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from deal_technicalmanager_id to userobm_id
UPDATE Deal SET deal_technicalmanager_id = NULL WHERE deal_technicalmanager_id NOT IN (SELECT userobm_id FROM UserObm) AND deal_technicalmanager_id IS NOT NULL;
ALTER TABLE Deal ADD CONSTRAINT deal_technicalmanager_id_userobm_id_fkey FOREIGN KEY (deal_technicalmanager_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from deal_source_id to leadsource_id
UPDATE Deal SET deal_source_id = NULL WHERE deal_source_id NOT IN (SELECT leadsource_id FROM LeadSource) AND deal_source_id IS NOT NULL;
ALTER TABLE Deal ADD CONSTRAINT deal_source_id_leadsource_id_fkey FOREIGN KEY (deal_source_id) REFERENCES LeadSource(leadsource_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from dealcompany_deal_id to deal_id
DELETE FROM DealCompany WHERE dealcompany_deal_id NOT IN (SELECT deal_id FROM Deal) AND dealcompany_deal_id IS NOT NULL;
ALTER TABLE DealCompany ADD CONSTRAINT dealcompany_deal_id_deal_id_fkey FOREIGN KEY (dealcompany_deal_id) REFERENCES Deal(deal_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from dealcompany_company_id to company_id
DELETE FROM DealCompany WHERE dealcompany_company_id NOT IN (SELECT company_id FROM Company) AND dealcompany_company_id IS NOT NULL;
ALTER TABLE DealCompany ADD CONSTRAINT dealcompany_company_id_company_id_fkey FOREIGN KEY (dealcompany_company_id) REFERENCES Company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from dealcompany_role_id to dealcompanyrole_id
UPDATE DealCompany SET dealcompany_role_id = NULL WHERE dealcompany_role_id NOT IN (SELECT dealcompanyrole_id FROM DealCompanyRole) AND dealcompany_role_id IS NOT NULL;
ALTER TABLE DealCompany ADD CONSTRAINT dealcompany_role_id_dealcompanyrole_id_fkey FOREIGN KEY (dealcompany_role_id) REFERENCES DealCompanyRole(dealcompanyrole_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from dealcompany_userupdate to userobm_id
UPDATE DealCompany SET dealcompany_userupdate = NULL WHERE dealcompany_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND dealcompany_userupdate IS NOT NULL;
ALTER TABLE DealCompany ADD CONSTRAINT dealcompany_userupdate_userobm_id_fkey FOREIGN KEY (dealcompany_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from dealcompany_usercreate to userobm_id
UPDATE DealCompany SET dealcompany_usercreate = NULL WHERE dealcompany_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND dealcompany_usercreate IS NOT NULL;
ALTER TABLE DealCompany ADD CONSTRAINT dealcompany_usercreate_userobm_id_fkey FOREIGN KEY (dealcompany_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from dealcompanyrole_domain_id to domain_id
DELETE FROM DealCompanyRole WHERE dealcompanyrole_domain_id NOT IN (SELECT domain_id FROM Domain) AND dealcompanyrole_domain_id IS NOT NULL;
ALTER TABLE DealCompanyRole ADD CONSTRAINT dealcompanyrole_domain_id_domain_id_fkey FOREIGN KEY (dealcompanyrole_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from dealcompanyrole_userupdate to userobm_id
UPDATE DealCompanyRole SET dealcompanyrole_userupdate = NULL WHERE dealcompanyrole_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND dealcompanyrole_userupdate IS NOT NULL;
ALTER TABLE DealCompanyRole ADD CONSTRAINT dealcompanyrole_userupdate_userobm_id_fkey FOREIGN KEY (dealcompanyrole_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from dealcompanyrole_usercreate to userobm_id
UPDATE DealCompanyRole SET dealcompanyrole_usercreate = NULL WHERE dealcompanyrole_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND dealcompanyrole_usercreate IS NOT NULL;
ALTER TABLE DealCompanyRole ADD CONSTRAINT dealcompanyrole_usercreate_userobm_id_fkey FOREIGN KEY (dealcompanyrole_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from dealstatus_domain_id to domain_id
DELETE FROM DealStatus WHERE dealstatus_domain_id NOT IN (SELECT domain_id FROM Domain) AND dealstatus_domain_id IS NOT NULL;
ALTER TABLE DealStatus ADD CONSTRAINT dealstatus_domain_id_domain_id_fkey FOREIGN KEY (dealstatus_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from dealstatus_userupdate to userobm_id
UPDATE DealStatus SET dealstatus_userupdate = NULL WHERE dealstatus_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND dealstatus_userupdate IS NOT NULL;
ALTER TABLE DealStatus ADD CONSTRAINT dealstatus_userupdate_userobm_id_fkey FOREIGN KEY (dealstatus_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from dealstatus_usercreate to userobm_id
UPDATE DealStatus SET dealstatus_usercreate = NULL WHERE dealstatus_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND dealstatus_usercreate IS NOT NULL;
ALTER TABLE DealStatus ADD CONSTRAINT dealstatus_usercreate_userobm_id_fkey FOREIGN KEY (dealstatus_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from dealtype_domain_id to domain_id
DELETE FROM DealType WHERE dealtype_domain_id NOT IN (SELECT domain_id FROM Domain) AND dealtype_domain_id IS NOT NULL;
ALTER TABLE DealType ADD CONSTRAINT dealtype_domain_id_domain_id_fkey FOREIGN KEY (dealtype_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from dealtype_userupdate to userobm_id
UPDATE DealType SET dealtype_userupdate = NULL WHERE dealtype_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND dealtype_userupdate IS NOT NULL;
ALTER TABLE DealType ADD CONSTRAINT dealtype_userupdate_userobm_id_fkey FOREIGN KEY (dealtype_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from dealtype_usercreate to userobm_id
UPDATE DealType SET dealtype_usercreate = NULL WHERE dealtype_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND dealtype_usercreate IS NOT NULL;
ALTER TABLE DealType ADD CONSTRAINT dealtype_usercreate_userobm_id_fkey FOREIGN KEY (dealtype_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from defaultodttemplate_domain_id to domain_id
DELETE FROM DefaultOdtTemplate WHERE defaultodttemplate_domain_id NOT IN (SELECT domain_id FROM Domain) AND defaultodttemplate_domain_id IS NOT NULL;
ALTER TABLE DefaultOdtTemplate ADD CONSTRAINT defaultodttemplate_domain_id_domain_id_fkey FOREIGN KEY (defaultodttemplate_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from defaultodttemplate_document_id to document_id
DELETE FROM DefaultOdtTemplate WHERE defaultodttemplate_document_id NOT IN (SELECT document_id FROM Document) AND defaultodttemplate_document_id IS NOT NULL;
ALTER TABLE DefaultOdtTemplate ADD CONSTRAINT defaultodttemplate_document_id_document_id_fkey FOREIGN KEY (defaultodttemplate_document_id) REFERENCES Document(document_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from deleted_user_id to userobm_id
DELETE FROM Deleted WHERE deleted_user_id NOT IN (SELECT userobm_id FROM UserObm) AND deleted_user_id IS NOT NULL;
ALTER TABLE Deleted ADD CONSTRAINT deleted_user_id_userobm_id_fkey FOREIGN KEY (deleted_user_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from deleted_domain_id to domain_id
DELETE FROM Deleted WHERE deleted_domain_id NOT IN (SELECT domain_id FROM Domain) AND deleted_domain_id IS NOT NULL;
ALTER TABLE Deleted ADD CONSTRAINT deleted_domain_id_domain_id_fkey FOREIGN KEY (deleted_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from display_user_id to userobm_id
DELETE FROM DisplayPref WHERE display_user_id NOT IN (SELECT userobm_id FROM UserObm) AND display_user_id IS NOT NULL;
ALTER TABLE DisplayPref ADD CONSTRAINT display_user_id_userobm_id_fkey FOREIGN KEY (display_user_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from document_domain_id to domain_id
DELETE FROM Document WHERE document_domain_id NOT IN (SELECT domain_id FROM Domain) AND document_domain_id IS NOT NULL;
ALTER TABLE Document ADD CONSTRAINT document_domain_id_domain_id_fkey FOREIGN KEY (document_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from document_userupdate to userobm_id
UPDATE Document SET document_userupdate = NULL WHERE document_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND document_userupdate IS NOT NULL;
ALTER TABLE Document ADD CONSTRAINT document_userupdate_userobm_id_fkey FOREIGN KEY (document_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from document_usercreate to userobm_id
UPDATE Document SET document_usercreate = NULL WHERE document_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND document_usercreate IS NOT NULL;
ALTER TABLE Document ADD CONSTRAINT document_usercreate_userobm_id_fkey FOREIGN KEY (document_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from document_mimetype_id to documentmimetype_id
UPDATE Document SET document_mimetype_id = NULL WHERE document_mimetype_id NOT IN (SELECT documentmimetype_id FROM DocumentMimeType) AND document_mimetype_id IS NOT NULL;
ALTER TABLE Document ADD CONSTRAINT document_mimetype_id_documentmimetype_id_fkey FOREIGN KEY (document_mimetype_id) REFERENCES DocumentMimeType(documentmimetype_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from documententity_document_id to document_id
DELETE FROM DocumentEntity WHERE documententity_document_id NOT IN (SELECT document_id FROM Document) AND documententity_document_id IS NOT NULL;
ALTER TABLE DocumentEntity ADD CONSTRAINT documententity_document_id_document_id_fkey FOREIGN KEY (documententity_document_id) REFERENCES Document(document_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from documentmimetype_domain_id to domain_id
DELETE FROM DocumentMimeType WHERE documentmimetype_domain_id NOT IN (SELECT domain_id FROM Domain) AND documentmimetype_domain_id IS NOT NULL;
ALTER TABLE DocumentMimeType ADD CONSTRAINT documentmimetype_domain_id_domain_id_fkey FOREIGN KEY (documentmimetype_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from documentmimetype_userupdate to userobm_id
UPDATE DocumentMimeType SET documentmimetype_userupdate = NULL WHERE documentmimetype_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND documentmimetype_userupdate IS NOT NULL;
ALTER TABLE DocumentMimeType ADD CONSTRAINT documentmimetype_userupdate_userobm_id_fkey FOREIGN KEY (documentmimetype_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from documentmimetype_usercreate to userobm_id
UPDATE DocumentMimeType SET documentmimetype_usercreate = NULL WHERE documentmimetype_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND documentmimetype_usercreate IS NOT NULL;
ALTER TABLE DocumentMimeType ADD CONSTRAINT documentmimetype_usercreate_userobm_id_fkey FOREIGN KEY (documentmimetype_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from domain_userupdate to userobm_id
UPDATE Domain SET domain_userupdate = NULL WHERE domain_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND domain_userupdate IS NOT NULL;
ALTER TABLE Domain ADD CONSTRAINT domain_userupdate_userobm_id_fkey FOREIGN KEY (domain_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from domain_usercreate to userobm_id
UPDATE Domain SET domain_usercreate = NULL WHERE domain_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND domain_usercreate IS NOT NULL;
ALTER TABLE Domain ADD CONSTRAINT domain_usercreate_userobm_id_fkey FOREIGN KEY (domain_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from domainmailserver_domain_id to domain_id
DELETE FROM DomainMailServer WHERE domainmailserver_domain_id NOT IN (SELECT domain_id FROM Domain) AND domainmailserver_domain_id IS NOT NULL;
ALTER TABLE DomainMailServer ADD CONSTRAINT domainmailserver_domain_id_domain_id_fkey FOREIGN KEY (domainmailserver_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from domainmailserver_mailserver_id to mailserver_id
DELETE FROM DomainMailServer WHERE domainmailserver_mailserver_id NOT IN (SELECT mailserver_id FROM MailServer) AND domainmailserver_mailserver_id IS NOT NULL;
ALTER TABLE DomainMailServer ADD CONSTRAINT domainmailserver_mailserver_id_mailserver_id_fkey FOREIGN KEY (domainmailserver_mailserver_id) REFERENCES MailServer(mailserver_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from domainpropertyvalue_domain_id to domain_id
DELETE FROM DomainPropertyValue WHERE domainpropertyvalue_domain_id NOT IN (SELECT domain_id FROM Domain) AND domainpropertyvalue_domain_id IS NOT NULL;
ALTER TABLE DomainPropertyValue ADD CONSTRAINT domainpropertyvalue_domain_id_domain_id_fkey FOREIGN KEY (domainpropertyvalue_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from evententity_event_id to calendarevent_id
DELETE FROM EventEntity WHERE evententity_event_id NOT IN (SELECT calendarevent_id FROM CalendarEvent) AND evententity_event_id IS NOT NULL;
ALTER TABLE EventEntity ADD CONSTRAINT evententity_event_id_calendarevent_id_fkey FOREIGN KEY (evententity_event_id) REFERENCES CalendarEvent(calendarevent_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from evententity_userupdate to userobm_id
UPDATE EventEntity SET evententity_userupdate = NULL WHERE evententity_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND evententity_userupdate IS NOT NULL;
ALTER TABLE EventEntity ADD CONSTRAINT evententity_userupdate_userobm_id_fkey FOREIGN KEY (evententity_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from evententity_usercreate to userobm_id
UPDATE EventEntity SET evententity_usercreate = NULL WHERE evententity_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND evententity_usercreate IS NOT NULL;
ALTER TABLE EventEntity ADD CONSTRAINT evententity_usercreate_userobm_id_fkey FOREIGN KEY (evententity_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from groupgroup_parent_id to group_id
DELETE FROM GroupGroup WHERE groupgroup_parent_id NOT IN (SELECT group_id FROM UGroup) AND groupgroup_parent_id IS NOT NULL;
ALTER TABLE GroupGroup ADD CONSTRAINT groupgroup_parent_id_group_id_fkey FOREIGN KEY (groupgroup_parent_id) REFERENCES UGroup(group_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from groupgroup_child_id to group_id
DELETE FROM GroupGroup WHERE groupgroup_child_id NOT IN (SELECT group_id FROM UGroup) AND groupgroup_child_id IS NOT NULL;
ALTER TABLE GroupGroup ADD CONSTRAINT groupgroup_child_id_group_id_fkey FOREIGN KEY (groupgroup_child_id) REFERENCES UGroup(group_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from host_domain_id to domain_id
DELETE FROM Host WHERE host_domain_id NOT IN (SELECT domain_id FROM Domain) AND host_domain_id IS NOT NULL;
ALTER TABLE Host ADD CONSTRAINT host_domain_id_domain_id_fkey FOREIGN KEY (host_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from host_userupdate to userobm_id
UPDATE Host SET host_userupdate = NULL WHERE host_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND host_userupdate IS NOT NULL;
ALTER TABLE Host ADD CONSTRAINT host_userupdate_userobm_id_fkey FOREIGN KEY (host_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from host_usercreate to userobm_id
UPDATE Host SET host_usercreate = NULL WHERE host_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND host_usercreate IS NOT NULL;
ALTER TABLE Host ADD CONSTRAINT host_usercreate_userobm_id_fkey FOREIGN KEY (host_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from import_domain_id to domain_id
DELETE FROM Import WHERE import_domain_id NOT IN (SELECT domain_id FROM Domain) AND import_domain_id IS NOT NULL;
ALTER TABLE Import ADD CONSTRAINT import_domain_id_domain_id_fkey FOREIGN KEY (import_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from import_userupdate to userobm_id
UPDATE Import SET import_userupdate = NULL WHERE import_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND import_userupdate IS NOT NULL;
ALTER TABLE Import ADD CONSTRAINT import_userupdate_userobm_id_fkey FOREIGN KEY (import_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from import_usercreate to userobm_id
UPDATE Import SET import_usercreate = NULL WHERE import_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND import_usercreate IS NOT NULL;
ALTER TABLE Import ADD CONSTRAINT import_usercreate_userobm_id_fkey FOREIGN KEY (import_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from import_datasource_id to datasource_id
UPDATE Import SET import_datasource_id = NULL WHERE import_datasource_id NOT IN (SELECT datasource_id FROM DataSource) AND import_datasource_id IS NOT NULL;
ALTER TABLE Import ADD CONSTRAINT import_datasource_id_datasource_id_fkey FOREIGN KEY (import_datasource_id) REFERENCES DataSource(datasource_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from import_marketingmanager_id to userobm_id
UPDATE Import SET import_marketingmanager_id = NULL WHERE import_marketingmanager_id NOT IN (SELECT userobm_id FROM UserObm) AND import_marketingmanager_id IS NOT NULL;
ALTER TABLE Import ADD CONSTRAINT import_marketingmanager_id_userobm_id_fkey FOREIGN KEY (import_marketingmanager_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incident_domain_id to domain_id
DELETE FROM Incident WHERE incident_domain_id NOT IN (SELECT domain_id FROM Domain) AND incident_domain_id IS NOT NULL;
ALTER TABLE Incident ADD CONSTRAINT incident_domain_id_domain_id_fkey FOREIGN KEY (incident_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from incident_contract_id to contract_id
DELETE FROM Incident WHERE incident_contract_id NOT IN (SELECT contract_id FROM Contract) AND incident_contract_id IS NOT NULL;
ALTER TABLE Incident ADD CONSTRAINT incident_contract_id_contract_id_fkey FOREIGN KEY (incident_contract_id) REFERENCES Contract(contract_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from incident_userupdate to userobm_id
UPDATE Incident SET incident_userupdate = NULL WHERE incident_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND incident_userupdate IS NOT NULL;
ALTER TABLE Incident ADD CONSTRAINT incident_userupdate_userobm_id_fkey FOREIGN KEY (incident_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incident_usercreate to userobm_id
UPDATE Incident SET incident_usercreate = NULL WHERE incident_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND incident_usercreate IS NOT NULL;
ALTER TABLE Incident ADD CONSTRAINT incident_usercreate_userobm_id_fkey FOREIGN KEY (incident_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incident_priority_id to incidentpriority_id
UPDATE Incident SET incident_priority_id = NULL WHERE incident_priority_id NOT IN (SELECT incidentpriority_id FROM IncidentPriority) AND incident_priority_id IS NOT NULL;
ALTER TABLE Incident ADD CONSTRAINT incident_priority_id_incidentpriority_id_fkey FOREIGN KEY (incident_priority_id) REFERENCES IncidentPriority(incidentpriority_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incident_status_id to incidentstatus_id
UPDATE Incident SET incident_status_id = NULL WHERE incident_status_id NOT IN (SELECT incidentstatus_id FROM IncidentStatus) AND incident_status_id IS NOT NULL;
ALTER TABLE Incident ADD CONSTRAINT incident_status_id_incidentstatus_id_fkey FOREIGN KEY (incident_status_id) REFERENCES IncidentStatus(incidentstatus_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incident_resolutiontype_id to incidentresolutiontype_id
UPDATE Incident SET incident_resolutiontype_id = NULL WHERE incident_resolutiontype_id NOT IN (SELECT incidentresolutiontype_id FROM IncidentResolutionType) AND incident_resolutiontype_id IS NOT NULL;
ALTER TABLE Incident ADD CONSTRAINT incident_resolutiontype_id_incidentresolutiontype_id_fkey FOREIGN KEY (incident_resolutiontype_id) REFERENCES IncidentResolutionType(incidentresolutiontype_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incident_logger to userobm_id
UPDATE Incident SET incident_logger = NULL WHERE incident_logger NOT IN (SELECT userobm_id FROM UserObm) AND incident_logger IS NOT NULL;
ALTER TABLE Incident ADD CONSTRAINT incident_logger_userobm_id_fkey FOREIGN KEY (incident_logger) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incident_owner to userobm_id
UPDATE Incident SET incident_owner = NULL WHERE incident_owner NOT IN (SELECT userobm_id FROM UserObm) AND incident_owner IS NOT NULL;
ALTER TABLE Incident ADD CONSTRAINT incident_owner_userobm_id_fkey FOREIGN KEY (incident_owner) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incidentpriority_domain_id to domain_id
DELETE FROM IncidentPriority WHERE incidentpriority_domain_id NOT IN (SELECT domain_id FROM Domain) AND incidentpriority_domain_id IS NOT NULL;
ALTER TABLE IncidentPriority ADD CONSTRAINT incidentpriority_domain_id_domain_id_fkey FOREIGN KEY (incidentpriority_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from incidentpriority_userupdate to userobm_id
UPDATE IncidentPriority SET incidentpriority_userupdate = NULL WHERE incidentpriority_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND incidentpriority_userupdate IS NOT NULL;
ALTER TABLE IncidentPriority ADD CONSTRAINT incidentpriority_userupdate_userobm_id_fkey FOREIGN KEY (incidentpriority_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incidentpriority_usercreate to userobm_id
UPDATE IncidentPriority SET incidentpriority_usercreate = NULL WHERE incidentpriority_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND incidentpriority_usercreate IS NOT NULL;
ALTER TABLE IncidentPriority ADD CONSTRAINT incidentpriority_usercreate_userobm_id_fkey FOREIGN KEY (incidentpriority_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incidentresolutiontype_domain_id to domain_id
DELETE FROM IncidentResolutionType WHERE incidentresolutiontype_domain_id NOT IN (SELECT domain_id FROM Domain) AND incidentresolutiontype_domain_id IS NOT NULL;
ALTER TABLE IncidentResolutionType ADD CONSTRAINT incidentresolutiontype_domain_id_domain_id_fkey FOREIGN KEY (incidentresolutiontype_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from incidentresolutiontype_userupdate to userobm_id
UPDATE IncidentResolutionType SET incidentresolutiontype_userupdate = NULL WHERE incidentresolutiontype_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND incidentresolutiontype_userupdate IS NOT NULL;
ALTER TABLE IncidentResolutionType ADD CONSTRAINT incidentresolutiontype_userupdate_userobm_id_fkey FOREIGN KEY (incidentresolutiontype_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incidentresolutiontype_usercreate to userobm_id
UPDATE IncidentResolutionType SET incidentresolutiontype_usercreate = NULL WHERE incidentresolutiontype_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND incidentresolutiontype_usercreate IS NOT NULL;
ALTER TABLE IncidentResolutionType ADD CONSTRAINT incidentresolutiontype_usercreate_userobm_id_fkey FOREIGN KEY (incidentresolutiontype_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incidentstatus_domain_id to domain_id
DELETE FROM IncidentStatus WHERE incidentstatus_domain_id NOT IN (SELECT domain_id FROM Domain) AND incidentstatus_domain_id IS NOT NULL;
ALTER TABLE IncidentStatus ADD CONSTRAINT incidentstatus_domain_id_domain_id_fkey FOREIGN KEY (incidentstatus_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from incidentstatus_userupdate to userobm_id
UPDATE IncidentStatus SET incidentstatus_userupdate = NULL WHERE incidentstatus_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND incidentstatus_userupdate IS NOT NULL;
ALTER TABLE IncidentStatus ADD CONSTRAINT incidentstatus_userupdate_userobm_id_fkey FOREIGN KEY (incidentstatus_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incidentstatus_usercreate to userobm_id
UPDATE IncidentStatus SET incidentstatus_usercreate = NULL WHERE incidentstatus_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND incidentstatus_usercreate IS NOT NULL;
ALTER TABLE IncidentStatus ADD CONSTRAINT incidentstatus_usercreate_userobm_id_fkey FOREIGN KEY (incidentstatus_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from invoice_domain_id to domain_id
DELETE FROM Invoice WHERE invoice_domain_id NOT IN (SELECT domain_id FROM Domain) AND invoice_domain_id IS NOT NULL;
ALTER TABLE Invoice ADD CONSTRAINT invoice_domain_id_domain_id_fkey FOREIGN KEY (invoice_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from invoice_company_id to company_id
DELETE FROM Invoice WHERE invoice_company_id NOT IN (SELECT company_id FROM Company) AND invoice_company_id IS NOT NULL;
ALTER TABLE Invoice ADD CONSTRAINT invoice_company_id_company_id_fkey FOREIGN KEY (invoice_company_id) REFERENCES Company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from invoice_project_id to project_id
DELETE FROM Invoice WHERE invoice_project_id NOT IN (SELECT project_id FROM Project) AND invoice_project_id IS NOT NULL;
ALTER TABLE Invoice ADD CONSTRAINT invoice_project_id_project_id_fkey FOREIGN KEY (invoice_project_id) REFERENCES Project(project_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from invoice_deal_id to deal_id
DELETE FROM Invoice WHERE invoice_deal_id NOT IN (SELECT deal_id FROM Deal) AND invoice_deal_id IS NOT NULL;
ALTER TABLE Invoice ADD CONSTRAINT invoice_deal_id_deal_id_fkey FOREIGN KEY (invoice_deal_id) REFERENCES Deal(deal_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from invoice_userupdate to userobm_id
UPDATE Invoice SET invoice_userupdate = NULL WHERE invoice_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND invoice_userupdate IS NOT NULL;
ALTER TABLE Invoice ADD CONSTRAINT invoice_userupdate_userobm_id_fkey FOREIGN KEY (invoice_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from invoice_usercreate to userobm_id
UPDATE Invoice SET invoice_usercreate = NULL WHERE invoice_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND invoice_usercreate IS NOT NULL;
ALTER TABLE Invoice ADD CONSTRAINT invoice_usercreate_userobm_id_fkey FOREIGN KEY (invoice_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from kind_domain_id to domain_id
DELETE FROM Kind WHERE kind_domain_id NOT IN (SELECT domain_id FROM Domain) AND kind_domain_id IS NOT NULL;
ALTER TABLE Kind ADD CONSTRAINT kind_domain_id_domain_id_fkey FOREIGN KEY (kind_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from kind_userupdate to userobm_id
UPDATE Kind SET kind_userupdate = NULL WHERE kind_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND kind_userupdate IS NOT NULL;
ALTER TABLE Kind ADD CONSTRAINT kind_userupdate_userobm_id_fkey FOREIGN KEY (kind_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from kind_usercreate to userobm_id
UPDATE Kind SET kind_usercreate = NULL WHERE kind_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND kind_usercreate IS NOT NULL;
ALTER TABLE Kind ADD CONSTRAINT kind_usercreate_userobm_id_fkey FOREIGN KEY (kind_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from lead_domain_id to domain_id
DELETE FROM Lead WHERE lead_domain_id NOT IN (SELECT domain_id FROM Domain) AND lead_domain_id IS NOT NULL;
ALTER TABLE Lead ADD CONSTRAINT lead_domain_id_domain_id_fkey FOREIGN KEY (lead_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from lead_company_id to company_id
DELETE FROM Lead WHERE lead_company_id NOT IN (SELECT company_id FROM Company) AND lead_company_id IS NOT NULL;
ALTER TABLE Lead ADD CONSTRAINT lead_company_id_company_id_fkey FOREIGN KEY (lead_company_id) REFERENCES Company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from lead_userupdate to userobm_id
UPDATE Lead SET lead_userupdate = NULL WHERE lead_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND lead_userupdate IS NOT NULL;
ALTER TABLE Lead ADD CONSTRAINT lead_userupdate_userobm_id_fkey FOREIGN KEY (lead_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from lead_usercreate to userobm_id
UPDATE Lead SET lead_usercreate = NULL WHERE lead_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND lead_usercreate IS NOT NULL;
ALTER TABLE Lead ADD CONSTRAINT lead_usercreate_userobm_id_fkey FOREIGN KEY (lead_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from lead_source_id to leadsource_id
UPDATE Lead SET lead_source_id = NULL WHERE lead_source_id NOT IN (SELECT leadsource_id FROM LeadSource) AND lead_source_id IS NOT NULL;
ALTER TABLE Lead ADD CONSTRAINT lead_source_id_leadsource_id_fkey FOREIGN KEY (lead_source_id) REFERENCES LeadSource(leadsource_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from lead_manager_id to userobm_id
UPDATE Lead SET lead_manager_id = NULL WHERE lead_manager_id NOT IN (SELECT userobm_id FROM UserObm) AND lead_manager_id IS NOT NULL;
ALTER TABLE Lead ADD CONSTRAINT lead_manager_id_userobm_id_fkey FOREIGN KEY (lead_manager_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from lead_contact_id to contact_id
UPDATE Lead SET lead_contact_id = NULL WHERE lead_contact_id NOT IN (SELECT contact_id FROM Contact) AND lead_contact_id IS NOT NULL;
ALTER TABLE Lead ADD CONSTRAINT lead_contact_id_contact_id_fkey FOREIGN KEY (lead_contact_id) REFERENCES Contact(contact_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from lead_status_id to leadstatus_id
UPDATE Lead SET lead_status_id = NULL WHERE lead_status_id NOT IN (SELECT leadstatus_id FROM LeadStatus) AND lead_status_id IS NOT NULL;
ALTER TABLE Lead ADD CONSTRAINT lead_status_id_leadstatus_id_fkey FOREIGN KEY (lead_status_id) REFERENCES LeadStatus(leadstatus_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from leadsource_domain_id to domain_id
DELETE FROM LeadSource WHERE leadsource_domain_id NOT IN (SELECT domain_id FROM Domain) AND leadsource_domain_id IS NOT NULL;
ALTER TABLE LeadSource ADD CONSTRAINT leadsource_domain_id_domain_id_fkey FOREIGN KEY (leadsource_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from leadsource_userupdate to userobm_id
UPDATE LeadSource SET leadsource_userupdate = NULL WHERE leadsource_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND leadsource_userupdate IS NOT NULL;
ALTER TABLE LeadSource ADD CONSTRAINT leadsource_userupdate_userobm_id_fkey FOREIGN KEY (leadsource_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from leadsource_usercreate to userobm_id
UPDATE LeadSource SET leadsource_usercreate = NULL WHERE leadsource_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND leadsource_usercreate IS NOT NULL;
ALTER TABLE LeadSource ADD CONSTRAINT leadsource_usercreate_userobm_id_fkey FOREIGN KEY (leadsource_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from leadstatus_domain_id to domain_id
DELETE FROM LeadStatus WHERE leadstatus_domain_id NOT IN (SELECT domain_id FROM Domain) AND leadstatus_domain_id IS NOT NULL;
ALTER TABLE LeadStatus ADD CONSTRAINT leadstatus_domain_id_domain_id_fkey FOREIGN KEY (leadstatus_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from leadstatus_userupdate to userobm_id
UPDATE LeadStatus SET leadstatus_userupdate = NULL WHERE leadstatus_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND leadstatus_userupdate IS NOT NULL;
ALTER TABLE LeadStatus ADD CONSTRAINT leadstatus_userupdate_userobm_id_fkey FOREIGN KEY (leadstatus_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from leadstatus_usercreate to userobm_id
UPDATE LeadStatus SET leadstatus_usercreate = NULL WHERE leadstatus_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND leadstatus_usercreate IS NOT NULL;
ALTER TABLE LeadStatus ADD CONSTRAINT leadstatus_usercreate_userobm_id_fkey FOREIGN KEY (leadstatus_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from list_domain_id to domain_id
DELETE FROM List WHERE list_domain_id NOT IN (SELECT domain_id FROM Domain) AND list_domain_id IS NOT NULL;
ALTER TABLE List ADD CONSTRAINT list_domain_id_domain_id_fkey FOREIGN KEY (list_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from list_userupdate to userobm_id
UPDATE List SET list_userupdate = NULL WHERE list_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND list_userupdate IS NOT NULL;
ALTER TABLE List ADD CONSTRAINT list_userupdate_userobm_id_fkey FOREIGN KEY (list_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from list_usercreate to userobm_id
UPDATE List SET list_usercreate = NULL WHERE list_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND list_usercreate IS NOT NULL;
ALTER TABLE List ADD CONSTRAINT list_usercreate_userobm_id_fkey FOREIGN KEY (list_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from mailserver_host_id to host_id
DELETE FROM MailServer WHERE mailserver_host_id NOT IN (SELECT host_id FROM Host) AND mailserver_host_id IS NOT NULL;
ALTER TABLE MailServer ADD CONSTRAINT mailserver_host_id_host_id_fkey FOREIGN KEY (mailserver_host_id) REFERENCES Host(host_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from mailserver_userupdate to userobm_id
UPDATE MailServer SET mailserver_userupdate = NULL WHERE mailserver_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND mailserver_userupdate IS NOT NULL;
ALTER TABLE MailServer ADD CONSTRAINT mailserver_userupdate_userobm_id_fkey FOREIGN KEY (mailserver_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from mailserver_usercreate to userobm_id
UPDATE MailServer SET mailserver_usercreate = NULL WHERE mailserver_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND mailserver_usercreate IS NOT NULL;
ALTER TABLE MailServer ADD CONSTRAINT mailserver_usercreate_userobm_id_fkey FOREIGN KEY (mailserver_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from mailserver_relayhost_id to Host
UPDATE MailServer SET mailserver_relayhost_id = NULL WHERE mailserver_relayhost_id NOT IN (SELECT host_id FROM Host) AND mailserver_relayhost_id IS NOT NULL;
ALTER TABLE MailServer ADD CONSTRAINT mailserver_relayhost_id_host_id_fkey FOREIGN KEY (mailserver_relayhost_id) REFERENCES Host(host_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from mailshare_domain_id to domain_id
DELETE FROM MailShare WHERE mailshare_domain_id NOT IN (SELECT domain_id FROM Domain) AND mailshare_domain_id IS NOT NULL;
ALTER TABLE MailShare ADD CONSTRAINT mailshare_domain_id_domain_id_fkey FOREIGN KEY (mailshare_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from mailshare_mail_server_id to mailserver_id
DELETE FROM MailShare WHERE mailshare_mail_server_id NOT IN (SELECT mailserver_id FROM MailServer) AND mailshare_mail_server_id IS NOT NULL;
ALTER TABLE MailShare ADD CONSTRAINT mailshare_mail_server_id_mailserver_id_fkey FOREIGN KEY (mailshare_mail_server_id) REFERENCES MailServer(mailserver_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from mailshare_userupdate to userobm_id
UPDATE MailShare SET mailshare_userupdate = NULL WHERE mailshare_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND mailshare_userupdate IS NOT NULL;
ALTER TABLE MailShare ADD CONSTRAINT mailshare_userupdate_userobm_id_fkey FOREIGN KEY (mailshare_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from mailshare_usercreate to userobm_id
UPDATE MailShare SET mailshare_usercreate = NULL WHERE mailshare_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND mailshare_usercreate IS NOT NULL;
ALTER TABLE MailShare ADD CONSTRAINT mailshare_usercreate_userobm_id_fkey FOREIGN KEY (mailshare_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from ogroup_domain_id to domain_id
DELETE FROM OGroup WHERE ogroup_domain_id NOT IN (SELECT domain_id FROM Domain) AND ogroup_domain_id IS NOT NULL;
ALTER TABLE OGroup ADD CONSTRAINT ogroup_domain_id_domain_id_fkey FOREIGN KEY (ogroup_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from ogroup_organizationalchart_id to organizationalchart_id
DELETE FROM OGroup WHERE ogroup_organizationalchart_id NOT IN (SELECT organizationalchart_id FROM OrganizationalChart) AND ogroup_organizationalchart_id IS NOT NULL;
ALTER TABLE OGroup ADD CONSTRAINT ogroup_organizationalchart_id_organizationalchart_id_fkey FOREIGN KEY (ogroup_organizationalchart_id) REFERENCES OrganizationalChart(organizationalchart_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from ogroup_parent_id to ogroup_id
DELETE FROM OGroup WHERE ogroup_parent_id NOT IN (SELECT ogroup_id FROM OGroup) AND ogroup_parent_id IS NOT NULL;
ALTER TABLE OGroup ADD CONSTRAINT ogroup_parent_id_ogroup_id_fkey FOREIGN KEY (ogroup_parent_id) REFERENCES OGroup(ogroup_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from ogroup_userupdate to userobm_id
UPDATE OGroup SET ogroup_userupdate = NULL WHERE ogroup_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND ogroup_userupdate IS NOT NULL;
ALTER TABLE OGroup ADD CONSTRAINT ogroup_userupdate_userobm_id_fkey FOREIGN KEY (ogroup_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from ogroup_usercreate to userobm_id
UPDATE OGroup SET ogroup_usercreate = NULL WHERE ogroup_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND ogroup_usercreate IS NOT NULL;
ALTER TABLE OGroup ADD CONSTRAINT ogroup_usercreate_userobm_id_fkey FOREIGN KEY (ogroup_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from ogroupentity_ogroup_id to ogroup_id
DELETE FROM OGroupEntity WHERE ogroupentity_ogroup_id NOT IN (SELECT ogroup_id FROM OGroup) AND ogroupentity_ogroup_id IS NOT NULL;
ALTER TABLE OGroupEntity ADD CONSTRAINT ogroupentity_ogroup_id_ogroup_id_fkey FOREIGN KEY (ogroupentity_ogroup_id) REFERENCES OGroup(ogroup_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from ogroupentity_domain_id to domain_id
DELETE FROM OGroupEntity WHERE ogroupentity_domain_id NOT IN (SELECT domain_id FROM Domain) AND ogroupentity_domain_id IS NOT NULL;
ALTER TABLE OGroupEntity ADD CONSTRAINT ogroupentity_domain_id_domain_id_fkey FOREIGN KEY (ogroupentity_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from ogroupentity_userupdate to userobm_id
UPDATE OGroupEntity SET ogroupentity_userupdate = NULL WHERE ogroupentity_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND ogroupentity_userupdate IS NOT NULL;
ALTER TABLE OGroupEntity ADD CONSTRAINT ogroupentity_userupdate_userobm_id_fkey FOREIGN KEY (ogroupentity_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from ogroupentity_usercreate to userobm_id
UPDATE OGroupEntity SET ogroupentity_usercreate = NULL WHERE ogroupentity_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND ogroupentity_usercreate IS NOT NULL;
ALTER TABLE OGroupEntity ADD CONSTRAINT ogroupentity_usercreate_userobm_id_fkey FOREIGN KEY (ogroupentity_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from obmbookmark_user_id to userobm_id
UPDATE ObmBookmark SET obmbookmark_user_id = NULL WHERE obmbookmark_user_id NOT IN (SELECT userobm_id FROM UserObm) AND obmbookmark_user_id IS NOT NULL;
ALTER TABLE ObmBookmark ADD CONSTRAINT obmbookmark_user_id_userobm_id_fkey FOREIGN KEY (obmbookmark_user_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from obmbookmarkproperty_bookmark_id to obmbookmark_id
DELETE FROM ObmBookmarkProperty WHERE obmbookmarkproperty_bookmark_id NOT IN (SELECT obmbookmark_id FROM ObmBookmark) AND obmbookmarkproperty_bookmark_id IS NOT NULL;
ALTER TABLE ObmBookmarkProperty ADD CONSTRAINT obmbookmarkproperty_bookmark_id_obmbookmark_id_fkey FOREIGN KEY (obmbookmarkproperty_bookmark_id) REFERENCES ObmBookmark(obmbookmark_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from organizationalchart_domain_id to domain_id
DELETE FROM OrganizationalChart WHERE organizationalchart_domain_id NOT IN (SELECT domain_id FROM Domain) AND organizationalchart_domain_id IS NOT NULL;
ALTER TABLE OrganizationalChart ADD CONSTRAINT organizationalchart_domain_id_domain_id_fkey FOREIGN KEY (organizationalchart_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from organizationalchart_userupdate to userobm_id
UPDATE OrganizationalChart SET organizationalchart_userupdate = NULL WHERE organizationalchart_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND organizationalchart_userupdate IS NOT NULL;
ALTER TABLE OrganizationalChart ADD CONSTRAINT organizationalchart_userupdate_userobm_id_fkey FOREIGN KEY (organizationalchart_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from organizationalchart_usercreate to userobm_id
UPDATE OrganizationalChart SET organizationalchart_usercreate = NULL WHERE organizationalchart_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND organizationalchart_usercreate IS NOT NULL;
ALTER TABLE OrganizationalChart ADD CONSTRAINT organizationalchart_usercreate_userobm_id_fkey FOREIGN KEY (organizationalchart_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from parentdeal_domain_id to domain_id
DELETE FROM ParentDeal WHERE parentdeal_domain_id NOT IN (SELECT domain_id FROM Domain) AND parentdeal_domain_id IS NOT NULL;
ALTER TABLE ParentDeal ADD CONSTRAINT parentdeal_domain_id_domain_id_fkey FOREIGN KEY (parentdeal_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from parentdeal_userupdate to userobm_id
UPDATE ParentDeal SET parentdeal_userupdate = NULL WHERE parentdeal_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND parentdeal_userupdate IS NOT NULL;
ALTER TABLE ParentDeal ADD CONSTRAINT parentdeal_userupdate_userobm_id_fkey FOREIGN KEY (parentdeal_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from parentdeal_usercreate to userobm_id
UPDATE ParentDeal SET parentdeal_usercreate = NULL WHERE parentdeal_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND parentdeal_usercreate IS NOT NULL;
ALTER TABLE ParentDeal ADD CONSTRAINT parentdeal_usercreate_userobm_id_fkey FOREIGN KEY (parentdeal_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from parentdeal_marketingmanager_id to userobm_id
UPDATE ParentDeal SET parentdeal_marketingmanager_id = NULL WHERE parentdeal_marketingmanager_id NOT IN (SELECT userobm_id FROM UserObm) AND parentdeal_marketingmanager_id IS NOT NULL;
ALTER TABLE ParentDeal ADD CONSTRAINT parentdeal_marketingmanager_id_userobm_id_fkey FOREIGN KEY (parentdeal_marketingmanager_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from parentdeal_technicalmanager_id to userobm_id
UPDATE ParentDeal SET parentdeal_technicalmanager_id = NULL WHERE parentdeal_technicalmanager_id NOT IN (SELECT userobm_id FROM UserObm) AND parentdeal_technicalmanager_id IS NOT NULL;
ALTER TABLE ParentDeal ADD CONSTRAINT parentdeal_technicalmanager_id_userobm_id_fkey FOREIGN KEY (parentdeal_technicalmanager_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from payment_domain_id to domain_id
DELETE FROM Payment WHERE payment_domain_id NOT IN (SELECT domain_id FROM Domain) AND payment_domain_id IS NOT NULL;
ALTER TABLE Payment ADD CONSTRAINT payment_domain_id_domain_id_fkey FOREIGN KEY (payment_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from payment_account_id to account_id
DELETE FROM Payment WHERE payment_account_id NOT IN (SELECT account_id FROM Account) AND payment_account_id IS NOT NULL;
ALTER TABLE Payment ADD CONSTRAINT payment_account_id_account_id_fkey FOREIGN KEY (payment_account_id) REFERENCES Account(account_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from payment_userupdate to userobm_id
UPDATE Payment SET payment_userupdate = NULL WHERE payment_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND payment_userupdate IS NOT NULL;
ALTER TABLE Payment ADD CONSTRAINT payment_userupdate_userobm_id_fkey FOREIGN KEY (payment_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from payment_usercreate to userobm_id
UPDATE Payment SET payment_usercreate = NULL WHERE payment_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND payment_usercreate IS NOT NULL;
ALTER TABLE Payment ADD CONSTRAINT payment_usercreate_userobm_id_fkey FOREIGN KEY (payment_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from payment_company_id to company_id
UPDATE Payment SET payment_company_id = NULL WHERE payment_company_id NOT IN (SELECT company_id FROM Company) AND payment_company_id IS NOT NULL;
ALTER TABLE Payment ADD CONSTRAINT payment_company_id_company_id_fkey FOREIGN KEY (payment_company_id) REFERENCES Company(company_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from payment_paymentkind_id to paymentkind_id
UPDATE Payment SET payment_paymentkind_id = NULL WHERE payment_paymentkind_id NOT IN (SELECT paymentkind_id FROM PaymentKind) AND payment_paymentkind_id IS NOT NULL;
ALTER TABLE Payment ADD CONSTRAINT payment_paymentkind_id_paymentkind_id_fkey FOREIGN KEY (payment_paymentkind_id) REFERENCES PaymentKind(paymentkind_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from paymentinvoice_invoice_id to invoice_id
DELETE FROM PaymentInvoice WHERE paymentinvoice_invoice_id NOT IN (SELECT invoice_id FROM Invoice) AND paymentinvoice_invoice_id IS NOT NULL;
ALTER TABLE PaymentInvoice ADD CONSTRAINT paymentinvoice_invoice_id_invoice_id_fkey FOREIGN KEY (paymentinvoice_invoice_id) REFERENCES Invoice(invoice_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from paymentinvoice_payment_id to payment_id
DELETE FROM PaymentInvoice WHERE paymentinvoice_payment_id NOT IN (SELECT payment_id FROM Payment) AND paymentinvoice_payment_id IS NOT NULL;
ALTER TABLE PaymentInvoice ADD CONSTRAINT paymentinvoice_payment_id_payment_id_fkey FOREIGN KEY (paymentinvoice_payment_id) REFERENCES Payment(payment_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from paymentinvoice_usercreate to userobm_id
UPDATE PaymentInvoice SET paymentinvoice_usercreate = NULL WHERE paymentinvoice_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND paymentinvoice_usercreate IS NOT NULL;
ALTER TABLE PaymentInvoice ADD CONSTRAINT paymentinvoice_usercreate_userobm_id_fkey FOREIGN KEY (paymentinvoice_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from paymentinvoice_userupdate to userobm_id
UPDATE PaymentInvoice SET paymentinvoice_userupdate = NULL WHERE paymentinvoice_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND paymentinvoice_userupdate IS NOT NULL;
ALTER TABLE PaymentInvoice ADD CONSTRAINT paymentinvoice_userupdate_userobm_id_fkey FOREIGN KEY (paymentinvoice_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from paymentkind_domain_id to domain_id
DELETE FROM PaymentKind WHERE paymentkind_domain_id NOT IN (SELECT domain_id FROM Domain) AND paymentkind_domain_id IS NOT NULL;
ALTER TABLE PaymentKind ADD CONSTRAINT paymentkind_domain_id_domain_id_fkey FOREIGN KEY (paymentkind_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from project_domain_id to domain_id
DELETE FROM Project WHERE project_domain_id NOT IN (SELECT domain_id FROM Domain) AND project_domain_id IS NOT NULL;
ALTER TABLE Project ADD CONSTRAINT project_domain_id_domain_id_fkey FOREIGN KEY (project_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from project_deal_id to deal_id
DELETE FROM Project WHERE project_deal_id NOT IN (SELECT deal_id FROM Deal) AND project_deal_id IS NOT NULL;
ALTER TABLE Project ADD CONSTRAINT project_deal_id_deal_id_fkey FOREIGN KEY (project_deal_id) REFERENCES Deal(deal_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from project_company_id to company_id
DELETE FROM Project WHERE project_company_id NOT IN (SELECT company_id FROM Company) AND project_company_id IS NOT NULL;
ALTER TABLE Project ADD CONSTRAINT project_company_id_company_id_fkey FOREIGN KEY (project_company_id) REFERENCES Company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from project_userupdate to userobm_id
UPDATE Project SET project_userupdate = NULL WHERE project_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND project_userupdate IS NOT NULL;
ALTER TABLE Project ADD CONSTRAINT project_userupdate_userobm_id_fkey FOREIGN KEY (project_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from project_usercreate to userobm_id
UPDATE Project SET project_usercreate = NULL WHERE project_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND project_usercreate IS NOT NULL;
ALTER TABLE Project ADD CONSTRAINT project_usercreate_userobm_id_fkey FOREIGN KEY (project_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from project_tasktype_id to tasktype_id
UPDATE Project SET project_tasktype_id = NULL WHERE project_tasktype_id NOT IN (SELECT tasktype_id FROM TaskType) AND project_tasktype_id IS NOT NULL;
ALTER TABLE Project ADD CONSTRAINT project_tasktype_id_tasktype_id_fkey FOREIGN KEY (project_tasktype_id) REFERENCES TaskType(tasktype_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from project_type_id to dealtype_id
UPDATE Project SET project_type_id = NULL WHERE project_type_id NOT IN (SELECT dealtype_id FROM DealType) AND project_type_id IS NOT NULL;
ALTER TABLE Project ADD CONSTRAINT project_type_id_dealtype_id_fkey FOREIGN KEY (project_type_id) REFERENCES DealType(dealtype_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from projectcv_project_id to project_id
DELETE FROM ProjectCV WHERE projectcv_project_id NOT IN (SELECT project_id FROM Project) AND projectcv_project_id IS NOT NULL;
ALTER TABLE ProjectCV ADD CONSTRAINT projectcv_project_id_project_id_fkey FOREIGN KEY (projectcv_project_id) REFERENCES Project(project_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from projectcv_cv_id to cv_id
DELETE FROM ProjectCV WHERE projectcv_cv_id NOT IN (SELECT cv_id FROM CV) AND projectcv_cv_id IS NOT NULL;
ALTER TABLE ProjectCV ADD CONSTRAINT projectcv_cv_id_cv_id_fkey FOREIGN KEY (projectcv_cv_id) REFERENCES CV(cv_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from projectclosing_project_id to project_id
DELETE FROM ProjectClosing WHERE projectclosing_project_id NOT IN (SELECT project_id FROM Project) AND projectclosing_project_id IS NOT NULL;
ALTER TABLE ProjectClosing ADD CONSTRAINT projectclosing_project_id_project_id_fkey FOREIGN KEY (projectclosing_project_id) REFERENCES Project(project_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from projectclosing_userupdate to userobm_id
UPDATE ProjectClosing SET projectclosing_userupdate = NULL WHERE projectclosing_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND projectclosing_userupdate IS NOT NULL;
ALTER TABLE ProjectClosing ADD CONSTRAINT projectclosing_userupdate_userobm_id_fkey FOREIGN KEY (projectclosing_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from projectclosing_usercreate to userobm_id
UPDATE ProjectClosing SET projectclosing_usercreate = NULL WHERE projectclosing_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND projectclosing_usercreate IS NOT NULL;
ALTER TABLE ProjectClosing ADD CONSTRAINT projectclosing_usercreate_userobm_id_fkey FOREIGN KEY (projectclosing_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from projectreftask_tasktype_id to tasktype_id
DELETE FROM ProjectRefTask WHERE projectreftask_tasktype_id NOT IN (SELECT tasktype_id FROM TaskType) AND projectreftask_tasktype_id IS NOT NULL;
ALTER TABLE ProjectRefTask ADD CONSTRAINT projectreftask_tasktype_id_tasktype_id_fkey FOREIGN KEY (projectreftask_tasktype_id) REFERENCES TaskType(tasktype_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from projectreftask_userupdate to userobm_id
UPDATE ProjectRefTask SET projectreftask_userupdate = NULL WHERE projectreftask_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND projectreftask_userupdate IS NOT NULL;
ALTER TABLE ProjectRefTask ADD CONSTRAINT projectreftask_userupdate_userobm_id_fkey FOREIGN KEY (projectreftask_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from projectreftask_usercreate to userobm_id
UPDATE ProjectRefTask SET projectreftask_usercreate = NULL WHERE projectreftask_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND projectreftask_usercreate IS NOT NULL;
ALTER TABLE ProjectRefTask ADD CONSTRAINT projectreftask_usercreate_userobm_id_fkey FOREIGN KEY (projectreftask_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from projecttask_project_id to project_id
DELETE FROM ProjectTask WHERE projecttask_project_id NOT IN (SELECT project_id FROM Project) AND projecttask_project_id IS NOT NULL;
ALTER TABLE ProjectTask ADD CONSTRAINT projecttask_project_id_project_id_fkey FOREIGN KEY (projecttask_project_id) REFERENCES Project(project_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from projecttask_parenttask_id to projecttask_id
DELETE FROM ProjectTask WHERE projecttask_parenttask_id NOT IN (SELECT projecttask_id FROM ProjectTask) AND projecttask_parenttask_id IS NOT NULL;
ALTER TABLE ProjectTask ADD CONSTRAINT projecttask_parenttask_id_projecttask_id_fkey FOREIGN KEY (projecttask_parenttask_id) REFERENCES ProjectTask(projecttask_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from projecttask_userupdate to userobm_id
UPDATE ProjectTask SET projecttask_userupdate = NULL WHERE projecttask_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND projecttask_userupdate IS NOT NULL;
ALTER TABLE ProjectTask ADD CONSTRAINT projecttask_userupdate_userobm_id_fkey FOREIGN KEY (projecttask_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from projecttask_usercreate to userobm_id
UPDATE ProjectTask SET projecttask_usercreate = NULL WHERE projecttask_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND projecttask_usercreate IS NOT NULL;
ALTER TABLE ProjectTask ADD CONSTRAINT projecttask_usercreate_userobm_id_fkey FOREIGN KEY (projecttask_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from projectuser_project_id to project_id
DELETE FROM ProjectUser WHERE projectuser_project_id NOT IN (SELECT project_id FROM Project) AND projectuser_project_id IS NOT NULL;
ALTER TABLE ProjectUser ADD CONSTRAINT projectuser_project_id_project_id_fkey FOREIGN KEY (projectuser_project_id) REFERENCES Project(project_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from projectuser_user_id to userobm_id
DELETE FROM ProjectUser WHERE projectuser_user_id NOT IN (SELECT userobm_id FROM UserObm) AND projectuser_user_id IS NOT NULL;
ALTER TABLE ProjectUser ADD CONSTRAINT projectuser_user_id_userobm_id_fkey FOREIGN KEY (projectuser_user_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from projectuser_userupdate to userobm_id
UPDATE ProjectUser SET projectuser_userupdate = NULL WHERE projectuser_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND projectuser_userupdate IS NOT NULL;
ALTER TABLE ProjectUser ADD CONSTRAINT projectuser_userupdate_userobm_id_fkey FOREIGN KEY (projectuser_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from projectuser_usercreate to userobm_id
UPDATE ProjectUser SET projectuser_usercreate = NULL WHERE projectuser_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND projectuser_usercreate IS NOT NULL;
ALTER TABLE ProjectUser ADD CONSTRAINT projectuser_usercreate_userobm_id_fkey FOREIGN KEY (projectuser_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from publication_domain_id to domain_id
DELETE FROM Publication WHERE publication_domain_id NOT IN (SELECT domain_id FROM Domain) AND publication_domain_id IS NOT NULL;
ALTER TABLE Publication ADD CONSTRAINT publication_domain_id_domain_id_fkey FOREIGN KEY (publication_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from publication_userupdate to userobm_id
UPDATE Publication SET publication_userupdate = NULL WHERE publication_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND publication_userupdate IS NOT NULL;
ALTER TABLE Publication ADD CONSTRAINT publication_userupdate_userobm_id_fkey FOREIGN KEY (publication_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from publication_usercreate to userobm_id
UPDATE Publication SET publication_usercreate = NULL WHERE publication_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND publication_usercreate IS NOT NULL;
ALTER TABLE Publication ADD CONSTRAINT publication_usercreate_userobm_id_fkey FOREIGN KEY (publication_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from publication_type_id to publicationtype_id
UPDATE Publication SET publication_type_id = NULL WHERE publication_type_id NOT IN (SELECT publicationtype_id FROM PublicationType) AND publication_type_id IS NOT NULL;
ALTER TABLE Publication ADD CONSTRAINT publication_type_id_publicationtype_id_fkey FOREIGN KEY (publication_type_id) REFERENCES PublicationType(publicationtype_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from publicationtype_domain_id to domain_id
DELETE FROM PublicationType WHERE publicationtype_domain_id NOT IN (SELECT domain_id FROM Domain) AND publicationtype_domain_id IS NOT NULL;
ALTER TABLE PublicationType ADD CONSTRAINT publicationtype_domain_id_domain_id_fkey FOREIGN KEY (publicationtype_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from publicationtype_userupdate to userobm_id
UPDATE PublicationType SET publicationtype_userupdate = NULL WHERE publicationtype_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND publicationtype_userupdate IS NOT NULL;
ALTER TABLE PublicationType ADD CONSTRAINT publicationtype_userupdate_userobm_id_fkey FOREIGN KEY (publicationtype_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from publicationtype_usercreate to userobm_id
UPDATE PublicationType SET publicationtype_usercreate = NULL WHERE publicationtype_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND publicationtype_usercreate IS NOT NULL;
ALTER TABLE PublicationType ADD CONSTRAINT publicationtype_usercreate_userobm_id_fkey FOREIGN KEY (publicationtype_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from rgroup_domain_id to domain_id
DELETE FROM RGroup WHERE rgroup_domain_id NOT IN (SELECT domain_id FROM Domain) AND rgroup_domain_id IS NOT NULL;
ALTER TABLE RGroup ADD CONSTRAINT rgroup_domain_id_domain_id_fkey FOREIGN KEY (rgroup_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from rgroup_userupdate to userobm_id
UPDATE RGroup SET rgroup_userupdate = NULL WHERE rgroup_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND rgroup_userupdate IS NOT NULL;
ALTER TABLE RGroup ADD CONSTRAINT rgroup_userupdate_userobm_id_fkey FOREIGN KEY (rgroup_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from rgroup_usercreate to userobm_id
UPDATE RGroup SET rgroup_usercreate = NULL WHERE rgroup_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND rgroup_usercreate IS NOT NULL;
ALTER TABLE RGroup ADD CONSTRAINT rgroup_usercreate_userobm_id_fkey FOREIGN KEY (rgroup_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from region_domain_id to domain_id
DELETE FROM Region WHERE region_domain_id NOT IN (SELECT domain_id FROM Domain) AND region_domain_id IS NOT NULL;
ALTER TABLE Region ADD CONSTRAINT region_domain_id_domain_id_fkey FOREIGN KEY (region_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from region_userupdate to userobm_id
UPDATE Region SET region_userupdate = NULL WHERE region_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND region_userupdate IS NOT NULL;
ALTER TABLE Region ADD CONSTRAINT region_userupdate_userobm_id_fkey FOREIGN KEY (region_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from region_usercreate to userobm_id
UPDATE Region SET region_usercreate = NULL WHERE region_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND region_usercreate IS NOT NULL;
ALTER TABLE Region ADD CONSTRAINT region_usercreate_userobm_id_fkey FOREIGN KEY (region_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from resource_domain_id to domain_id
DELETE FROM Resource WHERE resource_domain_id NOT IN (SELECT domain_id FROM Domain) AND resource_domain_id IS NOT NULL;
ALTER TABLE Resource ADD CONSTRAINT resource_domain_id_domain_id_fkey FOREIGN KEY (resource_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from resource_userupdate to userobm_id
UPDATE Resource SET resource_usercreate = NULL WHERE resource_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND resource_usercreate IS NOT NULL;
ALTER TABLE Resource ADD CONSTRAINT resource_userupdate_userobm_id_fkey FOREIGN KEY (resource_userupdate) REFERENCES UserObm (userobm_id) ON DELETE SET NULL ON UPDATE CASCADE;

-- Foreign key from resource_usercreate to userobm_id
UPDATE Resource SET resource_usercreate = NULL WHERE resource_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND resource_usercreate IS NOT NULL;
ALTER TABLE Resource ADD CONSTRAINT resource_usercreate_userobm_id_fkey FOREIGN KEY (resource_usercreate) REFERENCES UserObm (userobm_id) ON DELETE SET NULL ON UPDATE CASCADE;

-- Foreign key from resource_rtype_id to resourcetype_id
UPDATE Resource SET resource_rtype_id = NULL WHERE resource_rtype_id NOT IN (SELECT resourcetype_id FROM ResourceType) AND resource_rtype_id IS NOT NULL;
ALTER TABLE Resource ADD CONSTRAINT resource_rtype_id_resourcetype_id_fkey FOREIGN KEY (resource_rtype_id) REFERENCES ResourceType (resourcetype_id) ON DELETE SET NULL ON UPDATE CASCADE;

-- Foreign key from resourcegroup_rgroup_id to rgroup_id
DELETE FROM ResourceGroup WHERE resourcegroup_rgroup_id NOT IN (SELECT rgroup_id FROM RGroup) AND resourcegroup_rgroup_id IS NOT NULL;
ALTER TABLE ResourceGroup ADD CONSTRAINT resourcegroup_rgroup_id_rgroup_id_fkey FOREIGN KEY (resourcegroup_rgroup_id) REFERENCES RGroup(rgroup_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from resourcegroup_resource_id to resource_id
DELETE FROM ResourceGroup WHERE resourcegroup_resource_id NOT IN (SELECT resource_id FROM Resource) AND resourcegroup_resource_id IS NOT NULL;
ALTER TABLE ResourceGroup ADD CONSTRAINT resourcegroup_resource_id_resource_id_fkey FOREIGN KEY (resourcegroup_resource_id) REFERENCES Resource(resource_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from resourceitem_domain_id to domain_id
DELETE FROM ResourceItem WHERE resourceitem_domain_id NOT IN (SELECT domain_id FROM Domain) AND resourceitem_domain_id IS NOT NULL;
ALTER TABLE ResourceItem ADD CONSTRAINT resourceitem_domain_id_domain_id_fkey FOREIGN KEY (resourceitem_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from resourceitem_resourcetype_id to resourcetype_id
DELETE FROM ResourceItem WHERE resourceitem_resourcetype_id NOT IN (SELECT resourcetype_id FROM ResourceType) AND resourceitem_resourcetype_id IS NOT NULL;
ALTER TABLE ResourceItem ADD CONSTRAINT resourceitem_resourcetype_id_resourcetype_id_fkey FOREIGN KEY (resourceitem_resourcetype_id) REFERENCES ResourceType(resourcetype_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from resourcetype_domain_id to domain_id
UPDATE ResourceType SET resourcetype_domain_id = NULL WHERE resourcetype_domain_id NOT IN (SELECT domain_id FROM Domain) AND resourcetype_domain_id IS NOT NULL;
DELETE FROM ResourceType WHERE resourcetype_domain_id NOT IN (SELECT domain_id FROM Domain) AND resourcetype_domain_id IS NOT NULL;
ALTER TABLE ResourceType ADD CONSTRAINT resourcetype_domain_id_domain_id_fkey FOREIGN KEY (resourcetype_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from samba_domain_id to domain_id
DELETE FROM Samba WHERE samba_domain_id NOT IN (SELECT domain_id FROM Domain) AND samba_domain_id IS NOT NULL;
ALTER TABLE Samba ADD CONSTRAINT samba_domain_id_domain_id_fkey FOREIGN KEY (samba_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from subscription_domain_id to domain_id
DELETE FROM Subscription WHERE subscription_domain_id NOT IN (SELECT domain_id FROM Domain) AND subscription_domain_id IS NOT NULL;
ALTER TABLE Subscription ADD CONSTRAINT subscription_domain_id_domain_id_fkey FOREIGN KEY (subscription_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from subscription_publication_id to publication_id
DELETE FROM Subscription WHERE subscription_publication_id NOT IN (SELECT publication_id FROM Publication) AND subscription_publication_id IS NOT NULL;
ALTER TABLE Subscription ADD CONSTRAINT subscription_publication_id_publication_id_fkey FOREIGN KEY (subscription_publication_id) REFERENCES Publication(publication_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from subscription_contact_id to contact_id
DELETE FROM Subscription WHERE subscription_contact_id NOT IN (SELECT contact_id FROM Contact) AND subscription_contact_id IS NOT NULL;
ALTER TABLE Subscription ADD CONSTRAINT subscription_contact_id_contact_id_fkey FOREIGN KEY (subscription_contact_id) REFERENCES Contact(contact_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from subscription_userupdate to userobm_id
UPDATE Subscription SET subscription_userupdate = NULL WHERE subscription_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND subscription_userupdate IS NOT NULL;
ALTER TABLE Subscription ADD CONSTRAINT subscription_userupdate_userobm_id_fkey FOREIGN KEY (subscription_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from subscription_usercreate to userobm_id
UPDATE Subscription SET subscription_usercreate = NULL WHERE subscription_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND subscription_usercreate IS NOT NULL;
ALTER TABLE Subscription ADD CONSTRAINT subscription_usercreate_userobm_id_fkey FOREIGN KEY (subscription_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from subscription_reception_id to subscriptionreception_id
UPDATE Subscription SET subscription_reception_id = NULL WHERE subscription_reception_id NOT IN (SELECT subscriptionreception_id FROM SubscriptionReception) AND subscription_reception_id IS NOT NULL;
ALTER TABLE Subscription ADD CONSTRAINT subscription_reception_id_subscriptionreception_id_fkey FOREIGN KEY (subscription_reception_id) REFERENCES SubscriptionReception(subscriptionreception_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from subscriptionreception_domain_id to domain_id
DELETE FROM SubscriptionReception WHERE subscriptionreception_domain_id NOT IN (SELECT domain_id FROM Domain) AND subscriptionreception_domain_id IS NOT NULL;
ALTER TABLE SubscriptionReception ADD CONSTRAINT subscriptionreception_domain_id_domain_id_fkey FOREIGN KEY (subscriptionreception_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from subscriptionreception_userupdate to userobm_id
UPDATE SubscriptionReception SET subscriptionreception_userupdate = NULL WHERE subscriptionreception_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND subscriptionreception_userupdate IS NOT NULL;
ALTER TABLE SubscriptionReception ADD CONSTRAINT subscriptionreception_userupdate_userobm_id_fkey FOREIGN KEY (subscriptionreception_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from subscriptionreception_usercreate to userobm_id
UPDATE SubscriptionReception SET subscriptionreception_usercreate = NULL WHERE subscriptionreception_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND subscriptionreception_usercreate IS NOT NULL;
ALTER TABLE SubscriptionReception ADD CONSTRAINT subscriptionreception_usercreate_userobm_id_fkey FOREIGN KEY (subscriptionreception_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from tasktype_domain_id to domain_id
DELETE FROM TaskType WHERE tasktype_domain_id NOT IN (SELECT domain_id FROM Domain) AND tasktype_domain_id IS NOT NULL;
ALTER TABLE TaskType ADD CONSTRAINT tasktype_domain_id_domain_id_fkey FOREIGN KEY (tasktype_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from tasktype_userupdate to userobm_id
UPDATE TaskType SET tasktype_userupdate = NULL WHERE tasktype_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND tasktype_userupdate IS NOT NULL;
ALTER TABLE TaskType ADD CONSTRAINT tasktype_userupdate_userobm_id_fkey FOREIGN KEY (tasktype_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from tasktype_usercreate to userobm_id
UPDATE TaskType SET tasktype_usercreate = NULL WHERE tasktype_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND tasktype_usercreate IS NOT NULL;
ALTER TABLE TaskType ADD CONSTRAINT tasktype_usercreate_userobm_id_fkey FOREIGN KEY (tasktype_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from timetask_user_id to userobm_id
DELETE FROM TimeTask WHERE timetask_user_id NOT IN (SELECT userobm_id FROM UserObm) AND timetask_user_id IS NOT NULL;
ALTER TABLE TimeTask ADD CONSTRAINT timetask_user_id_userobm_id_fkey FOREIGN KEY (timetask_user_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from timetask_projecttask_id to projecttask_id
DELETE FROM TimeTask WHERE timetask_projecttask_id NOT IN (SELECT projecttask_id FROM ProjectTask) AND timetask_projecttask_id IS NOT NULL;
ALTER TABLE TimeTask ADD CONSTRAINT timetask_projecttask_id_projecttask_id_fkey FOREIGN KEY (timetask_projecttask_id) REFERENCES ProjectTask(projecttask_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from timetask_tasktype_id to tasktype_id
DELETE FROM TimeTask WHERE timetask_tasktype_id NOT IN (SELECT tasktype_id FROM TaskType) AND timetask_tasktype_id IS NOT NULL;
ALTER TABLE TimeTask ADD CONSTRAINT timetask_tasktype_id_tasktype_id_fkey FOREIGN KEY (timetask_tasktype_id) REFERENCES TaskType(tasktype_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from timetask_userupdate to userobm_id
UPDATE TimeTask SET timetask_userupdate = NULL WHERE timetask_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND timetask_userupdate IS NOT NULL;
ALTER TABLE TimeTask ADD CONSTRAINT timetask_userupdate_userobm_id_fkey FOREIGN KEY (timetask_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from timetask_usercreate to userobm_id
UPDATE TimeTask SET timetask_usercreate = NULL WHERE timetask_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND timetask_usercreate IS NOT NULL;
ALTER TABLE TimeTask ADD CONSTRAINT timetask_usercreate_userobm_id_fkey FOREIGN KEY (timetask_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from todo_domain_id to domain_id
DELETE FROM Todo WHERE todo_domain_id NOT IN (SELECT domain_id FROM Domain) AND todo_domain_id IS NOT NULL;
ALTER TABLE Todo ADD CONSTRAINT todo_domain_id_domain_id_fkey FOREIGN KEY (todo_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from todo_user to userobm_id
DELETE FROM Todo WHERE todo_user NOT IN (SELECT userobm_id FROM UserObm) AND todo_user IS NOT NULL;
ALTER TABLE Todo ADD CONSTRAINT todo_user_userobm_id_fkey FOREIGN KEY (todo_user) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from todo_userupdate to userobm_id
UPDATE Todo SET todo_userupdate = NULL WHERE todo_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND todo_userupdate IS NOT NULL;
ALTER TABLE Todo ADD CONSTRAINT todo_userupdate_userobm_id_fkey FOREIGN KEY (todo_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from todo_usercreate to userobm_id
UPDATE Todo SET todo_usercreate = NULL WHERE todo_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND todo_usercreate IS NOT NULL;
ALTER TABLE Todo ADD CONSTRAINT todo_usercreate_userobm_id_fkey FOREIGN KEY (todo_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from group_domain_id to domain_id
DELETE FROM UGroup WHERE group_domain_id NOT IN (SELECT domain_id FROM Domain) AND group_domain_id IS NOT NULL;
ALTER TABLE UGroup ADD CONSTRAINT group_domain_id_domain_id_fkey FOREIGN KEY (group_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from group_userupdate to userobm_id
UPDATE UGroup SET group_userupdate = NULL WHERE group_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND group_userupdate IS NOT NULL;
ALTER TABLE UGroup ADD CONSTRAINT group_userupdate_userobm_id_fkey FOREIGN KEY (group_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from group_usercreate to userobm_id
UPDATE UGroup SET group_usercreate = NULL WHERE group_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND group_usercreate IS NOT NULL;
ALTER TABLE UGroup ADD CONSTRAINT group_usercreate_userobm_id_fkey FOREIGN KEY (group_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from group_manager_id to userobm_id
UPDATE UGroup SET group_manager_id = NULL WHERE group_manager_id NOT IN (SELECT userobm_id FROM UserObm) AND group_manager_id IS NOT NULL;
ALTER TABLE UGroup ADD CONSTRAINT group_manager_id_userobm_id_fkey FOREIGN KEY (group_manager_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from updated_domain_id to domain_id
DELETE FROM Updated WHERE updated_domain_id NOT IN (SELECT domain_id FROM Domain) AND updated_domain_id IS NOT NULL;
ALTER TABLE Updated ADD CONSTRAINT updated_domain_id_domain_id_fkey FOREIGN KEY (updated_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from updated_user_id to userobm_id
UPDATE Updated SET updated_user_id = NULL WHERE updated_user_id NOT IN (SELECT userobm_id FROM UserObm) AND updated_user_id IS NOT NULL;
ALTER TABLE Updated ADD CONSTRAINT updated_user_id_userobm_id_fkey FOREIGN KEY (updated_user_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from updatedlinks_domain_id to domain_id
DELETE FROM Updatedlinks WHERE updatedlinks_domain_id NOT IN (SELECT domain_id FROM Domain) AND updatedlinks_domain_id IS NOT NULL;
ALTER TABLE Updatedlinks ADD CONSTRAINT updatedlinks_domain_id_domain_id_fkey FOREIGN KEY (updatedlinks_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from updatedlinks_user_id to userobm_id
UPDATE Updatedlinks SET updatedlinks_user_id = NULL WHERE updatedlinks_user_id NOT IN (SELECT userobm_id FROM UserObm) AND updatedlinks_user_id IS NOT NULL;
ALTER TABLE Updatedlinks ADD CONSTRAINT updatedlinks_user_id_userobm_id_fkey FOREIGN KEY (updatedlinks_user_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from userobm_domain_id to domain_id
DELETE FROM UserObm WHERE userobm_domain_id NOT IN (SELECT domain_id FROM Domain) AND userobm_domain_id IS NOT NULL;
ALTER TABLE UserObm ADD CONSTRAINT userobm_domain_id_domain_id_fkey FOREIGN KEY (userobm_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from userobm_userupdate to userobm_id
UPDATE UserObm SET userobm_userupdate = NULL WHERE userobm_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND userobm_userupdate IS NOT NULL;
ALTER TABLE UserObm ADD CONSTRAINT userobm_userupdate_userobm_id_fkey FOREIGN KEY (userobm_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from userobm_usercreate to userobm_id
UPDATE UserObm SET userobm_usercreate = NULL WHERE userobm_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND userobm_usercreate IS NOT NULL;
ALTER TABLE UserObm ADD CONSTRAINT userobm_usercreate_userobm_id_fkey FOREIGN KEY (userobm_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from userobm_mail_server_id to mailserver_id
UPDATE UserObm SET userobm_mail_server_id = NULL WHERE userobm_mail_server_id NOT IN (SELECT mailserver_id FROM MailServer) AND userobm_mail_server_id IS NOT NULL;
ALTER TABLE UserObm ADD CONSTRAINT userobm_mail_server_id_mailserver_id_fkey FOREIGN KEY (userobm_mail_server_id) REFERENCES MailServer(mailserver_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from userobm_host_id to host_id
UPDATE UserObm SET userobm_host_id = NULL WHERE userobm_host_id NOT IN (SELECT host_id FROM Host) AND userobm_host_id IS NOT NULL;
ALTER TABLE UserObm ADD CONSTRAINT userobm_host_id_host_id_fkey FOREIGN KEY (userobm_host_id) REFERENCES Host(host_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from userobm_photo_id to document_id
UPDATE UserObm SET userobm_photo_id = NULL WHERE userobm_photo_id NOT IN (SELECT document_id FROM Document) AND userobm_photo_id IS NOT NULL;
ALTER TABLE UserObm ADD CONSTRAINT userobm_photo_id_document_id_fkey FOREIGN KEY (userobm_photo_id) REFERENCES Document(document_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from userobmgroup_group_id to group_id
DELETE FROM UserObmGroup WHERE userobmgroup_group_id NOT IN (SELECT group_id FROM UGroup) AND userobmgroup_group_id IS NOT NULL;
ALTER TABLE UserObmGroup ADD CONSTRAINT userobmgroup_group_id_group_id_fkey FOREIGN KEY (userobmgroup_group_id) REFERENCES UGroup(group_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from userobmgroup_userobm_id to userobm_id
DELETE FROM UserObmGroup WHERE userobmgroup_userobm_id NOT IN (SELECT userobm_id FROM UserObm) AND userobmgroup_userobm_id IS NOT NULL;
ALTER TABLE UserObmGroup ADD CONSTRAINT userobmgroup_userobm_id_userobm_id_fkey FOREIGN KEY (userobmgroup_userobm_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from userobmpref_user_id to userobm_id
DELETE FROM UserObmPref WHERE userobmpref_user_id NOT IN (SELECT userobm_id FROM UserObm) AND userobmpref_user_id IS NOT NULL;
ALTER TABLE UserObmPref ADD CONSTRAINT userobmpref_user_id_userobm_id_fkey FOREIGN KEY (userobmpref_user_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from userobm_sessionlog_userobm_id to userobm_id
DELETE FROM UserObm_SessionLog WHERE userobm_sessionlog_userobm_id NOT IN (SELECT userobm_id FROM UserObm) AND userobm_sessionlog_userobm_id IS NOT NULL;
ALTER TABLE UserObm_SessionLog ADD CONSTRAINT userobm_sessionlog_userobm_id_userobm_id_fkey FOREIGN KEY (userobm_sessionlog_userobm_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from of_usergroup_group_id to group_id
DELETE FROM of_usergroup WHERE of_usergroup_group_id NOT IN (SELECT group_id FROM UGroup) AND of_usergroup_group_id IS NOT NULL;
ALTER TABLE of_usergroup ADD CONSTRAINT of_usergroup_group_id_group_id_fkey FOREIGN KEY (of_usergroup_group_id) REFERENCES UGroup(group_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from of_usergroup_user_id to userobm_id
DELETE FROM of_usergroup WHERE of_usergroup_user_id NOT IN (SELECT userobm_id FROM UserObm) AND of_usergroup_user_id IS NOT NULL;
ALTER TABLE of_usergroup ADD CONSTRAINT of_usergroup_user_id_userobm_id_fkey FOREIGN KEY (of_usergroup_user_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from profilemodule_profile_id to profile_id
ALTER TABLE Profile ADD CONSTRAINT profile_domain_id_domain_id_fkey FOREIGN KEY (profile_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from profile_userupdate to userobm_id
ALTER TABLE Profile ADD CONSTRAINT profile_userupdate_userobm_id_fkey FOREIGN KEY (profile_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from profile_usercreate to userobm_id
ALTER TABLE Profile ADD CONSTRAINT profile_usercreate_userobm_id_fkey FOREIGN KEY (profile_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from profilemodule_profile_id to profile_id
ALTER TABLE ProfileModule ADD CONSTRAINT profilemodule_profile_id_profile_id_fkey FOREIGN KEY (profilemodule_profile_id) REFERENCES Profile(profile_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from profilesection_profile_id to profile_id
ALTER TABLE ProfileSection ADD CONSTRAINT profilesection_profile_id_profile_id_fkey FOREIGN KEY (profilesection_profile_id) REFERENCES Profile(profile_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from profilepropertyvalue_profile_id to profile_id
ALTER TABLE ProfilePropertyValue ADD CONSTRAINT profilepropertyvalue_profile_id_profile_id_fkey FOREIGN KEY (profilepropertyvalue_profile_id) REFERENCES Profile(profile_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from profilepropertyvalue_property_id to profileproperty_id
ALTER TABLE ProfilePropertyValue ADD CONSTRAINT profilepropertyvalue_profileproperty_id_profileproperty_id_fkey FOREIGN KEY (profilepropertyvalue_property_id) REFERENCES ProfileProperty(profileproperty_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contact_birthday_id to calendarevent_id
ALTER TABLE Contact ADD CONSTRAINT contact_birthday_id_calendarevent_id_fkey FOREIGN KEY (contact_birthday_id) REFERENCES CalendarEvent(calendarevent_id) ON UPDATE CASCADE ON DELETE CASCADE;


-- DATA

-- module 'profile'
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'profile', 'profile_name', 1, 2);
