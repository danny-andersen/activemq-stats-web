package glib.app.cam.hm.hmstats.service;

import glib.app.cam.hm.hmstats.domain.Route;

import javax.management.AttributeList;

import org.springframework.beans.factory.annotation.Autowired;

public class RouteFactoryService {
	
	@Autowired
	private UriEnrichmentFactoryService uriFactory;
	
	public Route getRoute(AttributeList attrs) {
		return new Route(uriFactory, attrs);
	}
}
