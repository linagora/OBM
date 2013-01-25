<?php
interface HTTPRequester{
  /**
   * Opens an HTTP connection, executes a request, close the connection when
   * reponse is complete and returns the response
   */
  public function executeHTTPRequest($requestUrl,
                                      array $httpHeaders = array(), 
                                      array $postFields = array(),
                                      array $options = array());
}

class CurlRequester implements HTTPRequester{

	var $options = null;
	
	function __construct($options = array() ) {
		$this->options = $options;
	}

  /**
   * Execute a curl request on a given url
   *
   * @param string $requestUrl
   * @param array $httpHeaders
   * @param array $postFields
   * @return mixed Returns the result on success, false on failure.
   */
  public function executeHTTPRequest($requestUrl,
                                      array $httpHeaders = array(),
                                      array $postFields = array(),
                                      array $options = array()){
    $curlHTTPHeaders = array();
    foreach($httpHeaders as $headerName => $headerValue){
      $curlHTTPHeaders[] = $headerName.": ".$headerValue;
    }

	$localOptions = array_merge($this->options, $options);

    $ch = curl_init($requestUrl);
    if(!array_key_exists("httpMethod", $localOptions) || $localOptions['httpMethod'] == "POST")
    {
      curl_setopt($ch, CURLOPT_POST, true);
      if(count($postFields)){
        curl_setopt($ch, CURLOPT_POSTFIELDS, http_build_query($postFields));
      }
    }

	if ( array_key_exists("networkTimeout", $localOptions) && $localOptions['networkTimeout'] > 0 ) {
		curl_setopt($ch, CURLOPT_TIMEOUT, $localOptions['networkTimeout']);
	}

    curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, 0);
    curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, 0);
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
    curl_setopt($ch,CURLOPT_HTTPHEADER, $curlHTTPHeaders);

    $response = curl_exec($ch);
    curl_close($ch);
    return $response;
  }
}

class HttpRequesterMock implements HTTPRequester{
  public function executeHTTPRequest($requestUrl,
                                      array $httpHeaders = array(),
                                      array $postFields = array(),
                                      array $options = array()){
    return 
    '<?xml version="1.0" encoding="UTF-8"?>
      <token xmlns="http://www.obm.org/xsd/sync/token.xsd">
        <sid>b3013b8d-ec13-4960-9a08-d3f0f71b86a8</sid>
        <version major="2" minor="3" release="25"/>
        <domain>obm23.com</domain>
      </token>';
  }
}


