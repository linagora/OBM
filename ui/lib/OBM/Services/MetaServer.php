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
 * Class used to describe meta-data of servers (built by the OBM_Services class)
 **/
class OBM_Services_MetaServer extends OBM_Services_MetaElement {
  protected $service;

  /**
   * service getter
   * @access public
   * @return int
   **/
  public function get_service() {
    return $this->service;
  }

  /**
   * service setter
   * @param  OBM_Services_MetaService $service
   * @access public
   **/
  public function set_service(OBM_Services_MetaService $service) {
    $this->service = $service;
  }

}

