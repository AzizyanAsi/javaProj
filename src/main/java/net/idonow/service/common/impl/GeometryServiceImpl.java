package net.idonow.service.common.impl;

import lombok.extern.slf4j.Slf4j;
import net.idonow.service.common.GeometryService;
import net.idonow.transform.geo.LocationRequest;
import org.geolatte.geom.jts.JTS;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GeometryServiceImpl implements GeometryService {

    private final static GeometryFactory geometryFactory;

    static {
        geometryFactory = new GeometryFactory();
    }

    @Override
    public Point createPoint(LocationRequest locationRequest) {
        Point point = geometryFactory.createPoint(new Coordinate(locationRequest.getLongitude(), locationRequest.getLatitude()));
        point.setSRID(4326);
        return point;
    }

    // Hibernate uses Geolatte-geom to decode the database native types, so the method is used to convert Geolatte-geom to Jts
    public Point convertGeolatteToJts(org.geolatte.geom.Point<?> point) {
        return JTS.to(point);
    }
}
