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

include_once("CronJob.class.php");

/**
 * ContactIndexingJob 
 * 
 * @uses CronJob
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2007 Aliasource - Groupe LINAGORA
 * @author David Phan <david.phan@aliasource.fr> 
 * @license GPL 2.0
 */
class ContactIndexingJob extends CronJob {

  var $jobDelta = 120;

  /**
   * mustExecute 
   * 
   * @param mixed $date 
   * @access public
   * @return void
   */
  function mustExecute($date) {
    $date = new Of_Date($date);
    $min = date("i");
    $modulo = $this->jobDelta / 60;
    return ($min%$modulo === 0);
  }

  /**
   * getJobsFiles 
   * 
   * @access public
   * @return void
   */
  function getJobsFiles() {
    return array('obminclude/lib/Solr/Service.php');
  }

  /**
   * execute 
   * 
   * @param mixed $date 
   * @access public
   * @return void
   */
  function execute($date) {
    global $obm;

    $db = new DB_OBM;

    $query = "SELECT obminfo_value FROM ObmInfo WHERE obminfo_name='solr_contact_lastupdate'";
    $solr_contact_lastupdate = false;
    $res = $db->query($query);
    if($db->next_record()) {
      $solr_contact_lastupdate = $db->f('obminfo_value');
    }

    $query = "SELECT obminfo_value FROM ObmInfo WHERE obminfo_name='solr_lastcontact'";
    $solr_lastcontact = 0;
    $res = $db->query($query);
    if($db->next_record()) {
      $solr_lastcontact = $db->f('obminfo_value');
    }
    $maxid=$solr_lastcontact;
    $limit = 2000;

    $this->logger->debug("Execute ContactIndexingJob");
    $this->logger->debug("Last indexed contact : $solr_lastcontact");
    $this->logger->debug("Last indexed time : $solr_contact_lastupdate");

    // Get domain solr server
    $domains = of_domain_get_list();
    foreach($domains as $domain_id => $domain) {
      $solrservers = of_domain_get_domain_solrserver($domain_id, 'event');
      foreach ($solrservers as $s_id => $solrserver_info) {
	      foreach ($solrserver_info as $server_info) {
          $ip = $server_info['ip']; 
        }
      }      
      if($ip) $servers[$domain_id] = $ip;
    }


    if (is_array($servers)) {
      foreach($servers as $domain => $ip) {
        $solr = new Apache_Solr_Service($ip, '8080', '/solr/contact' );    
        if (!$solr->ping()) {
          $this->logger->warn("Solr server for domain $domain not responding ($ip)");
          return;
        }
        $this->logger->info("Solr server for domain $domain : $ip");

        $select = "SELECT Contact.*,
          Addressbook.name as addressbook_name,
          Company.company_name as company_name,
          Kind.*,
          ContactFunction.contactfunction_label
          FROM Contact 
          LEFT JOIN Addressbook ON contact_addressbook_id=id
          LEFT JOIN Company ON contact_company_id=company_id
          LEFT JOIN Kind ON contact_kind_id=kind_id
          LEFT JOIN ContactFunction ON contact_function_id=contactfunction_id
          WHERE contact_domain_id='$domain'";

        if (!$solr_contact_lastupdate) {
          $query = "$select ORDER BY contact_id LIMIT $limit";
        } else {
          $d = new Of_Date($solr_contact_lastupdate);
          $query = "$select AND (contact_id > $solr_lastcontact) 
           OR (contact_timecreate >= '$d' OR contact_timeupdate >= '$d')  
           ORDER BY contact_id LIMIT $limit";
        }

        $db->xquery($query);
        $documents = array();
        while($db->next_record()) {
          $doc = new Apache_Solr_Document();
          $id = $db->f('contact_id');
          $maxid = $id;

          $doc->setField('id', $id);
          $timecreate = new Of_Date($db->f('contact_timecreate'));
          $doc->setField('timecreate', $timecreate->format('Y-m-d\TH:i:s\Z'));
          $timeupdate = new Of_Date($db->f('contact_timeupdate'));
          $doc->setField('timeupdate', $timeupdate->format('Y-m-d\TH:i:s\Z'));
          $doc->setField('usercreate', $db->f('contact_usercreate'));
          $doc->setField('userupdate', $db->f('contact_userupdate'));
          $doc->setField('datasource', $db->f('contact_datasource'));
          $doc->setField('domain', $db->f('contact_domain_id'));
          $doc->setField('in', $db->f('addressbook_name'));
          $doc->setField('addressbookId', $db->f('contact_addressbook_id'));
          $doc->setField('company', $db->f('company_name'));
          $doc->setField('companyId', $db->f('contact_company_id'));
          $doc->setField('lastname', $db->f('contact_lastname'));
          $doc->setField('firstname', $db->f('contact_firstname'));
          $doc->setField('middlename', $db->f('contact_middlename'));
          $doc->setField('suffix', $db->f('contact_suffix'));
          $doc->setField('aka', $db->f('contact_aka'));
          $doc->setField('kind', $db->f('kind_minilabel'));
          $doc->setField('kind', $db->f('kind_header'));
          $doc->setField('manager', $db->f('contact_manager'));
          $doc->setField('assistant', $db->f('contact_assistant'));
          $doc->setField('spouse', $db->f('contact_spouse'));
          $doc->setField('category', $db->f('contact_category'));
          $categories = self::getCategories($id);
          while($categories->next_record()){
            $doc->setMultiValue('category', $categories->f('category_label'));
          }
          $doc->setField('service', $db->f('contact_service'));
          $doc->setField('function', $db->f('contactfunction_label'));
          $doc->setField('title', $db->f('contact_title'));
          if ($db->f('contact_archive')) {
            $doc->setField('is', 'archive');
          }
          if ($db->f('contact_collected')) {
            $doc->setField('is', 'collected');
          }
          if ($db->f('contact_mailing_ok')) {
            $doc->setField('is', 'mailing');
          }        
          if ($db->f('contact_newsletter')) {
            $doc->setField('is', 'newsletter');
          }
          $date = new Of_Date($db->f('contact_date'));
          $doc->setField('date', $date->format('Y-m-d\TH:i:s\Z'));
          $doc->setField('comment', $db->f('contact_comment'));
          $doc->setField('comment2', $db->f('contact_comment2'));
          $doc->setField('comment3', $db->f('contact_comment3'));
          $doc->setField('from', $db->f('contact_origin'));
          $coords = self::getCoords($id);
          $emails = $coords['emails'];
          foreach($emails as $email) {
            $doc->setMultiValue('email', $email['address']);
          }
          $phones = $coords['phones'];
          foreach($phones as $phone) {
            $doc->setMultiValue('phone', $phone['number']);
          }
          $ims = $coords['jabber'];
          foreach($ims as $im) {
            $doc->setMultiValue('jabber', $im['address']);
          }

          $addresses = $coords['addresses'];
          foreach($addresses as $address) {
            $doc->setMultiValue('street', $address['street']);
            $doc->setMultiValue('zipcode', $address['zipcode']);
            $doc->setMultiValue('expresspostal', $address['expresspostal']);
            $doc->setMultiValue('town', $address['town']);
            $doc->setMultiValue('country', $address['country']);
          }

          $documents[] = $doc;
        }

        $solr->addDocuments($documents);

        // Remove deleted event
        $query = "SELECT deletedcontact_contact_id FROM DeletedContact
          LEFT JOIN Contact ON deletedcontact_contact_id = contact_id
          LEFT JOIN UserObm ON deletedcontact_user_id = userobm_id
          WHERE userobm_domain_id='$domain' AND contact_id IS NULL";
        $db->query($query);
        while ($db->next_record()) { 
          $solr->deleteById($db->f('deletedcontact_contact_id'));
        }

        $this->logger->debug("Solr commit");
        $solr->commit();
        $this->logger->debug("Solr optimize");
        $solr->optimize();
      } 

      if (!$solr_contact_lastupdate) {
        $q_date = "INSERT INTO ObmInfo VALUES ('solr_contact_lastupdate', '$date')";
        $q_event = "INSERT INTO ObmInfo VALUES ('solr_lastcontact', '$maxid')";
      } else {
        $q_date = "UPDATE ObmInfo SET obminfo_value='$date' WHERE obminfo_name='solr_contact_lastupdate'";
        $q_event = "UPDATE ObmInfo SET obminfo_value='$maxid' WHERE obminfo_name='solr_lastcontact'";
      }
      $db->query($q_date);
      $db->query($q_event);

      $this->logger->debug("Update ObmInfo solr_lastupdate: $date");
      $this->logger->debug("Update ObmInfo solr_lastevent: $maxid");

      // w00t
      $this->logger->debug("EventIndexingJob complete");
    }
  }

