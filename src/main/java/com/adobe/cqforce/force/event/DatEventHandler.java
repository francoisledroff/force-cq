package com.adobe.cqforce.force.event;

import com.adobe.cqforce.force.service.UserService;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;


/**
 * Handler for DAT related events. This will trigger the sfdc user list cache load
 */
@Component(metatype = false, immediate = true)
@Service(value = {EventHandler.class})
@Property(name = EventConstants.EVENT_TOPIC,
        value = {DatEventConstants.DAT_AUTH_SUCCESS_EVENT_TOPIC, DatEventConstants.DAT_IDENTITY_VALID_TOPIC})
public class DatEventHandler implements EventHandler {

    @Reference
    private UserService userService;

    public void handleEvent(Event event) {
        userService.loadUserList((String) event.getProperty(DatEventConstants.USER_ID));
    }

}