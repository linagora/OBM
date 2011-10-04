<?php
#
# contact.php -dry : do not delete in database
# contact.php -wet : delete in database
# contact.php -[wet|dry] -u ID : Execute the script for the user with id ID
# Does not work for the public contact.
#
#
$path = ".";
$obminclude = getenv("OBM_INCLUDE_VAR");
if ($obminclude == "") $obminclude = "obminclude";
$includePath = realpath("$path/..");
set_include_path(".:$includePath");
include("$obminclude/global.inc");

$options = getopt('dryu:wet');

$dry = false;
if(isset($options['d']) && isset($options['r']) && isset($options['y'])) $dry = true;
elseif(isset($options['d']) || isset($options['r']) || isset($options['y'])) {
  echo "Unknown options \n";
  exit();
}elseif(!isset($options['w']) || !isset($options['e']) || !isset($options['t'])) {
  echo "-wet do delete -dry for dry run. You must specified one of this two options\n";
  exit();
}

if(is_numeric($options['u'])) {
  $userobm = "WHERE userobm_id = $options[u]";
}


$query = "SELECT userobm_id, userobm_login, id, name from UserObm INNER JOIN AddressBook ON owner = userobm_id $userobm GROUP BY userobm_id";
$obm_q = new DB_OBM;
$obm_q->query($query);
$obm_q2 = new DB_OBM;
while ($obm_q->next_record()) {
  $query = "SELECT 
    contact_id, contact_lastname, contact_firstname, 
    e1.email_address as contact_email,
    e2.email_address as contact_email2,
    WorkPhone.phone_number as contact_phone,
    MobilePhone.phone_number as contact_mobilephone,
    HomePhone.phone_number as contact_homephone,
    contact_archive,
    OtherPhone.phone_number as contact_otherphone
    FROM Contact 
    INNER JOIN ContactEntity ON contactentity_contact_id = contact_id
    LEFT JOIN Email e1 ON e1.email_entity_id = contactentity_entity_id AND e1.email_label = 'INTERNET;X-OBM-Ref1'
    LEFT JOIN Email e2 ON e2.email_entity_id = contactentity_entity_id AND e2.email_label = 'INTERNET;X-OBM-Ref1'
    LEFT JOIN Phone as WorkPhone ON WorkPhone.phone_entity_id = contactentity_entity_id AND WorkPhone.phone_label = 'WORK;VOICE;X-OBM-Ref1'
    LEFT JOIN Phone as MobilePhone ON MobilePhone.phone_entity_id = contactentity_entity_id  AND MobilePhone.phone_label = 'CELL;VOICE;X-OBM-Ref1'
    LEFT JOIN Phone as HomePhone ON HomePhone.phone_entity_id = contactentity_entity_id  AND HomePhone.phone_label = 'HOME;VOICE;X-OBM-Ref1'
    LEFT JOIN Phone as OtherPhone ON OtherPhone.phone_entity_id = contactentity_entity_id  AND OtherPhone.phone_label = 'OTHER;VOICE;X-OBM-Ref1'
    WHERE contact_addressbook_id = ".$obm_q->f('id')." 
    ORDER BY contact_id";
  $obm_q2->query($query);
  echo "===================================================\n";
  echo "For user ".$obm_q->f('userobm_login')." (".$obm_q->f('userobm_id')."), Addressbook ".$obm_q->f('name')." (".$obm_q->f('id').") : ".$obm_q2->nf()." contacts\n";
  $contacts = array();
  while($obm_q2->next_record()) {
    $datas = array_map('clean_values', $obm_q2->Record);
    $contacts[$obm_q2->f('contact_id')] = $datas;
  }
  $duples = array_duples($contacts);
  echo count($duples). " contacts to delete\n";
  if(count($duples) > 0 && !$dry) {
    $query = "INSERT INTO DeletedContact SELECT contact_id, contact_usercreate, NOW() , 'kill' FROM Contact WHERE contact_id IN (".implode(',', $duples).")";
    $obm_q2->query($query);
  }
}
if(!$dry) {
  $query = "DELETE FROM Entity WHERE entity_id IN (SELECT contactentity_entity_id FROM ContactEntity INNER JOIN DeletedContact ON deletedcontact_contact_id = contactentity_contact_id)";
  $obm_q->query($query);
  $query = "DELETE FROM Contact WHERE contact_id IN (SELECT deletedcontact_contact_id FROM DeletedContact)";
  $obm_q->query($query);
}


function clean_values($item) {
  $item = strtolower(trim($item));
  if($item == '-' || $item == '') {
    $item = false;
  }
  return $item;
}

