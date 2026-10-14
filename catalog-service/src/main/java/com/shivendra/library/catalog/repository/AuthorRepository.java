package com.shivendra.library.catalog.repository;

import com.shivendra.library.catalog.model.Author;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthorRepository extends JpaRepository<Author, Long> {
}
