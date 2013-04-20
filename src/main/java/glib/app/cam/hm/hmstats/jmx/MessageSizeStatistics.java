package glib.app.cam.hm.hmstats.jmx;

public class MessageSizeStatistics implements MessageSizeStatisticsMBean {
	public static final String MESSAGESTATS_MBEAN = "glib.app.cam.hm.hmstats.camel.jmx:type=MessageSizeStatistics,name=";

	private long numMessages;
	private long maxMessageSize;
	private long minMessageSize;
	private double totalMessageSize;
	
	public MessageSizeStatistics() {
		reset();
	}

	@Override
	public synchronized void addSize(long size) {
		if (size > this.maxMessageSize) {
			this.maxMessageSize = size;
		}
		if (size < this.minMessageSize) {
			this.minMessageSize = size;
		}
		totalMessageSize += size;
		numMessages++;
	}
	
	@Override
	public void reset() {
		this.numMessages = 0;
		this.maxMessageSize = Long.MIN_VALUE;
		this.minMessageSize = Long.MAX_VALUE;
		this.totalMessageSize = 0.0;
	}

	@Override
	public long getNumMessages() {
		return this.numMessages;
	}

	@Override
	public double getMaxMessageSizeKb() {
		if (this.maxMessageSize == Long.MIN_VALUE) {
			return 0;
		} else {
			double size = this.maxMessageSize / 1024.0;
			size = Math.round(size*10000.0) / 10000.0;
			return size;
		}
	}

	@Override
	public double getMinMessageSizeKb() {
		if (this.minMessageSize == Long.MAX_VALUE) {
			return 0;
		} else {
			double size = this.minMessageSize / 1024.0;
			size = Math.round(size*10000.0) / 10000.0;
			return size;
		}
	}

	@Override
	public double getAvgMessageSizeKb() {
		if (this.numMessages > 0) {
			double size = this.totalMessageSize / (1024 * this.numMessages);
			size = Math.round(size*10000.0) / 10000.0;
			return size;
		} else {
			return 0;
		}
	}

}
