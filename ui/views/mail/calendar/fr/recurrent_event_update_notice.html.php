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


?>
<table style="width:80%; border:1px solid #555; border-collapse:collapse; background:#FAFAFA; font-size:13px; margin:0 auto;">
    <tr>
        <th style="text-align:center; background-color: #368599; color:#FFF; font-size:15px;height:25px;" colspan="2">
          Invitation à un évènement récurrent : mise à jour
        </th>
    </tr>
    <tr style="height:25px;">
        <td colspan="2">
Le rendez-vous <?php echo $title; ?>, initialement prévu du <?php echo $old_startDate; ?> au <?php echo $old_endDate; ?> de <?php echo $old_startTime ?> à <?php echo $old_endTime ?> (lieu : <?php echo $old_location; ?>),
a été modifié : </td>
    </tr>
    <tr>
        <td style="text-align:right;width:20%;padding-right:1em;">Sujet</td><td style="font-weight:bold;"><?php echo $title; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Du</td><td style="font-weight:bold;"><?php echo $startDate; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Au</td><td style="font-weight:bold;"><?php echo $endDate; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Heure</td><td style="font-weight:bold;"><?php echo $startTime." - ".$endTime ; ?></td>
    </tr>    
    <tr>
        <td style="text-align:right;padding-right:1em;">Type de récurrence</td><td style="font-weight:bold;"><?php echo $repeat_kind; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Lieu</td><td style="font-weight:bold;"><?php echo $location; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Organisateur</td><td style="font-weight:bold;"><?php echo $organizer; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Créé par</td><td style="font-weight:bold;"><?php echo $creator; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;">Participant(s)</td><td style="font-weight:bold;"><?php echo $attendees; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;padding-right:1em;"></td><td style="font-weight:bold;"><?php echo $attendees; ?></td>
    </tr>
    <?php
    if ( $exceptions['changed'] != null){
    ?>
    <tr>
        <td style="text-align:right;padding-right:1em;vertical-align:top;font-weight:bold;color:#003300;">Occurrences Modifiées</td><td style="font-weight:bold;">
    <?php
        foreach ($exceptions['changed'] as $exception ) {
            $url = ' <a href="'.$host.'calendar/calendar_index.php?action=detailconsult&calendar_id='.$exception['id'].'">Détails</a><br/>';
            if( $exception['location'] != ''){
                echo $exception['date'].' (Nouveau lieu: '.$exception['location'].')'.$url;
            } else {
                echo $exception['date'].$url;
            }
        }
    ?>
        </td>
    </tr>
    <?php
    }
    if ($exceptions['removed'] != null){
    ?>
    <tr>
        <td style="text-align:right;padding-right:1em;vertical-align:top;font-weight:bold;color:#550000;">Occurrences Annulées</td><td style="font-weight:bold;">
    <?php
        foreach ($exceptions['removed'] as $key => $date) {
            echo $date.'</br>';
        }
    ?>
        </td>
    </tr>
    <?php
    }
    ?>
    <tr>
        <td colspan="2">
          <strong>NB : </strong>Si vous êtes utilisateur du connecteur Thunderbird ou de la synchronisation ActiveSync, vous devez synchroniser pour visualiser ce nouveau rendez-vous. 
        </td>
    </tr>
    <tr>
        <td style="text-align:right;" colspan="2">
          <a href="<?php echo $host; ?>calendar/calendar_index.php?action=detailconsult&calendar_id=<?php echo $id; ?>">Plus de détails</a>
        </td>
    </tr>
</table>
