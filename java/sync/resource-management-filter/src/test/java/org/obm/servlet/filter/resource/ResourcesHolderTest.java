/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2014 Linagora
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
package org.obm.servlet.filter.resource;

import static org.fest.assertions.api.Assertions.assertThat;

import java.util.Date;

import org.junit.Before;
import org.junit.Test;

public class ResourcesHolderTest {

	private ResourcesHolder testee;

	@Before
	public void setUp() {
		testee = new ResourcesHolder();
	}
	
	@Test
	public void getShouldReturnNullWhenKeyNoMatch() {
		assertThat(testee.get(TestResource.class)).isNull();
	}
	
	
	@Test
	public void putShouldMakeGetSucceedWhenKeyMatch() {
		TestResource resource = new TestResource();
		testee.put(TestResource.class, resource);
		
		assertThat(testee.get(TestResource.class)).isSameAs(resource);
	}
	
	@Test(expected=IllegalStateException.class)
	public void putShouldCloseOverridenResource() throws Exception {
		TestResource resourceOverrided = new TestResource();
		TestResource resource = new TestResource();
		testee.put(TestResource.class, resourceOverrided);
		
		try {
			testee.put(TestResource.class, resource);
		} catch (Exception e) {
			assertThat(resourceOverrided.isClosed).isTrue();
			throw e;
		}
	}

	@Test
	public void closeShouldNoopWhenNoResource() {
		testee.close();
	}
	
	@Test
	public void closeShouldCloseResource() {
		TestResource resource = new TestResource();
		testee.put(TestResource.class, resource);
		
		testee.close();
		
		assertThat(resource.isClosed).isTrue();
	}

	@Test
	public void removeShouldNoopWhenNoResource() {
		testee.remove(TestResource.class);
	}
	
	@Test
	public void removeShouldCloseResource() {
		TestResource resource = new TestResource();
		testee.put(TestResource.class, resource);
		
		testee.remove(TestResource.class);
		
		assertThat(resource.isClosed).isTrue();
	}
	
	@Test
	public void removeShouldFreeKeyForNewResource() {
		TestResource resource = new TestResource();
		testee.put(TestResource.class, resource);
		
		testee.remove(TestResource.class);
		testee.put(TestResource.class, new TestResource());
		
		assertThat(resource.isClosed).isTrue();
	}
	
	@Test
	public void closeShouldCloseEveryResourceEvenWhenException() {
		ResourceType1 first = new ResourceType1();
		CloseFailsResource second = new CloseFailsResource();
		ResourceType2 third = new ResourceType2();
		
		testee.put(ResourceType1.class, first);
		testee.put(CloseFailsResource.class, second);
		testee.put(ResourceType2.class, third);
		testee.close();

		assertThat(first.isClosed).isTrue();
		assertThat(second.isClosed).isTrue();
		assertThat(third.isClosed).isTrue();
	}
	
	@Test
	public void closeShouldCloseResourceInLifoOrder() {
		ResourceType1 first = new ResourceType1();
		ResourceType2 second = new ResourceType2();
		ResourceType3 third = new ResourceType3();
		ResourceType4 fourth = new ResourceType4();
		
		testee.put(ResourceType1.class, first);
		testee.put(ResourceType2.class, second);
		testee.put(ResourceType3.class, third);
		testee.put(ResourceType4.class, fourth);
		testee.close();

		assertThat(first.closedAt).isAfterOrEqualsTo(second.closedAt);
		assertThat(second.closedAt).isAfterOrEqualsTo(third.closedAt);
		assertThat(third.closedAt).isAfterOrEqualsTo(fourth.closedAt);
	}
	
	static class TestResource implements Resource {

		boolean isClosed = false;
		Date closedAt;

		@Override
		public void closeResource() {
			isClosed = true;
			closedAt = new Date();
		}
		
	}
	
	static class ResourceType1 extends TestResource {
	}
	
	static class ResourceType2 extends TestResource {
	}
	
	static class ResourceType3 extends TestResource {
	}
	
	static class ResourceType4 extends TestResource {
	}

	static class CloseFailsResource extends TestResource {
		
		@Override
		public synchronized void closeResource() {
			super.closeResource();
			throw new RuntimeException("error");
		}
		
	}
}
