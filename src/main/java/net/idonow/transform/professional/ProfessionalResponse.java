package net.idonow.transform.professional;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.idonow.transform.file.ImageResponse;
import net.idonow.transform.professional.service.ServiceResponse;
import org.locationtech.jts.geom.Point;

import java.time.LocalTime;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProfessionalResponse implements ProfessionalView {
    private Long id;
    private Point location;
    private String firstName;
    private String lastName;
    private String email;
    private String active;
    private String profilePictureUrl;
    private String coverPictureUrl;
    private String selfDescription;
    private String address;
    private String resumeUrl;
    private Set<ImageResponse> workingSampleUrls;
    private LocalTime workStartTime;
    private LocalTime workEndTime;
    private boolean workingInWeekend;
    private Set<ServiceResponse> services;
}
