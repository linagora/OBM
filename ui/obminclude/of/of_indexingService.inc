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



include_once 'obminclude/lib/Solr/Service.php';

class OBM_IndexingService {

  public static function connect($core) {
    if (!isset($_SESSION['solr_ip'] )) {
      $solrserver = of_domain_get_domain_solrserver($GLOBALS['obm']['domain_id'], $core);
      foreach ($solrserver as $s_id => $solrserver_info){
        foreach ($solrserver_info as $server_info){
          $solr_ip = $server_info['ip'];
        }
      }
      $_SESSION['solr_ip'] = $solr_ip;
    } else {
      $solr_ip = $_SESSION['solr_ip'];
    }

    if ($solr_ip) {
      $solr = new Apache_Solr_Service($solr_ip, '8080', "/solr/$core");
      if ($solr->ping()) {
        return $solr;
      }      
    }
    return false;
  }

  public function store($core, $doc) {
    $solr = OBM_IndexingService::connect($core);
    if ($solr) {
      $solr->addDocuments($doc);
    }
  }


  public function commit($core) {
    $solr = OBM_IndexingService::connect($core);
    if ($solr) {
      $solr->commit();
      // OBMFULL-3960 $solr->optimize();
    }
  }

  public function deleteByQuery($core, $query) {
    $solr = OBM_IndexingService::connect($core);
    if ($solr) {
      $solr->deleteByQuery($query);
      $solr->commit();
      // OBMFULL-3960 $solr->optimize();
    }
  }

  public function delete($core, $id) {
    $solr = OBM_IndexingService::connect($core);
    if ($solr) {
      $solr->deleteById($id);
      $solr->commit();
      // OBMFULL-3960 $solr->optimize();
    }
  }

}

?>
