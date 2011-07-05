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

import javax.mail.internet.MimeBodyPart;

import net.freeutils.tnef.Attr;

/**
 * The <code>TNEFMimeBodyPart</code> class subclasses MimeBodyPart with the
 * Added TNEF attributes.
 *
 * @author Amichai Rothman
 * @since 2003-04-27
 */
public class TNEFMimeBodyPart extends MimeBodyPart {

    List<Attr> attributes;

    /**
     * Constructs an empty TNEFMimeBodyPart with default content.
     */
    public TNEFMimeBodyPart() {
        super();
    }

    /**
     * Gets the TNEFMimeMessage TNEF attributes.
     *
     * @return the TNEFMimeMessage TNEF attributes
     */
    public List<Attr> getTNEFAttributes() {
        return this.attributes;
    }

    /**
     * Sets the TNEFMimeMessage TNEF attributes.
     *
     * @param attributes the TNEFMimeMessage TNEF attributes
     */
    public void setTNEFAttributes(List<Attr> attributes) {
        this.attributes = attributes;
    }

}
