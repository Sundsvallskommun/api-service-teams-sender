package se.sundsvall.teamssender.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TeamsResource {

    @PostMapping(path="send/teams/message")
    public ResponseEntity<String> sendTeamsMessage(){
        return ResponseEntity.ok("Sent Successfully");
    }
}
