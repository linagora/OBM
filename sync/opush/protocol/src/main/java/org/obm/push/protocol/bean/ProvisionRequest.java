package org.obm.push.protocol.bean;

import com.google.common.base.Objects;

public class ProvisionRequest {

	private final String policyType;
	private final int policyKey;

	public ProvisionRequest(String policyType, int policyKey) {
		this.policyType = policyType;
		this.policyKey = policyKey;
	}
	
	public int getPolicyKey() {
		return policyKey;
	}
	
	public String getPolicyType() {
		return policyType;
	}
	
	@Override
	public String toString() {
		return Objects.toStringHelper(getClass())
			.add("policyType", policyType)
			.add("policyKey", policyKey)
			.toString();
	}
	
}
