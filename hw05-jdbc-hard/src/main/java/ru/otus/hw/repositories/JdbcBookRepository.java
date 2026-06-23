package ru.otus.hw.repositories;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.otus.hw.exceptions.EntityNotFoundException;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
@RequiredArgsConstructor
public class JdbcBookRepository implements BookRepository {

    private final NamedParameterJdbcOperations jdbc;

    private final AuthorRepository authorRepository;

    private final GenreRepository genreRepository;

    @Override
    public Optional<Book> findById(long id) {
        Map<String, Object> parameters = Collections.singletonMap("id", id);
        String sql = """
				select books.id as book_id, books.title,
				authors.id as author_id, authors.full_name,
				genres.id as genre_id, genres.name
				from books
				left join authors on authors.id = books.author_id
				left join books_genres on books.id = books_genres.book_id
				left join genres on books_genres.genre_id = genres.id 
				where books.id = :id""";
        try {
            Book book = jdbc.query(sql, parameters, new BookResultSetExtractor());
            return Optional.ofNullable(book);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Book> findAll() {
        var genres = genreRepository.findAll();
        var books = getAllBooksWithoutGenres();
        var relations = getAllGenreRelations();
        mergeBooksInfo(books, genres, relations);
        return books;
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            return insert(book);
        }
        return update(book);
    }

    @Override
    public void deleteById(long id) {
        Map<String, Object> parameters = Collections.singletonMap("id", id);
        jdbc.update("delete from books where id = :id", parameters);

        removeGenresRelationsForId(id);
    }

    private List<Book> getAllBooksWithoutGenres() {
        return jdbc.query("select id, title, author_id from books", new BookRowMapper());
    }

    private List<BookGenreRelation> getAllGenreRelations() {
        return jdbc.query("select book_id, genre_id from books_genres", new BookGenreRelationRowMapper());
    }

    private void mergeBooksInfo(List<Book> booksWithoutGenres, List<Genre> genres,
                                List<BookGenreRelation> relations) {
        // Добавить книгам (booksWithoutGenres) жанры (genres) в соответствии со связями (relations)
        for (Book book: booksWithoutGenres) {
            List<Genre> genreList = new ArrayList<>();
            relations.stream()
                    .filter(r -> r.bookId() == book.getId())
                    .forEach(r -> {
                        genres.stream()
                                .filter(g -> g.getId() == r.genreId())
                                .forEach(genreList::add);
                    });
            book.setGenres(genreList);
        }
    }

    private Book insert(Book book) {
        var keyHolder = new GeneratedKeyHolder();

        var parameters = new MapSqlParameterSource();
        parameters.addValue("title", book.getTitle());
        parameters.addValue("author_id", book.getAuthor().getId());
        jdbc.update("insert into books(title, author_id) values (:title, :author_id)", parameters,
                keyHolder, new String[]{"id"});

        //noinspection DataFlowIssue
        book.setId(keyHolder.getKeyAs(Long.class));
        batchInsertGenresRelationsFor(book);
        return book;
    }

    private Book update(Book book) {
        if (findById(book.getId()).isEmpty()) {
            throw new EntityNotFoundException("Not found book to update with id " + book.getId());
        }
        var parameters = new MapSqlParameterSource();
        parameters.addValue("id", book.getId());
        parameters.addValue("title", book.getTitle());
        parameters.addValue("author_id", book.getAuthor().getId());
        jdbc.update("update books set title = :title, author_id = :author_id where id = :id", parameters);

        // Выбросить EntityNotFoundException если не обновлено ни одной записи в БД
        removeGenresRelationsFor(book);
        batchInsertGenresRelationsFor(book);

        return book;
    }

    private void batchInsertGenresRelationsFor(Book book) {
        SqlParameterSource[] sqlParameterSources = book.getGenres().stream()
                .map(genre -> {
                    var parameters = new MapSqlParameterSource();
                    parameters.addValue("book_id", book.getId());
                    parameters.addValue("genre_id", genre.getId());
                    return parameters;
                })
                .toArray(SqlParameterSource[]::new);
        jdbc.batchUpdate("insert into books_genres(book_id, genre_id) values (:book_id, :genre_id)",
                sqlParameterSources);
    }

    private void removeGenresRelationsFor(Book book) {
        removeGenresRelationsForId(book.getId());
    }

    private void removeGenresRelationsForId(long id) {
        Map<String, Object> parameters = Collections.singletonMap("id", id);
        jdbc.update("delete books_genres where book_id = :id", parameters);
    }

    private class BookRowMapper implements RowMapper<Book> {

        @Override
        public Book mapRow(ResultSet rs, int rowNum) throws SQLException {
            long id = rs.getLong("id");
            String title = rs.getString("title");
            long authorId = rs.getLong("author_id");
            return new Book(id, title, authorRepository.findById(authorId).get(), new ArrayList<>());
        }
    }

    // Использовать для findById
    @SuppressWarnings("ClassCanBeRecord")
    @RequiredArgsConstructor
    private static class BookResultSetExtractor implements ResultSetExtractor<Book> {

        @Override
        public Book extractData(ResultSet rs) throws SQLException, DataAccessException {
            Book book = null;
            if (rs.next()) {
                book = extractDataFromResultSet(rs);
            }
            return book;
        }

        private Book extractDataFromResultSet(ResultSet rs) throws SQLException {
            Book book = getBookFromResultSet(rs);
            while (rs.next()) {
                Genre genre = getGenreFromResultSet(rs);
                book.getGenres().add(genre);
            }
            return book;
        }

        private Book getBookFromResultSet(ResultSet rs) throws SQLException {
            long bookId = rs.getLong("book_id");
            String title = rs.getString("title");
            Author author = getAuthorFromResultSet(rs);
            Genre firstGenre = getGenreFromResultSet(rs);
            List<Genre> genres = new ArrayList<>();
            genres.add(firstGenre);
            return new Book(bookId, title, author, genres);
        }

        private Author getAuthorFromResultSet(ResultSet rs) throws SQLException {
            long id = rs.getLong("author_id");
            String authorName = rs.getString("full_name");
            return new Author(id, authorName);
        }

        private Genre getGenreFromResultSet(ResultSet rs) throws SQLException {
            long id = rs.getLong("genre_id");
            String name = rs.getString("name");
            return new Genre(id, name);
        }
    }

    private record BookGenreRelation(long bookId, long genreId) {
    }

    private class BookGenreRelationRowMapper implements RowMapper<BookGenreRelation> {
        @Override
        public BookGenreRelation mapRow(ResultSet rs, int rowNum) throws SQLException {
            long bookId = rs.getLong("book_id");
            long genreId = rs.getLong("genre_id");
            return new BookGenreRelation(bookId, genreId);
        }
    }
}
