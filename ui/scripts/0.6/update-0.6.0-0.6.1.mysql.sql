-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM Database from 0.6.0 to 0.6.1	                             //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$ //
-- ////////////////////////////////////////////////////////////////////////////


-------------------------------------------------------------------------------
-- Contact Update
-------------------------------------------------------------------------------
-- Add new column : contact archive
ALTER table Contact add column contact_archive char(1) not null default 0 AFTER contact_email;

-- move text columns at the row end
ALTER table Contact add column tmp text;
UPDATE Contact set tmp=contact_comment;
ALTER table Contact drop column contact_comment;
ALTER table Contact change tmp contact_comment text;
ALTER TABLE `CalendarEvent` CHANGE `calendarevent_endrepeat` `calendarevent_endrepeat` VARCHAR( 12 ) DEFAULT NULL; 
