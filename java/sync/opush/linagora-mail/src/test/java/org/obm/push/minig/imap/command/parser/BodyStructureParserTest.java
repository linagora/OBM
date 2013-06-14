/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2012  Linagora
 *
 * This program is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU Affero General Public License as 
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version, provided you comply 
 * with the Additional Terms applicable for OBM connector by Linagora 
 * pursuant to Section 7 of the GNU Affero General Public License, 
 * subsections (b), (c), and (e), pursuant to which you must notably (i) retain 
 * the “Message sent thanks to OBM, Free Communication by Linagora” 
 * signature notice appended to any and all outbound messages 
 * (notably e-mail and meeting requests), (ii) retain all hypertext links between 
 * OBM and obm.org, as well as between Linagora and linagora.com, and (iii) refrain 
 * from infringing Linagora intellectual property rights over its trademarks 
 * and commercial brands. Other Additional Terms apply, 
 * see <http://www.linagora.com/licenses/> for more details. 
 *
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License 
 * for more details. 
 *
 * You should have received a copy of the GNU Affero General Public License 
 * and its applicable Additional Terms for OBM along with this program. If not, 
 * see <http://www.gnu.org/licenses/> for the GNU Affero General Public License version 3 
 * and <http://www.linagora.com/licenses/> for the Additional Terms applicable to 
 * OBM connectors. 
 * 
 * ***** END LICENSE BLOCK ***** */
package org.obm.push.minig.imap.command.parser;

import static org.obm.push.mail.imap.MimeMessageFactory.createSimpleMimeMessage;
import static org.obm.push.mail.imap.MimeMessageFactory.createSimpleMimePart;
import static org.obm.push.mail.imap.MimeMessageFactory.createSimpleMimeTree;
import static org.obm.push.mail.imap.MimeMessageTestUtils.checkMimeTree;

import java.util.HashMap;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.obm.filter.Slow;
import org.obm.filter.SlowFilterRunner;
import org.obm.push.mail.mime.IMimePart;
import org.obm.push.mail.mime.MimeMessage;
import org.obm.push.minig.imap.command.parser.BodyStructureParser;

import com.google.common.collect.ImmutableMap;

@RunWith(SlowFilterRunner.class)
public class BodyStructureParserTest {

	private MimeMessage parseStringAsBodyStructure(String bs) {
		return new BodyStructureParser().parseBodyStructure(bs).build();
	}
	
	@Test
	public void testRFC3501Ex1() {
		String bs = "(\"TEXT\" \"PLAIN\" (\"CHARSET\" \"US-ASCII\") NIL NIL \"7BIT\" 2279 48)";
		IMimePart result = parseStringAsBodyStructure(bs);
		checkMimeTree(createSimpleMimeMessage("TEXT", "PLAIN", null, "7BIT", 2279, ImmutableMap.of("CHARSET", "US-ASCII")),
				result);
	}

	@Test
	public void testRFC3501Ex2() {
		String bs = "((\"TEXT\" \"PLAIN\" (\"CHARSET\" \"US-ASCII\") NIL NIL \"7BIT\" 1152 23)" +
						"(\"TEXT\" \"PLAIN\" (\"CHARSET\" \"US-ASCII\" \"NAME\" \"cc.diff\") " +
							"\"<960723163407.20117h@cac.washington.edu>\" \"Compiler diff\" " +
							"\"BASE64\" 4554 73) \"MIXED\")";
		IMimePart result = parseStringAsBodyStructure(bs);
		checkMimeTree(
				createSimpleMimeTree("multipart", "MIXED", null, null, new HashMap<String, String>(), 
						createSimpleMimePart("TEXT", "PLAIN", null, "7BIT", 1152, ImmutableMap.of("CHARSET", "US-ASCII")),
						createSimpleMimePart("TEXT", "PLAIN", "960723163407.20117h@cac.washington.edu", "BASE64", 4554, ImmutableMap.of("CHARSET", "US-ASCII", "NAME", "cc.diff"))),
				result);
	}
	
	@Test
	public void testBugzilla1502SimpleImagePart() {
		String bs = "(\"IMAGE\" \"PJPEG\" NIL NIL NIL \"BASE64\" 97418)";
		IMimePart result = parseStringAsBodyStructure(bs);
		checkMimeTree(createSimpleMimeMessage("IMAGE", "PJPEG", null, "BASE64", 97418, new HashMap<String, String>()),
				result);
	}
	
	@Test
	public void testBugzilla1502ImagePart() {
		String bs = "(\"IMAGE\" \"PJPEG\" NIL NIL NIL \"BASE64\" 97418 NIL " +
			"(\"ATTACHMENT\" (\"FILENAME\" \"=?UTF-8?Q?Coucher=20de=20soleil.jpg?=\")) NIL NIL)";
		IMimePart result = parseStringAsBodyStructure(bs);
		checkMimeTree(createSimpleMimeMessage("IMAGE", "PJPEG", null, "BASE64", 97418, ImmutableMap.of("FILENAME", "Coucher de soleil.jpg")),
				result);
	}
	
	@Test
	public void testBugzilla1502() {
		String bs = 
				"((\"TEXT\" \"PLAIN\" (\"CHARSET\" \"UTF-8\") NIL NIL \"QUOTED-PRINTABLE\" 6 1 NIL NIL NIL NIL)" +
				"(\"IMAGE\" \"PJPEG\" NIL NIL NIL \"BASE64\" 97418 NIL " +
					"(\"ATTACHMENT\" (\"FILENAME\" \"=?UTF-8?Q?Coucher=20de=20soleil.jpg?=\")) NIL NIL) " +
					"\"MIXED\" (\"BOUNDARY\" \"-=Part.2.4e8359a545e8f099.12be7d033d7.ddfb59d71741cd3a=-\") NIL NIL NIL)";
		IMimePart result = parseStringAsBodyStructure(bs);
		checkMimeTree(
				createSimpleMimeTree("multipart", "MIXED", null, null, ImmutableMap.of("BOUNDARY", "-=Part.2.4e8359a545e8f099.12be7d033d7.ddfb59d71741cd3a=-"), 
						createSimpleMimePart("TEXT", "PLAIN", null, "QUOTED-PRINTABLE", 6, ImmutableMap.of("CHARSET", "UTF-8")),
						createSimpleMimePart("IMAGE", "PJPEG", null, "BASE64", 97418, ImmutableMap.of("FILENAME", "Coucher de soleil.jpg"))),
				result);
	}
	
	@Test @Slow
	public void testBugzilla1502Adrien() {
		String bs = "(" +
				"(\"TEXT\" \"HTML\" (\"CHARSET\" \"UTF-8\") NIL NIL \"QUOTED-PRINTABLE\" 489 6 NIL NIL NIL NIL)" +
				"(\"MESSAGE\" \"RFC822\" NIL NIL NIL \"7BIT\" 4224 " +
					"(\"Mon, 8 Nov 2010 14:15:19 +0100\" \"[] OBM - aaa\" " +
					"((NIL NIL \"admin\" \"08000linux.com\")) " +
					"((NIL NIL \"admin\" \"08000linux.com\")) " +
					"((NIL NIL \"fpag\" \"linagora.com\")) " +
					"((NIL NIL \"undisclosed-recipients\" NIL)(NIL NIL NIL NIL)) NIL NIL \"<4534_32591@TOSCA.www.08000linux.com>\" \"<4534_32645@TOSCA.www.08000linux.com>\") " +
					"(" +
						"(\"TEXT\" \"PLAIN\" (\"CHARSET\" \"UTF-8\") NIL NIL \"QUOTED-PRINTABLE\" 769 24 NIL (\"INLINE\" NIL) NIL NIL)" +
						"(\"TEXT\" \"HTML\" (\"CHARSET\" \"UTF-8\") NIL NIL \"QUOTED-PRINTABLE\" 919 23 NIL (\"INLINE\" NIL) NIL NIL)" +
						" \"ALTERNATIVE\" (\"BOUNDARY\" \"mimepart_4cd7f7e7d342a_65d43fe5dce001bc430\") NIL NIL NIL) 106 NIL (\"ATTACHMENT\" (\"FILENAME\" \"forwarded_message_0.eml\")) NIL NIL) " +
					"\"MIXED\" (\"BOUNDARY\" \"-=Part.821.826e37e719e7f9f6.12c2f8e482d.f6034b7c2622b555=-\") NIL NIL NIL)";
		IMimePart result = parseStringAsBodyStructure(bs);
		checkMimeTree(
				createSimpleMimeTree("multipart", "MIXED", null, null, ImmutableMap.of("BOUNDARY", "-=Part.821.826e37e719e7f9f6.12c2f8e482d.f6034b7c2622b555=-"), 
						createSimpleMimePart("TEXT", "HTML", null, "QUOTED-PRINTABLE", 489, ImmutableMap.of("CHARSET", "UTF-8")),
						createSimpleMimePart("MESSAGE", "RFC822", null, "7BIT", 4224, ImmutableMap.of("FILENAME", "forwarded_message_0.eml", "BOUNDARY", "mimepart_4cd7f7e7d342a_65d43fe5dce001bc430"),
								createSimpleMimePart("TEXT", "PLAIN", null, "QUOTED-PRINTABLE", 769, ImmutableMap.of("CHARSET", "UTF-8")),
								createSimpleMimePart("TEXT", "HTML", null, "QUOTED-PRINTABLE", 919, ImmutableMap.of("CHARSET", "UTF-8")))),
				result);
	}
	
