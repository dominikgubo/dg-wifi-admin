package com.example.wifiadmin.configuration;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

@SpringBootTest
class ApplicationContextTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void applicationContextStartsWithDefaultProfile() {
        assertThat(applicationContext).isNotNull();
    }
}
