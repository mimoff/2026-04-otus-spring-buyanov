package ru.otus.hw.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.models.Author;
import ru.otus.hw.models.Book;
import ru.otus.hw.models.Genre;
import ru.otus.hw.repositories.AuthorRepository;
import ru.otus.hw.repositories.CommentRepository;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.repositories.GenreRepository;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.BookServiceImpl;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Method security сервиса книг должен")
@DataJpaTest
//@Transactional(propagation = Propagation.NEVER)
@Import({BookServiceImpl.class, AclConfig.class})
class BookServiceSecurityTest {

    private static final Long AUTHOR_1_ID = 1L;

    private static final Long AUTHOR_2_ID = 2L;

    private static final Long GENRE_1_ID = 1L;

    private static final Long GENRE_2_ID = 2L;

    private static final Long BOOK_1_ID = 1L;

    private static final Long BOOK_2_ID = 2L;

    private static final Long BOOK_3_ID = 3L;

    private static final String COMMENT_1_ID = "c1";

    @Autowired
    private BookService bookService;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CommentRepository bookCommentRepository;

/*    @BeforeEach
    void setUpData() {
        domainPermissionRepository.deleteAll();
        bookCommentRepository.deleteAll();
        bookRepository.deleteAll();
        genreRepository.deleteAll();
        authorRepository.deleteAll();

        var author1 = authorRepository.save(new Author(AUTHOR_1_ID, "Author_1"));
        var author2 = authorRepository.save(new Author(AUTHOR_2_ID, "Author_2"));
        var genre1 = genreRepository.save(new Genre(GENRE_1_ID, "Genre_1"));
        var genre2 = genreRepository.save(new Genre(GENRE_2_ID, "Genre_2"));

        var book1 = bookRepository.save(new Book(BOOK_1_ID, "BookTitle_1", author1, List.of(genre1)));
        bookRepository.save(new Book(BOOK_2_ID, "BookTitle_2", author2, List.of(genre2)));
        bookRepository.save(new Book(BOOK_3_ID, "BookTitle_3", author1, List.of(genre1, genre2)));
        bookCommentRepository.save(new BookComment(COMMENT_1_ID, "Comment_1", book1));

        domainPermissionRepository.saveAll(List.of(
                permission("perm-user-book-b1", "user", DomainPermissionTargetType.BOOK, BOOK_1_ID,
                        List.of(DomainPermissionAction.READ, DomainPermissionAction.WRITE)),
                permission("perm-user-book-b2", "user", DomainPermissionTargetType.BOOK, BOOK_2_ID,
                        List.of(DomainPermissionAction.READ)),
                permission("perm-reader-book-b3", "reader", DomainPermissionTargetType.BOOK, BOOK_3_ID,
                        List.of(DomainPermissionAction.READ))
        ));
    }*/

    @Test
    @WithMockUser(username = "guest", roles = "GUEST")
    @DisplayName("фильтровать список книг по READ-доступу")
    void shouldFilterBooksByReadPermission() {
        var books = bookService.findAll();

        assertThat(books)
                .extracting(BookDto::getId)
                .containsExactly(BOOK_1_ID, BOOK_2_ID, BOOK_3_ID);
    }

    @Test
    @WithMockUser(username = "reader", roles = "ADMIN")
    @DisplayName("разрешать чтение доступной книги")
    void shouldAllowPermittedBookRead() {
        var book = bookService.findById(BOOK_3_ID);

        assertThat(book.getTitle())
                .isEqualTo("BookTitle_3");
    }

    @Test
    @WithMockUser(username = "reader", roles = "USER")
    @DisplayName("запрещать чтение чужой книги прямым вызовом сервиса")
    void shouldDenyForbiddenBookRead() {
        assertThatThrownBy(() -> bookService.findById(BOOK_1_ID))
                .isInstanceOf(AccessDeniedException.class);
    }

/*    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("разрешать изменение книги с WRITE-доступом")
    void shouldAllowBookUpdateWithWritePermission() {
        var book = bookService.update(BOOK_1_ID, "BookTitle_Updated", AUTHOR_2_ID, Set.of(GENRE_2_ID));

        assertThat(book.getTitle()).isEqualTo("BookTitle_Updated");
        assertThat(bookRepository.findById(BOOK_1_ID).orElseThrow().getTitle()).isEqualTo("BookTitle_Updated");
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("запрещать изменение книги только с READ-доступом")
    void shouldDenyBookUpdateWithoutWritePermission() {
        assertThatThrownBy(() -> bookService.update(BOOK_2_ID, "BookTitle_Updated", AUTHOR_1_ID, Set.of(GENRE_1_ID)))
                .isInstanceOf(AccessDeniedException.class);

        assertThat(bookRepository.findById(BOOK_2_ID).orElseThrow().getTitle()).isEqualTo("BookTitle_2");
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("запрещать удаление без DELETE-доступа")
    void shouldDenyBookDeleteWithoutDeletePermission() {
        assertThatThrownBy(() -> bookService.deleteById(BOOK_1_ID))
                .isInstanceOf(AccessDeniedException.class);

        assertThat(bookRepository.findById(BOOK_1_ID)).isPresent();
        assertThat(bookCommentRepository.findById(COMMENT_1_ID)).isPresent();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("разрешать admin создание и удаление без отдельных ACL-записей")
    void shouldAllowAdminCreateAndDelete() {
        var insertedBook = bookService.insert("BookTitle_New", AUTHOR_1_ID, Set.of(GENRE_1_ID));

        assertThat(insertedBook.getId()).isNotBlank();

        bookService.deleteById(BOOK_2_ID);

        assertThat(bookRepository.findById(BOOK_2_ID)).isEmpty();
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("запрещать обычному пользователю создание книги")
    void shouldDenyCreateForNonAdmin() {
        assertThatThrownBy(() -> bookService.insert("BookTitle_New", AUTHOR_1_ID, Set.of(GENRE_1_ID)))
                .isInstanceOf(AccessDeniedException.class);
    }*/

/*    private DomainPermission permission(String id,
                                        String username,
                                        DomainPermissionTargetType targetType,
                                        String targetId,
                                        List<DomainPermissionAction> actions) {
        return new DomainPermission(id, username, targetType, targetId, actions);
    }*/
}
