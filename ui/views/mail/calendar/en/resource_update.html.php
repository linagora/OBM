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
<table style="width:100%; border:3px solid #000;">
    <tr>
        <th style="text-align:left; background-color: blue; color:#fff; font-size:16px" colspan="2">
          Resource reservation updated
        </th>
    </tr>
    <tr>
        <td colspan="2">

The resource reservation <?php echo $target; ?>, initially scheduled from <?php echo $old_start; ?> to <?php echo $old_end; ?>, (location : <?php echo $old_location; ?>),
was updated and will take place from <?php echo $start; ?> to <?php echo $end; ?>, (location : <?php echo $location; ?>).
</td>
    </tr>
    <tr>
        <td style="text-align:right;" colspan="2">
          <a href="<?php echo $host; ?>calendar/calendar_index.php?action=detailconsult&calendar_id=<?php echo $id; ?>">To accept or refuse this update</a>
        </td>
    </tr>
</table>
