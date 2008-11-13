--
-- New table
--
DROP TABLE IF EXISTS OBMTicket; 
--
-- Service
--
CREATE TABLE Service (
  service_id                                    int(8) NOT NULL auto_increment,
  service_key                                   varchar(255),
  PRIMARY KEY (service_id),
  UNIQUE KEY service_key (service_key)
);

--
-- Table structure for table `ServiceProperty`
--
CREATE TABLE ServiceProperty (
  serviceproperty_id                            int(8) NOT NULL auto_increment,
  serviceproperty_service_id                    int(8) NOT NULL,
  serviceproperty_key                           varchar(255),
  serviceproperty_default                       text,
  serviceproperty_type                          varchar(255),
  serviceproperty_min                           tinyint,
  serviceproperty_max                           tinyint,
  PRIMARY KEY (serviceproperty_id),
  UNIQUE KEY serviceproperty_key (serviceproperty_key)
);

--
-- Table structure for table `ServicePropertyDomain`
--
CREATE TABLE ServicePropertyDomain (
  servicepropertydomain_id                      int(8) NOT NULL,
  PRIMARY KEY (servicepropertydomain_id)
);

--
-- Table structure for table `ServicePropertyHost`
--
CREATE TABLE ServicePropertyHost (
  servicepropertyhost_id                        int(8) NOT NULL,
  PRIMARY KEY (servicepropertyhost_id),
  KEY servicepropertyhost_id_serviceproperty_id_fkey (servicepropertyhost_id)
);

--
-- Table structure for table `DomainServicePropertyValue`
--
CREATE TABLE DomainServiceValue (
  domainservicevalue_serviceproperty_id int(8) NOT NULL,
  domainservicevalue_domain_id          int(8) NOT NULL,
  domainservicevalue_value              text,
  PRIMARY KEY (domainservicevalue_serviceproperty_id,domainservicevalue_domain_id)
);

--
-- Table structure for table `HostServicePropertyValue`
--
CREATE TABLE HostServiceValue (
  hostservicevalue_serviceproperty_id   int(8) NOT NULL,
  hostservicevalue_host_id              int(8) NOT NULL,
  hostservicevalue_value                text,
  PRIMARY KEY (hostservicevalue_serviceproperty_id,hostservicevalue_host_id)
);

--
-- Table structure for table `ServiceDomain`
--
CREATE TABLE ServiceDomain (
  servicedomain_id                              int(8) NOT NULL,
  PRIMARY KEY (servicedomain_id)
);

--
-- Table structure for table `ServiceHost`
--
CREATE TABLE ServiceHost (
  servicehost_id                                int(8) NOT NULL,
  PRIMARY KEY (servicehost_id)
);

--
-- Table structure for table `DomainService`
--
CREATE TABLE DomainService (
  domainservice_service_id                      int(8) NOT NULL,
  domainservice_domain_id                       int(8) NOT NULL,
  PRIMARY KEY (domainservice_service_id,domainservice_domain_id)
);

--
-- Table structure for table `DomainService`
--
CREATE TABLE HostService (
  hostservice_service_id                        int(8) NOT NULL,
  hostservice_host_id                           int(8) NOT NULL,
  PRIMARY KEY (hostservice_service_id,hostservice_host_id),
  KEY hostservice_service_id_hostservice_host_id_fkey (hostservice_service_id),
  KEY hostservice_host_id_host_id_fkey (hostservice_host_id)
);

--
-- Address
--
DROP TABLE IF EXISTS Address;
CREATE TABLE Address (
  address_id                                    int(8) NOT NULL auto_increment,
  address_street1                               varchar(255),
  address_street2                               varchar(255),
  address_street3                               varchar(2555),
  address_zipcode                               varchar(14),
  address_town                                  varchar(128),
  address_expresspostal                         varchar(16),
  address_country                               char(2),
  address_im                                    varchar(255),
  address_label                                 varchar(255),
  PRIMARY KEY (address_id)
);
--
-- Phone
--
DROP TABLE IF EXISTS Phone;
CREATE TABLE Phone (
  phone_id                                      int(8) NOT NULL auto_increment,
  phone_label                                   varchar(255) NOT NULL,
  phone_number                                  varchar(32),
  PRIMARY KEY (phone_id)
);
--
-- Website
--
DROP TABLE IF EXISTS Website;
CREATE TABLE Website (
  website_id                                    int(8) NOT NULL auto_increment,
  website_label                                 varchar(255) NOT NULL,
  website_number                                varchar(32),
  PRIMARY KEY (website_id)
);
--
-- Email
--
DROP TABLE IF EXISTS Email;
CREATE TABLE Email (
  email_id                                      int(8) NOT NULL auto_increment,
  email_label                                   varchar(255) NOT NULL,
  email_address                                 varchar(255),
  PRIMARY KEY (email_id),
  KEY email_address (email_address)
);
--
-- Instant Messaging
--
DROP TABLE IF EXISTS IM;
CREATE TABLE IM (
  im_id                                         int(8) NOT NULL auto_increment,
  im_label                                      varchar(255) NOT NULL,
  im_address                                    varchar(255),
  PRIMARY KEY (im_id),
  KEY im_address (im_address)
);--
-- ContactAddress
--
DROP TABLE IF EXISTS ContactAddress;
CREATE TABLE ContactAddress (
  contactaddress_address_id                     int(8) NOT NULL,
  contactaddress_contact_id                     int(8) NOT NULL,
  PRIMARY KEY(contactaddress_contact_id,contactaddress_address_id),
  KEY contactaddress_address_id_address_id_fkey (contactaddress_address_id),
  KEY contactaddress_contact_id_contact_id_fkey (contactaddress_contact_id)
);
--
-- ContactPhone
--
DROP TABLE IF EXISTS ContactPhone;
CREATE TABLE ContactPhone (
  contactphone_phone_id                         int(8) NOT NULL,
  contactphone_contact_id                       int(8) NOT NULL,
  PRIMARY KEY(contactphone_contact_id,contactphone_phone_id)
);
--
-- ContactWebsite
--
DROP TABLE IF EXISTS ContactWebsite;
CREATE TABLE ContactWebsite (
  contactwebsite_website_id                     int(8) NOT NULL,
  contactwebsite_contact_id                     int(8) NOT NULL,
  PRIMARY KEY(contactwebsite_contact_id,contactwebsite_website_id)
);

--
-- ContactEmail
--
DROP TABLE IF EXISTS ContactEmail;
CREATE TABLE ContactEmail (
  contactemail_email_id                         int(8) NOT NULL,
  contactemail_contact_id                       int(8) NOT NULL,
  PRIMARY KEY(contactemail_contact_id,contactemail_email_id)
);

--
-- ContactIM
--
DROP TABLE IF EXISTS ContactIM;
CREATE TABLE ContactIM (
  contactim_im_id                               int(8) NOT NULL,
  contactim_contact_id                          int(8) NOT NULL,
  PRIMARY KEY(contactim_contact_id,contactim_im_id)
);

--
-- CompanyAddress
--
DROP TABLE IF EXISTS CompanyAddress;
CREATE TABLE CompanyAddress (
  companyaddress_address_id                     int(8) NOT NULL,
  companyaddress_company_id                     int(8) NOT NULL,
  PRIMARY KEY(companyaddress_company_id,companyaddress_address_id)
);

--
-- CompanyPhone
--
DROP TABLE IF EXISTS CompanyPhone;
CREATE TABLE CompanyPhone (
  companyphone_phone_id                         int(8) NOT NULL,
  companyphone_company_id                       int(8) NOT NULL,
  PRIMARY KEY(companyphone_company_id,companyphone_phone_id)
);

--
-- CompanyWebsite
--
DROP TABLE IF EXISTS CompanyWebsite;
CREATE TABLE CompanyWebsite (
  companywebsite_website_id                     int(8) NOT NULL,
  companywebsite_company_id                     int(8) NOT NULL,
  PRIMARY KEY(companywebsite_company_id,companywebsite_website_id)
);

--
-- CompanyEmail
--
DROP TABLE IF EXISTS CompanyEmail;
CREATE TABLE CompanyEmail (
  companyemail_email_id                         int(8) NOT NULL,
  companyemail_company_id                       int(8) NOT NULL,
  PRIMARY KEY(companyemail_company_id,companyemail_email_id),
  KEY companyemail_email_id_email_id_fkey (companyemail_email_id),
  KEY companyemail_company_id_company_id_fkey (companyemail_company_id)
);

--
-- Set integer to boolean when necessary
--

-- Domain
ALTER TABLE Domain ADD COLUMN domain_global BOOLEAN DEFAULT FALSE;
ALTER TABLE Domain DROP COLUMN domain_mail_server_id;
ALTER TABLE Domain ADD COLUMN domain_mail_server_auto int(2) default NULL;

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
ALTER TABLE OGroup MODIFY COLUMN ogroup_parent_id int(8);


-- -----------------------------------------------------------------------------
--
-- CalendarEvent + Todo to Event
--

-- Event Creation
CREATE TABLE Event (
  event_id              int(8) NOT NULL auto_increment,
  event_domain_id       int(8) NOT NULL,
  event_timeupdate      timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  event_timecreate      timestamp NOT NULL default '0000-00-00 00:00:00',
  event_userupdate      int(8) default NULL,
  event_usercreate      int(8) default NULL,
  event_ext_id          varchar(255) default '',
  event_type            enum('VEVENT', 'VTODO', 'VJOURNAL', 'VFREEBUSY') default 'VEVENT',
  event_origin          varchar(255) NOT NULL default '',
  event_owner           int(8) default NULL,
  event_timezone        varchar(255) default 'GMT',
  event_opacity         enum('OPAQUE', 'TRANSPARENT') default 'OPAQUE',
  event_title           varchar(255) default NULL,
  event_location        varchar(100) default NULL,
  event_category1_id    int(8) default NULL,
  event_priority        int(2) default NULL,
  event_privacy         int(2) NOT NULL default '0',
  event_date            datetime NOT NULL,
  event_duration        int(8) NOT NULL default '0',
  event_allday          boolean default false,
  event_repeatkind      varchar(20) default NULL,
  event_repeatfrequence int(3) default NULL,
  event_repeatdays      varchar(7) default NULL,
  event_endrepeat       datetime default NULL,
  event_color           varchar(7) default NULL,
  event_completed       datetime,
  event_url             text,
  event_description     text,
  event_properties      text,
  PRIMARY KEY (event_id),
  KEY event_domain_id_domain_id_fkey (event_domain_id),
  KEY event_owner_userobm_id_fkey (event_owner),
  KEY event_userupdate_userobm_id_fkey (event_userupdate),
  KEY event_usercreate_userobm_id_fkey (event_usercreate),
  KEY event_category1_id_calendarcategory1_id_fkey (event_category1_id)
);

-- Clean CalendarEvent before migration to Event
-- Foreign key domain_id
DELETE FROM CalendarEvent WHERE calendarevent_domain_id NOT IN (SELECT domain_id FROM Domain) AND calendarevent_domain_id IS NOT NULL;
-- Foreign key from calendarevent_userupdate to userobm_id
UPDATE CalendarEvent SET calendarevent_userupdate = NULL WHERE calendarevent_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND calendarevent_userupdate IS NOT NULL;
-- Foreign key from calendarevent_usercreate to userobm_id
UPDATE CalendarEvent SET calendarevent_usercreate = NULL WHERE calendarevent_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND calendarevent_usercreate IS NOT NULL;
-- Foreign key from calendarevent_owner to userobm_id
DELETE FROM CalendarEvent WHERE calendarevent_owner NOT IN (SELECT userobm_id FROM UserObm) AND calendarevent_owner IS NOT NULL;
-- Foreign key from calendarevent_category1_id to calendarcategory1_id
UPDATE CalendarEvent SET calendarevent_category1_id = NULL WHERE calendarevent_category1_id NOT IN (SELECT calendarcategory1_id FROM CalendarCategory1) AND calendarevent_category1_id IS NOT NULL;


INSERT INTO Event (event_id,
  event_domain_id, 
  event_timeupdate,
  event_timecreate,
  event_userupdate,
  event_usercreate,
  event_ext_id,
  event_type,
  event_origin,
  event_owner,
  event_timezone,
  event_opacity,
  event_title,
  event_location,
  event_category1_id,
  event_priority,
  event_privacy,
  event_date,
  event_duration,
  event_allday,
  event_repeatkind,
  event_repeatfrequence,
  event_repeatdays,
  event_endrepeat,
  event_color,
  event_completed,
  event_url,
  event_description,
  event_properties)
SELECT
  calendarevent_id,
  calendarevent_domain_id, 
  calendarevent_timeupdate,
  calendarevent_timecreate,
  calendarevent_userupdate,
  calendarevent_usercreate,
  calendarevent_ext_id,
  'VEVENT',
  'migration',
  calendarevent_owner,
  'Europe/Paris',
  'OPAQUE',
  calendarevent_title,
  calendarevent_location,
  calendarevent_category1_id,
  calendarevent_priority,
  calendarevent_privacy,
  calendarevent_date,
  calendarevent_duration,
  calendarevent_allday,
  calendarevent_repeatkind,
  calendarevent_repeatfrequence,
  calendarevent_repeatdays,
  calendarevent_endrepeat,
  calendarevent_color,
  NULL,
  NULL,
  calendarevent_description,
  calendarevent_properties
