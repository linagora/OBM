-- /////////////////////////////////////////////////////////////////////////////
-- OBM - File : update-2.3.0-2.3.1.pgsql.sql
-- 2009-12-16 Adrien Poupard
-- /////////////////////////////////////////////////////////////////////////////
-- $Id: $
-- /////////////////////////////////////////////////////////////////////////////

UPDATE ObmInfo SET obminfo_value = '2.3.1-pre' WHERE obminfo_name = 'db_version';

--
-- Table structure for table `opush_ping_heartbeat`
--
CREATE TABLE opush_ping_heartbeat (
       device_id        INTEGER NOT NULL REFERENCES opush_device(id) ON DELETE CASCADE,
       last_heartbeat   INTEGER NOT NULL
);
ALTER TABLE opush_ping_heartbeat ADD CONSTRAINT
unique_opush_ping_heartbeat_col_dev UNIQUE (device_id);

UPDATE ObmInfo SET obminfo_value = '2.3.1' WHERE obminfo_name = 'db_version';

