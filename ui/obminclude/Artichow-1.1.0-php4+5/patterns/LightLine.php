<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once ARTICHOW."/LinePlot.class.php";

class LightLinePattern extends Pattern {

	 function create() {
		
		$legend = $this->getArg('legend');
		
		$y = $this->getArg('y');
		
		if($y === NULL) {
			awImage::drawError("Class LightLinePattern: Argument 'y' must not be NULL.");
		}
		
		$plot = new LinePlot($y);
		$plot->setSize(0.7, 1);
		$plot->setCenter(0.35, 0.5);
		$plot->setPadding(35, 15, 35, 30);
		$plot->setColor(new Orange());
		$plot->setFillColor(new LightOrange(80));
		
		$plot->grid->setType(LINE_DASHED);
		
		$plot->mark->setType(MARK_CIRCLE);
		$plot->mark->setFill(new MidRed);
		$plot->mark->setSize(6);
		
		$plot->legend->setPosition(1, 0.5);
		$plot->legend->setAlign(LEGEND_LEFT);
		$plot->legend->shadow->smooth(TRUE);
		
		if($legend !== NULL) {
			$plot->legend->add($plot, $legend, LEGEND_MARK);
		}
		
		return $plot;

	}

}
?>