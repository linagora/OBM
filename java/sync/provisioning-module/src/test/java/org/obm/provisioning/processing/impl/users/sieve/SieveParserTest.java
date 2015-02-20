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
import com.google.common.collect.ImmutableList;

public class SieveParserTest {

	@Test
	public void sieveParserShouldParse() {
		String content = Joiner.on("\r\n").join(new String[] {
				"require [\"require1\", \"require2\"];",
				"rule1;",
				"rule2;",
				"rule3;"
		});
		OldSieveContent expected = new OldSieveContent(
				ImmutableList.of("require1", "require2"),
				ImmutableList.of("rule1;", "rule2;", "rule3;"));
		assertThat(new SieveParser(content).parse()).isEqualTo(expected);
	}

	@Test
	public void sieveParserShouldEatKnownObmRules() {
		String content = Joiner.on("\r\n").join(new String[] {
				"require [\"require1\", \"require2\"];",
				"rule1;",
				"rule2;",
				"rule3;",
				"# rule:[OBM obm_rule_1]",
				"obm rule 1;",
				"# rule:[OBM Nomade]",
				"obm rule nomade;",
				"# rule:[OBM obm_rule_2]",
				"obm rule 2;",
				"obm rule 3;",
				"# rule:RoundcubeRule",
				"roundcube rule 1;",
				"roundcube rule 2;"
		});
		OldSieveContent expected = new OldSieveContent(
				ImmutableList.of("require1", "require2"),
				ImmutableList.of(
						"rule1;",
						"rule2;",
						"rule3;",
						"# rule:[OBM obm_rule_1]",
						"obm rule 1;",
						"# rule:[OBM obm_rule_2]",
						"obm rule 2;",
						"obm rule 3;",
						"# rule:RoundcubeRule",
						"roundcube rule 1;",
						"roundcube rule 2;"
						));
		assertThat(new SieveParser(content).parse()).isEqualTo(expected);
	}

	@Test
	public void sieveParserShouldEatKnownObmRulesEvenWhenSplitOnlyWithLF() {
		String content = Joiner.on("\n").join(new String[] {
				"require [\"require1\", \"require2\"];",
				"rule1;",
				"rule2;",
				"rule3;",
				"# rule:[OBM obm_rule_1]",
				"obm rule 1;",
				"# rule:[OBM Nomade]",
				"obm rule nomade;",
				"# rule:[OBM obm_rule_2]",
				"obm rule 2;",
				"obm rule 3;",
				"# rule:RoundcubeRule",
				"roundcube rule 1;",
				"roundcube rule 2;"
		});
		OldSieveContent expected = new OldSieveContent(
				ImmutableList.of("require1", "require2"),
				ImmutableList.of(
						"rule1;",
						"rule2;",
						"rule3;",
						"# rule:[OBM obm_rule_1]",
						"obm rule 1;",
						"# rule:[OBM obm_rule_2]",
						"obm rule 2;",
						"obm rule 3;",
						"# rule:RoundcubeRule",
						"roundcube rule 1;",
						"roundcube rule 2;"
						));
		assertThat(new SieveParser(content).parse()).isEqualTo(expected);
	}


	@Test
	public void sieveParserShouldParseSingleRequire() {
		String content = Joiner.on("\r\n").join(new String[] {
				"require \"require1\";",
				"rule1;",
				"rule2;",
				"rule3;"
		});
		OldSieveContent expected = new OldSieveContent(
				ImmutableList.of("require1"),
				ImmutableList.of("rule1;", "rule2;", "rule3;"));
		assertThat(new SieveParser(content).parse()).isEqualTo(expected);
	}

	@Test
	public void sieveParserShouldEatEmptyLines() {
		String content = Joiner.on("\r\n").join(new String[] {
				"require \"require1\";",
				"rule1;",
				"",
				"rule2;",
				"",
				"rule3;",
				""
		});
		OldSieveContent expected = new OldSieveContent(
				ImmutableList.of("require1"),
				ImmutableList.of("rule1;", "rule2;", "rule3;"));
		assertThat(new SieveParser(content).parse()).isEqualTo(expected);
	}

	@Test
	public void sieveParserShouldParseWhenNoRequire() {
		String content = Joiner.on("\r\n").join(new String[] {
				"rule1;",
				"rule2;",
				"rule3;"
		});
		ImmutableList<String> empty = ImmutableList.of();
		OldSieveContent expected = new OldSieveContent(
				empty,
				ImmutableList.of("rule1;", "rule2;", "rule3;"));
		assertThat(new SieveParser(content).parse()).isEqualTo(expected);
	}

	@Test
	public void sieveParserShouldParseWhenNoRules() {
		String content = "require \"require1\";";
		ImmutableList<String> empty = ImmutableList.of();
		OldSieveContent expected = new OldSieveContent(
				ImmutableList.of("require1"),
				empty);
		assertThat(new SieveParser(content).parse()).isEqualTo(expected);
	}
}
