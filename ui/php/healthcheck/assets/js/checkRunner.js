define(["checkResult"], function(checkResult) {

  function checkRunner ( flatCheck, urlBuilder ) {
    this.flatCheck = flatCheck;
    this.urlBuilder = urlBuilder
  };

  checkRunner.prototype.run = function(callback) {
    
    $.ajax({
      url: this.urlBuilder(this.flatCheck),
      type: "GET",
      error: function(xhr) {
        return callback(new checkResult(2, xhr.statusText));
      },
      success: function(result) {
        if ($.isPlainObject(result)) {
          return callback(new checkResult(result.status, result.messages));
        } else {
          return callback(new checkResult(2, ["Server response is not in expected format"]));
        }
      }
    });
    
  };

  checkRunner.prototype.urlBuilder = null;
  checkRunner.prototype.flatCheck = null;
  
  return checkRunner;
});