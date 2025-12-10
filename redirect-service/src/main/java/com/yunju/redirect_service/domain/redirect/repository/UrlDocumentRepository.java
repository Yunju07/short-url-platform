package com.yunju.redirect_service.domain.redirect.repository;

import com.yunju.redirect_service.domain.redirect.model.UrlDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UrlDocumentRepository extends MongoRepository<UrlDocument, String> {
}
