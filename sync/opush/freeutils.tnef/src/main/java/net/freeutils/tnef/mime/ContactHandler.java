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
import java.text.SimpleDateFormat;
import java.util.*;

import javax.mail.*;
import javax.mail.internet.*;

import net.freeutils.tnef.*;


/**
 * The <code>ContactHandler</code> class converts contacts from a TNEF
 * message into a standard vCard 3.0 attachment, as per RFC 2426.
 *
 * @author Amichai Rothman
 * @since 2007-04-27
 */
public class ContactHandler {

    // the definition of all fields used in conversion from tnef to vCard 
    static ContactField[] contactFields = {
        new ContactField("BEGIN", "VCARD"),  // must be first
        new ContactField("VERSION", "3.0"),
        new ContactField("FN", new Integer[] { 
            new Integer(MAPIProp.PR_GIVEN_NAME),
            new Integer(MAPIProp.PR_MIDDLE_NAME),
            new Integer(MAPIProp.PR_SURNAME),
            new Integer(MAPIProp.PR_GENERATION) },
            ContactField.MUST,"%s %s %s %s"),
        new ContactField("N", new Integer[] {
            new Integer(MAPIProp.PR_SURNAME), 
            new Integer(MAPIProp.PR_GIVEN_NAME),
            new Integer(MAPIProp.PR_MIDDLE_NAME),
            new Integer(MAPIProp.PR_DISPLAY_NAME_PREFIX),
            new Integer(MAPIProp.PR_GENERATION) },
            ContactField.MUST,"%s;%s;%s;%s;%s"),
        new ContactField("NICKNAME", new Integer(MAPIProp.PR_NICKNAME)),
        //"PHOTO"
        new ContactField("BDAY", new Integer(MAPIProp.PR_BIRTHDAY),
                ContactField.NONE,"yyyy-MM-dd'T'HH:mm:ss'Z'"),
        new ContactField("ADR;TYPE=WORK", new Integer[] {
            null,
            new Integer(MAPIProp.PR_OFFICE_LOCATION),
            new Integer(MAPIProp.PR_STREET_ADDRESS), 
            new Integer(MAPIProp.PR_LOCALITY), 
            new Integer(MAPIProp.PR_STATE_OR_PROVINCE), 
            new Integer(MAPIProp.PR_POSTAL_CODE),
            new Integer(MAPIProp.PR_COUNTRY) },
            ContactField.NONE,"%s;%s;%s;%s;%s;%s;%s"),
        new ContactField("LABEL;TYPE=WORK", new Integer[] {
            new Integer(MAPIProp.PR_OFFICE_LOCATION),
            new Integer(MAPIProp.PR_STREET_ADDRESS), 
            new Integer(MAPIProp.PR_LOCALITY), 
            new Integer(MAPIProp.PR_STATE_OR_PROVINCE), 
            new Integer(MAPIProp.PR_POSTAL_CODE),
            new Integer(MAPIProp.PR_COUNTRY) },
            ContactField.NONE,"%s\r\n%s\r\n%s, %s %s\r\n%s"),
        new ContactField("ADR;TYPE=HOME", new Integer[] {
            null,
            null,
            new Integer(MAPIProp.PR_HOME_ADDRESS_STREET), 
            new Integer(MAPIProp.PR_HOME_ADDRESS_CITY), 
            new Integer(MAPIProp.PR_HOME_ADDRESS_STATE_OR_PROVINCE), 
            new Integer(MAPIProp.PR_HOME_ADDRESS_POSTAL_CODE),
            new Integer(MAPIProp.PR_HOME_ADDRESS_COUNTRY) },
            ContactField.NONE,"%s;%s;%s;%s;%s;%s;%s"),
        new ContactField("LABEL;TYPE=HOME", new Integer[] {
            new Integer(MAPIProp.PR_HOME_ADDRESS_STREET), 
            new Integer(MAPIProp.PR_HOME_ADDRESS_CITY), 
            new Integer(MAPIProp.PR_HOME_ADDRESS_STATE_OR_PROVINCE), 
            new Integer(MAPIProp.PR_HOME_ADDRESS_POSTAL_CODE),
            new Integer(MAPIProp.PR_HOME_ADDRESS_COUNTRY) },
            ContactField.NONE,"%s\r\n%s, %s %s\r\n%s"),
        new ContactField("ADR;TYPE=POSTAL", new Integer[] {
            null,
            null,
            new Integer(MAPIProp.PR_OTHER_ADDRESS_STREET), 
            new Integer(MAPIProp.PR_OTHER_ADDRESS_CITY), 
            new Integer(MAPIProp.PR_OTHER_ADDRESS_STATE_OR_PROVINCE), 
            new Integer(MAPIProp.PR_OTHER_ADDRESS_POSTAL_CODE),
            new Integer(MAPIProp.PR_OTHER_ADDRESS_COUNTRY) },
            ContactField.NONE,"%s;%s;%s;%s;%s;%s;%s"),
        new ContactField("LABEL;TYPE=POSTAL", new Integer[] {
            new Integer(MAPIProp.PR_OTHER_ADDRESS_STREET), 
            new Integer(MAPIProp.PR_OTHER_ADDRESS_CITY), 
            new Integer(MAPIProp.PR_OTHER_ADDRESS_STATE_OR_PROVINCE), 
            new Integer(MAPIProp.PR_OTHER_ADDRESS_POSTAL_CODE),
            new Integer(MAPIProp.PR_OTHER_ADDRESS_COUNTRY) },
            ContactField.NONE,"%s\r\n%s, %s %s\r\n%s"),
        new ContactField("TEL;TYPE=HOME,VOICE", new Integer(MAPIProp.PR_HOME_TELEPHONE_NUMBER)),
        new ContactField("TEL;TYPE=HOME", new Integer(MAPIProp.PR_HOME2_TELEPHONE_NUMBER)),
        new ContactField("TEL;TYPE=HOME,FAX", new Integer(MAPIProp.PR_HOME_FAX_NUMBER)),
        new ContactField("TEL;TYPE=WORK,VOICE", new Integer(MAPIProp.PR_BUSINESS_TELEPHONE_NUMBER)),
        new ContactField("TEL;TYPE=WORK,VOICE", new Integer(MAPIProp.PR_BUSINESS2_TELEPHONE_NUMBER)),
        new ContactField("TEL;TYPE=WORK,FAX", new Integer(MAPIProp.PR_BUSINESS_FAX_NUMBER)),
        new ContactField("TEL;TYPE=CELL,VOICE", new Integer(MAPIProp.PR_MOBILE_TELEPHONE_NUMBER)),
        new ContactField("TEL;TYPE=CAR,VOICE", new Integer(MAPIProp.PR_CAR_TELEPHONE_NUMBER)),
        new ContactField("TEL;TYPE=PREF,VOICE", new Integer(MAPIProp.PR_CALLBACK_TELEPHONE_NUMBER)),
        new ContactField("TEL;TYPE=VOICE", new Integer(MAPIProp.PR_OTHER_TELEPHONE_NUMBER)),
        new ContactField("TEL;TYPE=PAGER,VOICE", new Integer(MAPIProp.PR_PAGER_TELEPHONE_NUMBER)),
        new ContactField("TEL;TYPE=ISDN", new Integer(MAPIProp.PR_ISDN_NUMBER)),
        new ContactField("TEL;TYPE=PREF", new Integer(MAPIProp.PR_PRIMARY_TELEPHONE_NUMBER)),
        new ContactField("EMAIL;TYPE=PREF,INTERNET",
                new MAPIPropName(MAPIProp.GUID_CDOPROPSETID3,0x8083)), // CdoContact_EmailEmailAddress
        new ContactField("EMAIL;TYPE=TLX", new Integer(MAPIProp.PR_TELEX_NUMBER)),
        //"MAILER"
        //"TZ"
        //"GEO"
        new ContactField("TITLE", new Integer(MAPIProp.PR_TITLE)),
        new ContactField("ROLE", new Integer(MAPIProp.PR_PROFESSION)),
        //"LOGO"
        //"AGENT"
        new ContactField("ORG", new Integer[] {
            new Integer(MAPIProp.PR_COMPANY_NAME),
            new Integer(MAPIProp.PR_DEPARTMENT_NAME) },
            ContactField.NONE,"%s;%s"),
        //"CATEGORIES"
        new ContactField("NOTE", new Integer(Attr.attBody),ContactField.ATTR,null),
        //"PRODID"
        //"REV"
        //"SORT-STRING
        //"SOUND"
        //"UID"
        new ContactField("URL;TYPE=WORK", new Integer(MAPIProp.PR_BUSINESS_HOME_PAGE)),
        //"CLASS"
        new ContactField("KEY;TYPE=X509", new Integer(MAPIProp.PR_USER_X509_CERTIFICATE),
                ContactField.NONE | ContactField.BINARY | ContactField.CERT,null),
        new ContactField("END", "VCARD") // must be last
    };