FROM CalendarEvent;

-- Clean Todo before migration to Event
-- Foreign key from todo_domain_id to domain_id
DELETE FROM Todo WHERE todo_domain_id NOT IN (SELECT domain_id FROM Domain) AND todo_domain_id IS NOT NULL;
-- Foreign key from todo_user to userobm_id
DELETE FROM Todo WHERE todo_user NOT IN (SELECT userobm_id FROM UserObm) AND todo_user IS NOT NULL;
-- Foreign key from todo_userupdate to userobm_id
UPDATE Todo SET todo_userupdate = NULL WHERE todo_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND todo_userupdate IS NOT NULL;
-- Foreign key from todo_usercreate to userobm_id
UPDATE Todo SET todo_usercreate = NULL WHERE todo_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND todo_usercreate IS NOT NULL;

INSERT INTO Event (
  event_domain_id, 
  event_timeupdate,
  event_timecreate,
  event_userupdate,
  event_usercreate,
  event_type,
  event_origin,
  event_owner,
  event_timezone,
  event_opacity,
  event_title,
  event_priority,
  event_privacy,
  event_date,
  event_duration,
  event_allday,
  event_repeatkind,
  event_repeatfrequence,
  event_repeatdays,
  event_endrepeat,
  event_color,
  event_completed,
  event_url,
  event_description)
SELECT
  todo_domain_id,
  todo_timeupdate,
  todo_timecreate,
  todo_userupdate,
  todo_usercreate,
  'VTODO',
  'migration',
  todo_user,
  'Europe/Paris',
  'OPAQUE',
  todo_title,
  todo_priority,
  todo_privacy,
  todo_deadline,
  3600,
  FALSE,
  NULL,
  NULL,
  NULL,
  NULL,
  NULL,
  todo_deadline,
  NULL,
  todo_content
FROM Todo;


-- Table EventEntity

ALTER TABLE EventEntity ADD COLUMN evententity_state2 enum('NEEDS-ACTION', 'ACCEPTED', 'DECLINED', 'TENTATIVE', 'DELEGATED', 'COMPLETED', 'IN-PROGRESS') default 'NEEDS-ACTION';
UPDATE EventEntity set evententity_state2 = 'ACCEPTED' where evententity_state!='A' AND evententity_state!='W' AND evententity_state!='R';
UPDATE EventEntity set evententity_state2 = 'ACCEPTED' where evententity_state='A';
UPDATE EventEntity set evententity_state2 = 'NEEDS-ACTION' where evententity_state='W';
UPDATE EventEntity set evententity_state2 = 'DECLINED' where evententity_state='R';
ALTER TABLE EventEntity DROP COLUMN evententity_state;
ALTER TABLE EventEntity CHANGE COLUMN evententity_state2 evententity_state enum('NEEDS-ACTION', 'ACCEPTED', 'DECLINED', 'TENTATIVE', 'DELEGATED', 'COMPLETED', 'IN-PROGRESS') default 'NEEDS-ACTION';

ALTER TABLE EventEntity CHANGE COLUMN evententity_required evententity_required enum('CHAIR', 'REQ', 'OPT', 'NON') default 'REQ';
UPDATE EventEntity set evententity_required = 'REQ';

ALTER TABLE EventEntity ADD COLUMN evententity_percent float default 0;

-- Foreign key from evententity_event_id to event_id
DELETE FROM EventEntity WHERE evententity_event_id NOT IN (SELECT event_id FROM Event) AND evententity_event_id IS NOT NULL;

-- Foreign key from evententity_userupdate to userobm_id
UPDATE EventEntity SET evententity_userupdate = NULL WHERE evententity_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND evententity_userupdate IS NOT NULL;

-- Foreign key from evententity_usercreate to userobm_id
UPDATE EventEntity SET evententity_usercreate = NULL WHERE evententity_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND evententity_usercreate IS NOT NULL;


-- Table EventAlert

CREATE TABLE EventAlert (
  eventalert_timeupdate timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  eventalert_timecreate timestamp NOT NULL default '0000-00-00 00:00:00',
  eventalert_userupdate int(8) default NULL,
  eventalert_usercreate int(8) default NULL,
  eventalert_event_id   int(8) default NULL,
  eventalert_user_id    int(8) default NULL,
  eventalert_duration   int(8) NOT NULL default 0,
  KEY idx_eventalert_user (eventalert_user_id),
  KEY eventalert_event_id_event_id_fkey (eventalert_event_id),
  KEY eventalert_userupdate_userobm_id_fkey (eventalert_userupdate),
  KEY eventalert_usercreate_userobm_id_fkey (eventalert_usercreate)
);

-- Clean CalendarAlert before migration to EventAlert
-- Foreign key from calendaralert_event_id to event_id
DELETE FROM CalendarAlert WHERE calendaralert_event_id NOT IN (SELECT event_id FROM Event) AND calendaralert_event_id IS NOT NULL;

-- Foreign key from calendaralert_user_id to userobm_id
DELETE FROM CalendarAlert WHERE calendaralert_user_id NOT IN (SELECT userobm_id FROM UserObm) AND calendaralert_user_id IS NOT NULL;

-- Foreign key from calendaralert_userupdate to userobm_id
UPDATE CalendarAlert SET calendaralert_userupdate = NULL WHERE calendaralert_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND calendaralert_userupdate IS NOT NULL;

-- Foreign key from calendaralert_usercreate to userobm_id
UPDATE CalendarAlert SET calendaralert_usercreate = NULL WHERE calendaralert_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND calendaralert_usercreate IS NOT NULL;


INSERT INTO EventAlert (eventalert_timeupdate,
  eventalert_timecreate,
  eventalert_userupdate,
  eventalert_usercreate,
  eventalert_event_id,
  eventalert_user_id,
  eventalert_duration)
SELECT
  calendaralert_timeupdate,
  calendaralert_timecreate,
  calendaralert_userupdate,
  calendaralert_usercreate,
  calendaralert_event_id,
  calendaralert_user_id,
  calendaralert_duration
FROM CalendarAlert;


-- Table EventException

CREATE TABLE EventException (
  eventexception_timeupdate timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  eventexception_timecreate timestamp NOT NULL default '0000-00-00 00:00:00',
  eventexception_userupdate int(8) default NULL,
  eventexception_usercreate int(8) default NULL,
  eventexception_event_id   int(8) NOT NULL,
  eventexception_date       datetime NOT NULL,
  PRIMARY KEY (eventexception_event_id,eventexception_date),
  KEY eventexception_userupdate_userobm_id_fkey (eventexception_userupdate),
  KEY eventexception_usercreate_userobm_id_fkey (eventexception_usercreate)
);
-- Clean CalendarException before migration to EventException
-- Foreign key from calendarexception_event_id to event_id
DELETE FROM CalendarException WHERE calendarexception_event_id NOT IN (SELECT event_id FROM Event) AND calendarexception_event_id IS NOT NULL;

-- Foreign key from calendarexception_userupdate to userobm_id
UPDATE CalendarException SET calendarexception_userupdate = NULL WHERE calendarexception_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND calendarexception_userupdate IS NOT NULL;

-- Foreign key from calendarexception_usercreate to userobm_id
UPDATE CalendarException SET calendarexception_usercreate = NULL WHERE calendarexception_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND calendarexception_usercreate IS NOT NULL;


INSERT INTO EventException (eventexception_timeupdate,
  eventexception_timecreate,
  eventexception_userupdate,
  eventexception_usercreate,
  eventexception_event_id,
  eventexception_date)
SELECT
  calendarexception_timeupdate,
  calendarexception_timecreate,
  calendarexception_userupdate,
  calendarexception_usercreate,
  calendarexception_event_id,
  calendarexception_date
FROM CalendarException;


--
-- Table `DeletedEvent`
--

CREATE TABLE DeletedEvent (
  deletedevent_id        int(8) NOT NULL auto_increment,
  deletedevent_event_id  int(8) default NULL,
  deletedevent_user_id   int(8) default NULL,
  deletedevent_timestamp timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  PRIMARY KEY (deletedevent_id),
  KEY idx_dce_event (deletedevent_event_id),
  KEY idx_dce_user (deletedevent_user_id)
);
INSERT INTO DeletedEvent (deletedevent_id,
  deletedevent_event_id,
  deletedevent_user_id,
  deletedevent_timestamp)
SELECT
  deletedcalendarevent_id,
  deletedcalendarevent_event_id,
  deletedcalendarevent_user_id,
  deletedcalendarevent_timestamp
FROM DeletedCalendarEvent;


DROP Table DeletedCalendarEvent;
DROP Table CalendarAlert;
DROP Table CalendarException;
DROP Table CalendarEvent;
-- -----------------------------------------------------------------------------


-- Preferences
ALTER TABLE DisplayPref DROP PRIMARY KEY;
ALTER TABLE DisplayPref ADD COLUMN display_id int(8) auto_increment PRIMARY KEY FIRST;

-- Contact
ALTER TABLE Contact ADD COLUMN contact_birthday_id int(8) default NULL;
ALTER TABLE Contact ADD COLUMN contact_collected int(1) default false;
ALTER TABLE Contact ADD COLUMN contact_origin VARCHAR(255);
UPDATE Contact SET contact_origin='obm21';
ALTER TABLE Contact MODIFY COLUMN contact_origin VARCHAR(255) NOT NULL;


