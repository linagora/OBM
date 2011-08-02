package org.obm.push.bean;

import org.obm.push.provisioning.Policy;

public class ProvisionResponse {

	private final String policyType;
	private Long policyKey;
	private Policy policy;
	private int status;
	
	public ProvisionResponse(String policyType) {
		super();
		this.policyType = policyType;
	}
	
	public String getPolicyType() {
		return policyType;
	}

	public Long getPolicyKey() {
		return policyKey;
	}

	public Policy getPolicy() {
		return policy;	
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void setPolicy(Policy policy) {
		this.policy = policy;
	}

	public void setPolicyKey(Long policyKey) {
		this.policyKey = policyKey;
	}
	
}
