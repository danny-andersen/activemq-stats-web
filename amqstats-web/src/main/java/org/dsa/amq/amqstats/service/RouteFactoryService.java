package org.dsa.amq.amqstats.service;


import javax.management.AttributeList;

import org.dsa.amq.amqstats.domain.Route;
import org.springframework.beans.factory.annotation.Autowired;

public class RouteFactoryService {
	
	@Autowired
	private UriEnrichmentFactoryService uriFactory;
	
	public Route getRoute(AttributeList attrs) {
		return new Route(uriFactory, attrs);
	}
}
