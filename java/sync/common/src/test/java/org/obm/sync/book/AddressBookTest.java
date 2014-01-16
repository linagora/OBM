/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2014  Linagora
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
package org.obm.sync.book;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.DateUtils;
import org.obm.filter.SlowFilterRunner;
import org.obm.sync.book.AddressBook.Id;

@RunWith(SlowFilterRunner.class)
public class AddressBookTest {

	@Test
	public void testBuild() {
		Date createDate = DateUtils.date("2013-07-26T12:00:00");
		Date updateDate = DateUtils.date("2013-07-27T12:00:00");
		AddressBook book = AddressBook
				.builder()
				.name("book")
				.uid(Id.valueOf(1))
				.readOnly(true)
				.syncable(true)
				.defaultBook(true)
				.origin("origin")
				.timecreate(createDate)
				.timeupdate(updateDate)
				.build();

		assertThat(book.getName()).isEqualTo("book");
		assertThat(book.getUid().getId()).isEqualTo(1);
		assertThat(book.isReadOnly()).isTrue();
		assertThat(book.isSyncable()).isTrue();
		assertThat(book.isDefaultBook()).isTrue();
		assertThat(book.getOrigin()).isEqualTo("origin");
		assertThat(book.getTimecreate()).isEqualTo(createDate);
		assertThat(book.getTimeupdate()).isEqualTo(updateDate);
	}

	@Test
	public void testBuildWithDefaultReadOnly() {
		AddressBook book = AddressBook
				.builder()
				.name("book")
				.uid(Id.valueOf(1))
				.build();

		assertThat(book.getName()).isEqualTo("book");
		assertThat(book.getUid().getId()).isEqualTo(1);
		assertThat(book.isReadOnly()).isFalse();
	}

	@Test(expected = IllegalStateException.class)
	public void testBuildWithNoName() {
		AddressBook
				.builder()
				.uid(Id.valueOf(1))
				.build();
	}

	@Test
	public void testBuildId() {
		Id id = Id.builder().id(1).build();

		assertThat(id.getId()).isEqualTo(1);
	}

	@Test
	public void testIdValueOfString() {
		Id id = Id.valueOf("1");

		assertThat(id.getId()).isEqualTo(1);
	}

	@Test
	public void testIdValueOf() {
		Id id = Id.valueOf(1);

		assertThat(id.getId()).isEqualTo(1);
	}

	@Test(expected = NumberFormatException.class)
	public void testIdValueOfWithNull() {
		Id.valueOf(null);
	}

	@Test(expected = NumberFormatException.class)
	public void testIdValueOfWithEmptyString() {
		Id.valueOf("");
	}

	@Test(expected = NumberFormatException.class)
	public void testIdValueOfWithNaN() {
		Id.valueOf("NaN");
	}

}
