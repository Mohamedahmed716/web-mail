import { Component, Input, OnInit } from '@angular/core';
import { Email } from '../../../../shared/models/email';
import { CommonModule } from '@angular/common';

@Component({
  selector: 'app-EmailDisplay',
  templateUrl: './EmailDisplay.component.html',
  styleUrls: ['./EmailDisplay.component.css'],
  standalone: true,
  imports: [CommonModule]
})
export class EmailDisplayComponent implements OnInit {

  @Input()   mail:Email | null = null;
  @Input()   type:string = '';
  baseUrl: any;

  constructor() { }

  ngOnInit() {
  }
  getFileType(fileName: string): 'image' | 'link' | 'file' {
    if (!fileName) return 'file';
    
    // لو بيبدأ بـ http يبقى لينك
    if (fileName.startsWith('http') || fileName.startsWith('https')) {
      return 'link';
    }

    // لو بينتهي بامتداد صورة
    const imageExtensions = ['.jpg', '.jpeg', '.png', '.gif', '.bmp', '.webp'];
    const lowerName = fileName.toLowerCase();
    if (imageExtensions.some(ext => lowerName.endsWith(ext))) {
      return 'image';
    }

    // غير كدة يبقى ملف
    return 'file';
  }

  // 2. دالة بتجيب اسم الملف بس من المسار الطويل
  getFileName(path: string): string {
    if (path.startsWith('http')) return 'External Link';
    // بيقطع المسار وياخد آخر حتة (اسم الملف)
    return path.replace(/^.*[\\\/]/, '');
  }

  // 3. دالة بتجهز رابط التحميل
  // في دالة getAttachmentUrl
getAttachmentUrl(fileName: string): string {
    if (fileName.startsWith('http')) return fileName;
    
    // التعديل: بنبعت اسم الملف والإيميل بتاع الراسل (أو صاحب الصندوق)
    // افترضنا إن الإيميل المفتوح هو صاحب الملف
    const ownerEmail = this.mail?.sender || this.mail?.sender; 
    
    return `http://localhost:8080/api/attachments/download?file=${fileName}&email=${ownerEmail}`;
}
}


