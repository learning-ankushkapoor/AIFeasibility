package com.example.otpservice.service;

import com.example.otpservice.model.OtpDetails;
import com.example.otpservice.repository.OtpRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OtpServiceTest {

    @Mock
    private OtpRepository otpRepository;

    private OtpService otpService;

    private static final long OTP_EXPIRATION_MS = 5 * 60 * 1000; // 5 minutes

    @BeforeEach
    void setUp() {
        otpService = new OtpService(otpRepository);
    }

    @Test
    void testGenerateOtp_success() {
        String mobileNumber = "1234567890";
        String userId = "user123";

        String generatedOtp = otpService.generateOtp(mobileNumber, userId);

        assertNotNull(generatedOtp);
        assertEquals(6, generatedOtp.length());
        assertTrue(generatedOtp.matches("\\d{6}"), "OTP should be 6 digits");

        ArgumentCaptor<OtpDetails> otpDetailsCaptor = ArgumentCaptor.forClass(OtpDetails.class);
        verify(otpRepository).save(otpDetailsCaptor.capture());

        OtpDetails savedOtpDetails = otpDetailsCaptor.getValue();
        assertNotNull(savedOtpDetails);
        assertEquals(mobileNumber + ":" + userId, savedOtpDetails.getId());
        assertEquals(generatedOtp, savedOtpDetails.getOtp());
        assertEquals(mobileNumber, savedOtpDetails.getMobileNumber());
        assertEquals(userId, savedOtpDetails.getUserId());
        assertTrue(savedOtpDetails.getExpirationTime() > System.currentTimeMillis(), "Expiration time should be in the future");
        assertTrue(savedOtpDetails.getExpirationTime() <= System.currentTimeMillis() + OTP_EXPIRATION_MS + 1000, // Adding a small buffer for execution time
                "Expiration time should be around 5 minutes from now");
        
        // Verification of sendSms (private method) is skipped as per plan.
        // If it were protected/package-private, we could use a spy.
    }

    @Test
    void testValidateOtp_success() {
        String mobileNumber = "1234567890";
        String userId = "user123";
        String otp = "123456";
        String id = mobileNumber + ":" + userId;
        long futureExpirationTime = System.currentTimeMillis() + OTP_EXPIRATION_MS;
        OtpDetails otpDetails = new OtpDetails(id, otp, mobileNumber, userId, futureExpirationTime);

        when(otpRepository.findById(id)).thenReturn(Optional.of(otpDetails));

        boolean isValid = otpService.validateOtp(mobileNumber, userId, otp);

        assertTrue(isValid);
        verify(otpRepository).findById(id);
        verify(otpRepository).deleteById(id);
    }

    @Test
    void testValidateOtp_failure_otpMismatch() {
        String mobileNumber = "1234567890";
        String userId = "user123";
        String storedOtp = "123456";
        String providedOtp = "654321"; // Mismatched OTP
        String id = mobileNumber + ":" + userId;
        long futureExpirationTime = System.currentTimeMillis() + OTP_EXPIRATION_MS;
        OtpDetails otpDetails = new OtpDetails(id, storedOtp, mobileNumber, userId, futureExpirationTime);

        when(otpRepository.findById(id)).thenReturn(Optional.of(otpDetails));

        boolean isValid = otpService.validateOtp(mobileNumber, userId, providedOtp);

        assertFalse(isValid);
        verify(otpRepository).findById(id);
        verify(otpRepository).deleteById(id); // Delete is called even if OTP mismatches, as long as entry is found
    }

    @Test
    void testValidateOtp_failure_expired() {
        String mobileNumber = "1234567890";
        String userId = "user123";
        String otp = "123456";
        String id = mobileNumber + ":" + userId;
        long pastExpirationTime = System.currentTimeMillis() - OTP_EXPIRATION_MS; // Expired
        OtpDetails otpDetails = new OtpDetails(id, otp, mobileNumber, userId, pastExpirationTime);

        when(otpRepository.findById(id)).thenReturn(Optional.of(otpDetails));

        boolean isValid = otpService.validateOtp(mobileNumber, userId, otp);

        assertFalse(isValid);
        verify(otpRepository).findById(id);
        verify(otpRepository).deleteById(id);
    }

    @Test
    void testValidateOtp_failure_notFound() {
        String mobileNumber = "1234567890";
        String userId = "user123";
        String otp = "123456";
        String id = mobileNumber + ":" + userId;

        when(otpRepository.findById(id)).thenReturn(Optional.empty());

        boolean isValid = otpService.validateOtp(mobileNumber, userId, otp);

        assertFalse(isValid);
        verify(otpRepository).findById(id);
        verify(otpRepository, never()).deleteById(anyString()); // deleteById should not be called if OTP not found
    }
}
