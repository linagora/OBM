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



/*
 * Class used to authenticate with obm-satellite
 */
class Backup {
  static protected $auth;
  protected $details;

  /**
   * standard constructor
   * @access public
   **/
  public function __construct($entity,$entity_id) {
    if (!isset(Backup::$auth)) {
      try {
        Backup::$auth = new SatelliteAuth('obmsatelliterequest');
      } catch (Exception $e) {
        throw new Exception($GLOBALS['l_err_obm_satellite_usersystem']);
      }
    }
    $detailsFunc = $entity.'Details';
    $this->$detailsFunc($entity_id);
  }

  /**
   * Get the list of avalaible backups for current entity
   * @access public
   * return array
   */
  public function availableBackups() {
    $query = new OBM_Satellite_AvailableBackup(Backup::$auth, $this->details, array());
    return $query->execute();
  }

  /**
   * Retrieve the avaible backups on the FTP
   * @access public
   * return array
   */
  public function retrieveBackups($options = array()) {
    try {
    $data = array(
      'report' => true,
      'sendMail' => true,
      'email' => array()
    );
    $data = array_merge($data,$options);
    $query = new OBM_Satellite_RetrieveBackup(Backup::$auth, $this->details, $data);
    return $query->execute();
    } catch (Exception $e) {
      throw new Exception($GLOBALS['l_unable_to_retrieve_backup'].' ('.$e->getMessage().')');
    }
  }

  /**
   * Backup the current entity
   * @param array $options
   * @access public
   */
  public function doBackup($options = array()) {
    $dataFunc = $this->details['entity'].'Data';
    $data = array(
      'report' => true,
      'sendMail' => true,
      'email' => array()
    );
    $data = array_merge($this->$dataFunc(),$data,$options);
    try {
      $query = new OBM_Satellite_BackupEntity(Backup::$auth, $this->details, $data);
      return $query->execute();
    } catch (Exception $e) {
      throw new Exception($GLOBALS['l_err_cant_backup'].' ('.$e->getMessage().')');
    }
  }

  /**
   * Restore the current entity
   * @param string $filename
   * @param string $method
   * @param array $options
   * @access public
   */
  public function doRestore($filename, $method, $options = array()) {
    $restoreFunc = 'restore'.ucfirst($this->details['entity']);

    $url_args = $this->details;
    $url_args['data'] = in_array($method, OBM_Satellite_RestoreEntity::$restoreData) ? $method : null;

    $data = array(
      'report' => true,
      'sendMail' => true,
      'email' => array(),
      'backupFile' => $filename
    );
    $data = array_merge($data,$options);
    try {
      $updateEmail = ($method === 'mailbox' || $method === 'all');
      if ($updateEmail) {
          $this->updatePostfixAccess(false);
      }
      $query = new OBM_Satellite_RestoreEntity(Backup::$auth, $url_args, $data);
      $this->$restoreFunc($query->execute(),$method);
      if ($updateEmail) {
          $this->updatePostfixAccess(true);
      }
    } catch (Exception $e) {
      throw new Exception($GLOBALS['l_err_cant_restore'].' ('.$e->getMessage().')');
    }
  }

  private function updatePostfixAccess($enableAccess) {
    foreach ($this->details['smtpServers'] as $smtpServer) {
        $params = array(
            'host'  => $smtpServer,
            'login' => $this->details['login'],
            'realm' => $this->details['realm'],
        );
        $query = new OBM_Satellite_PostfixAccess(Backup::$auth, $params, array('enableAccess' => $enableAccess));
        $response = $query->execute();
        if (!$response['postfixAccessUpdate']['success']) {
          throw new Exception("Unable to update Postfix access: ".$response['postfixAccessUpdate']['msg']);
        }
    }
  }

