-- Write that the 2.1->2.2 has started
UPDATE ObmInfo SET obminfo_value='2.1->2.2' WHERE obminfo_name='db_version';


--  _________________
-- | Tables creation |
--  ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯

--
-- Table structure for table 'Entity'
--
DROP TABLE IF EXISTS Entity;
CREATE TABLE Entity (
  entity_id int(8) NOT NULL auto_increment,
  entity_mailing boolean,
  PRIMARY KEY  (entity_id)
) ;

--
-- Address
--
DROP TABLE IF EXISTS Address;
CREATE TABLE Address (
  address_id                                    int(8) NOT NULL auto_increment,
  address_entity_id                             int(8) NOT NULL,
  address_street                                text,
  address_zipcode                               varchar(14),
  address_town                                  varchar(128),
  address_state                         	varchar(128),
  address_expresspostal                         varchar(16),
  address_country                               char(2),
  address_label                                 varchar(255),
  PRIMARY KEY (address_id)
) ;

--
-- Phone
--
DROP TABLE IF EXISTS Phone;
CREATE TABLE Phone (
  phone_id                                      int(8) NOT NULL auto_increment,
  phone_entity_id                               int(8) NOT NULL,
  phone_label                                   varchar(255) NOT NULL,
  phone_number                                  varchar(32),
  PRIMARY KEY (phone_id)
) ;

--
-- Website
--
DROP TABLE IF EXISTS Website;
CREATE TABLE Website (
  website_id                                    int(8) NOT NULL auto_increment,
  website_entity_id                             int(8) NOT NULL,
  website_label                                 varchar(255) NOT NULL,
  website_url                                   text,
  PRIMARY KEY (website_id)
) ;

--
-- Email
--
DROP TABLE IF EXISTS Email;
CREATE TABLE Email (
  email_id                                      int(8) NOT NULL auto_increment,
  email_entity_id                               int(8) NOT NULL,
  email_label                                   varchar(255) NOT NULL,
  email_address                                 varchar(255),
  PRIMARY KEY (email_id),
  KEY email_address (email_address)
) ;

--
-- Instant Messaging
--
DROP TABLE IF EXISTS IM;
CREATE TABLE IM (
  im_id                                         int(8) NOT NULL auto_increment,
  im_entity_id                                  int(8) NOT NULL,
  im_label                                      varchar(255),
  im_address                                    varchar(255),
  im_protocol                                   varchar(255),
  PRIMARY KEY (im_id),
  KEY im_address (im_address)
) ;

-- -------------------------------------
-- Add tables structures around Profiles
-- -------------------------------------
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

--  _______________
-- | CalendarEvent |
--  ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
-- Event Creation
CREATE TABLE Event (
  event_id              int(8) NOT NULL auto_increment,
  event_domain_id       int(8) NOT NULL,
  event_timeupdate      timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  event_timecreate      timestamp NOT NULL default '0000-00-00 00:00:00',
  event_userupdate      int(8) default NULL,
  event_usercreate      int(8) default NULL,
  event_parent_id       int(8) default NULL,
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
  KEY event_category1_id_eventcategory1_id_fkey (event_category1_id)
);

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

-- Table EventCategory1
CREATE TABLE EventCategory1 (
  eventcategory1_id int(8) NOT NULL auto_increment,
  eventcategory1_domain_id int(8) NOT NULL,
  eventcategory1_timeupdate timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  eventcategory1_timecreate timestamp NOT NULL default '0000-00-00 00:00:00',
  eventcategory1_userupdate int(8) default NULL,
  eventcategory1_usercreate int(8) default NULL,
  eventcategory1_code varchar(10) default '',
  eventcategory1_label varchar(128) default NULL,
  eventcategory1_color char(6) default NULL,
  PRIMARY KEY (eventcategory1_id),
  KEY eventcategory1_domain_id_domain_id_fkey (eventcategory1_domain_id),
  KEY eventcategory1_userupdate_userobm_id_fkey (eventcategory1_userupdate),
  KEY eventcategory1_usercreate_userobm_id_fkey (eventcategory1_usercreate)
) ;

-- Table `DeletedEvent`
CREATE TABLE DeletedEvent (
  deletedevent_id        int(8) NOT NULL auto_increment,
  deletedevent_event_id  int(8) default NULL,
  deletedevent_user_id   int(8) default NULL,
  deletedevent_origin    varchar(255) NOT NULL,
  deletedevent_type      enum('VEVENT','VTODO','VJOURNAL','VFREEBUSY') default 'VEVENT',
  deletedevent_timestamp timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  PRIMARY KEY (deletedevent_id),
  KEY idx_dce_event (deletedevent_event_id),
  KEY idx_dce_user (deletedevent_user_id)
);


--
-- Table structure for table `ServiceProperty`
--

CREATE TABLE `ServiceProperty` (
  `serviceproperty_id` int(8) auto_increment,
  `serviceproperty_service` varchar(255) NOT NULL,
  `serviceproperty_property` varchar(255) NOT NULL,
  `serviceproperty_entity_id` int(8) NOT NULL,
  `serviceproperty_value` text,
  PRIMARY KEY  (`serviceproperty_id`),
  KEY `serviceproperty_service_key` (`serviceproperty_service`),
  KEY `serviceproperty_property_key` (`serviceproperty_property`),
  KEY `serviceproperty_entity_id_entity_id_fkey` (`serviceproperty_entity_id`)
);

--
-- Table structure for table `Service`
--

CREATE TABLE `Service` (
  `service_id` int(8) auto_increment,
  `service_service` varchar(255) NOT NULL,
  `service_entity_id` int(8) NOT NULL,
  PRIMARY KEY  (`service_id`),
  KEY `service_service_key` (`service_service`),
  KEY `service_entity_id_entity_id_fkey` (`service_entity_id`)
);
--
-- Table structure for table `SSOTicket`
--
DROP TABLE IF EXISTS `SSOTicket`;
CREATE TABLE `SSOTicket` (
  `ssoticket_ticket` varchar(255) NOT NULL,
  `ssoticket_user_id` int(8),
  `ssoticket_timestamp` timestamp NOT NULL,
  PRIMARY KEY (`ssoticket_ticket`)
);

--
-- Table structure for table `TaskEvent`
--

DROP TABLE IF EXISTS `TaskEvent`;
CREATE TABLE TaskEvent (
  taskevent_task_id int(8) NOT NULL,
  taskevent_event_id int(8) NOT NULL,
  PRIMARY KEY (taskevent_event_id, taskevent_task_id)
);
--  _______________
-- | Entity tables |
--  ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
-- Modification of table EventEntity before rename it to EventLink
--
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
DELETE FROM EventEntity WHERE evententity_event_id NOT IN (SELECT calendarevent_id FROM CalendarEvent);

-- Foreign key from evententity_userupdate to userobm_id
UPDATE EventEntity SET evententity_userupdate = NULL WHERE evententity_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND evententity_userupdate IS NOT NULL;

-- Foreign key from evententity_usercreate to userobm_id
UPDATE EventEntity SET evententity_usercreate = NULL WHERE evententity_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND evententity_usercreate IS NOT NULL;

ALTER TABLE EventEntity RENAME TO EventLink;
ALTER TABLE EventLink CHANGE COLUMN evententity_timeupdate eventlink_timeupdate timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP;
ALTER TABLE EventLink CHANGE COLUMN evententity_timecreate eventlink_timecreate timestamp NOT NULL default '0000-00-00 00:00:00';
ALTER TABLE EventLink CHANGE COLUMN evententity_userupdate eventlink_userupdate int(8) default NULL;
ALTER TABLE EventLink CHANGE COLUMN evententity_usercreate eventlink_usercreate int(8) default NULL;
ALTER TABLE EventLink CHANGE COLUMN evententity_event_id eventlink_event_id int(8) NOT NULL;
ALTER TABLE EventLink CHANGE COLUMN evententity_entity_id eventlink_entity_id int(8) NOT NULL;
ALTER TABLE EventLink CHANGE COLUMN evententity_entity eventlink_entity varchar(255) NOT NULL;
ALTER TABLE EventLink CHANGE COLUMN evententity_state eventlink_state enum('NEEDS-ACTION','ACCEPTED','DECLINED','TENTATIVE','DELEGATED','COMPLETED','IN-PROGRESS') default 'NEEDS-ACTION';
ALTER TABLE EventLink CHANGE COLUMN evententity_required eventlink_required enum('CHAIR','REQ','OPT','NON') default 'REQ';
ALTER TABLE EventLink CHANGE COLUMN evententity_percent eventlink_percent int(3) default '0';

ALTER TABLE DocumentEntity RENAME TO DocumentLink;
ALTER TABLE DocumentLink CHANGE COLUMN documententity_document_id documentlink_document_id int(8) NOT NULL;
ALTER TABLE DocumentLink CHANGE COLUMN documententity_entity_id documentlink_entity_id int(8) NOT NULL;
ALTER TABLE DocumentLink CHANGE COLUMN documententity_entity documentlink_entity varchar(255) NOT NULL;

ALTER TABLE OGroupEntity RENAME TO OGroupLink;
ALTER TABLE OGroupLink CHANGE COLUMN ogroupentity_id ogrouplink_id int(8) NOT NULL auto_increment;
ALTER TABLE OGroupLink CHANGE COLUMN ogroupentity_domain_id ogrouplink_domain_id int(8) NOT NULL;
ALTER TABLE OGroupLink CHANGE COLUMN ogroupentity_timeupdate ogrouplink_timeupdate timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP;
ALTER TABLE OGroupLink CHANGE COLUMN ogroupentity_timecreate ogrouplink_timecreate timestamp NOT NULL default '0000-00-00 00:00:00';
ALTER TABLE OGroupLink CHANGE COLUMN ogroupentity_userupdate ogrouplink_userupdate int(8) default NULL;
ALTER TABLE OGroupLink CHANGE COLUMN ogroupentity_usercreate ogrouplink_usercreate int(8) default NULL;
ALTER TABLE OGroupLink CHANGE COLUMN ogroupentity_ogroup_id ogrouplink_ogroup_id int(8) NOT NULL;
ALTER TABLE OGroupLink CHANGE COLUMN ogroupentity_entity_id ogrouplink_entity_id int(8) NOT NULL;
ALTER TABLE OGroupLink CHANGE COLUMN ogroupentity_entity ogrouplink_entity varchar(255) NOT NULL;

DROP TABLE IF EXISTS AccountEntity;
CREATE TABLE AccountEntity (
  accountentity_entity_id int(8) NOT NULL,
  accountentity_account_id int(8) NOT NULL,
  PRIMARY KEY (accountentity_entity_id, accountentity_account_id)
);
  
DROP TABLE IF EXISTS CvEntity;
CREATE TABLE CvEntity (
  cventity_entity_id int(8) NOT NULL,
  cventity_cv_id int(8) NOT NULL,
  PRIMARY KEY (cventity_entity_id, cventity_cv_id)
);
  
