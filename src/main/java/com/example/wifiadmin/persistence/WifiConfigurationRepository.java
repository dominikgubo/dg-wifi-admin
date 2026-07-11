package com.example.wifiadmin.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface WifiConfigurationRepository extends JpaRepository<WifiConfigurationEntity, String> {
}
