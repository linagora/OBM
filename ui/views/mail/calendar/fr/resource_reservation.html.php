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
          Réservation de ressource
        </th>
    </tr>
    <tr>
      <td colspan="2">Une ressource dont vous êtes responsable à été réservée</td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em; width:20%;">Sujet</td><td><?php echo $title; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Du</td><td><?php echo $start; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Au</td><td><?php echo $end; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Lieu</td><td><?php echo $location; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Organisateur</td><td><?php echo $auteur; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;" colspan="2">
          <a href="<?php echo $host; ?>calendar/calendar_index.php?action=detailconsult&calendar_id=<?php echo $id; ?>">Accepter/refuser la réservation</a>
        </td>
    </tr>
</table>
