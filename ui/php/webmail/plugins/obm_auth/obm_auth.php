<?php
/******************************************************************************
Copyright (C) 2011-2012 Linagora

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

class obm_auth extends rcube_plugin
{
  public $task = 'login|logout';

  function init()
  {
    $this->add_hook('startup', array($this, 'startup'));
    $this->add_hook('authenticate', array($this, 'authenticate'));
    $this->add_hook('logout_after', array($this, 'logout_after'));
  }

  function startup($args)
  {
    $rcmail = rcmail::get_instance();
    if( isset($_GET['obm_token']) ){
      $args['action'] = 'login';
    }

    return $args;
  }

  function authenticate($args)
  {
    $rcmail = rcmail::get_instance();

    if (!empty($args['user'])) {
        return $args;
    }
    $token = $_GET['obm_token'];
    $obm_q = new DB_OBM;

    $query = 'SELECT userobm_login, userobm_password, domain_name
              FROM TrustToken AS t
              INNER JOIN UserObm AS u ON t.userobm_id = u.userobm_id
              INNER JOIN Domain ON userobm_domain_id = domain_id
              WHERE token=\''.$token.'\';';
              
    $obm_q->query($query);

    $obm_q->next_record();

    $user_login = $obm_q->f('userobm_login').'@'.$obm_q->f('domain_name');
    $args['user'] = $user_login;
    $args['pass'] = $obm_q->f('userobm_password');
    $args['cookiecheck'] = false;
    $args['valid'] = true;
  
    return $args;
  }

  function logout_after($args){
    // TODO: check le nom de cookie OBM
    setcookie('OBM_Session', false, time()-3600 );

    return $args;
  }
  
}

