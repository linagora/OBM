<?php

class SDummyTransport implements SIMailTransport
{
    public function send(SMail $mail)
    {
        return $mail->__toString();
    }
}