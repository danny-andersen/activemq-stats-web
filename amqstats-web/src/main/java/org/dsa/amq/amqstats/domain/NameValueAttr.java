package org.dsa.amq.amqstats.domain;

public class NameValueAttr {
	public String name;
	public String value;
	public String filter;
	
	public NameValueAttr(String name, String value) {
		this.name = name;
		this.value = value;
	}

	public NameValueAttr(String name, String value, String filter) {
		this.name = name;
		this.value = value;
		this.filter = filter;
	}
}
