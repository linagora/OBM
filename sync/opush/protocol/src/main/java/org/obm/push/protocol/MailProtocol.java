package org.obm.push.protocol;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.obm.configuration.EmailConfiguration;
import org.obm.push.exception.QuotaExceededException;
import org.obm.push.protocol.bean.MailRequest;
import org.obm.push.protocol.request.ActiveSyncRequest;
import org.obm.push.utils.stream.SizeLimitExceededException;
import org.obm.push.utils.stream.SizeLimitingInputStream;

import com.google.common.io.ByteStreams;
import com.google.inject.Inject;
import com.google.inject.Singleton;

@Singleton
public class MailProtocol {
	
	private EmailConfiguration emailConfiguration;
	
	@Inject
	/* package */ MailProtocol(EmailConfiguration emailConfiguration) {
		this.emailConfiguration = emailConfiguration;
	}

	public MailRequest getRequest(ActiveSyncRequest request) throws IOException, QuotaExceededException {
		String collectionId = request.getParameter("CollectionId");
		String serverId = request.getParameter("ItemId");
		byte[] mailContent = streamBytes(request.getInputStream());
		return new MailRequest(collectionId, serverId, getSaveInSentParameter(request) , mailContent);
	}

	private boolean getSaveInSentParameter(ActiveSyncRequest request) {
		boolean saveInSent = false;
		String sis = request.getParameter("SaveInSent");
		if (sis != null) {
			saveInSent = sis.equalsIgnoreCase("T");
		}
		return saveInSent;
	}
	
	private byte[] streamBytes(InputStream in)
			throws IOException, QuotaExceededException {
		final int maxSize = emailConfiguration.getMessageMaxSize();
		SizeLimitingInputStream sizeLimitingInputStream = new SizeLimitingInputStream(in, maxSize);
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		try {
			ByteStreams.copy(sizeLimitingInputStream, byteArrayOutputStream);
			return byteArrayOutputStream.toByteArray();
		} catch (SizeLimitExceededException e) {
			throw new QuotaExceededException("The message must be smaller than " + maxSize, maxSize, 
					byteArrayOutputStream.toByteArray());
		}
	}

}
