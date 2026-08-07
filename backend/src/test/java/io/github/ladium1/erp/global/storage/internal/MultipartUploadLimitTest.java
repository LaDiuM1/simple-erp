package io.github.ladium1.erp.global.storage.internal;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.unit.DataSize;
import org.springframework.core.io.support.PropertiesLoaderUtils;

import java.io.IOException;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class MultipartUploadLimitTest {

    @Test
    @DisplayName("파일 30MiB와 multipart 오버헤드를 포함한 요청 한도 유지")
    void request_limit_keeps_headroom_above_file_limit() throws IOException {
        Properties properties = PropertiesLoaderUtils.loadProperties(
                new ClassPathResource("application.properties"));

        DataSize fileLimit = DataSize.parse(
                properties.getProperty("spring.servlet.multipart.max-file-size"));
        DataSize requestLimit = DataSize.parse(
                properties.getProperty("spring.servlet.multipart.max-request-size"));

        assertThat(fileLimit).isEqualTo(DataSize.ofMegabytes(30));
        assertThat(requestLimit).isEqualTo(DataSize.ofMegabytes(32));
        assertThat(requestLimit.toBytes()).isGreaterThan(fileLimit.toBytes());
    }
}
