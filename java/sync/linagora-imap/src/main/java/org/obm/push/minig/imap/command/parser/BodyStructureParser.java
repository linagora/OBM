/* ***** BEGIN LICENSE BLOCK *****
 * 
 * Copyright (C) 2011-2014  Linagora
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

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import org.obm.push.mail.mime.BodyParam;
import org.obm.push.mail.mime.BodyParams;
import org.obm.push.mail.mime.MimeMessageImpl;
import org.obm.push.mail.mime.MimePart;
import org.obm.push.mail.mime.MimePartImpl;
import org.obm.push.mail.mime.MimePartImpl.Builder;
import org.obm.push.minig.imap.mime.impl.BodyParamParser;
import org.parboiled.Parboiled;
import org.parboiled.Rule;
import org.parboiled.annotations.BuildParseTree;
import org.parboiled.parserunners.RecoveringParseRunner;
import org.parboiled.parserunners.TracingParseRunner;
import org.parboiled.support.ParsingResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;

public class BodyStructureParser {

	private final static Logger logger = LoggerFactory
			.getLogger(BodyStructureParser.class);
	
	@BuildParseTree
	static class Rules extends AbstractImapBaseParser {
	
		Rule addrAdl() {
			return nstringNoStack();
		}

		Rule address() {
			return Sequence('(', addrName(), whitespaces(), addrAdl(), whitespaces(), 
					addrMailbox(), whitespaces(), addrHost(), ')');

		}

		Rule addrHost() {
			return nstringNoStack();
		}

		Rule addrMailbox() {
			return nstringNoStack();
		}

		Rule addrName() {
			return nstringNoStack();
		}

		Rule body() {
			return Sequence('(', FirstOf(bodyType1part(), bodyTypeMPart()), ')');
		}

		Rule bodyExt1Part() {
			return Sequence(
						bodyFldMd5(),
						Optional(whitespaces(), bodyFldDsp(),
							Optional(whitespaces(), bodyFldLang(),
									Optional(whitespaces(), bodyFldLoc(),
											ZeroOrMore(whitespaces(), bodyExtension())))));
		}
		
		Rule bodyExtension() {
			return FirstOf(nstringNoStack(), numberNoStack(), Sequence('(', bodyExtension(), ZeroOrMore(whitespaces(), bodyExtension()), ')'));
		}

		Rule bodyExtMPart() {
			return Sequence(
						bodyFldParam(),
						Optional(whitespaces(), bodyFldDsp(),
							Optional(whitespaces(), bodyFldLang(), 
									Optional(whitespaces(), bodyFldLoc(),
											ZeroOrMore(whitespaces(), bodyExtension())))));
		}

		Rule bodyFields() {
			return SequenceWithWhitespaces(
					bodyFldParam(), bodyFldId(), bodyFldDesc(), bodyFldEnc(), bodyFldOctets());
		}

		Rule bodyFldDesc() {
			return nstringNoStack();
		}

		Rule bodyFldDsp() {
			
			return FirstOf
					(Sequence('(', string(), recordContentDisposition(), whitespaces(), bodyFldParam(), ')'), 
					nilNoStack());
		}

		Rule bodyFldEnc() {
			return Sequence(
					FirstOf(
						Sequence('"', 
								FirstOf("7BIT", "8BIT", "BINARY", "BASE64", "QUOTED-PRINTABLE"), push(match()), 
								'"'),
						string()),
					recordEncoding());
		}

		Rule bodyFldId() {
			return Sequence(nstring(), recordId());
		}

		Rule bodyFldLang() {
			return FirstOf(nstringNoStack(), Sequence('(', stringNoStack(), ZeroOrMore(whitespaces(), stringNoStack()), ')'));
		}

		Rule bodyFldLines() {
			return numberNoStack();
		}

		Rule bodyFldLoc() {
			return Sequence(nstring(), recordLocation());
		}

		Rule bodyFldMd5() {
			return nstringNoStack();
		}

		Rule bodyFldOctets() {
			return Sequence(number(), recordSize());
		}

		Rule bodyFldParam() {

			return Sequence(
					push(BodyParams.builder()),
					FirstOf(
						Sequence(
								'(', 
								string(),
								whitespaces(),
								string(),
								addBodyParam(),
								ZeroOrMore(
										whitespaces(), string(), whitespaces(), string(), 
										addBodyParam()), 
						')'),
						nilNoStack()),
					recordBodyParams());
		}

		Rule bodyType1part() {
			//be careful, bodyTypeBasic is an incompatible superset of bodyTypeText
			//so rules must be kept in this order
			return Sequence( 
						FirstOf(bodyTypeText(), bodyTypeMsg(), bodyTypeBasic()),
						Optional(whitespaces(), bodyExt1Part()),
						addMimePart());
		}

		Rule bodyTypeBasic() {
			return Sequence(push(MimePartImpl.builder()), mediaBasic(), whitespaces(), bodyFields());
		}

		Rule bodyTypeMPart() {
			return Sequence(
						push(MimePartImpl.builder().primaryMimeType("multipart")), 
						OneOrMore(body()), 
						whitespaces(), mediaSubType(), recordMimeSubtype(), 
						Optional(whitespaces(), bodyExtMPart()),
						addMimePart());
		}

		Rule bodyTypeMsg() {
			return Sequence(push(MimePartImpl.embeddedMessageBuilder()), mediaMessage(), whitespaces(), bodyFields(), whitespaces(), envelope(), 
					whitespaces(), body(), whitespaces(), bodyFldLines());
		}
		
		Rule bodyTypeText() {
			return Sequence(push(MimePartImpl.builder()), mediaText(), whitespaces(), bodyFields(), whitespaces(), bodyFldLines());
		}

		Rule envBcc() {
			return addressListNoStack();
		}

		Rule addressListNoStack() {
			return FirstOf(Sequence('(', OneOrMore(address()),')'), nilNoStack());
		}

		Rule envCc() {
			return addressListNoStack();
		}

		Rule envDate() {
			return nstringNoStack();
		}

		Rule envelope() {
			return Sequence('(', envDate(), whitespaces(), 
					envSubject(), whitespaces(),
					envFrom(), whitespaces(),
					envSender(), whitespaces(),
					envReplyTo(), whitespaces(),
					envTo(), whitespaces(),
					envCc(), whitespaces(),
					envBcc(), whitespaces(),
					envInReplyTo(), whitespaces(),
					envMessageId(), ')');
		}

		Rule envFrom() {
			return addressListNoStack();
		}

		Rule envInReplyTo() {
			return nstringNoStack();
		}

		Rule envMessageId() {
			return nstringNoStack();
		}

		Rule envReplyTo() {
			return addressListNoStack();
		}

		Rule envSender() {
			return addressListNoStack();
		}

		Rule envSubject() {
			return nstringNoStack();
		}

		Rule envTo() {
			return addressListNoStack();
		}

		Rule mediaBasic() {
			return Sequence(
						FirstOf(
							Sequence('"', FirstOf("APPLICATION", "AUDIO", "IMAGE", "MESSAGE", "VIDEO"), push(match()), '"'),
							string()),
						recordMimeType(),
						whitespaces(),
						mediaSubType(),
						recordMimeSubtype()
					);
		}
		
		Rule mediaMessage() {
			return Sequence("\"MESSAGE\"", whitespaces(), "\"RFC822\"", recordMessageRfc822());
		}

		Rule mediaSubType() {
			return string();
		}

		Rule mediaText() {
			return Sequence("\"TEXT\"", whitespaces(), mediaSubType(), recordTextMimeType());
		}

		public Rule rule() {
			return Sequence(push(MimeMessageImpl.builder()), body(), EOI);
		}
		
		boolean addBodyParam() {
			swap();
			BodyParam bodyParam = BodyParamParser.parse((String)pop(), (String)pop());
			BodyParams.Builder bodyParams = (BodyParams.Builder)peek();
			bodyParams.add(bodyParam);
			return true;
		}
		
		boolean addMimePart() {
			MimePartImpl.Builder obj = (MimePartImpl.Builder) pop();
			MimePart.Builder<?> mimeParent = (MimePart.Builder<?>) peek();
			mimeParent.addChild(obj.build());
			return true;
		}
				
		boolean recordContentDisposition() {
			String contentDisposition = (String)pop();
			MimePartImpl.Builder mimePartBuilder = (Builder) peek();
			mimePartBuilder.contentDisposition(contentDisposition);
			return true;
		}
		
		boolean recordEncoding() {
			String encoding = (String)pop();
			MimePartImpl.Builder mimePartBuilder = (Builder) peek();
			mimePartBuilder.encoding(encoding);
			return true;
		}

		
		boolean recordId() {
			String contentId = (String)pop();
			MimePartImpl.Builder mimePartBuilder = (Builder) peek();
			mimePartBuilder.contentId(stripSurroundingStripes(contentId));
			return true;
		}

		String stripSurroundingStripes(String payload) {
			if (payload != null && payload.startsWith("<") && payload.endsWith(">")) {
				return payload.substring(1, payload.length() - 1);
			} else {
				return payload;
			}
		}

		boolean recordLocation() {
			String contentLocation = (String)pop();
			MimePartImpl.Builder mimePartBuilder = (Builder) peek();
			mimePartBuilder.contentLocation(contentLocation);
			return true;
		}
		
		boolean recordBodyParams() {
			BodyParams.Builder bodyParamsBuilder = (BodyParams.Builder)pop();
			MimePartImpl.Builder mimePartBuilder = (Builder) peek();
			mimePartBuilder.bodyParams(bodyParamsBuilder.build());
			return true;
		}

		boolean recordMimeType() {
			String primaryMimeType = (String)pop();
			MimePartImpl.Builder mimePartBuilder = (Builder) peek();
			mimePartBuilder.primaryMimeType(primaryMimeType);
			return true;
		}

		
		boolean recordMimeSubtype() {
			String mimeType = (String)pop();
			MimePartImpl.Builder mimePartBuilder = (Builder) peek();
			mimePartBuilder.subMimeType(mimeType);
			return true;
		}

		boolean recordTextMimeType() {
			String subMimeType = (String) pop();
			MimePartImpl.Builder mimePartBuilder = (Builder) peek();
			mimePartBuilder.primaryMimeType("TEXT").subMimeType(subMimeType);
			return true;
		}
		
		boolean recordMessageRfc822() {
			MimePartImpl.Builder mimePartBuilder = (Builder) peek();
			mimePartBuilder.primaryMimeType("MESSAGE").subMimeType("RFC822");
			return true;
		}
		
		boolean recordSize() {
			Integer size = (Integer)pop();
			MimePartImpl.Builder mimePartBuilder = (Builder) peek();
			mimePartBuilder.size(size);
			return true;
		}
	}
	
	private static final Rules parser = Parboiled.createParser(BodyStructureParser.Rules.class);
	
	public MimeMessageImpl.Builder parseBodyStructureDebug(String payload) {
		Rules parserInstance = parser.newInstance();
		TracingParseRunner<MimeMessageImpl.Builder> runner = new TracingParseRunner<MimeMessageImpl.Builder>(parserInstance.rule());
		try {
			ParsingResult<MimeMessageImpl.Builder> result = runner.run(payload);
			return result.resultValue;
		} finally {
			logToFile(runner);
		}
	}


	private static void logToFile(TracingParseRunner<MimeMessageImpl.Builder> runner) {
		FileOutputStream fileOutputStream = null;
		try {
			fileOutputStream = new FileOutputStream("/tmp/log");
			OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, Charsets.UTF_8);
			outputStreamWriter.write(runner.getLog().toString());
			outputStreamWriter.close();
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (fileOutputStream != null) {
				try {
					fileOutputStream.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
	}

	
	public MimeMessageImpl.Builder parseBodyStructure(String payload) {
		Rules parserInstance = parser.newInstance();
		RecoveringParseRunner<MimeMessageImpl.Builder> runner = 
			//new RecoveringParseRunner<MimeTree>(parserInstance.rule(), new DebugValueStack());
			new RecoveringParseRunner<MimeMessageImpl.Builder>(parserInstance.rule());

		ParsingResult<MimeMessageImpl.Builder> result = runner.run(payload);
		return result.resultValue;

		//logger.info(ParseTreeUtils.printNodeTree(result));
	}
}
