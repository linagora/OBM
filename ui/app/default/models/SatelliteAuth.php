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
class SatelliteAuth extends OBM_Satellite_ICredentials {

  /**
   * standard constructor
   * @access public
   **/
  public function __construct($user='') {
    global $cdg_sql;

    if (empty($user)) {
      $user = 'obmsatelliterequest';
    }
    $obm_q = new DB_OBM;
    $query = "SELECT usersystem_password as password FROM UserSystem WHERE usersystem_login='$user'";
    display_debug_msg($query, $cdg_sql, "SatelliteCredentials::__construct()");
    $obm_q->query($query);

    if (!$obm_q->next_record()) {
      throw new Exception("Can't find {$user} system user");
    }
    //else
    parent::__construct($user, $obm_q->f('password'));
  }

}

