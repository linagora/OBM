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

    $db = new DB_OBM;

    $query = "SELECT obminfo_value FROM ObmInfo WHERE obminfo_name='solr_lastupdate'";
    $solr_lastupdate = false;
    $res = $db->query($query);
    if($db->next_record()) {
      $solr_lastupdate = $db->f('obminfo_value');
    }

    $query = "SELECT obminfo_value FROM ObmInfo WHERE obminfo_name='solr_lastevent'";
    $solr_lastevent = 0;
    $res = $db->query($query);
    if($db->next_record()) {
      $solr_lastevent = $db->f('obminfo_value');
    }
    $maxid=$solr_lastevent;

    $this->logger->debug("Execute EventIndexingJob");
    $this->logger->debug("Last indexed event : $solr_lastevent");
    $this->logger->debug("Last indexed time : $solr_lastupdate");

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
        $solr = new Apache_Solr_Service($ip, '8080', '/solr/event' );    
        if (!$solr->ping()) {
          $this->logger->warn("Solr server for domain $domain not responding ($ip)");
          return;
        }
        $this->logger->debug("Solr server for domain $domain : $ip");

        $limit = 2000;

        $select = "
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
          event_origin,
          #CONCAT(userobm_lastname, ' ', userobm_firstname) as owner 
        FROM Event
        LEFT JOIN EventCategory1 ON event_category1_id = eventcategory1_id
        LEFT JOIN EventTag ON eventtag_id = event_tag_id
        INNER JOIN UserObm ON userobm_id = event_owner
        WHERE event_domain_id='$domain'";

        if (!$solr_lastupdate) {
          $query = "$select ORDER BY event_id LIMIT $limit";
        } else {
          $d = new Of_Date($solr_lastupdate);
          $query = "$select AND (event_id > $solr_lastevent) 
           OR (event_timecreate >= '$d' OR event_timeupdate >= '$d')  
           ORDER BY event_id LIMIT $limit";
        }

        $db->xquery($query);
        $documents = array();
        while($db->next_record()) {

          $doc = new Apache_Solr_Document();

          // id
          $id = $db->f('event_id');
          $doc->setField('id', $id);
          $maxid = $id;

          // domain
          $doc->setField('domain', $db->f('event_domain_id'));
          
          // title
          $doc->setField('title', $db->f('event_title'));
          
          // location
          $doc->setField('location', $db->f('event_location'));
          
          // category
          $doc->setMultiValue('category', $db->f('eventcategory1_id'));
          $doc->setMultiValue('category', $db->f('eventcategory1_label'));
          
          // date
          $edate = new Of_Date($db->f('event_date'));
          $doc->setField('date', $edate->format('Y-m-d\TH:i:s\Z'));
          
          // duration
          $doc->setField('duration', $db->f('event_duration'));
          
          // owner
          $doc->setMultiValue('owner', $db->f('owner'));
          $doc->setMultiValue('owner', $db->f('event_owner'));
          
          // description
          $doc->setField('description', $db->f('event_description'));
          
          // tag
          $doc->setMultiValue('tag', $db->f('eventtag_id'));
          $doc->setMultiValue('tag', $db->f('eventtag_label'));
          
          // state
          // FIXME
          // $doc->setField('state', $db->f('eventlink_state'));
          
          // is
          if ($db->f('event_allday')) {
            $doc->setMultiValue('is', 'allday');
          }
          if ($db->f('event_repeatkind') != 'none') {
            $doc->setMultiValue('is', 'periodic');
          }
          if ($db->f('event_opacity') == 'OPAQUE') {
            $doc->setMultiValue('is', 'busy');
          } elseif ($db->f('event_opacity') == 'TRANSPARENT') {
            $doc->setMultiValue('is', 'free');
          }
          if ($db->f('event_privacy')) {
            $doc->setMultiValue('is', 'private');
          }

          $doc->setField('from', $db->f('event_origin'));
          
          $obm['domain_id'] = $db->f('event_domain_id'); 
          
          // with
          $attendees_q = run_query_get_events_attendee(array($id));
          while($attendees_q->next_record()) {
            $doc->setMultiValue('with', $attendees_q->f('eventlink_label'));
          }

          $documents[] = $doc;
        }

        $solr->addDocuments($documents);
        
        // Remove deleted event
        $query = "SELECT deletedevent_event_id FROM DeletedEvent
          LEFT JOIN Event ON deletedevent_event_id = event_id
          LEFT JOIN UserObm ON deletedevent_user_id = userobm_id
          WHERE userobm_domain_id='$domain' AND event_id IS NULL";
        $db->query($query);
        while ($db->next_record()) { 
          $solr->deleteById($db->f('deletedevent_event_id'));
        }

        $this->logger->debug("Solr commit");
        $solr->commit();
        $this->logger->debug("Solr optimize");
        $solr->optimize();
      }
    }

    if (!$solr_lastupdate) {
      $q_date = "INSERT INTO ObmInfo VALUES ('solr_lastupdate', '$date')";
      $q_event = "INSERT INTO ObmInfo VALUES ('solr_lastevent', '$maxid')";
    } else {
      $q_date = "UPDATE ObmInfo SET obminfo_value='$date' WHERE obminfo_name='solr_lastupdate'";
      $q_event = "UPDATE ObmInfo SET obminfo_value='$maxid' WHERE obminfo_name='solr_lastevent'";
    }
    $db->query($q_date);
    $db->query($q_event);

    $this->logger->debug("Update ObmInfo solr_lastupdate: $date");
    $this->logger->debug("Update ObmInfo solr_lastevent: $maxid");

    // w00t
    $this->logger->debug("EventIndexingJob complete");

  }

}
?>
