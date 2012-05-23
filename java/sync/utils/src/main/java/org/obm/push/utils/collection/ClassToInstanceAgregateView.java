package org.obm.push.utils.collection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.ClassToInstanceMap;
import com.google.common.collect.Iterables;

public class ClassToInstanceAgregateView<V> implements Iterable<V> {

	private final ArrayList<ClassToInstanceMap<V>> maps;

	public ClassToInstanceAgregateView() {
		maps = new ArrayList<ClassToInstanceMap<V>>();
	}
	
	
	public void addMap(ClassToInstanceMap<V> map) {
		maps.add(map);
	}
	
	@SuppressWarnings("unchecked")
	public <T> T get(Class<T> key) {
		T result = null;
		for (ClassToInstanceMap<V> map: maps) {
			V value = map.get(key);
			if (value != null) {
				if (result != null) {
					throw new IllegalStateException("type " + key + " is registered in several maps");
				}
				result = (T) value;
			}
		}
		return result;
	}

	@Override
	public Iterator<V> iterator() {
		List<Iterable<V>> valueIterables = new ArrayList<Iterable<V>>();
		for (ClassToInstanceMap<V> map: maps) {
			valueIterables.add(map.values());
		}
		return Iterables.concat(valueIterables).iterator();
	}
	
}
