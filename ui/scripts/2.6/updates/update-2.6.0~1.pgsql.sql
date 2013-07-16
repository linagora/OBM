BEGIN;

CREATE TYPE batch_status AS ENUM ('IDLE', 'RUNNING', 'ERROR', 'SUCCESS');
CREATE TYPE batch_entity_type AS ENUM ('GROUP', 'USER');
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
  key text NOT NULL,
  value text NOT NULL,
  operation integer NOT NULL,
  CONSTRAINT batch_operation_param_pkey PRIMARY KEY (id),
  CONSTRAINT batch_operation_param_operation_fkey FOREIGN KEY (operation)
      REFERENCES batch_operation (id)
      ON UPDATE CASCADE ON DELETE CASCADE
);

COMMIT;