DROP TABLE IF EXISTS CalendarEntity;
CREATE TABLE CalendarEntity (
  calendarentity_entity_id int(8) NOT NULL,
  calendarentity_calendar_id int(8) NOT NULL,
  PRIMARY KEY (calendarentity_entity_id, calendarentity_calendar_id)
);
  
DROP TABLE IF EXISTS CompanyEntity;
CREATE TABLE CompanyEntity (
  companyentity_entity_id int(8) NOT NULL,
  companyentity_company_id int(8) NOT NULL,
  PRIMARY KEY (companyentity_entity_id, companyentity_company_id)
);
  
DROP TABLE IF EXISTS ContactEntity;
CREATE TABLE ContactEntity (
  contactentity_entity_id int(8) NOT NULL,
  contactentity_contact_id int(8) NOT NULL,
  PRIMARY KEY (contactentity_entity_id, contactentity_contact_id)
);
  
DROP TABLE IF EXISTS ContractEntity;
CREATE TABLE ContractEntity (
  contractentity_entity_id int(8) NOT NULL,
  contractentity_contract_id int(8) NOT NULL,
  PRIMARY KEY (contractentity_entity_id, contractentity_contract_id)
);
  
DROP TABLE IF EXISTS DealEntity;
CREATE TABLE DealEntity (
  dealentity_entity_id int(8) NOT NULL,
  dealentity_deal_id int(8) NOT NULL,
  PRIMARY KEY (dealentity_entity_id, dealentity_deal_id)
);
  
DROP TABLE IF EXISTS DocumentEntity;
CREATE TABLE DocumentEntity (
  documententity_entity_id int(8) NOT NULL,
  documententity_document_id int(8) NOT NULL,
  PRIMARY KEY (documententity_entity_id, documententity_document_id)
);
  
DROP TABLE IF EXISTS DomainEntity;
CREATE TABLE DomainEntity (
  domainentity_entity_id int(8) NOT NULL,
  domainentity_domain_id int(8) NOT NULL,
  PRIMARY KEY (domainentity_entity_id, domainentity_domain_id)
);
  
DROP TABLE IF EXISTS EventEntity;
CREATE TABLE EventEntity (
  evententity_entity_id int(8) NOT NULL,
  evententity_event_id int(8) NOT NULL,
  PRIMARY KEY (evententity_entity_id, evententity_event_id)
);
  
DROP TABLE IF EXISTS HostEntity;
CREATE TABLE HostEntity (
  hostentity_entity_id int(8) NOT NULL,
  hostentity_host_id int(8) NOT NULL,
  PRIMARY KEY (hostentity_entity_id, hostentity_host_id)
);
  
DROP TABLE IF EXISTS ImportEntity;
CREATE TABLE ImportEntity (
  importentity_entity_id int(8) NOT NULL,
  importentity_import_id int(8) NOT NULL,
  PRIMARY KEY (importentity_entity_id, importentity_import_id)
);
  
DROP TABLE IF EXISTS IncidentEntity;
CREATE TABLE IncidentEntity (
  incidententity_entity_id int(8) NOT NULL,
  incidententity_incident_id int(8) NOT NULL,
  PRIMARY KEY (incidententity_entity_id, incidententity_incident_id)
);
  
DROP TABLE IF EXISTS InvoiceEntity;
CREATE TABLE InvoiceEntity (
  invoiceentity_entity_id int(8) NOT NULL,
  invoiceentity_invoice_id int(8) NOT NULL,
  PRIMARY KEY (invoiceentity_entity_id, invoiceentity_invoice_id)
);
  
DROP TABLE IF EXISTS LeadEntity;
CREATE TABLE LeadEntity (
  leadentity_entity_id int(8) NOT NULL,
  leadentity_lead_id int(8) NOT NULL,
  PRIMARY KEY (leadentity_entity_id, leadentity_lead_id)
);
  
DROP TABLE IF EXISTS ListEntity;
CREATE TABLE ListEntity (
  listentity_entity_id int(8) NOT NULL,
  listentity_list_id int(8) NOT NULL,
  PRIMARY KEY (listentity_entity_id, listentity_list_id)
);
  
DROP TABLE IF EXISTS MailshareEntity;
CREATE TABLE MailshareEntity (
  mailshareentity_entity_id int(8) NOT NULL,
  mailshareentity_mailshare_id int(8) NOT NULL,
  PRIMARY KEY (mailshareentity_entity_id, mailshareentity_mailshare_id)
);
  
DROP TABLE IF EXISTS MailboxEntity;
CREATE TABLE MailboxEntity (
  mailboxentity_entity_id int(8) NOT NULL,
  mailboxentity_mailbox_id int(8) NOT NULL,
  PRIMARY KEY (mailboxentity_entity_id, mailboxentity_mailbox_id)
);
  
DROP TABLE IF EXISTS OgroupEntity;
CREATE TABLE OgroupEntity (
  ogroupentity_entity_id int(8) NOT NULL,
  ogroupentity_ogroup_id int(8) NOT NULL,
  PRIMARY KEY (ogroupentity_entity_id, ogroupentity_ogroup_id)
);
  
DROP TABLE IF EXISTS ObmbookmarkEntity;
CREATE TABLE ObmbookmarkEntity (
  obmbookmarkentity_entity_id int(8) NOT NULL,
  obmbookmarkentity_obmbookmark_id int(8) NOT NULL,
  PRIMARY KEY (obmbookmarkentity_entity_id, obmbookmarkentity_obmbookmark_id)
);
  
DROP TABLE IF EXISTS OrganizationalchartEntity;
CREATE TABLE OrganizationalchartEntity (
  organizationalchartentity_entity_id int(8) NOT NULL,
  organizationalchartentity_organizationalchart_id int(8) NOT NULL,
  PRIMARY KEY (organizationalchartentity_entity_id, organizationalchartentity_organizationalchart_id)
);
  
DROP TABLE IF EXISTS ParentdealEntity;
CREATE TABLE ParentdealEntity (
  parentdealentity_entity_id int(8) NOT NULL,
  parentdealentity_parentdeal_id int(8) NOT NULL,
  PRIMARY KEY (parentdealentity_entity_id, parentdealentity_parentdeal_id)
);
  
DROP TABLE IF EXISTS PaymentEntity;
CREATE TABLE PaymentEntity (
  paymententity_entity_id int(8) NOT NULL,
  paymententity_payment_id int(8) NOT NULL,
  PRIMARY KEY (paymententity_entity_id, paymententity_payment_id)
);
  
DROP TABLE IF EXISTS ProfileEntity;
CREATE TABLE ProfileEntity (
  profileentity_entity_id int(8) NOT NULL,
  profileentity_profile_id int(8) NOT NULL,
  PRIMARY KEY (profileentity_entity_id, profileentity_profile_id)
);
  
DROP TABLE IF EXISTS ProjectEntity;
CREATE TABLE ProjectEntity (
  projectentity_entity_id int(8) NOT NULL,
  projectentity_project_id int(8) NOT NULL,
  PRIMARY KEY (projectentity_entity_id, projectentity_project_id)
);
  
DROP TABLE IF EXISTS PublicationEntity;
CREATE TABLE PublicationEntity (
  publicationentity_entity_id int(8) NOT NULL,
  publicationentity_publication_id int(8) NOT NULL,
  PRIMARY KEY (publicationentity_entity_id, publicationentity_publication_id)
);
  
DROP TABLE IF EXISTS ResourcegroupEntity;
CREATE TABLE ResourcegroupEntity (
  resourcegroupentity_entity_id int(8) NOT NULL,
  resourcegroupentity_resourcegroup_id int(8) NOT NULL,
  PRIMARY KEY (resourcegroupentity_entity_id, resourcegroupentity_resourcegroup_id)
);
  
DROP TABLE IF EXISTS ResourceEntity;
CREATE TABLE ResourceEntity (
  resourceentity_entity_id int(8) NOT NULL,
  resourceentity_resource_id int(8) NOT NULL,
  PRIMARY KEY (resourceentity_entity_id, resourceentity_resource_id)
);
  
DROP TABLE IF EXISTS SubscriptionEntity;
CREATE TABLE SubscriptionEntity (
  subscriptionentity_entity_id int(8) NOT NULL,
  subscriptionentity_subscription_id int(8) NOT NULL,
  PRIMARY KEY (subscriptionentity_entity_id, subscriptionentity_subscription_id)
);
  
DROP TABLE IF EXISTS GroupEntity;
CREATE TABLE GroupEntity (
  groupentity_entity_id int(8) NOT NULL,
  groupentity_group_id int(8) NOT NULL,
  PRIMARY KEY (groupentity_entity_id, groupentity_group_id)
);
  
DROP TABLE IF EXISTS UserEntity;
CREATE TABLE UserEntity (
  userentity_entity_id int(8) NOT NULL,
  userentity_user_id int(8) NOT NULL,
  PRIMARY KEY (userentity_entity_id, userentity_user_id)
);
  
CREATE TABLE TmpEntity (
  entity_id     int(8) auto_increment,
  id_entity     integer,
  PRIMARY KEY (entity_id)
);

--  __________________
-- | Cleanning tables |
--  ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
--
-- Clean CalendarEvent before migration to Event
--
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

--
-- Clean Todo before migration to Event
--
-- Foreign key from todo_domain_id to domain_id
DELETE FROM Todo WHERE todo_domain_id NOT IN (SELECT domain_id FROM Domain) AND todo_domain_id IS NOT NULL;
-- Foreign key from todo_user to userobm_id
DELETE FROM Todo WHERE todo_user NOT IN (SELECT userobm_id FROM UserObm) AND todo_user IS NOT NULL;
-- Foreign key from todo_userupdate to userobm_id
UPDATE Todo SET todo_userupdate = NULL WHERE todo_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND todo_userupdate IS NOT NULL;
-- Foreign key from todo_usercreate to userobm_id
UPDATE Todo SET todo_usercreate = NULL WHERE todo_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND todo_usercreate IS NOT NULL;

--
-- Migration of CalendarEvent to Event
--
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

--
-- Migration of Todo to Event
--
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


--
-- Clean CalendarAlert before migration to EventAlert
--
-- Foreign key from calendaralert_event_id to event_id
DELETE FROM CalendarAlert WHERE calendaralert_event_id NOT IN (SELECT event_id FROM Event) AND calendaralert_event_id IS NOT NULL;
-- Foreign key from calendaralert_user_id to userobm_id
DELETE FROM CalendarAlert WHERE calendaralert_user_id NOT IN (SELECT userobm_id FROM UserObm) AND calendaralert_user_id IS NOT NULL;
-- Foreign key from calendaralert_userupdate to userobm_id
UPDATE CalendarAlert SET calendaralert_userupdate = NULL WHERE calendaralert_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND calendaralert_userupdate IS NOT NULL;
-- Foreign key from calendaralert_usercreate to userobm_id
UPDATE CalendarAlert SET calendaralert_usercreate = NULL WHERE calendaralert_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND calendaralert_usercreate IS NOT NULL;

