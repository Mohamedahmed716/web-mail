import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Sidebar } from './components/sidebar/sidebar';
import { Compose } from './components/compose/compose';

@Component({
  selector: 'app-mail',
  standalone: true,
  imports: [RouterOutlet, Sidebar, Compose],
  templateUrl: './mail.html',
  styleUrl: './mail.css'
})
export class Mail {}
