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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;

import fr.aliacom.obm.common.user.ObmUser;

public class SieveBuilder {

	private final ObmUser obmUser;

	private static final Logger logger = LoggerFactory.getLogger(SieveBuilder.class);
	private static final String NOMAD_RULE = "Nomade";
	private static final String[] KNOWN_RULES = { NOMAD_RULE };

	public SieveBuilder(ObmUser obmUser) {
		this.obmUser = obmUser;
	}

	public Optional<String> buildFromOldContent(String oldStringContent) {
		OldSieveContent oldContent = new SieveParser(oldStringContent).parse();
		ImmutableList<ObmRule> rules = this.buildRules();
		NewSieveContent content = new NewSieveContent(oldContent, rules);
		return asOptionalString(content);
	}

	public Optional<String> build() {
		ImmutableList<ObmRule> rules = this.buildRules();
		NewSieveContent content = new NewSieveContent(rules);
		return asOptionalString(content);
	}

	private Optional<String> asOptionalString(NewSieveContent content) {
		return content.isEmpty() ?
				Optional.<String>absent() :
				Optional.of(new SieveSerializer(content).serialize());
	}

	public static boolean isKnownRule(String ruleHeader) {
		for (String ruleName : KNOWN_RULES) {
			if (SieveSerializer.buildObmRuleHeader(ruleName).equals(ruleHeader)) {
				return true;
			}
		}
		return false;
	}

	private ImmutableList<ObmRule> buildRules() {
		ImmutableList.Builder<ObmRule> rulesBuilder = ImmutableList.builder();
		Optional<ObmRule> maybeRedirectRule = this.emailRedirectRule();
		if (maybeRedirectRule.isPresent()) {
			rulesBuilder.add(maybeRedirectRule.get());
		}
		return rulesBuilder.build();
	}

	private Optional<ObmRule> emailRedirectRule() {
		Optional<ObmRule> maybeRule;
		if (obmUser.getNomad().isEnabled() && !Strings.isNullOrEmpty(obmUser.getNomad().getEmail())) {
			ImmutableList.Builder<String> rulesBuilder = ImmutableList.builder();
			rulesBuilder.add(String
							.format("redirect \"%s\";", obmUser.getNomad().getEmail()));
			if (obmUser.getNomad().hasLocalCopy()) {
				rulesBuilder.add("keep;");
			}
			maybeRule = Optional.of(new ObmRule(NOMAD_RULE, rulesBuilder.build()));
		}
		else if (obmUser.getNomad().isEnabled()
				&& Strings.isNullOrEmpty(obmUser.getNomad().getEmail())) {
			logger.warn(
					"The user {} has nomad enabled but no redirection email configured, the redirection rule will not be applied",
					obmUser);
			maybeRule = Optional.absent();
		}
		else {
			maybeRule = Optional.absent();
		}
		return maybeRule;
	}
}