    /**
     * Converts a TNEF message containing a contact into a vCard 3.0 attachment,
     * as per RFC 2426.
     *
     * @param message the tnef Message containing contact to convert
     * @return a BodyPart containing a standard vCard attachment
     * @throws IOException if an I/O error occurs
     * @throws MessagingException if an error occurs while accessing a mime part
     */
    public static Multipart convert(net.freeutils.tnef.Message message)
            throws IOException, MessagingException {

        // convert content
        StringBuffer vcard = new StringBuffer();
        for (int i = 0; i < contactFields.length; i++) {
            String v = contactFields[i].getValue(message);
            if (v != null)
                vcard.append(v);
        }

        // create attachment
        String name = (String)message.getAttribute(Attr.attSubject).getValue() + ".vcf";
        name = MimeUtility.encodeWord(name, "UTF-8", null);
        MimeBodyPart part = new MimeBodyPart();
        part.setText(vcard.toString(), "UTF-8");
        part.setHeader("Content-Type", "text/x-vcard; charset=UTF-8; name=\"" + name + '\"');
        part.setHeader("Content-Disposition", "attachment; filename=\"" + name + '\"');
        MimeMultipart mp = new MimeMultipart();
        mp.addBodyPart(part);
        return mp;
    }

    /**
     * The internal <code>ContactField</code> class represents a mapping of a single
     * field from its TNEF source to its vCard destination, and all methods required
     * to perform the field's conversion: parameters, types, formatting, escaping,
     * encoding, folding, etc, according to the vCard RFC.
     */
    static class ContactField {

