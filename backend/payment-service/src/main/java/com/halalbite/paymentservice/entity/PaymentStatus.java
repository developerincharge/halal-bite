package com.halalbite.paymentservice.entity;

/**
 * PaymentStatus — mirrors Stripe payment states
 *
 * PENDING    → PaymentIntent created, awaiting customer payment
 * PROCESSING → payment submitted, Stripe verifying
 * SUCCEEDED  → payment confirmed, funds captured
 * FAILED     → payment declined or error
 * REFUNDED   → payment reversed (order cancellation after payment)
 */
public enum PaymentStatus {
    PENDING,
    PROCESSING,
    SUCCEEDED,
    FAILED,
    REFUNDED
}
