package org.obm.locator.store;

import java.io.Serializable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.obm.configuration.ObmConfigurationService;
import org.obm.locator.LocatorClientException;
import org.obm.locator.LocatorClientImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.google.inject.Singleton;


@Singleton
public class LocatorCache implements LocatorService {

	private static final Logger logger = LoggerFactory.getLogger(LocatorClientImpl.class);
	private static final String DEFAULT_VALUE = new String();
	
	private final Map<Key, String> store;
	private final LocatorClientImpl locatorClientImpl;

	@Inject
	/* package */ LocatorCache(ObmConfigurationService obmConfigurationService, LocatorClientImpl locatorClientImpl) {
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
	            String value = getServiceLocation(key);
	        	if (value != null) {
	        		return value;
	        	}
	        	return DEFAULT_VALUE;
	        }
	    });
	}

	private String getServiceLocation(Key key) {
    	try {
			return locatorClientImpl.getServiceLocation(key.getServiceSlashProperty(), key.getLoginAtDomain());
    	} catch (LocatorClientException e) {
			logger.error(e.getMessage());
		}
		return null;
	}
	
	@Override
	public String getServiceLocation(String serviceSlashProperty, String loginAtDomain) throws LocatorClientException {
		Key key = new Key(serviceSlashProperty, loginAtDomain);
		String value = store.get(key);
		if (value == DEFAULT_VALUE) {
			throw new LocatorClientException("No host for { " + key.toString() + " }");
		}
		return value;
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
		public int hashCode(){
			return Objects.hashCode(serviceSlashProperty, loginAtDomain);
		}
		
		@Override
		public boolean equals(Object object){
			if (object instanceof Key) {
				Key that = (Key) object;
				return Objects.equal(this.serviceSlashProperty, that.serviceSlashProperty)
					&& Objects.equal(this.loginAtDomain, that.loginAtDomain);
			}
			return false;
		}

		@Override
		public String toString() {
			return Objects.toStringHelper(this)
				.add("serviceSlashProperty", serviceSlashProperty)
				.add("loginAtDomain", loginAtDomain)
				.toString();
		}
		
	}

}
