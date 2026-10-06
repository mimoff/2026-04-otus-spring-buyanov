import { User } from './user.model';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface AuthResponse {
  token: string; // JWT или другой токен
  user: User; // данные пользователя
}
