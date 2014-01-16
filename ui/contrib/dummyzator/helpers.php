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



class IdIterator {
  var $iterator = array
    (/*
    'start' => i,
    'value' => n,
    'end'   => j,
    'len'   => l,
    'every' => k,
    'loop'  => bool
  )
     */);

  /**
   * __construct 
   * 
   * @param mixed $iterator 
   * @access public
   * @return void
   */
  public function __construct($iterator) {
    if(!isset($iterator['start'])) {
      $iterator['start'] = 0;
    }
    if(!isset($iterator['every'])) {
      $iterator['every'] = 1;
    }
    if(!isset($iterator['inc'])) {
      $iterator['inc'] = 1;
    }
    if(!isset($iterator['loop'])) {
      $iterator['loop'] = false;
    }
    $iterator['value'] = $iterator['start'];
    $iterator['step'] = 0;

    if(isset($iterator['len']) && !isset($iterator['end'])) {
      $iterator['end'] = $iterator['start'] + ($iterator['len'] * $iterator['inc']);
    } elseif(!isset($iterator['len']) && isset($iterator['end'])) {
      $iterator['len'] = ($iterator['end'] - $iterator['start']) / $iterator['inc'];
    }
    $this->iterator = $iterator;
  }

  /**
   * currentInt 
   * 
   * @access public
   * @return void
   */
  public function current() {
    if($this->iterator['value'] >= $this->iterator['end']) {
      if($iterator['loop']) {
        $this->iterator['value'] = $this->iterator['start'];
        $this->iterator['step'] = 0;
      } else {
        return null;
      }
    }
    return $this->iterator['value'];
  }

  /**
   * seek 
   * 
   * @access public
   * @return void
   */
  public function seek($position) {
    $this->reset();
    $this->iterator['step'] = $position;
    $this->iterator['value'] += $position * $this->iterator['inc'];
    return $this->current();
  }

  /**
   * nextInt 
   * 
   * @access public
   * @return void
   */
  public function next() {
    $i = $this->current();
    if($i !== null) {
      $this->iterator['step']++;
      if($this->iterator['step'] >= $this->iterator['every']) {
        $this->iterator['step'] = 0;
        $this->iterator['value'] += $this->iterator['inc'];
      }
    }
    return $i;
  }

  /**
   * gotoStart 
   * 
   * @access public
   * @return void
   */
  public function reset() {
    $this->iterator['value'] = $this->iterator['start'];
    $this->iterator['step'] = 0;
  }

  /**
   * start 
   * 
   * @access public
   * @return void
   */
  public function start() {
    return $this->iterator['start'];
  }

  /**
   * end 
   * 
   * @access public
   * @return void
   */
  public function end() {
    return $this->iterator['end'];
  }

  /**
   * end 
   * 
   * @access public
   * @return void
   */
  public function last() {
    return $this->iterator['end'] - $this->iterator['inc'];
  }

  /**
   * getSize 
   * 
   * @access public
   * @return void
   */
  public function getSize() {
    return $this->iterator['len'];
  }

}

class RandomData {


  /**
   * getInstance 
   * 
   * @static
   * @access public
   * @return void
   */
  static function getInstance() {
    static $singleton = null;
    if (is_null($singleton)) {
      $singleton = new RandomData();
    }
    return $singleton;
  }

  /**
   * __construct 
   * 
   * @access public
   * @return void
   */
  public function __construct() {
    include $GLOBALS['realPath']."/firstnames.php";
    $db = new DB_OBM;
    foreach($firstnames as $key => $firstname) {
      $db->query("INSERT INTO firstnames (data1,data2,data3) VALUES ('".addslashes($firstname)."','".addslashes($firstnames[$key+1])."','".addslashes($firstnames[$key-1])."')");
      if($key > 4998) break;
    }
    include $GLOBALS['realPath']."/lastnames.php";
    foreach($lastnames as $key =>  $lastname){
      $db->query("INSERT INTO lastnames (data1,data2,data3) VALUES ('".addslashes($lastname)."','".addslashes($lastnames[$key+1])."','".addslashes($lastnames[$key-1])."')");
      if($key > 4998) break;
    }
    include $GLOBALS['realPath']."/words.php";
    foreach($words as $key => $word) {
      $db->query("INSERT INTO words (data1,data2,data3) VALUES ('".addslashes($word)."','".addslashes($words[$key+1])."','".addslashes($words[$key-1])."')");
      if($key > 4998) break;
    }
    include $GLOBALS['realPath']."/texts.php";
    foreach($texts as $key => $text){
      $db->query("INSERT INTO texts (data1,data2,data3) VALUES ('".addslashes($text)."','".addslashes($texts[$key+1])."','".addslashes($texts[$key-1])."')");
      if($key > 4998) break;
    }
    $this->firstnames = array('firstnames', 5000);
    $this->lastnames = array('lastnames', 841);
    $this->texts = array('texts',101);
    $this->words = array('words', 241);
  }

  /**
   * getRandomFirstname 
   * 
   * @access public
   * @return void
   */
  public function getRandomFirstname() {
    return $this->getRandomFileData($this->firstnames); 
  }

  /**
   * getRandomLastname 
   * 
   * @access public
   * @return void
   */
  public function getRandomLastname() {
    return $this->getRandomFileData($this->lastnames); 
  }

  /**
   * getRandomText 
   * 
   * @access public
   * @return void
   */
  public function getRandomText($maxSize=null) {
    if($maxSize)
      return DummyGenerators::substr($this->getRandomFileData($this->texts),1,$maxSize); 
    else
      return $this->getRandomFileData($this->texts); 
  }

  /**
   * getRandomWord 
   * 
   * @access public
   * @return void
   */
  public function getRandomWord($maxSize=null) {
    if($maxSize)
      return DummyGenerators::substr($this->getRandomFileData($this->words),1,$maxSize); 
    else
      return $this->getRandomFileData($this->words);
  }

  public function getRandomFileData($handle) {
    if(DBTYPE == 'pgsql') {
      return "(SELECT data".rand(1,3)." FROM $handle[0] WHERE ids.id = ids.id LIMIT 1 OFFSET ".DummyGenerators::random(0,$handle[1]).')';
    } else {
      return "IF(@val:=".DummyGenerators::random(1,$handle[1]+1).",(SELECT data".rand(1,3)." FROM $handle[0] WHERE ids.id = ids.id LIMIT 1), '$handle[0]')";
    }
  }
}
