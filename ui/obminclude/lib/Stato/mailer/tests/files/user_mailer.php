<?php

class UserMailer extends Stato_Mailer
{
    protected function welcomeMessage($user)
    {
        $mail = new Stato_Mail();
        $mail->addTo($user->mail, $user->name);
        $mail->setText($this->render('welcome.plain', array('username' => $user->name)));
        return $mail;
    }
    
    protected function greetingsMessage($user)
    {
        $mail = new Stato_Mail();
        $mail->addTo($user->mail, $user->name);
        $mail->setHtmlText($this->render('greetings.html', array('username' => $user->name)));
        return $mail;
    }
    
    protected function forgotPasswordMessage($user)
    {
        $mail = new Stato_Mail();
        $mail->addTo($user->mail, $user->name);
        $mail->setHtmlText($this->render('forgot_password.html', array('username' => $user->name)));
        return $mail;
    }
    
    protected function testMessage()
    {
        $mail = new Stato_Mail(new DateTime('2009-02-13 15:47:25', new DateTimeZone('Europe/Paris')));
        $mail->addTo('john.doe@fake.net', 'John Doe');
        $mail->setText('test');
        return $mail;
    }
    
    protected function signupNotification($user)
    {
        $this->recipients = 'john.doe@fake.net';
        $this->date = new DateTime('2009-02-13 15:47:25', new DateTimeZone('Europe/Paris'));
        $this->from = 'notifications@dummysite.com';
        $this->subject = 'Welcome to our site';
        $this->body = array('username' => $user->name);
    }
    
    protected function contactNotification($user)
    {
        $this->recipients = 'john.doe@fake.net';
        $this->date = new DateTime('2009-02-13 15:47:25', new DateTimeZone('Europe/Paris'));
        $this->from = 'notifications@dummysite.com';
        $this->subject = 'Welcome to our site';
        $this->parts[] = array(
            'content' => "BEGIN:VCARD\nEND:VCARD\n", 
            'content_type' => 'text/x-vcard'
        );
        $this->attachments[] = array(
            'content' => file_get_contents(dirname(__FILE__).'/image.png'),
            'filename' => 'hello.png', 'content_type' => 'image/png'
        );
    }
}