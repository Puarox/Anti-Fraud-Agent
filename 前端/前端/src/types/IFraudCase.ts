export interface IFraudCase {
  id?: number;
  caseId: string;
  title: string;
  targetGroup?: string;
  fraudType?: string;
  scenario?: string;
  process?: string;
  keyFeatures?: string;
  riskLevel?: '低' | '中' | '高' | '极高';
  amountInvolved?: number;
  region?: string;
  caseYear?: number;
  caseSource?: string;
  sourceUrl?: string;
  createdAt?: string;
  updatedAt?: string;
}
