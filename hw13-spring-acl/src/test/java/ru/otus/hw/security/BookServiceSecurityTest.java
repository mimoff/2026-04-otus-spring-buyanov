package ru.otus.hw.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.TestUtils;
import ru.otus.hw.dto.BookDto;
import ru.otus.hw.dto.BookUpdateDto;
import ru.otus.hw.repositories.BookRepository;
import ru.otus.hw.services.AclServiceWrapperServiceImpl;
import ru.otus.hw.services.BookService;
import ru.otus.hw.services.BookServiceImpl;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Method security сервиса книг должен")
@DataJpaTest
@Transactional(propagation = Propagation.NEVER)
@Import({BookServiceImpl.class, AclServiceWrapperServiceImpl.class, AclConfig.class})
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class BookServiceSecurityTest {

    private static final Long AUTHOR_1_ID = 1L;

    private static final Long GENRE_1_ID = 1L;

    private static final Long BOOK_1_ID = 1L;

    private static final Long BOOK_2_ID = 2L;

    @Autowired
    private BookService bookService;

    @Autowired
    private BookRepository bookRepository;

    @Test
    @WithMockUser(username = "guest", roles = "GUEST")
    @DisplayName("фильтровать список книг по READ-доступу")
    void shouldFilterBooksByReadPermission() {
        var newBook = new BookUpdateDto(0L, "new title", AUTHOR_1_ID, Set.of(GENRE_1_ID));
        var actualBook = bookService.insert(newBook);

        var books = bookService.findAll();

        assertThat(books)
                .extracting(BookDto::getId)
                .containsExactly(actualBook.getId());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("фильтровать список книг по READ-доступу")
    void shouldFilterBooksByRolePermission() {
        var expectedBooks = TestUtils.getDbBooksDto();
        var books = bookService.findAll();

        assertThat(books)
                .containsExactlyElementsOf(expectedBooks);
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("разрешать чтение доступной книги запросом по id")
    void shouldAllowPermittedBookRead() {
        var newBook = new BookUpdateDto(0L, "new title", AUTHOR_1_ID, Set.of(GENRE_1_ID));
        var insertedBook = bookService.insert(newBook);

        var actualBook = bookService.findById(insertedBook.getId());

        assertThat(actualBook)
                .usingRecursiveComparison()
                .isEqualTo(insertedBook);
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("запрещать чтение чужой книги запросом по id")
    void shouldDenyForbiddenBookRead() {
        assertThatThrownBy(() -> bookService.findById(BOOK_1_ID))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("разрешать изменение книги с WRITE-доступом")
    void shouldAllowBookUpdateWithWritePermission() {
        var newBook = new BookUpdateDto(0L, "new title", AUTHOR_1_ID, Set.of(GENRE_1_ID));
        var insertedBook = bookService.insert(newBook);
        var updatedBook = new BookUpdateDto(insertedBook.getId(), "updated title", AUTHOR_1_ID, Set.of(GENRE_1_ID));
        var actualBook = bookService.update(updatedBook);

        assertThat(actualBook.getTitle())
                .isEqualTo(updatedBook.getTitle());
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("запрещать изменение книги без WRITE-доступа")
    void shouldDenyBookUpdateWithoutWritePermission() {
        var updatedBook = new BookUpdateDto(BOOK_1_ID, "updated title", AUTHOR_1_ID, Set.of(GENRE_1_ID));
        assertThatThrownBy(() -> bookService.update(updatedBook))
                .isInstanceOf(AccessDeniedException.class);

        assertThat(bookRepository.findById(BOOK_1_ID)).isPresent();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    @DisplayName("разрешать admin удаление без отдельных ACL-записей")
    void shouldAllowAdminCreateAndDelete() {
        bookService.deleteById(BOOK_1_ID);

        assertThat(bookRepository.findById(BOOK_1_ID)).isEmpty();
    }

    @Test
    @WithMockUser(username = "user", roles = "USER")
    @DisplayName("запрещать удаление без DELETE-доступа")
    void shouldDenyBookDeleteWithoutDeletePermission() {
        assertThatThrownBy(() -> bookService.deleteById(BOOK_2_ID))
                .isInstanceOf(AccessDeniedException.class);

        assertThat(bookRepository.findById(BOOK_2_ID)).isPresent();
    }
}
