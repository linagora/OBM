/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
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
import static org.easymock.EasyMock.isA;
import static org.fest.assertions.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.annotations.database.DatabaseEntity;
import org.obm.annotations.database.DatabaseField;
import org.obm.sync.book.Address;
import org.obm.sync.book.Contact;
import org.obm.sync.book.InstantMessagingId;
import org.obm.sync.book.Phone;
import org.obm.sync.book.Website;
import org.obm.sync.calendar.Event;
import org.obm.test.GuiceModule;
import org.obm.test.SlowGuiceRunner;

import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;

import fr.aliacom.obm.ToolBox;

@RunWith(SlowGuiceRunner.class)
@GuiceModule(DatabaseTruncationServiceImplTest.Env.class)
public class DatabaseTruncationServiceImplTest {

	public static class Env extends AbstractModule {

		private IMocksControl control = createControl();

		@Override
		protected void configure() {
			bind(IMocksControl.class).toInstance(control);

			bindWithMock(DatabaseMetadataService.class);
		}

		private <T> void bindWithMock(Class<T> cls) {
			bind(cls).toInstance(control.createMock(cls));
		}

	}

	private static final String LONG_STRING = "This is a looooooooooooooooooooooooooooooooooong String";

	@Inject
	private DatabaseMetadataService metadataService;
	@Inject
	private DatabaseTruncationServiceImpl service;
	@Inject
	private IMocksControl control;

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
	public void testGetTruncatingEntityReturnsObjectOfSameType() throws Exception {
		expect(metadataService.getTableDescriptionOf("Test")).andReturn(tableDescription).anyTimes();
		expect(tableDescription.getMaxAllowedBytesOf("long_string")).andReturn(500).anyTimes();
		control.replay();

		assertThat(service.getTruncatingEntity(getTestObject())).isInstanceOf(TestObject.class);
	}

	@Test
	public void testGetTruncatingEntityReturnsNullWhenNullGiven() throws Exception {
		control.replay();

		assertThat(service.getTruncatingEntity(null)).isNull();
	}

	@Test
	public void testGetTruncatingEntityDoesNothingOnUnannotatedMethods() throws Exception {
		expect(metadataService.getTableDescriptionOf("Test")).andReturn(tableDescription).anyTimes();
		expect(tableDescription.getMaxAllowedBytesOf("long_string")).andReturn(500).anyTimes();
		control.replay();

		TestObject proxy = service.getTruncatingEntity(getTestObject());

		assertThat(proxy.getStringValue()).isEqualTo(LONG_STRING);
	}

	@Test
	public void testGetTruncatingEntityDoesNothingOnAnnotatedNonStringMethods() throws Exception {
		expect(metadataService.getTableDescriptionOf("Test")).andReturn(tableDescription).atLeastOnce();
		expect(tableDescription.getMaxAllowedBytesOf("long_string")).andReturn(500).atLeastOnce();
		control.replay();

		TestObject proxy = service.getTruncatingEntity(getTestObject());

		assertThat(proxy.getAnnotatedTestObject().getAnnotatedStringValue()).isEqualTo(LONG_STRING);
	}

	@Test
	public void testGetTruncatingEntityDoesNothingWhenAnnotatedMethodReturnsNull() throws Exception {
		expect(metadataService.getTableDescriptionOf("Test")).andReturn(tableDescription).atLeastOnce();
		expect(tableDescription.getMaxAllowedBytesOf("long_string")).andReturn(500).atLeastOnce();
		control.replay();

		TestObject proxy = service.getTruncatingEntity(getTestObject());

		assertThat(proxy.getLongStringReturnsNull()).isNull();
	}

	@Test(expected = SQLException.class)
	public void testGetTruncatingEntityWhenTableDoesntExist() throws Exception {
		expect(metadataService.getTableDescriptionOf("Test")).andThrow(new SQLException());
		control.replay();

		service.getTruncatingEntity(getTestObject()).getAnnotatedStringValue();
	}

