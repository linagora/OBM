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
  }

  function startup($args)
  {
    $rcmail = rcmail::get_instance();

    if (isset($_SESSION['obm_user_id']) && is_numeric($_SESSION['obm_user_id'])) {
      $query = 'SELECT userobm_password_type FROM userobm WHERE userobm_id='.$_SESSION['obm_user_id'];

      $obm_q = new DB_OBM;
      $obm_q->query($query);

      $obm_q->next_record();
      if ($obm_q->f('userobm_password_type') == 'PLAIN') {
        $rcmail->config->set('obm_user_id', $_SESSION['obm_user_id']);
        $args['action'] = 'login';
      }
    }

    return $args;
  }

  function authenticate($args)
  {
    $rcmail = rcmail::get_instance();

    if (!empty($args['user'])) {
        return $args;
    }

    $obm_user_id = $rcmail->config->get('obm_user_id');

    if ($obm_user_id && is_numeric($obm_user_id)) {
      $obm_q = new DB_OBM;

      $query = 'SELECT userobm_email, userobm_password, domain_name 
                FROM UserObm 
                INNER JOIN Domain ON userobm_domain_id=domain_id 
                WHERE userobm_id='.$obm_user_id;
                
      $obm_q->query($query);

      $obm_q->next_record();
      $username_array = explode("\n", $obm_q->f('userobm_email'));
      $username_array = array_map('trim', $username);
      $args['user'] = $username_array[0].'@'.$obm_q->f('domain_name');
      $args['pass'] = $obm_q->f('userobm_password');
      $args['cookiecheck'] = false;
      $args['valid'] = true;
    }

    return $args;
  }
  
}

