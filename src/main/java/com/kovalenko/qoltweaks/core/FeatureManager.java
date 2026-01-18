package com.kovalenko.qoltweaks.core;

import java.util.ArrayList;
import java.util.List;

public class FeatureManager {
    private final List<Feature> features = new ArrayList<>();

    public void register(Feature feature) {
        if (feature.isEnabled()) {
            feature.register();
            features.add(feature);
        }
    }
}