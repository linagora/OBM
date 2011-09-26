/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   MiniG.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.obm.push.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.james.mime4j.message.BinaryBody;
import org.apache.james.mime4j.message.BodyFactory;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.Message;
import org.apache.james.mime4j.message.Multipart;
import org.apache.james.mime4j.message.TextBody;
import org.apache.james.mime4j.storage.Storage;
import org.apache.james.mime4j.storage.StorageOutputStream;
import org.apache.james.mime4j.storage.StorageProvider;

public class Mime4jUtils {

	private Mime4jUtils() {
	}

	public static Message getNewMessage() {
		return new Message();
	}

	public static Multipart getMixedMultiPart() {
		return new Multipart("mixed");
	}

	public static void attach(Multipart multipart, InputStream in,
			String fileName, String mimeType) throws FileNotFoundException,
			IOException {
		BodyFactory bodyFactory = new BodyFactory();
		BodyPart attach = createBinaryPart(bodyFactory, in, mimeType, fileName);
		multipart.addBodyPart(attach);
	}

	/**
	 * Creates a text part from the specified string.
	 */
	public static BodyPart createTextPart(String text, String subtype) {
		text.replace("\r\n", "\n").replace("\n", "\r\n");
		BodyFactory bodyFactory = new BodyFactory();
		// Use UTF-8 to encode the specified text
		TextBody body = bodyFactory.textBody(text, "UTF-8");
		// Create a text/plain body part
		BodyPart bodyPart = new BodyPart();
		bodyPart.setText(body, subtype);
		bodyPart.setContentTransferEncoding("quoted-printable");

		return bodyPart;
	}
	
	public static InputStream toInputStream(Message message) throws IOException{
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		message.writeTo(out);
		message.dispose();
		return new ByteArrayInputStream(out.toByteArray());
	}

	private static BodyPart createBinaryPart(BodyFactory bodyFactory,
			InputStream in, String mimeType, String fileName)
			throws IOException {
		// Create a binary message body from the stream
		StorageProvider storageProvider = bodyFactory.getStorageProvider();
		Storage storage = storeStream(storageProvider, in);
		BinaryBody body = bodyFactory.binaryBody(storage);

		// Create a body part with the correct MIME-type and transfer encoding
		BodyPart bodyPart = new BodyPart();
		bodyPart.setBody(body, mimeType);
		if (!mimeType.endsWith("/rfc822")) {
			bodyPart.setContentTransferEncoding("base64");
		}
		// Specify a filename in the Content-Disposition header (implicitly sets
		// the disposition type to "attachment")
		bodyPart.setFilename(fileName);

		return bodyPart;
	}

	/**
	 * Stores the specified stream in a Storage object.
	 */
	private static Storage storeStream(StorageProvider storageProvider,
			InputStream in) throws IOException {
		// An output stream that is capable of building a Storage object.
		StorageOutputStream out = storageProvider.createStorageOutputStream();

		FileUtils.transfer(in, out, false);

		// Implicitly closes the output stream and returns the data that has
		// been written to it.
		return out.toStorage();
	}
}
