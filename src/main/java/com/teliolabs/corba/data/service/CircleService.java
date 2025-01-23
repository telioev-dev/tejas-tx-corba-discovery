package com.teliolabs.corba.data.service;

import com.teliolabs.corba.data.dto.Circle;
import com.teliolabs.corba.data.repository.CircleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

import java.util.List;

@Log4j2
@RequiredArgsConstructor
public class CircleService {
    private static CircleService instance;
    private final CircleRepository circleRepository;

    // Public method to get the singleton instance
    public static CircleService getInstance(CircleRepository circleRepository) {
        if (instance == null) {
            synchronized (CircleService.class) {
                if (instance == null) {
                    instance = new CircleService(circleRepository);
                    log.info("CircleService instance created.");
                }
            }
        }
        return instance;
    }

    public List<Circle> findAllCircles() {
        return circleRepository.findAllCircles();
    }

    public Circle findByNameAndVendor(String name, String vendor) {
        return circleRepository.findCircleByNameAndVendor(name, vendor).
                orElseThrow(() -> new IllegalArgumentException("No Circle found by name: " + name + " and vendor: " + vendor));
    }

}
