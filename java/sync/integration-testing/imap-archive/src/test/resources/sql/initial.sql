--
-- OBM
--
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
    profilemodule_id integer PRIMARY KEY AUTO_INCREMENT,
    profilemodule_domain_id integer NOT NULL,
    profilemodule_profile_id integer,
    profilemodule_module_name character varying(64) DEFAULT ''::character varying NOT NULL,
    profilemodule_right integer
);

CREATE TABLE userobm (
    userobm_id integer PRIMARY KEY AUTO_INCREMENT,
    userobm_domain_id integer NOT NULL,
    userobm_timeupdate timestamp,
    userobm_timecreate timestamp DEFAULT now(),
    userobm_userupdate integer,
    userobm_usercreate integer,
    userobm_local integer DEFAULT 1,
    userobm_ext_id character varying(36),
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

INSERT INTO host (host_domain_id, host_name, host_ip, host_fqdn)
  VALUES
    (1, 'mail', '127.0.0.1', 'localhost');

INSERT INTO domain (domain_name, domain_uuid, domain_label, domain_global) 
  VALUES 
    ('mydomain.org', 'ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6', 'mydomain.org', false),
    ('otherDomain.org', '31ae9172-ca35-4045-8ea3-c3125dab771e', 'otherDomain.org', false);

INSERT INTO profile (profile_domain_id, profile_name)
  VALUES
    (1, 'admin'),
    (2, 'admin');
    
INSERT INTO profilemodule (profilemodule_domain_id, profilemodule_profile_id, profilemodule_right, profilemodule_module_name)
  VALUES
    (1, 1, 31, 'domain'),
    (2, 2, 31, 'domain');
    
INSERT INTO UserObm (userobm_domain_id, userobm_login, userobm_password, userobm_password_type, userobm_perms, userobm_lastname, userobm_firstname, userobm_uid, userobm_gid, userobm_ext_id, userobm_archive, userobm_email, userobm_mail_server_id) 
  VALUES
    (1, 'admin', 'trust3dToken', 'PLAIN', 'admin', 'Lastname', 'Firstname', '1000', '512', 'd4ad341d-89eb-4f3d-807a-cb372314845d', '0', 'admin', 1),
    (2, 'admin', 'trust3dToken', 'PLAIN', 'admin', 'Lastname', 'Firstname', '1000', '512', 'd4ad341d-89eb-4f3d-807a-cb372314845d', '0', 'admin', 1),
    (1, 'usera', 'usera', 'PLAIN', 'user', 'Usera', 'Usera', '1001', '513', '08607f19-05a4-42a2-9b02-6f11f3ceff3b', '0', 'usera', 1),
    (1, 'userb', 'userb', 'PLAIN', 'user', 'Userb', 'Userb', '1002', '514', '8e30e673-1c47-4ca8-85e8-4609d4228c10', '0', 'userb', 1),
    (1, 'userc', 'userc', 'PLAIN', 'user', 'Userc', 'Userc', '1003', '515', '2d7a5942-46ab-4fad-9bd2-608bde249671', '0', 'userc', 1),
    (1, 'usera-test', 'usera-test', 'PLAIN', 'user', 'Usera-test', 'Usera-test', '1004', '516', '546cc0f9-5e02-4f86-8b29-91bbab23334e', '0', 'usera-test', 1);

INSERT INTO userentity (userentity_entity_id, userentity_user_id)
  VALUES
    (3, 1),
    (4, 2),
    (5, 3),
    (6, 4),
    (7, 5),
    (8, 6);

INSERT INTO UserSystem (usersystem_login, usersystem_password, usersystem_homedir)
  VALUES
    ('cyrus', 'cyrus', '');


--
-- IMAP Archive
-- 
CREATE DOMAIN repeat_kind AS VARCHAR (16) CHECK VALUE IN (
   'DAILY',
   'WEEKLY',
   'MONTHLY',
   'YEARLY'
);

CREATE TABLE mail_archive (
   id                             SERIAL PRIMARY KEY,
   mail_archive_domain_uuid       character(36) NOT NULL,
   mail_archive_activated         BOOLEAN DEFAULT FALSE,
   mail_archive_main_folder       TEXT NOT NULL,
   mail_archive_repeat_kind       repeat_kind,
   mail_archive_day_of_week       INTEGER,
   mail_archive_day_of_month      INTEGER,
   mail_archive_day_of_year       INTEGER,
   mail_archive_hour              INTEGER,
   mail_archive_minute            INTEGER,
   mail_archive_excluded_folder   TEXT,
   mail_archive_move              BOOLEAN DEFAULT FALSE,

   CONSTRAINT mail_archive_domain_uuid_ukey UNIQUE (mail_archive_domain_uuid)
);

CREATE TABLE mail_archive_excluded_users (
   id                                        SERIAL PRIMARY KEY,
   mail_archive_excluded_users_domain_uuid   character(36) NOT NULL,
   mail_archive_excluded_users_user_uuid     character(36) NOT NULL,
   mail_archive_excluded_users_user_login    TEXT NOT NULL,

   CONSTRAINT mail_archive_excluded_users_ukey UNIQUE (mail_archive_excluded_users_domain_uuid, mail_archive_excluded_users_user_uuid)
);

CREATE TABLE mail_archive_mailing (
   id                                 SERIAL PRIMARY KEY,
   mail_archive_mailing_domain_uuid   character(36) NOT NULL,
   mail_archive_mailing_email         TEXT NOT NULL,

   CONSTRAINT mail_archive_mailing_ukey UNIQUE (mail_archive_mailing_domain_uuid, mail_archive_mailing_email)
);

CREATE DOMAIN mail_archive_status AS VARCHAR (16) CHECK VALUE IN (
   'SCHEDULED',
   'RUNNING',
   'ERROR',
   'SUCCESS'
);

CREATE TABLE mail_archive_run (
   id                                SERIAL PRIMARY KEY,
   mail_archive_run_uuid             character(36) NOT NULL,
   mail_archive_run_domain_uuid      character(36) NOT NULL,
   mail_archive_run_status           mail_archive_status NOT NULL,
   mail_archive_run_schedule         TIMESTAMP,
   mail_archive_run_start            TIMESTAMP,
   mail_archive_run_end              TIMESTAMP,
   mail_archive_run_higher_boundary  TIMESTAMP,
   mail_archive_run_recurrent        BOOLEAN NOT NULL,

   CONSTRAINT mail_archive_run_uuid_ukey UNIQUE (mail_archive_run_uuid)
);

CREATE TABLE mail_archive_folder (
   id      SERIAL PRIMARY KEY,
   folder  TEXT,

   CONSTRAINT mail_archive_folder_folder_ukey UNIQUE (folder)
);

CREATE TABLE mail_archive_processed_folder (
   id                                       SERIAL PRIMARY KEY,
   mail_archive_processed_folder_run_uuid   character(36) NOT NULL,
   mail_archive_processed_folder_id         INTEGER NOT NULL,
   mail_archive_processed_folder_start      TIMESTAMP NOT NULL,
   mail_archive_processed_folder_end        TIMESTAMP NOT NULL,
    mail_archive_processed_folder_status    mail_archive_status NOT NULL,

   CONSTRAINT mail_archive_processed_folder_ukey UNIQUE (mail_archive_processed_folder_run_uuid, mail_archive_processed_folder_id),
   CONSTRAINT mail_archive_processed_folder_run_id_fkey FOREIGN KEY (mail_archive_processed_folder_run_uuid) REFERENCES mail_archive_run(mail_archive_run_uuid) ON UPDATE CASCADE ON DELETE CASCADE,
   CONSTRAINT mail_archive_processed_folder_id_fkey FOREIGN KEY (mail_archive_processed_folder_id) REFERENCES mail_archive_folder(id) ON UPDATE CASCADE ON DELETE CASCADE
);
