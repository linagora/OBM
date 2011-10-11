package org.obm.push.utils.collection;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import com.google.common.base.Preconditions;

public class Sets {

	/**
	 * This methods use the given comparator to perform a difference between col1 and col2.
	 * It uses {@link com.google.common.collect.Sets.difference} after wrapping col1 and col2 into TreeSets.
	 */
	public static <E> Set<E> difference(Collection<E> col1, Collection<E> col2, Comparator<E> comparator) {
		Preconditions.checkNotNull(col1, "col1");
		Preconditions.checkNotNull(col2, "col2");
		Preconditions.checkNotNull(comparator, "comparator");
		
		TreeSet<E> set1 = com.google.common.collect.Sets.newTreeSet(comparator);
		set1.addAll(col1);
		checkCollectionNoDuplicateEntry(col1, set1, "col1");
		TreeSet<E> set2 = com.google.common.collect.Sets.newTreeSet(comparator);
		set2.addAll(col2);
		checkCollectionNoDuplicateEntry(col2, set2, "col2");
		return com.google.common.collect.Sets.difference(set1, set2);
	}

	private static <E> void checkCollectionNoDuplicateEntry(Collection<E> col1, TreeSet<E> set1, String name) {
		if (col1.size() != set1.size()) {
			throw new IllegalStateException(name + " must not contain duplicate elements as evaluated by comparator");
		}
	}
	
}
