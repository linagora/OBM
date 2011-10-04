<?php

require_once dirname(__FILE__) . '/../../test/TestsHelper.php';

require_once 'i18n.php';
require_once 'backend/abstract.php';
require_once 'backend/simple.php';

class SSimpleBackendTest extends PHPUnit_Framework_TestCase
{
    public function setup()
    {
        $this->backend = new SSimpleBackend(dirname(__FILE__).'/data/simple');
    }
    
    public function teardown()
    {
        @unlink(dirname(__FILE__) . '/tmp/klingon.php');
        @unlink(dirname(__FILE__) . '/tmp/fr.php');
    }
    
    public function test_translate()
    {
        $this->assertEquals('Stato est un cadre de travail PHP5.', 
            $this->backend->translate('fr', 'Stato is a PHP5 framework.'));
    }
    
    public function test_translate_and_interpolate()
    {
        $this->assertEquals("La date d'aujourd'hui est 31/07/2007", 
            $this->backend->translate('fr', "Today's date is %date%", array('%date%' => '31/07/2007')));
        $this->assertEquals("La date d'aujourd'hui est 31/07/2007", 
            $this->backend->translate('fr', "Today's date is %date%", array('date' => '31/07/2007')));
    }
    
    public function test_translatef()
    {
        $this->assertEquals('Le champ IP est requis.', 
            $this->backend->translatef('fr', '%s is required.', array('IP')));
    }
    
    public function test_translate_and_pluralize()
    {
        $this->assertEquals('pas de message', $this->backend->translate_and_pluralize('fr', 'inbox', 0));
        $this->assertEquals('1 message', $this->backend->translate_and_pluralize('fr', 'inbox', 1));
        $this->assertEquals('2 messages', $this->backend->translate_and_pluralize('fr', 'inbox', 2));
        $this->assertEquals('3 messages', $this->backend->translate_and_pluralize('fr', 'inbox', 3));
    }
    
    public function test_translate_without_translation()
    {
        $this->assertEquals('hello world', $this->backend->translate('fr', 'hello world'));
    }
    
    public function test_store()
    {
        $this->assertEquals('hello world', $this->backend->translate('fr', 'hello world'));
        $this->backend->store('fr', 'hello world', 'bonjour le monde');
        $this->assertEquals('bonjour le monde', $this->backend->translate('fr', 'hello world'));
    }
    
    public function test_save()
    {
        $php = <<<EOT
<?php

return array(
    'The Klingon culture is a very ancient one, though there is no record of its roots.' => 'tIQqu\' tlhIngan Segh tIgh je, \'ach mungDaj qonlu\'be\'.',
);
EOT;
        $this->backend->store('klingon', 'The Klingon culture is a very ancient one, though there is no record of its roots.', 
                                         'tIQqu\' tlhIngan Segh tIgh je, \'ach mungDaj qonlu\'be\'.');
        $this->backend->save('klingon', dirname(__FILE__) . '/tmp');
        $this->assertEquals($php, file_get_contents(dirname(__FILE__) . '/tmp/klingon.php'));
    }
    
    public function test_save_with_existent_translations()
    {
        $php = <<<EOT
<?php

return array(
    'Stato is a PHP5 framework.' => 'Stato est un cadre de travail PHP5.',
    '%s is required.' => 'Le champ %s est requis.',
    'Today\'s date is %date%' => 'La date d\'aujourd\'hui est %date%',
    'inbox' => array(
        'zero' => 'pas de message',
        '1 message',
        '%d messages',
    ),
    'hello world' => 'bonjour le monde',
);
EOT;
        $this->backend->store('fr', 'hello world', 'bonjour le monde', 'foo_controller.php:10');
        $this->backend->save('fr', dirname(__FILE__) . '/tmp');
        $this->assertEquals($php, file_get_contents(dirname(__FILE__) . '/tmp/fr.php'));
    }
}