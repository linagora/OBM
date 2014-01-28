/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2013-2014  Linagora
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
package org.obm.push.store.ehcache;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.obm.push.store.ehcache.EhCacheConfiguration.Percentage;


public class EhCacheConfigurationTest {

	@Test
	public void testPercentageWhenUndefined() {
		assertThat(Percentage.UNDEFINED.isDefined()).isFalse();
	}
	
	@Test(expected=IllegalStateException.class)
	public void testPercentageGetWhenUndefined() {
		assertThat(Percentage.UNDEFINED.get());
	}
	
	@Test
	public void testPercentageWhenZero() {
		assertThat(Percentage.of(0).isDefined()).isTrue();
		assertThat(Percentage.of(0).get()).isEqualTo("0%");
	}
	
	@Test
	public void testPercentageWhenFiftyFive() {
		assertThat(Percentage.of(55).isDefined()).isTrue();
		assertThat(Percentage.of(55).get()).isEqualTo("55%");
	}
	
	@Test
	public void testPercentageGetWhenOneHundred() {
		assertThat(Percentage.of(100).get()).isEqualTo("100%");
	}
	
	@Test(expected=IllegalStateException.class)
	public void testCalculateOnUndefined() {
		Percentage.UNDEFINED.applyTo(5);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCalculateToNegative() {
		Percentage.of(50).applyTo(-1);
	}

	@Test
	public void testCalculateToZero() {
		assertThat(Percentage.of(25).applyTo(0)).isEqualTo(0);
	}

	@Test
	public void testCalculateWhenZero() {
		assertThat(Percentage.of(0).applyTo(5000)).isEqualTo(0);
	}

	@Test
	public void testCalculateWhenQuart() {
		assertThat(Percentage.of(25).applyTo(1000)).isEqualTo(250);
	}

	@Test
	public void testCalculateWhenRoundedDown() {
		assertThat(Percentage.of(1).applyTo(10)).isEqualTo(0);
	}

	@Test
	public void testCalculateWhenRoundedUp() {
		assertThat(Percentage.of(99).applyTo(10)).isEqualTo(10);
	}
	
	@Test(expected=IllegalStateException.class)
	public void testPercentageGetIntValueWhenUndefined() {
		assertThat(Percentage.UNDEFINED.getIntValue());
	}
	
	@Test
	public void testPercentageGetIntValueWhenZero() {
		assertThat(Percentage.of(0).isDefined()).isTrue();
		assertThat(Percentage.of(0).getIntValue()).isEqualTo(0);
	}
	
	@Test
	public void testPercentageGetIntValueWhenFiftyFive() {
		assertThat(Percentage.of(55).isDefined()).isTrue();
		assertThat(Percentage.of(55).getIntValue()).isEqualTo(55);
	}
	
	@Test
	public void testPercentageGetIntValueGetWhenOneHundred() {
		assertThat(Percentage.of(100).getIntValue()).isEqualTo(100);
	}
}
