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
	1, 2, 5, 4, 2, 3
);

$plot = new LinePlot($x);

// Change component padding
$plot->setPadding(10, 12, 12, 7);

// Set a background gradient
$plot->setBackgroundGradient(
	new LinearGradient(
		new Color(230, 230, 230),
		new Color(255, 255, 255),
		0
	)
);

// Change line background color
$plot->setFillGradient(
	new LinearGradient(
		new Color(200, 240, 215, 30),
		new Color(150, 190, 165, 30),
		0
	)
);

// Hide grid
$plot->grid->hide(TRUE);
$plot->grid->setNobackground();

$plot->yAxis->label->hide(TRUE);
$plot->xAxis->label->hide(TRUE);

$plot->label->set($x);
$plot->label->setBackgroundColor(new Color(240, 240, 240, 10));
$plot->label->border->setColor(new Color(255, 0, 0, 15));
$plot->label->setPadding(3, 2, 0, 0);
$plot->label->setFont(new Font1);

$graph->add($plot);
$graph->draw();
?>