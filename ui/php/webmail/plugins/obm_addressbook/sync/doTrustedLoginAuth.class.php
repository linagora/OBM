<?php
include_once(dirname(__FILE__) . '/obmSyncAuth.php');
include_once(dirname(__FILE__) . '/abstractAuth.class.php');

class doTrustedLoginAuth extends abstractAuth {

  const         TRUSTED_LOGIN_PATH      = "/obm-sync/services/login/trustedLogin";

  protected function getLoginUrl($requester){

    $obm_login = explode("@", $this->login);
    $userInfo = array('login' => $obm_login[0]);
    $token = get_trust_token($userInfo);

    $method = self::TRUSTED_LOGIN_PATH;
    $origin = self::$origin;
    $login = $obm_login[0];
    $password = $token;

    return $requester->getRootPath() . $method . $this->formatLoginParams($origin, $login, $password);
  }

}