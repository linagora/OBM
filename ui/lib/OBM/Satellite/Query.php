<?php
/******************************************************************************
Copyright (C) 2011-2014 Linagora

This program is free software: you can redistribute it and/or modify it under
the terms of the GNU Affero General Public License as published by the Free
Software Foundation, either version 3 of the License, or (at your option) any
later version, provided you comply with the Additional Terms applicable for OBM
software by Linagora pursuant to Section 7 of the GNU Affero General Public
License, subsections (b), (c), and (e), pursuant to which you must notably (i)
retain the displaying by the interactive user interfaces of the “OBM, Free
Communication by Linagora” Logo with the “You are using the Open Source and
free version of OBM developed and supported by Linagora. Contribute to OBM R&D
by subscribing to an Enterprise offer !” infobox, (ii) retain all hypertext
links between OBM and obm.org, between Linagora and linagora.com, as well as
between the expression “Enterprise offer” and pro.obm.org, and (iii) refrain
from infringing Linagora intellectual property rights over its trademarks and
commercial brands. Other Additional Terms apply, see
<http://www.linagora.com/licenses/> for more details.

This program is distributed in the hope that it will be useful, but WITHOUT ANY
WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License and
its applicable Additional Terms for OBM along with this program. If not, see
<http://www.gnu.org/licenses/> for the GNU Affero General   Public License
version 3 and <http://www.linagora.com/licenses/> for the Additional Terms
applicable to the OBM software.
******************************************************************************/



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
    $this->data = $data;
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
    if ($body===FALSE)
      throw new Exception(curl_error($this->request));

    curl_close($this->request);
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

