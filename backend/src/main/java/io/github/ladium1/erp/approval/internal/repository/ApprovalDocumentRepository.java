package io.github.ladium1.erp.approval.internal.repository;

import io.github.ladium1.erp.approval.internal.entity.ApprovalDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ApprovalDocumentRepository extends JpaRepository<ApprovalDocument, Long>, ApprovalDocumentRepositoryCustom {
}