  function getCategories($contact_id) {
    $query = "SELECT contactentity_contact_id, category_category, category_code, category_id, category_label 
      FROM CategoryLink
      INNER JOIN Category ON categorylink_category_id = category_id
      INNER JOIN ContactEntity ON contactentity_entity_id = categorylink_entity_id
      WHERE contactentity_contact_id='$contact_id'
      ORDER BY contactentity_contact_id, category_category, category_code, category_label";
    $db = new DB_OBM;
    $db->xquery($query);        
    return $db;
  }


  function getCoords($contact_id) {
    $db = new DB_OBM;

    // PHONE
    $query = "SELECT contactentity_contact_id AS contact_id, phone_label, phone_number FROM Phone 
      INNER JOIN ContactEntity ON phone_entity_id = contactentity_entity_id 
      WHERE contactentity_contact_id='$contact_id' ORDER BY phone_label";
    $db->xquery($query);        
    $phones = array();
    while ($db->next_record()) {
      $label = (explode(';', $db->f('phone_label')));
      array_push($phones, array('label' => $label, 'number' => $db->f('phone_number')));
    }

    // EMAIL
    $query = "SELECT contactentity_contact_id AS contact_id, email_label, email_address FROM Email 
      INNER JOIN ContactEntity ON email_entity_id = contactentity_entity_id 
      WHERE contactentity_contact_id='$contact_id' ORDER BY email_label";
    $db->xquery($query);         
    $emails = array();
    while ($db->next_record()) {
      $label = (explode(';', $db->f('email_label')));
      array_push($emails, array('label' => $label, 'address' => $db->f('email_address')));
    }

    // ADDRESS
    $lang = get_lang();
    $query = "SELECT contactentity_contact_id AS contact_id, address_label, address_street,
      address_zipcode, address_expresspostal, address_town, address_country, country_name, country_iso3166
      FROM Address 
      INNER JOIN ContactEntity ON address_entity_id = contactentity_entity_id 
      LEFT JOIN Country ON country_iso3166 = address_country AND country_lang='$lang' 
      WHERE contactentity_contact_id='$contact_id' ORDER BY address_label";
    $db->xquery($query);        
    $addresses = array();
    while ($db->next_record()) {
      $label = (explode(';',$db->f('address_label')));
      array_push($addresses, array(
        'label' => $label, 'street' => $db->f('address_street'), 'zipcode' => $db->f('address_zipcode'),
        'expresspostal' => $db->f('address_expresspostal'), 'town' => $db->f('address_town'), 
        'country' => $db->f('country_name'), 'country_iso3166' => $db->f('country_iso3166')));
    }

    // JABBER
    $query = "SELECT contactentity_contact_id AS contact_id, IM.* FROM IM 
      INNER JOIN ContactEntity ON im_entity_id = contactentity_entity_id 
      WHERE  contactentity_contact_id='$contact_id'";
    $db->xquery($query);        
    $ims = array();
    while ($db->next_record()) {
      array_push($ims, array('protocol' => $db->f('im_protocol'),'address' => $db->f('im_address')));
    }

    $return = array('phones' => $phones, 'emails' => $emails, 'addresses' => $addresses, 'jabber' => $ims);

    return $return;
  }

}
?>
