Message automatique envoyé par OBM
------------------------------------------------------------------
NOUVEAU RENDEZ-VOUS !
------------------------------------------------------------------

Vous êtes invité à participer à ce rendez-vous

du     : <?php echo $start; ?>

au     : <?php echo $end; ?>

sujet  : <?php echo $title; ?>

lieu   : <?php echo $location; ?>

auteur : <?php echo $auteur; ?>


:: Pour accepter : 
<?php echo $this->host; ?>calendar/calendar_index.php?action=update_decision&calendar_id=<?php echo $id; ?>&entity_kind=user&rd_decision_event=ACCEPTED

:: Pour refuser : 
<?php echo $this->host; ?>calendar/calendar_index.php?action=update_decision&calendar_id=<?php echo $id; ?>&entity_kind=user&rd_decision_event=DECLINED

:: Pour plus de détails : 
<?php echo $this->host; ?>calendar/calendar_index.php?action=detailconsult&calendar_id=<?php echo $id; ?>
