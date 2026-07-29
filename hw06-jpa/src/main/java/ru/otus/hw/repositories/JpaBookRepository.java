package ru.otus.hw.repositories;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.otus.hw.models.Book;

import java.util.List;
import java.util.Optional;

import static org.springframework.data.jpa.repository.EntityGraph.EntityGraphType.FETCH;

@Repository
public class JpaBookRepository implements BookRepository {
    @PersistenceContext
    private final EntityManager em;

    public JpaBookRepository(EntityManager em) {
        this.em = em;
    }

    @Override
    public Optional<Book> findById(long id) {
            EntityGraph<?> entityGraphAuthorGenres = em.getEntityGraph("book-author-genres-entity-graph");
            TypedQuery<Book> query = em.createQuery("select b from Book b where b.id = :id", Book.class);
            query.setParameter("id", id);
            query.setHint(FETCH.getKey(), entityGraphAuthorGenres);
            return query.getResultList().stream().findFirst();
    }

    @Override
    public List<Book> findAll() {
        EntityGraph<?> entityGraphAuthor = em.getEntityGraph("book-author-entity-graph");
        TypedQuery<Book> query = em.createQuery("select distinct b from Book b ", Book.class);
        query.setHint(FETCH.getKey(), entityGraphAuthor);
        return query.getResultList();
    }

    @Override
    @Transactional
    public Book save(Book book) {
        if (book.getId() == 0) {
            em.persist(book);
            return book;
        }
        return em.merge(book);
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        var book = em.getReference(Book.class, id);
        if (book != null) {
            em.remove(book);
        }
    }
}