-- Set Defaults 
ALTER TABLE Account MODIFY COLUMN account_domain_id int(8) NOT NULL ;
ALTER TABLE CV MODIFY COLUMN cv_domain_id int(8) NOT NULL ;
ALTER TABLE CalendarCategory1 MODIFY COLUMN calendarcategory1_domain_id int(8) NOT NULL ;
ALTER TABLE CategoryLink MODIFY COLUMN categorylink_category_id int(8) NOT NULL ;
ALTER TABLE CategoryLink MODIFY COLUMN categorylink_entity_id int(8) NOT NULL ;
ALTER TABLE Company MODIFY COLUMN company_domain_id int(8) NOT NULL ;
ALTER TABLE Company MODIFY COLUMN company_datasource_id int(8)  default NULL;
ALTER TABLE CompanyActivity MODIFY COLUMN companyactivity_domain_id int(8) NOT NULL ;
ALTER TABLE CompanyNafCode MODIFY COLUMN companynafcode_domain_id int(8) NOT NULL ;
ALTER TABLE CompanyType MODIFY COLUMN companytype_domain_id int(8) NOT NULL ;
ALTER TABLE Contact MODIFY COLUMN contact_domain_id int(8) NOT NULL ;
ALTER TABLE Contact MODIFY COLUMN contact_datasource_id int(8)  default NULL;
ALTER TABLE ContactFunction MODIFY COLUMN contactfunction_domain_id int(8) NOT NULL ;
ALTER TABLE ContactList MODIFY COLUMN contactlist_list_id int(8) NOT NULL ;
ALTER TABLE ContactList MODIFY COLUMN contactlist_contact_id int(8) NOT NULL ;
ALTER TABLE Contract MODIFY COLUMN contract_domain_id int(8) NOT NULL ;
ALTER TABLE ContractPriority MODIFY COLUMN contractpriority_domain_id int(8) NOT NULL ;
ALTER TABLE ContractStatus MODIFY COLUMN contractstatus_domain_id int(8) NOT NULL ;
ALTER TABLE ContractType MODIFY COLUMN contracttype_domain_id int(8) NOT NULL ;
ALTER TABLE Country MODIFY COLUMN country_domain_id int(8) NOT NULL ;
ALTER TABLE DataSource MODIFY COLUMN datasource_domain_id int(8) NOT NULL ;
ALTER TABLE Deal MODIFY COLUMN deal_domain_id int(8) NOT NULL ;
ALTER TABLE Deal MODIFY COLUMN deal_company_id int(8) NOT NULL ;
ALTER TABLE DealCompanyRole MODIFY COLUMN dealcompanyrole_domain_id int(8) NOT NULL ;
ALTER TABLE DealStatus MODIFY COLUMN dealstatus_domain_id int(8) NOT NULL ;
ALTER TABLE DealType MODIFY COLUMN dealtype_domain_id int(8) NOT NULL ;
ALTER TABLE DefaultOdtTemplate MODIFY COLUMN defaultodttemplate_domain_id int(8) NOT NULL ;
ALTER TABLE DeletedContact MODIFY COLUMN deletedcontact_contact_id int(8) NOT NULL ;
ALTER TABLE DeletedTodo MODIFY COLUMN deletedtodo_todo_id int(8) NOT NULL;
ALTER TABLE DeletedUser MODIFY COLUMN deleteduser_user_id int(8) NOT NULL ;
ALTER TABLE Document MODIFY COLUMN document_domain_id int(8) NOT NULL ;
ALTER TABLE DocumentMimeType MODIFY COLUMN documentmimetype_domain_id int(8) NOT NULL ;
ALTER TABLE DomainMailServer MODIFY COLUMN domainmailserver_domain_id int(8) NOT NULL ;
ALTER TABLE EntityRight MODIFY COLUMN entityright_entity_id int(8) NOT NULL ;
ALTER TABLE EntityRight ADD entityright_access int(1) NOT NULL default 0;
ALTER TABLE EntityRight MODIFY COLUMN entityright_consumer_id int(8) NOT NULL ;
ALTER TABLE EventEntity MODIFY COLUMN evententity_event_id int(8) NOT NULL ;
ALTER TABLE EventEntity MODIFY COLUMN evententity_entity_id int(8) NOT NULL ;
ALTER TABLE GroupGroup MODIFY COLUMN groupgroup_parent_id int(8) NOT NULL ;
ALTER TABLE GroupGroup MODIFY COLUMN groupgroup_child_id int(8) NOT NULL ;
ALTER TABLE Host MODIFY COLUMN host_domain_id int(8) NOT NULL ;
ALTER TABLE `Import` MODIFY COLUMN import_domain_id int(8) NOT NULL ;
ALTER TABLE `Import` MODIFY COLUMN import_datasource_id int(8)  default NULL;
ALTER TABLE Incident MODIFY COLUMN incident_domain_id int(8) NOT NULL ;
ALTER TABLE Incident MODIFY COLUMN incident_priority_id int(8)  default NULL;
ALTER TABLE Incident MODIFY COLUMN incident_status_id int(8)  default NULL;
ALTER TABLE Incident MODIFY COLUMN incident_resolutiontype_id int(11)  default NULL;
ALTER TABLE IncidentPriority MODIFY COLUMN incidentpriority_domain_id int(8) NOT NULL ;
ALTER TABLE IncidentResolutionType MODIFY COLUMN incidentresolutiontype_domain_id int(8) NOT NULL ;
ALTER TABLE IncidentStatus MODIFY COLUMN incidentstatus_domain_id int(8) NOT NULL ;
ALTER TABLE Invoice MODIFY COLUMN invoice_domain_id int(8) NOT NULL ;
ALTER TABLE Invoice MODIFY COLUMN invoice_status_id int(4) NOT NULL ;
ALTER TABLE Kind MODIFY COLUMN kind_domain_id int(8) NOT NULL ;
ALTER TABLE Lead MODIFY COLUMN lead_domain_id int(8) NOT NULL ;
ALTER TABLE Lead MODIFY COLUMN lead_company_id int(8) NOT NULL ;
ALTER TABLE LeadSource MODIFY COLUMN leadsource_domain_id int(8) NOT NULL ;
ALTER TABLE LeadStatus MODIFY COLUMN leadstatus_domain_id int(8) NOT NULL ;
ALTER TABLE List MODIFY COLUMN list_domain_id int(8) NOT NULL ;
ALTER TABLE MailServer MODIFY COLUMN mailserver_host_id int(8) NOT NULL ;
ALTER TABLE MailServerNetwork MODIFY COLUMN mailservernetwork_host_id int(8) NOT NULL ;
ALTER TABLE MailShare MODIFY COLUMN mailshare_domain_id int(8) NOT NULL ;
ALTER TABLE MailShare MODIFY COLUMN mailshare_mail_server_id int(8)  default NULL;
ALTER TABLE OGroup MODIFY COLUMN ogroup_domain_id int(8) NOT NULL ;
ALTER TABLE OGroupEntity MODIFY COLUMN ogroupentity_domain_id int(8) NOT NULL ;
ALTER TABLE OrganizationalChart MODIFY COLUMN organizationalchart_domain_id int(8) NOT NULL ;
ALTER TABLE P_EntityRight MODIFY COLUMN entityright_entity_id int(8) NOT NULL ;
ALTER TABLE P_EntityRight ADD entityright_access int(1) NOT NULL default 0;
ALTER TABLE P_EntityRight MODIFY COLUMN entityright_consumer_id int(8) NOT NULL ;
ALTER TABLE P_GroupGroup MODIFY COLUMN groupgroup_parent_id int(8) NOT NULL ;
ALTER TABLE P_GroupGroup MODIFY COLUMN groupgroup_child_id int(8) NOT NULL ;
ALTER TABLE P_Host MODIFY COLUMN host_domain_id int(8) NOT NULL ;
ALTER TABLE P_MailServer MODIFY COLUMN mailserver_host_id int(8) NOT NULL ;
ALTER TABLE P_MailServerNetwork MODIFY COLUMN mailservernetwork_host_id int(8) NOT NULL ;
ALTER TABLE P_MailShare MODIFY COLUMN mailshare_domain_id int(8) NOT NULL ;
ALTER TABLE P_MailShare MODIFY COLUMN mailshare_mail_server_id int(8)  default NULL;
ALTER TABLE P_Samba MODIFY COLUMN samba_domain_id int(8) NOT NULL ;
ALTER TABLE P_UGroup MODIFY COLUMN group_domain_id int(8) NOT NULL ;
ALTER TABLE P_UserObmGroup MODIFY COLUMN userobmgroup_group_id int(8) NOT NULL ;
ALTER TABLE P_UserObmGroup MODIFY COLUMN userobmgroup_userobm_id int(8) NOT NULL ;
ALTER TABLE P_of_usergroup MODIFY COLUMN of_usergroup_group_id int(8) NOT NULL ;
ALTER TABLE P_of_usergroup MODIFY COLUMN of_usergroup_user_id int(8) NOT NULL ;
ALTER TABLE ParentDeal MODIFY COLUMN parentdeal_domain_id int(8) NOT NULL ;
ALTER TABLE Payment MODIFY COLUMN payment_domain_id int(8) NOT NULL ;
ALTER TABLE PaymentKind MODIFY COLUMN paymentkind_domain_id int(8) NOT NULL ;
ALTER TABLE Project MODIFY COLUMN project_domain_id int(8) NOT NULL ;
ALTER TABLE ProjectTask MODIFY COLUMN projecttask_parenttask_id int(8)  default NULL;
ALTER TABLE Publication MODIFY COLUMN publication_domain_id int(8) NOT NULL ;
ALTER TABLE PublicationType MODIFY COLUMN publicationtype_domain_id int(8) NOT NULL ;
ALTER TABLE RGroup MODIFY COLUMN rgroup_domain_id int(8) NOT NULL ;
ALTER TABLE Region MODIFY COLUMN region_domain_id int(8) NOT NULL ;
ALTER TABLE Resource MODIFY COLUMN resource_domain_id int(8) NOT NULL ;
ALTER TABLE ResourceGroup MODIFY COLUMN resourcegroup_rgroup_id int(8) NOT NULL ;
ALTER TABLE ResourceGroup MODIFY COLUMN resourcegroup_resource_id int(8) NOT NULL ;
ALTER TABLE ResourceItem MODIFY COLUMN resourceitem_domain_id int(8) NOT NULL ;
ALTER TABLE ResourceType MODIFY COLUMN resourcetype_domain_id int(8) NOT NULL ;
ALTER TABLE Samba MODIFY COLUMN samba_domain_id int(8) NOT NULL ;
ALTER TABLE Subscription MODIFY COLUMN subscription_domain_id int(8) NOT NULL ;
ALTER TABLE SubscriptionReception MODIFY COLUMN subscriptionreception_domain_id int(8) NOT NULL ;
ALTER TABLE TaskType MODIFY COLUMN tasktype_domain_id int(8) NOT NULL ;
ALTER TABLE UGroup MODIFY COLUMN group_domain_id int(8) NOT NULL ;
ALTER TABLE UserObmGroup MODIFY COLUMN userobmgroup_group_id int(8) NOT NULL ;
ALTER TABLE UserObmGroup MODIFY COLUMN userobmgroup_userobm_id int(8) NOT NULL ;
ALTER TABLE UserObmPref MODIFY COLUMN userobmpref_user_id int(8) NULL ;
ALTER TABLE of_usergroup MODIFY COLUMN of_usergroup_group_id int(8) NOT NULL ;
ALTER TABLE of_usergroup MODIFY COLUMN of_usergroup_user_id int(8) NOT NULL ;
ALTER TABLE Category MODIFY COLUMN category_usercreate int(8) DEFAULT NULL;
ALTER TABLE Category MODIFY COLUMN category_userupdate int(8) DEFAULT NULL;
ALTER TABLE Deal MODIFY COLUMN deal_region_id int(8) DEFAULT NULL;
ALTER TABLE Deal MODIFY COLUMN deal_source_id int(8) DEFAULT NULL;
ALTER TABLE DealCompany MODIFY COLUMN dealcompany_role_id int(8) DEFAULT NULL;
ALTER TABLE Contract MODIFY COLUMN contract_priority_id int(8) DEFAULT NULL;
ALTER TABLE Contract MODIFY COLUMN contract_status_id int(8) DEFAULT NULL;
ALTER TABLE Document MODIFY COLUMN document_mimetype_id int(8) DEFAULT NULL;
ALTER TABLE Lead MODIFY COLUMN lead_contact_id int(8) DEFAULT NULL;
ALTER TABLE Payment MODIFY COLUMN payment_company_id int(8) DEFAULT NULL;
ALTER TABLE DisplayPref MODIFY COLUMN display_user_id int(8) DEFAULT NULL;
ALTER TABLE Payment MODIFY COLUMN payment_paymentkind_id int(8) DEFAULT NULL;
ALTER TABLE ProjectClosing MODIFY COLUMN projectclosing_usercreate int(8) DEFAULT NULL;
ALTER TABLE Subscription MODIFY COLUMN subscription_reception_id int(8) DEFAULT NULL;
ALTER TABLE UserObm MODIFY COLUMN userobm_host_id int(8) DEFAULT NULL;
ALTER TABLE UGroup MODIFY COLUMN group_manager_id int(8) DEFAULT NULL;

--
-- Add tables structures around Profiles
--

--
-- Table structure for table `Profile`
--

CREATE TABLE Profile (
  profile_id int(8) NOT NULL auto_increment,
  profile_domain_id int(8) NOT NULL,
  profile_timeupdate timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  profile_timecreate timestamp NOT NULL default '0000-00-00 00:00:00',
  profile_userupdate int(8) default NULL,
  profile_usercreate int(8) default NULL,
  profile_name varchar(64) default NULL,
  PRIMARY KEY  (profile_id)
);
--
-- Table structure for table `ProfileModule`
--

CREATE TABLE ProfileModule (
  profilemodule_id int(8) NOT NULL auto_increment,
  profilemodule_domain_id int(8) NOT NULL,
  profilemodule_profile_id int(8) default NULL,
  profilemodule_module_name varchar(64) NOT NULL default '',
  profilemodule_right int(2) default NULL,
  PRIMARY KEY (profilemodule_id)
);
--
-- Table structure for table `ProfileSection`
--

CREATE TABLE ProfileSection (
  profilesection_id int(8) NOT NULL auto_increment,
  profilesection_domain_id int(8) NOT NULL,
  profilesection_profile_id int(8) default NULL,
  profilesection_section_name varchar(64) NOT NULL default '',
  profilesection_show tinyint(1) default NULL,
  PRIMARY KEY (profilesection_id)
);
--
-- Table structure for table `ProfileProperty`
--

CREATE TABLE ProfileProperty (
  profileproperty_id int(8) NOT NULL auto_increment,
  profileproperty_type varchar(32) default NULL,
  profileproperty_default text default NULL,
  profileproperty_readonly int(1) default '0',
  profileproperty_name varchar(32) NOT NULL default '',
  PRIMARY KEY (profileproperty_id)
);
--
-- Table structure for table `ProfilePropertyValue`
--

CREATE TABLE ProfilePropertyValue (
  profilepropertyvalue_id int(8) NOT NULL auto_increment,
  profilepropertyvalue_profile_id int(8) default NULL,
  profilepropertyvalue_property_id int(8) default NULL,
  profilepropertyvalue_property_value text NOT NULL default '',
  PRIMARY KEY (profilepropertyvalue_id)
);
-- -----------------------------------------------------------------------------
-- Default Profile properties
-- -----------------------------------------------------------------------------
INSERT INTO ProfileProperty (profileproperty_name, profileproperty_type, profileproperty_default, profileproperty_readonly) VALUES ('update_state', 'integer', 1, 1);
INSERT INTO ProfileProperty (profileproperty_name, profileproperty_type, profileproperty_default) VALUES ('level', 'integer', 3);
INSERT INTO ProfileProperty (profileproperty_name, profileproperty_type, profileproperty_default) VALUES ('level_managepeers', 'integer', 0);
INSERT INTO ProfileProperty (profileproperty_name, profileproperty_type, profileproperty_default) VALUES ('access_restriction', 'text', 'ALLOW_ALL');
INSERT INTO ProfileProperty (profileproperty_name, profileproperty_type, profileproperty_default) VALUES ('admin_realm', 'text', '');
INSERT INTO ProfileProperty (profileproperty_name, profileproperty_type, profileproperty_default, profileproperty_readonly) VALUES ('last_public_contact_export', 'timestamp', 0, 1);


