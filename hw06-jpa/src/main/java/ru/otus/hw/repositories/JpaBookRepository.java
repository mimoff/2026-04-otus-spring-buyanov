package ru.otus.hw.repositories;

import jakarta.persistence.*;
import org.springframework.stereotype.Repository;
import ru.otus.hw.models.Author;
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
        return Optional.ofNullable(em.find(Book.class, id));
    }

    @Override
    public List<Book> findAll() {
        //EntityGraph<?> entityGraph = em.getEntityGraph("books-entity-graph");
        //TypedQuery<Book> query = em.createQuery("select distinct b from Book b left join fetch b.", Book.class);
        //query.setHint(FETCH.getKey(), entityGraph);
        var query = em.createQuery("select b from Book b", Book.class);
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
