<?php
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
