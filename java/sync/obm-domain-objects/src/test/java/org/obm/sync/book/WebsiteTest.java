/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2015 Linagora
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
package org.obm.sync.book;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;


public class WebsiteTest {
	
	@Test
	public void shouldNotBeEqualWhenDifferentLabels() {
		Website first = new Website("first", "url");
		Website second = new Website("second", "url");
		
		assertThat(first).isNotEqualTo(second);
		assertThat(first.hashCode()).isNotEqualTo(second.hashCode());
	}
	
	@Test
	public void shouldNotBeEqualWhenDifferentUrls() {
		Website first = new Website("same", "url1");
		Website second = new Website("same", "url2");
		
		assertThat(first).isNotEqualTo(second);
		assertThat(first.hashCode()).isNotEqualTo(second.hashCode());
	}
	
	@Test
	public void shouldNotBeEqualWhenNullLeftLabel() {
		Website first = new Website(null, "url");
		Website second = new Website("right", "url");
		
		assertThat(first).isNotEqualTo(second);
		assertThat(first.hashCode()).isNotEqualTo(second.hashCode());
	}
	
	@Test
	public void shouldNotBeEqualWhenNullRightLabel() {
		Website first = new Website("left", "url");
		Website second = new Website(null, "url");
		
		assertThat(first).isNotEqualTo(second);
		assertThat(first.hashCode()).isNotEqualTo(second.hashCode());
	}
	
	@Test
	public void shouldNotBeEqualWhenEmptyLeftLabel() {
		Website first = new Website("", "url");
		Website second = new Website(null, "url");
		
		assertThat(first).isNotEqualTo(second);
		assertThat(first.hashCode()).isEqualTo(second.hashCode());
	}
	
	@Test
	public void shouldNotBeEqualWhenEmptyRightLabel() {
		Website first = new Website(null, "url");
		Website second = new Website("", "url");
		
		assertThat(first).isNotEqualTo(second);
		assertThat(first.hashCode()).isEqualTo(second.hashCode());
	}

	@Test
	public void shouldBeEqualWhenNullLabels() {
		Website first = new Website(null, "url");
		Website second = new Website(null, "url");
		
		assertThat(first).isEqualTo(second);
		assertThat(first.hashCode()).isEqualTo(second.hashCode());
	}

	@Test
	public void shouldBeEqualWhenDifferentCaseLabels() {
		Website first = new Website("label", "url");
		Website second = new Website("LABEL", "url");
		
		assertThat(first).isEqualTo(second);
		assertThat(first.hashCode()).isEqualTo(second.hashCode());
	}
	
	@Test
	public void shouldBeEqualWhenSameCaseLabels() {
		Website first = new Website("label", "url");
		Website second = new Website("label", "url");
		
		assertThat(first).isEqualTo(second);
		assertThat(first.hashCode()).isEqualTo(second.hashCode());
	}
}
