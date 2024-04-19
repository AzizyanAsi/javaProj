package net.idonow.transform.system.systemuser.converter;

import lombok.RequiredArgsConstructor;
import net.idonow.controller.mapping.ResponseMappers;
import net.idonow.entity.system.SystemUser;
import net.idonow.transform.system.systemuser.SystemUserResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SystemUserConverter implements ISystemUserConverter {

    private final ResponseMappers responseMappers;
    @Override
    public SystemUser convertToEntityUpdateReq(SystemUserUpdateRequest request) {
        if (request == null) {
            return null;
        }
        SystemUser userEntity = new SystemUser();
        userEntity.setFirstName(request.getFirstName());
        userEntity.setLastName(request.getLastName());
        userEntity.setEmail(request.getEmail());
        userEntity.setPhoneNumber(request.getPhoneNumber());
        userEntity.setActive(request.getActive());
        return userEntity;
    }

    @Override
    public SystemUserResponse systemUserToResponse(SystemUser systemUser) {
        if ( systemUser == null ) {
            return null;
        }

        SystemUserResponse systemUserResponse = new SystemUserResponse();

        systemUserResponse.setId( systemUser.getId() );
        systemUserResponse.setFirstName( systemUser.getFirstName() );
        systemUserResponse.setLastName( systemUser.getLastName() );
        systemUserResponse.setEmail( systemUser.getEmail() );
        systemUserResponse.setPhoneNumber( systemUser.getPhoneNumber() );
        systemUserResponse.setActive( systemUser.getActive() );
        systemUserResponse.setRole( responseMappers.roleToResponse( systemUser.getRole() ) );
        systemUserResponse.setCreated( systemUser.getCreated() );
        systemUserResponse.setUpdated( systemUser.getUpdated() );

        return systemUserResponse;
    }
}