	@Test @Slow
	public void testBs02() {
		String bs = 
			"((\"TEXT\" \"PLAIN\" (\"CHARSET\" \"ISO-8859-1\" \"FORMAT\" \"flowed\") NIL NIL \"7BIT\" 235 5 NIL NIL NIL)" +
			 "(\"MESSAGE\" \"RFC822\" (\"NAME\" {51}Re: Suggestion de renommage de 'OBM sur mesure\".eml) NIL NIL \"8BIT\" 2329 " +
			  "(\"Wed, 27 Aug 2008 10:48:40 +0200\" {47}Re: Suggestion de renommage de 'OBM sur mesure\" " +
			   "((\"Pierre Baudracco\" NIL \"pierre.baudracco\" \"aliasource.fr\")) " +
			   "((\"Pierre Baudracco\" NIL \"pierre.baudracco\" \"aliasource.fr\")) " +
			   "((\"Pierre Baudracco\" NIL \"pierre.baudracco\" \"aliasource.fr\")) " +
			   "((NIL NIL \"brice.canizares\" \"aliasource.fr\")) " +
			   "((\"Alexandre ZAPOLSKY\" NIL \"azapolsky\" \"linagora.com\")" +
			    "(\"=?ISO-8859-1?Q?Anne-C=E9?= =?ISO-8859-1?Q?cile_Hunot?=\" NIL \"ac.hunot\" \"aliasource.fr\")" +
			    "(NIL NIL \"czapolsky\" \"linagora.com\")" +
			    "(\"=?ISO-8859-1?Q?Am=E9lie_DEGUERRY?=\" NIL \"adeguerry\" \"linagora.com\")" +
			    "(\"Olivier Boyer\" NIL \"olivier.boyer\" \"aliasource.fr\")) " +
			   "NIL \"<48B4F7E1.9000102@aliasource.fr>\" \"<48B514E8.2010208@aliasource.fr>\") " +
			   "(\"TEXT\" \"PLAIN\" (\"CHARSET\" \"ISO-8859-1\" \"FORMAT\" \"flowed\") NIL NIL \"BASE64\" 1498 21 NIL NIL NIL) 35 NIL " +
			  "(\"INLINE\" (\"FILENAME\" {51}Re: Suggestion de renommage de 'OBM sur mesure\".eml)) NIL) " +
			 "\"MIXED\" (\"BOUNDARY\" \"------------090508070608040004010708\") NIL NIL)";
		IMimePart result = parseStringAsBodyStructure(bs);
		checkMimeTree(
				createSimpleMimeTree("multipart", "MIXED", null, null, ImmutableMap.of("BOUNDARY", "------------090508070608040004010708"),
					createSimpleMimePart("TEXT", "PLAIN", null, "7BIT", 235,
						ImmutableMap.of("CHARSET", "ISO-8859-1", "FORMAT", "flowed")),
					createSimpleMimePart("MESSAGE", "RFC822",  null, "8BIT", 2329,
						ImmutableMap.of("NAME", "Re: Suggestion de renommage de 'OBM sur mesure\".eml",
										"FILENAME", "Re: Suggestion de renommage de 'OBM sur mesure\".eml"),
						createSimpleMimePart("TEXT", "PLAIN",  null, "BASE64", 1498,
								ImmutableMap.of("CHARSET", "ISO-8859-1", "FORMAT", "flowed")))),
				result);
	}
	
	@Test @Slow
	public void testBs03() {
		String bs = 
			"(" +
			 "(\"TEXT\" \"PLAIN\" (\"CHARSET\" \"iso-8859-1\" \"FORMAT\" \"flowed\") NIL NIL \"QUOTED-PRINTABLE\" 1071 27 NIL NIL NIL)" +
			 "(\"MESSAGE\" \"RFC822\" " +
			  "(\"NAME\" \"ALERT PROXY2/proxy: Le serveur Proxy2 est =?ISO-8859-1?Q?tomb=E9_-_Ba?==?ISO-8859-1?Q?scule_vers_Proxy1_=28Sat_Dec_8_12=3A16=3A11=29?==?ISO-8859-1?Q?=2Eeml?=\") " +
			  "NIL NIL \"8BIT\" 2806 " +
			  "(\"Sat, 8 Dec 2007 12:16:11 +0100 (CET)\" {90}ALERT PROXY2/proxy: Le serveur Proxy2 est tombe - Bascule vers	Proxy1 (Sat Dec 8 12:16:11) " +
			  "((NIL NIL \"supervision\" \"proxy1.assemblee-nationale.fr\")) " +
			  "((NIL NIL \"supervision\" \"proxy1.assemblee-nationale.fr\")) " +
			  "((NIL NIL \"supervision\" \"proxy1.assemblee-nationale.fr\")) " +
			  "((NIL NIL \"administrateurs.proxy\" \"assemblee-nationale.fr\")) NIL NIL NIL \"<20071208111611.B5F5CA3AE2@proxy1.assemblee-nationale.fr>\") " +
			  "((\"TEXT\" \"PLAIN\" (\"CHARSET\" \"us-ascii\") NIL NIL \"QUOTED-PRINTABLE\" 310 9 NIL NIL NIL)" +
			  "(\"TEXT\" \"PLAIN\" (\"CHARSET\" \"utf-8\") NIL NIL \"QUOTED-PRINTABLE\" 804 15 NIL " +
			  "(\"INLINE\" NIL) NIL) " +
			  "\"MIXED\" (\"BOUNDARY\" \"2JFYz.4GCHzc7Eh.t1JsN.8ipKmXd\") NIL NIL) 63 NIL " +
			  "(\"INLINE\" (\"FILENAME*\" {294}ISO-8859-1''%41%4C%45%52%54%20%50%52%4F%58%59%32%2F%70%72%6F%78%79%3A%20%4C%65%20%73%65%72%76%65%75%72%20%50%72%6F%78%79%32%20%65%73%74%20%74%6F%6D%62%E9%20%2D%20%42%61%73%63%75%6C%65%20%76%65%72%73%20%50%72%6F%78%79%31%20%28%53%61%74%20%44%65%63%20%38%20%31%32%3A%31%36%3A%31%31%29%2E%65%6D%6C)) NIL) " +
			 "\"MIXED\" (\"BOUNDARY\" \"------------090506050700020806090002\") NIL NIL";
		IMimePart result = parseStringAsBodyStructure(bs);
		checkMimeTree(
				createSimpleMimeTree("multipart", "MIXED", null, null, ImmutableMap.of("BOUNDARY", "------------090506050700020806090002"),
					createSimpleMimePart("TEXT", "PLAIN", null, "QUOTED-PRINTABLE", 1071,
						ImmutableMap.of("CHARSET", "iso-8859-1", "FORMAT", "flowed")),
					createSimpleMimePart("MESSAGE", "RFC822", null, "8BIT", 2806,
						ImmutableMap.of("NAME", "ALERT PROXY2/proxy: Le serveur Proxy2 est tombé - Bascule vers Proxy1 (Sat Dec 8 12:16:11).eml",
										"FILENAME", "ALERT PROXY2/proxy: Le serveur Proxy2 est tombé - Bascule vers Proxy1 (Sat Dec 8 12:16:11).eml",
										"BOUNDARY", "2JFYz.4GCHzc7Eh.t1JsN.8ipKmXd"),
								createSimpleMimePart("TEXT", "PLAIN", null, "QUOTED-PRINTABLE", 310, ImmutableMap.of("CHARSET", "us-ascii")),
								createSimpleMimePart("TEXT", "PLAIN", null, "QUOTED-PRINTABLE", 804, ImmutableMap.of("CHARSET", "utf-8"))
								)),
				result);
	}
	
