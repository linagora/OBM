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
package org.obm.push.backend;

import org.obm.push.bean.ICollectionPathHelper;
import org.obm.push.bean.PIMDataType;
import org.obm.push.bean.UserDataRequest;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.inject.Inject;

public class CollectionPath {

	public static class Builder {

		private final ICollectionPathHelper collectionPathHelper;
		private UserDataRequest userDataRequest;
		private PIMDataType pimType;
		private String backendName;
		private String fullyQualifiedCollectionPath;

		@Inject
		@VisibleForTesting Builder(ICollectionPathHelper collectionPathHelper) {
			this.collectionPathHelper = collectionPathHelper;
		}
		
		public Builder userDataRequest(UserDataRequest userDataRequest) {
			this.userDataRequest = userDataRequest;
			return this;
		}

		public Builder pimType(PIMDataType pimType) {
			this.pimType = pimType;
			return this;
		}
		
		public Builder backendName(String backendName) {
			this.backendName = backendName;
			return this;
		}
		
		public Builder fullyQualifiedCollectionPath(String fullyQualifiedCollectionPath) {
			this.fullyQualifiedCollectionPath = fullyQualifiedCollectionPath;
			return this;
		}
		
		public CollectionPath build() {
			Preconditions.checkState(userDataRequest != null);
			
			if (Strings.isNullOrEmpty(fullyQualifiedCollectionPath)) {
				Preconditions.checkState(pimType != null);
				Preconditions.checkState(!Strings.isNullOrEmpty(backendName));
				
				return buildFromPimType();
			} else {
				Preconditions.checkState(pimType == null, "Build either from pimType or fullyQualifiedCollectionPath");
				Preconditions.checkState(backendName == null, "Build either from backendName or fullyQualifiedCollectionPath");

				return buildFromFullyQualifiedCollectionPath();
			}
		}

		private CollectionPath buildFromPimType() {
			String collectionPath = collectionPathHelper.buildCollectionPath(userDataRequest, pimType, backendName);
			return new CollectionPath(collectionPath, pimType, backendName);
		}

		private CollectionPath buildFromFullyQualifiedCollectionPath() {
			PIMDataType recognizedPimType = collectionPathHelper.recognizePIMDataType(fullyQualifiedCollectionPath);
			if (recognizedPimType != PIMDataType.UNKNOWN) {
				return buildFromDiscoveredPimType(recognizedPimType);
			}
			return buildFromUnknownPimType();
		}
		
		private CollectionPath buildFromDiscoveredPimType(PIMDataType recognizedPimType) {
			String recognizedName = collectionPathHelper.extractFolder(userDataRequest, fullyQualifiedCollectionPath, recognizedPimType);
			return new CollectionPath(fullyQualifiedCollectionPath, recognizedPimType, recognizedName);
		}

		private CollectionPath buildFromUnknownPimType() {
			return new CollectionPath(fullyQualifiedCollectionPath, PIMDataType.UNKNOWN, null);
		}
	}

	private final String collectionPath;
	private final transient PIMDataType pimType;
	private final transient String backendName;
	
	@VisibleForTesting 
	protected CollectionPath(String collectionPath, PIMDataType pimType, String backendName) {
		this.collectionPath = collectionPath;
		this.pimType = pimType;
		this.backendName = backendName;
	}
	
	public PIMDataType pimType() {
		return pimType;
	}

	public String backendName() {
		return backendName;
	}

	public String collectionPath() {
		return collectionPath;
	}

	@Override
	public final int hashCode(){
		return Objects.hashCode(collectionPath);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof CollectionPath) {
			CollectionPath that = (CollectionPath) object;
			return Objects.equal(this.collectionPath, that.collectionPath);
		}
		return false;
	}

	@Override
	public final String toString() {
		return Objects.toStringHelper(this)
			.add("collectionPath", collectionPath)
			.toString();
	}

}
