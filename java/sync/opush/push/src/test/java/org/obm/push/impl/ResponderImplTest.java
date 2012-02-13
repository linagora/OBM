package org.obm.push.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;

import org.easymock.EasyMock;
import org.junit.Before;
import org.junit.Test;
import org.obm.push.protocol.data.IntEncoder;
import org.obm.push.wbxml.WBXMLTools;
import org.obm.push.wbxml.WBXmlException;
import org.w3c.dom.Document;

public class ResponderImplTest {

	@Before
	public void setUp() {
		
	}
	
	@Test
	public void testSendResponse() throws WBXmlException, IOException {
		String namespace = "myNamespace";
		byte[] data = {1, 2, 3, 4, 5, 6};
		sendResponseWithData(namespace, data);
	}

	@Test
	public void testSendResponseEmptyData() throws WBXmlException, IOException {
		String namespace = "myNamespace";
		byte[] data = {};
		sendResponseWithData(namespace, data);
	}

	@Test(expected=NullPointerException.class)
	public void testSendResponseNullData() throws Throwable {
		String namespace = "myNamespace";
		byte[] data = null;
		
		HttpServletResponse servletResponse = EasyMock.createMock(HttpServletResponse.class);
		IntEncoder intEncoder = EasyMock.createMock(IntEncoder.class);
		WBXMLTools wbxmlTools = EasyMock.createMock(WBXMLTools.class);
		Document document = EasyMock.createMock(Document.class);
		DOMDumper domDumper = EasyMock.createMock(DOMDumper.class);
		domDumper.dumpXml(EasyMock.anyObject(Document.class));
		EasyMock.expectLastCall().anyTimes();
		
		EasyMock.expect(wbxmlTools.toWbxml(namespace, document)).andReturn(data);
		EasyMock.replay(servletResponse, intEncoder, wbxmlTools, document, domDumper);
		ResponderImpl responder = new ResponderImpl(servletResponse, intEncoder, wbxmlTools, domDumper);
		
		
		try {
			responder.sendResponse(namespace, document);
		} catch (Throwable t) {
			EasyMock.verify(servletResponse, intEncoder, wbxmlTools, document);
			throw t;
		}

	}
	
	private void sendResponseWithData(String namespace, byte[] data)
			throws WBXmlException, IOException {
		HttpServletResponse servletResponse = EasyMock.createMock(HttpServletResponse.class);
		ServletOutputStream servletOutputStream = EasyMock.createMock(ServletOutputStream.class);
		IntEncoder intEncoder = EasyMock.createMock(IntEncoder.class);
		WBXMLTools wbxmlTools = EasyMock.createMock(WBXMLTools.class);
		Document document = EasyMock.createMock(Document.class);
		DOMDumper domDumper = EasyMock.createMock(DOMDumper.class);
		domDumper.dumpXml(EasyMock.anyObject(Document.class));
		EasyMock.expectLastCall().anyTimes();
		
		EasyMock.expect(wbxmlTools.toWbxml(namespace, document)).andReturn(data);
		servletResponse.setContentType("application/vnd.ms-sync.wbxml");
		servletResponse.setContentLength(data.length);
		EasyMock.expect(servletResponse.getOutputStream()).andReturn(servletOutputStream);
		servletOutputStream.write(data);
		servletOutputStream.flush();
		servletOutputStream.close();
		
		EasyMock.replay(servletResponse, intEncoder, wbxmlTools, document, domDumper);
		ResponderImpl responder = new ResponderImpl(servletResponse, intEncoder, wbxmlTools, domDumper);
		
		responder.sendResponse(namespace, document);
		
		EasyMock.verify(servletResponse, intEncoder, wbxmlTools, document);
	}

	@Test
	public void sendResponseFileWithData() throws IOException {
		String contentType = "application/pdf";
		byte[] data = {1, 2, 3, 4, 5};
		
		sendResponseFile(contentType, data);
	}
	
	@Test
	public void sendResponseFileWithEmptyStream() throws IOException {
		String contentType = "application/pdf";
		byte[] data = {};
		
		sendResponseFile(contentType, data);
	}

	private void sendResponseFile(String contentType, byte[] data)
			throws IOException {
		HttpServletResponse servletResponse = EasyMock.createMock(HttpServletResponse.class);
		ServletOutputStream servletOutputStream = EasyMock.createMock(ServletOutputStream.class);
		IntEncoder intEncoder = EasyMock.createMock(IntEncoder.class);
		WBXMLTools wbxmlTools = EasyMock.createMock(WBXMLTools.class);
		
		servletResponse.setContentType(contentType);
		servletResponse.setContentLength(data.length);
		EasyMock.expect(servletResponse.getOutputStream()).andReturn(servletOutputStream);
		servletOutputStream.write(data);
		servletOutputStream.flush();
		servletOutputStream.close();
		servletResponse.setStatus(200);
		
		EasyMock.replay(servletResponse, intEncoder, wbxmlTools);
		ResponderImpl responder = new ResponderImpl(servletResponse, intEncoder, wbxmlTools, null);
		
		responder.sendResponseFile(contentType, new ByteArrayInputStream(data));
		
		EasyMock.verify(servletResponse, intEncoder, wbxmlTools);
	}
	
