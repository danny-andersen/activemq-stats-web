package org.dsa.amq.amqstats.domain;


import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dsa.amq.amqstats.service.QueueService;

public class UriEnrichment {
	private static Log log = LogFactory.getLog(UriEnrichment.class);
	private static final String FILE_PREFIX = "file://";
	private int backLog = -1;
	private String fullUri;
	private String uri;
	private QueueService queueService;

	public UriEnrichment(QueueService queueService, String uri) {
		this.queueService = queueService;
		setUri(uri);
	}
	
	public NameValueAttr[] getAttrs() {
		return getAttrs(true);
	}

	public NameValueAttr[] getAttrs(boolean withBacklog) {
		NameValueAttr[] nv = new NameValueAttr[3];
		nv[0] = new NameValueAttr(Route.SHORT_URI, getUri()); 
		nv[1] = new NameValueAttr(Route.URI, getFullUri());
		if (withBacklog) {
			nv[2] = new NameValueAttr(Route.BACKLOG, getBackLogAsString());
		};
		return nv;
	}

	public String getBackLogAsString() {
		return String.valueOf(getBackLog());		
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.fullUri = uri;
		int param = uri.indexOf("?");
		if (param != -1) {
			this.uri = uri.substring(0, param);
		} else {
			this.uri = uri;
		}
	}
	
	private int getBackLog() {
		// If its a file uri then look at how many files are in the source
		// directory
		if (this.uri.startsWith(FILE_PREFIX, 0)) {
			URI uRI = null;
			try {
				uRI = new URI(this.uri);
			} catch (Exception e) {
				log.warn("Failed to create URI from endpoint URI: " + this.uri,	e);
			}
			if (uRI != null) {
				File file = null;
				try {
					file = new File(uRI);
				} catch (Exception e) {
					log.warn("Failed to create File from URI: " + uRI,	e);
				}
				final Pattern p = Pattern.compile(".*.camel.*");
				if (file != null ) {
					if (file.exists()) {
						File[] files = file.listFiles(new FileFilter() {
							public boolean accept(File filename) {
								if (filename.isFile()) {
									Matcher match = p.matcher(filename.getName());
									return !match.matches();
								} else {
									return false;
								}
							}
						});
						this.backLog = files.length;
					} else {
						log.debug("Source directory specified in route does not exist: " + file.getAbsolutePath());
					}
				}
			}
		} else {
			//See if its a queue
			int delimiter = this.uri.lastIndexOf(':') + 1;
			while (this.uri.charAt(delimiter) == '/') {
				delimiter++;
			}
			String queueName = this.uri.substring(delimiter, this.uri.length());
			Queue[] queues = queueService.getQueues(queueName);
			if (queues != null) {
				for (Queue queue : queues) {
					if (this.uri.contains(queue.getName())) {
						try {
							this.backLog = Integer.parseInt(queue.getBackLog());
						} catch (NumberFormatException ne) {
							log.warn("Queue backlog was not an integer: " + queue.getBackLog());
						}
					}
				}
			}
		}
		return this.backLog;
	}

	public String getFullUri() {
		return fullUri;
	}

	public void setFullUri(String fullUri) {
		this.fullUri = fullUri;
	}
	
}
