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



///////////////////////////////////////////////////////////////////////////////
// OBM - File : php/campaign/ws/campaignws.class.php
//     - Desc : campaign web service
// 2009-02-11 Christophe Liou Kee On
///////////////////////////////////////////////////////////////////////////////
// $Id:  $ //
///////////////////////////////////////////////////////////////////////////////


class CampaignWS {

  var $campaign_status_enum = array (
    'created' => 0,
    'ready' => 1,
    'running' => 2,
    'planified' => 3,
    'finished' => 4,
    'archived' => 5,
      'error_mail_format' => 6,
    'error_target' => 7,
    'error' => 8,
  );
  
  var $c_campaign_entity_group = 1;
  var $c_campaign_entity_user = 2;
  var $c_campaign_entity_list = 3;
  var $c_campaign_entity_contact = 4;
  
  /**
   * For each targets enqueued in the push mail
   *   apply $method[1] method from $method[0] object instance
   *
   * @param string $campaign_ids
   * @return array<Target>
   */

  function eachTargets($campaign_ids, $method) {
    $obm_q = new DB_OBM;

    // Get campaigns id concern about push mail

    //    $campaign_ids = array();
    //
    //
    //    $query = "SELECT campaign_id
    //    FROM Campaign
    //    WHERE campaign_status = " . $this->campaign_status_enum['planified'] . "
    //      AND campaign_start_date <= '$time';";
    //
    //    $obm_q->query($query);
    //    while ($obm_q->next_record()) {
    //      $campaign_ids[] = $obm_q->f('campaign_id');
    //    }
    //
    //    if (empty($campaign_ids))
    //      return $res;

    // Get targets from users
    
    $query = "
    SELECT
      campaigntarget_entity_id as entity_id,
      campaigntarget_campaign_id as campaign_id,
      userobm_email as email,
      domain_name as domain,
      campaign_timeupdate as timeupdate
    FROM CampaignTarget
    INNER JOIN Campaign
      ON campaigntarget_campaign_id = campaign_id
    INNER JOIN UserEntity
      ON userentity_entity_id = campaigntarget_entity_id
    INNER JOIN UserObm
      ON userentity_user_id = userobm_id
    INNER JOIN Domain
      ON userobm_domain_id = domain_id
    WHERE campaigntarget_campaign_id IN (" . join(',', $campaign_ids) . ")
      AND userobm_email IS NOT NULL
    ";
    
    $obm_q->query($query);
    while ($obm_q->next_record()) {
      $emails = split("\n", $obm_q->f('email'));

      $newTarget = new Target();

      if (strpos(trim($emails[0]), '@') == -1)
      $newTarget->emailAddress = trim($emails[0]). '@' . $obm_q->f('domain');
      else
      $newTarget->emailAddress = trim($emails[0]);

      $newTarget->emailContentId = $obm_q->f('campaign_id');
      $newTarget->time = ($obm_q->f('timeupdate'));

      $newTarget->infos['type'] = 'User';
      $newTarget->infos['id'] = $obm_q->f('entity_id');
      
      $method[0]->$method[1]($newTarget);
    }


    // Get targets from groups
    
    $query = "
    SELECT
      userentity_entity_id as entity_id,
      campaigntarget_campaign_id as campaign_id,
      userobm_email as email,
      domain_name as domain,
      campaign_timeupdate as timeupdate
    FROM CampaignTarget
    INNER JOIN Campaign
      ON campaigntarget_campaign_id = campaign_id
    INNER JOIN GroupEntity
      ON groupentity_entity_id = campaigntarget_entity_id
    INNER JOIN UserObmGroup
      ON userobmgroup_group_id = groupentity_group_id
    INNER JOIN UserObm
      ON userobmgroup_userobm_id = userobm_id
    INNER JOIN UserEntity
      ON userentity_user_id = userobm_id
    INNER JOIN Domain
      ON userobm_domain_id = domain_id
    WHERE campaigntarget_campaign_id IN (" . join(',', $campaign_ids) . ")
      AND userobm_email IS NOT NULL
      ";
    
    $obm_q->query($query);
    while ($obm_q->next_record()) {
      $emails = split("\n", $obm_q->f('email'));

      $newTarget = new Target();

      if (strpos(trim($emails[0]), '@') == -1)
      $newTarget->emailAddress = trim($emails[0]). '@' . $obm_q->f('domain');
      else
      $newTarget->emailAddress = trim($emails[0]);

      $newTarget->emailContentId = $obm_q->f('campaign_id');
      $newTarget->time = ($obm_q->f('timeupdate'));

      $newTarget->infos['type'] = 'User';
      $newTarget->infos['id'] = $obm_q->f('entity_id');

      $method[0]->$method[1]($newTarget);
    }


    // Get targets from lists
    foreach ($campaign_ids as $campaign_id) {
      if ($GLOBALS['obm_version'] < 2.2) {
        $query = "
        SELECT
          list_id as entity_id,
          list_query,
          campaign_timeupdate as timeupdate
        FROM CampaignTarget
        INNER JOIN Campaign
          ON campaigntarget_campaign_id = campaign_id
        INNER JOIN List
          ON campaigntarget_entity_id = list_id
        WHERE
          campaigntarget_campaign_id = $campaign_id
          AND campaigntarget_entity = $this->c_campaign_entity_list
        ";
        
      } else {
        $query = "
        SELECT
          list_id,
          campaigntarget_entity_id as entity_id,
          list_query,
          campaign_timeupdate as timeupdate
        FROM CampaignTarget
        INNER JOIN Campaign
          ON campaigntarget_campaign_id = campaign_id
        INNER JOIN ListEntity
          ON listentity_entity_id = campaigntarget_entity_id
        INNER JOIN List
          ON listentity_list_id = list_id
        WHERE
          campaigntarget_campaign_id = $campaign_id
        ";
        
      }
      
      $lists = array();

      $obm_q->query($query);

      while ($obm_q->next_record()) {
        $lists[] = array(
          'query' => $obm_q->f('list_query'),
          'entity_id' => $obm_q->f('entity_id'),
          'timeupdate' => $obm_q->f('timeupdate'),
        );
      }


      global $cgp_host;
      /*
       * 'http://'. $cgp_host. ($_SERVER['SERVER_PORT'] == 80 ? '': ':'
       . $_SERVER['SERVER_PORT']). preg_replace('/(.*)\/campaign\/ws.+/', '\1',
       $_SERVER['SCRIPT_NAME'])
       */
      $base_url = $cgp_host. 'public/contact/public_contact_index.php?key=';

      foreach ($lists as $list) {
        $query = $list['query'];
        $timeupdate = $list['timeupdate'];
        $list_id = $list['list_id'];
        
        if (!empty($query)) {
          $matches = (preg_split('/SELECT | FROM | WHERE /', $query));
          
          $query = "SELECT DISTINCT
            contact_id as entity_id,
            contact_id,
            contact_email,
            contact_firstname,
            contact_lastname
            FROM $matches[2]
            WHERE $matches[3]";
            
          $query = preg_replace('/ [0-9]+ /', '\'\1\'', $query);
        
          $query = "SELECT DISTINCT
            contactentity_id as entity_id,
            contact_id,
            contact_email,
            contact_firstname,
            contact_lastname
            FROM $matches[2]
            INNER JOIN ContactEntity ON contactentity_contact_id = contact_id
            WHERE $matches[3]";
        }
        
        if (!empty($query)) {
          $query .= ' UNION ';
        }
        
        $query .= "SELECT
          contactentity_id as entity_id,
          contact_id,
          contact_email,
          contact_firstname,
          contact_lastname
          FROM Contact
          INNER JOIN ContactEntity ON contactentity_contact_id = contact_id
          INNER JOIN contactlist
            ON contactlist_contact_id = contact_id
            AND contactlist_list_id = $list_id";
        
        if (!@$obm_q->query($query))
        throw new Exception('Unvalid list query.');

        while ($obm_q->next_record()) {
          $newTarget = new Target();

          $newTarget->emailAddress = $obm_q->f('contact_email');
          $newTarget->emailContentId = $campaign_id;
          $newTarget->time = $timeupdate;

          $newTarget->infos['type'] = 'Contact';
          $newTarget->infos['id'] = $obm_q->f('entity_id');

          $newTarget->infos['fullname'] = $obm_q->f('contact_firstname'). ' '. $obm_q->f('contact_lastname');
          
          $method[0]->$method[1]($newTarget);
        }
      }
    }
  }

