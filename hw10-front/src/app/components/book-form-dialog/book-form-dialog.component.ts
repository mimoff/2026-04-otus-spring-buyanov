import { Component, Inject } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogActions, MatDialogContent, MatDialogRef, MatDialogTitle} from '@angular/material/dialog';
import { Book, BookRequest } from '../../models/book.model';
import { MatError, MatFormField, MatInput, MatLabel } from '@angular/material/input';
import {MatButton} from "@angular/material/button";
import {MatOption, MatSelect} from "@angular/material/select";
import {Author} from "../../models/author.model";
import {Genre} from "../../models/genre.model";
import {AuthorService} from "../../services/author.service";
import {GenreService} from "../../services/genre.service";

export interface DialogData {
  mode: 'add' | 'edit';
  book?: Book;
}

@Component({
  selector: 'app-book-form-dialog',
  templateUrl: './book-form-dialog.component.html',
  styleUrls: ['./book-form-dialog.component.css'],
  imports: [
    MatDialogContent,
    MatFormField,
    MatLabel,
    MatError,
    MatDialogActions,
    ReactiveFormsModule,
    MatInput,
    MatButton,
    MatDialogTitle,
    MatSelect,
    MatOption,
  ],
})
export class BookFormDialogComponent {
  form: FormGroup;
  authors: Author[] = [];
  genres: Genre[] = [];
  isEditMode: boolean;
  isLoading = false;

  constructor(
    private fb: FormBuilder,
    private authorService: AuthorService,
    private genreService: GenreService,
    public dialogRef: MatDialogRef<BookFormDialogComponent>,
    @Inject(MAT_DIALOG_DATA) public data: DialogData,
  ) {
    this.isEditMode = data.mode === 'edit';
    this.form = this.fb.group({
      title: [data.book?.title || '', [Validators.required, Validators.minLength(2)]],
      author: [null, Validators.required],
      genres: [[], Validators.required],
    });
  }

  ngOnInit(): void {
    this.loadAuthorsAndGenres();
  }

  private loadAuthorsAndGenres(): void {
    this.isLoading = true;
    // Загружаем авторов и жанры параллельно
    Promise.all([
      this.authorService.getAuthors().toPromise(),
      this.genreService.getGenres().toPromise(),
    ])
      .then(([authors, genres]) => {
        this.authors = authors || [];
        this.genres = genres || [];
        this.isLoading = false;
        // Если редактирование, заполняем форму
        if (this.isEditMode && this.data.book) {
          this.patchForm(this.data.book);
        }
      })
      .catch((err) => {
        this.isLoading = false;
        console.error('Ошибка загрузки справочников', err);
      });
  }

  private patchForm(book: Book): void {
    this.form.patchValue({
      title: book.title,
      author: book.author, // объект Author
      genres: book.genres, // массив объектов Genre
    });
  }

  onCancel(): void {
    this.dialogRef.close();
  }

  _onSubmit(): void {
    if (this.form.invalid) {
      return;
    }
    const result = {
      ...(this.isEditMode ? { id: this.data.book!.id } : {}),
      title: this.form.value.title,
    };
    this.dialogRef.close(result);
  }

  onSubmit(): void {
    if (this.form.invalid) {
      return;
    }

    const formValue = this.form.value;
    // Собираем DTO для отправки
    const bookRequest: BookRequest = {
      title: formValue.title,
      authorId: formValue.author.id, // берём id выбранного автора
      genreIds: formValue.genres.map((g: Genre) => g.id), // массив id жанров
    };
    this.dialogRef.close(bookRequest);
  }

  // Для отображения в select (сравнение объектов)
  compareAuthors(a1: Author, a2: Author): boolean {
    return a1 && a2 ? a1.id === a2.id : a1 === a2;
  }

  compareGenres(g1: Genre, g2: Genre): boolean {
    return g1 && g2 ? g1.id === g2.id : g1 === g2;
  }
}
