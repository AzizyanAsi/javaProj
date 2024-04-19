package net.idonow.service.entity.impl;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.idonow.common.config.LimitsConfig;
import net.idonow.common.config.StorageConfig;
import net.idonow.common.util.LogUtils.Action;
import net.idonow.controller.exception.common.ActionNotAllowedException;
import net.idonow.controller.exception.common.EntityNotFoundException;
import net.idonow.controller.mapping.ResponseMappers;
import net.idonow.entity.Professional;
import net.idonow.entity.Service;
import net.idonow.entity.User;
import net.idonow.repository.ProfessionalRepository;
import net.idonow.security.enums.RoleType;
import net.idonow.service.common.GeometryService;
import net.idonow.service.common.StorageService;
import net.idonow.service.entity.MeasurementUnitService;
import net.idonow.service.entity.ProfessionService;
import net.idonow.service.entity.ProfessionalService;
import net.idonow.service.entity.UserService;
import net.idonow.transform.geo.LocationRequest;
import net.idonow.transform.professional.ProfessionalRequest;
import net.idonow.transform.professional.ProfessionalSelfResponse;
import net.idonow.transform.professional.ProfessionalUpdateRequest;
import net.idonow.transform.professional.ProfessionalView;
import net.idonow.transform.professional.service.ServiceRequest;
import net.idonow.transform.professional.service.ServiceUpdateRequest;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static net.idonow.common.cache.EntityCacheNames.PROFESSIONAL;
import static net.idonow.common.data.NumericConstants.SCALE_MONETARY;
import static net.idonow.common.util.LogUtils.auditLog;
import static net.idonow.common.util.LogUtils.buildJSONMessage;

@Slf4j
@org.springframework.stereotype.Service
public class ProfessionalServiceImpl implements ProfessionalService {

    private final ProfessionalRepository professionalRepository;
    private final UserService userService;
    private ProfessionService professionService;
    private MeasurementUnitService measurementUnitService;
    private GeometryService geometryService;
    private StorageService storageService;
    private StorageConfig storageConfig;
    private LimitsConfig limitsConfig;
    private ResponseMappers responseMappers;

    public ProfessionalServiceImpl(ProfessionalRepository professionalRepository, UserService userService) {
        this.professionalRepository = professionalRepository;
        this.userService = userService;
    }

    @Autowired
    public void setProfessionService(ProfessionService professionService) {
        this.professionService = professionService;
    }

    @Autowired
    public void setMeasurementUnitService(MeasurementUnitService measurementUnitService) {
        this.measurementUnitService = measurementUnitService;
    }

    @Autowired
    public void setGeometryService(GeometryService geometryService) {
        this.geometryService = geometryService;
    }

    @Autowired
    public void setStorageService(StorageService storageService) {
        this.storageService = storageService;
    }

    @Autowired
    public void setStorageConfig(StorageConfig storageConfig) {
        this.storageConfig = storageConfig;
    }

    @Autowired
    public void setLimitsConfig(LimitsConfig limitsConfig) {
        this.limitsConfig = limitsConfig;
    }

    @Autowired
    public void setResponseMappers(ResponseMappers responseMappers) {
        this.responseMappers = responseMappers;
    }

    @Override
    public List<Professional> getAllEntities() {
        return professionalRepository.findAll(); // TODO use pager
    }

    @Override
    public Professional getEntity(Long id) {
        Optional<Professional> optProfessional = professionalRepository.findById(id);
        if (optProfessional.isEmpty()) {
            throw new EntityNotFoundException(String.format("Professional with id {%s} not found", id));
        }
        return optProfessional.get();
    }

    @Override
    public Professional getActiveProfessional(Long id) {
        Optional<Professional> optProfessional = professionalRepository.findProfessionalByIdAndActiveIsTrue(id);
        if (optProfessional.isEmpty()) {
            throw new EntityNotFoundException(String.format("Professional with id {%s} not found", id));
        }
        return optProfessional.get();
    }

    @Override
    public Set<ProfessionalView> findNearestProfessionals(Long professionId, Double distance, Double latitude, Double longitude) {
        return professionalRepository.findNearestProfessionals(professionId, distance, latitude, longitude);
    }

    @Override
    public boolean professionalExists(String username) {
        User user = userService.getActiveUserByEmailCached(username);
        return professionalRepository.existsProfessionalById(user.getId());
    }

