<?php

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
    $this->firstnames = $firstnames;
    include $GLOBALS['realPath']."/lastnames.php";
    $this->lastnames = $lastnames;
    include $GLOBALS['realPath']."/texts.php";
    $this->texts = $texts;
    include $GLOBALS['realPath']."/words.php";
    $this->words = $words;
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
       return mb_substr($this->getRandomFileData($this->texts),0,$maxSize, 'UTF-8'); 
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
      return mb_substr($this->getRandomFileData($this->words),0,$maxSize, 'UTF-8'); 
    else
      return $this->getRandomFileData($this->words);
  }

  public function getRandomFileData($handle) {
    $pos = rand(0, (count($handle) - 1));
    return $handle[$pos];
  }
}
