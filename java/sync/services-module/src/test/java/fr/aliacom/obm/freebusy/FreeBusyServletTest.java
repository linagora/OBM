package fr.aliacom.obm.freebusy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.obm.sync.LoggerService;
import org.obm.sync.calendar.FreeBusyRequest;
import org.obm.sync.exception.ObmUserNotFoundException;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.TypeLiteral;


public class FreeBusyServletTest {

	private IMocksControl mocksControl;
	private LocalFreeBusyProvider localFreeBusyProvider;
	private RemoteFreeBusyProvider remoteFreeBusyProvider;
	private HttpServletRequest request;
	private HttpServletResponse response;
	private FreeBusyServlet freeBusyServlet;
	private LoggerService loggerService;
	private Injector injector;
	
	private ServletOutputStream outputStream;
	
	private final static String ICS = "ics";
	private final static String REQUEST_URI = "/obm-sync/freebusy";
	private final static String PATHINFO_ATTENDEE = "attendee";
	private final static String PARAM_ORGANIZER = "organizer";
	private final static String DATASOURCE_PARAMETER = "datasource";
	private final static String LOCAL_DATASOURCE = "local";
	private final static String REMOTE_DATASOURCE = "remote";
	
	@Before
	public void setUp() {
		mocksControl = createControl();
		outputStream = getFakeOutputStream();
		remoteFreeBusyProvider = mocksControl.createMock(RemoteFreeBusyProvider.class);
		localFreeBusyProvider = mocksControl.createMock((DatabaseFreeBusyProvider.class));
		request = mocksControl.createMock(HttpServletRequest.class);
		response = mocksControl.createMock(HttpServletResponse.class);
		injector = mocksControl.createMock(Injector.class);
		loggerService = mocksControl.createMock(LoggerService.class);
		loggerService.defineUser(anyObject(String.class));
		expectLastCall().anyTimes();
		loggerService.defineCommand(anyObject(String.class));
		expectLastCall().anyTimes();
		expect(injector.getInstance(Key.get(new TypeLiteral<Set<RemoteFreeBusyProvider>>() {})))
			.andReturn(ImmutableSet.of(remoteFreeBusyProvider));
	}
	
	@Test
	public void testDoGetWithNullDataSourceCallLocalProvider()
			throws IOException, ServletException, FreeBusyException, ObmUserNotFoundException {
		expectOnHttpRequest();
		expect(request.getParameterValues(DATASOURCE_PARAMETER)).andReturn(null).once();
		expect(localFreeBusyProvider.findFreeBusyIcs(anyObject(FreeBusyRequest.class))).andReturn(ICS).once();
		expect(response.getOutputStream()).andReturn(outputStream).once();
		
		response.setStatus(HttpServletResponse.SC_OK);
		expectLastCall().once();
		
		mocksControl.replay();

		freeBusyServlet = new FreeBusyServlet(loggerService, localFreeBusyProvider, injector);
		freeBusyServlet.doGet(request, response);
		
		mocksControl.verify();
		
		assertThat(outputStream.toString()).isEqualTo(ICS);
	}
	
	@Test
	public void testDoGetWithNullPathInfo() throws IOException, ServletException {
		expect(request.getRequestURI()).andReturn(REQUEST_URI).once();
		expect(request.getPathInfo()).andReturn(null).once();
		
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		expectLastCall().once();
		
		mocksControl.replay();

		freeBusyServlet = new FreeBusyServlet(loggerService, localFreeBusyProvider, injector);
		freeBusyServlet.doGet(request, response);
		
		mocksControl.verify();
		
		assertThat(outputStream.toString()).isEqualTo(null);
	}
	
