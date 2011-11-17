package org.obm.sync.server;

import javax.servlet.http.HttpServletRequest;

import org.easymock.EasyMock;
import org.fest.assertions.api.Assertions;
import org.junit.Test;

public class RequestTest {

	@Test
	public void testStatusRequestNoSlash() {
		HttpServletRequest httpRequest = EasyMock.createMock(HttpServletRequest.class);
		EasyMock.expect(httpRequest.getPathInfo()).andReturn(null);

		EasyMock.replay(httpRequest);

		Request request = new Request(httpRequest);
		Assertions.assertThat(request.getHandlerName() == null);
		Assertions.assertThat(request.getMethod() == null);
	}

	@Test
	public void testStatusRequestSlash() {
		HttpServletRequest httpRequest = EasyMock.createMock(HttpServletRequest.class);
		EasyMock.expect(httpRequest.getPathInfo()).andReturn("/");

		EasyMock.replay(httpRequest);

		Request request = new Request(httpRequest);
		Assertions.assertThat(request.getHandlerName() == null);
		Assertions.assertThat(request.getMethod() == null);
	}

	@Test
	public void testLoginRequest() {
		HttpServletRequest httpRequest = EasyMock.createMock(HttpServletRequest.class);
		EasyMock.expect(httpRequest.getPathInfo()).andReturn("/login/doLogin");

		EasyMock.replay(httpRequest);

		Request request = new Request(httpRequest);
		Assertions.assertThat(request.getHandlerName() == "login");
		Assertions.assertThat(request.getMethod() == "doLogin");
	}
}