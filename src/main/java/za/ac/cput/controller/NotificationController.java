package za.ac.cput.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import za.ac.cput.domain.Notification;
import za.ac.cput.domain.enums.NotificationStatus;
import za.ac.cput.domain.enums.NotificationType;
import za.ac.cput.domain.user.Patient;
import za.ac.cput.factory.NotificationFactory;
import za.ac.cput.service.NotificationService;
import za.ac.cput.service.PatientService;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;
    private final PatientService patientService;

    @Autowired
    public NotificationController(NotificationService notificationService, PatientService patientService) {
        this.notificationService = notificationService;
        this.patientService = patientService;
    }

    @PostMapping("/create")
    public ResponseEntity<Notification> create(@RequestBody Notification notification) {
        // Route through the factory so validation rules (Helper checks) are
        // actually enforced, instead of persisting a raw deserialized object.
        Notification validated = NotificationFactory.createNotification(
                notification.getNotificationId(),
                notification.getNotificationType(),
                notification.getNotificationStatus(),
                notification.getNotificationMessage(),
                notification.getPatient(),
                notification.getTicket(),
                notification.getAppointment(),
                notification.getNotificationDate()
        );

        if (validated == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(notificationService.create(validated));
    }

    @GetMapping("/read/{id}")
    public ResponseEntity<Notification> read(@PathVariable int id) {
        Notification notification = notificationService.read(id);
        if (notification == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(notification);
    }

    @PutMapping("/update")
    public ResponseEntity<Notification> update(@RequestBody Notification notification) {
        // Ensure the record actually exists before allowing an update,
        // otherwise JPA's save() will silently insert a new row.
        Notification existing = notificationService.read(notification.getNotificationId());
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(notificationService.update(notification));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> delete(@PathVariable int id) {
        Notification existing = notificationService.read(id);
        if (existing == null) {
            return ResponseEntity.notFound().build();
        }
        notificationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/getall")
    public ResponseEntity<List<Notification>> getAll() {
        return ResponseEntity.ok(notificationService.getAll());
    }

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<Notification>> findByPatient(@PathVariable int patientId) {
        // Look the patient up directly instead of routing through a
        // Notification lookup (which was using the wrong ID entirely).
        Patient patient = patientService.read(patientId);
        if (patient == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(notificationService.findByPatient(patient));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Notification>> findByNotificationStatus(@PathVariable NotificationStatus status) {
        return ResponseEntity.ok(notificationService.findByNotificationStatus(status));
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<Notification>> findByNotificationType(@PathVariable NotificationType type) {
        return ResponseEntity.ok(notificationService.findByNotificationType(type));
    }
}