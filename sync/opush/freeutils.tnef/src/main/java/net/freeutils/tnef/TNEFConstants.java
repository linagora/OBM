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

/**
 * The <code>TNEFConstants</code> class contains general TNEF related constants.
 *
 * @author Amichai Rothman
 * @since 2003-04-25
 */
public class TNEFConstants {

    /**
     * General TNEF constant.
     */
    public static final long
        TNEF_SIGNATURE  = 0x223e9f78,
        TNEF_VERSION    = 0x00010000;

    /**
     * TNEF message flag constant.
     */
    public static final byte
        fmsNull         = 0x00,
        fmsModified     = 0x01,
        fmsLocal        = 0x02,
        fmsSubmitted    = 0x04,
        fmsRead         = 0x20,
        fmsHasAttach    = (byte)0x80;

    /**
     * TNEF message priority constant.
     */
    public static final int
        prioLow     = 3,
        prioNorm    = 2,
        prioHigh    = 1;

}
