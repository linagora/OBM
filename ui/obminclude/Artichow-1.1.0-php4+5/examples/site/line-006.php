<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../../LinePlot.class.php";


$graph = new Graph(300, 200);

$graph->setAntiAliasing(TRUE);

$x = array(
	-4, -5, -2, -8, -3, 1, 4, 9, 5, 6, 2
);

$plot = new LinePlot($x);
$plot->setStyle(LINE_DASHED);

$plot->setSpace(4, 4, 10, 0);
$plot->setPadding(25, 15, 10, 18);

$plot->setBackgroundGradient(
	new LinearGradient(
		new Color(230, 230, 230),
		new Color(255, 255, 255),
		90
	)
);

$plot->setFilledArea(7, 9, new Red(25));
$plot->setFilledArea(1, 4, new Yellow(25));

$plot->setColor(new Color(0, 0, 150, 20));

$plot->grid->setColor(new VeryLightGray);

$plot->mark->setType(MARK_SQUARE);
$plot->mark->setSize(4);
$plot->mark->setFill(new VeryDarkGreen(30));
$plot->mark->border->show();
$plot->mark->border->setColor(new DarkBlue(60));

$plot->xAxis->label->hide(TRUE);
$plot->xAxis->setNumberByTick('minor', 'major', 3);

$plot->yAxis->setLabelNumber(8);

$plot->legend->add($plot, "My line");
$plot->legend->setPosition(0.9, 0.77);

$graph->add($plot);
$graph->draw();
?>