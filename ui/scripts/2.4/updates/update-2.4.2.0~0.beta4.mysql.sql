--
-- Adds a new preference 'set_allday_opacity'
--
-- Add Table: deletedeventlink : In order to save deleted users from an event 
--

BEGIN;

INSERT INTO UserObmPref (userobmpref_option, userobmpref_value)
SELECT 'set_allday_opacity', 'TRANSPARENT' FROM Dual
WHERE NOT EXISTS (SELECT 1 FROM UserObmPref WHERE userobmpref_option='set_allday_opacity');

-- TABLE deletedeventlink;

CREATE  TABLE `DeletedEventLink` (
  `deletedeventlink_id` INT NOT NULL AUTO_INCREMENT ,
  `deletedeventlink_userobm_id` INT NOT NULL ,
  `deletedeventlink_event_id` INT NOT NULL ,
  `deletedeventlink_event_ext_id` VARCHAR(300) NOT NULL ,
  `deletedeventlink_time_removed` TIMESTAMP NOT NULL DEFAULT now() ,
  PRIMARY KEY (`deletedeventlink_id`) ,
  INDEX `deletedeventlink_userobm_id_userobm_id_fkey` (`deletedeventlink_userobm_id` ASC) ,
  INDEX `deletedeventlink_event_id_event_id_fkey` (`deletedeventlink_event_id` ASC) ,
  CONSTRAINT `deletedeventlink_userobm_id_userobm_id_fkey`
    FOREIGN KEY (`deletedeventlink_userobm_id` )
    REFERENCES `UserObm` (`userobm_id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT `deletedeventlink_event_id_event_id_fkey`
    FOREIGN KEY (`deletedeventlink_event_id` )
    REFERENCES `Event` (`event_id` )
    ON DELETE CASCADE
    ON UPDATE CASCADE
);

--
-- Those tables drops are done because emails sync states are now managed out of the SQL database
--
DROP TABLE opush_sync_deleted_mail, opush_sync_mail;


COMMIT;
