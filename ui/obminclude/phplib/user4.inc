<?php
/*
 * Session Management for PHP4
 *
 * Copyright (c) 1998-2000 NetUSE AG
 *                    Boris Erdmann, Kristian Koehntopp, Barend Scholtus
 *
 * user4.inc,v 0.5 2000/10/29
 *
 */ 

class User {
  var $classname  = "User";         ## Needed for object serialization.
  var $that_class = "Session_sql";  ## Name of data storage container

  ##
  ## End of parameters.
  ##

  var $name;                          ## Session name
  var $id;                            ## Unique Session ID

  var $pt = array();                  ## This Array contains the registered things
  var $that;

  ## register($things):
  ##
  ## call this function to register the things that should become persistent

  function register($things) {
    $things = explode(",",$things);
    reset($things);
    while ( list(,$thing) = each($things) ) {
      $thing=trim($thing);
      if ( $thing ) {
        $this->pt[$thing] = true;
      }
    }
  }

  function is_registered($name) {
    if ($this->pt[$name] == true)
      return true;
    return false;
  }

  function unregister($things) {
    $things = explode(",", $things);
    reset($things);
    while (list(,$thing) = each($things)) {
      $thing = trim($thing);
      if ($thing) {
        unset($this->pt[$thing]);
      }
    }
  }

  ## get_id():
  ##
  ## Propagate the session id according to mode and lifetime.
  ## Will create a new id if necessary. To take over abandoned sessions,
  ## one may provide the new session id as a parameter (not recommended).
  function get_id($id = "") {
    $this->id = $id;
  }
  
  ## put_id():
  ## 
  ## Stop using the current session id (unset cookie, ...) and
  ## abandon a session.
  function put_id() {
    ;
  }

  ## serialize($prefix,&$str):
  ##
  ## appends a serialized representation of $$prefix
  ## at the end of $str.
  ##
  ## To be able to serialize an object, the object must implement
  ## a variable $classname (containing the name of the class as string)
  ## and a variable $persistent_slots (containing the names of the slots
  ## to be saved as an array of strings).
  ##
  ## You don't need to know...
  function serialize($prefix, $str) {
    static $t,$l,$k;

    ## Determine the type of $$prefix
    eval("\$t = gettype(\$$prefix);");
    switch ( $t ) {

      case "array":
        ## $$prefix is an array. Enumerate the elements and serialize them.
        eval("reset(\$$prefix); \$l = gettype(list(\$k)=each(\$$prefix));");
        $str .= "\$$prefix = array(); ";
        while ( "array" == $l ) {
          ## Structural recursion
          $this->serialize($prefix."['".ereg_replace("([\\'])", "\\\\1", $k)."']", &$str);
          eval("\$l = gettype(list(\$k)=each(\$$prefix));");
        }

      break;
      case "object":
        ## $$prefix is an object. Enumerate the slots and serialize them.
        eval("\$k = \$${prefix}->classname; \$l = reset(\$${prefix}->persistent_slots);");
        $str.="\$$prefix = new $k; ";
        while ( $l ) {
          ## Structural recursion.
          $this->serialize($prefix."->".$l,&$str);
          eval("\$l = next(\$${prefix}->persistent_slots);");
        }

      break;
      default:
        ## $$prefix is an atom. Extract it to $l, then generate code.
        eval("\$l = \$$prefix;");
        $str.="\$$prefix = '".ereg_replace("([\\'])", "\\\\1", $l)."'; ";


      break;
    }
  }

  function get_lock() {
      $this->that->ac_get_lock();
  }

  function release_lock() {
      $this->that->ac_release_lock();
  }

  ## freeze():
  ##
  ## freezes all registered things ( scalar variables, arrays, objects ) into
  ## a database table

  function freeze() {
    $str="";

    $this->serialize("this->in",&$str);
    $this->serialize("this->pt",&$str);

    reset($this->pt);
    while ( list($thing) = each($this->pt) ) {
      $thing=trim($thing);
      if ( $thing ) {
        $this->serialize("GLOBALS['".$thing."']",&$str);
      }
    }
    
    $r = $this->that->ac_store($this->id, $this->name, $str);
    $this->release_lock();
    if(!$r) $this->that->ac_halt("Session: freeze() failed.");
  }

  ## thaw:
  ##
  ## Reload frozen variables from the database and microwave them.       

  function thaw() {
    $this->get_lock();

    $vals = $this->that->ac_get_value($this->id, $this->name);         

    eval(sprintf(";%s",$vals));
  } 

  ##
  ## Initialization
  ##

  function start($sid = "") {
    $this->get_id($sid);
	
    if(!isset($this->cookiename)) {
      $this->cookiename="";
    };

    $this->name = $this->cookiename == "" ? $this->classname : $this->cookiename;
    $name = $this->that_class;
    $this->that = new $name;
    $this->that->ac_start();
 
    $this->thaw();
  }
}
?>
