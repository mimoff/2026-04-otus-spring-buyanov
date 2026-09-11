import {Book} from './book.model';

export interface Comment {
  id: number;
  text: string;
  book: Book;
}
