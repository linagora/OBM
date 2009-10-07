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
 * Draw border
 *
 * @package Artichow
 */
class awBorder {

	/**
	 * Border color
	 *
	 * @var Color
	 */
	var $color;

	/**
	 * Hide border ?
	 *
	 * @var bool
	 */
	var $hide = FALSE;

	/**
	 * Border line style
	 *
	 * @var int
	 */
	var $style;
	
	/**
	 * Build the border
	 *
	 * @param $color Border color
	 * @param int $style Border style
	 */
	 function awBorder($color = NULL, $style = LINE_SOLID) {
	
		$this->setStyle($style);
		
		if(is_a($color, 'awColor')) {
			$this->setColor($color);
		} else {
			$this->setColor(new awBlack);
		}
		
	}
	
	/**
	 * Change border color
	 * This method automatically shows the border if it is hidden
	 *
	 * @param $color
	 */
	 function setColor($color) {
		$this->color = $color;
		$this->show();
	}
	
	/**
	 * Change border style
	 *
	 * @param int $style
	 */
	 function setStyle($style) {
		$this->style = (int)$style;
	}
	
	/**
	 * Hide border ?
	 *
	 * @param bool $hide
	 */
	 function hide($hide = TRUE) {
		$this->hide = (bool)$hide;
	}
	
	/**
	 * Show border ?
	 *
	 * @param bool $show
	 */
	 function show($show = TRUE) {
		$this->hide = (bool)!$show;
	}
	
	/**
	 * Is the border visible ?
	 *
	 * @return bool
	 */
	 function visible() {
		return !$this->hide;
	}
	
	/**
	 * Draw border as a rectangle
	 *
	 * @param $driver
	 * @param $p1 Top-left corner
	 * @param $p2 Bottom-right corner
	 */
	 function rectangle($driver, $p1, $p2) {
	
		// Border is hidden
		if($this->hide) {
			return;
		}
	
		$line = new awLine;
		$line->setStyle($this->style);
		$line->setLocation($p1, $p2);
		
		$driver->rectangle($this->color, $line);
		
	}
	
	/**
	 * Draw border as an ellipse
	 *
	 * @param $driver
	 * @param $center Ellipse center
	 * @param int $width Ellipse width
	 * @param int $height Ellipse height
	 */
	 function ellipse($driver, $center, $width, $height) {
	
		// Border is hidden
		if($this->hide) {
			return;
		}
		
		switch($this->style) {
		
			case LINE_SOLID :
				$driver->ellipse($this->color, $center, $width, $height);
				break;
			
			default :
				awImage::drawError("Class Border: Dashed and dotted borders and not yet implemented on ellipses.");
				break;
		
		}
		
		
	}
	
	/**
	 * Draw border as a polygon
	 * 
	 * @param $driver A Driver object
	 * @param &$polygon A Polygon object
	 */
	 function polygon($driver, &$polygon) {
		
		// Border is hidden
		if($this->hide) {
			return;
		}
		
		$polygon->setStyle($this->style);
		$driver->polygon($this->color, $polygon);
		
		// In case of LINE_SOLID, Driver::polygon() uses imagepolygon()
		// which automatically closes the shape. In any other case,
		// we have to do it manually here.
		if($this->style !== LINE_SOLID) {
			$this->closePolygon($driver, $polygon);
		}
	}
	
	/**
	 * Draws the last line of a Polygon, between the first and last point
	 * 
	 * @param $driver A Driver object
	 * @param &$polygon The polygon object to close
	 */
	 function closePolygon($driver, &$polygon) {
		$first = $polygon->get(0);
		$last  = $polygon->get($polygon->count() - 1);
		
		$line = new awLine($first, $last, $this->style, $polygon->getThickness());
		$driver->line($this->color, $line);
	}
	
}

registerClass('Border');
?>