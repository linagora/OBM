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



///////////////////////////////////////////////////////////////////////////////
// OBM - File : log_query.inc                                                //
//     - Desc : log query File                                               //
// 2007-07-02 AliaSource - Pierre Baudracco                                  //
///////////////////////////////////////////////////////////////////////////////
// $Id$
///////////////////////////////////////////////////////////////////////////////


///////////////////////////////////////////////////////////////////////////////
// Get mail log list from Filesystem.
// Parameters:
//   - $params : hash values
// Returns:
// Array [$fullname] => array([0]=>$date [1]=>$size)
///////////////////////////////////////////////////////////////////////////////
function get_log_mail_files() {
  global $clog_root;

  $files = array();

  if(!file_exists($clog_root)){
    return array();
  }
  
  $log_dir = dir("$clog_root/");
  while ($entry=$log_dir->read()) {
    if (preg_match("/^mail.log(.*)(\.gz)?$/",$entry, $match)) {
      $filename = $match[0];
      $info = stat("$clog_root/$filename");
      $mdate = of_datetime_format($info['mtime']); 
      $size = filesize("$clog_root/$filename");

      $files[$mdate]['filename'] = $filename;
      $files[$mdate]['size'] = $size;
    }
  }

  krsort($files);
  return $files;
}


///////////////////////////////////////////////////////////////////////////////
// Get user emails pattern (login) from user ids
// Parameters:
//   - $params : hash values
// Returns:
// Array [$fullname] => array([0]=>$date [1]=>$size)
///////////////////////////////////////////////////////////////////////////////
function get_log_emails_from_users($params) {
  global $clog_root;

  $users = $params['sel_user_id'];
  $emails = array();

  if ((is_array($users)) && (count($users) > 0)) {
    foreach ($users as $u_id) {
     $emails[] = get_entity_email(null, null, false,', ', $u_id, user);
    }
  }
  return $emails;
}

///////////////////////////////////////////////////////////////////////////////
// Log by selected users
///////////////////////////////////////////////////////////////////////////////
function run_query_users_view_log($params){
  global $clog_root;
  $filename = $params['file'];
  // Stripcslashes to cancel the magic_quotes (aujourd'hui becomes aujourd\'hui which fails on fopen)
  if (get_magic_quotes_gpc())
    $filename = stripcslashes ($filename);
  
  $pla = new PostfixLogAnalyser($params['sel_user_id']);
  if (preg_match('/.gz$/', $filename)) {
    $handle = gzopen($clog_root."/".$filename,"r");
    while (!gzeof($handle)){
      $line = gzgets($handle);
      if($pla->analyse($line)) {
        $pla->store($line) ;
        if($pla->validateIdentityLine() === FALSE) {
          $pla->erase();
        }
      }
    }
    gzclose($handle);
    return $pla->toString();

  } else {
    $handle = fopen($clog_root."/".$filename,"r");
    while (!feof($handle)){
      $line = fgets($handle);
      if($pla->analyse($line)) {
        $pla->store($line) ;
        if($pla->validateIdentityLine() === FALSE) {
          $pla->erase();
        }
      }
    }
    fclose($handle);
    return $pla->toString();

  }
}


///////////////////////////////////////////////////////////////////////////////
// PostfixLogAnalyser Object
// Parse and sort log lines of postfix
///////////////////////////////////////////////////////////////////////////////
class PostfixLogAnalyser {

  private $_id;
  private $_line;
  private $_to;
  private $_from;
  private $lines = array();
  private $ids = array();
  private $emails;
  private $index;
  public function __construct($users) {
    $this->index = 0;
    $emails = array();
    if ((is_array($users)) && (count($users) > 0)) {
      foreach ($users as $u_id) {
       $emails = array_merge($emails,get_entity_email(null, null, false,null, $u_id, user));
      }
    }
    $this->emails = implode('|', $emails);
    $this->emails = preg_replace('/\$/', '\\\$', $this->emails);
    $this->emails = preg_replace('/#/', '\\#', $this->emails);
  }

  public function validateIdentityLine() {
    if($this->isValidId($this->_id) !== TRUE) {
      if(!is_null($this->_from)) {
        $this->setFlag('from',$this->_id, $this->isValidEmail($this->_from));
      }
      if(!is_null($this->_to)) {
        $this->setFlag('to',$this->_id, $this->isValidEmail($this->_to));
      }
      if($this->isValidId($this->_id) === FALSE) {
       return FALSE;
      }
    }
    return TRUE;
  }

  public function analyse($line) {
    $return = preg_match('#.*postfix[^:]*:\s*([A-Z0-9]*)\s*:\s*(from=<([^>]*)>)?(to=<([^>]*)>)?(,\s*orig_to=<([^>]*)>)?#', $line, $matches);
    if($return) {
      $this->_id = $matches[1];
      if($matches[2] != '') {
        $this->_from = $matches[3];
      } else {
        $this->_from = NULL;
      }
      if($matches[4] != '') {
        $this->_to = $matches[5];
        if($matches[7] != '') {
          $this->_to .= ' '.$matches[7];
        }
      } else {
        $this->_to = NULL;
      }
      $this->_line = $line;
      if($this->isValidId($this->_id) === FALSE) {
        $return = FALSE;
      }
    }
    return $return;
  }

  public function erase() {
    unset($this->lines[$this->_id]);
  }

  public function store($line) {
    $this->lines[$this->_id][$this->index] = $line;
    $this->index++;
  }

  private function setFlag($flag, $id, $bool) {
    $this->ids[$id][$flag] = $bool;
  }

  private function isValidId($id) {
    if($this->ids[$id]['to'] === FALSE && $this->ids[$id]['from'] === FALSE) {
      return FALSE;
    } elseif ($this->ids[$id]['to'] === TRUE || $this->ids[$id]['from'] === TRUE) {  
      return TRUE;
    }
    return NULL;
  }

  private function isValidEmail($mail) {

    if(preg_match("#($this->emails)#",$mail))
    return TRUE;
    return FALSE;
  }

  public function toString() {
    foreach($this->lines as $id => $lines) {
      if($this->isValidId($id) === TRUE) {
        foreach($lines as $index => $line) {
          $buffer[$index] = $line;
        }
      }
    }
    if(is_null($buffer)) {
      return '';
    }else{
      ksort($buffer);
      return implode('',$buffer);
    }
  }
}
?>
