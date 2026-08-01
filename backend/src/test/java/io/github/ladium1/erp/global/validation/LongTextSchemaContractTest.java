package io.github.ladium1.erp.global.validation;

import io.github.ladium1.erp.approval.internal.entity.ApprovalDocument;
import io.github.ladium1.erp.approval.internal.entity.ApprovalStep;
import io.github.ladium1.erp.board.internal.entity.Post;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.MariaDBDialect;
import org.hibernate.mapping.Column;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LongTextSchemaContractTest {

    @Test
    @DisplayName("MariaDB 스키마에서 게시글과 결재 본문을 TEXT 로 생성")
    void mariadb_schema_uses_text_for_long_content() {
        StandardServiceRegistry registry = new StandardServiceRegistryBuilder()
                .applySetting(AvailableSettings.DIALECT, MariaDBDialect.class.getName())
                .build();
        try {
            Metadata metadata = new MetadataSources(registry)
                    .addAnnotatedClass(Post.class)
                    .addAnnotatedClass(ApprovalDocument.class)
                    .addAnnotatedClass(ApprovalStep.class)
                    .buildMetadata();

            assertTextColumn(metadata, Post.class);
            assertTextColumn(metadata, ApprovalDocument.class);
        } finally {
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }

    private void assertTextColumn(Metadata metadata, Class<?> entityType) {
        Column contentColumn = metadata.getEntityBinding(entityType.getName())
                .getProperty("content")
                .getColumns()
                .getFirst();
        assertThat(contentColumn.getSqlType(metadata)).isEqualToIgnoringCase("text");
    }
}
