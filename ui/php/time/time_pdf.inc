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



require('obminclude/lib/Fpdf/fpdf.php');

class TimeSheetPDF extends FPDF
{
  var $filename;
  var $data;
  var $comments;
  var $projects;
  var $username;
  var $weekdays;
  var $func;
  var $period;

  //////////////////////////////////////////////////////////////////////////
  // Writes a line goes to the next one
  // params :
  //   $h    : Line height
  //   $txt  : String to print
  //   $link : URL or identifier returned by AddLink()
  //////////////////////////////////////////////////////////////////////////
  function WriteLn($h, $txt, $link = null) {
    $this->Write($h, $txt, $link);
    $this->Ln($h);
  }

  /////////////////////////////////////////////////////////////////////////
  // Sets the user fullname
  ////////////////////////////////////////////////////////////////////////// 
  function SetUserName() {
    $user = get_user_info($this->data["user_id"]);
    $this->username = $user['firstname'] . ' ' . $user['lastname'];
  }

  //////////////////////////////////////////////////////////////////////////
  // Selects a random color
  // return :
  //    an array containing red, green and blue values
  //////////////////////////////////////////////////////////////////////////
  function RandColor() {
    $pattern = "/rgb\(([0-9]+), ([0-9]+), ([0-9]+)\)/";
    preg_match($pattern, randomColor(150, 255, 0), $colors);
    array_slice($colors, 1);

    return ($colors);
  }

  //////////////////////////////////////////////////////////////////////////
  // Sets the fill color
  // params :
  //    $color : a table containing red, green and blue values
  // see also :
  //    PDF::RandColor
  //////////////////////////////////////////////////////////////////////////
  function SetFillColor($color = array(255, 255, 255)) {
    parent::SetFillColor($color[0], $color[1], $color[2]);
  }

  //////////////////////////////////////////////////////////////////////////
  // Initializes the PDF instance
  // params :
  //    $data : all data (get/post params and other informations)
  // see also :
  //    PDF::InitWeek, PDF::InitMonth
  //////////////////////////////////////////////////////////////////////////
  function Init($data) {
    $this->data = $data;

    $this->SetUserName();
    $proj_q = run_query_time_ra_projects($this->data);

    $i = 1;
    while ($proj_q->next_record()) {
      $id = $proj_q->f('project_id');
      $this->projects[$id]['num']        = $i;
      $this->projects[$id]['name']       = $proj_q->f('project_name');
      $this->projects[$id]['shortname']  = $proj_q->f('project_shortname');
      $this->projects[$id]['color']      = $this->RandColor();
      $i++;
    }

    if ($this->data['interval'] == 'week') {
      $this->data['date'] = $this->data['week'];
      $this->InitWeek();
    }
    elseif ($this->data['interval'] == 'month') {
      $this->data['date']   = date("Y") . $this->data['month'] . '01';
      $this->InitMonth();
    }
    else {
      $this->data['date'] = $this->data['int_date'];
      if ($this->data['int_type'] == 'week')
        $this->InitWeek();
      else
        $this->InitMonth();
    }

    $user = get_user_info($this->data["user_id"]);
    $name = strtoupper(substr($user['firstname'], 0, 1) . $user['lastname']);
    $year = substr($this->data['date'], 0, 4);

    $this->filename = 'RA_'.$this->period.'_'.$year.'_'.$name.'.pdf';
  }

