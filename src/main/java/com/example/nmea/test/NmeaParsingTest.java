package com.example.nmea.test;

import net.sf.marineapi.nmea.io.SentenceReader;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileDataStoreFactory;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.SchemaException;
import org.opengis.feature.simple.SimpleFeatureType;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

@Service
public class NmeaParsingTest {
    static SentenceReader reader;

    public  double fileParsing() throws InterruptedException, SchemaException, IllegalAccessException {
        try {
            reader = new SentenceReader(new FileInputStream("nmea.log"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        VTGGGAListener listener = new VTGGGAListener();
        reader.addSentenceListener(listener);
        reader.setPauseTimeout(500);
        reader.start();

        while (!listener.isStarted()) {
            Thread.sleep(500);
        }
        while (listener.isReading()) {
            Thread.sleep(500);
        }
        double distance;
        try {
            distance = listener.getTotalDistance();
            System.out.println(distance);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
        reader.stop();

        //GeoTools
        File newFile = new File("path.shp");

        final SimpleFeatureType TYPE = DataUtilities.createType(
                "Location",
                "the_geom:Point:srid=4326,"
        );

        ShapefileDataStoreFactory dataStoreFactory = new ShapefileDataStoreFactory();

        Map<String, Serializable> params = new HashMap<>();
        try {
            params.put("url", newFile.toURI().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        params.put("create spatial index", Boolean.TRUE);

        ShapefileDataStore newDataStore;
        try {
            newDataStore = (ShapefileDataStore) dataStoreFactory.createNewDataStore(params);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            newDataStore.createSchema(TYPE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Transaction transaction = new DefaultTransaction("create");

        String typeName;
        try {
            typeName = newDataStore.getTypeNames()[0];
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        SimpleFeatureSource featureSource;
        try {
            featureSource = newDataStore.getFeatureSource(typeName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        SimpleFeatureType SHAPE_TYPE = featureSource.getSchema();

        System.out.println("SHAPE:" + SHAPE_TYPE);

        if (featureSource instanceof SimpleFeatureStore featureStore) {


            SimpleFeatureCollection collection = new ListFeatureCollection(TYPE, listener.getFeatures());
            featureStore.setTransaction(transaction);
            try {
                featureStore.addFeatures(collection);
                transaction.commit();
            } catch (Exception problem) {
                problem.printStackTrace();
                try {
                    transaction.rollback();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } finally {
                try {
                    transaction.close();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return distance;
    }
}
