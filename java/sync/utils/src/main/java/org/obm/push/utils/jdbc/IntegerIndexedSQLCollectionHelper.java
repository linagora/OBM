package org.obm.push.utils.jdbc;

import java.util.ArrayList;
import java.util.Collection;

import org.obm.push.utils.index.IndexUtils;
import org.obm.push.utils.index.Indexed;

public class IntegerIndexedSQLCollectionHelper extends IntegerSQLCollectionHelper {

	public IntegerIndexedSQLCollectionHelper(Collection<? extends Indexed<Integer>> indexedItems) {
		super(indexedItemsAsIntegers(indexedItems));
	}

	private static ArrayList<Integer> indexedItemsAsIntegers(Collection<? extends Indexed<Integer>> indexedItems) {
		ArrayList<Integer> listIndexes = IndexUtils.listIndexes(indexedItems);
		return listIndexes;
	}
	
}
