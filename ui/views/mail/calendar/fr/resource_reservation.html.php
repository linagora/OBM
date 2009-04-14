<table style="width:100%; border:3px solid #000;">
    <tr>
        <th style="text-align:left; background-color: blue; color:#fff; font-size:16px" colspan="2">
          Réservation de ressource
        </th>
    </tr>
    <tr>
      <td colspan="2">Une ressource dont vous êtes responsable à été réservée</td>
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
          <a href="<?php echo $host; ?>calendar/calendar_index.php?action=detailconsult&calendar_id=<?php echo $id; ?>">Accepter/refuser la réservation</a>
        </td>
    </tr>
</table>
