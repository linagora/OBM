<?php
/*
 * This work is hereby released into the Public Domain.
 * To view a copy of the public domain dedication,
 * visit http://creativecommons.org/licenses/publicdomain/ or send a letter to
 * Creative Commons, 559 Nathan Abbott Way, Stanford, California 94305, USA.
 *
 */

require_once "../../LinePlot.class.php";
require_once "../../BarPlot.class.php";


	
$graph = new Graph(600, 250);

$graph->setBackgroundColor(new Color(0xF4, 0xF4, 0xF4));
$graph->shadow->setSize(3);

$graph->title->set("Evolution");
$graph->title->setFont(new Tuffy(15));
$graph->title->setColor(new Color(0x00, 0x00, 0x8B));


$group = new PlotGroup;
$group->setSize(0.82, 1);
$group->setCenter(0.41, 0.5);
$group->setPadding(35, 26, 40, 27);
$group->setSpace(2, 2);

$group->grid->setColor(new Color(0xC4, 0xC4, 0xC4));
$group->grid->setType(LINE_DASHED);
$group->grid->hideVertical(TRUE);
$group->grid->setBackgroundColor(new White);

$group->axis->left->setColor(new DarkGreen);
$group->axis->left->label->setFont(new Font2);

$group->axis->right->setColor(new DarkBlue);
$group->axis->right->label->setFont(new Font2);

$group->axis->bottom->label->setFont(new Font2);

$group->legend->setPosition(1.18);
$group->legend->setTextFont(new Tuffy(8));
$group->legend->setSpace(10);

// Add a bar plot
$x = array(16, 16, 12, 13, 11, 18, 10, 12, 11, 12, 11, 16);

$plot = new BarPlot($x, 1, 2);
$plot->setBarColor(new MidYellow);
$plot->setBarPadding(0.15, 0.15);
$plot->barShadow->setSize(3);
$plot->barShadow->smooth(TRUE);
$plot->barShadow->setColor(new Color(200, 200, 200, 10));
$plot->move(1, 0);

$group->legend->add($plot, "Yellow bar", LEGEND_BACKGROUND);
$group->add($plot);

// Add a bar plot
$x = array(20, 25, 20, 18, 16, 25, 29, 12, 15, 18, 21, 26);

$plot = new BarPlot($x, 2, 2);
$plot->setBarColor(new Color(120, 175, 80, 10));
$plot->setBarPadding(0.15, 0.15);
$plot->barShadow->setSize(3);
$plot->barShadow->smooth(TRUE);
$plot->barShadow->setColor(new Color(200, 200, 200, 10));

$group->legend->add($plot, "Green bar", LEGEND_BACKGROUND);
$group->add($plot);

// Add a second bar plot
$x = array(12, 14, 10, 9, 10, 16, 12, 8, 8, 10, 12, 13);

$plot = new BarPlot($x, 2, 2);
$plot->setBarColor(new Orange);
$plot->setBarPadding(0.15, 0.15);

$group->legend->add($plot, "Orange bar", LEGEND_BACKGROUND);
$group->add($plot);

// Add a line plot
$x = array(6, 5, 6, 5.5, 4.5, 4, 4.5, 4, 5, 4, 5, 5.5);

$plot = new LinePlot($x, LINEPLOT_MIDDLE);
$plot->setColor(new DarkBlue);
$plot->setThickness(5);
$plot->setYAxis(PLOT_RIGHT);
$plot->setYMax(12);

$plot->mark->setType(MARK_CIRCLE);
$plot->mark->setSize(6);
$plot->mark->setFill(new LightBlue);
$plot->mark->border->show();

$group->legend->add($plot, "Blue line", LEGEND_MARK);
$group->add($plot);

$graph->add($group);
$graph->draw();
?>