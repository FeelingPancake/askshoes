export * from './attachment-controller.service';
import { AttachmentControllerService } from './attachment-controller.service';
export * from './auth-controller.service';
import { AuthControllerService } from './auth-controller.service';
export * from './client-controller.service';
import { ClientControllerService } from './client-controller.service';
export * from './order-controller.service';
import { OrderControllerService } from './order-controller.service';
export * from './reference-controller.service';
import { ReferenceControllerService } from './reference-controller.service';
export * from './transfer-controller.service';
import { TransferControllerService } from './transfer-controller.service';
export const APIS = [
  AttachmentControllerService,
  AuthControllerService,
  ClientControllerService,
  OrderControllerService,
  ReferenceControllerService,
  TransferControllerService,
];
