<?php


/**
 * Some utils 
 *
 * @package
 * @version $Id:$
 * @copyright Copyright (c) 1997-2007 Aliasource - Groupe LINAGORA
 * @author Mehdi Rande <mehdi.rande@aliasource.fr>
 * @license GPL 2.0
 */
class Vcalendar_Utils {

  static function getFileType($file) {
    $handle = fopen($file, 'r');
    while($line = fgets($handle)) {
      if(preg_match('/^\s*VERSION\s*:\s*(.*)$/i',$line,$match)) {
        fclose($handle);
        if(trim($match[1]) == '1.0') {
          return 'vcs';
        } elseif(trim($match[1]) == '2.0') {
          return 'ics';
        }
      }
    }
    fclose($handle);
    return null;
  }

  static function userExist($id) {
    $db = new DB_OBM;
    $query = 'SELECT userobm_id From UserObm WHERE userobm_id = '.$id;
    $db->query($query);
    return $db->next_record();
  }
}

