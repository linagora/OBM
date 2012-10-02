--
-- Adds a primary key to the DeletedEvent table
--
 
BEGIN;

	ALTER TABLE DeletedEvent ADD PRIMARY KEY (deletedevent_id);

COMMIT;
