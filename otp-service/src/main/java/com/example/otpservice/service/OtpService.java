package com.example.otpservice.service;

import com.example.otpservice.model.OtpDetails;
import com.example.otpservice.repository.OtpRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {

    private static final Logger logger = LoggerFactory.getLogger(OtpService.class);
    private final OtpRepository otpRepository;
    private static final long OTP_EXPIRATION_MS = 5 * 60 * 1000; // 5 minutes in milliseconds

    @Autowired
    public OtpService(OtpRepository otpRepository) {
        this.otpRepository = otpRepository;
    }

    /**
     * Generates a random 6-digit numeric OTP, stores it in Redis, simulates sending it via SMS, and returns the OTP.
     *
     * @param mobileNumber The mobile number for which the OTP is generated.
     * @param userId       The user ID for whom the OTP is generated.
     * @return A 6-digit numeric OTP string.
     */
    public String generateOtp(String mobileNumber, String userId) {
        Random random = new Random();
        int otpNumber = random.nextInt(1000000); // Generates 0 to 999999
        String generatedOtp = String.format("%06d", otpNumber);

        String id = mobileNumber + ":" + userId;
        long expirationTime = System.currentTimeMillis() + OTP_EXPIRATION_MS;

        OtpDetails otpDetails = new OtpDetails(id, generatedOtp, mobileNumber, userId, expirationTime);
        otpRepository.save(otpDetails);

        sendSms(mobileNumber, generatedOtp);

        return generatedOtp;
    }

    /**
     * Validates the provided OTP against the stored OTP for the given mobile number and user ID.
     *
     * @param mobileNumber  The mobile number to validate against.
     * @param userId        The user ID to validate against.
     * @param otpToValidate The OTP string to validate.
     * @return True if the OTP is valid and not expired, false otherwise.
     */
    public boolean validateOtp(String mobileNumber, String userId, String otpToValidate) {
        String id = mobileNumber + ":" + userId;
        Optional<OtpDetails> otpDetailsOptional = otpRepository.findById(id);

        if (otpDetailsOptional.isEmpty()) {
            logger.warn("OTP validation failed: No OTP details found for id '{}'", id);
            return false; // OTP details not found
        }

        OtpDetails otpDetails = otpDetailsOptional.get();

        // OTP doesn't match
        if (!otpDetails.getOtp().equals(otpToValidate)) {
            logger.warn("OTP validation failed: Provided OTP does not match stored OTP for id '{}'", id);
            return false;
        }

        // Check for expiration
        boolean isExpired = System.currentTimeMillis() >= otpDetails.getExpirationTime();

        // Delete OTP regardless of whether it's valid or expired to prevent reuse of validated/expired OTPs
        otpRepository.deleteById(id);

        if (isExpired) {
            logger.warn("OTP validation failed: OTP for id '{}' has expired.", id);
            return false;
        }

        logger.info("OTP validation successful for id '{}'", id);
        return true; // Return true if not expired and matches
    }

    /**
     * Simulates sending an SMS with the OTP.
     * In a real application, this method would integrate with an SMS gateway (e.g., GCP SMS).
     *
     * @param mobileNumber The mobile number to send the SMS to.
     * @param otp          The OTP to send.
     */
    private void sendSms(String mobileNumber, String otp) {
        // TODO: Implement actual SMS sending logic using GCP SMS client
        // For now, we just log the simulation.
        // Example with GCP SMS client (conceptual, requires setup and credentials):
        // try (SmsServiceClient smsServiceClient = SmsServiceClient.create()) {
        //     SendSmsRequest request = SendSmsRequest.newBuilder()
        //         .setPhoneNumber(mobileNumber)
        //         .setMessage("Your OTP is: " + otp)
        //         // .setSenderId("YOUR_SENDER_ID") // Optional: if you have a specific sender ID
        //         .build();
        //     SendSmsResponse response = smsServiceClient.sendSms(request);
        //     logger.info("SMS sent successfully to {}: Message ID {}", mobileNumber, response.getMessageId());
        // } catch (Exception e) {
        //     logger.error("Failed to send SMS to {}: {}", mobileNumber, e.getMessage(), e);
        // }
        logger.info("Simulating SMS sent to {}: OTP is {}", mobileNumber, otp);
    }
}
