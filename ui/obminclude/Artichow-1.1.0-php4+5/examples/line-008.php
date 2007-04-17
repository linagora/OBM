<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../LinePlot.class.php";


// Use cache
$graph = new Graph(400, 400);
$graph->setAntiAliasing(TRUE);
$graph->border->setStyle(LINE_DOTTED);
$graph->border->setColor(new Red);

$x = array();
for($i = 0; $i < 10; $i++) {
	$x[] = mt_rand(20, 100);
}

$plot = new LinePlot($x);
$plot->setFilledArea(0, 1, new Red(40));
$plot->setFilledArea(1, 2, new LinearGradient(new Red(40), new Orange(40), 90));
$plot->setFilledArea(2, 4, new LinearGradient(new Orange(40), new Green(40), 90));
$plot->setFilledArea(4, 7, new LinearGradient(new Green(40), new Blue(40), 90));
$plot->setFilledArea(7, 8, new LinearGradient(new Blue(40), new Purple(40), 90));
$plot->setFilledArea(8, 9, new Purple(40));

$graph->add($plot);
$graph->draw();
?>