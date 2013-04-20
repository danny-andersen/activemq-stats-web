package glib.app.cam.hm.hmstats.service;

import glib.app.cam.hm.hmstats.domain.Broker;
import glib.app.cam.hm.hmstats.domain.NameValueAttr;
import glib.app.cam.hm.hmstats.domain.Queue;
import glib.app.cam.hm.hmstats.jmx.JmxCamel;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;

public class QueueService {
	private static final Log log = LogFactory.getLog(QueueService.class);
	private static final String QUEUE_QUERY = "org.apache.activemq:BrokerName=*,Type=Queue,Destination=";
//	private static final String[] QUEUE_ATTRS = new String[] {Queue.NAME, 
//										Queue.QUEUE_SIZE, 
//										Queue.ENQUEUE, 
//										Queue.DEQUEUE, 
//										Queue.EXPIRED, 
//										Queue.CONSUMER, 
//										Queue.MAX_ENQUEUE_TIME, 
//										Queue.AVG_ENQUEUE_TIME };

	private static final String BROKER_NAME_QUERY = "org.apache.activemq:BrokerName=*,Type=Broker";
	private static final String BROKER_NAME = "BrokerName";
	private static final String[] BROKER_ATTRS = new String[] { BROKER_NAME };

	@Autowired
	private JmxCamel jmxCamel;
	
	public Queue[] getQueues(String id) {
		//Lookup queue names
		StringBuilder sb = new StringBuilder(QUEUE_QUERY);
		if (id != null && !id.isEmpty()) {
			sb.append(removePreamble(id));
		} else {
			sb.append("*");
		}
		Set<ObjectName> queueNames = this.jmxCamel.getBeanNames(sb.toString());
		if (queueNames != null && queueNames.size() != 0) {
			List<Queue> queues = new ArrayList<Queue>(queueNames.size());
			//Network optimisation: only add secondary attrs if single q
			boolean summary = queueNames.size() == 1 ? false : true;
			log.debug("Generating a queue summary: " + summary);
			for (ObjectName queueName: queueNames) {
				log.debug("Got queue: " + queueName.getCanonicalName());
				AttributeList attrs = this.jmxCamel.getAttributes(queueName, null);
				queues.add(new Queue(attrs, summary));
			}
			return queues.toArray(new Queue[queues.size()]);
		} else {
			return null;
		}
	}
	
	public void controlQueue(String id, String command) {
		StringBuilder sb = new StringBuilder();
		sb.append(QUEUE_QUERY);
		if (id != null && !id.isEmpty()) {
			sb.append(removePreamble(id));
		} else {
			sb.append("*");
		}
		String queryStr = sb.toString();
		log.debug(String.format("Invoking command %s on queue %s",command,queryStr));
		this.jmxCamel.invokeCommand(queryStr, command);
	}

	private String removePreamble(String id) {
		//Remove any queue directives in the queue name
		return id.substring(id.lastIndexOf(":") + 1, id.length());
	}
	
	public Broker getBrokerName() {
		//Lookup queue names
		StringBuilder sb = new StringBuilder(BROKER_NAME_QUERY);
		Set<ObjectName> brokerNames = this.jmxCamel.getBeanNames(sb.toString());
		if (brokerNames == null || brokerNames.size() < 1) {
			log.warn("Failed to retrieve broker bean using query: " + BROKER_NAME_QUERY);
			return null;
		} else if (brokerNames.size() > 1) {
			log.warn("Retrieved more than one broker bean?? - using first one: " + brokerNames.size());
		}
		AttributeList attrs = this.jmxCamel.getAttributes(brokerNames.iterator().next(), BROKER_ATTRS);
		Broker broker = new Broker();
		for (Attribute attr : attrs.asList()) {
			if (attr.getName().compareTo(BROKER_NAME) == 0) {
				String brokerName = attr.getValue().toString(); 
				log.debug("Retrieved brokerName of: " + brokerName);
				broker.setName(brokerName);
			}
		}
		return broker;
	}

	public Broker getBrokerAttrs() {
		//Lookup queue names
		StringBuilder sb = new StringBuilder(BROKER_NAME_QUERY);
		Set<ObjectName> brokerNames = this.jmxCamel.getBeanNames(sb.toString());
		if (brokerNames == null || brokerNames.size() < 1) {
			log.warn("Failed to retrieve broker bean using query: " + BROKER_NAME_QUERY);
			return null;
		} else if (brokerNames.size() > 1) {
			log.warn("Retrieved more than one broker bean?? - using first one: " + brokerNames.size());
		}
		AttributeList attrs = this.jmxCamel.getAttributes(brokerNames.iterator().next(), null);
		Broker broker = new Broker();
		if (attrs == null) {
			return broker;
		}
		NameValueAttr[] props = new NameValueAttr[attrs.size()];
		int i = 0;
		for (Attribute attr : attrs.asList()) {
			if (attr.getName().compareTo(BROKER_NAME) == 0) {
				String brokerName = attr.getValue().toString(); 
				log.debug("Retrieved brokerName of: " + brokerName);
				broker.setName(brokerName);
			}
			String val = null;
			if (attr.getValue() != null) {
				val = attr.getValue().toString();
			}
			props[i++] = new NameValueAttr(attr.getName(), val);
		}
		broker.setAttrs(props);
		return broker;
	}
}
