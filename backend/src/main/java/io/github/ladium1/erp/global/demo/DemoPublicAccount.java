package io.github.ladium1.erp.global.demo;

public record DemoPublicAccount(
        String label,
        String description,
        String loginId,
        String password,
        boolean recommended
) {
}