--
-- Clean CalendarException before migration to EventException
--
-- Foreign key from calendarexception_event_id to event_id
DELETE FROM CalendarException WHERE calendarexception_event_id NOT IN (SELECT event_id FROM Event) AND calendarexception_event_id IS NOT NULL;
-- Foreign key from calendarexception_userupdate to userobm_id
UPDATE CalendarException SET calendarexception_userupdate = NULL WHERE calendarexception_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND calendarexception_userupdate IS NOT NULL;
-- Foreign key from calendarexception_usercreate to userobm_id
UPDATE CalendarException SET calendarexception_usercreate = NULL WHERE calendarexception_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND calendarexception_usercreate IS NOT NULL;

--
-- Clean CalendarCategory1 before migration to EventCategory1
--
-- Foreign key from calendarcategory1_domain_id to domain_id
DELETE FROM CalendarCategory1 WHERE calendarcategory1_domain_id NOT IN (SELECT domain_id FROM Domain) AND calendarcategory1_domain_id IS NOT NULL;
-- Foreign key from calendarcategory1_userupdate to userobm_id
UPDATE CalendarCategory1 SET calendarcategory1_userupdate = NULL WHERE calendarcategory1_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND calendarcategory1_userupdate IS NOT NULL;
-- Foreign key from calendarcategory1_usercreate to userobm_id
UPDATE CalendarCategory1 SET calendarcategory1_usercreate = NULL WHERE calendarcategory1_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND calendarcategory1_usercreate IS NOT NULL;

--
-- Migration of CalendarAlert to EventAlert
--
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

--
-- Migration of CalendarException to EventException
--
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
-- Migration of CalendarCategory1 to EventCategory1
--
INSERT INTO EventCategory1 (
  eventcategory1_id,
  eventcategory1_domain_id,
  eventcategory1_timeupdate,
  eventcategory1_timecreate,
  eventcategory1_userupdate,
  eventcategory1_usercreate,
  eventcategory1_code,
  eventcategory1_label,
  eventcategory1_color)
SELECT
  calendarcategory1_id,
  calendarcategory1_domain_id,
  calendarcategory1_timeupdate,
  calendarcategory1_timecreate,
  calendarcategory1_userupdate,
  calendarcategory1_usercreate,
  calendarcategory1_code,
  calendarcategory1_label,
  calendarcategory1_color
FROM CalendarCategory1;

--
-- Migration of DeletedCalendarEvent to DeletedEvent
--
INSERT INTO DeletedEvent (deletedevent_id,
  deletedevent_event_id,
  deletedevent_user_id,
  deletedevent_type,
  deletedevent_timestamp, deletedevent_origin)
SELECT
  deletedcalendarevent_id,
  deletedcalendarevent_event_id,
  deletedcalendarevent_user_id,
  'VEVENT',
  deletedcalendarevent_timestamp, 'obm21'
FROM DeletedCalendarEvent;




--  ______________________
-- | Tables modifications |
--  ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
-- NOTE : Set integer to boolean when necessary
-- ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
-- Domain
ALTER TABLE Domain ADD COLUMN domain_global BOOLEAN DEFAULT FALSE;
ALTER TABLE Domain DROP COLUMN domain_mail_server_id;

-- OGroup
ALTER TABLE OGroup MODIFY COLUMN ogroup_parent_id int(8);

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
ALTER TABLE DeletedContact ADD COLUMN deletedcontact_user_id int(8) NOT NULL;
ALTER TABLE DeletedContact ADD COLUMN deletedcontact_origin varchar(255) NOT NULL;
ALTER TABLE DeletedUser MODIFY COLUMN deleteduser_user_id int(8) NOT NULL ;
ALTER TABLE Document MODIFY COLUMN document_domain_id int(8) NOT NULL ;
ALTER TABLE DocumentMimeType MODIFY COLUMN documentmimetype_domain_id int(8) NOT NULL ;
ALTER TABLE DomainMailServer MODIFY COLUMN domainmailserver_domain_id int(8) NOT NULL ;
ALTER TABLE EntityRight MODIFY COLUMN entityright_entity_id int(8) NOT NULL ;
ALTER TABLE EntityRight ADD entityright_access int(1) NOT NULL default 0;
ALTER TABLE EntityRight MODIFY COLUMN entityright_consumer_id int(8);
ALTER TABLE EntityRight DROP PRIMARY KEY;
ALTER TABLE EntityRight ADD COLUMN entityright_id int(8) NOT NULL auto_increment PRIMARY KEY;
ALTER TABLE EventLink MODIFY COLUMN eventlink_event_id int(8) NOT NULL ;
ALTER TABLE EventLink MODIFY COLUMN eventlink_entity_id int(8) NOT NULL ;
ALTER TABLE GroupGroup MODIFY COLUMN groupgroup_parent_id int(8) NOT NULL ;
ALTER TABLE GroupGroup MODIFY COLUMN groupgroup_child_id int(8) NOT NULL ;
ALTER TABLE Host MODIFY COLUMN host_domain_id int(8) NOT NULL ;
ALTER TABLE Host ADD COLUMN host_fqdn varchar(255);
ALTER TABLE Import MODIFY COLUMN import_domain_id int(8) NOT NULL ;
ALTER TABLE Import MODIFY COLUMN import_datasource_id int(8)  default NULL;
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
ALTER TABLE Lead ADD COLUMN lead_priority int(2) DEFAULT 0;
ALTER TABLE LeadSource MODIFY COLUMN leadsource_domain_id int(8) NOT NULL ;
ALTER TABLE LeadStatus MODIFY COLUMN leadstatus_domain_id int(8) NOT NULL ;
ALTER TABLE List MODIFY COLUMN list_domain_id int(8) NOT NULL ;
ALTER TABLE MailServer MODIFY COLUMN mailserver_host_id int(8) NOT NULL ;
ALTER TABLE MailServerNetwork MODIFY COLUMN mailservernetwork_host_id int(8) NOT NULL ;
ALTER TABLE MailShare MODIFY COLUMN mailshare_domain_id int(8) NOT NULL ;
ALTER TABLE MailShare MODIFY COLUMN mailshare_mail_server_id int(8)  default NULL;
ALTER TABLE OGroup MODIFY COLUMN ogroup_domain_id int(8) NOT NULL ;
ALTER TABLE OGroupLink MODIFY COLUMN ogrouplink_domain_id int(8) NOT NULL ;
ALTER TABLE OrganizationalChart MODIFY COLUMN organizationalchart_domain_id int(8) NOT NULL ;
ALTER TABLE ParentDeal MODIFY COLUMN parentdeal_domain_id int(8) NOT NULL ;
ALTER TABLE Payment MODIFY COLUMN payment_domain_id int(8) NOT NULL ;
ALTER TABLE PaymentKind MODIFY COLUMN paymentkind_domain_id int(8) NOT NULL ;
ALTER TABLE Project MODIFY COLUMN project_domain_id int(8) NOT NULL ;
ALTER TABLE ProjectTask MODIFY COLUMN projecttask_parenttask_id int(8)  default NULL;
ALTER TABLE Publication MODIFY COLUMN publication_domain_id int(8) NOT NULL ;
ALTER TABLE PublicationType MODIFY COLUMN publicationtype_domain_id int(8) NOT NULL ;
ALTER TABLE RGroup MODIFY COLUMN rgroup_domain_id int(8) NOT NULL ;
ALTER TABLE Region MODIFY COLUMN region_domain_id int(8) NOT NULL ;
ALTER TABLE Resource MODIFY COLUMN resource_domain_id int(8) NOT NULL;
ALTER TABLE Resource ADD COLUMN resource_delegation varchar(64) default '';
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
ALTER TABLE UserObm MODIFY COLUMN userobm_email_nomade text default '';
ALTER TABLE UserObmGroup MODIFY COLUMN userobmgroup_userobm_id int(8) NOT NULL ;
ALTER TABLE UserObmPref MODIFY COLUMN userobmpref_user_id int(8) NULL ;
ALTER TABLE UserObmPref ADD COLUMN userobmpref_id int(8) NOT NULL AUTO_INCREMENT, ADD PRIMARY KEY (userobmpref_id);
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

-- Set archive to int(1) NOT NULL DEFAUL 0 everywhere

UPDATE Company set company_archive='0' where company_archive != '1';
ALTER TABLE Company MODIFY COLUMN company_archive int(1) DEFAULT 0 NOT NULL;

UPDATE Contact set contact_archive='0' where contact_archive != '1';
ALTER TABLE Contact MODIFY COLUMN contact_archive int(1) DEFAULT 0 NOT NULL;

UPDATE Deal set deal_archive='0' where deal_archive != '1';
ALTER TABLE Deal MODIFY COLUMN deal_archive int(1) DEFAULT 0 NOT NULL;

UPDATE ParentDeal set parentdeal_archive='0' where parentdeal_archive != '1';
ALTER TABLE ParentDeal MODIFY COLUMN parentdeal_archive int(1) DEFAULT 0 NOT NULL;

UPDATE Lead set lead_archive='0' where lead_archive != '1';
ALTER TABLE Lead MODIFY COLUMN lead_archive int(1) DEFAULT 0 NOT NULL;

UPDATE Incident set incident_archive='0' where incident_archive != '1';
ALTER TABLE Incident MODIFY COLUMN incident_archive int(1) DEFAULT 0 NOT NULL;

UPDATE Invoice set invoice_archive='0' where invoice_archive != '1';
ALTER TABLE Invoice MODIFY COLUMN invoice_archive int(1) DEFAULT 0 NOT NULL;

UPDATE List set list_contact_archive='0' where list_contact_archive != '1';
ALTER TABLE List MODIFY COLUMN list_contact_archive int(1) DEFAULT 0 NOT NULL;

UPDATE Project set project_archive='0' where project_archive != '1';
ALTER TABLE Project MODIFY COLUMN project_archive int(1) DEFAULT 0 NOT NULL;

ALTER TABLE Host ADD COLUMN host_archive int(1) DEFAULT 0 NOT NULL;

ALTER TABLE UGroup ADD COLUMN group_archive int(1) DEFAULT 0 NOT NULL;

--  _________________
-- | Updating values |
--  ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
-- Global Domain
INSERT INTO Domain (domain_timecreate,domain_label,domain_description,domain_name,domain_global) VALUES  (NOW(), 'Global Domain', 'Virtual domain for managing domains', 'global.virt', TRUE);
UPDATE UserObm SET userobm_domain_id = (SELECT domain_id FROM Domain WHERE domain_global = TRUE) WHERE userobm_domain_id = 0;
UPDATE Host SET host_domain_id = (SELECT domain_id FROM Domain WHERE domain_global = TRUE) WHERE host_domain_id = 0;

-- Preferences
INSERT INTO DisplayPref (display_user_id,display_entity,display_fieldname,display_fieldorder,display_display) VALUES (NULL,'profile', 'profile_name', 1, 2);
UPDATE UserObmPref SET userobmpref_value='event_priority' WHERE userobmpref_value='todo_priority';
UPDATE DisplayPref SET display_fieldname='event_title' WHERE display_fieldname='todo_title';
UPDATE DisplayPref SET display_fieldname='event_priority' WHERE display_fieldname='todo_priority';
UPDATE DisplayPref SET display_fieldname='event_date' WHERE display_fieldname='todo_date';
UPDATE DisplayPref SET display_fieldname='event_update' WHERE display_fieldname='todo_update';
UPDATE DisplayPref SET display_fieldname='eventlink_percent' WHERE display_fieldname='todo_percent';


