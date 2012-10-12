--
-- Adds a primary key to the DeletedEvent table
--
 
BEGIN;

ALTER TABLE DeletedEvent ADD PRIMARY KEY (deletedevent_id);

ALTER TABLE eventtag DROP CONSTRAINT eventtag_user_id_userobm_id_fkey;
ALTER TABLE eventtag ADD CONSTRAINT eventtag_user_id_userobm_id_fkey FOREIGN KEY (eventtag_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE resource ADD COLUMN resource_email TEXT;

UPDATE resource r SET resource_email='res-' || r.resource_id || '@' || (SELECT d.domain_name FROM domain d WHERE d.domain_id=r.resource_domain_id);

-- Not set to NOT NULL on purpose (otherwise we have problems during insert)

ALTER TABLE ONLY resource ADD CONSTRAINT resource_resource_email_key UNIQUE(resource_email);

COMMIT;
