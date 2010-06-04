UPDATE ObmInfo SET obminfo_value = '2.3.8-pre' WHERE obminfo_name = 'db_version';

CREATE TABLE _userpattern (
  id integer, 
  pattern varchar(255)
);

CREATE INDEX _userpattern_pattern_idx ON _userpattern (pattern);

CREATE INDEX _userpattern_id_fkey ON _userpattern (id);

ALTER TABLE ONLY _userpattern
    ADD CONSTRAINT _userpattern_id_userobm_id_fkey FOREIGN KEY (id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;


UPDATE ObmInfo SET obminfo_value = '2.3.8' WHERE obminfo_name = 'db_version';
