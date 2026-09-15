import { TestBed } from '@angular/core/testing';

import { AuthTokenStore } from './auth-token-store';

describe('authTokenStore', () => {
  let service: AuthTokenStore;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(AuthTokenStore);
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });
});
