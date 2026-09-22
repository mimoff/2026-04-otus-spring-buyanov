import { Component } from '@angular/core';
import { MatTab, MatTabGroup } from '@angular/material/tabs';
import { AuthorListComponent } from '../author-list/author-list.component';
import { BookListComponent } from '../book-list/book-list.component';
import { GenreListComponent } from '../genre-list/genre-list.component';
import { CommentListComponent } from '../comment-list/comment-list.component';

@Component({
  imports: [
    AuthorListComponent,
    MatTabGroup,
    MatTab,
    BookListComponent,
    GenreListComponent,
    CommentListComponent,
  ],
  selector: 'app-master-form',
  styleUrl: './master-form.component.css',
  templateUrl: './master-form.component.html',
})
export class MasterFormComponent {
}
