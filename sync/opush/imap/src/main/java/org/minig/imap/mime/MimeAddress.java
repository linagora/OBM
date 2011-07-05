package org.minig.imap.mime;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

public class MimeAddress {

	private String addr;
	private transient Integer nestLevel;
	
	public MimeAddress(String addr) {
		this.addr = addr;
	}
	
	@Override
	public String toString() {
		return addr;
	}
	
	public int compareNestLevel(MimeAddress rhs) {
		if (rhs == null) {
			return -1;
		}
		return this.countNestLevel() - rhs.countNestLevel();
	}

	public int countNestLevel() {
		if (nestLevel == null) {
			nestLevel = Iterables.size(Splitter.on(".").split(addr));
		}
		return nestLevel;
	}

	public static MimeAddress concat(MimeAddress firstPart,	Integer secondPart) {
		String firstPartAsString = null;
		if (firstPart != null) {
			firstPartAsString = firstPart.toString();
		}
		String secondPartAsString = null;
		if (secondPart != null) {
			secondPartAsString = String.valueOf(secondPart);
		}
		return new MimeAddress(Joiner.on(".").skipNulls().join(
				Strings.emptyToNull(firstPartAsString), secondPartAsString));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((addr == null) ? 0 : addr.hashCode());
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
		MimeAddress other = (MimeAddress) obj;
		if (addr == null) {
			if (other.addr != null)
				return false;
		} else if (!addr.equals(other.addr))
			return false;
		return true;
	}

	public int getLastIndex() {
		String lastIdx = Iterables.getLast(Splitter.on('.').split(addr));
		if (Strings.isNullOrEmpty(lastIdx)) {
			return -1;
		}
		return Integer.valueOf(lastIdx);
	}
	
	
	
}