	@Test
	public void testBs04() {
		String bs = 
			"((\"TEXT\" \"PLAIN\" (\"CHARSET\" \"iso-8859-1\" \"FORMAT\" \"flowed\") NIL NIL \"QUOTED-PRINTABLE\" 4295 114 NIL NIL NIL)" +
			"(\"TEXT\" \"HTML\" (\"CHARSET\" \"iso-8859-1\") NIL NIL \"QUOTED-PRINTABLE\" 5429 123 NIL NIL NIL) " +
			"\"ALTERNATIVE\" (\"BOUNDARY\" \"=====================_4229656==.ALT\") NIL NIL)";
		IMimePart result = parseStringAsBodyStructure(bs);
		checkMimeTree(
				createSimpleMimeTree("multipart", "ALTERNATIVE", null, null, ImmutableMap.of("BOUNDARY", "=====================_4229656==.ALT"),
					createSimpleMimePart("TEXT", "PLAIN", null, "QUOTED-PRINTABLE", 4295,
						ImmutableMap.of("CHARSET", "iso-8859-1", "FORMAT", "flowed")),
					createSimpleMimePart("TEXT", "HTML", null, "QUOTED-PRINTABLE", 5429,
						ImmutableMap.of("CHARSET", "iso-8859-1"))),
				result);
	}
	
	@Test
	public void testBs05() {
		String bs = 
			"((\"TEXT\" \"PLAIN\" (\"CHARSET\" \"iso-8859-15\") NIL NIL \"8BIT\" 1202 32 NIL NIL NIL)" +
			"(\"TEXT\" \"HTML\" (\"CHARSET\" \"iso-8859-15\") NIL NIL \"8BIT\" 8761 131 NIL NIL NIL) " +
			"\"ALTERNATIVE\" (\"BOUNDARY\" \"618027128011279051\") NIL NIL)";
		IMimePart result = parseStringAsBodyStructure(bs);
		checkMimeTree(
				createSimpleMimeTree("multipart", "ALTERNATIVE", null, null, ImmutableMap.of("BOUNDARY", "618027128011279051"),
					createSimpleMimePart("TEXT", "PLAIN", null, "8BIT", 1202,
						ImmutableMap.of("CHARSET", "iso-8859-15")),
					createSimpleMimePart("TEXT", "HTML", null, "8BIT", 8761,
						ImmutableMap.of("CHARSET", "iso-8859-15"))),
				result);
	}
	
	@Test
	public void testBs06() {
		String bs = 
			"(" +
			 "(" +
			  "(\"TEXT\" \"PLAIN\" (\"CHARSET\" \"iso-8859-1\") NIL NIL \"QUOTED-PRINTABLE\" 197 9 NIL (\"INLINE\" NIL) NIL NIL)" +
			  "(\"TEXT\" \"PLAIN\" (\"CHARSET\" \"utf-8\") NIL NIL \"QUOTED-PRINTABLE\" 165923 3430 NIL " +
			   "(\"ATTACHMENT\" (\"FILENAME\" \"term.log\")) NIL NIL) " +
			  "\"MIXED\" (\"BOUNDARY\" \"MGYHOYXEY6WxJCY8\") (\"INLINE\" NIL) NIL NIL)" +
			 "(\"APPLICATION\" \"PGP-SIGNATURE\" NIL NIL NIL \"7BIT\" 203 NIL (\"INLINE\" NIL) NIL NIL) " +
			 "\"SIGNED\" (\"MICALG\" \"pgp-sha1\" \"PROTOCOL\" \"application/pgp-signature\" \"BOUNDARY\" \"hHWLQfXTYDoKhP50\") (\"INLINE\" NIL) NIL NIL)";
		IMimePart result = parseStringAsBodyStructure(bs);
		checkMimeTree(
				createSimpleMimeTree("multipart", "SIGNED", null, null,
						ImmutableMap.of("BOUNDARY", "hHWLQfXTYDoKhP50",
										"MICALG", "pgp-sha1",
										"PROTOCOL", "application/pgp-signature"),
					createSimpleMimePart("multipart", "MIXED", null, null, null, ImmutableMap.of("BOUNDARY", "MGYHOYXEY6WxJCY8"), 
							createSimpleMimePart("TEXT", "PLAIN", null, "QUOTED-PRINTABLE", 197,
								ImmutableMap.of("CHARSET", "iso-8859-1")),
							createSimpleMimePart("TEXT", "PLAIN",  null, "QUOTED-PRINTABLE", 165923,
								ImmutableMap.of("CHARSET", "utf-8", "FILENAME", "term.log"))),
					createSimpleMimePart("APPLICATION", "PGP-SIGNATURE",  null, "7BIT", 203,
						new HashMap<String, String>())),
				result);
	}
	
