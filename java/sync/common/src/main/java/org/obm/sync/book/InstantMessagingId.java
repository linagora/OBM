package org.obm.sync.book;

public class InstantMessagingId implements IMergeable {

	private String protocol;
	private String id;

	public InstantMessagingId(String protocol, String address) {
		super();
		this.protocol = protocol;
		this.id = address;
	}

	public String getId() {
		return id;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	@Override
	public void merge(IMergeable previous) {
		if (previous instanceof InstantMessagingId) {
			InstantMessagingId prev = (InstantMessagingId) previous;
			if (getId() == null && prev.getId() != null) {
				setId(prev.getId());
			}
			if (getProtocol() == null && prev.getProtocol() != null) {
				setProtocol(prev.getProtocol());
			}
		}
	}

}
