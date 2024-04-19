package net.idonow.transform.system.systemuser.converter;

import net.idonow.entity.system.SystemUser;
import net.idonow.transform.system.systemuser.SystemUserResponse;

public interface ISystemUserConverter {
    SystemUser convertToEntityUpdateReq(SystemUserUpdateRequest userRequest);
    SystemUserResponse systemUserToResponse(SystemUser systemUser);
}
