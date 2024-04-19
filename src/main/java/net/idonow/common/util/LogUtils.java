package net.idonow.common.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
public abstract class LogUtils {

    // AUDIT logger
    public static final Logger auditLog = LoggerFactory.getLogger("CustomAuditLogger");

    // Custom action properties (FETCH - get for update)
    public enum Action {CREATE, FETCH, UPDATE, DELETE}

    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        // StdDateFormat is ISO8601 since jackson 2.9
        objectMapper.setDateFormat(new StdDateFormat().withColonInTimeZone(true));
    }

    /**
     * @param action Action with entity
     * @param entity Modified object or object before modification. Transform objects should be used to avoid from logging HibernateProxy objects instead of actual entities
     * @return The resulting json
     */
    public static String buildJSONMessage(Action action, Object entity) {
        Map<String, Object> messageMap = new LinkedHashMap<>();
        messageMap.put("ACTION", action);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            messageMap.put("ACCOUNT", authentication.getPrincipal());
        }

        if (entity != null) {
            // Get class name if DTO is provided
            String target = entity.getClass().getSimpleName().replaceFirst("(Brief)?(Extended)?(Self)?Response", "");
            messageMap.put("TARGET", target);
            messageMap.put("ENTITY", objectMapper.convertValue(entity, HashMap.class));
        }
        try {
            return objectMapper.writeValueAsString(messageMap);
        } catch (JsonProcessingException exception) {
            throw new RuntimeException(String.format("Error while serializing log message: {%s}", exception.getMessage()));
        }

    }

}
