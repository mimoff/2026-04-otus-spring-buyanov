import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { BehaviorSubject, delay, Observable, of, tap } from 'rxjs';
import { LoginRequest, AuthResponse } from '../models/auth-response.model';
import { User } from '../models/user.model';
import {environment} from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private apiUrl = environment.apiUrl;
  private readonly TOKEN_KEY = 'auth_token';
  private readonly USER_KEY = 'auth_user';

  private currentUserSubject = new BehaviorSubject<User | null>(this.getUserFromStorage());
  public currentUser$ = this.currentUserSubject.asObservable();

  private isAuthenticatedSubject = new BehaviorSubject<boolean>(this.hasToken());
  public isAuthenticated$ = this.isAuthenticatedSubject.asObservable();

  constructor(private http: HttpClient) {}

  /**
   * Авторизация пользователя.
   * Ожидает, что API возвращает { token, user }.
   */
  login(credentials: LoginRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.apiUrl}login`, credentials).pipe(
      tap((response) => {
        this.setToken(response.token);
        this.setUser(response.user);
        this.currentUserSubject.next(response.user);
        this.isAuthenticatedSubject.next(true);
      }),
    );
  }

  login_formdata(credentials: LoginRequest): Observable<AuthResponse> {
    const formData = new FormData();
    formData.append('username', credentials.username);
    formData.append('password', credentials.password);

    const formDataResponse: AuthResponse = {
      token: 'form-data-token',
      user: { id: 1, username: credentials.username, fullName: credentials.username },
    };

    return this.http.post<any>(`${this.apiUrl}login`, formData).pipe(
      tap(() => {
        this.setToken(formDataResponse.token);
        this.setUser(formDataResponse.user);
        this.currentUserSubject.next(formDataResponse.user);
        this.isAuthenticatedSubject.next(true);
      }),
    );
  }

  login_mock(credentials: LoginRequest): Observable<AuthResponse> {
    // Мок для разработки — заменить на реальный запрос в продакшене
    const mockResponse: AuthResponse = {
      token: 'mock-jwt-token',
      user: { id: 1, username: credentials.username, fullName: credentials.username },
    };
    return of(mockResponse).pipe(
      delay(500),
      tap((response) => {
        this.setToken(response.token);
        this.setUser(response.user);
        this.currentUserSubject.next(response.user);
        this.isAuthenticatedSubject.next(true);
      }),
    );
  }
  /**
   * Выход из системы: очищает хранилище и уведомляет подписчиков.
   */
  logout(): void {
    localStorage.removeItem(this.TOKEN_KEY);
    localStorage.removeItem(this.USER_KEY);
    this.currentUserSubject.next(null);
    this.isAuthenticatedSubject.next(false);
  }

  /**
   * Возвращает текущий токен (для интерцептора).
   */
  getToken(): string | null {
    return localStorage.getItem(this.TOKEN_KEY);
  }

  /**
   * Возвращает текущего пользователя.
   */
  getCurrentUser(): User | null {
    return this.currentUserSubject.value;
  }

  /**
   * Проверка наличия токена (используется в guard).
   */
  isAuthenticated(): boolean {
    return this.hasToken();
  }

  // ----- Приватные методы -----

  private setToken(token: string): void {
    localStorage.setItem(this.TOKEN_KEY, token);
  }

  private setUser(user: User): void {
    localStorage.setItem(this.USER_KEY, JSON.stringify(user));
  }

  private getUserFromStorage(): User | null {
    const userJson = localStorage.getItem(this.USER_KEY);
    if (!userJson) return null;
    try {
      return JSON.parse(userJson) as User;
    } catch {
      return null;
    }
  }

  private hasToken(): boolean {
    return !!localStorage.getItem(this.TOKEN_KEY);
  }
}
