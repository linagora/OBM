define([], function() {

  var badgeName = {"1" : "#badge-warnings", "2" : "#badge-errors" };
  
  var increment = function(code) {
    if ( !(code in badgeName) ) {
      return ;
    }
    setCount(code, (getCount(code)+1));
  };

  var decrement = function(code) {
    if ( !(code in badgeName) ) {
      return ;
    }
    var count = getCount(code);
    count--;
    if ( count < 0 ) {
      return ;
    }
    setCount(code, count);
  };
  
  var setCount = function(code, count) {
    if ( !(code in badgeName) ) {
      return ;
    }
    return $( badgeName[code] ).text(count);
  };
  
  var getCount = function(code) {
    if ( !(code in badgeName) ) {
      return ;
    }
    return parseInt($( badgeName[code] ).text(), 10);
  };
  
  var getErrorCount = function() {
    return getCount(2);
  };
  
  var getWarningCount = function() {
    return getCount(1);
  };
  
  var decrementError = function() {
    return decrement(2);
  };
  
  var decrementWarning = function() {
    return decrement(1);
  };
  
  return {
    increment: increment,
    decrement: decrement,
    getCount: getCount,
    setCount: setCount,
    getErrorCount: getErrorCount,
    getWarningCount: getWarningCount,
    decrementError: decrementError,
    decrementWarning: decrementWarning
  };
  
});
