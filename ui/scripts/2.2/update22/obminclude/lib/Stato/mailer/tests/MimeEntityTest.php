<?php

require_once dirname(__FILE__) . '/../../tests/TestsHelper.php';

require_once 'mime/mime.php';
require_once 'mime/entity.php';

class Stato_MimeEntityTest extends PHPUnit_Framework_TestCase
{
    public function setup()
    {
        $this->entity = new Stato_MimeEntity();
        $this->entity->addHeader('To', 'john@doe.net');
        $this->entity->addHeader('To', 'jane@doe.net');
        $this->entity->addHeader('From', 'root@dummy.net');
        $this->entity->addHeader('Subject', 'test');
    }
    
    public function testGetAllHeaderLines()
    {
        $this->assertEquals("To: john@doe.net, jane@doe.net\nFrom: root@dummy.net\nSubject: test",
                            $this->entity->getAllHeaderLines());
    }
    
    public function testGetMatchingHeaderLines()
    {
        $this->assertEquals("To: john@doe.net, jane@doe.net\nSubject: test",
                            $this->entity->getMatchingHeaderLines(array('To', 'Subject')));
    }
    
    public function testGetNonMatchingHeaderLines()
    {
        $this->assertEquals("To: john@doe.net, jane@doe.net\nSubject: test",
                            $this->entity->getNonMatchingHeaderLines(array('From')));
    }
}