  /**
   * get the details for entity of type user
   * @param int $user_id
   * @access protected
   */
  protected function userDetails($user_id) {
    $obm_q = new DB_OBM;
    $db_type = $obm_q->type;
    $multidomain = sql_multidomain('userobm');
    $id = sql_parse_id($user_id, true);	

// Comment for doing a backup user even if mailbox is disable
//  userobm_mail_perms as mail_enabled,
    $query = "SELECT
        userobm_id as id,
        userobm_login as login,
        'user' as entity,
        domain_name as realm,
        domain_id as realm_id,
        ms.host_ip as host
      FROM UserObm
      LEFT JOIN Domain on userobm_domain_id=domain_id
      LEFT JOIN Host ms on userobm_mail_server_id=ms.host_id
      WHERE userobm_id $id
        $multidomain";
    display_debug_msg($query, $GLOBALS['cdg_sql'], 'Backup::userDetails()');
    $obm_q->query($query);

    if (!$obm_q->next_record()) {
      throw new Exception($GLOBALS['l_err_reference']);
    }

    $host = $obm_q->f('host') ;
    if (empty($host)) {
      throw new Exception($GLOBALS['l_err_host']) ;
    }

// Comment for doing a backup user even if mailbox is disable
//    $mail_enabled = $obm_q->f('mail_enabled');
//    if (empty($mail_enabled)) {
//      throw new Exception($GLOBALS['l_err_backup_no_mail']);
//    }

    $user_details = $obm_q->Record;
    $realm_id = $user_details['realm_id'];
    $smtp_query = "SELECT
        DISTINCT host_ip AS host
        FROM Host
        INNER JOIN ServiceProperty ON host_id=#CAST(serviceproperty_value,INTEGER)
        INNER JOIN DomainEntity ON serviceproperty_entity_id=domainentity_entity_id
        WHERE domainentity_domain_id=$realm_id AND serviceproperty_service='mail' AND serviceproperty_property='smtp_in'";
    $obm_q->query($smtp_query);
    $smtp_servers = array();
    while ($obm_q->next_record()) {
        array_push($smtp_servers, $obm_q->f('host'));
    }
    $user_details['smtpServers'] = $smtp_servers;
    $this->details = $user_details;
  }

  /**
   * data to backup for entity of type user
   * @access protected
   * @return array
   */
  protected function userData() {
    $data = array();

    //getting calendar to backup
    $data['calendar'] = $this->user_vcalendar_export($this->details['id']);

    //getting contacts to backup
    $data['privateContact'] = $this->user_contacts_export($this->details['id']);

    return $data;
  }

  /**
   * used to restore user data
   * @param  array     $data
   * @access protected
   */
  protected function restoreUser($data,$method='') {

    //calendar data to restore
    if ($data['calendar']) {
      if (empty($method) || $method=='all' || $method=='calendar')
        $this->user_vcalendar_import($data['calendar'], $this->details['id']);
    }

    //contacts data to restore
    if (is_array($data['privateContact'])) {
      if (empty($method) || $method=='all' || $method=='contact')
        $this->user_contacts_import($data['privateContact'], $this->details['id']);
    }
  }

  /**
   * get the details for entity of type mailshare
   * @param int $mailshare_id
   * @access protected
   */
  protected function mailshareDetails($mailshare_id) {
    $obm_q = new DB_OBM;
    $db_type = $obm_q->type;
    $multidomain = sql_multidomain('mailshare');
    $id = sql_parse_id($mailshare_id, true);	

    $query = "SELECT
        mailshare_id as id,
        mailshare_name as login,
        'mailshare' as entity,
        domain_name as realm,
        ms.host_ip as host
      FROM MailShare
      LEFT JOIN Domain on mailshare_domain_id=domain_id
      LEFT JOIN Host ms on mailshare_mail_server_id=ms.host_id
      WHERE mailshare_id $id 
        $multidomain";
    display_debug_msg($query, $GLOBALS['cdg_sql'], 'Backup::mailshareDetails()');
    $obm_q->query($query);

    if (!$obm_q->next_record()) {
      throw new Exception($GLOBALS['l_err_reference']);
    }

    $this->details = $obm_q->Record;
    $this->details['login'] = strtolower($this->details['login']);
  }

