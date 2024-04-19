package net.idonow.controller.mapping;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.idonow.common.config.StorageConfig;
import net.idonow.entity.*;
import net.idonow.entity.system.SystemUser;
import net.idonow.transform.country.CountryConfigResponse;
import net.idonow.transform.country.CountryResponse;
import net.idonow.transform.file.ImageResponse;
import net.idonow.transform.profession.ProfessionResponse;
import net.idonow.transform.profession.category.ProfessionCategoryBriefResponse;
import net.idonow.transform.professional.ProfessionalResponse;
import net.idonow.transform.professional.ProfessionalSelfResponse;
import net.idonow.transform.professional.service.ServiceResponse;
import net.idonow.transform.role.RoleResponse;
import net.idonow.transform.system.systemuser.SystemUserResponse;
import net.idonow.transform.user.UserResponse;
import net.idonow.transform.user.UserSelfResponse;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class ResponseMappers {

    private StorageConfig storageConfig;

    @Autowired
    public void setStorageConfig(StorageConfig storageConfig) {
        this.storageConfig = storageConfig;
    }

    @Mapping(target = "phoneNumberData", ignore = true)
    public abstract CountryConfigResponse countryToConfigResponse(Country country);

    @Mapping(target = "currencyId", expression = "java(country.getCurrency().getId())")
    public abstract CountryResponse countryToResponse(Country country);

    public abstract ProfessionCategoryBriefResponse professionCategoryToBriefResponse(ProfessionCategory professionCategory);

    @Mapping(target = "categoryId", expression = "java(profession.getProfessionCategory().getId())")
    public abstract ProfessionResponse professionToResponse(Profession profession);

    @Mapping(target = "firstName", expression = "java(professional.getUser().getFirstName())")
    @Mapping(target = "lastName", expression = "java(professional.getUser().getLastName())")
    @Mapping(target = "email", expression = "java(professional.getUser().getEmail())")
    @Mapping(target = "active", expression = "java(String.valueOf(professional.isActive()))")
    @Mapping(target = "profilePictureUrl", expression = "java(setUserProfilePictureUrl(professional.getUser()))")
    @Mapping(target = "coverPictureUrl", expression = "java(setUserCoverPictureUrl(professional.getUser()))")
    @Mapping(target = "resumeUrl", expression = "java(setProfessionalResumeUrl(professional))")
    @Mapping(target = "workingSampleUrls", expression = "java(setProfessionalWorkingSampleUrls(professional))")
    public abstract ProfessionalResponse professionalToResponse(Professional professional);

    @InheritConfiguration
    public abstract ProfessionalSelfResponse professionalToSelfResponse(Professional professional);

    @Mapping(target = "professionId", expression = "java(service.getProfession().getId())")
    @Mapping(target = "measurementUnitId", expression = "java(service.getMeasurementUnit().getId())")
    public abstract ServiceResponse serviceToResponse(Service service);

    public abstract RoleResponse roleToResponse(Role role);

    @Mapping(target = "countryId", expression = "java(user.getCountry().getId())")
    public abstract UserResponse userToResponse(User user);

    @InheritConfiguration
    public abstract SystemUserResponse systemUserToResponse(SystemUser systemUser);

    @SuppressWarnings("UnmappedTargetProperties")
    @InheritConfiguration
    @Mapping(target = "profilePictureUrl", expression = "java(setUserProfilePictureUrl(user))")
    @Mapping(target = "coverPictureUrl", expression = "java(setUserCoverPictureUrl(user))")
    public abstract UserSelfResponse userToSelfResponse(User user);

    /* -- HELPERS and CONVERTERS -- */

    protected String setUserProfilePictureUrl(User user) {
        if (user.getProfilePictureName() != null) {
            return storageConfig.getUrlTemplate() + storageConfig.getProfilePictureDirectory() + user.getProfilePictureName();
        }
        return null;
    }

    protected String setUserCoverPictureUrl(User user) {
        if (user.getCoverPictureName() != null) {
            return storageConfig.getUrlTemplate() + storageConfig.getProfilePictureDirectory() + user.getCoverPictureName();
        }
        return null;
    }

    protected String setProfessionalResumeUrl(Professional professional) {
        if (professional.getResumeName() != null) {
            return storageConfig.getUrlTemplate() + storageConfig.getResumeDirectory() + professional.getResumeName();
        }
        return null;
    }

    protected Set<ImageResponse> setProfessionalWorkingSampleUrls(Professional professional) {
        if (professional.getWorkingSamples() != null) {
            return professional.getWorkingSamples().stream()
                    .map(f -> new ImageResponse(
                            storageConfig.getUrlTemplate() + storageConfig.getWorkingSampleDirectory() + f,
                            storageConfig.getUrlTemplate() + storageConfig.getWorkingSamplesThumbnailDirectory() + f,
                            getOrdinalFromName(f)))
                    .collect(Collectors.toSet());
        }
        return null;

    }

    private Integer getOrdinalFromName(String imageName) {
        String index = imageName.substring(imageName.lastIndexOf("_") + 1, imageName.lastIndexOf("."));
        return Integer.parseInt(index);
    }



    /* -- CUSTOM MAPPERS -- */

    public List<ProfessionCategoryTree> professionCategoryListToResponseTree(List<ProfessionCategory> categoryList) {

        List<ProfessionCategoryTree> rootCategories = new LinkedList<>();

        // Loop over categories (list is filtering dynamically by all levels)
        for (ProfessionCategory cat : new LinkedList<>(categoryList)) {

            if (cat.getParent() == null) {

                // Remove root category from categoryList
                categoryList.remove(cat);

                // Recursively get the branch of the root category (filtering categoryList)
                List<ProfessionCategoryTree> children = getCategoryChildren(cat, categoryList);

                ProfessionCategoryTree rootNode = categoryToResponse(cat);
                rootNode.setChildren(children);     // children must be already mapped

                rootCategories.add(rootNode);
            }
        }
        rootCategories.sort(Comparator.comparing(c -> c.getCategory().getCategoryName()));
        return rootCategories;
    }

    /* -- PRIVATE METHODS -- */

    private List<ProfessionCategoryTree> getCategoryChildren(ProfessionCategory node, List<ProfessionCategory> categoryList) {
        List<ProfessionCategory> childCategories = new LinkedList<>();

        // Get child categories
        for (ProfessionCategory cat : new LinkedList<>(categoryList)) {
            if (cat.getParent() != null && cat.getParent().equals(node)) {
                childCategories.add(cat);
                categoryList.remove(cat);
            }
        }

        // If no children - return null
        if (childCategories.isEmpty()) {
            return null;
        }

        // Else create a list of child categories and get their children recursively
        else {

            // In case of more than one child - sort by category name (String)
            if (childCategories.size() > 1) {
                childCategories.sort(Comparator.comparing(ProfessionCategory::getCategoryName));
            }

            List<ProfessionCategoryTree> children = new LinkedList<>();

            for (ProfessionCategory cat : childCategories) {
                // Get children RECURSIVELY
                List<ProfessionCategoryTree> recursiveChildren = getCategoryChildren(cat, categoryList);

                ProfessionCategoryTree child = categoryToResponse(cat);
                child.setChildren(recursiveChildren);
                children.add(child);
            }

            return children;
        }
    }

    // Children list is omitted - mapping is considered to be applied by recursion
    private ProfessionCategoryTree categoryToResponse(ProfessionCategory cat) {

        ProfessionCategoryTree node = new ProfessionCategoryTree();

        node.setCategory(professionCategoryToBriefResponse(cat));

        return node;
    }

    /* -- NESTED CLASSES -- */

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProfessionCategoryTree {

        private ProfessionCategoryBriefResponse category;
        private List<ProfessionCategoryTree> children;
    }

}
