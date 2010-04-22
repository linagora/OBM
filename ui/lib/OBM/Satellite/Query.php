<?php
/*
 +-------------------------------------------------------------------------+
 |  Copyright (c) 1997-2010 OBM.org project members team                   |
 |                                                                         |
 | This program is free software; you can redistribute it and/or           |
 | modify it under the terms of the GNU General Public License             |
 | as published by the Free Software Foundation; version 2                 |
 | of the License.                                                         |
 |                                                                         |
 | This program is distributed in the hope that it will be useful,         |
 | but WITHOUT ANY WARRANTY; without even the implied warranty of          |
 | MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           |
 | GNU General Public License for more details.                            |
 +-------------------------------------------------------------------------+
 | http://www.obm.org                                                      |
 +-------------------------------------------------------------------------+
*/

/*
 * Class used to query obm-satellite
 */
abstract class OBM_Satellite_Query {
  protected $request;

  /**
   * standard constructor
   * @param OBM_Satellite_ICredentials $auth   object used to authenticate with obm-satellite
   * @param array $args   arguments used to build the query url (used by build_url method)
   * @param array $data   data to put into the content of the query (used by prepare_query method)
   * @access public
   **/
  public function __construct($auth, $args, $data = null) {
    $this->request = curl_init();

    // default options
    $options = array(
      CURLOPT_URL => $this->buildUrl($args),
      CURLOPT_HTTPAUTH => CURLAUTH_BASIC,
      CURLOPT_USERPWD => $auth->credentials,
      CURLOPT_SSL_VERIFYPEER => FALSE,
      CURLOPT_SSL_VERIFYHOST => FALSE
    );
    // query specific options
    $this->addHeaders($options);
    // mandatory options
    $options[CURLOPT_RETURNTRANSFER] = TRUE;
    $options[CURLOPT_HEADER] = FALSE;
    $options[CURLOPT_FORBID_REUSE] = TRUE;
    $options[CURLOPT_HTTPHEADER] = array('Expect: ');

    $retour = curl_setopt_array($this->request, $options);
    if ($retour===FALSE)
      throw new Exception('Invalid query options');

    // query body
    $body = $this->buildBody($data);
    if ($body) {
      $retour = curl_setopt($this->request, CURLOPT_POSTFIELDS, $body);
    }
    if ($retour===FALSE)
      throw new Exception('Invalid query body');
  }

  /**
   * Execute the query and return the result
   * Throw an exception in case of error
   * @access public
   * @return mixed
   **/
  public function execute() {
    $body = curl_exec($this->request);
    $code = curl_getinfo($this->request, CURLINFO_HTTP_CODE);
    curl_close($this->request);
    if ($body===FALSE)
      throw new Exception('Unexpected response');

    if (($code >= 200) && ($code < 300)) {
      return $this->parseResponse($body);
    }

    if ($code >= 400) {
      $error = $this->parseError($body);
      throw new Exception("$code: $error");
    }

    //else
    throw new Exception("$code: Unexpected response code");
  }

  /**
   * Parse the error response xml body to return the message
   * @param string $xml   The response body
   * @access protected
   * @return string
   **/
  protected function parseError($xml) {
    $sxml = new SimpleXMLElement($xml);
    if (!$sxml->content)
      return $sxml['status'];
    return (string)$sxml->content;
  }



  /****** Children may implement following functions ******/

  /**
   * build the url to query from the given arguments
   * overload this function while implementing this class
   * @param  array     $args
   * @access protected
   * @return string
   **/
  abstract protected function buildUrl($args);

  /**
   * Allow to personalize http request options
   * overload this function while implementing this class
   * @param  array     $options    editable array
   * @access protected
   **/
  protected function addHeaders(&$options) {
  }

  /**
   * Prepare the query with the given data
   * overload this function while implementing this class
   * @param  array     $data
   * @access protected
   * @return string    the query body or null if no body
   **/
  abstract protected function buildBody($data);

  /**
   * Parse the response xml body to return it under an easily usable form
   * overload this function while implementing this class
   * @param string $xml   The response body
   * @access protected
   * @return mixed
   **/
  abstract protected function parseResponse($xml);

}

