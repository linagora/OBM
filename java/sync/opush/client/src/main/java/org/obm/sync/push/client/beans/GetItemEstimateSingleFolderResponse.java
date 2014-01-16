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
package org.obm.sync.push.client.beans;

import org.obm.push.utils.DOMUtils;
import org.obm.sync.push.client.IEasReponse;
import org.w3c.dom.Element;

import org.obm.push.bean.GetItemEstimateStatus;
import org.obm.push.bean.SyncKey;

import com.google.common.base.Objects;


public final class GetItemEstimateSingleFolderResponse implements IEasReponse {

	public static class XmlParser {
		public GetItemEstimateSingleFolderResponse parse(Element root) {
			Integer colId = DOMUtils.getElementInteger(DOMUtils.getUniqueElement(root, "CollectionId"));
			Integer estimate = DOMUtils.getElementInteger(DOMUtils.getUniqueElement(root, "Estimate"));
			Integer statusInteger = DOMUtils.getElementInteger(DOMUtils.getUniqueElement(root, "Status"));
			GetItemEstimateStatus status = GetItemEstimateStatus.fromSpecificationValue(statusInteger);
			return new GetItemEstimateSingleFolderResponse(colId, estimate, status);
		}
	}
	
	private final Integer collectionId;
	private final Integer estimate;
	private final GetItemEstimateStatus status;

	public GetItemEstimateSingleFolderResponse(Integer collectionId, Integer estimate, GetItemEstimateStatus status) {
		this.collectionId = collectionId;
		this.estimate = estimate;
		this.status = status;
	}
	
	@Override
	public SyncKey getReturnedSyncKey() {
		throw new IllegalAccessError("GetItemEstimate's response has no SyncKey");
	}

	public Integer getCollectionId() {
		return collectionId;
	}

	public Integer getEstimate() {
		return estimate;
	}

	public GetItemEstimateStatus getStatus() {
		return status;
	}

	@Override
	public int hashCode(){
		return Objects.hashCode(collectionId, estimate, status);
	}
	
	@Override
	public boolean equals(Object object){
		if (object instanceof GetItemEstimateSingleFolderResponse) {
			GetItemEstimateSingleFolderResponse that = (GetItemEstimateSingleFolderResponse) object;
			return Objects.equal(this.collectionId, that.collectionId)
				&& Objects.equal(this.estimate, that.estimate)
				&& Objects.equal(this.status, that.status);
		}
		return false;
	}
	
	
}
