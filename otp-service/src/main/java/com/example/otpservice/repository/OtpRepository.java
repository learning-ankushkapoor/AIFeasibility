package com.example.otpservice.repository;

import com.example.otpservice.model.OtpDetails;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OtpRepository extends CrudRepository<OtpDetails, String> {
    // CrudRepository provides methods like save, findById, delete, etc.
    // Additional custom query methods can be defined here if needed.
}
