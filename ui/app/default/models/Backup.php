<?php
/*
 +-------------------------------------------------------------------------+
 |  Copyright (c) 1997-2010 OBM.org project members team                   |
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
      $query = new OBM_Satellite_RestoreEntity(Backup::$auth, $url_args, $data);
      $this->$restoreFunc($query->execute(),$method);
    } catch (Exception $e) {
      throw new Exception($GLOBALS['l_err_cant_restore'].' ('.$e->getMessage().')');
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

    $query = "SELECT
        userobm_id as id,
        userobm_login as login,
        'user' as entity,
        domain_name as realm,
        userobm_mail_perms as mail_enabled,
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

    $mail_enabled = $obm_q->f('mail_enabled');
    if (empty($mail_enabled)) {
      throw new Exception($GLOBALS['l_err_backup_no_mail']);
    }

    $this->details = $obm_q->Record;
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
    $document = $reader->getDocument();
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
   * Perform the export of user contacts to the vcard format
   */
  private function user_contacts_export($user_id) {
    $vcards = array();

    include_once('php/contact/addressbook.php');

    $remember_uid = $GLOBALS['obm']['uid'];
    $GLOBALS['obm']['uid'] = $user_id; // some kind of sudo $user_id

    $addressbooks = OBM_AddressBook::search();
    foreach ($addressbooks as $addressbook) {
      if ($addressbook->name!='public_contacts') { // I'd better filter this addressbook on search
        $vcards[$addressbook->name] = $addressbook->toVcard();
      }
    }

    $GLOBALS['obm']['uid'] = $remember_uid;

    return $vcards;
  }

  /**
   * Perform the import of the contacts
   */
  function user_contacts_import($vcf_contacts,$user_id) {
    include_once('php/contact/addressbook.php');
    include_once('php/contact/contact_query.inc');

    $remember_uid = $GLOBALS['obm']['uid'];
    $GLOBALS['obm']['uid'] = $user_id; // some kind of sudo $user_id

    $addressbooks = OBM_AddressBook::search();
    $addressBookByName = array();
    foreach ($addressbooks as $addressbook) {
      if ($addressbook->name!='public_contacts') { // I'd better filter this addressbook on search
        if ($addressbook->isDefault || isset($vcf_contacts[$addressbook->name])) {
          $addressbook->reset();
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
      if($fd && $addressbook->write) {
        $ids = run_query_vcard_insert(array('vcard_fd' => $fd), $addressbook);
      }
    }

    $GLOBALS['obm']['uid'] = $remember_uid;
  }

}

