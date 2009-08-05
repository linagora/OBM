<?php

require_once dirname(__FILE__) . '/../../test/TestsHelper.php';

require_once 'mime/mime.php';
require_once 'mime/entity.php';

class SMimeEntityTest extends PHPUnit_Framework_TestCase
{
    public function setup()
    {
        $this->entity = new SMimeEntity();
        $this->entity->add_header('To', 'john@doe.net');
        $this->entity->add_header('To', 'jane@doe.net');
        $this->entity->add_header('From', 'root@dummy.net');
        $this->entity->add_header('Subject', 'test');
    }
    
    public function test_get_all_header_lines()
    {
        $this->assertEquals("To: john@doe.net, jane@doe.net\nFrom: root@dummy.net\nSubject: test",
                            $this->entity->get_all_header_lines());
    }
    
    public function test_get_matching_header_lines()
    {
        $this->assertEquals("To: john@doe.net, jane@doe.net\nSubject: test",
                            $this->entity->get_matching_header_lines(array('To', 'Subject')));
    }
    
    public function test_get_non_matching_header_lines()
    {
        $this->assertEquals("To: john@doe.net, jane@doe.net\nSubject: test",
                            $this->entity->get_non_matching_header_lines(array('From')));
    }
}