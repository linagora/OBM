-- ////////////////////////////////////////////////////////////////////////////
-- // Update OBM Database from 0.6.4 to 0.6.5	                             //
-- ////////////////////////////////////////////////////////////////////////////
-- // $Id$ //
-- ////////////////////////////////////////////////////////////////////////////

-------------------------------------------------------------------------------
-- Calendar module tables
-------------------------------------------------------------------------------
-- Old deprecated EventCategory table hasn't been removed in upgrade scripts
DROP TABLE IF EXISTS EventCategory;

-------------------------------------------------------------------------------
-- Calendar : create forgotten table
-------------------------------------------------------------------------------
CREATE TABLE CalendarRight (
  calendarright_ownerid int(8) NOT NULL default '0',
  calendarright_customerid int(8) NOT NULL default '0',
  calendarright_write int(1) NOT NULL default '0',
  calendarright_read int(1) NOT NULL default '0',
  PRIMARY KEY  (calendarright_ownerid,calendarright_customerid)
);
