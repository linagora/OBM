-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM Database from 0.6.2 to 0.6.3	                             //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$ //
-- ////////////////////////////////////////////////////////////////////////////

-------------------------------------------------------------------------------
-- Calendar Update (Right)
-------------------------------------------------------------------------------

CREATE TABLE CalendarRight (
  calendarright_ownerid int(8) NOT NULL default '0',
  calendarright_customerid int(8) NOT NULL default '0',
  calendarright_write int(1) NOT NULL default '0',
  calendarright_read int(1) NOT NULL default '0',
  PRIMARY KEY  (calendarright_ownerid,calendarright_customerid)
)
