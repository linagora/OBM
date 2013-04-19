<?php
include_once(dirname(__FILE__) . '/obmSyncAuth.php');
include_once(dirname(__FILE__) . '/abstractAuth.class.php');

class doLoginAuth extends abstractAuth {

  const         LOGIN_PATH              = "/obm-sync/services/login/doLogin";

  protected function getLoginUrl($requester){
    $loginAuthParameters = "?origin=".urlencode(self::$origin);
    if($this->authKind == "standalone"){
      if(!$this->login){
        throw new Exception("For a standalone authentication, you must set the login and password in obmSyncRequester", 500);
      }
    }
    if ( $this->login ) {
      $loginAuthParameters .= "&login=".urlencode($this->login);
    }
    if($this->authKind != "LemonLDAP" && $this->password ){
      $loginAuthParameters .= "&password=".urlencode($this->password);
    }
    return $requester->getRootPath() . self::LOGIN_PATH . $loginAuthParameters;
  }

}