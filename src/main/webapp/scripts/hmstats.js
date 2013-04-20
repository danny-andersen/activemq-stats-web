var hmstats = { 
		badgeGreen: $('<span>', {class : 'badge badge-success'}),
		// badgeGreen: "<span class='badge badge-success'>",
		badgeYellow: $('<span>', {class : 'badge badge-warning'}),
		// badgeYellow : "<span class='badge badge-warning'>",
		badgeRed: $('<span>', {class : 'badge badge-important'}),
		// badgeRed : "<span class='badge badge-important'>",
		badgeBlue: $('<span>', {class : 'badge badge-info'}),
		//badgeBlue : "<span class='badge badge-info'>",
		//badgeEnd : "</span>",
		live : false,
		pollPeriod : -1,
		ratePeriod : 10,
		rateCalc : false,
		lastTotals : {},
		currentRate: {},
		timeOfLastRateCalc : {},
		rateInd : {},
		defaultTable : "all-routesTable",
		tabCount : 0,
		brokerName : "",
}

$(document).ready(function() {
	getBrokerName();
	$('#filterBtn').click(function() {
		var filter = $('#filterText').val();
		var tabNo = "tab" + hmstats.tabCount++;
		var tabSelector = addTab(tabNo, 'Filter:' + filter, "routes", filter);
		//Add table to tab content
		$(tabSelector).append($('#routesTableTemplate').html());
		var filterTableId = tabNo + "-table";
		$('table', tabSelector).attr('id', filterTableId);
		var filterParam = {filter: filter};
		//Find routes and add to table
		showRoutes(filterParam, filterTableId);
		//Activate tab
		$('#tabList a[href="' + tabSelector + '"]').tab('show');
		$('#filterText').val("");
		return false;
	});

	//Route clicked in table - show selected route properties in a new tab
	$('.route').live('click', function() {
		var id = $(this).attr('id');
		//Add a tab to show the route details
		var tabNo = "tab" + hmstats.tabCount++;
		var tabSelector = addTab(tabNo, 'Route:' + id, "routeProp", id);
		$(tabSelector).append($('#propertiesTemplate').html());
		id = id.replace(/:/g, "-");
		var tableId = "id-" + id.replace(".","_") + "-" + tabNo + "-table";
		$('table', tabSelector).attr('id', tableId);
		showRouteProperties(id, tableId);
		//Activate tab
		$('#tabList a[href="' + tabSelector + '"]').tab('show');
		return false;
	});

	//Route clicked in table - show selected route properties in a new tab
	$('.queue').live('click', function() {
		var id = $(this).attr('id');
			//Add a tab to show the route details
		var tabNo = "tab" + hmstats.tabCount++;
		var tabSelector = addTab(tabNo, 'Queue:' + id, "queueProp", id);
		$(tabSelector).append($('#propertiesTemplate').html());
		id = id.replace(/:/g, "-");
		var tableId = "id-" + id.replace(".","_") + "-" + tabNo + "-table";
		$('table', tabSelector).attr('id', tableId);
		showQueueProperties(id, tableId);
		//Activate tab
		$('#tabList a[href="' + tabSelector + '"]').tab('show');
		return false;
	});

	$('#refreshBtn').click(function() {
		refresh();
	});

	$('.resetcounts').live('click', function() {
		var filter = $('.active.tab-pane').data('data-filter');
		var filterType = $('.active.tab-pane').data('data-filtertype');
		//Find active tab table
		var tableId = $('.active.tab-pane > table').attr('id');
		if (filterType == 'routes') {
			controlRoute(filter, "reset");
		} else if (filterType == 'queues') {
			controlQueue(filter, "resetStatistics");
		} else if (filterType == 'routeProp') {
			controlRoute(filter, "reset");
		} else if (filterType == 'queueProp') {
			controlQueue(filter, "resetStatistics");
		}
	});

	$('#liveUpdate').click(function() {
		var timer = $('#stopUpdate').data('timer');
		if (timer === undefined) { 
			hmstats.pollPeriod = $('#refreshPeriod').val() * 1.0;
			hmstats.ratePeriod = $('#ratePeriod').val() * 1.0;
			hmstats.timeSinceRateCalc = 0;
			var timer = window.setInterval(liveUpdate, hmstats.pollPeriod * 1000);
			$('#stopUpdate').data('timer', timer);
			$('#stopUpdate').removeAttr('disabled');
			$('#liveUpdate').attr('disabled', 'disabled');
		}
		hmstats.liveUpdate = true;
		return false;
	});

	$('#stopUpdate').click(function() {
		//Re-enable other buttons
		$('#liveUpdate').removeAttr('disabled');
		var timer = $('#stopUpdate').data('timer');
		window.clearInterval(timer);
		$('#stopUpdate').attr('disabled', 'disabled');
		$('#stopUpdate').removeData('timer')
		hmstats.liveUpdate = false;
		return false;
	});

	$('#queueBtn').click(function() {
		var filter = $('#filterText').val();
		var tabNo = "tab" + hmstats.tabCount++;
		var tabSelector;
		if (filter == null || filter == "") {
			tabSelector = addTab(tabNo, 'All Queues', "queues", filter);
		} else {
			tabSelector = addTab(tabNo, 'Queue:' + filter, "queues", filter);
		}
		//Add table to tab content
		$(tabSelector).append($('#queueTableTemplate').html());
		var filterTableId = tabNo + "-table";
		$('table', tabSelector).attr('id', filterTableId);
		//Find routes and add to table
		showQueues(filter, filterTableId);
		//Activate tab
		$('#tabList a[href="' + tabSelector + '"]').tab('show');
		$('#filterText').val("");
		return false;
	});

	$('#tab-remove').live('click', function() {
		var tabSelector = $(this).closest('a').attr('href');
		liEle = $(this).closest('li');
		$(tabSelector).remove();
		$(liEle).remove();
		if (tabSelector != "#tab0") {
			$('#tabList a[href="#tab0"]').tab('show');
		}
	});
	addDefaultTab();

});

