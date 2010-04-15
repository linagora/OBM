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
  protected $auth;
  protected $details;

  /**
   * standard constructor
   * @access public
   **/
  public function __construct($entity,$entity_id) {
    try {
      $this->auth = new SatelliteAuth();
    } catch (Exception $e) {
      throw new Exception($GLOBALS['l_err_obm_satellite_usersystem']);
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
    $query = new OBM_Satellite_AvailableBackup($this->auth, $this->details, array());
    return $query->execute();
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
      $query = new OBM_Satellite_BackupEntity($this->auth, $this->details, $data);
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
      $query = new OBM_Satellite_RestoreEntity($this->auth, $url_args, $data);
      return $query->execute();
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
        userobm_id as 'id',
        userobm_login as 'login',
        'user' as 'entity',
        domain_name as 'realm',
        ms.host_ip as 'host'
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
    $data['calendar'] = get_user_vcalendar_export($this->details['id']);

    //getting contacts to backup
    $data['privateContact'] = get_user_contacts_export($this->details['id']);

    return $data;
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
        mailshare_id as 'id',
        mailshare_name as 'login',
        'mailshare' as 'entity',
        domain_name as 'realm',
        ms.host_ip as 'host'
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
}


//FIXME: :'( snif
/**
 * Perform the export of the calendar to the vCalendar format
 */
function get_user_vcalendar_export($user_id) {
  $date = new Of_date();
  $start = clone $date;
  $start = $start->subYear(100);
  $end = clone $date;
  $end->addYear(100);
  $calendar_user['user'] = array($user_id => 'dummy');

  include_once('php/calendar/calendar_query.inc'); // FIXME: :'( snif
  include_once('obminclude/of/vcalendar/writer/ICS.php');
  include_once('obminclude/of/vcalendar/reader/OBM.php');

  $reader = new Vcalendar_Reader_OBM($calendar_user,NULL,$start,$end);
  $document = $reader->getDocument();
  $writer = new Vcalendar_Writer_ICS();
  $writer->writeDocument($document);
  return $writer->buffer;
}

/**
 * Perform the export of user contacts to the vcard format
 */
function get_user_contacts_export($user_id) {
  $vcards = '';

  include_once('php/contact/addressbook.php'); // FIXME: :'( snif

  $remember_uid = $GLOBALS['obm']['uid']; // FIXME: :'( snif
  $GLOBALS['obm']['uid'] = $user_id; // FIXME: :'( snif

  $addressbooks = OBM_AddressBook::search();
  $contacts = $addressbooks->searchContacts($params['searchpattern']);
  if (count($contacts)>0) {
    foreach ($contacts as $c) {
      $vcards .= $c->toVcard()->__toString();
    }
  }
  $GLOBALS['obm']['uid'] = $remember_uid; // FIXME: :'( snif
  return $vcards;
}

