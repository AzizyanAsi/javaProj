package net.idonow.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import net.idonow.common.api.ApiResponse;
import net.idonow.common.util.LocaleUtils;
import net.idonow.common.validation.constraints.ValidMultipartFile;
import net.idonow.controller.mapping.ResponseMappers;
import net.idonow.entity.Professional;
import net.idonow.service.entity.ProfessionalService;
import net.idonow.transform.geo.LocationRequest;
import net.idonow.transform.professional.*;
import net.idonow.transform.professional.service.ServiceRequest;
import net.idonow.transform.professional.service.ServiceUpdateRequest;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.Set;

@Validated
@RestController
@RequestMapping("/api/professionals")
public class ProfessionalController {

    private final ProfessionalService professionalService;
    private LocaleUtils localeUtils;
    private ResponseMappers responseMappers;

    public ProfessionalController(ProfessionalService professionalService) {
        this.professionalService = professionalService;
    }

    @Autowired
    public void setLocaleUtils(LocaleUtils localeUtils) {
        this.localeUtils = localeUtils;
    }

    @Autowired
    public void setResponseMappers(ResponseMappers responseMappers) {
        this.responseMappers = responseMappers;
    }

    @GetMapping
    @PreAuthorize("hasRole('CLIENT')")
    public ApiResponse<Set<ProfessionalView>> getServiceProvidersByDistance(@RequestParam("distance") @Positive(message = "{validation.positive.real}") Double distance,
                                                                            @RequestParam("professionId") @Positive(message = "{validation.positive}") Long professionId,
                                                                            @RequestParam("latitude") @DecimalMin(value = "-90.0") @DecimalMax(value = "90.0") Double latitude,
                                                                            @RequestParam("longitude") @DecimalMin(value = "-180.0") @DecimalMax(value = "180.0") Double longitude) {
        Set<ProfessionalView> professionals = professionalService.findNearestProfessionals(professionId, distance, latitude, longitude);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.entities.found"), professionals);
    }

    @GetMapping("{id}")
    @PreAuthorize("hasRole('CLIENT')")
    public ApiResponse<ProfessionalResponse> getProfessional(@PathVariable("id") @Positive(message = "{validation.positive}") Long id) {
        Professional professional = professionalService.getActiveProfessional(id);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.entity.found"), responseMappers.professionalToResponse(professional));
    }

    @GetMapping("self-info")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ApiResponse<ProfessionalSelfResponse> getSelfInfo(Principal principal) {
        Professional professional = professionalService.getProfessionalByUsername(principal.getName());
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.entity.found"), responseMappers.professionalToSelfResponse(professional));
    }

    // Mapping to check if client has already registered as professional
    @PostMapping("check")
    @PreAuthorize("hasRole('CLIENT')")
    public ResponseEntity<Void> professionalAlreadyExists(Principal principal) {
        boolean exists = professionalService.professionalExists(principal.getName());
        return exists ? ApiResponse.ok().build() : ApiResponse.badRequest().build();
    }

    // Mapping to register as professional
    @PostMapping("register")
    @PreAuthorize("hasRole('CLIENT')")
    public ApiResponse<ProfessionalSelfResponse> registerProfessional(@RequestBody @Valid ProfessionalRequest professionalRequest,
                                                                  HttpServletRequest request, HttpServletResponse response,
                                                                  Principal principal) {
        ProfessionalSelfResponse professionalSelfResponse = professionalService.registerProfessional(principal, professionalRequest, request, response);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.professional.register"), professionalSelfResponse);
    }

    // Mapping for transforming to client
    @PostMapping("transform-to-client")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ApiResponse<String> transformToClient(HttpServletRequest request, HttpServletResponse response, Principal principal) {
        professionalService.transformToClient(principal, request, response);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.user.transform"));
    }

    // Mapping for transforming to professional
    @PostMapping("transform-to-professional")
    @PreAuthorize("hasRole('CLIENT')")
    public ApiResponse<String> transformToProfessional(HttpServletRequest request, HttpServletResponse response, Principal principal) {
        professionalService.transformToProfessional(principal, request, response);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.user.transform"));
    }

    // Mapping to add service
    @PostMapping("services")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ApiResponse<ProfessionalSelfResponse> addService(@RequestBody @Valid ServiceRequest serviceRequest, Principal principal) {
        Professional professional = professionalService.addService(principal, serviceRequest);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.service.add"), responseMappers.professionalToSelfResponse(professional));
    }

    @PostMapping(value = "working-samples", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ApiResponse<ProfessionalSelfResponse> uploadWorkingSamples(
            Principal principal,
            @RequestPart(value = "images") @NotEmpty(message = "{validation.empty}") List<@ValidMultipartFile(message = "{validation.multipart-file}") MultipartFile> images
    ) throws IOException {
        Professional professional = professionalService.uploadWorkingSamples(principal, images);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.image.upload"), responseMappers.professionalToSelfResponse(professional));
    }

    @PostMapping(value = "resume", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ApiResponse<String> uploadResume(
            @RequestPart("resume") @ValidMultipartFile(message = "{validation.multipart-file}") MultipartFile resume,
            Principal principal) throws IOException {
        String resumeUrl = professionalService.uploadResume(principal, resume);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.document.add"), resumeUrl);
    }

    // Mapping to update service
    @PutMapping("services")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ApiResponse<ProfessionalSelfResponse> updateService(@RequestBody @Valid ServiceUpdateRequest serviceUpdateRequest, Principal principal) {
        Professional professional = professionalService.updateService(principal, serviceUpdateRequest);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.data.update"), responseMappers.professionalToSelfResponse(professional));
    }

    // Mapping to update self info
    @PutMapping
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ApiResponse<ProfessionalSelfResponse> updateProfessional(@RequestBody @Valid ProfessionalUpdateRequest professionalUpdateRequest, Principal principal) {
        Professional professional = professionalService.updateProfessional(principal, professionalUpdateRequest);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.data.update"), responseMappers.professionalToSelfResponse(professional));
    }

    @PutMapping("location")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ApiResponse<Point> updateLocation(@RequestBody @Valid LocationRequest locationRequest, Principal principal) {
        Point location = professionalService.updateLocation(principal, locationRequest);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.data.update"), location);
    }

    @DeleteMapping("working-samples")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ApiResponse<Void> deleteWorkingSample(
            Principal principal,
            @RequestParam("imageUrl") @NotEmpty(message = "{validation.empty}") String imageUrl
    ) {
        professionalService.deleteWorkingSample(principal, imageUrl);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.image.delete"));
    }

    @DeleteMapping("resume")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ApiResponse<Void> deleteResume(Principal principal) {
        professionalService.deleteResume(principal);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.document.delete"));
    }

    @DeleteMapping("services/{professionId}")
    @PreAuthorize("hasRole('PROFESSIONAL')")
    public ApiResponse<Void> deleteService(@PathVariable("professionId") @Positive(message = "{validation.positive}") Long professionId, Principal principal) {
        professionalService.deleteService(principal, professionId);
        return ApiResponse.ok(localeUtils.getLocalizedMessage("success.service.delete"));
    }
}