	@Test(expected=NullPointerException.class)
	public void sendResponseFileWithNullContentType() throws Throwable {
		String contentType = null;
		byte[] data = {1, 2, 3, 4, 5};
		
		HttpServletResponse servletResponse = EasyMock.createMock(HttpServletResponse.class);
		IntEncoder intEncoder = EasyMock.createMock(IntEncoder.class);
		WBXMLTools wbxmlTools = EasyMock.createMock(WBXMLTools.class);
		
		EasyMock.replay(servletResponse, intEncoder, wbxmlTools);
		ResponderImpl responder = new ResponderImpl(servletResponse, intEncoder, wbxmlTools, null);
		
		try {
			responder.sendResponseFile(contentType, new ByteArrayInputStream(data));
		} catch (Throwable t) {
			EasyMock.verify(servletResponse, intEncoder, wbxmlTools);
			throw t;
		}
	}
	
	@Test(expected=NullPointerException.class)
	public void sendResponseFileWithNullStream() throws Throwable {
		String contentType = "application/pdf";
		
		HttpServletResponse servletResponse = EasyMock.createMock(HttpServletResponse.class);
		IntEncoder intEncoder = EasyMock.createMock(IntEncoder.class);
		WBXMLTools wbxmlTools = EasyMock.createMock(WBXMLTools.class);
		
		EasyMock.replay(servletResponse, intEncoder, wbxmlTools);
		ResponderImpl responder = new ResponderImpl(servletResponse, intEncoder, wbxmlTools, null);
		
		try {
			responder.sendResponseFile(contentType, null);
		} catch (Throwable t) {
			EasyMock.verify(servletResponse, intEncoder, wbxmlTools);
			throw t;
		}
	}
	
	@Test
	public void sendError() throws IOException {
		int status = 123;
		HttpServletResponse servletResponse = EasyMock.createMock(HttpServletResponse.class);
		IntEncoder intEncoder = EasyMock.createMock(IntEncoder.class);
		WBXMLTools wbxmlTools = EasyMock.createMock(WBXMLTools.class);
		servletResponse.sendError(status);
		EasyMock.replay(servletResponse, intEncoder, wbxmlTools);
		ResponderImpl responder = new ResponderImpl(servletResponse, intEncoder, wbxmlTools, null);
		responder.sendError(status);
		EasyMock.verify(servletResponse, intEncoder, wbxmlTools);
	}
	
	@Test
	public void sendMSSyncMultipartResponse()
			throws IOException, WBXmlException {
		String namespace = "myNS";
		byte[] textData = {4, 3, 2, 1};
		byte[] binaryPart = {1, 2, 3};
		
		HttpServletResponse servletResponse = EasyMock.createMock(HttpServletResponse.class);
		ServletOutputStream servletOutputStream = EasyMock.createMock(ServletOutputStream.class);
		IntEncoder intEncoder = EasyMock.createMock(IntEncoder.class);
		WBXMLTools wbxmlTools = EasyMock.createMock(WBXMLTools.class);
		Document document = EasyMock.createMock(Document.class);
		
		servletResponse.setContentType("application/vnd.ms-sync.multipart");
		EasyMock.expect(servletResponse.getOutputStream()).andReturn(servletOutputStream);
		EasyMock.expect(wbxmlTools.toWbxml(namespace, document)).andReturn(textData);
		
		writeInt(servletOutputStream, intEncoder, (byte) 2); //two parts
		writeInt(servletOutputStream, intEncoder, (byte) 20); //start of part 1
		writeInt(servletOutputStream, intEncoder, (byte) 4); //length of part 1
		writeInt(servletOutputStream, intEncoder, (byte) 24); //start of part 2
		writeInt(servletOutputStream, intEncoder, (byte) 3); //length of part 2
		servletOutputStream.write(textData);
		servletOutputStream.write(binaryPart);
		servletOutputStream.close();
		
		EasyMock.replay(servletResponse, intEncoder, wbxmlTools, document);
		ResponderImpl responder = new ResponderImpl(servletResponse, intEncoder, wbxmlTools, null);
		
		responder.sendMSSyncMultipartResponse(namespace, document, Arrays.asList(binaryPart), false);
		
		EasyMock.verify(servletResponse, intEncoder, wbxmlTools, document);
	}

	private void writeInt(ServletOutputStream servletOutputStream,
			IntEncoder intEncoder, byte value) throws IOException {
		byte[] binaryEncoded = new byte[]{value, 0, 0, 0};
		EasyMock.expect(intEncoder.toByteArray(value)).andReturn(binaryEncoded);
		servletOutputStream.write(binaryEncoded);
	}
}
