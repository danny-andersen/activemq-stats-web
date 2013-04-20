package glib.app.cam.hm.hmstats.jmx;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.management.AttributeList;
import javax.management.MBeanAttributeInfo;
import javax.management.MBeanInfo;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class JmxCamel {
	private static final Log log = LogFactory.getLog(JmxCamel.class);

	private MBeanServer mbeanServer = null;
	
	public JmxCamel() {
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
	
	public Set<ObjectName> getBeanNames(String searchStr) {
		Set<ObjectName> names;
		if (this.mbeanServer == null) {
			log.warn("Cannot find any mBeans - no mBeanserver found in JVM");
			return null;
		}
		try {
			log.debug("Querying mbean server: " + searchStr);
			names = this.mbeanServer.queryNames(new ObjectName(searchStr), null);
		} catch (MalformedObjectNameException e) {
			log.error("Invalid query string: " + searchStr, e);
			return null;
		}
		SortedSet<ObjectName> sortedNames = new TreeSet<ObjectName>(new Comparator<ObjectName>() {

			@Override
			public int compare(ObjectName o1, ObjectName o2) {
				return o1.getCanonicalName().compareToIgnoreCase(o2.getCanonicalName());
			}
			
		});
		sortedNames.addAll(names);
		return sortedNames;
	}
	
	public AttributeList getAttributes(ObjectName name, String[] attributeNames) {
		AttributeList attrs = null;
		if (attributeNames == null) {
			//Find all mbean attrs
			MBeanInfo info = null;
			try {
				info = this.mbeanServer.getMBeanInfo(name);
				MBeanAttributeInfo[] attrInfos = info.getAttributes();
				attributeNames = new String[attrInfos.length];
				int i = 0;
				Pattern typePattern = Pattern.compile("\\[L.*|.*javax.*");
				for (MBeanAttributeInfo attrInfo : attrInfos) {
					Matcher m = typePattern.matcher(attrInfo.getType());
					if (!m.matches()) {
						//Ignore attrs that are ObjectNames
						log.trace("Got attribute of type: " + attrInfo.getType());
						attributeNames[i++] = attrInfo.getName();
					} else {
						log.trace("Ignoring type of: " + attrInfo.getType());
					}
				}
			} catch (Exception e) {
				log.warn("Failed to retrieve MBeanInfo for bean: " + name.getCanonicalName());
				return null;
			}
		}
		SortedSet<String> sortedNames = new TreeSet<String>();
		for (String a: attributeNames) {
			if (a != null && !a.isEmpty()) {
				sortedNames.add(a);
			}
		}
		attributeNames = new String[sortedNames.size()];
		int i = 0;
		for (String a : sortedNames) {
			attributeNames[i++] = a;
		}
		try {
			attrs = this.mbeanServer.getAttributes(name, attributeNames);
		} catch (Exception e) {
			log.error("Could not retreive attributes of mbean: " + name, e);
			return null;
		}
		if (attrs != null) {
			log.trace("Retreived attrs: " + attrs.size());
		}		
		return attrs;
	}
	
	public void invokeCommand(String searchStr, String command) {
		invokeCommand(searchStr, command, null, null);
	}
	
	public void invokeCommand(String searchStr, String command, Object[] params, String[] signature) {
		Set<ObjectName> names = getBeanNames(searchStr);
		if (names != null) {
			for (ObjectName name : names) {
				try {
					log.debug(String.format("Invoking command %s on bean %s: ",command,name.getCanonicalName()));
					this.mbeanServer.invoke(name, command, params, signature);
				} catch (Exception e) {
					log.error(String.format("Failed to invoke command %s on bean %s: ",command,name.getCanonicalName()), e);
				}
			}
		} else {
			log.warn("Failed to find any matching beans to control with search str: " + searchStr);
		}
	}
	
	public void registerMBean(Object object, ObjectName name) {
		try {
			this.mbeanServer.registerMBean(object, name);
		} catch (Exception e) {
			log.error("Failed to register mbean: " + name);
		}
	}
	
}
