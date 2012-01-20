package org.obm.opush;

import java.io.IOException;
import java.util.Locale;

import javax.xml.transform.TransformerException;

import org.easymock.EasyMock;
import org.fest.assertions.api.Assertions;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.obm.configuration.ConfigurationService;
import org.obm.opush.ActiveSyncServletModule.OpushServer;
import org.obm.opush.env.AbstractOpushEnv;
import org.obm.opush.env.JUnitGuiceRule;
import org.obm.push.exception.DaoException;
import org.obm.push.exception.activesync.CollectionNotFoundException;
import org.obm.push.store.CollectionDao;
import org.obm.push.store.DeviceDao;
import org.obm.push.utils.DOMUtils;
import org.obm.push.utils.collection.ClassToInstanceAgregateView;
import org.obm.sync.auth.AuthFault;
import org.obm.sync.client.login.LoginService;
import org.obm.sync.push.client.OPClient;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import com.google.common.collect.Sets;
import com.google.inject.Inject;

public class AutodiscoverHandlerTest {

	private static class AutodiscoverHandlerTestModule extends AbstractOpushEnv {}
	
	@Rule
	public JUnitGuiceRule guiceBerry = new JUnitGuiceRule(AutodiscoverHandlerTestModule.class);

	@Inject @PortNumber int port;
	@Inject SingleUserFixture singleUserFixture;
	@Inject OpushServer opushServer;
	@Inject ClassToInstanceAgregateView<Object> classToInstanceMap;

	@After
	public void shutdown() throws Exception {
		opushServer.stop();
	}

	@Test
	public void testAutodiscoverCommand() throws Exception {
		String externalUrl = "https://external-url/Microsoft-Server-ActiveSync";
		prepareMocks(externalUrl);
		opushServer.start();

		OPClient opClient = IntegrationTestUtils.buildOpushClient(singleUserFixture.jaures, port);
		
		String emailAddress = singleUserFixture.jaures.user.getEmail();
		Document document = opClient.postXml("Autodiscover", buildAutodiscoverCommand(emailAddress), "Autodiscover", null, false);
		
		checkAutodiscoverResponse(document, externalUrl, formatCultureParameter(Locale.getDefault()));
	}

	private void checkAutodiscoverResponse(Document response, String externalUrl, String culture) throws TransformerException {
		Assertions.assertThat(DOMUtils.serialise(response)).
		isEqualTo( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
				"<Autodiscover>" +
				"<Response>" +
				"<Culture>" + culture + "</Culture>" +
				"<User>" +
				"<DisplayName>Jean Jaures</DisplayName><EMailAddress>jaures@sfio.fr</EMailAddress>" +
				"</User>" +
				"<Action>" +
				"<Settings>" +
				"<Server><Type>MobileSync</Type><Url>" + externalUrl + "</Url>" +
				"<Name>" + externalUrl + "</Name></Server>" +
				"<Server><Type>CertEnroll</Type>" +
				"<Url>" + externalUrl + "</Url><ServerData>CertEnrollTemplate</ServerData></Server>" +
				"</Settings>" +
				"</Action>" +
				"</Response>" +
				"</Autodiscover>");
	}

	private void prepareMocks(String externalUrl) throws CollectionNotFoundException, DaoException, AuthFault {
		mockDeviceDao();
		mockLoginService();
		mockCollectionDaoNoChange();
		mockConfigurationService(externalUrl);
		IntegrationTestUtils.replayMocks(classToInstanceMap);
	}

	private void mockDeviceDao() throws DaoException {
		DeviceDao deviceDao = classToInstanceMap.get(DeviceDao.class);
		IntegrationTestUtils.expectUserDeviceAccess(deviceDao, singleUserFixture.jaures);
	}

	private void mockCollectionDaoNoChange() throws CollectionNotFoundException, DaoException {
		CollectionDao collectionDao = classToInstanceMap.get(CollectionDao.class);
		IntegrationTestUtils.expectUserCollectionsNeverChange(collectionDao, Sets.newHashSet(singleUserFixture.jaures));
	}
	
	private void mockLoginService() throws AuthFault {
		LoginService loginService = classToInstanceMap.get(LoginService.class);
		IntegrationTestUtils.expectUserLoginFromOpush(loginService, singleUserFixture.jaures);
	}
	
	private void mockConfigurationService(String externalUrl) {
		ConfigurationService configurationService = classToInstanceMap.get(ConfigurationService.class);
		EasyMock.expect(configurationService.getActiveSyncServletUrl()).andReturn(externalUrl);
	}
	
	private Document buildAutodiscoverCommand(String emailAddress)
			throws SAXException, IOException {
		return DOMUtils.parse("<Autodiscover xmlns=\"http://schemas.microsoft.com/exchange/autodiscover/mobilesync/requestschema/2006\">"
				+ "<Request>"
				+ "<EMailAddress>"
				+ emailAddress
				+ "</EMailAddress>"
				+ "<AcceptableResponseSchema>"
				+ "http://schemas.microsoft.com/exchange/autodiscover/mobilesync/responseschema/2006"
				+ "</AcceptableResponseSchema>"
				+ "</Request>"
				+ "</Autodiscover>");
	}

	private String formatCultureParameter(Locale locale) {
		return  locale.getLanguage().toLowerCase() + ":" + 
				locale.getCountry().toLowerCase() ;
	}
}
