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
package org.obm.push.bean;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import com.google.common.collect.ImmutableList;


public class SyncCollectionOptionsTest {

	@Test
	public void testCloneOnlyByExistingFieldsWhenFullOptions() {
		List<BodyPreference> bodyPreferences = ImmutableList.of(
			BodyPreference.builder()
				.bodyType(MSEmailBodyType.HTML)
				.truncationSize(100)
				.allOrNone(true)
				.build(),
			BodyPreference.builder()
				.bodyType(MSEmailBodyType.PlainText)
				.truncationSize(1000)
				.allOrNone(false)
				.build());

		SyncCollectionOptions cloningFromOptions = new SyncCollectionOptions();
		cloningFromOptions.setConflict(1);
		cloningFromOptions.setDeletesAsMoves(true);
		cloningFromOptions.setFilterType(FilterType.ONE_MONTHS_BACK);
		cloningFromOptions.setMimeSupport(5);
		cloningFromOptions.setMimeTruncation(2);
		cloningFromOptions.setTruncation(100);
		cloningFromOptions.setBodyPreferences(bodyPreferences);
		
		SyncCollectionOptions cloned = SyncCollectionOptions.cloneOnlyByExistingFields(cloningFromOptions);
		
		Assertions.assertThat(cloned).isEqualTo(cloningFromOptions);
	}

	@Test
	public void testCloneOnlyByExistingFieldsWhenNoConflict() {
		SyncCollectionOptions cloningFromOptions = new SyncCollectionOptions();
		cloningFromOptions.setConflict(null);
		
		SyncCollectionOptions cloned = SyncCollectionOptions.cloneOnlyByExistingFields(cloningFromOptions);
		
		Assertions.assertThat(cloned.getConflict()).isEqualTo(1);
	}

	@Test
	public void testCloneOnlyByExistingFieldsWhenNoDeleteAsMoved() {
		SyncCollectionOptions cloningFromOptions = new SyncCollectionOptions();
		cloningFromOptions.setDeletesAsMoves(null);
		
		SyncCollectionOptions cloned = SyncCollectionOptions.cloneOnlyByExistingFields(cloningFromOptions);
		
		Assertions.assertThat(cloned.isDeletesAsMoves()).isEqualTo(true);
	}

	@Test
	public void testCloneOnlyByExistingFieldsWhenNoFilterType() {
		SyncCollectionOptions cloningFromOptions = new SyncCollectionOptions();
		cloningFromOptions.setFilterType(null);
		
		SyncCollectionOptions cloned = SyncCollectionOptions.cloneOnlyByExistingFields(cloningFromOptions);
		
		Assertions.assertThat(cloned.getFilterType()).isEqualTo(FilterType.THREE_DAYS_BACK);
	}

	@Test
	public void testCloneOnlyByExistingFieldsWhenNoMimeSupport() {
		SyncCollectionOptions cloningFromOptions = new SyncCollectionOptions();
		cloningFromOptions.setMimeSupport(null);
		
		SyncCollectionOptions cloned = SyncCollectionOptions.cloneOnlyByExistingFields(cloningFromOptions);
		
		Assertions.assertThat(cloned.getMimeSupport()).isNull();
	}

	@Test
	public void testCloneOnlyByExistingFieldsWhenNoMimeTruncation() {
		SyncCollectionOptions cloningFromOptions = new SyncCollectionOptions();
		cloningFromOptions.setMimeTruncation(null);
		
		SyncCollectionOptions cloned = SyncCollectionOptions.cloneOnlyByExistingFields(cloningFromOptions);
		
		Assertions.assertThat(cloned.getMimeTruncation()).isNull();
	}

	@Test
	public void testCloneOnlyByExistingFieldsWhenNoTruncation() {
		SyncCollectionOptions cloningFromOptions = new SyncCollectionOptions();
		cloningFromOptions.setTruncation(null);
		
		SyncCollectionOptions cloned = SyncCollectionOptions.cloneOnlyByExistingFields(cloningFromOptions);
		
		Assertions.assertThat(cloned.getTruncation()).isEqualTo(9);
	}

	@Test
	public void testCloneOnlyByExistingFieldsWhenNoBodyPreference() {
		SyncCollectionOptions cloningFromOptions = new SyncCollectionOptions();
		cloningFromOptions.setBodyPreferences(null);
		
		SyncCollectionOptions cloned = SyncCollectionOptions.cloneOnlyByExistingFields(cloningFromOptions);
		
		Assertions.assertThat(cloned.getBodyPreferences()).isEmpty();
	}

	@Test
	public void testCloneOnlyByExistingFieldsWhenEmptyBodyPreference() {
		SyncCollectionOptions cloningFromOptions = new SyncCollectionOptions();
		cloningFromOptions.setBodyPreferences(ImmutableList.<BodyPreference>of());
		
		SyncCollectionOptions cloned = SyncCollectionOptions.cloneOnlyByExistingFields(cloningFromOptions);
		
		Assertions.assertThat(cloned.getBodyPreferences()).isEmpty();
	}
}
