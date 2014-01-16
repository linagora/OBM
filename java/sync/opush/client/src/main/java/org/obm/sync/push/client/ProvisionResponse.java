/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.sync.push.client;

import javax.xml.transform.TransformerException;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.obm.push.bean.ProvisionPolicyStatus;
import org.obm.push.bean.ProvisionStatus;
import org.obm.push.utils.DOMUtils;
import org.w3c.dom.Element;

import com.google.common.base.Objects;

public class ProvisionResponse {
	
	public static Builder builder() {
		return new Builder();
	}
	
	public static class Builder {
		private ProvisionStatus provisionStatus;
		private ProvisionPolicyStatus policyStatus;
		private Long policyKey;
		private String policyType;
		private Element policyData;

		private Builder() {
			super();
		}
		
		public Builder provisionStatus(ProvisionStatus provisionStatus) {
			this.provisionStatus = provisionStatus;
			return this;
		}
		
		public Builder policyStatus(ProvisionPolicyStatus policyStatus) {
			this.policyStatus = policyStatus;
			return this;
		}
		
		public Builder policyKey(Long policyKey) {
			this.policyKey = policyKey;
			return this;
		}
		
		public Builder policyType(String policyType) {
			this.policyType = policyType;
			return this;
		}

		public Builder policyData(Element policyData) {
			this.policyData = policyData;
			return this;
		}
		
		public ProvisionResponse build() throws TransformerException {
			String policyValue = serializePolicy();
			return new ProvisionResponse(provisionStatus, policyStatus, policyKey, policyType, policyData, policyValue);
		}

		private String serializePolicy() throws TransformerException {
			if (policyData != null) {
				return DOMUtils.prettySerialize(policyData);
			}
			return null;
		}
		
	}

	private final ProvisionStatus provisionStatus;
	private final ProvisionPolicyStatus policyStatus;
	private final Long policyKey;
	private final String policyType;
	private final String policyData;
	private final Element policyDataEl;
	
	private ProvisionResponse(ProvisionStatus provisionStatus, ProvisionPolicyStatus policyStatus,
			Long policyKey, String policyType, Element policyDataEl, String policyData) {
		
		this.provisionStatus = provisionStatus;
		this.policyKey = policyKey;
		this.policyStatus =  policyStatus;
		this.policyType = policyType;
		this.policyDataEl = policyDataEl;
		this.policyData = policyData;
	}

	public ProvisionStatus getProvisionStatus() {
		return provisionStatus;
	}
	
	public ProvisionPolicyStatus getPolicyStatus() {
		return policyStatus;
	}

	public Long getPolicyKey() {
		return policyKey;
	}

	public String getPolicyType() {
		return policyType;
	}

	public boolean hasPolicyData() {
		return policyData != null;
	}

	public String policyData() {
		return policyData;
	}
	
	public Element getPolicyDataEl() {
		return policyDataEl;
	}

	@Override
	public final int hashCode() {
		return Objects.hashCode(provisionStatus, policyStatus, policyKey, policyType, policyData);
	}
	
	@Override
	public final boolean equals(Object obj) {
		if (obj instanceof ProvisionResponse) {
			ProvisionResponse other = (ProvisionResponse) obj;
			return new EqualsBuilder()
				.append(provisionStatus, other.provisionStatus)
				.append(policyStatus, other.policyStatus)
				.append(policyKey, other.policyKey)
				.append(policyType, other.policyType)
				.append(policyData, other.policyData)
				.isEquals();
		}
		return false;
	}

}
