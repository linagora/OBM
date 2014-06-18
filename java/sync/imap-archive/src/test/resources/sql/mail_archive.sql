CREATE TABLE domain (
    domain_id integer NOT NULL,
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
CREATE SEQUENCE domain_domain_id_seq INCREMENT BY 1 CACHE 1;
ALTER TABLE domain ALTER COLUMN domain_id SET DEFAULT nextval('domain_domain_id_seq');

CREATE TABLE domainentity (
    domainentity_entity_id integer NOT NULL,
    domainentity_domain_id integer NOT NULL
);
ALTER TABLE domainentity ADD CONSTRAINT domainentity_pkey PRIMARY KEY (domainentity_entity_id, domainentity_domain_id);
CREATE INDEX domainentity_domain_id_fkey ON domainentity(domainentity_domain_id);
CREATE INDEX domainentity_entity_id_fkey ON domainentity(domainentity_entity_id);
ALTER TABLE domainentity
    ADD CONSTRAINT domainentity_domain_id_domain_id_fkey 
    FOREIGN KEY (domainentity_domain_id) 
    REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE;

CREATE TABLE entity (
    entity_id integer NOT NULL,
    entity_mailing boolean
);

ALTER TABLE domainentity
    ADD CONSTRAINT domainentity_entity_id_entity_id_fkey
    FOREIGN KEY (domainentity_entity_id) REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;

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
ALTER TABLE serviceproperty ALTER COLUMN serviceproperty_id SET DEFAULT nextval('serviceproperty_serviceproperty_id_seq');

CREATE DOMAIN repeat_kind AS VARCHAR (16) CHECK VALUE IN (
	'DAILY',
	'WEEKLY',
	'MONTHLY',
	'YEARLY'
);

CREATE TABLE mail_archive (
	id					SERIAL PRIMARY KEY,
	mail_archive_domain_id			INTEGER NOT NULL,
	mail_archive_activated			BOOLEAN DEFAULT FALSE,
	mail_archive_repeat_kind		repeat_kind,
	mail_archive_day_of_week		INTEGER,
	mail_archive_day_of_month		INTEGER,
	mail_archive_day_of_year		INTEGER,
	mail_archive_hour			INTEGER,
	mail_archive_minute			INTEGER,

	CONSTRAINT mail_archive_domain_id_ukey UNIQUE (mail_archive_domain_id),
	CONSTRAINT mail_archive_domain_id_fkey FOREIGN KEY (mail_archive_domain_id) REFERENCES domain(domain_id) ON UPDATE CASCADE ON DELETE CASCADE
);

