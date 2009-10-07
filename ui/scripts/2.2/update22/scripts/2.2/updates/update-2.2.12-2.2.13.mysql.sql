-- /////////////////////////////////////////////////////////////////////////////
-- OBM - File : update-2.2.11-2.2.12.mysql.sql
-- 2009-09-22 Mehdi Rande
-- /////////////////////////////////////////////////////////////////////////////
-- $Id: $
-- /////////////////////////////////////////////////////////////////////////////


UPDATE ObmInfo SET obminfo_value = '2.2.13-pre' WHERE obminfo_name = 'db_version';

ALTER TABLE Event
  ADD KEY `event_parent_id_event_id_fkey` (`event_parent_id`);  
  
ALTER TABLE Event
  ADD CONSTRAINT `event_parent_id_event_id_fkey` FOREIGN KEY (`event_parent_id`) REFERENCES `Event` (`event_id`) ON DELETE CASCADE ON UPDATE CASCADE;

CREATE INDEX entityright_admin_key ON EntityRight (entityright_admin) ; 
CREATE INDEX entityright_read_key ON EntityRight (entityright_read) ; 
CREATE INDEX entityright_access_key ON EntityRight (entityright_access) ; 
CREATE INDEX entityright_write_key ON EntityRight (entityright_write) ;

UPDATE ObmInfo SET obminfo_value = '2.2.13' WHERE obminfo_name = 'db_version';
