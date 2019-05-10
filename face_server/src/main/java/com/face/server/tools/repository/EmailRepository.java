package com.face.server.tools.repository;

import com.face.server.tools.domain.EmailConfig;
import com.face.server.tools.domain.EmailConfig;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author jie
 * @date 2018-12-26
 */
public interface EmailRepository extends JpaRepository<EmailConfig,Long> {
}
