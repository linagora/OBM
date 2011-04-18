UPDATE ObmInfo SET obminfo_value = '2.3.17-pre' WHERE obminfo_name = 'db_version';

CREATE TABLE deletedsyncedaddressbook (
  user_id        integer NOT NULL,
  addressbook_id integer NOT NULL,
  timestamp      timestamp without time zone NOT NULL DEFAULT now()
);

ALTER TABLE ONLY deletedsyncedaddressbook
  ADD CONSTRAINT deletedsyncedaddressbook_pkey PRIMARY KEY (user_id, addressbook_id);

ALTER TABLE ONLY deletedsyncedaddressbook
    ADD CONSTRAINT deletedsyncedaddressbook_user_id_userobm_id_fkey FOREIGN KEY (user_id) REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE ONLY deletedsyncedaddressbook
    ADD CONSTRAINT deletedsyncedaddressbook_addressbook_id_addressbook_id_fkey FOREIGN KEY (addressbook_id) REFERENCES addressbook(id) ON UPDATE CASCADE ON DELETE CASCADE;

UPDATE ObmInfo SET obminfo_value = '2.3.17' WHERE obminfo_name = 'db_version';
