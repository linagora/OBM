define([], function() {

  var progressBar = function(elementsCount) {
    this.elementsCount = elementsCount;
  }
  progressBar.prototype.elementsCount = null;
  progressBar.prototype.elementsDone = null;
  progressBar.prototype.increment = function() {
    var percent = (++this.elementsDone / this.elementsCount) * 100;
    $("#progress-bar").css('width', percent+"%"); 
  }

  return progressBar;
});