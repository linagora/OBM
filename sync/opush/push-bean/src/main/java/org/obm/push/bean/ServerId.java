package org.obm.push.bean;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.obm.push.exception.activesync.InvalidServerId;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

public class ServerId {

	private final int collectionId;
	private final Integer itemId;

	public ServerId(String serverId) throws InvalidServerId {
		Iterator<String> iterator = splitServerId(serverId);
		collectionId = getCollectionId(iterator, serverId);
		itemId = getItemId(iterator);
	}

	private Iterator<String> splitServerId(String serverId) throws InvalidServerId {
		Iterable<String> parts = Splitter.on(':').split(serverId);
		if (Iterables.size(parts) > 2) {
			throw new InvalidServerId("two many parts for a serverId");
		}
		Iterator<String> iterator = parts.iterator();
		return iterator;
	}

	private int getCollectionId(Iterator<String> iterator, String serverId) throws InvalidServerId {
		 try {
			 return Integer.valueOf(iterator.next());
		 } catch (NoSuchElementException e) {
			 throw new InvalidServerId("serverId is invalid : " + serverId);
		 } catch (NumberFormatException e) {
			 throw new InvalidServerId("collectionId is not an integer", e);
		 }
	}

	private Integer getItemId(Iterator<String> iterator) throws InvalidServerId {
		if (iterator.hasNext()) {
			try {
				return Integer.valueOf(iterator.next());
			} catch (NumberFormatException e) {
				throw new InvalidServerId("itemId is not an integer", e);
			}
		} else {
			return null;
		}
	}
	
	public int getCollectionId() {
		return collectionId;
	}
	
	public Integer getItemId() {
		return itemId;
	}

	@Override
	public String toString() {
		return collectionId + ":" + itemId;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + collectionId;
		result = prime * result + ((itemId == null) ? 0 : itemId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServerId other = (ServerId) obj;
		if (collectionId != other.collectionId)
			return false;
		if (itemId == null) {
			if (other.itemId != null)
				return false;
		} else if (!itemId.equals(other.itemId))
			return false;
		return true;
	}

	
}
