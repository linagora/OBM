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
package org.obm.sync.client.setting;

import java.util.Date;
import java.util.Map;

import org.obm.sync.ObmSyncTestCase;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.book.Contact;
import org.obm.sync.calendar.Event;
import org.obm.sync.locators.SettingLocator;
import org.obm.sync.setting.ForwardingSettings;
import org.obm.sync.setting.VacationSettings;

public class SettingClientTests extends ObmSyncTestCase {

	protected SettingClient settingClient;
	protected AccessToken token;

	public void testGetSettings() {
		try {
			Map<String, String> ret = settingClient.getSettings(token);
			assertNotNull(ret);
			assertTrue(ret.size() > 0);

		} catch (Exception e) {
			e.printStackTrace();
			fail("error on getSetting");
		}
	}

	public void testSetVacation() {
		try {
			VacationSettings vs = new VacationSettings();
			vs.setEnabled(true);
			vs.setStart(new Date());
			vs.setEnd(new Date());
			vs.setText("Soirée olive et kinder");

			settingClient.setVacationSettings(token, vs);

		} catch (Exception e) {
			e.printStackTrace();
			fail("error on setVacation");
		}
	}

	public void testGetVacation() {
		try {
			VacationSettings vs = settingClient.getVacationSettings(token);
			assertNotNull(vs);
			System.out.println("vs.enabled: " + vs.isEnabled() + " s: "
					+ vs.getStart() + " e: " + vs.getEnd() + " t: "
					+ vs.getText());
		} catch (Exception e) {
			e.printStackTrace();
			fail("error on getVacation");
		}
	}

	public void testSetEmailForwarding() {
		try {
			ForwardingSettings fs = new ForwardingSettings();
			fs.setEnabled(true);
			fs.setEmail("tcataldo@gmail.com");
			settingClient.setEmailForwarding(token, fs);
		} catch (Exception e) {
			e.printStackTrace();
			fail("error on setEmailForwarding");
		}
	}

	public void testGetEmailForwarding() {
		try {
			ForwardingSettings fs = settingClient.getEmailForwarding(token);
			assertNotNull(fs);
			System.out.println("ena: " + fs.isEnabled() + " email: "
					+ fs.getEmail());
		} catch (Exception e) {
			e.printStackTrace();
			fail("error on getEmailForwarding");
		}
	}

	public void testGetEmailForwardingLoop() {
		int COUNT = 1000;
		long time = System.currentTimeMillis();
		try {
			for (int i = 0; i < COUNT; i++) {
				settingClient.getEmailForwarding(token);
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("error on getEmailForwarding");
		}
		time = System.currentTimeMillis() - time;
		System.err.println(COUNT + " getEmailForwarding done in " + time
				+ "ms.");
	}

	public void testGetVacationSettingsLoop() {
		int COUNT = 1000;
		long time = System.currentTimeMillis();
		try {
			for (int i = 0; i < COUNT; i++) {
				settingClient.getVacationSettings(token);
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("error on getVacationSettings");
		}
		time = System.currentTimeMillis() - time;
		System.err.println(COUNT + " getVacationSettings done in " + time
				+ "ms.");
	}

	public void testGetSettingsLoop() {
		int COUNT = 1000;
		long time = System.currentTimeMillis();
		try {
			for (int i = 0; i < COUNT; i++) {
				settingClient.getSettings(token);
			}
		} catch (Exception e) {
			e.printStackTrace();
			fail("error on getSetting");
		}
		time = System.currentTimeMillis() - time;
		System.err.println(COUNT + " getSettings done in " + time + "ms.");
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		settingClient = SettingLocator.locate(p("obm.sync.url"));
		assertNotNull(settingClient);
		token = settingClient.login(p("login"), p("password"), "junit");
		assertNotNull(token);
	}

	@Override
	protected void tearDown() throws Exception {
		settingClient.logout(token);
		super.tearDown();
	}

	@Override
	protected Contact getTestContact() {
		return null;
	}

	@Override
	protected Event getTestEvent() {
		return null;
	}

}
