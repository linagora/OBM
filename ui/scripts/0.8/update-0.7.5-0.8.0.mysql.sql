-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM MySQL Database from 0.7.5 to 0.8.0                          //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Update User preferences
-------------------------------------------------------------------------------
-- Update preference : todo_order
UPDATE UserObmPref set userobmpref_option='set_todo', userobmpref_value='priority' where userobmpref_option='todo_order';


-------------------------------------------------------------------------------
-- Update Calendar tables
-------------------------------------------------------------------------------
-- Change column : calendarsegment_date from varchar to timestamp
 
ALTER TABLE CalendarSegment CHANGE calendarsegment_date calendarsegment_date VARCHAR( 14 ) NOT NULL ;
UPDATE CalendarSegment SET calendarsegment_date =CONCAT(calendarsegment_date,"00");
ALTER table CalendarSegment change column calendarsegment_date calendarsegment_date timestamp(14) NOT NULL;

ALTER TABLE CalendarEvent CHANGE calendarevent_endrepeat calendarevent_endrepeat VARCHAR( 14 ) NOT NULL; 
UPDATE CalendarEvent SET calendarevent_endrepeat = CONCAT(calendarevent_endrepeat,"00");
ALTER table CalendarEvent change column calendarevent_endrepeat calendarevent_endrepeat timestamp(14);

ALTER table CalendarEvent change column calendarevent_length calendarevent_length INT(14);
