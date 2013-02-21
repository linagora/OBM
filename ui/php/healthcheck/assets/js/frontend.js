/******************************************************************************
 * Health Check Javascript
 *****************************************************************************/
function test(){
	alert("test");
}

$.obm = {};
$.obm.getModuleTemplate = function(callback) {
	$.ajax({
		url: "module.tpl",
		error: function(jqXhr) {
			var status = jqXhr.status;
			if ( status == 0 ) {
				status = 500;
			}
			callback(status);
		},
		success: function(code_html){
			callback(null,code_html);
		}
	});
};

$.obm.getCheckList = function(callback) {
	$.ajax({
		url: "/healthcheck/backend/HealthCheck.php/",
		error: function(jqXhr, errorType) {
			var status = jqXhr.status;
			if ( status == 0 || errorType) {
				status = 500;
			}
			callback(status);
		},
		success: function(mList){
			if ($.isPlainObject(mList)) {
				var count = mList.modules.length;

				mList.modules.forEach(function (module) {
					$.obm.getChecksByModule(module, function (err) {
						count--;

						if (err) {
							callback(err);
						} else if (count <= 0) {
							callback(null, mList);
						}
					})
				});
			} else {
				callback(500);
			}
		}
	});
};

$.obm.getChecksByModule = function(module, callback) {
	$.ajax({
		url: "/healthcheck/backend/HealthCheck.php/" + module.id,
		error: function(jqXhr) {
			callback(jqXhr.status);
		},
		success: function(checkList){
			if ($.isPlainObject(checkList)) {
				module.checks = checkList.checks;
			}

			callback(null);
		}
	});
};

$.obm.bootstrap = function(callback) {
	var actionDone=0, actionCount=2;
	var moduleTemplate = null;
	var checkList = null;

	var checkAllDone = function() {
		if ( actionCount == actionDone ) {
			callback(null, checkList, moduleTemplate);
		}
	};

	$.obm.getModuleTemplate(function(err,html) {
		if ( err ) {
			return callback("Fail to get the template");
		}
		moduleTemplate = html;
		actionDone++;
		checkAllDone();
	});
	$.obm.getCheckList(function(err,json) {
		if ( err ) {
			return callback("Fail to get the check list");
		}
		checkList = json;
		actionDone++;
		checkAllDone();
	});
}

$(document).ready( function(){
	$("#startCheckButton").click( function(){
		$.obm.hideStartButton(this);
		$.obm.displayObject("#pauseCheckButton");
		$("#introduction").fadeOut('100', function() {
			$.obm.hideObject("#introduction");
		});
		$.obm.bootstrap(
			function(err, checkList, moduleTemplate) {
				if ( err ) {
					alert( "Error: "+err );
					return ;
				}
				$.obm.addModules(checkList, moduleTemplate);
				$.obm.bindRetryCheckButton();
				$.obm.runChecks(checkList);
			}
		);
	});

	$("#restartCheckButton").click(function() {
	  window.location.replace(window.location.pathname+"?autostart=true");
	});

	$("#pauseCheckButton").click(function() {
		$.obm.togglePauseButton(this);
	});

	$("#resumeCheckButton").click(function() {
		$.obm.togglePauseButton($("#pauseCheckButton"));
	});

	if ( $.getQuery().autostart == "true" ) {
	  $("#startCheckButton").click();
	}
});

$.obm.codeToStatus = {0: "success", 1: "warning", 2: "error"};

$.obm.bindRetryCheckButton = function(){
	$(".retryButton").click(function() {
		$.obm.isExternalCheck( $(this).data('module'), $(this).data('check'), $(this).data('external') );
	});
};

$.obm.isExternalCheck = function(moduleId, checkId, moduleUrl) {
	if( checkId == "ExternalAccess" ){
		if( moduleUrl != ""){
			$.obm.relaunchExternalCheck( moduleId, checkId, moduleUrl );
		}
	} else {
		$.obm.relaunchCheck( moduleId, checkId );
	}
};

$.obm.relaunchCheck = function(moduleId, checkId) {
	var flatCheck = {moduleId: moduleId, checkId: checkId};
	var urlBuilder = $.obm.callbacks.buildUrlBuilder();
	var htmlId = $.obm.htmlId(moduleId, checkId);

	$.obm.removeCheckInBadge(htmlId);
	$("#"+htmlId+"-info").removeClass('visibility-visible').addClass('visibility-hidden');
	$.obm.setCheckStatus(moduleId, checkId, "info");
	$.obm.progressBarRunnning();

	require(["checkRunner"], function(checkRunner) {
		var runner = new checkRunner(flatCheck, urlBuilder);
		runner.run(function(result) {
			$.obm.updateAfterRelaunchCheck(moduleId, checkId, result.code, result.messages);
	    });
	});
};

