CREATE DOMAIN batch_status AS VARCHAR CHECK VALUE IN ('IDLE', 'RUNNING', 'ERROR', 'SUCCESS');
CREATE DOMAIN batch_entity_type AS VARCHAR CHECK VALUE IN ('GROUP', 'USER', 'GROUP_MEMBERSHIP', 'USER_MEMBERSHIP');
CREATE DOMAIN http_verb AS VARCHAR CHECK VALUE IN ('PUT', 'PATCH', 'GET', 'POST', 'DELETE');

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
 (2, 0, 0, 4, 'GroupWhoSubgroupHaveUser', 0, 1002, 'GroupWhoSubgroupHaveUser', 'Group Who Subgroup Have User', 'group_with_subgroup@obm.org');
 
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
		(2, 'User1','user1','','','', '', '', '2002', '512', '0', 'user1', NULL);
 
INSERT INTO userobmgroup (userobmgroup_group_id, userobmgroup_userobm_id) VALUES (2, 1);
INSERT INTO groupgroup (groupgroup_parent_id, groupgroup_child_id) VALUES (3, 1), (4, 2);

INSERT INTO of_usergroup (of_usergroup_group_id, of_usergroup_user_id) VALUES (2, 1);

INSERT INTO UserSystem (usersystem_login, usersystem_password, usersystem_homedir)
    VALUES
        ('obmsatelliterequest', 'osrpassword', ''),
        ('cyrus', 'cyrus', ''),
        ('ldapadmin', 'secret', '');
