package org.dsa.amq.amqstats.domain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dsa.amq.amqstats.service.UriEnrichmentFactoryService;
import org.xml.sax.InputSource;

public class Route {
	private static final Log log = LogFactory.getLog(Route.class);
	private static final String ROUTE_ATTR = "routeXml";
	private static final String TO_START = "<to uri=\"";
	private static final String TO_END = "\"";
	private static final String DEST_URI = "uri-dest";

	public static final String ROUTE_ID = "RouteId";
	public static final String URI = "EndpointUri";
	public static final String SUCCESS = "ExchangesCompleted";
	public static final String TOTAL = "ExchangesTotal";
	public static final String FAILED = "ExchangesFailed";
	// public static final String INFLIGHT = "InflightExchanges";
	// public static final String FIRST_TIME =
	// "FirstExchangeCompletedTimestamp";
	public static final String LAST_TIME = "LastExchangeCompletedTimestamp";
	public static final String LAST_FAIL = "LastExchangeFailureTimestamp";
	// public static final String MAX_PROC = "MaxProcessingTime";
	// public static final String MIN_PROC = "MinProcessingTime";
	public static final String MEAN_PROC = "MeanProcessingTime";
	public static final String AVG_SIZE = "AvgMessageSizeKb";
	public static final String STATE = "State";
	public static final String STOP = "stop";
	public static final String START = "start";
	public static final String RESET = "reset";
	// Enriched set
	public static final String SHORT_URI = "uri-source";
	public static final String BACKLOG = "backLog";

	private String id;
	List<NameValueAttr> routeAttrs;
	private UriEnrichment uri;

	private UriEnrichmentFactoryService uriFactory;

	public Route(UriEnrichmentFactoryService uriFactory, AttributeList attrs) {
		this.uriFactory = uriFactory;
		addAttrs(attrs);
	}
	
	public NameValueAttr[] getAttrs() {
		return this.getAttrs(true);
	}

	public NameValueAttr[] getAttrs(boolean sorted) {
		if (!sorted) {
			NameValueAttr[] attrs = routeAttrs
					.toArray(new NameValueAttr[routeAttrs.size()]);
			return attrs;
		} else {
			final Pattern uri = Pattern.compile("uri-.*");
			SortedSet<NameValueAttr> sortedNames = new TreeSet<NameValueAttr>(
					new Comparator<NameValueAttr>() {
						@Override
						public int compare(NameValueAttr o1, NameValueAttr o2) {
							if (o1.name.equals("uri-source")) {
								return -1;
							} else if (o2.name.equals("uri-source")) {
								return 1;
							} else {
								Matcher m1 = uri.matcher(o1.name);
								Matcher m2 = uri.matcher(o2.name);
								if (m1.matches() && !m2.matches()) {
									return -1;
								} else if (!m1.matches() && m2.matches()) {
									return 1;
								} else {
									return o1.name.compareToIgnoreCase(o2.name);
								}
							}
						}
					});

			for (NameValueAttr attr : this.routeAttrs) {
				if (attr != null) {
					sortedNames.add(attr);
				}
			}
			NameValueAttr[] attributes = new NameValueAttr[sortedNames.size()];
			int i = 0;
			for (NameValueAttr a : sortedNames) {
				attributes[i++] = a;
			}
			return attributes;
		}
	}

	public void addAttrs(AttributeList attrs) {
		if (this.routeAttrs == null) {
			this.routeAttrs = new ArrayList<NameValueAttr>();
		}
		for (Attribute attr : attrs.asList()) {
			String val = attr.getValue() != null ? attr.getValue().toString()
					: null;
			log.trace(String.format("Adding attr: %s=%s", attr.getName(), val));
			if (attr.getName().compareToIgnoreCase(Route.ROUTE_ID) == 0) {
				this.setId(val);
			} else if (attr.getName().compareToIgnoreCase(Route.URI) == 0) {
				this.uri = uriFactory.getUriEnrichment(val);
				for (NameValueAttr nv : this.uri.getAttrs()) {
					routeAttrs.add(nv);
				}
			} else {
				routeAttrs.add(new NameValueAttr(attr.getName(), val));
			}
		}
	}

	public void addAttribute(NameValueAttr attr) {
		this.routeAttrs.add(attr);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setRouteXml(String routeXml) {
		// Parse xml to extract <to> endpoints
		int count = 1;
		int beginIndex = 0;
		while (beginIndex != -1) {
			beginIndex = routeXml.indexOf(TO_START, beginIndex);
			if (beginIndex != -1) {
				beginIndex += TO_START.length();
				int endIndex = routeXml.indexOf(TO_END, beginIndex);
				if (endIndex != -1) {
					String to = routeXml.substring(beginIndex, endIndex);
					this.addAttribute(new NameValueAttr(DEST_URI + count++, to));
				}
			}
		}
		this.addAttribute(new NameValueAttr(ROUTE_ATTR, formatXml(routeXml)));
	}

	private String formatXml(String xml) {
		try {
			Transformer serializer = SAXTransformerFactory.newInstance()
					.newTransformer();
			serializer.setOutputProperty(OutputKeys.INDENT, "yes");
			serializer
					.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
			serializer.setOutputProperty(
					"{http://xml.apache.org/xslt}indent-amount", "2");
			// serializer.setOutputProperty("{http://xml.customer.org/xslt}indent-amount",
			// "2");
			Source xmlSource = new SAXSource(new InputSource(
					new ByteArrayInputStream(xml.getBytes())));
			StreamResult res = new StreamResult(new ByteArrayOutputStream());
			serializer.transform(xmlSource, res);
			return new String(
					((ByteArrayOutputStream) res.getOutputStream())
							.toByteArray());
		} catch (Exception e) {
			log.warn("Failed to pretty print route xml: ", e);
			return xml;
		}
	}

}
