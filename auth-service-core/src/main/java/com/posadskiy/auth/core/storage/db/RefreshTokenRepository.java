package com.posadskiy.auth.core.storage.db;

import static io.micronaut.data.model.query.builder.sql.Dialect.POSTGRES;

import com.posadskiy.auth.core.storage.db.entity.RefreshTokenEntity;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.repository.CrudRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;

@JdbcRepository(dialect = POSTGRES)
public interface RefreshTokenRepository extends CrudRepository<RefreshTokenEntity, Long> {

    @Transactional
    RefreshTokenEntity save(
            @NonNull @NotBlank String username,
            @NonNull @NotBlank String refreshToken,
            @NonNull @NotNull Boolean revoked);

    Optional<RefreshTokenEntity> findByRefreshToken(@NonNull @NotBlank String refreshToken);
}
