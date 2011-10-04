package fr.aliacom.obm.common.domain;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import com.google.common.base.Function;
import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class DomainCache implements DomainService {

	private final ConcurrentMap<String, ObmDomain> domainCache;
	
	@Inject
	private DomainCache(DomainDao domainDao) {
		this.domainCache = configureObmDomainCache(domainDao);
	}

	private ConcurrentMap<String, ObmDomain> configureObmDomainCache(final DomainDao domainDao) {
		return new MapMaker().
		expireAfterWrite(1, TimeUnit.MINUTES).
		concurrencyLevel(1).
		makeComputingMap(new Function<String, ObmDomain>() {
			@Override
			public ObmDomain apply(String domainName) {
				return domainDao.findDomainByName(domainName);
			}
		});
	}
	
	@Override
	public ObmDomain findDomainByName(String domainName) {
		try {
			return domainCache.get(domainName);
		} catch (NullPointerException e) {
			return null;
		}
	}
}