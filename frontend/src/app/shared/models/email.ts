import {User} from './user';

export interface Email {
  id : string;
  sender: User;
  receivers: User[];
  subject: string;
  body: string;
  timestamp: Date;
  priority: number;      // 1-5 (5 = highest)
  attachments: File[];
  folder: string;
}
