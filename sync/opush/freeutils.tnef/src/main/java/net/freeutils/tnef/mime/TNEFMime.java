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

import java.io.*;


import java.util.*;

import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;

import net.freeutils.tnef.*;

/**
 * The <code>TNEFMime</code> class provides high-level utility methods to
 * access TNEF streams and extract their contents, using the JavaMail API.
 *
 * Note: This class is experimental and is intended to show possible uses
 * of the Java TNEF package.
 *
 * @author Amichai Rothman
 * @since 2003-04-29
 */
public class TNEFMime {

    /**
     * Extracts a TNEF attachment from a specified MIME file.
     *
     * @param mimeFilename the filename of a file containing a MIME message
     * @param tnefFilename the filename of a file to which the TNEF attachment
     *        extracted from the MIME message should be written
     * @return true if a TNEF attachment was extracted, false otherwise
     * @throws IOException if an I/O error occurs
     * @throws MessagingException if an error occurs while accessing a mime part
     */
    public static boolean extractTNEF(String mimeFilename, String tnefFilename)
            throws IOException, MessagingException {
        Properties props = new Properties();
        Session session = Session.getInstance(props, null);
        FileInputStream fis = null;
        MimeMessage message = null;
        try {
            fis = new FileInputStream(mimeFilename);
            message = new MimeMessage(session, fis);
            return extractTNEF(message, tnefFilename);
        } finally {
            if (fis != null)
                fis.close();
        }
    }

