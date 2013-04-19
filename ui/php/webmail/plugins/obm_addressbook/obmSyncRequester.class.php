<?php
include_once dirname(__FILE__).'/httpRequester.class.php';
require_once(dirname(__FILE__) . '/obmServiceRequester.class.php');
require_once(dirname(__FILE__) . '/sync/doLoginAuth.class.php');
require_once(dirname(__FILE__) . '/sync/doTrustedLoginAuth.class.php');

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
class obmSyncRequester extends obmServiceRequester{
  private       $sid                    = false;
  private       $authKind               = null;
  private       $auth;
  private       $login;
  private       $password;
  private       $domainName;
  private       $implementedServices    =  array(
                                            "login"=>"GET",
                                            "importICalendar"=>"POST",
                                            "createContact"=>"POST",
                                            "updateContact"=>"POST",
                                            "deleteContact"=>"GET",
                                            "listAllBooks"=>"GET",
                                            "searchContactInGroup"=>"GET",
                                            "countContactsInGroup"=>"GET",
                                            "getContactFromId"=>"GET"
                                           );
  private       $authorizedHttpMethods  = array("GET", "POST");
  public static $rootPathScheme         = "http://%s:8080";
  public static $port                   = "8080";
  public static $userEmail;
  const         LOCATION_PATH           = "/location/host/sync/obm_sync/";
  private       $simpleFields           = array(
                                            'name'          => 'commonname',
                                            'firstname'     => 'first',
                                            'surname'       => 'last',
                                            'nickname'      => 'aka',
                                            'jobtitle'      => 'title',
                                            'organization'  => 'company',
                                            'department'    => 'service',
                                            'manager'       => 'manager',
                                            'assistant'     => 'assistant',
                                            'spouse'        => 'spouse',
                                            'notes'         => 'comment',
                                          );

  private       $multipleFields         = array(
                                            'email'         => array(
                                                                  'nodeName'  => 'emails',
                                                                  'itemName'  => 'mail',
                                                                  'labels'    => array(
                                                                                    'home'  => 'INTERNET',
                                                                                    'work'  => 'INTERNET',
                                                                                    'other' => 'OTHER'
                                                                                  )
                                                                ),
                                            'phone'         => array(
                                                                  'nodeName'  => 'phones',
                                                                  'itemName'  => 'phone',
                                                                  'labels'    => array(
                                                                                    'home'    => 'HOME;VOICE',
                                                                                    'home2'   => 'HOME;VOICE',
                                                                                    'work'    => 'WORK;VOICE',
                                                                                    'work2'   => 'WORK;VOICE',
                                                                                    'mobile'  => 'CELL;VOICE',
                                                                                    'main'    => 'WORK;VOICE',
                                                                                    'homefax' => 'HOME;FAX',
                                                                                    'workfax' => 'WORK;FAX',
                                                                                    'pager'   => 'PAGER',
                                                                                    'car'     => 'OTHER',
                                                                                    'assistant' => 'OTHER',
                                                                                    'video'   => 'OTHER',
                                                                                    'other'   => 'OTHER'
                                                                                  )
                                                                ),
                                            'address'       => array(
                                                                  'nodeName'  => 'addresses',
                                                                  'itemName'  => 'address',
                                                                  'labels'    => array(
                                                                                    'home'  => 'HOME',
                                                                                    'work'  => 'WORK',
                                                                                    'other' => 'OTHER'
                                                                                  )
                                                                ),
                                            'website'       => array(
                                                                  'nodeName'  => 'websites',
                                                                  'itemName'  => 'site',
                                                                  'labels'    => array(
                                                                                    'homepage'  => 'URL',
                                                                                    'blog'      => 'BLOG',
                                                                                    'other'     => 'OTHER',
                                                                                    'work'      => 'WORK'
                                                                                  )
                                                                ),
                                            'im'            => array(
                                                                  'nodeName'  => 'instantmessaging',
                                                                  'itemName'  => 'im',
                                                                  'labels'    => array(
                                                                                    'aim'   => 'AIM',
                                                                                    'icq'   => 'X_ICQ',
                                                                                    'msn'   => 'MSN',
                                                                                    'yahoo' => 'YMSGR',
                                                                                    'jabber'=> 'XMPP',
                                                                                    'other' => 'OTHER'
                                                                                  )
                                                                ),
                                        );
  private $sequences = array();

