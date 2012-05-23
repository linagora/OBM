package org.obm.sync;

import java.io.InputStream;
import java.util.Properties;

import junit.framework.TestCase;

import org.obm.sync.book.Contact;
import org.obm.sync.calendar.Event;

/**
 * Base class for OBM sync test cases. Provides access to test.properties values
 * to customize tests execution.
 * 
 * @author tom
 * 
 */
public abstract class ObmSyncTestCase extends TestCase {

	private Properties props;

	protected void setUp() throws Exception {
		super.setUp();
		InputStream in = ObmSyncTestCase.class.getClassLoader()
				.getResourceAsStream("test.properties");
		if (in == null) {
			fail("Cannot load test.properties");
		}
		props = new Properties();
		props.load(in);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
	}

	protected String p(String key) {
		return props.getProperty(key);
	}

	protected abstract Contact getTestContact();

	protected abstract Event getTestEvent();

}