	@Test(expected = SQLException.class)
	public void testGetTruncatingEntityWhenColumnDoesntExist() throws Exception {
		expect(metadataService.getTableDescriptionOf("Test")).andReturn(tableDescription);
		expect(tableDescription.getMaxAllowedBytesOf("long_string")).andThrow(new SQLException());
		control.replay();

		service.getTruncatingEntity(getTestObject()).getAnnotatedStringValue();
	}

	@Test
	public void testGetTruncatingEntityTruncatesAnnotatedStringMethods() throws Exception {
		expect(metadataService.getTableDescriptionOf("Test")).andReturn(tableDescription).atLeastOnce();
		expect(tableDescription.getMaxAllowedBytesOf("long_string")).andReturn(10).atLeastOnce();
		control.replay();

		TestObject proxy = service.getTruncatingEntity(getTestObject());

		assertThat(proxy.getAnnotatedStringValue()).isEqualTo(LONG_STRING.substring(0, 10));
	}

	@Test
	public void testGetTruncatingEntityTruncatesAnnotatedStringMethodsOnlyIfRequired() throws Exception {
		expect(metadataService.getTableDescriptionOf("Test")).andReturn(tableDescription).atLeastOnce();
		expect(tableDescription.getMaxAllowedBytesOf("long_string")).andReturn(500).atLeastOnce();
		control.replay();

		TestObject proxy = service.getTruncatingEntity(getTestObject());

		assertThat(proxy.getAnnotatedStringValue()).isEqualTo(LONG_STRING);
	}

	@Test
	public void testGetTruncatingEntityWithEvent() throws Exception {
		expect(metadataService.getTableDescriptionOf(Event.EVENT_TABLE)).andReturn(tableDescription).atLeastOnce();
		expect(tableDescription.getMaxAllowedBytesOf(isA(String.class))).andReturn(500).atLeastOnce();
		control.replay();

		assertThat(service.getTruncatingEntity(new Event())).isInstanceOf(Event.class);
	}

	@Test
	public void testGetTruncatingEntityWithContact() throws Exception {
		control.replay();

		assertThat(service.getTruncatingEntity(new Contact())).isInstanceOf(Contact.class);
	}

	@Test
	public void testGetTruncatingEntityDoesntBreakEquals() throws Exception {
		expect(metadataService.getTableDescriptionOf("Test")).andReturn(tableDescription).anyTimes();
		expect(tableDescription.getMaxAllowedBytesOf("long_string")).andReturn(10).anyTimes();
		control.replay();

		TestObject obj1 = getTestObject("1"), obj2 = getTestObject("2");
		TestObject proxy1 = service.getTruncatingEntity(obj1), proxy2 = service.getTruncatingEntity(obj2);

		assertThat(proxy1).isEqualTo(obj1);
		assertThat(obj1).isEqualTo(proxy1);
		assertThat(proxy2).isNotEqualTo(obj1);
		assertThat(proxy2).isNotEqualTo(proxy1);
	}

	@Test
	public void testGetTruncatingEntityDoesntBreakHashCode() throws Exception {
		expect(metadataService.getTableDescriptionOf("Test")).andReturn(tableDescription).atLeastOnce();
		expect(tableDescription.getMaxAllowedBytesOf("long_string")).andReturn(500).atLeastOnce();
		control.replay();

		TestObject obj1 = getTestObject("1"), obj2 = getTestObject("2");
		TestObject proxy1 = service.getTruncatingEntity(obj1), proxy2 = service.getTruncatingEntity(obj2);

		assertThat(proxy1.hashCode()).isEqualTo(obj1.hashCode());
		assertThat(proxy2.hashCode()).isNotEqualTo(obj1.hashCode());
		assertThat(proxy2.hashCode()).isNotEqualTo(proxy1.hashCode());
	}

