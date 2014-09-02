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

/**
 * This class contains a rule to step forward in time. An instance of RuleApp
 * only deals with a specific time unit (seconds, minutes, hours, weeks, months
 * or years).
 *
 * An instance of RuleApp can, given a date, attempt to step forward in time, by
 * adding one or more of its associated time units to the date and returning the
 * result. It can also determine, given two dates, if the two dates are
 * identical with respect to the time unit associated to this instance. Finally,
 * it can reset a given date to a base value with respect to its associated time
 * unit.
 */
class RuleApp {
    private $get_next_valid_value_func;
    private $has_same_value_func;
    private $reset_func;

    /**
     * Constructor. Takes as parameters closures containing the implementations of the
     * three actions available to consumers of RuleApp.
     *
     * @param callable $get_next_valid_value A function which, given a date,
     * will return the next valid value by adding one or more time units to the
     * date, may throw CannotFindNextOccurrenceException.
     *
     * @param callable $has_same_value A function which, given two dates, will
     * check if they are identical with respect to the time unit associated to
     * the rule.
     *
     * @param callable $reset A function which, given a date, will reset it to a
     * minimum value.
     */
    public function __construct($get_next_valid_value, $has_same_value, $reset) {
        $this->get_next_valid_value_func = $get_next_valid_value;
        $this->has_same_value_func = $has_same_value;
        $this->reset_func = $reset;
    }

    /**
     * Increments a date by a number of time units. For instance, a RuleApp
     * instance associated with the 'day' time unit and created with an interval
     * of 2, given a date equal to '20140131T140000', will return a date equal
     * to '20140202T140000'. Always returns a copy of the date in parameter.
     *
     * @param Of_Date $occ A date
     * @return Of_Date A copy of $occ, incremented by one or more time units
     * @throws CannotFindNextOccurrenceException if a valid date could not be
     * generated (eg, the recurrence rule is invalid)
     */
    public function get_next_valid_value($occ) {
        $func = $this->get_next_valid_value_func;
        return $func($occ);
    }

    /**
     * Tests if two dates have the same value wrt the associated time unit. For
     * instance, a RuleApp instance associated with the 'hour' time unit will
     * find that '20140131T140000' and '20140202T140000' have the same value.
     *
     * @param Of_Date $occ1 A date
     * @param Of_Date $occ2 Another date
     * @return boolean Returns true if the two dates are equals wrt to the
     * associated time unit
     */
    public function has_same_value($occ1, $occ2) {
        $func = $this->has_same_value_func;
        return $func($occ1, $occ2);
    }

    /**
     * Resets the given date and returns a copy of the date, reset to a minimum
     * value wrt to the associated time unit.
     *
     * @param Of_Date $occ A date to reset
     * @return Of_Date Another date
     */
    public function reset($occ) {
        $func = $this->reset_func;
        return $func(clone $occ);
    }
}

/**
 * Represents the application of a given ICS RRULE. Given an RRULE, it will
 * will create an instance which will be able to compute subsequent occurrences
 * of the event. It implements partially RFC 5545 section 3.3.10.
 *
 * It does not support:
 *
 * * BYYEARDAY
 * * positive/negative specifiers for week days (eg, +1MO)
 * * negative numbers for BYWEEKNO
 * * WKST (a work week always starts on Monday)
 * * BYSETPOS
 *
 */
class RRuleApplication {

    /**
     * @const MAX_ITERATIONS The number of times the application of FREQ will be
     * attempted in order to build a new valid date, and also the number of
     * times FREQ will be applied in order to satisfy a BYXXX constraint.
     */
    const MAX_ITERATIONS = 10000;

    const FREQ = 'FREQ';

    const SECONDLY = 'SECONDLY';

    const MINUTELY = 'MINUTELY';

    const HOURLY = 'HOURLY';

    const DAILY = 'DAILY';

    const WEEKLY = 'WEEKLY';

    const MONTHLY = 'MONTHLY';

    const YEARLY = 'YEARLY';

    const BYSECOND = 'BYSECOND';

    const BYMINUTE = 'BYMINUTE';

    const BYHOUR = 'BYHOUR';

    const BYDAY = 'BYDAY';

    const BYMONTHDAY = 'BYMONTHDAY';

    const BYWEEKNO = 'BYWEEKNO';

    const BYMONTH = 'BYMONTH';

    const MO = 'MO';

    const TU = 'TU';

