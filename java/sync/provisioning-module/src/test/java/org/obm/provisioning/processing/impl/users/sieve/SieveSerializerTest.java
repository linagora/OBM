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

import org.junit.Test;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;

import static org.assertj.core.api.Assertions.assertThat;

public class SieveSerializerTest {

	@Test
	public void sieveSerializerShouldRemoveDuplicates() {
		NewSieveContent sieveContent = new NewSieveContent(
				new OldSieveContent(
						ImmutableList.of("old_require1", "old_require2", "old_and_new_require"),
						ImmutableList.of("old_rule1;", "old_rule2;")
				),
				ImmutableList.of(
						new ObmRule("first_rule_with_requires",
								ImmutableList.of("new_require1", "new_require2",
										"old_and_new_require"),
								ImmutableList.of("rule1_line1;", "rule1_line2;")),
						new ObmRule("rule_with_no_requires",
								ImmutableList.of("rule2_line1;", "rule2_line2;")),
						new ObmRule("second_rule_with_requires",
								ImmutableList.of("new_require3", "new_require4",
										"old_and_new_require"),
								ImmutableList.of("rule3_line1;", "rule3_line2;"))
						)
				);
		String expectedContent = Joiner
				.on('\n')
				.join(new String[] {
						"require [\"new_require1\", \"new_require2\", \"new_require3\", \"new_require4\", \"old_and_new_require\", \"old_require1\", \"old_require2\"];",
						"old_rule1;",
						"old_rule2;",
						"# rule:[OBM first_rule_with_requires]",
						"rule1_line1;",
						"rule1_line2;",
						"# rule:[OBM rule_with_no_requires]",
						"rule2_line1;",
						"rule2_line2;",
						"# rule:[OBM second_rule_with_requires]",
						"rule3_line1;",
						"rule3_line2;\n",
				});
		assertThat(new SieveSerializer(sieveContent).serialize()).isEqualTo(expectedContent);
	}

	@Test
	public void sieveSerializerShouldNotSerializeMissingOldContent() {
		NewSieveContent sieveContent = new NewSieveContent(
				ImmutableList.of(
						new ObmRule("first_rule_with_requires",
								ImmutableList.of("new_require1", "new_require2",
										"duplicate_require"),
								ImmutableList.of("rule1_line1;", "rule1_line2;")),
						new ObmRule("rule_with_no_requires",
								ImmutableList.of("rule2_line1;", "rule2_line2;")),
						new ObmRule("second_rule_with_requires",
								ImmutableList.of("new_require3", "new_require4",
										"duplicate_require"),
								ImmutableList.of("rule3_line1;", "rule3_line2;"))
						)
				);
		String expectedContent = Joiner
				.on('\n')
				.join(new String[] {
						"require [\"duplicate_require\", \"new_require1\", \"new_require2\", \"new_require3\", \"new_require4\"];",
						"# rule:[OBM first_rule_with_requires]",
						"rule1_line1;",
						"rule1_line2;",
						"# rule:[OBM rule_with_no_requires]",
						"rule2_line1;",
						"rule2_line2;",
						"# rule:[OBM second_rule_with_requires]",
						"rule3_line1;",
						"rule3_line2;\n",
				});
		assertThat(new SieveSerializer(sieveContent).serialize()).isEqualTo(expectedContent);
	}

	@Test
	public void sieveSerializerShouldNotSerializeEmptyObmRules() {
		List<ObmRule> empty = ImmutableList.of();
		NewSieveContent sieveContent = new NewSieveContent(
				new OldSieveContent(
						ImmutableList.of("old_require1", "duplicate_require", "duplicate_require", "old_require2"),
						ImmutableList.of("old_rule1;", "old_rule2;")
				),
				empty);
		String expectedContent = Joiner
				.on('\n')
				.join(new String[] {
						"require [\"duplicate_require\", \"old_require1\", \"old_require2\"];",
						"old_rule1;",
						"old_rule2;\n",
				});
		assertThat(new SieveSerializer(sieveContent).serialize()).isEqualTo(expectedContent);
	}

	@Test
	public void sieveSerializerShouldNotPrintRequireLineIfNoRequires() {
		List<String> empty = ImmutableList.of();
		NewSieveContent sieveContent = new NewSieveContent(
				new OldSieveContent(
						empty,
						ImmutableList.of("old_rule1;", "old_rule2;")
				),
				ImmutableList.of(
						new ObmRule("first_rule",
								ImmutableList.of("rule1_line1;", "rule1_line2;")),
						new ObmRule("second_rule",
								ImmutableList.of("rule2_line1;", "rule2_line2;")),
						new ObmRule("third_rule",
								ImmutableList.of("rule3_line1;", "rule3_line2;"))
						)
				);
		String expectedContent = Joiner
				.on('\n')
				.join(new String[] {
						"old_rule1;",
						"old_rule2;",
						"# rule:[OBM first_rule]",
						"rule1_line1;",
						"rule1_line2;",
						"# rule:[OBM second_rule]",
						"rule2_line1;",
						"rule2_line2;",
						"# rule:[OBM third_rule]",
						"rule3_line1;",
						"rule3_line2;\n",
				});
		assertThat(new SieveSerializer(sieveContent).serialize()).isEqualTo(expectedContent);
	}
}
