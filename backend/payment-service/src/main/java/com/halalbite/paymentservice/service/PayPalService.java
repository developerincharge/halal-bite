package com.halalbite.paymentservice.service;

import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayPalService {

    private final APIContext apiContext;

    @Value("${paypal.base-url}")
    private String baseUrl;

    /**
     * Create a PayPal order and return the approval URL.
     * The customer is redirected to this URL to approve the payment.
     */
    public Payment createPayment(
            BigDecimal totalAmount,
            String currency,
            String orderId,
            String returnUrl,
            String cancelUrl
    ) throws PayPalRESTException {

        // Amount
        Amount amount = new Amount();
        amount.setCurrency(currency);
        amount.setTotal(totalAmount.setScale(2, RoundingMode.HALF_UP).toString());

        // Transaction
        Transaction transaction = new Transaction();
        transaction.setDescription("Halal Bite Order #" + orderId.substring(0, 8).toUpperCase());
        transaction.setAmount(amount);

        // Payer — customer pays via PayPal
        Payer payer = new Payer();
        payer.setPaymentMethod("paypal");

        // Redirect URLs — where PayPal sends customer after approve/cancel
        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setReturnUrl(returnUrl);
        redirectUrls.setCancelUrl(cancelUrl);

        // Build payment
        Payment payment = new Payment();
        payment.setIntent("sale");
        payment.setPayer(payer);
        payment.setTransactions(List.of(transaction));
        payment.setRedirectUrls(redirectUrls);

        Payment created = payment.create(apiContext);
        log.info("PayPal payment created: {} for order: {}", created.getId(), orderId);
        return created;
    }

    /**
     * Execute the payment after the customer approves it on PayPal.
     * Called when PayPal redirects back to our returnUrl.
     */
    public Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        Payment payment = new Payment();
        payment.setId(paymentId);

        PaymentExecution execution = new PaymentExecution();
        execution.setPayerId(payerId);

        Payment executed = payment.execute(apiContext, execution);
        log.info("PayPal payment executed: {} status: {}", paymentId, executed.getState());
        return executed;
    }
}