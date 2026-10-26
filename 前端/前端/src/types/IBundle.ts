export interface IBundle {
  id: string;
  name: string;
  description: string;
  price: number;
  dataLimit: number;
  callMinutes: number;
  smsCount: number;
  effectiveDate: string | null;
  expiryDate: string | null;
  validityDays: number;
  state: string;
  [property: string]: any;
}
