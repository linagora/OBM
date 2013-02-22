define([], function() {
  
  var modules = {};
  
  var frontEndModule = function(id) {
    var widget = $("#"+id+"-header");
    var checksWidgets = widget.children(".accordion-body").children().children().children();
    this.setStatus = function(status) {
      widget.children(".accordion-heading").removeClass("test-info test-warning test-error test-success").addClass("test-"+status);
    };
    
    this.updateWidgetFromChecksStatus = function() {
      var warnings = checksWidgets.find(".test-warning").length;
      var errors = checksWidgets.find(".test-error").length;
      if( errors ) {
        this.setStatus("error");
      } else if( warnings ) {
        this.setStatus("warning");
      } else {
        this.setStatus("success");
        this.closeContainer();
      }
    };
    
    this.closeContainer = function() {
      $("#"+id+"-inner").removeClass("in");
    };
  };
  
  
  
  
  
  
  
  
  
  
  
  var module = function(id) {
    if ( id in modules ) {
      return modules[id];
    }
    modules[id] = new frontEndModule(id);
    return modules[id];
  };
  
  
  return {
    module: module
  };
});