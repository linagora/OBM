package org.obm.push;

import java.util.Set;

import org.obm.push.impl.Credentials;

public interface UnsynchronizedItemService {
	
	void storeItemToAdd(Credentials credentials, int collectionId, ItemChange ic);

	Set<ItemChange> listItemToAdd(Credentials credentials, int collectionId);

	void clearItemToAdd(Credentials credentials, int collectionId);
	
	void storeItemToRemove(Credentials credentials, int collectionId, ItemChange ic);

	Set<ItemChange> listItemToRemove(Credentials credentials, int collectionId);

	void clearItemToRemove(Credentials credentials, int collectionId);

}
