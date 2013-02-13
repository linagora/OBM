define(["checkRunner"], function(checkRunner) {
  
  function checkScheduler ( availableChecks, urlBuilder ) {
    this.availableChecks = availableChecks;
    this.urlBuilder = urlBuilder;
  };

  checkScheduler.prototype.flattenChecks = function(availableChecks) {
    var flatChecks = [];
    availableChecks.modules.forEach(function(module) {
      var moduleId = module.id;
      module.checks.forEach(function(check) {
	flatChecks.push( {moduleId: moduleId, checkId: check.id} );
      });
    });
    return flatChecks;
  };

  checkScheduler.prototype.runChecks = function(checkStartCallback, checkCompleteCallback,  endCallback) {
    var flatChecks = this.flattenChecks(this.availableChecks);
    var checksCount = flatChecks.length;
    var urlBuilder = this.urlBuilder;
    var runOneCheck = function() {
      if ( flatChecks.length == 0 ) {
	return endCallback();
      }
      var nextCheck = flatChecks.shift();
      checkStartCallback(nextCheck);
      var runner = new checkRunner(nextCheck, urlBuilder);
      runner.run(function(result) {
	checkCompleteCallback(nextCheck, result);
	if ( result.code == 2 ) {
	  return endCallback();
	}
	runOneCheck();
      });
    };
    
    runOneCheck();
    return checksCount;
  };

  checkScheduler.prototype.availableChecks = null;
  checkScheduler.prototype.urlBuilder = null;
  
  return checkScheduler;
});