<?php
include_once('CronJob.class.php');

global $obminclude; // FIXME
require_once("$obminclude/of/of_category.inc");

class ContactExpiration extends CronJob {
  /**
   * @var Logger
   */
  var $logger;
  
  function mustExecute($date) {
    global $cgp_use;
    if ($cgp_use["service"]["user"]) {
      $hours = date('G');
      return ($hours == 6);
    } else {
     return false;
    }
  }

  function execute($date) {
    $this->logger->debug('Delete contacts where archived since 6 months');
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
        AND #MONTHDIFF(contact_timeupdate,now()) >= 6";
    $this->logger->core($query);
    $obm_q->xquery($query);
    
    while ($obm_q->next_record()) {
      $obj->$method_callback($obm_q->f('contact_id'));
    }
    
    $this->logger->info($obm_q->affected_rows()." contacts deleted.");
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
    
    $query = "SELECT contact_company_id, contact_birthday_id FROM Contact
      WHERE contact_id $sql_id";
    $this->logger->core($query);
    $obm_q->query($query);
    $obm_q->next_record();
    $comp_id = $obm_q->f('contact_company_id');
    $birthday_id = $obm_q->f('contact_birthday_id');
    
    // Hook : Pre
    if (function_exists('hook_pre_run_query_contact_delete')) {
      if (! hook_pre_run_query_contact_delete($c_id)) {
        return false;
      }
    }
    
    run_query_global_delete_document_links($c_id, 'contact');    
    $ret = of_userdata_query_delete('contact', $c_id);
    
    // BEGIN birthday support
    
    $query = "DELETE FROM CalendarEvent WHERE calendarevent_id = '$birthday_id'";
    $this->logger->core($query);
    $obm_q->query($query);
    
    // END birthday support
    
    $query = "DELETE FROM Contact WHERE contact_id $sql_id";
    $this->logger->core($query);
    $obm_q->query($query);
    
    // If connectors in use
    
    if ($c_use_connectors) {
      $now = date('Y-m-d H:i:s');
      $query = "INSERT INTO
        DeletedContact (deletedcontact_contact_id, deletedcontact_timestamp)
        VALUES ($c_id, '$now')";
      $this->logger->core($query);
      $obm_q->query($query);
    }
    
    // After contact deletion to get correct number
    run_query_global_company_contact_number_update($comp_id);
  }
}
?>
