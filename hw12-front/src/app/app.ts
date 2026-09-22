import { Component, signal } from '@angular/core';
import { Router, RouterOutlet } from '@angular/router';
import { MatToolbar } from '@angular/material/toolbar';
import { MatIcon } from '@angular/material/icon';
import { MatIconButton } from '@angular/material/button';
import { Observable } from 'rxjs';
import { AuthService } from './services/auth.service';
import { User } from './models/user.model';
import { AsyncPipe } from '@angular/common';

@Component({
  imports: [RouterOutlet, MatToolbar, MatIcon, MatIconButton, AsyncPipe],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {
  protected readonly title = signal('hw12-front');
  currentUser$: Observable<User | null>;
  isAuthenticated$: Observable<boolean>;

  constructor(
    private authService: AuthService,
    private router: Router,
  ) {
    this.currentUser$ = this.authService.currentUser$;
    this.isAuthenticated$ = this.authService.isAuthenticated$;
  }

  ngOnInit(): void {}

  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
