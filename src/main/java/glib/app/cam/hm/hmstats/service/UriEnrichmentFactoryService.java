package glib.app.cam.hm.hmstats.service;

import glib.app.cam.hm.hmstats.domain.UriEnrichment;

import org.springframework.beans.factory.annotation.Autowired;

public class UriEnrichmentFactoryService {
	
	@Autowired
	private QueueService queueService;
	
	public UriEnrichment getUriEnrichment(String uri) {
		return new UriEnrichment(queueService, uri);
	}

}