        /**
         * ContactField flag constant.
         */
        public static final int 
            NONE      = 0,
            MUST      = 1,
            ATTR      = 2,
            BINARY    = 4,
            CERT      = 8;
    
        String type;    // the vCard prefix string
        Object src;     // the corresponding TNEF source object
        int flags;      // flags used to determine the conversion type
        String format;  // formatting string for output (where applicable)
        
        /**
         * Constructs a Contact field with all necessary details.
         * 
         * @param type the field type (excluding the encoding parameter)
         * @param src the TNEF source object
         * @param flags the conversion flags
         * @param format the format string (null if not applicable)
         */
        public ContactField(String type, Object src, int flags, String format) {
            this.type = type;
            this.src = src;
            this.flags = flags;
            this.format = format;
            if (isFlag(BINARY))
                this.type += ";ENCODING=b";
        }

        /**
         * Constructs a Contact field with given type and source,
         * and with the NONE flag and a null format string.
         * 
         * @param type the field type (excluding the encoding parameter)
         * @param src the TNEF source object
         */
        public ContactField(String type, Object src) {
            this(type, src, NONE, null);
        }

        /**
         * Returns the vCard field string corresponding to this contact field,
         * the data itself taken from the given TNEF message.
         * The returned string is properly encoded, escaped, formatted, folded
         * etc. according to the vCard RFC.
         * 
         * @param message the TNEF message from which the field data is taken
         * @return the vCard field string with the required contact data
         * @throws IOException if an error occurs while reading from message
         */
        public String getValue(net.freeutils.tnef.Message message) throws IOException {
            String v = null;
            if (src instanceof Integer) {
                int id = ((Integer)src).intValue();
                if (isFlag(ATTR)) {
                    Attr attr = message.getAttribute(id);
                    if (attr != null)
                        v = toVCardField(toString(attr.getValue()), true);
                } else {
                    v = toVCardFields(message.getMAPIProps().getProp(id));
                }
            } else if (src instanceof MAPIPropName) {
                MAPIPropName propname = (MAPIPropName)src;
                v = toVCardFields(message.getMAPIProps().getProp(propname));
            } else if (src instanceof Integer[]) {
                Integer[] ids = (Integer[])src;
                int len = ids.length;
                String[] vals = new String[len];
                boolean hasValue = false;
                for (int i = 0; i < len; i++) {
                    Integer id = ids[i];
                    vals[i] = (id == null) ? null :
                        escape((String)message.getMAPIProps().getPropValue(id.intValue()));
                    if (vals[i] != null)
                        hasValue = true;
                }
                if (hasValue)
                    v = toVCardField(format(vals), false);
            } else if (src instanceof String) {
                v = toVCardField((String)src, true);
            } else {
                throw new IllegalArgumentException("Invalid source: " + src);
            }

            return v;
        }

