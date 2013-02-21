define(["checkRunner", "pubsub", "checkExternal"], function(checkRunner, pubsub, checkExternal) {
  var startTopic = pubsub.topic("checkScheduler:start");
  var endOfCheckTopic = pubsub.topic("checkScheduler:endOfCheck");
  function checkScheduler ( availableChecks, urlBuilder ) {
    this.availableChecks = availableChecks;
    this.urlBuilder = urlBuilder;
  };

  checkScheduler.prototype.flattenChecks = function(availableChecks) {
    var flatChecks = [];
    availableChecks.modules.forEach(function(module) {
      var moduleId = module.id;
      module.checks.forEach(function(check) {
        if ( check.id == "ExternalAccess") {
          flatChecks.push( {moduleId: moduleId, checkId: check.id, moduleUrl: module.url} );
        } else {
          flatChecks.push( {moduleId: moduleId, checkId: check.id} );
        }
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

      if( nextCheck.moduleUrl ){
        var runner = new checkExternal(nextCheck);
      } else {
        var runner = new checkRunner(nextCheck, urlBuilder);
      }
      runner.run(function(result) {
        checkCompleteCallback(nextCheck, result);
        endOfCheckTopic.publish({module: nextCheck.moduleId, check: nextCheck.checkId, result: result});
        runOneCheck();
      });
    };
    startTopic.publish({checksCount: checksCount});
    runOneCheck();
    return checksCount;
  };

  checkScheduler.prototype.availableChecks = null;
  checkScheduler.prototype.urlBuilder = null;
  
  return checkScheduler;
});