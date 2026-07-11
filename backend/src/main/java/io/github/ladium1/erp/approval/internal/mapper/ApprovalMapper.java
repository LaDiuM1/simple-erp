package io.github.ladium1.erp.approval.internal.mapper;

import io.github.ladium1.erp.approval.internal.dto.ApprovalAttachmentResponse;
import io.github.ladium1.erp.approval.internal.dto.ApprovalDetailResponse;
import io.github.ladium1.erp.approval.internal.dto.ApprovalStepResponse;
import io.github.ladium1.erp.approval.internal.dto.ApprovalSummaryResponse;
import io.github.ladium1.erp.approval.internal.entity.ApprovalDocument;
import io.github.ladium1.erp.approval.internal.entity.ApprovalStep;
import io.github.ladium1.erp.global.storage.StoredFileInfo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/**
 * 단순 변환만 담당 — 직원 이름 join 은 서비스가 EmployeeApi.findByIds 로 맵 구성 후 전달.
 */
@Mapper(componentModel = "spring")
public interface ApprovalMapper {

    @Mapping(source = "document.id", target = "id")
    @Mapping(source = "document.docType", target = "docType")
    @Mapping(source = "document.title", target = "title")
    @Mapping(source = "drafterName", target = "drafterName")
    @Mapping(source = "document.status", target = "status")
    @Mapping(source = "document.createdAt", target = "createdAt")
    @Mapping(source = "document.currentStepOrder", target = "currentStepOrder")
    @Mapping(source = "totalSteps", target = "totalSteps")
    ApprovalSummaryResponse toSummaryResponse(ApprovalDocument document, String drafterName, int totalSteps);

    @Mapping(source = "step.stepOrder", target = "stepOrder")
    @Mapping(source = "step.approverId", target = "approverId")
    @Mapping(source = "approverName", target = "approverName")
    @Mapping(source = "step.status", target = "status")
    @Mapping(source = "step.comment", target = "comment")
    @Mapping(source = "step.decidedAt", target = "decidedAt")
    ApprovalStepResponse toStepResponse(ApprovalStep step, String approverName);

    @Mapping(source = "id", target = "fileId")
    @Mapping(source = "originalName", target = "name")
    ApprovalAttachmentResponse toAttachmentResponse(StoredFileInfo info);

    @Mapping(source = "document.id", target = "id")
    @Mapping(source = "document.docType", target = "docType")
    @Mapping(source = "document.title", target = "title")
    @Mapping(source = "document.content", target = "content")
    @Mapping(source = "document.drafterId", target = "drafterId")
    @Mapping(source = "drafterName", target = "drafterName")
    @Mapping(source = "document.refId", target = "refId")
    @Mapping(source = "document.status", target = "status")
    @Mapping(source = "document.currentStepOrder", target = "currentStepOrder")
    @Mapping(source = "document.createdAt", target = "createdAt")
    @Mapping(source = "steps", target = "steps")
    @Mapping(source = "attachments", target = "attachments")
    @Mapping(source = "myTurn", target = "myTurn")
    @Mapping(source = "cancelable", target = "cancelable")
    ApprovalDetailResponse toDetailResponse(ApprovalDocument document,
                                            String drafterName,
                                            List<ApprovalStepResponse> steps,
                                            List<ApprovalAttachmentResponse> attachments,
                                            boolean myTurn,
                                            boolean cancelable);
}
