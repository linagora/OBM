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
/*
    global $HTTP_COOKIE_VARS, $HTTP_GET_VARS, $HTTP_POST_VARS, $QUERY_STRING;
    $newid=true;
 
    $this->name = $this->cookiename==""?$this->classname:$this->cookiename;
 
    if ( "" == $id ) {
      $newid=false;
      switch ($this->mode) {
        case "get":
          if ("" == ($id = isset($HTTP_GET_VARS[$this->name]) ? $HTTP_GET_VARS[$this->name] : ""))
            $id = isset($HTTP_POST_VARS[$this->name]) ? $HTTP_POST_VARS[$this->name] : "";
        break;
        case "cookie":
          $id = isset($HTTP_COOKIE_VARS[$this->name]) ? $HTTP_COOKIE_VARS[$this->name] : "";
        break;
        default:
          die("This has not been coded yet.");
        break;
      }
    }
 
    if ( "" == $id ) {
      $newid=true;
      $id = $this->that->ac_newid(md5(uniqid($this->magic)), $this->name);
    }
 
    switch ($this->mode) {
      case "cookie":
        if ( $newid && ( 0 == $this->lifetime ) ) {
          SetCookie($this->name, $id, 0, "/", $this->cookie_domain);
        }
        if ( 0 < $this->lifetime ) {
          SetCookie($this->name, $id, time()+$this->lifetime*60, "/", $this->cookie_domain);
        }
      break;
      case "get":
        if ( isset($QUERY_STRING) ) {
          $QUERY_STRING = ereg_replace(
            "(^|&)".quotemeta(urlencode($this->name))."=".$id."(&|$)",
            "\\1", $QUERY_STRING);
        }
      break;
      default:
        ;
      break;
    }
 
    $this->id = $id;
*/
  }

  ## put_id():
  ## 
  ## Stop using the current session id (unset cookie, ...) and
  ## abandon a session.
  function put_id() {
/*
    global $HTTP_COOKIE_VARS;
 
    $this->name = $this->cookiename==""?$this->classname:$this->cookiename;
 
    switch ($this->mode) {
      case "inline":
        die("This has not been coded yet.");
      break;
 
      case "get":
        die("This has not been coded yet.");
      break;
 
      default:
            SetCookie($this->name, "", 0, "/", $this->cookie_domain);
            $HTTP_COOKIE_VARS[$this->name] = "";
      break;
    }
*/
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


/*
      $this->that->ac_delete($this->id, $this->name);
      $this->put_id();
*/
  }

  ## url($url):
  ##
  ## Helper function: returns $url concatenated with the current 
  ## session $id.
   
  function url($url){
    $url=ereg_replace("[&?]+$", "", $url);
//A VIRER
    $this->mode = "get";
// A VIRER
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
//    printf("<input type=\"hidden\" name=\"%s\" value=\"%s\">\n", $this->name, $this->id);
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
//      $this->that->ac_get_lock();
  }

  function release_lock() {
//      $this->that->ac_release_lock();
  }

  ## freeze():
  ##
  ## freezes all registered things ( scalar variables, arrays, objects ) into
  ## a database table

  function freeze() {
/*
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
    echo "amjkfmodfkmsldfklzeeoprfdfksdlmfjzerjbhgrjkehterké";    
    $r = $this->that->ac_store($this->id, $this->name, $str);
    $this->release_lock();
    if(!$r) $this->that->ac_halt("Session: freeze() failed.");
*/
  }

  ## thaw:
  ##
  ## Reload frozen variables from the database and microwave them.

  function thaw() {
/*
    $this->get_lock();

    $vals = $this->that->ac_get_value($this->id, $this->name);

    eval(sprintf(";%s",$vals));
*/
  }

  ##
  ## Garbage collection
  ##
  ## Destroy all session data older than this
  ##
  function gc() {
//      $this->that->ac_gc($this->gc_time, $this->name);
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
/*
    $name = $this->that_class;
    $this->that = new $name;

    $this->that->ac_start();
*/
  }

  function set_tokenname(){
//    $this->name = $this->cookiename==""?$this->classname:$this->cookiename;
  }

  function release_token(){ 
/*
    global $HTTP_COOKIE_VARS, $HTTP_GET_VARS, $HTTP_HOST, $HTTPS;
    if (   isset($this->fallback_mode)
    && ( "get" == $this->fallback_mode ) 
    && ( "cookie" == $this->mode )
    && ( ! isset($HTTP_COOKIE_VARS[$this->name]) ) ) {
      if ( isset($HTTP_GET_VARS[$this->name]) ) {
        $this->mode = $this->fallback_mode;
      } else {
        header("Status: 302 Moved Temporarily");
        $this->get_id($sid);
	$this->mode = $this->fallback_mode;
        if( isset($HTTPS) && $HTTPS == 'on' ){
        ## You will need to fix suexec as well, if you use Apache and CGI PHP
          $PROTOCOL='https';
        } else {
          $PROTOCOL='http';
        }
        header("Location: ". $PROTOCOL. "://".$HTTP_HOST.$this->self_url());
        exit;
      }
    }
*/
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
/*
    srand(time());
    if ((rand()%100) < $this->gc_probability) {
      $this->that->ac_gc($this->gc_time, $this->name);
    }
*/
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
     if ($lifetime == 0)
        $lifetime = $this->lifetime_default;
     // Use setcookie() as session_set_cookie_params() doesn't seem to work!
     setcookie(session_name(), session_id(), time()+$lifetime, "/");
   //   session_set_cookie_params($lifetime);
    }
  }

  function start($sid = "") {
    global $HTTP_COOKIE_VARS;
//    $this->set_container();
//    $this->set_tokenname(); 
//    $this->release_token($sid);
    $this->put_headers();
    $this->get_id($sid);
//    $this->thaw();
//    $this->gc();
    $this->name = $this->cookiename==""?$this->classname:$this->cookiename;
    session_name($this->name);
    $HTTP_COOKIE_VARS[$this->name] = "OBM_Session";
    if (isset($HTTP_COOKIE_VARS[$this->name])) {
      $this->mode = "cookie";
    } else {
      $this->mode = "get";
    }
// A VIRER les COMMENTAIRE   session_start();
    $this->id = session_id();
   }
}
?>