-- Timezone 
INSERT INTO UserObmPref(userobmpref_user_id,userobmpref_option,userobmpref_value) values (NULL,'set_timezone','Europe/Paris');

-- Default Profile properties
INSERT INTO ProfileProperty (profileproperty_name, profileproperty_type, profileproperty_default, profileproperty_readonly) VALUES ('update_state', 'integer', 1, 1);
INSERT INTO ProfileProperty (profileproperty_name, profileproperty_type, profileproperty_default) VALUES ('level', 'integer', 3);
INSERT INTO ProfileProperty (profileproperty_name, profileproperty_type, profileproperty_default) VALUES ('level_managepeers', 'integer', 0);
INSERT INTO ProfileProperty (profileproperty_name, profileproperty_type, profileproperty_default) VALUES ('access_restriction', 'text', 'ALLOW_ALL');
INSERT INTO ProfileProperty (profileproperty_name, profileproperty_type, profileproperty_default) VALUES ('admin_realm', 'text', '');
INSERT INTO ProfileProperty (profileproperty_name, profileproperty_type, profileproperty_default, profileproperty_readonly) VALUES ('last_public_contact_export', 'timestamp', 0, 1);


-- --------------------
-- Entity tables update
-- --------------------
INSERT INTO TmpEntity (id_entity) SELECT account_id FROM Account;
INSERT INTO Entity (entity_id) SELECT entity_id FROM TmpEntity WHERE id_entity IS NOT NULL;
INSERT INTO AccountEntity (accountentity_entity_id, accountentity_account_id) SELECT entity_id, id_entity FROM TmpEntity WHERE id_entity IS NOT NULL;
UPDATE TmpEntity SET id_entity = NULL;
  
INSERT INTO TmpEntity (id_entity) SELECT cv_id FROM CV;
INSERT INTO Entity (entity_id) SELECT entity_id FROM TmpEntity WHERE id_entity IS NOT NULL;
INSERT INTO CvEntity (cventity_entity_id, cventity_cv_id) SELECT entity_id, id_entity FROM TmpEntity WHERE id_entity IS NOT NULL;
UPDATE TmpEntity SET id_entity = NULL;
  
INSERT INTO TmpEntity (id_entity) SELECT userobm_id FROM UserObm;
INSERT INTO Entity (entity_id) SELECT entity_id FROM TmpEntity WHERE id_entity IS NOT NULL;
INSERT INTO CalendarEntity (calendarentity_entity_id, calendarentity_calendar_id) SELECT entity_id, id_entity FROM TmpEntity WHERE id_entity IS NOT NULL;
UPDATE TmpEntity SET id_entity = NULL;
  
INSERT INTO TmpEntity (id_entity) SELECT company_id FROM Company;
INSERT INTO Entity (entity_id) SELECT entity_id FROM TmpEntity WHERE id_entity IS NOT NULL;
INSERT INTO CompanyEntity (companyentity_entity_id, companyentity_company_id) SELECT entity_id, id_entity FROM TmpEntity WHERE id_entity IS NOT NULL;
UPDATE TmpEntity SET id_entity = NULL;
  
INSERT INTO TmpEntity (id_entity) SELECT contact_id FROM Contact;
INSERT INTO Entity (entity_id) SELECT entity_id FROM TmpEntity WHERE id_entity IS NOT NULL;
INSERT INTO ContactEntity (contactentity_entity_id, contactentity_contact_id) SELECT entity_id, id_entity FROM TmpEntity WHERE id_entity IS NOT NULL;
UPDATE TmpEntity SET id_entity = NULL;
  
INSERT INTO TmpEntity (id_entity) SELECT contract_id FROM Contract;
INSERT INTO Entity (entity_id) SELECT entity_id FROM TmpEntity WHERE id_entity IS NOT NULL;
INSERT INTO ContractEntity (contractentity_entity_id, contractentity_contract_id) SELECT entity_id, id_entity FROM TmpEntity WHERE id_entity IS NOT NULL;
UPDATE TmpEntity SET id_entity = NULL;
  
INSERT INTO TmpEntity (id_entity) SELECT deal_id FROM Deal;
INSERT INTO Entity (entity_id) SELECT entity_id FROM TmpEntity WHERE id_entity IS NOT NULL;
INSERT INTO DealEntity (dealentity_entity_id, dealentity_deal_id) SELECT entity_id, id_entity FROM TmpEntity WHERE id_entity IS NOT NULL;
UPDATE TmpEntity SET id_entity = NULL;
  
-- INSERT INTO TmpEntity (id_entity) SELECT defaultodttemplate_id FROM DefaultOdtTemplate;
INSERT INTO TmpEntity (id_entity) SELECT document_id FROM Document;
INSERT INTO Entity (entity_id) SELECT entity_id FROM TmpEntity WHERE id_entity IS NOT NULL;
INSERT INTO DocumentEntity (documententity_entity_id, documententity_document_id) SELECT entity_id, id_entity FROM TmpEntity WHERE id_entity IS NOT NULL;
UPDATE TmpEntity SET id_entity = NULL;
  
INSERT INTO TmpEntity (id_entity) SELECT domain_id FROM Domain;
INSERT INTO Entity (entity_id) SELECT entity_id FROM TmpEntity WHERE id_entity IS NOT NULL;
INSERT INTO DomainEntity (domainentity_entity_id, domainentity_domain_id) SELECT entity_id, id_entity FROM TmpEntity WHERE id_entity IS NOT NULL;
UPDATE TmpEntity SET id_entity = NULL;
  
INSERT INTO TmpEntity (id_entity) SELECT event_id FROM Event;
INSERT INTO Entity (entity_id) SELECT entity_id FROM TmpEntity WHERE id_entity IS NOT NULL;
INSERT INTO EventEntity (evententity_entity_id, evententity_event_id) SELECT entity_id, id_entity FROM TmpEntity WHERE id_entity IS NOT NULL;
UPDATE TmpEntity SET id_entity = NULL;
  
INSERT INTO TmpEntity (id_entity) SELECT host_id FROM Host;
INSERT INTO Entity (entity_id) SELECT entity_id FROM TmpEntity WHERE id_entity IS NOT NULL;
INSERT INTO HostEntity (hostentity_entity_id, hostentity_host_id) SELECT entity_id, id_entity FROM TmpEntity WHERE id_entity IS NOT NULL;
UPDATE TmpEntity SET id_entity = NULL;
  
INSERT INTO TmpEntity (id_entity) SELECT import_id FROM Import;
INSERT INTO Entity (entity_id) SELECT entity_id FROM TmpEntity WHERE id_entity IS NOT NULL;
INSERT INTO ImportEntity (importentity_entity_id, importentity_import_id) SELECT entity_id, id_entity FROM TmpEntity WHERE id_entity IS NOT NULL;
UPDATE TmpEntity SET id_entity = NULL;
  
INSERT INTO TmpEntity (id_entity) SELECT incident_id FROM Incident;
INSERT INTO Entity (entity_id) SELECT entity_id FROM TmpEntity WHERE id_entity IS NOT NULL;
INSERT INTO IncidentEntity (incidententity_entity_id, incidententity_incident_id) SELECT entity_id, id_entity FROM TmpEntity WHERE id_entity IS NOT NULL;
UPDATE TmpEntity SET id_entity = NULL;
  
INSERT INTO TmpEntity (id_entity) SELECT invoice_id FROM Invoice;
INSERT INTO Entity (entity_id) SELECT entity_id FROM TmpEntity WHERE id_entity IS NOT NULL;
INSERT INTO InvoiceEntity (invoiceentity_entity_id, invoiceentity_invoice_id) SELECT entity_id, id_entity FROM TmpEntity WHERE id_entity IS NOT NULL;
UPDATE TmpEntity SET id_entity = NULL;
  
INSERT INTO TmpEntity (id_entity) SELECT lead_id FROM Lead;
INSERT INTO Entity (entity_id) SELECT entity_id FROM TmpEntity WHERE id_entity IS NOT NULL;
INSERT INTO LeadEntity (leadentity_entity_id, leadentity_lead_id) SELECT entity_id, id_entity FROM TmpEntity WHERE id_entity IS NOT NULL;
UPDATE TmpEntity SET id_entity = NULL;
  
INSERT INTO TmpEntity (id_entity) SELECT list_id FROM List;
INSERT INTO Entity (entity_id) SELECT entity_id FROM TmpEntity WHERE id_entity IS NOT NULL;
INSERT INTO ListEntity (listentity_entity_id, listentity_list_id) SELECT entity_id, id_entity FROM TmpEntity WHERE id_entity IS NOT NULL;
UPDATE TmpEntity SET id_entity = NULL;
  
INSERT INTO TmpEntity (id_entity) SELECT mailshare_id FROM MailShare;
INSERT INTO Entity (entity_id) SELECT entity_id FROM TmpEntity WHERE id_entity IS NOT NULL;
INSERT INTO MailshareEntity (mailshareentity_entity_id, mailshareentity_mailshare_id) SELECT entity_id, id_entity FROM TmpEntity WHERE id_entity IS NOT NULL;
UPDATE TmpEntity SET id_entity = NULL;
  
INSERT INTO TmpEntity (id_entity) SELECT userobm_id FROM UserObm;
INSERT INTO Entity (entity_id) SELECT entity_id FROM TmpEntity WHERE id_entity IS NOT NULL;
INSERT INTO MailboxEntity (mailboxentity_entity_id, mailboxentity_mailbox_id) SELECT entity_id, id_entity FROM TmpEntity WHERE id_entity IS NOT NULL;
UPDATE TmpEntity SET id_entity = NULL;
  
INSERT INTO TmpEntity (id_entity) SELECT ogroup_id FROM OGroup;
INSERT INTO Entity (entity_id) SELECT entity_id FROM TmpEntity WHERE id_entity IS NOT NULL;
INSERT INTO OgroupEntity (ogroupentity_entity_id, ogroupentity_ogroup_id) SELECT entity_id, id_entity FROM TmpEntity WHERE id_entity IS NOT NULL;
UPDATE TmpEntity SET id_entity = NULL;
  
INSERT INTO TmpEntity (id_entity) SELECT obmbookmark_id FROM ObmBookmark;
INSERT INTO Entity (entity_id) SELECT entity_id FROM TmpEntity WHERE id_entity IS NOT NULL;
INSERT INTO ObmbookmarkEntity (obmbookmarkentity_entity_id, obmbookmarkentity_obmbookmark_id) SELECT entity_id, id_entity FROM TmpEntity WHERE id_entity IS NOT NULL;
UPDATE TmpEntity SET id_entity = NULL;
  
