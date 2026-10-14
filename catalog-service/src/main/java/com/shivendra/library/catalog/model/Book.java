package com.shivendra.library.catalog.model;

import com.shivendra.library.catalog.exception.NoCopiesAvailableException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "books")
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, unique = true)
    private String isbn;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "author_id")
    private Author author;

    private int totalCopies;

    private int availableCopies;

    /** Optimistic locking: two concurrent reservations of the last copy cannot both succeed. */
    @Version
    private Long version;

    protected Book() {
    }

    public Book(String title, String isbn, Author author, int totalCopies) {
        this.title = title;
        this.isbn = isbn;
        this.author = author;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
    }

    /** Domain rule lives in the entity: a copy can only be reserved while one is available. */
    public void reserveCopy() {
        if (availableCopies <= 0) {
            throw new NoCopiesAvailableException(id);
        }
        availableCopies--;
    }

    public void releaseCopy() {
        if (availableCopies < totalCopies) {
            availableCopies++;
        }
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getIsbn() {
        return isbn;
    }

    public Author getAuthor() {
        return author;
    }

    public int getTotalCopies() {
        return totalCopies;
    }

    public int getAvailableCopies() {
        return availableCopies;
    }
}
