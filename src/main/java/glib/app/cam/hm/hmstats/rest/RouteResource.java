package glib.app.cam.hm.hmstats.rest;

import java.util.ArrayList;

import glib.app.cam.hm.hmstats.domain.Route;
import glib.app.cam.hm.hmstats.domain.RouteSummary;
import glib.app.cam.hm.hmstats.service.RouteService;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
			@QueryParam("filter") String filter
			) {
		ArrayList<Route> routes = this.routeService.getAllRouteStatus(filter);
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
