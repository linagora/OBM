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
<table style="width:80%; border:1px solid #000; border-collapse:collapse;background:#EFF0F2;font-size:12px;">
    <tr>
        <th style="text-align:center; background-color: #509CBC; color:#FFF; font-size:14px" colspan="2">
          Partage d'agenda
        </th>
    </tr>
    <tr>
        <td colspan="2"><?php echo "$firstname $name"; ?> vous autorise désormais à consulter son agenda OBM.</td>
    </tr>
    <tr>
        <td style="text-align:right;" colspan="2">
          <a href="<?php echo $host; ?>calendar/calendar_render.php?externalToken=<?php echo $token; ?>">Consulter l'agenda</a>
        </td>
    </tr>
</table>
