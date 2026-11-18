
package com.pbl.parkingsystem.repository;

import com.pbl.parkingsystem.entity.RevokedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RevokedTokenRepository
        extends JpaRepository<RevokedToken, String> {
}
