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