        private String toVCardFields(MAPIProp prop) throws IOException {
            String v = null;
            if (prop != null) {
                MAPIValue[] vals = prop.getValues();
                if (vals != null) {
                    v = "";
                    for (int i = 0; i < vals.length; i++) {
                        if (vals[i] != null)
                            v += toVCardField(toString(vals[i].getValue()), true);
                    }
                }
            }
            return v;
        }

        String toVCardField(String v, boolean escape) {
            // escape the value
            if (escape)
                v = escape(v);

            // treat empty strings as no value
            if (v != null && v.trim().length() == 0)
                v = null;

            // force required attributes to be specified
            if (isFlag(MUST) && v == null)
                v = "";

            if (v != null) {
                // escape line breaks within value
                v = TNEFUtils.replace(v, "\r\n", "\\n");
                v = TNEFUtils.replace(v, "\n", "\\n");
                // prepare final value and fold
                v = fold(type + ":" + v) + "\r\n";
            }
            return v;
        }

        /**
         * Returns whether this ContactField contains given flag.
         * 
         * @param flag the flag to check
         * @return true if this ContactField contains the given flag,
         *         false otherwise
         */
        boolean isFlag(int flag) {
            return (flags & flag) != 0;
        }

        /**
         * Returns a string representation of the given value.
         * 
         * @param val the value to represent as a string
         * @return a string representation of the given value
         * @throws IOException if an error occurs while processing value
         */
        String toString(Object val) throws IOException {
            String v = null;
            if (val != null) {
                if (val instanceof Date) {
                    // format the date according to given format string
                    SimpleDateFormat df = new SimpleDateFormat(format);
                    df.setTimeZone(TimeZone.getTimeZone("GMT")); // mapi props should be GMT
                    v = df.format((Date)val);
                } else if (val instanceof RawInputStream) {
                    RawInputStream ris = (RawInputStream)val;
                    // special handling for certificates
                    if (isFlag(CERT)) {
                        ris = getTLVProp(ris, 0x0003); // 0x003 is the actual certificate
                        // make sure it's a valid cert - it may just be the cert hash
                        if (ris != null && ris.available() <= 32)
                            ris = null;
                    }
                    // encode in base64
                    if (ris != null) {
                        try {
                            ByteArrayOutputStream bout = new ByteArrayOutputStream();
                            
                            OutputStream out = MimeUtility.encode(bout, "base64");
                            out.write(ris.toByteArray());
                            out.flush();
                            v = bout.toString("US-ASCII");
                            v = TNEFUtils.replace(v, "\r\n", ""); // remove MimeUtility's folding
                        } catch (MessagingException ignore) {}
                    }
                } else {
                    v = val.toString();
                }
            }
            return v;
        }

