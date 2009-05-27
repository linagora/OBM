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

class EventManager {
  var $date;
  var $initial_date;
  var $events = array(/*
      '2009-05-14' => array(
        '1', '2', '10', '11', ... // hours taken
      ),
      ...
*/);

  public function __construct($current_time = null)
  {
    if($current_time === null) {
      $current_time = time();
    }
    $this->date = new Of_Date($current_time, 'Europe/Paris');
    // Begin one month before today
    $this->date->setHour(0)->setMinute(0)->setSecond(0)->subMonth(1);
    $this->initial_date = clone $this->date;
  }

  public function gotoInitialDate()
  {
    $this->date = clone $this->initial_date;
  }

  public function addRepeatEvent($allday, $hour, $duration, $end, $repeatkind, $freq, $repeatdays)
  {
    /* We use $event and $of only for occurences calculations */
    $event = new Event(0, $duration,
                       /* title */null, /* location */null, /* category1 */null,
                       /* privacy */0, /* desc */null, /* properties */null,
                       $allday, $repeatkind, /* owner */0,
                       /* owner name */null, /* color*/null);
    //$of = new OccurrenceFactory();
    $of = &OccurrenceFactory::getInstance();
    // setBegin() and setEnd() takes their dates with hour 0
    $this->date->setHour(0);
    $end->setHour(0);
    $of->setBegin($this->date);
    $of->setEnd($end);

    $this->date->setHour($hour);
    $end->setHour($hour);

    //$of->setBegin(2009-05-17 22:00:00);
    //$of->setEnd(2009-05-24 22:00:00);
    //calendar_daily_repeatition(2009-05-14 06:00:00,2009-05-17 06:00:00,2009-05-22 07:00:00,2,$event,5,user, ACCEPTED)
    switch ($repeatkind) {
      case 'daily' :
        calendar_daily_repeatition
          ($this->date,$this->date, $end, $freq, $event, 0, 'user', 'ACCEPTED');
        break;
      case 'weekly' :
        calendar_weekly_repeatition
          ($this->date,$this->date, $end, $repeatdays, $freq, $event, 0, 'user', 'ACCEPTED');
        break;
      case 'monthlybyday' :
        calendar_monthlybyday_repeatition
          ($this->date,$this->date, $end, $freq, $event, 0, 'user', 'ACCEPTED');
        break;
      case 'monthlybydate' :
        calendar_monthlybydate_repeatition
          ($this->date,$this->date, $end, $freq, $event, 0, 'user', 'ACCEPTED');
        break;
      case 'yearly' :
        calendar_yearly_repeatition
          ($this->date,$this->date, $end, $freq, $event, 0, 'user', 'ACCEPTED');
        break;
    }

    /* Get the generated events back in your data */
    $occurences = $of->getOccurrences();
    $of->reset();
    foreach($occurences as $oc) {
      $this->date = clone $oc->date;
      if(!$this->addEvent($allday, $oc->event->duration, false)) {
        return false;
      }
    }
    return true;
  }

  public function addEvent($allday, &$duration, $do_mangle = true, $pretend = false)
  {
    if($allday) {
      $duration = 3600 * 24;
      $this->date->setHour(0);
    }
    $hours = clone $this->date;
    $hours->setMinute(0)->setSecond(0);

    $endhour = clone $hours;
    $endhour->addSecond($duration);

    $is_busy = 1;
    while(Of_Date::cmp($hours, $endhour) < 0) {
      $is_busy = $this->isDateTimeBusy($hours);
      if($is_busy)
        break;

      $hours->addHour(1);
    }

    if(!$is_busy) {
      /* All right, insert the event */
      if(!$pretend) {
        $this->registerEvent($duration);
      }
      return true;
    }
    if(!$do_mangle) {
      return false;
    }

    /* Try to mangle the event to make it not to conflict */ 
    if(!$allday) {
      /* Decrease the endtime */
      $dur = $duration;
      while($dur > 0) {
        if($this->addEvent($allday, $dur, false)) {
          $duration = $dur;
          return true;
        }
        $dur -= 3600;
      }
    }

    /* Increase the starttime */
    $bak_date = clone $this->date;
    while(1) {
      if($allday) {
        $this->date->addDay(1);
      } else {
        $this->date->addHour(1);
      }
      /* This call may mangle the event trying, to decrease its duration */
      if($this->addEvent($allday, $duration, true)) {
        return true;
      }
    }
    $this->date = $bak_date;

    return false;
  }

  private function registerEvent($duration)
  {
    $starthour = clone $this->date;
    $endhour   = clone $this->date;
    $endhour->addSecond($duration);

    while(Of_Date::cmp($starthour, $endhour) < 0) {
      $date = $starthour->getDateIso();
      $this->ensureArray($this->events, $date);

      if(assert(!in_array($starthour->getHour(), $this->events[$date])))
        array_push($this->events[$date], $starthour->getHour());

      $starthour->addHour(1);
    }
  }

  public function gotoNextFreeTime()
  {
    while($this->isDateTimeBusy()) {
      $this->date->addHour(1);
    }
  }

  public function isDateTimeBusy($date = null)
  {
    if($date === null) {
      $date = $this->date;
    }
    $day = $date->getDateIso();
    $hour = $date->getHour();
    if(array_key_exists($day, $this->events)) {
      return in_array($hour, $this->events[$day]);
    } else {
      return false;
    }
  }

  private function ensureArray(&$arr, $key)
  {
    if(!isset($arr[$key])) {
      $arr[$key] = array();
    }
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
