package dev.tomas.dma.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import com.stripe.Stripe;

@Configuration
public class StripeConfig {

    @Value("${stripe.private}")
    private String stripeSecret;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeSecret;
    }
}
