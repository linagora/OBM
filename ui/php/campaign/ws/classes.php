<?php

require_once "$path/public/contact/public_contact_query.inc";

/**
 * XML render for CampaignWS class
 *
 */
class CampaignWSHM extends CampaignWS {
  /**
   * call method used in listTargets method
   *
   * @param Target $target
   */
  function eachTargetsCallback ($target) {
    echo "<target>";
    echo "<emailAddress>" . $target->emailAddress . "</emailAddress>";
    echo "<emailContentId>" . $target->emailContentId . "</emailContentId>";
    echo "<time>" . $target->time . "</time>";
    $xml_infos = "";
    foreach ($target->infos as $k => $v) {
      $xml_infos .= "<info>";
      $xml_infos .= "<key>$k</key>";
      $xml_infos .= "<value>$v</value>";
      $xml_infos .= "</info>";
    }
    echo "<infos>" . $xml_infos . "</infos>";
    echo "</target>";
  }

  /**
   * list targets
   *
   * @param array $campaign_ids
   */
  function listTargets($campaign_ids) {
    echo "<?xml version='1.0'?>";
    echo "<targets>";
    try {
      parent::eachTargets($campaign_ids, array($this, 'eachTargetsCallback'));
    } catch (Exception $e) {
      echo '<error>', $e->getMessage(), '</error>';
    }
    echo "</targets>";
  }

  /**
   * list today's campaigns to process
   *
   * @param array $campaign_ids
   */
  function listTodayCampaignEmails() {
    echo "<?xml version='1.0'?>";
    echo "<campaignEmails>";
    foreach (parent::listCampaignEmails(date("Y-m-d H:i:s")) as $e) {
      echo "<campaignEmail>";
      echo "<campaignId>" . $e->campaignId . "</campaignId>";
      echo "<documentId>" . $e->documentId . "</documentId>";
      echo "</campaignEmail>";
    }
    echo "</campaignEmails>";
  }

  /**
   * set campaigns status
   *
   * @param array $campaign_ids
   * @param int $status
   */
  function setCampaignsStatus($campaign_ids, $status) {
    echo "<?xml version='1.0'?>";
    echo "<success/>";
    parent::setCampaignsStatus($campaign_ids, $status);
  }

  /**
   * get binary stream from document_id
   *
   * @param int $document_id
   */
  function getMailDocument($document_id) {
    global $path;
    require "$path/document/document_query.inc";
    require "$path/document/document_display.inc";

    if ($document_id > 0) {
      $doc_q = run_query_document_detail($document_id);

      if ($doc_q->num_rows() == 1) {
        dis_document_file($doc_q);
        exit();
      }
    }
  }
}

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
      
    ";
    //FIXME AND userobm_email IS NOT NULL
    
    $obm_q->query($query);
    while ($obm_q->next_record()) {
      //FIXME $emails = split("\n", $obm_q->f('email'));
      $emails[] = 'clioukeeon@linagora.com';

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
      $query = "
      SELECT
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
        $entity_id = $list['entity_id'];
        
        preg_match('/^SELECT (.+) FROM (.+) WHERE (.+)$/', $query, $matches);
        /*
        $query = str_replace("\n", ' ', $query);
        $pos = strpos($query, ' FROM ');
        $query = substr($query, $pos, strlen($query) - $pos);
*/
        $query = "SELECT DISTINCT
          contact_id as entity_id,
          contact_id,
          contact_email,
          contact_firstname,
          contact_lastname
          FROM $matches[2]
          JOIN ContactEntity ON contactentity_contact_id = contact_id
          WHERE $matches[3]";

        if (!@$obm_q->query($query))
        throw new Exception('Unvalid list query.');

        while ($obm_q->next_record()) {
          $newTarget = new Target();

          $newTarget->emailAddress = $obm_q->f('contact_email');
          $newTarget->emailContentId = $campaign_id;
          $newTarget->time = $timeupdate;

          $newTarget->infos['type'] = 'Contact';
          $newTarget->infos['id'] = $obm_q->f('entity_id');

          $newTarget->infos['contact_edit_url'] = $base_url
          . public_contact_make_editkey($obm_q->f('contact_id'));

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

class CampaignEmail {
  /**
   * @var string
   */
  public $campaignId;
  /**
   * @var string
   */
  public $documentId;
}

class Target {
  /**
   * @var string
   */
  public $emailAddress;
  /**
   * @var string
   */
  public $emailContentId;
  /**
   * @var string
   */
  public $time;
  /**
   * @var string
   */
  public $infos;



  function Target () {
    $infos = array();
  }

  function setMySQLDateTime ($str) {
    $this->time = $this->convert_datetime($str);
  }

  function convert_datetime($str) {
    list($date, $time) = explode(' ', $str);
    list($year, $month, $day) = explode('-', $date);
    list($hour, $minute, $second) = explode(':', $time);

    $timestamp = mktime($hour, $minute, $second, $month, $day, $year);

    return $timestamp;
  }
}



?>