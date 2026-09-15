package com.atomskills.argent;

import com.atomskills.argent.attachment.AttachmentController;
import com.atomskills.argent.attachment.AttachmentRepository;
import com.atomskills.argent.attachment.AttachmentStorageService;
import com.atomskills.argent.audit.AuditEventListener;
import com.atomskills.argent.auth.AuthController;
import com.atomskills.argent.auth.UserAuthRepository;
import com.atomskills.argent.auth.UserSession;
import com.atomskills.argent.auth.UserSessionRepository;
import com.atomskills.argent.error.ArgentExceptionHandler;
import com.atomskills.argent.reference.RefItemRepository;
import com.atomskills.argent.reference.RefTypeRepository;
import com.atomskills.argent.reference.ReferenceController;
import com.atomskills.argent.security.RoleRepository;
import com.atomskills.argent.security.UserRoleRepository;
import com.atomskills.argent.sequence.NumberGenerator;
import com.atomskills.argent.sequence.SequenceRepository;
import com.atomskills.argent.transfer.DocumentExportService;
import com.atomskills.argent.transfer.TableExportService;
import com.atomskills.argent.transfer.TableImportService;
import com.atomskills.argent.transfer.TransferController;
import com.atomskills.argent.user.UserRepository;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import org.hibernate.boot.Metadata;
import org.hibernate.boot.spi.BootstrapContext;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.jpa.boot.spi.IntegratorProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.hibernate.autoconfigure.HibernatePropertiesCustomizer;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Точка входа ядра {@code argent} в любое Spring Boot приложение, подключившее {@code
 * argent-backend} как зависимость.
 *
 * <p>Регистрируется через {@code
 * META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports} — Spring Boot
 * находит этот класс автоматически при старте, без единого явного {@code @Import} в коде
 * приложения-потребителя.
 *
 * <p>{@code @EnableJpaRepositories}/{@code @EntityScan} без явного {@code basePackages} по
 * умолчанию сканируют от пакета <em>этого</em> класса вниз ({@code com.atomskills.argent}) — это и
 * покрывает сущности/репозитории во всех подпакетах ядра ({@code user}, {@code auth}, ...), а не
 * только пакет приложения-потребителя, как было бы при неявном поведении
 * {@code @SpringBootApplication}.
 */
@AutoConfiguration
@EnableJpaRepositories
@EntityScan(basePackages = "com.atomskills.argent")
public class ArgentAutoConfiguration {

  /** Диагностический маячок — подтверждает, что autoconfigure реально подхватился. */
  @Bean
  public CommandLineRunner commandLineRunner() {
    return args -> {
      System.out.println("ArgentAutoConfiguration is working!");
    };
  }

  @Bean
  public ArgentExceptionHandler argentExceptionHandler() {
    return new ArgentExceptionHandler();
  }

  @Bean
  public AuthController authController(
      UserRepository userRepository,
      UserAuthRepository userAuthRepository,
      PasswordEncoder passwordEncoder,
      UserSessionRepository userSessionRepository,
      JwtEncoder jwtEncoder) {
    return new AuthController(
        userRepository, userAuthRepository, passwordEncoder, userSessionRepository, jwtEncoder);
  }

  @Bean
  public ReferenceController referenceController(
      RefTypeRepository refTypeRepository, RefItemRepository refItemRepository) {
    return new ReferenceController(refTypeRepository, refItemRepository);
  }

  @Bean
  public AttachmentStorageService attachmentStorageService(
      @Value("${argent.storage.root-path}") String storageRootPath,
      AttachmentRepository attachmentRepository) {
    return new AttachmentStorageService(storageRootPath, attachmentRepository);
  }

  @Bean
  public AttachmentController attachmentController(
      AttachmentStorageService attachmentStorageService,
      AttachmentRepository attachmentRepository) {
    return new AttachmentController(attachmentStorageService, attachmentRepository);
  }

  @Bean
  public TableExportService tableExportService() {
    return new TableExportService();
  }

  @Bean
  public DocumentExportService documentExportService() {
    return new DocumentExportService();
  }

  @Bean
  public TableImportService tableImportService() {
    return new TableImportService();
  }