    const WE = 'WE';

    const TH = 'TH';

    const FR = 'FR';

    const SA = 'SA';

    const SU = 'SU';

    private static function split_values($val) {
        return array_map('trim', explode(',', $val));
    }

    private static function split_integer_values($val) {
        return array_map('intval', self::split_values($val));
    }

    private static function week_day_to_number($day) {
        $number = null;
        if ($day == self::MO) {
            $number = 1;
        }
        elseif ($day == self::TU) {
            $number = 2;
        }
        elseif ($day == self::WE) {
            $number = 3;
        }
        elseif ($day == self::TH) {
            $number = 4;
        }
        elseif ($day == self::FR) {
            $number = 5;
        }
        elseif ($day == self::SA) {
            $number = 6;
        }
        elseif ($day == self::SU) {
            $number = 7;
        }
        else {
            throw InvalidWeekdayException("'$day' is an invalid week day");
        }
        return $number;
    }

    private static function week_days_to_numbers($week_days) {
        return array_map('self::week_day_to_number', $week_days);
    }

    private static function increment_field($date, $increment_function, $interval) {
        for ($multiplier = 1 ; $multiplier < self::MAX_ITERATIONS ; $multiplier++) {
            $new_date = clone $date;
            if ($increment_function($new_date, $interval * $multiplier)) {
                return $new_date;
            }
        }
        throw new CannotFindNextOccurrenceException();
    }

    private static function build_secondly_rule_app($rrule, $freq_interval) {
        $freq = $rrule[self::FREQ];

        $bysecond = array_key_exists(self::BYSECOND, $rrule) ? self::split_integer_values($rrule[self::BYSECOND]) : array();
        sort($bysecond);

        if ($freq != self::SECONDLY && empty($bysecond)) {
            return null;
        }

        $secondly_interval = $freq == self::SECONDLY ? $freq_interval : 1;

        $min_value = empty($bysecond) ? 0 : $bysecond[0];

        $get_next_valid_value = function($after_date) use ($secondly_interval, $bysecond) {
            $new_occ = clone $after_date;
            for ($i = 0 ; $i < self::MAX_ITERATIONS ; $i++) {
                $new_occ = self::increment_field($new_occ,
                    function($date, $interval) {
                        return $date->addSecond($interval);
                    }, $secondly_interval);
                if (!empty($bysecond) && !in_array($new_occ->getSecond(), $bysecond)) {
                    continue;
                }
                return $new_occ;
            }
            throw new CannotFindNextOccurrenceException();
        };
        $has_same_value = function($occ1, $occ2) {
            return $occ1->getSecond() == $occ2->getSecond();
        };
        $reset = function($occ) use ($min_value) {
            $occ->setSecond($min_value);
            return $occ;
        };
        return new RuleApp(
            $get_next_valid_value,
            $has_same_value,
            $reset
        );
    }

    private static function build_minutely_rule_app($rrule, $freq_interval) {
        $freq = $rrule[self::FREQ];

        $byminute = array_key_exists(self::BYMINUTE, $rrule) ?
            self::split_integer_values($rrule[self::BYMINUTE]) :
            array();
        sort($byminute);

        if ($freq != self::MINUTELY && empty($byminute)) {
            return null;
        }

        $minutely_interval = $freq == self::MINUTELY ? $freq_interval : 1;

        $min_value = empty($byminute) ? 0 : $byminute[0];

        $get_next_valid_value = function($after_date) use ($minutely_interval, $byminute) {
            $new_occ = clone $after_date;
            for ($i = 0 ; $i < self::MAX_ITERATIONS ; $i++) {
                $new_occ = self::increment_field($new_occ,
                    function($date, $interval) {
                        return $date->addMinute($interval);
                    }, $minutely_interval);
                if (!empty($byminute) && !in_array($new_occ->getMinute(), $byminute)) {
                    continue;
                }
                return $new_occ;
            }
            throw new CannotFindNextOccurrenceException();
        };
        $has_same_value = function($occ1, $occ2) {
            return $occ1->getMinute() == $occ2->getMinute();
        };
        $reset = function($occ) use ($min_value) {
            $occ->setMinute($min_value);
            return $occ;
        };
        return new RuleApp(
            $get_next_valid_value,
            $has_same_value,
            $reset
        );
    }

