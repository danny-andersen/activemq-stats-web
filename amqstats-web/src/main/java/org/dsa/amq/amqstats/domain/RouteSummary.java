package org.dsa.amq.amqstats.domain;

public class RouteSummary {
	public static final String ID = "id";
	private String id;
	private String state;
	private String uri;
	private String backLog;
	private String success;
	private String total;
	private String failed;
	private String avgProcTimeMs;
	private String avgMessageSizeKb;
	private String lastTime;
	private String lastFail;

	public RouteSummary() {
		
	}
	
	public RouteSummary(Route route) {
		setId(route.getId());
		NameValueAttr[] attrs = route.getAttrs(false);
		for (NameValueAttr attr : attrs) {
			if (attr == null || attr.name == null) {
				continue;
			}
			if (attr.name.compareTo(Route.STATE) == 0) {
				setState(attr.value);
			} else if (attr.name.compareTo(Route.SHORT_URI) == 0) {
				setUri(attr.value);
			} else if (attr.name.compareTo(Route.BACKLOG) == 0) {
				setBackLog(attr.value);
			} else if (attr.name.compareTo(Route.SUCCESS) == 0) {
				setSuccess(attr.value);
			} else if (attr.name.compareTo(Route.TOTAL) == 0) {
				setTotal(attr.value);
			} else if (attr.name.compareTo(Route.LAST_TIME) == 0) {
				setLastTime(attr.value);
			} else if (attr.name.compareTo(Route.FAILED) == 0) {
				setFailed(attr.value);
			} else if (attr.name.compareTo(Route.LAST_FAIL) == 0) {
				setLastFail(attr.value);
			} else if (attr.name.compareTo(Route.MEAN_PROC) == 0) {
				setAvgProcTimeMs(attr.value);
			} else if (attr.name.compareTo(Route.AVG_SIZE) == 0) {
				setAvgMessageSizeKb(attr.value);
			}
		}
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	public String getUri() {
		return uri;
	}
	public void setUri(String endPointUri) {
		this.uri = endPointUri;
	}
	public String getLastTime() {
		return lastTime;
	}
	public void setLastTime(String lastTime) {
		this.lastTime = lastTime;
	}
	public String getSuccess() {
		return success;
	}
	public void setSuccess(String success) {
		this.success = success;
	}
	public String getTotal() {
		return total;
	}
	public void setTotal(String total) {
		this.total = total;
	}
	public String getFailed() {
		return failed;
	}
	public void setFailed(String failed) {
		this.failed = failed;
	}

	public String getLastFail() {
		return lastFail;
	}

	public void setLastFail(String lastFail) {
		this.lastFail = lastFail;
	}
	public String getBackLog() {
		return backLog;
	}

	public void setBackLog(String backLog) {
		this.backLog = backLog;
	}

	public String getAvgProcTimeMs() {
		return avgProcTimeMs;
	}

	public void setAvgProcTimeMs(String avgProcessingTime) {
		this.avgProcTimeMs = avgProcessingTime;
	}

	public String getAvgMessageSizeKb() {
		return avgMessageSizeKb;
	}

	public void setAvgMessageSizeKb(String avgMessageSize) {
		this.avgMessageSizeKb = avgMessageSize;
	}

	
}
