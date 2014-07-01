-- -----------------------------------------------------------------------------
-- IMAP Archive is not supported
------------------------------------------------------------------------
UPDATE ObmInfo SET obminfo_value='3.1.0' WHERE obminfo_name='db_version';
