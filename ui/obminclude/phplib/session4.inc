<?php
/*
 * Session Management for PHP4
 *
 * Copyright (c) 1998-2000 NetUSE AG
 *                    Boris Erdmann, Kristian Koehntopp, Barend Scholtus
 *
 * session4.inc,v 0.5 2000/10/29
 *
 */ 

class Session {
  var $classname = "Session";         ## Needed for object serialization.

  ## Define the parameters of your session by either overwriting
  ## these values or by subclassing session (recommended).

//  var $magic = "";              ## Some string you should change.
  var $mode = "";                 ## We propagate session IDs with cookies
  var $cookiename = "";           ## Name of session cookie default : classname
//  var $fallback_mode;           ## If this doesn't work, fall back...

//  var $cookie_domain = "";      ## If set, the domain for which the
                                  ## session cookie is set.

//  var $gc_time  = 2;            ## Purge all session data older than 1440 minutes.
//  var $gc_probability = 1;      ## Garbage collect probability in percent

  var $auto_init = "";            ## Name of the autoinit-File, if any.
  var $secure_auto_init = 1;      ## Set to 0 only, if all pages call
                                  ## page_close() guaranteed.
  var $in = false;                ## Marker: Did we already include the autoinit file?

  var $allowcache = "passive";    ## "passive", "no", "private", "public"
  var $allowcache_expire = 1440;  ## If you allowcache, data expires in this
                                  ## many minutes.
//  var $that_class = "";         ## Name of data storage container
  var $lifetime = 7200;           ## 0 = do session cookies, else minutes

  ##
  ## End of parameters.
  ##

  var $name;                      ## Session name
  var $id;                        ## Unique Session ID
//  var $that;

//  var $pt = array();            ## This Array contains the registered things

  ## register($things):
  ##
  ## call this function to register the things that should become persistent

  function register($things) {
    $things = explode(",",$things);
    reset($things);
    while ( list(,$thing) = each($things) ) {
      $thing=trim($thing);
      if ( $thing ) {
//        $this->pt[$thing] = true;
        session_register($thing);
      }
    }
  }

  function is_registered($name) {
//    if ($this->pt[$name] == true)
    if (session_is_registered($name))
      return true;
    return false;
  }

  function unregister($things) {
    $things = explode(",", $things);
    reset($things);
    while (list(,$thing) = each($things)) {
      $thing = trim($thing);
      if ($thing) {
//        unset($this->pt[$thing]);
        session_unregister($thing);
      }
    }
  }

  ## get_id():
  ##
  ## Propagate the session id according to mode and lifetime.
  ## Will create a new id if necessary. To take over abandoned sessions,
  ## one may provide the new session id as a parameter (not recommended).

  function get_id($id = "") {
    if ("" != $id)
      session_id($id);
  }

  ## put_id():
  ## 
  ## Stop using the current session id (unset cookie, ...) and
  ## abandon a session.
  function put_id() {
  }

  ## delete():
  ##
  ## Delete the current session record and put the session id.
  
  function delete() {
    global $HTTP_COOKIE_VARS;
    session_start();
    session_destroy();
    unset($HTTP_COOKIE_VARS[session_name()]);
    session_start();

  }

  ## url($url):
  ##
  ## Helper function: returns $url concatenated with the current 
  ## session $id.
   
  function url($url){
    $url=ereg_replace("[&?]+$", "", $url);

    switch ($this->mode) {
      case "get":
        if ( strpos($url, urlencode($this->name)) == false ) { 
          $url .= ( strpos($url, "?") != false ?  "&amp;" : "?" ).
                urlencode($this->name)."=".$this->id;
        }
 
      break;
      default:
        ;
      break;
    }
    return $url;
  }

  function purl($url) {
    print $this->url($url);
  }

  function self_url() {
    global $PHP_SELF, $QUERY_STRING;

    return $this->url($PHP_SELF.
           ((isset($QUERY_STRING) && ("" != $QUERY_STRING)) ? "?".$QUERY_STRING : ""));
  }

  function pself_url() {
    print $this->self_url();
  }
  
  function hidden_session()
  {
    printf("<input type=\"hidden\" name=\"%s\" value=\"%s\">\n", session_name(), session_id());
  }

  function add_query($qarray) {
    global $PHP_SELF;
    global $QUERY_STRING;

    if ((isset($QUERY_STRING) && ("" != $QUERY_STRING))
     || ($this->mode == "get")) {
      $sep_char = "&";
    } else {
      $sep_char = "?";
    }

    $qstring = "";
    while (list($k, $v) = each($qarray)) {
      $qstring .= $sep_char . urlencode($k) . "=" . urlencode($v);
      $sep_char = "&";
    }

    return $qstring;
  }

  function padd_query($qarray) {
    print $this->add_query($qarray);
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
  }

