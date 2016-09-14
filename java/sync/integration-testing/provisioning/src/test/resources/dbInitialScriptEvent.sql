CREATE DOMAIN batch_status AS VARCHAR CHECK VALUE IN ('IDLE', 'RUNNING', 'ERROR', 'SUCCESS');
CREATE DOMAIN batch_entity_type AS VARCHAR CHECK VALUE IN ('GROUP', 'USER', 'GROUP_MEMBERSHIP', 'USER_MEMBERSHIP', 'EVENT', 'CONTACT');
CREATE DOMAIN http_verb AS VARCHAR CHECK VALUE IN ('PUT', 'PATCH', 'GET', 'POST', 'DELETE');
CREATE DOMAIN vkind AS VARCHAR (16) CHECK VALUE IN ('VEVENT', 'VCONTACT');


CREATE DOMAIN vcomponent AS VARCHAR (16) CHECK VALUE IN (
    'VEVENT',
    'VTODO',
    'VJOURNAL',
    'VFREEBUSY'
);

CREATE DOMAIN vopacity AS VARCHAR (16) CHECK VALUE IN (
    'OPAQUE',
    'TRANSPARENT'
);

CREATE DOMAIN vpartstat AS VARCHAR (16) CHECK VALUE IN (
    'NEEDS-ACTION',
    'ACCEPTED',
    'DECLINED',
    'TENTATIVE',
    'DELEGATED',
    'COMPLETED',
    'IN-PROGRESS'
);

CREATE DOMAIN vrole AS VARCHAR (16) CHECK VALUE IN (
    'CHAIR',
    'REQ',
    'OPT',
    'NON'
);

CREATE TABLE serviceproperty (
    serviceproperty_id integer PRIMARY KEY AUTO_INCREMENT,
    serviceproperty_service character varying(255) NOT NULL,
    serviceproperty_property character varying(255) NOT NULL,
    serviceproperty_entity_id integer NOT NULL,
    serviceproperty_value text
);

CREATE TABLE host (
    host_id integer PRIMARY KEY AUTO_INCREMENT,
    host_domain_id integer NOT NULL,
    host_timeupdate timestamp,
    host_timecreate timestamp DEFAULT now(),
    host_userupdate integer,
    host_usercreate integer,
    host_uid integer,
    host_gid integer,
    host_archive smallint DEFAULT 0 NOT NULL,
    host_name character varying(32) NOT NULL,
    host_fqdn character varying(255),
    host_ip character varying(16),
    host_delegation character varying(256) DEFAULT '',
    host_description character varying(128)
);

CREATE TABLE domain (
    domain_id integer PRIMARY KEY AUTO_INCREMENT,
    domain_timeupdate timestamp,
    domain_timecreate timestamp,
    domain_usercreate integer,
    domain_userupdate integer,
    domain_label character varying(32) NOT NULL,
    domain_description character varying(255),
    domain_name character varying(128),
    domain_alias text,
    domain_global boolean,
    domain_uuid character(36) NOT NULL
);

CREATE TABLE domainentity (
    domainentity_entity_id integer NOT NULL,
    domainentity_domain_id integer NOT NULL
);

