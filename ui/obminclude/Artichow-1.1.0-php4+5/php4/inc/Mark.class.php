<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */
 
require_once dirname(__FILE__)."/../Graph.class.php";

/* <php4> */

define("MARK_CIRCLE", 1);
define("MARK_SQUARE", 2);
define("MARK_TRIANGLE", 3);
define("MARK_INVERTED_TRIANGLE", 4);
define("MARK_RHOMBUS", 5);
define("MARK_CROSS", 6);
define("MARK_PLUS", 7);
define("MARK_IMAGE", 8);
define("MARK_STAR", 9);
define("MARK_PAPERCLIP", 10);
define("MARK_BOOK", 11);

/* </php4> */
 
/**
 * Draw marks
 *
 * @package Artichow
 */
class awMark {

	/**
	 * Circle mark
	 *
	 * @var int
	 */
	

	/**
	 * Square mark
	 *
	 * @var int
	 */
	

	/**
	 * Triangle mark
	 * 
	 * @var int
	 */
	
	
	/**
	 * Inverted triangle mark
	 * 
	 * @var int
	 */
	

	/**
	 * Rhombus mark
	 * 
	 * @var int
	 */
	

	/**
	 * Cross (X) mark
	 * 
	 * @var int
	 */
	

	/**
	 * Plus mark
	 * 
	 * @var int
	 */
	

	/**
	 * Image mark
	 *
	 * @var int
	 */
	

	/**
	 * Star mark
	 *
	 * @var int
	 */
	

	/**
	 * Paperclip mark
	 *
	 * @var int
	 */
	

	/**
	 * Book mark
	 *
	 * @var int
	 */
	

	/**
	 * Must marks be hidden ?
	 *
	 * @var bool
	 */
	var $hide;

	/**
	 * Mark type
	 *
	 * @var int
	 */
	var $type;

	/**
	 * Mark size
	 *
	 * @var int
	 */
	var $size = 8;

	/**
	 * Fill mark
	 *
	 * @var Color, Gradient
	 */
	var $fill;

	/**
	 * Mark image
	 *
	 * @var Image
	 */
	var $image;

	/**
	 * To draw marks
	 *
	 * @var Driver
	 */
	var $driver;

	/**
	 * Move position from this vector
	 *
	 * @var Point
	 */
	var $move;
	
	/**
	 * Marks border
	 *
	 * @var Border
	 */
	var $border;

	/**
	 * Build the mark
	 */
	 function awMark() {
		
		$this->fill = new awColor(255, 0, 0, 0);
		$this->border = new awBorder;
		$this->border->hide();
		
		$this->move = new awPoint(0, 0);
	
	}
	
	/**
	 * Change mark position
	 *
	 * @param int $x Add this interval to X coord
	 * @param int $y Add this interval to Y coord
	 */
	 function move($x, $y) {
	
		$this->move = $this->move->move($x, $y);
	
	}
	
	/**
	 * Hide marks ?
	 *
	 * @param bool $hide TRUE to hide marks, FALSE otherwise
	 */
	 function hide($hide = TRUE) {
		$this->hide = (bool)$hide;
	}
	
	/**
	 * Show marks ?
	 *
	 * @param bool $show
	 */
	 function show($show = TRUE) {
		$this->hide = (bool)!$show;
	}
	
	/**
	 * Change mark type
	 *
	 * @param int $size Size in pixels
	 */
	 function setSize($size) {
		$this->size = (int)$size;
	}
	
	/**
	 * Change mark type
	 *
	 * @param int $type New mark type
	 * @param int $size Mark size (can be NULL)
	 */
	 function setType($type, $size = NULL) {
		$this->type = (int)$type;
		if($size !== NULL) {
			$this->setSize($size);
		}
	}
	
	/**
	 * Fill the mark with a color or a gradient
	 *
	 * @param mixed $fill A color or a gradient
	 */
	 function setFill($fill) {
		if(is_a($fill, 'awColor') or is_a($fill, 'awGradient')) {
			$this->fill = $fill;
		}
	}
	
	/**
	 * Set an image
	 * Only for MARK_IMAGE type.
	 *
	 * @param Image An image
	 */
	 function setImage(&$image) {
		$this->image = $image;
	}
	
	/**
	 * Draw the mark
	 *
	 * @param $driver
	 * @param $point Mark center
	 */
	 function draw($driver, $point) {
	
		// Hide marks ?
		if($this->hide) {
			return;
		}
	
		// Check if we can print marks
		if($this->type !== NULL) {
		
			$this->driver = $driver;
			$realPoint = $this->move->move($point->x, $point->y);
		
			switch($this->type) {
			
				case MARK_CIRCLE :
					$this->drawCircle($realPoint);
					break;
			
				case MARK_SQUARE :
					$this->drawSquare($realPoint);
					break;
				
				case MARK_TRIANGLE :
					$this->drawTriangle($realPoint);
					break;

				case MARK_INVERTED_TRIANGLE :
					$this->drawTriangle($realPoint, TRUE);
					break;
				
				case MARK_RHOMBUS :
					$this->drawRhombus($realPoint);
					break;

				case MARK_CROSS :
					$this->drawCross($realPoint);
					break;
					
				case MARK_PLUS :
					$this->drawCross($realPoint, TRUE);
					break;
			
				case MARK_IMAGE :
					$this->drawImage($realPoint);
					break;
					
				case MARK_STAR :
					$this->changeType('star');
					$this->draw($driver, $point);
					break;
					
				case MARK_PAPERCLIP :
					$this->changeType('paperclip');
					$this->draw($driver, $point);
					break;
					
				case MARK_BOOK :
					$this->changeType('book');
					$this->draw($driver, $point);
					break;
					
			}
		
		}
	
	}
	
