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

///////////////////////////////////////////////////////////////////////////////
// OBM - File : php/campaign/ws/classes.php
//     - Desc : campaign web service
// 2008-02-11 Christophe Liou Kee On
///////////////////////////////////////////////////////////////////////////////
// $Id:  $ //
///////////////////////////////////////////////////////////////////////////////

require_once 'campaignws.class.php';

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