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

include_once 'obminclude/lib/Solr/Service.php';

class OBM_IndexingService {

  public static function connect($core) {
    $solrserver = of_domain_get_domain_solrserver($GLOBALS['obm']['domain_id'], $core);
    foreach ($solrserver as $s_id => $solrserver_info){
      foreach ($solrserver_info as $server_info){
        $solr_ip = $server_info['ip'];
      }
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
      $solr->optimize();
    }
  }

  public function deleteByQuery($core, $query) {
    $solr = OBM_IndexingService::connect($core);
    if ($solr) {
      $solr->deleteByQuery($query);
      $solr->commit();
      $solr->optimize();
    }
  }

  public function delete($core, $id) {
    $solr = OBM_IndexingService::connect($core);
    if ($solr) {
      $solr->deleteById($id);
      $solr->commit();
      $solr->optimize();
    }
  }

}

?>