$.obm.relaunchExternalCheck = function(moduleId, checkId, moduleUrl) {
	var flatCheck = {moduleId: moduleId, checkId: checkId, moduleUrl: moduleUrl };
	var htmlId = $.obm.htmlId(moduleId, checkId);

	$.obm.removeCheckInBadge(htmlId);
	$("#"+htmlId+"-info").removeClass('visibility-visible').addClass('visibility-hidden');
	$.obm.setCheckStatus(moduleId, checkId, "info");
	$.obm.progressBarRunnning();

	require(["checkExternal"], function(checkExternal) {
		var runner = new checkExternal(flatCheck, moduleUrl);
		runner.run(function(result) {
			$.obm.updateAfterRelaunchCheck(moduleId, checkId, result.code, result.messages);
	    });
	});
};

$.obm.runChecks = function(checkList) {
	require(["checkScheduler", "progressBar", "pubsub"], function(checkScheduler, progressBar, pubsub) {
		$.obm.realRunChecks(checkList, checkScheduler, progressBar, pubsub);
	});
};

$.obm.realRunChecks = function(checkList, checkScheduler, progressBar, pubsub) {
	var modulesStatus = {};
	checkList.modules.forEach(function(module) {
	modulesStatus[module.id] = {
		checksCount: module.checks.length,
		checksDone: 0,
		status: "success"
		};
	});
	var startTopic = pubsub.topic("checkScheduler:start");
	var endOfCheckTopic = pubsub.topic("checkScheduler:endOfCheck");
	var progressBarInstance = new progressBar();
	startTopic.subscribe(function(data) { progressBarInstance.setElementCount(data.checksCount); });
	endOfCheckTopic.subscribe(function(data) { progressBarInstance.increment(); });

	var endCallback = $.obm.callbacks.buildEndCallback();
	var checkStartCallback = $.obm.callbacks.buildCheckStartCallback();
	var checkCompleteCallback = $.obm.callbacks.buildCheckCompleteCallback(modulesStatus);
	var scheduler = new checkScheduler(checkList, $.obm.callbacks.buildUrlBuilder());
	scheduler.runChecks(checkStartCallback, checkCompleteCallback, endCallback);
};

$.obm.callbacks = {
	buildUrlBuilder: function() {
		return function(flatCheck) {
			return "/healthcheck/backend/HealthCheck.php/"+flatCheck.moduleId+"/" + flatCheck.checkId;
		};
	},
	buildCheckStartCallback: function() {
		return function(flatCheck) {
			$.obm.showModuleContainer(flatCheck.moduleId);
			$.obm.showCheckContainer(flatCheck.moduleId, flatCheck.checkId);
		};
	},
	buildCheckCompleteCallback: function(modulesStatus) {
		return function(flatCheck, checkResult) {
			var moduleStatus = modulesStatus[flatCheck.moduleId];
			moduleStatus.checksDone++;
			moduleStatus.status = $.obm.codeToStatus[checkResult.code];

			$.obm.setCheckStatus(flatCheck.moduleId, flatCheck.checkId, $.obm.codeToStatus[checkResult.code]);
			$.obm.displayCheckInfo(flatCheck.moduleId, flatCheck.checkId, checkResult.code, checkResult.messages);

			if( moduleStatus.status == "error"){
				$.obm.setModuleStatus(flatCheck.moduleId, moduleStatus.status);
				$.obm.openCheckContainer(flatCheck.moduleId, flatCheck.checkId);
			} else if( moduleStatus.status == "warning" ) {
				if( !$("#"+flatCheck.moduleId).hasClass("test-error") ){
					$.obm.setModuleStatus(flatCheck.moduleId, moduleStatus.status);
				}
				$.obm.openCheckContainer(flatCheck.moduleId, flatCheck.checkId);
			}

			if ( moduleStatus.checksCount == moduleStatus.checksDone ){
				$.obm.updateModule( $.obm.htmlId(flatCheck.moduleId, flatCheck.checkId), flatCheck.moduleId, flatCheck.checkId);
			}
			};
		},
		buildEndCallback: function() {
			return function() {
				$.obm.displayObject("#restartCheckButton");
				$.obm.endColorProgressBar();
		};
	}
};

$.obm.htmlId = function(moduleId, checkId) {
	return moduleId+"-"+checkId;
};

$.obm.addModules = function(checkList, moduleTemplate, testTemplate) {
	var counter = 0;
	checkList.modules.forEach(function(module) {
		if(module.url){
			$.obm.insertExternalCheck(module);
		};
		module.checks.forEach(function(check) {
			check.htmlId = module.id+"-"+check.id;
			check.referentModule = module.id;
			check.external = module.url;
		});
	});
	for( var index in checkList.modules){
		var output = Mustache.render(moduleTemplate, checkList.modules[index]);
		$("#modules-list").append(output);
	}
};

$.obm.insertExternalCheck = function(module){
	module.checks.unshift(
		{
			"id": "ExternalAccess",
			"name": "External Access to " + module.name,
			"description": "Check access to " + module.name + " module ( " + module.url + " ) from this browser.",
			"url": "http://obm.org",
			"parentId": null
		}
	);
}

