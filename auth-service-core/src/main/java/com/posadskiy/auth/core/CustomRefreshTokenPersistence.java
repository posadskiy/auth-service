package com.posadskiy.auth.core;

import static io.micronaut.security.errors.IssuingAnAccessTokenErrorCode.INVALID_GRANT;

import com.posadskiy.auth.core.storage.db.RefreshTokenRepository;
import com.posadskiy.auth.core.storage.db.entity.RefreshTokenEntity;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.errors.OauthErrorResponseException;
import io.micronaut.security.token.event.RefreshTokenGeneratedEvent;
import io.micronaut.security.token.refresh.RefreshTokenPersistence;
import jakarta.inject.Singleton;
import java.util.Optional;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;

@Singleton
public class CustomRefreshTokenPersistence implements RefreshTokenPersistence {

    private final RefreshTokenRepository refreshTokenRepository;

    public CustomRefreshTokenPersistence(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Override
    public void persistToken(RefreshTokenGeneratedEvent event) {
        if (event != null
                && event.getRefreshToken() != null
                && event.getAuthentication() != null
                && event.getAuthentication().getName() != null) {
            String payload = event.getRefreshToken();
            refreshTokenRepository.save(event.getAuthentication().getName(), payload, false);
        }
    }

    @Override
    public Publisher<Authentication> getAuthentication(String refreshToken) {
        return Flux.create(
                emitter -> {
                    Optional<RefreshTokenEntity> tokenOpt = refreshTokenRepository.findByRefreshToken(refreshToken);
                    if (tokenOpt.isPresent()) {
                        RefreshTokenEntity token = tokenOpt.get();
                        if (token.getRevoked()) {
                            emitter.error(
                                    new OauthErrorResponseException(INVALID_GRANT, "refresh token revoked", null));
                        } else {
                            emitter.next(Authentication.build(token.getUsername()));
                            emitter.complete();
                        }
                    } else {
                        emitter.error(new OauthErrorResponseException(INVALID_GRANT, "refresh token not found", null));
                    }
                },
                FluxSink.OverflowStrategy.ERROR);
    }
}
