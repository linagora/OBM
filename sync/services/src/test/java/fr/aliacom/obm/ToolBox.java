package fr.aliacom.obm;

import java.util.Locale;
import java.util.TimeZone;

import org.easymock.EasyMock;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.setting.SettingsService;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserSettings;

public class ToolBox {

	public static ObmUser getDefaultObmUser(){
		ObmDomain obmDomain = new ObmDomain();
		obmDomain.setName("test.tlse.lng");
		ObmUser obmUser = new ObmUser();
		obmUser.setDomain(obmDomain);
		return obmUser;
	}
	
	public static UserSettings getDefaultSettings() {
		UserSettings settings = EasyMock.createMock(UserSettings.class);
		EasyMock.expect(settings.locale()).andReturn(Locale.FRENCH).anyTimes();
		EasyMock.expect(settings.timezone()).andReturn(TimeZone.getTimeZone("Europe/Paris")).anyTimes();
		EasyMock.replay(settings);
		return settings;
	}
	
	public static SettingsService getDefaultSettingsService() {
		SettingsService service = EasyMock.createMock(SettingsService.class);
		service.getSettings(EasyMock.anyObject(ObmUser.class));
		EasyMock.expectLastCall().andReturn(getDefaultSettings()).anyTimes();
		EasyMock.replay(service);
		return service;
	}
	
}
