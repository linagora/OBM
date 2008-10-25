package org.obm.tz.client;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.TimeZone;

import junit.framework.TestCase;

public class TestParse extends TestCase {

	public void testParse() throws IOException {
		File f = new File("/usr/share/zoneinfo/Europe/Paris");
		RandomAccessFile raf = new RandomAccessFile(f.getAbsoluteFile(), "r");
		byte[] data = new byte[(int) f.length()];
		raf.readFully(data);
		TZParser.parseTimeZoneData(data);


		for (int i = 1890; i < 1913; i++) {
			System.out.print("year: " + i);
			Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
			for (int j = 0; j < 12; j++) {
				cal.set(Calendar.YEAR, i);
				cal.set(Calendar.MONTH, j);
				System.out.print(" m:"+j+" " + TZParser.getOffset(""+cal.getTimeInMillis()));
			}
			System.out.println();
		}
	}

	public void testOld() throws IOException {
		File f = new File("/usr/share/zoneinfo/Europe/Paris");
		new OldZoneInfo(f);
	}

}
