<?php

class UserMailer extends SMailer
{
    protected function welcome_message($user)
    {
        $mail = new SMail();
        $mail->add_to($user->mail, $user->name);
        $mail->set_text($this->render('welcome.plain', array('username' => $user->name)));
        return $mail;
    }
    
    protected function welcomeMessage($user)
    {
        $mail = new SMail();
        $mail->add_to($user->mail, $user->name);
        $mail->set_text($this->render('welcome.plain', array('username' => $user->name)));
        return $mail;
    }
    
    protected function greetings_message($user)
    {
        $mail = new SMail();
        $mail->add_to($user->mail, $user->name);
        $mail->set_html_text($this->render('greetings.html', array('username' => $user->name)));
        return $mail;
    }
    
    protected function forgot_password_message($user)
    {
        $mail = new SMail();
        $mail->add_to($user->mail, $user->name);
        $mail->set_html_text($this->render('forgot_password.html', array('username' => $user->name)));
        return $mail;
    }
    
    protected function test_message()
    {
        $mail = new SMail(new DateTime('2009-02-13 15:47:25', new DateTimeZone('Europe/Paris')));
        $mail->add_to('john.doe@fake.net', 'John Doe');
        $mail->set_text('test');
        return $mail;
    }
    
    protected function signup_notification($user)
    {
        $this->recipients = 'john.doe@fake.net';
        $this->date = new DateTime('2009-02-13 15:47:25', new DateTimeZone('Europe/Paris'));
        $this->from = 'notifications@dummysite.com';
        $this->subject = 'Welcome to our site';
        $this->body = array('username' => $user->name);
    }
    
    protected function contact_notification($user)
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