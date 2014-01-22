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



include_once('obminclude/lib/Solr/Service.php');
include_once('obminclude/of/of_indexingService.inc');

require_once 'obminclude/Patchwork/PHP/Shim/Normalizer.php';
require_once 'obminclude/Normalizer.php';
require_once 'obminclude/Patchwork/Utf8.php';

use \Patchwork;

class  OBM_Search {

  private static function normalize($str) {
      return \Patchwork\Utf8::toAscii($str);
  }

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
          $normalized_pattern = self::normalize($pattern);
          $response = $solr->search($normalized_pattern, $offset, $limit, $options);
          display_debug_solr($normalized_pattern, $cdg_solr, "OBM_Search::search($core)");
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
 * @copyright Copyright (c) 1997-2007 Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr> 
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
