package net.idonow.service.entity.impl.system.chat;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.idonow.security.enums.RoleType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    private Long senderId;  // The ID of the sender (either app user or system user)
    private String senderName;  // The name of the sender
    private String content;  // The content of the message
    private RoleType userType; // Enum to represent UserType (USER, SYSTEM_USER)

}
