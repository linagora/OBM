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
          Resource state updated
        </th>
    </tr>
    <tr>
      <td colspan="2">
<?php echo $target; ?> owner has changed its state to <?php echo $targetState; ?> for the appointment <?php echo $title; ?> scheduled on <?php echo $start; ?>.
      </td>
    </tr>

</table>