  @Bean
  public TransferController transferController(
      TableExportService tableExportService,
      DocumentExportService documentExportService,
      TableImportService tableImportService) {
    return new TransferController(tableExportService, documentExportService, tableImportService);
  }

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public JwtEncoder jwtEncoder(RSAKey rsaKey) throws Exception {
    JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(new JWKSet(rsaKey));

    return new NimbusJwtEncoder(jwkSource);
  }

  @Bean
  public JwtDecoder jwtDecoder(RSAKey rsaKey, UserSessionRepository userSessionRepository)
      throws Exception {
    NimbusJwtDecoder decoder =
        NimbusJwtDecoder.withPublicKey((RSAPublicKey) rsaKey.toRSAPublicKey()).build();

    OAuth2TokenValidator<Jwt> revokedValidator =
        jwt -> {
          UUID sessionId = UUID.fromString(jwt.getClaimAsString("sessionId"));
          boolean revoked =
              userSessionRepository
                  .findById(sessionId)
                  .map(UserSession::getRevoked)
                  .orElse(true); // сессии нет вообще — считаем как отозванную

          if (revoked) {
            return OAuth2TokenValidatorResult.failure(
                new OAuth2Error("invalid_token", "Session revoked or not found", null));
          }
          return OAuth2TokenValidatorResult.success();
        };

    decoder.setJwtValidator(
        new DelegatingOAuth2TokenValidator<>(JwtValidators.createDefault(), revokedValidator));

    return decoder;
  }

  @Bean
  public SecurityFilterChain securityFilterChain(
      HttpSecurity http,
      JwtAuthenticationConverter jwtAuthenticationConverter,
      CorsConfigurationSource corsConfigurationSource)
      throws Exception {
    return http.authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        "/api/auth/login",
                        "/api/test/**",
                        "/v3/api-docs/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**")
                    .permitAll()
                    .anyRequest()
                    .authenticated())
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .oauth2ResourceServer(
            oauth2 -> oauth2.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter)))
        .csrf(csrf -> csrf.disable())
        .build();
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:4200"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);

    return source;
  }

  @Bean
  public RSAKey argentRsaKey() throws NoSuchAlgorithmException {
    KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
    keyPairGenerator.initialize(2048);
    KeyPair keyPair = keyPairGenerator.generateKeyPair();

    return new RSAKey.Builder((RSAPublicKey) keyPair.getPublic())
        .privateKey((RSAPrivateKey) keyPair.getPrivate())
        .keyID(UUID.randomUUID().toString())
        .build();
  }

  @Bean
  public JwtAuthenticationConverter jwtAuthenticationConverter(
      UserRoleRepository userRoleRepository, RoleRepository roleRepository) {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
    converter.setJwtGrantedAuthoritiesConverter(
        jwt -> {
          UUID userId = UUID.fromString(jwt.getSubject());
          return userRoleRepository.findByUserId(userId).stream()
              .map(userRole -> roleRepository.findById(userRole.getRoleId()))
              .flatMap(Optional::stream)
              .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName()))
              .collect(Collectors.toSet());
        });

    return converter;
  }

  @Bean
  public NumberGenerator numberGenerator(
      SequenceRepository sequenceRepository, JdbcTemplate jdbcTemplate) {
    return new NumberGenerator(sequenceRepository, jdbcTemplate);
  }

  @Bean
  public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(JdbcTemplate jdbcTemplate) {
    AuditEventListener auditEventListener = new AuditEventListener(jdbcTemplate);

    Integrator auditIntegrator =
        new Integrator() {
          @Override
          public void integrate(
              Metadata metadata,
              BootstrapContext bootstrapContext,
              SessionFactoryImplementor sessionFactory) {
            EventListenerRegistry registry =
                sessionFactory.getServiceRegistry().getService(EventListenerRegistry.class);
            registry.appendListeners(EventType.POST_INSERT, auditEventListener);
            registry.appendListeners(EventType.POST_UPDATE, auditEventListener);
            registry.appendListeners(EventType.POST_DELETE, auditEventListener);
          }
        };

    IntegratorProvider integratorProvider = () -> List.of(auditIntegrator);

    return hibernateProperties ->
        hibernateProperties.put("hibernate.integrator_provider", integratorProvider);
  }
}
