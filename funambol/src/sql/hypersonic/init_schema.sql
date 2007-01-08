--
-- Initialization data for the Funambol-OBM module
--

--
-- Module structure registration
--

delete from fnbl_sync_source_type where id='obm-contact';
insert into fnbl_sync_source_type(id, description, class, admin_class)
values('obm-contact','OBM Contact SyncSource','fr.aliasource.funambol.engine.source.ContactSyncSource','fr.aliasource.funambol.admin.ObmSyncSourceConfigPanel');

delete from fnbl_sync_source_type where id='obm-calendar';
insert into fnbl_sync_source_type(id, description, class, admin_class)
values('obm-calendar','OBM Calendar SyncSource','fr.aliasource.funambol.engine.source.CalendarSyncSource','fr.aliasource.funambol.admin.ObmSyncSourceConfigPanel');

delete from fnbl_module where id='obm';
insert into fnbl_module (id, name, description)
values('obm','obm','Funambol OBM Connector');

delete from fnbl_connector where id='obm';
insert into fnbl_connector(id, name, description, admin_class)
values('obm','Funambol OBM Connector','Funambol OBM Connector','');

delete from fnbl_connector_source_type where connector='obm' and sourcetype='obm-contact';
insert into fnbl_connector_source_type(connector, sourcetype)
values('obm','obm-contact');

delete from fnbl_connector_source_type where connector='obm' and sourcetype='obm-calendar';
insert into fnbl_connector_source_type(connector, sourcetype)
values('obm','obm-calendar');

delete from fnbl_module_connector where module='obm' and connector='obm';
insert into fnbl_module_connector(module, connector)
values('obm','obm');