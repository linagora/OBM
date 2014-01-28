
--
-- Domains (replacing enums)
--
CREATE DOMAIN userstatus AS VARCHAR(32);
CREATE DOMAIN vcomponent AS VARCHAR(32);
CREATE DOMAIN vopacity AS VARCHAR(32);
CREATE DOMAIN vpartstat AS VARCHAR(32);
CREATE DOMAIN vrole AS VARCHAR(32);
CREATE DOMAIN vkind AS VARCHAR(32);

--
-- Table userobm
--
CREATE TABLE userobm (
    userobm_id integer NOT NULL,
    userobm_domain_id integer NOT NULL,
    userobm_timeupdate timestamp,
    userobm_timecreate timestamp,
    userobm_userupdate integer,
    userobm_usercreate integer,
    userobm_local integer,
    userobm_ext_id character varying(16),
    userobm_system integer,
    userobm_archive smallint NOT NULL,
    userobm_status userstatus,
    userobm_timelastaccess timestamp,
    userobm_login character varying(80) NOT NULL,
    userobm_nb_login_failed integer,
    userobm_password_type character varying(6) NOT NULL,
    userobm_password character varying(64) NOT NULL,
    userobm_password_dateexp date,
    userobm_account_dateexp date,
    userobm_perms character varying(254),
    userobm_delegation_target character varying(256),
    userobm_delegation character varying(256),
    userobm_calendar_version timestamp,
    userobm_uid integer,
    userobm_gid integer,
    userobm_datebegin date,
    userobm_hidden integer,
    userobm_kind character varying(64),
    userobm_lastname character varying(64),
    userobm_firstname character varying(64),
    userobm_title character varying(256),
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
    userobm_country_iso3166 character(2),
    userobm_phone character varying(32),
    userobm_phone2 character varying(32),
    userobm_mobile character varying(32),
    userobm_fax character varying(32),
    userobm_fax2 character varying(32),
    userobm_web_perms integer,
    userobm_web_list text,
    userobm_web_all integer,
    userobm_mail_perms integer,
    userobm_mail_ext_perms integer,
    userobm_email text,
    userobm_mail_server_id integer,
    userobm_mail_quota integer,
    userobm_mail_quota_use integer,
    userobm_mail_login_date timestamp,
    userobm_nomade_perms integer,
    userobm_nomade_enable integer,
    userobm_nomade_local_copy integer,
    userobm_nomade_datebegin timestamp,
    userobm_nomade_dateend timestamp,
    userobm_email_nomade text,
    userobm_vacation_enable integer,
    userobm_vacation_datebegin timestamp,
    userobm_vacation_dateend timestamp,
    userobm_vacation_message text,
    userobm_samba_perms integer,
    userobm_samba_home character varying(255),
    userobm_samba_home_drive character(2),
    userobm_samba_logon_script character varying(128),
    userobm_host_id integer,
    userobm_description character varying(255),
    userobm_location character varying(255),
    userobm_education character varying(255),
    userobm_photo_id integer,
    userobm_commonname character varying(256) DEFAULT ''::character varying
);
ALTER TABLE userobm ALTER COLUMN userobm_id SET DEFAULT nextval('userobm_userobm_id_seq');
CREATE SEQUENCE userobm_userobm_id_seq INCREMENT BY 1 CACHE 1;


--
-- Table userobmpref
--
CREATE TABLE userobmpref (
    userobmpref_id integer NOT NULL,
    userobmpref_user_id integer,
    userobmpref_option character varying(50) NOT NULL,
    userobmpref_value character varying(50) NOT NULL
);
ALTER TABLE userobmpref ALTER COLUMN userobmpref_id SET DEFAULT nextval('userobmpref_userobmpref_id_seq');
CREATE SEQUENCE userobmpref_userobmpref_id_seq INCREMENT BY 1 CACHE 1;


