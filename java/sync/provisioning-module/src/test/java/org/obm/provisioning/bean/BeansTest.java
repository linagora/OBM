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
package org.obm.provisioning.bean;

import java.util.List;

import org.junit.Test;
import org.obm.provisioning.processing.impl.users.sieve.NewSieveContent;
import org.obm.provisioning.processing.impl.users.sieve.ObmRule;
import org.obm.provisioning.processing.impl.users.sieve.OldSieveContent;
import org.obm.sync.bean.EqualsVerifierUtils;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

public class BeansTest {

	@Test
	public void test() {
		List<String> requires1 = ImmutableList.of("foo1", "bar1");
		List<String> rules1 = ImmutableList.of("rule1;");
		List<String> requires2 = ImmutableList.of("foo2", "bar2");
		List<String> rules2 = ImmutableList.of("rule2;");
		OldSieveContent oldSieveContent1 = new OldSieveContent(requires1, rules1);
		OldSieveContent oldSieveContent2 = new OldSieveContent(requires2, rules2);

		ImmutableList<Class<?>> list =
				ImmutableList.<Class<?>> builder()
						.add(UserIdentifier.class)
						.add(GroupIdentifier.class)
						.add(OldSieveContent.class)
						.add(ObmRule.class)
						.add(NewSieveContent.class)
						.build();
		EqualsVerifierUtils.EqualsVerifierBuilder
				.builder()
				.prefabValue(Optional.class,
						Optional.of(oldSieveContent1),
						Optional.of(oldSieveContent2))
				.equalsVerifiers(list)
				.verify();
	}

}