  /**
   * Initializes mandatory properties for services calls
   *
   * @param HTTPRequester     HttpRequester which will be responsible of doing
   *                          HTTP requests to obm-sync
   * @param string $authKind  Authentification method
   * @param int $serverIp     (optionnal) Server ip where to call services
   */
  public function __construct(HTTPRequester $httpRequester,
                              $authKind,
                              $serverIp = null,
                              $user,
                              $pass,
                              $domain
                              ) {
    parent::__construct($httpRequester, $serverIp, self::$userEmail);
    $this->authKind = $authKind;
    if(!$this->serverIp){
      throw new Exception("The IP of the obm-sync is null", 500);
    }

    if ( $authKind == 'trust' ) {
      if ( function_exists('get_trust_token') ) {
        $this->auth = new doTrustedLoginAuth($user, null, $domain, $authKind);
      } else{
        throw new Exception("Unable to do trusted Authentification, only unified UI (OBM 3.0.0) can do this.", 500);
      }
    } else {
      $this->auth = new doLoginAuth($user, $pass, $domain, $authKind);
    }
  }

  /**
   * Calculates the name of the method to call to get the service url
   * @param  string $serviceName
   * @return string the method to call
   */
  private function getServiceUrlMethod($serviceName){
    return "get".ucfirst($serviceName)."Url";
  }

  protected function getImportICalendarUrl(){
    $insertPath = "/obm-sync/services/calendar/importICalendar";
    $insertUrl = $this->rootPath.$insertPath."?sid=".$this->sid."&calendar=".$this->login;

    return $insertUrl;
  }
  
  protected function getCreateContactUrl($contact){
    $createContactPath = "/obm-sync/services/book/createContactInBook";
    $createContactUrl = $this->rootPath.$createContactPath."?sid=".$this->sid."&bookId=".preg_replace("/^OBM/","",$contact["source"]);
//     echo $createContactUrl;
    return $createContactUrl;
  }

  protected function getUpdateContactUrl($contact){
    $path = "/obm-sync/services/book/modifyContact";
    $url = $this->rootPath.$path."?sid=".$this->sid."&book=contacts";//.preg_replace("/^OBM/","",$contact["source"]);
    return $url;
  }

  protected function getDeleteContactUrl($contact){
    $path = "/obm-sync/services/book/removeContact";
    $url = $this->rootPath.$path."?sid=".$this->sid."&book=contacts"/*.preg_replace("/^OBM/","",$contact["source"])*/."&id=".$contact["id"];
    return $url;
  }

  protected function getListAllBooksUrl(){
    $listAllBooksPath = "/obm-sync/services/book/listAllBooks";
    $listAllBooksUrl = $this->rootPath.$listAllBooksPath."?sid=".$this->sid;

    return $listAllBooksUrl;
  }

  protected function getSearchContactInGroupUrl($groupId, $query, $limit=200, $offset=0){

    $searchContactPath = "/obm-sync/services/book/searchContactInGroup";
    $searchContactUrl = $this->rootPath.$searchContactPath."?sid=".$this->sid;
    $searchContactUrl .= "&query=".$query."&group=".$groupId."&limit=".$limit."&offset=".$offset;

    return $searchContactUrl;
  }

  protected function getCountContactsInGroupUrl($groupId){

    $countContactPath = "/obm-sync/services/book/countContactsInGroup";
    $countContactUrl = $this->rootPath.$countContactPath."?sid=".$this->sid;
    $countContactUrl .= "&group=".$groupId;

    return $countContactUrl;
  }

