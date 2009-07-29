/* ***** BEGIN LICENSE BLOCK *****
 * Version: GPL 2.0
 *
 * The contents of this file are subject to the GNU General Public
 * License Version 2 or later (the "GPL").
 *
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * The Initial Developer of the Original Code is
 *   obm.org project members
 *
 * ***** END LICENSE BLOCK ***** */

package org.obm.caldav.server.reports;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.obm.caldav.server.share.filter.CompFilter;
import org.obm.caldav.server.share.filter.Filter;
import org.obm.caldav.server.share.filter.ParamFilter;
import org.obm.caldav.server.share.filter.PropFilter;
import org.obm.caldav.server.share.filter.TextMatch;
import org.obm.caldav.server.share.filter.TimeRange;
import org.obm.caldav.utils.DOMUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class FilterParser {

	private static Log logger = LogFactory.getLog(FilterParser.class);

	private final static String patternUTC = "yyyyMMdd'T'HHmmss'Z'";

	public static Filter parse(Document doc) {
		Filter filter = null;

		Element root = doc.getDocumentElement();
		Element filterNode = DOMUtils.getUniqueElement(root, "filter",Filter.NAMESPACE);
		if (filterNode != null) {
			filter = new Filter();
			Element compFilterNodePere = DOMUtils.getUniqueElement(filterNode,
					"comp-filter",CompFilter.NAMESPACE);
			if (compFilterNodePere != null) {
				filter.setCompFilter(parseCompFilter(compFilterNodePere));
			}
		}
		return filter;
	}

	private static CompFilter parseCompFilter(Element compFilterNode) {
		CompFilter compFilter = null;
		if (compFilterNode != null) {
			compFilter = new CompFilter();
			String name = compFilterNode.getAttribute("name");
			compFilter.setName(name);

			compFilter.setIsNotDefined(getIsNotDefined(compFilterNode));
			compFilter.setTimeRange(getTimeRange(compFilterNode));

			NodeList compFilters = compFilterNode
					.getElementsByTagNameNS(Filter.NAMESPACE,"comp-filter");
			for (int i = 0; i < compFilters.getLength(); i++) {
				Element cf = (Element) compFilters.item(i);
				compFilter.addCompFilter(parseCompFilter(cf));
			}

			NodeList propFilters = compFilterNode
					.getElementsByTagNameNS(Filter.NAMESPACE,"prop-filter");
			for (int i = 0; i < propFilters.getLength(); i++) {
				Element pf = (Element) propFilters.item(i);
				compFilter.addPropFilter(parsePropFilter(pf));
			}
		}
		return compFilter;
	}

	private static PropFilter parsePropFilter(Element propFilterNode) {
		PropFilter propFilter = new PropFilter();
		propFilter.setName(propFilterNode.getAttribute("name"));
		propFilter.setNotDefined(getIsNotDefined(propFilterNode));
		propFilter.setTimeRange(getTimeRange(propFilterNode));
		propFilter.setTextMatch(getTextMatch(propFilterNode));

		NodeList paramFilters = propFilterNode.getElementsByTagNameNS(Filter.NAMESPACE, "param-filter");
		
		for (int i = 0; i < paramFilters.getLength(); i++) {
			Element pf = (Element) paramFilters.item(i);
			propFilter.addParamFilter(parseParamFilter(pf));
		}

		return propFilter;
	}

	private static ParamFilter parseParamFilter(Element paramFilterNode) {
		ParamFilter pf = new ParamFilter();
		pf.setName(paramFilterNode.getAttribute("name"));
		pf.setNotDefined(getIsNotDefined(paramFilterNode));
		pf.setTextMatch(getTextMatch(paramFilterNode));
		return pf;
	}

	private static TextMatch getTextMatch(Element elem) {
		Element textMatchNode = DOMUtils.getUniqueElement(elem,
				"text-match",Filter.NAMESPACE);
		TextMatch tm = null;
		if (textMatchNode != null) {
			tm = new TextMatch();
			tm.setCollation(textMatchNode.getAttribute("collation"));
			String negateCondition = textMatchNode
					.getAttribute("negate-condition");
			if ("yes".equalsIgnoreCase(negateCondition)) {
				tm.setNegateCondition(true);
			} else {
				tm.setNegateCondition(false);
			}

			tm.setText(textMatchNode.getTextContent());
		}
		return tm;
	}

	private static boolean getIsNotDefined(Element elem) {
		Element isNotDefined = DOMUtils
				.getUniqueElement(elem, "is-not-defined",Filter.NAMESPACE);
		if (isNotDefined == null) {
			return false;
		} else {
			return true;
		}
	}

	private static TimeRange getTimeRange(Element elem) {

		TimeRange tr = new TimeRange();
		Element timeRange = DOMUtils.getUniqueElement(elem, "time-range",Filter.NAMESPACE);
		if (timeRange != null) {
			DateFormat df = new SimpleDateFormat(patternUTC);
			df.setTimeZone(TimeZone.getTimeZone("UTC"));

			String start = timeRange.getAttribute("start");
			if (start != null && "".equals(start)) {
				try {
					Date dStart = df.parse(start);
					tr.setStart(dStart);
				} catch (ParseException e) {
					logger.error(e.getMessage(), e);
				}
			}

			String end = timeRange.getAttribute("end");
			if (end != null && "".equals(start)) {
				try {
					Date dEnd = df.parse(end);
					tr.setEnd(dEnd);
				} catch (ParseException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		return tr;
	}

}