--
-- Prepare value for foreign keys
--

-- Domain
UPDATE UserObm SET userobm_domain_id = NULL WHERE userobm_domain_id = 0;
UPDATE Host SET host_domain_id = NULL WHERE host_domain_id = 0;
-- UserObm
UPDATE UserObmPref SET userobmpref_user_id = NULL WHERE userobmpref_user_id = 0;
UPDATE DisplayPref SET display_user_id = NULL WHERE display_user_id = 0;
-- OGroup
UPDATE OGroup SET ogroup_parent_id = NULL WHERE ogroup_parent_id = 0;

-- Foreign key from account_domain_id to domain_id
DELETE FROM Account WHERE account_domain_id NOT IN (SELECT domain_id FROM Domain) AND account_domain_id IS NOT NULL;

-- Foreign key from account_usercreate to userobm_id
UPDATE Account SET account_usercreate = NULL WHERE account_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND account_usercreate IS NOT NULL;

-- Foreign key from account_userupdate to userobm_id
UPDATE Account SET account_userupdate = NULL WHERE account_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND account_userupdate IS NOT NULL;

-- Foreign key from activeuserobm_userobm_id to userobm_id
DELETE FROM ActiveUserObm WHERE activeuserobm_userobm_id NOT IN (SELECT userobm_id FROM UserObm) AND activeuserobm_userobm_id IS NOT NULL;

-- Foreign key from cv_domain_id to domain_id
DELETE FROM CV WHERE cv_domain_id NOT IN (SELECT domain_id FROM Domain) AND cv_domain_id IS NOT NULL;

-- Foreign key from cv_userobm_id to userobm_id
DELETE FROM CV WHERE cv_userobm_id NOT IN (SELECT userobm_id FROM UserObm) AND cv_userobm_id IS NOT NULL;

-- Foreign key from cv_userupdate to userobm_id
UPDATE CV SET cv_userupdate = NULL WHERE cv_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND cv_userupdate IS NOT NULL;

-- Foreign key from cv_usercreate to userobm_id
UPDATE CV SET cv_usercreate = NULL WHERE cv_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND cv_usercreate IS NOT NULL;

-- Foreign key from calendarcategory1_domain_id to domain_id
DELETE FROM CalendarCategory1 WHERE calendarcategory1_domain_id NOT IN (SELECT domain_id FROM Domain) AND calendarcategory1_domain_id IS NOT NULL;

-- Foreign key from calendarcategory1_userupdate to userobm_id
UPDATE CalendarCategory1 SET calendarcategory1_userupdate = NULL WHERE calendarcategory1_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND calendarcategory1_userupdate IS NOT NULL;

-- Foreign key from calendarcategory1_usercreate to userobm_id
UPDATE CalendarCategory1 SET calendarcategory1_usercreate = NULL WHERE calendarcategory1_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND calendarcategory1_usercreate IS NOT NULL;


-- Foreign key from category_domain_id to domain_id
DELETE FROM Category WHERE category_domain_id NOT IN (SELECT domain_id FROM Domain) AND category_domain_id IS NOT NULL;

-- Foreign key from category_userupdate to userobm_id
UPDATE Category SET category_userupdate = NULL WHERE category_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND category_userupdate IS NOT NULL;

-- Foreign key from category_usercreate to userobm_id
UPDATE Category SET category_usercreate = NULL WHERE category_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND category_usercreate IS NOT NULL;

-- Foreign key from categorylink_category_id to category_id
DELETE FROM CategoryLink WHERE categorylink_category_id NOT IN (SELECT category_id FROM Category) AND categorylink_category_id IS NOT NULL;

-- Foreign key from company_domain_id to domain_id
DELETE FROM Company WHERE company_domain_id NOT IN (SELECT domain_id FROM Domain) AND company_domain_id IS NOT NULL;

-- Foreign key from company_userupdate to userobm_id
UPDATE Company SET company_userupdate = NULL WHERE company_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND company_userupdate IS NOT NULL;

-- Foreign key from company_usercreate to userobm_id
UPDATE Company SET company_usercreate = NULL WHERE company_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND company_usercreate IS NOT NULL;

-- Foreign key from company_datasource_id to datasource_id
UPDATE Company SET company_datasource_id = NULL WHERE company_datasource_id NOT IN (SELECT datasource_id FROM DataSource) AND company_datasource_id IS NOT NULL;

-- Foreign key from company_type_id to companytype_id
UPDATE Company SET company_type_id = NULL WHERE company_type_id NOT IN (SELECT companytype_id FROM CompanyType) AND company_type_id IS NOT NULL;

-- Foreign key from company_activity_id to companyactivity_id
UPDATE Company SET company_activity_id = NULL WHERE company_activity_id NOT IN (SELECT companyactivity_id FROM CompanyActivity) AND company_activity_id IS NOT NULL;

-- Foreign key from company_nafcode_id to companynafcode_id
UPDATE Company SET company_nafcode_id = NULL WHERE company_nafcode_id NOT IN (SELECT companynafcode_id FROM CompanyNafCode) AND company_nafcode_id IS NOT NULL;

-- Foreign key from company_marketingmanager_id to userobm_id
UPDATE Company SET company_marketingmanager_id = NULL WHERE company_marketingmanager_id NOT IN (SELECT userobm_id FROM UserObm) AND company_marketingmanager_id IS NOT NULL;

-- Foreign key from companyactivity_domain_id to domain_id
DELETE FROM CompanyActivity WHERE companyactivity_domain_id NOT IN (SELECT domain_id FROM Domain) AND companyactivity_domain_id IS NOT NULL;

-- Foreign key from companyactivity_userupdate to userobm_id
UPDATE CompanyActivity SET companyactivity_userupdate = NULL WHERE companyactivity_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND companyactivity_userupdate IS NOT NULL;

-- Foreign key from companyactivity_usercreate to userobm_id
UPDATE CompanyActivity SET companyactivity_usercreate = NULL WHERE companyactivity_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND companyactivity_usercreate IS NOT NULL;

-- Foreign key from companynafcode_domain_id to domain_id
DELETE FROM CompanyNafCode WHERE companynafcode_domain_id NOT IN (SELECT domain_id FROM Domain) AND companynafcode_domain_id IS NOT NULL;

-- Foreign key from companynafcode_userupdate to userobm_id
UPDATE CompanyNafCode SET companynafcode_userupdate = NULL WHERE companynafcode_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND companynafcode_userupdate IS NOT NULL;

-- Foreign key from companynafcode_usercreate to userobm_id
UPDATE CompanyNafCode SET companynafcode_usercreate = NULL WHERE companynafcode_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND companynafcode_usercreate IS NOT NULL;

-- Foreign key from companytype_domain_id to domain_id
DELETE FROM CompanyType WHERE companytype_domain_id NOT IN (SELECT domain_id FROM Domain) AND companytype_domain_id IS NOT NULL;

-- Foreign key from companytype_userupdate to userobm_id
UPDATE CompanyType SET companytype_userupdate = NULL WHERE companytype_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND companytype_userupdate IS NOT NULL;

-- Foreign key from companytype_usercreate to userobm_id
UPDATE CompanyType SET companytype_usercreate = NULL WHERE companytype_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND companytype_usercreate IS NOT NULL;

-- Foreign key from contact_domain_id to domain_id
DELETE FROM Contact WHERE contact_domain_id NOT IN (SELECT domain_id FROM Domain) AND contact_domain_id IS NOT NULL;

-- Foreign key from contact_company_id to company_id
DELETE FROM Contact WHERE contact_company_id NOT IN (SELECT company_id FROM Company) AND contact_company_id IS NOT NULL;

-- Foreign key from contact_userupdate to userobm_id
UPDATE Contact SET contact_userupdate = NULL WHERE contact_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND contact_userupdate IS NOT NULL;

-- Foreign key from contact_usercreate to userobm_id
UPDATE Contact SET contact_usercreate = NULL WHERE contact_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND contact_usercreate IS NOT NULL;

-- Foreign key from contact_datasource_id to datasource_id
UPDATE Contact SET contact_datasource_id = NULL WHERE contact_datasource_id NOT IN (SELECT datasource_id FROM DataSource) AND contact_datasource_id IS NOT NULL;

-- Foreign key from contact_kind_id to kind_id
UPDATE Contact SET contact_kind_id = NULL WHERE contact_kind_id NOT IN (SELECT kind_id FROM Kind) AND contact_kind_id IS NOT NULL;

-- Foreign key from contact_marketingmanager_id to userobm_id
UPDATE Contact SET contact_marketingmanager_id = NULL WHERE contact_marketingmanager_id NOT IN (SELECT userobm_id FROM UserObm) AND contact_marketingmanager_id IS NOT NULL;

-- Foreign key from contact_function_id to contactfunction_id
UPDATE Contact SET contact_function_id = NULL WHERE contact_function_id NOT IN (SELECT contactfunction_id FROM ContactFunction) AND contact_function_id IS NOT NULL;

-- Foreign key from contactfunction_domain_id to domain_id
DELETE FROM ContactFunction WHERE contactfunction_domain_id NOT IN (SELECT domain_id FROM Domain) AND contactfunction_domain_id IS NOT NULL;

-- Foreign key from contactfunction_userupdate to userobm_id
UPDATE ContactFunction SET contactfunction_userupdate = NULL WHERE contactfunction_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND contactfunction_userupdate IS NOT NULL;

-- Foreign key from contactfunction_usercreate to userobm_id
UPDATE ContactFunction SET contactfunction_usercreate = NULL WHERE contactfunction_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND contactfunction_usercreate IS NOT NULL;

-- Foreign key from contactlist_list_id to list_id
DELETE FROM ContactList WHERE contactlist_list_id NOT IN (SELECT list_id FROM List) AND contactlist_list_id IS NOT NULL;

-- Foreign key from contactlist_contact_id to contact_id
DELETE FROM ContactList WHERE contactlist_contact_id NOT IN (SELECT contact_id FROM Contact) AND contactlist_contact_id IS NOT NULL;

-- Foreign key from contract_domain_id to domain_id
DELETE FROM Contract WHERE contract_domain_id NOT IN (SELECT domain_id FROM Domain) AND contract_domain_id IS NOT NULL;

-- Foreign key from contract_deal_id to deal_id
DELETE FROM Contract WHERE contract_deal_id NOT IN (SELECT deal_id FROM Deal) AND contract_deal_id IS NOT NULL;

-- Foreign key from contract_company_id to company_id
DELETE FROM Contract WHERE contract_company_id NOT IN (SELECT company_id FROM Company) AND contract_company_id IS NOT NULL;

-- Foreign key from contract_userupdate to userobm_id
UPDATE Contract SET contract_userupdate = NULL WHERE contract_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND contract_userupdate IS NOT NULL;

-- Foreign key from contract_usercreate to userobm_id
UPDATE Contract SET contract_usercreate = NULL WHERE contract_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND contract_usercreate IS NOT NULL;

-- Foreign key from contract_type_id to contracttype_id
UPDATE Contract SET contract_type_id = NULL WHERE contract_type_id NOT IN (SELECT contracttype_id FROM ContractType) AND contract_type_id IS NOT NULL;

-- Foreign key from contract_priority_id to contractpriority_id
UPDATE Contract SET contract_priority_id = NULL WHERE contract_priority_id NOT IN (SELECT contractpriority_id FROM ContractPriority) AND contract_priority_id IS NOT NULL;

-- Foreign key from contract_status_id to contractstatus_id
UPDATE Contract SET contract_status_id = NULL WHERE contract_status_id NOT IN (SELECT contractstatus_id FROM ContractStatus) AND contract_status_id IS NOT NULL;

-- Foreign key from contract_contact1_id to contact_id
UPDATE Contract SET contract_contact1_id = NULL WHERE contract_contact1_id NOT IN (SELECT contact_id FROM Contact) AND contract_contact1_id IS NOT NULL;

-- Foreign key from contract_contact2_id to contact_id
UPDATE Contract SET contract_contact2_id = NULL WHERE contract_contact2_id NOT IN (SELECT contact_id FROM Contact) AND contract_contact2_id IS NOT NULL;

-- Foreign key from contract_techmanager_id to userobm_id
UPDATE Contract SET contract_techmanager_id = NULL WHERE contract_techmanager_id NOT IN (SELECT userobm_id FROM UserObm) AND contract_techmanager_id IS NOT NULL;

-- Foreign key from contract_marketmanager_id to userobm_id
UPDATE Contract SET contract_marketmanager_id = NULL WHERE contract_marketmanager_id NOT IN (SELECT userobm_id FROM UserObm) AND contract_marketmanager_id IS NOT NULL;

