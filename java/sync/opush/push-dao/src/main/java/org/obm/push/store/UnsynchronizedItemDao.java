package org.obm.push.store;

import java.util.Collection;
import java.util.Set;

import org.obm.push.bean.Credentials;
import org.obm.push.bean.Device;
import org.obm.push.bean.ItemChange;

public interface UnsynchronizedItemDao {
	
	void storeItemsToAdd(Credentials credentials, Device device, int collectionId, Collection<ItemChange> ic);

	Set<ItemChange> listItemsToAdd(Credentials credentials, Device device, int collectionId);

	void clearItemsToAdd(Credentials credentials, Device device, int collectionId);
	
	void storeItemsToRemove(Credentials credentials, Device device, int collectionId, Collection<ItemChange> ic);

	Set<ItemChange> listItemsToRemove(Credentials credentials, Device device, int collectionId);

	void clearItemsToRemove(Credentials credentials, Device device, int collectionId);

}
