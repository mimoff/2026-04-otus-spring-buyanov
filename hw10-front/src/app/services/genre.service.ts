import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Genre } from '../models/genre.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class GenreService {
  private apiUrl = environment.apiUrl + 'api/genres';

  constructor(private http: HttpClient) {}

  // Получить все жанры
  getGenres(): Observable<Genre[]> {
    return this.http.get<Genre[]>(this.apiUrl);
  }
}
