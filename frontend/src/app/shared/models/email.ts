export interface Email {
  id : string;
  sender: string;
  receivers: string[];
  subject: string;
  body: string;
  timestamp: Date;
  priority: number;      // 1-5 (5 = highest)
  attachments: File[];
  attachmentNames?: string[];
  folder: string;
}
