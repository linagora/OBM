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

/**
 * Class used to describe meta-data of services (built by the OBM_Services class)
 **/
class OBM_Services_MetaService extends OBM_Services_MetaElement {
  protected $requirements;

  /**
   * requirements getter
   * @access public
   * @return int
   **/
  public function get_requirements() {
    return $this->requirements;
  }

  /**
   * requirement getter
   * @param  string $name    the server name
   * @access public
   * @return int
   **/
  public function get_requirement($name) {
    return $this->requirements[$name];
  }

  /**
   * add a server to the requirements
   * @param  OBM_Services_MetaServer $server          the server object
   * @param  string                  $multiplicity    '1' or '*'
   * @access public
   **/
  public function requires(OBM_Services_MetaServer $server, $multiplicity='1') {
    $server->set_service($this);
    $this->requirements[$server->name] = array('server' => $server, 'multiplicity' => $multiplicity);
  }

}

