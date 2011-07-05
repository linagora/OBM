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

import net.freeutils.tnef.RawInputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

/**
 * The <code>RawDataSource</code> class is a generic DataSource implementation
 * that handles raw data from a byte array or InputStream with any reported
 * mime type.
 *
 * @author Amichai Rothman
 * @since 2003-04-29
 */
public class RawDataSource implements DataSource {

    RawInputStream in;
    String mimeType;
    String name;

    /**
     * Creates a RawDataSource from a RawInputStream.
     *
     * @param in the RawInputStream providing the data
     * @param mimeType the mime type of the data
     */
    public RawDataSource(RawInputStream in, String mimeType) {
        this(in, mimeType, null);
    }

    /**
     * Creates a RawDataSource from a RawInputStream.
     *
     * @param in the RawInputStream providing the data
     * @param mimeType the mime type of the data
     * @param name the name associated with the data (such as a filename)
     */
    public RawDataSource(RawInputStream in, String mimeType, String name) {
        this.mimeType = mimeType;
        this.in = in;
        this.name = name;
    }

    /**
     * Returns an InputStream providing the raw data bytes.
     * Note: a new stream instance is returned each time, in accordance
     * with the DataSource interface.
     *
     * @return an InputStream providing the raw data bytes
     */
    public InputStream getInputStream() throws IOException {
        return new RawInputStream(in);
    }

    /**
     * Returns an OutputStream for setting the raw data bytes.
     * Note: a new stream instance is returned each time, in accordance
     * with the DataSource interface.
     *
     * @return an OutputStream for setting the raw data bytes
     */
    public OutputStream getOutputStream() throws IOException {
        throw new IOException("OutputStream is not supported.");
    }

    /**
     * Returns the MIME content type associated with the raw data bytes.
     *
     * @return the MIME content type
     */
    public String getContentType() {
        return mimeType;
    }

    /**
     * Returns a name associated with the raw data bytes.
     *
     * @return a name associated with the raw data bytes, or
     *         null if none is available
     */
    public String getName() {
        return name;
    }

}
