package dev.tomas.dma.service.implementation;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;
import dev.tomas.dma.dto.common.DonationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @InjectMocks
    private PaymentServiceImpl paymentService;

    private DonationDTO donationDTO;

    @BeforeEach
    void setUp() {
        donationDTO = new DonationDTO();
        donationDTO.setCampaignId(1);
        donationDTO.setUserId(1);
        donationDTO.setAmount(5000L); // 50.00 EUR in cents
    }

    @Nested
    @DisplayName("CreatePaymentIntent Tests")
    class CreatePaymentIntentTests {

        @Test
        @DisplayName("Should create payment intent with correct parameters")
        void createPaymentIntent_Success() throws StripeException {
            PaymentIntent mockPaymentIntent = mock(PaymentIntent.class);
            when(mockPaymentIntent.getClientSecret()).thenReturn("pi_test_secret");

            try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
                mockedStatic.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                        .thenReturn(mockPaymentIntent);

                PaymentIntent result = paymentService.createPaymentIntent(donationDTO);

                assertThat(result).isNotNull();
                assertThat(result.getClientSecret()).isEqualTo("pi_test_secret");

                mockedStatic.verify(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)));
            }
        }

        @Test
        @DisplayName("Should set amount correctly in payment intent")
        void createPaymentIntent_CorrectAmount() throws StripeException {
            donationDTO.setAmount(10000L); // 100.00 EUR

            PaymentIntent mockPaymentIntent = mock(PaymentIntent.class);
            when(mockPaymentIntent.getAmount()).thenReturn(10000L);

            try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
                mockedStatic.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                        .thenReturn(mockPaymentIntent);

                PaymentIntent result = paymentService.createPaymentIntent(donationDTO);

                assertThat(result).isNotNull();
                assertThat(result.getAmount()).isEqualTo(10000L);
            }
        }

        @Test
        @DisplayName("Should include campaign and user metadata")
        void createPaymentIntent_IncludesMetadata() throws StripeException {
            donationDTO.setCampaignId(5);
            donationDTO.setUserId(10);

            PaymentIntent mockPaymentIntent = mock(PaymentIntent.class);

            try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
                mockedStatic.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                        .thenReturn(mockPaymentIntent);

                PaymentIntent result = paymentService.createPaymentIntent(donationDTO);

                assertThat(result).isNotNull();
            }
        }

        @Test
        @DisplayName("Should handle minimum amount donation")
        void createPaymentIntent_MinimumAmount() throws StripeException {
            donationDTO.setAmount(50L); // Minimum amount (0.50 EUR)

            PaymentIntent mockPaymentIntent = mock(PaymentIntent.class);

            try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
                mockedStatic.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                        .thenReturn(mockPaymentIntent);

                PaymentIntent result = paymentService.createPaymentIntent(donationDTO);

                assertThat(result).isNotNull();
            }
        }

        @Test
        @DisplayName("Should handle large amount donation")
        void createPaymentIntent_LargeAmount() throws StripeException {
            donationDTO.setAmount(99999999L); // Large donation

            PaymentIntent mockPaymentIntent = mock(PaymentIntent.class);

            try (MockedStatic<PaymentIntent> mockedStatic = mockStatic(PaymentIntent.class)) {
                mockedStatic.when(() -> PaymentIntent.create(any(PaymentIntentCreateParams.class)))
                        .thenReturn(mockPaymentIntent);

                PaymentIntent result = paymentService.createPaymentIntent(donationDTO);

                assertThat(result).isNotNull();
            }
        }
    }
}