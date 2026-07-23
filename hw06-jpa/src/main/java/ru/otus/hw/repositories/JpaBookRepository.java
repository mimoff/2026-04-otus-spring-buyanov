package ru.otus.hw.repositories;

import jakarta.persistence.EntityGraph;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;
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
        try {
            EntityGraph<?> entityGraphAuthor = em.getEntityGraph("book-author-entity-graph");
            EntityGraph<?> entityGraphGenres = em.getEntityGraph("book-genres-entity-graph");
            TypedQuery<Book> query = em.createQuery("select distinct b from Book b where b.id = :id", Book.class);
            query.setParameter("id", id);
            query.setHint(FETCH.getKey(), entityGraphAuthor);
            query.setHint(FETCH.getKey(), entityGraphGenres);
            return Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Book> findAll() {
        EntityGraph<?> entityGraphAuthor = em.getEntityGraph("book-author-entity-graph");
        EntityGraph<?> entityGraphGenres = em.getEntityGraph("book-genres-entity-graph");
        TypedQuery<Book> query = em.createQuery("select distinct b from Book b ", Book.class);
        query.setHint(FETCH.getKey(), entityGraphAuthor);
        query.setHint(FETCH.getKey(), entityGraphGenres);
        return query.getResultList();
    }

    @Override
    public Book save(Book book) {
        if (book.getId() == 0) {
            em.persist(book);
            return book;
        }
        return em.merge(book);
    }

    @Override
    public void deleteById(long id) {
        var book = em.find(Book.class, id);
        if (book != null) {
            em.remove(book);
        }
    }
}
