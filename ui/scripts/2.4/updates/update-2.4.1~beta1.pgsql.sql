	 
--
-- Remove duplicates
--
 
BEGIN;

	DELETE FROM DeletedEvent WHERE deletedevent_id NOT IN (SELECT MAX(deletedevent_id) FROM DeletedEvent GROUP BY deletedevent_event_id, deletedevent_user_id);

	CREATE TABLE Tmp_DeletedEvent AS SELECT DISTINCT * FROM DeletedEvent;
	DROP TABLE DeletedEvent;
	ALTER TABLE Tmp_DeletedEvent RENAME TO DeletedEvent;


	ALTER TABLE DeletedEvent ADD CONSTRAINT deletedevent_uniquekey UNIQUE (deletedevent_event_id, deletedevent_user_id);
	ALTER TABLE DeletedEvent ADD PRIMARY KEY (deletedevent_id);

COMMIT;
