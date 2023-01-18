package com.example.nmea.test;

import net.sf.marineapi.nmea.event.SentenceEvent;
import net.sf.marineapi.nmea.event.SentenceListener;
import net.sf.marineapi.nmea.parser.DataNotAvailableException;
import net.sf.marineapi.nmea.sentence.*;
import net.sf.marineapi.nmea.util.Position;
import org.geotools.data.DataUtilities;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import java.util.ArrayList;
import java.util.List;

public class VTGGGAListener implements SentenceListener {
    private Position lastPosition;
    private double totalDistance;
    private List<SimpleFeature> features;
    private GeometryFactory geometryFactory;
    private SimpleFeatureBuilder featureBuilder;
    private boolean isReading;
    private boolean isStarted;

    public VTGGGAListener() {
        isReading = false;
        isStarted = false;
        totalDistance = 0;
        features = new ArrayList<>();

        geometryFactory = JTSFactoryFinder.getGeometryFactory();
        final SimpleFeatureType TYPE;
        try {
            TYPE = DataUtilities.createType(
                    "Location",
                    "the_geom:Point:srid=4326,"
            );
        } catch (SchemaException e) {
            throw new RuntimeException(e);
        }
        featureBuilder = new SimpleFeatureBuilder(TYPE);
    }

    @Override
    public void readingPaused() {
        isReading = false;
    }
    @Override
    public void readingStarted() {
        isReading = true;
        isStarted = true;
    }
    @Override
    public void readingStopped() {
        isReading = false;
        isStarted = false;
    }
    @Override
    public void sentenceRead(SentenceEvent event) {
        Sentence s = event.getSentence();
        if (!s.getSentenceId().equals("GGA")) {
            return;
        }
        GGASentence gga = (GGASentence) s;
        Position currentPosition;
        try {
            currentPosition = gga.getPosition();
        } catch (DataNotAvailableException e) {
            return;
        }
        if (lastPosition != null) {
            totalDistance += currentPosition.distanceTo(lastPosition);
        }
        lastPosition = currentPosition;

        Point point = geometryFactory.createPoint(new Coordinate(currentPosition.getLongitude(), currentPosition.getLatitude()));
        featureBuilder.add(point);
        features.add(featureBuilder.buildFeature(null));
    }
    public double getTotalDistance() throws IllegalAccessException {
        if (!isReading) {
            return totalDistance;
        }
        throw new IllegalAccessException();
    }
    public List<SimpleFeature> getFeatures() throws IllegalAccessException {
        if (!isReading) {
            return features;
        }
        throw new IllegalAccessException();
    }
    public boolean isStarted() {
        return isStarted;
    }
    public boolean isReading() {
        return isReading;
    }
}