	@Test
	public void testGetTruncatingEntityProxiesReturnedDatabaseEntities() throws Exception {
		expect(metadataService.getTableDescriptionOf("Test")).andReturn(tableDescription).atLeastOnce();
		expect(tableDescription.getMaxAllowedBytesOf("long_string")).andReturn(10).atLeastOnce();
		control.replay();

		TestObject proxy = service.getTruncatingEntity(getTestObject());

		assertThat(proxy.getTestObject().getAnnotatedStringValue()).isEqualTo(LONG_STRING.substring(0, 10));
	}

	@Test
	public void testGetTruncatingEntityReturnsASerializableObject() throws Exception {
		expect(metadataService.getTableDescriptionOf("Test")).andReturn(tableDescription).atLeastOnce();
		expect(tableDescription.getMaxAllowedBytesOf("long_string")).andReturn(500).atLeastOnce();
		control.replay();

		TestObject proxy = service.getTruncatingEntity(getTestObject());

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(baos);

		oos.writeObject(proxy);
		oos.close();

		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(bais);
		Object object = ois.readObject();

		ois.close();

		assertThat(object).isInstanceOf(TestObject.class);
	}

	@Test
	public void testGetTruncatingEntityDoesntBreakEqualsOnEvent() throws Exception {
		expect(metadataService.getTableDescriptionOf(Event.EVENT_TABLE)).andReturn(tableDescription).anyTimes();
		expect(tableDescription.getMaxAllowedBytesOf(isA(String.class))).andReturn(500).anyTimes();
		control.replay();

		Event event = ToolBox.getFakeEvent(1);
		Event proxy = service.getTruncatingEntity(event);

		assertThat(proxy).isEqualTo(event);
	}

	@Test
	public void testGetTruncatingEntityDoesntBreakHashcodeOnEvent() throws Exception {
		expect(metadataService.getTableDescriptionOf(Event.EVENT_TABLE)).andReturn(tableDescription).anyTimes();
		expect(tableDescription.getMaxAllowedBytesOf(isA(String.class))).andReturn(500).anyTimes();
		control.replay();

		Event event = ToolBox.getFakeEvent(1);
		Event proxy = service.getTruncatingEntity(event);

		assertThat(proxy.hashCode()).isEqualTo(event.hashCode());
	}

	@Test
	public void testGetTruncatingEntityDoesntBreakEqualsOnContact() throws Exception {
		expect(metadataService.getTableDescriptionOf(Contact.CONTACT_TABLE)).andReturn(tableDescription).atLeastOnce();
		expect(metadataService.getTableDescriptionOf(Phone.PHONE_TABLE)).andReturn(tableDescription).atLeastOnce();
		expect(metadataService.getTableDescriptionOf(InstantMessagingId.IM_TABLE)).andReturn(tableDescription).atLeastOnce();
		expect(metadataService.getTableDescriptionOf(Address.ADDRESS_TABLE)).andReturn(tableDescription).atLeastOnce();
		expect(metadataService.getTableDescriptionOf(Website.WEBSITE_TABLE)).andReturn(tableDescription).atLeastOnce();
		expect(tableDescription.getMaxAllowedBytesOf(isA(String.class))).andReturn(500).atLeastOnce();
		control.replay();

		Contact contact = ToolBox.getFakeContact(1);
		Contact proxy = service.getTruncatingEntity(contact);

		assertThat(proxy).isEqualTo(contact);
	}

	@Test
	public void testGetTruncatingEntityDoesntBreakHashcodeOnContact() throws Exception {
		expect(metadataService.getTableDescriptionOf(Contact.CONTACT_TABLE)).andReturn(tableDescription).atLeastOnce();
		expect(metadataService.getTableDescriptionOf(Phone.PHONE_TABLE)).andReturn(tableDescription).atLeastOnce();
		expect(metadataService.getTableDescriptionOf(InstantMessagingId.IM_TABLE)).andReturn(tableDescription).atLeastOnce();
		expect(metadataService.getTableDescriptionOf(Address.ADDRESS_TABLE)).andReturn(tableDescription).atLeastOnce();
		expect(metadataService.getTableDescriptionOf(Website.WEBSITE_TABLE)).andReturn(tableDescription).atLeastOnce();
		expect(tableDescription.getMaxAllowedBytesOf(isA(String.class))).andReturn(500).atLeastOnce();
		control.replay();

		Contact contact = ToolBox.getFakeContact(1);
		Contact proxy = service.getTruncatingEntity(contact);

		assertThat(proxy.hashCode()).isEqualTo(contact.hashCode());
	}