  protected function getGetContactFromIdUrl($bookId, $contactId){
    $getContactPath = "/obm-sync/services/book/getContactFromId";
    $getContactUrl = $this->rootPath.$getContactPath."?sid=".$this->sid;
    $getContactUrl .= "&bookId=".$bookId."&id=".$contactId;

    return $getContactUrl;
  }
  
  protected function getCreateContactPostParameters($contact){
    $fields = $this->escapeFields($contact["record"]);
    $xml = $this->getXmlFromFieldArray($fields);
    return array('contact' => '<contact>'.$xml.'</contact>');
  }
  
  protected function getUpdateContactPostParameters($contact){
    $fields = $this->escapeFields($contact["record"]);
    $xml = $this->getXmlFromFieldArray($fields);
    return array('contact' => '<contact collected="false" uid="' . $contact['id'] . '">'.$xml.'</contact>');
  }

  protected function getXmlFromFieldArray($fields) {
    foreach ($fields as $fieldName => $fieldValue) {
      if (is_array($fieldValue)) {
        foreach ($fieldValue as $value) {
          if (!empty($value)) {
            foreach ($this->multipleFields as $fieldGroup => $xmlAssociation) {
              if (preg_match('#^'.$fieldGroup.'#', $fieldName)) {

                $fieldLabel = substr($fieldName, strpos($fieldName, ':') + 1);
                $nodeName = $xmlAssociation['nodeName'];
                $itemName = $xmlAssociation['itemName'];
                $labels = $xmlAssociation['labels'];

                if (isset($xmlFields[$nodeName])) {
                  end($xmlFields[$nodeName]);
                  $i = key($xmlFields[$nodeName]) + 1;
                } else {
                  $i = 1;
                }

                $sequenceName = "$nodeName/$itemName/$fieldLabel";
                if (!array_key_exists($sequenceName, $this->sequences)) {
                  $this->sequences[$sequenceName] = 0;
                }

                $this->sequences[$sequenceName]++;
                $sequenceValue = $this->sequences[$sequenceName];

                switch ($fieldGroup) {
                  case 'email':
                    $xmlFields[$nodeName][$i] = '<'.$itemName.' 
                    label="'.$labels[$fieldLabel].';X-OBM-Ref'.$sequenceValue.'"
                    value="'.$value.'"/>';
                    break;

                  case 'phone':
                    $xmlFields[$nodeName][$i] = '<'.$itemName.' 
                    label="'.$labels[$fieldLabel].';X-OBM-Ref'.$sequenceValue.'"
                    number="'.$value.'"/>';
                    break;

                  case 'address':
                    $xmlFields[$nodeName][$i] = '<'.$itemName.' 
                    label="'.$labels[$fieldLabel].';X-OBM-Ref'.$sequenceValue.'"
                    country="'.$value['country'].'" 
                    zip="'.$value['zipcode'].'" 
                    expressPostal="" 

                    town="'.$value['locality'].'" 
                    state="'.$value['region'].'" 
                    >'.$value['street'].'</'.$itemName.'>';
                    break;

                  case 'website':
                    $xmlFields[$nodeName][$i] = '<'.$itemName.' 
                    label="'.$labels[$fieldLabel].';X-OBM-Ref'.$sequenceValue.'"
                    url="'.$value.'"/>';
                    break; 

                  case 'im':
                    $xmlFields[$nodeName][$i] = '<'.$itemName.' 
                    label="'.$labels[$fieldLabel].';X-OBM-Ref'.$sequenceValue.'"
                    protocol="'.$labels[$fieldLabel].'" 
                    address="'.$value.'"/>';
                    break;
                }
              }
            }
          }
        }
      } else {
        $xmlFields['simpleFields'][] = '<'.$this->simpleFields[$fieldName].'>'.$fieldValue.'</'.$this->simpleFields[$fieldName].'>';
      }
    }

    foreach ($xmlFields as $node => $xmlLines) {
      if ($node != 'simpleFields') $xml .= '<'.$node.'>';
        foreach ($xmlLines as $xmlLine) {
          $xml .= $xmlLine;
        }
      if ($node != 'simpleFields') $xml .= '</'.$node.'>';        
    }

    return $xml;
  }