--
-- Table domain
--
CREATE TABLE domain (
    domain_id integer NOT NULL,
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
ALTER TABLE domain ALTER COLUMN domain_id SET DEFAULT nextval('domain_domain_id_seq');
CREATE SEQUENCE domain_domain_id_seq INCREMENT BY 1 CACHE 1;

CREATE TABLE domainentity (
    domainentity_entity_id integer NOT NULL,
    domainentity_domain_id integer NOT NULL
);

CREATE TABLE host (
    host_id integer NOT NULL,
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

--
-- Table event
--
CREATE TABLE event (
    event_id integer NOT NULL,
    event_domain_id integer NOT NULL,
    event_timeupdate timestamp,
    event_timecreate timestamp DEFAULT now(),
    event_userupdate integer,
    event_usercreate integer,
    event_ext_id character varying(300) DEFAULT ''::character varying NOT NULL,
    event_type vcomponent DEFAULT 'VEVENT'::vcomponent,
    event_origin character varying(255) DEFAULT ''::character varying NOT NULL,
    event_owner integer,
    event_timezone character varying(255) DEFAULT 'GMT'::character varying,
    event_opacity vopacity DEFAULT 'OPAQUE'::vopacity,
    event_title character varying(255) DEFAULT NULL::character varying,
    event_location character varying(255) DEFAULT NULL::character varying,
    event_category1_id integer,
    event_priority integer,
    event_privacy integer DEFAULT 0 NOT NULL,
    event_date timestamp,
    event_duration integer DEFAULT 0 NOT NULL,
    event_allday boolean DEFAULT false,
    event_repeatkind character varying(20) DEFAULT 'none'::character varying NOT NULL,
    event_repeatfrequence integer,
    event_repeatdays character varying(7) DEFAULT NULL::character varying,
    event_endrepeat timestamp,
    event_color character varying(7),
    event_completed timestamp,
    event_url text,
    event_allow_documents boolean DEFAULT false,
    event_description text,
    event_properties text,
    event_tag_id integer,
    event_sequence integer DEFAULT 0,
    CONSTRAINT duration_check CHECK ((event_duration >= 0))
);
ALTER TABLE event ALTER COLUMN event_id SET DEFAULT nextval('event_event_id_seq');
CREATE SEQUENCE event_event_id_seq START WITH 1 INCREMENT BY 1 CACHE 1;

--
-- Table eventcategory
--
CREATE TABLE eventcategory1 (
    eventcategory1_id integer NOT NULL,
    eventcategory1_domain_id integer NOT NULL,
    eventcategory1_timeupdate timestamp,
    eventcategory1_timecreate timestamp DEFAULT now(),
    eventcategory1_userupdate integer,
    eventcategory1_usercreate integer,
    eventcategory1_code character varying(10) DEFAULT ''::character varying,
    eventcategory1_label character varying(128) DEFAULT NULL::character varying,
    eventcategory1_color character(6)
);
ALTER TABLE eventcategory1 ADD CONSTRAINT eventcategory1_pkey PRIMARY KEY (eventcategory1_id);
ALTER TABLE eventcategory1 ALTER COLUMN eventcategory1_id SET DEFAULT nextval('eventcategory1_eventcategory1_id_seq');
CREATE SEQUENCE eventcategory1_eventcategory1_id_seq INCREMENT BY 1 CACHE 1;

--
-- Table eventalert
--
CREATE TABLE eventalert (
    eventalert_timeupdate timestamp ,
    eventalert_timecreate timestamp  DEFAULT now(),
    eventalert_userupdate integer DEFAULT NULL,
    eventalert_usercreate integer DEFAULT NULL,
    eventalert_event_id integer,
    eventalert_user_id integer,
    eventalert_duration integer DEFAULT 0 NOT NULL
);

--
-- Table eventexception
--
CREATE TABLE eventexception (
    eventexception_timeupdate timestamp,
    eventexception_timecreate timestamp DEFAULT now(),
    eventexception_userupdate integer,
    eventexception_usercreate integer,
    eventexception_parent_id integer NOT NULL,
    eventexception_child_id integer,
    eventexception_date timestamp NOT NULL
);

--
-- Table deletedevent
--
CREATE TABLE deletedevent (
    deletedevent_id integer NOT NULL,
    deletedevent_event_id integer,
    deletedevent_user_id integer,
    deletedevent_origin character varying(255) NOT NULL,
    deletedevent_type vcomponent DEFAULT 'VEVENT'::vcomponent,
    deletedevent_timestamp timestamp,
    deletedevent_event_ext_id character varying(300) DEFAULT ''::character varying
);
ALTER TABLE deletedevent ALTER COLUMN deletedevent_id SET DEFAULT nextval('deletedevent_deletedevent_id_seq');
CREATE SEQUENCE deletedevent_deletedevent_id_seq INCREMENT BY 1 CACHE 1;


--
-- Table deletedeventlink
--
CREATE TABLE deletedeventlink (
    deletedeventlink_id integer DEFAULT nextval('deletedeventlink_deletedeventlink_id_seq') NOT NULL,
    deletedeventlink_userobm_id integer NOT NULL,
    deletedeventlink_event_id integer NOT NULL,
    deletedeventlink_event_ext_id character varying(300) NOT NULL,
    deletedeventlink_time_removed timestamp DEFAULT now() NOT NULL
);
CREATE SEQUENCE deletedeventlink_deletedeventlink_id_seq START WITH 1 INCREMENT BY 1 CACHE 1;


--
-- Table eventlink
--
CREATE TABLE eventlink (
    eventlink_timeupdate timestamp ,
    eventlink_timecreate timestamp DEFAULT now(),
    eventlink_userupdate integer,
    eventlink_usercreate integer,
    eventlink_event_id integer NOT NULL,
    eventlink_entity_id integer NOT NULL,
    eventlink_state vpartstat DEFAULT 'NEEDS-ACTION'::vpartstat,
    eventlink_required vrole DEFAULT 'REQ'::vrole,
    eventlink_percent double precision DEFAULT 0,
    eventlink_is_organizer boolean DEFAULT false,
    eventlink_comment character varying(255)
);
ALTER TABLE deletedeventlink ADD CONSTRAINT deletedeventlink_deletedeventlink_id_pkey PRIMARY KEY (deletedeventlink_id);
ALTER TABLE eventlink ADD CONSTRAINT eventlink_pkey PRIMARY KEY (eventlink_event_id, eventlink_entity_id);


--
-- Table serviceproperty
--
CREATE TABLE serviceproperty (
    serviceproperty_id integer NOT NULL,
    serviceproperty_service character varying(255) NOT NULL,
    serviceproperty_property character varying(255) NOT NULL,
    serviceproperty_entity_id integer NOT NULL,
    serviceproperty_value text
);
CREATE SEQUENCE serviceproperty_serviceproperty_id_seq INCREMENT BY 1 CACHE 1;
ALTER TABLE serviceproperty
    ADD CONSTRAINT serviceproperty_pkey PRIMARY KEY (serviceproperty_id);
ALTER TABLE serviceproperty ALTER COLUMN serviceproperty_id SET DEFAULT nextval('serviceproperty_serviceproperty_id_seq');


--
-- Table contact
--
CREATE TABLE contact (
    contact_id integer NOT NULL,
    contact_domain_id integer NOT NULL,
    contact_timeupdate timestamp ,
    contact_timecreate timestamp DEFAULT now(),
    contact_userupdate integer,
    contact_usercreate integer,
    contact_datasource_id integer,
    contact_addressbook_id integer,
    contact_company_id integer,
    contact_company character varying(64),
    contact_kind_id integer,
    contact_marketingmanager_id integer,
    contact_lastname character varying(64) DEFAULT ''::character varying,
    contact_firstname character varying(64),
    contact_middlename character varying(32) DEFAULT NULL::character varying,
    contact_suffix character varying(16) DEFAULT NULL::character varying,
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
    contact_commonname character varying(256) DEFAULT ''::character varying
);
ALTER TABLE contact ADD CONSTRAINT contact_pkey PRIMARY KEY (contact_id);
ALTER TABLE contact ALTER COLUMN contact_id SET DEFAULT nextval('contact_contact_id_seq');
CREATE SEQUENCE contact_contact_id_seq INCREMENT BY 1 CACHE 1;

--
-- Table resource
--
CREATE TABLE resource (
    resource_id integer NOT NULL,
    resource_domain_id integer NOT NULL,
    resource_rtype_id integer,
    resource_timeupdate timestamp,
    resource_timecreate timestamp DEFAULT now(),
    resource_userupdate integer,
    resource_usercreate integer,
    resource_name character varying(32) DEFAULT ''::character varying NOT NULL,
    resource_delegation character varying(256) DEFAULT ''::character varying,
    resource_description character varying(255),
    resource_qty integer DEFAULT 0,
    resource_email text
);
ALTER TABLE resource ALTER COLUMN resource_id SET DEFAULT nextval('resource_resource_id_seq');
ALTER TABLE resource ADD CONSTRAINT resource_pkey PRIMARY KEY (resource_id);
ALTER TABLE resource ADD CONSTRAINT resource_resource_email_key UNIQUE (resource_email);
CREATE SEQUENCE resource_resource_id_seq START WITH 1 INCREMENT BY 1 CACHE 1;

--
-- Table email
--
CREATE TABLE email (
    email_id integer NOT NULL,
    email_entity_id integer NOT NULL,
    email_label character varying(255) NOT NULL,
    email_address character varying(255)
);
ALTER TABLE email ALTER COLUMN email_id SET DEFAULT nextval('email_email_id_seq');
ALTER TABLE email ADD CONSTRAINT email_pkey PRIMARY KEY (email_id);
CREATE SEQUENCE email_email_id_seq INCREMENT BY 1 CACHE 1;

--
-- Table addressbook
--
CREATE TABLE addressbook (
    id integer NOT NULL,
    domain_id integer NOT NULL,
    timeupdate timestamp ,
    timecreate timestamp DEFAULT now(),
    userupdate integer,
    usercreate integer,
    origin character varying(255) NOT NULL,
    owner integer,
    name character varying(64) NOT NULL,
    is_default boolean DEFAULT false,
    syncable boolean DEFAULT true
);
ALTER TABLE addressbook ALTER COLUMN id SET DEFAULT nextval('addressbook_id_seq');
ALTER TABLE addressbook ADD CONSTRAINT addressbook_pkey PRIMARY KEY (id);
CREATE SEQUENCE addressbook_id_seq INCREMENT BY 1 CACHE 1;

--
-- Table phone
--
CREATE TABLE phone (
    phone_id integer NOT NULL,
    phone_entity_id integer NOT NULL,
    phone_label character varying(255) NOT NULL,
    phone_number character varying(32)
);
ALTER TABLE phone ALTER COLUMN phone_id SET DEFAULT nextval('phone_phone_id_seq');
ALTER TABLE phone ADD CONSTRAINT phone_pkey PRIMARY KEY (phone_id);
CREATE SEQUENCE phone_phone_id_seq INCREMENT BY 1 CACHE 1;

--
-- Table address
--
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

--
-- Table Country
--
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
 
--
-- Table website
--
CREATE TABLE website (
    website_id integer NOT NULL,
    website_entity_id integer NOT NULL,
    website_label character varying(255) NOT NULL,
    website_url text
);
ALTER TABLE website ADD CONSTRAINT website_pkey PRIMARY KEY (website_id);
ALTER TABLE website ALTER COLUMN website_id SET DEFAULT nextval('website_website_id_seq');
CREATE SEQUENCE website_website_id_seq START WITH 1 INCREMENT BY 1 CACHE 1;

--
-- Table im
--
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

--
-- Table entity
--
CREATE TABLE entity (
    entity_id integer NOT NULL,
    entity_mailing boolean
);
ALTER TABLE entity ALTER COLUMN entity_id SET DEFAULT nextval('entity_entity_id_seq');
ALTER TABLE entity ADD CONSTRAINT entity_pkey PRIMARY KEY (entity_id);
CREATE SEQUENCE entity_entity_id_seq INCREMENT BY 1 CACHE 1;

--
-- Table evententity
--
CREATE TABLE evententity (
    evententity_entity_id integer NOT NULL,
    evententity_event_id integer NOT NULL
);
ALTER TABLE evententity ADD CONSTRAINT evententity_pkey PRIMARY KEY (evententity_entity_id, evententity_event_id);

--
-- Table userentity
--
CREATE TABLE userentity (
    userentity_entity_id integer NOT NULL,
    userentity_user_id integer NOT NULL
);
ALTER TABLE userentity ADD CONSTRAINT userentity_pkey PRIMARY KEY (userentity_entity_id, userentity_user_id);

--
-- Table contactentity
--
CREATE TABLE contactentity (
    contactentity_entity_id integer NOT NULL,
    contactentity_contact_id integer NOT NULL
);
ALTER TABLE contactentity ADD CONSTRAINT contactentity_pkey PRIMARY KEY (contactentity_entity_id, contactentity_contact_id);

--
-- Table addressbookentity
--
CREATE TABLE addressbookentity (
    addressbookentity_entity_id integer NOT NULL,
    addressbookentity_addressbook_id integer NOT NULL
);
ALTER TABLE addressbookentity ADD CONSTRAINT addressbookentity_pkey PRIMARY KEY (addressbookentity_entity_id, addressbookentity_addressbook_id);

--
-- Table resourceentity
--
CREATE TABLE resourceentity (
    resourceentity_entity_id integer NOT NULL,
    resourceentity_resource_id integer NOT NULL
);
ALTER TABLE resourceentity ADD CONSTRAINT resourceentity_pkey PRIMARY KEY (resourceentity_entity_id, resourceentity_resource_id);

--
-- Table entityright
--
CREATE TABLE entityright (
    entityright_id integer NOT NULL,
    entityright_entity_id integer NOT NULL,
    entityright_consumer_id integer,
    entityright_access integer DEFAULT 0 NOT NULL,
    entityright_read integer DEFAULT 0 NOT NULL,
    entityright_write integer DEFAULT 0 NOT NULL,
    entityright_admin integer DEFAULT 0 NOT NULL
);
ALTER TABLE entityright ALTER COLUMN entityright_id SET DEFAULT nextval('entityright_entityright_id_seq');
ALTER TABLE entityright ADD CONSTRAINT entityright_pkey PRIMARY KEY (entityright_id);
CREATE SEQUENCE entityright_entityright_id_seq INCREMENT BY 1 CACHE 1;

--
-- Table of_usergroup
--
CREATE TABLE of_usergroup (
    of_usergroup_group_id integer NOT NULL,
    of_usergroup_user_id integer NOT NULL
);
ALTER TABLE of_usergroup ADD CONSTRAINT of_usergroup_pkey PRIMARY KEY (of_usergroup_group_id, of_usergroup_user_id);

--
-- Table groupentity
--
CREATE TABLE groupentity (
    groupentity_entity_id integer NOT NULL,
    groupentity_group_id integer NOT NULL
);
ALTER TABLE groupentity ADD CONSTRAINT groupentity_pkey PRIMARY KEY (groupentity_entity_id, groupentity_group_id);

--
-- Table calendarentity
--
CREATE TABLE calendarentity (
    calendarentity_entity_id integer NOT NULL,
    calendarentity_calendar_id integer NOT NULL
);

--
-- Table commitedoperation
--
CREATE TABLE commitedoperation (
    commitedoperation_hash_client_id character varying(44) NOT NULL,
    commitedoperation_entity_id integer NOT NULL,
    commitedoperation_kind vkind NOT NULL
);
ALTER TABLE commitedoperation ADD CONSTRAINT commitedoperation_pkey PRIMARY KEY (commitedoperation_hash_client_id);

--
-- Table profile 
--

CREATE TABLE profile (
    profile_id integer NOT NULL,
    profile_domain_id integer NOT NULL,
    profile_timeupdate timestamp,
    profile_timecreate timestamp,
    profile_userupdate integer,
    profile_usercreate integer,
    profile_name character varying(64) DEFAULT NULL
);
ALTER TABLE profile ADD CONSTRAINT profile_pkey PRIMARY KEY (profile_id);
CREATE SEQUENCE profile_profile_id_seq INCREMENT BY 1 CACHE 1;
ALTER TABLE profile ALTER COLUMN profile_id SET DEFAULT nextval('profile_profile_id_seq');

--
-- Table profilemodule 
--

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

--
-- Cascades
--
ALTER TABLE contactentity
    ADD CONSTRAINT contactentity_contact_id_contact_id_fkey FOREIGN KEY (contactentity_contact_id) REFERENCES contact(contact_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE contactentity
    ADD CONSTRAINT contactentity_entity_id_entity_id_fkey FOREIGN KEY (contactentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;



ALTER TABLE userentity
    ADD CONSTRAINT userentity_entity_id_entity_id_fkey FOREIGN KEY (userentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE userentity
    ADD CONSTRAINT userentity_user_id_user_id_fkey FOREIGN KEY (userentity_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


ALTER TABLE commitedoperation
    ADD CONSTRAINT commitedoperation_entity_id_fkey FOREIGN KEY (commitedoperation_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


ALTER TABLE serviceproperty
    ADD CONSTRAINT serviceproperty_entity_id_entity_id_fkey FOREIGN KEY (serviceproperty_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;



ALTER TABLE eventalert
    ADD CONSTRAINT eventalert_event_id_event_id_fkey FOREIGN KEY (eventalert_event_id) REFERENCES event(event_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE eventalert
    ADD CONSTRAINT eventalert_user_id_userobm_id_fkey FOREIGN KEY (eventalert_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE eventalert
    ADD CONSTRAINT eventalert_usercreate_userobm_id_fkey FOREIGN KEY (eventalert_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;
ALTER TABLE eventalert
    ADD CONSTRAINT eventalert_userupdate_userobm_id_fkey FOREIGN KEY (eventalert_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;



ALTER TABLE eventexception
    ADD CONSTRAINT eventexception_parent_id_event_id_fkey FOREIGN KEY (eventexception_parent_id) REFERENCES event(event_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE eventexception
    ADD CONSTRAINT eventexception_child_id_event_id_fkey FOREIGN KEY (eventexception_child_id) REFERENCES event(event_id) ON UPDATE CASCADE ON DELETE SET NULL;
ALTER TABLE eventexception
    ADD CONSTRAINT eventexception_usercreate_userobm_id_fkey FOREIGN KEY (eventexception_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;
ALTER TABLE eventexception
    ADD CONSTRAINT eventexception_userupdate_userobm_id_fkey FOREIGN KEY (eventexception_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;



ALTER TABLE contact
    ADD CONSTRAINT contact_addressbook_id_addressbook_id_fkey FOREIGN KEY (contact_addressbook_id) REFERENCES addressbook(id) ON UPDATE CASCADE ON DELETE CASCADE;


ALTER TABLE email
    ADD CONSTRAINT email_entity_id_entity_id_fkey FOREIGN KEY (email_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


ALTER TABLE addressbook
    ADD CONSTRAINT addressbook_domain_id_domain_id_fkey FOREIGN KEY (domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE addressbook
    ADD CONSTRAINT addressbook_owner_userobm_id_fkey FOREIGN KEY (owner) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;
ALTER TABLE addressbook
    ADD CONSTRAINT addressbook_usercreate_userobm_id_fkey FOREIGN KEY (usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;
ALTER TABLE addressbook
    ADD CONSTRAINT addressbook_userupdate_userobm_id_fkey FOREIGN KEY (userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


ALTER TABLE addressbookentity
    ADD CONSTRAINT addressbookentity_addressbook_id_addressbook_id_fkey FOREIGN KEY (addressbookentity_addressbook_id) REFERENCES addressbook(id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE addressbookentity
    ADD CONSTRAINT addressbookentity_entity_id_entity_id_fkey FOREIGN KEY (addressbookentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE entityright
    ADD CONSTRAINT entityright_consumer_id_entity_id FOREIGN KEY (entityright_consumer_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE entityright
    ADD CONSTRAINT entityright_entity_id_entity_id FOREIGN KEY (entityright_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;


ALTER TABLE of_usergroup
    ADD CONSTRAINT of_usergroup_user_id_userobm_id_fkey FOREIGN KEY (of_usergroup_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE groupentity
    ADD CONSTRAINT groupentity_entity_id_entity_id_fkey FOREIGN KEY (groupentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE phone
    ADD CONSTRAINT phone_entity_id_entity_id_fkey FOREIGN KEY (phone_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE address
    ADD CONSTRAINT address_entity_id_entity_id_fkey FOREIGN KEY (address_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE website
    ADD CONSTRAINT website_entity_id_entity_id_fkey FOREIGN KEY (website_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE im
    ADD CONSTRAINT im_entity_id_entity_id_fkey FOREIGN KEY (im_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE eventcategory1
    ADD CONSTRAINT eventcategory1_domain_id_domain_id_fkey FOREIGN KEY (eventcategory1_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE eventcategory1
    ADD CONSTRAINT eventcategory1_usercreate_userobm_id_fkey FOREIGN KEY (eventcategory1_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;
ALTER TABLE eventcategory1
    ADD CONSTRAINT eventcategory1_userupdate_userobm_id_fkey FOREIGN KEY (eventcategory1_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;


ALTER TABLE eventlink
    ADD CONSTRAINT eventlink_entity_id_entity_id_fkey FOREIGN KEY (eventlink_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE eventlink
    ADD CONSTRAINT eventlink_event_id_event_id_fkey FOREIGN KEY (eventlink_event_id) REFERENCES event(event_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE eventlink
    ADD CONSTRAINT eventlink_usercreate_userobm_id_fkey FOREIGN KEY (eventlink_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;
ALTER TABLE eventlink
    ADD CONSTRAINT eventlink_userupdate_userobm_id_fkey FOREIGN KEY (eventlink_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE resourceentity
    ADD CONSTRAINT resourceentity_entity_id_entity_id_fkey FOREIGN KEY (resourceentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE deletedeventlink
    ADD CONSTRAINT deletedeventlink_event_id_event_id_fkey FOREIGN KEY (deletedeventlink_event_id) REFERENCES event(event_id) ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE deletedeventlink
    ADD CONSTRAINT deletedeventlink_userobm_id_userobm_id_fkey FOREIGN KEY (deletedeventlink_userobm_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


ALTER TABLE resource
    ADD CONSTRAINT resource_domain_id_domain_id_fkey FOREIGN KEY (resource_domain_id) REFERENCES domain(domain_id)  ON UPDATE CASCADE ON DELETE CASCADE;
ALTER TABLE resource
    ADD CONSTRAINT resource_usercreate_userobm_id_fkey FOREIGN KEY (resource_usercreate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;
ALTER TABLE resource
    ADD CONSTRAINT resource_userupdate_userobm_id_fkey FOREIGN KEY (resource_userupdate) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE SET NULL;

ALTER TABLE profilemodule
    ADD CONSTRAINT profilemodule_profile_id_profile_id_fkey FOREIGN KEY (profilemodule_profile_id) REFERENCES profile(profile_id) ON UPDATE CASCADE ON DELETE CASCADE;

--
-- Data inserts
--

INSERT INTO Domain (domain_timecreate,domain_label,domain_description,domain_name,domain_global, domain_uuid) 
	VALUES
		(NOW(), 'Test domain', 'Test domain', 'domain.org', FALSE, 'b55911e6-6848-4f16-abd4-52d94b6901a6'),
		(NOW(), 'Test domain 2', 'Test domain 2', 'domain2.org', FALSE, 'abcdefgh');

INSERT INTO UserObm (userobm_domain_id, userobm_login, userobm_password, userobm_password_type, userobm_perms, userobm_lastname, userobm_firstname, userobm_uid, userobm_gid, userobm_archive, userobm_email) 
	VALUES
        (1, 'user1','user1','PLAIN','user', 'Lastname', 'Firstname', '1000', '512', '0', 'user1'),
        (1, 'user2','user2','PLAIN','user', 'Lastname', 'Firstname', '1000', '512', '0', 'user2'),
        (1, 'user3','user3','PLAIN','user', 'Lastname', 'Firstname', '1000', '512', '0', 'user3'),
        (2, 'user2','user2','PLAIN','user', 'Lastname', 'Firstname', '1000', '512', '0', 'user2'),;

INSERT INTO entity (entity_mailing) VALUES (TRUE), (TRUE), (TRUE), (TRUE), (TRUE), (TRUE);
INSERT INTO userentity (userentity_entity_id, userentity_user_id) VALUES (1, 1), (2, 2), (3, 3), (4, 4);
INSERT INTO domainentity (domainentity_entity_id, domainentity_domain_id)
    VALUES
        (1, 6),
        (2, 7);
INSERT INTO calendarentity (calendarentity_entity_id, calendarentity_calendar_id) VALUES (1, 1), (2, 2), (3, 3), (4, 4);
INSERT INTO EntityRight (entityright_entity_id, entityright_consumer_id, entityright_access, entityright_read, entityright_write, entityright_admin)
    VALUES
        (1, 2, 1, 1, 1, 0),
        (1, 3, 1, 1, 0, 0);

INSERT INTO addressbook (domain_id, timeupdate, timecreate, userupdate, usercreate, owner, origin, name, is_default)
    VALUES
        (1, now(), now(), 1, 1, 1, 'integration-testing', 'collected_contacts', TRUE),
        (1, now(), now(), 2, 2, 2, 'integration-testing', 'collected_contacts', TRUE),
        (1, now(), now(), 3, 3, 3, 'integration-testing', 'collected_contacts', TRUE),
        (1, now(), now(), 1, 1, 1, 'integration-testing', 'contacts', TRUE);
INSERT INTO AddressBookEntity (addressbookentity_entity_id, addressbookentity_addressbook_id)
	VALUES (1, 1),
		            (2, 2),
		            (3, 3),
		            (4, 4);

INSERT INTO eventcategory1 (eventcategory1_domain_id, eventcategory1_timeupdate, eventcategory1_timecreate, eventcategory1_userupdate, eventcategory1_usercreate, eventcategory1_label)
    VALUES (1, NULL,NULL,NULL,NULL,'existing_category');

INSERT INTO UserObmPref (userobmpref_option, userobmpref_value)
    VALUES ('set_lang', 'FR');

INSERT INTO country (country_domain_id, country_timeupdate, country_timecreate, country_userupdate, country_usercreate, country_iso3166, country_name, country_lang, country_phone )
	VALUES	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'AD', 'Andorre', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'AE', 'Emirats Arabes Unis', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'AL', 'Albanie', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'AM', 'Arménie', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'AO', 'Angola', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'AR', 'Argentine', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'AT', 'Autriche', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'AU', 'Australie', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'AZ', 'Azerbaidjan', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'BA', 'Bosnie-Herzégovine', 'FR', '+387'), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'BB', 'La Barbade', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'BD', 'Bangladesh', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'BE', 'Belgique', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'BF', 'Burkina Faso', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'BG', 'Bulgarie', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'BJ', 'Benin', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'BO', 'Bolivie', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'BR', 'Brésil', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'BS', 'Bahamas', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'BY', 'Bielorussie', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'CA', 'Canada', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'CF', 'Rép. Centraficaine', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'CG', 'Congo', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'CH', 'Suisse', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'CI', 'Rep. Côte Ivoire', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'CL', 'Chili', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'CM', 'Cameroun', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'CN', 'Chine', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'CO', 'Colombie', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'CR', 'Costa Rica', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'CS', 'Serbie-Monténégro', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'CU', 'Cuba', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'CY', 'Chypre', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'CZ', 'Rep.Tchèque', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'DE', 'Allemagne', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'DK', 'Danemark', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'DZ', 'Algérie', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'EC', 'Equateur', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'EE', 'Estonie', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'EG', 'Egypte', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'ES', 'Espagne', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'FI', 'Finlande', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'FR', 'France', 'EN', '+33'), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'FR', 'France', 'FR', '+33'), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'GA', 'Gabon', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'GB', 'Royaume Uni', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'GE', 'Georgie', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'GH', 'Ghana', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'GI', 'Gibraltar', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'GL', 'Groenland', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'GN', 'Guinée', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'GR', 'Grèce', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'GT', 'Guatemala', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'HK', 'Hong Kong', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'HR', 'Croatie', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'HU', 'Hongrie', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'ID', 'Indonésie', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'IE', 'Irlande', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'IL', 'Israel', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'IN', 'Inde', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'IQ', 'Irak', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'IR', 'Iran', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'IS', 'Islande', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'IT', 'Italie', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'JM', 'Jamaique', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'JO', 'Jordanie', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'JP', 'Japon', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'KE', 'Kenya', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'KP', 'Corée du Nord', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'KR', 'Corée du Sud', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'KW', 'Koweit', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'KZ', 'Kazakhstan', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'LB', 'Liban', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'LI', 'Liechtenstein', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'LK', 'Sri Lanka', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'LT', 'Lituanie', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'LU', 'Luxembourg', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'LV', 'Lettonie', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'LY', 'Libye', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'MA', 'Maroc', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'MC', 'Monaco', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'MD', 'Moldova', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'MG', 'Madagascar', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'MK', '« Ex République Yougoslave de Macedoine »', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'ML', 'Mali', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'MT', 'Malte', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'MU', 'Mauritius', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'MW', 'Malawi', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'MX', 'Mexique', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'MY', 'Malaisie', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'NA', 'Namibie', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'NE', 'Nigeria', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'NI', 'Nicaragua', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'NL', 'Pays Bas', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'NO', 'Norvège', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'NP', 'Népal', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'NZ', 'Nouvelle Zélande', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'OM', 'Oman', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'PE', 'Pérou', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'PF', 'Polynésie Française', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'PH', 'Phillipines', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'PK', 'Pakistan', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'PL', 'Pologne', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'PR', 'Porto Rico', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'PT', 'Portugal', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'PY', 'Paraguay', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'RO', 'Roumanie', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'RU', 'Russie', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'SA', 'Arabie Saoudite', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'SE', 'Suède', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'SG', 'Singapour', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'SI', 'Slovenie', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'SK', 'Slovaquie', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'SM', 'San Marino', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'SN', 'Sénégal', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'SY', 'Syrie', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'TG', 'Togo', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'TH', 'Thailande', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'TN', 'Tunisie', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'TR', 'Turquie', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'TT', 'Trinité & Tobago', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'TW', 'Taiwan', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'UA', 'Ukraine', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'US', 'USA', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'UY', 'Uruguay', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'VA', 'Saint-Siège', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'VE', 'Vénézuela', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'VN', 'Vietnam', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'ZA', 'Afriq. Sud', 'FR', ''), 
	(1, '2009-03-16 10:29:58', '2009-03-16 10:29:58', NULL, NULL, 'ZW', 'Zimbabwe', 'FR', '');
    
