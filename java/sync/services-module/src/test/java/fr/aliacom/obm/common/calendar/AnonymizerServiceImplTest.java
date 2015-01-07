/* ***** BEGIN LICENSE BLOCK *****
 * Copyright (C) 2011-2015  Linagora
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

package fr.aliacom.obm.common.calendar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.sync.auth.AccessToken;
import org.obm.sync.calendar.Attendee;
import org.obm.sync.calendar.Event;
import org.obm.sync.calendar.EventPrivacy;
import org.obm.sync.calendar.Participation;
import org.obm.sync.calendar.UserAttendee;

import fr.aliacom.obm.ToolBox;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserService;
import fr.aliacom.obm.services.constant.ObmSyncConfigurationService;
import fr.aliacom.obm.utils.HelperService;

public class AnonymizerServiceImplTest {

	private AnonymizerServiceImpl anonymizerService;
	private ObmSyncConfigurationService configurationService;
	private IMocksControl control;
	private HelperService helperService;
	private UserService userService;
	protected AccessToken accessToken;
	protected ObmUser viewer;
	
	
	@Before
	public void setup() {
		accessToken = ToolBox.mockAccessToken();
		viewer = ToolBox.getDefaultObmUser();
		control = createControl();
		configurationService  = control.createMock(ObmSyncConfigurationService.class);
		helperService = control.createMock(HelperService.class);
		userService = control.createMock(UserService.class);
	}
	
	@Test
	public void testAnonymizerShouldNotAnonymizeWithPublicEvent() {
		Event event = new Event();
		
		expect(userService.getUserFromAccessToken(accessToken)).andReturn(viewer);
		expect(configurationService.isPrivateEventAnonymizationEnabled()).andReturn(true);
		expect(helperService.canWriteOnCalendar(accessToken, viewer.getEmail())).andReturn(true);
		control.replay();
		boolean isAnonymizedEvent = getAnonymizerServiceResult(event, viewer.getEmail(), accessToken).isAnonymized();
		control.verify();
		
		assertThat(isAnonymizedEvent).isFalse();
	}	

	@Test
	public void testAnonymizerShouldAnonymizeWhenViewerDoesntAttendPrivateEvent() {
		Event event = newPrivateEvent();
		expect(configurationService.isPrivateEventAnonymizationEnabled()).andReturn(true);
		expect(userService.getUserFromAccessToken(accessToken)).andReturn(viewer);
		expect(helperService.canWriteOnCalendar(accessToken, viewer.getEmail())).andReturn(true);
		control.replay();
		boolean isAnonymizedEvent = getAnonymizerServiceResult(event, viewer.getEmail(), accessToken).isAnonymized();
		control.verify();
		
		assertThat(isAnonymizedEvent).isTrue();
	}	
	
	@Test
	public void testAnonymizerShouldAnonymizeWhenViewerDoesntAttendConfidentialEvent() {
		Event event = newConfidentialEvent();
		
		expect(userService.getUserFromAccessToken(accessToken)).andReturn(viewer);
		expect(configurationService.isPrivateEventAnonymizationEnabled()).andReturn(true);
		expect(helperService.canWriteOnCalendar(accessToken, viewer.getEmail())).andReturn(false);
		control.replay();
		boolean isAnonymizedEvent = getAnonymizerServiceResult(event, viewer.getEmail(), accessToken).isAnonymized();
		control.verify();
		
		assertThat(isAnonymizedEvent).isFalse();
	}
	
	@Test
	public void testAnonymizerShouldNotAnonymizeWhenViewerAttendsPublicEvent() {
		Event event = addUserAttendee(new Event(), viewer); 

		expect(userService.getUserFromAccessToken(accessToken)).andReturn(viewer);
		expect(configurationService.isPrivateEventAnonymizationEnabled()).andReturn(true);
		expect(helperService.canWriteOnCalendar(accessToken, viewer.getEmail())).andReturn(false);
		control.replay();
		boolean isAnonymizedEvent = getAnonymizerServiceResult(event, viewer.getEmail(), accessToken).isAnonymized();
		control.verify();

		assertThat(isAnonymizedEvent).isFalse();
	}
	
	@Test
	public void testAnonymizerShouldNotAnonymizeWhenViewerAttendsPrivateEvent() {
		Event event = addUserAttendee(newPrivateEvent(), viewer); 
		
		expect(userService.getUserFromAccessToken(accessToken)).andReturn(viewer);
		expect(configurationService.isPrivateEventAnonymizationEnabled()).andReturn(true);
		expect(helperService.canWriteOnCalendar(accessToken, viewer.getEmail())).andReturn(true);
		control.replay();
		boolean isAnonymizedEvent = getAnonymizerServiceResult(event, viewer.getEmail(), accessToken).isAnonymized();
		control.verify();

		assertThat(isAnonymizedEvent).isFalse();
	}
	
	@Test
	public void testAnonymizerShouldAnonymizeWhenViewerAttendsPrivateEventWithConfigurationSetFalse() {
		Event event = addUserAttendee(newPrivateEvent(), viewer); 
		
		expect(userService.getUserFromAccessToken(accessToken)).andReturn(viewer);
		expect(configurationService.isPrivateEventAnonymizationEnabled()).andReturn(false);
		expect(helperService.canWriteOnCalendar(accessToken, viewer.getEmail())).andReturn(false);
		control.replay();
		boolean isAnonymizedEvent = getAnonymizerServiceResult(event, viewer.getEmail(), accessToken).isAnonymized();
		control.verify();

		assertThat(isAnonymizedEvent).isFalse();
	}
	
	@Test
	public void testAnonymizerShouldNotAnonymizeWhenViewerAttendsConfidentialEvent() {
		Event event = addUserAttendee(newConfidentialEvent(), viewer); 
		
		expect(userService.getUserFromAccessToken(accessToken)).andReturn(viewer);
		expect(configurationService.isPrivateEventAnonymizationEnabled()).andReturn(true);
		expect(helperService.canWriteOnCalendar(accessToken, viewer.getEmail())).andReturn(false);
		control.replay();
		boolean isAnonymizedEvent = getAnonymizerServiceResult(event, viewer.getEmail(), accessToken).isAnonymized();
		control.verify();

		assertThat(isAnonymizedEvent).isFalse();
	}

	private Event newPrivateEvent() {
		Event event = new Event();

		event.setPrivacy(EventPrivacy.PRIVATE);

		return event;
	}	
	
	private Event newConfidentialEvent() {
		Event event = new Event();

		event.setPrivacy(EventPrivacy.CONFIDENTIAL);

		return event;
	}	

	private Event addUserAttendee(Event event, ObmUser user) {
		Attendee attendee = UserAttendee
				.builder()
				.canWriteOnCalendar(false)
				.displayName(user.getDisplayName())
				.email(user.getEmail())
				.entityId(user.getEntityId())
				.participation(Participation.needsAction())
				.build();

		event.addAttendee(attendee);

		return event;
	}
	
	private Event getAnonymizerServiceResult(Event event, String mail, AccessToken accessToken){
		anonymizerService = new AnonymizerServiceImpl(configurationService, helperService, userService);
		return anonymizerService.anonymize(event, mail, accessToken);
	}
	
}