$.obm.displayCheckInfo = function(moduleId, checkId, code, messages) {
	var htmlId = $.obm.htmlId(moduleId, checkId);

	if (messages) {
		$.obm.updateBadges(code);
		if(code > 0) {
			$.obm.updateProgressBarColor(code);
		}
		if(messages.length > 0){
			$("#"+htmlId+"-info").removeClass('visibility-hidden').addClass('visibility-visible text-' + $.obm.codeToStatus[code]);
			$("#"+htmlId+"-info").html( "<strong>Messages:</strong><br/>" + messages.join("<br/>") );
		}
	}
};

$.obm.updateAfterRelaunchCheck = function(moduleId, checkId, code, messages) {
	var htmlId = $.obm.htmlId(moduleId, checkId);

	$.obm.setCheckStatus(moduleId, checkId, $.obm.codeToStatus[code]);
	$.obm.displayCheckInfo(moduleId, checkId, code, messages);
	$.obm.updateModule(htmlId, moduleId, checkId);
	$.obm.endColorProgressBar();
};

$.obm.updateBadges = function(code) {
	var badgeName = {"1" : "#badge-warnings", "2" : "#badge-errors" };
	var value = $(badgeName[code]).text();
	if (code > 0){
		value++;
		$(badgeName[code]).html(value);
	}
};

$.obm.removeCheckInBadge = function (htmlId){
	var errors = $("#badge-errors").text();
	var warnings = $("#badge-warnings").text();

	if( $("#"+htmlId).hasClass("test-error") && errors > 0 ){
		errors--;
		$("#badge-errors").html(errors);
	} else if( $("#"+htmlId).hasClass("test-warning") && warnings > 0 ){
		warnings--;
		$("#badge-warnings").html(warnings);
	}
}

$.obm.showModuleContainer = function(id) {
	$.obm.displayObject( $("#"+id+"-header") );
};

$.obm.showCheckContainer = function(moduleId, checkId) {
	var htmlId = $.obm.htmlId(moduleId, checkId);
	$.obm.displayObject( $("#"+htmlId+"-header") );
};

$.obm.closeCheckContainer = function(id) {
	$("#"+id+"-test").removeClass("in");
}

$.obm.setModuleStatus = function(id, status) {
	$("#"+id).removeClass("test-info test-warning test-error test-success").addClass("test-"+status);
};

$.obm.updateModule = function(htmlId, moduleId, checkId){
	var warnings = $("#"+moduleId+"-header").children().hasClass("test-warning");
	var errors = $("#"+moduleId+"-header").children().hasClass("test-error");

	if( errors ) {
		$.obm.setModuleStatus(moduleId, "error");
	} else if( warnings ) {
		$.obm.setModuleStatus(moduleId, "warning");
	} else {
		$.obm.setModuleStatus(moduleId, "success");
		$.obm.closeModuleContainer(moduleId);
	}
}

$.obm.setCheckStatus = function(moduleId, checkId, status) {
	var htmlId = $.obm.htmlId(moduleId, checkId);
	$("#"+htmlId).removeClass("test-info test-warning test-error test-success").addClass("test-"+status);
};

$.obm.closeModuleContainer = function(id) {
	$("#"+id+"-inner").removeClass("in");
}

$.obm.hideStartButton = function(startBtn) {
	if ( $(startBtn).css("display") != "none" ){
		$.obm.hideObject(startBtn);
		$.obm.displayObject("#restartWrapper");
	}
}
$.obm.togglePauseButton = function(pauseBtn) {
	if ( $(pauseBtn).css("display") != "none" ){
		$.obm.hideObject(pauseBtn);
		$.obm.displayObject("#resumeWrapper");
	} else {
		$.obm.displayObject(pauseBtn);
		$.obm.hideObject("#resumeWrapper");
	}
}

$.obm.displayObject = function(object) {
	$(object).css("display","block");
}

$.obm.hideObject = function(object) {
	$(object).css("display","none");
}

$.obm.openCheckContainer = function(moduleId, checkId) {
	var htmlId = $.obm.htmlId(moduleId, checkId);
	$("#"+htmlId+"-test").addClass("in");
}

$.obm.updateProgressBarColor = function(code) {
	var colorClass = (code == 2 ) ? "progress-danger" : "progress-" + $.obm.codeToStatus[code] ;
	$("#progress-bar-color").removeClass("progress-info progress-warning progress-error progress-success").addClass(colorClass);
}

$.obm.progressBarRunnning = function(){
	$("#progress-bar-color").addClass("progress-striped");
	$("#progress-bar-color").removeClass("progress-info progress-warning progress-error progress-success").addClass("progress-info");
}

$.obm.endColorProgressBar = function() {
	var errors = $("#badge-errors").text();
	var warnings = $("#badge-warnings").text();

	$("#progress-bar-color").removeClass("progress-striped");

	if( errors > 0){
		$.obm.updateProgressBarColor(2);
	}else if(warnings > 0){
		$.obm.updateProgressBarColor(1);
	}else{
		$.obm.updateProgressBarColor(0);
	}
}