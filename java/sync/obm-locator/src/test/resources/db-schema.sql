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

INSERT INTO domain (domain_id, domain_label, domain_name, domain_uuid) VALUES ('5978', 'test-domain', 'test-domain', 'e560a9c1-681c-4775-93c0-f589481d74ed');
INSERT INTO serviceproperty (serviceproperty_entity_id, serviceproperty_service, serviceproperty_property, serviceproperty_value) VALUES ('4455', 'sync', 'obm_sync', '222');
INSERT INTO domainentity (domainentity_entity_id, domainentity_domain_id) VALUES ('4455', '5978');
INSERT INTO host (host_id, host_domain_id, host_name, host_ip) VALUES ('222', '5978', 'myhost', '12.23.34.45');