<?php
/**
 * Compatibility alias for GET listings.php
 */
$_SERVER['REQUEST_METHOD'] = 'GET';
require_once __DIR__ . '/listings.php';
