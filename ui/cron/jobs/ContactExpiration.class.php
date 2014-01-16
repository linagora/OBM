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


include_once('CronJob.class.php');

global $obminclude; 
require_once("$obminclude/of/of_category.inc");
require_once("$obminclude/of/of_indexingService.inc");

class ContactExpiration extends CronJob {
  /**
   * @var Logger
   */
  var $logger;
  
  var $expiration = 0;


  function mustExecute($date) {
    if(isset($GLOBALS['cgp_contact_expiration'])) $this->expiration = $GLOBALS['cgp_contact_expiration'];
    if ($this->expiration == 0) return false;

    $delta   = 24*60;         //every days
    $instant = (6*60)%$delta; //at 6:00
    $min = (int)($date/60);
    return ($min%$delta === $instant);
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
    
    $query = "SELECT contact_company_id, contact_birthday_id, contact_usercreate, contact_addressbook_id FROM Contact
      WHERE contact_id $sql_id";
    $this->logger->core($query);
    $obm_q->query($query);
    $obm_q->next_record();
    $comp_id = $obm_q->f('contact_company_id');
    $birthday_id = $obm_q->f('contact_birthday_id');
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
    
    if ($c_use_connectors) {
      $query = "INSERT INTO
        DeletedContact (deletedcontact_contact_id, deletedcontact_addressbook_id, deletedcontact_timestamp, deletedcontact_origin)
        VALUES ($c_id, $ad, NOW(), '$GLOBALS[c_origin_cron]')";
      $this->logger->core($query);
      $obm_q->query($query);
    }
    
    // After contact deletion to get correct number
    run_query_global_company_contact_number_update($comp_id);

    // Delete index
    OBM_IndexingService::delete('contact', $c_id);  
  }
}
?>
