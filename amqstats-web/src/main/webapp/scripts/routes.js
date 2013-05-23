function showRouteProperties(id, tableId) {
		//Get the route properties
	$.getJSON('rest/route', {id: id}, function(route, status, xhr) {
			var tableSelector = '#' + tableId;
			var body = $('<tbody>');
			var row = $('<tr>');
			row.append($('<td>', { html: 'id'}));
			row.append($('<td>', { html: route.id}));
			body.append(row);
			route.attrs.forEach(function(prop) {
				if (prop != undefined && prop != null) {
					row = $('<tr>');
					row.append($('<td>', { html: prop.name}));
					if (prop.name == 'routeXml') {
						row.append($('<td>').append($('<pre>').text(prop.value)));
					} else {
						row.append($('<td>', { html: prop.value}));
					}
					body.append(row);
				}
			});
			//Remove old body
			$(tableSelector + ' > tbody').remove();
			//Add body to table
			$(tableSelector).append(body);
			var updateTime = new Date().toUTCString();
			$('#lastTime').html(updateTime);
			var tabId = '#' + $(tableSelector).parents('.tab-pane').attr('id');
			$('a[href="' + tabId + '"]').data('lastUpdateTime', updateTime);
			//Save tab update time in the tab
			$('#refreshBtn').css({ cursor: 'pointer'});
			$('body').css({ cursor: 'auto'});
	});
};

//Shows a count of all routes, started and stopped on the passed in Div wrapped set
function showRouteCount(total, on, off, countDiv$) {
	$('.count', countDiv$).remove();
	countDiv$.append(
		$('<div>', { 
			class : 'count',
			html : "Number of Routes:" ,
		}));
	$('div', countDiv$).append($('<span>', { 
										class: 'badge',
										title: 'Total',
										html: total,
									}));
	if (on != undefined) {
		$('div', countDiv$).append($('<span>', { class: 'badge badge-success',
												title: 'Started',
												html: on}));
	}
	if (off != undefined) { 
		$('div', countDiv$).append($('<span>', { class: 'badge badge-important',
												title: 'Stopped',
												html: off}));
	}
};

function controlRoute(id, command) {
	var params = {id: id, command: command};
	$.post('rest/route/control', params, function(result, status, xhr) {
		//Refresh current state
		refresh();
	});

}

function showRoutes(filter, routeTable) {
	$.getJSON('rest/route/summary', filter, function(results, status, xhr) {
		var routeSel = '#' + routeTable;
		var countDiv$ = $(routeSel).siblings('.routeCount');
		if (results[0] == null) {
			showRouteCount(0, 0, 0, countDiv$);
			return;
		}
		var bodyE = $('<tbody>', { class: 'routeBody'});
		// rowStr = "<tbody class='routeBody'>";
		var filterData = [];
		var on = 0, off = 0;
		results.forEach(function(result) {
			var rowE = $('<tr>', { 
					id: result.id});
			rowE.append($('<td>').append($('<a>', { 
					html: result.id,
					class: 'route',
					style : 'cursor:pointer',
					title: 'Click to view all route properties'})));
			filterData.push(result.id);
			if (result.state == "Started") {
				on++;
				rowE.append($('<td>').append(
					$('<span>', {
						class : 'badge badge-success',
						style : 'cursor:pointer',
						html: result.state,
						click: function() {
							controlRoute(result.id, "stop");
							return false;
						},
					})));
			} else {
				off++;
				rowE.append($('<td>').append(
					$('<span>', {
						class : 'badge badge-important',
						style : 'cursor:pointer',
						html: result.state,
						click: function() {
							controlRoute(result.id, "start");
							return false;
						},
					})));
			}
			if (result.sourceUri.indexOf("file:") == -1 && result.backLog != -1) {
			 	//Route uri doesn't start with file and we have a non-negative backlog so assume a queue
				var queueId = result.sourceUri.substring(result.sourceUri.indexOf("://") + 3, result.sourceUri.length);
				rowE.append($('<td>').append($('<a>', { 
					html: result.sourceUri,
					style : 'cursor:pointer',
					class: 'queue',
					title: 'Click to view all queue properties',
					id: queueId,
				})));
			} else {
				rowE.append($('<td>', { 
					html: result.sourceUri,
					}));
			}
			var brStr = null;
			if (result.destUri != null) {
				brStr = result.destUri.replace(/\n/g, "<br>");
			}	
			rowE.append($('<td>', {
						html: brStr,
			}));
			var backLogBadge = 'badge badge-success';
			if (result.backLog > 0) {
				backLogBadge = 'badge badge-important';
			} else if (result.backLog < 0) {
				backLogBadge = 'badge badge-info';
				result.backLog = '?';
			}
			rowE.append($('<td>').append(
					$('<span>', {
						class : backLogBadge,
						html: result.backLog,
					})));
			rowE.append($('<td>').append(
				$('<span>', {
					class : 'badge badge-success',
					html: result.success
				})));
			if (result.failed > 0) {
				rowE.append($('<td>').append(
					$('<span>', {
						class : 'badge badge-important',
						html: result.failed})));
			} else {
				rowE.append($('<td>').append(
					$('<span>', {
						class : 'badge badge-success',
						html: result.failed
					})));
			}
			rowE.append($('<td>').append(
				$('<span>', {
					class : 'badge badge-info',
					html: result.total
				})));
			calcRate(result.id, result.total);
			if (hmstats.currentRate[result.id] != undefined) {
				rowE.append($('<td>').append(
					$('<span>', {
						class : 'badge badge-info',
						html: hmstats.currentRate[result.id],
					})).append(hmstats.rateInd[result.id]));
			} else {
				rowE.append($('<td>', { 
					html: "wait..",
					}));
			}
			rowE.append($('<td>').append(
				$('<span>', {
					class : 'badge badge-info',
					html: result.avgProcTimeMs,
				})));
			rowE.append($('<td>').append(
				$('<span>', {
					class : 'badge badge-info',
					html: result.avgMessageSizeKb,
				})));
			if (result.lastTime != null) {
				rowE.append($('<td>', { 
					html: result.lastTime,
					}));
			}
			bodyE.append(rowE);
		});
		//Delete old and append new
		$('.routeBody', routeSel).remove();
		$(routeSel).append(bodyE);
		$('#filterText').attr("data-source", JSON.stringify(filterData));
		showRouteCount(results.length, on, off, countDiv$);
		var updateTime = new Date().toUTCString();
		$('#lastTime').html(updateTime);
		var tabId = '#' + $(routeSel).parents('.tab-pane').attr('id');
		$('a[href="' + tabId + '"]').data('lastUpdateTime', updateTime);
		$('#refreshBtn').css({ cursor: 'pointer'});
		$('body').css({ cursor: 'auto'});
	 });
}