CREATE TABLE batch
(
  id integer PRIMARY KEY AUTO_INCREMENT,
  status batch_status NOT NULL,
  timecreate timestamp NOT NULL DEFAULT NOW(),
  timecommit timestamp,
  domain integer NOT NULL,
  CONSTRAINT batch_batch_domain_id_fkey FOREIGN KEY (domain)
      REFERENCES domain (domain_id)
      ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE batch_operation
(
  id integer PRIMARY KEY AUTO_INCREMENT,
  status batch_status NOT NULL,
  timecreate timestamp NOT NULL DEFAULT NOW(),
  timecommit timestamp,
  error text,
  resource_path text NOT NULL,
  body text,
  verb http_verb NOT NULL,
  entity_type batch_entity_type NOT NULL,
  batch integer NOT NULL,
  CONSTRAINT batch_operation_batch_fkey FOREIGN KEY (batch)
      REFERENCES batch (id)
      ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE batch_operation_param
(
  id integer PRIMARY KEY AUTO_INCREMENT,
  param_key text NOT NULL,
  value text NOT NULL,
  operation integer NOT NULL,
  CONSTRAINT batch_operation_param_operation_fkey FOREIGN KEY (operation)
      REFERENCES batch_operation (id)
      ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE ugroup (
    group_id integer PRIMARY KEY AUTO_INCREMENT,
    group_domain_id integer NOT NULL,
    group_timeupdate timestamp,
    group_timecreate timestamp DEFAULT now(),
    group_userupdate integer,
    group_usercreate integer,
    group_system integer DEFAULT 0,
    group_archive smallint DEFAULT 0 NOT NULL,
    group_privacy integer DEFAULT 0,
    group_local integer DEFAULT 1,
    group_ext_id character varying(255),
    group_samba integer DEFAULT 0,
    group_gid integer,
    group_mailing integer DEFAULT 0,
    group_delegation character varying(256) DEFAULT '',
    group_manager_id integer,
    group_name character varying(255) NOT NULL,
    group_desc character varying(128),
    group_email character varying(128)
);

CREATE TABLE userobm (
    userobm_id integer PRIMARY KEY AUTO_INCREMENT,
    userobm_domain_id integer NOT NULL,
    userobm_timeupdate timestamp,
    userobm_timecreate timestamp DEFAULT NULL,
    userobm_userupdate integer,
    userobm_usercreate integer,
    userobm_local integer DEFAULT 1,
    userobm_ext_id character varying(16),
    userobm_system integer DEFAULT 0,
    userobm_archive smallint DEFAULT 0 NOT NULL,
    userobm_status character varying(64) DEFAULT 'VALID',
    userobm_timelastaccess timestamp,
    userobm_login character varying(64) DEFAULT ''::character varying NOT NULL,
    userobm_nb_login_failed integer DEFAULT 0,
    userobm_password_type character varying(6) DEFAULT 'PLAIN'::character varying NOT NULL,
    userobm_password character varying(64) DEFAULT ''::character varying NOT NULL,
    userobm_password_dateexp date,
    userobm_account_dateexp date,
    userobm_perms character varying(254),
    userobm_delegation_target character varying(256) DEFAULT '',
    userobm_delegation character varying(256) DEFAULT '',
    userobm_calendar_version timestamp,
    userobm_uid integer,
    userobm_gid integer,
    userobm_datebegin date,
    userobm_hidden integer DEFAULT 0,
    userobm_kind character varying(12),
    userobm_lastname character varying(64) DEFAULT '',
    userobm_firstname character varying(64) DEFAULT '',
    userobm_title character varying(64) DEFAULT '',
    userobm_sound character varying(64),
    userobm_company character varying(64),
    userobm_direction character varying(64),
    userobm_service character varying(64),
    userobm_address1 character varying(64),
    userobm_address2 character varying(64),
    userobm_address3 character varying(64),
    userobm_zipcode character varying(14),
    userobm_town character varying(64),
    userobm_expresspostal character varying(16),
    userobm_country_iso3166 character(2) DEFAULT '0',
    userobm_phone character varying(32) DEFAULT '',
    userobm_phone2 character varying(32) DEFAULT '',
    userobm_mobile character varying(32) DEFAULT '',
    userobm_fax character varying(32) DEFAULT '',
    userobm_fax2 character varying(32) DEFAULT '',
    userobm_web_perms integer DEFAULT 0,
    userobm_web_list text,
    userobm_web_all integer DEFAULT 0,
    userobm_mail_perms integer DEFAULT 0,
    userobm_mail_ext_perms integer DEFAULT 0,
    userobm_email text DEFAULT '',
    userobm_mail_server_id integer,
    userobm_mail_quota integer DEFAULT 0,
    userobm_mail_quota_use integer DEFAULT 0,
    userobm_mail_login_date timestamp,
    userobm_nomade_perms integer DEFAULT 0,
    userobm_nomade_enable integer DEFAULT 0,
    userobm_nomade_local_copy integer DEFAULT 0,
    userobm_email_nomade text DEFAULT '',
    userobm_vacation_enable integer DEFAULT 0,
    userobm_vacation_datebegin timestamp,
    userobm_vacation_dateend timestamp,
    userobm_vacation_message text DEFAULT '',
    userobm_samba_perms integer DEFAULT 0,
    userobm_samba_home character varying(255) DEFAULT '',
    userobm_samba_home_drive character(2) DEFAULT '',
    userobm_samba_logon_script character varying(128) DEFAULT '',
    userobm_host_id integer,
    userobm_description character varying(255),
    userobm_location character varying(255),
    userobm_education character varying(255),
    userobm_photo_id integer,
    userobm_commonname character varying(256) DEFAULT ''
);

 CREATE TABLE entity (
    entity_id integer ,
    entity_mailing boolean
);

CREATE TABLE userentity (
    userentity_entity_id integer PRIMARY KEY AUTO_INCREMENT,
    userentity_user_id integer NOT NULL
);

CREATE TABLE userobmgroup (
    userobmgroup_group_id integer PRIMARY KEY AUTO_INCREMENT,
    userobmgroup_userobm_id integer NOT NULL
);

CREATE TABLE userobmpref (
    userobmpref_id integer PRIMARY KEY AUTO_INCREMENT,
    userobmpref_user_id integer,
    userobmpref_option character varying(50) NOT NULL,
    userobmpref_value character varying(50) NOT NULL
);

CREATE TABLE groupgroup (
    groupgroup_parent_id integer NOT NULL,
    groupgroup_child_id integer NOT NULL
);

CREATE TABLE usersystem (
    usersystem_id integer primary key auto_increment,
    usersystem_login character varying(32) DEFAULT ''::character varying NOT NULL,
    usersystem_password character varying(32) DEFAULT ''::character varying NOT NULL,
    usersystem_uid character varying(6) DEFAULT NULL,
    usersystem_gid character varying(6) DEFAULT NULL,
    usersystem_homedir character varying(32) DEFAULT '/tmp'::character varying NOT NULL,
    usersystem_lastname character varying(32) DEFAULT NULL,
    usersystem_firstname character varying(32) DEFAULT NULL,
    usersystem_shell character varying(32) DEFAULT NULL
);

CREATE TABLE of_usergroup (
    of_usergroup_group_id integer NOT NULL,
    of_usergroup_user_id integer NOT NULL
);

CREATE TABLE profile (
    profile_id integer PRIMARY KEY AUTO_INCREMENT,
    profile_domain_id integer NOT NULL,
    profile_timeupdate timestamp,
    profile_timecreate timestamp,
    profile_userupdate integer,
    profile_usercreate integer,
    profile_name character varying(64)
);

CREATE TABLE profilemodule (
    profilemodule_id integer NOT NULL,
    profilemodule_domain_id integer NOT NULL,
    profilemodule_profile_id integer,
    profilemodule_module_name character varying(64) DEFAULT ''::character varying NOT NULL,
    profilemodule_right integer
);
ALTER TABLE profilemodule ADD CONSTRAINT profilemodule_pkey PRIMARY KEY (profilemodule_id);
CREATE SEQUENCE profilemodule_profilemodule_id_seq INCREMENT BY 1 CACHE 1;
ALTER TABLE profilemodule ALTER COLUMN profilemodule_id SET DEFAULT nextval('profilemodule_profilemodule_id_seq');



CREATE TABLE event (
    event_id integer PRIMARY KEY AUTO_INCREMENT,
    event_domain_id integer NOT NULL,
    event_timeupdate timestamp,
    event_timecreate timestamp DEFAULT now(),
    event_userupdate integer,
    event_usercreate integer,
    event_ext_id character varying(300) DEFAULT ''::character varying NOT NULL,
    event_type vcomponent DEFAULT 'VEVENT',
    event_origin character varying(255) DEFAULT ''::character varying NOT NULL,
    event_owner integer,
    event_timezone character varying(255) DEFAULT 'GMT',
    event_opacity vopacity DEFAULT 'OPAQUE',
    event_title character varying(255) DEFAULT NULL,
    event_location character varying(255) DEFAULT NULL,
    event_category1_id integer,
    event_priority integer,
    event_privacy integer DEFAULT 0 NOT NULL,
    event_date timestamp,
    event_duration integer DEFAULT 0 NOT NULL,
    event_allday boolean DEFAULT false,
    event_repeatkind character varying(20) DEFAULT 'none'::character varying NOT NULL,
    event_repeatfrequence integer,
    event_repeatdays character varying(7) DEFAULT NULL,
    event_endrepeat timestamp,
    event_color character varying(7),
    event_completed timestamp,
    event_url text,
    event_allow_documents boolean DEFAULT false,
    event_description text,
    event_properties text,
    event_tag_id integer,
    event_sequence integer DEFAULT 0
);

CREATE SEQUENCE event_event_id_seq INCREMENT BY 1 CACHE 1;

CREATE TABLE eventalert (
    eventalert_timeupdate timestamp,
    eventalert_timecreate timestamp DEFAULT now(),
    eventalert_userupdate integer,
    eventalert_usercreate integer,
    eventalert_event_id integer,
    eventalert_user_id integer,
    eventalert_duration integer DEFAULT 0 NOT NULL
);

CREATE TABLE eventcategory1 (
    eventcategory1_id integer PRIMARY KEY AUTO_INCREMENT,
    eventcategory1_domain_id integer NOT NULL,
    eventcategory1_timeupdate timestamp,
    eventcategory1_timecreate timestamp DEFAULT now(),
    eventcategory1_userupdate integer,
    eventcategory1_usercreate integer,
    eventcategory1_code character varying(10) DEFAULT '',
    eventcategory1_label character varying(128) DEFAULT NULL,
    eventcategory1_color character(6)
);

CREATE SEQUENCE eventcategory1_eventcategory1_id_seq
    INCREMENT BY 1
    CACHE 1;

CREATE TABLE evententity (
    evententity_entity_id integer NOT NULL,
    evententity_event_id integer NOT NULL
);

CREATE TABLE eventexception (
    eventexception_timeupdate timestamp,
    eventexception_timecreate timestamp DEFAULT now(),
    eventexception_userupdate integer,
    eventexception_usercreate integer,
    eventexception_parent_id integer NOT NULL,
    eventexception_child_id integer,
    eventexception_date timestamp NOT NULL
);

CREATE TABLE eventlink (
    eventlink_timeupdate timestamp,
    eventlink_timecreate timestamp DEFAULT now(),
    eventlink_userupdate integer,
    eventlink_usercreate integer,
    eventlink_event_id integer NOT NULL,
    eventlink_entity_id integer NOT NULL,
    eventlink_state vpartstat DEFAULT 'NEEDS-ACTION',
    eventlink_required vrole DEFAULT 'REQ',
    eventlink_percent double precision DEFAULT 0,
    eventlink_is_organizer boolean DEFAULT false,
    eventlink_comment character varying(255)
);

CREATE SEQUENCE deletedeventlink_deletedeventlink_id_seq
    START WITH 1
    INCREMENT BY 1
    CACHE 1;

CREATE TABLE deletedeventlink (
    deletedeventlink_id integer DEFAULT nextval('deletedeventlink_deletedeventlink_id_seq') NOT NULL,
    deletedeventlink_userobm_id integer NOT NULL,
    deletedeventlink_event_id integer NOT NULL,
    deletedeventlink_event_ext_id character varying(300) NOT NULL,
    deletedeventlink_time_removed timestamp DEFAULT now() NOT NULL
);

CREATE TABLE eventtag (
    eventtag_id integer PRIMARY KEY AUTO_INCREMENT,
    eventtag_user_id integer NOT NULL,
    eventtag_label character varying(128) DEFAULT '',
    eventtag_color character(7) DEFAULT NULL
);


CREATE SEQUENCE eventtag_eventtag_id_seq
    START WITH 1
    INCREMENT BY 1
    CACHE 1;


CREATE TABLE eventtemplate (
    eventtemplate_id integer PRIMARY KEY AUTO_INCREMENT,
    eventtemplate_domain_id integer NOT NULL,
    eventtemplate_timeupdate timestamp,
    eventtemplate_timecreate timestamp DEFAULT now(),
    eventtemplate_userupdate integer,
    eventtemplate_usercreate integer,
    eventtemplate_owner integer,
    eventtemplate_name character varying(255) DEFAULT NULL,
    eventtemplate_title character varying(255) DEFAULT NULL,
    eventtemplate_location character varying(100) DEFAULT NULL,
    eventtemplate_category1_id integer,
    eventtemplate_priority integer,
    eventtemplate_privacy integer,
    eventtemplate_date timestamp,
    eventtemplate_duration integer DEFAULT 0 NOT NULL,
    eventtemplate_allday boolean DEFAULT false,
    eventtemplate_repeatkind character varying(20) DEFAULT 'none'::character varying NOT NULL,
    eventtemplate_repeatfrequence integer,
    eventtemplate_repeatdays character varying(7) DEFAULT NULL,
    eventtemplate_endrepeat timestamp,
    eventtemplate_allow_documents boolean DEFAULT false,
    eventtemplate_alert integer DEFAULT 0 NOT NULL,
    eventtemplate_description text,
    eventtemplate_properties text,
    eventtemplate_tag_id integer,
    eventtemplate_user_ids text,
    eventtemplate_contact_ids text,
    eventtemplate_resource_ids text,
    eventtemplate_document_ids text,
    eventtemplate_group_ids text,
    eventtemplate_organizer integer DEFAULT 0,
    eventtemplate_force_insertion boolean DEFAULT false,
    eventtemplate_opacity vopacity DEFAULT 'OPAQUE',
    eventtemplate_show_user_calendar boolean DEFAULT false,
    eventtemplate_show_resource_calendar boolean DEFAULT false
);

CREATE SEQUENCE eventtemplate_eventtemplate_id_seq
    START WITH 1
    INCREMENT BY 1
    CACHE 1;

CREATE TABLE contact (
    contact_id integer DEFAULT nextval('contact_contact_id_seq') NOT NULL,
    contact_domain_id integer NOT NULL,
    contact_timeupdate timestamp,
    contact_timecreate timestamp DEFAULT now(),
    contact_userupdate integer,
    contact_usercreate integer,
    contact_datasource_id integer,
    contact_addressbook_id integer,
    contact_company_id integer,
    contact_company character varying(64),
    contact_kind_id integer,
    contact_marketingmanager_id integer,
    contact_lastname character varying(64) DEFAULT ''::character varying NOT NULL,
    contact_firstname character varying(64),
    contact_middlename character varying(32) DEFAULT NULL,
    contact_suffix character varying(16) DEFAULT NULL,
    contact_aka character varying(255),
    contact_sound character varying(48),
    contact_manager character varying(64),
    contact_assistant character varying(64),
    contact_spouse character varying(64),
    contact_category character varying(255),
    contact_service character varying(64),
    contact_function_id integer,
    contact_title character varying(64),
    contact_mailing_ok smallint DEFAULT 0,
    contact_newsletter smallint DEFAULT 0,
    contact_archive smallint DEFAULT 0 NOT NULL,
    contact_date timestamp,
    contact_birthday_id integer,
    contact_anniversary_id integer,
    contact_photo_id integer,
    contact_comment text,
    contact_comment2 text,
    contact_comment3 text,
    contact_collected boolean DEFAULT false,
    contact_origin character varying(255) NOT NULL,
    contact_commonname character varying(256) DEFAULT ''
);

CREATE SEQUENCE contact_contact_id_seq
    INCREMENT BY 1
    CACHE 1;

CREATE TABLE contactentity (
    contactentity_entity_id integer NOT NULL,
    contactentity_contact_id integer NOT NULL
);


CREATE TABLE contactfunction (
    contactfunction_id integer NOT NULL,
    contactfunction_domain_id integer NOT NULL,
    contactfunction_timeupdate timestamp,
    contactfunction_timecreate timestamp DEFAULT now(),
    contactfunction_userupdate integer,
    contactfunction_usercreate integer,
    contactfunction_code character varying(10) DEFAULT '',
    contactfunction_label character varying(64)
);

CREATE SEQUENCE contactfunction_contactfunction_id_seq
    START WITH 1
    INCREMENT BY 1
    CACHE 1;

CREATE TABLE categorylink (
    categorylink_category_id integer NOT NULL,
    categorylink_entity_id integer NOT NULL,
    categorylink_category character varying(24) DEFAULT ''::character varying NOT NULL
);

CREATE TABLE category (
    category_id integer PRIMARY KEY AUTO_INCREMENT,
    category_domain_id integer NOT NULL,
    category_timeupdate timestamp,
    category_timecreate timestamp DEFAULT now(),
    category_userupdate integer,
    category_usercreate integer,
    category_category character varying(24) DEFAULT ''::character varying NOT NULL,
    category_code character varying(100) DEFAULT ''::character varying NOT NULL,
    category_label character varying(100) DEFAULT ''::character varying NOT NULL
);

ALTER TABLE categorylink ADD CONSTRAINT categorylink_pkey PRIMARY KEY (categorylink_category_id, categorylink_entity_id);

CREATE INDEX categorylink_category_id_fkey ON categorylink(categorylink_category_id);

CREATE TABLE contactgroup (
    contact_id integer NOT NULL,
    group_id integer NOT NULL
);

CREATE TABLE contactlist (
    contactlist_list_id integer NOT NULL,
    contactlist_contact_id integer NOT NULL
);

CREATE TABLE email (
    email_id integer DEFAULT nextval('email_email_id_seq') NOT NULL,
    email_entity_id integer NOT NULL,
    email_label character varying(255) NOT NULL,
    email_address character varying(255)
);

CREATE SEQUENCE email_email_id_seq
    INCREMENT BY 1
    CACHE 1;

CREATE TABLE addressbook (
    id integer DEFAULT nextval('deletedeventlink_deletedeventlink_id_seq') NOT NULL,
    domain_id integer NOT NULL,
    timeupdate timestamp,
    timecreate timestamp DEFAULT now(),
    userupdate integer,
    usercreate integer,
    origin character varying(255) NOT NULL,
    owner integer,
    name character varying(64) NOT NULL,
    is_default boolean DEFAULT false,
    syncable boolean DEFAULT true
);

CREATE SEQUENCE addressbook_id_seq
    INCREMENT BY 1
    CACHE 1;


CREATE TABLE addressbookentity (
    addressbookentity_entity_id integer NOT NULL,
    addressbookentity_addressbook_id integer NOT NULL
);

CREATE TABLE syncedaddressbook (
    user_id integer NOT NULL,
    addressbook_id integer NOT NULL,
    "timestamp" timestamp DEFAULT now() NOT NULL
);

CREATE TABLE entityright (
    entityright_id integer NOT NULL,
    entityright_entity_id integer NOT NULL,
    entityright_consumer_id integer,
    entityright_access integer DEFAULT 0 NOT NULL,
    entityright_read integer DEFAULT 0 NOT NULL,
    entityright_write integer DEFAULT 0 NOT NULL,
    entityright_admin integer DEFAULT 0 NOT NULL
);

CREATE SEQUENCE entityright_entityright_id_seq
    INCREMENT BY 1
    CACHE 1;
ALTER TABLE entityright ALTER COLUMN entityright_id SET DEFAULT nextval('entityright_entityright_id_seq');

CREATE TABLE groupentity (
    groupentity_entity_id integer NOT NULL,
    groupentity_group_id integer NOT NULL
);

CREATE TABLE resource (
    resource_id integer PRIMARY KEY AUTO_INCREMENT,
    resource_domain_id integer NOT NULL,
    resource_rtype_id integer,
    resource_timeupdate timestamp,
    resource_timecreate timestamp DEFAULT now(),
    resource_userupdate integer,
    resource_usercreate integer,
    resource_name character varying(32) DEFAULT ''::character varying NOT NULL,
    resource_delegation character varying(256) DEFAULT '',
    resource_description character varying(255),
    resource_qty integer DEFAULT 0,
    resource_email text UNIQUE
);

CREATE SEQUENCE resource_resource_id_seq
    INCREMENT BY 1
    CACHE 1;
    

CREATE TABLE resourceentity (
    resourceentity_entity_id integer NOT NULL,
    resourceentity_resource_id integer NOT NULL
);

CREATE TABLE resourcegroup (
    resourcegroup_rgroup_id integer NOT NULL,
    resourcegroup_resource_id integer NOT NULL
);

CREATE TABLE resourcegroupentity (
    resourcegroupentity_entity_id integer NOT NULL,
    resourcegroupentity_resourcegroup_id integer NOT NULL
);

CREATE TABLE resourceitem (
    resourceitem_id integer NOT NULL,
    resourceitem_domain_id integer NOT NULL,
    resourceitem_label character varying(32) NOT NULL,
    resourceitem_resourcetype_id integer NOT NULL,
    resourceitem_description text
);

CREATE SEQUENCE resourceitem_resourceitem_id_seq
    INCREMENT BY 1
    CACHE 1;

CREATE TABLE resourcetype (
    resourcetype_id integer NOT NULL,
    resourcetype_domain_id integer NOT NULL,
    resourcetype_label character varying(32) NOT NULL,
    resourcetype_property character varying(32),
    resourcetype_pkind integer DEFAULT 0 NOT NULL
);

CREATE SEQUENCE resourcetype_resourcetype_id_seq
    INCREMENT BY 1
    CACHE 1;

CREATE TABLE phone (
    phone_id integer NOT NULL,
    phone_entity_id integer NOT NULL,
    phone_label character varying(255) NOT NULL,
    phone_number character varying(32)
);
ALTER TABLE phone ALTER COLUMN phone_id SET DEFAULT nextval('phone_phone_id_seq');
ALTER TABLE phone ADD CONSTRAINT phone_pkey PRIMARY KEY (phone_id);
CREATE SEQUENCE phone_phone_id_seq INCREMENT BY 1 CACHE 1;

CREATE TABLE address (
    address_id integer NOT NULL,
    address_entity_id integer NOT NULL,
    address_street text,
    address_zipcode character varying(14),
    address_town character varying(128),
    address_expresspostal character varying(16),
    address_state character varying(128),
    address_country character(2),
    address_label character varying(255)
);
ALTER TABLE address ADD CONSTRAINT address_pkey PRIMARY KEY (address_id);
ALTER TABLE address ALTER COLUMN address_id SET DEFAULT nextval('address_address_id_seq');
CREATE SEQUENCE address_address_id_seq INCREMENT BY 1 CACHE 1;

CREATE TABLE country (
    country_domain_id integer NOT NULL,
    country_timeupdate timestamp,
    country_timecreate timestamp DEFAULT now(),
    country_userupdate integer,
    country_usercreate integer,
    country_iso3166 character(2) NOT NULL,
    country_name character varying(64),
    country_lang character(2) NOT NULL,
    country_phone character varying(4)
);

ALTER TABLE country ADD CONSTRAINT country_pkey PRIMARY KEY (country_iso3166, country_lang);

CREATE TABLE website (
    website_id integer NOT NULL,
    website_entity_id integer NOT NULL,
    website_label character varying(255) NOT NULL,
    website_url text
);
ALTER TABLE website ADD CONSTRAINT website_pkey PRIMARY KEY (website_id);
ALTER TABLE website ALTER COLUMN website_id SET DEFAULT nextval('website_website_id_seq');
CREATE SEQUENCE website_website_id_seq START WITH 1 INCREMENT BY 1 CACHE 1;

CREATE TABLE im (
    im_id integer NOT NULL,
    im_entity_id integer NOT NULL,
    im_label character varying(255),
    im_address character varying(255),
    im_protocol character varying(255)
);
ALTER TABLE im ADD CONSTRAINT im_pkey PRIMARY KEY (im_id);
ALTER TABLE im ALTER COLUMN im_id SET DEFAULT nextval('im_im_id_seq');
CREATE SEQUENCE im_im_id_seq START WITH 1 INCREMENT BY 1 CACHE 1;

CREATE TABLE kind (
    kind_id integer DEFAULT nextval('kind_kind_id_seq') NOT NULL,
    kind_domain_id integer NOT NULL,
    kind_timeupdate timestamp,
    kind_timecreate timestamp DEFAULT now(),
    kind_userupdate integer,
    kind_usercreate integer,
    kind_minilabel character varying(64),
    kind_header character varying(64),
    kind_lang character(2),
    kind_default integer DEFAULT 0 NOT NULL
);

CREATE SEQUENCE kind_kind_id_seq INCREMENT BY 1 CACHE 1;
ALTER TABLE kind ADD CONSTRAINT kind_domain_id_domain_id_fkey FOREIGN KEY (kind_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE kind ADD CONSTRAINT kind_usercreate_userobm_id_fkey FOREIGN KEY (kind_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;
ALTER TABLE kind ADD CONSTRAINT kind_userupdate_userobm_id_fkey FOREIGN KEY (kind_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

CREATE TABLE commitedoperation (
    commitedoperation_hash_client_id character varying(44) NOT NULL,
    commitedoperation_entity_id integer NOT NULL,
    commitedoperation_kind vkind NOT NULL,
    commitedoperation_client_date timestamp
);


INSERT INTO domainentity (domainentity_entity_id, domainentity_domain_id) VALUES (1, 1), (2, 2), (3, 3);

INSERT INTO domain (domain_name, domain_uuid, domain_label, domain_global) 
	VALUES 
	('global.virt', 'abf7c2bc-aa84-461c-b057-ee42c5dce40a', 'global.virt', true),
	('test.tlse.lng', 'ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6', 'test.tlse.lng', false),
	('test2.tlse.lng', '3a2ba641-4ae0-4b40-aa5e-c3fd3acb78bf', 'test2.tlse.lng', false);

INSERT INTO UGroup (group_domain_id, group_system, group_privacy, group_local, group_ext_id, group_samba, group_gid, group_name, group_desc, group_email)
 VALUES (2, 0, 0, 1, 'AdminExtId', 0, 1001, 'Admin', 'Admin Group Desc', 'group_admin@obm.org'), 
 (2, 0, 0, 2, 'GroupWithUsers', 0, 1002, 'GroupWithUsers', 'Group With Users', 'group_with_user@obm.org'), 
 (2, 0, 0, 3, 'GroupWithSubGroup', 0, 1002, 'GroupWithSubGroup', 'Group With SubGroup', 'group_with_subgroup@obm.org'),
 (2, 0, 0, 4, 'GroupWhoSubgroupHaveUser', 0, 1002, 'GroupWhoSubgroupHaveUser', 'Group Who Subgroup Have User', 'group_with_subgroup@obm.org'),
 (2, 0, 1, 5, 'GroupPrivate', 0, 1003, 'GroupPrivate', 'Group Private', 'GroupPrivate@obm.org');
 
INSERT INTO entity (entity_mailing)
    VALUES
        (true),
        (true),
        (true),
        (true),
        (true);
        
INSERT INTO userentity (userentity_entity_id, userentity_user_id)
    VALUES (4, 1), (5, 2);

INSERT INTO UserObm (userobm_domain_id, userobm_ext_id, userobm_login, userobm_password, userobm_password_type, userobm_perms, userobm_lastname, userobm_firstname, userobm_uid, userobm_gid, userobm_archive, userobm_email, userobm_mail_server_id) 
	 VALUES (1, 'Admin0ExtId','admin0','admin0','PLAIN','admin', 'Lastname', 'Firstname', '1000', '512', '0', 'admin0', NULL),
		(2, 'User1','user1','user1','PLAIN','user', '', '', '2002', '512', '0', 'user1', NULL);
 
INSERT INTO userobmgroup (userobmgroup_group_id, userobmgroup_userobm_id) VALUES (2, 1), (5, 1);

INSERT INTO groupgroup (groupgroup_parent_id, groupgroup_child_id) VALUES (3, 1), (4, 2);

INSERT INTO of_usergroup (of_usergroup_group_id, of_usergroup_user_id) VALUES (2, 1), (5, 1);

INSERT INTO UserSystem (usersystem_login, usersystem_password, usersystem_homedir)
    VALUES
        ('obmsatelliterequest', 'osrpassword', ''),
        ('cyrus', 'cyrus', ''),
        ('ldapadmin', 'secret', '');
