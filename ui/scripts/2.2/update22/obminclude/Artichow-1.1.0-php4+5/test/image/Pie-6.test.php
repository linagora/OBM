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

$graph->shadow->setSize(10);
$graph->shadow->smooth(TRUE);

$x = array();

for($j = 0; $j < mt_rand(3, 6); $j++) {
	$x[] = mt_rand(35, 100);
}

$plot = new Pie($x, PIE_DARK);
$plot->setSize(0.8, 0.8);
$plot->setCenter(mt_rand(45, 55) / 100, mt_rand(45, 55) / 100);

$plot->set3D(10);
$plot->setBorderColor(new Color(230, 230, 230));
$plot->explode(array(1 => mt_rand(5, 50), 2 => mt_rand(5, 50), 3 => 12));

$plot->legend->setPadding(10, 10, 10, 10);
$plot->legend->setTextMargin(8, 0);
$plot->legend->hide(TRUE);

$graph->add($plot);
	
$graph->draw();
?>