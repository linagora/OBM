UPDATE ObmInfo SET obminfo_value = '2.3.9-pre' WHERE obminfo_name = 'db_version';
CREATE INDEX eventexception_parent_id_event_id_fkey ON EventException (eventexception_parent_id);
CREATE INDEX eventexception_child_id_event_id_fkey ON EventException (eventexception_child_id);
UPDATE ObmInfo SET obminfo_value = '2.3.9' WHERE obminfo_name = 'db_version';