	@Test
	public void testDoGetWithNullDataSourceCallRemoteProvider()
			throws IOException, ServletException, FreeBusyException, ObmUserNotFoundException {
		expectOnHttpRequest();
		expect(request.getParameterValues(DATASOURCE_PARAMETER)).andReturn(null).once();
		expect(localFreeBusyProvider.findFreeBusyIcs(anyObject(FreeBusyRequest.class))).andReturn(null).once();
		expect(remoteFreeBusyProvider.findFreeBusyIcs(anyObject(FreeBusyRequest.class))).andReturn(ICS).once();
		expect(response.getOutputStream()).andReturn(outputStream).once();
		
		response.setStatus(HttpServletResponse.SC_OK);
		expectLastCall().once();
		
		mocksControl.replay();

		freeBusyServlet = new FreeBusyServlet(loggerService, localFreeBusyProvider, injector);
		freeBusyServlet.doGet(request, response);
		
		mocksControl.verify();
		
		assertThat(outputStream.toString()).isEqualTo(ICS);
	}
	
	@Test
	public void testDoGetCallFollowingProviderOnException()
			throws IOException, ServletException, FreeBusyException, ObmUserNotFoundException {
		expectOnHttpRequest();
		expect(request.getParameterValues(DATASOURCE_PARAMETER)).andReturn(null).once();
		expect(localFreeBusyProvider.findFreeBusyIcs(anyObject(FreeBusyRequest.class)))
			.andThrow(new RuntimeException());
		expect(remoteFreeBusyProvider.findFreeBusyIcs(anyObject(FreeBusyRequest.class))).andReturn(ICS).once();
		expect(response.getOutputStream()).andReturn(outputStream).once();
		
		response.setStatus(HttpServletResponse.SC_OK);
		expectLastCall().once();
		
		mocksControl.replay();

		freeBusyServlet = new FreeBusyServlet(loggerService, localFreeBusyProvider, injector);
		freeBusyServlet.doGet(request, response);
		
		mocksControl.verify();
		
		assertThat(outputStream.toString()).isEqualTo(ICS);
	}
	
	@Test
	public void testDoGetFreeBusyReturnNullIcs()
			throws ServletException, FreeBusyException, IOException, ObmUserNotFoundException {
		expectOnHttpRequest();
		expect(request.getParameterValues(DATASOURCE_PARAMETER)).andReturn(null).once();
		expect(localFreeBusyProvider.findFreeBusyIcs(anyObject(FreeBusyRequest.class))).andReturn(null).once();
		expect(remoteFreeBusyProvider.findFreeBusyIcs(anyObject(FreeBusyRequest.class))).andReturn(null).once();
		
		response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		expectLastCall().once();
		
		mocksControl.replay();

		freeBusyServlet = new FreeBusyServlet(loggerService, localFreeBusyProvider, injector);
		freeBusyServlet.doGet(request, response);
		
		mocksControl.verify();
		
		assertThat(outputStream.toString()).isNull();
	}
	
	@Test
	public void testDoGetDoesNotWantLocalDataSource() throws IOException, ServletException {
		String[] dataSource = {};
		
		expectOnHttpRequest();
		expect(request.getParameterValues(DATASOURCE_PARAMETER)).andReturn(dataSource).once();
		
		response.setStatus(HttpServletResponse.SC_NO_CONTENT);
		expectLastCall().once();
		
		mocksControl.replay();

		freeBusyServlet = new FreeBusyServlet(loggerService, localFreeBusyProvider, injector);
		freeBusyServlet.doGet(request, response);
		
		mocksControl.verify();
		
		assertThat(outputStream.toString()).isNull();
	}
	
	@Test
	public void testDoGetWithLocalProviderButNoRemote()
			throws FreeBusyException, IOException, ServletException, ObmUserNotFoundException {
		String[] dataSource = {LOCAL_DATASOURCE};
		
		expectOnHttpRequest();
		expect(request.getParameterValues(DATASOURCE_PARAMETER)).andReturn(dataSource).once();
		expect(localFreeBusyProvider.findFreeBusyIcs(anyObject(FreeBusyRequest.class))).andReturn(ICS).once();
		expect(response.getOutputStream()).andReturn(outputStream).once();
		
		response.setStatus(HttpServletResponse.SC_OK);
		expectLastCall().once();
		
		mocksControl.replay();

		freeBusyServlet = new FreeBusyServlet(loggerService, localFreeBusyProvider, injector);
		freeBusyServlet.doGet(request, response);
		
		mocksControl.verify();
		
		assertThat(outputStream.toString()).isEqualTo(ICS);
	}
	
