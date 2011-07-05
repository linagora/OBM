/*
 *  (c) copyright 2003-2009 Amichai Rothman
 *
 *  This file is part of the Java TNEF package.
 *
 *  The Java TNEF package is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  The Java TNEF package is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.freeutils.tnef.mime;

import java.io.IOException;
import java.text.DateFormat;
import java.util.Date;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;

import net.freeutils.tnef.*;


/**
 * The <code>ReadReceiptHandler</code> class converts a read receipt 
 * from a TNEF message into a standard RFC 2298 notification message.
 *
 * @author Amichai Rothman
 * @since 2007-04-27
 */
public class ReadReceiptHandler {

    /**
     * Converts a TNEF message containing a read receipt into an RFC 2298 
     * notification message.
     *
     * @param message the tnef Message containing read receipt to convert
     * @return a Part containing the read receipt text
     * @throws IOException if an I/O error occurs
     * @throws MessagingException if an error occurs while accessing a mime part
     */
    public static Multipart convert(net.freeutils.tnef.Message message)
            throws IOException, MessagingException {

        // get required data from message
        MAPIProps props = message.getMAPIProps();
        String recipient = (String)props.getPropValue(MAPIProp.PR_ORIGINAL_DISPLAY_TO);
        String subject = (String)props.getPropValue(MAPIProp.PR_CONVERSATION_TOPIC);
        Date sentDate = (Date)props.getPropValue(MAPIProp.PR_ORIGINAL_SUBMIT_TIME);
        Date readDate = (Date)props.getPropValue(MAPIProp.PR_REPORT_TIME);
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);

        // create multipart
        MimeMultipart multipart = new MimeMultipart("report; report-type=disposition-notification");
        MimeBodyPart part;

        // add text part
        StringBuffer text = new StringBuffer();
        text.append("Your message\r\n");
        text.append("\r\n      To:\t");
        if (recipient != null)
            text.append(recipient);
        text.append("\r\n      Subject:\t");
        if (subject != null)
            text.append(subject);
        text.append("\r\n      Sent:\t");
        if (sentDate != null)
            text.append(format.format(sentDate));
        if (readDate != null)
            text.append("\r\n\r\nwas read on ").append(format.format(readDate)).append('.');
        text.append("\r\n");
        part = new MimeBodyPart();
        part.setText(text.toString(), "UTF-8");
        multipart.addBodyPart(part);

        // add notification part
        text.setLength(0);
        text.append("Original-Recipient: rfc822;").append(recipient).append("\r\n")
            .append("Final-Recipient: rfc822;").append(recipient).append("\r\n")
            .append("Disposition: manual-action/MDN-sent-manually; displayed").append("\r\n");
        part = new MimeBodyPart();
        part.setText(text.toString(), "UTF-8");
        part.setHeader("Content-Type", "message/disposition-notification; charset=UTF-8");
        multipart.addBodyPart(part);

        return multipart;
    }

}