	 function changeType($image) {
		$this->setType(MARK_IMAGE);
		$this->setImage(new awFileImage(ARTICHOW_IMAGE.DIRECTORY_SEPARATOR.$image.'.png'));
	}
	
	 function drawCircle($point) {
		
		$this->driver->filledEllipse(
			$this->fill,
			$point,
			$this->size, $this->size
		);
	
		$this->border->ellipse(
			$this->driver,
			$point,
			$this->size, $this->size
		);
	
	}
	
	 function drawSquare($point) {
	
		list($x, $y) = $point->getLocation();
	
		$x1 = (int)($x - $this->size / 2);
		$x2 = $x1 + $this->size;
		$y1 = (int)($y - $this->size / 2);
		$y2 = $y1 + $this->size;
		
		$this->border->rectangle($this->driver, new awPoint($x1, $y1), new awPoint($x2, $y2));
		
		$size = $this->border->visible() ? 1 : 0;
		
		$this->driver->filledRectangle(
			$this->fill,
			new awLine(
				new awPoint($x1 + $size, $y1 + $size),
				new awPoint($x2 - $size, $y2 - $size)
			)
		);
	
	}
	
	 function drawTriangle($point, $inverted = FALSE) {
		
		list($x, $y) = $point->getLocation();
		
		$size = $this->size;
		
		$triangle = new awPolygon;
		// Set default style and thickness
		$triangle->setStyle(POLYGON_SOLID);
		$triangle->setThickness(1);
		
		if($inverted === TRUE) {
			// Bottom of the triangle
			$triangle->append(new awPoint($x, $y + $size / sqrt(3)));
		
			// Upper left corner
			$triangle->append(new awPoint($x - $size / 2, $y - $size / (2 * sqrt(3))));

			// Upper right corner
			$triangle->append(new awPoint($x + $size / 2, $y - $size / (2 * sqrt(3))));
		} else {
			// Top of the triangle
			$triangle->append(new awPoint($x, $y - $size / sqrt(3)));
			
			// Lower left corner
			$triangle->append(new awPoint($x - $size / 2, $y + $size / (2 * sqrt(3))));
	
			// Lower right corner
			$triangle->append(new awPoint($x + $size / 2, $y + $size / (2 * sqrt(3))));
		}

		$this->driver->filledPolygon($this->fill, $triangle);
		
		if($this->border->visible()) {			
			$this->border->polygon($this->driver, $triangle);
		}
	}
	
	 function drawRhombus($point) {
	
		list($x, $y) = $point->getLocation();

		$rhombus = new awPolygon;
		// Set default style and thickness
		$rhombus->setStyle(POLYGON_SOLID);
		$rhombus->setThickness(1);
		
		// Top of the rhombus
		$rhombus->append(new awPoint($x, $y - $this->size / 2));
		
		// Right of the rhombus
		$rhombus->append(new awPoint($x + $this->size / 2, $y));
		
		// Bottom of the rhombus
		$rhombus->append(new awPoint($x, $y + $this->size / 2));
		
		// Left of the rhombus
		$rhombus->append(new awPoint($x - $this->size / 2, $y));
		
		$this->driver->filledPolygon($this->fill, $rhombus);
		
		if($this->border->visible()) {			
			$this->border->polygon($this->driver, $rhombus);
		}
	}
	
	 function drawCross($point, $upright = FALSE) {
	
		list($x, $y) = $point->getLocation();

		if($upright === TRUE) {
			$x11 = (int)($x);
			$y11 = (int)($y - $this->size / 2);
			$x12 = (int)($x);
			$y12 = (int)($y + $this->size / 2);
	
			$y21 = (int)($y);
			$y22 = (int)($y);
		} else {
			$x11 = (int)($x - $this->size / 2);
			$y11 = (int)($y + $this->size / 2);
			$x12 = (int)($x + $this->size / 2);
			$y12 = (int)($y - $this->size / 2);

			$y21 = (int)($y - $this->size / 2);
			$y22 = (int)($y + $this->size / 2);
		}
			
		$x21 = (int)($x - $this->size / 2);
		$x22 = (int)($x + $this->size / 2);
		
		$this->driver->line(
			$this->fill,
			new awLine(
				new awPoint($x11, $y11),
				new awPoint($x12, $y12)
			)
		);
		
		$this->driver->line(
			$this->fill,
			new awLine(
				new awPoint($x21, $y21),
				new awPoint($x22, $y22)
			)
		);
	}

	 function drawImage($point) {
		
		if(is_a($this->image, 'awImage')) {
		
			$width = $this->image->width;
			$height = $this->image->height;
	
			list($x, $y) = $point->getLocation();
		
			$x1 = (int)($x - $width / 2);
			$x2 = $x1 + $width;
			$y1 = (int)($y - $width / 2);
			$y2 = $y1 + $height;
		
			$this->border->rectangle($this->driver, new awPoint($x1 - 1, $y1 - 1), new awPoint($x2 + 1, $y2 + 1));
			
			$this->driver->copyImage($this->image, new awPoint($x1, $y1), new awPoint($x2, $y2));
			
		}
	
	}

}

registerClass('Mark');
?>