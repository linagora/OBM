<?php

class Stato_DummyTransport implements Stato_IMailTransport
{
    public function send(Stato_Mail $mail)
    {
        return $mail->__toString();
    }
}