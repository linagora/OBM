<?php
/******************************************************************************
Copyright (C) 2016 Linagora

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
 * This plugin provides multidomain support for Roundcube.
 * It fetches the following information from the OBM database for the currently logged-in user in OBM:
 *  - IMAP host
 *  - SMTP host
 *  - OBM-Sync host
 *
 * This works by initially sending the userobm_id of the logged-in user through a GET parameter to the iframe
 * hosting Roundcube. The identifier is then transimitted to the client via a cookie, so that it is transmitted
 * back on each request (this allows to survive the initial PHP session creation). The cookie is destroyed when
 * the user logs out and the Roundcube session is destroyed.
 *
 * Once the information is fetched from the database, it is stored in the PHP session to be reused later on.
 *
 * @author OBM Team <lgs-obm-dev@linagora.com>
 */
class obm_multidomain extends rcube_plugin
{
  private static $servicePropertyToConfigKey = array(
    'imap_frontend' => 'default_host',
    'smtp_out' => 'smtp_server',
    'obm_sync' => 'obmSyncIp'
  );

  function init()
  {
    $rcmail = rcmail::get_instance();

    if (!isset($_SESSION['obm_hosts'])) {
      $id = $this->findUserObmId();

      if (!$id) {
        $this->log('Cannot read userobm_id');

        return;
      }

      setcookie('userobm_id', $id);

      $obm_q = new DB_OBM;
      $obm_q->query("
        SELECT serviceproperty_property, host_ip
        FROM UserObm
        INNER JOIN Domain ON domain_id = userobm_domain_id
        INNER JOIN DomainEntity ON domainentity_domain_id = domain_id
        LEFT JOIN ServiceProperty ON serviceproperty_entity_id = domainentity_entity_id
        LEFT JOIN Host ON host_id = CAST(serviceproperty_value AS INTEGER)
        WHERE serviceproperty_property IN ('imap_frontend', 'smtp_out', 'obm_sync') AND userobm_id = " . $obm_q->escape($id) . ";
      ");

      $logMessage = "Setting configuration for userobm_id=$id";

      while ($obm_q->next_record()) {
        $key = obm_multidomain::$servicePropertyToConfigKey[$obm_q->f('serviceproperty_property')];
        $value = $this->formatUrl($rcmail->config->get($key . '_scheme', ''), $obm_q->f('host_ip'));

        $_SESSION['obm_hosts'][$key] = $value;
        $logMessage .= " $key=$value";
      }

      $this->log($logMessage);
    }

    $rcmail->config->merge($_SESSION['obm_hosts']);
    $this->add_hook('logout_after', array($this, 'logout_after'));
  }

  function logout_after() {
    setcookie('userobm_id', false, time() - 3600);
  }

  private function log($message) {
    rcmail::write_log('obm_multidomain', $message);
  }

  private function formatUrl($scheme, $host) {
    return $scheme ? sprintf("%s://%s", strtolower($scheme), $host) : $host;
  }

  private function findUserObmId() {
    if (isset($_COOKIE['userobm_id'])) {
      return $_COOKIE['userobm_id'];
    }

    if (isset($_GET['userobm_id'])) {
      return $_GET['userobm_id'];
    }

    return null;
  }
}
