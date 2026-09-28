package com.argent.kernel.reference;

public record RefTypeResponse(String code, String name) {

  static RefTypeResponse from(RefType refType) {
    return new RefTypeResponse(refType.getCode(), refType.getName());
  }
}
