<table style="width:100%; border:3px solid #000;">
    <tr>
        <th style="text-align:left; background-color: blue; color:#fff; font-size:16px" colspan="2">
          New resource reservation
        </th>
    </tr>
    <tr>
      <td colspan="2">
A new resource reservation was scheduled
</td>
    </tr>
    <tr>
        <td style="text-align:right; width:20%">Subject</td><td><?php echo $title; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;">From</td><td><?php echo $start; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;">To</td><td><?php echo $end; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;">Location</td><td><?php echo $location; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;">Author</td><td><?php echo $auteur; ?></td>
    </tr>
    <tr>
        <td style="text-align:right;" colspan="2">
          <a href="<?php echo $host; ?>calendar/calendar_index.php?action=detailconsult&calendar_id=<?php echo $id; ?>">To accept or refuse this reservation</a>
        </td>
    </tr>
</table>
