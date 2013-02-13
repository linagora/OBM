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
		error: function(jqXhr) {
			var status = jqXhr.status;
			if ( status == 0 ) {
				status = 500;
			}
			callback(status);
		},
		success: function(checkList){
			callback(null,checkList);
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
			callback(err);
		}
		moduleTemplate = html;
		actionDone++;
		checkAllDone();
	});
	$.obm.getCheckList(function(err,json) {
		if ( err ) {
			callback(err);
		}
		checkList = json;
		actionDone++;
		checkAllDone();
	});
}

$(document).ready( function(){
	$("#startCheckButton").click( function(){
		$(this).attr("disabled","true");
		$.obm.bootstrap(
			function(err, checkList, moduleTemplate) {
				if ( err ) {
					// code pour dire à l'utilisateur que ça merde
					alert( "Error "+err );
					return ;
				}
				$.obm.addModules(checkList, moduleTemplate);
				$.obm.runChecks(checkList);
			}
		);
	});
	$("#restartCheckButton").click(function() {
	  window.location.replace(window.location.pathname+"?autostart=true");
	});
	if ( $.getQuery().autostart == "true" ) {
	  $("#startCheckButton").click();
	}
});

$.obm.codeToStatus = {0: "success", 1: "warning", 2: "error"};

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
      
      if ( moduleStatus.status == "error" || moduleStatus.checksCount == moduleStatus.checksDone ) {
	$.obm.setModuleStatus(flatCheck.moduleId, moduleStatus.status);
	if ( moduleStatus.status == "success" ) {
	  $.obm.closeModuleContainer(flatCheck.moduleId);
	} else {
	  $.obm.openCheckContainer(flatCheck.moduleId,flatCheck.checkId);
	}
      }
    };
  },
  buildEndCallback: function() {
    return function() {
      alert("Tests completed");
      $("#restartCheckButton").removeClass("visibility-hidden");
    };
  }
};

$.obm.htmlId = function(moduleId, checkId) {
  return moduleId+"-"+checkId;
};

$.obm.addModules = function(checkList, moduleTemplate, testTemplate) {
    var counter = 0;
    checkList.modules.forEach(function(module) {
      module.checks.forEach(function(check) {
	check.htmlId = module.id+"-"+check.id;
      });
    });
    for( var index in checkList.modules){
      var output = Mustache.render(moduleTemplate, checkList.modules[index]);
      $("#modules-list").append(output);
    }
};

$.obm.displayCheckInfo = function(moduleId, checkId, code, messages) {
  var htmlId = $.obm.htmlId(moduleId, checkId);
  $("#"+htmlId+"-info").removeClass('visibility-hidden').addClass('visibility-visible text-' + $.obm.codeToStatus[code]);
  if (messages) {
	$("#"+htmlId+"-info").html(messages.join("<br/>"));
  }
};

$.obm.showModuleContainer = function(id) {
	$("#"+id+"-header").removeClass('visibility-hidden').addClass('visibility-visible');
};

$.obm.showCheckContainer = function(moduleId, checkId) {
  var htmlId = $.obm.htmlId(moduleId, checkId);
  $("#"+htmlId+"-header").removeClass('visibility-hidden').addClass('visibility-visible');
};

$.obm.setModuleStatus = function(id, status) {
	$("#"+id).removeClass("test-info test-warning test-error test-success").addClass("test-"+status);
};

$.obm.setCheckStatus = function(moduleId, checkId, status) {
  var htmlId = $.obm.htmlId(moduleId, checkId);
  $("#"+htmlId).removeClass("test-info test-warning test-error test-success").addClass("test-"+status);
};

$.obm.closeModuleContainer = function(id) {
  $("#"+id+"-inner").removeClass("in");
}

$.obm.openCheckContainer = function(moduleId, checkId) {
  var htmlId = $.obm.htmlId(moduleId, checkId);
  $("#"+htmlId+"-test").addClass("in");
}

$.obm.setAlert = function(status, message) {
	// To Do: Add Alert after progress bar, before #modules-list http://twitter.github.com/bootstrap/components.html#alerts
};