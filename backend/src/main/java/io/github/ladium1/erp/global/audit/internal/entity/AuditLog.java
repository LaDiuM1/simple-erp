package io.github.ladium1.erp.global.audit.internal.entity;

import io.github.ladium1.erp.global.audit.AuditAction;
import io.github.ladium1.erp.global.jpa.BaseEntity;
import io.github.ladium1.erp.global.menu.Menu;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "audit_logs", indexes = {
        @Index(name = "idx_audit_menu_created",  columnList = "menu_code, created_at DESC"),
        @Index(name = "idx_audit_actor_created", columnList = "actor_id, created_at DESC"),
        @Index(name = "idx_audit_target",        columnList = "target_type, target_id, created_at DESC")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AuditLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "menu_code", nullable = false, length = 64)
    private Menu menuCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private AuditAction action;

    @Column(name = "actor_id")
    private Long actorId;

    @Column(name = "actor_login_id", nullable = false, length = 64)
    private String actorLoginId;

    @Column(name = "target_type", length = 64)
    private String targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "trace_id", length = 8)
    private String traceId;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Builder
    private AuditLog(Menu menuCode, AuditAction action,
                     Long actorId, String actorLoginId,
                     String targetType, Long targetId,
                     String traceId, String ipAddress) {
        this.menuCode = menuCode;
        this.action = action;
        this.actorId = actorId;
        this.actorLoginId = actorLoginId;
        this.targetType = targetType;
        this.targetId = targetId;
        this.traceId = traceId;
        this.ipAddress = ipAddress;
    }
}
