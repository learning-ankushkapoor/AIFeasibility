package com.example.otpservice.controller;

import com.example.otpservice.dto.OtpGenerationRequest;
import com.example.otpservice.dto.OtpValidationRequest;
import com.example.otpservice.service.OtpService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otp")
public class OtpController {

    private final OtpService otpService;

    @Autowired
    public OtpController(OtpService otpService) {
        this.otpService = otpService;
    }

    @PostMapping("/generate")
    public ResponseEntity<String> generateOtp(@RequestBody OtpGenerationRequest request) {
        if (request.getMobileNumber() == null || request.getMobileNumber().trim().isEmpty() ||
            request.getUserId() == null || request.getUserId().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Mobile number and User ID must not be empty.");
        }

        String generatedOtp = otpService.generateOtp(request.getMobileNumber(), request.getUserId());
        return ResponseEntity.ok("Generated OTP: " + generatedOtp); // For testing; consider not sending OTP in response
    }

    @PostMapping("/validate")
    public ResponseEntity<String> validateOtp(@RequestBody OtpValidationRequest request) {
        if (request.getMobileNumber() == null || request.getMobileNumber().trim().isEmpty() ||
            request.getUserId() == null || request.getUserId().trim().isEmpty() ||
            request.getOtp() == null || request.getOtp().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Mobile number, User ID, and OTP must not be empty.");
        }

        boolean isValid = otpService.validateOtp(
                request.getMobileNumber(),
                request.getUserId(),
                request.getOtp()
        );

        if (isValid) {
            return ResponseEntity.ok("OTP validated successfully.");
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired OTP.");
        }
    }
}
