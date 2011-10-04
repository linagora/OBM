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
 * Class used to store user data
 * 
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2009 Aliasource - Groupe LINAGORA
 * @author Vincent Alquier <vincent.alquier@aliasource.fr> 
 * @license GPL 2.0
 */
class User {
  private $id;
  private $domain_id;
  private $domain_name;
  private $timecreate;
  private $timeupdate;
  private $usercreate_id;
  private $usercreate_login;
  private $userupdate_id;
  private $userupdate_login;
  private $local;
  private $ext_id;
  private $system;
  private $archive;
  private $status;
  private $timelastaccess;
  private $login;
  private $nb_login_failed;
  private $password_type;
  private $password_dateexp;
  private $account_dateexp;
  private $perms;
  private $delegation_target;
  private $delegation;
  private $calendar_version;
  private $uid;
  private $gid;
  private $datebegin;
  private $hidden;
  private $kind;
  private $lastname;
  private $firstname;
  private $title;
  private $sound;
  private $company;
  private $direction;
  private $service;
  private $address1;
  private $address2;
  private $address3;
  private $zipcode;
  private $town;
  private $expresspostal;
  private $country_iso3166;
  private $phone;
  private $phone2;
  private $mobile;
  private $fax;
  private $fax2;
  private $web_perms;
  private $web_list;
  private $web_all;
  private $mail_perms;
  private $mail_ext_perms;
  private $email;
  private $mail_server_id;
  private $mail_server_name;
  private $mail_quota;
  private $mail_quota_use;
  private $mail_login_date;
  private $nomade_perms;
  private $nomade_enable;
  private $nomade_local_copy;
  private $email_nomade;
  private $vacation_enable;
  private $vacation_datebegin;
  private $vacation_dateend;
  private $vacation_message;
  private $samba_perms;
  private $samba_home;
  private $samba_home_drive;
  private $samba_logon_script;
  private $host_id;
  private $description;
  private $location;
  private $education;
  private $has_photo;

  public function __set($var, $val) {
    $this->$var = $val;
  }

  public function __get($var) {
    return $this->$var;
  }

}