-- Foreign key from contractpriority_domain_id to domain_id
DELETE FROM ContractPriority WHERE contractpriority_domain_id NOT IN (SELECT domain_id FROM Domain) AND contractpriority_domain_id IS NOT NULL;

-- Foreign key from contractpriority_userupdate to userobm_id
UPDATE ContractPriority SET contractpriority_userupdate = NULL WHERE contractpriority_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND contractpriority_userupdate IS NOT NULL;

-- Foreign key from contractpriority_usercreate to userobm_id
UPDATE ContractPriority SET contractpriority_usercreate = NULL WHERE contractpriority_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND contractpriority_usercreate IS NOT NULL;

-- Foreign key from contractstatus_domain_id to domain_id
DELETE FROM ContractStatus WHERE contractstatus_domain_id NOT IN (SELECT domain_id FROM Domain) AND contractstatus_domain_id IS NOT NULL;

-- Foreign key from contractstatus_userupdate to userobm_id
UPDATE ContractStatus SET contractstatus_userupdate = NULL WHERE contractstatus_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND contractstatus_userupdate IS NOT NULL;

-- Foreign key from contractstatus_usercreate to userobm_id
UPDATE ContractStatus SET contractstatus_usercreate = NULL WHERE contractstatus_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND contractstatus_usercreate IS NOT NULL;

-- Foreign key from contracttype_domain_id to domain_id
DELETE FROM ContractType WHERE contracttype_domain_id NOT IN (SELECT domain_id FROM Domain) AND contracttype_domain_id IS NOT NULL;

-- Foreign key from contracttype_userupdate to userobm_id
UPDATE ContractType SET contracttype_userupdate = NULL WHERE contracttype_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND contracttype_userupdate IS NOT NULL;

-- Foreign key from contracttype_usercreate to userobm_id
UPDATE ContractType SET contracttype_usercreate = NULL WHERE contracttype_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND contracttype_usercreate IS NOT NULL;

-- Foreign key from country_domain_id to domain_id
DELETE FROM Country WHERE country_domain_id NOT IN (SELECT domain_id FROM Domain) AND country_domain_id IS NOT NULL;

-- Foreign key from country_userupdate to userobm_id
UPDATE Country SET country_userupdate = NULL WHERE country_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND country_userupdate IS NOT NULL;

-- Foreign key from country_usercreate to userobm_id
UPDATE Country SET country_usercreate = NULL WHERE country_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND country_usercreate IS NOT NULL;

-- Foreign key from datasource_domain_id to domain_id
DELETE FROM DataSource WHERE datasource_domain_id NOT IN (SELECT domain_id FROM Domain) AND datasource_domain_id IS NOT NULL;

-- Foreign key from datasource_userupdate to userobm_id
UPDATE DataSource SET datasource_userupdate = NULL WHERE datasource_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND datasource_userupdate IS NOT NULL;

-- Foreign key from datasource_usercreate to userobm_id
UPDATE DataSource SET datasource_usercreate = NULL WHERE datasource_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND datasource_usercreate IS NOT NULL;

-- Foreign key from deal_domain_id to domain_id
DELETE FROM Deal WHERE deal_domain_id NOT IN (SELECT domain_id FROM Domain) AND deal_domain_id IS NOT NULL;

-- Foreign key from deal_parentdeal_id to parentdeal_id
DELETE FROM Deal WHERE deal_parentdeal_id NOT IN (SELECT parentdeal_id FROM ParentDeal) AND deal_parentdeal_id IS NOT NULL;

-- Foreign key from deal_company_id to company_id
DELETE FROM Deal WHERE deal_company_id NOT IN (SELECT company_id FROM Company) AND deal_company_id IS NOT NULL;

-- Foreign key from deal_userupdate to userobm_id
UPDATE Deal SET deal_userupdate = NULL WHERE deal_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND deal_userupdate IS NOT NULL;

-- Foreign key from deal_usercreate to userobm_id
UPDATE Deal SET deal_usercreate = NULL WHERE deal_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND deal_usercreate IS NOT NULL;

-- Foreign key from deal_type_id to dealtype_id
UPDATE Deal SET deal_type_id = NULL WHERE deal_type_id NOT IN (SELECT dealtype_id FROM DealType) AND deal_type_id IS NOT NULL;

-- Foreign key from deal_region_id to region_id
UPDATE Deal SET deal_region_id = NULL WHERE deal_region_id NOT IN (SELECT region_id FROM Region) AND deal_region_id IS NOT NULL;

-- Foreign key from deal_tasktype_id to tasktype_id
UPDATE Deal SET deal_tasktype_id = NULL WHERE deal_tasktype_id NOT IN (SELECT tasktype_id FROM TaskType) AND deal_tasktype_id IS NOT NULL;

-- Foreign key from deal_contact1_id to contact_id
UPDATE Deal SET deal_contact1_id = NULL WHERE deal_contact1_id NOT IN (SELECT contact_id FROM Contact) AND deal_contact1_id IS NOT NULL;

-- Foreign key from deal_contact2_id to contact_id
UPDATE Deal SET deal_contact2_id = NULL WHERE deal_contact2_id NOT IN (SELECT contact_id FROM Contact) AND deal_contact2_id IS NOT NULL;

-- Foreign key from deal_marketingmanager_id to userobm_id
UPDATE Deal SET deal_marketingmanager_id = NULL WHERE deal_marketingmanager_id NOT IN (SELECT userobm_id FROM UserObm) AND deal_marketingmanager_id IS NOT NULL;

-- Foreign key from deal_technicalmanager_id to userobm_id
UPDATE Deal SET deal_technicalmanager_id = NULL WHERE deal_technicalmanager_id NOT IN (SELECT userobm_id FROM UserObm) AND deal_technicalmanager_id IS NOT NULL;

-- Foreign key from deal_source_id to leadsource_id
UPDATE Deal SET deal_source_id = NULL WHERE deal_source_id NOT IN (SELECT leadsource_id FROM LeadSource) AND deal_source_id IS NOT NULL;

-- Foreign key from dealcompany_deal_id to deal_id
DELETE FROM DealCompany WHERE dealcompany_deal_id NOT IN (SELECT deal_id FROM Deal) AND dealcompany_deal_id IS NOT NULL;

-- Foreign key from dealcompany_company_id to company_id
DELETE FROM DealCompany WHERE dealcompany_company_id NOT IN (SELECT company_id FROM Company) AND dealcompany_company_id IS NOT NULL;

-- Foreign key from dealcompany_role_id to dealcompanyrole_id
UPDATE DealCompany SET dealcompany_role_id = NULL WHERE dealcompany_role_id NOT IN (SELECT dealcompanyrole_id FROM DealCompanyRole) AND dealcompany_role_id IS NOT NULL;

-- Foreign key from dealcompany_userupdate to userobm_id
UPDATE DealCompany SET dealcompany_userupdate = NULL WHERE dealcompany_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND dealcompany_userupdate IS NOT NULL;

-- Foreign key from dealcompany_usercreate to userobm_id
UPDATE DealCompany SET dealcompany_usercreate = NULL WHERE dealcompany_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND dealcompany_usercreate IS NOT NULL;

-- Foreign key from dealcompanyrole_domain_id to domain_id
DELETE FROM DealCompanyRole WHERE dealcompanyrole_domain_id NOT IN (SELECT domain_id FROM Domain) AND dealcompanyrole_domain_id IS NOT NULL;

-- Foreign key from dealcompanyrole_userupdate to userobm_id
UPDATE DealCompanyRole SET dealcompanyrole_userupdate = NULL WHERE dealcompanyrole_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND dealcompanyrole_userupdate IS NOT NULL;

-- Foreign key from dealcompanyrole_usercreate to userobm_id
UPDATE DealCompanyRole SET dealcompanyrole_usercreate = NULL WHERE dealcompanyrole_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND dealcompanyrole_usercreate IS NOT NULL;

-- Foreign key from dealstatus_domain_id to domain_id
DELETE FROM DealStatus WHERE dealstatus_domain_id NOT IN (SELECT domain_id FROM Domain) AND dealstatus_domain_id IS NOT NULL;

-- Foreign key from dealstatus_userupdate to userobm_id
UPDATE DealStatus SET dealstatus_userupdate = NULL WHERE dealstatus_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND dealstatus_userupdate IS NOT NULL;

-- Foreign key from dealstatus_usercreate to userobm_id
UPDATE DealStatus SET dealstatus_usercreate = NULL WHERE dealstatus_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND dealstatus_usercreate IS NOT NULL;

-- Foreign key from dealtype_domain_id to domain_id
DELETE FROM DealType WHERE dealtype_domain_id NOT IN (SELECT domain_id FROM Domain) AND dealtype_domain_id IS NOT NULL;

-- Foreign key from dealtype_userupdate to userobm_id
UPDATE DealType SET dealtype_userupdate = NULL WHERE dealtype_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND dealtype_userupdate IS NOT NULL;

-- Foreign key from dealtype_usercreate to userobm_id
UPDATE DealType SET dealtype_usercreate = NULL WHERE dealtype_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND dealtype_usercreate IS NOT NULL;

-- Foreign key from defaultodttemplate_domain_id to domain_id
DELETE FROM DefaultOdtTemplate WHERE defaultodttemplate_domain_id NOT IN (SELECT domain_id FROM Domain) AND defaultodttemplate_domain_id IS NOT NULL;

-- Foreign key from defaultodttemplate_document_id to document_id
DELETE FROM DefaultOdtTemplate WHERE defaultodttemplate_document_id NOT IN (SELECT document_id FROM Document) AND defaultodttemplate_document_id IS NOT NULL;

-- Foreign key from deleted_user_id to userobm_id
DELETE FROM Deleted WHERE deleted_user_id NOT IN (SELECT userobm_id FROM UserObm) AND deleted_user_id IS NOT NULL;

-- Foreign key from deleted_domain_id to domain_id
DELETE FROM Deleted WHERE deleted_domain_id NOT IN (SELECT domain_id FROM Domain) AND deleted_domain_id IS NOT NULL;

-- Foreign key from display_user_id to userobm_id
DELETE FROM DisplayPref WHERE display_user_id NOT IN (SELECT userobm_id FROM UserObm) AND display_user_id IS NOT NULL;

-- Foreign key from document_domain_id to domain_id
DELETE FROM Document WHERE document_domain_id NOT IN (SELECT domain_id FROM Domain) AND document_domain_id IS NOT NULL;

-- Foreign key from document_userupdate to userobm_id
UPDATE Document SET document_userupdate = NULL WHERE document_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND document_userupdate IS NOT NULL;

-- Foreign key from document_usercreate to userobm_id
UPDATE Document SET document_usercreate = NULL WHERE document_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND document_usercreate IS NOT NULL;

-- Foreign key from document_mimetype_id to documentmimetype_id
UPDATE Document SET document_mimetype_id = NULL WHERE document_mimetype_id NOT IN (SELECT documentmimetype_id FROM DocumentMimeType) AND document_mimetype_id IS NOT NULL;

-- Foreign key from documententity_document_id to document_id
DELETE FROM DocumentEntity WHERE documententity_document_id NOT IN (SELECT document_id FROM Document) AND documententity_document_id IS NOT NULL;

-- Foreign key from documentmimetype_domain_id to domain_id
DELETE FROM DocumentMimeType WHERE documentmimetype_domain_id NOT IN (SELECT domain_id FROM Domain) AND documentmimetype_domain_id IS NOT NULL;

-- Foreign key from documentmimetype_userupdate to userobm_id
UPDATE DocumentMimeType SET documentmimetype_userupdate = NULL WHERE documentmimetype_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND documentmimetype_userupdate IS NOT NULL;

-- Foreign key from documentmimetype_usercreate to userobm_id
UPDATE DocumentMimeType SET documentmimetype_usercreate = NULL WHERE documentmimetype_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND documentmimetype_usercreate IS NOT NULL;

-- Foreign key from domain_userupdate to userobm_id
UPDATE Domain SET domain_userupdate = NULL WHERE domain_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND domain_userupdate IS NOT NULL;

-- Foreign key from domain_usercreate to userobm_id
UPDATE Domain SET domain_usercreate = NULL WHERE domain_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND domain_usercreate IS NOT NULL;

-- Foreign key from domainmailserver_domain_id to domain_id
DELETE FROM DomainMailServer WHERE domainmailserver_domain_id NOT IN (SELECT domain_id FROM Domain) AND domainmailserver_domain_id IS NOT NULL;

-- Foreign key from domainmailserver_mailserver_id to mailserver_id
DELETE FROM DomainMailServer WHERE domainmailserver_mailserver_id NOT IN (SELECT mailserver_id FROM MailServer) AND domainmailserver_mailserver_id IS NOT NULL;

