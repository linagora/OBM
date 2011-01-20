package org.obm.sync;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.google.common.collect.MapEvictionListener;
import com.google.common.collect.MapMaker;

public class TimeOutMapTest {

	@Test
	public void testTimeOutMap() throws Exception {
		System.out.println("Test started !");
		ConcurrentMap<String, Object> t = new MapMaker().
			expiration(5, TimeUnit.SECONDS).
			evictionListener(new MapEvictionListener<String, Object>() {
				@Override
				public void onEviction(String key, Object value) {
					System.out.println("key " + key + " evicted");
				}
			}).makeMap();
		t.put("a", new Object());
		assertNotNull(t.get("a"));
		for (int i = 0; i < 4; i++) {
			Thread.sleep(1000);
			assertNotNull(t.get("a"));
			System.out.println((i + 1) + " second elapsed");
		}
		Thread.sleep(1000);
		assertNull(t.get("a"));
	}

}