	@Test
	public void testGetTruncatingEntityProxiesReturnedMapOfDatabaseEntities() throws Exception {
		expect(metadataService.getTableDescriptionOf("Test")).andReturn(tableDescription).atLeastOnce();
		expect(tableDescription.getMaxAllowedBytesOf("long_string")).andReturn(10).atLeastOnce();
		control.replay();

		TestObject proxy = service.getTruncatingEntity(getTestObject());

		assertThat(proxy.getTestObjectMap().get("Object").getAnnotatedStringValue()).isEqualTo(LONG_STRING.substring(0, 10));
	}

	@Test
	public void testGetTruncatingEntityProxiesReturnedSetOfDatabaseEntities() throws Exception {
		expect(metadataService.getTableDescriptionOf("Test")).andReturn(tableDescription).atLeastOnce();
		expect(tableDescription.getMaxAllowedBytesOf("long_string")).andReturn(10).atLeastOnce();
		control.replay();

		TestObject proxy = service.getTruncatingEntity(getTestObject());

		assertThat(proxy.getTestObjectSet().iterator().next().getAnnotatedStringValue()).isEqualTo(LONG_STRING.substring(0, 10));
	}

	@Test
	public void testGetTruncatingEntityProxiesReturnedListOfDatabaseEntities() throws Exception {
		expect(metadataService.getTableDescriptionOf("Test")).andReturn(tableDescription).atLeastOnce();
		expect(tableDescription.getMaxAllowedBytesOf("long_string")).andReturn(10).atLeastOnce();
		control.replay();

		TestObject proxy = service.getTruncatingEntity(getTestObject());

		assertThat(proxy.getTestObjectList().get(0).getAnnotatedStringValue()).isEqualTo(LONG_STRING.substring(0, 10));
	}

	@Test
	public void testGetTruncatingEntityDoesNothingOnUnsupportedCollections() throws Exception {
		expect(metadataService.getTableDescriptionOf("Test")).andReturn(tableDescription).atLeastOnce();
		expect(tableDescription.getMaxAllowedBytesOf("long_string")).andReturn(10).atLeastOnce();
		control.replay();

		TestObject proxy = service.getTruncatingEntity(getTestObject());

		assertThat(proxy.getTestObjectQueue().poll().getAnnotatedStringValue()).isEqualTo(LONG_STRING);
	}

	@Test
	public void testTruncateReturnsNullWhenNullGiven() throws Exception {
		control.replay();

		assertThat(service.truncate(null, "", "")).isNull();
	}

	@Test(expected = SQLException.class)
	public void testTruncateWhenColumnDoesntExist() throws Exception {
		expect(metadataService.getTableDescriptionOf("Test")).andReturn(tableDescription);
		expect(tableDescription.getMaxAllowedBytesOf("Column")).andThrow(new SQLException());
		control.replay();

		service.truncate(LONG_STRING, "Test", "Column");
	}

	@Test(expected = SQLException.class)
	public void testTruncateWhenTableDoesntExist() throws Exception {
		expect(metadataService.getTableDescriptionOf("Test")).andThrow(new SQLException());
		control.replay();

		service.truncate(LONG_STRING, "Test", "Column");
	}

	@Test
	public void testTruncate() throws Exception {
		expect(metadataService.getTableDescriptionOf("Test")).andReturn(tableDescription);
		expect(tableDescription.getMaxAllowedBytesOf("Column")).andReturn(10);
		control.replay();

		assertThat(service.truncate(LONG_STRING, "Test", "Column")).isEqualTo(LONG_STRING.substring(0, 10));
	}

