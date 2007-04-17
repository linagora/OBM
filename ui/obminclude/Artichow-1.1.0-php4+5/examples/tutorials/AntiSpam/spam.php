<?php

require_once '../../../AntiSpam.class.php';

// On créé l'image anti-spam
$object = new AntiSpam();

// La valeur affichée sur l'image fera 5 caractères
$object->setRand(5);

// On assigne un nom à cette image pour vérifier
// ultérieurement la valeur fournie par l'utilisateur
$object->save('example');

// On affiche l'image à l'écran
$object->draw();

?>
