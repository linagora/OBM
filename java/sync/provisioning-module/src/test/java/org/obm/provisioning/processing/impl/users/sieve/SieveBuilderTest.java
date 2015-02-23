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

import com.google.common.base.Joiner;
import com.google.common.base.Optional;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserLogin;
import fr.aliacom.obm.common.user.UserNomad;

public class SieveBuilderTest {

	@Test
	public void sieveBuilderShouldUpdateOldContentWithRedirectRule() {
		ObmUser user = ObmUser
				.builder()
				.uid(1)
				.login(UserLogin.valueOf("scipio.africanus"))
				.domain(ObmDomain.builder().name("rome.it").build())
				.nomad(UserNomad.builder().enabled(true).email("scipio.africanus@carthage.tn")
						.build())
				.build();
		String oldContent = "require \"foo\";\r\nold rule;";
		Optional<String> expected = Optional.of(Joiner.on("\r\n").join(new String[] {
				"require [\"foo\"];",
				"old rule;",
				"# rule:[OBM Nomade]",
				"redirect \"scipio.africanus@carthage.tn\";\r\n"
		}));
		assertThat(new SieveBuilder(user).buildFromOldContent(oldContent)).isEqualTo(expected);
	}

	@Test
	public void sieveBuilderShouldUpdateOldContentWithRedirectRuleEvenIfOldContentIsEmpty() {
		ObmUser user = ObmUser
				.builder()
				.uid(1)
				.login(UserLogin.valueOf("scipio.africanus"))
				.domain(ObmDomain.builder().name("rome.it").build())
				.nomad(UserNomad.builder().enabled(true).email("scipio.africanus@carthage.tn")
						.build())
				.build();
		String oldContent = "";
		Optional<String> expected = Optional.of(Joiner.on("\r\n").join(new String[] {
				"# rule:[OBM Nomade]",
				"redirect \"scipio.africanus@carthage.tn\";\r\n"
		}));
		assertThat(new SieveBuilder(user).buildFromOldContent(oldContent)).isEqualTo(expected);
	}

	@Test
	public void sieveBuilderShouldReturnAbsentIfNomadDisabledAndOldContentIsEmpty() {
		ObmUser user = ObmUser
				.builder()
				.uid(1)
				.login(UserLogin.valueOf("scipio.africanus"))
				.domain(ObmDomain.builder().name("rome.it").build())
				.nomad(UserNomad.builder().enabled(false).email("scipio.africanus@carthage.tn")
						.build())
				.build();
		String oldContent = "";
		Optional<String> expected = Optional.absent();
		assertThat(new SieveBuilder(user).buildFromOldContent(oldContent)).isEqualTo(expected);
	}

	@Test
	public void sieveBuilderShouldUpdateOldContentWithoutRedirectRuleIfNoEmail() {
		ObmUser user = ObmUser.builder()
				.uid(1)
				.login(UserLogin.valueOf("scipio.africanus"))
				.domain(ObmDomain.builder().name("rome.it").build())
				.nomad(UserNomad.builder().enabled(true).build())
				.build();
		String oldContent = "require \"foo\";\r\nold rule;";
		Optional<String> expected = Optional.of(Joiner.on("\r\n").join(new String[] {
				"require [\"foo\"];",
				"old rule;\r\n",
		}));
		assertThat(new SieveBuilder(user).buildFromOldContent(oldContent)).isEqualTo(expected);
	}

	@Test
	public void sieveBuilderShouldUpdateOldContentWithNomadDisabled() {
		ObmUser user = ObmUser
				.builder()
				.uid(1)
				.login(UserLogin.valueOf("scipio.africanus"))
				.domain(ObmDomain.builder().name("rome.it").build())
				.nomad(UserNomad.builder().enabled(false).email("scipio.africanus@carthage.tn")
						.build())
				.build();
		String oldContent = "require \"foo\";\r\nold rule;";
		Optional<String> expected = Optional.of(Joiner.on("\r\n").join(new String[] {
				"require [\"foo\"];",
				"old rule;\r\n",
		}));
		assertThat(new SieveBuilder(user).buildFromOldContent(oldContent)).isEqualTo(expected);
	}

