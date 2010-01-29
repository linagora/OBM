-- Write that the 2.3->2.4 has started
UPDATE ObmInfo SET obminfo_value='2.3.x->2.4.0' WHERE obminfo_name='db_version';
-- -----------------------------------------------------------------------------


-- FIXME: put upgrades here
--
-- Table structure for `calendarcolor`
--
DROP TABLE IF EXISTS calendarcolor;
CREATE TABLE calendarcolor (
  user_id INTEGER NOT NULL,
  entity_id INTEGER NOT NULL,
  eventowner INTEGER DEFAULT NULL
);

ALTER TABLE calendarcolor 
  ADD CONSTRAINT pkey PRIMARY KEY (user_id,entity_id);

CREATE INDEX user_id_fkey ON calendarcolor(user_id);
CREATE INDEX entity_id_fkey ON calendarcolor(entity_id);

ALTER TABLE calendarcolor 
  ADD CONSTRAINT user_id_user_id_fkey FOREIGN KEY (user_id)
  REFERENCES userobm(userobm_id) ON UPDATE CASCADE ON DELETE CASCADE;

ALTER TABLE calendarcolor 
  ADD CONSTRAINT entity_id_entity_id_fkey FOREIGN KEY (entity_id)
  REFERENCES entity(entity_id) ON UPDATE CASCADE ON DELETE CASCADE;

--
-- Update Resource
--
ALTER TABLE resource
ALTER resource_delegation TYPE character varying(256);

--
-- Update deleted
--
ALTER TABLE deleted
ALTER deleted_delegation TYPE character varying(256);

--
-- Update host
--
ALTER TABLE host
ALTER host_delegation TYPE character varying(256);

--
-- Update mailshare
--
ALTER TABLE mailshare
ALTER mailshare_delegation TYPE character varying(256);

--
-- Update ugroup
--
ALTER TABLE ugroup
ALTER group_delegation TYPE character varying(256);

--
-- Update updated
--
ALTER TABLE updated
ALTER updated_delegation TYPE character varying(256);

--
-- Update updatedlinks
--
ALTER TABLE updatedlinks
ALTER updatedlinks_delegation TYPE character varying(256);

--
-- Update userobm
--
ALTER TABLE userobm 
ALTER userobm_delegation TYPE character varying(256),
ALTER userobm_delegation_target TYPE character varying(256);


--
-- Update obmbookmarkproperty
--
ALTER TABLE obmbookmarkproperty
ALTER obmbookmarkproperty_value TYPE character varying(256);


-- -----------------------------------------------------------------------------
-- Write that the 2.3->2.4 is completed
UPDATE ObmInfo SET obminfo_value='2.4.0' WHERE obminfo_name='db_version';


