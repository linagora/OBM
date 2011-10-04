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

$x = array();

for($i = 0; $i < 10; $i++) {
	$x[] = mt_rand(0, 100);
}

$plot = new Pie($x);
$plot->setSize(0.8, 0.8);
$plot->setCenter(0.45, 0.5);
$plot->set3D(20);
$plot->setBorderColor(new Color(100, 100, 100));

$plot->label->setPadding(2, 2, 2, 2);
$plot->label->border->setColor(new Red(60));
$plot->label->setFont(new Tuffy(7));
$plot->label->setBackgroundGradient(
	new LinearGradient(
		new Red(80),
		new White(80),
		0
	)
);
$plot->setLabelPrecision(1);

$plot->legend->setPadding(10, 10, 10, 10);
$plot->legend->setTextMargin(8, 0);
$plot->legend->setPosition(1.16, 0.5);

$graph->add($plot);
$graph->draw();
?>