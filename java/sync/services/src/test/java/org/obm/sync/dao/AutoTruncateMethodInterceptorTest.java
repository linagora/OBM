/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync.dao;

import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.fest.assertions.api.Assertions.assertThat;

import org.aopalliance.intercept.MethodInterceptor;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.annotations.database.AutoTruncate;
import org.obm.annotations.database.DatabaseEntity;
import org.obm.annotations.database.DatabaseField;
import org.obm.test.GuiceModule;
import org.obm.test.SlowGuiceRunner;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.matcher.Matchers;

import fr.aliacom.obm.services.constant.ObmSyncConfigurationService;

@RunWith(SlowGuiceRunner.class)
@GuiceModule(AutoTruncateMethodInterceptorTest.Env.class)
public class AutoTruncateMethodInterceptorTest {

	private static final String LONG_STRING = "This is a loooooooooooooooooooong String";
	private static final String TABLE = "Test";

	public static class Env extends AbstractModule {

		private IMocksControl control = createControl();

		@Override
		protected void configure() {
			bind(IMocksControl.class).toInstance(control);
			bindWithMock(DatabaseMetadataService.class);
			bind(DatabaseTruncationService.class).to(DatabaseTruncationServiceImpl.class);
			bindWithMock(ObmSyncConfigurationService.class);

			MethodInterceptor interceptor = new AutoTruncateMethodInterceptor();

			requestInjection(interceptor);
			bindInterceptor(Matchers.annotatedWith(AutoTruncate.class), Matchers.annotatedWith(AutoTruncate.class), interceptor);
		}

		private <T> void bindWithMock(Class<T> cls) {
			bind(cls).toInstance(control.createMock(cls));
		}

	}

	@Inject
	private DatabaseMetadataService metadataService;
	@Inject
	private IMocksControl control;
	@Inject
	private AutoTruncateTestService service;
	@Inject
	private AutoTruncateTestServiceNoAnnotation serviceWithoutTruncation;
	@Inject
	private ObmSyncConfigurationService configuration;

	private TableDescription tableDescription;

	@Before
	public void setUp() {
		tableDescription = control.createMock(TableDescription.class);
	}

	@After
	public void tearDown() {
		control.verify();
	}

	@Test
	public void testUnannotatedMethodOnAnnotatedClass() {
		control.replay();

		assertThat(service.getTestObjectStringNoAnnotation(new TestObject())).isEqualTo(LONG_STRING);
	}

	@Test
	public void testUnannotatedMethodOnUnannotatedClass() {
		control.replay();

		assertThat(serviceWithoutTruncation.getTestObjectStringNoAnnotation(new TestObject())).isEqualTo(LONG_STRING);
	}

	@Test
	public void testAnnotatedMethodOnUnannotatedClass() {
		control.replay();

		assertThat(serviceWithoutTruncation.getTestObjectString(new TestObject())).isEqualTo(LONG_STRING);
	}

	@Test
	public void testMethodAnnotatedWithDatabaseEntityOnAnnotatedClass() throws Exception {
		expect(metadataService.getTableDescriptionOf(TABLE)).andReturn(tableDescription);
		expect(tableDescription.getMaxAllowedBytesOf("LongString")).andReturn(10);
		expect(configuration.isAutoTruncateEnabled()).andReturn(true);
		control.replay();

		assertThat(service.getTestObjectString(new TestObject(LONG_STRING))).isEqualTo(LONG_STRING.substring(0, 10));
	}

	@Test
	public void testMethodAnnotatedWithDatabaseFieldOnAnnotatedClass() throws Exception {
		expect(metadataService.getTableDescriptionOf(TABLE)).andReturn(tableDescription);
		expect(tableDescription.getMaxAllowedBytesOf("Str")).andReturn(10);
		expect(configuration.isAutoTruncateEnabled()).andReturn(true);
		control.replay();

		assertThat(service.getTruncatedString(LONG_STRING)).isEqualTo(LONG_STRING.substring(0, 10));
	}

	@Test
	public void testMethodAnnotatedWithDatabaseEntityOnAnnotatedClassWhenDisabled() {
		expect(configuration.isAutoTruncateEnabled()).andReturn(false);
		control.replay();

		assertThat(service.getTestObjectString(new TestObject(LONG_STRING))).isEqualTo(LONG_STRING);
	}

	@Test
	public void testMethodAnnotatedWithDatabaseFieldOnAnnotatedClassWhenDisabled() {
		expect(configuration.isAutoTruncateEnabled()).andReturn(false);
		control.replay();

		assertThat(service.getTruncatedString(LONG_STRING)).isEqualTo(LONG_STRING);
	}

	public static class TestObject {

		private String string = LONG_STRING;

		public TestObject() {
		}

		public TestObject(String string) {
			this.string = string;
		}

		@DatabaseField(table = TABLE, column = "LongString")
		public String getString() {
			return string;
		}

		public void setString(String string) {
			this.string = string;
		}

	}

	@AutoTruncate
	public static class AutoTruncateTestService {

		@AutoTruncate
		public String getTestObjectString(@DatabaseEntity TestObject object) {
			return object.getString();
		}

		@AutoTruncate
		public String getTruncatedString(@DatabaseField(table = TABLE, column = "Str") String str) {
			return str;
		}

		public String getTestObjectStringNoAnnotation(TestObject object) {
			return object.getString();
		}

	}

	public static class AutoTruncateTestServiceNoAnnotation {

		@AutoTruncate
		public String getTestObjectString(@DatabaseEntity TestObject object) {
			return object.getString();
		}

		public String getTestObjectStringNoAnnotation(TestObject object) {
			return object.getString();
		}

	}

}
