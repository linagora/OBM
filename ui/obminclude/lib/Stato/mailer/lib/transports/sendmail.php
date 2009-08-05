<?php

class SSendmailTransport implements SIMailTransport
{
    public function send(SMail $mail)
    {
        $to = $mail->get_to();
        $subject = $mail->get_subject();
        
        $result = mail($to, $subject, $mail->get_content(), $mail->get_non_matching_header_lines(array('To', 'Subject')));
        
        if (!$result) throw new Exception('Unable to send mail');
    }
}