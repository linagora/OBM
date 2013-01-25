<?php

include_once dirname(__FILE__).'/httpRequester.class.php';

abstract class obmServiceRequester{
  protected     $rootPath               = null;
  protected     $errors                 = array();
  protected     $serverIp               = null;
  protected     $defaultRequestHeaders  = array(
                                           "Content-Type"=>"application/x-www-form-urlencoded; charset=utf-8"
                                          );
  protected     $lastXmlValidResponse   = null;
  protected     $lastResponse           = null;
  protected     $httpRequester;

  /**
   * Initializes mandatory properties for services calls and tries to retrieve
   * the service location with provided informations
   *
   * @param string $serverIp    Server ip where to call services
   * @param HTTPRequester       HttpRequester which will be responsible of doing
   *                            HTTP requests to obm-sync
   */
  public function __construct(HTTPRequester $httpRequester, $serverIp = null) {

    $this->httpRequester = $httpRequester;
    if(   !$serverIp   ){
      throw new Exception("Invalid obm server ip", 500);
    }
    else{
      $this->serverIp = $serverIp;
      $this->rootPath = $this->getRootPathForIp($serverIp);
    }
  }
  /**
   * Calls a service
   *
   * @param string $serviceName
   * @param string $serviceUrl
   * @param array  $arguments (optionnal) Necessary arguments for post parameters
   *                          and http headers retrieval
   * @param array  $options (optionnal) Options concerning the request (for
   *               example the http method to call the service)
   * @return mixed returns the xml of the response as a DomDocument or false if
   *               an error occured. If the response is not XML formated it is
   *               returned as a string
   */
  protected function callService( $serviceName,
                                  $serviceUrl,
                                  $arguments = array(),
                                  $options = array()){
    $httpHeaders =  array_merge(
                      $this->defaultRequestHeaders,
                      $this->getHttpHeadersForService($serviceName, $arguments)
                    );
    $postParameters = $this->getPostParametersForService($serviceName, $arguments);
    $response = $this->httpRequester
                ->executeHTTPRequest(
                  $serviceUrl,
                  $httpHeaders,
                  $postParameters,
                  $options
                );

    if($errorMessage = $this->getErrorMessageFromResponse($response, $serviceName)){
      $this->errors[] = $errorMessage;
      return false;
    }

    return $this->lastXmlValidResponse ? $this->lastXmlValidResponse : $this->lastResponse;
  }

  protected function getHttpHeadersForService($serviceName, $arguments = array()){
    $methodName = "get".ucfirst($serviceName)."HttpHeaders";
    if(method_exists($this, $methodName)){
      return call_user_func_array(array($this, $methodName), $arguments);
    }
    return array();
  }

  protected function getPostParametersForService($serviceName, $arguments = array()){
    $methodName = "get".ucfirst($serviceName)."PostParameters";
    if(method_exists($this, $methodName)){
      return call_user_func_array(array($this, $methodName), $arguments);
    }
    return array();
  }

  /**
   * Retrieves eventual error message from an obm-sync webservice response
   *
   * @param mixed Response to an obm-sync webservice after a http request
   * @param string $serviceName Name of the called service
   * @return string Error message if exists, empty string, false or null otherwise
   */
  protected function getErrorMessageFromResponse($response, $serviceName){
    $methodName = "getErrorMessageFor".  ucfirst($serviceName);
    if(!$response){
//       echo "response is empty ";
      if(method_exists($this, $methodName))
        return call_user_func_array(array($this, $methodName), array());
      else
        return $this->getDefaultErrorMessage($serviceName);
    }
    $this->lastResponse = $response;
    $xmlResponse = new DomDocument();

    // If response is not a valid xml string an error can't be parsed out of it
    if(!@$xmlResponse->loadXML($response)){
//       echo "can't parse response to xml ".$response;
      return null;
    }
    $errors = $xmlResponse->getElementsByTagName("error");
    if($errors->length > 0){
      if(method_exists($this, $methodName)){
        return call_user_func_array(array($this, $method_name), array($xmlResponse, $errors));
      }
      else{
        return $this->getDefaultErrorMessage($serviceName, $xmlResponse, $errors);
      }
    }
//     echo "setting lastXmlValidResponse to domdocument ".$response;
    $this->lastXmlValidResponse = $xmlResponse;
    return null;
  }

  /**
   * Return an error message if there is no response or if an <error> tag exists
   *
   * @param string $serviceName
   * @param DOMDocument $response
   * @param DOMNodeList $errors
   * @return string error message
   */
  private function getDefaultErrorMessage($serviceName, DOMDocument $response = null, DOMNodeList $errors = null){
    if(!$response){
      return "Nothing returned from service '".$serviceName."' call";
    }
    if($errors instanceof DOMNodeList && $errors->length > 0){
      $error = $errors->item(0)->nodeValue ?
             $errors->item(0)->nodeValue :
             "unknown error";
      return "An error occured when calling service '".$serviceName."' : ".$error;
    }
    return false;
  }

  /**
   * Request headers setter
   * @param array $requestHeaders Must be an $key=>$values array where keys are
   *              header names and values = header values
   */
  public function setDefaultRequestHeaders(array $requestHeaders){
    if(is_array($requestHeaders)){
      $this->defaultRequestHeaders = $requestHeaders;
    }
  }

  public function getLastValidXmlResponse(){
    return $this->lastXmlValidResponse;
  }

  public function getErrors(){
    return $this->errors;
  }

  public function countErrors(){
    return count($this->errors);
  }

  public function getFirstError(){
    if($this->countErrors()){
      $errors = $this->errors;
      return $errors[0];
    }
    else{
      return null;
    }
  }

  abstract protected function getRootPathForIp($serverIp);
}

