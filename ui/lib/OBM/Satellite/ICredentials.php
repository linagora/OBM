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

