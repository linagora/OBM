CREATE DOMAIN userstatus AS VARCHAR(32);

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
ALTER TABLE domain ADD CONSTRAINT domain_pkey PRIMARY KEY (domain_id);
ALTER TABLE domain ALTER COLUMN domain_id SET DEFAULT nextval('domain_domain_id_seq');

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
CREATE SEQUENCE host_host_id_seq INCREMENT BY 1 CACHE 1;
ALTER TABLE host ALTER COLUMN host_id SET DEFAULT nextval('host_host_id_seq');

CREATE TABLE serviceproperty (
    serviceproperty_id integer NOT NULL,
    serviceproperty_service character varying(255) NOT NULL,
    serviceproperty_property character varying(255) NOT NULL,
    serviceproperty_entity_id integer NOT NULL,
    serviceproperty_value text
);
CREATE SEQUENCE serviceproperty_serviceproperty_id_seq INCREMENT BY 1 CACHE 1;
ALTER TABLE serviceproperty ADD CONSTRAINT serviceproperty_pkey PRIMARY KEY (serviceproperty_id);
ALTER TABLE serviceproperty ALTER COLUMN serviceproperty_id SET DEFAULT nextval('serviceproperty_serviceproperty_id_seq');

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

INSERT INTO domain (domain_id, domain_label, domain_name, domain_uuid) VALUES ('5978', 'test-domain', 'test-domain', 'e560a9c1-681c-4775-93c0-f589481d74ed');
INSERT INTO serviceproperty (serviceproperty_entity_id, serviceproperty_service, serviceproperty_property, serviceproperty_value)
  VALUES  ('4455', 'sync', 'obm_sync', '222'),
          ('4455', 'mail', 'imap', '291'),
          ('4455', 'mail', 'imap', '292'),
          ('4455', 'mail', 'imap', '293');
INSERT INTO domainentity (domainentity_entity_id, domainentity_domain_id) VALUES ('4455', '5978');
INSERT INTO host (host_id, host_domain_id, host_name, host_ip)
  VALUES  ('222', '5978', 'myhost', '12.23.34.45'),
          ('291', '5978', 'backend1', '1.2.3.1'),
          ('292', '5978', 'backend2', '1.2.3.2'),
          ('293', '5978', 'backend3', '1.2.3.3');
INSERT INTO userobm (userobm_id, userobm_login, userobm_password_type, userobm_password, userobm_archive, userobm_domain_id, userobm_mail_server_id)
  VALUES  (101, 'usera', 'PLAIN', 'secret', 0, 5978, 291),
          (102, 'userb', 'PLAIN', 'secret', 0, 5978, 292);