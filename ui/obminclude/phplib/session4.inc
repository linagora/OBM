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

  var $mode = "";                 ## We propagate session IDs with cookies
  var $cookiename = "";           ## Name of session cookie default : classname

//  var $cookie_domain = "";      ## If set, the domain for which the
                                  ## session cookie is set.
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


  ## register($things):
  ##
  ## call this function to register the things that should become persistent

  function register($things) {
    $things = explode(",",$things);
    reset($things);
    while ( list(,$thing) = each($things) ) {
      $thing=trim($thing);
      if ( $thing ) {
        session_register($thing);
      }
    }
  }

  function is_registered($name) {
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


  ##
  ## All this is support infrastructure for the start() method
  ##

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
