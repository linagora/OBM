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
include_once('CronJob.class.php');

global $obminclude; 
require_once("$obminclude/of/of_category.inc");

class ContactExpiration extends CronJob {
  /**
   * @var Logger
   */
  var $logger;
  
  var $expiration = 0;


  function mustExecute($date) {
    if(isset($GLOBALS['cgp_contact_expiration'])) $this->expiration = $GLOBALS['cgp_contact_expiration'];
    if ($this->expiration == 0) return false;
    
    $hours = date('G');
    return ($hours == 6);
  }

  function execute($date) {
    $this->logger->debug('Delete contacts where archived since '. $this->expiration. ' months');
    $enable = $this->EachOldArchivedContact($this, 'DeleteContact');
  }
  
  /**
   * Doing a : oldArchivedContact.each {|contact_id| do_something }
   *
   * @param Object $obj
   * @param String $method_callback
   */
  function EachOldArchivedContact($obj, $method_callback) {
    $this->logger->debug("For each old archived contacts");

    $obm_q = new DB_OBM;
    $query = "SELECT contact_id FROM Contact
      WHERE contact_archive = '1'
        AND #MONTHDIFF(contact_timeupdate,now()) >= ". $this->expiration;
    $this->logger->core($query);
    $obm_q->xquery($query);
    
    while ($obm_q->next_record()) {
      $obj->$method_callback($obm_q->f('contact_id'));
    }
    
    $this->logger->info($obm_q->nf()." contacts deleted.");
  }
  
  /**
   * Delete a contact
   *
   * @param Integer $c_id contact_id
   * @return Boolean
   */
  function DeleteContact($c_id)  {
    global $cdg_sql, $c_use_connectors;
    
    $sql_id = sql_parse_id($c_id, true);
    $this->logger->debug("Delete contact $c_id");
    
    $obm_q = new DB_OBM;
    
    $query = "SELECT contact_company_id, contact_birthday_id, contact_usercreate, contact_privacy FROM Contact
      WHERE contact_id $sql_id";
    $this->logger->core($query);
    $obm_q->query($query);
    $obm_q->next_record();
    $comp_id = $obm_q->f('contact_company_id');
    $birthday_id = $obm_q->f('contact_birthday_id');
    $privacy = $obm_q->f('contact_privacy');
    $uid = $obm_q->f('contact_usercreate');
    $ad = $obm_q->f('contact_addressbook_id');
    
    // Hook : Pre
    if (function_exists('hook_pre_run_query_contact_delete')) {
      if (! hook_pre_run_query_contact_delete($c_id)) {
        return false;
      }
    }
    
    run_query_global_delete_document_links($c_id, 'contact');    
    $ret = of_userdata_query_delete('contact', $c_id);
    
    
    // BEGIN birthday support
    
    $query = "DELETE FROM Event WHERE event_id = '$birthday_id'";
    $this->logger->core($query);
    $obm_q->query($query);
    
    // END birthday support
    
    
    of_entity_delete('contact', $c_id); 
    $query = "DELETE FROM Contact WHERE contact_id $sql_id";
    $this->logger->core($query);
    $obm_q->query($query);
    
    // If connectors in use
    
    if ($c_use_connectors && $privacy == 1) {
      $query = "INSERT INTO
        DeletedContact (deletedcontact_contact_id, deletedcontact_addressbook_id, deletedcontact_timestamp, deletedcontact_origin)
        VALUES ($c_id, $ad, NOW(), '$GLOBALS[c_origin_cron]')";
      $this->logger->core($query);
      $obm_q->query($query);
    }
    
    // After contact deletion to get correct number
    run_query_global_company_contact_number_update($comp_id);
  }
}
?>
