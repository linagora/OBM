/*
 * Created on Nov 25, 2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
package fr.aliasource.obm.aliapool.pool;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLWarning;
import java.sql.SQLXML;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Map;

/**
 * @author tom
 *
 */
public class ResultSetProxy implements ResultSet {
	
	private ResultSet rs;
	private AbstractStatementProxy asp;
	
	public ResultSetProxy(ResultSet rs, AbstractStatementProxy asp) {
		this.rs = rs;
		this.asp = asp;
		asp.addResult(this);
	}

	/**
	 * @param row
	 * @return
	 * @throws java.sql.SQLException
	 */
	public boolean absolute(int row) throws SQLException {
		return rs.absolute(row);
	}

	/**
	 * @throws java.sql.SQLException
	 */
	public void afterLast() throws SQLException {
		rs.afterLast();
	}

	/**
	 * @throws java.sql.SQLException
	 */
	public void beforeFirst() throws SQLException {
		rs.beforeFirst();
	}

	/**
	 * @throws java.sql.SQLException
	 */
	public void cancelRowUpdates() throws SQLException {
		rs.cancelRowUpdates();
	}

	/**
	 * @throws java.sql.SQLException
	 */
	public void clearWarnings() throws SQLException {
		rs.clearWarnings();
	}

	/**
	 * @throws java.sql.SQLException
	 */
	public void close() throws SQLException {
		rs.close();
		asp.closeResult(this);
	}

	/**
	 * @throws java.sql.SQLException
	 */
	public void deleteRow() throws SQLException {
		rs.deleteRow();
	}