    @Override
    @Cacheable(value = PROFESSIONAL, key = "#username.toLowerCase()", unless = "#result == null")
    public Professional getProfessionalByUsername(String username) {
        Optional<Professional> optProfessional = professionalRepository.findProfessionalByUser_Email(username);
        if (optProfessional.isEmpty()) {
            throw new EntityNotFoundException(String.format("Professional with username '%s' not found", username));
        }
        return optProfessional.get();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public ProfessionalSelfResponse registerProfessional(Principal principal, ProfessionalRequest professionalRequest,
                                                     HttpServletRequest request, HttpServletResponse response) {
        User user = userService.getUserByEmail(principal.getName());
        if (professionalRepository.existsProfessionalById(user.getId())) {
            throw new ActionNotAllowedException("Professional account already exists");
        }
        Professional professional = new Professional();
        professional.setAddress(professionalRequest.getAddress());
        professional.setLocation(geometryService.createPoint(professionalRequest.getLocation()));
        professional.setActive(true);
        professional.setBalance(BigDecimal.valueOf(0, SCALE_MONETARY));
        professional.setWorkStartTime(LocalTime.of(9,0,0));
        professional.setWorkEndTime(LocalTime.of(18, 0, 0));
        professional.setWorkingInWeekend(false);
        professional.setUser(user);

        professionalRepository.save(professional);
        userService.updateRole(user, RoleType.PROFESSIONAL, request, response);

        ProfessionalSelfResponse professionalSelfResponse = responseMappers.professionalToSelfResponse(professional);
        auditLog.info(buildJSONMessage(Action.CREATE, professionalSelfResponse));
        log.info("Professional successfully registered: " + professional);

        return professionalSelfResponse;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    @CacheEvict(value = PROFESSIONAL, key = "#principal.name")
    public void transformToClient(Principal principal, HttpServletRequest request, HttpServletResponse response) {
        // TODO - reject transforming if there is pending order
        User user = userService.getUserByEmail(principal.getName());
        Professional professional = getProfessionalByUsername(principal.getName());
        professional.setActive(false);
        userService.updateRole(user, RoleType.CLIENT, request, response);
        log.info("Professional successfully transformed to Client: {}", user);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    @CacheEvict(value = PROFESSIONAL, key = "#principal.name")
    public void transformToProfessional(Principal principal, HttpServletRequest request, HttpServletResponse response) {
        // TODO - reject transforming if there is pending order
        User user = userService.getUserByEmail(principal.getName());
        Professional professional = getProfessionalByUsername(principal.getName());
        professional.setActive(true);
        userService.updateRole(user, RoleType.PROFESSIONAL, request, response);
        log.info("Client successfully transformed to Professional: {}", user);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    @CacheEvict(value = PROFESSIONAL, key = "#principal.name")
    public Professional addService(Principal principal, ServiceRequest serviceRequest) {
        Professional professional = getProfessionalByUsername(principal.getName());

        // Get services and check existence of the profession
        Set<Service> services = professional.getServices();
        boolean exists = services.stream().anyMatch(s -> s.getProfession().getId().equals(serviceRequest.getProfessionId()));
        if (exists) {
            throw new ActionNotAllowedException("Service with provided profession already exists");
        } else if (services.size() >= limitsConfig.getMaxServiceCount()) {
            throw new ActionNotAllowedException("Services max limit exceeded");
        }

        Service service = new Service();
        service.setPrice(serviceRequest.getPrice());
        service.setProfession(professionService.getEntity(serviceRequest.getProfessionId()));
        service.setMeasurementUnit(measurementUnitService.getEntity(serviceRequest.getMeasurementUnitId()));
        services.add(service);
        log.info("Service '{}' successfully added to professional: '{}'", service.getProfession(), professional);
        auditLog.info(buildJSONMessage(Action.CREATE, responseMappers.serviceToResponse(service)));
        return professional;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    @CacheEvict(value = PROFESSIONAL, key = "#principal.name")
    public Professional updateService(Principal principal, ServiceUpdateRequest serviceUpdateRequest) {
        Professional professional = getProfessionalByUsername(principal.getName());

        // Get service by profession
        Service service = professional.getServices().stream()
                .filter(s -> s.getProfession().getId().equals(serviceUpdateRequest.getProfessionId()))
                .findFirst()
                .orElseThrow(() -> new ActionNotAllowedException("Service not found"));
        service.setPrice(serviceUpdateRequest.getPrice());
        service.setMeasurementUnit(measurementUnitService.getEntity(serviceUpdateRequest.getMeasurementUnitId()));
        log.info("Service successfully updated: {}", service);
        auditLog.info(buildJSONMessage(Action.UPDATE, responseMappers.serviceToResponse(service)));
        return professional;
    }

    @Override
    @CacheEvict(value = PROFESSIONAL, key = "#principal.name")
    public Professional updateProfessional(Principal principal, ProfessionalUpdateRequest professionalUpdateRequest) {
        if (!professionalUpdateRequest.getWorkEndTime().isAfter(professionalUpdateRequest.getWorkStartTime())) {
            throw new ActionNotAllowedException("Work end time must be after work start time");
        }
        Professional professional = getProfessionalByUsername(principal.getName());
        professional.setSelfDescription(professionalUpdateRequest.getSelfDescription());
        professional.setAddress(professionalUpdateRequest.getAddress());
        professional.setWorkStartTime(professionalUpdateRequest.getWorkStartTime());
        professional.setWorkEndTime(professionalUpdateRequest.getWorkEndTime());
        professional.setWorkingInWeekend(professionalUpdateRequest.isWorkingInWeekend());
        professionalRepository.save(professional);
        log.info("Professional successfully updated: {}", professional);
        return professional;
    }

    @Override
    public Professional updateProfessionalById(Long id, ProfessionalUpdateRequest professionalUpdateRequest) {
        if (!professionalUpdateRequest.getWorkEndTime().isAfter(professionalUpdateRequest.getWorkStartTime())) {
            throw new ActionNotAllowedException("Work end time must be after work start time");
        }
        Professional professional = getEntity(id);
        professional.setSelfDescription(professionalUpdateRequest.getSelfDescription());
        professional.setAddress(professionalUpdateRequest.getAddress());
        professional.setWorkStartTime(professionalUpdateRequest.getWorkStartTime());
        professional.setWorkEndTime(professionalUpdateRequest.getWorkEndTime());
        professional.setWorkingInWeekend(professionalUpdateRequest.isWorkingInWeekend());
        professionalRepository.save(professional);
        log.info("Professional successfully updated: {}", professional);
        return professional;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    @CacheEvict(value = PROFESSIONAL, key = "#principal.name")
    public Point updateLocation(Principal principal, LocationRequest locationRequest) {
        Professional professional = getProfessionalByUsername(principal.getName());
        professional.setLocation(geometryService.createPoint(locationRequest));
        return professional.getLocation();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    @CacheEvict(value = PROFESSIONAL, key = "#principal.name")
    public Professional uploadWorkingSamples(Principal principal, List<MultipartFile> images) throws IOException {
        Professional professional = getProfessionalByUsername(principal.getName());

        Set<String> workingSamples = professional.getWorkingSamples();
        // Max size is 20
        if (workingSamples.size() + images.size() > 10) {
            throw new ActionNotAllowedException("Max size limit is exceeded");
        }
        List<String> imageNames = storageService.createNewImageNamesForOrderedSequence(images, workingSamples);
        workingSamples.addAll(imageNames);
        professionalRepository.flush();

        // Upload files to cloud
        for (int i = 0; i < images.size(); i++) {
            storageService.uploadImage(images.get(i), storageConfig.getWorkingSampleDirectory(), imageNames.get(i), false);
            storageService.uploadImage(images.get(i), storageConfig.getWorkingSamplesThumbnailDirectory(), imageNames.get(i), true);
        }

        log.info("Working samples gallery successfully updated: {}", professional);
        return professional;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    @CacheEvict(value = PROFESSIONAL, key = "#principal.name")
    public String uploadResume(Principal principal, MultipartFile resume) throws IOException {
        Professional professional = getProfessionalByUsername(principal.getName());

        if (professional.getResumeName() != null) {
            throw new ActionNotAllowedException("Resume already exists");
        }
        String resumeName = storageService.createFileName(resume);
        professional.setResumeName(resumeName);
        professionalRepository.flush();
        return storageService.uploadDocument(resume, storageConfig.getResumeDirectory(), resumeName);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    @CacheEvict(value = PROFESSIONAL, key = "#principal.name")
    public void deleteService(Principal principal, Long professionId) {
        Professional professional = getProfessionalByUsername(principal.getName());

        Set<Service> services = professional.getServices();
        boolean deleted = services.removeIf(s -> s.getProfession().getId().equals(professionId));
        if (!deleted) {
            throw new ActionNotAllowedException("Service does not found");
        } else {
            log.info("Service with profession 'id={}' successfully deleted", professionId);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    @CacheEvict(value = PROFESSIONAL, key = "#principal.name")
    public void deleteWorkingSample(Principal principal, String imageUrl) {
        Professional professional = getProfessionalByUsername(principal.getName());

        // Get image name from image url
        String fileName = imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
        if (!professional.getWorkingSamples().contains(fileName)) {
            throw new ActionNotAllowedException("Requested project image not found", HttpStatus.NOT_FOUND);
        }
        professional.getWorkingSamples().remove(fileName);
        professionalRepository.flush();

        storageService.deleteObject(storageConfig.getWorkingSampleDirectory(), fileName);
        storageService.deleteObject(storageConfig.getWorkingSamplesThumbnailDirectory(), fileName);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    @CacheEvict(value = PROFESSIONAL, key = "#principal.name")
    public void deleteResume(Principal principal) {
        Professional professional = getProfessionalByUsername(principal.getName());

        if (professional.getResumeName() == null) {
            throw new ActionNotAllowedException("Resume is not found");
        }
        String fileToDelete = professional.getResumeName();
        professional.setResumeName(null);
        professionalRepository.flush();
        storageService.deleteObject(storageConfig.getResumeDirectory(), fileToDelete);
    }

    @Override
    public Page<Professional> getListOfProfessionals(String firstName, String email, Boolean active, Pageable pageable) {
        String sanitizedFirstName = (firstName != null) ? firstName : "";
        String sanitizedEmail = (email != null) ? email : "";
        Boolean sanitizedActive = (active != null) ? active : true;

        return professionalRepository.findProfessionalsWithFilters(sanitizedActive,sanitizedFirstName, sanitizedEmail,pageable);
//        return professionalRepository.findProfessionalsWithFilters(sanitizedActive,sanitizedFirstName, sanitizedEmail,pageable);
//        return professionalRepository.findAllByUserEmail(sanitizedEmail,pageable);
    }




}


