package org.obm.push.protocol.bean;

import org.obm.push.bean.ProvisionStatus;
import org.obm.push.protocol.provisioning.Policy;

public class ProvisionResponse {

	private final String policyType;
	private Long policyKey;
	private Policy policy;
	private ProvisionStatus status;
	
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

	public ProvisionStatus getStatus() {
		return status;
	}

	public void setStatus(ProvisionStatus status) {
		this.status = status;
	}

	public void setPolicy(Policy policy) {
		this.policy = policy;
	}

	public void setPolicyKey(Long policyKey) {
		this.policyKey = policyKey;
	}
	
}
