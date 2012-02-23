/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
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
package fr.aliacom.obm;

import java.util.Locale;
import java.util.TimeZone;

import org.easymock.EasyMock;
import org.obm.icalendar.Ical4jUser;
import org.obm.sync.auth.AccessToken;

import com.linagora.obm.sync.Producer;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.setting.SettingsService;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserSettings;

public class ToolBox {

	public static AccessToken mockAccessToken() {
		ObmUser user = getDefaultObmUser();
		return mockAccessToken(user.getLogin(), user.getDomain());
	}
	
	public static AccessToken mockAccessToken(String login, ObmDomain domain) {
		AccessToken accessToken = EasyMock.createMock(AccessToken.class);
		EasyMock.expect(accessToken.getDomain()).andReturn(domain).atLeastOnce();
		EasyMock.expect(accessToken.getUserLogin()).andReturn(login).anyTimes();
		EasyMock.expect(accessToken.getOrigin()).andReturn("unittest").anyTimes();
		EasyMock.expect(accessToken.getConversationUid()).andReturn(1).anyTimes();
		return accessToken;
	}

	public static ObmUser mockObmUser(String userEmail, ObmDomain domain) {
		ObmUser user = EasyMock.createMock(ObmUser.class);
		EasyMock.expect(user.getEmail()).andReturn(userEmail).atLeastOnce();
		EasyMock.expect(user.getDomain()).andReturn(domain).anyTimes();
		return user;
	}
	
	public static ObmDomain getDefaultObmDomain() {
		ObmDomain obmDomain = new ObmDomain();
		obmDomain.setName("test.tlse.lng");
		obmDomain.setUuid("ac21bc0c-f816-4c52-8bb9-e50cfbfec5b6");
		return obmDomain;
	}
	
	public static ObmUser getDefaultObmUser(){
		ObmDomain obmDomain = getDefaultObmDomain();
		ObmUser obmUser = new ObmUser();
		obmUser.setFirstName("Obm");
		obmUser.setLastName("User");
		obmUser.setLogin("user");
		obmUser.setEmail("user@test");
		obmUser.setDomain(obmDomain);
		return obmUser;
	}
	
	public static Ical4jUser getIcal4jUser() {
		ObmUser obmUser = getDefaultObmUser();
		return getIcal4jUser(obmUser);
	}
	
	public static Ical4jUser getIcal4jUser(ObmUser obmUser) {
		return Ical4jUser.Factory.create().createIcal4jUser(obmUser.getEmail(), obmUser.getDomain());
	}
	
	public static UserSettings getDefaultSettings() {
		UserSettings settings = EasyMock.createMock(UserSettings.class);
		EasyMock.expect(settings.locale()).andReturn(Locale.FRENCH).anyTimes();
		EasyMock.expect(settings.timezone()).andReturn(TimeZone.getTimeZone("Europe/Paris")).anyTimes();
		return settings;
	}
	
	public static SettingsService getDefaultSettingsService() {
		UserSettings defaultSettings = getDefaultSettings();
		SettingsService service = EasyMock.createMock(SettingsService.class);
		service.getSettings(EasyMock.anyObject(ObmUser.class));
		EasyMock.expectLastCall().andReturn(defaultSettings).anyTimes();
		EasyMock.replay(defaultSettings);
		return service;
	}
	
	public static Producer getDefaultProducer() {
		return EasyMock.createMock(Producer.class);
	}
	
}
