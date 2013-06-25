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
  `url` text NOT NULL,
  `body` text,
  `verb` ENUM('PUT', 'PATCH', 'GET', 'POST', 'DELETE') NOT NULL,
  `entity_type` ENUM('GROUP', 'USER') NOT NULL,
  `batch` int(8) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `batch_operation_batch_fkey` FOREIGN KEY (`batch`)
      REFERENCES batch (`id`)
      ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS batch_operation_param
(
  `id` int(8) NOT NULL AUTO_INCREMENT,
  `key` text NOT NULL,
  `value` text NOT NULL,
  `operation` int(8) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `batch_operation_param_operation_fkey` FOREIGN KEY (`operation`)
      REFERENCES batch_operation (`id`)
      ON UPDATE CASCADE ON DELETE CASCADE
);

COMMIT;