    private static function build_hourly_rule_app($rrule, $freq_interval) {
        $freq = $rrule[self::FREQ];

        $byhour = array_key_exists(self::BYHOUR, $rrule) ?
            self::split_integer_values($rrule[self::BYHOUR]) :
            array();
        sort($byhour);

        if ($freq != self::HOURLY && empty($byhour)) {
            return null;
        }

        $hourly_interval = $freq == self::HOURLY ? $freq_interval : 1;

        $min_value = empty($byhour) ? 0 : $byhour[0];

        $get_next_valid_value = function($after_date) use ($hourly_interval, $byhour) {
            $new_occ = clone $after_date;
            for ($i = 0 ; $i < self::MAX_ITERATIONS ; $i++) {
                $new_occ = self::increment_field($new_occ,
                    function($date, $interval) {
                        return $date->addHour($interval);
                    }, $hourly_interval);
                if (!empty($byhour) && !in_array($new_occ->getHour(), $byhour)) {
                    continue;
                }
                return $new_occ;
            }
            throw new CannotFindNextOccurrenceException();
        };
        $has_same_value = function($occ1, $occ2) {
            return $occ1->getHour() == $occ2->getHour();
        };
        $reset = function($occ) use ($min_value) {
            $occ->setHour($min_value);
            return $occ;
        };
        return new RuleApp(
            $get_next_valid_value,
            $has_same_value,
            $reset
        );
    }

    private static function build_daily_rule_app($rrule, $freq_interval) {
        $freq = $rrule[self::FREQ];

        $byday = array_key_exists(self::BYDAY, $rrule) ?
            self::week_days_to_numbers(self::split_values($rrule[self::BYDAY])) :
            array();
        sort($byday);

        $bymonthday = array_key_exists(self::BYMONTHDAY, $rrule) ?
            self::split_integer_values($rrule[self::BYMONTHDAY]) :
            array();
        sort($bymonthday);

        if ($freq != self::DAILY && empty($byday) && empty ($bymonthday)) {
            return null;
        }

        $daily_interval = $freq == self::DAILY ? $freq_interval : 1;
        $min_byday = empty($byday) ? 31 : $byday[0];
        $min_bymonthday = empty($bymonthday) ? 31 : $bymonthday[0];
        $get_next_valid_value = function($after_date) use ($daily_interval, $byday, $bymonthday) {
            $new_occ = clone $after_date;
            for ($i = 0 ; $i < self::MAX_ITERATIONS ; $i++) {
                $new_occ = self::increment_field($new_occ,
                    function($date, $interval) {
                        return $date->addDay($interval);
                    }, $daily_interval);
                if (!(empty($byday)) && !in_array($new_occ->getIso8601Weekday(), $byday)) {
                    continue;
                }
                # The day of the month, as a negative number, counting from the
                # end of the month (31st of December is -1)
                $month_day_counting_from_end_of_month = $new_occ->getDay() - ($new_occ->getDayCountInMonth() + 1);
                if (!empty($bymonthday) &&
                    !in_array($new_occ->getDay(), $bymonthday) &&
                    !in_array($month_day_counting_from_end_of_month, $bymonthday)) {
                    continue;
                }
                return $new_occ;
            }
            throw new CannotFindNextOccurrenceException();
        };
        $has_same_value = function($occ1, $occ2) {
            return $occ1->getDay() == $occ2->getDay();
        };
        $reset = function($occ) use ($byday, $bymonthday, $rrule, $freq) {
            if (empty($byday) && empty($bymonthday)) {
                $occ->setDay(1);
            }
            elseif (!empty($byday)) {
                $weekday = $occ->getIso8601Weekday();
                if ($freq != self::WEEKLY && !array_key_exists(self::BYWEEKNO, $rrule)) {
                    while (!in_array($weekday, $byday)) {
                        $occ->addDay(1);
                        $weekday = $occ->getIso8601Weekday();
                    }
                }
                else {
                    $diff = $byday[0] - $weekday;
                    $occ->addDay($diff);
                }
            }
            else {
                $day_count = $occ->getDayCountInMonth();
                $min_day = null;
                foreach ($bymonthday as $negative_or_positive_day) {
                    $positive_day = null;
                    if ($negative_or_positive_day < 0) {
                        $positive_day = $day_count + 1 + $negative_or_positive_day;
                    }
                    else {
                        $positive_day = $negative_or_positive_day;
                    }
                    $min_day = $min_day == null || $min_day > $positive_day ?
                        $positive_day :
                        $min_day;
                }
                $occ->setDay($min_day);
            }
            return $occ;
        };
        return new RuleApp(
            $get_next_valid_value,
            $has_same_value,
            $reset
        );
    }

