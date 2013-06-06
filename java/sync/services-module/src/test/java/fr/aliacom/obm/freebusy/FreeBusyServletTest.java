package fr.aliacom.obm.freebusy;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;
import static org.easymock.EasyMock.replay;
import static org.fest.assertions.api.Assertions.assertThat;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.easymock.IMocksControl;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.guice.GuiceModule;
import org.obm.guice.SlowGuiceRunner;
import org.obm.sync.calendar.FreeBusyRequest;
import org.obm.sync.exception.ObmUserNotFoundException;

import com.google.common.collect.Sets;
import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;

@GuiceModule(FreeBusyServletTest.Env.class)
@RunWith(SlowGuiceRunner.class)
public class FreeBusyServletTest {

	public static class Env extends AbstractModule {
		private IMocksControl mocksControl;

		@Override
		protected void configure() {
			mocksControl = createControl();
			RemoteFreeBusyProvider remoteFreeBusyProvider = mocksControl.createMock(RemoteFreeBusyProvider.class);
			
			bind(IMocksControl.class).toInstance(mocksControl);
			bind(LocalFreeBusyProvider.class)
				.toInstance(mocksControl.createMock((DatabaseFreeBusyProvider.class)));
			bind(RemoteFreeBusyProvider.class)
				.toInstance(remoteFreeBusyProvider);
			bind(new TypeLiteral<Set<RemoteFreeBusyProvider>>() {})
				.toInstance(Sets.newHashSet(remoteFreeBusyProvider));
			bindWithMock(HttpServletRequest.class);
			bindWithMock(HttpServletResponse.class);
			bindWithMock(ServletConfig.class);
		}
		
		private <T> void bindWithMock(Class<T> cls) {
			bind(cls).toInstance(mocksControl.createMock(cls));
		}
	}
	
	@Inject
	private IMocksControl mocksControl;
	@Inject
	private FreeBusyServlet freeBusyServlet;
	@Inject
	private LocalFreeBusyProvider localFreeBusyProvider;
	@Inject
	private RemoteFreeBusyProvider remoteFreeBusyProvider;
	@Inject
	private HttpServletRequest request;
	@Inject
	private HttpServletResponse response;
	@Inject
	private ServletConfig servletConfig;
	@Inject
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
		outputStream = getFakeOutputStream();
		
		ServletContext servletContext = createMock(ServletContext.class);

		expect(servletConfig.getServletContext()).andReturn(servletContext);
		expect(servletContext.getAttribute(isA(String.class))).andReturn(injector);

		replay(servletContext);
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
		
		freeBusyServlet.init(servletConfig);
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
		
		freeBusyServlet.init(servletConfig);
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
		
		freeBusyServlet.init(servletConfig);
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
		
		freeBusyServlet.init(servletConfig);
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
		
		freeBusyServlet.init(servletConfig);
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
		
		freeBusyServlet.init(servletConfig);
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
		
		freeBusyServlet.init(servletConfig);
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
		
		freeBusyServlet.init(servletConfig);
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
		
		freeBusyServlet.init(servletConfig);
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
		
		freeBusyServlet.init(servletConfig);
		freeBusyServlet.doGet(request, response);
		
		mocksControl.verify();
		
		assertThat(outputStream.toString()).isNull();
	}
	
	@Test
	public void testMakeFreeBusyRequestWithNullOrganizer() {
		FreeBusyRequest fbr = freeBusyServlet.makeFreeBusyRequest(null, "attendee", new Date(), new Date());
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
