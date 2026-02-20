package com.cm.sanchalak.platform.subscription;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class FeatureService {

    private final FeatureRepository featureRepository;

    public FeatureService(FeatureRepository featureRepository) {
        this.featureRepository = featureRepository;
    }

    public List<Feature> getAllFeatures() {
        return featureRepository.findAll();
    }

    @Transactional
    public Feature createFeature(Feature feature) {
        return featureRepository.save(feature);
    }

    @Transactional
    public void deleteFeature(UUID id) {
        featureRepository.deleteById(id);
    }
}
