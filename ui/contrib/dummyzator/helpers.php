<?php

class IntIterator {
  var $iter = array
    (/*
      'start' => i,
      'value' => n,
      'end'   => j,
      'len'   => l,
      'every' => k,
      'loop'  => bool
      )
     */);

  public function __construct($iter) {
    if(!isset($iter['start'])) {
      $iter['start'] = 0;
    }
    if(!isset($iter['every'])) {
      $iter['every'] = 1;
    }
    if(!isset($iter['inc'])) {
      $iter['inc'] = 1;
    }
    if(!isset($iter['loop'])) {
      $iter['loop'] = false;
    }
    $iter['value'] = $iter['start'];
    $iter['step'] = 0;

    if(isset($iter['len']) && !isset($iter['end'])) {
      $iter['end'] = $iter['start'] + $iter['len'];
    }
    $this->iter = $iter;
  }

  public function currentInt() {
    if($this->iter['value'] >= $this->iter['end']) {
      if($iter['loop']) {
        $this->iter['value'] = $this->iter['start'];
        $this->iter['step'] = 0;
      } else {
        return null;
      }
    }
    return $this->iter['value'];
  }

  public function nextInt()
  {
    $i = $this->currentInt();
    if($i !== null) {
      $this->iter['step']++;
      if($this->iter['step'] >= $this->iter['every']) {
        $this->iter['step'] = 0;
        $this->iter['value'] += $this->iter['inc'];
      }
    }
    return $i;
  }

  public function start()
  {
    return $this->iter['start'];
  }

  public function end()
  {
    return $this->iter['end'];
  }

  public function every()
  {
    return $this->iter['every'];
  }
}


function randomAsciiStr($len = 8)
{
  $str = '';
  while($len--) {
    $str .= chr(rand(33, 126));
  }
  return $str;
}

function randomAlphaStr($len = 8)
{
  $str = '';
  while($len--) {
    do {
      $i = rand(48, 122);
      } while(($i > 90 && $i < 97) || ($i > 57 && $i < 65));
    $str .= chr($i);
  }
  return $str;
}
