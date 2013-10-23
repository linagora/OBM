/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2012  Linagora
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
package fr.aliacom.obm.common.domain;

import static org.fest.assertions.api.Assertions.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.SlowFilterRunner;

@RunWith(SlowFilterRunner.class)
public class ObmDomainTest {

	@Test(expected = NullPointerException.class)
	public void testBuildWhenNullAliasGiven() {
		ObmDomain.builder().alias(null).build();
	}
	
	@Test(expected = NullPointerException.class)
	public void testBuildWhenNullAliasesGiven() {
		ObmDomain.builder().aliases(null).build();
	}
	
	@Test
	public void testBuildWhenMultipleAliasGiven() {
		ObmDomain domain = ObmDomain.builder().alias("alias").alias("alias2").build();
		
		assertThat(domain.getAliases()).containsExactly("alias", "alias2");
	}

	@Test
	public void testGetNamesNoAlias() {
		ObmDomain domain = ObmDomain.builder().name("name").build();
		
		assertThat(domain.getNames()).containsExactly("name");
	}

	@Test
	public void testGetNamesOneAlias() {
		ObmDomain domain = ObmDomain.builder().name("name").alias("alias").build();
		
		assertThat(domain.getNames()).containsOnly("name", "alias");
	}

	@Test
	public void testGetNamesOneAliasSameThanName() {
		ObmDomain domain = ObmDomain.builder().name("name").alias("name").build();
		
		assertThat(domain.getNames()).containsOnly("name");
	}

	@Test
	public void testGetNamesThreeAlias() {
		ObmDomain domain = ObmDomain.builder()
				.name("name")
				.alias("alias1")
				.alias("alias2")
				.alias("alias3").build();
		
		assertThat(domain.getNames()).containsOnly("name", "alias1", "alias2", "alias3");
	}

	@Test
	public void testGetNamesThreeAliasOneSameThanName() {
		ObmDomain domain = ObmDomain.builder()
				.name("name")
				.alias("alias1")
				.alias("name")
				.alias("alias3").build();
		
		assertThat(domain.getNames()).containsOnly("name", "alias1", "alias3");
	}
}
