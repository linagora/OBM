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

function compute_recurrence_end($vevent, $display_period_end_date) {
  $rrule = $vevent->getRrule();
  if ($rrule == null) {
    return null;
  }
  $last_occurrence = null;
  if ($rrule['until'] != null) {
    if ($rrule['count'] != null) {
      error_log("Got event with invalid recurrence rule: contains both UNTIL and COUNT");
    }
    $last_occurrence = $rrule['until'];
  }
  elseif ($rrule['count'] != null) {
    $last_occurrence = compute_last_occurrence_from_count($vevent);
  }
  $recurrence_end = $last_occurrence != null ? $last_occurrence->addTimestamp($vevent->getDuration()) : null;
  $recurrence_end_within_display_period = $recurrence_end != null && $display_period_end_date->compare($until) > 0 ?
    $recurrence_end :
    clone $display_period_end_date;
  return $recurrence_end_within_display_period;
}

function get_freq($rrule) {
  $freq = $rrule['freq'];
  if ($freq == null) {
    error_log("Got an invalid recurrence rule without a freq: ".print_r($rrule, true));
    return null;
  }
  if (!in_array($freq, array('secondly', 'minutely', 'hourly', 'daily', 'weekly', 'monthly', 'yearly'))) {
    error_log("Got unknown freq type '".$freq."' in recurrence rule: ".print_r($rrule, true));
    return null;
  }
  return $freq;
}

function split_rrule_prop($rrule, $prop_name) {
  return $rrule[$prop_name] != null ? explode(',', $rrule[$prop_name]) : null;
}

# Returns the smallest value in $byxxx which is > to $date_field.
function find_next_abs_byxxx_value_for_this_period($date_field, $byxxx) {
  $next_byxxx_value = null;
  foreach ($byxxx as $byxxx_value) {
    if ($byxxx_value > $date_field) {
      if ($next_byxxx_value == null || $byxxx_value < $next_byxxx_value) {
        $next_byxxx_value = $byxxx_value;
      }
    }
  }
  return $next_byxxx_value;
}

# Finds either the smallest value in $byxxx which is > $date_field, or the
# smallest value > $min_value
function find_next_abs_byxxx_value($date_field, $byxxx, $min_value) {
  $next_value = null;
  $cur_period_next_value = find_next_abs_byxxx_value_for_this_period($date_field, $byxxx);
  if ($cur_period_next_value != null) {
    $next_value = $cur_period_next_value;
  }
  else {
    $next_value = find_next_abs_byxxx_value_for_this_period($min_value - 1, $byxxx);
  }
  return $next_value;
}

# returns $byxx with any negative value substracted (in absolute value) from
# $max_value + 1 (lets us turn BYYEARDAY: -1 into 365 for a year with 365 days)
# If the normalized value is negative, it is discarded
function normalize_rel_byxxx($byxxx, $max_value, $min_value) {
  $normalized_byxxx = array();
  foreach ($byxxx as $byxxx_value) {
    $normalized_byxxx_value = $byxxx_value > 0 : $byxxx_value : $max_value + 1 + $byxxx_value;
    if ($normalized_byxxx_value >= $min_value) {
      $normalized_byxxx[] = $normalized_byxxx_value;
    }
  }
  return $normalized_byxxx;
}

function find_next_rel_byxxx_value($date_field, $byxxx, $min_value, $cur_period_max_value, $next_period_max_value) {
  $next_value = null;
  $cur_period_normalized_byxxx = normalize_rel_byxxx($byxxx, $cur_period_max_value, $min_value);
  $cur_period_next_value = find_next_abs_byxxx_value_for_this_period($date_field, $cur_period_normalized_byxxx);
  if ($cur_period_next_value != null) {
    $next_value = $cur_period_next_value;
  }
  else {
    $next_period_normalized_byxxx = $cur_period_max_value != $next_period_max_value ?
      normalize_rel_byxxx($byxxx, $next_period_max_value, $min_value);
    $next_value = find_next_abs_byxxx_value_for_this_period($min_value, $next_period_normalized_byxxx);
  }
  return $next_value;
}

function increase_by_month($date, $bymonth) {
  if (empty($bymonth)) {
    return;
  }
  $min_month = 1;
  $next_month_number = find_next_abs_byxxx_value($date->getMonth(), $bymonth, $min_month);
  if ($next_month_number > $date->getMonth()) {
    $date->setMonth($next_month_number);
  }
  else {
    $date->setMonth($next_month_number);
    $date->addYear(1);
  }
}

function mk_date_of_last_day_of_year($year) {
  $date = new Of_Date(0);
  $date->setDay(31);
  $date->setMonth(12);
  $date->setYear($year);
  return $date;
}

