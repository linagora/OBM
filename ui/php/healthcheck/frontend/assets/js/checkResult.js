define([], function() {
  
  function checkResult (code, messages) {
    this.code = code;
    this.messages = messages;
  };

  checkResult.prototype.code = null;
  checkResult.prototype.messages = null;
  
  return checkResult;
});