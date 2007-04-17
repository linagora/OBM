<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../../LinePlot.class.php";


$graph = new Graph(150, 100);

$graph->setAntiAliasing(TRUE);

$x = array(
	0, 2, 5, 2, 3, 8
);

$plot = new LinePlot($x);
$plot->setXAxisZero(FALSE);
$plot->grid->setNobackground();

$plot->setSpace(6, 6, 10, 10);
$plot->setPadding(30, 6, 8, 18);

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
		0
	)
);

// Change mark type
$plot->mark->setType(MARK_CIRCLE);
$plot->mark->border->show();
$plot->mark->setSize(6);

$plot->yAxis->setLabelPrecision(1);
$plot->yAxis->label->setFont(new Font1);
$plot->xAxis->label->setFont(new Font1);

$graph->add($plot);
$graph->draw();
?>