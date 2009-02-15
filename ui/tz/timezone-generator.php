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
$path='/tmp/timezone';
if(!is_dir($path)) {
  mkdir($path);
}
$timezones = DateTimeZone::listIdentifiers();
foreach ($timezones as $tzIdentifier) {
  $timezone = new DateTimeZone($tzIdentifier);
  $transitions = $timezone->getTransitions();
  $timezoneInfos = array();
  if($transitions[0]) {
    $timezoneInfos[] = array( 'from' => false, 'to' => $transitions[0]['ts'], 'offset' => $transitions[0]['offset']);
  } else {
    $timezoneInfos[] = array( 'from' => false, 'to' => false, 'offset' => 0);
  }
  for($i = 0; $i < count($transitions); $i++) {
    $transition = $transitions[$i];
    if($i == (count($transitions) -1)) {
      $timezoneInfos[] = array( 'from' => $transition['ts'], 'to' => false, 'offset' => $transition['offset']);
    } else {
      $timezoneInfos[] = array( 'from' => $transition['ts'], 'to' => $transitions[$i +1]['ts'], 'offset' => $transition['offset']);
    }
  }
  $filePath = explode('/', $tzIdentifier);
  $currenPath = $path;
  $i = 0;
  while($i < (count($filePath) - 1)) {
    $currenPath .= '/'.$filePath[$i];
    if(!is_dir($currenPath)) {
      mkdir($currenPath);
    }
    $i++;
  }
  $handle = fopen($currenPath.'/'.$filePath[$i], 'w');
  fwrite($handle, json_encode($timezoneInfos));
  fclose($handle);
}
