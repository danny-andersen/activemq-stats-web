package org.dsa.amq.amqstats.service;

import java.util.ArrayList;
import java.util.Set;

import javax.management.AttributeList;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dsa.amq.amqstats.domain.Route;
import org.dsa.amq.amqstats.jmx.JmxCamel;
import org.dsa.amq.amqstats.jmx.MessageSizeStatistics;
import org.springframework.beans.factory.annotation.Autowired;

public class RouteService {
	private static final Log log = LogFactory.getLog(RouteService.class);
	private static final String ROUTE_NAME_QUERY = "org.apache.camel:context=*,type=routes,name=\"";
	private static final String ROUTE_DUMP_COMMAND = "dumpRouteAsXml";

	@Autowired
	private JmxCamel jmxCamel;

	@Autowired
	private RouteFactoryService routeFactory;

	public ArrayList<Route> getAllRouteStatus(String filter, boolean detailed, boolean withBackLog) {
		StringBuilder sb = new StringBuilder();
		sb.append(ROUTE_NAME_QUERY);
		if (filter != null && !filter.isEmpty()) {
			sb.append(filter);
			sb.append("\"");
		} else {
			sb.append("*\"");
		}
		Set<ObjectName> routeNames = this.jmxCamel.getBeanNames(sb.toString());
		log.debug("Found routes: " + routeNames.size());
		ArrayList<Route> routes = new ArrayList<Route>(routeNames.size());
		// Retrieve attrs for each route
		for (ObjectName name : routeNames) {
			log.trace("Got route: " + name.getCanonicalName());
			AttributeList attrs = this.jmxCamel.getAttributes(name, null);
			Route route = routeFactory.getRoute();
			route.addAttrs(attrs, withBackLog);
			try {
				attrs = this.jmxCamel.getAttributes(
						new ObjectName(MessageSizeStatistics.MESSAGESTATS_MBEAN
								+ route.getId()), null);
				if (attrs != null) {
					route.addAttrs(attrs);
				}
			} catch (Exception e) {
				log.debug("Failed to get message stats bean for route: "
						+ route.getId(), e);
			}
			if (detailed) {
				// Get route as xml
				String routeXml = (String) this.jmxCamel.invokeCommand(name,
						ROUTE_DUMP_COMMAND);
				route.setRouteXml(routeXml);
			}
			routes.add(route);
		}
		return routes;
	}

	public Route getDetailedRouteStatus(String id) {
		ArrayList<Route> routes = getAllRouteStatus(id, true, true);
		log.debug(String.format("Looked for route: %s and got %d routes", id,
				routes.size()));
		return routes.get(0);
	}

	public Object controlRoute(String id, String command) {
		StringBuilder sb = new StringBuilder();
		Object retObject = null;
		sb.append(ROUTE_NAME_QUERY);
		if (id != null && !id.isEmpty()) {
			sb.append(id);
			sb.append("\"");
		} else {
			sb.append("*\"");
		}
		String queryStr = sb.toString();
		log.debug(String.format("Invoking command %s on route %s", command,
				queryStr));
		this.jmxCamel.invokeCommand(queryStr, command);
		if (command.compareToIgnoreCase("reset") == 0) {
			// Reset message size stats
			sb = new StringBuilder();
			sb.append(MessageSizeStatistics.MESSAGESTATS_MBEAN);
			if (id != null) {
				sb.append(id);
			}
			sb.append("*");
			queryStr = sb.toString();
			retObject = this.jmxCamel.invokeCommand(queryStr, command);
		}
		return retObject;
	}
}
