package se.sundsvall.teamssender.service;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class TeamsMessageService {

    public ResponseEntity<String> sendTeamsMessage() {
        return ResponseEntity.ok("Sent successfully");
    }
}
