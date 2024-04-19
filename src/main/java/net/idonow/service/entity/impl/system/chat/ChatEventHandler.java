package net.idonow.service.entity.impl.system.chat;

import com.corundumstudio.socketio.*;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DataListener;
import com.corundumstudio.socketio.listener.DisconnectListener;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import net.idonow.controller.exception.common.ActionNotAllowedException;
import net.idonow.entity.User;
import net.idonow.entity.system.SystemUser;
import net.idonow.repository.UserRepository;
import net.idonow.repository.system.SystemUserRepository;
import net.idonow.security.enums.RoleType;
import net.idonow.security.service.common.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ChatEventHandler implements ConnectListener, DisconnectListener, DataListener<String> {//TODO not checked
    private final UserRepository userRepository;
    private final SystemUserRepository systemUserRepository;
    private final JwtService jwtService;

    @Autowired
    public ChatEventHandler(UserRepository userRepository, SystemUserRepository systemUserRepository,
            JwtService jwtService) {
        this.userRepository = userRepository;
        this.systemUserRepository = systemUserRepository;
        this.jwtService = jwtService;
    }

    private static final String USER_NAMESPACE = "/user";
    private static final String PROF_USER_NAMESPACE = "/professional";
    private static final String SYSTEM_USER_NAMESPACE = "/system-user";

    @OnConnect
    public void onConnect(SocketIOClient client) {
        AuthenticationInfo authInfo = getAuthenticationInfo(client);
        if (authInfo.getUserType().equals(RoleType.CLIENT.toString()) || authInfo.getUserType()
                .equals(RoleType.PROFESSIONAL.toString())) {
            handleUserConnection(client, authInfo);
        } else if (authInfo.getUserType().equals(RoleType.SUPPORT_AGENT.toString())) {
            handleSystemUserConnection(client, authInfo);
        }
    }

    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        if (isNamespace(client, USER_NAMESPACE)) {
            handleUserDisconnect(client);
        } else if (isNamespace(client, PROF_USER_NAMESPACE)) {
            handleUserDisconnect(client);
        } else if (isNamespace(client, SYSTEM_USER_NAMESPACE)) {
            handleSystemUserDisconnect(client);
        }
        log.info("Client disconnected: " + client.getSessionId().toString());
    }

    @Override
    public void onData(SocketIOClient client, String data, AckRequest ackSender) {
        handleIncomingMessage(client, data);
    }

    private void handleUserConnection(SocketIOClient client, AuthenticationInfo authInfo) {
        User user = authenticateUser(authInfo, client);
        if (user != null) {
            updateUserConnectionStatus(user, client, true);
            sendWelcomeMessage(client, "Welcome, " + user.getFirstName() + "!");
        } else {
            client.disconnect();
        }
    }

    private void handleSystemUserConnection(SocketIOClient client, AuthenticationInfo authInfo) {
        SystemUser systemUser = authenticateSystemUser(authInfo, client);
        if (systemUser != null) {
            updateSystemUserConnectionStatus(systemUser, client, true);
            sendWelcomeMessage(client, "Welcome, " + systemUser.getFirstName() + "!");
        } else {
            client.disconnect();
        }
    }

    private void handleUserDisconnect(SocketIOClient client) {
        User user = userRepository.findBySocketSessionId(client.getSessionId().toString());
        if (user != null) {
            updateUserConnectionStatus(user, client, false);
        }
    }

    private void handleSystemUserDisconnect(SocketIOClient client) {
        SystemUser systemUser = systemUserRepository.findBySocketSessionId(client.getSessionId().toString());
        if (systemUser != null) {
            updateSystemUserConnectionStatus(systemUser, client, false);
        }
    }

    private void handleIncomingMessage(SocketIOClient client, String message) {
        log.info("Received message from " + client.getSessionId() + ": " + message);
        broadcastMessage(client, message);
    }

    private void broadcastMessage(SocketIOClient senderClient, String message) {
        SocketIONamespace namespace = senderClient.getNamespace();
        namespace.getBroadcastOperations().sendEvent("chatMessage", message);
    }

    private AuthenticationInfo getAuthenticationInfo(SocketIOClient client) {
        String token = client.getHandshakeData().getSingleUrlParam("token");
        RoleType userType = determineUserType(client.getNamespace().getName());
        AuthenticationInfo authInfo = new AuthenticationInfo();
        authInfo.setToken(token);
        authInfo.setUserType(userType.toString());
        return authInfo;
    }

    public User authenticateUser(AuthenticationInfo authInfo, SocketIOClient client) {
        String token = authInfo.getToken();
        // Validate the token using JwtService
        try {
            Authentication authentication = jwtService.decodeAccessJwt(token, null);//check it
            if (authentication instanceof UsernamePasswordAuthenticationToken) {
                // Retrieve the email from the principal
                String email = (String) authentication.getPrincipal();
                RoleType userType = RoleType.valueOf(authentication.getAuthorities().iterator().next().getAuthority());
                // Retrieve a User object based on the email and user type
                User user = getUserByEmailAndType(email, userType);
                if (user != null) {
                    updateUserConnectionStatus(user, client, true);
                    return user;
                }
            }
        }
        catch (JwtException | ActionNotAllowedException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Authentication failed");
    }

    public SystemUser authenticateSystemUser(AuthenticationInfo authInfo, SocketIOClient client) {
        String token = authInfo.getToken();
        // Validate the token using JwtService
        try {
            Authentication authentication = jwtService.decodeAccessJwt(token, null);
            if (authentication instanceof UsernamePasswordAuthenticationToken) {
                // Retrieve the email from the principal
                String email = (String) authentication.getPrincipal();
                RoleType userType = RoleType.valueOf(authentication.getAuthorities().iterator().next().getAuthority());
                // Retrieve a User object based on the email and user type
                SystemUser user = getSystemUserByEmailAndType(email, userType);
                if (user != null) {
                    updateSystemUserConnectionStatus(user, client, true);
                    return user;
                }
            }
        }
        catch (JwtException | ActionNotAllowedException e) {
            e.printStackTrace();
        }
        throw new RuntimeException("Authentication failed");
    }

    private User getUserByEmailAndType(String email, RoleType userType) {
        return userRepository.findByEmailAndRoleRoleType(email, userType);
    }

    private SystemUser getSystemUserByEmailAndType(String email, RoleType userType) {
        return systemUserRepository.findByEmailAndRoleRoleType(email, userType);
    }

    private RoleType determineUserType(String namespace) {
        if (namespace.equals(USER_NAMESPACE)) {
            return RoleType.CLIENT;
        } else if (namespace.equals(SYSTEM_USER_NAMESPACE)) {
            return RoleType.SUPPORT_AGENT;
        } else if (namespace.equals(PROF_USER_NAMESPACE)) {
            return RoleType.PROFESSIONAL;
        }
        throw new RuntimeException("UserType doesn't supported" + namespace);
    }

    private void updateUserConnectionStatus(User user, SocketIOClient client, boolean online) {
        user.setOnline(online);
        user.setSocketSessionId(client.getSessionId().toString());
        userRepository.save(user);
    }

    private void updateSystemUserConnectionStatus(SystemUser systemUser, SocketIOClient client, boolean online) {
        systemUser.setOnline(online);
        systemUser.setSocketSessionId(client.getSessionId().toString());
        systemUserRepository.save(systemUser);
    }

    private void sendWelcomeMessage(SocketIOClient client, String message) {
        client.sendEvent("welcome-message", message);
    }

    private boolean isNamespace(SocketIOClient client, String namespace) {
        return client.getNamespace().getName().equals(namespace);
    }
}
