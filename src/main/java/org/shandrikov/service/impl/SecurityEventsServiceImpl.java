package org.shandrikov.service.impl;

import org.shandrikov.entity.SecurityEvent;
import org.shandrikov.enums.EventAction;
import org.shandrikov.service.SecurityEventsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.shandrikov.repository.SecurityEventRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SecurityEventsServiceImpl implements SecurityEventsService {
    @Autowired
    SecurityEventRepository securityEventRepository;

    @Override
    public List<SecurityEvent> getAllEvents() {
        return securityEventRepository.findAll();
    }

    @Override
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