	@Test @Slow
	public void testrfc2231ParamEncoding() {
		String bs = 
			"((\"TEXT\" \"HTML\" (\"CHARSET\" \"UTF-8\") NIL NIL \"7BIT\" 148 0 NIL NIL NIL NIL)" +
			 "(\"MESSAGE\" \"RFC822\" NIL NIL NIL \"7BIT\" 2536015 " +
			  "(\"Fri, 17 Sep 2010 10:34:14 +0200\" \"[Fwd: Budget Global]\" " +
			   "((\"Baitiche Malika\" NIL \"m.baitiche\" \"ch-gers.fr\")) " +
			   "((\"Baitiche Malika\" NIL \"m.baitiche\" \"ch-gers.fr\")) " +
			   "((\"Baitiche Malika\" NIL \"m.baitiche\" \"ch-gers.fr\")) " +
			   "((\"Zenone Arnaud\" NIL \"a.zenone\" \"ch-gers.fr\")) " +
			   "NIL NIL NIL \"<1284712454876-m.baitiche@ch-gers.fr>\") " +
			  "((\"TEXT\" \"HTML\" (\"CHARSET\" \"UTF-8\") NIL NIL \"QUOTED-PRINTABLE\" 424 5 NIL NIL NIL NIL)" +
			   "(\"MESSAGE\" \"RFC822\" NIL NIL NIL \"7BIT\" 2534456 " +
				"(\"Thu, 16 Sep 2010 15:45:44 +0200\" \"Budget Global\" " +
				 "((\"SALESSES Claude\" NIL \"claude.salesses\" \"cpam-auch.cnamts.fr\")) " +
				 "((\"SALESSES Claude\" NIL \"claude.salesses\" \"cpam-auch.cnamts.fr\")) " +
				 "((\"SALESSES Claude\" NIL \"claude.salesses\" \"cpam-auch.cnamts.fr\")) " +
				 "((\"CHS responsable\" NIL \"m.baitiche\" \"ch-gers.fr\")) " +
				 "NIL NIL NIL \"<4C921F88.6090502@cpam-auch.cnamts.fr>\") " +
				"(((\"TEXT\" \"PLAIN\" (\"FORMAT\" \"flowed\" \"CHARSET\" \"utf-8\") NIL NIL \"QUOTED-PRINTABLE\" 1333 33 NIL NIL NIL NIL)" +
				  "((\"TEXT\" \"HTML\" (\"CHARSET\" \"utf-8\") NIL NIL \"7BIT\" 1840 32 NIL NIL NIL NIL)" +
				   "(\"IMAGE\" \"JPEG\" NIL \"<part1.05060905.06000802@cpam-auch.cnamts.fr>\" NIL \"BASE64\" 3544 NIL NIL NIL NIL)" +
				   "(\"IMAGE\" \"JPEG\" NIL \"<part2.06080502.08080709@cpam-auch.cnamts.fr>\" NIL \"BASE64\" 9638 NIL NIL NIL NIL) " +
				  "\"RELATED\" (\"BOUNDARY\" \"Boundary_(ID_j1VqHCLvxMbObr0Y49iZgQ)\") NIL NIL NIL) " +
				 "\"ALTERNATIVE\" (\"BOUNDARY\" \"Boundary_(ID_FPVkfB47oL+ZOyQ0T97MeA)\") NIL NIL NIL)" +
				"(\"DOCUMENT\" \"PDF\" " +
				 "(\"NAME*\" {50}ISO-8859-1''Infos%20erron%E9es%20du%20010910_1.pdf) NIL NIL \"BASE64\" 2513828 NIL " +
				 "(\"INLINE\" (\"FILENAME*\" {50}ISO-8859-1''Infos%20erron%E9es%20du%20010910_1.pdf)) NIL NIL) " +
				"\"MIXED\" (\"BOUNDARY\" \"Boundary_(ID_qsulokVQKbYW8nQHLPYp7Q)\") NIL NIL NIL) 34326 NIL " +
					 		  "(\"ATTACHMENT\" (\"FILENAME\" \"forwarded_message_0.eml\")) NIL NIL) " +
					 		  "\"MIXED\" (\"BOUNDARY\" \"-=Part.e5.7a00b7a55124f47f.12b1ed45c88.99ded3b40b1821b4=-\") NIL NIL NIL) 34360 NIL " +
					 		  "(\"ATTACHMENT\" (\"FILENAME\" \"forwarded_message_0.eml\")) NIL NIL) " +
					 		  "\"MIXED\" (\"BOUNDARY\" \"-=Part.1bc.1c1bae7fc5abaefb.12b1f2d8ba0.24d31e725226dabf=-\") NIL NIL NIL)";
		IMimePart result = parseStringAsBodyStructure(bs);
		checkMimeTree(
			createSimpleMimeTree("multipart", "MIXED", null, null,
					ImmutableMap.of("BOUNDARY", "-=Part.1bc.1c1bae7fc5abaefb.12b1f2d8ba0.24d31e725226dabf=-"),
			 createSimpleMimePart("TEXT", "HTML", null, "7BIT", 148, ImmutableMap.of("CHARSET", "UTF-8")),
			 createSimpleMimePart("MESSAGE", "RFC822", null, "7BIT", 2536015,
					 ImmutableMap.of("FILENAME", "forwarded_message_0.eml",
							 		 "BOUNDARY", "-=Part.e5.7a00b7a55124f47f.12b1ed45c88.99ded3b40b1821b4=-"),
			    createSimpleMimePart("TEXT", "HTML", null, "QUOTED-PRINTABLE", 424, ImmutableMap.of("CHARSET", "UTF-8")),
			    createSimpleMimePart("MESSAGE", "RFC822", null, "7BIT", 2534456,
			    		ImmutableMap.of("FILENAME", "forwarded_message_0.eml", "BOUNDARY", "Boundary_(ID_qsulokVQKbYW8nQHLPYp7Q)"),
			      createSimpleMimePart("multipart", "ALTERNATIVE", null, null, null,
			    		 ImmutableMap.of("BOUNDARY", "Boundary_(ID_FPVkfB47oL+ZOyQ0T97MeA)"),
			       createSimpleMimePart("TEXT", "PLAIN", null, "QUOTED-PRINTABLE", 1333,
			    		  ImmutableMap.of("FORMAT", "flowed", "CHARSET", "utf-8")),
			       createSimpleMimePart("multipart", "RELATED", null, null, null,
						    		 ImmutableMap.of("BOUNDARY", "Boundary_(ID_j1VqHCLvxMbObr0Y49iZgQ)"),
			        createSimpleMimePart("TEXT", "HTML", null, "7BIT", 1840, ImmutableMap.of("CHARSET", "utf-8")),
			        createSimpleMimePart("IMAGE", "JPEG", "part1.05060905.06000802@cpam-auch.cnamts.fr", "BASE64", 3544, new HashMap<String, String>()),
			        createSimpleMimePart("IMAGE", "JPEG", "part2.06080502.08080709@cpam-auch.cnamts.fr", "BASE64", 9638, new HashMap<String, String>()))),
			      createSimpleMimePart("DOCUMENT", "PDF", null, "BASE64", 2513828,
			    		  ImmutableMap.of("NAME", "Infos erronées du 010910_1.pdf",
			    				  "FILENAME", "Infos erronées du 010910_1.pdf"))  
			        ))), result);
			  
	}
	
	@Test @Slow
	public void testM1() {
		String bs = "(" +
				"(\"TEXT\" \"PLAIN\" " +
					"(\"CHARSET\" \"windows-1252\" \"FORMAT\" \"flowed\") NIL NIL \"8BIT\" 446 16 NIL NIL NIL)" +
				"(\"MESSAGE\" \"RFC822\" " +
					"(\"NAME\" \"Your travel information.eml\") NIL NIL \"7BIT\" 14002 " +
					"(\"Thu, 28 Oct 2010 08:51:20 +0100\" \"Your travel information\" " +
						"((\"PUJOL VOYAGES          (AGENTID00688986)\" NIL \"emailserver\" \"pop3.amadeus.net\")) " +
						"((\"PUJOL VOYAGES          (AGENTID00688986)\" NIL \"emailserver\" \"pop3.amadeus.net\")) " +
						"((NIL NIL \"Please do not respond\" NIL)(NIL NIL NIL NIL)) " +
						"((NIL NIL \"VIRGINIE.NOUZIES\" \"LINAGORA.COM\")) " +
						"NIL NIL NIL \"<20101028085120.ED772A145ED@mail2.amadeus.net>\") " +
					"(" +
					"(\"TEXT\" \"PLAIN\" " +
						"(\"CHARSET\" \"US-ASCII\") NIL \"Message in clear text\" \"QUOTED-PRINTABLE\" 5099 134 NIL NIL NIL)" +
					"(\"TEXT\" \"HTML\" (\"CHARSET\" \"iso-8859-1\") NIL \"Message in HTML form\" \"BASE64\" 4720 64 NIL NIL NIL) " +
						"\"ALTERNATIVE\" (\"BOUNDARY\" \"AGENTID00688986-=_HrKq2VwID3ocQY0E4wYbwX4wB\") NIL NIL) " +
				"281 NIL (\"ATTACHMENT\" (\"FILENAME\" \"Your travel information.eml\")) NIL) " +
					"\"MIXED\" (\"BOUNDARY\" \"------------060906090906080602020903\") NIL NIL)";
		IMimePart result = parseStringAsBodyStructure(bs);
		checkMimeTree(
				createSimpleMimeTree("multipart", "MIXED", null, null, ImmutableMap.of("BOUNDARY", "------------060906090906080602020903"), 
						createSimpleMimePart("TEXT", "PLAIN", null, "8BIT", 446,
							ImmutableMap.of("CHARSET", "windows-1252", "FORMAT", "flowed")),
						createSimpleMimePart("MESSAGE", "RFC822", null, "7BIT", 14002,
								ImmutableMap.of("NAME", "Your travel information.eml", "FILENAME", "Your travel information.eml",
												"BOUNDARY", "AGENTID00688986-=_HrKq2VwID3ocQY0E4wYbwX4wB"),
										createSimpleMimePart("TEXT", "PLAIN", null, "QUOTED-PRINTABLE", 5099,
												ImmutableMap.of("CHARSET", "US-ASCII")),
										createSimpleMimePart("TEXT", "HTML", null, "BASE64", 4720,
												ImmutableMap.of("CHARSET", "iso-8859-1")))),
				result);
				
	}
	
