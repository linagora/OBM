BEGIN;

CREATE TYPE batch_status AS ENUM ('IDLE', 'RUNNING', 'ERROR', 'SUCCESS');
CREATE TYPE batch_entity_type AS ENUM ('GROUP', 'USER', 'GROUP_MEMBERSHIP', 'USER_MEMBERSHIP');
CREATE TYPE http_verb AS ENUM ('PUT', 'PATCH', 'GET', 'POST', 'DELETE');

CREATE TABLE batch
(
  id serial NOT NULL,
  status batch_status NOT NULL,
  timecreate timestamp without time zone NOT NULL DEFAULT NOW(),
  timecommit timestamp without time zone,
  domain integer NOT NULL,
  CONSTRAINT batch_pkey PRIMARY KEY (id),
  CONSTRAINT batch_batch_domain_id_fkey FOREIGN KEY (domain)
      REFERENCES domain (domain_id)
      ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE batch_operation
(
  id serial NOT NULL,
  status batch_status NOT NULL,
  timecreate timestamp without time zone NOT NULL DEFAULT NOW(),
  timecommit timestamp without time zone,
  error text,
  resource_path text NOT NULL,
  body text,
  verb http_verb NOT NULL,
  entity_type batch_entity_type NOT NULL,
  batch integer NOT NULL,
  CONSTRAINT operation_pkey PRIMARY KEY (id),
  CONSTRAINT batch_operation_batch_fkey FOREIGN KEY (batch)
      REFERENCES batch (id)
      ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE TABLE batch_operation_param
(
  id serial NOT NULL,
  param_key text NOT NULL,
  value text NOT NULL,
  operation integer NOT NULL,
  CONSTRAINT batch_operation_param_pkey PRIMARY KEY (id),
  CONSTRAINT batch_operation_param_operation_fkey FOREIGN KEY (operation)
      REFERENCES batch_operation (id)
      ON UPDATE CASCADE ON DELETE CASCADE
);

CREATE INDEX batch_operation_param_operation_idx ON batch_operation_param(operation);
CREATE INDEX batch_operation_batch_idx ON batch_operation(batch);

ALTER TABLE ONLY userobm ALTER userobm_ext_id TYPE CHARACTER(36);

UPDATE ONLY userobm
SET userobm_ext_id = UUID()
WHERE userobm_ext_id IS NULL;

ALTER TABLE ONLY userobm ALTER userobm_ext_id SET NOT NULL;
CREATE UNIQUE INDEX userobm_ext_id_unique_idx ON userobm (userobm_domain_id, userobm_ext_id);

ALTER TABLE ONLY p_userobm ALTER userobm_ext_id TYPE CHARACTER(36);

UPDATE ONLY p_userobm pu
SET userobm_ext_id = u.userobm_ext_id
FROM userobm u
WHERE pu.userobm_id = u.userobm_id;

ALTER TABLE ONLY p_userobm ALTER userobm_ext_id SET NOT NULL;
CREATE UNIQUE INDEX p_userobm_ext_id_unique_idx ON p_userobm (userobm_domain_id, userobm_ext_id);

ALTER TABLE ONLY ugroup ALTER group_ext_id TYPE CHARACTER(36);

UPDATE ONLY ugroup
SET group_ext_id = UUID()
WHERE group_ext_id IS NULL;

ALTER TABLE ONLY ugroup ALTER group_ext_id SET NOT NULL;
CREATE UNIQUE INDEX group_ext_id_unique_idx ON ugroup (group_domain_id, group_ext_id);

ALTER TABLE ONLY p_ugroup ALTER group_ext_id TYPE CHARACTER(36);

UPDATE ONLY p_ugroup pug
SET group_ext_id = ug.group_ext_id
FROM ugroup ug
WHERE pug.group_id = ug.group_id;

ALTER TABLE ONLY p_ugroup ALTER group_ext_id SET NOT NULL;
CREATE UNIQUE INDEX p_ugroup_ext_id_unique_idx ON p_ugroup (group_domain_id, group_ext_id);

UPDATE profileproperty SET profileproperty_value = 'domain' WHERE profileproperty_value = 'admin';

COMMIT;
