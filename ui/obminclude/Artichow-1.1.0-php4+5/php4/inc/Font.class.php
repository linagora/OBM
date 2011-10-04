<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once dirname(__FILE__)."/../Graph.class.php";

/**
 * Common font characteristics and methods.
 * Declared abstract only so that it can't be instanciated.
 * Users have to call 'new awPHPFont' or 'new awFileFont',
 * or any of their inherited classes (awFont1, awTuffy, awTTFFont, etc.)
 *
 * @package Artichow
 */
 class awFont {

	/**
	 * Build the font
	 *
	 */
	 function awFont() {
		
	}

	/**
	 * Draw a text
	 *
	 * @param $driver
	 * @param $p Draw text at this point
	 * @param &$text The text
	 * @param int $width Text box width
	 */
	 function draw($driver, $point, &$text, $width = NULL) {
		
		$driver->string($this, $text, $point, $width);
		
	}

}

registerClass('Font', TRUE);

/**
 * Class for fonts that cannot be transformed,
 * like the built-in PHP fonts for example.
 * 
 * @package Artichow
 */
class awPHPFont extends awFont {
	
	/**
	 * The used font identifier
	 * 
	 * @var int
	 */
	var $font;
	
	 function awPHPFont($font = NULL) {
		parent::awFont();
		
		if($font !== NULL) {
			$this->font = (int)$font;
		}
	}
	
}

registerClass('PHPFont');

/**
 * Class for fonts that can be transformed (rotated, skewed, etc.),
 * like TTF or FDB fonts for example.
 *
 * @package Artichow
 */
class awFileFont extends awFont {
	
	/**
	 * The name of the font, without the extension
	 *
	 * @var string
	 */
	var $name;
	
	/**
	 * The size of the font
	 *
	 * @var int
	 */
	var $size;
	
	/**
	 * The font filename extension
	 * 
	 * @var string
	 */
	var $extension;
	
	 function awFileFont($name, $size) {
		parent::awFont();
		
		$this->setName($name);
		$this->setSize($size);
	}
	
	/**
	 * Set the name of the font. The $name variable can contain the full path,
	 * or just the filename. Artichow will try to do The Right Thing,
	 * as well as set the extension property correctly if possible.
	 *
	 * @param string $name
	 */
	 function setName($name) {
		$fontInfo = pathinfo((string)$name);
		
		if(strpos($fontInfo['dirname'], '/') !== 0) {
			// Path is not absolute, use ARTICHOW_FONT
			$name = ARTICHOW_FONT.DIRECTORY_SEPARATOR.$fontInfo['basename'];
			$fontInfo = pathinfo($name);
		}
		
		$this->name = $fontInfo['dirname'].DIRECTORY_SEPARATOR.$fontInfo['basename'];
		
		if(array_key_exists('extension', $fontInfo) and $fontInfo['extension'] !== '') {
			$this->setExtension($fontInfo['extension']);
		}
	}
	
	/**
	 * Return the name of the font, i.e. the absolute path and the filename, without the extension.
	 *
	 * @return string
	 */
	 function getName() {
		return $this->name;
	}
	
	/**
	 * Set the size of the font, in pixels
	 *
	 * @param int $size
	 */
	 function setSize($size) {
		$this->size = (int)$size;
	}
	
	/**
	 * Return the size of the font, in pixels
	 *
	 * @return int
	 */
	 function getSize() {
		return $this->size;
	}
	
	/**
	 * Set the extension, without the dot
	 *
	 * @param string $extension
	 */
	 function setExtension($extension) {
		$this->extension = (string)$extension;
	}
	
	/**
	 * Get the filename extension for that font
	 * 
	 * @return string
	 */
	 function getExtension() {
		return $this->extension;
	}

}

registerClass('FileFont');

/**
 * Class representing TTF fonts
 * 
 * @package Artichow
 */
class awTTFFont extends awFileFont {
	
	 function awTTFFont($name, $size) {
		parent::awFileFont($name, $size);
		
		if($this->getExtension() === NULL) {
			$this->setExtension('ttf');
		}
	}

}

registerClass('TTFFont');



$php = '';

for($i = 1; $i <= 5; $i++) {

	$php .= '
	class awFont'.$i.' extends awPHPFont {

		function awFont'.$i.'() {
			parent::awPHPFont('.$i.');
		}

	}
	';

	if(ARTICHOW_PREFIX !== 'aw') {
		$php .= '
		class '.ARTICHOW_PREFIX.'Font'.$i.' extends awFont'.$i.' {
		}
		';
	}

}

eval($php);

$php = '';

foreach($fonts as $font) {

	$php .= '
	class aw'.$font.' extends awFileFont {

		function aw'.$font.'($size) {
			parent::awFileFont(\''.$font.'\', $size);
		}

	}
	';

	if(ARTICHOW_PREFIX !== 'aw') {
		$php .= '
		class '.ARTICHOW_PREFIX.$font.' extends aw'.$font.' {
		}
		';
	}

}

eval($php);



/*
 * Environment modification for GD2 and TTF fonts
 */
if(function_exists('putenv')) {
	putenv('GDFONTPATH='.ARTICHOW_FONT);
}

?>