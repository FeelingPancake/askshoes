package com.atomskills.argent.auth;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data репозиторий для {@link UserSession}. */
public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {}
