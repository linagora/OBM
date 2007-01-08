--
-- Initialization data for the Dummy module
-- @version $Id: init_schema.sql,v 1.1 2006/01/18 16:04:56 luigiafassina Exp $
--

--
-- Module structure registration
--

delete from fnbl_sync_source_type where id='dummy';
insert into fnbl_sync_source_type(id, description, class, admin_class)
values('dummy','Dummy SyncSource','com.funambol.examples.engine.source.DummySyncSource','com.funambol.examples.admin.DummySyncSourceConfigPanel');

delete from fnbl_module where id='dummy';
insert into fnbl_module (id, name, description)
values('dummy','dummy','Dummy');

delete from fnbl_connector where id='dummy';
insert into fnbl_connector(id, name, description, admin_class)
values('dummy','FunambolDummyConnector','Funambol Dummy Connector','');

delete from fnbl_connector_source_type where connector='dummy' and sourcetype='dummy';
insert into fnbl_connector_source_type(connector, sourcetype)
values('dummy','dummy');

delete from fnbl_module_connector where module='dummy' and connector='dummy';
insert into fnbl_module_connector(module, connector)
values('dummy','dummy');