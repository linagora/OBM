CREATE DOMAIN mail_archive_status AS VARCHAR (16) CHECK VALUE IN (
	'SCHEDULED',
	'RUNNING',
	'ERROR',
	'SUCCESS'
);

CREATE TABLE mail_archive_run (
	id									SERIAL PRIMARY KEY,
	mail_archive_run_uuid				character(36) NOT NULL,
	mail_archive_run_domain_uuid		character(36) NOT NULL,
 	mail_archive_run_status				mail_archive_status NOT NULL,
	mail_archive_run_schedule			TIMESTAMP,
	mail_archive_run_start				TIMESTAMP,
	mail_archive_run_end				TIMESTAMP,
	mail_archive_run_higher_boundary	TIMESTAMP,
	mail_archive_run_recurrent			BOOLEAN NOT NULL,

	CONSTRAINT mail_archive_run_uuid_ukey UNIQUE (mail_archive_run_uuid)
);

