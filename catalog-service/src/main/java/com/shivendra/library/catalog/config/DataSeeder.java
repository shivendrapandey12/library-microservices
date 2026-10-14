package com.shivendra.library.catalog.config;

import com.shivendra.library.catalog.model.Author;
import com.shivendra.library.catalog.model.Book;
import com.shivendra.library.catalog.repository.AuthorRepository;
import com.shivendra.library.catalog.repository.BookRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Inserts a few demo books on first start so the system can be tried immediately. */
@Configuration
public class DataSeeder {

    @Bean
    ApplicationRunner seedCatalog(AuthorRepository authors, BookRepository books) {
        return args -> {
            if (books.count() > 0) {
                return;
            }
            Author martin = authors.save(new Author("Robert C. Martin"));
            Author bloch = authors.save(new Author("Joshua Bloch"));
            books.save(new Book("Clean Code", "9780132350884", martin, 3));
            books.save(new Book("Effective Java", "9780134685991", bloch, 2));
        };
    }
}