function array_duples($array) {
  $drop = array();
  $copy = $array;
  foreach($array as $key => $values)  {
    foreach($copy as $ckey => $cvalues) {
      if($ckey == $key) {
	continue;
      }
      if(in_array($key, $drop)) {
        break;
      }
      if(is_equal($values, $cvalues)) {
        echo "################";
        echo "$key and $ckey are equal, $ckey will be deleted\n";
        foreach($cvalues as $field => $value) {
          if(!is_numeric($field)) {
            echo str_pad($field,32,' ',STR_PAD_RIGHT) .":". str_pad($value,64,' ',STR_PAD_LEFT).'|'.$values[$field]."\n";
          }
        }
        array_push($drop, $ckey);
      }
    }
  }
  return $drop;
}

function is_equal($val1, $val2) {
  if($val1['contact_archive'] == 1 && $val2['contact_archive'] == 0) {
    return false;
  }
  if($val1['contact_lastname'] == $val2['contact_lastname']) {
    if($val1['contact_lastname']) {
      if($val1['contact_firstname'] && $val1['contact_firstname'] == $val2['contact_firstname']) {
        return true;
      }elseif($val1['contact_firstname'] != $val2['contact_firstname']) {
	return false;
      }
      if($val1['contact_email'] && $val1['contact_email'] == $val2['contact_email']) {
        return true;
      }
      if($val1['contact_email2'] && $val1['contact_email2'] == $val2['contact_email2']) {
        return true;
      }
      if($val1['contact_phone'] && $val1['contact_phone'] == $val2['contact_phone']) {
        return true;
      }
      if($val1['contact_homephone'] && $val1['contact_homephone'] == $val2['contact_homephone']) {
        return true;
      }
      if($val1['contact_mobilephone'] && $val1['contact_mobilephone'] == $val2['contact_mobilephone']) {
        return true;
      }
    } else {
      if($val1['contact_email'] == $val2['contact_email']) {
        if($val1['contact_email']) {
          if($val1['contact_firstname'] && $val1['contact_firstname'] == $val2['contact_firstname']) {
            return true;
          }elseif($val1['contact_firstname'] != $val2['contact_firstname']) {
	    return false;
          }
          if($val1['contact_phone'] && $val1['contact_phone'] == $val2['contact_phone']) {
            return true;
          }
          if($val1['contact_homephone'] && $val1['contact_homephone'] == $val2['contact_homephone']) {
            return true;
          }
          if($val1['contact_mobilephone'] && $val1['contact_mobilephone'] == $val2['contact_mobilephone']) {
            return true;
          }
        } else {
          if($val1['contact_mobilephone'] == $val2['contact_mobilephone']) {
            if($val1['contact_mobilephone']) {
              if($val1['contact_firstname'] && $val1['contact_firstname'] == $val2['contact_firstname']) {
                return true;
              }elseif($val1['contact_firstname'] != $val2['contact_firstname']) {
        	return false;
              }
              if($val1['contact_phone'] && $val1['contact_phone'] == $val2['contact_phone']) {
                return true;
              }
              if($val1['contact_homephone'] && $val1['contact_homephone'] == $val2['contact_homephone']) {
                return true;
              }              
            } else {
              if($val1['contact_phone'] == $val2['contact_phone']) {
                if($val1['contact_phone']) {
                  if($val1['contact_firstname'] && $val1['contact_firstname'] == $val2['contact_firstname']) {
                    return true;
                  } elseif($val1['contact_firstname'] != $val2['contact_firstname']) {
		    return false;
                  }
                } else {
                  if($val1['contact_homephone'] == $val2['contact_homephone']) {
                    if($val1['contact_homephone']) {
                      if($val1['contact_firstname'] && $val1['contact_firstname'] == $val2['contact_firstname']) {
                        return true;
                      }elseif($val1['contact_firstname'] != $val2['contact_firstname']) {
                	return false;
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
  if($val1['contact_lastname'] == $val2['contact_lastname'] && !$val1['contact_firstname'] ) {
   if(!$val2['contact_email'] && !$val2['contact_email2'] && !$val2['contact_phone'] && !$val2['contact_homephone'] && !$val2['contact_mobilephone'] && !$val2['contact_otherphone'] && !$val2['contact_firstname'])
     return true;
  }
  if($val1['contact_firstname'] == $val2['contact_firstname'] && !$val1['contact_lastname'] ) {
   if(!$val2['contact_email'] && !$val2['contact_email2'] && !$val2['contact_phone'] && !$val2['contact_homephone'] && !$val2['contact_mobilephone'] && !$val2['contact_otherphone'] && !$val2['contact_lastname'])
     return true;
  }
  return false;
}
