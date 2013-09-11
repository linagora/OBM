BEGIN;

CREATE TABLE IF NOT EXISTS `batch`
(
  `id` int(8) NOT NULL AUTO_INCREMENT,
  `status` ENUM('IDLE', 'RUNNING', 'ERROR', 'SUCCESS') NOT NULL,
  `timecreate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `timecommit` timestamp,
  `domain` int(8) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `batch_batch_domain_id_fkey` FOREIGN KEY (`domain`)
      REFERENCES Domain (`domain_id`)
      ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS `batch_operation`
(
  `id` int(8) NOT NULL AUTO_INCREMENT,
  `status` ENUM('IDLE', 'RUNNING', 'ERROR', 'SUCCESS') NOT NULL,
  `timecreate` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `timecommit` timestamp,
  `error` text,
  `resource_path` text NOT NULL,
  `body` text,
  `verb` ENUM('PUT', 'PATCH', 'GET', 'POST', 'DELETE') NOT NULL,
  `entity_type` ENUM('GROUP', 'USER', 'GROUP_MEMBERSHIP', 'USER_MEMBERSHIP') NOT NULL,
  `batch` int(8) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `batch_operation_batch_fkey` FOREIGN KEY (`batch`)
      REFERENCES batch (`id`)
      ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS batch_operation_param
(
  `id` int(8) NOT NULL AUTO_INCREMENT,
  `param_key` text NOT NULL,
  `value` text NOT NULL,
  `operation` int(8) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `batch_operation_param_operation_fkey` FOREIGN KEY (`operation`)
      REFERENCES batch_operation (`id`)
      ON UPDATE CASCADE ON DELETE CASCADE
);

UPDATE UserObm
SET userobm_ext_id = UUID()
WHERE userobm_ext_id IS NULL;

ALTER TABLE UserObm MODIFY userobm_ext_id CHARACTER(36) NOT NULL;
ALTER TABLE UserObm ADD UNIQUE (userobm_domain_id, userobm_ext_id);

UPDATE P_UserObm pu
INNER JOIN UserObm u
ON u.userobm_id = pu.userobm_id
SET pu.userobm_ext_id = u.userobm_ext_id;

ALTER TABLE P_UserObm MODIFY userobm_ext_id CHARACTER(36) NOT NULL;
ALTER TABLE P_UserObm ADD UNIQUE (userobm_domain_id, userobm_ext_id);

UPDATE UGroup
SET group_ext_id = UUID()
WHERE group_ext_id IS NULL;

ALTER TABLE UGroup MODIFY group_ext_id CHARACTER(36) NOT NULL;
ALTER TABLE UGroup ADD UNIQUE (group_domain_id, group_ext_id);

UPDATE P_UGroup pug
INNER JOIN UGroup ug
ON ug.group_id = pug.group_id
SET pug.group_ext_id = ug.group_ext_id;

ALTER TABLE P_UGroup MODIFY group_ext_id CHARACTER(36) NOT NULL;
ALTER TABLE P_UGroup ADD UNIQUE (group_domain_id, group_ext_id);

UPDATE ProfileProperty SET profileproperty_value = 'domain' WHERE profileproperty_value = 'admin';

COMMIT;
