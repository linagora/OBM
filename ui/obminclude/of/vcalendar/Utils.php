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
  
  static function entityExist($id, $entity) {
    $fn = $entity.'Exist';
    return self::$fn($id);
  }

  static function userExist($id) {
    $db = new DB_OBM;
    $query = 'SELECT userobm_id From UserObm WHERE userobm_id = '.$id;
    $db->query($query);
    return $db->next_record();
  }

  static function resourceExist($id) {
    $db = new DB_OBM;
    $query = 'SELECT resource_id From Resource WHERE resource_id = '.$id;
    $db->query($query);
    return $db->next_record();
  }  

  static function privatizeEvent($vevent) {
    if($vevent->private) {
      $vevent->reset('summary');
      $vevent->set('summary', $GLOBALS['l_private']);
      $vevent->reset('description');
      $vevent->set('description', $GLOBALS['l_private']);
      $vevent->reset('location');
      $vevent->set('location', $GLOBALS['l_private']);
      $vevent->reset('categories');
      $vevent->set('categories', array($GLOBALS['l_private']));
    }
    return $vevent;
  }
}

