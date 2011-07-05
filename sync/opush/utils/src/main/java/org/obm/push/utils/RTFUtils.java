package org.obm.push.utils;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.rtf.RTFEditorKit;

import net.freeutils.tnef.CompressedRTFInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class RTFUtils {

	private static final Log logger = LogFactory.getLog(RTFUtils.class);

	public static String getFolderId(String devId, String dataClass) {
		return devId + "\\" + dataClass;
	}

	public static String extractB64CompressedRTF(String b64) {
		String ret = "";
		try {
			byte[] bin = Base64.decode(b64.toCharArray());
			if (bin.length > 0) {
				ByteArrayInputStream in = new ByteArrayInputStream(bin);
				CompressedRTFInputStream cin = new CompressedRTFInputStream(in);

				String rtfDecompressed = FileUtils.streamString(cin, true);
				ret = extractRtfText(new ByteArrayInputStream(rtfDecompressed
						.getBytes()));
			}
		} catch (Exception e) {
			logger.error("error extracting compressed rtf", e);
		}
		return ret;
	}

	public static String extractCompressedRTF(InputStream in) {
		String ret = "";
		try {
			CompressedRTFInputStream cin = new CompressedRTFInputStream(in);
			String rtfDecompressed = FileUtils.streamString(cin, true);
			ret = extractRtfText(new ByteArrayInputStream(rtfDecompressed
					.getBytes()));
		} catch (Exception e) {
			logger.error("error extracting compressed rtf", e);
		}
		return ret;
	}

	private static String extractRtfText(InputStream stream)
			throws IOException, BadLocationException {
		RTFEditorKit kit = new RTFEditorKit();
		Document doc = kit.createDefaultDocument();
		kit.read(stream, doc, 0);

		return doc.getText(0, doc.getLength());
	}

}
