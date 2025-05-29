package com.example.otpservice.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@RedisHash("OtpDetails")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OtpDetails {

    @Id
    private String id; // Can be a combination of mobileNumber and userId

    private String otp;
    private String mobileNumber;
    private String userId;
    private long expirationTime; // Timestamp

}
