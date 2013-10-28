/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.push.utils.collection;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;


import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;

import org.obm.filter.SlowFilterRunner;

@RunWith(SlowFilterRunner.class)
public class ClassToInstanceAgregateViewTest {

	@Test
	public void testViewIsLive() {
		ClassToInstanceAgregateView<Object> multiMapView = new ClassToInstanceAgregateView<Object>();
		MutableClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
		multiMapView.addMap(map);
		Integer i = new Integer(1);
		map.putInstance(Integer.class, i);
		Assertions.assertThat(multiMapView.get(Integer.class)).isSameAs(i);
	}

	@Test
	public void testViewNoMap() {
		ClassToInstanceAgregateView<Object> multiMapView = new ClassToInstanceAgregateView<Object>();
		Assertions.assertThat(multiMapView.get(Integer.class)).isNull();
	}
	
	@Test
	public void testViewNoResult() {
		ClassToInstanceAgregateView<Object> multiMapView = new ClassToInstanceAgregateView<Object>();
		MutableClassToInstanceMap<Object> map = MutableClassToInstanceMap.create();
		multiMapView.addMap(map);
		Assertions.assertThat(multiMapView.get(Integer.class)).isNull();
	}
	
	@Test
	public void testViewTwoSources() {
		ClassToInstanceAgregateView<Object> multiMapView = new ClassToInstanceAgregateView<Object>();
		Integer i = new Integer(1);
		String s = new String("2");
		multiMapView.addMap(ImmutableClassToInstanceMap.builder()
				.put(Integer.class, i).build());
		multiMapView.addMap(ImmutableClassToInstanceMap.builder()
				.put(String.class, s).build());
		Assertions.assertThat(multiMapView.get(Integer.class)).isSameAs(i);
		Assertions.assertThat(multiMapView.get(String.class)).isSameAs(s);
	}
	
	@Test(expected=IllegalStateException.class)
	public void testViewDuplicate() {
		ClassToInstanceAgregateView<Object> multiMapView = new ClassToInstanceAgregateView<Object>();
		Integer i = new Integer(1);
		multiMapView.addMap(ImmutableClassToInstanceMap.builder()
				.put(Integer.class, i).build());
		multiMapView.addMap(ImmutableClassToInstanceMap.builder()
				.put(Integer.class, i).build());
		multiMapView.get(Integer.class);
	}
	
	@Test(expected=IllegalStateException.class)
	public void testViewSameMapTwice() {
		ClassToInstanceAgregateView<Object> multiMapView = new ClassToInstanceAgregateView<Object>();
		Integer i = new Integer(1);
		ImmutableClassToInstanceMap<Object> map = ImmutableClassToInstanceMap.builder()
				.put(Integer.class, i).build();
		multiMapView.addMap(map);
		multiMapView.addMap(map);
		multiMapView.get(Integer.class);
	}
}