INSERT INTO TmpEntity (id_entity) SELECT organizationalchart_id FROM OrganizationalChart;
INSERT INTO Entity (entity_id) SELECT entity_id FROM TmpEntity WHERE id_entity IS NOT NULL;
INSERT INTO OrganizationalchartEntity (organizationalchartentity_entity_id, organizationalchartentity_organizationalchart_id) SELECT entity_id, id_entity FROM TmpEntity WHERE id_entity IS NOT NULL;
UPDATE TmpEntity SET id_entity = NULL;
  
INSERT INTO TmpEntity (id_entity) SELECT parentdeal_id FROM ParentDeal;
INSERT INTO Entity (entity_id) SELECT entity_id FROM TmpEntity WHERE id_entity IS NOT NULL;
INSERT INTO ParentdealEntity (parentdealentity_entity_id, parentdealentity_parentdeal_id) SELECT entity_id, id_entity FROM TmpEntity WHERE id_entity IS NOT NULL;
UPDATE TmpEntity SET id_entity = NULL;
  
INSERT INTO TmpEntity (id_entity) SELECT payment_id FROM Payment;
INSERT INTO Entity (entity_id) SELECT entity_id FROM TmpEntity WHERE id_entity IS NOT NULL;
INSERT INTO PaymentEntity (paymententity_entity_id, paymententity_payment_id) SELECT entity_id, id_entity FROM TmpEntity WHERE id_entity IS NOT NULL;
UPDATE TmpEntity SET id_entity = NULL;
  
INSERT INTO TmpEntity (id_entity) SELECT profile_id FROM Profile;
INSERT INTO Entity (entity_id) SELECT entity_id FROM TmpEntity WHERE id_entity IS NOT NULL;
INSERT INTO ProfileEntity (profileentity_entity_id, profileentity_profile_id) SELECT entity_id, id_entity FROM TmpEntity WHERE id_entity IS NOT NULL;
UPDATE TmpEntity SET id_entity = NULL;
  
INSERT INTO TmpEntity (id_entity) SELECT project_id FROM Project;
INSERT INTO Entity (entity_id) SELECT entity_id FROM TmpEntity WHERE id_entity IS NOT NULL;
INSERT INTO ProjectEntity (projectentity_entity_id, projectentity_project_id) SELECT entity_id, id_entity FROM TmpEntity WHERE id_entity IS NOT NULL;
UPDATE TmpEntity SET id_entity = NULL;
  
INSERT INTO TmpEntity (id_entity) SELECT publication_id FROM Publication;
INSERT INTO Entity (entity_id) SELECT entity_id FROM TmpEntity WHERE id_entity IS NOT NULL;
INSERT INTO PublicationEntity (publicationentity_entity_id, publicationentity_publication_id) SELECT entity_id, id_entity FROM TmpEntity WHERE id_entity IS NOT NULL;
UPDATE TmpEntity SET id_entity = NULL;
  
INSERT INTO TmpEntity (id_entity) SELECT rgroup_id FROM RGroup;
INSERT INTO Entity (entity_id) SELECT entity_id FROM TmpEntity WHERE id_entity IS NOT NULL;
INSERT INTO ResourcegroupEntity (resourcegroupentity_entity_id, resourcegroupentity_resourcegroup_id) SELECT entity_id, id_entity FROM TmpEntity WHERE id_entity IS NOT NULL;
UPDATE TmpEntity SET id_entity = NULL;
  
INSERT INTO TmpEntity (id_entity) SELECT resource_id FROM Resource;
INSERT INTO Entity (entity_id) SELECT entity_id FROM TmpEntity WHERE id_entity IS NOT NULL;
INSERT INTO ResourceEntity (resourceentity_entity_id, resourceentity_resource_id) SELECT entity_id, id_entity FROM TmpEntity WHERE id_entity IS NOT NULL;
UPDATE TmpEntity SET id_entity = NULL;
  
INSERT INTO TmpEntity (id_entity) SELECT subscription_id FROM Subscription;
INSERT INTO Entity (entity_id) SELECT entity_id FROM TmpEntity WHERE id_entity IS NOT NULL;
INSERT INTO SubscriptionEntity (subscriptionentity_entity_id, subscriptionentity_subscription_id) SELECT entity_id, id_entity FROM TmpEntity WHERE id_entity IS NOT NULL;
UPDATE TmpEntity SET id_entity = NULL;
  
INSERT INTO TmpEntity (id_entity) SELECT group_id FROM UGroup;
INSERT INTO Entity (entity_id) SELECT entity_id FROM TmpEntity WHERE id_entity IS NOT NULL;
INSERT INTO GroupEntity (groupentity_entity_id, groupentity_group_id) SELECT entity_id, id_entity FROM TmpEntity WHERE id_entity IS NOT NULL;
UPDATE TmpEntity SET id_entity = NULL;
  
INSERT INTO TmpEntity (id_entity) SELECT userobm_id FROM UserObm;
INSERT INTO Entity (entity_id) SELECT entity_id FROM TmpEntity WHERE id_entity IS NOT NULL;
INSERT INTO UserEntity (userentity_entity_id, userentity_user_id) SELECT entity_id, id_entity FROM TmpEntity WHERE id_entity IS NOT NULL;
UPDATE TmpEntity SET id_entity = NULL;
  
DELETE FROM EntityRight WHERE entityright_entity_id NOT IN (SELECT mailshare_id FROM MailShare) AND entityright_entity = 'mailshare';
UPDATE EntityRight SET entityright_entity_id = (SELECT mailshareentity_entity_id FROM MailshareEntity INNER JOIN MailShare ON mailshareentity_mailshare_id = mailshare_id WHERE mailshare_id = entityright_entity_id), entityright_entity = 'entity' WHERE entityright_entity = 'mailshare'; 
DELETE FROM EntityRight WHERE entityright_entity_id NOT IN (SELECT userobm_id FROM UserObm) AND entityright_entity = 'calendar';
UPDATE EntityRight SET entityright_entity_id = (SELECT calendarentity_entity_id FROM CalendarEntity INNER JOIN UserObm ON calendarentity_calendar_id = userobm_id WHERE userobm_id = entityright_entity_id), entityright_entity = 'entity' WHERE entityright_entity = 'calendar'; 
DELETE FROM EntityRight WHERE entityright_entity_id NOT IN (SELECT resource_id FROM Resource) AND entityright_entity = 'resource';
UPDATE EntityRight SET entityright_entity_id = (SELECT resourceentity_entity_id FROM ResourceEntity INNER JOIN Resource ON resourceentity_resource_id = resource_id WHERE resource_id = entityright_entity_id), entityright_entity = 'entity' WHERE entityright_entity = 'resource'; 
DELETE FROM EntityRight WHERE entityright_entity_id NOT IN (SELECT userobm_id FROM UserObm) AND entityright_entity = 'mailbox';
UPDATE EntityRight SET entityright_entity_id = (SELECT mailboxentity_entity_id FROM MailboxEntity INNER JOIN UserObm ON mailboxentity_mailbox_id = userobm_id WHERE userobm_id = entityright_entity_id), entityright_entity = 'entity' WHERE entityright_entity = 'mailbox'; 
UPDATE EntityRight SET entityright_consumer_id = NULL WHERE entityright_consumer_id = 0;
DELETE FROM EntityRight WHERE entityright_consumer_id NOT IN (SELECT userobm_id FROM UserObm) AND entityright_consumer = 'user' AND entityright_consumer_id IS NOT NULL;
UPDATE EntityRight SET entityright_consumer_id = (SELECT userentity_entity_id FROM UserEntity INNER JOIN UserObm ON userentity_user_id = userobm_id WHERE userobm_id = entityright_consumer_id), entityright_consumer = 'entity' WHERE entityright_consumer = 'user' AND entityright_consumer_id IS NOT NULL; 
DELETE FROM EntityRight WHERE entityright_consumer_id NOT IN (SELECT group_id FROM UGroup) AND entityright_consumer = 'group' AND entityright_consumer_id IS NOT NULL;
UPDATE EntityRight SET entityright_consumer_id = (SELECT groupentity_entity_id FROM GroupEntity INNER JOIN UGroup ON groupentity_group_id = group_id WHERE group_id = entityright_consumer_id), entityright_consumer = 'entity' WHERE entityright_consumer = 'group' AND entityright_consumer_id IS NOT NULL;

INSERT INTO EntityRight (entityright_entity_id, entityright_consumer_id) SELECT calendarentity_entity_id, NULL FROM CalendarEntity WHERE calendarentity_entity_id NOT IN (SELECT entityright_entity_id FROM EntityRight WHERE entityright_consumer_id IS NULL);
INSERT INTO EntityRight (entityright_entity_id, entityright_consumer_id) SELECT mailshareentity_entity_id, NULL FROM MailshareEntity WHERE mailshareentity_entity_id NOT IN (SELECT entityright_entity_id FROM EntityRight WHERE entityright_consumer_id IS NULL);
INSERT INTO EntityRight (entityright_entity_id, entityright_consumer_id) SELECT resourceentity_entity_id, NULL FROM ResourceEntity WHERE resourceentity_entity_id NOT IN (SELECT entityright_entity_id FROM EntityRight WHERE entityright_consumer_id IS NULL);
INSERT INTO EntityRight (entityright_entity_id, entityright_consumer_id) SELECT mailboxentity_entity_id, NULL FROM MailboxEntity WHERE mailboxentity_entity_id NOT IN (SELECT entityright_entity_id FROM EntityRight WHERE entityright_consumer_id IS NULL);
UPDATE EntityRight SET entityright_access = 1 WHERE entityright_consumer_id IS NULL;


DELETE FROM EntityRight WHERE entityright_entity != 'entity';
ALTER TABLE EntityRight DROP COLUMN entityright_entity;
ALTER TABLE EntityRight DROP COLUMN entityright_consumer;