    private static function build_weekly_rule_app($rrule, $freq_interval) {
        $freq = $rrule[self::FREQ];

        $byweekno = array_key_exists(self::BYWEEKNO, $rrule) ?
            self::split_integer_values($rrule[self::BYWEEKNO]) :
            array();
        sort($byweekno);

        if ($freq != self::WEEKLY && empty($byweekno)) {
            return null;
        }

        $weekly_interval = $freq == self::WEEKLY ? $freq_interval : 1;
        $min_value = empty($byweekno) ? 1 : $byweekno[0];

        $get_next_valid_value = function($after_date) use ($weekly_interval, $byweekno) {
            $new_occ = clone $after_date;
            for ($i = 0 ; $i < self::MAX_ITERATIONS ; $i++) {
                $new_occ = self::increment_field($new_occ,
                    function($date, $interval) {
                        return $date->addWeek($interval);
                    }, $weekly_interval);
                if (!(empty($byweekno)) && !in_array($new_occ->getWeek(), $byweekno)) {
                    continue;
                }
                return $new_occ;
            }
            throw new CannotFindNextOccurrenceException();
        };
        $has_same_value = function($occ1, $occ2) {
            return $occ1->getWeek() == $occ2->getWeek();
        };
        $reset = function($occ) use ($min_value) {
            $occ->setWeek($min_value);
            return $occ;
        };
        return new RuleApp(
            $get_next_valid_value,
            $has_same_value,
            $reset
        );
    }

    private static function build_monthly_rule_app($rrule, $freq_interval) {
        $freq = $rrule[self::FREQ];

        $bymonth = array_key_exists(self::BYMONTH, $rrule) ?
            self::split_integer_values($rrule[self::BYMONTH]) :
            array();
        sort($bymonth);

        if ($freq != self::MONTHLY && empty($bymonth)) {
            return null;
        }

        $monthly_interval = $freq == self::MONTHLY ? $freq_interval : 1;
        $min_value = empty($bymonth) ? 1 : $bymonth[0];

        $increment_date_func = function($date, $monthly_interval) {
            # $date->addMonth() doesn't work properly
            $original_day = $date->getDay();
            for ($i = 0 ; $i < $monthly_interval ; $i++) {
                $day_count_until_end_of_month = ($date->getDayCountInMonth() - $date->getDay());
                if (!$date->addDay($day_count_until_end_of_month + 1)) {
                    return false;
                }
                if ($date->getDayCountInMonth() >= $original_day) {
                    $date->setDay($original_day);
                }
            }
            return true;
        };

        $get_next_valid_value = function($after_date) use ($monthly_interval, $bymonth, $increment_date_func) {
            $new_occ = clone $after_date;
            for ($i = 0 ; $i < self::MAX_ITERATIONS ; $i++) {
                $new_occ = self::increment_field($new_occ, $increment_date_func, $monthly_interval);
                if (!empty($bymonth) && !in_array($new_occ->getMonth(), $bymonth)) {
                    continue;
                }
                return $new_occ;
            }
            throw new CannotFindNextOccurrenceException();
        };
        $has_same_value = function($occ1, $occ2) {
            return $occ1->getMonth() == $occ2->getMonth();
        };
        $reset = function($occ) use ($min_value) {
            $occ->setMonth($min_value);
            return $occ;
        };
        return new RuleApp(
            $get_next_valid_value,
            $has_same_value,
            $reset
        );
    }

    private static function build_yearly_rule_app($rrule, $freq_interval) {
        $freq = $rrule[self::FREQ];

        if ($freq != self::YEARLY) {
            return null;
        }

        $get_next_valid_value = function($after_date) use ($freq_interval) {
            $new_occ = clone $after_date;

            if ($new_occ->addYear($freq_interval)) {
                return $new_occ;
            }
            else {
                throw new CannotFindNextOccurrenceException();
            }
        };
        $has_same_value = function($occ1, $occ2) {
            return $occ1->getYear() == $occ2->getYear();
        };
        $reset = function($occ) {
            # As there is no other rule which comes after 'year', reset should
            # never be invoked
            throw new YearResetException();
        };
        return new RuleApp(
            $get_next_valid_value,
            $has_same_value,
            $reset
        );
    }