-- Foreign key from domainpropertyvalue_domain_id to domain_id
DELETE FROM DomainPropertyValue WHERE domainpropertyvalue_domain_id NOT IN (SELECT domain_id FROM Domain) AND domainpropertyvalue_domain_id IS NOT NULL;

-- Foreign key from groupgroup_parent_id to group_id
DELETE FROM GroupGroup WHERE groupgroup_parent_id NOT IN (SELECT group_id FROM UGroup) AND groupgroup_parent_id IS NOT NULL;

-- Foreign key from groupgroup_child_id to group_id
DELETE FROM GroupGroup WHERE groupgroup_child_id NOT IN (SELECT group_id FROM UGroup) AND groupgroup_child_id IS NOT NULL;

-- Foreign key from host_domain_id to domain_id
DELETE FROM Host WHERE host_domain_id NOT IN (SELECT domain_id FROM Domain) AND host_domain_id IS NOT NULL;

-- Foreign key from host_userupdate to userobm_id
UPDATE Host SET host_userupdate = NULL WHERE host_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND host_userupdate IS NOT NULL;

-- Foreign key from host_usercreate to userobm_id
UPDATE Host SET host_usercreate = NULL WHERE host_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND host_usercreate IS NOT NULL;

-- Foreign key from import_domain_id to domain_id
DELETE FROM Import WHERE import_domain_id NOT IN (SELECT domain_id FROM Domain) AND import_domain_id IS NOT NULL;

-- Foreign key from import_userupdate to userobm_id
UPDATE Import SET import_userupdate = NULL WHERE import_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND import_userupdate IS NOT NULL;

-- Foreign key from import_usercreate to userobm_id
UPDATE Import SET import_usercreate = NULL WHERE import_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND import_usercreate IS NOT NULL;

-- Foreign key from import_datasource_id to datasource_id
UPDATE Import SET import_datasource_id = NULL WHERE import_datasource_id NOT IN (SELECT datasource_id FROM DataSource) AND import_datasource_id IS NOT NULL;

-- Foreign key from import_marketingmanager_id to userobm_id
UPDATE Import SET import_marketingmanager_id = NULL WHERE import_marketingmanager_id NOT IN (SELECT userobm_id FROM UserObm) AND import_marketingmanager_id IS NOT NULL;

-- Foreign key from incident_domain_id to domain_id
DELETE FROM Incident WHERE incident_domain_id NOT IN (SELECT domain_id FROM Domain) AND incident_domain_id IS NOT NULL;

-- Foreign key from incident_contract_id to contract_id
DELETE FROM Incident WHERE incident_contract_id NOT IN (SELECT contract_id FROM Contract) AND incident_contract_id IS NOT NULL;

-- Foreign key from incident_userupdate to userobm_id
UPDATE Incident SET incident_userupdate = NULL WHERE incident_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND incident_userupdate IS NOT NULL;

-- Foreign key from incident_usercreate to userobm_id
UPDATE Incident SET incident_usercreate = NULL WHERE incident_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND incident_usercreate IS NOT NULL;

-- Foreign key from incident_priority_id to incidentpriority_id
UPDATE Incident SET incident_priority_id = NULL WHERE incident_priority_id NOT IN (SELECT incidentpriority_id FROM IncidentPriority) AND incident_priority_id IS NOT NULL;

-- Foreign key from incident_status_id to incidentstatus_id
UPDATE Incident SET incident_status_id = NULL WHERE incident_status_id NOT IN (SELECT incidentstatus_id FROM IncidentStatus) AND incident_status_id IS NOT NULL;

-- Foreign key from incident_resolutiontype_id to incidentresolutiontype_id
UPDATE Incident SET incident_resolutiontype_id = NULL WHERE incident_resolutiontype_id NOT IN (SELECT incidentresolutiontype_id FROM IncidentResolutionType) AND incident_resolutiontype_id IS NOT NULL;

-- Foreign key from incident_logger to userobm_id
UPDATE Incident SET incident_logger = NULL WHERE incident_logger NOT IN (SELECT userobm_id FROM UserObm) AND incident_logger IS NOT NULL;

-- Foreign key from incident_owner to userobm_id
UPDATE Incident SET incident_owner = NULL WHERE incident_owner NOT IN (SELECT userobm_id FROM UserObm) AND incident_owner IS NOT NULL;

-- Foreign key from incidentpriority_domain_id to domain_id
DELETE FROM IncidentPriority WHERE incidentpriority_domain_id NOT IN (SELECT domain_id FROM Domain) AND incidentpriority_domain_id IS NOT NULL;

-- Foreign key from incidentpriority_userupdate to userobm_id
UPDATE IncidentPriority SET incidentpriority_userupdate = NULL WHERE incidentpriority_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND incidentpriority_userupdate IS NOT NULL;

-- Foreign key from incidentpriority_usercreate to userobm_id
UPDATE IncidentPriority SET incidentpriority_usercreate = NULL WHERE incidentpriority_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND incidentpriority_usercreate IS NOT NULL;

-- Foreign key from incidentresolutiontype_domain_id to domain_id
DELETE FROM IncidentResolutionType WHERE incidentresolutiontype_domain_id NOT IN (SELECT domain_id FROM Domain) AND incidentresolutiontype_domain_id IS NOT NULL;

-- Foreign key from incidentresolutiontype_userupdate to userobm_id
UPDATE IncidentResolutionType SET incidentresolutiontype_userupdate = NULL WHERE incidentresolutiontype_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND incidentresolutiontype_userupdate IS NOT NULL;

-- Foreign key from incidentresolutiontype_usercreate to userobm_id
UPDATE IncidentResolutionType SET incidentresolutiontype_usercreate = NULL WHERE incidentresolutiontype_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND incidentresolutiontype_usercreate IS NOT NULL;

-- Foreign key from incidentstatus_domain_id to domain_id
DELETE FROM IncidentStatus WHERE incidentstatus_domain_id NOT IN (SELECT domain_id FROM Domain) AND incidentstatus_domain_id IS NOT NULL;

-- Foreign key from incidentstatus_userupdate to userobm_id
UPDATE IncidentStatus SET incidentstatus_userupdate = NULL WHERE incidentstatus_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND incidentstatus_userupdate IS NOT NULL;

-- Foreign key from incidentstatus_usercreate to userobm_id
UPDATE IncidentStatus SET incidentstatus_usercreate = NULL WHERE incidentstatus_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND incidentstatus_usercreate IS NOT NULL;

-- Foreign key from invoice_domain_id to domain_id
DELETE FROM Invoice WHERE invoice_domain_id NOT IN (SELECT domain_id FROM Domain) AND invoice_domain_id IS NOT NULL;

-- Foreign key from invoice_company_id to company_id
DELETE FROM Invoice WHERE invoice_company_id NOT IN (SELECT company_id FROM Company) AND invoice_company_id IS NOT NULL;

-- Foreign key from invoice_project_id to project_id
DELETE FROM Invoice WHERE invoice_project_id NOT IN (SELECT project_id FROM Project) AND invoice_project_id IS NOT NULL;

-- Foreign key from invoice_deal_id to deal_id
DELETE FROM Invoice WHERE invoice_deal_id NOT IN (SELECT deal_id FROM Deal) AND invoice_deal_id IS NOT NULL;

-- Foreign key from invoice_userupdate to userobm_id
UPDATE Invoice SET invoice_userupdate = NULL WHERE invoice_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND invoice_userupdate IS NOT NULL;

-- Foreign key from invoice_usercreate to userobm_id
UPDATE Invoice SET invoice_usercreate = NULL WHERE invoice_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND invoice_usercreate IS NOT NULL;

-- Foreign key from kind_domain_id to domain_id
DELETE FROM Kind WHERE kind_domain_id NOT IN (SELECT domain_id FROM Domain) AND kind_domain_id IS NOT NULL;

-- Foreign key from kind_userupdate to userobm_id
UPDATE Kind SET kind_userupdate = NULL WHERE kind_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND kind_userupdate IS NOT NULL;

-- Foreign key from kind_usercreate to userobm_id
UPDATE Kind SET kind_usercreate = NULL WHERE kind_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND kind_usercreate IS NOT NULL;

-- Foreign key from lead_domain_id to domain_id
DELETE FROM Lead WHERE lead_domain_id NOT IN (SELECT domain_id FROM Domain) AND lead_domain_id IS NOT NULL;

-- Foreign key from lead_company_id to company_id
DELETE FROM Lead WHERE lead_company_id NOT IN (SELECT company_id FROM Company) AND lead_company_id IS NOT NULL;

-- Foreign key from lead_userupdate to userobm_id
UPDATE Lead SET lead_userupdate = NULL WHERE lead_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND lead_userupdate IS NOT NULL;

-- Foreign key from lead_usercreate to userobm_id
UPDATE Lead SET lead_usercreate = NULL WHERE lead_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND lead_usercreate IS NOT NULL;

-- Foreign key from lead_source_id to leadsource_id
UPDATE Lead SET lead_source_id = NULL WHERE lead_source_id NOT IN (SELECT leadsource_id FROM LeadSource) AND lead_source_id IS NOT NULL;

-- Foreign key from lead_manager_id to userobm_id
UPDATE Lead SET lead_manager_id = NULL WHERE lead_manager_id NOT IN (SELECT userobm_id FROM UserObm) AND lead_manager_id IS NOT NULL;

-- Foreign key from lead_contact_id to contact_id
UPDATE Lead SET lead_contact_id = NULL WHERE lead_contact_id NOT IN (SELECT contact_id FROM Contact) AND lead_contact_id IS NOT NULL;

-- Foreign key from lead_status_id to leadstatus_id
UPDATE Lead SET lead_status_id = NULL WHERE lead_status_id NOT IN (SELECT leadstatus_id FROM LeadStatus) AND lead_status_id IS NOT NULL;

-- Foreign key from leadsource_domain_id to domain_id
DELETE FROM LeadSource WHERE leadsource_domain_id NOT IN (SELECT domain_id FROM Domain) AND leadsource_domain_id IS NOT NULL;

-- Foreign key from leadsource_userupdate to userobm_id
UPDATE LeadSource SET leadsource_userupdate = NULL WHERE leadsource_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND leadsource_userupdate IS NOT NULL;

-- Foreign key from leadsource_usercreate to userobm_id
UPDATE LeadSource SET leadsource_usercreate = NULL WHERE leadsource_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND leadsource_usercreate IS NOT NULL;

-- Foreign key from leadstatus_domain_id to domain_id
DELETE FROM LeadStatus WHERE leadstatus_domain_id NOT IN (SELECT domain_id FROM Domain) AND leadstatus_domain_id IS NOT NULL;

-- Foreign key from leadstatus_userupdate to userobm_id
UPDATE LeadStatus SET leadstatus_userupdate = NULL WHERE leadstatus_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND leadstatus_userupdate IS NOT NULL;

-- Foreign key from leadstatus_usercreate to userobm_id
UPDATE LeadStatus SET leadstatus_usercreate = NULL WHERE leadstatus_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND leadstatus_usercreate IS NOT NULL;

-- Foreign key from list_domain_id to domain_id
DELETE FROM List WHERE list_domain_id NOT IN (SELECT domain_id FROM Domain) AND list_domain_id IS NOT NULL;

-- Foreign key from list_userupdate to userobm_id
UPDATE List SET list_userupdate = NULL WHERE list_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND list_userupdate IS NOT NULL;

-- Foreign key from list_usercreate to userobm_id
UPDATE List SET list_usercreate = NULL WHERE list_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND list_usercreate IS NOT NULL;

-- Foreign key from mailserver_host_id to host_id
DELETE FROM MailServer WHERE mailserver_host_id NOT IN (SELECT host_id FROM Host) AND mailserver_host_id IS NOT NULL;

-- Foreign key from mailserver_userupdate to userobm_id
UPDATE MailServer SET mailserver_userupdate = NULL WHERE mailserver_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND mailserver_userupdate IS NOT NULL;

-- Foreign key from mailserver_usercreate to userobm_id
UPDATE MailServer SET mailserver_usercreate = NULL WHERE mailserver_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND mailserver_usercreate IS NOT NULL;

-- Foreign key from mailserver_relayhost_id to Host
UPDATE MailServer SET mailserver_relayhost_id = NULL WHERE mailserver_relayhost_id NOT IN (SELECT host_id FROM Host) AND mailserver_relayhost_id IS NOT NULL;

-- Foreign key from mailshare_domain_id to domain_id
DELETE FROM MailShare WHERE mailshare_domain_id NOT IN (SELECT domain_id FROM Domain) AND mailshare_domain_id IS NOT NULL;

-- Foreign key from mailshare_mail_server_id to mailserver_id
DELETE FROM MailShare WHERE mailshare_mail_server_id NOT IN (SELECT mailserver_id FROM MailServer) AND mailshare_mail_server_id IS NOT NULL;

-- Foreign key from mailshare_userupdate to userobm_id
UPDATE MailShare SET mailshare_userupdate = NULL WHERE mailshare_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND mailshare_userupdate IS NOT NULL;

