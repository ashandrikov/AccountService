package org.shandrikov.service;

import org.shandrikov.entity.SecurityEvent;
import org.shandrikov.enums.EventAction;

import java.util.List;

public interface SecurityEventsService {
    List<SecurityEvent> getAllEvents();
    void registerEvent(EventAction action, String subject, String object, String path);
}
