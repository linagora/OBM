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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.obm.imap.sieve.SieveConstants;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

public class SieveParser {

	private final static Pattern REQUIRE_RX = Pattern
			.compile("\\s*require\\s+(?:\\[)?(.+?)(?:\\])?;");
	private final static Pattern NON_EMPTY_STRING_RX = Pattern.compile("\"([^\"]+)\"");
	private final static String GENERATED_RULE_START_MARK = "# rule";

	private final String scriptContent;
	private List<String> scriptLines;

	public SieveParser(String scriptContent) {
		this.scriptContent = scriptContent;
		this.scriptLines = null;
	}

	public OldSieveContent parse() {
		this.scriptLines = Splitter.on(SieveConstants.SPLIT_EXPR).omitEmptyStrings().splitToList(scriptContent);
		ImmutableList<String> requires = parseRequires();
		this.stripOBMRules();
		return new OldSieveContent(requires, ImmutableList.copyOf(scriptLines));
	}

	private ImmutableList<String> parseRequires() {
		ImmutableList.Builder<String> requiresBuilder = ImmutableList.builder();
		ImmutableList.Builder<String> scriptLinesWithoutRequiresBuilder = ImmutableList.builder();
		for (String line : scriptLines) {
			Matcher requireLineMatcher = REQUIRE_RX.matcher(line);
			if (requireLineMatcher.matches()) {
				String requiresString = requireLineMatcher.group(1);
				Iterable<String> requires = Iterables.transform(
						Splitter.on(',').split(requiresString),
						new Function<String, String>() {

							@Override
							public String apply(String require) {
								Matcher stringMatcher = NON_EMPTY_STRING_RX.matcher(require.trim());
								if (stringMatcher.matches()) {
									String stringContent = stringMatcher.group(1);
									return stringContent;
								}
								else {
									throw new IllegalArgumentException("Bad require: " + require);
								}
							}

						}
						);
				requiresBuilder.addAll(requires);
			}
			else {
				scriptLinesWithoutRequiresBuilder.add(line);
			}
		}
		this.scriptLines = scriptLinesWithoutRequiresBuilder.build();
		return requiresBuilder.build();
	}

	private void stripOBMRules() {
		ImmutableList.Builder<String> scriptLinesWithoutOBMRulesBuilder = ImmutableList.builder();
		boolean discard = false;
		for (String line : scriptLines) {
			if (line.startsWith(GENERATED_RULE_START_MARK)) {
				// Only discard known rules, they will be recreated later, keep
				// all OBM/Roundcube generated rules we don't know how to
				// process
				discard = SieveBuilder.isKnownRule(line.trim());
			}
			if (!discard) {
				scriptLinesWithoutOBMRulesBuilder.add(line);
			}
		}
		this.scriptLines = scriptLinesWithoutOBMRulesBuilder.build();
	}
}
