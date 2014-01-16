/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
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
