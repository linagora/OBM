package org.obm.push.bean;

import java.io.Serializable;
import com.google.common.base.Objects;


public class SyncCollectionChange implements Serializable {

	private String serverId;
	private String clientId;
	private String modType;
	private PIMDataType type;
	private IApplicationData data;
	
	public SyncCollectionChange(String serverId, String clientId,
			String modType, IApplicationData data, PIMDataType type) {
		super();
		this.serverId = serverId;
		this.clientId = clientId;
		this.modType = modType;
		this.data = data;
		this.type = type;
	}

	public String getServerId() {
		return serverId;
	}

	public String getClientId() {
		return clientId;
	}

	public String getModType() {
		return modType;
	}

	public IApplicationData getData() {
		return data;
	}

	public PIMDataType getType() {
		return type;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(serverId, clientId, modType, type, data);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof SyncCollectionChange) {
			SyncCollectionChange that = (SyncCollectionChange) object;
			return Objects.equal(this.serverId, that.serverId)
				&& Objects.equal(this.clientId, that.clientId)
				&& Objects.equal(this.modType, that.modType)
				&& Objects.equal(this.type, that.type)
				&& Objects.equal(this.data, that.data);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("serverId", serverId)
			.add("clientId", clientId)
			.add("modType", modType)
			.add("type", type)
			.add("data", data)
			.toString();
	}
	
}