	@Test
	public void testDoGetWithRemoteProviderButNoLocal()
			throws FreeBusyException, IOException, ServletException, ObmUserNotFoundException {
		String[] dataSource = {REMOTE_DATASOURCE};
		
		expectOnHttpRequest();
		expect(request.getParameterValues(DATASOURCE_PARAMETER)).andReturn(dataSource).once();
		expect(remoteFreeBusyProvider.findFreeBusyIcs(anyObject(FreeBusyRequest.class))).andReturn(ICS).once();
		expect(response.getOutputStream()).andReturn(outputStream).once();
		
		response.setStatus(HttpServletResponse.SC_OK);
		expectLastCall().once();
		
		mocksControl.replay();

		freeBusyServlet = new FreeBusyServlet(loggerService, localFreeBusyProvider, injector);
		freeBusyServlet.doGet(request, response);
		
		mocksControl.verify();
		
		assertThat(outputStream.toString()).isEqualTo(ICS);
	}
	
	@Test
	public void testDoGetWarnForPrivateFreeBusy()
			throws ServletException, IOException, FreeBusyException, ObmUserNotFoundException {
		expectOnHttpRequest();
		expect(request.getParameterValues(DATASOURCE_PARAMETER)).andReturn(null).once();
		expect(localFreeBusyProvider.findFreeBusyIcs(anyObject(FreeBusyRequest.class)))
			.andThrow(new PrivateFreeBusyException());
		
		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		expectLastCall().once();
		
		mocksControl.replay();

		freeBusyServlet = new FreeBusyServlet(loggerService, localFreeBusyProvider, injector);
		freeBusyServlet.doGet(request, response);
		
		mocksControl.verify();
		
		assertThat(outputStream.toString()).isNull();
	}
	
	@Test
	public void testDoGetWarnForNotFoundObmUser()
			throws ObmUserNotFoundException, FreeBusyException, ServletException, IOException {
		expectOnHttpRequest();
		expect(request.getParameterValues(DATASOURCE_PARAMETER)).andReturn(null).once();
		expect(localFreeBusyProvider.findFreeBusyIcs(anyObject(FreeBusyRequest.class)))
			.andThrow(new ObmUserNotFoundException("not found"));
		
		response.setStatus(HttpServletResponse.SC_NOT_FOUND);
		expectLastCall().once();
		
		mocksControl.replay();

		freeBusyServlet = new FreeBusyServlet(loggerService, localFreeBusyProvider, injector);
		freeBusyServlet.doGet(request, response);
		
		mocksControl.verify();
		
		assertThat(outputStream.toString()).isNull();
	}
	
	@Test
	public void testMakeFreeBusyRequestWithNullOrganizer() {
		mocksControl.replay();
		freeBusyServlet = new FreeBusyServlet(loggerService, localFreeBusyProvider, injector);
		FreeBusyRequest fbr = freeBusyServlet.makeFreeBusyRequest(null, "attendee", new Date(), new Date());
		mocksControl.verify();
		assertThat(fbr.getOwner()).isEqualTo("attendee");
	}

	private void expectOnHttpRequest() {
		expect(request.getRequestURI()).andReturn(REQUEST_URI).once();
		expect(request.getPathInfo()).andReturn(PATHINFO_ATTENDEE).once();
		expect(request.getParameter(PARAM_ORGANIZER)).andReturn(PARAM_ORGANIZER).once();
	}
	
	private ServletOutputStream getFakeOutputStream() {
		return new ServletOutputStream() {
			
			private String ics;

			@Override
			public void write(int b) throws IOException {
			}
			
			@Override
			public void write(byte[] b) {
				ics = new String(b);
			}
			
			@Override
			public String toString() {
				return ics;
			}
		};
	}
}
