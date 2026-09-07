import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Book, BookRequest } from '../models/book.model';
import { environment } from '../../environments/environment';

@Injectable({
  providedIn: 'root',
})
export class BookService {
  private apiUrl = environment.apiUrl + 'api/books';

  constructor(private http: HttpClient) {}

  // Получить все книги
  getBooks(): Observable<Book[]> {
    return this.http.get<Book[]>(this.apiUrl);
  }

  // Получить книгу по id
  getBook(id: number): Observable<Book> {
    return this.http.get<Book>(`${this.apiUrl}/${id}`);
  }

  // Создание книги с использованием DTO
  createBook(bookRequest: BookRequest): Observable<Book> {
    return this.http.post<Book>(`${this.apiUrl}`, bookRequest);
  }

  // Обновление книги
  updateBook(id: number, bookRequest: BookRequest): Observable<Book> {
    return this.http.put<Book>(`${this.apiUrl}/${id}`, bookRequest);
  }

  // Удалить книгу
  deleteBook(id: number): Observable<void> {
    return this.http.delete<void>(`${this.apiUrl}/${id}`);
  }
}