	@Test
	public void testTruncateTruncatesOnlyIfRequired() throws Exception {
		expect(metadataService.getTableDescriptionOf("Test")).andReturn(tableDescription);
		expect(tableDescription.getMaxAllowedBytesOf("Column")).andReturn(500);
		control.replay();

		assertThat(service.truncate(LONG_STRING, "Test", "Column")).isEqualTo(LONG_STRING);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testTruncateWithNullTable() throws Exception {
		control.replay();

		service.truncate("", null, "");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testTruncateWithNullColumn() throws Exception {
		control.replay();

		service.truncate("", "", null);
	}

	private static TestObject getTestObject() {
		return getTestObject(LONG_STRING);
	}

	private static TestObject getTestObject(String strValue) {
		TestObject object = new TestObject(strValue);

		object.setTestObject(new TestObject(strValue));
		object.setTestObjectList(ImmutableList.of(new TestObject(strValue)));
		object.setTestObjectSet(ImmutableSet.of(new TestObject(strValue)));
		object.setTestObjectMap(ImmutableMap.of("Object", new TestObject(strValue)));
		object.setTestObjectQueue(new LinkedBlockingQueue<DatabaseTruncationServiceImplTest.TestObject>(Collections.singleton(new TestObject(strValue))));

		return object;
	}

	public static class TestObject implements Serializable {

		private String stringValue;
		private String annotatedStringValue;
		private TestObject testObject;
		private Map<String, TestObject> testObjectMap;
		private Queue<TestObject> testObjectQueue;
		private List<TestObject> testObjectList;
		private Set<TestObject> testObjectSet;

		public TestObject() {
		}

		public TestObject(String stringValue) {
			this.stringValue = stringValue;
			this.annotatedStringValue = stringValue;
		}

		public String getStringValue() {
			return stringValue;
		}

		public void setStringValue(String stringValue) {
			this.stringValue = stringValue;
		}

		@DatabaseField(table = "Test", column = "long_string")
		public String getAnnotatedStringValue() {
			return annotatedStringValue;
		}

		public void setAnnotatedStringValue(String annotatedStringValue) {
			this.annotatedStringValue = annotatedStringValue;
		}

		@DatabaseField(table = "Test", column = "object")
		public TestObject getAnnotatedTestObject() {
			return testObject;
		}

		public void setAnnotatedTestObject(TestObject testObject) {
			this.testObject = testObject;
		}

		@DatabaseField(table = "Test", column = "long_string")
		public String getLongStringReturnsNull() {
			return null;
		}

		public void setLongStringReturnsNull(@SuppressWarnings("unused") String str) {
		}

		@DatabaseEntity
		public TestObject getTestObject() {
			return testObject;
		}

		@DatabaseEntity
		public Set<TestObject> getTestObjectSet() {
			return testObjectSet;
		}

		@DatabaseEntity
		public List<TestObject> getTestObjectList() {
			return testObjectList;
		}

		@DatabaseEntity
		public Queue<TestObject> getTestObjectQueue() {
			return testObjectQueue;
		}

		@DatabaseEntity
		public Map<String, TestObject> getTestObjectMap() {
			return testObjectMap;
		}

		public void setTestObject(TestObject testObject) {
			this.testObject = testObject;
		}

		public void setTestObjectMap(Map<String, TestObject> testObjectMap) {
			this.testObjectMap = testObjectMap;
		}

		public void setTestObjectQueue(Queue<TestObject> testObjectQueue) {
			this.testObjectQueue = testObjectQueue;
		}

		public void setTestObjectList(List<TestObject> testObjectList) {
			this.testObjectList = testObjectList;
		}

		public void setTestObjectSet(Set<TestObject> testObjectSet) {
			this.testObjectSet = testObjectSet;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(stringValue);
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof TestObject)) {
				return false;
			}

			TestObject other = (TestObject) obj;

			return Objects.equal(stringValue, other.stringValue);
		}

	}
}
