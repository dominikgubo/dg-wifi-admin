package com.example.wifiadmin.persistence.repositories;

import com.example.wifiadmin.persistence.entities.WifiConfigurationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WifiConfigurationRepository extends JpaRepository<WifiConfigurationEntity, String> {
}
