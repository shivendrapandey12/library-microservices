package com.shivendra.library.catalog.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.shivendra.library.catalog.exception.NoCopiesAvailableException;
import org.junit.jupiter.api.Test;

class BookTest {

    private Book bookWithCopies(int copies) {
        return new Book("Clean Code", "9780132350884", new Author("Robert C. Martin"), copies);
    }

    @Test
    void reserveDecrementsAvailableCopies() {
        Book book = bookWithCopies(2);
        book.reserveCopy();
        assertEquals(1, book.getAvailableCopies());
    }

    @Test
    void reserveFailsWhenNoCopiesLeft() {
        Book book = bookWithCopies(1);
        book.reserveCopy();
        assertThrows(NoCopiesAvailableException.class, book::reserveCopy);
        assertEquals(0, book.getAvailableCopies());
    }

    @Test
    void releaseNeverExceedsTotalCopies() {
        Book book = bookWithCopies(1);
        book.releaseCopy();
        assertEquals(1, book.getAvailableCopies());
    }

    @Test
    void reserveThenReleaseRestoresCount() {
        Book book = bookWithCopies(2);
        book.reserveCopy();
        book.releaseCopy();
        assertEquals(2, book.getAvailableCopies());
    }
}
