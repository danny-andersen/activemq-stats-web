package org.dsa.amq.amqstats.domain;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dsa.amq.amqstats.service.UriEnrichmentFactoryService;
import org.xml.sax.InputSource;

public class Route {
	private static final Log log = LogFactory.getLog(Route.class);
	private static final String ROUTE_ATTR = "routeXml";
	private static final String TO_START = "<to uri=\"";
	private static final String TO_END = "\"";
	private static final String SEND_TO = "sendTo(Endpoint[";
	private static final String SEND_TO_END = "]";
	private static final Pattern SIMPLE_EXT_PATTERN = Pattern.compile("when Simple: .*file.*ext.* \"");
	private static final String SIMPLE_END = "\":";

	public static final String DEST_URI = "uri-dest";

	public static final String ROUTE_ID = "RouteId";
	public static final String URI = "EndpointUri";
	public static final String DESC = "Description";
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
		if (attrs != null) {
			addAttrs(attrs);
		}
	}
	
	public Route(UriEnrichmentFactoryService uriFactory) {
		this.uriFactory = uriFactory;
	}
	
	public NameValueAttr[] getAttrs() {
		return this.getAttrs(true);
	}

	public NameValueAttr[] getAttrs(boolean sorted) {
		Map<String, NameValueAttr> nvMap = new HashMap<String, NameValueAttr>();
		Set<NameValueAttr> dupeValues = new HashSet<NameValueAttr>();
		for (NameValueAttr nv : this.routeAttrs) {
			//Combine filters of same destinations
			if (nvMap.containsKey(nv.value)) {
				if (nv.name.startsWith(DEST_URI)) {
					//Combine filter
					NameValueAttr exist = nvMap.get(nv.value);
					if (exist.filter == null) {
						exist.filter = nv.filter;
					} else if (nv.filter != null) {
						exist.filter += "," + nv.filter;
					}
				} else {
					dupeValues.add(nv);
				}
			} else {
				nvMap.put(nv.value, nv);
			}
		}
		if (!sorted) {
			NameValueAttr[] attributes = new NameValueAttr[nvMap.size() + dupeValues.size()];
			int i = 0;
			for (NameValueAttr a : nvMap.values()) {
				if (a.filter == null) {
					attributes[i++] = a;
				} else {
					attributes[i++] = new NameValueAttr(a.name, a.filter + ";" + a.value);
				}
			}
			for (NameValueAttr a : dupeValues) {
				attributes[i++] = a;
			}
			return attributes;
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

			for (NameValueAttr attr : nvMap.values()) {
				if (attr != null) {
					sortedNames.add(attr);
				}
			}
			for (NameValueAttr attr : dupeValues) {
				if (attr != null) {
					sortedNames.add(attr);
				}
			}
			NameValueAttr[] attributes = new NameValueAttr[sortedNames.size()];
			int i = 0;
			for (NameValueAttr a : sortedNames) {
				if (a.filter == null) {
					attributes[i++] = a;
				} else {
					attributes[i++] = new NameValueAttr(a.name, a.filter + ":" + a.value);
				}
			}
			return attributes;
		}
	}

	public void addAttrs(AttributeList attrs) {
		this.addAttrs(attrs, true);
	}

	public void addAttrs(AttributeList attrs, boolean withBackLog) {
		for (Attribute attr : attrs.asList()) {
			String val = attr.getValue() != null ? attr.getValue().toString()
					: null;
			log.trace(String.format("Adding attr: %s=%s", attr.getName(), val));
			if (attr.getName().compareToIgnoreCase(Route.ROUTE_ID) == 0) {
				this.setId(val);
			} else if (attr.getName().compareToIgnoreCase(Route.URI) == 0) {
				if (val != null) {
					this.uri = uriFactory.getUriEnrichment(val);
					for (NameValueAttr nv : this.uri.getAttrs(withBackLog)) {
						if (nv != null) {
							this.addAttribute(nv);
						}
					}
				}
			} else if (attr.getName().compareToIgnoreCase(Route.DESC) == 0) {
				parseDescription(val);
			} else {
				this.addAttribute(new NameValueAttr(attr.getName(), val));
			}
		}
	}

	public void addAttribute(NameValueAttr attr) {
		if (this.routeAttrs == null) {
			this.routeAttrs = new ArrayList<NameValueAttr>();
		}
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
//		int count = 1;
//		int beginIndex = 0;
//		while (beginIndex != -1) {
//			beginIndex = routeXml.indexOf(TO_START, beginIndex);
//			if (beginIndex != -1) {
//				beginIndex += TO_START.length();
//				int endIndex = routeXml.indexOf(TO_END, beginIndex);
//				if (endIndex != -1) {
//					String to = routeXml.substring(beginIndex, endIndex);
//					this.addAttribute(new NameValueAttr(DEST_URI + count++, to));
//				}
//			}
//		}
		this.addAttribute(new NameValueAttr(ROUTE_ATTR, formatXml(routeXml)));
	}
	
	//EventDrivenConsumerRoute[Endpoint[file:///home/danny/ActiveMQ/fromdir] -> Instrumentation:
	//route[DelegateAsync[UnitOfWork(RouteContextProcessor[Channel[choice{
	//when Simple: ${file:ext} == "fred": Channel[sendTo(Endpoint[file:///home/danny/ActiveMQ/filterDir])], when Simple: ${file:ext} == "joe": Channel[sendTo(Endpoint[file:///home/danny/ActiveMQ/filterDir])], when Simple: ${file:ext} == "jane": Channel[sendTo(Endpoint[file:///home/danny/ActiveMQ/filterDir])], when Simple: ${file:onlyname} == ${file:onlyname.noext}: Channel[sendTo(Endpoint[file:///home/danny/ActiveMQ/filterNoExtDir])], otherwise: Channel[sendTo(Endpoint[file:///home/danny/ActiveMQ/todir])]}]])]]]

	private void parseDescription(String desc) {
		int count = 1;
		int sendToEnd = 0;
		int sendToStart = desc.indexOf(SEND_TO);
		Matcher m = SIMPLE_EXT_PATTERN.matcher(desc);
		while (sendToStart != -1) {
			String filter = null;
			int filterEnd = desc.indexOf(SIMPLE_END, sendToEnd);
			if (filterEnd != -1)  {
				m.region(sendToEnd, filterEnd);
				if (m.find()) {
					int filterStart = m.end();
					if (filterStart < sendToStart) {
							filter = desc.substring(filterStart, filterEnd);
					}	
					log.trace("Got a when clause associated with a file ext: " + filter);
				}
			}
			sendToStart += SEND_TO.length();
			sendToEnd = desc.indexOf(SEND_TO_END, sendToStart);
			if (sendToEnd != -1) {
				String to = desc.substring(sendToStart, sendToEnd);
				int params = to.indexOf("?");
				if (params != -1) {
					to = to.substring(0, params);
				}
				this.addAttribute(new NameValueAttr(DEST_URI + count++, to, filter));
			}
			sendToStart = desc.indexOf(SEND_TO, sendToEnd);
		}
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
