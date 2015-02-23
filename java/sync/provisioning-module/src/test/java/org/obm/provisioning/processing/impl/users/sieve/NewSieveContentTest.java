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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;

import com.google.common.collect.ImmutableList;

public class NewSieveContentTest {

	@Test
	public void getAllRequiresShouldReturnOldRequiresIfPresent() {
		NewSieveContent sieveContent = new NewSieveContent(
				new OldSieveContent(
						ImmutableList.of("oldrequire1", "oldrequire2"),
						ImmutableList.of("oldwhatever")
				),
				ImmutableList.of(
						new ObmRule("name1", ImmutableList.of("newrequire1", "newrequire2"),
								ImmutableList.of("newwhatever1")),
						new ObmRule("name2", ImmutableList.of("newrequire3", "newrequire4"),
								ImmutableList.of("newwhatever2")),
						new ObmRule("name3", ImmutableList.of("newwhatever5"))
						)
				);
		assertThat(sieveContent.getAllRequires()).containsOnly("oldrequire1", "oldrequire2",
				"newrequire1", "newrequire2", "newrequire3", "newrequire4");
	}

	@Test
	public void getAllRequiresShouldHandleEmptyOldRequires() {
		ImmutableList<String> empty = ImmutableList.of();
		NewSieveContent sieveContent = new NewSieveContent(
				new OldSieveContent(
						empty,
						ImmutableList.of("oldwhatever")
				),
				ImmutableList.of(
						new ObmRule("name1", ImmutableList.of("newrequire1", "newrequire2"),
								ImmutableList.of("newwhatever1")),
						new ObmRule("name2", ImmutableList.of("newrequire3", "newrequire4"),
								ImmutableList.of("newwhatever2")),
						new ObmRule("name3", ImmutableList.of("newwhatever5"))
						)
				);
		assertThat(sieveContent.getAllRequires()).containsExactly(
				"newrequire1", "newrequire2", "newrequire3", "newrequire4");
	}

	@Test
	public void getAllRequiresShouldHandleEmptyObmRules() {
		ImmutableList<ObmRule> empty = ImmutableList.of();
		NewSieveContent sieveContent = new NewSieveContent(
				new OldSieveContent(
						ImmutableList.of("oldrequire1", "oldrequire2"),
						ImmutableList.of("oldwhatever")
				),
				empty);
		assertThat(sieveContent.getAllRequires()).containsExactly("oldrequire1", "oldrequire2");
	}

	@Test
	public void getAllRequiresShouldHandleAllEmpty() {
		ImmutableList<String> emptyOldRequires = ImmutableList.of();
		ImmutableList<ObmRule> emptyRules = ImmutableList.of();
		NewSieveContent sieveContent = new NewSieveContent(
				new OldSieveContent(
						emptyOldRequires,
						ImmutableList.of("oldwhatever")
				),
				emptyRules
				);
		assertThat(sieveContent.getAllRequires()).isEmpty();
	}

	@Test
	public void emptyNewSieveContentShouldBeEmpty() {
		ImmutableList<String> empty = ImmutableList.of();
		ImmutableList<ObmRule> emptyRules = ImmutableList.of();
		OldSieveContent emptyOldSieveContent = new OldSieveContent(empty, empty);
		NewSieveContent sieveContent = new NewSieveContent(
				emptyOldSieveContent,
				emptyRules);
		assertThat(sieveContent.isEmpty()).isTrue();
	}

	@Test
	public void newSieveContentWithNonEmptyOldSieveContentShouldNotBeEmpty() {
		ImmutableList<String> empty = ImmutableList.of();
		ImmutableList<ObmRule> emptyRules = ImmutableList.of();
		OldSieveContent oldSieveContent = new OldSieveContent(empty, ImmutableList.of("old rule;"));
		NewSieveContent sieveContent = new NewSieveContent(
				oldSieveContent,
				emptyRules);
		assertThat(sieveContent.isEmpty()).isFalse();
	}

	@Test
	public void newSieveContentWithNonEmptyRulesShouldNotBeEmpty() {
		ImmutableList<String> empty = ImmutableList.of();
		OldSieveContent emptyOldSieveContent = new OldSieveContent(empty, empty);
		NewSieveContent sieveContent = new NewSieveContent(
				emptyOldSieveContent,
				ImmutableList.of(new ObmRule("rule name", ImmutableList.of("rule content"))));
		assertThat(sieveContent.isEmpty()).isFalse();
	}
}
