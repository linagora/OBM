define([], function() {

  var progressBar = function() {
  }
  progressBar.prototype.elementsCount = null;
  progressBar.prototype.elementsDone = null;
  progressBar.prototype.setElementCount = function(elementsCount) {
    this.elementsCount = elementsCount;
  };
  progressBar.prototype.increment = function() {
    if ( ! this.elementsCount ) {
      return ;
    }
    var percent = (++this.elementsDone / this.elementsCount) * 100;
    $("#progress-bar").css('width', percent+"%"); 
  }

  return progressBar;
});