package org.obm.locator.store;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.obm.configuration.ObmConfigurationService;
import org.obm.locator.LocatorClientImpl;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.google.inject.Singleton;


@Singleton
public class LocatorCache implements LocatorService {

	private final Map<Key, String> store;
	private final LocatorClientImpl locatorClientImpl;

	@Inject
	private LocatorCache(ObmConfigurationService obmConfigurationService, LocatorClientImpl locatorClientImpl) {
		this.locatorClientImpl = locatorClientImpl;
		this.store = createStore(obmConfigurationService.getLocatorCacheTimeout(), 
							   obmConfigurationService.getLocatorCacheTimeUnit()); 
	}

	private Map<Key, String> createStore(int duration, TimeUnit unit) {
		return new MapMaker()
	    .expireAfterWrite(duration, unit)
	    .makeComputingMap(new Function<Key, String>() {
	        @Override
	        public String apply(Key key) {
	            return locatorClientImpl.
	            		getServiceLocation(key.getServiceSlashProperty(), key.getLoginAtDomain());
	        }
	    });
	}

	@Override
	public String getServiceLocation(String serviceSlashProperty, String loginAtDomain) {
		return store.get(new Key(serviceSlashProperty, loginAtDomain));
	}
	
	private class Key implements Serializable {
		private final String serviceSlashProperty;
		private final String loginAtDomain;
		
		public Key(String serviceSlashProperty, String loginAtDomain) {
			this.serviceSlashProperty = serviceSlashProperty;
			this.loginAtDomain = loginAtDomain;
		}
		
		public String getLoginAtDomain() {
			return loginAtDomain;
		}
		
		public String getServiceSlashProperty() {
			return serviceSlashProperty;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((loginAtDomain == null) ? 0 : loginAtDomain.hashCode());
			result = prime * result + ((serviceSlashProperty == null) ? 0 : serviceSlashProperty.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Key other = (Key) obj;
			if (loginAtDomain == null) {
				if (other.loginAtDomain != null)
					return false;
			} else if (!loginAtDomain.equals(other.loginAtDomain))
				return false;
			if (serviceSlashProperty == null) {
				if (other.serviceSlashProperty != null)
					return false;
			} else if (!serviceSlashProperty.equals(other.serviceSlashProperty))
				return false;
			return true;
		}
	}

}