    /**
     * Extracts a TNEF attachment from a specified message Part (recursively).
     *
     * @param part a message Part which may contain a TNEF attachment or
     *        additional parts
     * @param tnefFilename the filename of a file to which the TNEF attachment
     *        extracted from the Part should be written
     * @return true if a TNEF attachment was extracted, false otherwise
     * @throws IOException if an I/O error occurs
     * @throws MessagingException if an error occurs while accessing a mime part
     */
    public static boolean extractTNEF(Part part, String tnefFilename)
            throws IOException, MessagingException {
        boolean extracted = false;
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart)part.getContent();
            int count = mp.getCount();
            for (int i = 0; i < count; i++)
                if (extractTNEF(mp.getBodyPart(i), tnefFilename))
                    extracted = true;
        } else if (TNEFUtils.isTNEFMimeType(part.getContentType())) {
            InputStream in = part.getInputStream();
            OutputStream out = null;
            try {
                out = new BufferedOutputStream(new FileOutputStream(tnefFilename));
                int b;
                while ((b = in.read()) != -1)
                    out.write(b);
                extracted = true;
            } finally {
                in.close();
                if (out != null)
                    out.close();
            }
        }
        return extracted;
    }

    /**
     * Adds a text part to the given multipart, with the given text and content type,
     * using the UTF-8 encoding (and utf-8 charset parameter).
     * 
     * @param mp the multipart to add the text to
     * @param text the text to add
     * @param contentType the full text mime type
     * @throws MessagingException if an error occurs while accessing a mime part
     */
    protected static void addTextPart(Multipart mp, String text, String contentType) throws MessagingException {
        MimeBodyPart part = new MimeBodyPart();
        part.setText(text, "UTF-8");
        part.setHeader("Content-Type", contentType + "; charset=utf-8");
        mp.addBodyPart(part);
    }

    /**
     * Constructs a TNEFMimeMessage from the given TNEFInputStream.
     * TNEF Attributes are both added to the message, and translated into
     * Mime fields and structure (where applicable).
     *
     * @param session the session used to handle the MimeMessage
     * @param in the TNEFInputStream containing message to convert
     * @return the converted TNEFMimeMessage
     * @throws IOException if an I/O error occurs
     * @throws MessagingException if an error occurs while accessing a mime part
     */
    public static TNEFMimeMessage convert(Session session, TNEFInputStream in)
            throws IOException, MessagingException {
        return convert(session, new net.freeutils.tnef.Message(in));
    }

    /**
     * Constructs a TNEFMimeMessage from the given TNEF Message.
     * TNEF Attributes are both added to the message, and translated into
     * Mime fields and structures (where applicable).
     *
     * @param session the session used to handle the MimeMessage
     * @param message the Message to convert
     * @return the converted TNEFMimeMessage
     * @throws IOException if an I/O error occurs
     * @throws MessagingException if an error occurs while accessing a mime part
     */
    public static TNEFMimeMessage convert(Session session, net.freeutils.tnef.Message message)
            throws IOException, MessagingException {
        TNEFMimeMessage mime = new TNEFMimeMessage(session);
        Multipart mp;
        Attr attr;

        // handle special conversion according to message class
        Attr messageClass = message.getAttribute(Attr.attMessageClass);
        String messageClassName = messageClass == null ? "" : ((String)messageClass.getValue());
        if (messageClassName.equalsIgnoreCase("IPM.Contact")) {
            // convert contact to standard vCard
            mp = ContactHandler.convert(message);
            mime.setContent(mp);
            mime.saveChanges();
            return mime;
        } else if (messageClassName.equalsIgnoreCase("IPM.Microsoft Mail.Read Receipt")) {
            // convert read receipt to standard notification
            mp = ReadReceiptHandler.convert(message);
            mime.setContent(mp);
            mime.saveChanges();
            return mime;
        }

        // add TNEF attributes
        mime.setTNEFAttributes(message.getAttributes());
        // translate TNEF attributes to mime
        // from
        attr = message.getAttribute(Attr.attFrom);
        if (attr != null) {
            net.freeutils.tnef.Address address = (net.freeutils.tnef.Address)attr.getValue();
            mime.setFrom(new InternetAddress(address.getAddress(), address.getDisplayName()));
        }
        // date sent
        attr = message.getAttribute(Attr.attDateSent);
        if (attr != null)
            mime.setSentDate((Date)attr.getValue());
        // recipients
        attr = message.getAttribute(Attr.attRecipTable);
        if (attr != null) {
            MAPIProps[] recipients = (MAPIProps[])attr.getValue();
            String name = null;
            String address = null;
            int type;
            InternetAddress internetAddress = null;
            for (int i = 0; i < recipients.length; i++) {
                name = (String)recipients[i].getPropValue(MAPIProp.PR_DISPLAY_NAME);
                address = (String)recipients[i].getPropValue(MAPIProp.PR_EMAIL_ADDRESS);
                internetAddress = new InternetAddress(address, name);
                type = ((Integer)recipients[i].getPropValue(MAPIProp.PR_RECIPIENT_TYPE)).intValue();

                javax.mail.Message.RecipientType recipientType;
                switch (type) {
                    case MAPIProp.MAPI_TO: recipientType = javax.mail.Message.RecipientType.TO; break;
                    case MAPIProp.MAPI_CC: recipientType = javax.mail.Message.RecipientType.CC; break;
                    case MAPIProp.MAPI_BCC: recipientType = javax.mail.Message.RecipientType.BCC; break;
                    default: throw new IllegalArgumentException("invalid PR_RECIPIENT_TYPE: " + type);
                }
                mime.addRecipient(recipientType, internetAddress);
            }
        }
        // subject
        attr = message.getAttribute(Attr.attSubject);
        if (attr != null)
            mime.setSubject((String)attr.getValue());

        // body
        mp = new MimeMultipart();
        attr = message.getAttribute(Attr.attBody);
        if (attr != null) {
            String text = (String)attr.getValue();
            addTextPart(mp, text, "text/plain");
        }
        MAPIProps props = message.getMAPIProps();
        if (props != null) {
            // compressed RTF body
            RawInputStream ris = (RawInputStream)props.getPropValue(MAPIProp.PR_RTF_COMPRESSED);
            if (ris != null) {
                String text = new String(CompressedRTFInputStream.decompressRTF(ris.toByteArray()));
                addTextPart(mp, text, "text/rtf");
            } else {
                // HTML body (either PR_HTML or PR_BODY_HTML - both have the
                // same ID, but one is a string and one is a byte array)
                Object html = props.getPropValue(MAPIProp.PR_HTML);
                if (html != null) {
                    String text = (html instanceof RawInputStream)
                        ? new String(((RawInputStream)html).toByteArray(), "UTF-8")
                        : (String)html;
                    addTextPart(mp, text, "text/html");
                }
            }
        }

        // add attachments and nested messages
        for (Attachment attachment : message.getAttachments()) {
            TNEFMimeBodyPart part = new TNEFMimeBodyPart();
            if (attachment.getNestedMessage() == null) {
                // add TNEF attributes
                part.setTNEFAttributes(attachment.getAttributes());
                // translate TNEF attributes to Mime
                String filename = attachment.getFilename();
                if (filename != null)
                    part.setFileName(filename);
                String contentType = null;
                if (attachment.getMAPIProps() != null)
                    contentType = (String)attachment.getMAPIProps().getPropValue(MAPIProp.PR_ATTACH_MIME_TAG);
                if (contentType == null && filename != null)
                    contentType = MimetypesFileTypeMap.getDefaultFileTypeMap().getContentType(filename);
                if (contentType == null)
                    contentType = "application/octet-stream";
                DataSource ds = new RawDataSource(attachment.getRawData(), contentType, filename);
                part.setDataHandler(new DataHandler(ds));
                mp.addBodyPart(part);
            } else { // nested message
                MimeMessage nestedMessage = convert(session, attachment.getNestedMessage());
                part.setDataHandler(new DataHandler(nestedMessage, "message/rfc822"));
                mp.addBodyPart(part);
            }
        }
        mime.setContent(mp);
        mime.saveChanges();
        return mime;
    }

    /**
     * Converts TNEF parts within given message to MIME-structured parts (recursively).
     *
     * @param session a Session instance used in creating new Parts
     * @param message the MIME message to convert
     * @return the given message instance, with TNEF attachment parts replaced
     *         by MIME-structured parts
     * @throws IOException if an I/O error occurs
     * @throws MessagingException if an error occurs while accessing a mime part
     */
    public static MimeMessage convert(Session session, MimeMessage message)
            throws IOException, MessagingException {
        return convert(session, message, true);
    }

    /**
     * Converts TNEF parts within given message to MIME-structured parts (recursively).
     *
     * @param session a Session instance used in creating new Parts
     * @param message the MIME message to convert
     * @param embed if true, the messages obtained from converted
     *        TNEF parts are embedded in the message in place of the TNEF parts.
     *        If false, the first TNEF part encountered is converted to a mime
     *        message and returned.
     * @return the given message instance, with TNEF attachment parts replaced
     *         by MIME-structured parts
     * @throws IOException if an I/O error occurs
     * @throws MessagingException if an error occurs while accessing a mime part
     */
    public static MimeMessage convert(Session session, MimeMessage message,
            boolean embed) throws IOException, MessagingException {
        message = (MimeMessage)convert(session, (Part)message, embed);
        message.saveChanges();
        return message;
    }

    /**
     * Converts TNEF parts within given part to MIME-structured parts (recursively).
     *
     * @param session a Session instance used in creating new Parts
     * @param part the MIME part to convert
     * @return the original part, with TNEF attachment parts replaced
     *         by MIME-structured parts. If the part itself is a TNEF attachment,
     *         a converted MimeMessage is returned instead.
     * @throws IOException if an I/O error occurs
     * @throws MessagingException if an error occurs while accessing a mime part
     */
    public static Part convert(Session session, Part part) throws IOException, MessagingException {
        return convert(session, part, true);
    }

    /**
     * Converts TNEF parts within given part to MIME-structured parts (recursively).
     *
     * @param session a Session instance used in creating new Parts
     * @param part the MIME part to convert
     * @param embed if true, the messages obtained from converted
     *        TNEF parts are embedded in the part in place of the TNEF parts.
     *        If false, the first TNEF part encountered is converted to a mime
     *        message and returned.
     * @return the original part, with TNEF attachment parts replaced
     *         by MIME-structured parts. If the part itself is a TNEF attachment,
     *         a converted MimeMessage is returned instead.
     * @throws IOException if an I/O error occurs
     * @throws MessagingException if an error occurs while accessing a mime part
     */
    public static Part convert(Session session, Part part, boolean embed)
            throws IOException, MessagingException {
        if (part.isMimeType("multipart/*")) {
            Multipart mp = (Multipart)part.getContent();
            int count = mp.getCount();
            for (int i = 0; i < count; i++) {
                Part mpPart = mp.getBodyPart(i);
                Part convertedPart = convert(session, mpPart);
                if (mpPart != convertedPart) {
                    if (!embed)
                        return convertedPart;
                    mp.removeBodyPart(i);
                    TNEFMimeBodyPart newPart = new TNEFMimeBodyPart();
                    newPart.setDataHandler(new DataHandler(convertedPart, "message/rfc822"));
                    mp.addBodyPart(newPart, i);
                }
            }
            part.setContent(mp);
        } else if (TNEFUtils.isTNEFMimeType(part.getContentType())) {
            TNEFInputStream in = new TNEFInputStream(part.getInputStream());

            if (part instanceof MimeMessage) {
                // if the root message is the attachment itself, a.k.a Summary TNEF (STNEF)
                MimeMessage mm = (MimeMessage)part;
                MimeMessage converted = convert(session, in);
                // remove the attachment content
                mm.removeHeader("Content-Type");
                mm.removeHeader("Content-Transfer-Encoding");
                mm.removeHeader("Content-Disposition");
                // and add the converted content instead, preserving original headers
                mm.setContent(converted.getContent(), converted.getContentType());
            } else {
                // if it's an attachment within a message
                part = convert(session, in);
            }
        }
        return part;
    }

    /**
     * Main entry point for command-line utility.
     *
     * @param args the command-line arguments
     */
    public static void main(String[] args) {
        String infile = null;
        String outfile = null;

        String usage = new StringBuffer()
            .append("\nUsage:\n\n")
            .append("  java net.freeutils.tnef.mime.TNEFMime -[e|c|w] <infile> <outfile>\n")
            .append("\nOptions:\n\n")
            .append("  e Extract the TNEF attachment from a MIME file.\n")
            .append("  c Convert a MIME file containing a TNEF attachment to a MIME file\n")
            .append("    with a nested rfc822 message.\n")
            .append("  w Convert a TNEF attachment to a MIME file.\n")
            .append("\nExamples:\n\n")
            .append("  java net.freeutils.tnef.mime.TNEFMime -e c:\\temp\\1.mime c:\\temp\\winmail.dat\n")
            .append("  java net.freeutils.tnef.mime.TNEFMime -c c:\\temp\\1.mime c:\\temp\\2.mime\n")
            .append("  java net.freeutils.tnef.mime.TNEFMime -w c:\\temp\\winmail.dat c:\\temp\\1.mime\n")
            .toString();

        if (args.length < 3) {
            System.out.println(usage);
            System.exit(1);
        }

        String options = args[0].toLowerCase();
        if (options.startsWith("-") || options.startsWith("/"))
            options = options.substring(1).trim();
        infile = args[1];
        outfile = args[2];

        System.out.println("Processing file " + infile);
        Session session = Session.getInstance(new Properties());
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            if ("e".equals(options)) {
                extractTNEF(infile, outfile);
            } else if ("c".equals(options)) {
                in = new FileInputStream(infile);
                MimeMessage mime = new MimeMessage(session, in);
                out = new FileOutputStream(outfile);
                mime = convert(session, mime);
                mime.writeTo(out);
            } else if ("w".equals(options)) {
                in = new FileInputStream(infile);
                TNEFInputStream tin = new TNEFInputStream(in);
                out = new FileOutputStream(outfile);
                MimeMessage mime = convert(session, tin);
                mime.writeTo(out);
            } else {
                System.out.println("\nInvalid option: " + options);
                System.out.println(usage);
                System.exit(1);
            }
            System.out.println("Done.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (in != null)
                    in.close();
                if (out != null)
                    out.close();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
    }

}
