<?php

require_once dirname(__FILE__) . '/../../tests/TestsHelper.php';

require_once 'i18n.php';
require_once 'backend/abstract.php';
require_once 'backend/simple.php';

class Stato_I18nTest extends PHPUnit_Framework_TestCase
{
    public function setup()
    {
        Stato_I18n::addDataPath(dirname(__FILE__).'/data/simple');
        Stato_I18n::setLocale('fr');
    }
    
    public function testShouldDefaultToSimpleBackend()
    {
        $this->assertEquals('Stato_I18n_SimpleBackend', get_class(Stato_I18n::getBackend()));
    }
    
    public function testDefaultLocale()
    {
        $this->assertEquals('en', Stato_I18n::getDefaultLocale());
    }
    
    public function testTranslate()
    {
        $this->assertEquals('Stato est un cadre de travail PHP5.', 
            Stato_I18n::translate('Stato is a PHP5 framework.'));
    }
    
    public function testTranslateInterpolation()
    {
        $this->assertEquals("La date d'aujourd'hui est 31/07/2007", 
            Stato_I18n::translate("Today's date is %date%", array('%date%' => '31/07/2007')));
    }
    
    public function testPluralization()
    {
        $this->assertEquals('pas de message', Stato_I18n::translate('inbox', array('count' => 0)));
        $this->assertEquals('1 message', Stato_I18n::translate('inbox', array('count' => 1)));
        $this->assertEquals('2 messages', Stato_I18n::translate('inbox', array('count' => 2)));
        $this->assertEquals('3 messages', Stato_I18n::translate('inbox', array('count' => 3)));
    }
}