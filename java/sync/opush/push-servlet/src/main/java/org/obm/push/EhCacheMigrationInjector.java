package org.obm.push;

import org.obm.push.store.ehcache.EhCacheConfiguration;
import org.obm.push.store.ehcache.EhCacheConfigurationMigrationImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.util.Modules;

public class EhCacheMigrationInjector {

	public static Injector createMigrationInjector() {
		return Guice.createInjector(new AbstractModule() {

			@Override
			protected void configure() {
				install(Modules.override(new OpushModule()).with( 
						new AbstractModule() {
							
							@Override
							protected void configure() {
								bind(EhCacheConfiguration.class).to(EhCacheConfigurationMigrationImpl.class);
							}
 						}));
			}
		});
	}
}