	@Test @Slow
	public void bugJaures() {
		String bs = "(" +
				"(\"TEXT\" \"PLAIN\" (\"CHARSET\" \"us-ascii\") NIL \"Notification\" \"7BIT\" 503 14 NIL NIL NIL NIL)" +
				"(\"MESSAGE\" \"DELIVERY-STATUS\" NIL NIL \"Delivery report\" \"7BIT\" 418 NIL NIL NIL NIL)" +
				"(\"MESSAGE\" \"RFC822\" NIL NIL \"Undelivered Message\" \"8BIT\" 6378 " +
						"(\"Thu, 23 Sep 10 09:12:39 +0200\" \"Nouvel =?UTF-8?Q?=C3=A9v=C3=A9nement=20de=20Jean=20Jaures=20sur=20OBM=20?=  =?UTF-8?Q?=3A=20sup=32?=\" " +
						"((\"Jean Jaures\" NIL \"jaures\" \"obm.matthieu.lng\")) " +
						"((\"Jean Jaures\" NIL \"jaures\" \"obm.matthieu.lng\")) " +
						"((\"Jean Jaures\" NIL \"jaures\" \"obm.matthieu.lng\")) " +
						"((\"=?UTF-8?Q?L=C3=A9on=20Blum?=\" NIL \"blum\" \"obm.matthieu.lng\")) NIL NIL NIL \"<20100923071239.E93BC697BF@debian-lenny-amd64.matthieu.lng>\") " +
					"(" +
						"(" + //3.1
							"(\"TEXT\" \"PLAIN\" (\"CHARSET\" \"UTF-8\") NIL NIL \"8BIT\" 790 20 NIL NIL NIL NIL)" +
							"(\"TEXT\" \"HTML\" (\"CHARSET\" \"UTF-8\") NIL NIL \"8BIT\" 1638 32 NIL NIL NIL NIL)" +
							"(\"TEXT\" \"CALENDAR\" (\"CHARSET\" \"UTF-8\" \"METHOD\" \"REQUEST\" \"CHARSET\" \"UTF-8\") NIL NIL \"8BIT\" 859 29 NIL NIL NIL NIL) " +
							"\"ALTERNATIVE\" (\"BOUNDARY\" \"7e51bdf35d0f4b4605c44068c47f8c7e\") NIL NIL NIL)" + //3.1
						"(\"APPLICATION\" \"ICS\" (\"NAME\" \"meeting.ics\") NIL NIL \"BASE64\" 1178 NIL (\"ATTACHMENT\" (\"FILENAME\" \"meeting.ics\")) NIL NIL) " + //3.2
					"\"MIXED\" (\"BOUNDARY\" \"7d1ea5ebf3d19eeb5e039c3b99dbda5c\") NIL NIL NIL) 142 NIL NIL NIL NIL) " +
					"\"REPORT\" (\"REPORT-TYPE\" \"delivery-status\" \"BOUNDARY\" \"5A7C5697BD.1286371801/debian-lenny-amd64.matthieu.lng\") NIL NIL NIL)";
		IMimePart result = parseStringAsBodyStructure(bs);
		checkMimeTree(
				createSimpleMimeTree("multipart", "REPORT", null, null,
						ImmutableMap.of("REPORT-TYPE", "delivery-status", "BOUNDARY", "5A7C5697BD.1286371801/debian-lenny-amd64.matthieu.lng"), 
						createSimpleMimePart("TEXT", "PLAIN", null, "7BIT",	 503, ImmutableMap.of("CHARSET", "us-ascii")),
						createSimpleMimePart("MESSAGE", "DELIVERY-STATUS", null, "7BIT", 418, new HashMap<String, String>()),
						createSimpleMimePart("MESSAGE", "RFC822", null, "8BIT", 6378, ImmutableMap.of("BOUNDARY", "7d1ea5ebf3d19eeb5e039c3b99dbda5c"), 
								createSimpleMimePart("multipart", "ALTERNATIVE", null, null, null, ImmutableMap.of("BOUNDARY", "7e51bdf35d0f4b4605c44068c47f8c7e"),
									createSimpleMimePart("TEXT", "PLAIN", null, "8BIT", 790, ImmutableMap.of("CHARSET", "UTF-8")),
									createSimpleMimePart("TEXT", "HTML", null, "8BIT", 1638, ImmutableMap.of("CHARSET", "UTF-8")),
									createSimpleMimePart("TEXT", "CALENDAR", null, "8BIT", 859, ImmutableMap.of("CHARSET", "UTF-8", "METHOD", "REQUEST"))),
								createSimpleMimePart("APPLICATION", "ICS", null, "BASE64", 1178, ImmutableMap.of("FILENAME", "meeting.ics", "NAME", "meeting.ics")))),
				result);
	}

	@Test @Slow
	public void testBugToulouse1() {
		String bs = 
			"(" +
			"(\"TEXT\" \"PLAIN\" (\"CHARSET\" \"iso-8859-1\") NIL NIL \"QUOTED-PRINTABLE\" 1090 35 NIL NIL NIL NIL)" +
			"(\"MESSAGE\" \"RFC822\" " +
				"(\"NAME\" \" =?iso-8859-1?Q?[Pr=E9fecture_de_Police]_-_Assistance_m=E9t	hodologique_po?= ur les =?iso-8859-1?Q?d=E9veloppements_PHP_/_Zend_Framew	ork.msg?=\") NIL NIL \"8BIT\" 50589 " +
				"(\"Wed, 15 Oct 2008 14:41:33 +0200\" \"=?iso-8859-1?Q?=5BPr=E9fecture_de_Police=5D_-_Assistance_m=E9t?= =?iso-8859-1?Q?hodologique_pour_les_d=E9veloppements_PHP_/_Zend_Framew?= =?iso-8859-1?Q?ork?=\" " +
					"((\"BARTHE Michel PP-DOSTL SDSIC DAM-SSI\" NIL \"michel.barthe\" \"interieur.gouv.fr\")) " +
					"((\"BARTHE Michel PP-DOSTL SDSIC DAM-SSI\" NIL \"michel.barthe\" \"interieur.gouv.fr\")) " +
					"((\"BARTHE Michel PP-DOSTL SDSIC DAM-SSI\" NIL \"michel.barthe\" \"interieur.gouv.fr\")) " +
					"((NIL NIL \"denis.larghero\" \"linagora.com\")) " +
					"((\"LESTREE Laurent PP-DOSTL SDSIC DAM-SSI\" NIL \"laurent.lestree\" \"interieur.gouv.fr\")(\"CERVONI Alain PP-DOSTL SDSIC DAM-SSI\" NIL \"alain.cervoni\" \"interieur.gouv.fr\")(\"ANTONI Christophe PP-DOSTL SDSIC DGM\" NIL \"christophe.antoni\" \"interieur.gouv.fr\")) NIL NIL \"<B7624895DC8CDA119BE6000BDB939DF605A896B6@msg16pp.ppol.mi>\") " +
					"(" +
						"(" +
							"(\"TEXT\" \"PLAIN\" (\"CHARSET\" \"iso-8859-1\") NIL NIL \"QUOTED-PRINTABLE\" 2351 65 NIL NIL NIL NIL)" +
							"(\"TEXT\" \"HTML\" (\"CHARSET\" \"iso-8859-1\") NIL NIL \"QUOTED-PRINTABLE\" 4441 95 NIL NIL NIL NIL) " +
							"\"ALTERNATIVE\" (\"BOUNDARY\" \"----_=_NextPart_001_01C92EC3.599812EE\") NIL NIL NIL)" +
						"(\"APPLICATION\" \"OCTET-STREAM\" (\"NAME\" \"CCT_2009_AssistancePHP_CdCFPrestation_VersionValidee.pdf\") NIL NIL \"BASE64\" 40064 NIL " +
							"(\"ATTACHMENT\" (\"FILENAME\" \"CCT_2009_AssistancePHP_CdCFPrestation_VersionValidee.pdf\")) NIL NIL) " +
					"\"MIXED\" (\"BOUNDARY\" \"----_=_NextPart_000_01C92EC3.599812EE\") NIL NIL NIL) 751 NIL " +
					"(\"ATTACHMENT\" (\"FILENAME\" \" =?iso-8859-1?Q?[Pr=E9fecture_de_Police]_-_Assistance_m=E9t	hodologique_po?= ur les =?iso-8859-1?Q?d=E9veloppements_PHP_/_Zend_Framew	ork.msg?=\")) NIL NIL) " +
			"\"MIXED\" (\"BOUNDARY\" \"----=_20081016144512_83167\") NIL NIL NIL)";
		IMimePart result = parseStringAsBodyStructure(bs);
		checkMimeTree(
				createSimpleMimeTree("multipart", "MIXED", null, null, ImmutableMap.of("BOUNDARY", "----=_20081016144512_83167"),
					createSimpleMimePart("TEXT", "PLAIN", null, "QUOTED-PRINTABLE", 1090,
						ImmutableMap.of("CHARSET", "iso-8859-1")),
					createSimpleMimePart("MESSAGE", "RFC822", null, "8BIT", 50589,
						ImmutableMap.of("NAME", "[Préfecture de Police] - Assistance mét	hodologique po ur les développements PHP / Zend Framew	ork.msg",
										"FILENAME", "[Préfecture de Police] - Assistance mét	hodologique po ur les développements PHP / Zend Framew	ork.msg",
										"BOUNDARY", "----_=_NextPart_000_01C92EC3.599812EE"),
							createSimpleMimePart("multipart", "ALTERNATIVE", null, null, null, ImmutableMap.of("BOUNDARY", "----_=_NextPart_001_01C92EC3.599812EE"), 
								createSimpleMimePart("TEXT", "PLAIN", null, "QUOTED-PRINTABLE", 2351, ImmutableMap.of("CHARSET", "iso-8859-1")),
								createSimpleMimePart("TEXT", "HTML", null, "QUOTED-PRINTABLE", 4441, ImmutableMap.of("CHARSET", "iso-8859-1"))),
						createSimpleMimePart("APPLICATION", "OCTET-STREAM", null, "BASE64", 40064,
								ImmutableMap.of("NAME", "CCT_2009_AssistancePHP_CdCFPrestation_VersionValidee.pdf",
										"FILENAME", "CCT_2009_AssistancePHP_CdCFPrestation_VersionValidee.pdf")))),
				result);
		result.toString();
	}
	
