package org.obm.sync.book;

public class Website implements IMergeable {

	private String url;

	public Website(String url) {
		super();
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public void merge(IMergeable previous) {
		//do nothing on merge
	}

}