        /**
         * Folds the given string according to the vCard RFC.
         * 
         * @param v the value to fold
         * @return the folded string
         */
        String fold(String v) {
            int vlen = v.length();
            if (vlen > 75) { // apply folding only if necessary
                StringBuffer folded = new StringBuffer(vlen * (int)(1 + 3f / 75));
                int breakpos = -1;
                int count = 0;
                for (int i = 0; i < vlen; i++) {
                    char ch = v.charAt(i);
                    // mark potential break positions
                    if (Character.isWhitespace(ch) || ch == '\\')
                        breakpos = i;
                    // break the line when we have enough chars on it
                    if (count == 74) {
                        if (breakpos == -1) // if there's no preferred break position,
                            breakpos = i; // force a break at end of line
                        folded.append(v.substring(i - count, breakpos)).append("\r\n ");
                        count = i - breakpos;
                        breakpos = -1;
                    }
                    count++;
                }
                // add the last partial line
                if (count > 0)
                    folded.append(v.substring(vlen - count));
                v = folded.toString();
            }
            return v;
        }

        /**
         * Formats a series of values according to this field's format string.
         * 
         * An escape sequence within the format string consists of a '%' character
         * followed by a format identifier char:
         *   '%s' outputs the corresponding output value.
         *   '%%' outputs a literal '%' character.
         * All other characters are copied to the output as-is.
         * 
         * @param vals the values to be used in formatting the output string
         * @return the formatted string
         * @throws IllegalArgumentException if the format string is invalid,
         *         or the given value array contains less elements than the
         *         number of '%s' sequences in the format string
         */
        String format(String[] vals) {
            StringBuffer sb = new StringBuffer();
            int len = format.length();
            int ind = 0;
            for (int i = 0; i < len; i++) {
                char c = format.charAt(i);
                if (c == '%') { // process special format sequence
                    // make sure the sequence is complete
                    if (++i >= len)
                        throw new IllegalArgumentException(
                            "format error at position " + i + ": " + format);
                    // get format identifier char and output the appropriate value
                    c = format.charAt(i);
                    if (c == 's') { // insert value string
                        if (ind >= vals.length)
                            throw new IllegalArgumentException(
                                "format error (too few parameters): " + format);
                        if (vals[ind] != null)
                            sb.append(vals[ind]);
                        ind++;
                    } else if (c == '%') { // insert explicit '%' char
                        sb.append('%');
                    } else {
                        throw new IllegalArgumentException(
                            "format error at position " + i + ": " + format);
                    }
                } else { // copy format char as-is
                    sb.append(c);
                }
            }
            return sb.toString();
        }

        /**
         * Escapes the given string according to the vCard RFC.
         * 
         * @param v the string to escape
         * @return the escaped string
         */
        String escape(String v) {
            if (v == null)
                return null;
            v = TNEFUtils.replace(v, "\r\n", "\\n");
            v = TNEFUtils.replace(v, "\n", "\\n");
            v = TNEFUtils.replace(v, ",", "\\,");
            v = TNEFUtils.replace(v, ";", "\\;");
            return v;
        }

        /**
         * Retrieves the value of the property with given tag
         * from within a stream, which is treated as a
         * tag-length-value list of properties.
         * 
         * @param in the RawInputStream containing the tag-length-value data
         * @param tag the tag whose value is requested
         * @return the requested tag's value, or null if not found
         * @throws IOException if an I/O error occurs
         */
        RawInputStream getTLVProp(RawInputStream in, int tag) throws IOException {
            in = new RawInputStream(in); // make a copy, don't modify original
            while (in.available() >= 4) {
                int tg = in.readU16();
                int len = in.readU16() - 4; // subtract the tag and length fields
                if (tg == tag)
                    return (RawInputStream)in.newStream(0, len);
                in.skip(len);
            }
            return null;
        }
    }

}