    /**
     * Compiles an RRuleApp from an ICS rule, given as a dictionary.
     * For instance:
     *
     * RRuleApp::from_rrule(array('FREQ' => 'DAILY'));
     *
     * @param string[] $rrule An ICS RRULE
     * @return RRuleApp A new RRuleApp instance
     */
    public static function from_rrule($rrule) {
        $interval = array_key_exists('INTERVAL', $rrule) ? intval($rrule['INTERVAL']) : 1;
        $maybe_rule_apps = array(
            self::build_secondly_rule_app($rrule, $interval),
            self::build_minutely_rule_app($rrule, $interval),
            self::build_hourly_rule_app($rrule, $interval),
            self::build_daily_rule_app($rrule, $interval),
            self::build_weekly_rule_app($rrule, $interval),
            self::build_monthly_rule_app($rrule, $interval),
            self::build_yearly_rule_app($rrule, $interval),
        );
        # Remove null rule applications
        $rule_apps = array_filter($maybe_rule_apps);
        return new RRuleApplication($rule_apps);
    }

    private $rule_apps;

    private function __construct($rule_apps) {
        $this->rule_apps = $rule_apps;
    }

    /**
     * Returns the occurrence following the date given in parameter. The
     * parameter is supposed to be itself a valid occurrence of the rule.
     *
     * @param Of_Date $after_date An occurrence of the recurrence rule
     * @return Of_Date The next occurrence
     * @throws CannotFindNextOccurrenceException If the next occurrence cannot
     * be found
     * @throws InvalidWeekdayException If a BYDAY clause exists and contains an
     * invalid week day
     */
    public function next_occurrence($after_date) {
        $already_incremented = false;
        $previous_rule_apps = array();
        $new_occ = clone $after_date;
        /*
         * Algorithm: the RRULE application contains a series of individual
         * RuleApp. Each RuleApp is associated with a given time unit, and
         * stored in a $rule_apps field, ordered by time unit. The order is:
         *
         * * secondly
         * * minutely
         * * hourly
         * * daily
         * * weekly
         * * monthly
         * * yearly
         *
         * Not all rules may be present (eg, there may be only a 'daily' rule
         * for an RRULE with a DAILY freq).
         *
         * The function applies the first rule found to find the next
         * occurrence. Then, for each rule which follows, it checks whether the
         * time unit for this rule has changed compared to $after_date. If it
         * has (eg, applying the 'daily' rule incremented the month), it checks
         * that the time unit of the new value is equal to the time unit expect
         * if only this rule had incremented $after_date (eg, the month value
         * created by the 'daily' rule will be incorrect if the month changed
         * from January to February but the monthly rule has an interval of 2).
         *
         * In case the time unit is not correct, the 'reset' function is called
         * on each previous rule, to get back to the 'basic' value for this time
         * unit (eg, if the 'daily' rule was created from BYMONTHDAY='4,5', the
         * day part of the occurrence will be set to 4).
         *
         */
        foreach ($this->rule_apps as $rule_app) {
            if (!$already_incremented) {
                $next_valid_value = $rule_app->get_next_valid_value($new_occ);

                $new_occ = $next_valid_value;
                foreach ($previous_rule_apps as $previous_rule_app) {
                    $new_occ = $previous_rule_app->reset($new_occ);
                }
                $already_incremented = true;
            }
            else {
                # Previous steps have incremented the date
                # The field of the current rule should then be either unchanged
                # or equal to the next valid value it can generate
                if (!$rule_app->has_same_value($after_date, $new_occ)) {
                    $next_valid_value = $rule_app->get_next_valid_value($after_date);
                    if (!$rule_app->has_same_value($new_occ, $next_valid_value))
                    {
                        $new_occ = $next_valid_value;
                        foreach ($previous_rule_apps as $previous_rule_app) {
                            $new_occ = $previous_rule_app->reset($new_occ);
                        }
                        $already_incremented = true;
                    }
                }
            }
            $previous_rule_apps[] = $rule_app;
        }
        if (!$already_incremented) {
            throw new CannotFindNextOccurrenceException();
        }
        return $new_occ;
    }
}

class RRuleException extends Exception {}

class CannotFindNextOccurrenceException extends RRuleException {}

class YearResetException extends RRuleException {}

class InvalidWeekdayException extends RRuleException {}
?>
