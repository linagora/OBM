package org.obm.push.bean;

public enum ProvisionStatus {

	SUCCESS, // 1

	// if the parent element is the 'PROVISION' element
	PROTOCOL_ERROR, // 2
	GENERAL_SERVER_ERROR, // 3
	
	// if the parent element is the 'POLICY' element (the child of the POLICY is in the RESPONSE)

	POLICY_NOT_DEFINED, // 2
	UNKNOW_POLICY_TYPE_VALUE, // 3
	THE_CLIENT_IS_ACKNOWLEDGING_THE_WRONG_POLICY_KEY; // 5
	
	public String asXmlValue() {
		switch (this) {
		case PROTOCOL_ERROR:
		case POLICY_NOT_DEFINED:
			return "2";
		case GENERAL_SERVER_ERROR:
		case UNKNOW_POLICY_TYPE_VALUE:
				return "3";
		case THE_CLIENT_IS_ACKNOWLEDGING_THE_WRONG_POLICY_KEY:
			return "5";
		case SUCCESS:
		default:
			return "1";
		}
	}
}
