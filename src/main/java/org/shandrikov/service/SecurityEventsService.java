package org.shandrikov.service;

import org.shandrikov.entity.SecurityEvent;
import org.shandrikov.enums.EventAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.shandrikov.repository.SecurityEventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SecurityEventsService {
    @Autowired
    SecurityEventRepository securityEventRepository;

    public List<SecurityEvent> getAllEvents() {
        return securityEventRepository.findAll();
    }

    public void registerEvent(EventAction action, String subject, String object, String path){
        SecurityEvent event = SecurityEvent.builder()
                .date(LocalDateTime.now())
                .action(action)
                .subject(subject)
                .object(object)
                .path(path)
                .build();
        securityEventRepository.save(event);
    }
}