  function release_lock() {
  }

  ## freeze():
  ##
  ## freezes all registered things ( scalar variables, arrays, objects ) into
  ## a database table

  function freeze() {
  }

  ## thaw:
  ##
  ## Reload frozen variables from the database and microwave them.

  function thaw() {
  }

  ##
  ## Garbage collection
  ##
  ## Destroy all session data older than this
  ##
  function gc() {
  }

  ##
  ## Variable precedence functions
  ##
  function reimport_get_vars() {
    $this->reimport_any_vars("HTTP_GET_VARS");
  }

  function reimport_post_vars() {
    $this->reimport_any_vars("HTTP_POST_VARS");
  }

  function reimport_cookie_vars() {
    $this->reimport_any_vars("HTTP_COOKIE_VARS");
  }

  function reimport_any_vars($arrayname) {
    global $$arrayname;

    if (!is_array($$arrayname))
      return;

    reset($$arrayname);
    while(list($key, $val) = each($$arrayname)) {
      $GLOBALS[$key] = $val;
    }
  }

  ##
  ## All this is support infrastructure for the start() method
  ##

  function set_container() {
  }

  function set_tokenname(){
  }

  function release_token(){ 
  }   

  function put_headers() {
    # Allowing a limited amount of caching, as suggested by
    # Padraic Renaghan on phplib@lists.netuse.de.
    #
    # Note that in HTTP/1.1 the Cache-Control headers override the Expires
    # headers and HTTP/1.0 ignores headers it does not recognize (e.g,
    # Cache-Control). Mulitple Cache-Control directives are split into 
    # mulitple headers to better support MSIE 4.x.
    #
    # Added pre- and post-check for MSIE 5.x as suggested by R.C.Winters,
    # see http://msdn.microsoft.com/workshop/author/perf/perftips.asp#Use%20Cache-Control%20Extensions
    # for details
    switch ($this->allowcache) {

      case "passive":
        $mod_gmt = gmdate("D, d M Y H:i:s", getlastmod()) . " GMT";                             
        header("Last-Modified: " . $mod_gmt);
        # possibly ie5 needs the pre-check line. This needs testing.
        header("Cache-Control: post-check=0, pre-check=0");
      break;

      case "public":
        $exp_gmt = gmdate("D, d M Y H:i:s", time() + $this->allowcache_expire * 60) . " GMT";
        $mod_gmt = gmdate("D, d M Y H:i:s", getlastmod()) . " GMT";                             
        header("Expires: " . $exp_gmt);
        header("Last-Modified: " . $mod_gmt);
        header("Cache-Control: public");
        header("Cache-Control: max-age=" . $this->allowcache_expire * 60);
      break;
 
      case "private":
        $mod_gmt = gmdate("D, d M Y H:i:s", getlastmod()) . " GMT";
        header("Expires: Mon, 26 Jul 1997 05:00:00 GMT");
        header("Last-Modified: " . $mod_gmt);
        header("Cache-Control: private");
        header("Cache-Control: max-age=" . $this->allowcache_expire * 60);
        header("Cache-Control: pre-check=" . $this->allowcache_expire * 60);
      break;

      default:
        header("Expires: Mon, 26 Jul 1997 05:00:00 GMT");
        header("Last-Modified: " . gmdate("D, d M Y H:i:s") . " GMT");
        header("Cache-Control: no-cache");
        header("Cache-Control: post-check=0, pre-check=0");
        header("Pragma: no-cache");
      break;
    }
  }

  ##
  ## Garbage collection
  ##
  ## Destroy all session data older than this
  ##
  function gc() {
  }

  ##
  ## Initialization
  ##

  /////////////////////////////////////////////////////////////////////////////
  // Set the OBM cookie                                                      //
  /////////////////////////////////////////////////////////////////////////////
  function setcookie() {
    global $lifetime, $session_cookie;

    if ($session_cookie) {
      setcookie(session_name(), session_id(), 0, "/");
    } else {
      if ($lifetime == 0) {
        $lifetime = $this->lifetime_default;
      }
      // Use setcookie() as session_set_cookie_params() doesn't seem to work!
      setcookie(session_name(), session_id(), time()+$lifetime, "/");
      //   session_set_cookie_params($lifetime);
    }
  }

  function start($sid = "") {
    global $HTTP_COOKIE_VARS;

    $this->put_headers();
    $this->get_id($sid);
    $this->name = $this->cookiename==""?$this->classname:$this->cookiename;
    session_name($this->name);
    $HTTP_COOKIE_VARS[$this->name] = "OBM_Session";
    if (isset($HTTP_COOKIE_VARS[$this->name])) {
      $this->mode = "cookie";
    } else {
      $this->mode = "get";
    }
    session_start();
    $this->id = session_id();

   }
}
?>
