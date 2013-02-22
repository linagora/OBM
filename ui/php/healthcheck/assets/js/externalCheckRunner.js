define(["checkResult"], function(checkResult) {
  
  function externalCheckRunner ( flatCheck ) {
    this.flatCheck = flatCheck;
  };

  externalCheckRunner.prototype.run = function(callback) {    
    $.ajax({
      url: this.flatCheck.moduleUrl,
      type: "GET",
      error: function(xhr) {
        var reponseMessage = xhr.status + " " + xhr.statusText ;
        if( xhr.responseText ){
          var html = xhr.responseText;
          var div = document.createElement("div");
          div.innerHTML = html;
          var responseText = div.textContent || div.innerText || "";

          var reponseMessage = reponseMessage + " : <pre class=\"codingMe\">" + responseText + "</pre>";
        }

      	return callback( new checkResult(2, [reponseMessage]) );
      },
      success: function(result) {
      	return callback( new checkResult(0, []) );
      }
    });
  };

  externalCheckRunner.prototype.flatCheck = null;
  externalCheckRunner.prototype.url = null;
  
  return externalCheckRunner;
});