<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../LinePlot.class.php";


$graph = new Graph(400, 300);
$graph->setAntiAliasing(TRUE);

$x = array(
	1, 2, 5, 0.5, 3, 8
);

$plot = new LinePlot($x);

$plot->setSpace(6, 6, 10, 10);
$plot->setXAxisZero(FALSE);

// Set a background gradient
$plot->setBackgroundGradient(
	new LinearGradient(
		new Color(210, 210, 210),
		new Color(255, 255, 255),
		0
	)
);

// Change line color
$plot->setColor(new Color(0, 0, 150, 20));

// Set line background gradient
$plot->setFillGradient(
	new LinearGradient(
		new Color(150, 150, 210),
		new Color(230, 230, 255),
		90
	)
);

// Change mark type
$plot->mark->setType(MARK_CIRCLE);
$plot->mark->border->show();

$graph->add($plot);
$graph->draw();
?>