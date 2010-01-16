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
//   * Neither the name of the LINAGORA GROUP nor the names of its contributors may
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

/**
 * LemonLDAP logger.
 * This logger will log to STDERR. Normaly, this should be log into the
 * global Apache error log.
 */
class LemonLDAP_Logger {

  /**
   * Log file (optinal).
   * @var string.
   */
  private $_file = null;

  /**
   * Log level.
   * @var string
   */
  private $_level = 4;

  /**
   * Debug levels.
   * @var array
   */
  private static $_levels = Array(
      DEFAULT_LEMONLDAP_LOGLEVEL_DEBUG => 0,
      DEFAULT_LEMONLDAP_LOGLEVEL_INFO  => 1,
      DEFAULT_LEMONLDAP_LOGLEVEL_WARN  => 2,
      DEFAULT_LEMONLDAP_LOGLEVEL_ERROR => 3,
      DEFAULT_LEMONLDAP_LOGLEVEL_NONE  => 4
    );

  /**
   * The unique instance of this engine.
   * @var LemonLDAP_Logger
   */
  private static $_instance = null;

  /**
   * Constructor.
   * @param Boolean $init Initializes from configuration file or not.
   */
  protected function __construct ()
  {
    $this->initializeFromConfiguration();
  }

  /**
   * Print some logs.
   * @param $level The debug level.
   * @param $msg The message to trace.
   */
  private function _log ($level, $msg)
  {
    if (!is_int($level))
    {
      $levelstr = $level;
      $level = self::$_levels[$level];
    }
    else
    {
      $levelstr = array_search($level, self::$_levels);
    }
    if ($this->_level <= $level)
    {
      $traces = debug_backtrace(false);
      $function = $traces[2]['function'];
      $line = $traces[0]['line'];
      $now = date(DEFAULT_LEMONLDAP_LOGFORMAT_DATE);
      $log = "[$now] [$levelstr] OBM LemonLDAP Connector - $function($line): $msg\n";
      if (!is_null($this->_file))
      {
        $f = fopen($this->_file, "a+");
      }
      else
      {
        $f = fopen('php://stderr', 'w');
      }
      fputs($f, "$log");
      fclose($f);
    }
  }

  /**
   * Return the unique instance of this object.
   * @return LemonLDAP_LogLayout This unique instance of this object.
   */
  public static function getInstance ()
  {
    if (is_null(self::$_instance))
    {
      self::$_instance = new LemonLDAP_Logger();
    }
    return self::$_instance;
  }

  /**
   * Initialize parameters from configuration.
   */
  public function initializeFromConfiguration()
  {
    global $lemonldap_config;
    if (array_key_exists('debug', $lemonldap_config) !== false
        && $lemonldap_config['debug'])
    {
      //
      // For compatibilities. Default is to log in DEBUG mode.
      //
      $this->setLevel(DEFAULT_LEMONLDAP_LOGLEVEL_DEBUG);
      if (array_key_exists('debug_filepath', $lemonldap_config) !== false)
      {
        $this->_file = $lemonldap_config['debug_filepath'];
      }
    }
    if (array_key_exists('debug_level', $lemonldap_config) !== false)
    {
      $this->setLevel($lemonldap_config['debug_level']);
      unset($this->_file);
    }
  }

  /**
   * Log with DEBUG level.
   * @param $msg The message to trace.
   */
  public function debug ($msg)
  {
    $this->_log(DEFAULT_LEMONLDAP_LOGLEVEL_DEBUG, $msg);
  }

  /**
   * Log with ERROR level.
   * @param $msg The message to trace.
   */
  public function error ($msg)
  {
    $this->_log(DEFAULT_LEMONLDAP_LOGLEVEL_ERROR, $msg);
  }

  /**
   * Log with INFO level.
   * @param $msg The message to trace.
   */
  public function info ($msg)
  {
    $this->_log(DEFAULT_LEMONLDAP_LOGLEVEL_INFO, $msg);
  }

  /**
   * Log with DEBUG level.
   * @param $msg The message to trace.
   */
  public function warn ($msg)
  {
    $this->_log(DEFAULT_LEMONLDAP_LOGLEVEL_WARN, $msg);
  }

  /**
   * Set debug level.
   * @param String $level The debug level (see constants).
   * @return Boolean True if debug level could be set.
   */
  public function setLevel ($level = DEFAULT_LEMONLDAP_LOGLEVEL_NONE)
  {
    $level = strtoupper($level);
    if (array_key_exists($level, self::$_levels))
    {
      $this->_level = self::$_levels[$level];
      return true;
    }
    return false;
  }

}

?>
