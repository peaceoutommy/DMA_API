package dev.tomas.dma.controller;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import dev.tomas.dma.dto.common.DonationDTO;
import dev.tomas.dma.dto.response.DonationByUserGetAllRes;
import dev.tomas.dma.service.CampaignService;
import dev.tomas.dma.service.DonationService;
import dev.tomas.dma.service.PaymentService;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/donations")
public class DonationController {
    private final PaymentService paymentService;
    private final CampaignService campaignService;
    private final DonationService donationService;

    @Value("${stripe.webhook.private}")
    private String webhookSecret;

    @PostMapping()
    public ResponseEntity<Map<String, String>> donate(@RequestBody DonationDTO request) throws StripeException {
        PaymentIntent paymentIntent = paymentService.createPaymentIntent(request);

        Map<String, String> response = new HashMap<>();
        response.put("clientSecret", paymentIntent.getClientSecret());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<DonationByUserGetAllRes>> getAllByUserId(@PathVariable @Positive Integer userId){
        return ResponseEntity.ok(donationService.getAllByUserId(userId));
    }

    @PostMapping("/webhook")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) throws StripeException {

        Event event;

        try {
            // Verify the webhook signature
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
        }

        // Handle the event
        if (Objects.equals(event.getType(), "payment_intent.succeeded")) {
            PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                    .getObject()
                    .orElse(null);

            if (paymentIntent != null) {
                Map<String, String> metadata = paymentIntent.getMetadata();
                DonationDTO donationDTO = new DonationDTO();

                if (Objects.nonNull(metadata)) {
                    try {
                        donationDTO.setCampaignId(Integer.parseInt(metadata.get("campaignId")));
                        donationDTO.setUserId(Integer.parseInt(metadata.get("userId")));
                        donationDTO.setAmount(paymentIntent.getAmount());
                    } catch (NumberFormatException e) {
                        throw new IllegalArgumentException("Campaign id or User id not valid");
                    }
                } else {
                    throw new IllegalArgumentException("Payment intent is null");
                }

                donationService.save(donationDTO);
            }
        }

        return ResponseEntity.ok("Success");
    }

}
