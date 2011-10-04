<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../../LinePlot.class.php";


$graph = new Graph(300, 175);

$graph->setAntiAliasing(TRUE);

$x = array(
	4, 3, 1, 0, -2, 1, 3, 2, 3, 5, 4, 1
);

$plot = new LinePlot($x);
$plot->setXAxisZero(FALSE);

$plot->grid->hide(TRUE);

$plot->title->set("Using dashed line and legend");
$plot->title->setFont(new TuffyItalic(9));
$plot->title->setBackgroundColor(new Color(255, 255, 255, 50));
$plot->title->setPadding(3, 3, 3, 3);
$plot->title->move(0, 20);

$plot->setSpace(6, 6, 10, 10);
$plot->setPadding(30, 10, 15, 25);

$plot->setBackgroundColor(
	new Color(245, 245, 245)
);

$plot->setStyle(LINE_DASHED);
$plot->setColor(new Color(0, 150, 0, 20));

$plot->setFillGradient(
	new LinearGradient(
		new Color(220, 220, 150, 40),
		new Color(255, 255, 210, 30),
		0
	)
);

$graph->shadow->setSize(4);
$graph->shadow->setPosition(SHADOW_LEFT_BOTTOM);
$graph->shadow->smooth(TRUE);

$plot->legend->add($plot, "Apples");
$plot->legend->shadow->setSize(0);
$plot->legend->setAlign(LEGEND_CENTER, LEGEND_TOP);
$plot->legend->setPosition(0.75, 0.60);
$plot->legend->setTextFont(new Tuffy(8));

$graph->add($plot);
$graph->draw();
?>