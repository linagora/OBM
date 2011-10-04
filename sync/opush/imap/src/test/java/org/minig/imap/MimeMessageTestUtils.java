package org.minig.imap;

import java.util.Collection;
import java.util.Iterator;

import org.junit.Assert;
import org.minig.imap.mime.IMimePart;
import org.minig.imap.mime.MimeAddress;

public class MimeMessageTestUtils {

	private static String prefixMessage(IMimePart expected) {
		return "part with address " + expected.getAddress();
	}
	
	public static void checkMimeTree(IMimePart expected, IMimePart actual) {
		Assert.assertEquals(prefixMessage(expected) + " has wrong number of children", 
				expected.getChildren().size(), actual.getChildren().size());
		Assert.assertEquals(prefixMessage(expected), expected.getMimeType(), actual.getMimeType());
		Assert.assertEquals(prefixMessage(expected), expected.getMimeSubtype(), actual.getMimeSubtype());
		Assert.assertEquals(prefixMessage(expected), expected.getContentTransfertEncoding(), actual.getContentTransfertEncoding());
		Assert.assertEquals(prefixMessage(expected), expected.getContentId(), actual.getContentId());
		Assert.assertArrayEquals(prefixMessage(expected), expected.getBodyParams().toArray(), actual.getBodyParams().toArray());
		Iterator<IMimePart> expectedParts = expected.getChildren().iterator();
		Iterator<IMimePart> actualParts = actual.getChildren().iterator();
		while (actualParts.hasNext()) {
			checkMimeTree(expectedParts.next(), actualParts.next());
		}
	}

	public static IMimePart getPartByAddress(IMimePart message, MimeAddress addr) {
		Collection<IMimePart> children = message.getChildren();
		for (IMimePart part: children) {
			if (addr.equals(part.getAddress())) {
				return part;
			}
			IMimePart result = getPartByAddress(part, addr);
			if (result != null) {
				return result;
			}
		}
		return null;
	}
	
}
