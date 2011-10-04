<?php

require_once dirname(__FILE__) . '/../../test/TestsHelper.php';

require_once 'i18n.php';
require_once 'backend/abstract.php';
require_once 'backend/simple.php';

class SI18nTest extends PHPUnit_Framework_TestCase
{
    public function setup()
    {
        SI18n::add_data_path(dirname(__FILE__).'/data/simple');
        SI18n::set_locale('fr');
    }
    
    public function test_should_default_to_simple_backend()
    {
        $this->assertEquals('SSimpleBackend', get_class(SI18n::get_backend()));
    }
    
    public function test_default_locale()
    {
        $this->assertEquals('en', SI18n::get_default_locale());
    }
    
    public function test_translate()
    {
        $this->assertEquals('Stato est un cadre de travail PHP5.', 
            SI18n::translate('Stato is a PHP5 framework.'));
        $this->assertEquals('Stato est un cadre de travail PHP5.', 
            __('Stato is a PHP5 framework.'));
    }
    
    public function test_translate_and_interpolate()
    {
        $this->assertEquals("La date d'aujourd'hui est 31/07/2007", 
            SI18n::translate("Today's date is %date%", array('date' => '31/07/2007')));
        $this->assertEquals("La date d'aujourd'hui est 31/07/2007", 
            __("Today's date is %date%", array('date' => '31/07/2007')));
    }
    
    public function test_translatef()
    {
        $this->assertEquals('Le champ IP est requis.', 
            SI18n::translatef('%s is required.', array('IP')));
        $this->assertEquals('Le champ IP est requis.', 
            _f('%s is required.', array('IP')));
    }
    
    public function test_translate_and_pluralize()
    {
        $this->assertEquals('2 messages', SI18n::translate_and_pluralize('inbox', 2));
        $this->assertEquals('2 messages', _p('inbox', 2));
    }
}