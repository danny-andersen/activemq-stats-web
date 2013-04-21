package org.dsa.amq.amqstats.camel;

import java.util.ArrayList;
import java.util.Set;

import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class PushBrokerNameInHeader implements Processor {
	private static final Log log = LogFactory.getLog(PushBrokerNameInHeader.class);
	private static final String BROKER_NAME_QUERY = "org.apache.activemq:BrokerName=*,Type=Broker";
	private static final String BROKER_NAME = "BrokerName";
	private static final String[] BROKER_ATTRS = new String[] { BROKER_NAME };

	MBeanServer mbeanServer = null;
	String brokerName = null;
	
	public PushBrokerNameInHeader() {
	}

	@Override
	public void process(Exchange exchange) throws Exception {
		//Get broker name
		String name = getBrokerName();
		exchange.getIn().setHeader("brokerName", name);
	}

	private String getBrokerName() {
		if (this.brokerName == null) {
			//Lookup queue names
			StringBuilder sb = new StringBuilder(BROKER_NAME_QUERY);
			Set<ObjectName> brokerNames = getBeanNames(sb.toString());
			if (brokerNames == null || brokerNames.size() < 1) {
				log.warn("Failed to retrieve broker bean using query: " + BROKER_NAME_QUERY);
				return null;
			} else if (brokerNames.size() > 1) {
				log.warn("Retrieved more than one broker bean?? - using first one: " + brokerNames.size());
			}
			AttributeList attrs = getAttributes(brokerNames.iterator().next(), BROKER_ATTRS);
			for (Attribute attr : attrs.asList()) {
				if (attr.getName().compareTo(BROKER_NAME) == 0) {
					String brokerName = attr.getValue().toString(); 
					log.debug("Retrieved brokerName of: " + brokerName);
				}
			}
		}
		return this.brokerName;
	}
	
	private MBeanServer getMBeanServer() {
		if (this.mbeanServer == null) {
			//Connect to MBeanserver
			ArrayList<MBeanServer> mbeanServers = MBeanServerFactory.findMBeanServer(null);
			if (mbeanServers == null || mbeanServers.size() == 0) {
				log.warn("Failed to find any mbeanServers in JVM");
			} else {
				if (mbeanServers.size() > 1) {
					log.warn("There is more than one mBean server in the JVM: " + mbeanServers.size());
				}
				this.mbeanServer = mbeanServers.get(0);
				log.info(String.format("Using mbeanServer: %s with %d domains and %d mbeans",this.mbeanServer.toString(),this.mbeanServer.getDomains().length,this.mbeanServer.getMBeanCount()));
				if (log.isDebugEnabled()) {
					String[] domains = this.mbeanServer.getDomains();
					for (String domain : domains) {
						log.debug("Found domain: " + domain);
					}
				}
			}
		}
		return this.mbeanServer;

	}

	private Set<ObjectName> getBeanNames(String searchStr) {
		Set<ObjectName> names;
		if (this.mbeanServer == null) {
			log.warn("Cannot find any mBeans - no mBeanserver found in JVM");
			return null;
		}
		try {
			log.debug("Querying mbean server: " + searchStr);
			names = getMBeanServer().queryNames(new ObjectName(searchStr), null);
		} catch (MalformedObjectNameException e) {
			log.error("Invalid query string: " + searchStr, e);
			return null;
		}
		return names;
	}
	
	private AttributeList getAttributes(ObjectName name, String[] attributeNames) {
		AttributeList attrs = null;
		try {
			attrs = getMBeanServer().getAttributes(name, attributeNames);
		} catch (Exception e) {
			log.error("Could not retreive attributes of mbean: " + name, e);
			return null;
		}
		if (attrs != null) {
			log.debug("Retreived attrs: " + attrs.size());
		}		
		return attrs;
	}

}
