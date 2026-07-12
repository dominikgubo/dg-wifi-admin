package com.example.wifiadmin.validators;

import com.example.wifiadmin.models.api.WifiConfigurationPayload;
import com.example.wifiadmin.models.domain.EncryptionType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class WifiConfigurationValidator implements ConstraintValidator<ValidWifiConfiguration, WifiConfigurationPayload> {

    @Override
    public boolean isValid(WifiConfigurationPayload payload, ConstraintValidatorContext context) {
        if (payload == null || payload.encryptionType() == null) {
            return true;
        }

        boolean hasPassword = payload.password() != null && !payload.password().isBlank();
        if (payload.encryptionType() == EncryptionType.OPEN) {
            return !hasPassword;
        }
        return hasPassword;
    }
}
