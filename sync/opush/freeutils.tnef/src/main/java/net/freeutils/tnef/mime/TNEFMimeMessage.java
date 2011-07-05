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

import java.util.List;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;

/**
 * The <code>TNEFMimeMessage</code> class subclasses MimeMessage with the
 * Added TNEF attributes.
 *
 * @author Amichai Rothman
 * @since 2003-04-27
 */
public class TNEFMimeMessage extends MimeMessage {

    List attributes;

    /**
     * Constructs an empty TNEFMimeMessage with default content.
     *
     * @param session the session used to handle the MimeMessage
     */
    public TNEFMimeMessage(Session session) {
        super(session);
    }

    /**
     * Gets the TNEFMimeMessage TNEF attributes.
     *
     * @return the TNEFMimeMessage TNEF attributes
     */
    public List getTNEFAttributes() {
        return this.attributes;
    }

    /**
     * Sets the TNEFMimeMessage TNEF attributes.
     *
     * @param attributes the TNEFMimeMessage TNEF attributes
     */
    public void setTNEFAttributes(List attributes) {
        this.attributes = attributes;
    }

}
