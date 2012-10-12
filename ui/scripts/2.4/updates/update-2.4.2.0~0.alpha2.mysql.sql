BEGIN;

ALTER TABLE `EventTag` DROP CONSTRAINT `eventtag_user_id_userobm_id_fkey`;
ALTER TABLE `EventTag` ADD CONSTRAINT `eventtag_user_id_userobm_id_fkey` FOREIGN KEY (`eventtag_user_id`) REFERENCES `UserObm` (`userobm_id`) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE Resource ADD COLUMN resource_email TEXT;

UPDATE Resource r SET resource_email=CONCAT('res-', r.resource_id, '@', (SELECT d.domain_name FROM Domain d WHERE d.domain_id=r.resource_domain_id));

-- Not set to NOT NULL on purpose (otherwise we have problems during insert)

ALTER TABLE Resource ADD UNIQUE INDEX (resource_email(100));

COMMIT;
