import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { Sidebar } from './components/sidebar/sidebar';
import { Compose } from './components/compose/compose';
import {Toast} from './components/toast/toast';

@Component({
  selector: 'app-mail',
  standalone: true,
  imports: [RouterOutlet, Sidebar, Compose, Toast],
  templateUrl: './mail.html',
  styleUrl: './mail.css'
})
export class Mail {}
