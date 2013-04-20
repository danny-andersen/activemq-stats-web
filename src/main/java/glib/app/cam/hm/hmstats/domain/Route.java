package glib.app.cam.hm.hmstats.domain;

import glib.app.cam.hm.hmstats.service.UriEnrichmentFactoryService;

import java.util.ArrayList;
import java.util.List;

import javax.management.Attribute;
import javax.management.AttributeList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Route {
	private static final Log log = LogFactory.getLog(Route.class);

	public static final String ROUTE_ID = "RouteId";
	public static final String URI = "EndpointUri";
	public static final String SUCCESS = "ExchangesCompleted";
	public static final String TOTAL = "ExchangesTotal";
	public static final String FAILED = "ExchangesFailed";
//	public static final String INFLIGHT = "InflightExchanges";
//	public static final String FIRST_TIME = "FirstExchangeCompletedTimestamp";
	public static final String LAST_TIME = "LastExchangeCompletedTimestamp";
	public static final String LAST_FAIL = "LastExchangeFailureTimestamp";
//	public static final String MAX_PROC = "MaxProcessingTime";
//	public static final String MIN_PROC = "MinProcessingTime";
	public static final String MEAN_PROC = "MeanProcessingTime";
	public static final String AVG_SIZE = "AvgMessageSizeKb";
	public static final String STATE = "State";
	public static final String STOP = "stop";
	public static final String START = "start";
	public static final String RESET = "reset";
	//Enriched set
	public static final String SHORT_URI = "uri";
	public static final String BACKLOG = "backLog";

	private String id;
	private NameValueAttr[] attrs;
	private UriEnrichment uri;
	
	private UriEnrichmentFactoryService uriFactory;
	
	public Route(UriEnrichmentFactoryService uriFactory, AttributeList attrs) {
		this.uriFactory = uriFactory;
		addAttrs(attrs);
	}
	
	public NameValueAttr[] getAttrs() {
		return attrs;
	}

	public void setAttrs(NameValueAttr[] attrs) {
		this.attrs = attrs;
	}
	
	public void addAttrs(AttributeList attrs) {
		List<NameValueAttr> routeAttrs = new ArrayList<NameValueAttr>();
		if (this.attrs != null) {
			//Add in current attrs
			for (NameValueAttr attr : this.attrs) {
				routeAttrs.add(attr);
			}
		}
		for (Attribute attr : attrs.asList()) {
			String val = attr.getValue() != null ? attr.getValue().toString() : null;
			log.trace(String.format("Adding attr: %s=%s",attr.getName(), val));
			if (attr.getName().compareToIgnoreCase(Route.ROUTE_ID) == 0) {
				this.setId(val);
			} else if (attr.getName().compareToIgnoreCase(Route.URI) == 0) {
				this.uri = uriFactory.getUriEnrichment(val);
				for (NameValueAttr nv : this.uri.getAttrs()){
					routeAttrs.add(nv);
				}
			} else {
				routeAttrs.add(new NameValueAttr(attr.getName(), val));
			}
		}
		this.setAttrs(routeAttrs.toArray(new NameValueAttr[routeAttrs.size()]));
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

}
