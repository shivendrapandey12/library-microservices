package com.shivendra.library.catalog.service;

import com.shivendra.library.catalog.dto.BookRequest;
import com.shivendra.library.catalog.dto.BookResponse;
import com.shivendra.library.catalog.exception.ResourceNotFoundException;
import com.shivendra.library.catalog.model.Author;
import com.shivendra.library.catalog.model.Book;
import com.shivendra.library.catalog.repository.AuthorRepository;
import com.shivendra.library.catalog.repository.BookRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookService {

    private final BookRepository books;
    private final AuthorRepository authors;

    public BookService(BookRepository books, AuthorRepository authors) {
        this.books = books;
        this.authors = authors;
    }

    @Transactional(readOnly = true)
    public List<BookResponse> findAll(String title) {
        List<Book> result = (title == null || title.isBlank())
                ? books.findAll()
                : books.findByTitleContainingIgnoreCase(title);
        return result.stream().map(BookResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public BookResponse get(Long id) {
        return BookResponse.from(find(id));
    }

    @Transactional
    public BookResponse create(BookRequest request) {
        Author author = authors.findById(request.authorId())
                .orElseThrow(() -> new ResourceNotFoundException("Author " + request.authorId() + " not found"));
        Book book = new Book(request.title(), request.isbn(), author, request.totalCopies());
        return BookResponse.from(books.save(book));
    }

    /**
     * Called by circulation-service when a book is issued. The @Version column makes
     * concurrent reservations of the last copy fail safely with an optimistic-lock error.
     */
    @Transactional
    public BookResponse reserve(Long id) {
        Book book = find(id);
        book.reserveCopy();
        return BookResponse.from(book);
    }

    /** Called by circulation-service when a book is returned or an issue is rolled back. */
    @Transactional
    public BookResponse release(Long id) {
        Book book = find(id);
        book.releaseCopy();
        return BookResponse.from(book);
    }

    private Book find(Long id) {
        return books.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book " + id + " not found"));
    }
}
