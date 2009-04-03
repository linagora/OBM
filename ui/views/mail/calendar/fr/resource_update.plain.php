Message automatique envoyé par OBM
------------------------------------------------------------------
MODIFICATION D'UNE RESERVATION DE RESSOURCE !
------------------------------------------------------------------

Le rendez-vous <?php echo $title; ?>, initialement prévu du <?php echo $old_start; ?> au <?php echo $old_end; ?>, (lieu : <?php echo $old_location; ?>),
a été modifié et se déroulera du <?php echo $start; ?> au <?php echo $end; ?>, (lieu : <?php echo $location; ?>).

:: Pour plus de détails : 
<?php echo $this->host; ?>/calendar/calendar_index.php?action=detailconsult&calendar_id=<?php echo $id; ?>


:: Pour accepter les modifications :
<?php echo $this->host; ?>/calendar/calendar_index.php?action=update_decision&calendar_id=<?php echo $id; ?>&entity_kind=user&rd_decision_event=ACCEPTED

:: Pour refuser les modifications : 
<?php echo $this->host; ?>/calendar/calendar_index.php?action=update_decision&calendar_id=<?php echo $id; ?>&entity_kind=user&rd_decision_event=DECLINED
