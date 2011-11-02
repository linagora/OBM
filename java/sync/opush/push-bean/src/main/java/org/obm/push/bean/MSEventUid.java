package org.obm.push.bean;

import com.google.common.base.Objects;

public class MSEventUid {

	private final String uid;

	public MSEventUid(String uid) {
		super();
		this.uid = uid;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(uid);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof MSEventUid) {
			MSEventUid that = (MSEventUid) object;
			return Objects.equal(this.uid, that.uid);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("uid", uid)
			.toString();
	}
	
}
