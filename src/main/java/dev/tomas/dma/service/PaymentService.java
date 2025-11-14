package dev.tomas.dma.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import dev.tomas.dma.dto.common.DonationDTO;

public interface PaymentService {
    PaymentIntent createPaymentIntent(DonationDTO req) throws StripeException;
}
