CREATE DOMAIN userstatus AS VARCHAR (16) CHECK VALUE IN (
    'INIT',
    'VALID'
);

CREATE TABLE domain (
    domain_id integer PRIMARY KEY AUTO_INCREMENT,
    domain_timeupdate timestamp,
    domain_timecreate timestamp DEFAULT now(),
    domain_usercreate integer,
    domain_userupdate integer,
    domain_label character varying(32) NOT NULL,
    domain_description character varying(255),
    domain_name character varying(128),
    domain_alias text,
    domain_global boolean DEFAULT false,
    domain_uuid character(36) NOT NULL
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
    userobm_status userstatus DEFAULT 'VALID',
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
    userobm_commonname character varying(256) DEFAULT '',
    CONSTRAINT userobm_domain_id_domain_id_fkey FOREIGN KEY (userobm_domain_id) REFERENCES domain(domain_id) ON DELETE CASCADE
);

CREATE TABLE opush_device (
    id integer PRIMARY KEY AUTO_INCREMENT,
    identifier character varying(255) NOT NULL,
    owner integer,
    type character varying(64) NOT NULL,
    CONSTRAINT opush_device_owner_fkey FOREIGN KEY (owner) REFERENCES userobm(userobm_id) ON DELETE CASCADE
);

CREATE TABLE opush_folder_mapping (
    id integer PRIMARY KEY AUTO_INCREMENT,
    device_id integer NOT NULL,
    collection character varying(255) NOT NULL,
    CONSTRAINT opush_folder_mapping_device_id_fkey FOREIGN KEY (device_id) REFERENCES opush_device(id) ON DELETE CASCADE
);

CREATE TABLE opush_folder_sync_state (
    id integer PRIMARY KEY AUTO_INCREMENT,
    sync_key character varying(64) NOT NULL,
    device_id integer NOT NULL,
    CONSTRAINT opush_folder_sync_state_sync_key_key UNIQUE (sync_key),
    CONSTRAINT opush_folder_sync_state_device_id_fkey FOREIGN KEY (device_id) REFERENCES opush_device(id) ON DELETE CASCADE
);

CREATE TABLE opush_folder_snapshot (
    id integer PRIMARY KEY AUTO_INCREMENT,
    folder_sync_state_id integer NOT NULL,
    collection_id integer NOT NULL,
    CONSTRAINT opush_folder_snapshot_collection_id_fkey FOREIGN KEY (collection_id) REFERENCES opush_folder_mapping(id) ON DELETE CASCADE,
    CONSTRAINT opush_folder_snapshot_folder_sync_state_id_fkey FOREIGN KEY (folder_sync_state_id) REFERENCES opush_folder_sync_state(id) ON DELETE CASCADE
);


INSERT INTO Domain (domain_timecreate,domain_label,domain_description,domain_name,domain_global, domain_uuid)
 VALUES  (NOW(), 'TestingDomain', 'Testing domain', 'domain.org', TRUE, RANDOM_UUID());

INSERT INTO userobm (userobm_domain_id, userobm_archive, userobm_login, userobm_password_type, userobm_password)
 VALUES (1, 0, 'user', 'PLAIN', 'user');
 
INSERT INTO opush_device (identifier, type, owner)
 VALUES ('devId', 'devType', 1);