DELETE FROM DocumentLink WHERE documentlink_entity_id NOT IN (SELECT contact_id FROM Contact) AND documentlink_entity = 'contact';
UPDATE DocumentLink SET documentlink_entity_id = (SELECT contactentity_entity_id FROM ContactEntity INNER JOIN Contact ON contactentity_contact_id = contact_id WHERE contact_id = documentlink_entity_id), documentlink_entity = 'entity' WHERE documentlink_entity = 'contact';
DELETE FROM DocumentLink WHERE documentlink_entity_id NOT IN (SELECT invoice_id FROM Invoice) AND documentlink_entity = 'invoice';
UPDATE DocumentLink SET documentlink_entity_id = (SELECT invoiceentity_entity_id FROM InvoiceEntity INNER JOIN Invoice ON invoiceentity_invoice_id = invoice_id WHERE invoice_id = documentlink_entity_id), documentlink_entity = 'entity' WHERE documentlink_entity = 'invoice';
DELETE FROM DocumentLink WHERE documentlink_entity_id NOT IN (SELECT deal_id FROM Deal) AND documentlink_entity = 'deal';
UPDATE DocumentLink SET documentlink_entity_id = (SELECT dealentity_entity_id FROM DealEntity INNER JOIN Deal ON dealentity_deal_id = deal_id WHERE deal_id = documentlink_entity_id), documentlink_entity = 'entity' WHERE documentlink_entity = 'deal';
DELETE FROM DocumentLink WHERE documentlink_entity_id NOT IN (SELECT project_id FROM Project) AND documentlink_entity = 'project';
UPDATE DocumentLink SET documentlink_entity_id = (SELECT projectentity_entity_id FROM ProjectEntity INNER JOIN Project ON projectentity_project_id = project_id WHERE project_id = documentlink_entity_id), documentlink_entity = 'entity' WHERE documentlink_entity = 'project';
DELETE FROM DocumentLink WHERE documentlink_entity_id NOT IN (SELECT company_id FROM Company) AND documentlink_entity = 'company';
UPDATE DocumentLink SET documentlink_entity_id = (SELECT companyentity_entity_id FROM CompanyEntity INNER JOIN Company ON companyentity_company_id = company_id WHERE company_id = documentlink_entity_id), documentlink_entity = 'entity' WHERE documentlink_entity = 'company';
DELETE FROM DocumentLink WHERE documentlink_entity_id NOT IN (SELECT contract_id FROM Contract) AND documentlink_entity = 'contract';
UPDATE DocumentLink SET documentlink_entity_id = (SELECT contractentity_entity_id FROM ContractEntity INNER JOIN Contract ON contractentity_contract_id = contract_id WHERE contract_id = documentlink_entity_id), documentlink_entity = 'entity' WHERE documentlink_entity = 'contract';
DELETE FROM DocumentLink WHERE documentlink_entity_id NOT IN (SELECT incident_id FROM Incident) AND documentlink_entity = 'incident';
UPDATE DocumentLink SET documentlink_entity_id = (SELECT incidententity_entity_id FROM IncidentEntity INNER JOIN Incident ON incidententity_incident_id = incident_id WHERE incident_id = documentlink_entity_id), documentlink_entity = 'entity' WHERE documentlink_entity = 'incident';
ALTER TABLE DocumentLink DROP COLUMN documentlink_entity;

DELETE FROM CategoryLink WHERE categorylink_entity_id NOT IN (SELECT contact_id FROM Contact) AND categorylink_entity = 'contact';
UPDATE CategoryLink SET categorylink_entity_id = (SELECT contactentity_entity_id FROM ContactEntity INNER JOIN Contact ON contactentity_contact_id = contact_id WHERE contact_id = categorylink_entity_id), categorylink_entity = 'entity' WHERE categorylink_entity = 'contact';
DELETE FROM CategoryLink WHERE categorylink_entity_id NOT IN (SELECT company_id FROM Company) AND categorylink_entity = 'company';
UPDATE CategoryLink SET categorylink_entity_id = (SELECT companyentity_entity_id FROM CompanyEntity INNER JOIN Company ON companyentity_company_id = company_id WHERE company_id = categorylink_entity_id), categorylink_entity = 'entity' WHERE categorylink_entity = 'company';
DELETE FROM CategoryLink WHERE categorylink_entity_id NOT IN (SELECT document_id FROM Document) AND categorylink_entity = 'document';
UPDATE CategoryLink SET categorylink_entity_id = (SELECT documententity_entity_id FROM DocumentEntity INNER JOIN Document ON documententity_document_id = document_id WHERE document_id = categorylink_entity_id), categorylink_entity = 'entity' WHERE categorylink_entity = 'document';
DELETE FROM CategoryLink WHERE categorylink_entity_id NOT IN (SELECT incident_id FROM Incident) AND categorylink_entity = 'incident';
UPDATE CategoryLink SET categorylink_entity_id = (SELECT incidententity_entity_id FROM IncidentEntity INNER JOIN Incident ON incidententity_incident_id = incident_id WHERE incident_id = categorylink_entity_id), categorylink_entity = 'entity' WHERE categorylink_entity = 'incident';
DELETE FROM CategoryLink WHERE categorylink_entity_id NOT IN (SELECT userobm_id FROM UserObm) AND categorylink_entity = 'user';
UPDATE CategoryLink SET categorylink_entity_id = (SELECT userentity_entity_id FROM UserEntity INNER JOIN UserObm ON userentity_user_id = userobm_id WHERE userobm_id = categorylink_entity_id), categorylink_entity = 'entity' WHERE categorylink_entity = 'user';
DELETE FROM CategoryLink WHERE categorylink_entity_id NOT IN (SELECT group_id FROM UGroup) AND categorylink_entity = 'group';
UPDATE CategoryLink SET categorylink_entity_id = (SELECT groupentity_entity_id FROM GroupEntity INNER JOIN UGroup ON groupentity_group_id = group_id WHERE group_id = categorylink_entity_id), categorylink_entity = 'entity' WHERE categorylink_entity = 'group';
DELETE FROM CategoryLink WHERE categorylink_entity_id NOT IN (SELECT deal_id FROM Deal) AND categorylink_entity = 'deal';
UPDATE CategoryLink SET categorylink_entity_id = (SELECT dealentity_entity_id FROM DealEntity INNER JOIN Deal ON dealentity_deal_id = deal_id WHERE deal_id = categorylink_entity_id), categorylink_entity = 'entity' WHERE categorylink_entity = 'deal';
ALTER TABLE CategoryLink DROP COLUMN categorylink_entity;

DELETE FROM OGroupLink WHERE ogrouplink_entity_id NOT IN (SELECT group_id FROM UGroup) AND ogrouplink_entity = 'group';
UPDATE OGroupLink SET ogrouplink_entity_id = (SELECT groupentity_entity_id FROM GroupEntity INNER JOIN UGroup ON groupentity_group_id = group_id WHERE group_id = ogrouplink_entity_id), ogrouplink_entity = 'entity' WHERE ogrouplink_entity = 'group';
DELETE FROM OGroupLink WHERE ogrouplink_entity_id NOT IN (SELECT userobm_id FROM UserObm) AND ogrouplink_entity = 'user';
UPDATE OGroupLink SET ogrouplink_entity_id = (SELECT userentity_entity_id FROM UserEntity INNER JOIN UserObm ON userentity_user_id = userobm_id WHERE userobm_id = ogrouplink_entity_id), ogrouplink_entity = 'entity' WHERE ogrouplink_entity = 'user';
ALTER TABLE OGroupLink DROP COLUMN ogrouplink_entity;

DELETE FROM EventLink WHERE eventlink_entity_id NOT IN (SELECT userobm_id FROM UserObm) AND eventlink_entity = 'user';
UPDATE EventLink SET eventlink_entity_id = (SELECT userentity_entity_id FROM UserEntity INNER JOIN UserObm ON userentity_user_id = userobm_id WHERE userobm_id = eventlink_entity_id), eventlink_entity = 'entity' WHERE eventlink_entity = 'user';
DELETE FROM EventLink WHERE eventlink_entity_id NOT IN (SELECT resource_id FROM Resource) AND eventlink_entity = 'resource';
UPDATE EventLink SET eventlink_entity_id = (SELECT resourceentity_entity_id FROM ResourceEntity INNER JOIN Resource ON resourceentity_resource_id = resource_id WHERE resource_id = eventlink_entity_id), eventlink_entity = 'entity' WHERE eventlink_entity = 'resource';
DELETE FROM EventLink WHERE eventlink_entity_id NOT IN (SELECT projecttask_id FROM ProjectTask) AND eventlink_entity = 'task';
INSERT INTO TaskEvent (taskevent_task_id, taskevent_event_id) SELECT eventlink_entity_id, eventlink_event_id FROM EventLink WHERE eventlink_entity = 'task';

DELETE FROM EventLink where eventlink_entity != 'entity';
ALTER TABLE EventLink DROP COLUMN eventlink_entity;

--  _______________________________________________________
-- |Migrating Address  information from Contact and Company|
--  --------------------------------------------------------
INSERT INTO Address (address_entity_id, address_street, address_zipcode, address_town, address_expresspostal, address_country, address_label)
SELECT contactentity_entity_id, CONCAT(contact_address1,'\n',contact_address2,'\n',contact_address3), contact_zipcode, contact_town, contact_expresspostal, contact_country_iso3166, 'WORK; X-OBM-REF1' FROM Contact INNER JOIN ContactEntity ON contactentity_contact_id = contact_id WHERE (contact_address1 != '' AND contact_address1 IS NOT NULL) OR (contact_address2 !='' AND contact_address2 IS NOT NULL) OR (contact_address3 !='' AND contact_address3 IS NOT NULL) OR (contact_zipcode !='' AND contact_zipcode IS NOT NULL) OR (contact_town !='' AND contact_town IS NOT NULL) OR (contact_expresspostal !='' AND contact_expresspostal IS NOT NULL) OR (contact_country_iso3166 !='0' AND contact_country_iso3166 !='' AND contact_country_iso3166 IS NOT NULL)
UNION
SELECT companyentity_entity_id, CONCAT(company_address1,'\n', company_address2,'\n', company_address3), company_zipcode, company_town, company_expresspostal, company_country_iso3166, 'WORK; X-OBM-REF1' FROM Company INNER JOIN CompanyEntity ON companyentity_company_id = company_id WHERE (company_address1 !='' AND company_address1 IS NOT NULL) OR (company_address2 !='' AND company_address2 IS NOT NULL) OR (company_address3 !='' AND company_address3 IS NOT NULL) OR (company_zipcode !='' AND company_zipcode IS NOT NULL) OR (company_town !='' AND company_town IS NOT NULL) OR (company_expresspostal !='' AND company_expresspostal IS NOT NULL) OR (company_country_iso3166 !='0' AND company_country_iso3166 !='' AND company_country_iso3166 IS NOT NULL);

INSERT INTO Phone (phone_entity_id, phone_number, phone_label)
SELECT contactentity_entity_id, contact_phone, 'WORK; X-OBM-REF1' FROM Contact INNER JOIN ContactEntity ON contactentity_contact_id = contact_id WHERE contact_phone != '' AND contact_phone IS NOT NULL
UNION
SELECT contactentity_entity_id, contact_homephone, 'HOME; X-OBM-REF1' FROM Contact INNER JOIN ContactEntity ON contactentity_contact_id = contact_id WHERE contact_homephone != '' AND contact_homephone IS NOT NULL
UNION
SELECT contactentity_entity_id, contact_mobilephone, 'CELL; X-OBM-REF1' FROM Contact INNER JOIN ContactEntity ON contactentity_contact_id = contact_id WHERE contact_mobilephone != '' AND contact_mobilephone IS NOT NULL
UNION
SELECT contactentity_entity_id, contact_fax, 'FAX; X-OBM-REF1' FROM Contact INNER JOIN ContactEntity ON contactentity_contact_id = contact_id WHERE contact_fax != '' AND contact_fax IS NOT NULL
UNION
SELECT companyentity_entity_id, company_phone, 'WORK; X-OBM-REF1' FROM Company INNER JOIN CompanyEntity ON companyentity_company_id = company_id WHERE company_phone != '' AND company_phone IS NOT NULL
UNION
SELECT companyentity_entity_id, company_fax, 'FAX; X-OBM-REF1' FROM Company INNER JOIN CompanyEntity ON companyentity_company_id = company_id WHERE company_fax != '' AND company_fax IS NOT NULL;

