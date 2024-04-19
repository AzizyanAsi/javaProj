package net.idonow.service.entity.impl.system.chat;

import com.corundumstudio.socketio.SocketIOClient;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationInfo {
    private String token;
    private String userType;
    private SocketIOClient client;
    private HttpServletRequest request;


}
