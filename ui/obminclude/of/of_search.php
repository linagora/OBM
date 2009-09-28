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

  public static function buildSearchQuery($searchable, $pattern) {
    $fields = call_user_func(array($searchable,'fieldsMap'));
    $search = self::parse($pattern);
    $query = '1 = 1';
    foreach($search as $fieldname => $values)  {
      $conditions = array();
      if($fieldname == '*') {
        foreach($fields as $map => $sql) {
          $subconditions = array();
          foreach($values as $value) {
            $subconditions[] = "$sql #LIKE '$value%'";
          }
          $conditions[]  =  '('.implode(' AND ', $subconditions).')';
        } 
        $query .= ' AND ('.implode(' OR ', $conditions).')';
      } elseif($fields[$fieldname]) {
        foreach($values as $value) {
          $conditions[] .= $fields[$fieldname]." #LIKE '$value%'";
        }
        $query .= ' AND '.implode(' AND ', $conditions);
      }
    }
    return "($query)";
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

interface OBM_ISearchable {
  public static function fieldsMap();
}
