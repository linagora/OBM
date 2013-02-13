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
		url: "/healthcheck/backend/HealthCheck.php/getAvailableChecks",
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

$.obm.addModules = function(checkList, moduleTemplate, testTemplate) {
	for( var index in checkList.modules){
		var output = Mustache.render(moduleTemplate, checkList.modules[index]);
		$("#modules-list").append(output);
	}
};

$.obm.displayItem = function(id) {
	$("#"+id+"-header").toggleClass('visibility-hidden visibility-visible');
};

$.obm.setStatus = function(id, status) {
	$("#"+id).removeClass("test-info test-warning test-error test-success").addClass("test-"+status);
};

$.obm.setAlert = function(status, message) {
	// To Do: Add Alert after progress bar, before #modules-list http://twitter.github.com/bootstrap/components.html#alerts
};

$.obm.updateProgressBar = function(pourcent) {
	// To Do: $("#progress-bar").css('width', x+"%");
};