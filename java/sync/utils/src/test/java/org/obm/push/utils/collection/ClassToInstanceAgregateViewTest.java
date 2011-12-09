package org.obm.push.utils.collection;

import org.fest.assertions.Assertions;
import org.junit.Test;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;

import com.google.common.collect.ImmutableClassToInstanceMap;
import com.google.common.collect.MutableClassToInstanceMap;

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
