<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../../Pie.class.php";

function createPie($values, $title, $x, $y) {
	
	$plot = new Pie($values, PIE_EARTH);
	$plot->title->set($title);
	$plot->title->setFont(new TuffyBold(9));
	$plot->title->move(NULL, -12);
	
	$plot->label->setFont(new Tuffy(7));
	$plot->legend->hide(TRUE);
	$plot->setLabelPosition(5);
	$plot->setSize(0.48, 0.35);
	$plot->setCenter($x, $y);
	$plot->set3D(8);
	$plot->setBorderColor(new White);
	
	return $plot;

}

$graph = new Graph(280, 350);
$graph->setAntiAliasing(TRUE);

$plot = createPie(array(1, 4, 5, 2, 3), "Cowléoptère", 0.25, 0.24);
$graph->add($plot);

$plot = createPie(array(1, 9, 1, 2, 1), "Asticow", 0.75, 0.24);
$graph->add($plot);

$plot = createPie(array(5, 7, 8, 6, 3), "Cowlibri", 0.25, 0.65);
$graph->add($plot);

$plot = createPie(array(6, 4, 6, 5, 6), "Bourricow", 0.75, 0.65);
$plot->legend->setModel(LEGEND_MODEL_BOTTOM);
$plot->setLegend(array('plip', 'plop', 'plap', 'plup', 'plep'));
$plot->legend->hide(FALSE); // We print only one legend
$plot->legend->setPosition(0, 1.10); 
$graph->add($plot);

$graph->draw();
?>