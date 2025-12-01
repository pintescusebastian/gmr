package com.mycompany.report_generator.security;

import com.mycompany.report_generator.models.Doctor;
import com.mycompany.report_generator.repositories.DoctorRepository;
import java.util.Collections;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class DoctorDetailsService implements UserDetailsService {

    private final DoctorRepository doctorRepository;

    public DoctorDetailsService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String doctorCode)
        throws UsernameNotFoundException {
        Doctor doctor = doctorRepository
            .findByCode(doctorCode)
            .orElseThrow(() ->
                new UsernameNotFoundException(
                    "Doctorul cu codul " + doctorCode + " nu a fost găsit."
                )
            );

        return new org.springframework.security.core.userdetails.User(
            doctor.getCode(),
            "{noop}secure_password",
            Collections.emptyList()
        );
    }
}
