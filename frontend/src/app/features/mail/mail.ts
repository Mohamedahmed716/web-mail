import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Sidebar } from './components/sidebar/sidebar'; // Assuming you have this

@Component({
  selector: 'app-mail',
  standalone: true,
  imports: [RouterOutlet, Sidebar],
  templateUrl: './mail.html',
  styleUrl: './mail.css'
})
export class Mail {}
