CREATE DOMAIN batch_status AS VARCHAR CHECK VALUE IN ('IDLE', 'RUNNING', 'ERROR', 'SUCCESS');
CREATE DOMAIN batch_entity_type AS VARCHAR CHECK VALUE IN ('GROUP', 'USER');
CREATE DOMAIN http_verb AS VARCHAR CHECK VALUE IN ('PUT', 'PATCH', 'GET', 'POST', 'DELETE');

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

CREATE TABLE domainentity (
    domainentity_entity_id integer NOT NULL,
    domainentity_domain_id integer NOT NULL
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

CREATE TABLE userobm (
    userobm_id integer PRIMARY KEY AUTO_INCREMENT,
    userobm_domain_id integer NOT NULL,
    userobm_timeupdate timestamp,
    userobm_timecreate timestamp DEFAULT now(),
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

CREATE TABLE userobmpref (
    userobmpref_id integer NOT NULL,
    userobmpref_user_id integer,
    userobmpref_option character varying(50) NOT NULL,
    userobmpref_value character varying(50) NOT NULL
);

CREATE TABLE userentity (
    userentity_entity_id integer NOT NULL,
    userentity_user_id integer NOT NULL
);

INSERT INTO domain (domain_name, domain_uuid, domain_label, domain_global) 
	VALUES 
	('test.tlse.lng', 'ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6', 'test.tlse.lng', false),
	('test2.tlse.lng', '3a2ba641-4ae0-4b40-aa5e-c3fd3acb78bf', 'test2.tlse.lng', false),
	('test3.tlse.lng', '68936f0f-2bb5-447c-87f5-efcd46f58122', 'test3.tlse.lng', false),
	('global.virt', '123456789', 'global.virt', true);

INSERT INTO UserObm (userobm_domain_id, userobm_login, userobm_password, userobm_password_type, userobm_perms, userobm_lastname, userobm_firstname, userobm_uid, userobm_gid, userobm_archive, userobm_email, userobm_mail_server_id) 
    VALUES
        (4, 'admin0','admin0','PLAIN','user', 'Lastname', 'Firstname', '1000', '512', '0', 'user1', 1);

INSERT INTO userobmpref (userobmpref_id, userobmpref_user_id, userobmpref_option, userobmpref_value)
    VALUES
	(1, 1, 'set_public_fb', 'true');

INSERT INTO userentity (userentity_entity_id, userentity_user_id)
    VALUES
        (3, 1);

INSERT INTO host (host_domain_id, host_name, host_ip, host_fqdn)
    VALUES
        (1, 'mail', '1.2.3.4', 'mail.tlse.lng'),