  /**
   * data to backup for entity of type mailshare
   * @access protected
   * @return array
   */
  protected function mailshareData() {
    return array();
  }

  /**
   * used to restore mailshare data
   * @param  array     $data
   * @access protected
   */
  protected function restoreMailshare($data,$method='') {
  }




  /**
   * Perform the export of the calendar to the vCalendar format
   */
  private function user_vcalendar_export($user_id) {
    $date = new Of_date();
    $start = clone $date;
    $start = $start->subYear(100);
    $end = clone $date;
    $end->addYear(100);
    $calendar_user['user'] = array($user_id => 'dummy');

    include_once('php/calendar/calendar_query.inc');
    include_once('obminclude/of/vcalendar/writer/ICS.php');
    include_once('obminclude/of/vcalendar/reader/OBM.php');

    $reader = new Vcalendar_Reader_OBM($calendar_user,NULL,$start,$end);
    $document = $reader->getDocument('PUBLISH', false, false);
    $writer = new Vcalendar_Writer_ICS();
    $writer->writeDocument($document);
    $document->destroy();
    return $writer->buffer;
  }

  /**
   * Perform the import of the vcalendar
   */
  private function user_vcalendar_import($fd,$user_id) {
    include_once('php/calendar/calendar_query.inc');
    include_once('php/calendar/event_observer.php');
    include_once('obminclude/lib/Solr/Document.php');
    include_once('obminclude/of/of_indexingService.inc');
    include_once('obminclude/of/vcalendar/Utils.php');
    include_once('obminclude/of/vcalendar/writer/OBM.php');
    include_once('obminclude/of/vcalendar/reader/ICS.php');

    $remember_uid = $GLOBALS['obm']['uid'];
    $GLOBALS['obm']['uid'] = $user_id; // some kind of sudo $user_id

    //reset calendar
    run_query_calendar_reset($user_id,array('delete_meeting' => true));

    //restore calendar
    $reader = new Vcalendar_Reader_ICS($fd);
    $document = $reader->getDocument();
    $writer = new Vcalendar_Writer_OBM(true);  
    $writer->writeDocument($document);
    $document->destroy();

    $GLOBALS['obm']['uid'] = $remember_uid;
  }

  /**
   * Perform the export of user contacts
   */
  private function user_contacts_export($user_id) {
    include_once('php/contact/addressbook.php');

    $addressbooks = OBM_AddressBook::searchOwnAddressBooks($user_id);
    return $addressbooks;
  }


  function getDetails() {
    return $this->details;
  } 

  /**
   * Perform the import of the contacts
   */
  function user_contacts_import($vcf_contacts,$user_id) {
    include_once('php/contact/addressbook.php');
    include_once('php/contact/contact_query.inc');

    $remember_uid = $GLOBALS['obm']['uid'];
    $GLOBALS['obm']['uid'] = $user_id; // some kind of sudo $user_id

    $addressbooks = OBM_AddressBook::searchOwnAddressBooks($user_id);
    $addressBookByName = array();
    foreach ($addressbooks as $addressbook) {
      if ($addressbook->name!='public_contacts') { // I'd better filter this addressbook on search
        if ($addressbook->isDefault || isset($vcf_contacts[$addressbook->name])) {
          $addressbook->soft_reset();
          $addressBookByName[$addressbook->name] = $addressbook;
        } else {
          OBM_AddressBook::delete(array('addressbook_id' => $addressbook->id));
        }
      }
    }

    foreach ($vcf_contacts as $addBookName => $fd) {
      if (isset($addressBookByName[$addBookName])) {
        $addressbook = $addressBookByName[$addBookName];
      } else {
        $addressbook = OBM_AddressBook::create(array('name' => $addBookName));
      }
      if($fd) {
        $ids = run_query_vcard_insert(array('vcard_fd' => $fd), $addressbook);
      }
    }

    $GLOBALS['obm']['uid'] = $remember_uid;
  }

}