INSERT INTO Email (email_entity_id, email_address, email_label) 
SELECT contactentity_entity_id, contact_email, 'WORK; X-OBM-REF1' FROM Contact INNER JOIN ContactEntity ON contactentity_contact_id = contact_id WHERE contact_email != '' AND contact_email IS NOT NULL
UNION
SELECT contactentity_entity_id, contact_email2, 'WORK; X-OBM-REF2' FROM Contact INNER JOIN ContactEntity ON contactentity_contact_id = contact_id WHERE contact_email2 != '' AND contact_email2 IS NOT NULL
UNION
SELECT companyentity_entity_id, company_email, 'WORK; X-OBM-REF1' FROM Company INNER JOIN CompanyEntity ON companyentity_company_id = company_id WHERE company_email != '' AND company_email IS NOT NULL;

INSERT INTO Website (website_entity_id, website_url, website_label) 
SELECT companyentity_entity_id, company_web, 'WORK; X-OBM-REF1' FROM Company INNER JOIN CompanyEntity ON companyentity_company_id = company_id WHERE company_web != '' AND company_web IS NOT NULL;

ALTER TABLE Contact DROP COLUMN contact_address1;
ALTER TABLE Contact DROP COLUMN contact_address2;
ALTER TABLE Contact DROP COLUMN contact_address3;
ALTER TABLE Contact DROP COLUMN contact_zipcode;
ALTER TABLE Contact DROP COLUMN contact_town;
ALTER TABLE Contact DROP COLUMN contact_expresspostal;
ALTER TABLE Contact DROP COLUMN contact_country_iso3166;
ALTER TABLE Contact DROP COLUMN contact_phone;
ALTER TABLE Contact DROP COLUMN contact_homephone;
ALTER TABLE Contact DROP COLUMN contact_mobilephone;
ALTER TABLE Contact DROP COLUMN contact_fax;
ALTER TABLE Contact DROP COLUMN contact_email;
ALTER TABLE Contact DROP COLUMN contact_email2;

ALTER TABLE Company DROP COLUMN company_address1;
ALTER TABLE Company DROP COLUMN company_address2;
ALTER TABLE Company DROP COLUMN company_address3;
ALTER TABLE Company DROP COLUMN company_zipcode;
ALTER TABLE Company DROP COLUMN company_town;
ALTER TABLE Company DROP COLUMN company_expresspostal;
ALTER TABLE Company DROP COLUMN company_country_iso3166;
ALTER TABLE Company DROP COLUMN company_phone;
ALTER TABLE Company DROP COLUMN company_fax;
ALTER TABLE Company DROP COLUMN company_web;
ALTER TABLE Company DROP COLUMN company_email;

-- -----------------------------------
-- Migrating database for service model
-- -----------------------------------
INSERT INTO Service (service_service, service_entity_id) 
SELECT 'samba', hostentity_entity_id FROM Host INNER JOIN HostEntity ON hostentity_host_id = host_id WHERE host_samba = 1;

INSERT INTO Service (service_service, service_entity_id) 
SELECT 'smtp_in', hostentity_entity_id FROM MailServer INNER JOIN HostEntity ON hostentity_host_id = mailserver_host_id WHERE mailserver_smtp_in = 1;

INSERT INTO Service (service_service, service_entity_id) 
SELECT 'smtp_out', hostentity_entity_id FROM MailServer INNER JOIN HostEntity ON hostentity_host_id = mailserver_host_id WHERE mailserver_smtp_out = 1;

INSERT INTO Service (service_service, service_entity_id) 
SELECT 'imap', hostentity_entity_id FROM MailServer INNER JOIN HostEntity ON hostentity_host_id = mailserver_host_id WHERE mailserver_imap = 1;

INSERT INTO Service (service_service, service_entity_id)
SELECT 'mail', domainentity_entity_id FROM Domain 
INNER JOIN DomainEntity ON domainentity_domain_id = domain_id
INNER JOIN DomainMailServer ON domainmailserver_domain_id = domain_id
GROUP BY domainentity_entity_id;


INSERT INTO Service (service_service, service_entity_id)
SELECT 'samba', domainentity_entity_id FROM Domain 
INNER JOIN DomainEntity ON domainentity_domain_id = domain_id
INNER JOIN Samba ON samba_domain_id = domain_id
GROUP BY domainentity_entity_id;

UPDATE UserObm SET userobm_mail_server_id = (SELECT host_id FROM MailServer LEFT JOIN Host ON mailserver_host_id = host_id WHERE mailserver_id = userobm_mail_server_id);

UPDATE MailShare SET mailshare_mail_server_id = (SELECT host_id FROM MailServer LEFT JOIN Host ON mailserver_host_id = host_id WHERE mailserver_id = mailshare_mail_server_id);

INSERT INTO ServiceProperty (serviceproperty_property, serviceproperty_service, serviceproperty_entity_id, serviceproperty_value) 
SELECT 'imap', 'mail', domainentity_entity_id, mailserver_host_id
FROM Domain 
INNER JOIN DomainEntity ON domain_id = domainentity_domain_id 
INNER JOIN DomainMailServer ON domainmailserver_domain_id = domain_id
INNER JOIN MailServer ON domainmailserver_mailserver_id = mailserver_id
WHERE domainmailserver_role = 'imap';

INSERT INTO ServiceProperty (serviceproperty_property, serviceproperty_service, serviceproperty_entity_id, serviceproperty_value) 
SELECT 'smtp_in', 'mail', domainentity_entity_id, mailserver_host_id
FROM Domain 
INNER JOIN DomainEntity ON domain_id = domainentity_domain_id 
INNER JOIN DomainMailServer ON domainmailserver_domain_id = domain_id
INNER JOIN MailServer ON domainmailserver_mailserver_id = mailserver_id
WHERE domainmailserver_role = 'smtp_in';

INSERT INTO ServiceProperty (serviceproperty_property, serviceproperty_service, serviceproperty_entity_id, serviceproperty_value) 
SELECT 'smtp_out', 'mail', domainentity_entity_id, mailserver_host_id
FROM Domain 
INNER JOIN DomainEntity ON domain_id = domainentity_domain_id 
INNER JOIN DomainMailServer ON domainmailserver_domain_id = domain_id
INNER JOIN MailServer ON domainmailserver_mailserver_id = mailserver_id
WHERE domainmailserver_role = 'smtp_out';

INSERT INTO ServiceProperty (serviceproperty_property, serviceproperty_service, serviceproperty_entity_id, serviceproperty_value) 
SELECT 'sid', 'samba', domainentity_entity_id, samba_value 
FROM Domain 
INNER JOIN DomainEntity ON domain_id = domainentity_domain_id 
INNER JOIN Samba ON samba_domain_id = domain_id
WHERE samba_name = 'samba_sid';

INSERT INTO ServiceProperty (serviceproperty_property, serviceproperty_service, serviceproperty_entity_id, serviceproperty_value) 
SELECT 'domain', 'samba', domainentity_entity_id, samba_value 
FROM Domain 
INNER JOIN DomainEntity ON domain_id = domainentity_domain_id 
INNER JOIN Samba ON samba_domain_id = domain_id
WHERE samba_name = 'samba_domain';

INSERT INTO ServiceProperty (serviceproperty_property, serviceproperty_service, serviceproperty_entity_id, serviceproperty_value) 
SELECT 'profile', 'samba', domainentity_entity_id, samba_value 
FROM Domain 
INNER JOIN DomainEntity ON domain_id = domainentity_domain_id 
INNER JOIN Samba ON samba_domain_id = domain_id
WHERE samba_name = 'samba_profile';

INSERT INTO ServiceProperty (serviceproperty_property, serviceproperty_service, serviceproperty_entity_id, serviceproperty_value) 
SELECT 'home', 'samba', domainentity_entity_id, samba_value 
FROM Domain 
INNER JOIN DomainEntity ON domain_id = domainentity_domain_id 
INNER JOIN Samba ON samba_domain_id = domain_id
WHERE samba_name = 'samba_home_def';

INSERT INTO ServiceProperty (serviceproperty_property, serviceproperty_service, serviceproperty_entity_id, serviceproperty_value) 
SELECT 'drive', 'samba', domainentity_entity_id, samba_value 
FROM Domain 
INNER JOIN DomainEntity ON domain_id = domainentity_domain_id 
INNER JOIN Samba ON samba_domain_id = domain_id
WHERE samba_name = 'home_drive';

ALTER TABLE Host DROP COLUMN host_web_perms;
ALTER TABLE Host DROP COLUMN host_web_list;
ALTER TABLE Host DROP COLUMN host_web_all;
ALTER TABLE Host DROP COLUMN host_ftp_perms;
ALTER TABLE Host DROP COLUMN host_firewall_perms;
ALTER TABLE Host DROP COLUMN host_samba;

DROP TABLE Samba;
DROP TABLE DomainMailServer;
DROP TABLE MailServer;
DROP TABLE MailServerNetwork;

-- -----------------------------------------
-- Updates that need to be after Entity work
-- -----------------------------------------

-- Migration of group_mailing to a category
INSERT INTO Category (
  category_domain_id,
  category_category,
  category_code,
  category_label)
SELECT domain_id,
  'groupcategory',
  '1',
  'external address'
FROM Domain
WHERE domain_global is not true;
  
INSERT INTO CategoryLink (
  categorylink_category_id,
  categorylink_entity_id,
  categorylink_category)
SELECT category_id,
  groupentity_entity_id,
  'groupcategory'
FROM Category, GroupEntity
LEFT JOIN UGroup ON groupentity_group_id=group_id
WHERE category_category='groupcategory' AND category_label='external address'
  AND group_mailing = 1;

ALTER TABLE UGroup DROP COLUMN group_mailing;


-- Create links from "todos" to users (need to be after userentity..)
INSERT INTO EventLink (
  eventlink_timeupdate,
  eventlink_timecreate,
  eventlink_userupdate,
  eventlink_usercreate,
  eventlink_event_id,
  eventlink_entity_id,
  eventlink_state,
  eventlink_required,
  eventlink_percent)
SELECT
  todo_timeupdate,
  todo_timecreate,
  todo_userupdate,
  todo_usercreate,
  event_id,
  userentity_entity_id,
  'ACCEPTED',
  'REQ',
  todo_percent
FROM Todo
LEFT JOIN Event on todo_usercreate=event_usercreate and todo_timecreate=event_timecreate and todo_timeupdate=event_timeupdate and todo_user=event_owner and todo_title=event_title
LEFT JOIN UserEntity on todo_user=userentity_user_id;


-- ------------------------------
-- Prepare value for foreign keys
-- ------------------------------

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

-- Foreign key from documentlink_document_id to document_id
DELETE FROM DocumentLink WHERE documentlink_document_id NOT IN (SELECT document_id FROM Document) AND documentlink_document_id IS NOT NULL;

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

-- Foreign key from mailshare_domain_id to domain_id
DELETE FROM MailShare WHERE mailshare_domain_id NOT IN (SELECT domain_id FROM Domain) AND mailshare_domain_id IS NOT NULL;

