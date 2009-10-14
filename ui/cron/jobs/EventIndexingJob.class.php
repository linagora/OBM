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

/**
 * EventIndexingJob 
 * 
 * @uses CronJob
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2007 Aliasource - Groupe LINAGORA
 * @author David Phan <david.phan@aliasource.fr> 
 * @license GPL 2.0
 */
class EventIndexingJob extends CronJob {

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
    return array('php/calendar/calendar_query.inc', 'obminclude/lib/Solr/Service.php');
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

    // Get domain solr server
    $domains = of_domain_get_list();
    foreach($domains as $domain_id => $domain) {
      $solrservers = of_domain_get_domain_solrserver($domain_id);
      foreach ($solrservers as $s_id => $solrserver_info) {
	      foreach ($solrserver_info as $server_info) {
          $ip = $server_info['ip']; 
        }
      }      
      if($ip) $servers[$domain_id] = $ip;
    }

    if (is_array($servers)) {
      foreach($servers as $domain => $ip) {
        $solr = new Apache_Solr_Service($ip, '8080', '/solr/event' );    
        if (!$solr->ping()) {
          echo 'Solr service not responding.';
          exit;
        }
        
        $documents = array();
        $db = new DB_OBM;
        $query = "
        SELECT 
          event_id,
          event_domain_id,
          event_title,
          event_location,
          event_date,
          event_duration,
          eventcategory1_id, 
          eventcategory1_label, 
          event_owner,
          event_description,
          eventtag_id,
          eventtag_label,
          event_allday,
          event_repeatkind,
          event_opacity,
          event_privacy,
          eventlink_state,
          #CONCAT(userobm_lastname, ' ', userobm_firstname) as owner 
        FROM Event
        LEFT JOIN EventCategory1 ON event_category1_id = eventcategory1_id
        LEFT JOIN EventTag ON eventtag_id = event_tag_id
        INNER JOIN EventLink ON eventlink_event_id = event_id
        INNER JOIN UserObm ON userobm_id = event_owner
        WHERE event_domain_id='$domain'";
        
        $db->xquery($query);
        
        while($db->next_record()) {
          $part = new Apache_Solr_Document();
        
          // id
          $part->setField('id', $db->f('event_id'));
        
          // domain
          $part->setField('domain', $db->f('event_domain_id'));
        
          // title
          $part->setField('title', $db->f('event_title'));
        
          // location
          $part->setField('location', $db->f('event_location'));
        
          // category
          $part->setMultiValue('category', $db->f('eventcategory1_id'));
          $part->setMultiValue('category', $db->f('eventcategory1_label'));
        
          // date
          $date = new Of_Date($db->f('event_date'));
          $part->setField('date', $date->format('Y-m-d\TH:i:s\Z'));
        
          // duration
          $part->setField('duration', $db->f('event_duration'));
        
          // owner
          $part->setMultiValue('owner', $db->f('owner'));
          $part->setMultiValue('owner', $db->f('event_owner'));
        
          // description
          $part->setField('description', $db->f('event_description'));
        
          // tag
          $part->setMultiValue('tag', $db->f('eventtag_id'));
          $part->setMultiValue('tag', $db->f('eventtag_label'));
        
          // state
          $part->setField('state', $db->f('eventlink_state'));
        
          // is
          if ($db->f('event_allday')) {
            $part->setMultiValue('is', 'allday');
          }
          if ($db->f('event_repeatkind') != 'none') {
            $part->setMultiValue('is', 'periodic');
          }
          if ($db->f('event_opacity') == 'OPAQUE') {
            $part->setMultiValue('is', 'busy');
          } elseif ($db->f('event_opacity') == 'TRANSPARENT') {
            $part->setMultiValue('is', 'free');
          }
          if ($db->f('event_privacy')) {
            $part->setMultiValue('is', 'private');
          }
        
          $obm['domain_id'] = $db->f('event_domain_id'); 
        
          // with
          $attendees_q = run_query_get_events_attendee(array($db->f('event_id')));
          while($attendees_q->next_record()) {
            $part->setMultiValue('with', $attendees_q->f('eventlink_label'));
          }
        
          $documents[] = $part;
        }
        
        try {
          $solr->addDocuments($documents);
          $solr->commit();
          $solr->optimize();
        } catch ( Exception $e ) {
          echo $e->getMessage();
        }
      }
    }
  }

}
?>
