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
import java.util.List;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public class SyncCollectionOptions implements Serializable {
	
	private static final long serialVersionUID = 7306997586579565585L;
	
	public static final Integer SYNC_TRUNCATION_ALL = 9;
	
	private Integer truncation;
	private Integer mimeSupport;
	private Integer mimeTruncation;
	private Integer conflict;
	private Boolean deletesAsMoves;
	private FilterType filterType;
	private List<BodyPreference> bodyPreferences;
	
	public SyncCollectionOptions(List<BodyPreference> bodyPreferences) {
		this.bodyPreferences = bodyPreferences;
		this.conflict = 1;
		this.truncation = SYNC_TRUNCATION_ALL;
		this.deletesAsMoves = true;
		this.filterType = FilterType.THREE_DAYS_BACK;
	}
	
	public SyncCollectionOptions() {
		this(Lists.<BodyPreference>newArrayList());
	}
	
	public Integer getConflict() {
		return conflict;
	}
	public void setConflict(Integer conflict) {
		this.conflict = conflict;
	}

	public Integer getTruncation() {
		return truncation;
	}

	public void setTruncation(Integer truncation) {
		this.truncation = truncation;
	}

	public Boolean isDeletesAsMoves() {
		return deletesAsMoves;
	}

	public void setDeletesAsMoves(Boolean deletesAsMoves) {
		if(deletesAsMoves != null){
			this.deletesAsMoves = deletesAsMoves;
		}
	}

	public FilterType getFilterType() {
		return filterType;
	}

	public void setFilterType(FilterType filterType) {
		this.filterType = filterType;
	}
	
	public Integer getMimeSupport() {
		return mimeSupport;
	}

	public void setMimeSupport(Integer mimeSupport) {
		this.mimeSupport = mimeSupport;
	}

	public Integer getMimeTruncation() {
		return mimeTruncation;
	}

	public void setMimeTruncation(Integer mimeTruncation) {
		this.mimeTruncation = mimeTruncation;
	}
	
	public void setBodyPreferences(List<BodyPreference> bodyPreferences) {
		this.bodyPreferences = bodyPreferences;
	}
	
	public List<BodyPreference> getBodyPreferences() {
		return bodyPreferences;
	}
	
	public void initTruncation() {
		setTruncation(null);
		Iterable<BodyPreference> bp = Iterables.transform(this.bodyPreferences, new Function<BodyPreference, BodyPreference>() {
			@Override
			public BodyPreference apply(BodyPreference input) {
				return BodyPreference.builder()
					.allOrNone(input.isAllOrNone())
					.bodyType(input.getType()).build();
			}
		});
		this.bodyPreferences = Lists.newArrayList(bp);
	}

	public static SyncCollectionOptions cloneOnlyByExistingFields(SyncCollectionOptions cloningFromOptions) {
		SyncCollectionOptions cloned = new SyncCollectionOptions();
		if (cloningFromOptions.getConflict() != null) {
			cloned.setConflict(cloningFromOptions.getConflict());
		}
		if (cloningFromOptions.getFilterType() != null) {
			cloned.setFilterType(cloningFromOptions.getFilterType());
		}
		if (cloningFromOptions.getMimeSupport() != null) {
			cloned.setMimeSupport(cloningFromOptions.getMimeSupport());
		}
		if (cloningFromOptions.getMimeTruncation() != null) {
			cloned.setMimeTruncation(cloningFromOptions.getMimeTruncation());
		}
		if (cloningFromOptions.getTruncation() != null) {
			cloned.setTruncation(cloningFromOptions.getTruncation());
		}
		if (cloningFromOptions.getBodyPreferences() != null) {
			cloned.setBodyPreferences(cloningFromOptions.getBodyPreferences());
		}
		return cloned;
	}
	
	@Override
	public final int hashCode(){
		return Objects.hashCode(truncation, mimeSupport, mimeTruncation, conflict, 
				deletesAsMoves, filterType, bodyPreferences);
	}
	
	@Override
	public final boolean equals(Object object){
		if (object instanceof SyncCollectionOptions) {
			SyncCollectionOptions that = (SyncCollectionOptions) object;
			return Objects.equal(this.truncation, that.truncation)
				&& Objects.equal(this.mimeSupport, that.mimeSupport)
				&& Objects.equal(this.mimeTruncation, that.mimeTruncation)
				&& Objects.equal(this.conflict, that.conflict)
				&& Objects.equal(this.deletesAsMoves, that.deletesAsMoves)
				&& Objects.equal(this.filterType, that.filterType)
				&& Objects.equal(this.bodyPreferences, that.bodyPreferences);
		}
		return false;
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this)
			.add("SYNC_TRUNCATION_ALL", SYNC_TRUNCATION_ALL)
			.add("truncation", truncation)
			.add("mimeSupport", mimeSupport)
			.add("mimeTruncation", mimeTruncation)
			.add("conflict", conflict)
			.add("deletesAsMoves", deletesAsMoves)
			.add("filterType", filterType)
			.add("bodyPreferences", bodyPreferences)
			.toString();
	}
}