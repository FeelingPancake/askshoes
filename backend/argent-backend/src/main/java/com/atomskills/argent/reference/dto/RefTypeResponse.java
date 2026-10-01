package com.atomskills.argent.reference.dto;

import com.atomskills.argent.reference.entity.RefType;

public record RefTypeResponse(String code, String name) {

  public static RefTypeResponse from(RefType refType) {
    return new RefTypeResponse(refType.getCode(), refType.getName());
  }
}
