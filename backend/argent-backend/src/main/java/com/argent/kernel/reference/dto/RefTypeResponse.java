package com.argent.kernel.reference.dto;

import com.argent.kernel.reference.entity.RefType;

public record RefTypeResponse(String code, String name) {

  public static RefTypeResponse from(RefType refType) {
    return new RefTypeResponse(refType.getCode(), refType.getName());
  }
}
