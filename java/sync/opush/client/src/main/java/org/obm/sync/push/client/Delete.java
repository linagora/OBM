package org.obm.sync.push.client;

import com.google.common.base.Objects;

public final class Delete {
	
	private String serverId;

	public Delete() {
	}
	
	public Delete(String serverId) {
		setServerId(serverId);
	}
	
	public String getServerId() {
		return serverId;
	}

	public void setServerId(String serverId) {
		this.serverId = serverId;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(serverId);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof Delete) {
			Delete that = (Delete) object;
			return Objects.equal(this.serverId, that.serverId);
		}
		return false;
	}
	
}
