/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package org.obm.push.bean;

import java.io.Serializable;

import com.google.common.base.Objects;

public class MSAttachement implements Serializable {
	
	private static final long serialVersionUID = 3716693847098628406L;
	
	private String displayName;
	private String fileReference;
	private MethodAttachment method;
	private Integer estimatedDataSize;
	
	public MSAttachement(){
		method = MethodAttachment.NormalAttachment;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getFileReference() {
		return fileReference;
	}

	public void setFileReference(String fileReference) {
		this.fileReference = fileReference;
	}

	public MethodAttachment getMethod() {
		return method;
	}

	public void setMethod(MethodAttachment method) {
		this.method = method;
	}

	public Integer getEstimatedDataSize() {
		return estimatedDataSize;
	}

	public void setEstimatedDataSize(Integer estimatedDataSize) {
		this.estimatedDataSize = estimatedDataSize;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(displayName, fileReference, method, estimatedDataSize);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof MSAttachement) {
			MSAttachement that = (MSAttachement) object;
			return Objects.equal(this.displayName, that.displayName)
				&& Objects.equal(this.fileReference, that.fileReference)
				&& Objects.equal(this.method, that.method)
				&& Objects.equal(this.estimatedDataSize, that.estimatedDataSize);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("displayName", displayName)
			.add("fileReference", fileReference)
			.add("method", method)
			.add("estimatedDataSize", estimatedDataSize)
			.toString();
	}
	
}
