package net.idonow.service.entity;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import net.idonow.entity.Professional;
import net.idonow.service.entity.templates.EntityReadService;
import net.idonow.transform.geo.LocationRequest;
import net.idonow.transform.professional.ProfessionalRequest;
import net.idonow.transform.professional.ProfessionalSelfResponse;
import net.idonow.transform.professional.ProfessionalUpdateRequest;
import net.idonow.transform.professional.ProfessionalView;
import net.idonow.transform.professional.service.ServiceRequest;
import net.idonow.transform.professional.service.ServiceUpdateRequest;
import org.locationtech.jts.geom.Point;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Set;

public interface ProfessionalService extends EntityReadService<Professional> {
    Professional getActiveProfessional(Long id);
    Set<ProfessionalView> findNearestProfessionals(Long professionId, Double distance, Double latitude, Double longitude);
    boolean professionalExists(String username);
    Professional getProfessionalByUsername(String username);
    ProfessionalSelfResponse registerProfessional(Principal principal, ProfessionalRequest professionalRequest, HttpServletRequest request, HttpServletResponse response);
    void transformToClient(Principal principal, HttpServletRequest request, HttpServletResponse response);
    void transformToProfessional(Principal principal, HttpServletRequest request, HttpServletResponse response);
    Professional addService(Principal principal, ServiceRequest serviceRequest);
    Professional updateService(Principal principal, ServiceUpdateRequest serviceUpdateRequest);
    Professional updateProfessional(Principal principal, ProfessionalUpdateRequest professionalUpdateRequest);
    Point updateLocation(Principal principal, LocationRequest locationRequest);
    Professional uploadWorkingSamples(Principal principal, List<MultipartFile> images) throws IOException;
    String uploadResume(Principal principal, MultipartFile resume) throws IOException;
    void deleteService(Principal principal, Long professionId);
    void deleteWorkingSample(Principal principal, String imageUrl);
    void deleteResume(Principal principal);

    Page<Professional> getListOfProfessionals(String firstName, String email, Boolean active, Pageable pageable);

    Professional updateProfessionalById(Long id, ProfessionalUpdateRequest professionalUpdateRequest);
}
