package net.idonow.service.entity;

import net.idonow.entity.Country;
import net.idonow.service.entity.templates.EntityReadService;

public interface CountryService extends EntityReadService<Country> {
    Country getByCode(String code);
}
