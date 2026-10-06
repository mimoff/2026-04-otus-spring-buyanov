import { Component } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, ActivatedRoute } from '@angular/router';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthService } from '../../services/auth.service';
import { MatCard, MatCardContent, MatCardHeader, MatCardSubtitle, MatCardTitle } from '@angular/material/card';
import { MatError, MatFormField, MatInput, MatLabel } from '@angular/material/input';
import { MatIcon } from '@angular/material/icon';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatButton, MatIconButton } from '@angular/material/button';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.css'],
  imports: [
    MatCard,
    MatCardHeader,
    MatCardTitle,
    MatCardSubtitle,
    MatCardContent,
    MatFormField,
    MatLabel,
    MatIcon,
    MatError,
    MatProgressSpinner,
    MatInput,
    MatIconButton,
    ReactiveFormsModule,
    MatButton,
  ],
})
export class LoginComponent {
  form: FormGroup;
  isLoading = false;
  hidePassword = true;

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router,
    private route: ActivatedRoute,
    private snackBar: MatSnackBar,
  ) {
    this.form = this.fb.group({
      username: ['', [Validators.required, Validators.minLength(3)]],
      password: ['', [Validators.required, Validators.minLength(4)]],
    });
  }

  onSubmit(): void {
    if (this.form.invalid) return;

    this.isLoading = true;
    this.authService.login(this.form.value).subscribe({
      next: () => {
        this.isLoading = false;
        this.snackBar.open('Вы успешно вошли', 'Закрыть', { duration: 3000 });
        const returnUrl = this.route.snapshot.queryParams['returnUrl'] || '/books';
        this.router.navigateByUrl(returnUrl);
      },
      error: (err) => {
        this.isLoading = false;
        const message = err?.error?.message || err.message || 'Неверные учётные данные';
        this.snackBar.open('Ошибка входа: ' + message, 'Закрыть', {
          duration: 4000,
          panelClass: ['error-snackbar'],
        });
      },
    });
  }
}
