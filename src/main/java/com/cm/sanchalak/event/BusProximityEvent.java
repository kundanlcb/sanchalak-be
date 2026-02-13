package com.cm.sanchalak.event;

/**
 * Event published when bus is approaching a student's stop
 */
public class BusProximityEvent {
    private Long vehicleId;
    private Long routeId;
    private String routeName;
    private Long stopId;
    private String stopName;
    private Integer etaMinutes;
    private Double distanceKm;
    private Long studentId; // Student assigned to this stop

    public BusProximityEvent(Long vehicleId, Long routeId, String routeName, Long stopId, String stopName, Integer etaMinutes, Double distanceKm, Long studentId) {
        this.vehicleId = vehicleId;
        this.routeId = routeId;
        this.routeName = routeName;
        this.stopId = stopId;
        this.stopName = stopName;
        this.etaMinutes = etaMinutes;
        this.distanceKm = distanceKm;
        this.studentId = studentId;
    }

    public Long getVehicleId() { return vehicleId; }
    public void setVehicleId(Long vehicleId) { this.vehicleId = vehicleId; }

    public Long getRouteId() { return routeId; }
    public void setRouteId(Long routeId) { this.routeId = routeId; }

    public String getRouteName() { return routeName; }
    public void setRouteName(String routeName) { this.routeName = routeName; }

    public Long getStopId() { return stopId; }
    public void setStopId(Long stopId) { this.stopId = stopId; }

    public String getStopName() { return stopName; }
    public void setStopName(String stopName) { this.stopName = stopName; }

    public Integer getEtaMinutes() { return etaMinutes; }
    public void setEtaMinutes(Integer etaMinutes) { this.etaMinutes = etaMinutes; }

    public Double getDistanceKm() { return distanceKm; }
    public void setDistanceKm(Double distanceKm) { this.distanceKm = distanceKm; }

    public Long getStudentId() { return studentId; }
    public void setStudentId(Long studentId) { this.studentId = studentId; }
}
