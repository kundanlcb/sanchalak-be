package com.cm.sanchalak.scheduler;

import com.cm.sanchalak.entity.Parent;
import com.cm.sanchalak.entity.ParentStudentLink;
import com.cm.sanchalak.repository.ParentStudentLinkRepository;
import com.cm.sanchalak.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduled jobs for notification triggers
 */
@Component
@RequiredArgsConstructor
public class NotificationScheduler {

    private static final Logger log = LoggerFactory.getLogger(NotificationScheduler.class);

    private final NotificationService notificationService;
    private final ParentStudentLinkRepository parentStudentLinkRepository;

    /**
     * Fee due reminder job
     * Runs daily at 9:00 AM
     * Checks for fees due in the next 3 days and sends reminders to parents
     */
    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Kolkata")
    public void sendFeeDueReminders() {
        log.info("Starting fee due reminder job");

        try {
            LocalDate today = LocalDate.now();
            LocalDate dueDate = today.plusDays(3);

            // TODO: Query finance service or fee repository for upcoming due fees
            // For now, this is a placeholder structure

            // Example logic (to be implemented with actual fee queries):
            // List<Fee> upcomingFees = feeRepository.findDueFeesInDateRange(today,
            // dueDate);
            //
            // for (Fee fee : upcomingFees) {
            // Student student = fee.getStudent();
            // List<ParentStudentLink> links =
            // parentStudentLinkRepository.findActiveByStudentId(student.getId());
            //
            // for (ParentStudentLink link : links) {
            // Parent parent = link.getParent();
            // if (parent.getUser() != null) {
            // notificationService.sendFeeDueReminder(
            // parent.getUser().getId(),
            // student.getName(),
            // fee.getAmount(),
            // fee.getDueDate().toString()
            // );
            // }
            // }
            // }

            log.info("Fee due reminder job completed");

        } catch (Exception e) {
            log.error("Error in fee due reminder job: {}", e.getMessage(), e);
        }
    }

    /**
     * Bus proximity monitoring job
     * Runs every 2 minutes to check bus proximity to stops
     * Triggers proximity alerts for students whose bus is approaching
     */
    @Scheduled(fixedRate = 120000) // 2 minutes
    public void monitorBusProximity() {
        try {
            // TODO: Implement bus proximity checking logic
            // 1. Get all active trips for today
            // 2. Get latest GPS location for each vehicle
            // 3. Calculate distance to upcoming stops
            // 4. If distance < 2km, publish BusProximityEvent

            // Example logic (to be implemented):
            // List<Trip> activeTrips = tripRepository.findByDateAndStatus(LocalDate.now(),
            // "IN_PROGRESS");
            //
            // for (Trip trip : activeTrips) {
            // LocationPing latestLocation =
            // locationPingRepository.findLatestByVehicleId(trip.getVehicle().getId());
            // if (latestLocation == null) continue;
            //
            // List<Stop> upcomingStops =
            // stopRepository.findByRouteIdOrderByStopOrder(trip.getRoute().getId());
            //
            // for (Stop stop : upcomingStops) {
            // double distanceKm = calculateDistance(latestLocation.getLatitude(),
            // latestLocation.getLongitude(),
            // stop.getLatitude(), stop.getLongitude());
            //
            // if (distanceKm <= 2.0) {
            // // Get students at this stop
            // List<StudentTransportAssignment> assignments =
            // assignmentRepository.findActiveByStopId(stop.getId());
            //
            // for (StudentTransportAssignment assignment : assignments) {
            // BusProximityEvent event = new BusProximityEvent(
            // trip.getVehicle().getId(),
            // trip.getRoute().getId(),
            // trip.getRoute().getRouteName(),
            // stop.getId(),
            // stop.getStopName(),
            // calculateEta(distanceKm, latestLocation.getSpeedKmh()),
            // distanceKm,
            // assignment.getStudent().getId()
            // );
            //
            // applicationEventPublisher.publishEvent(event);
            // }
            // }
            // }
            // }

        } catch (Exception e) {
            log.error("Error in bus proximity monitoring job: {}", e.getMessage(), e);
        }
    }
}
