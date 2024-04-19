package net.idonow.service.entity;

import net.idonow.entity.Profession;
import net.idonow.service.entity.templates.EntityReadService;
import net.idonow.transform.profession.ProfessionRequest;
import net.idonow.transform.profession.ProfessionUpdateRequest;

import java.util.List;

public interface ProfessionService extends EntityReadService<Profession> {

    List<Profession> getEntitiesByCategory(Long categoryId);

    Profession createProfession(ProfessionRequest professionRequest);

    Profession updateProfession(ProfessionUpdateRequest professionUpdateRequest);

    void deleteProfession(Long id);
}
