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
 * Class used to authenticate with obm-satellite
 */
class OBM_Satellite_ICredentials {
  public static $port = 30000;
  protected $user;
  protected $password;

  /**
   * standard constructor
   * @param  string $user
   * @param  string $password
   * @access public
   **/
  public function __construct($user, $password) {
    $this->user = $user;
    $this->password = $password;
  }

  /**
   * execute a OBM_Satellite query created with the given arguments
   * Throw an exception in case of error
   * @access public
   * @return mixed       query result
   **/
  public function __call($name, $arguments) {
    $class = "OBM_Satellite_{$name}";
    $query = new $class($this, $arguments[0], $arguments[1]);
    return $query->execute();
  }

  /**
   * standard getter
   * @param  string $key    the attribute to get
   * @access public
   * @return mixed
   **/
  public function __get($key) {
    $getter = 'get'.ucfirst($key);
    if (method_exists($this,$getter))
      return $this->$getter();
    //else
    return null;
  }

  /**
   * user getter
   * @access public
   * @return mixed
   **/
  public function getUser() {
    return $this->user;
  }

  /**
   * password getter
   * @access public
   * @return mixed
   **/
  public function getPassword() {
    return $this->password;
  }

  /**
   * password getter
   * @access public
   * @return mixed
   **/
  public function getCredentials() {
    return "{$this->user}:{$this->password}";
  }

  /**
   * standard setter
   * @param  string $key    the attribute to set
   * @param  mixed  $value  the value
   * @access public
   * @return mixed
   **/
  public function __set($key, $value) {
    $setter = 'set'.ucfirst($key);
    if (method_exists($this,$setter))
      return $this->$setter($value);
    //else
    return null;
  }

  /**
   * user setter
   * @param  string $user
   * @access public
   * @return mixed
   **/
  public function setUser($user) {
    return $this->user = $user;
  }

  /**
   * password setter
   * @param  string $password
   * @access public
   * @return mixed
   **/
  public function setPassword($password) {
    return $this->password = $password;
  }

}