	@Test
	public void sieveBuilderShouldUpdateOldContentWithNomadDisabledAlsoWithKeepRule() {
		ObmUser user = ObmUser.builder()
				.uid(1)
				.login(UserLogin.valueOf("scipio.africanus"))
				.domain(ObmDomain.builder().name("rome.it").build())
				.nomad(UserNomad.builder()
						.enabled(false)
						.email("scipio.africanus@carthage.tn")
						.localCopy(true)
						.build())
				.build();
		String oldContent = "require \"foo\";\r\nold rule;";
		Optional<String> expected = Optional.of(Joiner.on("\r\n").join(new String[] {
				"require [\"foo\"];",
				"old rule;\r\n",
		}));
		assertThat(new SieveBuilder(user).buildFromOldContent(oldContent)).isEqualTo(expected);
	}

	@Test
	public void sieveBuilderShouldBuildNewContentWithRedirectRule() {
		ObmUser user = ObmUser
				.builder()
				.uid(1)
				.login(UserLogin.valueOf("scipio.africanus"))
				.domain(ObmDomain.builder().name("rome.it").build())
				.nomad(UserNomad.builder().enabled(true).email("scipio.africanus@carthage.tn")
						.build())
				.build();
		Optional<String> expected = Optional.of(Joiner.on("\r\n").join(new String[] {
				"# rule:[OBM Nomade]",
				"redirect \"scipio.africanus@carthage.tn\";\r\n"
		}));
		assertThat(new SieveBuilder(user).build()).isEqualTo(expected);
	}

	@Test
	public void sieveBuilderShouldBuildNewContentWithKeepRule() {
		ObmUser user = ObmUser.builder()
				.uid(1)
				.login(UserLogin.valueOf("scipio.africanus"))
				.domain(ObmDomain.builder().name("rome.it").build())
				.nomad(UserNomad.builder()
						.enabled(true)
						.email("scipio.africanus@carthage.tn")
						.localCopy(true)
						.build())
				.build();
		Optional<String> expected = Optional.of(Joiner.on("\r\n").join(new String[] {
				"# rule:[OBM Nomade]",
				"redirect \"scipio.africanus@carthage.tn\";",
				"keep;\r\n"
		}));
		assertThat(new SieveBuilder(user).build()).isEqualTo(expected);
	}

	@Test
	public void sieveBuilderShouldUpdateOldContentWithKeepRuleIfRedirectPresent() {
		ObmUser user = ObmUser.builder()
				.uid(1)
				.login(UserLogin.valueOf("scipio.africanus"))
				.domain(ObmDomain.builder().name("rome.it").build())
				.nomad(UserNomad.builder()
						.enabled(true)
						.email("scipio.africanus@carthage.tn")
						.localCopy(true)
						.build())
				.build();
		String oldContent = "require \"foo\";\r\nold rule;";
		Optional<String> expected = Optional.of(Joiner.on("\r\n").join(new String[] {
				"require [\"foo\"];",
				"old rule;",
				"# rule:[OBM Nomade]",
				"redirect \"scipio.africanus@carthage.tn\";",
				"keep;\r\n"
		}));
		assertThat(new SieveBuilder(user).buildFromOldContent(oldContent)).isEqualTo(expected);
	}

	@Test
	public void sieveBuilderShouldNotAddRuleToOldContentIfEmailAddressIsMissing() {
		ObmUser user = ObmUser.builder()
				.uid(1)
				.login(UserLogin.valueOf("scipio.africanus"))
				.domain(ObmDomain.builder().name("rome.it").build())
				.nomad(UserNomad.builder()
						.enabled(true)
						.localCopy(true)
						.build())
				.build();
		String oldContent = "require \"foo\";\r\nold rule;";
		Optional<String> expected = Optional.of(Joiner.on("\r\n").join(new String[] {
				"require [\"foo\"];",
				"old rule;\r\n" }));
		assertThat(new SieveBuilder(user).buildFromOldContent(oldContent)).isEqualTo(expected);
	}

	@Test
	public void sieveBuilderShouldNotAddRuleToNewContentIfEmailAddressIsMissing() {
		ObmUser user = ObmUser.builder()
				.uid(1)
				.login(UserLogin.valueOf("scipio.africanus"))
				.domain(ObmDomain.builder().name("rome.it").build())
				.nomad(UserNomad.builder()
						.enabled(true)
						.localCopy(true)
						.build())
				.build();
		Optional<String> expected = Optional.absent();
		assertThat(new SieveBuilder(user).build()).isEqualTo(expected);
	}
}
