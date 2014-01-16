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