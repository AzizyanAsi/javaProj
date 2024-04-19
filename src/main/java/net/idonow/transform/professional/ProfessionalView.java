package net.idonow.transform.professional;

import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Value;

public interface ProfessionalView {
    Long getId();
    @Value("#{@geometryServiceImpl.convertGeolatteToJts(target.location)}")
    Point getLocation();
    String getFirstName();
    String getLastName();
    @Value("#{@storageConfig.getUrlTemplate() + @storageConfig.getProfilePictureDirectory() + target.profilePictureName}") // todo Correct for NULL case
    String getProfilePictureUrl();
}
