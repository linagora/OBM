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



/**
 * Class used to store user data
 * 
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2009 Groupe LINAGORA
 * @author Vincent Alquier <vincent.alquier@aliasource.fr> 
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
