package io.github.ladium1.erp.global.security;

import java.util.Optional;

public interface DataScopePrincipalReader {

    DataScopePrincipal getRequiredByLoginId(String loginId);

    Optional<DataScopePrincipal> findByLoginId(String loginId);
}
