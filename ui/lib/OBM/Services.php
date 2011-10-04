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

define('SERVICE_DESC_FILE', '/root/services/service.xml'); //FIXME: hard coded path

/**
 * Singleton class OBM_used to describe services (autoload data from service.xml file)
 **/
class OBM_Services {
  private static $instance;
  protected $servers;
  protected $forms;

  /**
   * external use not allowed
   * @access private
   **/
  private function __construct() {
    $this->servers = new ArrayObject();
    $this->services = new ArrayObject();
    $this->forms = new ArrayObject();
    $xml = new SimpleXMLElement(SERVICE_DESC_FILE, NULL, TRUE);

    $servers = array();
    $services = array();

    foreach ($xml->host->server as $data) {
      $server = new OBM_Services_MetaServer((string)$data['name'], (string)$data->label);
      //FIXME: read properties
      $servers[$server->name] = $server;
    }

    foreach ($xml->domain->obmservice as $data) {
      $service = new OBM_Services_MetaService((string)$data['name'], (string)$data->label);
      foreach ($data->require as $requirement) {
        $server = $servers[(string)$requirement['server']];
        if (isset($server)) {
          $service->requires($server,(string)$requirement['multiplicity']);
          //FIXME: requirement label
          //FIXME: requirement properties

        } else {
          //FIXME: error: the service depends on an undeclared server
        }
      }
      //FIXME: read properties
      $services[$service->name] = $service;
    }

    // we only keep servers and services for enabled services
    foreach ($servers as $name => $server) {
      if ($this->is_service_enabled($server->service->name)) {
        $this->servers[$name] = $server;
      }
    }

    foreach ($services as $name => $service) {
      if ($this->is_service_enabled($service->name)) {
        $this->services[$name] = $service;
      }
    }
  }

  /**
   * object copy not allowed
   * @access private
   **/
  private function __clone() {
  }

  /**
   * return the unique instance of OBM_Services object
   * @access public
   * @return OBM_Services
   **/
  public static function getInstance() {
    if (!(self::$instance instanceof self)) {
      self::$instance = new OBM_Services();
    }
    return self::$instance;
  }

  /**
   * standard getter
   * @access public
   * @return mixed
   **/
  public function __get($key) {
    $getter = "get_{$key}";
    if (method_exists($this,$getter))
      return $this->$getter();
    //else
    if (!preg_match('/^[a-zA-Z]\\w*Form$/',$key))
      return null;
    if (!($entity = strtolower(substr($key,0,-4))))
      return null;
    return $this->get_form($entity);
  }

  /**
   * servers getter
   * @access public
   * @return mixed
   **/
  public function get_servers() {
    return $this->servers;
  }

  /**
   * form getter
   * @param  string $entity
   * @access public
   * @return mixed
   **/
  public function get_form($entity) {
    $entity = strtolower($entity);
    if (!isset($this->forms[$entity])) {
      $class = 'OBM_Services_'.ucfirst($entity).'Form';
      if (!class_exists($class))
        $class = 'OBM_Services_ConsumerForm';
      $this->forms[$entity] = new $class(strtolower($entity));
    }
    return $this->forms[$entity];
  }

  /**
   * true if a service is enabled
   * @param  string  $service      the service name
   * @access public
   * @return boolean
   **/
  public function is_service_enabled($service) {
    // FIXME: check that service is not disabled in the ini file
    // FIXME: must check that domain is global or service is enabled for this domain
    if (isset($GLOBALS['cgp_use']['service'][$service]))
      return $GLOBALS['cgp_use']['service'][$service];
    //else
    return true;
  }

}


