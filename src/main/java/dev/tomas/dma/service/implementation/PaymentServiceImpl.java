package dev.tomas.dma.service.implementation;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import dev.tomas.dma.dto.common.DonationDTO;
import dev.tomas.dma.service.PaymentService;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {

    public PaymentIntent createPaymentIntent(DonationDTO req) throws StripeException {
        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(req.getAmount())
                        .setCurrency("eur")
                        .setAutomaticPaymentMethods(
                                PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                                        .setEnabled(true)
                                        .build()
                        )
                        .putMetadata("campaignId", req.getCampaignId().toString())
                        .putMetadata("userId", req.getUserId().toString())
                        .build();
        return PaymentIntent.create(params);
    }
}