-- Foreign key from mailshare_mail_server_id to mailserver_id
-- DELETE FROM MailShare WHERE mailshare_mail_server_id NOT IN (SELECT mailserver_id FROM MailServer) AND mailshare_mail_server_id IS NOT NULL;

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

-- Foreign key from ogrouplink_ogroup_id to ogroup_id
DELETE FROM OGroupLink WHERE ogrouplink_ogroup_id NOT IN (SELECT ogroup_id FROM OGroup) AND ogrouplink_ogroup_id IS NOT NULL;

-- Foreign key from ogrouplink_domain_id to domain_id
DELETE FROM OGroupLink WHERE ogrouplink_domain_id NOT IN (SELECT domain_id FROM Domain) AND ogrouplink_domain_id IS NOT NULL;

-- Foreign key from ogrouplink_userupdate to userobm_id
UPDATE OGroupLink SET ogrouplink_userupdate = NULL WHERE ogrouplink_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND ogrouplink_userupdate IS NOT NULL;

-- Foreign key from ogrouplink_usercreate to userobm_id
UPDATE OGroupLink SET ogrouplink_usercreate = NULL WHERE ogrouplink_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND ogrouplink_usercreate IS NOT NULL;

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

--  _________________
-- | Drop old tables |
--  ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
DROP TABLE IF EXISTS OBMTicket;
DROP TABLE DeletedCalendarEvent;
DROP TABLE CalendarAlert;
DROP TABLE CalendarException;
DROP TABLE CalendarEvent;
DROP TABLE CalendarCategory1;
DROP TABLE Todo;
DROP TABLE DeletedTodo;
DROP TABLE TmpEntity;


-- Campaign Module


--
-- Table structure for table `Campaign`
--

CREATE TABLE `Campaign` (
  `campaign_id` int(8) NOT NULL auto_increment,
  `campaign_name` varchar(50) default NULL,
  `campaign_timeupdate` timestamp NOT NULL default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP,
  `campaign_timecreate` timestamp NOT NULL default '0000-00-00 00:00:00',
  `campaign_userupdate` int(8) default NULL,
  `campaign_usercreate` int(8) default NULL,
  `campaign_manager_id` int(8) default NULL,
  `campaign_tracker_key` int(11) default NULL,
  `campaign_refer_url` varchar(255) default NULL,
  `campaign_nb_sent` int(10) default NULL,
  `campaign_nb_error` int(10) default NULL,
  `campaign_nb_inqueue` int(10) default NULL,
  `campaign_progress` int(3) default NULL,
  `campaign_start_date` date default NULL,
  `campaign_end_date` date default NULL,
  `campaign_status` int(3) default NULL,
  `campaign_type` int(2) default NULL,
  `campaign_objective` text default NULL,
  `campaign_comment` text default NULL,
  `campaign_domain_id` int(8) NOT NULL,
  `campaign_email` int(8) default NULL,
  `campaign_parent` int(8) default NULL,
  `campaign_child_order` int(2) default NULL,
  KEY `campaign_parent_fkey` (`campaign_parent`),
  PRIMARY KEY (`campaign_id`)
);

--
-- Table structure for table `CampaignEntity`
--

CREATE TABLE `CampaignEntity` (
  `campaignentity_entity_id` int(8) NOT NULL,
  `campaignentity_campaign_id` int(8) NOT NULL,
  PRIMARY KEY  (`campaignentity_entity_id`,`campaignentity_campaign_id`),
  KEY `campaignentity_campaign_id_campaign_id_fkey` (`campaignentity_campaign_id`)
);

--
-- Table structure for table `CampaignDisabledEntity`
--

CREATE TABLE `CampaignDisabledEntity` (
  `campaigndisabledentity_entity_id` int(8) NOT NULL,
  `campaigndisabledentity_campaign_id` int(8) NOT NULL,
  PRIMARY KEY  (`campaigndisabledentity_entity_id`,`campaigndisabledentity_campaign_id`),
  KEY `campaigndisabledentity_campaign_id_campaign_id_fkey` (`campaigndisabledentity_campaign_id`)
);

--
-- Table structure for table `CampaignTarget`
--

CREATE TABLE `CampaignTarget` (
  `campaigntarget_id` int(8) NOT NULL auto_increment,
  `campaigntarget_campaign_id` int(8) NOT NULL,
  `campaigntarget_entity_id` int(8),
  `campaigntarget_status` int(8) NULL,
  PRIMARY KEY (`campaigntarget_id`),
  KEY `campaigntarget_campaign_id_campaign_id_fkey` (`campaigntarget_campaign_id`)
);

--
-- Table structure for table `CampaignMailTarget`
--

CREATE TABLE `CampaignMailTarget` (
  `campaignmailtarget_id` int(8) NOT NULL auto_increment,
  `campaignmailtarget_campaign_id` int(8) NOT NULL,
  `campaignmailtarget_entity_id` int(8),
  `campaignmailtarget_status` int(8) NULL,
  PRIMARY KEY (`campaignmailtarget_id`),
  KEY `campaignmailtarget_campaign_id_campaign_id_fkey` (`campaignmailtarget_campaign_id`)
);

--
-- Table structure for table `CampaignMailContent`
--

CREATE TABLE `CampaignMailContent` (
  `campaignmailcontent_id`         INT(8) NOT NULL AUTO_INCREMENT,
  `campaignmailcontent_refext_id`  VARCHAR(8),
  `campaignmailcontent_content`    BLOB,
  PRIMARY KEY (`campaignmailcontent_id`)
);

--
-- Table structure for table `CampaignPushTarget`
--

CREATE TABLE `CampaignPushTarget` (
  `campaignpushtarget_id`             INT(8) NOT NULL AUTO_INCREMENT,
  `campaignpushtarget_mailcontent_id` INT(8) NOT NULL,
  `campaignpushtarget_refext_id`      VARCHAR(8),

  `campaignpushtarget_status`         INT(2) NOT NULL DEFAULT '1',
  -- 1 : not sent
  -- 2 : sent
  -- 3 : error occurred
  
  `campaignpushtarget_email_address`  VARCHAR(512) NOT NULL,
  `campaignpushtarget_properties`     TEXT,
  `campaignpushtarget_start_time`     DATETIME,
  `campaignpushtarget_sent_time`      DATETIME,
  `campaignpushtarget_retries`        INT(3),
  PRIMARY KEY (`campaignpushtarget_id`)
);

--
--
--
DROP TABLE P_Samba;
DROP TABLE P_MailServer;
DROP TABLE P_MailServerNetwork;

--
-- Table structure for table `P_Domain`
--

DROP TABLE IF EXISTS `P_Domain`;
CREATE TABLE `P_Domain` (LIKE `Domain`);
INSERT INTO P_Domain SELECT * FROM Domain;


--
-- Table structure for table `P_DomainEntity`
--

DROP TABLE IF EXISTS `P_DomainEntity`;
CREATE TABLE `P_DomainEntity` (LIKE `DomainEntity`);
INSERT INTO P_DomainEntity SELECT * FROM DomainEntity;


--
-- Table structure for table `P_EntityRight`
--

DROP TABLE IF EXISTS `P_EntityRight`;
CREATE TABLE `P_EntityRight` (LIKE `EntityRight`);
INSERT INTO P_EntityRight SELECT * FROM EntityRight;
 

--
-- Table structure for table `P_GroupEntity`
--

DROP TABLE IF EXISTS `P_GroupEntity`;
CREATE TABLE `P_GroupEntity` (LIKE `GroupEntity`);
INSERT INTO P_GroupEntity SELECT * FROM GroupEntity;


--
-- Table structure for table `P_GroupGroup`
--

DROP TABLE IF EXISTS `P_GroupGroup`;
CREATE TABLE `P_GroupGroup` (LIKE `GroupGroup`);
INSERT INTO P_GroupGroup SELECT * FROM GroupGroup;


--
-- Table structure for table `P_Host`
--

DROP TABLE IF EXISTS `P_Host`;
CREATE TABLE `P_Host` (LIKE `Host`);
INSERT INTO P_Host SELECT * FROM Host;

--
-- Table structure for table `P_HostEntity`
--

DROP TABLE IF EXISTS `P_HostEntity`;
CREATE TABLE `P_HostEntity` (LIKE `HostEntity`);
INSERT INTO P_HostEntity SELECT * FROM HostEntity;


--
-- Table structure for table `P_MailShare`
--

DROP TABLE IF EXISTS `P_MailShare`;
CREATE TABLE `P_MailShare` (LIKE `MailShare`);
INSERT INTO P_MailShare SELECT * FROM MailShare;


--
-- Table structure for table `P_MailshareEntity`
--

DROP TABLE IF EXISTS `P_MailshareEntity`;
CREATE TABLE `P_MailshareEntity` (LIKE `MailshareEntity`);
INSERT INTO P_MailshareEntity SELECT * FROM MailshareEntity;


--
-- Table structure for table `P_MailboxEntity`
--

DROP TABLE IF EXISTS `P_MailboxEntity`;
CREATE TABLE `P_MailboxEntity` (LIKE `MailboxEntity`);
INSERT INTO P_MailboxEntity SELECT * FROM MailboxEntity;


--
-- Table structure for table `P_Service`
--

DROP TABLE IF EXISTS `P_Service`;
CREATE TABLE `P_Service` (LIKE `Service`);
INSERT INTO P_Service SELECT * FROM Service;


--
-- Table structure for table `P_ServiceProperty`
--

DROP TABLE IF EXISTS `P_ServiceProperty`;
CREATE TABLE `P_ServiceProperty` (LIKE `ServiceProperty`);
INSERT INTO P_ServiceProperty SELECT * FROM ServiceProperty;


--
-- Table structure for table `P_UGroup`
--

DROP TABLE IF EXISTS `P_UGroup`;
CREATE TABLE `P_UGroup` (LIKE `UGroup`);
INSERT INTO P_UGroup SELECT * FROM UGroup;


--
-- Table structure for table `P_UserObm`
--

DROP TABLE IF EXISTS `P_UserEntity`;
CREATE TABLE `P_UserEntity` (LIKE `UserEntity`);
INSERT INTO P_UserEntity SELECT * FROM UserEntity;


--
-- Table structure for table `P_UserObm`
--

DROP TABLE IF EXISTS `P_UserObm`;
CREATE TABLE `P_UserObm` (LIKE `UserObm`);
INSERT INTO P_UserObm SELECT * FROM UserObm;


--
-- Table structure for table `P_UserObmGroup`
--

DROP TABLE IF EXISTS `P_UserObmGroup`;


--
-- Table structure for table `P_of_usergroup`
--

DROP TABLE IF EXISTS `P_of_usergroup`;
CREATE TABLE `P_of_usergroup` (LIKE `of_usergroup`);
INSERT INTO P_of_usergroup SELECT * FROM of_usergroup;



-- Write that the 2.1->2.2 is completed
UPDATE ObmInfo SET obminfo_value='2.2.0' WHERE obminfo_name='db_version';
