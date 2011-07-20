package org.obm.push;

import java.util.List;

import org.obm.push.impl.Credentials;

public interface UnsynchronizedItemService {
	
	void add(Credentials credentials, int collectionId, ItemChange ic);

	List<ItemChange> list(Credentials credentials, int collectionId);

	void clear(Credentials credentials, int collectionId);

}
