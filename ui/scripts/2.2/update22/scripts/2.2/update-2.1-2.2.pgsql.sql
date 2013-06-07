-- Write that the 2.1->2.2 has started
UPDATE ObmInfo SET obminfo_value='2.1->2.2' WHERE obminfo_name='db_version';


--  _________________
-- | Tables creation |
--  ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯


--
-- Table structure for table 'Entity'
--
CREATE TABLE Entity (
  entity_id serial,
  entity_mailing boolean,
  PRIMARY KEY (entity_id)
);

---
--- Address
---
CREATE TABLE Address (
  address_id                                    serial,
  address_entity_id                             integer NOT NULL,
  address_street                                text,
  address_zipcode                               varchar(14),
  address_town                                  varchar(128),
  address_expresspostal                         varchar(16),
  address_state                         	varchar(128),
  address_country                               char(2),
  address_label                                 varchar(255),
  PRIMARY KEY (address_id),
  CONSTRAINT address_entity_id_entity_id_fkey FOREIGN KEY (address_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE
);

---
--- Phone
---
CREATE TABLE Phone (
  phone_id                                      serial,
  phone_entity_id                               integer NOT NULL,
  phone_label                                   varchar(255) NOT NULL,
  phone_number                                  varchar(32),
  PRIMARY KEY (phone_id),
  CONSTRAINT phone_entity_id_entity_id_fkey FOREIGN KEY (phone_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE
);

---
--- Website
---
CREATE TABLE Website (
  website_id                                    serial,
  website_entity_id                             integer NOT NULL,
  website_label                                 varchar(255) NOT NULL,
  website_url                                   text,
  PRIMARY KEY (website_id),
  CONSTRAINT website_entity_id_entity_id_fkey FOREIGN KEY (website_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE
);

---
--- Email
---
CREATE TABLE Email (
  email_id                                      serial,
  email_entity_id                               integer NOT NULL,
  email_label                                   varchar(255) NOT NULL,
  email_address                                 varchar(255),
  PRIMARY KEY (email_id),
  CONSTRAINT email_entity_id_entity_id_fkey FOREIGN KEY (email_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE
);

---
--- IM
---
CREATE TABLE IM (
  im_id                                         serial,
  im_entity_id                                  integer NOT NULL,
  im_label                                      varchar(255),
  im_address                                    varchar(255),
  im_protocol                                   varchar(255),
  PRIMARY KEY (im_id),
  CONSTRAINT im_entity_id_entity_id_fkey FOREIGN KEY (im_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- -------------------------------------
-- Add tables structures around Profiles
-- -------------------------------------
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
-- Table structure for table ProfileSection
--

CREATE TABLE ProfileSection (
	profilesection_id			serial,
	profilesection_domain_id	integer NOT NULL,
	profilesection_profile_id	integer default NULL,
	profilesection_section_name	varchar(64) NOT NULL default '',
	profilesection_show	        smallint default NULL,
	PRIMARY KEY (profilesection_id)
);

--
-- Table structure for table ProfileProperty
--

CREATE TABLE ProfileProperty (
	profileproperty_id 			serial,
	profileproperty_profile_id		integer default NULL,
	profileproperty_name		        varchar(32) NOT NULL default '',
	profileproperty_value	                text NOT NULL default '',
	PRIMARY KEY (profileproperty_id)
);


--
-- Table structure for table ServiceProperty
--

CREATE TABLE ServiceProperty (
  serviceproperty_id serial,
  serviceproperty_service varchar(255) NOT NULL,
  serviceproperty_property varchar(255) NOT NULL,
  serviceproperty_entity_id integer NOT NULL,
  serviceproperty_value text,
  PRIMARY KEY  (serviceproperty_id),
  CONSTRAINT serviceproperty_entity_id_entity_id_fkey FOREIGN KEY (serviceproperty_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE
);
create INDEX serviceproperty_service_key ON ServiceProperty (serviceproperty_service);
create INDEX serviceproperty_property_key ON ServiceProperty (serviceproperty_property);

--
-- Table structure for table Service
--

CREATE TABLE Service (
  service_id serial,
  service_service varchar(255) NOT NULL,
  service_entity_id integer NOT NULL,
  PRIMARY KEY  (service_id),
  CONSTRAINT service_entity_id_entity_id_fkey FOREIGN KEY (service_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE
);
create INDEX service_service_key ON Service (service_service);

-- Table structure for table SSOTicket
--
CREATE TABLE SSOTicket (
  ssoticket_ticket varchar(255) NOT NULL,
  ssoticket_user_id integer,
  ssoticket_timestamp timestamp NOT NULL,
  PRIMARY KEY (ssoticket_ticket),
  CONSTRAINT ssoticket_user_id_userobm_id_fkey FOREIGN KEY (ssoticket_user_id) REFERENCES UserObm (userobm_id) ON DELETE CASCADE ON UPDATE CASCADE
);
--  _______________
-- | CalendarEvent |
--  ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
-- Table EventCategory1
CREATE TABLE EventCategory1 (
  eventcategory1_id          serial,
  eventcategory1_domain_id   integer NOT NULL,
  eventcategory1_timeupdate  timestamp,
  eventcategory1_timecreate  timestamp,
  eventcategory1_userupdate  integer default NULL,
  eventcategory1_usercreate  integer default NULL,
  eventcategory1_code        varchar(10) default '',
  eventcategory1_label       varchar(128) default NULL,
  eventcategory1_color       char(6),
  PRIMARY KEY (eventcategory1_id)
);
ALTER TABLE EventCategory1 ADD CONSTRAINT eventcategory1_domain_id_domain_id_fkey FOREIGN KEY (eventcategory1_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE EventCategory1 ADD CONSTRAINT eventcategory1_userupdate_userobm_id_fkey FOREIGN KEY (eventcategory1_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;
ALTER TABLE EventCategory1 ADD CONSTRAINT eventcategory1_usercreate_userobm_id_fkey FOREIGN KEY (eventcategory1_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


CREATE TYPE vcomponent AS ENUM ('VEVENT', 'VTODO', 'VJOURNAL', 'VFREEBUSY');
CREATE TYPE vopacity AS ENUM ('OPAQUE', 'TRANSPARENT');
-- Event Creation
CREATE TABLE Event (
  event_id           	serial,
  event_domain_id    	integer NOT NULL,
  event_timeupdate   	timestamp,
  event_timecreate   	timestamp,
  event_userupdate   	integer DEFAULT NULL,
  event_usercreate   	integer DEFAULT NULL,
  event_parent_id       integer default NULL,
  event_ext_id       	varchar(255) default '', 
  event_type            vcomponent default 'VEVENT',
  event_origin          varchar(255) NOT NULL default '',
  event_owner           integer default NULL,    
  event_timezone        varchar(255) default 'GMT',    
  event_opacity         vopacity default 'OPAQUE',
  event_title           varchar(255) default NULL,
  event_location        varchar(100) default NULL,
  event_category1_id    integer default NULL,
  event_priority        integer,
  event_privacy         integer,
  event_date            timestamp NULL,
  event_duration        integer NOT NULL default 0,
  event_allday          BOOLEAN default FALSE,
  event_repeatkind      varchar(20) default 'none' NOT NULL,
  event_repeatfrequence integer default NULL,
  event_repeatdays      varchar(7) default NULL,
  event_endrepeat       timestamp default NULL,
  event_color           varchar(7),
  event_completed       timestamp,
  event_url             text,
  event_description     text,
  event_properties      text,
  PRIMARY KEY (event_id)
);
-- Foreign key from event_domain_id to domain_id
ALTER TABLE Event ADD CONSTRAINT event_domain_id_domain_id_fkey FOREIGN KEY (event_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;
-- Foreign key from event_owner to userobm_id
ALTER TABLE Event ADD CONSTRAINT event_owner_userobm_id_fkey FOREIGN KEY (event_owner) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;
-- Foreign key from event_userupdate to userobm_id
ALTER TABLE Event ADD CONSTRAINT event_userupdate_userobm_id_fkey FOREIGN KEY (event_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;
-- Foreign key from event_usercreate to userobm_id
ALTER TABLE Event ADD CONSTRAINT event_usercreate_userobm_id_fkey FOREIGN KEY (event_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;
-- Foreign key from event_category1_id to calendarcategory1_id
ALTER TABLE Event ADD CONSTRAINT event_category1_id_eventcategory1_id_fkey FOREIGN KEY (event_category1_id) REFERENCES EventCategory1(eventcategory1_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Table EventAlert

CREATE TABLE EventAlert (
  eventalert_timeupdate  timestamp,
  eventalert_timecreate  timestamp,
  eventalert_userupdate  integer default NULL,
  eventalert_usercreate  integer default NULL,
  eventalert_event_id    integer,
  eventalert_user_id     integer,
  eventalert_duration    integer NOT NULL default 0
);
CREATE INDEX idx_eventalert_user ON EventAlert (eventalert_user_id);
ALTER TABLE EventAlert ADD CONSTRAINT eventalert_event_id_event_id_fkey FOREIGN KEY (eventalert_event_id) REFERENCES Event(event_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE EventAlert ADD CONSTRAINT eventalert_user_id_userobm_id_fkey FOREIGN KEY (eventalert_user_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE EventAlert ADD CONSTRAINT eventalert_userupdate_userobm_id_fkey FOREIGN KEY (eventalert_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;
ALTER TABLE EventAlert ADD CONSTRAINT eventalert_usercreate_userobm_id_fkey FOREIGN KEY (eventalert_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Table EventException

CREATE TABLE EventException (
  eventexception_timeupdate   timestamp,
  eventexception_timecreate   timestamp,
  eventexception_userupdate   integer default NULL,
  eventexception_usercreate   integer default NULL,
  eventexception_event_id     integer,
  eventexception_date         timestamp NOT NULL,
  PRIMARY KEY (eventexception_event_id,eventexception_date)
);
ALTER TABLE EventException ADD CONSTRAINT eventexception_event_id_event_id_fkey FOREIGN KEY (eventexception_event_id) REFERENCES Event(event_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE EventException ADD CONSTRAINT eventexception_userupdate_userobm_id_fkey FOREIGN KEY (eventexception_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;
ALTER TABLE EventException ADD CONSTRAINT eventexception_usercreate_userobm_id_fkey FOREIGN KEY (eventexception_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

--
-- Table DeletedEvent
--

CREATE TABLE DeletedEvent (
  deletedevent_id         serial,
  deletedevent_event_id   integer,
  deletedevent_user_id    integer,
  deletedevent_origin     varchar(255) NOT NULL,
  deletedevent_type       vcomponent DEFAULT 'VEVENT'::vcomponent,
  deletedevent_timestamp  timestamp
);
create INDEX idx_dce_event_id ON DeletedEvent (deletedevent_event_id);
create INDEX idx_dce_user_id ON DeletedEvent (deletedevent_user_id);

CREATE SEQUENCE deletedevent_deletedevent_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MAXVALUE
    NO MINVALUE
    CACHE 1;


ALTER SEQUENCE deletedevent_deletedevent_id_seq OWNED BY deletedevent.deletedevent_id;

ALTER TABLE DeletedEvent ALTER COLUMN deletedevent_id SET DEFAULT nextval('deletedevent_deletedevent_id_seq'::regclass);

--
-- Table structure for table TaskEvent
--

CREATE TABLE TaskEvent (
  taskevent_task_id integer NOT NULL,
  taskevent_event_id integer NOT NULL,
  PRIMARY KEY (taskevent_event_id, taskevent_task_id),
  CONSTRAINT taskevent_task_id_projecttask_id_fkey FOREIGN KEY (taskevent_task_id) REFERENCES ProjectTask (projecttask_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT taskevent_event_id_event_id_fkey FOREIGN KEY (taskevent_event_id) REFERENCES Event (event_id) ON DELETE CASCADE ON UPDATE CASCADE
);

--
-- Modification of table EventEntity before rename it to EventLink
--
CREATE TYPE vpartstat AS ENUM ('NEEDS-ACTION', 'ACCEPTED', 'DECLINED', 'TENTATIVE', 'DELEGATED', 'COMPLETED', 'IN-PROGRESS');
ALTER TABLE EventEntity ADD COLUMN evententity_state2 vpartstat default 'NEEDS-ACTION';
UPDATE EventEntity set evententity_state2 = 'ACCEPTED' where evententity_state!='A' AND evententity_state!='W' AND evententity_state!='R';
UPDATE EventEntity set evententity_state2 = 'ACCEPTED' where evententity_state='A';
UPDATE EventEntity set evententity_state2 = 'NEEDS-ACTION' where evententity_state='W';
UPDATE EventEntity set evententity_state2 = 'DECLINED' where evententity_state='R';
ALTER TABLE EventEntity DROP COLUMN evententity_state;
ALTER TABLE EventEntity RENAME COLUMN evententity_state2 TO evententity_state;

CREATE TYPE vrole AS ENUM ('CHAIR', 'REQ', 'OPT', 'NON');
ALTER TABLE EventEntity ADD COLUMN evententity_required2 vrole default 'REQ';
ALTER TABLE EventEntity DROP COLUMN evententity_required;
ALTER TABLE EventEntity RENAME COLUMN evententity_required2 TO evententity_required;
UPDATE EventEntity set evententity_required = 'REQ';

ALTER TABLE EventEntity ADD COLUMN evententity_percent float default 0;

ALTER TABLE EventEntity RENAME TO EventLink;
ALTER TABLE EventLink RENAME COLUMN evententity_timeupdate TO eventlink_timeupdate;
ALTER TABLE EventLink RENAME COLUMN evententity_timecreate TO eventlink_timecreate;
ALTER TABLE EventLink RENAME COLUMN evententity_userupdate TO eventlink_userupdate;
ALTER TABLE EventLink RENAME COLUMN evententity_usercreate TO eventlink_usercreate;
ALTER TABLE EventLink RENAME COLUMN evententity_event_id TO eventlink_event_id;
ALTER TABLE EventLink RENAME COLUMN evententity_entity_id TO eventlink_entity_id;
ALTER TABLE EventLink RENAME COLUMN evententity_entity TO eventlink_entity;
ALTER TABLE EventLink RENAME COLUMN evententity_state TO eventlink_state;
ALTER TABLE EventLink RENAME COLUMN evententity_required TO eventlink_required;
ALTER TABLE EventLink RENAME COLUMN evententity_percent TO eventlink_percent;

-- Foreign key from evententity_event_id to event_id
DELETE FROM EventLink WHERE eventlink_event_id NOT IN (SELECT event_id FROM Event) AND eventlink_event_id IS NOT NULL;
ALTER TABLE EventLink ADD CONSTRAINT eventlink_event_id_event_id_fkey FOREIGN KEY (eventlink_event_id) REFERENCES Event(event_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from evententity_userupdate to userobm_id
UPDATE EventLink SET eventlink_userupdate = NULL WHERE eventlink_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND eventlink_userupdate IS NOT NULL;
ALTER TABLE EventLink ALTER COLUMN eventlink_userupdate SET DEFAULT NULL;
ALTER TABLE EventLink ADD CONSTRAINT eventlink_userupdate_userobm_id_fkey FOREIGN KEY (eventlink_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from evententity_usercreate to userobm_id
UPDATE EventLink SET eventlink_usercreate = NULL WHERE eventlink_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND eventlink_usercreate IS NOT NULL;
ALTER TABLE EventLink ALTER COLUMN eventlink_usercreate SET DEFAULT NULL;
ALTER TABLE EventLink ADD CONSTRAINT eventlink_usercreate_userobm_id_fkey FOREIGN KEY (eventlink_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE OGroupEntity RENAME TO OGroupLink;
ALTER TABLE OGroupLink RENAME COLUMN ogroupentity_id TO ogrouplink_id;
ALTER TABLE OGroupLink RENAME COLUMN ogroupentity_domain_id TO ogrouplink_domain_id;
ALTER TABLE OGroupLink RENAME COLUMN ogroupentity_timeupdate TO ogrouplink_timeupdate;
ALTER TABLE OGroupLink RENAME COLUMN ogroupentity_timecreate TO ogrouplink_timecreate;
ALTER TABLE OGroupLink RENAME COLUMN ogroupentity_userupdate TO ogrouplink_userupdate;
ALTER TABLE OGroupLink RENAME COLUMN ogroupentity_usercreate TO ogrouplink_usercreate;
ALTER TABLE OGroupLink RENAME COLUMN ogroupentity_ogroup_id TO ogrouplink_ogroup_id;
ALTER TABLE OGroupLink RENAME COLUMN ogroupentity_entity_id TO ogrouplink_entity_id;
ALTER TABLE OGroupLink RENAME COLUMN ogroupentity_entity TO ogrouplink_entity;

ALTER TABLE DocumentEntity RENAME TO DocumentLink;
ALTER TABLE DocumentLink RENAME COLUMN documententity_document_id TO documentlink_document_id;
ALTER TABLE DocumentLink RENAME COLUMN documententity_entity_id TO documentlink_entity_id;
ALTER TABLE DocumentLink RENAME COLUMN documententity_entity TO documentlink_entity;

--  _______________
-- | Entity tables |
--  ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯

CREATE TABLE AccountEntity (
  accountentity_entity_id integer NOT NULL,
  accountentity_account_id integer NOT NULL,
  PRIMARY KEY (accountentity_entity_id, accountentity_account_id)
);
  
CREATE TABLE CvEntity (
  cventity_entity_id integer NOT NULL,
  cventity_cv_id integer NOT NULL,
  PRIMARY KEY (cventity_entity_id, cventity_cv_id)
);
  
CREATE TABLE CalendarEntity (
  calendarentity_entity_id integer NOT NULL,
  calendarentity_calendar_id integer NOT NULL,
  PRIMARY KEY (calendarentity_entity_id, calendarentity_calendar_id)
);
  
CREATE TABLE CompanyEntity (
  companyentity_entity_id integer NOT NULL,
  companyentity_company_id integer NOT NULL,
  PRIMARY KEY (companyentity_entity_id, companyentity_company_id)
);
  
CREATE TABLE ContactEntity (
  contactentity_entity_id integer NOT NULL,
  contactentity_contact_id integer NOT NULL,
  PRIMARY KEY (contactentity_entity_id, contactentity_contact_id)
);
  
CREATE TABLE ContractEntity (
  contractentity_entity_id integer NOT NULL,
  contractentity_contract_id integer NOT NULL,
  PRIMARY KEY (contractentity_entity_id, contractentity_contract_id)
);
  
CREATE TABLE DealEntity (
  dealentity_entity_id integer NOT NULL,
  dealentity_deal_id integer NOT NULL,
  PRIMARY KEY (dealentity_entity_id, dealentity_deal_id)
);
  
CREATE TABLE DocumentEntity (
  documententity_entity_id integer NOT NULL,
  documententity_document_id integer NOT NULL,
  PRIMARY KEY (documententity_entity_id, documententity_document_id)
);
  
CREATE TABLE DomainEntity (
  domainentity_entity_id integer NOT NULL,
  domainentity_domain_id integer NOT NULL,
  PRIMARY KEY (domainentity_entity_id, domainentity_domain_id)
);
  
CREATE TABLE EventEntity (
  evententity_entity_id integer NOT NULL,
  evententity_event_id integer NOT NULL,
  PRIMARY KEY (evententity_entity_id, evententity_event_id)
);
  
CREATE TABLE HostEntity (
  hostentity_entity_id integer NOT NULL,
  hostentity_host_id integer NOT NULL,
  PRIMARY KEY (hostentity_entity_id, hostentity_host_id)
);
  
CREATE TABLE ImportEntity (
  importentity_entity_id integer NOT NULL,
  importentity_import_id integer NOT NULL,
  PRIMARY KEY (importentity_entity_id, importentity_import_id)
);
  
CREATE TABLE IncidentEntity (
  incidententity_entity_id integer NOT NULL,
  incidententity_incident_id integer NOT NULL,
  PRIMARY KEY (incidententity_entity_id, incidententity_incident_id)
);
  
CREATE TABLE InvoiceEntity (
  invoiceentity_entity_id integer NOT NULL,
  invoiceentity_invoice_id integer NOT NULL,
  PRIMARY KEY (invoiceentity_entity_id, invoiceentity_invoice_id)
);
  
CREATE TABLE LeadEntity (
  leadentity_entity_id integer NOT NULL,
  leadentity_lead_id integer NOT NULL,
  PRIMARY KEY (leadentity_entity_id, leadentity_lead_id)
);
  
CREATE TABLE ListEntity (
  listentity_entity_id integer NOT NULL,
  listentity_list_id integer NOT NULL,
  PRIMARY KEY (listentity_entity_id, listentity_list_id)
);
  
CREATE TABLE MailshareEntity (
  mailshareentity_entity_id integer NOT NULL,
  mailshareentity_mailshare_id integer NOT NULL,
  PRIMARY KEY (mailshareentity_entity_id, mailshareentity_mailshare_id)
);
  
CREATE TABLE MailboxEntity (
  mailboxentity_entity_id integer NOT NULL,
  mailboxentity_mailbox_id integer NOT NULL,
  PRIMARY KEY (mailboxentity_entity_id, mailboxentity_mailbox_id)
);
  
CREATE TABLE OgroupEntity (
  ogroupentity_entity_id integer NOT NULL,
  ogroupentity_ogroup_id integer NOT NULL,
  PRIMARY KEY (ogroupentity_entity_id, ogroupentity_ogroup_id)
);
  
CREATE TABLE ObmbookmarkEntity (
  obmbookmarkentity_entity_id integer NOT NULL,
  obmbookmarkentity_obmbookmark_id integer NOT NULL,
  PRIMARY KEY (obmbookmarkentity_entity_id, obmbookmarkentity_obmbookmark_id)
);
  
CREATE TABLE OrganizationalchartEntity (
  organizationalchartentity_entity_id integer NOT NULL,
  organizationalchartentity_organizationalchart_id integer NOT NULL,
  PRIMARY KEY (organizationalchartentity_entity_id, organizationalchartentity_organizationalchart_id)
);
  
CREATE TABLE ParentdealEntity (
  parentdealentity_entity_id integer NOT NULL,
  parentdealentity_parentdeal_id integer NOT NULL,
  PRIMARY KEY (parentdealentity_entity_id, parentdealentity_parentdeal_id)
);
  
CREATE TABLE PaymentEntity (
  paymententity_entity_id integer NOT NULL,
  paymententity_payment_id integer NOT NULL,
  PRIMARY KEY (paymententity_entity_id, paymententity_payment_id)
);
  
CREATE TABLE ProfileEntity (
  profileentity_entity_id integer NOT NULL,
  profileentity_profile_id integer NOT NULL,
  PRIMARY KEY (profileentity_entity_id, profileentity_profile_id)
);
  
CREATE TABLE ProjectEntity (
  projectentity_entity_id integer NOT NULL,
  projectentity_project_id integer NOT NULL,
  PRIMARY KEY (projectentity_entity_id, projectentity_project_id)
);
  
CREATE TABLE PublicationEntity (
  publicationentity_entity_id integer NOT NULL,
  publicationentity_publication_id integer NOT NULL,
  PRIMARY KEY (publicationentity_entity_id, publicationentity_publication_id)
);
  
CREATE TABLE ResourcegroupEntity (
  resourcegroupentity_entity_id integer NOT NULL,
  resourcegroupentity_resourcegroup_id integer NOT NULL,
  PRIMARY KEY (resourcegroupentity_entity_id, resourcegroupentity_resourcegroup_id)
);
  
CREATE TABLE ResourceEntity (
  resourceentity_entity_id integer NOT NULL,
  resourceentity_resource_id integer NOT NULL,
  PRIMARY KEY (resourceentity_entity_id, resourceentity_resource_id)
);
  
CREATE TABLE SubscriptionEntity (
  subscriptionentity_entity_id integer NOT NULL,
  subscriptionentity_subscription_id integer NOT NULL,
  PRIMARY KEY (subscriptionentity_entity_id, subscriptionentity_subscription_id)
);
  
CREATE TABLE GroupEntity (
  groupentity_entity_id integer NOT NULL,
  groupentity_group_id integer NOT NULL,
  PRIMARY KEY (groupentity_entity_id, groupentity_group_id)
);
  
CREATE TABLE UserEntity (
  userentity_entity_id integer NOT NULL,
  userentity_user_id integer NOT NULL,
  PRIMARY KEY (userentity_entity_id, userentity_user_id)
);
  
CREATE TABLE TmpEntity (
  entity_id     serial,
  id_entity     integer,
  PRIMARY KEY (entity_id)
);
--  __________________
-- | Cleanning tables |
--  ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
-- Clean CalendarEvent before migration to Event
-- Foreign key domain_id
DELETE FROM CalendarEvent WHERE calendarevent_domain_id NOT IN (SELECT domain_id FROM Domain) AND calendarevent_domain_id IS NOT NULL;
-- Foreign key from calendarevent_userupdate to userobm_id
UPDATE CalendarEvent SET calendarevent_userupdate = NULL WHERE calendarevent_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND calendarevent_userupdate IS NOT NULL;
ALTER TABLE CalendarEvent ALTER COLUMN calendarevent_userupdate SET DEFAULT NULL;
-- Foreign key from calendarevent_usercreate to userobm_id
UPDATE CalendarEvent SET calendarevent_usercreate = NULL WHERE calendarevent_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND calendarevent_usercreate IS NOT NULL;
ALTER TABLE CalendarEvent ALTER COLUMN calendarevent_usercreate SET DEFAULT NULL;
-- Foreign key from calendarevent_owner to userobm_id
DELETE FROM CalendarEvent WHERE calendarevent_owner NOT IN (SELECT userobm_id FROM UserObm) AND calendarevent_owner IS NOT NULL;
-- Foreign key from calendarevent_category1_id to calendarcategory1_id
UPDATE CalendarEvent SET calendarevent_category1_id = NULL WHERE calendarevent_category1_id NOT IN (SELECT calendarcategory1_id FROM CalendarCategory1) AND calendarevent_category1_id IS NOT NULL;
-- event_repeatkind will be NOT NULL default 'none'
UPDATE CalendarEvent SET calendarevent_repeatkind='none' WHERE calendarevent_repeatkind IS NULL;

-- Clean Todo before migration to Event
-- Foreign key from todo_domain_id to domain_id
DELETE FROM Todo WHERE todo_domain_id NOT IN (SELECT domain_id FROM Domain) AND todo_domain_id IS NOT NULL;
-- Foreign key from todo_user to userobm_id
DELETE FROM Todo WHERE todo_user NOT IN (SELECT userobm_id FROM UserObm) AND todo_user IS NOT NULL;
-- Foreign key from todo_userupdate to userobm_id
UPDATE Todo SET todo_userupdate = NULL WHERE todo_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND todo_userupdate IS NOT NULL;
ALTER TABLE Todo ALTER COLUMN todo_userupdate SET DEFAULT NULL;
-- Foreign key from todo_usercreate to userobm_id
UPDATE Todo SET todo_usercreate = NULL WHERE todo_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND todo_usercreate IS NOT NULL;
ALTER TABLE Todo ALTER COLUMN todo_usercreate SET DEFAULT NULL;

--
-- Migration of ClandarEvent to Event
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
  CAST(calendarevent_allday AS BOOLEAN),
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

SELECT setval('event_event_id_seq', max(event_id)) FROM Event;

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
  'none',
  NULL,
  NULL,
  NULL,
  NULL,
  todo_deadline,
  NULL,
  todo_content
FROM Todo;

-- Clean CalendarAlert before migration to EventAlert
-- Foreign key from calendaralert_event_id to event_id
DELETE FROM CalendarAlert WHERE calendaralert_event_id NOT IN (SELECT event_id FROM Event) AND calendaralert_event_id IS NOT NULL;
-- Foreign key from calendaralert_user_id to userobm_id
DELETE FROM CalendarAlert WHERE calendaralert_user_id NOT IN (SELECT userobm_id FROM UserObm) AND calendaralert_user_id IS NOT NULL;
-- Foreign key from calendaralert_userupdate to userobm_id
UPDATE CalendarAlert SET calendaralert_userupdate = NULL WHERE calendaralert_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND calendaralert_userupdate IS NOT NULL;
ALTER TABLE CalendarAlert ALTER COLUMN calendaralert_userupdate SET DEFAULT NULL;
-- Foreign key from calendaralert_usercreate to userobm_id
UPDATE CalendarAlert SET calendaralert_usercreate = NULL WHERE calendaralert_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND calendaralert_usercreate IS NOT NULL;
ALTER TABLE CalendarAlert ALTER COLUMN calendaralert_usercreate SET DEFAULT NULL;

-- Clean CalendarException before migration to EventException
-- Foreign key from calendarexception_event_id to calendarevent_id
DELETE FROM CalendarException WHERE calendarexception_event_id NOT IN (SELECT calendarevent_id FROM CalendarEvent) AND calendarexception_event_id IS NOT NULL;
-- Foreign key from calendarexception_userupdate to userobm_id
UPDATE CalendarException SET calendarexception_userupdate = NULL WHERE calendarexception_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND calendarexception_userupdate IS NOT NULL;
ALTER TABLE CalendarException ALTER COLUMN calendarexception_userupdate SET DEFAULT NULL;
-- Foreign key from calendarexception_usercreate to userobm_id
UPDATE CalendarException SET calendarexception_usercreate = NULL WHERE calendarexception_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND calendarexception_usercreate IS NOT NULL;
ALTER TABLE CalendarException ALTER COLUMN calendarexception_usercreate SET DEFAULT NULL;

-- Clean CalendarCategory1 before migration to EventCategory1
-- Foreign key from calendarcategory1_domain_id to domain_id
DELETE FROM CalendarCategory1 WHERE calendarcategory1_domain_id NOT IN (SELECT domain_id FROM Domain) AND calendarcategory1_domain_id IS NOT NULL;
-- Foreign key from calendarcategory1_userupdate to userobm_id
UPDATE CalendarCategory1 SET calendarcategory1_userupdate = NULL WHERE calendarcategory1_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND calendarcategory1_userupdate IS NOT NULL;
ALTER TABLE CalendarCategory1 ALTER COLUMN calendarcategory1_userupdate SET DEFAULT NULL;
-- Foreign key from calendarcategory1_usercreate to userobm_id
UPDATE CalendarCategory1 SET calendarcategory1_usercreate = NULL WHERE calendarcategory1_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND calendarcategory1_usercreate IS NOT NULL;
ALTER TABLE CalendarCategory1 ALTER COLUMN calendarcategory1_usercreate SET DEFAULT NULL;

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

SELECT setval('eventcategory1_eventcategory1_id_seq', max(eventcategory1_id)) FROM EventCategory1;

--
-- Migration of DeletedCalendarEvent to DeletedEvent
--
INSERT INTO DeletedEvent (deletedevent_id,
  deletedevent_event_id,
  deletedevent_user_id,
  deletedevent_type,
  deletedevent_timestamp,
  deletedevent_origin)
SELECT
  deletedcalendarevent_id,
  deletedcalendarevent_event_id,
  deletedcalendarevent_user_id,
  'VEVENT',
  deletedcalendarevent_timestamp,
  'obm21'
FROM DeletedCalendarEvent;

SELECT setval('deletedevent_deletedevent_id_seq', max(deletedevent_id)) FROM DeletedEvent;

--
-- Migration of DeletedTodo to DeletedEvent
--
INSERT INTO DeletedEvent (
  deletedevent_event_id,
  deletedevent_user_id,
  deletedevent_type,
  deletedevent_timestamp,
  deletedevent_origin)
SELECT
  deletedtodo_todo_id,
  null,
  'VTODO',
  deletedtodo_timestamp,
  'obm21'
FROM DeletedTodo;

SELECT setval('deletedevent_deletedevent_id_seq', max(deletedevent_id)) FROM DeletedEvent;

--  ______________________
-- | Tables modifications |
--  ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
-- NOTE : Set integer to boolean when necessary
-- ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
-- Domain
ALTER TABLE Domain ADD COLUMN domain_global BOOLEAN DEFAULT FALSE;
ALTER TABLE Domain DROP COLUMN domain_mail_server_id;

SELECT setval('domain_domain_id_seq', max(domain_id)) FROM Domain;

-- OGroup
ALTER TABLE OGroup ALTER COLUMN ogroup_parent_id DROP NOT NULL;

-- ObmBookmarkProperty
ALTER TABLE ObmBookmarkProperty ALTER COLUMN obmbookmarkproperty_value TYPE varchar(255);
ALTER TABLE ObmBookmarkProperty ALTER COLUMN obmbookmarkproperty_value SET NOT NULL;
ALTER TABLE ObmBookmarkProperty ALTER COLUMN obmbookmarkproperty_value SET DEFAULT '';

-- Preferences
ALTER TABLE DisplayPref DROP CONSTRAINT displaypref_pkey;
ALTER TABLE DisplayPref ADD CONSTRAINT displaypref_key  UNIQUE (display_user_id, display_entity, display_fieldname);
ALTER TABLE DisplayPref ADD COLUMN display_id serial PRIMARY KEY;

-- Contact
ALTER TABLE Contact ADD COLUMN contact_middlename varchar(32);
ALTER TABLE Contact ADD COLUMN contact_suffix varchar(16);
ALTER TABLE Contact ADD COLUMN contact_manager varchar(64);
ALTER TABLE Contact ADD COLUMN contact_assistant varchar(64);
ALTER TABLE Contact ADD COLUMN contact_spouse varchar(64);
ALTER TABLE Contact ADD COLUMN contact_category varchar(255);
ALTER TABLE Contact ADD COLUMN contact_birthday_id INTEGER default NULL;
ALTER TABLE Contact ADD COLUMN contact_anniversary_id INTEGER default NULL;
ALTER TABLE Contact ADD COLUMN contact_photo_id INTEGER default NULL;
ALTER TABLE Contact ADD COLUMN contact_collected BOOLEAN default FALSE;
ALTER TABLE Contact ADD COLUMN contact_origin VARCHAR(255);
UPDATE Contact SET contact_origin='obm21';
ALTER TABLE Contact ALTER COLUMN contact_origin SET DEFAULT NOT NULL;

ALTER TABLE DeletedContact ADD COLUMN deletedcontact_user_id integer;
ALTER TABLE DeletedContact ADD COLUMN deletedcontact_origin varchar(255);
UPDATE DeletedContact SET deletedcontact_origin='obm21';
ALTER TABLE DeletedContact ALTER COLUMN deletedcontact_origin SET NOT NULL;


CREATE TYPE userstatus AS ENUM ('INIT', 'VALID');
ALTER TABLE UserObm ADD COLUMN userobm_status userstatus DEFAULT 'VALID';

-- NOT NULL to NULL Convertion
ALTER TABLE UserObm ALTER COLUMN userobm_domain_id SET NOT NULL;
ALTER TABLE UserObmPref ALTER COLUMN userobmpref_user_id DROP NOT NULL;
ALTER TABLE UserObmPref ALTER COLUMN userobmpref_user_id SET default NULL;
ALTER TABLE UserObmPref ADD COLUMN userobmpref_id serial;
ALTER TABLE UserObmPref ADD PRIMARY KEY (userobmpref_id);
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
ALTER TABLE EntityRight DROP CONSTRAINT entityright_pkey;
ALTER TABLE EntityRight ADD COLUMN entityright_id serial PRIMARY KEY;
ALTER TABLE EntityRight ALTER COLUMN entityright_consumer_id DROP NOT NULL;
ALTER TABLE EntityRight ALTER COLUMN entityright_consumer_id SET default NULL;
ALTER TABLE Kind ALTER COLUMN kind_domain_id SET NOT NULL;
ALTER TABLE ContactFunction ALTER COLUMN contactfunction_domain_id SET NOT NULL;
ALTER TABLE LeadSource ALTER COLUMN leadsource_domain_id SET NOT NULL;
ALTER TABLE LeadStatus ALTER COLUMN leadstatus_domain_id SET NOT NULL;
ALTER TABLE Lead ALTER COLUMN lead_domain_id SET NOT NULL;
ALTER TABLE Lead ALTER COLUMN lead_source_id SET default NULL;
ALTER TABLE Lead ALTER COLUMN lead_manager_id SET default NULL;
ALTER TABLE Lead ADD COLUMN lead_priority integer DEFAULT 0;
ALTER TABLE ParentDeal ALTER COLUMN parentdeal_domain_id SET NOT NULL;
ALTER TABLE Deal ALTER COLUMN deal_domain_id SET NOT NULL;
ALTER TABLE DealStatus ALTER COLUMN dealstatus_domain_id SET NOT NULL;
ALTER TABLE DealType ALTER COLUMN dealtype_domain_id SET NOT NULL;
ALTER TABLE DealCompanyRole ALTER COLUMN dealcompanyrole_domain_id SET NOT NULL;
ALTER TABLE List ALTER COLUMN list_domain_id SET NOT NULL;
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
ALTER TABLE UGroup ALTER COLUMN group_name varchar(255);
ALTER TABLE OrganizationalChart ALTER COLUMN organizationalchart_domain_id SET NOT NULL;
ALTER TABLE OGroup ALTER COLUMN ogroup_domain_id SET NOT NULL;
ALTER TABLE OGroupLink ALTER COLUMN ogrouplink_domain_id SET NOT NULL;
ALTER TABLE Host ADD COLUMN host_fqdn varchar(255);
ALTER TABLE Import ALTER COLUMN import_domain_id SET NOT NULL;
ALTER TABLE Import ALTER COLUMN import_datasource_id SET default NULL;
ALTER TABLE Resource ALTER COLUMN resource_domain_id SET NOT NULL;
ALTER TABLE Resource ADD COLUMN resource_delegation varchar(64) default '';
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
ALTER TABLE UserObm ALTER COLUMN userobm_email_nomade TYPE text default '';

--  _________________
-- | Updating values |
--  ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
-- Global Domain
INSERT INTO Domain (domain_timecreate,domain_label,domain_description,domain_name,domain_global) VALUES  (NOW(), 'Global Domain', 'Virtual domain for managing domains', 'global.virt', TRUE);
INSERT INTO DomainPropertyValue (domainpropertyvalue_domain_id, domainpropertyvalue_property_key, domainpropertyvalue_value) SELECT domain_id , 'update_state', 0 From Domain WHERE domain_global = TRUE;
UPDATE UserObm SET userobm_domain_id = (SELECT domain_id FROM Domain WHERE domain_global = TRUE) WHERE userobm_domain_id = 0;
UPDATE Host SET host_domain_id = (SELECT domain_id FROM Domain WHERE domain_global = TRUE) WHERE host_domain_id = 0;
INSERT INTO UserSystem VALUES (4,'obmsatellite','mG4_Zdnh','200','65534','/','OBM Satellite','LDAP Reader','/bin/false');

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

-- Clean group gid and user uid(private group must have gid = NULL)
ALTER TABLE ONLY ugroup DROP CONSTRAINT ugroup_group_gid_key;
ALTER TABLE ONLY ugroup ADD CONSTRAINT ugroup_group_gid_key UNIQUE (group_gid, group_domain_id);
UPDATE UGroup set group_gid=NULL WHERE group_privacy=1;
DROP INDEX k_uid_user_userobm_index;
CREATE INDEX k_uid_user_userobm_index ON userobm USING btree (userobm_uid);
ALTER TABLE ONLY host DROP CONSTRAINT host_host_uid_key;

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
UPDATE EntityRight SET entityright_access = 1;
DELETE FROM EntityRight WHERE entityright_entity != 'entity' AND entityright_consumer_id IS NOT NULL;
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

-- Category Link migration needs a tmp table to not trigger the unique contraint
-- and allow update request while moving to the entity model

CREATE TABLE CategoryLinkTmp (
  categorylink_category_id integer,
  categorylink_entity_id   integer,
  categorylink_category    varchar(24) NOT NULL default '',
  categorylink_entity      varchar(32) NOT NULL default ''
);


DELETE FROM CategoryLink WHERE categorylink_entity_id NOT IN (SELECT contact_id FROM Contact) AND categorylink_entity = 'contact';
DELETE FROM CategoryLink WHERE categorylink_entity_id NOT IN (SELECT company_id FROM Company) AND categorylink_entity = 'company';
DELETE FROM CategoryLink WHERE categorylink_entity_id NOT IN (SELECT document_id FROM Document) AND categorylink_entity = 'document';
DELETE FROM CategoryLink WHERE categorylink_entity_id NOT IN (SELECT incident_id FROM Incident) AND categorylink_entity = 'incident';
DELETE FROM CategoryLink WHERE categorylink_entity_id NOT IN (SELECT userobm_id FROM UserObm) AND categorylink_entity = 'user';
DELETE FROM CategoryLink WHERE categorylink_entity_id NOT IN (SELECT group_id FROM UGroup) AND categorylink_entity = 'group';
DELETE FROM CategoryLink WHERE categorylink_entity_id NOT IN (SELECT deal_id FROM Deal) AND categorylink_entity = 'deal';

INSERT INTO CategoryLinkTmp SELECT * FROM CategoryLink;

UPDATE CategoryLinkTmp SET categorylink_entity_id = (SELECT contactentity_entity_id FROM ContactEntity INNER JOIN Contact ON contactentity_contact_id = contact_id WHERE contact_id = categorylink_entity_id), categorylink_entity = 'entity' WHERE categorylink_entity = 'contact';
UPDATE CategoryLinkTmp SET categorylink_entity_id = (SELECT companyentity_entity_id FROM CompanyEntity INNER JOIN Company ON companyentity_company_id = company_id WHERE company_id = categorylink_entity_id), categorylink_entity = 'entity' WHERE categorylink_entity = 'company';
UPDATE CategoryLinkTmp SET categorylink_entity_id = (SELECT documententity_entity_id FROM DocumentEntity INNER JOIN Document ON documententity_document_id = document_id WHERE document_id = categorylink_entity_id), categorylink_entity = 'entity' WHERE categorylink_entity = 'document';
UPDATE CategoryLinkTmp SET categorylink_entity_id = (SELECT incidententity_entity_id FROM IncidentEntity INNER JOIN Incident ON incidententity_incident_id = incident_id WHERE incident_id = categorylink_entity_id), categorylink_entity = 'entity' WHERE categorylink_entity = 'incident';
UPDATE CategoryLinkTmp SET categorylink_entity_id = (SELECT userentity_entity_id FROM UserEntity INNER JOIN UserObm ON userentity_user_id = userobm_id WHERE userobm_id = categorylink_entity_id), categorylink_entity = 'entity' WHERE categorylink_entity = 'user';
UPDATE CategoryLinkTmp SET categorylink_entity_id = (SELECT groupentity_entity_id FROM GroupEntity INNER JOIN UGroup ON groupentity_group_id = group_id WHERE group_id = categorylink_entity_id), categorylink_entity = 'entity' WHERE categorylink_entity = 'group';
UPDATE CategoryLinkTmp SET categorylink_entity_id = (SELECT dealentity_entity_id FROM DealEntity INNER JOIN Deal ON dealentity_deal_id = deal_id WHERE deal_id = categorylink_entity_id), categorylink_entity = 'entity' WHERE categorylink_entity = 'deal';

DELETE FROM CategoryLink;
INSERT INTO CategoryLink SELECT * from CategoryLinkTmp;
DROP TABLE CategoryLinkTmp;
ALTER TABLE CategoryLink DROP COLUMN categorylink_entity;
-- Ends category link migration

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


-- -----------------------------------------
-- Updates that need to be after Entity work
-- -----------------------------------------

-- Update entity sequence
SELECT setval('entity_entity_id_seq', max(entity_id)) FROM Entity;

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

-- utilisateur 'obmsatellite', mot de passe 'mG4_Zdnh' - doit avoir le droit de
-- lecture sur l'arborescence d'OBM
DELETE FROM UserSystem WHERE usersystem_login = 'obmsatellite';
INSERT INTO UserSystem VALUES (4,'obmsatellite','mG4_Zdnh','200','65534','/','OBM Satellite','LDAP Reader','/bin/false');

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
JOIN Event on todo_usercreate=event_usercreate and todo_timecreate=event_timecreate and todo_timeupdate=event_timeupdate and todo_user=event_owner and todo_title=event_title
JOIN UserEntity on todo_user=userentity_user_id;

--  _______________________________________________________
-- |Migrating Address  information from Contact and Company|
--  --------------------------------------------------------
INSERT INTO Address (address_entity_id, address_street, address_zipcode, address_town, address_expresspostal, address_country, address_label)
SELECT contactentity_entity_id, (contact_address1 || E'\n' || contact_address2 || E'\n' || contact_address3), contact_zipcode, contact_town, contact_expresspostal, contact_country_iso3166, 'PREF;WORK;X-OBM-Ref1' FROM Contact INNER JOIN ContactEntity ON contactentity_contact_id = contact_id WHERE (contact_address1 != '' AND contact_address1 IS NOT NULL) OR (contact_address2 !='' AND contact_address2 IS NOT NULL) OR (contact_address3 !='' AND contact_address3 IS NOT NULL) OR (contact_zipcode !='' AND contact_zipcode IS NOT NULL) OR (contact_town !='' AND contact_town IS NOT NULL) OR (contact_expresspostal !='' AND contact_expresspostal IS NOT NULL) OR (contact_country_iso3166 !='' AND contact_country_iso3166 IS NOT NULL)
UNION
SELECT companyentity_entity_id, (company_address1 || E'\n' || company_address2 || E'\n' || company_address3), company_zipcode, company_town, company_expresspostal, company_country_iso3166, 'PREF;HQ;X-OBM-Ref1' FROM Company INNER JOIN CompanyEntity ON companyentity_company_id = company_id WHERE (company_address1 !='' AND company_address1 IS NOT NULL) OR (company_address2 !='' AND company_address2 IS NOT NULL) OR (company_address3 !='' AND company_address3 IS NOT NULL) OR (company_zipcode !='' AND company_zipcode IS NOT NULL) OR (company_town !='' AND company_town IS NOT NULL) OR (company_expresspostal !='' AND company_expresspostal IS NOT NULL) OR (company_country_iso3166 !='' AND company_country_iso3166 IS NOT NULL);

INSERT INTO Phone (phone_entity_id, phone_number, phone_label)
SELECT contactentity_entity_id, contact_phone, 'WORK;VOICE;X-OBM-Ref1' FROM Contact INNER JOIN ContactEntity ON contactentity_contact_id = contact_id WHERE contact_phone != '' AND contact_phone IS NOT NULL
UNION
SELECT contactentity_entity_id, contact_homephone, 'HOME;VOICE;X-OBM-Ref1' FROM Contact INNER JOIN ContactEntity ON contactentity_contact_id = contact_id WHERE contact_homephone != '' AND contact_homephone IS NOT NULL
UNION
SELECT contactentity_entity_id, contact_mobilephone, 'CELL;VOICE;X-OBM-Ref1' FROM Contact INNER JOIN ContactEntity ON contactentity_contact_id = contact_id WHERE contact_mobilephone != '' AND contact_mobilephone IS NOT NULL
UNION
SELECT contactentity_entity_id, contact_fax, 'WORK;FAX;X-OBM-Ref1' FROM Contact INNER JOIN ContactEntity ON contactentity_contact_id = contact_id WHERE contact_fax != '' AND contact_fax IS NOT NULL
UNION
SELECT companyentity_entity_id, company_phone, 'WORK;VOICE;X-OBM-Ref1' FROM Company INNER JOIN CompanyEntity ON companyentity_company_id = company_id WHERE company_phone != '' AND company_phone IS NOT NULL
UNION
SELECT companyentity_entity_id, company_fax, 'WORK;FAX;X-OBM-Ref1' FROM Company INNER JOIN CompanyEntity ON companyentity_company_id = company_id WHERE company_fax != '' AND company_fax IS NOT NULL;

UPDATE Phone SET phone_label=('PREF;' || phone_label) WHERE phone_id IN (SELECT min(phone_id) FROM Phone GROUP BY phone_entity_id);

INSERT INTO Email (email_entity_id, email_address, email_label) 
SELECT contactentity_entity_id, contact_email, 'INTERNET;X-OBM-Ref1' FROM Contact INNER JOIN ContactEntity ON contactentity_contact_id = contact_id WHERE contact_email != '' AND contact_email IS NOT NULL
UNION
SELECT contactentity_entity_id, contact_email2, 'INTERNET;X-OBM-Ref2' FROM Contact INNER JOIN ContactEntity ON contactentity_contact_id = contact_id WHERE contact_email2 != '' AND contact_email2 IS NOT NULL
UNION
SELECT companyentity_entity_id, company_email, 'INTERNET;X-OBM-Ref1' FROM Company INNER JOIN CompanyEntity ON companyentity_company_id = company_id WHERE company_email != '' AND company_email IS NOT NULL;

UPDATE Email SET email_label=('PREF;' || email_label) WHERE email_id IN (SELECT min(email_id) FROM Email GROUP BY email_entity_id);

INSERT INTO Website (website_entity_id, website_url, website_label) 
SELECT companyentity_entity_id, company_web, 'PREF;URL;X-OBM-Ref1' FROM Company INNER JOIN CompanyEntity ON companyentity_company_id = company_id WHERE company_web != '' AND company_web IS NOT NULL;

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

-- -----------------------------------------------
-- Migrating event all day date to a ~correct time
-- -----------------------------------------------
-- UPDATE Event SET
-- event_date = DATE_FORMAT(event_date,'%Y-%m-%d 00:00:00'),
-- event_duration = UNIX_TIMESTAMP(DATE_FORMAT(DATE_ADD(DATE_ADD(event_date, INTERVAL (event_duration - 1) SECOND), INTERVAL 1 DAY),'%Y-%m-%d 00:00:00')) - UNIX_TIMESTAMP(DATE_FORMAT(event_date,'%Y-%m-%d 00:00:00'))
-- WHERE event_allday = TRUE;
-- --------------------------------------------
-- Migrating date from system timezone to gmt
-- --------------------------------------------
-- UPDATE Event SET 
-- event_date = CONVERT_TZ(event_date, 'SYSTEM', '+00:00'), 
-- event_endrepeat = CONVERT_TZ(event_date, 'SYSTEM', '+00:00'),
-- event_completed = CONVERT_TZ(event_date, 'SYSTEM', '+00:00');
-- 
-- UPDATE EventException SET
-- eventexception_date = CONVERT_TZ(eventexception_date, 'SYSTEM', '+00:00');
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
ALTER TABLE Account ADD CONSTRAINT account_domain_id_domain_id_fkey FOREIGN KEY (account_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from account_usercreate to userobm_id
UPDATE Account SET account_usercreate = NULL WHERE account_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND account_usercreate IS NOT NULL;
ALTER TABLE Account ALTER COLUMN account_usercreate SET DEFAULT NULL;
ALTER TABLE Account ADD CONSTRAINT account_usercreate_userobm_id_fkey FOREIGN KEY (account_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from account_userupdate to userobm_id
UPDATE Account SET account_userupdate = NULL WHERE account_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND account_userupdate IS NOT NULL;
ALTER TABLE Account ALTER COLUMN account_userupdate SET DEFAULT NULL;
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
ALTER TABLE CV ALTER COLUMN cv_userupdate SET DEFAULT NULL;
ALTER TABLE CV ADD CONSTRAINT cv_userupdate_userobm_id_fkey FOREIGN KEY (cv_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from cv_usercreate to userobm_id
UPDATE CV SET cv_usercreate = NULL WHERE cv_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND cv_usercreate IS NOT NULL;
ALTER TABLE CV ALTER COLUMN cv_usercreate SET DEFAULT NULL;
ALTER TABLE CV ADD CONSTRAINT cv_usercreate_userobm_id_fkey FOREIGN KEY (cv_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from category_domain_id to domain_id
DELETE FROM Category WHERE category_domain_id NOT IN (SELECT domain_id FROM Domain) AND category_domain_id IS NOT NULL;
ALTER TABLE Category ADD CONSTRAINT category_domain_id_domain_id_fkey FOREIGN KEY (category_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from category_userupdate to userobm_id
UPDATE Category SET category_userupdate = NULL WHERE category_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND category_userupdate IS NOT NULL;
ALTER TABLE Category ALTER COLUMN category_userupdate SET DEFAULT NULL;
ALTER TABLE Category ADD CONSTRAINT category_userupdate_userobm_id_fkey FOREIGN KEY (category_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from category_usercreate to userobm_id
UPDATE Category SET category_usercreate = NULL WHERE category_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND category_usercreate IS NOT NULL;
ALTER TABLE Category ALTER COLUMN category_usercreate SET DEFAULT NULL;
ALTER TABLE Category ADD CONSTRAINT category_usercreate_userobm_id_fkey FOREIGN KEY (category_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from categorylink_category_id to category_id
DELETE FROM CategoryLink WHERE categorylink_category_id NOT IN (SELECT category_id FROM Category) AND categorylink_category_id IS NOT NULL;
ALTER TABLE CategoryLink ADD CONSTRAINT categorylink_category_id_category_id_fkey FOREIGN KEY (categorylink_category_id) REFERENCES Category(category_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from company_domain_id to domain_id
DELETE FROM Company WHERE company_domain_id NOT IN (SELECT domain_id FROM Domain) AND company_domain_id IS NOT NULL;
ALTER TABLE Company ADD CONSTRAINT company_domain_id_domain_id_fkey FOREIGN KEY (company_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from company_userupdate to userobm_id
UPDATE Company SET company_userupdate = NULL WHERE company_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND company_userupdate IS NOT NULL;
ALTER TABLE Company ALTER COLUMN company_userupdate SET DEFAULT NULL;
ALTER TABLE Company ADD CONSTRAINT company_userupdate_userobm_id_fkey FOREIGN KEY (company_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from company_usercreate to userobm_id
UPDATE Company SET company_usercreate = NULL WHERE company_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND company_usercreate IS NOT NULL;
ALTER TABLE Company ALTER COLUMN company_usercreate SET DEFAULT NULL;
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
ALTER TABLE CompanyActivity ALTER COLUMN companyactivity_userupdate SET DEFAULT NULL;
ALTER TABLE CompanyActivity ADD CONSTRAINT companyactivity_userupdate_userobm_id_fkey FOREIGN KEY (companyactivity_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from companyactivity_usercreate to userobm_id
UPDATE CompanyActivity SET companyactivity_usercreate = NULL WHERE companyactivity_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND companyactivity_usercreate IS NOT NULL;
ALTER TABLE CompanyActivity ALTER COLUMN companyactivity_usercreate SET DEFAULT NULL;
ALTER TABLE CompanyActivity ADD CONSTRAINT companyactivity_usercreate_userobm_id_fkey FOREIGN KEY (companyactivity_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from companynafcode_domain_id to domain_id
DELETE FROM CompanyNafCode WHERE companynafcode_domain_id NOT IN (SELECT domain_id FROM Domain) AND companynafcode_domain_id IS NOT NULL;
ALTER TABLE CompanyNafCode ADD CONSTRAINT companynafcode_domain_id_domain_id_fkey FOREIGN KEY (companynafcode_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from companynafcode_userupdate to userobm_id
UPDATE CompanyNafCode SET companynafcode_userupdate = NULL WHERE companynafcode_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND companynafcode_userupdate IS NOT NULL;
ALTER TABLE CompanyNafCode ALTER COLUMN companynafcode_userupdate SET DEFAULT NULL;
ALTER TABLE CompanyNafCode ADD CONSTRAINT companynafcode_userupdate_userobm_id_fkey FOREIGN KEY (companynafcode_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from companynafcode_usercreate to userobm_id
UPDATE CompanyNafCode SET companynafcode_usercreate = NULL WHERE companynafcode_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND companynafcode_usercreate IS NOT NULL;
ALTER TABLE CompanyNafCode ALTER COLUMN companynafcode_usercreate SET DEFAULT NULL;
ALTER TABLE CompanyNafCode ADD CONSTRAINT companynafcode_usercreate_userobm_id_fkey FOREIGN KEY (companynafcode_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from companytype_domain_id to domain_id
DELETE FROM CompanyType WHERE companytype_domain_id NOT IN (SELECT domain_id FROM Domain) AND companytype_domain_id IS NOT NULL;
ALTER TABLE CompanyType ADD CONSTRAINT companytype_domain_id_domain_id_fkey FOREIGN KEY (companytype_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from companytype_userupdate to userobm_id
UPDATE CompanyType SET companytype_userupdate = NULL WHERE companytype_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND companytype_userupdate IS NOT NULL;
ALTER TABLE CompanyType ALTER COLUMN companytype_userupdate SET DEFAULT NULL;
ALTER TABLE CompanyType ADD CONSTRAINT companytype_userupdate_userobm_id_fkey FOREIGN KEY (companytype_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from companytype_usercreate to userobm_id
UPDATE CompanyType SET companytype_usercreate = NULL WHERE companytype_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND companytype_usercreate IS NOT NULL;
ALTER TABLE CompanyType ALTER COLUMN companytype_usercreate SET DEFAULT NULL;
ALTER TABLE CompanyType ADD CONSTRAINT companytype_usercreate_userobm_id_fkey FOREIGN KEY (companytype_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contact_domain_id to domain_id
DELETE FROM Contact WHERE contact_domain_id NOT IN (SELECT domain_id FROM Domain) AND contact_domain_id IS NOT NULL;
ALTER TABLE Contact ADD CONSTRAINT contact_domain_id_domain_id_fkey FOREIGN KEY (contact_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contact_company_id to company_id
UPDATE Contact SET contact_company_id = NULL WHERE contact_company_id NOT IN (SELECT company_id FROM Company) AND contact_company_id IS NOT NULL;
ALTER TABLE Contact ADD CONSTRAINT contact_company_id_company_id_fkey FOREIGN KEY (contact_company_id) REFERENCES Company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contact_userupdate to userobm_id
UPDATE Contact SET contact_userupdate = NULL WHERE contact_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND contact_userupdate IS NOT NULL;
ALTER TABLE Contact ALTER COLUMN contact_userupdate SET DEFAULT NULL;
ALTER TABLE Contact ADD CONSTRAINT contact_userupdate_userobm_id_fkey FOREIGN KEY (contact_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contact_usercreate to userobm_id
UPDATE Contact SET contact_usercreate = NULL WHERE contact_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND contact_usercreate IS NOT NULL;
ALTER TABLE Contact ALTER COLUMN contact_usercreate SET DEFAULT NULL;
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
ALTER TABLE ContactFunction ALTER COLUMN contactfunction_userupdate SET DEFAULT NULL;
ALTER TABLE ContactFunction ADD CONSTRAINT contactfunction_userupdate_userobm_id_fkey FOREIGN KEY (contactfunction_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contactfunction_usercreate to userobm_id
UPDATE ContactFunction SET contactfunction_usercreate = NULL WHERE contactfunction_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND contactfunction_usercreate IS NOT NULL;
ALTER TABLE ContactFunction ALTER COLUMN contactfunction_usercreate SET DEFAULT NULL;
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
UPDATE Contract SET contract_deal_id=NULL WHERE contract_deal_id NOT IN (SELECT deal_id FROM Deal) AND contract_deal_id IS NOT NULL;
ALTER TABLE Contract ADD CONSTRAINT contract_deal_id_deal_id_fkey FOREIGN KEY (contract_deal_id) REFERENCES Deal(deal_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contract_company_id to company_id
DELETE FROM Contract WHERE contract_company_id NOT IN (SELECT company_id FROM Company) AND contract_company_id IS NOT NULL;
ALTER TABLE Contract ADD CONSTRAINT contract_company_id_company_id_fkey FOREIGN KEY (contract_company_id) REFERENCES Company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contract_userupdate to userobm_id
UPDATE Contract SET contract_userupdate = NULL WHERE contract_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND contract_userupdate IS NOT NULL;
ALTER TABLE Contract ALTER COLUMN contract_userupdate SET DEFAULT NULL;
ALTER TABLE Contract ADD CONSTRAINT contract_userupdate_userobm_id_fkey FOREIGN KEY (contract_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contract_usercreate to userobm_id
UPDATE Contract SET contract_usercreate = NULL WHERE contract_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND contract_usercreate IS NOT NULL;
ALTER TABLE Contract ALTER COLUMN contract_usercreate SET DEFAULT NULL;
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
ALTER TABLE ContractPriority ALTER COLUMN contractpriority_userupdate SET DEFAULT NULL;
ALTER TABLE ContractPriority ADD CONSTRAINT contractpriority_userupdate_userobm_id_fkey FOREIGN KEY (contractpriority_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contractpriority_usercreate to userobm_id
UPDATE ContractPriority SET contractpriority_usercreate = NULL WHERE contractpriority_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND contractpriority_usercreate IS NOT NULL;
ALTER TABLE ContractPriority ALTER COLUMN contractpriority_usercreate SET DEFAULT NULL;
ALTER TABLE ContractPriority ADD CONSTRAINT contractpriority_usercreate_userobm_id_fkey FOREIGN KEY (contractpriority_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contractstatus_domain_id to domain_id
DELETE FROM ContractStatus WHERE contractstatus_domain_id NOT IN (SELECT domain_id FROM Domain) AND contractstatus_domain_id IS NOT NULL;
ALTER TABLE ContractStatus ADD CONSTRAINT contractstatus_domain_id_domain_id_fkey FOREIGN KEY (contractstatus_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contractstatus_userupdate to userobm_id
UPDATE ContractStatus SET contractstatus_userupdate = NULL WHERE contractstatus_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND contractstatus_userupdate IS NOT NULL;
ALTER TABLE ContractStatus ALTER COLUMN contractstatus_userupdate SET DEFAULT NULL;
ALTER TABLE ContractStatus ADD CONSTRAINT contractstatus_userupdate_userobm_id_fkey FOREIGN KEY (contractstatus_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contractstatus_usercreate to userobm_id
UPDATE ContractStatus SET contractstatus_usercreate = NULL WHERE contractstatus_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND contractstatus_usercreate IS NOT NULL;
ALTER TABLE ContractStatus ALTER COLUMN contractstatus_usercreate SET DEFAULT NULL;
ALTER TABLE ContractStatus ADD CONSTRAINT contractstatus_usercreate_userobm_id_fkey FOREIGN KEY (contractstatus_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contracttype_domain_id to domain_id
DELETE FROM ContractType WHERE contracttype_domain_id NOT IN (SELECT domain_id FROM Domain) AND contracttype_domain_id IS NOT NULL;
ALTER TABLE ContractType ADD CONSTRAINT contracttype_domain_id_domain_id_fkey FOREIGN KEY (contracttype_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contracttype_userupdate to userobm_id
UPDATE ContractType SET contracttype_userupdate = NULL WHERE contracttype_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND contracttype_userupdate IS NOT NULL;
ALTER TABLE ContractType ALTER COLUMN contracttype_userupdate SET DEFAULT NULL;
ALTER TABLE ContractType ADD CONSTRAINT contracttype_userupdate_userobm_id_fkey FOREIGN KEY (contracttype_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from contracttype_usercreate to userobm_id
UPDATE ContractType SET contracttype_usercreate = NULL WHERE contracttype_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND contracttype_usercreate IS NOT NULL;
ALTER TABLE ContractType ALTER COLUMN contracttype_usercreate SET DEFAULT NULL;
ALTER TABLE ContractType ADD CONSTRAINT contracttype_usercreate_userobm_id_fkey FOREIGN KEY (contracttype_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from country_domain_id to domain_id
DELETE FROM Country WHERE country_domain_id NOT IN (SELECT domain_id FROM Domain) AND country_domain_id IS NOT NULL;
ALTER TABLE Country ADD CONSTRAINT country_domain_id_domain_id_fkey FOREIGN KEY (country_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from country_userupdate to userobm_id
UPDATE Country SET country_userupdate = NULL WHERE country_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND country_userupdate IS NOT NULL;
ALTER TABLE Country ALTER COLUMN country_userupdate SET DEFAULT NULL;
ALTER TABLE Country ADD CONSTRAINT country_userupdate_userobm_id_fkey FOREIGN KEY (country_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from country_usercreate to userobm_id
UPDATE Country SET country_usercreate = NULL WHERE country_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND country_usercreate IS NOT NULL;
ALTER TABLE Country ALTER COLUMN country_usercreate SET DEFAULT NULL;
ALTER TABLE Country ADD CONSTRAINT country_usercreate_userobm_id_fkey FOREIGN KEY (country_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from datasource_domain_id to domain_id
DELETE FROM DataSource WHERE datasource_domain_id NOT IN (SELECT domain_id FROM Domain) AND datasource_domain_id IS NOT NULL;
ALTER TABLE DataSource ADD CONSTRAINT datasource_domain_id_domain_id_fkey FOREIGN KEY (datasource_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from datasource_userupdate to userobm_id
UPDATE DataSource SET datasource_userupdate = NULL WHERE datasource_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND datasource_userupdate IS NOT NULL;
ALTER TABLE DataSource ALTER COLUMN datasource_userupdate SET DEFAULT NULL;
ALTER TABLE DataSource ADD CONSTRAINT datasource_userupdate_userobm_id_fkey FOREIGN KEY (datasource_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from datasource_usercreate to userobm_id
UPDATE DataSource SET datasource_usercreate = NULL WHERE datasource_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND datasource_usercreate IS NOT NULL;
ALTER TABLE DataSource ALTER COLUMN datasource_usercreate SET DEFAULT NULL;
ALTER TABLE DataSource ADD CONSTRAINT datasource_usercreate_userobm_id_fkey FOREIGN KEY (datasource_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from deal_domain_id to domain_id
DELETE FROM Deal WHERE deal_domain_id NOT IN (SELECT domain_id FROM Domain) AND deal_domain_id IS NOT NULL;
ALTER TABLE Deal ADD CONSTRAINT deal_domain_id_domain_id_fkey FOREIGN KEY (deal_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from deal_parentdeal_id to parentdeal_id
UPDATE Deal SET deal_parentdeal_id=NULL WHERE deal_parentdeal_id NOT IN (SELECT parentdeal_id FROM ParentDeal) AND deal_parentdeal_id IS NOT NULL;
ALTER TABLE Deal ADD CONSTRAINT deal_parentdeal_id_parentdeal_id_fkey FOREIGN KEY (deal_parentdeal_id) REFERENCES ParentDeal(parentdeal_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from deal_company_id to company_id
DELETE FROM Deal WHERE deal_company_id NOT IN (SELECT company_id FROM Company) AND deal_company_id IS NOT NULL;
ALTER TABLE Deal ADD CONSTRAINT deal_company_id_company_id_fkey FOREIGN KEY (deal_company_id) REFERENCES Company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from deal_userupdate to userobm_id
UPDATE Deal SET deal_userupdate = NULL WHERE deal_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND deal_userupdate IS NOT NULL;
ALTER TABLE Deal ALTER COLUMN deal_userupdate SET DEFAULT NULL;
ALTER TABLE Deal ADD CONSTRAINT deal_userupdate_userobm_id_fkey FOREIGN KEY (deal_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from deal_usercreate to userobm_id
UPDATE Deal SET deal_usercreate = NULL WHERE deal_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND deal_usercreate IS NOT NULL;
ALTER TABLE Deal ALTER COLUMN deal_usercreate SET DEFAULT NULL;
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
ALTER TABLE DealCompany ALTER COLUMN dealcompany_userupdate SET DEFAULT NULL;
ALTER TABLE DealCompany ADD CONSTRAINT dealcompany_userupdate_userobm_id_fkey FOREIGN KEY (dealcompany_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from dealcompany_usercreate to userobm_id
UPDATE DealCompany SET dealcompany_usercreate = NULL WHERE dealcompany_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND dealcompany_usercreate IS NOT NULL;
ALTER TABLE DealCompany ALTER COLUMN dealcompany_usercreate SET DEFAULT NULL;
ALTER TABLE DealCompany ADD CONSTRAINT dealcompany_usercreate_userobm_id_fkey FOREIGN KEY (dealcompany_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from dealcompanyrole_domain_id to domain_id
DELETE FROM DealCompanyRole WHERE dealcompanyrole_domain_id NOT IN (SELECT domain_id FROM Domain) AND dealcompanyrole_domain_id IS NOT NULL;
ALTER TABLE DealCompanyRole ADD CONSTRAINT dealcompanyrole_domain_id_domain_id_fkey FOREIGN KEY (dealcompanyrole_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from dealcompanyrole_userupdate to userobm_id
UPDATE DealCompanyRole SET dealcompanyrole_userupdate = NULL WHERE dealcompanyrole_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND dealcompanyrole_userupdate IS NOT NULL;
ALTER TABLE DealCompanyRole ALTER COLUMN dealcompanyrole_userupdate SET DEFAULT NULL;
ALTER TABLE DealCompanyRole ADD CONSTRAINT dealcompanyrole_userupdate_userobm_id_fkey FOREIGN KEY (dealcompanyrole_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from dealcompanyrole_usercreate to userobm_id
UPDATE DealCompanyRole SET dealcompanyrole_usercreate = NULL WHERE dealcompanyrole_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND dealcompanyrole_usercreate IS NOT NULL;
ALTER TABLE DealCompanyRole ALTER COLUMN dealcompanyrole_usercreate SET DEFAULT NULL;
ALTER TABLE DealCompanyRole ADD CONSTRAINT dealcompanyrole_usercreate_userobm_id_fkey FOREIGN KEY (dealcompanyrole_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from dealstatus_domain_id to domain_id
DELETE FROM DealStatus WHERE dealstatus_domain_id NOT IN (SELECT domain_id FROM Domain) AND dealstatus_domain_id IS NOT NULL;
ALTER TABLE DealStatus ADD CONSTRAINT dealstatus_domain_id_domain_id_fkey FOREIGN KEY (dealstatus_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from dealstatus_userupdate to userobm_id
UPDATE DealStatus SET dealstatus_userupdate = NULL WHERE dealstatus_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND dealstatus_userupdate IS NOT NULL;
ALTER TABLE DealStatus ALTER COLUMN dealstatus_userupdate SET DEFAULT NULL;
ALTER TABLE DealStatus ADD CONSTRAINT dealstatus_userupdate_userobm_id_fkey FOREIGN KEY (dealstatus_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from dealstatus_usercreate to userobm_id
UPDATE DealStatus SET dealstatus_usercreate = NULL WHERE dealstatus_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND dealstatus_usercreate IS NOT NULL;
ALTER TABLE DealStatus ALTER COLUMN dealstatus_usercreate SET DEFAULT NULL;
ALTER TABLE DealStatus ADD CONSTRAINT dealstatus_usercreate_userobm_id_fkey FOREIGN KEY (dealstatus_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from dealtype_domain_id to domain_id
DELETE FROM DealType WHERE dealtype_domain_id NOT IN (SELECT domain_id FROM Domain) AND dealtype_domain_id IS NOT NULL;
ALTER TABLE DealType ADD CONSTRAINT dealtype_domain_id_domain_id_fkey FOREIGN KEY (dealtype_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from dealtype_userupdate to userobm_id
UPDATE DealType SET dealtype_userupdate = NULL WHERE dealtype_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND dealtype_userupdate IS NOT NULL;
ALTER TABLE DealType ALTER COLUMN dealtype_userupdate SET DEFAULT NULL;
ALTER TABLE DealType ADD CONSTRAINT dealtype_userupdate_userobm_id_fkey FOREIGN KEY (dealtype_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from dealtype_usercreate to userobm_id
UPDATE DealType SET dealtype_usercreate = NULL WHERE dealtype_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND dealtype_usercreate IS NOT NULL;
ALTER TABLE DealType ALTER COLUMN dealtype_usercreate SET DEFAULT NULL;
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
ALTER TABLE Document ALTER COLUMN document_userupdate SET DEFAULT NULL;
ALTER TABLE Document ADD CONSTRAINT document_userupdate_userobm_id_fkey FOREIGN KEY (document_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from document_usercreate to userobm_id
UPDATE Document SET document_usercreate = NULL WHERE document_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND document_usercreate IS NOT NULL;
ALTER TABLE Document ALTER COLUMN document_usercreate SET DEFAULT NULL;
ALTER TABLE Document ADD CONSTRAINT document_usercreate_userobm_id_fkey FOREIGN KEY (document_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from document_mimetype_id to documentmimetype_id
UPDATE Document SET document_mimetype_id = NULL WHERE document_mimetype_id NOT IN (SELECT documentmimetype_id FROM DocumentMimeType) AND document_mimetype_id IS NOT NULL;
ALTER TABLE Document ADD CONSTRAINT document_mimetype_id_documentmimetype_id_fkey FOREIGN KEY (document_mimetype_id) REFERENCES DocumentMimeType(documentmimetype_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from documentlink_document_id to document_id
DELETE FROM DocumentLink WHERE documentlink_document_id NOT IN (SELECT document_id FROM Document) AND documentlink_document_id IS NOT NULL;
ALTER TABLE DocumentLink ADD CONSTRAINT documentlink_document_id_document_id_fkey FOREIGN KEY (documentlink_document_id) REFERENCES Document(document_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from documentmimetype_domain_id to domain_id
DELETE FROM DocumentMimeType WHERE documentmimetype_domain_id NOT IN (SELECT domain_id FROM Domain) AND documentmimetype_domain_id IS NOT NULL;
ALTER TABLE DocumentMimeType ADD CONSTRAINT documentmimetype_domain_id_domain_id_fkey FOREIGN KEY (documentmimetype_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from documentmimetype_userupdate to userobm_id
UPDATE DocumentMimeType SET documentmimetype_userupdate = NULL WHERE documentmimetype_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND documentmimetype_userupdate IS NOT NULL;
ALTER TABLE DocumentMimeType ALTER COLUMN documentmimetype_userupdate SET DEFAULT NULL;
ALTER TABLE DocumentMimeType ADD CONSTRAINT documentmimetype_userupdate_userobm_id_fkey FOREIGN KEY (documentmimetype_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from documentmimetype_usercreate to userobm_id
UPDATE DocumentMimeType SET documentmimetype_usercreate = NULL WHERE documentmimetype_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND documentmimetype_usercreate IS NOT NULL;
ALTER TABLE DocumentMimeType ALTER COLUMN documentmimetype_usercreate SET DEFAULT NULL;
ALTER TABLE DocumentMimeType ADD CONSTRAINT documentmimetype_usercreate_userobm_id_fkey FOREIGN KEY (documentmimetype_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from domain_userupdate to userobm_id
UPDATE Domain SET domain_userupdate = NULL WHERE domain_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND domain_userupdate IS NOT NULL;
ALTER TABLE Domain ALTER COLUMN domain_userupdate SET DEFAULT NULL;
ALTER TABLE Domain ADD CONSTRAINT domain_userupdate_userobm_id_fkey FOREIGN KEY (domain_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from domain_usercreate to userobm_id
UPDATE Domain SET domain_usercreate = NULL WHERE domain_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND domain_usercreate IS NOT NULL;
ALTER TABLE Domain ALTER COLUMN domain_usercreate SET DEFAULT NULL;
ALTER TABLE Domain ADD CONSTRAINT domain_usercreate_userobm_id_fkey FOREIGN KEY (domain_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from domainpropertyvalue_domain_id to domain_id
DELETE FROM DomainPropertyValue WHERE domainpropertyvalue_domain_id NOT IN (SELECT domain_id FROM Domain) AND domainpropertyvalue_domain_id IS NOT NULL;
ALTER TABLE DomainPropertyValue ADD CONSTRAINT domainpropertyvalue_domain_id_domain_id_fkey FOREIGN KEY (domainpropertyvalue_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

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
ALTER TABLE Host ALTER COLUMN host_userupdate SET DEFAULT NULL;
ALTER TABLE Host ADD CONSTRAINT host_userupdate_userobm_id_fkey FOREIGN KEY (host_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from host_usercreate to userobm_id
UPDATE Host SET host_usercreate = NULL WHERE host_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND host_usercreate IS NOT NULL;
ALTER TABLE Host ALTER COLUMN host_usercreate SET DEFAULT NULL;
ALTER TABLE Host ADD CONSTRAINT host_usercreate_userobm_id_fkey FOREIGN KEY (host_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from import_domain_id to domain_id
DELETE FROM Import WHERE import_domain_id NOT IN (SELECT domain_id FROM Domain) AND import_domain_id IS NOT NULL;
ALTER TABLE Import ADD CONSTRAINT import_domain_id_domain_id_fkey FOREIGN KEY (import_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from import_userupdate to userobm_id
UPDATE Import SET import_userupdate = NULL WHERE import_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND import_userupdate IS NOT NULL;
ALTER TABLE Import ALTER COLUMN import_userupdate SET DEFAULT NULL;
ALTER TABLE Import ADD CONSTRAINT import_userupdate_userobm_id_fkey FOREIGN KEY (import_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from import_usercreate to userobm_id
UPDATE Import SET import_usercreate = NULL WHERE import_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND import_usercreate IS NOT NULL;
ALTER TABLE Import ALTER COLUMN import_usercreate SET DEFAULT NULL;
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
ALTER TABLE Incident ALTER COLUMN incident_userupdate SET DEFAULT NULL;
ALTER TABLE Incident ADD CONSTRAINT incident_userupdate_userobm_id_fkey FOREIGN KEY (incident_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incident_usercreate to userobm_id
UPDATE Incident SET incident_usercreate = NULL WHERE incident_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND incident_usercreate IS NOT NULL;
ALTER TABLE Incident ALTER COLUMN incident_usercreate SET DEFAULT NULL;
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
ALTER TABLE IncidentPriority ALTER COLUMN incidentpriority_userupdate SET DEFAULT NULL;
ALTER TABLE IncidentPriority ADD CONSTRAINT incidentpriority_userupdate_userobm_id_fkey FOREIGN KEY (incidentpriority_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incidentpriority_usercreate to userobm_id
UPDATE IncidentPriority SET incidentpriority_usercreate = NULL WHERE incidentpriority_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND incidentpriority_usercreate IS NOT NULL;
ALTER TABLE IncidentPriority ALTER COLUMN incidentpriority_usercreate SET DEFAULT NULL;
ALTER TABLE IncidentPriority ADD CONSTRAINT incidentpriority_usercreate_userobm_id_fkey FOREIGN KEY (incidentpriority_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incidentresolutiontype_domain_id to domain_id
DELETE FROM IncidentResolutionType WHERE incidentresolutiontype_domain_id NOT IN (SELECT domain_id FROM Domain) AND incidentresolutiontype_domain_id IS NOT NULL;
ALTER TABLE IncidentResolutionType ADD CONSTRAINT incidentresolutiontype_domain_id_domain_id_fkey FOREIGN KEY (incidentresolutiontype_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from incidentresolutiontype_userupdate to userobm_id
UPDATE IncidentResolutionType SET incidentresolutiontype_userupdate = NULL WHERE incidentresolutiontype_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND incidentresolutiontype_userupdate IS NOT NULL;
ALTER TABLE IncidentResolutionType ALTER COLUMN incidentresolutiontype_userupdate SET DEFAULT NULL;
ALTER TABLE IncidentResolutionType ADD CONSTRAINT incidentresolutiontype_userupdate_userobm_id_fkey FOREIGN KEY (incidentresolutiontype_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incidentresolutiontype_usercreate to userobm_id
UPDATE IncidentResolutionType SET incidentresolutiontype_usercreate = NULL WHERE incidentresolutiontype_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND incidentresolutiontype_usercreate IS NOT NULL;
ALTER TABLE IncidentResolutionType ALTER COLUMN incidentresolutiontype_usercreate SET DEFAULT NULL;
ALTER TABLE IncidentResolutionType ADD CONSTRAINT incidentresolutiontype_usercreate_userobm_id_fkey FOREIGN KEY (incidentresolutiontype_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incidentstatus_domain_id to domain_id
DELETE FROM IncidentStatus WHERE incidentstatus_domain_id NOT IN (SELECT domain_id FROM Domain) AND incidentstatus_domain_id IS NOT NULL;
ALTER TABLE IncidentStatus ADD CONSTRAINT incidentstatus_domain_id_domain_id_fkey FOREIGN KEY (incidentstatus_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from incidentstatus_userupdate to userobm_id
UPDATE IncidentStatus SET incidentstatus_userupdate = NULL WHERE incidentstatus_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND incidentstatus_userupdate IS NOT NULL;
ALTER TABLE IncidentStatus ALTER COLUMN incidentstatus_userupdate SET DEFAULT NULL;
ALTER TABLE IncidentStatus ADD CONSTRAINT incidentstatus_userupdate_userobm_id_fkey FOREIGN KEY (incidentstatus_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from incidentstatus_usercreate to userobm_id
UPDATE IncidentStatus SET incidentstatus_usercreate = NULL WHERE incidentstatus_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND incidentstatus_usercreate IS NOT NULL;
ALTER TABLE IncidentStatus ALTER COLUMN incidentstatus_usercreate SET DEFAULT NULL;
ALTER TABLE IncidentStatus ADD CONSTRAINT incidentstatus_usercreate_userobm_id_fkey FOREIGN KEY (incidentstatus_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from invoice_domain_id to domain_id
DELETE FROM Invoice WHERE invoice_domain_id NOT IN (SELECT domain_id FROM Domain) AND invoice_domain_id IS NOT NULL;
ALTER TABLE Invoice ADD CONSTRAINT invoice_domain_id_domain_id_fkey FOREIGN KEY (invoice_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from invoice_company_id to company_id
DELETE FROM Invoice WHERE invoice_company_id NOT IN (SELECT company_id FROM Company) AND invoice_company_id IS NOT NULL;
ALTER TABLE Invoice ADD CONSTRAINT invoice_company_id_company_id_fkey FOREIGN KEY (invoice_company_id) REFERENCES Company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from invoice_project_id to project_id
UPDATE Invoice SET invoice_project_id=NULL WHERE invoice_project_id NOT IN (SELECT project_id FROM Project) AND invoice_project_id IS NOT NULL;
ALTER TABLE Invoice ADD CONSTRAINT invoice_project_id_project_id_fkey FOREIGN KEY (invoice_project_id) REFERENCES Project(project_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from invoice_deal_id to deal_id
UPDATE Invoice SET invoice_deal_id=NULL WHERE invoice_deal_id NOT IN (SELECT deal_id FROM Deal) AND invoice_deal_id IS NOT NULL;
ALTER TABLE Invoice ADD CONSTRAINT invoice_deal_id_deal_id_fkey FOREIGN KEY (invoice_deal_id) REFERENCES Deal(deal_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from invoice_userupdate to userobm_id
UPDATE Invoice SET invoice_userupdate = NULL WHERE invoice_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND invoice_userupdate IS NOT NULL;
ALTER TABLE Invoice ALTER COLUMN invoice_userupdate SET DEFAULT NULL;
ALTER TABLE Invoice ADD CONSTRAINT invoice_userupdate_userobm_id_fkey FOREIGN KEY (invoice_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from invoice_usercreate to userobm_id
UPDATE Invoice SET invoice_usercreate = NULL WHERE invoice_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND invoice_usercreate IS NOT NULL;
ALTER TABLE Invoice ALTER COLUMN invoice_usercreate SET DEFAULT NULL;
ALTER TABLE Invoice ADD CONSTRAINT invoice_usercreate_userobm_id_fkey FOREIGN KEY (invoice_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from kind_domain_id to domain_id
DELETE FROM Kind WHERE kind_domain_id NOT IN (SELECT domain_id FROM Domain) AND kind_domain_id IS NOT NULL;
ALTER TABLE Kind ADD CONSTRAINT kind_domain_id_domain_id_fkey FOREIGN KEY (kind_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from kind_userupdate to userobm_id
UPDATE Kind SET kind_userupdate = NULL WHERE kind_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND kind_userupdate IS NOT NULL;
ALTER TABLE Kind ALTER COLUMN kind_userupdate SET DEFAULT NULL;
ALTER TABLE Kind ADD CONSTRAINT kind_userupdate_userobm_id_fkey FOREIGN KEY (kind_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from kind_usercreate to userobm_id
UPDATE Kind SET kind_usercreate = NULL WHERE kind_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND kind_usercreate IS NOT NULL;
ALTER TABLE Kind ALTER COLUMN kind_usercreate SET DEFAULT NULL;
ALTER TABLE Kind ADD CONSTRAINT kind_usercreate_userobm_id_fkey FOREIGN KEY (kind_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from lead_domain_id to domain_id
DELETE FROM Lead WHERE lead_domain_id NOT IN (SELECT domain_id FROM Domain) AND lead_domain_id IS NOT NULL;
ALTER TABLE Lead ADD CONSTRAINT lead_domain_id_domain_id_fkey FOREIGN KEY (lead_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from lead_company_id to company_id
DELETE FROM Lead WHERE lead_company_id NOT IN (SELECT company_id FROM Company) AND lead_company_id IS NOT NULL;
ALTER TABLE Lead ADD CONSTRAINT lead_company_id_company_id_fkey FOREIGN KEY (lead_company_id) REFERENCES Company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from lead_userupdate to userobm_id
UPDATE Lead SET lead_userupdate = NULL WHERE lead_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND lead_userupdate IS NOT NULL;
ALTER TABLE Lead ALTER COLUMN lead_userupdate SET DEFAULT NULL;
ALTER TABLE Lead ADD CONSTRAINT lead_userupdate_userobm_id_fkey FOREIGN KEY (lead_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from lead_usercreate to userobm_id
UPDATE Lead SET lead_usercreate = NULL WHERE lead_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND lead_usercreate IS NOT NULL;
ALTER TABLE Lead ALTER COLUMN lead_usercreate SET DEFAULT NULL;
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
ALTER TABLE LeadSource ALTER COLUMN leadsource_userupdate SET DEFAULT NULL;
ALTER TABLE LeadSource ADD CONSTRAINT leadsource_userupdate_userobm_id_fkey FOREIGN KEY (leadsource_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from leadsource_usercreate to userobm_id
UPDATE LeadSource SET leadsource_usercreate = NULL WHERE leadsource_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND leadsource_usercreate IS NOT NULL;
ALTER TABLE LeadSource ALTER COLUMN leadsource_usercreate SET DEFAULT NULL;
ALTER TABLE LeadSource ADD CONSTRAINT leadsource_usercreate_userobm_id_fkey FOREIGN KEY (leadsource_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from leadstatus_domain_id to domain_id
DELETE FROM LeadStatus WHERE leadstatus_domain_id NOT IN (SELECT domain_id FROM Domain) AND leadstatus_domain_id IS NOT NULL;
ALTER TABLE LeadStatus ADD CONSTRAINT leadstatus_domain_id_domain_id_fkey FOREIGN KEY (leadstatus_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from leadstatus_userupdate to userobm_id
UPDATE LeadStatus SET leadstatus_userupdate = NULL WHERE leadstatus_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND leadstatus_userupdate IS NOT NULL;
ALTER TABLE LeadStatus ALTER COLUMN leadstatus_userupdate SET DEFAULT NULL;
ALTER TABLE LeadStatus ADD CONSTRAINT leadstatus_userupdate_userobm_id_fkey FOREIGN KEY (leadstatus_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from leadstatus_usercreate to userobm_id
UPDATE LeadStatus SET leadstatus_usercreate = NULL WHERE leadstatus_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND leadstatus_usercreate IS NOT NULL;
ALTER TABLE LeadStatus ALTER COLUMN leadstatus_usercreate SET DEFAULT NULL;
ALTER TABLE LeadStatus ADD CONSTRAINT leadstatus_usercreate_userobm_id_fkey FOREIGN KEY (leadstatus_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from list_domain_id to domain_id
DELETE FROM List WHERE list_domain_id NOT IN (SELECT domain_id FROM Domain) AND list_domain_id IS NOT NULL;
ALTER TABLE List ADD CONSTRAINT list_domain_id_domain_id_fkey FOREIGN KEY (list_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from list_userupdate to userobm_id
UPDATE List SET list_userupdate = NULL WHERE list_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND list_userupdate IS NOT NULL;
ALTER TABLE List ALTER COLUMN list_userupdate SET DEFAULT NULL;
ALTER TABLE List ADD CONSTRAINT list_userupdate_userobm_id_fkey FOREIGN KEY (list_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from list_usercreate to userobm_id
UPDATE List SET list_usercreate = NULL WHERE list_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND list_usercreate IS NOT NULL;
ALTER TABLE List ALTER COLUMN list_usercreate SET DEFAULT NULL;
ALTER TABLE List ADD CONSTRAINT list_usercreate_userobm_id_fkey FOREIGN KEY (list_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from mailshare_domain_id to domain_id
DELETE FROM MailShare WHERE mailshare_domain_id NOT IN (SELECT domain_id FROM Domain) AND mailshare_domain_id IS NOT NULL;
ALTER TABLE MailShare ADD CONSTRAINT mailshare_domain_id_domain_id_fkey FOREIGN KEY (mailshare_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from mailshare_mail_server_id to mailserver_id
-- DELETE FROM MailShare WHERE mailshare_mail_server_id NOT IN (SELECT mailserver_id FROM MailServer) AND mailshare_mail_server_id IS NOT NULL;
-- ALTER TABLE MailShare ADD CONSTRAINT mailshare_mail_server_id_mailserver_id_fkey FOREIGN KEY (mailshare_mail_server_id) REFERENCES MailServer(mailserver_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from mailshare_userupdate to userobm_id
UPDATE MailShare SET mailshare_userupdate = NULL WHERE mailshare_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND mailshare_userupdate IS NOT NULL;
ALTER TABLE MailShare ALTER COLUMN mailshare_userupdate SET DEFAULT NULL;
ALTER TABLE MailShare ADD CONSTRAINT mailshare_userupdate_userobm_id_fkey FOREIGN KEY (mailshare_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from mailshare_usercreate to userobm_id
UPDATE MailShare SET mailshare_usercreate = NULL WHERE mailshare_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND mailshare_usercreate IS NOT NULL;
ALTER TABLE MailShare ALTER COLUMN mailshare_usercreate SET DEFAULT NULL;
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
ALTER TABLE OGroup ALTER COLUMN ogroup_userupdate SET DEFAULT NULL;
ALTER TABLE OGroup ADD CONSTRAINT ogroup_userupdate_userobm_id_fkey FOREIGN KEY (ogroup_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from ogroup_usercreate to userobm_id
UPDATE OGroup SET ogroup_usercreate = NULL WHERE ogroup_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND ogroup_usercreate IS NOT NULL;
ALTER TABLE OGroup ALTER COLUMN ogroup_usercreate SET DEFAULT NULL;
ALTER TABLE OGroup ADD CONSTRAINT ogroup_usercreate_userobm_id_fkey FOREIGN KEY (ogroup_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from ogrouplink_ogroup_id to ogroup_id
DELETE FROM OGroupLink WHERE ogrouplink_ogroup_id NOT IN (SELECT ogroup_id FROM OGroup) AND ogrouplink_ogroup_id IS NOT NULL;
ALTER TABLE OGroupLink ADD CONSTRAINT ogrouplink_ogroup_id_ogroup_id_fkey FOREIGN KEY (ogrouplink_ogroup_id) REFERENCES OGroup(ogroup_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from ogrouplink_domain_id to domain_id
DELETE FROM OGroupLink WHERE ogrouplink_domain_id NOT IN (SELECT domain_id FROM Domain) AND ogrouplink_domain_id IS NOT NULL;
ALTER TABLE OGroupLink ADD CONSTRAINT ogrouplink_domain_id_domain_id_fkey FOREIGN KEY (ogrouplink_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from ogrouplink_userupdate to userobm_id
UPDATE OGroupLink SET ogrouplink_userupdate = NULL WHERE ogrouplink_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND ogrouplink_userupdate IS NOT NULL;
ALTER TABLE OGroupLink ALTER COLUMN ogrouplink_userupdate SET DEFAULT NULL;
ALTER TABLE OGroupLink ADD CONSTRAINT ogrouplink_userupdate_userobm_id_fkey FOREIGN KEY (ogrouplink_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from ogrouplink_usercreate to userobm_id
UPDATE OGroupLink SET ogrouplink_usercreate = NULL WHERE ogrouplink_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND ogrouplink_usercreate IS NOT NULL;
ALTER TABLE OGroupLink ALTER COLUMN ogrouplink_usercreate SET DEFAULT NULL;
ALTER TABLE OGroupLink ADD CONSTRAINT ogrouplink_usercreate_userobm_id_fkey FOREIGN KEY (ogrouplink_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from obmbookmark_user_id to userobm_id
DELETE FROM ObmBookmark WHERE obmbookmark_user_id NOT IN (SELECT userobm_id FROM UserObm) AND obmbookmark_user_id IS NOT NULL;
ALTER TABLE ObmBookmark ADD CONSTRAINT obmbookmark_user_id_userobm_id_fkey FOREIGN KEY (obmbookmark_user_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from obmbookmarkproperty_bookmark_id to obmbookmark_id
DELETE FROM ObmBookmarkProperty WHERE obmbookmarkproperty_bookmark_id NOT IN (SELECT obmbookmark_id FROM ObmBookmark) AND obmbookmarkproperty_bookmark_id IS NOT NULL;
ALTER TABLE ObmBookmarkProperty ADD CONSTRAINT obmbookmarkproperty_bookmark_id_obmbookmark_id_fkey FOREIGN KEY (obmbookmarkproperty_bookmark_id) REFERENCES ObmBookmark(obmbookmark_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from organizationalchart_domain_id to domain_id
DELETE FROM OrganizationalChart WHERE organizationalchart_domain_id NOT IN (SELECT domain_id FROM Domain) AND organizationalchart_domain_id IS NOT NULL;
ALTER TABLE OrganizationalChart ADD CONSTRAINT organizationalchart_domain_id_domain_id_fkey FOREIGN KEY (organizationalchart_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from organizationalchart_userupdate to userobm_id
UPDATE OrganizationalChart SET organizationalchart_userupdate = NULL WHERE organizationalchart_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND organizationalchart_userupdate IS NOT NULL;
ALTER TABLE OrganizationalChart ALTER COLUMN organizationalchart_userupdate SET DEFAULT NULL;
ALTER TABLE OrganizationalChart ADD CONSTRAINT organizationalchart_userupdate_userobm_id_fkey FOREIGN KEY (organizationalchart_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from organizationalchart_usercreate to userobm_id
UPDATE OrganizationalChart SET organizationalchart_usercreate = NULL WHERE organizationalchart_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND organizationalchart_usercreate IS NOT NULL;
ALTER TABLE OrganizationalChart ALTER COLUMN organizationalchart_usercreate SET DEFAULT NULL;
ALTER TABLE OrganizationalChart ADD CONSTRAINT organizationalchart_usercreate_userobm_id_fkey FOREIGN KEY (organizationalchart_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from parentdeal_domain_id to domain_id
DELETE FROM ParentDeal WHERE parentdeal_domain_id NOT IN (SELECT domain_id FROM Domain) AND parentdeal_domain_id IS NOT NULL;
ALTER TABLE ParentDeal ADD CONSTRAINT parentdeal_domain_id_domain_id_fkey FOREIGN KEY (parentdeal_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from parentdeal_userupdate to userobm_id
UPDATE ParentDeal SET parentdeal_userupdate = NULL WHERE parentdeal_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND parentdeal_userupdate IS NOT NULL;
ALTER TABLE ParentDeal ALTER COLUMN parentdeal_userupdate SET DEFAULT NULL;
ALTER TABLE ParentDeal ADD CONSTRAINT parentdeal_userupdate_userobm_id_fkey FOREIGN KEY (parentdeal_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from parentdeal_usercreate to userobm_id
UPDATE ParentDeal SET parentdeal_usercreate = NULL WHERE parentdeal_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND parentdeal_usercreate IS NOT NULL;
ALTER TABLE ParentDeal ALTER COLUMN parentdeal_usercreate SET DEFAULT NULL;
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
-- DELETE FROM Payment WHERE payment_account_id NOT IN (SELECT account_id FROM Account) AND payment_account_id IS NOT NULL;
ALTER TABLE Payment ADD CONSTRAINT payment_account_id_account_id_fkey FOREIGN KEY (payment_account_id) REFERENCES Account(account_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from payment_userupdate to userobm_id
UPDATE Payment SET payment_userupdate = NULL WHERE payment_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND payment_userupdate IS NOT NULL;
ALTER TABLE Payment ALTER COLUMN payment_userupdate SET DEFAULT NULL;
ALTER TABLE Payment ADD CONSTRAINT payment_userupdate_userobm_id_fkey FOREIGN KEY (payment_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from payment_usercreate to userobm_id
UPDATE Payment SET payment_usercreate = NULL WHERE payment_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND payment_usercreate IS NOT NULL;
ALTER TABLE Payment ALTER COLUMN payment_usercreate SET DEFAULT NULL;
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
ALTER TABLE PaymentInvoice ALTER COLUMN paymentinvoice_usercreate SET DEFAULT NULL;
ALTER TABLE PaymentInvoice ADD CONSTRAINT paymentinvoice_usercreate_userobm_id_fkey FOREIGN KEY (paymentinvoice_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from paymentinvoice_userupdate to userobm_id
UPDATE PaymentInvoice SET paymentinvoice_userupdate = NULL WHERE paymentinvoice_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND paymentinvoice_userupdate IS NOT NULL;
ALTER TABLE PaymentInvoice ALTER COLUMN paymentinvoice_userupdate SET DEFAULT NULL;
ALTER TABLE PaymentInvoice ADD CONSTRAINT paymentinvoice_userupdate_userobm_id_fkey FOREIGN KEY (paymentinvoice_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from paymentkind_domain_id to domain_id
DELETE FROM PaymentKind WHERE paymentkind_domain_id NOT IN (SELECT domain_id FROM Domain) AND paymentkind_domain_id IS NOT NULL;
ALTER TABLE PaymentKind ADD CONSTRAINT paymentkind_domain_id_domain_id_fkey FOREIGN KEY (paymentkind_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from project_domain_id to domain_id
DELETE FROM Project WHERE project_domain_id NOT IN (SELECT domain_id FROM Domain) AND project_domain_id IS NOT NULL;
ALTER TABLE Project ADD CONSTRAINT project_domain_id_domain_id_fkey FOREIGN KEY (project_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from project_deal_id to deal_id
UPDATE Project SET project_deal_id=NULL WHERE project_deal_id NOT IN (SELECT deal_id FROM Deal) AND project_deal_id IS NOT NULL;
ALTER TABLE Project ADD CONSTRAINT project_deal_id_deal_id_fkey FOREIGN KEY (project_deal_id) REFERENCES Deal(deal_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from project_company_id to company_id
UPDATE Project SET project_company_id=NULL WHERE project_company_id NOT IN (SELECT company_id FROM Company) AND project_company_id IS NOT NULL;
ALTER TABLE Project ADD CONSTRAINT project_company_id_company_id_fkey FOREIGN KEY (project_company_id) REFERENCES Company(company_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from project_userupdate to userobm_id
UPDATE Project SET project_userupdate = NULL WHERE project_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND project_userupdate IS NOT NULL;
ALTER TABLE Project ALTER COLUMN project_userupdate SET DEFAULT NULL;
ALTER TABLE Project ADD CONSTRAINT project_userupdate_userobm_id_fkey FOREIGN KEY (project_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from project_usercreate to userobm_id
UPDATE Project SET project_usercreate = NULL WHERE project_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND project_usercreate IS NOT NULL;
ALTER TABLE Project ALTER COLUMN project_usercreate SET DEFAULT NULL;
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
ALTER TABLE ProjectClosing ALTER COLUMN projectclosing_userupdate SET DEFAULT NULL;
ALTER TABLE ProjectClosing ADD CONSTRAINT projectclosing_userupdate_userobm_id_fkey FOREIGN KEY (projectclosing_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from projectclosing_usercreate to userobm_id
UPDATE ProjectClosing SET projectclosing_usercreate = NULL WHERE projectclosing_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND projectclosing_usercreate IS NOT NULL;
ALTER TABLE ProjectClosing ALTER COLUMN projectclosing_usercreate SET DEFAULT NULL;
ALTER TABLE ProjectClosing ADD CONSTRAINT projectclosing_usercreate_userobm_id_fkey FOREIGN KEY (projectclosing_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from projectreftask_tasktype_id to tasktype_id
DELETE FROM ProjectRefTask WHERE projectreftask_tasktype_id NOT IN (SELECT tasktype_id FROM TaskType) AND projectreftask_tasktype_id IS NOT NULL;
ALTER TABLE ProjectRefTask ADD CONSTRAINT projectreftask_tasktype_id_tasktype_id_fkey FOREIGN KEY (projectreftask_tasktype_id) REFERENCES TaskType(tasktype_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from projectreftask_userupdate to userobm_id
UPDATE ProjectRefTask SET projectreftask_userupdate = NULL WHERE projectreftask_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND projectreftask_userupdate IS NOT NULL;
ALTER TABLE ProjectRefTask ALTER COLUMN projectreftask_userupdate SET DEFAULT NULL;
ALTER TABLE ProjectRefTask ADD CONSTRAINT projectreftask_userupdate_userobm_id_fkey FOREIGN KEY (projectreftask_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from projectreftask_usercreate to userobm_id
UPDATE ProjectRefTask SET projectreftask_usercreate = NULL WHERE projectreftask_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND projectreftask_usercreate IS NOT NULL;
ALTER TABLE ProjectRefTask ALTER COLUMN projectreftask_usercreate SET DEFAULT NULL;
ALTER TABLE ProjectRefTask ADD CONSTRAINT projectreftask_usercreate_userobm_id_fkey FOREIGN KEY (projectreftask_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from projecttask_project_id to project_id
DELETE FROM ProjectTask WHERE projecttask_project_id NOT IN (SELECT project_id FROM Project) AND projecttask_project_id IS NOT NULL;
ALTER TABLE ProjectTask ADD CONSTRAINT projecttask_project_id_project_id_fkey FOREIGN KEY (projecttask_project_id) REFERENCES Project(project_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from projecttask_parenttask_id to projecttask_id
UPDATE ProjectTask SET projecttask_parenttask_id = NULL WHERE projecttask_parenttask_id = 0;
DELETE FROM ProjectTask WHERE projecttask_parenttask_id NOT IN (SELECT projecttask_id FROM ProjectTask) AND projecttask_parenttask_id IS NOT NULL;
ALTER TABLE ProjectTask ADD CONSTRAINT projecttask_parenttask_id_projecttask_id_fkey FOREIGN KEY (projecttask_parenttask_id) REFERENCES ProjectTask(projecttask_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from projecttask_userupdate to userobm_id
UPDATE ProjectTask SET projecttask_userupdate = NULL WHERE projecttask_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND projecttask_userupdate IS NOT NULL;
ALTER TABLE ProjectTask ALTER COLUMN projecttask_userupdate SET DEFAULT NULL;
ALTER TABLE ProjectTask ADD CONSTRAINT projecttask_userupdate_userobm_id_fkey FOREIGN KEY (projecttask_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from projecttask_usercreate to userobm_id
UPDATE ProjectTask SET projecttask_usercreate = NULL WHERE projecttask_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND projecttask_usercreate IS NOT NULL;
ALTER TABLE ProjectTask ALTER COLUMN projecttask_usercreate SET DEFAULT NULL;
ALTER TABLE ProjectTask ADD CONSTRAINT projecttask_usercreate_userobm_id_fkey FOREIGN KEY (projecttask_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from projectuser_project_id to project_id
DELETE FROM ProjectUser WHERE projectuser_project_id NOT IN (SELECT project_id FROM Project) AND projectuser_project_id IS NOT NULL;
ALTER TABLE ProjectUser ADD CONSTRAINT projectuser_project_id_project_id_fkey FOREIGN KEY (projectuser_project_id) REFERENCES Project(project_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from projectuser_user_id to userobm_id
DELETE FROM ProjectUser WHERE projectuser_user_id NOT IN (SELECT userobm_id FROM UserObm) AND projectuser_user_id IS NOT NULL;
ALTER TABLE ProjectUser ADD CONSTRAINT projectuser_user_id_userobm_id_fkey FOREIGN KEY (projectuser_user_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from projectuser_userupdate to userobm_id
UPDATE ProjectUser SET projectuser_userupdate = NULL WHERE projectuser_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND projectuser_userupdate IS NOT NULL;
ALTER TABLE ProjectUser ALTER COLUMN projectuser_userupdate SET DEFAULT NULL;
ALTER TABLE ProjectUser ADD CONSTRAINT projectuser_userupdate_userobm_id_fkey FOREIGN KEY (projectuser_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from projectuser_usercreate to userobm_id
UPDATE ProjectUser SET projectuser_usercreate = NULL WHERE projectuser_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND projectuser_usercreate IS NOT NULL;
ALTER TABLE ProjectUser ALTER COLUMN projectuser_usercreate SET DEFAULT NULL;
ALTER TABLE ProjectUser ADD CONSTRAINT projectuser_usercreate_userobm_id_fkey FOREIGN KEY (projectuser_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from publication_domain_id to domain_id
DELETE FROM Publication WHERE publication_domain_id NOT IN (SELECT domain_id FROM Domain) AND publication_domain_id IS NOT NULL;
ALTER TABLE Publication ADD CONSTRAINT publication_domain_id_domain_id_fkey FOREIGN KEY (publication_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from publication_userupdate to userobm_id
UPDATE Publication SET publication_userupdate = NULL WHERE publication_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND publication_userupdate IS NOT NULL;
ALTER TABLE Publication ALTER COLUMN publication_userupdate SET DEFAULT NULL;
ALTER TABLE Publication ADD CONSTRAINT publication_userupdate_userobm_id_fkey FOREIGN KEY (publication_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from publication_usercreate to userobm_id
UPDATE Publication SET publication_usercreate = NULL WHERE publication_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND publication_usercreate IS NOT NULL;
ALTER TABLE Publication ALTER COLUMN publication_usercreate SET DEFAULT NULL;
ALTER TABLE Publication ADD CONSTRAINT publication_usercreate_userobm_id_fkey FOREIGN KEY (publication_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from publication_type_id to publicationtype_id
UPDATE Publication SET publication_type_id = NULL WHERE publication_type_id NOT IN (SELECT publicationtype_id FROM PublicationType) AND publication_type_id IS NOT NULL;
ALTER TABLE Publication ADD CONSTRAINT publication_type_id_publicationtype_id_fkey FOREIGN KEY (publication_type_id) REFERENCES PublicationType(publicationtype_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from publicationtype_domain_id to domain_id
DELETE FROM PublicationType WHERE publicationtype_domain_id NOT IN (SELECT domain_id FROM Domain) AND publicationtype_domain_id IS NOT NULL;
ALTER TABLE PublicationType ADD CONSTRAINT publicationtype_domain_id_domain_id_fkey FOREIGN KEY (publicationtype_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from publicationtype_userupdate to userobm_id
UPDATE PublicationType SET publicationtype_userupdate = NULL WHERE publicationtype_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND publicationtype_userupdate IS NOT NULL;
ALTER TABLE PublicationType ALTER COLUMN publicationtype_userupdate SET DEFAULT NULL;
ALTER TABLE PublicationType ADD CONSTRAINT publicationtype_userupdate_userobm_id_fkey FOREIGN KEY (publicationtype_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from publicationtype_usercreate to userobm_id
UPDATE PublicationType SET publicationtype_usercreate = NULL WHERE publicationtype_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND publicationtype_usercreate IS NOT NULL;
ALTER TABLE PublicationType ALTER COLUMN publicationtype_usercreate SET DEFAULT NULL;
ALTER TABLE PublicationType ADD CONSTRAINT publicationtype_usercreate_userobm_id_fkey FOREIGN KEY (publicationtype_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from rgroup_domain_id to domain_id
DELETE FROM RGroup WHERE rgroup_domain_id NOT IN (SELECT domain_id FROM Domain) AND rgroup_domain_id IS NOT NULL;
ALTER TABLE RGroup ADD CONSTRAINT rgroup_domain_id_domain_id_fkey FOREIGN KEY (rgroup_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from rgroup_userupdate to userobm_id
UPDATE RGroup SET rgroup_userupdate = NULL WHERE rgroup_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND rgroup_userupdate IS NOT NULL;
ALTER TABLE RGroup ALTER COLUMN rgroup_userupdate SET DEFAULT NULL;
ALTER TABLE RGroup ADD CONSTRAINT rgroup_userupdate_userobm_id_fkey FOREIGN KEY (rgroup_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from rgroup_usercreate to userobm_id
UPDATE RGroup SET rgroup_usercreate = NULL WHERE rgroup_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND rgroup_usercreate IS NOT NULL;
ALTER TABLE RGroup ALTER COLUMN rgroup_usercreate SET DEFAULT NULL;
ALTER TABLE RGroup ADD CONSTRAINT rgroup_usercreate_userobm_id_fkey FOREIGN KEY (rgroup_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from region_domain_id to domain_id
DELETE FROM Region WHERE region_domain_id NOT IN (SELECT domain_id FROM Domain) AND region_domain_id IS NOT NULL;
ALTER TABLE Region ADD CONSTRAINT region_domain_id_domain_id_fkey FOREIGN KEY (region_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from region_userupdate to userobm_id
UPDATE Region SET region_userupdate = NULL WHERE region_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND region_userupdate IS NOT NULL;
ALTER TABLE Region ALTER COLUMN region_userupdate SET DEFAULT NULL;
ALTER TABLE Region ADD CONSTRAINT region_userupdate_userobm_id_fkey FOREIGN KEY (region_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from region_usercreate to userobm_id
UPDATE Region SET region_usercreate = NULL WHERE region_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND region_usercreate IS NOT NULL;
ALTER TABLE Region ALTER COLUMN region_usercreate SET DEFAULT NULL;
ALTER TABLE Region ADD CONSTRAINT region_usercreate_userobm_id_fkey FOREIGN KEY (region_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from resource_domain_id to domain_id
DELETE FROM Resource WHERE resource_domain_id NOT IN (SELECT domain_id FROM Domain) AND resource_domain_id IS NOT NULL;
ALTER TABLE Resource ADD CONSTRAINT resource_domain_id_domain_id_fkey FOREIGN KEY (resource_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from resource_userupdate to userobm_id
UPDATE Resource SET resource_userupdate = NULL WHERE resource_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND resource_userupdate IS NOT NULL;
ALTER TABLE Resource ALTER COLUMN resource_userupdate SET DEFAULT NULL;
ALTER TABLE Resource ADD CONSTRAINT resource_userupdate_userobm_id_fkey FOREIGN KEY (resource_userupdate) REFERENCES UserObm (userobm_id) ON DELETE SET NULL ON UPDATE CASCADE;

-- Foreign key from resource_usercreate to userobm_id
UPDATE Resource SET resource_usercreate = NULL WHERE resource_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND resource_usercreate IS NOT NULL;
ALTER TABLE Resource ALTER COLUMN resource_usercreate SET DEFAULT NULL;
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
ALTER TABLE Subscription ALTER COLUMN subscription_userupdate SET DEFAULT NULL;
ALTER TABLE Subscription ADD CONSTRAINT subscription_userupdate_userobm_id_fkey FOREIGN KEY (subscription_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from subscription_usercreate to userobm_id
UPDATE Subscription SET subscription_usercreate = NULL WHERE subscription_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND subscription_usercreate IS NOT NULL;
ALTER TABLE Subscription ALTER COLUMN subscription_usercreate SET DEFAULT NULL;
ALTER TABLE Subscription ADD CONSTRAINT subscription_usercreate_userobm_id_fkey FOREIGN KEY (subscription_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from subscription_reception_id to subscriptionreception_id
UPDATE Subscription SET subscription_reception_id = NULL WHERE subscription_reception_id NOT IN (SELECT subscriptionreception_id FROM SubscriptionReception) AND subscription_reception_id IS NOT NULL;
ALTER TABLE Subscription ADD CONSTRAINT subscription_reception_id_subscriptionreception_id_fkey FOREIGN KEY (subscription_reception_id) REFERENCES SubscriptionReception(subscriptionreception_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from subscriptionreception_domain_id to domain_id
DELETE FROM SubscriptionReception WHERE subscriptionreception_domain_id NOT IN (SELECT domain_id FROM Domain) AND subscriptionreception_domain_id IS NOT NULL;
ALTER TABLE SubscriptionReception ADD CONSTRAINT subscriptionreception_domain_id_domain_id_fkey FOREIGN KEY (subscriptionreception_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from subscriptionreception_userupdate to userobm_id
UPDATE SubscriptionReception SET subscriptionreception_userupdate = NULL WHERE subscriptionreception_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND subscriptionreception_userupdate IS NOT NULL;
ALTER TABLE SubscriptionReception ALTER COLUMN subscriptionreception_userupdate SET DEFAULT NULL;
ALTER TABLE SubscriptionReception ADD CONSTRAINT subscriptionreception_userupdate_userobm_id_fkey FOREIGN KEY (subscriptionreception_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from subscriptionreception_usercreate to userobm_id
UPDATE SubscriptionReception SET subscriptionreception_usercreate = NULL WHERE subscriptionreception_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND subscriptionreception_usercreate IS NOT NULL;
ALTER TABLE SubscriptionReception ALTER COLUMN subscriptionreception_usercreate SET DEFAULT NULL;
ALTER TABLE SubscriptionReception ADD CONSTRAINT subscriptionreception_usercreate_userobm_id_fkey FOREIGN KEY (subscriptionreception_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from tasktype_domain_id to domain_id
DELETE FROM TaskType WHERE tasktype_domain_id NOT IN (SELECT domain_id FROM Domain) AND tasktype_domain_id IS NOT NULL;
ALTER TABLE TaskType ADD CONSTRAINT tasktype_domain_id_domain_id_fkey FOREIGN KEY (tasktype_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from tasktype_userupdate to userobm_id
UPDATE TaskType SET tasktype_userupdate = NULL WHERE tasktype_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND tasktype_userupdate IS NOT NULL;
ALTER TABLE TaskType ALTER COLUMN tasktype_userupdate SET DEFAULT NULL;
ALTER TABLE TaskType ADD CONSTRAINT tasktype_userupdate_userobm_id_fkey FOREIGN KEY (tasktype_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from tasktype_usercreate to userobm_id
UPDATE TaskType SET tasktype_usercreate = NULL WHERE tasktype_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND tasktype_usercreate IS NOT NULL;
ALTER TABLE TaskType ALTER COLUMN tasktype_usercreate SET DEFAULT NULL;
ALTER TABLE TaskType ADD CONSTRAINT tasktype_usercreate_userobm_id_fkey FOREIGN KEY (tasktype_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from timetask_user_id to userobm_id
DELETE FROM TimeTask WHERE timetask_user_id NOT IN (SELECT userobm_id FROM UserObm) AND timetask_user_id IS NOT NULL;
ALTER TABLE TimeTask ADD CONSTRAINT timetask_user_id_userobm_id_fkey FOREIGN KEY (timetask_user_id) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from timetask_projecttask_id to projecttask_id
UPDATE TimeTask SET timetask_projecttask_id=NULL WHERE timetask_projecttask_id NOT IN (SELECT projecttask_id FROM ProjectTask) AND timetask_projecttask_id IS NOT NULL;
ALTER TABLE TimeTask ADD CONSTRAINT timetask_projecttask_id_projecttask_id_fkey FOREIGN KEY (timetask_projecttask_id) REFERENCES ProjectTask(projecttask_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from timetask_tasktype_id to tasktype_id
DELETE FROM TimeTask WHERE timetask_tasktype_id NOT IN (SELECT tasktype_id FROM TaskType) AND timetask_tasktype_id IS NOT NULL;
ALTER TABLE TimeTask ADD CONSTRAINT timetask_tasktype_id_tasktype_id_fkey FOREIGN KEY (timetask_tasktype_id) REFERENCES TaskType(tasktype_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from timetask_userupdate to userobm_id
UPDATE TimeTask SET timetask_userupdate = NULL WHERE timetask_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND timetask_userupdate IS NOT NULL;
ALTER TABLE TimeTask ALTER COLUMN timetask_userupdate SET DEFAULT NULL;
ALTER TABLE TimeTask ADD CONSTRAINT timetask_userupdate_userobm_id_fkey FOREIGN KEY (timetask_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from timetask_usercreate to userobm_id
UPDATE TimeTask SET timetask_usercreate = NULL WHERE timetask_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND timetask_usercreate IS NOT NULL;
ALTER TABLE TimeTask ALTER COLUMN timetask_usercreate SET DEFAULT NULL;
ALTER TABLE TimeTask ADD CONSTRAINT timetask_usercreate_userobm_id_fkey FOREIGN KEY (timetask_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from group_domain_id to domain_id
DELETE FROM UGroup WHERE group_domain_id NOT IN (SELECT domain_id FROM Domain) AND group_domain_id IS NOT NULL;
ALTER TABLE UGroup ADD CONSTRAINT group_domain_id_domain_id_fkey FOREIGN KEY (group_domain_id) REFERENCES Domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from group_userupdate to userobm_id
UPDATE UGroup SET group_userupdate = NULL WHERE group_userupdate NOT IN (SELECT userobm_id FROM UserObm) AND group_userupdate IS NOT NULL;
ALTER TABLE UGroup ALTER COLUMN group_userupdate SET DEFAULT NULL;
ALTER TABLE UGroup ADD CONSTRAINT group_userupdate_userobm_id_fkey FOREIGN KEY (group_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from group_usercreate to userobm_id
UPDATE UGroup SET group_usercreate = NULL WHERE group_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND group_usercreate IS NOT NULL;
ALTER TABLE UGroup ALTER COLUMN group_usercreate SET DEFAULT NULL;
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
ALTER TABLE UserObm ALTER COLUMN userobm_userupdate SET DEFAULT NULL;
ALTER TABLE UserObm ADD CONSTRAINT userobm_userupdate_userobm_id_fkey FOREIGN KEY (userobm_userupdate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

-- Foreign key from userobm_usercreate to userobm_id
UPDATE UserObm SET userobm_usercreate = NULL WHERE userobm_usercreate NOT IN (SELECT userobm_id FROM UserObm) AND userobm_usercreate IS NOT NULL;
ALTER TABLE UserObm ALTER COLUMN userobm_usercreate SET DEFAULT NULL;
ALTER TABLE UserObm ADD CONSTRAINT userobm_usercreate_userobm_id_fkey FOREIGN KEY (userobm_usercreate) REFERENCES UserObm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

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

-- Foreign key from profileproperty_profile_id to profile_id
ALTER TABLE ProfileProperty ADD CONSTRAINT profileproperty_profile_id_profile_id_fkey FOREIGN KEY (profileproperty_profile_id) REFERENCES Profile(profile_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contact_birthday_id to event_id
ALTER TABLE Contact ADD CONSTRAINT contact_birthday_id_event_id_fkey FOREIGN KEY (contact_birthday_id) REFERENCES Event(event_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contact_anniversary_id to event_id
ALTER TABLE Contact ADD CONSTRAINT contact_anniversary_id_event_id_fkey FOREIGN KEY (contact_anniversary_id) REFERENCES Event(event_id) ON UPDATE CASCADE ON DELETE CASCADE;

-- Foreign key from contact_photo_id to document_id
ALTER TABLE Contact ADD CONSTRAINT contact_photo_id_document_id_fkey FOREIGN KEY (contact_photo_id) REFERENCES Document(document_id) ON UPDATE CASCADE ON DELETE SET NULL;


ALTER TABLE CategoryLink ADD CONSTRAINT categorylink_entity_id_entity_id_fkey FOREIGN KEY (categorylink_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE EventLink ADD CONSTRAINT eventlink_entity_id_entity_id_fkey FOREIGN KEY (eventlink_entity_id) REFERENCES Entity (entity_id) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE EntityRight ADD CONSTRAINT entityright_entity_id_entity_id FOREIGN KEY (entityright_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE EntityRight ADD CONSTRAINT entityright_consumer_id_entity_id FOREIGN KEY (entityright_consumer_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE DocumentLink ADD CONSTRAINT documentlink_entity_id_entity_id_fkey FOREIGN KEY (documentlink_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE OGroupLink ADD CONSTRAINT ogrouplink_entity_id_entity_id_fkey FOREIGN KEY (ogrouplink_entity_id) REFERENCES Entity (entity_id) ON DELETE SET NULL ON UPDATE CASCADE;

ALTER TABLE AccountEntity ADD CONSTRAINT accountentity_account_id_account_id_fkey FOREIGN KEY (accountentity_account_id) REFERENCES Account (account_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE AccountEntity ADD CONSTRAINT accountentity_entity_id_entity_id_fkey FOREIGN KEY (accountentity_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE CvEntity ADD CONSTRAINT cventity_cv_id_cv_id_fkey FOREIGN KEY (cventity_cv_id) REFERENCES CV (cv_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE CvEntity ADD CONSTRAINT cventity_entity_id_entity_id_fkey FOREIGN KEY (cventity_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE CalendarEntity ADD CONSTRAINT calendarentity_calendar_id_calendar_id_fkey FOREIGN KEY (calendarentity_calendar_id) REFERENCES UserObm (userobm_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE CalendarEntity ADD CONSTRAINT calendarentity_entity_id_entity_id_fkey FOREIGN KEY (calendarentity_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE CompanyEntity ADD CONSTRAINT companyentity_company_id_company_id_fkey FOREIGN KEY (companyentity_company_id) REFERENCES Company (company_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE CompanyEntity ADD CONSTRAINT companyentity_entity_id_entity_id_fkey FOREIGN KEY (companyentity_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE ContactEntity ADD CONSTRAINT contactentity_contact_id_contact_id_fkey FOREIGN KEY (contactentity_contact_id) REFERENCES Contact (contact_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE ContactEntity ADD CONSTRAINT contactentity_entity_id_entity_id_fkey FOREIGN KEY (contactentity_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE ContractEntity ADD CONSTRAINT contractentity_contract_id_contract_id_fkey FOREIGN KEY (contractentity_contract_id) REFERENCES Contract (contract_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE ContractEntity ADD CONSTRAINT contractentity_entity_id_entity_id_fkey FOREIGN KEY (contractentity_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE DealEntity ADD CONSTRAINT dealentity_deal_id_deal_id_fkey FOREIGN KEY (dealentity_deal_id) REFERENCES Deal (deal_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE DealEntity ADD CONSTRAINT dealentity_entity_id_entity_id_fkey FOREIGN KEY (dealentity_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE DocumentEntity ADD CONSTRAINT documententity_document_id_document_id_fkey FOREIGN KEY (documententity_document_id) REFERENCES Document (document_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE DocumentEntity ADD CONSTRAINT documententity_entity_id_entity_id_fkey FOREIGN KEY (documententity_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE DomainEntity ADD CONSTRAINT domainentity_domain_id_domain_id_fkey FOREIGN KEY (domainentity_domain_id) REFERENCES Domain (domain_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE DomainEntity ADD CONSTRAINT domainentity_entity_id_entity_id_fkey FOREIGN KEY (domainentity_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE EventEntity ADD CONSTRAINT evententity_event_id_event_id_fkey FOREIGN KEY (evententity_event_id) REFERENCES Event (event_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE EventEntity ADD CONSTRAINT evententity_entity_id_entity_id_fkey FOREIGN KEY (evententity_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE HostEntity ADD CONSTRAINT hostentity_host_id_host_id_fkey FOREIGN KEY (hostentity_host_id) REFERENCES Host (host_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE HostEntity ADD CONSTRAINT hostentity_entity_id_entity_id_fkey FOREIGN KEY (hostentity_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE ImportEntity ADD CONSTRAINT importentity_import_id_import_id_fkey FOREIGN KEY (importentity_import_id) REFERENCES Import (import_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE ImportEntity ADD CONSTRAINT importentity_entity_id_entity_id_fkey FOREIGN KEY (importentity_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE IncidentEntity ADD CONSTRAINT incidententity_incident_id_incident_id_fkey FOREIGN KEY (incidententity_incident_id) REFERENCES Incident (incident_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE IncidentEntity ADD CONSTRAINT incidententity_entity_id_entity_id_fkey FOREIGN KEY (incidententity_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE InvoiceEntity ADD CONSTRAINT invoiceentity_invoice_id_invoice_id_fkey FOREIGN KEY (invoiceentity_invoice_id) REFERENCES Invoice (invoice_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE InvoiceEntity ADD CONSTRAINT invoiceentity_entity_id_entity_id_fkey FOREIGN KEY (invoiceentity_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE LeadEntity ADD CONSTRAINT leadentity_lead_id_lead_id_fkey FOREIGN KEY (leadentity_lead_id) REFERENCES Lead (lead_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE LeadEntity ADD CONSTRAINT leadentity_entity_id_entity_id_fkey FOREIGN KEY (leadentity_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE ListEntity ADD CONSTRAINT listentity_list_id_list_id_fkey FOREIGN KEY (listentity_list_id) REFERENCES List (list_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE ListEntity ADD CONSTRAINT listentity_entity_id_entity_id_fkey FOREIGN KEY (listentity_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE MailshareEntity ADD CONSTRAINT mailshareentity_mailshare_id_mailshare_id_fkey FOREIGN KEY (mailshareentity_mailshare_id) REFERENCES MailShare (mailshare_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE MailshareEntity ADD CONSTRAINT mailshareentity_entity_id_entity_id_fkey FOREIGN KEY (mailshareentity_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE MailboxEntity ADD CONSTRAINT mailboxentity_mailbox_id_mailbox_id_fkey FOREIGN KEY (mailboxentity_mailbox_id) REFERENCES UserObm (userobm_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE MailboxEntity ADD CONSTRAINT mailboxentity_entity_id_entity_id_fkey FOREIGN KEY (mailboxentity_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE OgroupEntity ADD CONSTRAINT ogroupentity_ogroup_id_ogroup_id_fkey FOREIGN KEY (ogroupentity_ogroup_id) REFERENCES OGroup (ogroup_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE OgroupEntity ADD CONSTRAINT ogroupentity_entity_id_entity_id_fkey FOREIGN KEY (ogroupentity_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE ObmbookmarkEntity ADD CONSTRAINT obmbookmarkentity_obmbookmark_id_obmbookmark_id_fkey FOREIGN KEY (obmbookmarkentity_obmbookmark_id) REFERENCES ObmBookmark (obmbookmark_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE ObmbookmarkEntity ADD CONSTRAINT obmbookmarkentity_entity_id_entity_id_fkey FOREIGN KEY (obmbookmarkentity_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE OrganizationalchartEntity ADD CONSTRAINT organizationalchart_id_organizationalchart_id_fkey FOREIGN KEY (organizationalchartentity_organizationalchart_id) REFERENCES OrganizationalChart (organizationalchart_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE OrganizationalchartEntity ADD CONSTRAINT organizationalchartentity_entity_id_entity_id_fkey FOREIGN KEY (organizationalchartentity_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE ParentdealEntity ADD CONSTRAINT parentdealentity_parentdeal_id_parentdeal_id_fkey FOREIGN KEY (parentdealentity_parentdeal_id) REFERENCES ParentDeal (parentdeal_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE ParentdealEntity ADD CONSTRAINT parentdealentity_entity_id_entity_id_fkey FOREIGN KEY (parentdealentity_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE PaymentEntity ADD CONSTRAINT paymententity_payment_id_payment_id_fkey FOREIGN KEY (paymententity_payment_id) REFERENCES Payment (payment_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE PaymentEntity ADD CONSTRAINT paymententity_entity_id_entity_id_fkey FOREIGN KEY (paymententity_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE ProfileEntity ADD CONSTRAINT profileentity_profile_id_profile_id_fkey FOREIGN KEY (profileentity_profile_id) REFERENCES Profile (profile_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE ProfileEntity ADD CONSTRAINT profileentity_entity_id_entity_id_fkey FOREIGN KEY (profileentity_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE ProjectEntity ADD CONSTRAINT projectentity_project_id_project_id_fkey FOREIGN KEY (projectentity_project_id) REFERENCES Project (project_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE ProjectEntity ADD CONSTRAINT projectentity_entity_id_entity_id_fkey FOREIGN KEY (projectentity_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE PublicationEntity ADD CONSTRAINT publicationentity_publication_id_publication_id_fkey FOREIGN KEY (publicationentity_publication_id) REFERENCES Publication (publication_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE PublicationEntity ADD CONSTRAINT publicationentity_entity_id_entity_id_fkey FOREIGN KEY (publicationentity_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE ResourcegroupEntity ADD CONSTRAINT resourcegroupentity_resourcegroup_id_resourcegroup_id_fkey FOREIGN KEY (resourcegroupentity_resourcegroup_id) REFERENCES RGroup (rgroup_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE ResourcegroupEntity ADD CONSTRAINT resourcegroupentity_entity_id_entity_id_fkey FOREIGN KEY (resourcegroupentity_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE ResourceEntity ADD CONSTRAINT resourceentity_resource_id_resource_id_fkey FOREIGN KEY (resourceentity_resource_id) REFERENCES Resource (resource_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE ResourceEntity ADD CONSTRAINT resourceentity_entity_id_entity_id_fkey FOREIGN KEY (resourceentity_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE SubscriptionEntity ADD CONSTRAINT subscriptionentity_subscription_id_subscription_id_fkey FOREIGN KEY (subscriptionentity_subscription_id) REFERENCES Subscription (subscription_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE SubscriptionEntity ADD CONSTRAINT subscriptionentity_entity_id_entity_id_fkey FOREIGN KEY (subscriptionentity_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE GroupEntity ADD CONSTRAINT groupentity_group_id_group_id_fkey FOREIGN KEY (groupentity_group_id) REFERENCES UGroup (group_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE GroupEntity ADD CONSTRAINT groupentity_entity_id_entity_id_fkey FOREIGN KEY (groupentity_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE UserEntity ADD CONSTRAINT userentity_user_id_user_id_fkey FOREIGN KEY (userentity_user_id) REFERENCES UserObm (userobm_id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE UserEntity ADD CONSTRAINT userentity_entity_id_entity_id_fkey FOREIGN KEY (userentity_entity_id) REFERENCES Entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE;

-- Char to smallint
ALTER TABLE userobm ALTER COLUMN userobm_archive DROP DEFAULT;
ALTER TABLE userobm ALTER COLUMN userobm_archive TYPE SMALLINT USING CASE userobm_archive WHEN '1' THEN 1 ELSE 0 END;
ALTER TABLE userobm ALTER COLUMN userobm_archive SET DEFAULT 0;

ALTER TABLE company ALTER COLUMN company_archive DROP DEFAULT;
ALTER TABLE company ALTER COLUMN company_archive TYPE SMALLINT USING CASE company_archive WHEN '1' THEN 1 ELSE 0 END;
ALTER TABLE company ALTER COLUMN company_archive SET DEFAULT 0;

ALTER TABLE contact ALTER COLUMN contact_mailing_ok DROP DEFAULT;
ALTER TABLE contact ALTER COLUMN contact_mailing_ok TYPE SMALLINT USING CASE contact_mailing_ok WHEN '1' THEN 1 ELSE 0 END;
ALTER TABLE contact ALTER COLUMN contact_mailing_ok SET DEFAULT 0;

ALTER TABLE contact ALTER COLUMN contact_newsletter DROP DEFAULT;
ALTER TABLE contact ALTER COLUMN contact_newsletter TYPE SMALLINT USING CASE contact_newsletter WHEN '1' THEN 1 ELSE 0 END;
ALTER TABLE contact ALTER COLUMN contact_newsletter SET DEFAULT 0;

ALTER TABLE contact ALTER COLUMN contact_archive DROP DEFAULT;
ALTER TABLE contact ALTER COLUMN contact_archive TYPE SMALLINT USING CASE contact_archive WHEN '1' THEN 1 ELSE 0 END;
ALTER TABLE contact ALTER COLUMN contact_archive SET DEFAULT 0;

ALTER TABLE lead ALTER COLUMN lead_archive DROP DEFAULT;
ALTER TABLE lead ALTER COLUMN lead_archive TYPE SMALLINT USING CASE lead_archive WHEN '1' THEN 1 ELSE 0 END;
ALTER TABLE lead ALTER COLUMN lead_archive SET DEFAULT 0;

ALTER TABLE parentdeal ALTER COLUMN parentdeal_archive DROP DEFAULT;
ALTER TABLE parentdeal ALTER COLUMN parentdeal_archive TYPE SMALLINT USING CASE parentdeal_archive WHEN '1' THEN 1 ELSE 0 END;
ALTER TABLE parentdeal ALTER COLUMN parentdeal_archive SET DEFAULT 0;

ALTER TABLE deal ALTER COLUMN deal_archive DROP DEFAULT;
ALTER TABLE deal ALTER COLUMN deal_archive TYPE SMALLINT USING CASE deal_archive WHEN '1' THEN 1 ELSE 0 END;
ALTER TABLE deal ALTER COLUMN deal_archive SET DEFAULT 0;

ALTER TABLE list ALTER COLUMN list_contact_archive DROP DEFAULT;
ALTER TABLE list ALTER COLUMN list_contact_archive TYPE SMALLINT USING CASE list_contact_archive WHEN '1' THEN 1 ELSE 0 END;
ALTER TABLE list ALTER COLUMN list_contact_archive SET DEFAULT 0;

ALTER TABLE project ALTER COLUMN project_archive DROP DEFAULT;
ALTER TABLE project ALTER COLUMN project_archive TYPE SMALLINT USING CASE project_archive WHEN '1' THEN 1 ELSE 0 END;
ALTER TABLE project ALTER COLUMN project_archive SET DEFAULT 0;

ALTER TABLE contract ALTER COLUMN contract_archive DROP DEFAULT;
ALTER TABLE contract ALTER COLUMN contract_archive TYPE SMALLINT USING CASE contract_archive WHEN '1' THEN 1 ELSE 0 END;
ALTER TABLE contract ALTER COLUMN contract_archive SET DEFAULT 0;

ALTER TABLE incident ALTER COLUMN incident_archive DROP DEFAULT;
ALTER TABLE incident ALTER COLUMN incident_archive TYPE SMALLINT USING CASE incident_archive WHEN '1' THEN 1 ELSE 0 END;
ALTER TABLE incident ALTER COLUMN incident_archive SET DEFAULT 0;

ALTER TABLE invoice ALTER COLUMN invoice_archive DROP DEFAULT;
ALTER TABLE invoice ALTER COLUMN invoice_archive TYPE SMALLINT USING CASE invoice_archive WHEN '1' THEN 1 ELSE 0 END;
ALTER TABLE invoice ALTER COLUMN invoice_archive SET DEFAULT 0;

ALTER TABLE payment ALTER COLUMN payment_checked DROP DEFAULT;
ALTER TABLE payment ALTER COLUMN payment_checked TYPE SMALLINT USING CASE payment_checked WHEN '1' THEN 1 ELSE 0 END;
ALTER TABLE payment ALTER COLUMN payment_checked SET DEFAULT 0;

ALTER TABLE mailshare ALTER COLUMN mailshare_archive DROP DEFAULT;
ALTER TABLE mailshare ALTER COLUMN mailshare_archive TYPE SMALLINT USING CASE mailshare_archive WHEN '1' THEN 1 ELSE 0 END;
ALTER TABLE mailshare ALTER COLUMN mailshare_archive SET DEFAULT 0;

ALTER TABLE organizationalchart ALTER COLUMN organizationalchart_archive DROP DEFAULT;
ALTER TABLE organizationalchart ALTER COLUMN organizationalchart_archive TYPE SMALLINT USING CASE organizationalchart_archive WHEN '1' THEN 1 ELSE 0 END;
ALTER TABLE organizationalchart ALTER COLUMN organizationalchart_archive SET DEFAULT 0;

ALTER TABLE host ADD COLUMN host_archive smallint DEFAULT 0 NOT NULL;

ALTER TABLE ugroup ADD COLUMN group_archive smallint DEFAULT 0 NOT NULL;

-- Module Campaign

--
-- Name: campaign; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE campaign (
  campaign_id serial,
  campaign_name character varying(50) default NULL,
  campaign_timeupdate timestamp without time zone,
  campaign_timecreate timestamp without time zone DEFAULT now(),
  campaign_userupdate integer default NULL,
  campaign_usercreate integer default NULL,
  campaign_manager_id integer default NULL,
  campaign_tracker_key integer default NULL,
  campaign_refer_url character varying(255) default NULL,
  campaign_nb_sent integer default NULL,
  campaign_nb_error integer default NULL,
  campaign_nb_inqueue integer default NULL,
  campaign_progress integer default 0,
  campaign_start_date date default NULL,
  campaign_end_date date default NULL,
  campaign_status integer default NULL,
  campaign_type integer default NULL,
  campaign_objective text default NULL,
  campaign_comment text default NULL,
  campaign_domain_id integer NOT NULL,
  campaign_email integer default NULL,
  campaign_parent integer default NULL,
  campaign_child_order integer default NULL,
  CONSTRAINT campaign_parent_fkey FOREIGN KEY (campaign_parent) REFERENCES campaign (campaign_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT campaign_email_fkey FOREIGN KEY (campaign_email) REFERENCES Document (document_id) ON DELETE CASCADE ON UPDATE CASCADE,
  PRIMARY KEY (campaign_id)
);


--
-- Name: campaignentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE campaignentity (
  campaignentity_entity_id integer NOT NULL,
  campaignentity_campaign_id integer NOT NULL,
  PRIMARY KEY  (campaignentity_entity_id,campaignentity_campaign_id),
  CONSTRAINT campaignentity_campaign_id_campaign_id_fkey FOREIGN KEY (campaignentity_campaign_id) REFERENCES campaign (campaign_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT campaignentity_entity_id_entity_id_fkey FOREIGN KEY (campaignentity_entity_id) REFERENCES entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE
);


--
-- Name: campaigndisabledentity; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE campaigndisabledentity (
  campaigndisabledentity_entity_id integer NOT NULL,
  campaigndisabledentity_campaign_id integer NOT NULL,
  PRIMARY KEY  (campaigndisabledentity_entity_id,campaigndisabledentity_campaign_id),
  CONSTRAINT campaigndisabledentity_campaign_id_campaign_id_fkey FOREIGN KEY (campaigndisabledentity_campaign_id) REFERENCES campaign (campaign_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT campaigndisabledentity_entity_id_entity_id_fkey FOREIGN KEY (campaigndisabledentity_entity_id) REFERENCES entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE
);


--
-- Name: campaigntarget; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE campaigntarget (
  campaigntarget_id serial,
  campaigntarget_campaign_id integer NOT NULL,
  campaigntarget_entity_id integer,
  campaigntarget_status integer NULL,
  PRIMARY KEY (campaigntarget_id),
  CONSTRAINT campaigntarget_campaign_id_campaign_id_fkey FOREIGN KEY (campaigntarget_campaign_id) REFERENCES campaign (campaign_id) ON DELETE CASCADE ON UPDATE CASCADE,  
  CONSTRAINT campaigntarget_entity_id_entity_id_fkey FOREIGN KEY (campaigntarget_entity_id) REFERENCES entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE
);


--
-- Name: campaignmailtarget; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE campaignmailtarget (
  campaignmailtarget_id serial,
  campaignmailtarget_campaign_id integer NOT NULL,
  campaignmailtarget_entity_id integer,
  campaignmailtarget_status integer NULL,
  PRIMARY KEY (campaignmailtarget_id),
  CONSTRAINT campaignmailtarget_campaign_id_campaign_id_fkey FOREIGN KEY (campaignmailtarget_campaign_id) REFERENCES campaign (campaign_id) ON DELETE CASCADE ON UPDATE CASCADE,  
  CONSTRAINT campaignmailtarget_entity_id_entity_id_fkey FOREIGN KEY (campaignmailtarget_entity_id) REFERENCES entity (entity_id) ON DELETE CASCADE ON UPDATE CASCADE
);


--
-- Name: campaignmailcontent; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE campaignmailcontent (
  campaignmailcontent_id         serial,
  campaignmailcontent_refext_id  character varying(8),
  campaignmailcontent_content    character varying,
  PRIMARY KEY (campaignmailcontent_id)
);

--
-- Name: campaignpushtarget; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--

CREATE TABLE campaignpushtarget (
  campaignpushtarget_id             serial,
  campaignpushtarget_mailcontent_id integer NOT NULL,
  campaignpushtarget_refext_id      character varying(8),

  campaignpushtarget_status         smallint DEFAULT 1 NOT NULL,
  -- 1 : not sent
  -- 2 : sent
  -- 3 : error occurred
  
  campaignpushtarget_email_address  character varying(512) NOT NULL,
  campaignpushtarget_properties     text,
  campaignpushtarget_start_time     timestamp,
  campaignpushtarget_sent_time      timestamp,
  campaignpushtarget_retries        smallint,
  PRIMARY KEY (campaignpushtarget_id)
);


--
-- Name: synchedcontact; Type: TABLE; Schema: public; Owner: -; Tablespace: 
--
CREATE TABLE synchedcontact (
  synchedcontact_user_id integer NOT NULL,
  synchedcontact_contact_id integer NOT NULL,
  synchedcontact_timestamp timestamp without time zone NOT NULL DEFAULT now(),
  PRIMARY KEY (synchedcontact_user_id, synchedcontact_contact_id),
  CONSTRAINT synchedcontact_user_id_userobm_id_fkey FOREIGN KEY (synchedcontact_user_id) REFERENCES UserObm (userobm_id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT synchedcontact_contact_id_contact_id_fkey FOREIGN KEY (synchedcontact_contact_id) REFERENCES Contact (contact_id) ON DELETE CASCADE ON UPDATE CASCADE
);

INSERT INTO SynchedContact (synchedcontact_user_id, synchedcontact_contact_id, synchedcontact_timestamp) 
SELECT contact_usercreate, contact_id, contact_timecreate FROM Contact WHERE contact_privacy = 1 AND contact_usercreate IS NOT NULL;

--
--
--
DROP TABLE P_Samba;
DROP TABLE P_MailServer;
DROP TABLE P_MailServerNetwork;

--
-- Table structure for table P_Domain
--

DROP TABLE P_Domain;
CREATE TABLE P_Domain (LIKE Domain);
INSERT INTO P_Domain SELECT * FROM Domain;


--
-- Table structure for table P_DomainEntity
--

CREATE TABLE P_DomainEntity (LIKE DomainEntity);
INSERT INTO P_DomainEntity SELECT * FROM DomainEntity;


--
-- Table structure for table P_EntityRight
--

DROP TABLE P_EntityRight;
CREATE TABLE P_EntityRight (LIKE EntityRight);
INSERT INTO P_EntityRight SELECT * FROM EntityRight;
 

--
-- Table structure for table P_GroupEntity
--

CREATE TABLE P_GroupEntity (LIKE GroupEntity);
INSERT INTO P_GroupEntity SELECT * FROM GroupEntity;


--
-- Table structure for table P_GroupGroup
--

DROP TABLE P_GroupGroup;


--
-- Table structure for table P_Host
--

DROP TABLE P_Host;
CREATE TABLE P_Host (LIKE Host);
INSERT INTO P_Host SELECT * FROM Host;

--
-- Table structure for table P_HostEntity
--

CREATE TABLE P_HostEntity (LIKE HostEntity);
INSERT INTO P_HostEntity SELECT * FROM HostEntity;


--
-- Table structure for table P_MailShare
--

DROP TABLE P_MailShare;
CREATE TABLE P_MailShare (LIKE MailShare);
INSERT INTO P_MailShare SELECT * FROM MailShare;


--
-- Table structure for table P_MailshareEntity
--

CREATE TABLE P_MailshareEntity (LIKE MailshareEntity);
INSERT INTO P_MailshareEntity SELECT * FROM MailshareEntity;


--
-- Table structure for table P_MailboxEntity
--

CREATE TABLE P_MailboxEntity (LIKE MailboxEntity);
INSERT INTO P_MailboxEntity SELECT * FROM MailboxEntity;


--
-- Table structure for table P_Service
--

CREATE TABLE P_Service (LIKE Service);
INSERT INTO P_Service SELECT * FROM Service;


--
-- Table structure for table P_ServiceProperty
--

CREATE TABLE P_ServiceProperty (LIKE ServiceProperty);
INSERT INTO P_ServiceProperty SELECT * FROM ServiceProperty;


--
-- Table structure for table P_UGroup
--

DROP TABLE P_UGroup;
CREATE TABLE P_UGroup (LIKE UGroup);
INSERT INTO P_UGroup SELECT * FROM UGroup WHERE group_privacy=0;


--
-- Table structure for table P_UserObm
--

CREATE TABLE P_UserEntity (LIKE UserEntity);
INSERT INTO P_UserEntity SELECT * FROM UserEntity;


--
-- Table structure for table P_UserObm
--

DROP TABLE P_UserObm;
CREATE TABLE P_UserObm (LIKE UserObm);
INSERT INTO P_UserObm SELECT * FROM UserObm;


--
-- Table structure for table P_UserObmGroup
--

DROP TABLE P_UserObmGroup;


--
-- Table structure for table P_of_usergroup
--

DROP TABLE P_of_usergroup;
CREATE TABLE P_of_usergroup (LIKE of_usergroup);
INSERT INTO P_of_usergroup SELECT * FROM of_usergroup;

--  _________________
-- | Drop old tables |
--  ¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯¯
DROP TABLE DeletedCalendarEvent;
DROP TABLE CalendarAlert;
DROP TABLE CalendarException;
DROP TABLE CalendarEvent;
DROP TABLE CalendarCategory1;
DROP TABLE Todo;
DROP TABLE DeletedTodo;
DROP TABLE TmpEntity;

-- Write that the 2.1->2.2 is completed
UPDATE ObmInfo SET obminfo_value='2.2' WHERE obminfo_name='db_version';
