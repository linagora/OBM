package org.obm.sync.book;


public class Website {

	private String url;
	private String label;
	
	public Website(String label, String url) {
		super();
		this.label = label;
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public boolean isCalendarUrl() {
		if (label.toLowerCase().startsWith("caluri")) {
			return true;
		}
		return false;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Website other = (Website) obj;
		if (label != null && label.equalsIgnoreCase(other.label)) {
			return true;
		}
		return false;
	}

	@Override
	public int hashCode() {
		int prime = 31;
		int result = 1;
		result = prime * result + 
		((label == null) ? 0 : label.toLowerCase().hashCode());
		result = prime * result + 
		((url == null) ? 0 : url.hashCode());
		return result;
	}

}
