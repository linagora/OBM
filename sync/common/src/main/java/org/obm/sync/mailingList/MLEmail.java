package org.obm.sync.mailingList;

public class MLEmail {

	private Integer id;
	private String label;
	private String address;

	public MLEmail(String label, String address) {
		this.address = address;
		this.label = label;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getAddress() {
		return address;
	}

	public void setAddressl(String address) {
		this.address = address;
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((label == null) ? 0 : label.hashCode());
		result = prime * result + ((address == null) ? 0 : address.hashCode());
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
		MLEmail other = (MLEmail) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		if (address == null) {
			if (other.address != null)
				return false;
		} else if (!address.equals(other.address))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MLEmail [displayName=" + label + ", address=" + address + "]";
	}
	
	
}
