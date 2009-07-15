-- Write that the 2.2->2.3 has started
UPDATE ObmInfo SET obminfo_value='2.2.x->2.3.0' WHERE obminfo_name='db_version';





-- Write that the 2.2->2.3 is completed
UPDATE ObmInfo SET obminfo_value='2.3.0' WHERE obminfo_name='db_version';

