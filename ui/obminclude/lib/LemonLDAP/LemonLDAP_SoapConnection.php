<?php

//
// Copyright (c) 2009, Thomas Chemineau - thomas.chemineau<at>gmail.com
// All rights reserved.
// 
// Redistribution and use in source and binary forms, with or without modification,
// are permitted provided that the following conditions are met:
//
//   * Redistributions of source code must retain the above copyright notice, this
//     list of conditions and the following disclaimer.
//   * Redistributions in binary form must reproduce the above copyright notice,
//     this list of conditions and the following disclaimer in the documentation
//     and/or other materials provided with the distribution.
//   * Neither the name of the AEPIK.NET nor the names of its contributors may
//     be used to endorse or promote products derived from this software without
//     specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
// ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
// ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//
//

/**
 * LemonLDAP SOAP connection class.
 * Require NuSOAP library and php-soap.
 */
class LemonLDAP_SoapConnection {

	/**
	 * Server config URL.
	 * @var String
	 */
	private $_soapEndPointConfig = '';

	/**
	 * Server sessions URL.
	 * @var String
	 */
	private $_soapEndPointSessions = '';

	/**
	 * Proxy host.
	 * @var String
	 */
	private $_soapProxyHost = false;

	/**
	 * Proxy port.
	 * @var int
	 */
	private $_soapProxyPort = false;

	/**
	 * Proxy username.
	 * @var String
	 */
	private $_soapProxyUser = '';

	/**
	 * Proxy password.
	 * @var String
	 */
	private $_soapProxyPass = '';

	/**
	 * The SOAP connexion.
	 */
	private $_soapConn = null;

	/**
	 * Default log layout.
	 */
	private $_log = null;

	/**
	 * Constructor.
	 * @param Boolean $init Initializes from configuration file or not.
	 */
	function __construct ($init = false)
	{
		require_once DEFAULT_NUSOAP_PATH;
	}

	/**
	 * Call a function via SOAP.
	 * @param String $function The function which will be called
	 * @param Array $params Parameters of this function.
	 * @return Object Values returned by the function, or false.
	 */
	private function _call ($function, $params)
	{
		if (is_null($this->_soapConn))
		{
			$this->_log->logError('missing SOAP connection');
			return false;
		}

		$result = $this->_soapConn->call($function, $params, DEFAULT_NUSOAP_NAMESPACE);
		if ($this->_soapConn->fault)
		{
			$this->_log->logError('Invalid SOAP answer, ' . var_export($result, true));
			return false;
		}

		$err = $this->_soapConn->getError();
		if ($err)
		{
			$this->_log->logError('Unkown error, ' . var_export($err, true));
			return false;
		}

		return $result;
	}

	/**
	 * Initialize the SOAP client connection.
	 * @param String $endpoint URL to connect.
	 * @return Boolean True if the connection succeed.
	 */
	private function _connect ($endpoint, $proxyhost = null, $proxyport = null, $proxyuser = null, $proxypass = null)
	{
		if (is_null($proxyhost))
			$proxyhost = $this->_soapProxyHost;
		if (is_null($proxyport))
			$proxyport = $this->_soapProxyPort;
		if (is_null($proxyuser))
			$proxyuser = $this->_soapProxyUser;
		if (is_null($proxypass))
			$proxypass = $this->_soapProxyPass;

		$conn = new nusoap_client($endpoint, false, $proxyhost, $proxyport, $proxyuser, $proxypass);
		$err = $conn->getError();
		if ($err)
		{
                        $this->_log->logError($err);
                        $this->_log->logError($conn->getDebug());
			$this->_soapConn = null;
                        return false;
                }
		else
		{
                	$conn->setUseCurl(DEFAULT_NUSOAP_CURLREQUEST);
			$this->_soapConn = $conn;
			return true;
		}
	}

	/**
	 * Return the LemonLDAP::NG server configuration, get from a SOAP called.
	 * @return Array The configuration, or false
	 */
	public static function getConfig ()
	{
		$soap = new LemonLDAP_SoapConnection(true);

		if (!$soap->_connect($soap->getConfigEndPoint()))
			return false;

		$num = $soap->_call('lastCfg', Array());
		return $soap->_call('getConfig', Array($num));
	}

	/**
	 * Get LemonLDAP::NG SOAP config endpoint.
	 * @return String LemonLDAP::NG SOAP config endpoint
	 */
	public function getConfigEndPoint ()
	{
		return $this->_soapEndPointConfig;
	}

	/**
	 * Get LemonLDAP::NG SOAP sessions endpoint.
	 * @return String LemonLDAP::NG SOAP sessions endpoint
	 */
	public function getSessionsEndPoint ()
	{
		return $this->_soapEndPointSessions;
	}

	/**
	 * Initialize parameters from configuration.
	 */
	public function initializeFromConfiguration()
	{
		global $lemonldap_config;
		$this->_soapEndPointConfig = $lemonldap_config['soap_endpoint_config'];
		$this->_soapEndPointSessions = $lemonldap_config['soap_endpoint_sessions'];
		$this->_soapProxyHost = $lemonldap_config['soap_proxy_host'];
		$this->_soapProxyPort = $lemonldap_config['soap_proxy_port'];
		$this->_soapProxyUser = $lemonldap_config['soap_proxy_user'];
		$this->_soapProxyPass = $lemonldap_config['soap_proxy_pass'];
	}

	/**
	 * Set LemonLDAP::NG SOAP config endpoint.
	 * @param String $endpoint LemonLDAP::NG SOAP config endpoint
	 */
	public function setConfigEndPoint ($endpoint)
	{
		$this->_soapEndPointConfig = $endpoint;
	}

	/**
	 * Set LemonLDAP::NG SOAP sessions endpoint.
	 * @param String $endpoint LemonLDAP::NG SOAP sessions endpoint
	 */
	public function setSessionsEndPoint ($endpoint)
	{
		$this->_soapEndPointSessions = $endpoint;
	}

	/**
	 * Set proxy host.
	 * @param String $proxyhost Proxy host
	 */
	public function setProxyHost ($proxyhost)
	{
		$this->_soapProxyHost = $proxyhost;
	}

	/**
	 * Set proxy port.
	 * @param int $proxyport Proxy port
	 */
	public function setProxyPort ($proxyport)
	{
		$this->_soapProxyPort = $proxyport;
	}

	/**
	 * Set proxy user name.
	 * @param String $proxyuser Proxy user name
	 */
	public function setProxyUser ($proxyuser)
	{
		$this->_soapProxyUser = $proxyuser;
	}

	/**
	 * Set proxy user password.
	 * @param String $proxypass Proxy user password
	 */
	public function setProxyPass ($proxypass)
	{
		$this->_soapProxyPass = $proxypass;
	}

}

?>
