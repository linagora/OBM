<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */
require_once "../../Graph.class.php";

$graph = new Graph(400, 400);
//$graph->setAntiAliasing(TRUE);
$graph->title->set("Ticks");

$drawer = $graph->getDrawer();

// Simple horizontal line
$line = new Vector(
	new Point(10, 50),
	new Point(390, 50)
);
$drawer->line(new VeryLightGray, $line);
$tick = new Tick(10, 8);
$tick->draw($drawer, $line);
$tick2 = new Tick(40 - 3, 5);
$tick2->setInterval(2);
$tick2->draw($drawer, $line);
$tick3 = new Tick(0, 2);
$tick3->setNumberByTick($tick2, 1);
$tick3->draw($drawer, $line);

// Simple horizontal line
// TICK_OUT style
// Hide first tick
$line = new Vector(
	new Point(10, 70),
	new Point(390, 70)
);
$drawer->line(new VeryLightGray, $line);
$tick = new Tick(20, 5);
$tick->setStyle(TICK_OUT);
$tick->hideFirst(TRUE);
$tick->draw($drawer, $line);

// Simple horizontal line
// TICK_IN_OUT style
// Change color and hide last tick
$line = new Vector(
	new Point(10, 90),
	new Point(390, 90)
);
$drawer->line(new VeryLightGray, $line);
$tick = new Tick(10, 5);
$tick->setColor(new Red);
$tick->setStyle(TICK_IN_OUT);
$tick->hideLast(TRUE);
$tick->draw($drawer, $line);

// Horizontal line 180°
$line = new Vector(
	new Point(390, 110),
	new Point(10, 110)
);
$drawer->line(new VeryLightGray, $line);
$tick = new Tick(60, 5);
$tick->draw($drawer, $line);

// Simple vertical line
$line = new Vector(
	new Point(20, 130),
	new Point(20, 390)
);
$drawer->line(new VeryLightGray, $line);
$tick = new Tick(10, 5);
$tick->draw($drawer, $line);

// Simple vertical line
// TICK_OUT style
// Hide first tick
$line = new Vector(
	new Point(40, 130),
	new Point(40, 390)
);
$drawer->line(new VeryLightGray, $line);
$tick = new Tick(10, 5);
$tick->setStyle(TICK_OUT);
$tick->hideFirst(TRUE);
$tick->draw($drawer, $line);

// Simple vertical line
// TICK_IN_OUT style
// Change color and hide last tick
$line = new Vector(
	new Point(60, 130),
	new Point(60, 390)
);
$drawer->line(new VeryLightGray, $line);
$tick = new Tick(10, 5);
$tick->setColor(new Blue);
$tick->setStyle(TICK_IN_OUT);
$tick->hideLast(TRUE);
$tick->draw($drawer, $line);

// Complex line
$line = new Vector(
	new Point(80, 130),
	new Point(390, 390)
);
$drawer->line(new VeryLightGray, $line);
$tick = new Tick(10, 5);
$tick->draw($drawer, $line);

// Complex line
// Hide first tick
// TICK_OUT style
// Change color
$line = new Vector(
	new Point(390, 130),
	new Point(80, 390)
);
$drawer->line(new VeryLightGray, $line);
$tick = new Tick(10, 10);
$tick->setColor(new DarkGreen);
$tick->setStyle(TICK_OUT);
$tick->hideFirst(TRUE);
$tick->draw($drawer, $line);

// Complex line
// TICK_IN_OUT style
// Hide last tick
$line = new Vector(
	new Point(360, 390),
	new Point(80, 160)
);
$drawer->line(new VeryLightGray, $line);
$tick = new Tick(30, 15);
$tick->setStyle(TICK_IN_OUT);
$tick->hideLast(TRUE);
$tick->draw($drawer, $line);

$graph->draw();
?>