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

    $result = array();
    $solr = OBM_IndexingService::connect($core);
    if ($solr) {
        try {
          if ($pattern != "") {
            $pattern = "$pattern domain:$obm[domain_id]"; 
          } else {
            $pattern = "domain:$obm[domain_id]";
          }
          // $pattern = strtolower($pattern); => do not work because of solr keyword "OR" and "AND"
          // replace compound word in pattern
          $pattern = preg_replace("/(?<=[a-zA-Z])-(?=[a-zA-Z])/"," + ", $pattern);
          $pattern = preg_replace("/(\w*)\*/e", "strtolower('$1').'*'", $pattern);
          $response = $solr->search($pattern, $offset, $limit, $options);
          display_debug_solr($pattern, $cdg_solr, "OBM_Search::search($core)");
          if($response->response->numFound > 0) {
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

  public static function count($core, $pattern) {
    global $obm;

    $solr = OBM_IndexingService::connect($core);
    if ($solr) {
      try {
        if ($pattern != "") {
          $pattern = "$pattern domain:$obm[domain_id]"; 
        } else {
          $pattern = "domain:$obm[domain_id]";
        }
        $pattern = preg_replace("/(?<=[a-zA-Z])-(?=[a-zA-Z])/"," + ", $pattern);
        $pattern = preg_replace("/(\w*)\*/e", "strtolower('$1').'*'", $pattern);
        $response = $solr->search($pattern);
        return $response->response->numFound;
      } catch(Exception $e) {
      }

      return 0;
    }      
  }


  public static function buildSearchQuery($core, $pattern, $offset, $limit, $options) {

    $ids = self::search($core, $pattern, $offset, $limit, $options);
    if (sizeof($ids)>0) {
      $query = implode(',', $ids);
      return "${core}_id IN ($query)";
    }

    return false;
  }

  public static function parse($pattern, $fields = null) {
    // Add a delimiter at the end of the string
    $pattern .= ' ';
    $searchPattern = array();
    // Regexp... if you have a problem here, you have two problems...
    // search for : [word:]word|"string"|(string)
    // a word 
    preg_match_all("/(([-aA-zZ0-9_]+):)?(([^ \"(]+)|\"([^\"]+)\"|\(([^)]+)\))[^:]/", $pattern, $match );
    foreach($match[0] as $index => $string) {
      // If no field present, the pattern must be search on *
      if($match[2][$index] == '') {
        $field = '';
      } elseif ($fields && !$fields[$match[2][$index]]){
        $field = '';
        $searchPattern[$field][] = $match[2][$index].':';
      } else {
        $field = $match[2][$index];
      }
      // if value is a single word, field should match field like 'value%'
      if($match[4][$index] != '') {
        $searchPattern[$field][] = $match[4][$index];
      // if value is a string encapsulated by ", field should match field like 'string%'
      }elseif($match[5][$index] != '') {
        $searchPattern[$field][] = $match[5][$index];
      // if value is a string encapsulaed by (), field should match field like 'word1' and field like 'word2'....
      } elseif($match[6][$index] != '') {
        $patterns = explode(' ', $match[6][$index]);
        foreach($patterns as $subPattern) {
          $searchPattern[$field][] = $subPattern;
        } 
      }
    }
    return $searchPattern;
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