	@Test
	public void testToulouse2() {
		String bs ="(" +
				"(\"TEXT\" \"PLAIN\" (\"CHARSET\" \"UTF-8\") NIL NIL \"7BIT\" 36 2 NIL NIL NIL NIL)" +
				"(\"IMAGE\" \"PNG\" (\"NAME\" {72}Screenshot-SFR - Parametrage : parametrez votre  mobile ! - Chromium.png) " +
					"NIL NIL \"BASE64\" 322754 NIL " +
					"(\"ATTACHMENT\" (\"FILENAME\" {72}Screenshot-SFR - Parametrage : parametrez  votre mobile ! - Chromium.png)) " +
					"NIL NIL" +
				") \"MIXED\" (\"BOUNDARY\" \"JQXYJlTd9koAxLsisBpF/IX+AhDOQibtZgGtSogfOJM=\") NIL NIL NIL)";
		IMimePart result = parseStringAsBodyStructure(bs);
		checkMimeTree(
				createSimpleMimeTree("multipart", "MIXED", null, null, ImmutableMap.of("BOUNDARY", "JQXYJlTd9koAxLsisBpF/IX+AhDOQibtZgGtSogfOJM="), 
						createSimpleMimePart("TEXT", "PLAIN", null, "7BIT", 36,
							ImmutableMap.of("CHARSET", "UTF-8")),
						createSimpleMimePart("IMAGE", "PNG", null, "BASE64", 322754,
								ImmutableMap.of("NAME", "Screenshot-SFR - Parametrage : parametrez votre  mobile ! - Chromium.png", 
										"FILENAME", "Screenshot-SFR - Parametrage : parametrez  votre mobile ! - Chromium.png")
								)),
				result);
				
	}
	
