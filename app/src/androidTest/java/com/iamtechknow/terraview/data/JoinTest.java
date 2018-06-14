package com.iamtechknow.terraview.data;

import android.support.test.runner.AndroidJUnit4;

import com.iamtechknow.terraview.model.CatMeasureJoin;
import com.iamtechknow.terraview.model.Category;
import com.iamtechknow.terraview.model.Layer;
import com.iamtechknow.terraview.model.MeasureLayerJoin;
import com.iamtechknow.terraview.model.Measurement;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

@RunWith(AndroidJUnit4.class)
public class JoinTest extends DbTest {
    @Test
    public void testCategoryRelationships() {
        Category all = Category.getAllCategory(), fires = new Category("Fires");

        List<Measurement> fireMeasurements = new ArrayList<>(), other = new ArrayList<>();
        fireMeasurements.add(new Measurement("Aerosol Optical Depth"));
        fireMeasurements.add(new Measurement("Fires and Thermal Anomalies"));
        fireMeasurements.add(new Measurement("Sulfur Dioxide"));

        other.add(new Measurement("Aerosol Albedo"));
        other.add(new Measurement("Blue Marble"));
        other.add(new Measurement("Cloud Pressure"));
        other.add(new Measurement("Corrected Reflectance"));
        other.add(new Measurement("Temperature"));

        //Add measurements and categories THEN joins
        List<CatMeasureJoin> joins = new ArrayList<>();
        for(Measurement m : fireMeasurements) {
            joins.add(new CatMeasureJoin(fires.getName(), m.getName()));
            joins.add(new CatMeasureJoin(all.getName(), m.getName()));
        }
        for(Measurement m : other)
            joins.add(new CatMeasureJoin(all.getName(), m.getName()));
        db.getTVDao().insertMeasurements(fireMeasurements);
        db.getTVDao().insertMeasurements(other);
        db.getTVDao().insertCategories(Arrays.asList(all, fires));
        db.getJoinDAO().insertCatMeasureJoin(joins);

        //Test queries, verify the first measurement in fireMeasurements is in both categories,
        //Aerosol Albedo is not a Fires measurement, and all measurements are in the All category.
        List<Measurement> fireQuery = db.getJoinDAO().getMeasurementsForCategory(fires.getName()).blockingGet(),
                allQuery = db.getJoinDAO().getMeasurementsForCategory(all.getName()).blockingGet();

        assertNotEquals(-1, fireQuery.indexOf(fireMeasurements.get(0)));
        assertNotEquals(-1, allQuery.indexOf(fireMeasurements.get(0)));
        assertEquals(-1, fireQuery.indexOf(other.get(0)));
        assertEquals(fireMeasurements.size() + other.size(), allQuery.size());
    }

    @Test
    public void testMeasurementRelationships() {
        //Insert data
        Measurement blueMarble = new Measurement("Blue Marble");

        List<Layer> layers = new ArrayList<>();
        Layer layer1 = new Layer(), layer2 = new Layer(), layer3 = new Layer();
        layer1.setIdentifier("BlueMarble_NextGeneration");
        layer2.setIdentifier("BlueMarble_ShadedRelief");
        layer3.setIdentifier("BlueMarble_ShadedRelief_Bathymetry");
        layers.add(layer1);
        layers.add(layer2);
        layers.add(layer3);

        //Add layers and measurements THEN joins
        List<MeasureLayerJoin> joins = new ArrayList<>();
        for(Layer l : layers)
            joins.add(new MeasureLayerJoin(blueMarble.getName(), l.getIdentifier()));
        db.getTVDao().insertLayers(layers);
        db.getTVDao().insertMeasurements(Collections.singletonList(blueMarble));
        db.getJoinDAO().insertMeasureLayerJoin(joins);

        //Test queries, verify all three layers are part of the measurement
        List<Layer> blueQuery = db.getJoinDAO().getLayersForMeasurement(blueMarble.getName()).blockingGet();
        assertNotEquals(-1, blueQuery.indexOf(layer1));
        assertNotEquals(-1, blueQuery.indexOf(layer2));
        assertNotEquals(-1, blueQuery.indexOf(layer3));
    }
}
