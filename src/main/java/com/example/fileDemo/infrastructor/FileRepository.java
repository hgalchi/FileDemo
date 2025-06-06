package com.example.fileDemo.infrastructor;


import com.example.fileDemo.domain.File;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FileRepository extends JpaRepository<File, Long> {
    Optional<File> findByOriginalFilename(String userId);
}
