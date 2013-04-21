package org.dsa.amq.amqstats.domain;

public class Broker {
	String name;
	NameValueAttr[] attrs;
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public NameValueAttr[] getAttrs() {
		return attrs;
	}

	public void setAttrs(NameValueAttr[] attrs) {
		this.attrs = attrs;
	}
	
	
}
