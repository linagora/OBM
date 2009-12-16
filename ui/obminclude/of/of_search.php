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
include_once('obminclude/lib/Solr/Service.php');
include_once('obminclude/of/of_indexingService.inc');

class  OBM_Search {

  public static function search($core, $pattern, $offset, $limit, $options) {
    global $obm, $cdg_solr, $display;

    $solr = OBM_IndexingService::connect($core);
    if ($solr) {
        try {
          if ($pattern != "") {
            $pattern = "$pattern domain:$obm[domain_id]"; 
          } else {
            $pattern = "domain:$obm[domain_id]";
          }
	      	$response = $solr->search($pattern, $offset, $limit, $options);
          display_debug_solr($pattern, $cdg_solr, "OBM_Search::search($core)");
          if($response->response->numFound > 0) {
            $result = array();
            foreach ($response->response->docs as $doc) { 
              array_push($result, $doc->id);
            }
          }
        } catch (Exception $e) {
          //echo $e->getMessage();
        }
    } else {
      $display['msg'] = display_err_msg($GLOBALS['l_solr_connection_err']);
    }

    return $result;
  }


  public static function buildSearchQuery($core, $pattern, $offset, $limit, $options) {

    $ids = self::search($core, $pattern, $offset, $limit, $options);
    if (sizeof($ids)>0) {
      $query = implode(',', $ids);
      return "${core}_id IN ($query)";
    }

    return false;
  }

}

/**
 * OBM_ISearchable 
 * 
 * @package 
 * @version $id:$
 * @copyright Copyright (c) 1997-2007 Aliasource - Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
 * @license GPL 2.0
 */
interface OBM_ISearchable {
  /**
   * must return an hashmap with :
   * key = key in the search form
   * value = sql field name
   * exemple :
   * return array('lastname' => 'contact_lastname') 
   * 
   * @static
   * @access public
   * @return array() 
   */
  public static function fieldsMap();
}
