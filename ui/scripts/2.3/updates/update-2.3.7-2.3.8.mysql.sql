UPDATE ObmInfo SET obminfo_value = '2.3.8-pre' WHERE obminfo_name = 'db_version';

CREATE TABLE _userpattern (
  id integer NOT NULL, 
  pattern varchar(255),
  KEY (`pattern`),
  CONSTRAINT `_userpattern_id_fkey` FOREIGN KEY (`id`) REFERENCES `UserObm` (`userobm_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

UPDATE ObmInfo SET obminfo_value = '2.3.8' WHERE obminfo_name = 'db_version';
