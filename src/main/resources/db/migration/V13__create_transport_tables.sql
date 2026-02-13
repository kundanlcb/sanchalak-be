-- V13__create_transport_tables.sql
-- Create complete transport/bus tracking system with 8 tables

-- Table 1: vehicles
CREATE TABLE vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_number VARCHAR(20) NOT NULL UNIQUE,
    vehicle_type VARCHAR(20) NOT NULL DEFAULT 'BUS',
    capacity INT NOT NULL,
    driver_name VARCHAR(100) NOT NULL,
    driver_mobile VARCHAR(15) NOT NULL,
    driver_license VARCHAR(50) NULL,
    gps_device_id VARCHAR(100) NULL,
    insurance_expiry DATE NULL,
    last_maintenance_date DATE NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_vehicle_number (vehicle_number),
    INDEX idx_vehicle_status (status),
    INDEX idx_vehicle_gps_device (gps_device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table 2: routes
CREATE TABLE routes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_name VARCHAR(100) NOT NULL,
    route_code VARCHAR(20) NOT NULL UNIQUE,
    vehicle_id BIGINT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    route_type VARCHAR(20) NOT NULL DEFAULT 'PICKUP',
    total_distance_km DECIMAL(10, 2) NULL,
    estimated_duration_minutes INT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_route_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE SET NULL,
    
    INDEX idx_route_code (route_code),
    INDEX idx_route_vehicle (vehicle_id),
    INDEX idx_route_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table 3: stops
CREATE TABLE stops (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_id BIGINT NOT NULL,
    stop_name VARCHAR(100) NOT NULL,
    stop_order INT NOT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    landmark_description TEXT NULL,
    estimated_arrival TIME NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_stop_route FOREIGN KEY (route_id) REFERENCES routes(id) ON DELETE CASCADE,
    
    INDEX idx_stop_route (route_id),
    INDEX idx_stop_order (route_id, stop_order),
    INDEX idx_stop_location (latitude, longitude)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table 4: trips
CREATE TABLE trips (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_id BIGINT NOT NULL,
    vehicle_id BIGINT NOT NULL,
    trip_date DATE NOT NULL,
    trip_type VARCHAR(20) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    start_time TIMESTAMP NULL,
    end_time TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_trip_route FOREIGN KEY (route_id) REFERENCES routes(id) ON DELETE CASCADE,
    CONSTRAINT fk_trip_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE,
    
    INDEX idx_trip_route (route_id),
    INDEX idx_trip_vehicle (vehicle_id),
    INDEX idx_trip_date (trip_date),
    INDEX idx_trip_status (status),
    INDEX idx_trip_active (route_id, trip_date, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table 5: student_transport_assignments
CREATE TABLE student_transport_assignments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    student_id BIGINT NOT NULL,
    route_id BIGINT NOT NULL,
    pickup_stop_id BIGINT NULL,
    drop_stop_id BIGINT NULL,
    assigned_date DATE NOT NULL,
    end_date DATE NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_student_transport_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_student_transport_route FOREIGN KEY (route_id) REFERENCES routes(id) ON DELETE CASCADE,
    CONSTRAINT fk_student_transport_pickup_stop FOREIGN KEY (pickup_stop_id) REFERENCES stops(id) ON DELETE SET NULL,
    CONSTRAINT fk_student_transport_drop_stop FOREIGN KEY (drop_stop_id) REFERENCES stops(id) ON DELETE SET NULL,
    
    INDEX idx_student_transport_student (student_id),
    INDEX idx_student_transport_route (route_id),
    INDEX idx_student_transport_status (status),
    INDEX idx_student_transport_active (student_id, status, assigned_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Table 6: location_pings (high-frequency GPS data with partitioning hint)
CREATE TABLE location_pings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    vehicle_id BIGINT NOT NULL,
    trip_id BIGINT NULL,
    latitude DECIMAL(10, 8) NOT NULL,
    longitude DECIMAL(11, 8) NOT NULL,
    speed DECIMAL(6, 2) NULL COMMENT 'Speed in km/h',
    heading DECIMAL(6, 2) NULL COMMENT 'Direction in degrees (0-360)',
    accuracy DECIMAL(8, 2) NULL COMMENT 'GPS accuracy in meters',
    captured_at TIMESTAMP NOT NULL COMMENT 'Device timestamp',
    received_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Server timestamp',
    
    CONSTRAINT fk_location_ping_vehicle FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE CASCADE,
    CONSTRAINT fk_location_ping_trip FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE SET NULL,
    
    INDEX idx_location_ping_vehicle (vehicle_id),
    INDEX idx_location_ping_trip (trip_id),
    INDEX idx_location_ping_received_at (received_at),
    INDEX idx_location_ping_vehicle_time (vehicle_id, received_at DESC),
    INDEX idx_location_ping_cleanup (received_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
-- Note: For production, consider partitioning by RANGE(TO_DAYS(received_at)) for 30-day retention

-- Table 7: transport_events
CREATE TABLE transport_events (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    trip_id BIGINT NOT NULL,
    student_id BIGINT NULL,
    event_type VARCHAR(30) NOT NULL,
    event_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    latitude DECIMAL(10, 8) NULL,
    longitude DECIMAL(11, 8) NULL,
    remarks TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT fk_transport_event_trip FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE CASCADE,
    CONSTRAINT fk_transport_event_student FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE SET NULL,
    
    INDEX idx_transport_event_trip (trip_id),
    INDEX idx_transport_event_student (student_id),
    INDEX idx_transport_event_type (event_type),
    INDEX idx_transport_event_time (event_time),
    INDEX idx_transport_event_lookup (trip_id, student_id, event_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Add comments
ALTER TABLE vehicles COMMENT = 'Bus/vehicle fleet management';
ALTER TABLE routes COMMENT = 'Bus routes with schedule and stops';
ALTER TABLE stops COMMENT = 'Ordered stops along each route with GPS coordinates';
ALTER TABLE trips COMMENT = 'Daily bus trips (morning pickup, afternoon drop)';
ALTER TABLE student_transport_assignments COMMENT = 'Student-to-route assignments with pickup/drop stops';
ALTER TABLE location_pings COMMENT = 'High-frequency GPS tracking data (10-30 second intervals)';
ALTER TABLE transport_events COMMENT = 'Significant events: bus start, stop arrival, student pickup/drop, trip complete';
