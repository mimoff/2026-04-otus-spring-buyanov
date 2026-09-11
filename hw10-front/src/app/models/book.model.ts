import {Author} from './author.model';
import {Genre} from './genre.model';

export interface Book {
  id: number;
  title: string;
  author: Author;
  genres: Genre[];
}

// DTO для отправки на сервер (если API ожидает id, а не полные объекты)
export interface BookRequest {
  title: string;
  authorId: number;
  genreIds: number[];
}