-- Foreign key from mailshare_usercreate to userobm_id
UPDATE MailShare SET mailshare_usercreate = NULL WHERE mailshare_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND mailshare_usercreate IS NOT NULL;

-- Foreign key from ogroup_domain_id to domain_id
DELETE FROM OGroup WHERE ogroup_domain_id NOT IN (SELECT domain_id FROM Domain) AND ogroup_domain_id IS NOT NULL;

-- Foreign key from ogroup_organizationalchart_id to organizationalchart_id
DELETE FROM OGroup WHERE ogroup_organizationalchart_id NOT IN (SELECT organizationalchart_id FROM OrganizationalChart) AND ogroup_organizationalchart_id IS NOT NULL;

-- Foreign key from ogroup_parent_id to ogroup_id
DELETE FROM OGroup WHERE ogroup_parent_id NOT IN (SELECT ogroup_id FROM (SELECT ogroup_id FROM OGroup) AS Parent) AND ogroup_parent_id IS NOT NULL;

-- Foreign key from ogroup_userupdate to userobm_id
UPDATE OGroup SET ogroup_userupdate = NULL WHERE ogroup_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND ogroup_userupdate IS NOT NULL;

-- Foreign key from ogroup_usercreate to userobm_id
UPDATE OGroup SET ogroup_usercreate = NULL WHERE ogroup_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND ogroup_usercreate IS NOT NULL;

-- Foreign key from ogroupentity_ogroup_id to ogroup_id
DELETE FROM OGroupEntity WHERE ogroupentity_ogroup_id NOT IN (SELECT ogroup_id FROM OGroup) AND ogroupentity_ogroup_id IS NOT NULL;

-- Foreign key from ogroupentity_domain_id to domain_id
DELETE FROM OGroupEntity WHERE ogroupentity_domain_id NOT IN (SELECT domain_id FROM Domain) AND ogroupentity_domain_id IS NOT NULL;

-- Foreign key from ogroupentity_userupdate to userobm_id
UPDATE OGroupEntity SET ogroupentity_userupdate = NULL WHERE ogroupentity_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND ogroupentity_userupdate IS NOT NULL;

-- Foreign key from ogroupentity_usercreate to userobm_id
UPDATE OGroupEntity SET ogroupentity_usercreate = NULL WHERE ogroupentity_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND ogroupentity_usercreate IS NOT NULL;

-- Foreign key from obmbookmark_user_id to userobm_id
DELETE FROM ObmBookmark WHERE obmbookmark_user_id NOT IN (SELECT userobm_id FROM UserObm) AND obmbookmark_user_id IS NOT NULL;

-- Foreign key from obmbookmarkproperty_bookmark_id to obmbookmark_id
DELETE FROM ObmBookmarkProperty WHERE obmbookmarkproperty_bookmark_id NOT IN (SELECT obmbookmark_id FROM ObmBookmark) AND obmbookmarkproperty_bookmark_id IS NOT NULL;

-- Foreign key from organizationalchart_domain_id to domain_id
DELETE FROM OrganizationalChart WHERE organizationalchart_domain_id NOT IN (SELECT domain_id FROM Domain) AND organizationalchart_domain_id IS NOT NULL;

-- Foreign key from organizationalchart_userupdate to userobm_id
UPDATE OrganizationalChart SET organizationalchart_userupdate = NULL WHERE organizationalchart_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND organizationalchart_userupdate IS NOT NULL;

-- Foreign key from organizationalchart_usercreate to userobm_id
UPDATE OrganizationalChart SET organizationalchart_usercreate = NULL WHERE organizationalchart_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND organizationalchart_usercreate IS NOT NULL;

-- Foreign key from parentdeal_domain_id to domain_id
DELETE FROM ParentDeal WHERE parentdeal_domain_id NOT IN (SELECT domain_id FROM Domain) AND parentdeal_domain_id IS NOT NULL;

-- Foreign key from parentdeal_userupdate to userobm_id
UPDATE ParentDeal SET parentdeal_userupdate = NULL WHERE parentdeal_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND parentdeal_userupdate IS NOT NULL;

-- Foreign key from parentdeal_usercreate to userobm_id
UPDATE ParentDeal SET parentdeal_usercreate = NULL WHERE parentdeal_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND parentdeal_usercreate IS NOT NULL;

-- Foreign key from parentdeal_marketingmanager_id to userobm_id
UPDATE ParentDeal SET parentdeal_marketingmanager_id = NULL WHERE parentdeal_marketingmanager_id NOT IN (SELECT userobm_id FROM UserObm) AND parentdeal_marketingmanager_id IS NOT NULL;

-- Foreign key from parentdeal_technicalmanager_id to userobm_id
UPDATE ParentDeal SET parentdeal_technicalmanager_id = NULL WHERE parentdeal_technicalmanager_id NOT IN (SELECT userobm_id FROM UserObm) AND parentdeal_technicalmanager_id IS NOT NULL;

-- Foreign key from payment_domain_id to domain_id
DELETE FROM Payment WHERE payment_domain_id NOT IN (SELECT domain_id FROM Domain) AND payment_domain_id IS NOT NULL;

-- Foreign key from payment_account_id to account_id
DELETE FROM Payment WHERE payment_account_id NOT IN (SELECT account_id FROM Account) AND payment_account_id IS NOT NULL;

-- Foreign key from payment_userupdate to userobm_id
UPDATE Payment SET payment_userupdate = NULL WHERE payment_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND payment_userupdate IS NOT NULL;

-- Foreign key from payment_usercreate to userobm_id
UPDATE Payment SET payment_usercreate = NULL WHERE payment_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND payment_usercreate IS NOT NULL;

-- Foreign key from payment_company_id to company_id
UPDATE Payment SET payment_company_id = NULL WHERE payment_company_id NOT IN (SELECT company_id FROM Company) AND payment_company_id IS NOT NULL;

-- Foreign key from payment_paymentkind_id to paymentkind_id
UPDATE Payment SET payment_paymentkind_id = NULL WHERE payment_paymentkind_id NOT IN (SELECT paymentkind_id FROM PaymentKind) AND payment_paymentkind_id IS NOT NULL;

-- Foreign key from paymentinvoice_invoice_id to invoice_id
DELETE FROM PaymentInvoice WHERE paymentinvoice_invoice_id NOT IN (SELECT invoice_id FROM Invoice) AND paymentinvoice_invoice_id IS NOT NULL;

-- Foreign key from paymentinvoice_payment_id to payment_id
DELETE FROM PaymentInvoice WHERE paymentinvoice_payment_id NOT IN (SELECT payment_id FROM Payment) AND paymentinvoice_payment_id IS NOT NULL;

-- Foreign key from paymentinvoice_usercreate to userobm_id
UPDATE PaymentInvoice SET paymentinvoice_usercreate = NULL WHERE paymentinvoice_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND paymentinvoice_usercreate IS NOT NULL;

-- Foreign key from paymentinvoice_userupdate to userobm_id
UPDATE PaymentInvoice SET paymentinvoice_userupdate = NULL WHERE paymentinvoice_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND paymentinvoice_userupdate IS NOT NULL;

-- Foreign key from paymentkind_domain_id to domain_id
DELETE FROM PaymentKind WHERE paymentkind_domain_id NOT IN (SELECT domain_id FROM Domain) AND paymentkind_domain_id IS NOT NULL;

-- Foreign key from project_domain_id to domain_id
DELETE FROM Project WHERE project_domain_id NOT IN (SELECT domain_id FROM Domain) AND project_domain_id IS NOT NULL;

-- Foreign key from project_deal_id to deal_id
DELETE FROM Project WHERE project_deal_id NOT IN (SELECT deal_id FROM Deal) AND project_deal_id IS NOT NULL;

-- Foreign key from project_company_id to company_id
DELETE FROM Project WHERE project_company_id NOT IN (SELECT company_id FROM Company) AND project_company_id IS NOT NULL;

-- Foreign key from project_userupdate to userobm_id
UPDATE Project SET project_userupdate = NULL WHERE project_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND project_userupdate IS NOT NULL;

-- Foreign key from project_usercreate to userobm_id
UPDATE Project SET project_usercreate = NULL WHERE project_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND project_usercreate IS NOT NULL;

-- Foreign key from project_tasktype_id to tasktype_id
UPDATE Project SET project_tasktype_id = NULL WHERE project_tasktype_id NOT IN (SELECT tasktype_id FROM TaskType) AND project_tasktype_id IS NOT NULL;

-- Foreign key from project_type_id to dealtype_id
UPDATE Project SET project_type_id = NULL WHERE project_type_id NOT IN (SELECT dealtype_id FROM DealType) AND project_type_id IS NOT NULL;

-- Foreign key from projectcv_project_id to project_id
DELETE FROM ProjectCV WHERE projectcv_project_id NOT IN (SELECT project_id FROM Project) AND projectcv_project_id IS NOT NULL;

-- Foreign key from projectcv_cv_id to cv_id
DELETE FROM ProjectCV WHERE projectcv_cv_id NOT IN (SELECT cv_id FROM CV) AND projectcv_cv_id IS NOT NULL;

-- Foreign key from projectclosing_project_id to project_id
DELETE FROM ProjectClosing WHERE projectclosing_project_id NOT IN (SELECT project_id FROM Project) AND projectclosing_project_id IS NOT NULL;

-- Foreign key from projectclosing_userupdate to userobm_id
UPDATE ProjectClosing SET projectclosing_userupdate = NULL WHERE projectclosing_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND projectclosing_userupdate IS NOT NULL;

-- Foreign key from projectclosing_usercreate to userobm_id
UPDATE ProjectClosing SET projectclosing_usercreate = NULL WHERE projectclosing_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND projectclosing_usercreate IS NOT NULL;

-- Foreign key from projectreftask_tasktype_id to tasktype_id
DELETE FROM ProjectRefTask WHERE projectreftask_tasktype_id NOT IN (SELECT tasktype_id FROM TaskType) AND projectreftask_tasktype_id IS NOT NULL;

-- Foreign key from projectreftask_userupdate to userobm_id
UPDATE ProjectRefTask SET projectreftask_userupdate = NULL WHERE projectreftask_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND projectreftask_userupdate IS NOT NULL;

-- Foreign key from projectreftask_usercreate to userobm_id
UPDATE ProjectRefTask SET projectreftask_usercreate = NULL WHERE projectreftask_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND projectreftask_usercreate IS NOT NULL;

-- Foreign key from projecttask_project_id to project_id
DELETE FROM ProjectTask WHERE projecttask_project_id NOT IN (SELECT project_id FROM Project) AND projecttask_project_id IS NOT NULL;

-- Foreign key from projecttask_parenttask_id to projecttask_id
UPDATE ProjectTask SET projecttask_parenttask_id = NULL WHERE projecttask_parenttask_id = 0;
DELETE FROM ProjectTask WHERE projecttask_parenttask_id NOT IN (SELECT projecttask_id FROM (SELECT projecttask_id FROM ProjectTask) AS Parent) AND projecttask_parenttask_id IS NOT NULL;

-- Foreign key from projecttask_userupdate to userobm_id
UPDATE ProjectTask SET projecttask_userupdate = NULL WHERE projecttask_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND projecttask_userupdate IS NOT NULL;

-- Foreign key from projecttask_usercreate to userobm_id
UPDATE ProjectTask SET projecttask_usercreate = NULL WHERE projecttask_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND projecttask_usercreate IS NOT NULL;

-- Foreign key from projectuser_project_id to project_id
DELETE FROM ProjectUser WHERE projectuser_project_id NOT IN (SELECT project_id FROM Project) AND projectuser_project_id IS NOT NULL;

-- Foreign key from projectuser_user_id to userobm_id
DELETE FROM ProjectUser WHERE projectuser_user_id NOT IN (SELECT userobm_id FROM UserObm) AND projectuser_user_id IS NOT NULL;

-- Foreign key from projectuser_userupdate to userobm_id
UPDATE ProjectUser SET projectuser_userupdate = NULL WHERE projectuser_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND projectuser_userupdate IS NOT NULL;

-- Foreign key from projectuser_usercreate to userobm_id
UPDATE ProjectUser SET projectuser_usercreate = NULL WHERE projectuser_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND projectuser_usercreate IS NOT NULL;

-- Foreign key from publication_domain_id to domain_id
DELETE FROM Publication WHERE publication_domain_id NOT IN (SELECT domain_id FROM Domain) AND publication_domain_id IS NOT NULL;

-- Foreign key from publication_userupdate to userobm_id
UPDATE Publication SET publication_userupdate = NULL WHERE publication_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND publication_userupdate IS NOT NULL;

-- Foreign key from publication_usercreate to userobm_id
UPDATE Publication SET publication_usercreate = NULL WHERE publication_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND publication_usercreate IS NOT NULL;

