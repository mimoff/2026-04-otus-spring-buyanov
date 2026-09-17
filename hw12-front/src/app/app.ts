import { Component, signal } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { AuthorListComponent } from './components/author-list/author-list.component';
import { MatTab, MatTabGroup } from '@angular/material/tabs';
import {NavigationComponent} from './components/navigation/navigation.component';
import { BookListComponent } from './components/book-list/book-list.component';
import { GenreListComponent } from './components/genre-list/genre-list.component';
import { CommentListComponent } from './components/comment-list/comment-list.component';

@Component({
  imports: [
    RouterOutlet,
    AuthorListComponent,
    MatTabGroup,
    MatTab,
    NavigationComponent,
    BookListComponent,
    GenreListComponent,
    CommentListComponent,
  ],
  selector: 'app-root',
  styleUrl: './app.css',
  templateUrl: './app.html',
})
export class App {
  protected readonly title = signal('hw10-front');
}
