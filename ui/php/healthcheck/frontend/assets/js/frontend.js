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

$.obm.getTestTemplate = function(callback) {
	$.ajax({
		url: "test.tpl",
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
	var actionDone=0, actionCount=3;
	var moduleTemplate = null;
	var testTemplate = null;
	var checkList = null;

	var checkAllDone = function() {
		if ( actionCount == actionDone ) {
			callback(null, checkList, moduleTemplate, testTemplate);
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
	$.obm.getTestTemplate(function(err,html) {
		if ( err ) {
			callback(err);
		}
		testTemplate = html;
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

$.obm.bootstrap(
	function(err, checkList, moduleTemplate, testTemplate) {
		if ( err ) {
			// code pour dire à l'utilisateur que ça merde
			alert( "Error "+err );
			return ;
		}
		$.obm.addModules(checkList, moduleTemplate, testTemplate);
	}
);

$.obm.addModules = function(checkList, moduleTemplate, testTemplate) {
	console.log(checkList);
	for( var index in checkList.modules){
		var output = Mustache.render(moduleTemplate, checkList.modules[index]);
		$("#modules-list").append(output);
	}
};

$.obm.statusModule = function(moduleId, moduleStatus) {
	moduleId.removeClass("test-info test-warning test-error test-sucess").addClass(moduleStatus);
};

$.obm.statusCheck = function(checkId, checkStatus) {
	checkId.removeClass("test-info test-warning test-error test-sucess").addClass(checkStatus);
};