	/**
	 * @param columnName
	 * @return
	 * @throws java.sql.SQLException
	 */
	public int findColumn(String columnName) throws SQLException {
		return rs.findColumn(columnName);
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	public boolean first() throws SQLException {
		return rs.first();
	}

	/**
	 * @param i
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Array getArray(int i) throws SQLException {
		return rs.getArray(i);
	}

	/**
	 * @param colName
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Array getArray(String colName) throws SQLException {
		return rs.getArray(colName);
	}

	/**
	 * @param columnIndex
	 * @return
	 * @throws java.sql.SQLException
	 */
	public InputStream getAsciiStream(int columnIndex) throws SQLException {
		return rs.getAsciiStream(columnIndex);
	}

	/**
	 * @param columnName
	 * @return
	 * @throws java.sql.SQLException
	 */
	public InputStream getAsciiStream(String columnName) throws SQLException {
		return rs.getAsciiStream(columnName);
	}

	/**
	 * @param columnIndex
	 * @return
	 * @throws java.sql.SQLException
	 */
	public BigDecimal getBigDecimal(int columnIndex) throws SQLException {
		return rs.getBigDecimal(columnIndex);
	}

	/**
	 * @deprecated
	 * @param columnIndex
	 * @param scale
	 * @return
	 * @throws java.sql.SQLException
	 */
	public BigDecimal getBigDecimal(int columnIndex, int scale)
		throws SQLException {
		return rs.getBigDecimal(columnIndex, scale);
	}

	/**
	 * @param columnName
	 * @return
	 * @throws java.sql.SQLException
	 */
	public BigDecimal getBigDecimal(String columnName) throws SQLException {
		return rs.getBigDecimal(columnName);
	}

	/**
	 * @deprecated
	 * @param columnName
	 * @param scale
	 * @return
	 * @throws java.sql.SQLException
	 */
	public BigDecimal getBigDecimal(String columnName, int scale)
		throws SQLException {
		return rs.getBigDecimal(columnName, scale);
	}

	/**
	 * @param columnIndex
	 * @return
	 * @throws java.sql.SQLException
	 */
	public InputStream getBinaryStream(int columnIndex) throws SQLException {
		return rs.getBinaryStream(columnIndex);
	}

	/**
	 * @param columnName
	 * @return
	 * @throws java.sql.SQLException
	 */
	public InputStream getBinaryStream(String columnName) throws SQLException {
		return rs.getBinaryStream(columnName);
	}

	/**
	 * @param i
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Blob getBlob(int i) throws SQLException {
		return rs.getBlob(i);
	}

	/**
	 * @param colName
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Blob getBlob(String colName) throws SQLException {
		return rs.getBlob(colName);
	}

	/**
	 * @param columnIndex
	 * @return
	 * @throws java.sql.SQLException
	 */
	public boolean getBoolean(int columnIndex) throws SQLException {
		return rs.getBoolean(columnIndex);
	}

	/**
	 * @param columnName
	 * @return
	 * @throws java.sql.SQLException
	 */
	public boolean getBoolean(String columnName) throws SQLException {
		return rs.getBoolean(columnName);
	}

	/**
	 * @param columnIndex
	 * @return
	 * @throws java.sql.SQLException
	 */
	public byte getByte(int columnIndex) throws SQLException {
		return rs.getByte(columnIndex);
	}

	/**
	 * @param columnName
	 * @return
	 * @throws java.sql.SQLException
	 */
	public byte getByte(String columnName) throws SQLException {
		return rs.getByte(columnName);
	}

	/**
	 * @param columnIndex
	 * @return
	 * @throws java.sql.SQLException
	 */
	public byte[] getBytes(int columnIndex) throws SQLException {
		return rs.getBytes(columnIndex);
	}

	/**
	 * @param columnName
	 * @return
	 * @throws java.sql.SQLException
	 */
	public byte[] getBytes(String columnName) throws SQLException {
		return rs.getBytes(columnName);
	}

	/**
	 * @param columnIndex
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Reader getCharacterStream(int columnIndex) throws SQLException {
		return rs.getCharacterStream(columnIndex);
	}

	/**
	 * @param columnName
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Reader getCharacterStream(String columnName) throws SQLException {
		return rs.getCharacterStream(columnName);
	}

	/**
	 * @param i
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Clob getClob(int i) throws SQLException {
		return rs.getClob(i);
	}

	/**
	 * @param colName
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Clob getClob(String colName) throws SQLException {
		return rs.getClob(colName);
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	public int getConcurrency() throws SQLException {
		return rs.getConcurrency();
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	public String getCursorName() throws SQLException {
		return rs.getCursorName();
	}

	/**
	 * @param columnIndex
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Date getDate(int columnIndex) throws SQLException {
		return rs.getDate(columnIndex);
	}

	/**
	 * @param columnIndex
	 * @param cal
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Date getDate(int columnIndex, Calendar cal) throws SQLException {
		return rs.getDate(columnIndex, cal);
	}

	/**
	 * @param columnName
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Date getDate(String columnName) throws SQLException {
		return rs.getDate(columnName);
	}

	/**
	 * @param columnName
	 * @param cal
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Date getDate(String columnName, Calendar cal) throws SQLException {
		return rs.getDate(columnName, cal);
	}

	/**
	 * @param columnIndex
	 * @return
	 * @throws java.sql.SQLException
	 */
	public double getDouble(int columnIndex) throws SQLException {
		return rs.getDouble(columnIndex);
	}

	/**
	 * @param columnName
	 * @return
	 * @throws java.sql.SQLException
	 */
	public double getDouble(String columnName) throws SQLException {
		return rs.getDouble(columnName);
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	public int getFetchDirection() throws SQLException {
		return rs.getFetchDirection();
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	public int getFetchSize() throws SQLException {
		return rs.getFetchSize();
	}

	/**
	 * @param columnIndex
	 * @return
	 * @throws java.sql.SQLException
	 */
	public float getFloat(int columnIndex) throws SQLException {
		return rs.getFloat(columnIndex);
	}

	/**
	 * @param columnName
	 * @return
	 * @throws java.sql.SQLException
	 */
	public float getFloat(String columnName) throws SQLException {
		return rs.getFloat(columnName);
	}

	/**
	 * @param columnIndex
	 * @return
	 * @throws java.sql.SQLException
	 */
	public int getInt(int columnIndex) throws SQLException {
		return rs.getInt(columnIndex);
	}

	/**
	 * @param columnName
	 * @return
	 * @throws java.sql.SQLException
	 */
	public int getInt(String columnName) throws SQLException {
		return rs.getInt(columnName);
	}

	/**
	 * @param columnIndex
	 * @return
	 * @throws java.sql.SQLException
	 */
	public long getLong(int columnIndex) throws SQLException {
		return rs.getLong(columnIndex);
	}

	/**
	 * @param columnName
	 * @return
	 * @throws java.sql.SQLException
	 */
	public long getLong(String columnName) throws SQLException {
		return rs.getLong(columnName);
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	public ResultSetMetaData getMetaData() throws SQLException {
		return rs.getMetaData();
	}

	/**
	 * @param columnIndex
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Object getObject(int columnIndex) throws SQLException {
		return rs.getObject(columnIndex);
	}

	/**
	 * @param columnName
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Object getObject(String columnName) throws SQLException {
		return rs.getObject(columnName);
	}

	/**
	 * @param i
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Ref getRef(int i) throws SQLException {
		return rs.getRef(i);
	}

	/**
	 * @param colName
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Ref getRef(String colName) throws SQLException {
		return rs.getRef(colName);
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	public int getRow() throws SQLException {
		return rs.getRow();
	}

	/**
	 * @param columnIndex
	 * @return
	 * @throws java.sql.SQLException
	 */
	public short getShort(int columnIndex) throws SQLException {
		return rs.getShort(columnIndex);
	}

	/**
	 * @param columnName
	 * @return
	 * @throws java.sql.SQLException
	 */
	public short getShort(String columnName) throws SQLException {
		return rs.getShort(columnName);
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Statement getStatement() throws SQLException {
		return rs.getStatement();
	}

	/**
	 * @param columnIndex
	 * @return
	 * @throws java.sql.SQLException
	 */
	public String getString(int columnIndex) throws SQLException {
		return rs.getString(columnIndex);
	}

	/**
	 * @param columnName
	 * @return
	 * @throws java.sql.SQLException
	 */
	public String getString(String columnName) throws SQLException {
		return rs.getString(columnName);
	}

	/**
	 * @param columnIndex
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Time getTime(int columnIndex) throws SQLException {
		return rs.getTime(columnIndex);
	}

	/**
	 * @param columnIndex
	 * @param cal
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Time getTime(int columnIndex, Calendar cal) throws SQLException {
		return rs.getTime(columnIndex, cal);
	}

	/**
	 * @param columnName
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Time getTime(String columnName) throws SQLException {
		return rs.getTime(columnName);
	}

	/**
	 * @param columnName
	 * @param cal
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Time getTime(String columnName, Calendar cal) throws SQLException {
		return rs.getTime(columnName, cal);
	}

	/**
	 * @param columnIndex
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Timestamp getTimestamp(int columnIndex) throws SQLException {
		return rs.getTimestamp(columnIndex);
	}

	/**
	 * @param columnIndex
	 * @param cal
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Timestamp getTimestamp(int columnIndex, Calendar cal)
		throws SQLException {
		return rs.getTimestamp(columnIndex, cal);
	}

	/**
	 * @param columnName
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Timestamp getTimestamp(String columnName) throws SQLException {
		return rs.getTimestamp(columnName);
	}

	/**
	 * @param columnName
	 * @param cal
	 * @return
	 * @throws java.sql.SQLException
	 */
	public Timestamp getTimestamp(String columnName, Calendar cal)
		throws SQLException {
		return rs.getTimestamp(columnName, cal);
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	public int getType() throws SQLException {
		return rs.getType();
	}

	/**
	 * @deprecated
	 * @param columnIndex
	 * @return
	 * @throws java.sql.SQLException
	 */
	public InputStream getUnicodeStream(int columnIndex) throws SQLException {
		return rs.getUnicodeStream(columnIndex);
	}

	/**
	 * @deprecated
	 * @param columnName
	 * @return
	 * @throws java.sql.SQLException
	 */
	public InputStream getUnicodeStream(String columnName)
		throws SQLException {
		return rs.getUnicodeStream(columnName);
	}

	/**
	 * @param columnIndex
	 * @return
	 * @throws java.sql.SQLException
	 */
	public URL getURL(int columnIndex) throws SQLException {
		return rs.getURL(columnIndex);
	}

	/**
	 * @param columnName
	 * @return
	 * @throws java.sql.SQLException
	 */
	public URL getURL(String columnName) throws SQLException {
		return rs.getURL(columnName);
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	public SQLWarning getWarnings() throws SQLException {
		return rs.getWarnings();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return rs.hashCode();
	}

	/**
	 * @throws java.sql.SQLException
	 */
	public void insertRow() throws SQLException {
		rs.insertRow();
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	public boolean isAfterLast() throws SQLException {
		return rs.isAfterLast();
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	public boolean isBeforeFirst() throws SQLException {
		return rs.isBeforeFirst();
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	public boolean isFirst() throws SQLException {
		return rs.isFirst();
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	public boolean isLast() throws SQLException {
		return rs.isLast();
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	public boolean last() throws SQLException {
		return rs.last();
	}

	/**
	 * @throws java.sql.SQLException
	 */
	public void moveToCurrentRow() throws SQLException {
		rs.moveToCurrentRow();
	}

	/**
	 * @throws java.sql.SQLException
	 */
	public void moveToInsertRow() throws SQLException {
		rs.moveToInsertRow();
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	public boolean next() throws SQLException {
		return rs.next();
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	public boolean previous() throws SQLException {
		return rs.previous();
	}

	/**
	 * @throws java.sql.SQLException
	 */
	public void refreshRow() throws SQLException {
		rs.refreshRow();
	}

	/**
	 * @param rows
	 * @return
	 * @throws java.sql.SQLException
	 */
	public boolean relative(int rows) throws SQLException {
		return rs.relative(rows);
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	public boolean rowDeleted() throws SQLException {
		return rs.rowDeleted();
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	public boolean rowInserted() throws SQLException {
		return rs.rowInserted();
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	public boolean rowUpdated() throws SQLException {
		return rs.rowUpdated();
	}

	/**
	 * @param direction
	 * @throws java.sql.SQLException
	 */
	public void setFetchDirection(int direction) throws SQLException {
		rs.setFetchDirection(direction);
	}

	/**
	 * @param rows
	 * @throws java.sql.SQLException
	 */
	public void setFetchSize(int rows) throws SQLException {
		rs.setFetchSize(rows);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return rs.toString();
	}

	/**
	 * @param columnIndex
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateArray(int columnIndex, Array x) throws SQLException {
		rs.updateArray(columnIndex, x);
	}

	/**
	 * @param columnName
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateArray(String columnName, Array x) throws SQLException {
		rs.updateArray(columnName, x);
	}

	/**
	 * @param columnIndex
	 * @param x
	 * @param length
	 * @throws java.sql.SQLException
	 */
	public void updateAsciiStream(int columnIndex, InputStream x, int length)
		throws SQLException {
		rs.updateAsciiStream(columnIndex, x, length);
	}

	/**
	 * @param columnName
	 * @param x
	 * @param length
	 * @throws java.sql.SQLException
	 */
	public void updateAsciiStream(String columnName, InputStream x, int length)
		throws SQLException {
		rs.updateAsciiStream(columnName, x, length);
	}

	/**
	 * @param columnIndex
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateBigDecimal(int columnIndex, BigDecimal x)
		throws SQLException {
		rs.updateBigDecimal(columnIndex, x);
	}

	/**
	 * @param columnName
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateBigDecimal(String columnName, BigDecimal x)
		throws SQLException {
		rs.updateBigDecimal(columnName, x);
	}

	/**
	 * @param columnIndex
	 * @param x
	 * @param length
	 * @throws java.sql.SQLException
	 */
	public void updateBinaryStream(int columnIndex, InputStream x, int length)
		throws SQLException {
		rs.updateBinaryStream(columnIndex, x, length);
	}

	/**
	 * @param columnName
	 * @param x
	 * @param length
	 * @throws java.sql.SQLException
	 */
	public void updateBinaryStream(
		String columnName,
		InputStream x,
		int length)
		throws SQLException {
		rs.updateBinaryStream(columnName, x, length);
	}

	/**
	 * @param columnIndex
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateBlob(int columnIndex, Blob x) throws SQLException {
		rs.updateBlob(columnIndex, x);
	}

	/**
	 * @param columnName
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateBlob(String columnName, Blob x) throws SQLException {
		rs.updateBlob(columnName, x);
	}

	/**
	 * @param columnIndex
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateBoolean(int columnIndex, boolean x) throws SQLException {
		rs.updateBoolean(columnIndex, x);
	}

	/**
	 * @param columnName
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateBoolean(String columnName, boolean x)
		throws SQLException {
		rs.updateBoolean(columnName, x);
	}

	/**
	 * @param columnIndex
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateByte(int columnIndex, byte x) throws SQLException {
		rs.updateByte(columnIndex, x);
	}

	/**
	 * @param columnName
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateByte(String columnName, byte x) throws SQLException {
		rs.updateByte(columnName, x);
	}

	/**
	 * @param columnIndex
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateBytes(int columnIndex, byte[] x) throws SQLException {
		rs.updateBytes(columnIndex, x);
	}

	/**
	 * @param columnName
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateBytes(String columnName, byte[] x) throws SQLException {
		rs.updateBytes(columnName, x);
	}

	/**
	 * @param columnIndex
	 * @param x
	 * @param length
	 * @throws java.sql.SQLException
	 */
	public void updateCharacterStream(int columnIndex, Reader x, int length)
		throws SQLException {
		rs.updateCharacterStream(columnIndex, x, length);
	}

	/**
	 * @param columnName
	 * @param reader
	 * @param length
	 * @throws java.sql.SQLException
	 */
	public void updateCharacterStream(
		String columnName,
		Reader reader,
		int length)
		throws SQLException {
		rs.updateCharacterStream(columnName, reader, length);
	}

	/**
	 * @param columnIndex
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateClob(int columnIndex, Clob x) throws SQLException {
		rs.updateClob(columnIndex, x);
	}

	/**
	 * @param columnName
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateClob(String columnName, Clob x) throws SQLException {
		rs.updateClob(columnName, x);
	}

	/**
	 * @param columnIndex
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateDate(int columnIndex, Date x) throws SQLException {
		rs.updateDate(columnIndex, x);
	}

	/**
	 * @param columnName
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateDate(String columnName, Date x) throws SQLException {
		rs.updateDate(columnName, x);
	}

	/**
	 * @param columnIndex
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateDouble(int columnIndex, double x) throws SQLException {
		rs.updateDouble(columnIndex, x);
	}

	/**
	 * @param columnName
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateDouble(String columnName, double x) throws SQLException {
		rs.updateDouble(columnName, x);
	}

	/**
	 * @param columnIndex
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateFloat(int columnIndex, float x) throws SQLException {
		rs.updateFloat(columnIndex, x);
	}

	/**
	 * @param columnName
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateFloat(String columnName, float x) throws SQLException {
		rs.updateFloat(columnName, x);
	}

	/**
	 * @param columnIndex
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateInt(int columnIndex, int x) throws SQLException {
		rs.updateInt(columnIndex, x);
	}

	/**
	 * @param columnName
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateInt(String columnName, int x) throws SQLException {
		rs.updateInt(columnName, x);
	}

	/**
	 * @param columnIndex
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateLong(int columnIndex, long x) throws SQLException {
		rs.updateLong(columnIndex, x);
	}

	/**
	 * @param columnName
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateLong(String columnName, long x) throws SQLException {
		rs.updateLong(columnName, x);
	}

	/**
	 * @param columnIndex
	 * @throws java.sql.SQLException
	 */
	public void updateNull(int columnIndex) throws SQLException {
		rs.updateNull(columnIndex);
	}

	/**
	 * @param columnName
	 * @throws java.sql.SQLException
	 */
	public void updateNull(String columnName) throws SQLException {
		rs.updateNull(columnName);
	}

	/**
	 * @param columnIndex
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateObject(int columnIndex, Object x) throws SQLException {
		rs.updateObject(columnIndex, x);
	}

	/**
	 * @param columnIndex
	 * @param x
	 * @param scale
	 * @throws java.sql.SQLException
	 */
	public void updateObject(int columnIndex, Object x, int scale)
		throws SQLException {
		rs.updateObject(columnIndex, x, scale);
	}

	/**
	 * @param columnName
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateObject(String columnName, Object x) throws SQLException {
		rs.updateObject(columnName, x);
	}

	/**
	 * @param columnName
	 * @param x
	 * @param scale
	 * @throws java.sql.SQLException
	 */
	public void updateObject(String columnName, Object x, int scale)
		throws SQLException {
		rs.updateObject(columnName, x, scale);
	}

	/**
	 * @param columnIndex
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateRef(int columnIndex, Ref x) throws SQLException {
		rs.updateRef(columnIndex, x);
	}

	/**
	 * @param columnName
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateRef(String columnName, Ref x) throws SQLException {
		rs.updateRef(columnName, x);
	}

	/**
	 * @throws java.sql.SQLException
	 */
	public void updateRow() throws SQLException {
		rs.updateRow();
	}

	/**
	 * @param columnIndex
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateShort(int columnIndex, short x) throws SQLException {
		rs.updateShort(columnIndex, x);
	}

	/**
	 * @param columnName
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateShort(String columnName, short x) throws SQLException {
		rs.updateShort(columnName, x);
	}

	/**
	 * @param columnIndex
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateString(int columnIndex, String x) throws SQLException {
		rs.updateString(columnIndex, x);
	}

	/**
	 * @param columnName
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateString(String columnName, String x) throws SQLException {
		rs.updateString(columnName, x);
	}

	/**
	 * @param columnIndex
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateTime(int columnIndex, Time x) throws SQLException {
		rs.updateTime(columnIndex, x);
	}

	/**
	 * @param columnName
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateTime(String columnName, Time x) throws SQLException {
		rs.updateTime(columnName, x);
	}

	/**
	 * @param columnIndex
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateTimestamp(int columnIndex, Timestamp x)
		throws SQLException {
		rs.updateTimestamp(columnIndex, x);
	}

	/**
	 * @param columnName
	 * @param x
	 * @throws java.sql.SQLException
	 */
	public void updateTimestamp(String columnName, Timestamp x)
		throws SQLException {
		rs.updateTimestamp(columnName, x);
	}

	/**
	 * @return
	 * @throws java.sql.SQLException
	 */
	public boolean wasNull() throws SQLException {
		return rs.wasNull();
	}

	public int getHoldability() throws SQLException {
		return rs.getHoldability();
	}

	public Reader getNCharacterStream(int columnIndex) throws SQLException {
		return rs.getNCharacterStream(columnIndex);
	}

	public Reader getNCharacterStream(String columnLabel) throws SQLException {
		return rs.getNCharacterStream(columnLabel);
	}

	public NClob getNClob(int columnIndex) throws SQLException {
		return rs.getNClob(columnIndex);
	}

	public NClob getNClob(String columnLabel) throws SQLException {
		return rs.getNClob(columnLabel);
	}

	public String getNString(int columnIndex) throws SQLException {
		return rs.getNString(columnIndex);
	}

	public String getNString(String columnLabel) throws SQLException {
		return rs.getNString(columnLabel);
	}

	public RowId getRowId(int columnIndex) throws SQLException {
		return rs.getRowId(columnIndex);
	}

	public RowId getRowId(String columnLabel) throws SQLException {
		return rs.getRowId(columnLabel);
	}

	public SQLXML getSQLXML(int columnIndex) throws SQLException {
		return rs.getSQLXML(columnIndex);
	}

	public SQLXML getSQLXML(String columnLabel) throws SQLException {
		return rs.getSQLXML(columnLabel);
	}

	public boolean isClosed() throws SQLException {
		return rs.isClosed();
	}

	public boolean isWrapperFor(Class<?> iface) throws SQLException {
		return rs.isWrapperFor(iface);
	}

	public <T> T unwrap(Class<T> iface) throws SQLException {
		return rs.unwrap(iface);
	}

	public void updateAsciiStream(int columnIndex, InputStream x, long length)
			throws SQLException {
		rs.updateAsciiStream(columnIndex, x, length);
	}

	public void updateAsciiStream(int columnIndex, InputStream x)
			throws SQLException {
		rs.updateAsciiStream(columnIndex, x);
	}

	public void updateAsciiStream(String columnLabel, InputStream x, long length)
			throws SQLException {
		rs.updateAsciiStream(columnLabel, x, length);
	}

	public void updateAsciiStream(String columnLabel, InputStream x)
			throws SQLException {
		rs.updateAsciiStream(columnLabel, x);
	}

	public void updateBinaryStream(int columnIndex, InputStream x, long length)
			throws SQLException {
		rs.updateBinaryStream(columnIndex, x, length);
	}

	public void updateBinaryStream(int columnIndex, InputStream x)
			throws SQLException {
		rs.updateBinaryStream(columnIndex, x);
	}

	public void updateBinaryStream(String columnLabel, InputStream x,
			long length) throws SQLException {
		rs.updateBinaryStream(columnLabel, x, length);
	}

	public void updateBinaryStream(String columnLabel, InputStream x)
			throws SQLException {
		rs.updateBinaryStream(columnLabel, x);
	}

	public void updateBlob(int columnIndex, InputStream inputStream, long length)
			throws SQLException {
		rs.updateBlob(columnIndex, inputStream, length);
	}

	public void updateBlob(int columnIndex, InputStream inputStream)
			throws SQLException {
		rs.updateBlob(columnIndex, inputStream);
	}

	public void updateBlob(String columnLabel, InputStream inputStream,
			long length) throws SQLException {
		rs.updateBlob(columnLabel, inputStream, length);
	}

	public void updateBlob(String columnLabel, InputStream inputStream)
			throws SQLException {
		rs.updateBlob(columnLabel, inputStream);
	}

	public void updateCharacterStream(int columnIndex, Reader x, long length)
			throws SQLException {
		rs.updateCharacterStream(columnIndex, x, length);
	}

	public void updateCharacterStream(int columnIndex, Reader x)
			throws SQLException {
		rs.updateCharacterStream(columnIndex, x);
	}

	public void updateCharacterStream(String columnLabel, Reader reader,
			long length) throws SQLException {
		rs.updateCharacterStream(columnLabel, reader, length);
	}

	public void updateCharacterStream(String columnLabel, Reader reader)
			throws SQLException {
		rs.updateCharacterStream(columnLabel, reader);
	}

	public void updateClob(int columnIndex, Reader reader, long length)
			throws SQLException {
		rs.updateClob(columnIndex, reader, length);
	}

	public void updateClob(int columnIndex, Reader reader) throws SQLException {
		rs.updateClob(columnIndex, reader);
	}

	public void updateClob(String columnLabel, Reader reader, long length)
			throws SQLException {
		rs.updateClob(columnLabel, reader, length);
	}

	public void updateClob(String columnLabel, Reader reader)
			throws SQLException {
		rs.updateClob(columnLabel, reader);
	}

	public void updateNCharacterStream(int columnIndex, Reader x, long length)
			throws SQLException {
		rs.updateNCharacterStream(columnIndex, x, length);
	}

	public void updateNCharacterStream(int columnIndex, Reader x)
			throws SQLException {
		rs.updateNCharacterStream(columnIndex, x);
	}

	public void updateNCharacterStream(String columnLabel, Reader reader,
			long length) throws SQLException {
		rs.updateNCharacterStream(columnLabel, reader, length);
	}

	public void updateNCharacterStream(String columnLabel, Reader reader)
			throws SQLException {
		rs.updateNCharacterStream(columnLabel, reader);
	}

	public void updateNClob(int columnIndex, NClob clob) throws SQLException {
		rs.updateNClob(columnIndex, clob);
	}

	public void updateNClob(int columnIndex, Reader reader, long length)
			throws SQLException {
		rs.updateNClob(columnIndex, reader, length);
	}

	public void updateNClob(int columnIndex, Reader reader) throws SQLException {
		rs.updateNClob(columnIndex, reader);
	}

	public void updateNClob(String columnLabel, NClob clob) throws SQLException {
		rs.updateNClob(columnLabel, clob);
	}

	public void updateNClob(String columnLabel, Reader reader, long length)
			throws SQLException {
		rs.updateNClob(columnLabel, reader, length);
	}

	public void updateNClob(String columnLabel, Reader reader)
			throws SQLException {
		rs.updateNClob(columnLabel, reader);
	}

	public void updateNString(int columnIndex, String string)
			throws SQLException {
		rs.updateNString(columnIndex, string);
	}

	public void updateNString(String columnLabel, String string)
			throws SQLException {
		rs.updateNString(columnLabel, string);
	}

	public void updateRowId(int columnIndex, RowId x) throws SQLException {
		rs.updateRowId(columnIndex, x);
	}

	public void updateRowId(String columnLabel, RowId x) throws SQLException {
		rs.updateRowId(columnLabel, x);
	}

	public void updateSQLXML(int columnIndex, SQLXML xmlObject)
			throws SQLException {
		rs.updateSQLXML(columnIndex, xmlObject);
	}

	public void updateSQLXML(String columnLabel, SQLXML xmlObject)
			throws SQLException {
		rs.updateSQLXML(columnLabel, xmlObject);
	}

	public Object getObject(int columnIndex, Map<String, Class<?>> map)
			throws SQLException {
		return rs.getObject(columnIndex, map);
	}

	public Object getObject(String columnLabel, Map<String, Class<?>> map)
			throws SQLException {
		return rs.getObject(columnLabel, map);
	}

}
