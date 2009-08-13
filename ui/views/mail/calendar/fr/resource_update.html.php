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
          Invitation à un évènement : mise à jour
        </th>
    </tr>
    <tr>
        <td colspan="2">Le rendez-vous <strong><?php echo $title; ?></strong>, initialement prévu du <?php echo $old_start; ?> au <?php echo $old_end; ?>, (lieu : <?php echo $old_location; ?>),
a été modifié :</td>
    </tr>
    <tr>
        <td style="text-align:right; width:20%">Sujet</td><td><?php echo $title; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;">Du</td><td><?php echo $start; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;">Au</td><td><?php echo $end; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;">Lieu</td><td><?php echo $location; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;">Organisateur</td><td><?php echo $auteur; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;" colspan="2">
          <a href="<?php echo $host; ?>calendar/calendar_index.php?action=update_decision&calendar_id=<?php echo $id; ?>&entity_kind=user&rd_decision_event=ACCEPTED">Accepter</a>
          <a href="<?php echo $host; ?>calendar/calendar_index.php?action=update_decision&calendar_id=<?php echo $id; ?>&entity_kind=user&rd_decision_event=DECLINED">Refuser</a>
          <a href="<?php echo $host; ?>calendar/calendar_index.php?action=detailconsult&calendar_id=<?php echo $id; ?>">Consulter l'agenda</a>
        </td>
    </tr>
</table>
