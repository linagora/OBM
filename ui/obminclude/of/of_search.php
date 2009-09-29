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
class  OBM_Search {

  /**
   * The search pattern must be of the form  [word:]word|"string"|(string) *
   * if the field prefix is not present the pattern will be searched on all fields.
   * exemple of valid pattern :
   * field:value value2 field:(value3 value4) field:"value 5" (value5 value6) "value 7"
   * 
   *
   * @param mixed $searchable 
   * @param mixed $pattern 
   * @static
   * @access public
   * @return void
   */
  public static function buildSearchQuery($searchable, $pattern) {
    $fields = call_user_func(array($searchable,'fieldsMap'));
    $search = self::parse($pattern);
    $query = '1 = 1';
    foreach($search as $fieldname => $values)  {
      $conditions = array();
      foreach($values as $value) {
        $subconditions = array();
        if(is_array($fields[$fieldname])) {
          foreach($fields[$fieldname] as $sql => $type) {
            $subconditions[] = self::buildSql($sql, $type, $value);
          }
          $conditions[]  =  '('.implode(' OR ', $subconditions).')';
        }
      }
      $query .= ' AND '.implode(' AND ', $conditions);
    }
    return "($query)";
  }

  public static function buildSql($sql, $type, $value) {
    switch($type) {
    case 'integer' :
      if(is_numeric($value) || strpos($value, ',') !== false) return "$sql IN ($value)";
      break;
    case 'text' :
      return "$sql #LIKE '$value'";
    }
  }

  public static function parse($pattern) {
    // Add a delimiter at the end of the string
    $pattern .= ' ';
    $searchPattern = array();
    // Regexp... if you have a problem here, you have two problems...
    // search for : [word:]word|"string"|(string)
    // a word 
    preg_match_all("/(([aA-zZ0-9_]+):)?(([^ \"(]+)|\"([^\"]+)\"|\(([^)]+)\))[^:]/", $pattern, $match );
    foreach($match[0] as $index => $string) {
      // If no field present, the pattern must be search on *
      if($match[2][$index] == '') {
        $field = '*';
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
