package org.dsa.amq.amqstats.domain;

import java.util.ArrayList;
import java.util.List;

import javax.management.Attribute;
import javax.management.AttributeList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Queue {
	private static final Log log = LogFactory.getLog(Queue.class);

	public static final String NAME = "Name";
	public static final String QUEUE_SIZE = "QueueSize";
	public static final String ENQUEUE = "EnqueueCount";
	public static final String DEQUEUE = "DequeueCount";
	public static final String EXPIRED = "ExpiredCount";
	public static final String MAX_ENQUEUE_TIME = "MaxEnqueueTime";
	public static final String AVG_ENQUEUE_TIME = "AverageEnqueueTime";
	public static final String CONSUMER = "ConsumerCount";

	private String name;
	private String backLog;
	private String enqueued;
	private String dequeued;
	private String expired;
	private String consumers;
	private String maxEnqueueTime;
	private String avgEnqueueTime;
	
	private NameValueAttr[] attributes = null;

	public Queue() {

	}

	public Queue(AttributeList attrs, boolean summary) {
		List<NameValueAttr> nva = new ArrayList<NameValueAttr>();
		for (Attribute attr : attrs.asList()) {
			String val = attr.getValue() != null ? attr.getValue().toString()
					: null;
			log.trace(String.format("Adding attr: %s=%s", attr.getName(), val));
			if (attr.getName().compareToIgnoreCase(Queue.NAME) == 0) {
				this.setName(val);
			} else if (attr.getName().compareTo(Queue.AVG_ENQUEUE_TIME) == 0) {
				setAvgEnqueueTime(val);
			} else if (attr.getName().compareTo(Queue.CONSUMER) == 0) {
				setConsumers(val);
			} else if (attr.getName().compareTo(Queue.DEQUEUE) == 0) {
				setDequeued(val);
			} else if (attr.getName().compareTo(Queue.ENQUEUE) == 0) {
				setEnqueued(val);
			} else if (attr.getName().compareTo(Queue.EXPIRED) == 0) {
				setExpired(val);
			} else if (attr.getName().compareTo(Queue.MAX_ENQUEUE_TIME) == 0) {
				setMaxEnqueueTime(val);
			} else if (attr.getName().compareTo(Queue.QUEUE_SIZE) == 0) {
				setBackLog(val);
			} else if (!summary) {
				//Only add non first class attrs if not a summary queue
				nva.add(new NameValueAttr(attr.getName(), attr.getValue().toString()));
			}
		}
		if (!summary) {
			this.attributes = nva.toArray(new NameValueAttr[nva.size()]);
		} 
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getBackLog() {
		return backLog;
	}

	public void setBackLog(String backLog) {
		this.backLog = backLog;
	}

	public String getEnqueued() {
		return enqueued;
	}

	public void setEnqueued(String enqueued) {
		this.enqueued = enqueued;
	}

	public String getDequeued() {
		return dequeued;
	}

	public void setDequeued(String dequeued) {
		this.dequeued = dequeued;
	}

	public String getExpired() {
		return expired;
	}

	public void setExpired(String expired) {
		this.expired = expired;
	}

	public String getConsumers() {
		return consumers;
	}

	public void setConsumers(String consumers) {
		this.consumers = consumers;
	}

	public String getMaxEnqueueTime() {
		return maxEnqueueTime;
	}

	public void setMaxEnqueueTime(String maxEnqueueTime) {
		this.maxEnqueueTime = maxEnqueueTime;
	}

	public String getAvgEnqueueTime() {
		return avgEnqueueTime;
	}

	public void setAvgEnqueueTime(String avgEnqueueTime) {
		this.avgEnqueueTime = avgEnqueueTime;
	}

	public NameValueAttr[] getAttributes() {
		return attributes;
	}

	public void setAttributes(NameValueAttr[] attributes) {
		this.attributes = attributes;
	}

}
