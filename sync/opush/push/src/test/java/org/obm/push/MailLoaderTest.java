package org.obm.push;

import java.math.BigDecimal;
import java.util.Properties;

import junit.framework.TestCase;

import org.junit.Ignore;
import org.minig.imap.StoreClient;
import org.obm.push.backend.BackendSession;
import org.obm.push.bean.MSEmail;
import org.obm.push.bean.MSEvent;
import org.obm.push.impl.Credentials;
import org.obm.push.mail.MailMessageLoader;
import org.obm.sync.client.calendar.CalendarClient;
import org.obm.sync.locators.CalendarLocator;

@Ignore("It's necessary to do again all tests")
public class MailLoaderTest extends TestCase {

	public void testMailLoader() {
		BackendSession bs = new BackendSession(new Credentials("thomas@zz.com", "aliacom"),
				"command", new Device("devType", "devId", new Properties()), new BigDecimal(0));
		StoreClient store = new StoreClient("obm23.buffy.kvm", 143,
				"thomas@zz.com", "aliacom");
		store.login();
		store.select("INBOX");
		CalendarLocator cl = new CalendarLocator();
		CalendarClient calCli = cl.locate("http://obm23.buffy.kvm:8080/obm-sync/services");
		MailMessageLoader mml = new MailMessageLoader(store, calCli);
		MSEmail mail = mml.fetch(25, 523, bs);
		assertNotNull(mail);
		MSEvent invit = mail.getInvitation();
		assertNotNull(invit);
	}
	
	public void testMailLoader1() {
		BackendSession bs = new BackendSession(new Credentials("adrien@test.tlse.lng", "aliacom"),
				"command", new Device("devType", "devId",new Properties()), new BigDecimal(0));
		StoreClient store = new StoreClient("obm", 143,
				"adrien@test.tlse.lng", "aliacom");
		store.login();
		store.select("INBOX");
		CalendarLocator cl = new CalendarLocator();
		CalendarClient calCli = cl.locate("http://obm:8080/obm-sync/services");
		MailMessageLoader mml = new MailMessageLoader(store, calCli);
		MSEmail mail = mml.fetch(315, 457, bs);
		assertNotNull(mail);
	}
	
	public void testMailLoader2() {
		BackendSession bs = new BackendSession(new Credentials("adrien@test.tlse.lng", "aliacom"),
				"command", new Device("devType","devId", new Properties()), new BigDecimal(0));
		StoreClient store = new StoreClient("obm", 143,
				"adrien@test.tlse.lng", "aliacom");
		store.login();
		store.select("INBOX");
		CalendarLocator cl = new CalendarLocator();
		CalendarClient calCli = cl.locate("http://obm:8080/obm-sync/services");
		MailMessageLoader mml = new MailMessageLoader(store, calCli);
		MSEmail mail = mml.fetch(675, 1352, bs);
		assertNotNull(mail);
		assertTrue(mail.getAttachements().size()>0);
	}

}
