<?php

class Stato_SendmailTransport implements Stato_IMailTransport
{
    public function send(Stato_Mail $mail)
    {
        $to = $mail->getTo();
        $subject = $mail->getSubject();
        
        $result = mail($to, $subject, $mail->getContent(), $mail->getNonMatchingHeaderLines(array('To', 'Subject')));
        
        if (!$result) throw new Exception('Unable to send mail');
    }
}