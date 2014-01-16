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



include_once('CronJob.class.php');

global $obminclude; 

class DelegationGroups extends CronJob {
  /**
   * @var Logger
   */
  var $logger;

  function mustExecute($date) {
    $delta   = 10; //every 10 minutes
    $instant =  0; //at 0:00, 0:10, etc
    $min = (int)($date/60);
    return ($min%$delta === $instant);
  }

  function execute($date) {
    $this->logger->debug('Updating groups built from delegations');
    $domains = $this->getDomains();
    foreach ($domains as $domain_id) {
      $this->processDomain($domain_id);
    }
  }

  protected function processDomain($domain_id) {
    global $cmd_update;
    $delegations = $this->buildDelegationsList($domain_id);

    $groups = array();
    foreach ($delegations as $delegation => $parent) {
      list($group_id, $group_name) = $this->getDelegationGroup($domain_id, $delegation);
      $this->purgeGroupMembers($group_id);
      $this->populateGroup($domain_id, $group_id, $delegation);
      if ($parent) {
        list($parent_id, $parent_name) = $this->getDelegationGroup($domain_id, $parent);
        $this->associatesDelegationGroups($parent_id, $group_id);
      }
      $groups[$group_id] = "group:$group_name";
    }

    foreach ($groups as $group_id => $ignore) {
      of_usergroup_update_group_node($group_id);
    }

    // automate
    if (sizeof($groups) > 0) {
      exec("echo '".implode($groups, "\n")."' | $cmd_update --domain-id $domain_id --entity");
    }

  }

  public function delegationGroupName($delegation) {
    include("obminclude/lang/".$GLOBALS[ini_array][lang]."/global.inc");
    if ($delegation == "/") {
      $name = "Tous";
    } else {
      $name = "Tous ".$delegation;
    }
    return $name;
  }

  protected function associatesDelegationGroups($parent_id, $child_id) {
    $obm_q = new DB_OBM;
    $query = "INSERT INTO GroupGroup (groupgroup_parent_id, groupgroup_child_id)
      VALUES ($parent_id, $child_id)";
    $this->logger->core($query);
    $obm_q->query($query);
  }

  protected function populateGroup($domain_id, $group_id, $delegation) {
    $obm_q = new DB_OBM;
    $query = "INSERT INTO UserObmGroup (userobmgroup_group_id, userobmgroup_userobm_id)
      SELECT $group_id, userobm_id
      FROM UserObm
      WHERE userobm_domain_id=$domain_id AND (userobm_delegation='$delegation' OR userobm_delegation='$delegation/')";
    $this->logger->core($query);
    $obm_q->query($query);
  }

  protected function purgeGroupMembers($group_id) {
    $obm_q = new DB_OBM;
    $query = "DELETE FROM UserObmGroup WHERE userobmgroup_group_id=$group_id";
    $this->logger->core($query);
    $obm_q->query($query);
    $query = "DELETE FROM GroupGroup WHERE groupgroup_parent_id=$group_id OR groupgroup_child_id=$group_id";
    $this->logger->core($query);
    $obm_q->query($query);
  }

  protected function getDelegationGroup($domain_id, $delegation) {
    $groupName = $this->delegationGroupName($delegation);
    $obm_q = new DB_OBM;
    $query = "SELECT group_id, group_name FROM UGroup WHERE group_domain_id=$domain_id AND group_name='$groupName'";
    $this->logger->core($query);
    $obm_q->query($query);
    if ($obm_q->next_record()) {
      return array($obm_q->f('group_id'), $obm_q->f('group_name'));
    }

    // The group does not exists, we create !
    return $this->createGroup($domain_id, $delegation);
  }

  protected function createGroup($domain_id, $delegation) {
    $groupName = $this->delegationGroupName($delegation);
    $gid = sql_parse_int(get_first_group_free_gid());
    $q_delegation = of_delegation_query_insert_clauses('group_delegation', $delegation);
    $group_email = preg_replace('/Tous /', 'Tous', $groupName);
    $group_email = preg_replace('/\//', '-', $group_email);
    $group_email = preg_replace('/\s/', '', $group_email);

    $query = "INSERT INTO UGroup (
    group_timeupdate,
    group_timecreate,
    group_userupdate,
    group_usercreate,
    group_domain_id,
    group_privacy,
    group_gid,
    group_name
    $q_delegation[field],
    group_email,
    group_desc
    ) VALUES (
    null,
    NOW(),
    null,
    1,
    $domain_id,
    0,
    $gid,
    '$groupName'
    $q_delegation[value],
    '$group_email',
    ''
    )";
    $this->logger->core($query);
    $obm_q = new DB_OBM;
    $retour = $obm_q->query($query);
    $id = $obm_q->lastid();
    if ($id > 0) {
      $entity_id = of_entity_insert('group', $id);  
    }
    return array($id, $groupName);
  }

  protected function buildDelegationsList($domain_id) {
    $separator = '/';
    $delegations = array();
    $obm_q = new DB_OBM;
    $query = "SELECT DISTINCT userobm_delegation FROM UserObm WHERE userobm_domain_id=$domain_id";
    $this->logger->core($query);
    $obm_q->query($query);

    while ($obm_q->next_record()) {
      $data = explode($separator,$obm_q->f('userobm_delegation'));
      $delegation = "$separator";
      $parent = false;
      foreach ($data as $current) {
        $delegation.= $separator.$current;
        if ($delegation[strlen($delegation)-1]==$separator) {
          $delegation = substr($delegation, 0, -1);
        }
        $delegation = str_replace($separator.$separator,$separator,$delegation);
        if (!isset($delegations[$delegation]))
          $delegations[$delegation] = $parent;
        $parent = $delegation;
      }
    }

    $obm_q = new DB_OBM;
    $query = "SELECT domainpropertyvalue_value FROM DomainPropertyValue WHERE domainpropertyvalue_property_key='delegation' AND domainpropertyvalue_domain_id=".$domain_id;
    $this->logger->core($query);
    $obm_q->query($query);

    if($obm_q->next_record()) {
        $main_delegation = $obm_q->f('domainpropertyvalue_value');
    }

    // Remove ending '/' on main delegation
    $main_delegation = preg_replace('/\/$/', '', $main_delegation);
    foreach ($delegations as $current => $value) {
        if(strcmp($separator, $current)==0) {
	    unset($delegations[$current]);
            continue;
        }

        if(strcmp($main_delegation, $current)>0) {
            unset($delegations[$current]);
        }elseif(strcmp($main_delegation, $current)==0) {
            $delegations[$current] = false;
        }
    }

    return $delegations;
  }

  protected function getDomains() {
    $domains = array();
    $obm_q = new DB_OBM;
    $query = "SELECT domain_id FROM Domain WHERE domain_global=0";
    $this->logger->core($query);
    $obm_q->query($query);

    while ($obm_q->next_record()) {
      $domains[] = $obm_q->f('domain_id');
    }
    return $domains;
  }
}
?>
