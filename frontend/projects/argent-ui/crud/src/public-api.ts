/*
 * Secondary entry point `argent-ui/crud` — тяжёлые CRUD-экраны (PrimeNG Table, Dialog, ...).
 * Вынесены из основного `argent-ui`, чтобы приложение грузило их лениво (`loadComponent`)
 * и не тащило в стартовый бандл.
 */
export { RefCrudPage } from './ref-crud-page/ref-crud-page';
