import { Component, OnInit, ViewChild } from '@angular/core';
import {MatCell, MatCellDef, MatColumnDef, MatHeaderCell, MatHeaderCellDef,
  MatHeaderRow, MatHeaderRowDef, MatRow, MatRowDef, MatTable, MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatSnackBar } from '@angular/material/snack-bar';
import { AuthorService } from '../../services/author.service';
import { Author } from '../../models/author.model';
import { MatProgressSpinner } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-author-list',
  templateUrl: './author-list.component.html',
  styleUrls: ['./author-list.component.css'],
  imports: [
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
    MatHeaderRow,
    MatRow,
    MatCellDef,
  ],
})
export class AuthorListComponent implements OnInit {
  displayedColumns: string[] = ['id', 'fullName'];
  dataSource = new MatTableDataSource<Author>([]);
  isLoading = false;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private authorService: AuthorService,
    private snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.loadAuthors();
  }

  loadAuthors(): void {
    this.isLoading = true;
    this.authorService.getAuthors().subscribe({
      next: (authors) => {
        this.dataSource.data = authors;
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

  private showSnackBar(message: string): void {
    this.snackBar.open(message, 'Закрыть', {
      duration: 3000,
      verticalPosition: 'top',
    });
  }

}
