<table style="width:100%; border:3px solid #000;">
    <tr>
        <th style="text-align:left; background-color: blue; color:#fff; font-size:16px" colspan="2">
          Annulation d'une réservation 
        </th>
    </tr>
    <tr>
        <td colspan="2">La réservation suivante à été annulée:</td>
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
</table>
