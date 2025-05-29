package com.example.otpservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpValidationRequest {
    private String mobileNumber;
    private String userId;
    private String otp;
}
