package com.face.server.tools.repository;

import com.face.server.tools.domain.AlipayConfig;
import com.face.server.tools.domain.AlipayConfig;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author jie
 * @date 2018-12-31
 */
public interface AlipayRepository extends JpaRepository<AlipayConfig,Long> {
}
