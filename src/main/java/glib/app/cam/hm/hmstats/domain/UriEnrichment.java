package glib.app.cam.hm.hmstats.domain;

import glib.app.cam.hm.hmstats.service.QueueService;

import java.io.File;
import java.io.FileFilter;
import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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
		return new NameValueAttr[] { 
				new NameValueAttr(Route.SHORT_URI, getUri()), 
				new NameValueAttr(Route.URI, getFullUri()), 
				new NameValueAttr(Route.BACKLOG, getBackLogAsString()), 
				};
	}

	public int getBackLog() {
		return backLog;
	}
	
	public String getBackLogAsString() {
		return String.valueOf(this.backLog);		
	}

	public void setBackLog(int backLog) {
		this.backLog = backLog;
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
						log.warn("Source directory specified in route does not exist: " + file.getAbsolutePath());
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
	}

	public String getFullUri() {
		return fullUri;
	}

	public void setFullUri(String fullUri) {
		this.fullUri = fullUri;
	}
	
}
