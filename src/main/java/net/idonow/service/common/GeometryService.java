package net.idonow.service.common;

import net.idonow.transform.geo.LocationRequest;
import org.locationtech.jts.geom.Point;

public interface GeometryService {
    Point createPoint(LocationRequest locationRequest);
}
