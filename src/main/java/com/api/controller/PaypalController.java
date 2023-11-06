package com.api.controller;

import com.api.model.Order;
import com.api.service.impl.PaypalServiceImpl;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/api/payment")
public class PaypalController {
    @Autowired
    private PaypalServiceImpl service;

        public static final String SUCCESS_URL = "pay/success";
        public static final String CANCEL_URL = "pay/cancel";

        @PostMapping("/pay")
        public ResponseEntity<String> payment(@RequestBody Order order) {
            try {
                Payment payment = service.createPayment(order.getPrice(), order.getCurrency(), order.getMethod(),
                        order.getIntent(), order.getDescription(), "http://localhost:9090/" + CANCEL_URL,
                        "http://localhost:9090/" + SUCCESS_URL);
                for (Links link : payment.getLinks()) {
                    if (link.getRel().equals("approval_url")) {
                        return ResponseEntity.status(HttpStatus.OK).body(link.getHref());
                    }
                }
            } catch (PayPalRESTException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Payment creation failed.");
        }
        @GetMapping(value = CANCEL_URL)
        public ResponseEntity<String> cancelPay() {
            return ResponseEntity.status(HttpStatus.OK).body("Payment cancelled.");
        }

        @GetMapping(value = SUCCESS_URL)
        public ResponseEntity<String> successPay(@RequestParam("paymentId") String paymentId,
                                                 @RequestParam("PayerID") String payerId) {
            try {
                Payment payment = service.executePayment(paymentId, payerId);
                if (payment.getState().equals("approved")) {
                    return ResponseEntity.status(HttpStatus.OK).body("Payment approved.");
                }
            } catch (PayPalRESTException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Payment approval failed.");
        }
    }