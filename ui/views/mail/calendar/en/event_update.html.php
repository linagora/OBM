<table style="width:80%; border:1px solid #000; border-collapse:collapse;background:#EFF0F2;font-size:12px;">
    <tr>
        <th style="text-align:center; background-color: #509CBC; color:#FFF; font-size:14px" colspan="2">
          Appointment Updated
        </th>
    </tr>
    <tr>
        <td colspan="2">
The appointment <?php echo $title; ?>, initially scheduled from <?php echo $old_start; ?> to <?php echo $old_end; ?>, (location : <?php echo $old_location; ?>),
was updated and will take place from <?php echo $start; ?> to <?php echo $end; ?>, (location : <?php echo $location; ?>). </td>
    </tr>
    <tr>
        <td style="text-align:right;" colspan="2">
          <a href="<?php echo $host; ?>calendar/calendar_index.php?action=update_decision&calendar_id=<?php echo $id; ?>&entity_kind=user&rd_decision_event=ACCEPTED">Accept</a>
          <a href="<?php echo $host; ?>calendar/calendar_index.php?action=update_decision&calendar_id=<?php echo $id; ?>&entity_kind=user&rd_decision_event=DECLINED">Decline</a>
          <a href="<?php echo $host; ?>calendar/calendar_index.php?action=detailconsult&calendar_id=<?php echo $id; ?>">More informations</a>
        </td>
    </tr>
</table>