-- Foreign key from publication_type_id to publicationtype_id
UPDATE Publication SET publication_type_id = NULL WHERE publication_type_id NOT IN (SELECT publicationtype_id FROM PublicationType) AND publication_type_id IS NOT NULL;

-- Foreign key from publicationtype_domain_id to domain_id
DELETE FROM PublicationType WHERE publicationtype_domain_id NOT IN (SELECT domain_id FROM Domain) AND publicationtype_domain_id IS NOT NULL;

-- Foreign key from publicationtype_userupdate to userobm_id
UPDATE PublicationType SET publicationtype_userupdate = NULL WHERE publicationtype_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND publicationtype_userupdate IS NOT NULL;

-- Foreign key from publicationtype_usercreate to userobm_id
UPDATE PublicationType SET publicationtype_usercreate = NULL WHERE publicationtype_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND publicationtype_usercreate IS NOT NULL;

-- Foreign key from rgroup_domain_id to domain_id
DELETE FROM RGroup WHERE rgroup_domain_id NOT IN (SELECT domain_id FROM Domain) AND rgroup_domain_id IS NOT NULL;

-- Foreign key from rgroup_userupdate to userobm_id
UPDATE RGroup SET rgroup_userupdate = NULL WHERE rgroup_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND rgroup_userupdate IS NOT NULL;

-- Foreign key from rgroup_usercreate to userobm_id
UPDATE RGroup SET rgroup_usercreate = NULL WHERE rgroup_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND rgroup_usercreate IS NOT NULL;

-- Foreign key from region_domain_id to domain_id
DELETE FROM Region WHERE region_domain_id NOT IN (SELECT domain_id FROM Domain) AND region_domain_id IS NOT NULL;

-- Foreign key from region_userupdate to userobm_id
UPDATE Region SET region_userupdate = NULL WHERE region_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND region_userupdate IS NOT NULL;

-- Foreign key from region_usercreate to userobm_id
UPDATE Region SET region_usercreate = NULL WHERE region_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND region_usercreate IS NOT NULL;

-- Foreign key from resource_domain_id to domain_id
DELETE FROM Resource WHERE resource_domain_id NOT IN (SELECT domain_id FROM Domain) AND resource_domain_id IS NOT NULL;

-- Foreign key from resource_userupdate to userobm_id
UPDATE Resource SET resource_userupdate = NULL WHERE resource_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND resource_userupdate IS NOT NULL;

-- Foreign key from resource_usercreate to userobm_id
UPDATE Resource SET resource_usercreate = NULL WHERE resource_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND resource_usercreate IS NOT NULL;

-- Foreign key from resource_rtype_id to resourcetype_id
UPDATE Resource SET resource_rtype_id = NULL WHERE resource_rtype_id NOT IN (SELECT resourcetype_id FROM ResourceType) AND resource_rtype_id IS NOT NULL;

-- Foreign key from resourcegroup_rgroup_id to rgroup_id
DELETE FROM ResourceGroup WHERE resourcegroup_rgroup_id NOT IN (SELECT rgroup_id FROM RGroup) AND resourcegroup_rgroup_id IS NOT NULL;

-- Foreign key from resourcegroup_resource_id to resource_id
DELETE FROM ResourceGroup WHERE resourcegroup_resource_id NOT IN (SELECT resource_id FROM Resource) AND resourcegroup_resource_id IS NOT NULL;

-- Foreign key from resourceitem_domain_id to domain_id
DELETE FROM ResourceItem WHERE resourceitem_domain_id NOT IN (SELECT domain_id FROM Domain) AND resourceitem_domain_id IS NOT NULL;

-- Foreign key from resourceitem_resourcetype_id to resourcetype_id
DELETE FROM ResourceItem WHERE resourceitem_resourcetype_id NOT IN (SELECT resourcetype_id FROM ResourceType) AND resourceitem_resourcetype_id IS NOT NULL;

-- Foreign key from resourcetype_domain_id to domain_id
UPDATE ResourceType SET resourcetype_domain_id = NULL WHERE resourcetype_domain_id NOT IN (SELECT domain_id FROM Domain) AND resourcetype_domain_id IS NOT NULL;

-- Foreign key from samba_domain_id to domain_id
DELETE FROM Samba WHERE samba_domain_id NOT IN (SELECT domain_id FROM Domain) AND samba_domain_id IS NOT NULL;

-- Foreign key from subscription_domain_id to domain_id
DELETE FROM Subscription WHERE subscription_domain_id NOT IN (SELECT domain_id FROM Domain) AND subscription_domain_id IS NOT NULL;

-- Foreign key from subscription_publication_id to publication_id
DELETE FROM Subscription WHERE subscription_publication_id NOT IN (SELECT publication_id FROM Publication) AND subscription_publication_id IS NOT NULL;

-- Foreign key from subscription_contact_id to contact_id
DELETE FROM Subscription WHERE subscription_contact_id NOT IN (SELECT contact_id FROM Contact) AND subscription_contact_id IS NOT NULL;

-- Foreign key from subscription_userupdate to userobm_id
UPDATE Subscription SET subscription_userupdate = NULL WHERE subscription_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND subscription_userupdate IS NOT NULL;

-- Foreign key from subscription_usercreate to userobm_id
UPDATE Subscription SET subscription_usercreate = NULL WHERE subscription_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND subscription_usercreate IS NOT NULL;

-- Foreign key from subscription_reception_id to subscription_reception_id
UPDATE Subscription SET subscription_reception_id = NULL WHERE subscription_reception_id NOT IN (SELECT subscriptionreception_id FROM SubscriptionReception) AND subscription_reception_id IS NOT NULL;

-- Foreign key from subscriptionreception_domain_id to domain_id
DELETE FROM SubscriptionReception WHERE subscriptionreception_domain_id NOT IN (SELECT domain_id FROM Domain) AND subscriptionreception_domain_id IS NOT NULL;

-- Foreign key from subscriptionreception_userupdate to userobm_id
UPDATE SubscriptionReception SET subscriptionreception_userupdate = NULL WHERE subscriptionreception_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND subscriptionreception_userupdate IS NOT NULL;

-- Foreign key from subscriptionreception_usercreate to userobm_id
UPDATE SubscriptionReception SET subscriptionreception_usercreate = NULL WHERE subscriptionreception_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND subscriptionreception_usercreate IS NOT NULL;

-- Foreign key from tasktype_domain_id to domain_id
DELETE FROM TaskType WHERE tasktype_domain_id NOT IN (SELECT domain_id FROM Domain) AND tasktype_domain_id IS NOT NULL;

-- Foreign key from tasktype_userupdate to userobm_id
UPDATE TaskType SET tasktype_userupdate = NULL WHERE tasktype_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND tasktype_userupdate IS NOT NULL;

-- Foreign key from tasktype_usercreate to userobm_id
UPDATE TaskType SET tasktype_usercreate = NULL WHERE tasktype_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND tasktype_usercreate IS NOT NULL;

-- Foreign key from timetask_user_id to userobm_id
DELETE FROM TimeTask WHERE timetask_user_id NOT IN (SELECT userobm_id FROM UserObm) AND timetask_user_id IS NOT NULL;

-- Foreign key from timetask_projecttask_id to projecttask_id
DELETE FROM TimeTask WHERE timetask_projecttask_id NOT IN (SELECT projecttask_id FROM ProjectTask) AND timetask_projecttask_id IS NOT NULL;

-- Foreign key from timetask_tasktype_id to tasktype_id
DELETE FROM TimeTask WHERE timetask_tasktype_id NOT IN (SELECT tasktype_id FROM TaskType) AND timetask_tasktype_id IS NOT NULL;

-- Foreign key from timetask_userupdate to userobm_id
UPDATE TimeTask SET timetask_userupdate = NULL WHERE timetask_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND timetask_userupdate IS NOT NULL;

-- Foreign key from timetask_usercreate to userobm_id
UPDATE TimeTask SET timetask_usercreate = NULL WHERE timetask_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND timetask_usercreate IS NOT NULL;

-- Foreign key from group_domain_id to domain_id
DELETE FROM UGroup WHERE group_domain_id NOT IN (SELECT domain_id FROM Domain) AND group_domain_id IS NOT NULL;

-- Foreign key from group_userupdate to userobm_id
UPDATE UGroup SET group_userupdate = NULL WHERE group_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND group_userupdate IS NOT NULL;

-- Foreign key from group_usercreate to userobm_id
UPDATE UGroup SET group_usercreate = NULL WHERE group_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND group_usercreate IS NOT NULL;

-- Foreign key from group_manager_id to userobm_id
UPDATE UGroup SET group_manager_id = NULL WHERE group_manager_id NOT IN (SELECT userobm_id FROM UserObm) AND group_manager_id IS NOT NULL;

-- Foreign key from updated_domain_id to domain_id
DELETE FROM Updated WHERE updated_domain_id NOT IN (SELECT domain_id FROM Domain) AND updated_domain_id IS NOT NULL;

-- Foreign key from updated_user_id to userobm_id
UPDATE Updated SET updated_user_id = NULL WHERE updated_user_id NOT IN (SELECT userobm_id FROM UserObm) AND updated_user_id IS NOT NULL;

-- Foreign key from updatedlinks_domain_id to domain_id
DELETE FROM Updatedlinks WHERE updatedlinks_domain_id NOT IN (SELECT domain_id FROM Domain) AND updatedlinks_domain_id IS NOT NULL;

-- Foreign key from updatedlinks_user_id to userobm_id
UPDATE Updatedlinks SET updatedlinks_user_id = NULL WHERE updatedlinks_user_id NOT IN (SELECT userobm_id FROM UserObm) AND updatedlinks_user_id IS NOT NULL;

-- Foreign key from userobm_domain_id to domain_id
DELETE FROM UserObm WHERE userobm_domain_id NOT IN (SELECT domain_id FROM Domain) AND userobm_domain_id IS NOT NULL;

-- Foreign key from userobm_userupdate to userobm_id
UPDATE UserObm SET userobm_userupdate = NULL WHERE userobm_userupdate NOT IN (SELECT userobm_id FROM (SELECT userobm_id FROM UserObm) AS Parent) AND userobm_userupdate IS NOT NULL;

-- Foreign key from userobm_usercreate to userobm_id
UPDATE UserObm SET userobm_usercreate = NULL WHERE userobm_usercreate NOT IN (SELECT userobm_id FROM (SELECT userobm_id FROM UserObm) AS Parent) AND userobm_usercreate IS NOT NULL;

-- Foreign key from userobm_mail_server_id to mailserver_id
UPDATE UserObm SET userobm_mail_server_id = NULL WHERE userobm_mail_server_id NOT IN (SELECT mailserver_id FROM MailServer) AND userobm_mail_server_id IS NOT NULL;

-- Foreign key from userobm_host_id to host_id
UPDATE UserObm SET userobm_host_id = NULL WHERE userobm_host_id NOT IN (SELECT host_id FROM Host) AND userobm_host_id IS NOT NULL;

-- Foreign key from userobm_photo_id to document_id
UPDATE UserObm SET userobm_photo_id = NULL WHERE userobm_photo_id NOT IN (SELECT document_id FROM Document) AND userobm_photo_id IS NOT NULL;

-- Foreign key from userobmgroup_group_id to group_id
DELETE FROM UserObmGroup WHERE userobmgroup_group_id NOT IN (SELECT group_id FROM UGroup) AND userobmgroup_group_id IS NOT NULL;

-- Foreign key from userobmgroup_userobm_id to userobm_id
DELETE FROM UserObmGroup WHERE userobmgroup_userobm_id NOT IN (SELECT userobm_id FROM UserObm) AND userobmgroup_userobm_id IS NOT NULL;

-- Foreign key from userobmpref_user_id to userobm_id
DELETE FROM UserObmPref WHERE userobmpref_user_id NOT IN (SELECT userobm_id FROM UserObm) AND userobmpref_user_id IS NOT NULL;

-- Foreign key from userobm_sessionlog_userobm_id to userobm_id
DELETE FROM UserObm_SessionLog WHERE userobm_sessionlog_userobm_id NOT IN (SELECT userobm_id FROM UserObm) AND userobm_sessionlog_userobm_id IS NOT NULL;

-- Foreign key from of_usergroup_group_id to group_id
DELETE FROM of_usergroup WHERE of_usergroup_group_id NOT IN (SELECT group_id FROM UGroup) AND of_usergroup_group_id IS NOT NULL;

-- Foreign key from of_usergroup_user_id to userobm_id
DELETE FROM of_usergroup WHERE of_usergroup_user_id NOT IN (SELECT userobm_id FROM UserObm) AND of_usergroup_user_id IS NOT NULL;

-- Foreign key from contact_birthday_id to event_id
-- UPDATE Contact SET contact_birthday_id = NULL WHERE contact_birthday_id NOT IN (SELECT event_id FROM Event) AND contact_birthday_id IS NOT NULL;

-- User prefs 
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'profile', 'profile_name', 1, 2);

-- Timezone 
insert into UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values (NULL,'set_timezone','Europe/Paris');
