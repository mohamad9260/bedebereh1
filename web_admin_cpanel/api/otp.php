<?php
/**
 * Unified OTP Router for BedeBere
 * Supports ?action=request and ?action=verify or POST body 'action'
 */
header('Content-Type: application/json; charset=utf-8');

$action = trim($_GET['action'] ?? '');
if (empty($action)) {
    $rawInput = json_decode(file_get_contents('php://input'), true) ?? $_POST;
    $action = trim($rawInput['action'] ?? '');
}

if ($action === 'verify') {
    require_once __DIR__ . '/otp_verify.php';
} else {
    // Default to request
    require_once __DIR__ . '/otp_request.php';
}
