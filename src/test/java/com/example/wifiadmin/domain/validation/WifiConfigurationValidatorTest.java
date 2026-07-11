package com.example.wifiadmin.domain.validation;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.wifiadmin.api.WifiConfigurationPayload;
import com.example.wifiadmin.domain.EncryptionType;
import com.example.wifiadmin.domain.WifiBand;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WifiConfigurationValidatorTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void defaultsMissingEncryptionToValidOpenConfiguration() {
        WifiConfigurationPayload payload = payload(null, null);

        assertThat(validator.validate(payload)).isEmpty();
    }

    @Test
    void acceptsOpenConfigurationWithoutPassword() {
        WifiConfigurationPayload payload = payload(EncryptionType.OPEN, null);

        assertThat(validator.validate(payload)).isEmpty();
    }

    @Test
    void rejectsOpenConfigurationWithPassword() {
        WifiConfigurationPayload payload = payload(EncryptionType.OPEN, "secret");

        assertThat(validator.validate(payload)).isNotEmpty();
    }

    @Test
    void rejectsSecureConfigurationWithoutPassword() {
        for (EncryptionType encryptionType : EncryptionType.values()) {
            if (encryptionType != EncryptionType.OPEN) {
                Set<?> violations = validator.validate(payload(encryptionType, null));
                assertThat(violations).isNotEmpty();
            }
        }
    }

    @Test
    void rejectsMissingRequiredFields() {
        WifiConfigurationPayload payload = new WifiConfigurationPayload(null, null, null, null, null);

        assertThat(validator.validate(payload)).hasSize(3);
    }

    private WifiConfigurationPayload payload(EncryptionType encryptionType, String password) {
        return new WifiConfigurationPayload("CPE_001", WifiBand.BAND_2_4_GHZ, "Office", encryptionType, password);
    }
}
