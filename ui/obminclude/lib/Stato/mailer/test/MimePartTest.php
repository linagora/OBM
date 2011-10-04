<?php

require_once dirname(__FILE__) . '/../../test/TestsHelper.php';

require_once 'mime/mime.php';
require_once 'mime/entity.php';
require_once 'mime/part.php';

class SMimePartTest extends PHPUnit_Framework_TestCase
{
    public function test_text_plain_part()
    {
        $part = <<<EOT
Content-Type: text/plain; charset="UTF-8"
Content-Transfer-Encoding: 8bit

test
EOT;
        $this->assertEquals($part, (string) new SMimePart('test'));
    }
    
    public function test_set_content_type()
    {
        
    }
}