package org.obm.push.bean.autodiscover;

import com.google.common.base.Objects;

public class AutodiscoverResponseServer {

	private final String type;
	private final String url;
	private final String name;
	private final String serverData;
	
	public AutodiscoverResponseServer(String type, String url, String name,
			String serverData) {
		
		this.type = type;
		this.url = url;
		this.name = name;
		this.serverData = serverData;
	}

	/**
	 *  Indicates that the URL that is returned by the URL element
	 *  
	 * @return MobileSync or CertEnroll
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Specifies a URL string that conveys the protocol, port, resource location, and other
	 * information.
	 *
	 * @return url
	 */
	public String getUrl() {
		return url;
	}
	
	/**
	 * If the Type element value is "MobileSync", then the Name element specifies the URL that conveys
	 * the protocol. If the Type element value is "CertEnroll", then the Name element value is NULL.
	 * 
	 * @return name
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * The ServerData element is a string value that is present only when the Type element (section
	 * 2.2.3.159.1) value is set to "CertEnroll".
	 *
	 * @return
	 */
	public String getServerData() {
		return serverData;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(type, url, name, serverData);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof AutodiscoverResponseServer) {
			AutodiscoverResponseServer that = (AutodiscoverResponseServer) object;
			return Objects.equal(this.type, that.type)
				&& Objects.equal(this.url, that.url)
				&& Objects.equal(this.name, that.name)
				&& Objects.equal(this.serverData, that.serverData);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("type", type)
			.add("url", url)
			.add("name", name)
			.add("serverData", serverData)
			.toString();
	}
	
}
