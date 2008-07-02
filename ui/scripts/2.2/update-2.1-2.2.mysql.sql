--
-- Set integer to boolean when necessary
--

-- CalendarEvent
ALTER TABLE CalendarEvent MODIFY COLUMN calendarevent_allday BOOLEAN DEFAULT FALSE;

