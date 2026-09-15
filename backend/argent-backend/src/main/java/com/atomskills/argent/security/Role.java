package com.atomskills.argent.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/** Роль ({@code krn_role}) — справочник, например {@code ADMIN}/{@code MASTER}. */
@Entity
@Table(name = "krn_role")
@Getter
@Setter
public class Role {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Setter(AccessLevel.NONE)
  UUID id;

  @Column(name = "name", nullable = false, unique = true)
  String name;
}
