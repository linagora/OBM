<?php
if ($obminclude == "") $obminclude = "obminclude";
include_once("$obminclude/CAS/CAS.php");	
$cas_version = CAS_VERSION_2_0;

class obmCASClient extends CASClient {
  
  
  function obmCASClient($server_version,
		     $proxy,
		     $server_hostname,
		     $server_port,
		     $server_uri)
    {
      global $cas_version;
      phpCAS::traceBegin();
      
      // activate session mechanism
      session_start();

      $this->_proxy = $proxy;
      if($server_version == "") 
        $server_version = $cas_version;
      // check version
      switch ($server_version) {
      case CAS_VERSION_1_0:
	if ( $this->isProxy() )
	  phpCAS::error('CAS proxies are not supported in CAS '
			.$server_version);
	break;
      case CAS_VERSION_2_0:
	break;
      default:
	phpCAS::error('this version of CAS (`'
		      .$server_version
		      .'\') is not supported by phpCAS '
			.phpCAS::getVersion());
      }
      $this->_server['version'] = $server_version;

      // check hostname
      if ( empty($server_hostname) 
	   || !preg_match('/[\.\d\-abcdefghijklmnopqrstuvwxyz]*/',$server_hostname) ) {
	phpCAS::error('bad CAS server hostname (`'.$server_hostname.'\')');
      }
      $this->_server['hostname'] = $server_hostname;

      // check port
     /* if ( $server_port == 0 
	   || !is_int($server_port) ) {
	phpCAS::error('bad CAS server port (`'.$server_hostname.'\')');
      }*/
      $this->_server['port'] = $server_port;

      // check URI
      if ( !preg_match('/[\.\d\-_abcdefghijklmnopqrstuvwxyz\/]*/',$server_uri) ) {
	phpCAS::error('bad CAS server URI (`'.$server_uri.'\')');
      }
      // add leading and trailing `/' and remove doubles      
      $server_uri = preg_replace('/\/\//','/','/'.$server_uri.'/');
      $this->_server['uri'] = $server_uri;

      // set to callback mode if PgtIou and PgtId CGI GET parameters are provided 
      if ( $this->isProxy() ) {
	$this->setCallbackMode(!empty($_GET['pgtIou'])&&!empty($_GET['pgtId']));
      }

      if ( $this->isCallbackMode() ) {
	// callback mode: check that phpCAS is secured
	if ( $_SERVER['HTTPS'] != 'on' ) {
	  phpCAS::error('CAS proxies must be secured to use phpCAS; PGT\'s will not be received from the CAS server');
	}
      } else {
	// normal mode: get ticket and remove it from CGI parameters for developpers
	$ticket = $_GET['ticket'];
	// at first check for a Service Ticket
	if( preg_match('/^ST-/',$ticket)) {
	  phpCAS::trace('ST \''.$ticket.'\' found');
	  // ST present
	  $this->setST($ticket);
	} 
	// in a second time check for a Proxy Ticket (CAS >= 2.0)
	else if( ($this->getServerVersion()!=CAS_VERSION_1_0) && preg_match('/^PT-/',$ticket) ) {
	  phpCAS::trace('PT \''.$ticket.'\' found');
	  $this->setPT($ticket);
	} 
	// ill-formed ticket, halt
	else if ( !empty($ticket) ) {
	  phpCAS::error('ill-formed ticket found in the URL (ticket=`'.htmlentities($ticket).'\')');
	}
	// ticket has been taken into account, unset it to hide it to applications
	unset($_GET['ticket']);
      }
      phpCAS::traceEnd();
    }


