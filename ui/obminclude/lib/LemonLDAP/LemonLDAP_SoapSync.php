<?php

//
// $Id$
//
// Copyright (c) 2009, Thomas Chemineau - thomas.chemineau<at>gmail.com
// All rights reserved.
// 
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
//   * Redistributions of source code must retain the above copyright notice,
//     this list of conditions and the following disclaimer.
//   * Redistributions in binary form must reproduce the above copyright notice,
//     this list of conditions and the following disclaimer in the documentation
//     and/or other materials provided with the distribution.
//   * Neither the name of the LINAGORA GROUP nor the names of its contributors
//     may be used to endorse or promote products derived from this software
//     without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
// ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
// LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
// CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
// SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
// INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
// CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
// ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
// POSSIBILITY OF SUCH DAMAGE.
// 

/**
 * LemonLDAP SOAP synchronization class.
 */
class LemonLDAP_SoapSync extends LemonLDAP_Sync {

  /**
   * LemonLDAP::NG configuration.
   */
  private $_config = Array();

  /**
   * Indicates if SOAP service is configured.
   */
  private $_isSoap = false;

  /**
   * Constructor.
   */
  function __construct ($engine)
  {
    parent::__construct($engine);
    $this->initializeFromSOAP();
  }

  /**
   * Initiliaze internal parameters from configuration.
   */
  function initializeFromConfiguration ()
  {
    global $lemonldap_config;
    if (array_key_exists('soap', $lemonldap_config) !== false)
    {
      $this->_isSoap = $lemonldap_config['soap'];
    }
    //
    // If SOAP is configured, then launched NuSOAP PHp library.
    // Default path is defined into LemonLDAP_Constants.php.
    //
    if ($this->_isSoap && !class_exists('nusoap_base', false))
    {
      require_once DEFAULT_NUSOAP_PATH;
    }
    parent::initializeFromConfiguration();
  }

  /**
   * Initialize internal parameters from SOAP.
   */
  function initializeFromSOAP ()
  {
  }

}

?>
