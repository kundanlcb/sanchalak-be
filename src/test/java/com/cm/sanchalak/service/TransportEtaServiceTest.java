package com.cm.sanchalak.service;

import com.cm.sanchalak.entity.LocationPing;
import com.cm.sanchalak.entity.Stop;
import com.cm.sanchalak.entity.Vehicle;
import com.cm.sanchalak.service.LocationTrackingService;
import com.cm.sanchalak.service.TransportEtaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

class TransportEtaServiceTest {

    @Mock
    private LocationTrackingService locationTrackingService;

    @InjectMocks
    private TransportEtaService transportEtaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testCalculateEtaMinutes_Success() {
        Long vehicleId = 1L;
        Stop stop = new Stop();
        stop.setId(10L);
        stop.setLatitude(28.6139); // New Delhi
        stop.setLongitude(77.2090);

        LocationPing location = new LocationPing();
        Vehicle vehicle = new Vehicle();
        vehicle.setId(vehicleId);
        location.setVehicle(vehicle);
        location.setLatitude(28.5355); // Okhla (approx 10km away)
        location.setLongitude(77.2736);
        location.setSpeedKmh(30.0); // 30 km/h
        location.setCapturedAt(Instant.now());

        when(locationTrackingService.getLatestLocationForVehicle(vehicleId)).thenReturn(location);

        Integer eta = transportEtaService.calculateEtaMinutes(vehicleId, stop);

        assertNotNull(eta);
        assertTrue(eta > 0);
        // Distance roughly 10-12 km. Speed 30km/h. Time ~20-25 mins.
        // Let's print out if exact needed, but >0 is good enough for logic flow check.
        // Haversine ~ 10.5 km. (10.5 / 30) * 60 = 21 mins.
        assertEquals(22, eta, 5); // Allow small margin due to precision
    }

    @Test
    void testCalculateEtaMinutes_NoLocation() {
        Long vehicleId = 1L;
        Stop stop = new Stop();
        
        when(locationTrackingService.getLatestLocationForVehicle(vehicleId)).thenReturn(null);
        
        Integer eta = transportEtaService.calculateEtaMinutes(vehicleId, stop);
        
        assertNull(eta);
    }

    @Test
    void testCalculateHaversineDistance() {
        // Known distance: Lat(51.5007, 0.1246) London to Lat(40.7128, 74.0060) NYC = ~5570 km
        // But let's use small distance.
        // 0,0 to 0,1 degrees. 1 degree longitude at equator is ~111km.
        
        // Reflection to test private method or use public wrapping
        // Since it's private and used by calculateEtaMinutes, we tested it implicitly above.
        // But for coverage let's do a direct test if possible or rely on the above.
        // We will stick to public API testing.
        
        Long vehicleId = 1L;
        Stop stop = new Stop();
        stop.setLatitude(0.0);
        stop.setLongitude(1.0); // 1 degree away along equator

        LocationPing location = new LocationPing();
        location.setLatitude(0.0);
        location.setLongitude(0.0);
        location.setSpeedKmh(111.0); // Go fast so 1 hour exactly
        location.setCapturedAt(Instant.now());
        
        when(locationTrackingService.getLatestLocationForVehicle(vehicleId)).thenReturn(location);

        Integer eta = transportEtaService.calculateEtaMinutes(vehicleId, stop);
        
        // Distance is ~111 km
        // Speed is clamped to MAX_SPEED_KMH (50.0)
        // Time = (111.19 / 50.0) * 60 = 133.4 minutes
        assertEquals(133, eta, 2);
    }
}
