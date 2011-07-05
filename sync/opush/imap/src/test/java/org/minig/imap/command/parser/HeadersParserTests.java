package org.minig.imap.command.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.minig.imap.command.parser.HeadersParser;

import com.google.common.collect.Sets;

public class HeadersParserTests {


	private Map<String, String> parseStringAsHeaders(String data) throws IOException {
		return new HeadersParser().parseRawHeaders(new StringReader(data));
	}
	
	@Test
	public void testParsingBasicHeaders() throws IOException {
		String data = "headername: headervalue";
		Map<String, String> headers = parseStringAsHeaders(data);
		Assert.assertEquals(1, headers.size());
		Assert.assertArrayEquals(new String[] {"headername"}, headers.keySet().toArray());
		Assert.assertArrayEquals(new String[] {"headervalue"}, headers.values().toArray());
	}
	
	@Test
	public void testParsingBasicHeadersKeyCaseInsensitive() throws IOException {
		String data = "headerName: headervalue";
		Map<String, String> headers = parseStringAsHeaders(data);
		Assert.assertEquals(1, headers.size());
		Assert.assertArrayEquals(new String[] {"headername"}, headers.keySet().toArray());
		Assert.assertArrayEquals(new String[] {"headervalue"}, headers.values().toArray());
	}

	@Test
	public void testParsingBasicHeadersValueCaseSensitive() throws IOException {
		String data = "headername: headerValue";
		Map<String, String> headers = parseStringAsHeaders(data);
		Assert.assertEquals(1, headers.size());
		Assert.assertArrayEquals(new String[] {"headername"}, headers.keySet().toArray());
		Assert.assertArrayEquals(new String[] {"headerValue"}, headers.values().toArray());
	}
	
	@Test
	public void testParsingHeadersValueContainsColon() throws IOException {
		String data = "headername: header:Value";
		Map<String, String> headers = parseStringAsHeaders(data);
		Assert.assertEquals(1, headers.size());
		Assert.assertArrayEquals(new String[] {"headername"}, headers.keySet().toArray());
		Assert.assertArrayEquals(new String[] {"header:Value"}, headers.values().toArray());
	}

	@Test
	public void testParsingAllPossibleValueForName() throws IOException {
		StringBuilder sb = new StringBuilder();
		HashSet<Character> excludedChars = Sets.newHashSet(':');
		for (int i = 33; i <= 126; ++i) {
			Character character = Character.valueOf((char) i);
			if (!excludedChars.contains(character)) {
				sb.append(character);
			}
		}
		String headerName = sb.toString();
		String data = headerName + ": headerValue";
		Map<String, String> headers = parseStringAsHeaders(data);
		Assert.assertEquals(1, headers.size());
		Assert.assertArrayEquals(new String[] {headerName.toLowerCase()}, headers.keySet().toArray());
		Assert.assertArrayEquals(new String[] {"headerValue"}, headers.values().toArray());
	}

	
	@Test
	public void testParsingHeadersRFC822Ch311Ex1() throws IOException {
		String data = "To:  \"Joe & J. Harvey\" <ddd @Org>, JJV @ BBN";
		Map<String, String> headers = parseStringAsHeaders(data);
		Assert.assertEquals(1, headers.size());
		Assert.assertArrayEquals(new String[] {"to"}, headers.keySet().toArray());
		Assert.assertArrayEquals(new String[] {"\"Joe & J. Harvey\" <ddd @Org>, JJV @ BBN"}, headers.values().toArray());
	}
	
	@Test
	public void testParsingHeadersRFC822Ch311Ex2() throws IOException {
		String data = "To:  \"Joe & J. Harvey\" <ddd @Org>,\n" +
				"        JJV @ BBN";
		Map<String, String> headers = parseStringAsHeaders(data);
		Assert.assertEquals(1, headers.size());
		Assert.assertArrayEquals(new String[] {"to"}, headers.keySet().toArray());
		Assert.assertArrayEquals(new String[] {"\"Joe & J. Harvey\" <ddd @Org>, JJV @ BBN"}, headers.values().toArray());
	}
	
	@Test
	public void testParsingHeadersRFC822Ch311WithHTAB() throws IOException {
		String data = "To:  \"Joe & J. Harvey\" <ddd @Org>,\n" +
				"\tJJV @ BBN";
		Map<String, String> headers = parseStringAsHeaders(data);
		Assert.assertEquals(1, headers.size());
		Assert.assertArrayEquals(new String[] {"to"}, headers.keySet().toArray());
		Assert.assertArrayEquals(new String[] {"\"Joe & J. Harvey\" <ddd @Org>, JJV @ BBN"}, headers.values().toArray());
	}
	
	@Test
	public void testParsingHeadersWithCr() throws IOException {
		String data = "To:  \"Joe & J. Harvey\" <ddd @Org>,\r" +
				"        JJV @ BBN";
		Map<String, String> headers = parseStringAsHeaders(data);
		Assert.assertEquals(1, headers.size());
		Assert.assertArrayEquals(new String[] {"to"}, headers.keySet().toArray());
		Assert.assertArrayEquals(new String[] {"\"Joe & J. Harvey\" <ddd @Org>, JJV @ BBN"}, headers.values().toArray());
	}
	
	@Test
	public void testParsingHeadersWithCrLf() throws IOException {
		String data = "To:  \"Joe & J. Harvey\" <ddd @Org>,\r\n" +
				"        JJV @ BBN";
		Map<String, String> headers = parseStringAsHeaders(data);
		Assert.assertEquals(1, headers.size());
		Assert.assertArrayEquals(new String[] {"to"}, headers.keySet().toArray());
		Assert.assertArrayEquals(new String[] {"\"Joe & J. Harvey\" <ddd @Org>, JJV @ BBN"}, headers.values().toArray());
	}
	
	@Test
	public void testParsingHeadersWithEmptyLine() throws IOException {
		String data = "		";
		Map<String, String> headers = parseStringAsHeaders(data);
		Assert.assertEquals(0, headers.size());
	}
	
}
