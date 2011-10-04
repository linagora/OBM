<?php

require_once "../../Graph.class.php";


$message = "Missing imageantialias() function.\nCheck your PHP installation.";


$message = wordwrap($message, 48, "\n", TRUE);

$width = 400;
$height = max(90, 50 + 13 * (substr_count($message, "\n") + 1));

$graph = new Graph($width, $height);

$driver = $graph->getDriver();

// Display title
$driver->filledRectangle(
	new White,
	new Line(
		new Point(0, 0),
		new Point($width, $height)
	)
);

$driver->filledRectangle(
	new Red,
	new Line(
		new Point(0, 0),
		new Point(110, 25)
	)
);

$text = new Text(
	"Artichow error",
	new Font3,
	new White,
	0
);

$driver->string($text, new Point(5, 6));

// Display red box
$driver->rectangle(
	new Red,
	new Line(
		new Point(0, 25),
		new Point($width - 90, $height - 1)
	)
);

// Display error image
$image = new FileImage('error.png');
$driver->copyImage($image, new Point($width - 81, $height - 81), new Point($width - 1, $height - 1));

// Draw message
$text = new Text(
	$message,
	new Font2,
	new Black,
	0
);

$driver->string($text, new Point(10, 40));

$graph->draw();

?>
