package net.idonow.security.configs;

import com.corundumstudio.socketio.SocketIOServer;
import net.idonow.service.entity.impl.system.chat.ChatEventHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.corundumstudio.socketio.annotation.SpringAnnotationScanner;
import org.springframework.beans.factory.annotation.Value;

@Configuration
@ConfigurationProperties(prefix = "application.socketio.server")
public class SocketIOConfig {


//    @Value("${socketio.server.host}")
    private String host;

//    @Value("${socketio.server.port}")
    private int port;

    @Bean
    public SocketIOServer socketIOServer(ChatEventHandler chatEventHandler) {
        com.corundumstudio.socketio.Configuration config = new com.corundumstudio.socketio.Configuration();
        config.setHostname(host);
        config.setPort(port);
        SocketIOServer socketIoServer = new SocketIOServer(config);

        socketIoServer.addConnectListener(chatEventHandler);
        socketIoServer.addDisconnectListener(chatEventHandler);
        socketIoServer.addListeners(chatEventHandler);

        return socketIoServer;
    }


    @Bean
    public SpringAnnotationScanner springAnnotationScanner(SocketIOServer socketIoServer) {
        return new SpringAnnotationScanner(socketIoServer);
    }
}


