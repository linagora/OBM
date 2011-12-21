package org.obm.push.bean.protocol;

import java.util.List;

import com.google.common.base.Objects;

public class AutodiscoverResponse {

	private final String responseCulture;
	private final AutodiscoverResponseUser responseUser;
	private final String actionRedirect;
	private final List<AutodiscoverResponseServer> listActionServer;
	private final AutodiscoverResponseError actionError;
	private final AutodiscoverResponseError responseError;
	
	public AutodiscoverResponse(String responseCulture, AutodiscoverResponseUser responseUser, String actionRedirect,
			List<AutodiscoverResponseServer> listActionServer, AutodiscoverResponseError actionError, 
			AutodiscoverResponseError responseError) {
		
		this.responseCulture = responseCulture;
		this.responseUser = responseUser;
		this.actionRedirect = actionRedirect;
		this.listActionServer = listActionServer;
		this.actionError = actionError;
		this.responseError = responseError;
	}
	
	public String getResponseCulture() {
		return responseCulture;
	}
	
	public AutodiscoverResponseUser getResponseUser() {
		return responseUser;
	}
	
	public String getActionRedirect() {
		return actionRedirect;
	}
	
	public List<AutodiscoverResponseServer> getListActionServer() {
		return listActionServer;
	}
	
	public AutodiscoverResponseError getActionError() {
		return actionError;
	}
	
	public AutodiscoverResponseError getResponseError() {
		return responseError;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(responseCulture, responseUser, actionRedirect, listActionServer, actionError, responseError);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof AutodiscoverResponse) {
			AutodiscoverResponse that = (AutodiscoverResponse) object;
			return Objects.equal(this.responseCulture, that.responseCulture)
				&& Objects.equal(this.responseUser, that.responseUser)
				&& Objects.equal(this.actionRedirect, that.actionRedirect)
				&& Objects.equal(this.listActionServer, that.listActionServer)
				&& Objects.equal(this.actionError, that.actionError)
				&& Objects.equal(this.responseError, that.responseError);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("responseCulture", responseCulture)
			.add("responseUser", responseUser)
			.add("actionRedirect", actionRedirect)
			.add("actionServer", listActionServer)
			.add("actionError", actionError)
			.add("responseError", responseError)
			.toString();
	}
	
}
