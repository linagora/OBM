<?php
include_once dirname(__FILE__).'/../httpRequester.class.php';
require_once(dirname(__FILE__) . '/../obmServiceRequester.class.php');
include_once(dirname(__FILE__) . '/obmSyncAuth.php');


/**
 * This class handles requests to obm-sync services
 * To implement a service callable on obm-sync, the name of the service must be
 * added to the implementedServices property and a method must be implemented.
 *
 * For a service named "foo", a method getFooUrl() must be implemented and "foo"
 * must be added to the implementedServices array.
 *
 * You can implement a getFooHttpHeaders() returning an array of headers if some
 * specific headers need to be sent for the foo webservice.
 *
 * You can implement a getFooPostParameters() returning an array of parameters
 * if some specific parameters need to be sent for the foo webservice.
 *
 * You can implement a getErrorMessageForFoo() to determine if there is an error
 * depending on the xml response. If not implemented, an error message will only
 * occur if there is an error tag in xml response to webservice or if there is
 * no response.
 */
abstract class abstractAuth implements obmSyncAuth {

  protected       $authKind     = null;
  protected       $login;
  protected       $password;
  protected       $domainName;

  public static $origin         = "obm-ui";

  public function __construct($login, $password, $domainName, $authKind){
    $this->login = $login;
    $this->password = $password;
    $this->domainName = $domainName;
    $this->authKind = $authKind;
  }

  /**
   * Determines which headers should be set depending on the authentification type
   *
   * @return array headers to set for obm-sync login
   */
  private function getLoginHttpHeaders(){
    if($this->authKind == "LemonLDAP"){
      if ( !$this->domainName ) {
        $loginSplit = explode("@",$this->login);
        if ( count($loginSplit) == 2 ) {
          $this->login = $loginSplit[0];
          $this->domainName = $loginSplit[1];
        }
      }
      if(!$this->login || !$this->domainName){
        throw new Exception("You must set login and domain name before trying to log in with LemonLDAP", 500);
      }
      return array("obm_uid" => $this->login, "obm_domain" => $this->domainName);
    }
    return array();
  }

  /**
   * Calculates obm-sync login url
   * Must not return an empty string in any case. An exception must be thrown if
   * an element is missing
   * 
   * @return string obm-sync login webservice url
   */
  protected abstract function getLoginUrl($requester);

  /**
   * Executes obmSync login and gets a session id. Must be executed before
   * calling a service from obm-sync.
   * 
   * @return boolean True if everything went well, false otherwise
   */
  public function logIn($obmSyncRequester){
    try {
      $loginUrl = $this->getLoginUrl($obmSyncRequester);
      $response = $obmSyncRequester->getHttpRequester()
                  ->executeHTTPRequest(
                    $loginUrl,
                    $this->getLoginHttpHeaders(),
                    array("httpMethod" => "GET")
                  );
      if($response === false){
        $this->errors[] = "Couldn't login to obm-sync";
        throw new Exception("No response from login request", 500);
      }

      // XML parsing in order to find some eventual errors
      $xmlLogin = new DomDocument();
      $xmlLogin->loadXML($response);
      $errors = $xmlLogin->getElementsByTagName("error");
      if($errors->length > 0){
        $this->errors[] = "Couldn't login to obm-sync (".$errors->item(0)->nodeValue.")";
        throw new Exception("An error occured at login request", 500);
      }
      if($xmlLogin->getElementsByTagName('sid')->item(0) == null){
        $this->errors[] = "Couldn't login to obm-sync (no session id returned)";
        throw new Exception("An error occured at login request", 500);
      }
    } catch (Exception $exc) {
      error_log($exc); 
      return false;
    }

    // Everything went well : user is logged in and has a session id
    $this->sid = $xmlLogin->getElementsByTagName('sid')
                  ->item(0)
                  ->nodeValue;
    return $this->sid;
  }

}