function addDefaultTab() {
	var tabNo = "tab" + hmstats.tabCount++;
	var tabSelector = addTab(tabNo, "All Routes", "routes", "");
	$(tabSelector).append($('#routesTableTemplate').html());
	$('table', tabSelector).attr('id', hmstats.defaultTable);
	$('#tabList a[href="' + tabSelector + '"]').tab('show');
	showRoutes(null, hmstats.defaultTable);
};

function addTab(tabName, tabTitle, filterType, filter) {
	var tabSelector = '#' + tabName;
	//Add new tab to the list of tabs
	$('<li>').append($('<a>', {
			href: tabSelector,
			"data-toggle": 'tab',
			html: tabTitle + " ",
		}).append($('<i>', {
			id: "tab-remove",
			class: "icon-remove",
		}))).appendTo($('#tabList'));
	//Add content tab
	//Store the tabName in a custom data field
	$('<div>', {
		class: 'tab-pane fade',
		id: tabName,
	}).data('data-filtertype', filterType)
		.data('data-filter', filter)
		.appendTo('#contentTabs');
	return tabSelector;
}

function liveUpdate() {
	refresh();
	hmstats.ratePeriod = $('#ratePeriod').val() * 1.0;
	var period = $('#refreshPeriod').val() * 1.0;
	if (period != hmstats.pollPeriod) {
		//Set new poll period
		hmstats.pollPeriod = period;
		var timer = $('#stopUpdate').data('timer');
		window.clearInterval(timer);
		var timer = window.setInterval(liveUpdate, hmstats.pollPeriod * 1000);
		$('#stopUpdate').data('timer', timer);
	}
};

function refresh() {
	$('body').css({ pointer: 'progress'});
	var filter = $('.active.tab-pane').data('data-filter');
	var filterType = $('.active.tab-pane').data('data-filtertype');
	//Find active tab table
	var tableId = $('.active.tab-pane > table').attr('id');
	if (filterType == 'routes') {
		showRoutes({filter: filter}, tableId);
	} else if (filterType == 'queues') {
		showQueues(filter, tableId);
	} else if (filterType == 'routeProp') {
		showRouteProperties(filter, tableId);		
	} else if (filterType == 'queueProp') {
		showQueueProperties(filter, tableId);
	} else if (filterType == 'brokerProp') {
		showBrokerProperties(tableId);
	}

}


function calcRate (id, currentVal) {
	var now = Date.now();
	if (hmstats.lastTotals[id] == undefined) {
		hmstats.lastTotals[id] = currentVal;
	}
	if (hmstats.timeOfLastRateCalc[id] == undefined) {
		hmstats.timeOfLastRateCalc[id] = now;
	}
	var elapsed = (now - hmstats.timeOfLastRateCalc[id]) / 1000.0;
	if ( elapsed >= hmstats.ratePeriod) {
		var rate = (currentVal - hmstats.lastTotals[id]) / elapsed;
		rate = Math.round(rate*100.0);
		rate = rate / 100.0;
		hmstats.lastTotals[id] = currentVal;
		hmstats.rateInd[id] = $('<i>');
		if (hmstats.currentRate[id] != undefined) {
			if (rate > hmstats.currentRate[id]) {
				hmstats.rateInd[id].addClass('icon-arrow-up');
			} else if (rate < hmstats.currentRate[id]) {
				hmstats.rateInd[id].addClass('icon-arrow-down');
			} else {
				hmstats.rateInd[id].addClass('icon-arrow-right');
			}
		} else {
			// hmstats.rateInd[id].addClass('icon-arrow-right');
		}
		hmstats.currentRate[id] = rate;
		hmstats.timeOfLastRateCalc[id] = now;
	}
}


