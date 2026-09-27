export * from './attachment-controller.service';
import { AttachmentControllerService } from './attachment-controller.service';
export * from './auth-controller.service';
import { AuthControllerService } from './auth-controller.service';
export * from './reference-controller.service';
import { ReferenceControllerService } from './reference-controller.service';
export * from './test-rest-controller.service';
import { TestRestControllerService } from './test-rest-controller.service';
export * from './transfer-controller.service';
import { TransferControllerService } from './transfer-controller.service';
export const APIS = [
  AttachmentControllerService,
  AuthControllerService,
  ReferenceControllerService,
  TestRestControllerService,
  TransferControllerService,
];
