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
//New resource reservation
//A new resource reservation was scheduled
Resource Name
Subject
From
To
Location
Organizer
Created by
Comment : <?php echo $targetComment; ?>.
To accept or refuse this reservation
******************************************************************************/


?>
<table style="width:100%; border:3px solid #000;">
    <tr>
        <th style="text-align:left; background-color: blue; color:#fff; font-size:16px" colspan="2">
          新资源预订
        </th>
    </tr>
    <tr>
      <td colspan="2">
一个新的资源预订计划已被制定
</td>
    </tr>
    <tr>
        <td style="text-align:right; width:20%">资源名称</td><td><?php echo $resourceLabel; ?></td>
    </tr>
    <tr>
        <td style="text-align:right; width:20%">主题</td><td><?php echo $title; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;">从</td><td><?php echo $start; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;">到</td><td><?php echo $end; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;">地点</td><td><?php echo $location; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;">组织者</td><td><?php echo $organizer; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;">创建者</td><td><?php echo $creator; ?></td>
    </tr>
    <tr>
      <td colspan="2">
        评论: <?php echo $targetComment; ?>.
      </td>
    </tr>
    <tr>
        <td style="text-align:right;" colspan="2">
          <a href="<?php echo $host; ?>calendar/calendar_index.php?action=detailconsult&calendar_id=<?php echo $id; ?>">要接受或拒绝此预订</a>
        </td>
    </tr>
</table>
