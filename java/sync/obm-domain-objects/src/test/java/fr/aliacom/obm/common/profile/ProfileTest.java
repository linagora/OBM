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
package fr.aliacom.obm.common.profile;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Date;

import org.junit.Test;
import org.obm.DateUtils;
import org.obm.provisioning.ProfileId;
import org.obm.provisioning.ProfileName;
import org.obm.sync.Right;

import fr.aliacom.obm.common.domain.Domains;
import fr.aliacom.obm.common.profile.Profile.AccessRestriction;
import fr.aliacom.obm.common.profile.Profile.AdminRealm;


public class ProfileTest {

	private final Date date = DateUtils.date("2013-08-01T12:00:00");

	@Test
	public void testBuild() {
		Profile profile = Profile
				.builder()
				.id(ProfileId.valueOf("1"))
				.name(ProfileName.valueOf("profile"))
				.domain(Domains.linagora)
				.level(0)
				.managePeers(true)
				.adminRealms(AdminRealm.USER, AdminRealm.DOMAIN)
				.accessRestriction(AccessRestriction.DENY_ALL)
				.accessExceptions("usera")
				.defaultMailQuota(10)
				.maxMailQuota(50)
				.defaultCheckBoxState(Module.CALENDAR, ModuleCheckBoxStates
						.builder()
						.module(Module.CALENDAR)
						.checkBoxState(Right.ACCESS, CheckBoxState.CHECKED)
						.build())
				.timecreate(date)
				.timeupdate(date)
				.build();

		assertThat(profile.getId().getId()).isEqualTo(1);
		assertThat(profile.getName().getName()).isEqualTo("profile");
		assertThat(profile.getDomain()).isEqualTo(Domains.linagora);
		assertThat(profile.getTimecreate()).isEqualTo(date);
		assertThat(profile.getTimeupdate()).isEqualTo(date);
		assertThat(profile.getLevel()).isEqualTo(0);
		assertThat(profile.isManagePeers()).isTrue();
		assertThat(profile.getAdminRealms()).containsOnly(AdminRealm.USER, AdminRealm.DOMAIN);
		assertThat(profile.getAccessRestriction()).isEqualTo(AccessRestriction.DENY_ALL);
		assertThat(profile.getAccessExceptions()).isEqualTo("usera");
		assertThat(profile.getDefaultMailQuota()).isEqualTo(10);
		assertThat(profile.getMaxMailQuota()).isEqualTo(50);
		assertThat(profile.getDefaultCheckBoxStates().get(Module.CALENDAR).getCheckBoxState(Right.ACCESS)).isEqualTo(CheckBoxState.CHECKED);
	}

	@Test(expected = IllegalStateException.class)
	public void testBuildWithNoId() {
		Profile
			.builder()
			.name(ProfileName.valueOf("profile"))
			.domain(Domains.linagora)
			.level(0)
			.timecreate(date)
			.timeupdate(date)
			.build();
	}

	@Test(expected = IllegalStateException.class)
	public void testBuildWithNoName() {
		Profile
			.builder()
			.id(ProfileId.valueOf("1"))
			.domain(Domains.linagora)
			.level(0)
			.timecreate(date)
			.timeupdate(date)
			.build();
	}

	@Test(expected = IllegalStateException.class)
	public void testBuildWithNoDomain() {
		Profile
			.builder()
			.id(ProfileId.valueOf("1"))
			.name(ProfileName.valueOf("profile"))
			.level(0)
			.timecreate(date)
			.timeupdate(date)
			.build();
	}

	@Test(expected = IllegalStateException.class)
	public void testBuildWithNoLevel() {
		Profile
			.builder()
			.id(ProfileId.valueOf("1"))
			.name(ProfileName.valueOf("profile"))
			.domain(Domains.linagora)
			.timecreate(date)
			.timeupdate(date)
			.build();
	}

}
