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

package net.freeutils.tnef;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The <code>Message</code> class encapsulates a TNEF message.
 * 
 * @author Amichai Rothman
 * @since 2003-04-25
 */
public class Message {

	List<Attr> attributes;
	List<Attachment> attachments;

	/**
	 * Constructs an empty Message.
	 */
	public Message() {
		attributes = new ArrayList<Attr>();
		attachments = new ArrayList<Attachment>();
	}

	/**
	 * Constructs a Message using the given TNEFInputStream.
	 * 
	 * @param in
	 *            the TNEFInputStream containing message data
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public Message(TNEFInputStream in) throws IOException {
		this();
		read(in);
	}

	/**
	 * Reads all Message contents from the given TNEFInputStream.
	 * 
	 * @param in
	 *            the TNEFInputStream containing message data
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	protected void read(TNEFInputStream in) throws IOException {
		Attachment attachment = null;
		Attr attr;
		while ((attr = in.readAttr()) != null) {
			try {
				switch (attr.getLevel()) {

				case Attr.LVL_ATTACHMENT:
					switch (attr.getID()) {
					case Attr.attAttachRenddata:
						if (attachment != null)
							attachments.add(attachment);
						attachment = new Attachment();
						attachment.addAttribute(attr);
						break;
					case Attr.attAttachment:
						MAPIProps props = new MAPIProps((RawInputStream) attr
								.getValue());
						attachment.setMAPIProps(props);
						break;
					case Attr.attAttachData:
						attachment.setRawData((RawInputStream) attr.getValue());
						break;
					case Attr.attAttachTransportFilename:
						RawInputStream data = (RawInputStream) attr.getValue();
						String filename = TNEFUtils
								.removeTerminatingNulls(new String(data
										.toByteArray(), getOEMCodePage()));
						attachment.setFilename(filename);
						break;
					default:
						attachment.addAttribute(attr);
						break;
					} // switch ID for LVL_ATTACHMENT
					break;
				case Attr.LVL_MESSAGE:
					attributes.add(attr);
					break;
				default:
					// throw new IOException("Invalid attribute level: " +
					// attr.getLevel());
				} // switch level
			} catch (Exception e) {
				// TODO: handle exception
			}
		} // while
		// since there's no attachment closing attribute
		// in TNEF format, finish up last attachment
		if (attachment != null)
			attachments.add(attachment);
	}

	/**
	 * Gets the Message MAPI properties.
	 * 
	 * @return the Message MAPI properties, or null of none exist
	 * @throws IOException
	 *             if an I/O error occurs
	 */
	public MAPIProps getMAPIProps() throws IOException {
		Attr attr = getAttribute(Attr.attMAPIProps);
		return attr != null ? (MAPIProps) attr.getValue() : null;
	}

	/**
	 * Gets the charset name corresponding to the {@code attOemCodepage}
	 * attribute.
	 * 
	 * @return the charset name, or null if the {@code attOemCodepage} attribute
	 *         is invalid or does not exist
	 */
	public String getOEMCodePage() {
		Attr attr = getAttribute(Attr.attOemCodepage);
		if (attr == null)
			return null;
		try {
			RawInputStream data = (RawInputStream) attr.getValue();
			return "Cp" + data.readU16();
		} catch (IOException e) {
			return null;
		}
	}

	/**
	 * Gets the Message attributes.
	 * 
	 * @return the Message attributes
	 */
	public List<Attr> getAttributes() {
		return this.attributes;
	}

	/**
	 * Sets the Message attributes.
	 * 
	 * @param attributes
	 *            the Message attributes
	 */
	public void setAttributes(List<Attr> attributes) {
		this.attributes = attributes;
	}

	/**
	 * Gets a specific Message attribute.
	 * 
	 * @param ID
	 *            the requested attribute ID
	 * @return the requested Message attribute, or null if no such attribute
	 *         exists
	 */
	public Attr getAttribute(int ID) {
		return Attr.findAttr(this.attributes, ID);
	}

	/**
	 * Gets the Message attachments.
	 * 
	 * @return the Message attachments
	 */
	public List<Attachment> getAttachments() {
		return this.attachments;
	}

	/**
	 * Sets the Message attachments.
	 * 
	 * @param attachments
	 *            the Message attachments
	 */
	public void setAttachments(List<Attachment> attachments) {
		this.attachments = attachments;
	}

	/**
	 * Adds an attribute to this message.
	 * 
	 * @param attr
	 *            an attribute to add to this message
	 */
	public void addAttribute(Attr attr) {
		this.attributes.add(attr);
	}

	/**
	 * Adds an attachment to this message.
	 * 
	 * @param attachment
	 *            an attachment to add to this message
	 */
	public void addAttachment(Attachment attachment) {
		this.attachments.add(attachment);
	}

	/**
	 * Returns a string representation of this object.
	 * 
	 * @return a string representation of this object
	 */
	public String toString() {
		StringBuffer s = new StringBuffer();
		s.append("Message:");
		s.append("\n  Attributes:");
		for (int i = 0; i < attributes.size(); i++)
			s.append("\n    ").append(attributes.get(i));
		s.append("\n  Attachments:");
		for (int i = 0; i < attachments.size(); i++)
			s.append("\n    ").append(attachments.get(i));
		return s.toString();
	}

}
