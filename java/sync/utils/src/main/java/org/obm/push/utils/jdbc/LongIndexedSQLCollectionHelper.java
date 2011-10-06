package org.obm.push.utils.jdbc;

import java.util.ArrayList;
import java.util.Collection;

import org.obm.push.utils.index.IndexUtils;
import org.obm.push.utils.index.Indexed;

public class LongIndexedSQLCollectionHelper extends LongSQLCollectionHelper {

	public LongIndexedSQLCollectionHelper(Collection<? extends Indexed<Long>> indexedItems) {
		super(indexedItemsAsLongs(indexedItems));
	}

	private static ArrayList<Long> indexedItemsAsLongs(Collection<? extends Indexed<Long>> indexedItems) {
		ArrayList<Long> listIndexes = IndexUtils.listIndexes(indexedItems);
		return listIndexes;
	}
	
}
