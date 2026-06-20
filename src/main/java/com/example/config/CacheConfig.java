package com.example.config;

import com.example.user.application.dto.UserResponse;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;
import tools.jackson.databind.type.TypeFactory;

import java.time.Duration;
import java.util.List;

/**
 * Habilita o cache da aplicação e configura o backend Redis.
 *
 * <p>Os valores são serializados em JSON (em vez da serialização Java padrão),
 * o que torna o conteúdo do cache legível e independente de linguagem.</p>
 *
 * <p>Quando {@code spring.cache.type=simple} (por exemplo, no perfil {@code local}),
 * os beans abaixo não são registrados e o Spring usa um cache em memória, dispensando o
 * Redis durante o desenvolvimento.</p>
 */
@Configuration
@EnableCaching
public class CacheConfig {

    /** Cache de usuários individuais, indexado pelo id. */
    public static final String USERS_CACHE = "users";

    /** Cache da listagem completa de usuários. */
    public static final String USERS_LIST_CACHE = "usersList";

    private static final Duration TTL = Duration.ofMinutes(10);

    /**
     * Configuração padrão dos caches Redis: TTL de 10 min, sem cache de nulos e
     * serialização JSON genérica com informação de tipo ({@code @class}).
     *
     * <p>O default typing grava o tipo concreto junto do JSON, evitando que valores
     * cacheados voltem como {@link java.util.LinkedHashMap}. É aplicada ao cache
     * {@link #USERS_CACHE} (objeto único). O cache de listagem usa uma configuração
     * dedicada — ver {@link #redisCacheManagerCustomizer()}.</p>
     */
    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = true)
    public RedisCacheConfiguration redisCacheConfiguration() {
        return baseConfiguration()
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(GenericJacksonJsonRedisSerializer.builder()
                    .enableDefaultTyping(redisTypeValidator())
                    .build()));
    }

    /**
     * Customiza o {@link org.springframework.data.redis.cache.RedisCacheManager}:
     *
     * <ul>
     *   <li>{@code enableStatistics()} — necessário para que o Micrometer colete
     *       {@code cache.gets} (hit/miss), {@code cache.puts} e {@code cache.removals}.
     *       Sem isto o {@code RedisCache} usa {@code CacheStatisticsCollector.none()} e
     *       as métricas ficam zeradas.</li>
     *   <li>Configuração dedicada para {@link #USERS_LIST_CACHE}: guarda
     *       {@code List<UserResponse>} com um serializer <em>tipado</em>. O default typing
     *       com {@code As.PROPERTY} não grava o type-id do container na raiz, o que quebra
     *       o round-trip de coleções; o serializer tipado conhece o {@link JavaType} e
     *       dispensa o type-id.</li>
     * </ul>
     */
    @Bean
    @ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis", matchIfMissing = true)
    public RedisCacheManagerBuilderCustomizer redisCacheManagerCustomizer() {
        return builder -> builder
            .enableStatistics()
            .withCacheConfiguration(USERS_LIST_CACHE, usersListCacheConfiguration());
    }

    /** Configuração do cache de listagem com serializer tipado {@code List<UserResponse>}. */
    private static RedisCacheConfiguration usersListCacheConfiguration() {
        JavaType listType = TypeFactory.createDefaultInstance()
            .constructCollectionType(List.class, UserResponse.class);
        return baseConfiguration()
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new JacksonJsonRedisSerializer<>(listType)));
    }

    private static RedisCacheConfiguration baseConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(TTL)
            .disableCachingNullValues();
    }

    /**
     * Validador de tipos polimórficos para a desserialização do cache genérico.
     *
     * <p>Restringe a desserialização aos pacotes da aplicação e às coleções padrão,
     * evitando o risco de gadgets associado ao default typing irrestrito.</p>
     */
    private static PolymorphicTypeValidator redisTypeValidator() {
        return BasicPolymorphicTypeValidator.builder()
            .allowIfSubType("com.example.")
            .allowIfSubType("java.util.")
            .build();
    }
}
