BEGIN;

ALTER TABLE `EventTag` DROP CONSTRAINT `eventtag_user_id_userobm_id_fkey`;
ALTER TABLE `EventTag` ADD CONSTRAINT `eventtag_user_id_userobm_id_fkey` FOREIGN KEY (`eventtag_user_id`) REFERENCES `UserObm` (`userobm_id`) ON UPDATE CASCADE ON DELETE CASCADE;

COMMIT;