	@Test @Slow
	public void testTorture() {
		String bs = 
			"(" +
				"(\"TEXT\" \"PLAIN\" NIL NIL \"Explanation\" \"7BIT\" 190 3 NIL NIL NIL NIL)" +
				"(\"MESSAGE\" \"RFC822\" NIL NIL \"Rich Text demo\" \"7BIT\" 4940 (\"Tue, 24 Dec 1991 08:14:27 -0500 (EST)\" \"Re: a MIME-Version misfeature\" ((\"Nathaniel Borenstein\" NIL \"nsb\" \"thumper.bellcore.com\")) ((\"Nathaniel Borenstein\" NIL \"nsb\" \"thumper.bellcore.com\")) ((\"Nathaniel Borenstein\" NIL \"nsb\" \"thumper.bellcore.com\")) ((NIL NIL \"ietf-822\" \"dimacs.rutgers.edu\")) NIL NIL NIL \"<sdJn_nq0M2YtNKaFtO@thumper.bellcore.com>\") " +
					"(" +
						"(\"TEXT\" \"PLAIN\" (\"CHARSET\" \"us-ascii\") NIL NIL \"7BIT\" 767 16 NIL NIL NIL NIL)" +
						"(" +
							"(\"TEXT\" \"RICHTEXT\" NIL NIL NIL \"7BIT\" 887 13 NIL NIL NIL NIL) " +
							"\"MIXED\" (\"BOUNDARY\" \"Alternative_Boundary_8dJn:mu0M2Yt5KaFZ8AdJn:mu0M2Yt1KaFdA\") NIL NIL NIL)" +
						"(\"APPLICATION\" \"ANDREW-INSET\" NIL NIL NIL \"7BIT\" 917 NIL NIL NIL NIL) \"ALTERNATIVE\" (\"BOUNDARY\" \"Interpart_Boundary_AdJn:mu0M2YtJKaFh9AdJn:mu0M2YtJKaFk=\") NIL NIL NIL) 107 NIL NIL NIL NIL)" +
				"(\"MESSAGE\" \"RFC822\" NIL NIL \"Voice Mail demo\" \"7BIT\" 562276 (\"Tue, 8 Oct 91 10:25:36 EDT\" \"Re: multipart mail\" ((\"Nathaniel Borenstein\" NIL \"nsb\" \"thumper.bellcore.com\")) ((\"Nathaniel Borenstein\" NIL \"nsb\" \"thumper.bellcore.com\")) ((\"Nathaniel Borenstein\" NIL \"nsb\" \"thumper.bellcore.com\")) ((NIL NIL \"mrc\" \"panda.com\")) NIL NIL NIL \"<9110081425.AA00616@greenbush.bellcore.com>\") " +
				"(\"AUDIO\" \"BASIC\" NIL NIL \"Hi Mark\" \"BASE64\" 561308 NIL NIL NIL NIL) 7608 NIL NIL NIL NIL)" +
				"(\"AUDIO\" \"BASIC\" NIL NIL \"Flint phone\" \"BASE64\" 36234 NIL NIL NIL NIL)" +
				"(\"IMAGE\" \"PBM\" NIL NIL \"MTR's photo\" \"BASE64\" 1814 NIL NIL NIL NIL)" +
				"(\"MESSAGE\" \"RFC822\" NIL NIL \"Star Trek Party\" \"7BIT\" 182936 (\"Thu, 19 Sep 91 12:41:43 EDT\" \"No Subject\" ((\"Nathaniel Borenstein\" NIL \"nsb\" \"thumper.bellcore.com\")) ((\"Nathaniel Borenstein\" NIL \"nsb\" \"thumper.bellcore.com\")) ((\"Nathaniel Borenstein\" NIL \"nsb\" \"thumper.bellcore.com\")) ((NIL NIL \"abel\" \"thumper.bellcore.com\")(NIL NIL \"bianchi\" \"thumper.bellcore.com\")(NIL NIL \"braun\" \"thumper.bellcore.com\")(NIL NIL \"cameron\" \"thumper.bellcore.com\")(NIL NIL \"carmen\" \"thumper.bellcore.com\")(NIL NIL \"jfp\" \"thumper.bellcore.com\")(NIL NIL \"jxr\" \"thumper.bellcore.com\")(NIL NIL \"kraut\" \"thumper.bellcore.com\")(NIL NIL \"lamb\" \"thumper.bellcore.com\")(NIL NIL \"lowery\" \"thumper.bellcore.com\")(NIL NIL \"lynn\" \"thumper.bellcore.com\")(NIL NIL \"mlittman\" \"thumper.bellcore.com\")(NIL NIL \"nancyg\" \"thumper.bellcore.com\")(NIL NIL \"sau\" \"thumper.bellcore.com\")(NIL NIL \"shoshi\" \"thumper.bellcore.com\")(NIL NIL \"slr\" \"thumper.bellcore.com\")(NIL NIL \"stornett\" \"flash.bellcore.com\")(NIL NIL \"tkl\" \"thumper.bellcore.com\")) ((NIL NIL \"nsb\" \"thumper.bellcore.com\")(NIL NIL \"trina\" \"flash.bellcore.com\")) NIL NIL \"<9109191641.AA12840@greenbush.bellcore.com>\") " +
				"(" +
					"(" +
						"(\"TEXT\" \"PLAIN\" (\"CHARSET\" \"us-ascii\") NIL NIL \"7BIT\" 731 16 NIL NIL NIL NIL)" +
						"(\"AUDIO\" \"X-SUN\" NIL NIL \"He's dead, Jim\" \"BASE64\" 31472 NIL NIL NIL NIL) " +
						"\"MIXED\" (\"BOUNDARY\" \"Where_No_One_Has_Gone_Before\") NIL NIL NIL)" +
					"(" +
						"(\"IMAGE\" \"GIF\" NIL NIL \"Kirk/Spock/McCoy\" \"BASE64\" 26000 NIL NIL NIL NIL)" +
						"(\"IMAGE\" \"GIF\" NIL NIL \"Star Trek Next Generation\" \"BASE64\" 18666 NIL NIL NIL NIL)" +
						"(\"APPLICATION\" \"X-BE2\" (\"VERSION\" \"12\") NIL NIL \"7BIT\" 46125 NIL NIL NIL NIL)" +
						"(\"APPLICATION\" \"ATOMICMAIL\" (\"VERSION\" \"1.12\") NIL NIL \"7BIT\" 9203 NIL NIL NIL NIL) " +
						"\"MIXED\" (\"BOUNDARY\" \"Where_No_Man_Has_Gone_Before\") NIL NIL NIL)" +
					"(\"AUDIO\" \"X-SUN\" NIL NIL \"Distress calls\" \"BASE64\" 47822 NIL NIL NIL NIL) " +
					"\"MIXED\" (\"BOUNDARY\" \"Outermost_Trek\") NIL NIL NIL) 4574 NIL NIL NIL NIL)" +
				"(\"MESSAGE\" \"RFC822\" NIL NIL \"Digitizer test\" \"7BIT\" 86163 (\"Fri, 24 May 91 10:40:25 EDT\" \"A cheap digitizer test\" ((\"Stephen A Uhler\" NIL \"sau\" \"sleepy.bellcore.com\")) ((\"Stephen A Uhler\" NIL \"sau\" \"sleepy.bellcore.com\")) ((\"Stephen A Uhler\" NIL \"sau\" \"sleepy.bellcore.com\")) ((NIL NIL \"nsb\" \"sleepy.bellcore.com\")) ((NIL NIL \"sau\" \"sleepy.bellcore.com\")) NIL NIL \"<9105241440.AA08935@sleepy.bellcore.com>\") " +
				"(" +
					"(\"TEXT\" \"PLAIN\" (\"CHARSET\" \"us-ascii\") NIL NIL \"7BIT\" 21 0 NIL NIL NIL NIL)" +
					"(\"IMAGE\" \"PGM\" NIL NIL \"Bellcore mug\" \"BASE64\" 84174 NIL NIL NIL NIL)" +
					"(\"TEXT\" \"PLAIN\" (\"CHARSET\" \"us-ascii\") NIL NIL \"7BIT\" 267 8 NIL NIL NIL NIL) " +
					"\"MIXED\" (\"BOUNDARY\" \"mail.sleepy.sau.144.8891\") NIL NIL NIL) 486 NIL NIL NIL NIL)" +
				"(\"MESSAGE\" \"RFC822\" NIL NIL \"More Imagery\" \"7BIT\" 74487 (\"Fri, 7 Jun 91 09:09:05 EDT\" \"meta-mail\" ((\"Stephen A Uhler\" NIL \"sau\" \"sleepy.bellcore.com\")) ((\"Stephen A Uhler\" NIL \"sau\" \"sleepy.bellcore.com\")) ((\"Stephen A Uhler\" NIL \"sau\" \"sleepy.bellcore.com\")) ((NIL NIL \"nsb\" \"sleepy.bellcore.com\")) NIL NIL NIL \"<9106071309.AA00574@sleepy.bellcore.com>\") " +
				"(" +
					"(\"TEXT\" \"PLAIN\" (\"CHARSET\" \"us-ascii\") NIL NIL \"7BIT\" 1246 26 NIL NIL NIL NIL)" +
					"(\"IMAGE\" \"PBM\" NIL NIL \"Mail architecture slide\" \"BASE64\" 71686 NIL NIL NIL NIL) " +
					"\"MIXED\" (\"BOUNDARY\" \"mail.sleepy.sau.158.532\") NIL NIL NIL) 433 NIL NIL NIL NIL)" +
				"(\"MESSAGE\" \"RFC822\" NIL NIL \"PostScript demo\" \"7BIT\" 398041 (\"Mon, 7 Oct 91 12:13:55 EDT\" \"An image that went gif->ppm->ps\" ((\"Nathaniel Borenstein\" NIL \"nsb\" \"thumper.bellcore.com\")) ((\"Nathaniel Borenstein\" NIL \"nsb\" \"thumper.bellcore.com\")) ((\"Nathaniel Borenstein\" NIL \"nsb\" \"thumper.bellcore.com\")) ((NIL NIL \"mrc\" \"cac.washington.edu\")) NIL NIL NIL \"<9110071613.AA10867@greenbush.bellcore.com>\") " +
					"(\"APPLICATION\" \"POSTSCRIPT\" NIL NIL \"Captain Picard\" \"7BIT\" 397154 NIL NIL NIL NIL) 6439 NIL NIL NIL NIL)" +
				"(\"IMAGE\" \"GIF\" NIL NIL \"Quoted-Printable test\" \"BASE64\" 45596 NIL NIL NIL NIL)" +
				"(\"MESSAGE\" \"RFC822\" NIL NIL \"q-p vs. base64 test\" \"7BIT\" 82165 (\"Sat, 26 Oct 91 09:35:10 EDT\" \"Audio in q-p\" ((\"Nathaniel Borenstein\" NIL \"nsb\" \"thumper.bellcore.com\")) ((\"Nathaniel Borenstein\" NIL \"nsb\" \"thumper.bellcore.com\")) ((\"Nathaniel Borenstein\" NIL \"nsb\" \"thumper.bellcore.com\")) ((NIL NIL \"mrc\" \"akbar.cac.washington.edu\")) NIL NIL NIL \"<9110261335.AA01130@greenbush.bellcore.com>\") " +
				"(" +
					"(\"AUDIO\" \"BASIC\" NIL NIL \"I'm sorry, Dave (q-p)\" \"BASE64\" 40580 NIL NIL NIL NIL)" +
					"(\"AUDIO\" \"BASIC\" NIL NIL \"I'm sorry, Dave (BASE64)\" \"BASE64\" 40634 NIL NIL NIL NIL) " +
					"\"MIXED\" (\"BOUNDARY\" \"hal_9000\") NIL NIL NIL) 1099 NIL NIL NIL NIL)" +
				"(\"MESSAGE\" \"RFC822\" NIL NIL \"Multiple encapsulation\" \"7BIT\" 297367 (\"Thu, 24 Oct 1991 17:32:56 -0700 (PDT)\" \"Here's some more\" ((\"Mark Crispin\" NIL \"MRC\" \"CAC.Washington.EDU\")) ((\"Mark Crispin\" NIL \"mrc\" \"Tomobiki-Cho.CAC.Washington.EDU\")) ((\"Mark Crispin\" NIL \"MRC\" \"CAC.Washington.EDU\")) ((\"Mark Crispin\" NIL \"MRC\" \"CAC.Washington.EDU\")) NIL NIL NIL \"<MailManager.688350776.11603.mrc@Tomobiki-Cho.CAC.Washington.EDU>\") " +
				"(" +
					"(\"APPLICATION\" \"POSTSCRIPT\" NIL NIL \"The Simpsons!!\" \"7BIT\" 40531 NIL NIL NIL NIL)" +
					"(\"TEXT\" \"PLAIN\" (\"CHARSET\" \"us-ascii\") NIL \"Alice's PDP-10 w/ TECO & DDT\" \"7BIT\" 13685 237 NIL NIL NIL NIL)" +
					"(\"MESSAGE\" \"RFC822\" NIL NIL \"Going deeper\" \"7BIT\" 242126 (\"Thu, 24 Oct 1991 17:08:20 -0700 (PDT)\" \"A Multipart message\" ((\"Nathaniel S. Borenstein\" NIL \"nsb\" \"thumper.bellcore.com\")) ((\"Nathaniel S. Borenstein\" NIL \"nsb\" \"thumper.bellcore.com\")) ((\"Nathaniel S. Borenstein\" NIL \"nsb\" \"thumper.bellcore.com\")) ((NIL NIL \"nsb\" \"thumper.bellcore.com\")) NIL NIL NIL \"<1291802183.2663.32.camel@matthieu-laptop.lng>\") " +
					"(" +
						"(\"TEXT\" \"PLAIN\" (\"CHARSET\" \"us-ascii\") NIL NIL \"7BIT\" 319 7 NIL NIL NIL NIL)" +
						"(" +
							"(\"IMAGE\" \"GIF\" NIL NIL \"Bunny\" \"BASE64\" 3276 NIL NIL NIL NIL)" +
							"(\"AUDIO\" \"BASIC\" NIL NIL \"TV Theme songs\" \"BASE64\" 156706 NIL NIL NIL NIL) " +
							"\"PARALLEL\" (\"BOUNDARY\" \"seconddivider\") NIL NIL NIL)" +
						"(\"APPLICATION\" \"ATOMICMAIL\" NIL NIL NIL \"7BIT\" 4924 NIL NIL NIL NIL)" +
						"(\"MESSAGE\" \"RFC822\" NIL NIL \"Yet another level deeper...\" \"7BIT\" 75980 (\"Thu, 24 Oct 1991 17:09:10 -0700 (PDT)\" \"Monster!\" ((\"Nathaniel Borenstein\" NIL \"nsb\" \"thumper.bellcore.com\")) ((\"Nathaniel Borenstein\" NIL \"nsb\" \"thumper.bellcore.com\")) ((\"Nathaniel Borenstein\" NIL \"nsb\" \"thumper.bellcore.com\")) NIL NIL NIL NIL \"<1291802183.2663.33.camel@matthieu-laptop.lng>\") " +
							"(\"AUDIO\" \"X-SUN\" NIL NIL \"I'm Twying...\" \"BASE64\" 75682 NIL NIL NIL NIL) 1032 NIL NIL NIL NIL) " +
							"\"MIXED\" (\"BOUNDARY\" \"foobarbazola\") NIL NIL NIL) 2101 NIL NIL NIL NIL) " +
						"\"MIXED\" (\"BOUNDARY\" \"16819560-2078917053-688350843:#11603\") NIL NIL NIL) 4182 NIL NIL NIL NIL) " +
					"\"MIXED\" (\"BOUNDARY\" \"owatagusiam\") NIL NIL NIL)";
		MimeMessage message = 
		createSimpleMimeTree("multipart", "MIXED", null, null, ImmutableMap.of("BOUNDARY", "owatagusiam"),
			createSimpleMimePart("TEXT", "PLAIN", null, "7BIT", 190, new HashMap<String, String>()),
			createSimpleMimePart("MESSAGE", "RFC822", null, "7BIT", 4940, ImmutableMap.of("BOUNDARY", "Interpart_Boundary_AdJn:mu0M2YtJKaFh9AdJn:mu0M2YtJKaFk="),
				createSimpleMimePart("TEXT", "PLAIN", null, "7BIT", 767, ImmutableMap.of("CHARSET", "US-ASCII")),
				createSimpleMimePart("multipart", "MIXED", null, null, null, ImmutableMap.of("BOUNDARY", "Alternative_Boundary_8dJn:mu0M2Yt5KaFZ8AdJn:mu0M2Yt1KaFdA"),
					createSimpleMimePart("TEXT", "RICHTEXT", null, "7BIT", 887, new HashMap<String, String>())),
				createSimpleMimePart("APPLICATION", "ANDREW-INSET", null, "7BIT", 917, new HashMap<String, String>())),
			createSimpleMimePart("MESSAGE", "RFC822", null, "7BIT", 562276, new HashMap<String, String>(),
				createSimpleMimePart("AUDIO", "BASIC", null, "BASE64", 561308, new HashMap<String, String>())),
			createSimpleMimePart("AUDIO", "BASIC", null, "BASE64", 36234, new HashMap<String, String>()),
			createSimpleMimePart("IMAGE", "PBM", null, "BASE64", 1814, new HashMap<String, String>()),
			createSimpleMimePart("MESSAGE", "RFC822", null, "7BIT", 182936, ImmutableMap.of("BOUNDARY", "Outermost_Trek"),
				createSimpleMimePart("multipart", "MIXED", null, null, null, ImmutableMap.of("BOUNDARY", "Where_No_One_Has_Gone_Before"),
					createSimpleMimePart("TEXT", "PLAIN", null, "7BIT", 731, ImmutableMap.of("CHARSET", "US-ASCII")),
					createSimpleMimePart("AUDIO", "X-SUN", null, "BASE64", 31472, new HashMap<String, String>())),
				createSimpleMimePart("multipart", "MIXED", null, null, null, ImmutableMap.of("BOUNDARY", "Where_No_Man_Has_Gone_Before"),
					createSimpleMimePart("IMAGE", "GIF", null, "BASE64", 26000, new HashMap<String, String>()),
					createSimpleMimePart("IMAGE", "GIF", null, "BASE64", 18666, new HashMap<String, String>()),
					createSimpleMimePart("APPLICATION", "X-BE2", null, "7BIT", 46125, ImmutableMap.of("VERSION", "12")),
					createSimpleMimePart("APPLICATION", "ATOMICMAIL", null, "7BIT", 9203, ImmutableMap.of("VERSION", "1.12"))),
				createSimpleMimePart("AUDIO", "X-SUN", null, "BASE64", 47822, new HashMap<String, String>())),
			createSimpleMimePart("MESSAGE", "RFC822", null, "7BIT", 86163, ImmutableMap.of("BOUNDARY", "mail.sleepy.sau.144.8891"),
				createSimpleMimePart("TEXT", "PLAIN", null, "7BIT", 21, ImmutableMap.of("CHARSET", "US-ASCII")),
				createSimpleMimePart("IMAGE", "PGM", null, "BASE64", 84174, new HashMap<String, String>()),
				createSimpleMimePart("TEXT", "PLAIN", null, "7BIT", 267, ImmutableMap.of("CHARSET", "US-ASCII"))),
			createSimpleMimePart("MESSAGE", "RFC822", null, "7BIT", 74487, ImmutableMap.of("BOUNDARY", "mail.sleepy.sau.158.532"),
				createSimpleMimePart("TEXT", "PLAIN", null, "7BIT", 1246, ImmutableMap.of("CHARSET", "US-ASCII")),
				createSimpleMimePart("IMAGE", "PBM", null, "BASE64", 71686, new HashMap<String, String>())),
			createSimpleMimePart("MESSAGE", "RFC822", null, "7BIT", 398041, new HashMap<String, String>(),
				createSimpleMimePart("APPLICATION", "POSTSCRIPT", null, "7BIT", 397154, new HashMap<String, String>())),
			createSimpleMimePart("IMAGE", "GIF", null, "BASE64", 45596, new HashMap<String, String>()),
			createSimpleMimePart("MESSAGE", "RFC822", null, "7BIT", 82165, ImmutableMap.of("BOUNDARY", "hal_9000"),
				createSimpleMimePart("AUDIO", "BASIC", null, "BASE64", 40580, new HashMap<String, String>()),
				createSimpleMimePart("AUDIO", "BASIC", null, "BASE64", 40634, new HashMap<String, String>())),
			createSimpleMimePart("MESSAGE", "RFC822", null, "7BIT", 297367, ImmutableMap.of("BOUNDARY", "16819560-2078917053-688350843:#11603"),
				createSimpleMimePart("APPLICATION", "POSTSCRIPT", null, "7BIT", 40531, new HashMap<String, String>()),
				createSimpleMimePart("TEXT", "PLAIN", null, "7BIT", 13685, ImmutableMap.of("CHARSET", "US-ASCII")),
				createSimpleMimePart("MESSAGE", "RFC822", null, "7BIT", 242126, ImmutableMap.of("BOUNDARY", "foobarbazola"),
					createSimpleMimePart("TEXT", "PLAIN", null, "7BIT", 319, ImmutableMap.of("CHARSET", "US-ASCII")),
					createSimpleMimePart("multipart", "PARALLEL", null, null, null, ImmutableMap.of("BOUNDARY", "seconddivider"),
						createSimpleMimePart("IMAGE", "GIF", null, "BASE64", 3276, new HashMap<String, String>()),
						createSimpleMimePart("AUDIO", "BASIC", null, "BASE64", 156706, new HashMap<String, String>())),
					createSimpleMimePart("APPLICATION", "ATOMICMAIL", null, "7BIT", 4924, new HashMap<String, String>()),
					createSimpleMimePart("MESSAGE", "RFC822", null, "7BIT", 75980, new HashMap<String, String>(),
						createSimpleMimePart("AUDIO", "X-SUN", null, "BASE64", 75682, new HashMap<String, String>())))));
		IMimePart result = parseStringAsBodyStructure(bs);
		result.toString();
		checkMimeTree(message, result);
	}
}
