package org.obm.push.protocol;

import java.io.IOException;
import java.io.InputStream;

import org.easymock.EasyMock;
import org.junit.Test;
import org.obm.configuration.EmailConfiguration;
import org.obm.push.exception.QuotaExceededException;
import org.obm.push.protocol.request.ActiveSyncRequest;


public class MailProtocolTest {
	
	@Test
	public void testWithBigMessageMaxSize() throws IOException, QuotaExceededException {
		EmailConfiguration emailConfiguration = EasyMock.createMock(EmailConfiguration.class);
		ActiveSyncRequest request = EasyMock.createMock(ActiveSyncRequest.class);
		
		EasyMock.expect(request.getParameter("CollectionId")).andReturn("1").once();
		EasyMock.expect(request.getParameter("ItemId")).andReturn("1").once();
		EasyMock.expect(request.getInputStream()).andReturn(loadDataFile("bigEml.eml")).once();
		EasyMock.expect(request.getParameter("SaveInSent")).andReturn("T").once();
		
		EasyMock.expect(emailConfiguration.getMessageMaxSize()).andReturn(10485760).once();
		EasyMock.replay(request, emailConfiguration);
		
		MailProtocol mailProtocol = new MailProtocol(emailConfiguration);
		mailProtocol.getRequest(request);
		EasyMock.verify(request, emailConfiguration);
		
	}
	
	@Test(expected=QuotaExceededException.class)
	public void testWithSmallMessageMaxSize() throws IOException, QuotaExceededException {
		EmailConfiguration emailConfiguration = EasyMock.createMock(EmailConfiguration.class);
		ActiveSyncRequest request = EasyMock.createMock(ActiveSyncRequest.class);
		
		EasyMock.expect(request.getParameter("CollectionId")).andReturn("1").once();
		EasyMock.expect(request.getParameter("ItemId")).andReturn("1").once();
		EasyMock.expect(request.getInputStream()).andReturn(loadDataFile("bigEml.eml")).once();
		EasyMock.expect(request.getParameter("SaveInSent")).andReturn("T").once();
		
		EasyMock.expect(emailConfiguration.getMessageMaxSize()).andReturn(1024).once();
		EasyMock.replay(request, emailConfiguration);
		
		MailProtocol mailProtocol = new MailProtocol(emailConfiguration);
		mailProtocol.getRequest(request);
		EasyMock.verify(request, emailConfiguration);
		
	}

	protected InputStream loadDataFile(String name) {
		return getClass().getClassLoader().getResourceAsStream(
				"file/" + name);
	}
}
