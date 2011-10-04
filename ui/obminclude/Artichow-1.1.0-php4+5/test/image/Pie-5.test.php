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

$x = array();

for($i = 0; $i < 7; $i++) {
	$x[] = mt_rand(20, 100);
}

$plot = new Pie($x, PIE_EARTH);
$plot->setBorderColor(new Color(50, 50, 50));
$plot->set3D(20);

$plot->setLabelPosition(-60);
$plot->label->setPadding(2, 2, 2, 2);
$plot->label->border->setColor(new Red(20));
$plot->label->setFont(new Tuffy(7));
$plot->label->setBackgroundGradient(
	new LinearGradient(
		new LightRed(30),
		new White(30),
		0
	)
);

$plot->legend->setPadding(10, 10, 10, 10);
$plot->legend->setTextMargin(8, 0);

$graph->add($plot);
$graph->draw();
?>