  protected function escapeFields($mixed, $quote_style = ENT_QUOTES, $charset = 'UTF-8') {
      if (is_array($mixed)) {
          foreach($mixed as $key => $value) {
              $mixed[$key] = $this->escapeFields($value, $quote_style, $charset);
          }
      } else {
          $mixed = htmlspecialchars(htmlspecialchars_decode($mixed, $quote_style), $quote_style, $charset);
      }
      return $mixed;
  } 

  protected function getImportICalendarPostParameters($icsFile){
    return array("ics"=>$icsFile);
  }

  /**
   * Executes obmSync login and gets a session id. Must be executed before
   * calling a service from obm-sync.
   * 
   * @return boolean True if everything went well, false otherwise
   */
  public function obmSyncLogin(){

    $this->sid = $this->auth->logIn($this);
    return true;
  }

  protected function getErrorMessageForImportICalendar(DOMDocument $response = null, DOMNodeList $errors = null){
    if(!$response){
      return "An unknown error occured during the ics import";
    }
    $error = $errors->item(0)->nodeValue ?
             $errors->item(0)->nodeValue :
             "An unknown error occured during the ics import";
    if(preg_match("/Calendar : duplicate with same extId found/", $error, $matches)){
      preg_match("/\[.*\]/", $error, $matches);

      $error = "An event from the ics already exists"." ".$matches[0];
    }

    return $error;
  }

  /**
   * Calls an obm-sync service
   * 
   * @param string $serviceName
   * @param array  $arguments (optionnal) Arguments used to construct the service
   *               call (eg. the ics for an ics import)
   * @param array  $options (optionnal) Options concerning the request (for
   *               example the http method to call the service)
   * @return mixed returns the xml of the response as a DomDocument or false if
   *               an error occured
   */
  public function callObmSyncService($serviceName, $arguments = array(), $options = array()){
    // Login if not already done
    if(!$this->sid){
      if(!$this->obmSyncLogin()){
        return false;
      }
    }

    $getServiceUrlMethod = $this->getServiceUrlMethod($serviceName);
    if(!$this->serviceExists($serviceName, $getServiceUrlMethod)){
      throw new Exception("The obm-sync service '".$serviceName."' isn't defined");
    }

    $serviceUrl = call_user_func_array(array($this, $getServiceUrlMethod), $arguments);
    $options =  array_merge(
                    array("httpMethod"=>$this->getServiceHttpMethod($serviceName)),
                    $options
                  );
    return $this->callService($serviceName, $serviceUrl, $arguments, $options);
  }

  /**
   * Tells if a service is available or not
   * To be available, a service must have a method to define its url and must
   * be defined in implementedServices
   *
   * @param string $serviceName
   * @return boolean True if service exists, false otherwise
   */
  private function serviceExists($serviceName, $getServiceUrlMethod){
    $serviceMethodExists = method_exists($this, $getServiceUrlMethod);
    return array_key_exists($serviceName, $this->implementedServices)
           && in_array($this->implementedServices[$serviceName], $this->authorizedHttpMethods)
           && $serviceMethodExists;
  }

  private function getServiceHttpMethod($serviceName){
    if(array_key_exists($serviceName, $this->implementedServices)){
      return $this->implementedServices[$serviceName];
    }
    return null;
  }

  protected function getRootPathForIp($serverIp){
    return sprintf(self::$rootPathScheme, trim($serverIp));
  }

  public function setLogin($login){
    $this->login = $login;
  }

  public function setPassword($password){
    $this->password = $password;
  }

  public function setDomainName($domainName){
    $this->domainName = $domainName;
  }

  public function isLoggedIn(){
    return $this->sid != null;
  }
}
