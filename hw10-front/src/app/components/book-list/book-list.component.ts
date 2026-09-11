import { Component, OnInit, ViewChild } from '@angular/core';
import {MatCell, MatCellDef, MatColumnDef, MatHeaderCell, MatHeaderCellDef,
  MatHeaderRow, MatHeaderRowDef, MatRow, MatRowDef, MatTable, MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatDialog } from '@angular/material/dialog';
import { MatSnackBar } from '@angular/material/snack-bar';
import { BookService } from '../../services/book.service';
import { Book, BookRequest } from '../../models/book.model';
import { BookFormDialogComponent } from '../book-form-dialog/book-form-dialog.component';
import { MatIcon } from '@angular/material/icon';
import { MatProgressSpinner } from '@angular/material/progress-spinner';
import { MatButton, MatIconButton} from '@angular/material/button';
import { Genre } from '../../models/genre.model';

@Component({
  selector: 'app-book-list',
  templateUrl: './book-list.component.html',
  styleUrls: ['./book-list.component.css'],
  imports: [
    MatIcon,
    MatProgressSpinner,
    MatTable,
    MatPaginator,
    MatHeaderRowDef,
    MatColumnDef,
    MatRowDef,
    MatSort,
    MatHeaderCell,
    MatHeaderCellDef,
    MatCell,
    MatIconButton,
    MatHeaderRow,
    MatRow,
    MatCellDef,
    MatButton,
  ],
})
export class BookListComponent implements OnInit {
  displayedColumns: string[] = ['id', 'title', 'author', 'genres', 'actions'];
  dataSource = new MatTableDataSource<Book>([]);
  isLoading = false;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private bookService: BookService,
    private dialog: MatDialog,
    private snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.loadBooks();
  }

  loadBooks(): void {
    this.isLoading = true;
    this.bookService.getBooks().subscribe({
      next: (books) => {
        this.dataSource.data = books;
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.showSnackBar('Ошибка загрузки книг: ' + err.message);
      },
    });
  }

  openAddDialog(): void {
    const dialogRef = this.dialog.open(BookFormDialogComponent, {
      width: '500px',
      data: { mode: 'add' }
    });

    dialogRef.afterClosed().subscribe((result: BookRequest) => {
      if (result) {
        this.createBook(result);
      }
    });
  }

  openEditDialog(book: Book): void {
    const dialogRef = this.dialog.open(BookFormDialogComponent, {
      width: '500px',
      data: { mode: 'edit', book }
    });

    dialogRef.afterClosed().subscribe((result: BookRequest) => {
      if (result) {
        this.updateBook(book.id, result);
      }
    });
  }

  createBook(bookRequest: BookRequest): void {
    this.isLoading = true;
    this.bookService.createBook(bookRequest).subscribe({
      next: (newBook) => {
        this.dataSource.data = [...this.dataSource.data, newBook];
        this.isLoading = false;
        this.showSnackBar('Книга добавлена');
      },
      error: (err) => {
        this.isLoading = false;
        this.showSnackBar('Ошибка добавления: ' + err.message);
      }
    });
  }

  updateBook(id: number, bookRequest: BookRequest): void {
    this.isLoading = true;
    this.bookService.updateBook(id, bookRequest).subscribe({
      next: (updatedBook) => {
        const index = this.dataSource.data.findIndex(b => b.id === id);
        if (index !== -1) {
          const newData = [...this.dataSource.data];
          newData[index] = updatedBook;
          this.dataSource.data = newData;
        }
        this.isLoading = false;
        this.showSnackBar('Книга обновлена');
      },
      error: (err) => {
        this.isLoading = false;
        this.showSnackBar('Ошибка обновления: ' + err.message);
      }
    });
  }

  // Удалить книгу
  deleteBook(id: number): void {
    if (confirm('Вы уверены, что хотите удалить эту книгу?')) {
      this.isLoading = true;
      this.bookService.deleteBook(id).subscribe({
        next: () => {
          this.dataSource.data = this.dataSource.data.filter((b) => b.id !== id);
          this.isLoading = false;
          this.showSnackBar('Книга удалена');
        },
        error: (err) => {
          this.isLoading = false;
          this.showSnackBar('Ошибка удаления: ' + err.message);
        },
      });
    }
  }

  private showSnackBar(message: string): void {
    this.snackBar.open(message, 'Закрыть', {
      duration: 3000,
      verticalPosition: 'top',
    });
  }

  formatGenres(genres: Genre[]) {
    return genres.map((genre) => genre.name).join(', ');
  }
}
