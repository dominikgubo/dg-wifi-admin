package com.example.wifiadmin.domain.validation;

import com.example.wifiadmin.api.WifiConfigurationPayload;
import com.example.wifiadmin.domain.EncryptionType;
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
