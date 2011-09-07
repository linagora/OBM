package org.obm.locator;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Function;
import com.google.common.collect.MapEvictionListener;
import com.google.common.collect.MapMaker;

public class TimeOutMapTest {

	private final static String APPLY_VALUE = "DEFAULT-VALUE";
	private Map<String, String> cache;

	@Before
	public void init() {
		cache = new MapMaker()
	    .expireAfterAccess(2, TimeUnit.SECONDS)
	    .makeComputingMap(new Function<String, String>() {
	        @Override
	        public String apply(String input) {
	            return APPLY_VALUE;
	        }
	    });
	}
	
	@After
	public void flush() {
		cache.clear();
	}
	
	@Test
	public void basicOperation() {
		String value = "ONE-VALUE";
		String key = "ONE-KEY";
		cache.put(key, value);
		Assert.assertEquals(value, cache.get(key) );
	}
	
	@Test
	public void returnApplyValue() {
		Assert.assertEquals(APPLY_VALUE, cache.get("KEY-NOT-EXIST") );
	}
	
	@Test
	public void returnApplyValueExpireAfterAccess() throws InterruptedException {
		String value = "ONE-VALUE";
		String key = "ONE-KEY";
		cache.put(key, value);
		
		Thread.sleep(5000);
		Assert.assertEquals(APPLY_VALUE, cache.get(key) );
	}
	
	@Test
	public void testTimeOutMap() throws Exception {
		ConcurrentMap<String, Object> t = new MapMaker().
			expireAfterWrite(5, TimeUnit.SECONDS).
			evictionListener(new MapEvictionListener<String, Object>() {
				@Override
				public void onEviction(String key, Object value) {
				}
			}).makeMap();
		t.put("a", new Object());
		assertNotNull(t.get("a"));
		for (int i = 0; i < 4; i++) {
			Thread.sleep(1000);
			assertNotNull(t.get("a"));
		}
		Thread.sleep(1000);
		assertNull(t.get("a"));
	}

}
