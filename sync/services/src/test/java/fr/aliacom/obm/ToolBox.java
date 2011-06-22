package fr.aliacom.obm;

import java.util.Locale;
import java.util.TimeZone;

import org.easymock.EasyMock;

import com.linagora.obm.sync.Producer;

import fr.aliacom.obm.common.domain.ObmDomain;
import fr.aliacom.obm.common.setting.SettingsService;
import fr.aliacom.obm.common.user.ObmUser;
import fr.aliacom.obm.common.user.UserSettings;

public class ToolBox {

	public static ObmDomain getDefaultObmDomain() {
		ObmDomain obmDomain = new ObmDomain();
		obmDomain.setName("test.tlse.lng");
		return obmDomain;
	}
	
	public static ObmUser getDefaultObmUser(){
		ObmDomain obmDomain = getDefaultObmDomain();
		ObmUser obmUser = new ObmUser();
		obmUser.setLogin("user");
		obmUser.setEmail("user@test");
		obmUser.setDomain(obmDomain);
		return obmUser;
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