function increase_by_week_number($date, $byweekno) {
  if (empty($byweekno)) {
    return;
  }
  $min_week = 1;
  $cur_year_week_count = mk_date_of_last_day_of_year($date->getYear())->getWeek();
  $next_year_week_count = mk_date_of_last_day_of_year($date->getYear() + 1)->getWeek();
  $next_week_no_number = find_next_rel_byxxx_value($date->getWeek(), $byweekno, $min_week, $cur_year_week_count, $next_year_week_count);
  if ($next_week_no_number > $date->getWeek()) {
    $date->setWeek($next_week_no_number);
  }
  else {
    $date->setWeek($next_week_no_number);
    $date->addYear(1);
  }
}

function increase_by_year_day($date, $byyearday) {
  if (empty($byyearday)) {
    return;
  }
  $min_day = 1;
  $cur_year_day_count = mk_date_of_last_day_of_year($date->getYear())->getDayOfYear();
  $next_year_day_count = mk_date_of_last_day_of_year($date->getYear() + 1)->getDayOfYear();
  $next_year_day = find_next_rel_byxxx_value($date->getDayOfYear(), $byyearday, $min_day, $cur_year_day_count, $next_year_day_count); 
  if ($next_year_day > $day->getDayOfYear()) {
    $date->setDayOfYear($next_year_day);
  }
  else {
    $date->setDayOfYear($next_year_day);
    $date->addYear(1);
  }
}

function mk_first_day_of_month($month, $year) {
  $date = new Of_Date(0);
  $date->setDay(1);
  $date->setMonth($month);
  $date->setYear($year);
  return $date;
}

