package glib.app.cam.hm.hmstats.rest;

import glib.app.cam.hm.hmstats.domain.Broker;
import glib.app.cam.hm.hmstats.domain.Queue;
import glib.app.cam.hm.hmstats.service.QueueService;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Path("/queue")
@Produces(MediaType.APPLICATION_JSON)
public class QueueResource {

	@Autowired
	private QueueService queueService;
	
	@GET
	public Queue[] getQueues(@QueryParam("id") String id) {
		return this.queueService.getQueues(id);
	}

	@POST
	@Path("/control")
	public void controlQueue(@FormParam("id") String id, @FormParam("command") String command) {
		this.queueService.controlQueue(id, command);
	}
	
	@GET
	@Path("/broker")
	public Broker getBrokerName() {
		return this.queueService.getBrokerName();
	}

	@GET
	@Path("/broker/attr")
	public Broker getBrokerAttrs() {
		return this.queueService.getBrokerAttrs();
	}
}
