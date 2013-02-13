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

	$("#showButton").click( function(){
		$.obm.displayItem("php");
		$.obm.displayItem("PhpEnvironmentCheck");
	});

	$("#successButton").click( function(){
		$.obm.setStatus("php", "success");
		$.obm.setStatus("PhpEnvironmentCheck", "success");
	});
});

$.obm.codeToStatus = {0: "success", 1: "warning", 2: "error"};

$.obm.runChecks = function(checkList) {
  var modulesStatus = {};
  checkList.modules.forEach(function(module) {
    modulesStatus[module.id] = {
      checksCount: module.checks.length,
      checksDone: 0,
      status: "success"
    };
  });
  var urlBuilder = function(flatCheck) {
    return "/healthcheck/backend/HealthCheck.php/"+flatCheck.moduleId+"/" + flatCheck.checkId;
  };
  
  var checkStartCallback = function(flatCheck) {
    $.obm.displayItem(flatCheck.moduleId);
    $.obm.displayItem(flatCheck.checkId);
  };
  
  var checkCompleteCallback = function(flatCheck, checkResult) {
    var moduleStatus = modulesStatus[flatCheck.moduleId];
    moduleStatus.checksDone++;
    moduleStatus.status = $.obm.codeToStatus[checkResult.code];
    
    $.obm.setStatus(flatCheck.checkId, $.obm.codeToStatus[checkResult.code]);
    $.obm.displayCheckInfo(flatCheck.checkId, checkResult.code, checkResult.messages);
    
    if ( moduleStatus.status == "error" || moduleStatus.checksCount == moduleStatus.checksDone ) {
      $.obm.setStatus(flatCheck.moduleId, moduleStatus.status);
      if ( moduleStatus.status == "success" ) {
	$.obm.closeModuleContainer(flatCheck.moduleId);
      } else {
	$.obm.openCheckContainer(flatCheck.checkId);
      }
    }
  };
  
  var endCallback = function() {
    alert("Tests compmleted");
  };
  
  var scheduler = new checkScheduler(checkList, urlBuilder);
  scheduler.runChecks(checkStartCallback, checkCompleteCallback, endCallback);
  
};


$.obm.addModules = function(checkList, moduleTemplate, testTemplate) {
	for( var index in checkList.modules){
		var output = Mustache.render(moduleTemplate, checkList.modules[index]);
		$("#modules-list").append(output);
	}
};

$.obm.displayCheckInfo = function(id, code, messages) {
	$("#"+id+"-info").removeClass('visibility-hidden').addClass('visibility-visible text-' + $.obm.codeToStatus[code]);
	if (messages) {
	      $("#"+id+"-info").html(messages.join("<br/>"));
	}
};
$.obm.displayItem = function(id) {
	$("#"+id+"-header").removeClass('visibility-hidden').addClass('visibility-visible');
};

$.obm.setStatus = function(id, status) {
	$("#"+id).removeClass("test-info test-warning test-error test-success").addClass("test-"+status);
};

$.obm.closeModuleContainer = function(id) {
  $("#"+id+"-inner").removeClass("in");
}
$.obm.openCheckContainer = function(id) {
  $("#"+id+"-test").addClass("in");
}

$.obm.setAlert = function(status, message) {
	// To Do: Add Alert after progress bar, before #modules-list http://twitter.github.com/bootstrap/components.html#alerts
};

$.obm.updateProgressBar = function(pourcent) {
	// To Do: $("#progress-bar").css('width', x+"%");
};