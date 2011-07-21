package org.obm.push;

import java.util.Set;

import org.obm.push.impl.Credentials;

public interface UnsynchronizedItemService {
	
	void storeItemToAdd(Credentials credentials, Device device, int collectionId, ItemChange ic);

	Set<ItemChange> listItemToAdd(Credentials credentials, Device device, int collectionId);

	void clearItemToAdd(Credentials credentials, Device device, int collectionId);
	
	void storeItemToRemove(Credentials credentials, Device device, int collectionId, ItemChange ic);

	Set<ItemChange> listItemToRemove(Credentials credentials, Device device, int collectionId);

	void clearItemToRemove(Credentials credentials, Device device, int collectionId);

}
