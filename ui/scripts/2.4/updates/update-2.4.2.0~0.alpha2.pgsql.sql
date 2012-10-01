--
-- Adds a primary key to the DeletedEvent table
--
 
BEGIN;

ALTER TABLE DeletedEvent ADD PRIMARY KEY (deletedevent_id);

ALTER TABLE eventtag DROP CONSTRAINT eventtag_user_id_userobm_id_fkey;
ALTER TABLE eventtag ADD CONSTRAINT eventtag_user_id_userobm_id_fkey FOREIGN KEY (eventtag_user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

COMMIT;
