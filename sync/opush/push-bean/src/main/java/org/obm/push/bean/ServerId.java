package org.obm.push.bean;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.obm.push.exception.activesync.InvalidServerId;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.base.Objects;

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

	public boolean isItem() {
		return itemId != null;
	}
	
	@Override
	public String toString() {
		return collectionId + ":" + itemId;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(collectionId, itemId);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof ServerId) {
			ServerId that = (ServerId) object;
			return Objects.equal(this.collectionId, that.collectionId)
				&& Objects.equal(this.itemId, that.itemId);
		}
		return false;
	}
	


	
}
