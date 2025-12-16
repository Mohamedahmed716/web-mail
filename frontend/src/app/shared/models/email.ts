import {User} from './user';

export interface Email {
  id : string;
  sender: string;
  receivers: string[];
  subject: string;
  body: string;
  timestamp: Date;
  priority: number;      // 1-5(5 = Urgent)
  attachments: File[];
  folder: string;
  attachmentNames?: string[];
  isRead?: boolean;  // Read status of the email
}
