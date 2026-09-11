import { Component, OnInit, ViewChild } from '@angular/core';
import {MatCell, MatCellDef, MatColumnDef, MatHeaderCell, MatHeaderCellDef,
  MatHeaderRow, MatHeaderRowDef, MatRow, MatRowDef, MatTable, MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { MatSort } from '@angular/material/sort';
import { MatSnackBar } from '@angular/material/snack-bar';
import { GenreService } from '../../services/genre.service';
import { Genre } from '../../models/genre.model';
import { MatProgressSpinner } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-genre-list',
  templateUrl: './genre-list.component.html',
  styleUrls: ['./genre-list.component.css'],
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
export class GenreListComponent implements OnInit {
  displayedColumns: string[] = ['id', 'name'];
  dataSource = new MatTableDataSource<Genre>([]);
  isLoading = false;

  @ViewChild(MatPaginator) paginator!: MatPaginator;
  @ViewChild(MatSort) sort!: MatSort;

  constructor(
    private genreService: GenreService,
    private snackBar: MatSnackBar,
  ) {}

  ngOnInit(): void {
    this.loadGenres();
  }

  loadGenres(): void {
    this.isLoading = true;
    this.genreService.getGenres().subscribe({
      next: (genres) => {
        this.dataSource.data = genres;
        this.dataSource.paginator = this.paginator;
        this.dataSource.sort = this.sort;
        this.isLoading = false;
      },
      error: (err) => {
        this.isLoading = false;
        this.showSnackBar('Ошибка загрузки жанров: ' + err.message);
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
