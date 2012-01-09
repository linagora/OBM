package org.obm.push;

import org.obm.annotations.transactional.TransactionalModule;
import org.obm.configuration.ConfigurationService;
import org.obm.configuration.SyncPermsConfigurationService;
import org.obm.locator.store.LocatorCache;
import org.obm.locator.store.LocatorService;
import org.obm.push.backend.IBackend;
import org.obm.push.backend.IContentsExporter;
import org.obm.push.backend.IContentsImporter;
import org.obm.push.backend.IErrorsManager;
import org.obm.push.backend.IHierarchyExporter;
import org.obm.push.backend.OBMBackend;
import org.obm.push.service.DeviceService;
import org.obm.push.service.EventService;
import org.obm.push.service.OpushSyncPermsConfigurationService;
import org.obm.push.service.PushPublishAndSubscribe;
import org.obm.push.service.impl.DeviceServiceImpl;
import org.obm.push.service.impl.EventServiceImpl;
import org.obm.push.service.impl.MappingService;
import org.obm.push.service.impl.MappingServiceImpl;
import org.obm.push.service.impl.PushPublishAndSubscribeImpl;
import org.obm.push.store.DaoModule;
import org.obm.push.store.ItemTrackingDao;
import org.obm.push.store.MonitoredCollectionDao;
import org.obm.push.store.SyncedCollectionDao;
import org.obm.push.store.UnsynchronizedItemDao;
import org.obm.push.store.ehcache.MonitoredCollectionDaoEhcacheImpl;
import org.obm.push.store.ehcache.SyncedCollectionDaoEhcacheImpl;
import org.obm.push.store.ehcache.UnsynchronizedItemDaoEhcacheImpl;
import org.obm.push.store.jdbc.ItemTrackingDaoJdbcImpl;
import org.obm.sync.ObmSyncHttpClientModule;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;


public class OpushImplModule extends AbstractModule {

	@Override
	protected void configure() {
		install(new TransactionalModule());
		install(new DaoModule());
		install(new OpushServletModule());
		install(new ObmSyncHttpClientModule());
		bind(IHierarchyExporter.class).to(HierarchyExporter.class);
		bind(IContentsExporter.class).to(ContentsExporter.class);
		bind(ConfigurationService.class).to(OpushConfigurationService.class);
		bind(IInvitationFilterManager.class).to(DummyInvitationFilterManager.class);	
		bind(IBackend.class).to(OBMBackend.class);
		bind(IContentsImporter.class).to(ContentsImporter.class);
		bind(IErrorsManager.class).to(ErrorsManager.class);
		bind(UnsynchronizedItemDao.class).to(UnsynchronizedItemDaoEhcacheImpl.class);
		bind(MonitoredCollectionDao.class).to(MonitoredCollectionDaoEhcacheImpl.class);
		bind(SyncedCollectionDao.class).to(SyncedCollectionDaoEhcacheImpl.class);
		bind(DeviceService.class).to(DeviceServiceImpl.class);
		bind(SyncPermsConfigurationService.class).to(OpushSyncPermsConfigurationService.class);
		bind(ItemTrackingDao.class).to(ItemTrackingDaoJdbcImpl.class);
		bind(LocatorService.class).to(LocatorCache.class);
		bind(EventService.class).to(EventServiceImpl.class);
		bind(PushPublishAndSubscribe.Factory.class).to(PushPublishAndSubscribeImpl.Factory.class);
		bind(MappingService.class).to(MappingServiceImpl.class);
		bind(String.class).annotatedWith(Names.named("origin")).toInstance("o-push");
	}
}
