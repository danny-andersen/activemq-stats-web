package org.dsa.amq.amqstats.jmx;

public interface MessageSizeStatisticsMBean {
	public void addSize(long size);
	public void reset();
	
	public long getNumMessages();
	public double getMaxMessageSizeKb();
	public double getMinMessageSizeKb();
	public double getAvgMessageSizeKb();
}