  function listCampaignEmails ($time) {
    $res = array();

    $obm_q = new DB_OBM;

    $query = "SELECT campaign_id, campaign_email
    FROM Campaign
    WHERE campaign_status = 3
    AND campaign_start_date <= '$time';";

    $obm_q->query($query);
    while ($obm_q->next_record()) {
      $new = new CampaignEmail();

      $new->campaignId = $obm_q->f('campaign_id');
      $new->documentId = $obm_q->f('campaign_email');

      $res[] = $new;
    }

    return $res;
  }

  function setCampaignRunningStatus ($time) {
    $res = array();

    $obm_q = new DB_OBM;

    $query = "UPDATE Campaign
      SET campaign_status = " . $this->campaign_status_enum['running'] . "
    WHERE campaign_status = " . $this->campaign_status_enum['planified'] . "
    AND campaign_start_date <= '$time';";

    $obm_q->query($query);

    return $res;
  }

  function setCampaignsStatus ($campaign_ids, $status) {
    $res = array();

    $obm_q = new DB_OBM;

    if ($status != 'error_mail_format') {
      $query = "UPDATE Campaign
        SET campaign_status = " . $this->campaign_status_enum[$status] . "
      WHERE campaign_id IN (" . join(',', $campaign_ids) . ");";

      $obm_q->query($query);

    } else {
      $query = "SELECT campaign_email FROM Campaign WHERE campaign_id IN (" . join(',', $campaign_ids) . ");";
      $obm_q->query($query);

      while ($obm_q->next_record()) {
        $document_id = $obm_q->f('campaign_email');
        run_query_global_delete_document($document_id);
      }

      $query = "UPDATE Campaign
        SET campaign_status = " . $this->campaign_status_enum[$status] . ",
          campaign_email = NULL
      WHERE campaign_id IN (" . join(',', $campaign_ids) . ");";

      $obm_q->query($query);
    }

    return $res;
  }

  function reportMailProgress ($campaign_id, $nb_in_queue, $nb_sent, $nb_error) {
    $obm_q = new DB_OBM;
    if ($nb_in_queue == 0) {
      $query = "UPDATE Campaign SET
        campaign_status = " . $this->campaign_status_enum['finished'] . ",
        campaign_progress = 100
      WHERE campaign_id = " . $campaign_id . ";";
    } else {
      $query = "UPDATE Campaign
        SET campaign_progress = " . round(($nb_sent + $nb_error) / ($nb_sent + $nb_error + $nb_in_queue) * 100) . "
      WHERE campaign_id = " . $campaign_id . ";";
    }

    $obm_q->query($query);
  }
}

?>