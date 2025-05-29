package com.example.otpservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OtpGenerationRequest {
    private String mobileNumber;
    private String userId;
}
