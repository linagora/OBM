BEGIN;

CREATE TYPE repeat_kind AS ENUM ('DAILY', 'WEEKLY', 'MONTHLY', 'YEARLY');

CREATE TABLE mail_archive (
id     SERIAL PRIMARY KEY,
mail_archive_domain_uuid character(36) NOT NULL,
mail_archive_activated BOOLEAN DEFAULT FALSE,
mail_archive_repeat_kind repeat_kind,
mail_archive_day_of_week INTEGER,
mail_archive_day_of_month INTEGER,
mail_archive_day_of_year INTEGER,
mail_archive_hour INTEGER,
mail_archive_minute INTEGER,
CONSTRAINT mail_archive_domain_uuid_ukey UNIQUE (mail_archive_domain_uuid)
);

CREATE TYPE mail_archive_status AS ENUM ('ERROR', 'WARNING', 'RUNNING', 'SUCCESS');

CREATE TABLE mail_archive_run (
id SERIAL PRIMARY KEY,
mail_archive_run_uuid character(36) NOT NULL,
mail_archive_run_domain_uuid character(36) NOT NULL,
mail_archive_run_status mail_archive_status,
mail_archive_run_start TIMESTAMP NOT NULL,
mail_archive_run_end TIMESTAMP,
mail_archive_run_lower_boundary TIMESTAMP,
mail_archive_run_higher_boundary TIMESTAMP,
CONSTRAINT mail_archive_run_uuid_ukey UNIQUE (mail_archive_run_uuid)
);

------------------------------------------------------------------------
-- Write that the 3.0->3.1 is completed
UPDATE ObmInfo SET obminfo_value='3.1.0' WHERE obminfo_name='db_version';

COMMIT;
