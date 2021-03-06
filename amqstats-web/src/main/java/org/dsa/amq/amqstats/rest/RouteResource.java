package org.dsa.amq.amqstats.rest;

import java.util.ArrayList;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dsa.amq.amqstats.domain.Route;
import org.dsa.amq.amqstats.domain.RouteSummary;
import org.dsa.amq.amqstats.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/route")
@Produces(MediaType.APPLICATION_JSON)
public class RouteResource {
	private static final Log log = LogFactory.getLog(RouteResource.class);

	@Autowired
	private RouteService routeService;

	@GET
	public Route getRoute(@QueryParam("id") String id) {
		Route route = this.routeService.getDetailedRouteStatus(id);
		log.debug("Got route: " + route.getId());
		return route;
	}

	@GET
	@Path("/summary")
	public RouteSummary[] getAllRoutesStatus(			
			@QueryParam("filter") String filter,
			@DefaultValue("true") @QueryParam("backlog") boolean withBackLog
			) {
		ArrayList<Route> routes = this.routeService.getAllRouteStatus(filter, false, withBackLog);
		if (routes != null) {
			log.debug("Got routes: " + routes.size());
			RouteSummary[] summaries = new RouteSummary[routes.size()];
			int i = 0;
			for (Route route : routes) {
				RouteSummary summary = new RouteSummary(route);
				summaries[i++] = summary; 
			}
			return summaries;
		} else {
			return null;
		}
	}

	@POST
	@Path("/control")
	public void routeControl(@FormParam("id") String id, @FormParam("command") String command) {
		this.routeService.controlRoute(id, command);
	}
}
