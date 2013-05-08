function showQueueProperties(id, tableId) {
		//Get the route properties
	$.getJSON('rest/queue?id=' + id, function(queue, status, xhr) {
			var tableSelector = '#' + tableId;
			var body = $('<tbody>');
			// var row = $('<tr>');
			// row.append($('<td>', { html: 'id'}));
			// row.append($('<td>', { html: queue.name}));
			// body.append(row);
			if (queue == null) {
				return;
			}
			var q = queue[0]
			for(prop in q) {
				if (prop != undefined && prop != null) {
					if (prop != 'attributes') {
						var row = $('<tr>');
						row.append($('<td>', { html: prop}));
						row.append($('<td>', { html: q[prop]}));
						body.append(row);
					} else {
						//These are just name value pairs
						q[prop].forEach(function(nv) {
							if (nv != undefined && nv != null) {
								row = $('<tr>');
								row.append($('<td>', { html: nv.name}));
								row.append($('<td>', { html: nv.value}));
								body.append(row);
							}
						});
					}
				}
			}
			//Remove old body
			$(tableSelector + ' > tbody').remove();
			//Add body to table
			$(tableSelector).append(body);
			$('#refreshBtn').css({ cursor: 'pointer'});
			$('body').css({ cursor: 'auto'});
	});
};

function controlQueue(id, command) {
	var params = {id: id, command: command};
	$.post('rest/queue/control', params, function(result, status, xhr) {
		//Refresh current state
		refresh();
	});

}


function getBrokerName() {
	$.getJSON('rest/queue/broker', function(result, status, xhr) {
		hmstats.brokerName = result.name;
		$('#statusTitle').append(hmstats.brokerName + " Status");
		$('#header').append($('<span>', { html: 'Broker: ' })
			.append($('<a>', {
				id: 'broker',
				title: 'Click here to list Broker properties', 
				html: hmstats.brokerName,
				click: function() {
					var tabNo = "tab" + hmstats.tabCount++;
					var tabSelector = addTab(tabNo, 'Broker', "brokerProp");
					//Add table to tab content
					$(tabSelector).append($('#propertiesTemplate').html());
					var tableId = tabNo + "-table";
					$('table', tabSelector).attr('id', tableId);
					showBrokerProperties(tableId);
					//Activate tab
					$('#tabList a[href="' + tabSelector + '"]').tab('show');
					return false;
				}
			})));
	});
};

function showBrokerProperties(tableId) {
		//Get the route properties
	$.getJSON('rest/queue/broker/attr', function(broker, status, xhr) {
			var tableSelector = '#' + tableId;
			var body = $('<tbody>');
			if (broker.attrs != null) {
				broker.attrs.forEach(function(prop) {
					if (prop != undefined && prop != null) {
						var row = $('<tr>');
						row.append($('<td>', { html: prop.name}));
						row.append($('<td>', { html: prop.value}));
						body.append(row);
					}
				});
			}
			//Remove old body
			$(tableSelector + ' > tbody').remove();
			//Add body to table
			$(tableSelector).append(body);
	});
};
	
//Shows a count of all routes, started and stopped on the passed in Div wrapped set
function showQueueCount(total, countDiv$) {
	$('.count', countDiv$).remove();
	countDiv$.append(
		$('<div>', { 
			class : 'count',
			html : "Number of Queues:" ,
		}));
	$('div', countDiv$).append($('<span>', { 
										class: 'badge',
										title: 'Total',
										html: total,
									}));
};

function showQueues(filter, queueTable) {
	$.getJSON('rest/queue', {id: filter}, function(results, status, xhr) {
		var queueSel = '#' + queueTable;
		var countDiv$ = $(queueSel).siblings('.queueCount');
		if (results == null || results.length == 0) {
			showQueueCount(0, countDiv$);
			return;
		}
		var bodyE = $('<tbody>', { class: 'tableBody'});
		var filterData = [];
		var on = 0, off = 0;
		results.forEach(function(result) {
			var rowE = $('<tr>', { 
					class: 'queue',
					id: result.name});
			rowE.append($('<td>').append($('<a>', { 
				html: result.name, 
				style : 'cursor:pointer',
				title: 'Click to view all properties'})));
			filterData.push(result.name);
			var backLogBadge ='badge badge-success';
			if (result.backLog > 0) {
				backLogBadge = 'badge badge-important'
			} 
			rowE.append($('<td>').append(
				$('<span>', {
					class : backLogBadge,
					html: result.backLog,
				})));
			var enq = result.name + "-enq";
			calcRate(enq, result.enqueued);
			var deq = result.name + "-deq";
			calcRate(deq, result.dequeued);
			rowE.append($('<td>').append(
						$('<span>', {
							class : 'badge badge-info',
							html: result.enqueued,
						})));
			if (hmstats.currentRate[enq] != undefined) {
				rowE.append($('<td>').append(
					$('<span>', {
						class : 'badge badge-info',
						html: hmstats.currentRate[enq],
					})).append(hmstats.rateInd[enq]));
			} else {
				rowE.append($('<td>', { 
					html: "wait..",
					}));
			}
			rowE.append($('<td>').append(
						$('<span>', {
							class : 'badge badge-info',
							html: result.dequeued,
						})));
			if (hmstats.currentRate[deq] != undefined) {
				rowE.append($('<td>').append(
					$('<span>', {
						class : 'badge badge-info',
						html: hmstats.currentRate[deq],
					})).append(hmstats.rateInd[deq]));
			} else {
				rowE.append($('<td>', { 
					html: "wait..",
					}));
			}
			var consumerClass;
			if (result.consumers > 0) {
				consumerClass = 'badge badge-success';
			} else {
				consumerClass = 'badge badge-important';
			}
			rowE.append($('<td>').append(
						$('<span>', {
							class : consumerClass,
							html: result.consumers,
						})));
			bodyE.append(rowE);
		});
		//Delete old and append new
		$('.tableBody', queueSel).remove();
		$(queueSel).append(bodyE);
		$('#filterText').attr("data-source", JSON.stringify(filterData));
		showQueueCount(results.length, countDiv$);
		$('#lastTime').html(new Date().toUTCString());
		$('#refreshBtn').css({ cursor: 'pointer'});
		$('body').css({ cursor: 'auto'});
	 });
}