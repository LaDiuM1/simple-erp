package io.github.ladium1.erp.salescustomer.internal.entity;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.AvailableSettings;
import org.hibernate.dialect.MariaDBDialect;
import org.hibernate.exception.ConstraintViolationException;
import org.hibernate.mapping.Column;
import org.hibernate.mapping.UniqueKey;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SalesAssignmentDatabaseConstraintTest {

    @Test
    @DisplayName("MariaDB DDL 은 활성 고객사·직원 유일성 생성 컬럼 구성")
    void mariadb_schema_defines_active_assignment_unique_key() {
        try (DatabaseHarness database = new DatabaseHarness()) {
            Column activeEmployeeColumn = database.metadata().getEntityBinding(SalesAssignment.class.getName())
                    .getProperty("activeEmployeeId")
                    .getColumns()
                    .getFirst();
            UniqueKey uniqueKey = database.metadata().getEntityBinding(SalesAssignment.class.getName())
                    .getTable()
                    .getUniqueKey(SalesAssignment.ACTIVE_ASSIGNMENT_UNIQUE_CONSTRAINT);

            assertThat(activeEmployeeColumn.getSqlType(database.metadata()))
                    .containsIgnoringCase("generated always")
                    .containsIgnoringCase("end_date is null")
                    .containsIgnoringCase("employee_id");
            assertThat(uniqueKey.getColumns())
                    .extracting(Column::getName)
                    .containsExactly("customer_id", "active_employee_id");
        }
    }

    @Test
    @DisplayName("같은 고객사·직원의 활성 배정 중복을 DB 에서 차단")
    void database_rejects_duplicate_active_assignment() {
        try (DatabaseHarness database = new DatabaseHarness()) {
            database.persist(assignment(null));

            assertThatThrownBy(() -> database.persist(assignment(null)))
                    .isInstanceOf(ConstraintViolationException.class);
        }
    }

    @Test
    @DisplayName("종료 이력은 중복 보관하고 같은 직원을 다시 활성 배정")
    void database_allows_history_and_reassignment() {
        try (DatabaseHarness database = new DatabaseHarness()) {
            assertThatCode(() -> {
                database.persist(assignment(LocalDate.of(2026, 1, 31)));
                database.persist(assignment(LocalDate.of(2026, 2, 28)));
                database.persist(assignment(null));
            }).doesNotThrowAnyException();
        }
    }

    private SalesAssignment assignment(LocalDate endDate) {
        return SalesAssignment.builder()
                .customerId(1L)
                .employeeId(10L)
                .startDate(LocalDate.of(2026, 1, 1))
                .endDate(endDate)
                .primary(false)
                .reason("담당 배정")
                .build();
    }

    private static final class DatabaseHarness implements AutoCloseable {

        private final StandardServiceRegistry registry;
        private final Metadata metadata;
        private final SessionFactory sessionFactory;

        private DatabaseHarness() {
            String databaseName = "sales_assignment_" + UUID.randomUUID().toString().replace("-", "");
            String jdbcUrl = "jdbc:h2:mem:" + databaseName + ";MODE=MariaDB;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
            registry = new StandardServiceRegistryBuilder()
                    .applySetting(AvailableSettings.JAKARTA_JDBC_DRIVER, "org.h2.Driver")
                    .applySetting(AvailableSettings.JAKARTA_JDBC_URL, jdbcUrl)
                    .applySetting(AvailableSettings.JAKARTA_JDBC_USER, "sa")
                    .applySetting(AvailableSettings.JAKARTA_JDBC_PASSWORD, "")
                    .applySetting(AvailableSettings.DIALECT, MariaDBDialect.class.getName())
                    .applySetting(AvailableSettings.HBM2DDL_AUTO, "create-drop")
                    .applySetting(AvailableSettings.SHOW_SQL, false)
                    .build();
            metadata = new MetadataSources(registry)
                    .addAnnotatedClass(SalesAssignment.class)
                    .buildMetadata();
            sessionFactory = metadata.buildSessionFactory();
        }

        private Metadata metadata() {
            return metadata;
        }

        private void persist(SalesAssignment assignment) {
            try (Session session = sessionFactory.openSession()) {
                Transaction transaction = session.beginTransaction();
                try {
                    session.persist(assignment);
                    transaction.commit();
                } catch (RuntimeException exception) {
                    if (transaction.isActive()) {
                        transaction.rollback();
                    }
                    throw exception;
                }
            }
        }

        @Override
        public void close() {
            sessionFactory.close();
            StandardServiceRegistryBuilder.destroy(registry);
        }
    }
}