function increase_by_monthday($date, $bymonthday) {
  if (empty($bymonthday)) {
    return;
  }
  $min_day = 1;
  $cur_month_day_count = $date->getDayCountInMonth();
  $next_month_day_count = mk_first_day_of_month($date->getMonth() + 1, $date->getYear)->getDayCountInMonth();
  $next_month_day = find_next_rel_byxxx_value($date->getDay(), $bymonthday, $min_day, $cur_month_day_count, $next_month_day_count);
  if ($next_month_day == null) {
    return;
  }
  if ($next_month_day > $day->getDay()) {
    $date->setDay($next_month_day);
  }
  else {
    if ($next_month_day_count
    $date->setDay($next_month_day);
    $date->addMonth(1);
  }
}

function week_day_to_number($day) {
  $number = null;
  if ($day == 'MO') {
    $number = 0;
  }
  elseif ($day == 'TU') [
    $number = 1;
  }
  elseif ($day == 'WE') {
    $number = 2;
  }
  elseif ($day == 'TH') {
    $number = 3;
  }
  elseif ($day == 'FR') {
    $number = 4;
  }
  elseif ($day == 'SA') {
    $number = 5;
  }
  elseif ($day == 'SU') {
    $number = 6;
  }
  else {
    error_log("Invalid week day $day, using Monday as default");
    $number = 0;
  }
  return $number;
}

function increase_by_day($date, $byday_number) {
  if (empty($byday_number)) {
    return;
  }
  $min_day = 0;
  $next_day_number = find_next_abs_byxxx_value($date->getWeekday(), $byday_number, $min_day);
  if ($next_day_number > $date->getDay()) {
    $date->setWeekday($next_day_number);
  }
  else {
    $date->setWeekday($next_week_no_number);
    $date->addWeek(1);
  }
}

function increase_by_hour($date, $byhour) {
  if (empty($byhour)) {
    return;
  }
  $min_hour = 0;
  $next_hour = find_next_abs_byxxx_value($date->getHour(), $byhour, $min_hour);
  if ($next_hour > $date->getHour()) {
    $date->setHour($next_hour);
  }
  else {
    $date->setHour($next_hour);
    $date->addDay(1);
  }
}

function increase_by_minute($date, $byminute) {
  if (empty($byminute)) {
    return;
  }
  $min_minute = 0;
  $next_minute = find_next_abs_byxxx_value($date->getMinute(), $byminute, $min_minute);
  if ($next_minute > $date->getMinute()) {
    $date->setMinute($next_minute);
  }
  else {
    $date->setMinute($next_minute);
    $date->addHour(1);
  }
}

function increase_by_second($date, $bysecond) {
  if (empty($bysecond)) {
    return;
  }
  $min_second = 0;
  $next_second = find_next_abs_byxxx_value($date->getSecond(), $bysecond, $min_second);
  if ($next_minute > $date->getSecond()) {
    $date->setSecond($next_second);
  }
  else {
    $date->setSecond($next_second);
    $date->addMinute(1);
  }
}

function compute_last_occurrence_from_count($vevent, $interval_start, $interval_end) {
  $occurrences = compute_occurrences_in_interval($vevent, $start, $end);
  return empty($occurrences) ? null : end($occurences);
}

function compute_occurrences_in_interval($vevent, $interval_start, $interval_end) {
  $rrule = $vevent->getRrule();
  $max_date = min($interval_start, $rrule['until']);

  if ($max_date < $interval_start || $max_date < $vevent->start) {
    return array();
  }

  $freq = get_freq($rrule);
  if ($freq == null) {
    return null;
  }
  $count = (int)$rrule['count'];
  $time_unit_count = $rrule['interval'] != null ? (int)$rrule['interval'] : 1;
  $bysecond = split_rrule_prop($rrule, 'bysecond');
  $byminute = split_rrule_prop($rrule, 'byminute');
  $byhour = split_rrule_prop($rrule, 'byhour');
  $byday = split_rrule_prop($rrule, 'byday');
  $byday_number = $byday != null ? array_map('week_day_to_number', $byday);
  $bymonthday = split_rrule_prop($rrule, 'bymonthday');
  $byyearday = split_rrule_prop($rrule, 'byyearday');
  $byweekno = split_rrule_prop($rrule, 'byweekno');
  $bymonth = split_rrule_prop($rrule, 'bymonth');
  $bysetpos = split_rrule_prop($rrule, 'bysetpos');
  $work_week_start = $rrule['wkst'];
  if ($work_week_start != null && $work_week_start != 'MO') {
    error_log("Can't handle work week not starting on monday (got $work_week_start), tough luck");
  }
  if ($bysetpos != null) {
    error_log("Sorry, no support for bysetpos");
  }

  $occurrences = array();
  $last_occurrence = clone $vevent->start;
  if ($last_occurrence >= $interval_start) {
    $occurrences[] = $last_occurrence;
  }
  # Decrease count as DTSTART count as the first occurrence
  $time_unit_count = $interval * $count;
  $real_count = $count - 1;
  for ($i = 0 ; $i < $real_count ; $i++) {
    $occurrence = clone $last_occurrence;
        # BYMONTH, BYWEEKNO, BYYEARDAY, BYMONTHDAY, BYDAY,
    #       BYHOUR, BYMINUTE, BYSECOND and BYSETPOS
    switch ($freq) {
      case 'secondly':
        $last_occurrence = $occurrence->addSecond($time_unit_count);
        increase_by_month($occurrence, $bymonth);
        increase_by_year_day($occurrence, $byyearday);
        increase_by_monthday($occurrence, $bymonthday);
        increase_by_week_day_number($occurrence, $byday_number);
        increase_by_hour($occurrence, $byhour);
        increase_by_minute($occurrence, $bysecond);
        break;
      case 'minutely':
        increase_by_month($occurrence, $bymonth);
        increase_by_year_day($occurrence, $byyearday);
        increase_by_monthday($occurrence, $bymonthday);
        increase_by_week_day_number($occurrence, $byday_number);
        $last_occurrence = $occurrence->addMinute($time_unit_count);
        break;
      case 'hourly':
        increase_by_month($occurrence, $bymonth);
        increase_by_year_day($occurrence, $byyearday);
        increase_by_monthday($occurrence, $bymonthday);
        increase_by_week_day_number($occurrence, $byday_number);
        $last_occurrence = $occurrence->addHour($time_unit_count);
        break;
      case 'daily':
        increase_by_month($occurrence, $bymonth);
        increase_by_monthday($occurrence, $bymonthday);
        increase_by_week_day_number($occurrence, $byday_number);
        $last_occurrence = $occurrence->addDay($time_unit_count);
        break;
      case 'weekly':
        increase_by_month($occurrence, $bymonth);
        increase_by_week_day_number($occurrence, $byday_number);
        $last_occurrence = $occurrence->addWeek($time_unit_count);
        break;
      case 'monthly':
        increase_by_month($occurrence, $bymonth);
        increase_by_monthday($occurrence, $bymonthday);
        
        # See note 1 
        increase_by_week_day_number($occurrence, $byday_number);
        $last_occurrence = $occurrence->addMonth($time_unit_count);
        break;
      case 'yearly':
        increase_by_month($occurrence, $bymonth);
        increase_by_week_number($occurrence, $byweekno);
        increase_by_year_day($occurrence, $byyearday);
        increase_by_monthday($occurrence, $bymonthday);
        
        # See note 2
        increase_by_week_day_number($occurrence, $byday_number);
        $last_occurrence = $occurrence->addYear($time_unit_count);
        break;
    }
    $last_occurrence = $occurrence;
    if ($occurrence >= $interval_start && $occurrence <= $max_date) {
      $occurrences[] = $occurrences;
    }
  }
  return $occurrences;
}
?>
