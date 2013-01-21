<?php
/******************************************************************************
Copyright (C) 2011-2012 Linagora

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


?>
<table style="width:80%; border:1px solid #000; border-collapse:collapse;background:#EFF0F2;font-size:12px;">
    <tr>
        <th style="text-align:center; background-color: #509CBC; color:#FFF; font-size:14px" colspan="2">
          Appointment Updated
        </th>
    </tr>
    <tr>
        <td colspan="2">
The appointment <?php echo $title; ?>, initially scheduled from <?php echo $old_start; ?> to <?php echo $old_end; ?>, (location : <?php echo $old_location; ?>),
was updated :</td>
    </tr>
    <tr>
       <td style="text-align:right; width:20%;padding-right:1em;">Subject</td><td style="font-weight:bold;"><?php echo $title; ?></td>
    </tr>
    <tr>
      <td style="text-align:right;padding-right:1em;">From</td><td style="font-weight:bold;"><?php echo $start; ?></td>
    </tr>
    <tr>
      <td style="text-align:right;padding-right:1em;">To</td><td style="font-weight:bold;"><?php echo $end; ?></td>
    </tr>
    <tr>
      <td style="text-align:right;padding-right:1em;">Location</td><td style="font-weight:bold;"><?php echo $location; ?></td>
    </tr>
    <tr>
      <td style="text-align:right;padding-right:1em;">Organized by</td><td style="font-weight:bold;"><?php echo $organizer; ?></td>
    </tr>
    <tr>
      <td style="text-align:right;padding-right:1em;">Created by</td><td style="font-weight:bold;"><?php echo $creator; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Attendee(s)</td><td style="font-weight:bold;"><?php echo $attendees; ?></td>
    </tr>
</table>
