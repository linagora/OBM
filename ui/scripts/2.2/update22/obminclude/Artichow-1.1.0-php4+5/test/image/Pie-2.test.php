<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../../Pie.class.php";


$graph = new Graph(400, 300);
$graph->setTiming(TRUE);
$graph->setAntiAliasing(TRUE);

for($i = 0; $i < 4; $i++) {

	$x = array();
	
	for($j = 0; $j < 6; $j++) {
		$x[] = mt_rand(35, 100);
	}
	
	$plot = new Pie($x, PIE_DARK);
	$plot->setStartAngle(mt_rand(0, 360));
	$plot->title->set('Pie #'.$i);
	$plot->setSize(0.45, 0.45);
	$plot->setCenter(($i % 2) / 2 + 0.20, ($i > 1) ? 0.20 : 0.70);
	
	if(mt_rand(0, 1) === 1) {
		$plot->set3D(15);
	}
	
	$plot->setBorderColor(new Color(230, 230, 230));
	$plot->explode(array(mt_rand(5, 35), 3 => 8));
	
	if($i === 3) {
		$plot->legend->setPosition(1.1, 1.0);
	} else {
		$plot->legend->setTextMargin(8, 0);
		$plot->legend->hide(TRUE);
	}

	$graph->add($plot);
	
}
$graph->draw();
?>