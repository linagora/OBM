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
);


-------------------------------------------------------------------------------
-- Update deal hitrate values according to state
-------------------------------------------------------------------------------
-- Set hitrate = 100 where status = SIGNED,DONE,INVOICE,PAYED
UPDATE Deal set deal_hitrate = '100' where deal_status_id = '1' or deal_status_id = '7' or deal_status_id = '8' or deal_status_id = '9';