  //////////////////////////////////////////////////////////////////////////
  // Gets data about selected week from database
  // see also :
  //    PDF::Init, PDF::InitMonth
  //////////////////////////////////////////////////////////////////////////
  function InitWeek() {
    global $l_pdf_com_date_format;
    global $c_week_first_day;

    $this->func = 'Week';
    $this->period = 'S'.date('W', strtotime($this->data['date']));

    $task_q = run_query_time_task_list($this->data);

    // gets the first day of the selected week (timestamp)
    $d_start_week   = of_date_get_first_day_week($this->data['date']);
    $work_day_dates = get_time_week_working_days_dates($d_start_week);
    $cpt_end        = $c_week_first_day + 5;

    // assigns labels to wee days
    for ($day = $c_week_first_day; $day < $cpt_end; $day++)
      if (isset($work_day_dates[$day]))
        $this->weekdays[$work_day_dates[$day][1]]['label'] = $work_day_dates[$day][0];

    while ($task_q->next_record()) {
      $id = $task_q->f("project_id");
      if (id != '' && array_key_exists($id, $this->projects)) {
        $val     = intval($task_q->f('timetask_length'));
        $idx     = date("Ymd", $task_q->f('date_task'));
        $proj_id = $task_q->f('project_id');

        if (isset($this->weekdays[$idx]['worked'][$proj_id]))
          $val += $this->weekdays[$idx]['worked'][$proj_id];

        $this->weekdays[$idx]['worked'][$proj_id] = $val;

        if ($task_q->f('timetask_label') != '') {
          $date  = date($l_pdf_com_date_format, $task_q->f('date_task'));
          $label = $task_q->f('timetask_label');
          $num   = $this->projects[$proj_id]['num'];
          $this->comments[] = array('date' => $date, 'num' => $num, 'label' => $label);
        }
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////
  // Gets data about selected month from database
  // see also:
  //    PDF::Init, PDF::InitWeek
  //////////////////////////////////////////////////////////////////////////
  function InitMonth() {
    global $l_monthsofyear;

    $this->func = 'Month';
    $this->period = $l_monthsofyear[intval($this->data['month']) - 1];

    $task_q = run_query_time_user_month_planning($this->data);

    while ($task_q->next_record()) {
      $id   = $task_q->f('id');
      $day = intval(date('d', $task_q->f('date')));
      if (array_key_exists($id, $this->projects))
        $this->projects[$id]['worked'][$day] = $task_q->f("timetask_length");
    }
  }

  //////////////////////////////////////////////////////////////////////////
  // Writes de PDF header (Logo, Title, Generation date)
  // see also :
  //    PDF::Body, PDF::Footer
  //////////////////////////////////////////////////////////////////////////
  function Header() {
    global $l_pdf_title, $l_pdf_date, $l_pdf_date_format;
    global $cimage_logo;

    $pdf_logo_w = 55;
    $pdf_logo_h = 15;

    // logo
    $file_logo = "../../conf/themes/images/logos/$cimage_logo";
    if (file_exists($file_logo)) {
      $this->Image($file_logo, 10, 10, $pdf_logo_w, $pdf_logo_h);
    }

    // title
    $this->SetFont('Arial', 'B', 15);
    $this->SetX(65);
    $this->Cell(90, 15, $l_pdf_title, 0, 0, 'C');

    // generation date
    $this->SetFont('Arial', '', 8);
    $this->SetTextColor(150, 150, 150);
    $this->SetXY($this->GetX(), $this->GetY() + 5);
    $this->Cell(45, 5, $l_pdf_date, 0, 2, 'C');
    $this->Cell(45, 5, date($l_pdf_date_format), 0, 0, 'C');
    $this->SetTextColor(0, 0, 0);
    $y = $this->getY();
    $this->Line(10, $y + 10, 200, $y + 10);

    $this->Ln(40);
  }

  //////////////////////////////////////////////////////////////////////////
  // Writes the PDF Body
  // see also :
  //    PDF::Header, PDF::Footer
  //////////////////////////////////////////////////////////////////////////
  function Body() {
    global $l_pdf_user;

    $this->SetX(20);
    $this->SetFont('Arial', 'U', 10);
    $this->Write(10, $l_pdf_user);
    $this->SetFont('Arial', '', 10);
    $this->Write(10, ' : ');
    $this->SetFont('Arial', 'B', 10);
    $this->WriteLn(10, $this->username);

    $this->ProjectsList();
    $this->{$this->func}();
    $this->Comments();
    $this->Signatures();
  }

  //////////////////////////////////////////////////////////////////////////
  // Displays the selected projects list
  //////////////////////////////////////////////////////////////////////////
  function ProjectsList() {
    global $l_pdf_projects;

    $this->SetX(20);
    $this->SetFont('Arial', 'U', 10);
    $this->Write(10, $l_pdf_projects);
    $this->SetFont('Arial', '', 10);
    $this->Write(10, ' :');
    $this->SetFont('Arial', 'B', 10);

    foreach ($this->projects as $proj_id => $proj) {
      $this->Ln(5);
      $this->SetX(30);
      $this->Write(8, "[{$proj['num']}] {$proj['name']}");
    }

    $this->SetFont('Arial', '', 10);
    $this->Ln(10);
  }

  //////////////////////////////////////////////////////////////////////////
  // Displays a table representing a week activity
  // see also :
  //    PDF::InitWeek, PDF::Month
  //////////////////////////////////////////////////////////////////////////
  function Week() {
    $y = $this->GetY();

    $i = 0;
    foreach ($this->weekdays as $day) {
      $this->WeekDay($i, $day, $y);
      $i++;
    }
    $this->SetY($y + 40);
  }

  //////////////////////////////////////////////////////////////////////////
  // Displays a week column
  // see also :
  //    PDF::Week
  //////////////////////////////////////////////////////////////////////////
  function WeekDay($cpt, $day, $y) {
    global $c_day_fraction;

    $worked = $day['worked'];
    $cell_w = 34;
    $x      = 20 + $cpt * $cell_w;

    $this->SetXY($x, $y);
    $year = substr($this->data['date'], 2, 2);
    $this->Cell($cell_w, 10, $day['label'].' '.$year, 1, 2, 'C');

    $this->SetX($x);
    $this->Cell($cell_w, 30, '', 1, 0, '', 0);

    if (!empty($worked)) {
      ksort($worked);
      foreach ($worked as $id => $length) {
        $h   = min(30 * ($length/$c_day_fraction), 30);
        if ($length == $c_day_fraction)
            $len = '1';
        else if ($length == $c_day_fraction/2)
            $len = '0.5';
        else
            $len = $length.'/'.$c_day_fraction;
        $val = "[{$this->projects[$id]['num']}] $len";

        $this->SetX($x);
        $this->SetFontSize(8);
        $this->SetFillColor($this->projects[$id]['color']);
        $this->Cell($cell_w, $h, $val, 'LR', 2, 'C', 1);
        $this->SetFillColor();
        $this->SetFontSize(10);
      }
    }
  }

  //////////////////////////////////////////////////////////////////////////
  // Displays a table representing a month activity
  // see also :
  //    PDF::InitMonth, PDF::Week
  //////////////////////////////////////////////////////////////////////////
  function Month() {
    global $c_day_fraction;
    global $l_daysofweekfirst;
    global $l_monthsofyear;

    $cell_w = 5;

    // number of days in the month
    $days = date('t', strtotime($this->data['date']));
    $week_day = date("w", strtotime($this->data['date']));

    $this->SetFontSize(14);
    $month = $l_monthsofyear[intval($this->data['month']) - 1];
    $year = substr($this->data['date'], 0, 4);
    $this->Cell(190, 8, $month.' '.$year, 0, 2, 'C');

    $y = $this->GetY();
    $margin = (210 - ($days + 1) * $cell_w) / 2;
    $this->SetX($margin + $cell_w);
    $this->SetFontSize(8);

    for  ($i = 0; $i < $days; $i++) {
      $this->Cell($cell_w, $cell_w, $l_daysofweekfirst[$week_day], 0, 0, 'C');
      $week_day = ($week_day + 1) % 7;
    }

    $this->Ln(5);
    $this->SetX($margin + $cell_w);
    $this->SetFontSize(9);

    for  ($i = 1; $i <= $days; $i++)
      $this->Cell($cell_w, $cell_w, ($i > 9 ? $i : "0$i"), 1, 0, 'C');

    $this->Ln($cell_w);

    $this->SetFillColor();
    $this->SetFontSize(8);
    foreach ($this->projects as $id => $proj) {
      $this->SetX($margin);
      $this->Cell($cell_w, $cell_w, "[{$proj['num']}]", 1, 0, 'C');
      for  ($i = 1; $i <= $days; $i++) {
        if ($proj['worked'][$i] != '')
        {
          if ($proj['worked'][$i] == $c_day_fraction)
            $len = '1';
          else if ($proj['worked'][$i] == $c_day_fraction/2)
            $len = '0.5';
          else
            $len = $proj['worked'][$i].'/'.$c_day_fraction;

          $this->SetFillColor($proj['color']);
        }
        else
          $len = '';

        $this->Cell($cell_w, $cell_w, $len, 1, 0, 'C', 1);
        $this->SetFillColor();
      }
      $this->Ln($cell_w);
    }
    $this->SetY($y + (count($this->projects) + 2) * $cell_w);
  }

  //////////////////////////////////////////////////////////////////////////
  // Displays the list of comments
  //////////////////////////////////////////////////////////////////////////
  function Comments() {
    global $l_pdf_comments;

    $this->SetFontSize(10);
    $this->Ln(5);
    $this->SetX(20);
    $this->SetFont('Arial', 'U', 10);
    $this->Write(5, $l_pdf_comments);
    $this->SetFont('Arial', '', 10);
    $this->WriteLn(5, ' :');
    $y = $this->GetY();
    $this->Line(20, $y, 190, $y);

    $this->SetXY(25, $y + 2);
    $this->SetMargins(25, 10, 25);
    if ($this->data['comment'] != "") {
      $this->Write(5, $this->data['comment']);
      $this->Ln(10);
    }

    $this->SetFont('Arial', 'B', 10);
    if ($this->data['interval'] == 'week' && !empty($this->comments)) {
      foreach ($this->comments as $comment) {
        $this->SetX(25);
        $this->WriteLn(5, "{$comment['date']} : [{$comment['num']}] {$comment['label']}");
      }
    }

    $this->SetFont('Arial', '', 10);
    $y = $this->GetY();
    $this->Line(20, $y + 2, 190, $y + 2);
  }

  //////////////////////////////////////////////////////////////////////////
  // Displays the signatures zone
  //////////////////////////////////////////////////////////////////////////
  function Signatures() {
    global $l_pdf_visa_user;
    global $l_pdf_visa_client;
    global $l_pdf_visa_company;

    $this->SetXY(14, 217);
    $this->Cell(54, 10, $l_pdf_visa_user, 0, 0, 'C');
    $this->SetXY(78, 217);
    $this->Cell(54, 10, $l_pdf_visa_client, 0, 0, 'C');
    $this->SetXY(142, 217);
    $this->Cell(54, 10, $l_pdf_visa_company, 0, 0, 'C');

    $this->SetXY(14, 227);
    $this->Cell(54, 45, '', 1, 0);
    $this->SetXY(78, 227);
    $this->Cell(54, 45, '', 1, 0);
    $this->SetXY(142, 227);
    $this->Cell(54, 45, '', 1, 0);
  }

  //////////////////////////////////////////////////////////////////////////
  // Displays the PDF footer
  //////////////////////////////////////////////////////////////////////////
  function Footer() {}
}

?>