    function getURL()
    {
      phpCAS::traceBegin();
      // the URL is built when needed only
      if ( empty($this->_url) ) {
        
	if(empty($_SERVER['HTTP_X_FORWARDED_SERVER'])){
	  
	  $this->_url = ($_SERVER['HTTPS'] == 'on') ? 'https' : 'http';
	  $this->_url .= '://';
          
	  if (empty($_SERVER['SERVER_NAME'])) {
            $this->_url .= $_SERVER['HTTP_HOST'];
          } else {
            $this->_url .= $_SERVER['SERVER_NAME'];
          }
        
	} else {
	  
	  $this->_url = ($_SERVER['x-aliacom-proxy-protocol'] == "HAVE_x-aliacom-proxy-protocol") ? 'https' : 'http';
	  $this->_url .= '://';

          $this->_url .= obmCAS::getFirstForwarder($$_SERVER['HTTP_X_FORWARDED_SERVER']);
        }
	if ( ($_SERVER['HTTPS']=='on' && $_SERVER['SERVER_PORT']!=443)
	     || ($_SERVER['HTTPS']!='on' && $_SERVER['SERVER_PORT']!=80) ) {
	  $this->_url .= ':';
	  $this->_url .= $_SERVER['SERVER_PORT'];
	}
	$this->_url .= strtok($_SERVER['REQUEST_URI'],"?");
	$cgi_params = '?'.strtok("?");
	// remove the ticket if present in the CGI parameters
	$cgi_params = preg_replace('/&ticket=[^&]*/','',$cgi_params);
	$cgi_params = preg_replace('/\?ticket=[^&]*/','?',$cgi_params);
	$cgi_params = preg_replace('/\?$/','',$cgi_params);
	$this->_url .= $cgi_params;
      }
      phpCAS::traceEnd($this->_url);
      return $this->_url;
    }
    
    function getServerBaseURL() { 
      // the URL is build only when needed
      if ( empty($this->_server['base_url']) ) {
	if(obmCAS::isSecure()) {
	  $this->_server['base_url'] = 'https://';
	} else {
          $this->_server['base_url'] = 'http://';
	}
	$this->_server['base_url'] .= $this->getServerHostname();
	if ($this->getServerPort()) {
	  $this->_server['base_url'] .= ':';
	  $this->_server['base_url'] .= $this->getServerPort();
	}	  
	  $this->_server['base_url'] .= $this->getServerURI();
      }
      return $this->_server['base_url']; 
    }
}



class obmCAS extends phpCAS {

  function client($server_version,
		  $server_hostname,
		  $server_port,
		  $server_uri)
    {
      global $PHPCAS_CLIENT, $PHPCAS_INIT_CALL;

      phpCAS::traceBegin();
      if ( is_object($PHPCAS_CLIENT) ) {
	phpCAS::error($PHPCAS_INIT_CALL['method'].'() has already been called (at '.$PHPCAS_INIT_CALL['file'].':'.$PHPCAS_INIT_CALL['line'].')');
      }
      if ( gettype($server_version) != 'string' ) {
	phpCAS::error('type mismatched for parameter $server_version (should be `string\')');
      }
      if ( gettype($server_hostname) != 'string' ) {
        $server_hostname = obmCAS::getHost();
      }
      if ( gettype($server_uri) != 'string' ) {
	phpCAS::error('type mismatched for parameter $server_uri (should be `string\')');
      }

      // store where the initialzer is called from
      $dbg = phpCAS::backtrace();
      $PHPCAS_INIT_CALL = array('done' => TRUE,
				'file' => $dbg[0]['file'],
				'line' => $dbg[0]['line'],
				'method' => __CLASS__.'::'.__FUNCTION__);

      // initialize the global object $PHPCAS_CLIENT
      $PHPCAS_CLIENT = new obmCASClient($server_version,FALSE/*proxy*/,$server_hostname,$server_port,$server_uri);
      phpCAS::traceEnd();
    }

    function getHost() {
      if(empty($_SERVER['HTTP_X_FORWARDED_SERVER'])){
	if (empty($_SERVER['SERVER_NAME'])) {
	  $url .= $_SERVER['HTTP_HOST'];
	} else {
	  $url .= $_SERVER['SERVER_NAME'];
	}
      } else {
	$url .= obmCAS::getFirstForwarder($_SERVER['HTTP_X_FORWARDED_SERVER']);
      }
      return $url;
    }

    function getFirstForwarder() {
      $data = explode(",",$_SERVER['HTTP_X_FORWARDED_SERVER']);
      return $data[0];
    }   
    
    function isSecure() {
      return true;
    }
}
?>
