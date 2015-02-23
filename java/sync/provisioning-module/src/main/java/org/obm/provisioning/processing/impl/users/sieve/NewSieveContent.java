/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2015  Linagora
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version, provided you comply with the Additional Terms applicable for OBM
 * software by Linagora pursuant to Section 7 of the GNU Affero General Public
 * License, subsections (b), (c), and (e), pursuant to which you must notably (i)
 * retain the displaying by the interactive user interfaces of the “OBM, Free
 * Communication by Linagora” Logo with the “You are using the Open Source and
 * free version of OBM developed and supported by Linagora. Contribute to OBM R&D
 * by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
 * links between OBM and obm.org, between Linagora and linagora.com, as well as
 * between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
 * from infringing Linagora intellectual property rights over its trademarks and
 * commercial brands. Other Additional Terms apply, see
 * <http://www.linagora.com/licenses/> for more details.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License and
 * its applicable Additional Terms for OBM along with this program. If not, see
 * <http://www.gnu.org/licenses/> for the GNU Affero General   Public License
 * version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
 * applicable to the OBM software.
 * ***** END LICENSE BLOCK ***** */
package org.obm.provisioning.processing.impl.users.sieve;

import java.util.List;
import java.util.Set;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSortedSet;

public class NewSieveContent {

	private final Optional<OldSieveContent> maybeOldSieveContent;
	private final ImmutableList<ObmRule> obmRules;

	public NewSieveContent(ImmutableList<ObmRule> obmRules) {
		this.maybeOldSieveContent = Optional.absent();
		this.obmRules = obmRules;
	}

	public NewSieveContent(OldSieveContent oldSieveContent, ImmutableList<ObmRule> obmRules) {
		this.maybeOldSieveContent = Optional.of(oldSieveContent);
		this.obmRules = obmRules;
	}

	public Optional<OldSieveContent> getMaybeOldSieveContent() {
		return this.maybeOldSieveContent;
	}

	public List<ObmRule> getObmRules() {
		return this.obmRules;
	}

	public Set<String> getAllRequires() {
		// The 'ordered' part is only useful for testing the serializer
		ImmutableSortedSet.Builder<String> requiresBuilder = ImmutableSortedSet.naturalOrder();
		Optional<OldSieveContent> oldSieveContent = this.getMaybeOldSieveContent();
		if (oldSieveContent.isPresent()) {
			requiresBuilder.addAll(oldSieveContent.get().getRequires());
		}
		for (ObmRule obmRule : this.getObmRules()) {
			requiresBuilder.addAll(obmRule.getRequires());
		}
		return requiresBuilder.build();
	}

	public boolean isEmpty() {
		boolean isOldContentEmpty = this.maybeOldSieveContent.isPresent() ?
				this.maybeOldSieveContent.get().isEmpty() : true;
		return isOldContentEmpty && this.obmRules.isEmpty();
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof NewSieveContent)) {
			return false;
		}
		NewSieveContent other = (NewSieveContent) o;
		return this == other || (
				Objects.equal(this.maybeOldSieveContent, other.maybeOldSieveContent)
				&& Objects.equal(this.obmRules, other.obmRules));
	}

	@Override
	public int hashCode() {
		return Objects.hashCode(
				this.maybeOldSieveContent,
				this.obmRules);
	}
}
