package org.obm.push.store;


public class SyncCollectionChange {
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

}
