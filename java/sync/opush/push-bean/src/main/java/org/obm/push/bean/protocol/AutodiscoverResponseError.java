package org.obm.push.bean.protocol;


import com.google.common.base.Objects;

public class AutodiscoverResponseError {

	private final AutodiscoverStatus status;
	private final String message;
	private final String debugData;
	private final Integer errorCode;
	
	public AutodiscoverResponseError(AutodiscoverStatus status, String message,
			String debugData, Integer errorCode) {
		this.status = status;
		this.message = message;
		this.debugData = debugData;
		this.errorCode = errorCode;
	}
	
	public AutodiscoverStatus getStatus() {
		return status;
	}
	
	public String getMessage() {
		return message;
	}
	
	public String getDebugData() {
		return debugData;
	}
	
	public Integer getErrorCode() {
		return errorCode;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(status, message, debugData, errorCode);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof AutodiscoverResponseError) {
			AutodiscoverResponseError that = (AutodiscoverResponseError) object;
			return Objects.equal(this.status, that.status)
				&& Objects.equal(this.message, that.message)
				&& Objects.equal(this.debugData, that.debugData)
				&& Objects.equal(this.errorCode, that.errorCode);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("status", status)
			.add("message", message)
			.add("debugData", debugData)
			.add("errorCode", errorCode)
			.toString();
	}
	
}
