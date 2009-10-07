<?php
/*
 +-------------------------------------------------------------------------+
 |  Copyright (c) 1997-2009 OBM.org project members team                   |
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
?>
<?php

/**
 * Class used to store group data
 * 
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2009 Aliasource - Groupe LINAGORA
 * @author Vincent Alquier <vincent.alquier@aliasource.fr> 
 * @license GPL 2.0
 */
class Group {
  private $id;
  private $domain_id;
  private $domain_name;
  private $timecreate;
  private $timeupdate;
  private $usercreate_id;
  private $usercreate_login;
  private $userupdate_id;
  private $userupdate_login;
  private $system;
  private $archive;
  private $privacy;
  private $local;
  private $ext_id;
  private $samba;
  private $gid;
  private $mailing;
  private $delegation;
  private $manager_id;
  private $name;
  private $desc;
  private $email;
  private $contacts;
  private $nb_user;

  public function __set($var, $val) {
    $this->$var = $val;
  }

  public function __get($var) {
    return $this->$var;
  }

}
