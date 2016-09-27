BEGIN;

-- Add EVENT, CONTACT to the batch_entity_type ENUM
ALTER TYPE batch_entity_type rename to batch_entity_type_old;
CREATE TYPE batch_entity_type AS ENUM ('GROUP', 'USER', 'GROUP_MEMBERSHIP', 'USER_MEMBERSHIP', 'EVENT', 'CONTACT');
ALTER TABLE batch_operation ALTER COLUMN entity_type TYPE batch_entity_type USING entity_type::text::batch_entity_type;
DROP TYPE batch_entity_type_old ;

ALTER TABLE commitedoperation ADD COLUMN commitedoperation_client_date timestamp without time zone;

CREATE TABLE addressbookreference (
    addressbook_id integer NOT NULL,
    reference character varying(255) NOT NULL,
    origin character varying(255) NOT NULL
);

ALTER TABLE addressbookreference
    ADD CONSTRAINT addressbookreference_addressbook_id_fkey FOREIGN KEY (addressbook_id) REFERENCES addressbook(id) ON DELETE CASCADE;

ALTER TABLE addressbookreference
    ADD CONSTRAINT addressbookreference_uniquekey UNIQUE (reference, origin);


UPDATE ObmInfo SET obminfo_value = '3.2.1' WHERE obminfo_name = 'db_version';

COMMIT;
