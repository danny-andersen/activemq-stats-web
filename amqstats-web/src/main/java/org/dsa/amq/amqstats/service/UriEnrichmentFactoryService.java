package org.dsa.amq.amqstats.service;


import org.dsa.amq.amqstats.domain.UriEnrichment;
import org.springframework.beans.factory.annotation.Autowired;

public class UriEnrichmentFactoryService {
	
	@Autowired
	private QueueService queueService;
	
	public UriEnrichment getUriEnrichment(String uri) {
		return new UriEnrichment(queueService, uri);
	}

}
