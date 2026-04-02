package com.halalbite.restaurantservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

/**
 * OperatingHours Entity — maps to "operating_hours" table
 *
 * One record per day of the week per restaurant.
 * e.g. Monday: 11:00 - 22:00, Tuesday: 11:00 - 22:00 etc.
 *
 * isClosed = true means the restaurant is closed that day.
 */
@Entity
@Table(name = "operating_hours")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperatingHours {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "restaurant_id", nullable = false)
    private Restaurant restaurant;

    @Enumerated(EnumType.STRING)
    @Column(name = "day_of_week", nullable = false, length = 10)
    private DayOfWeek dayOfWeek;   // MONDAY, TUESDAY ... SUNDAY

    @Column(name = "open_time")
    private LocalTime openTime;    // e.g. 11:00

    @Column(name = "close_time")
    private LocalTime closeTime;   // e.g. 22:00

    @Column(name = "is_closed", nullable = false)
    @Builder.Default
    private Boolean isClosed = false;
}
