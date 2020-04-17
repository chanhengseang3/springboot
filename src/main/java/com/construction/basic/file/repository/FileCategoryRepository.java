package com.construction.basic.file.repository;

import com.construction.basic.file.domain.FileCategory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileCategoryRepository extends JpaRepository<FileCategory, Long> {
}
