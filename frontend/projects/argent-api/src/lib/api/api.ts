export * from './auth-controller.service';
import { AuthControllerService } from './auth-controller.service';
export * from './reference-controller.service';
import { ReferenceControllerService } from './reference-controller.service';
export * from './test-rest-controller.service';
import { TestRestControllerService } from './test-rest-controller.service';
export const APIS = [AuthControllerService, ReferenceControllerService, TestRestControllerService];
