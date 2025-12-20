package com.ecommerce.auth.event;

import com.ecommerce.auth.entity.User;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserRegisteredEvent extends ApplicationEvent {

    private final User user;
    private final String ipAddress;
    private final String userAgent;

    public UserRegisteredEvent(Object source, User user,
                               String ipAddress, String userAgent) {
        super(source);
        this.user = user;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }
}
