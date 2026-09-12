package com.aimovie.backend

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter

@Configuration
class SecurityConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }
    @Bean
fun jwtAuthenticationConverter(): JwtAuthenticationConverter {
    val grantedAuthoritiesConverter = JwtGrantedAuthoritiesConverter()
    grantedAuthoritiesConverter.setAuthoritiesClaimName("role")
    grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_")

    return JwtAuthenticationConverter().apply {
        setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter)
    }
}

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {

        http
            .csrf { it.disable() }

            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }

            .authorizeHttpRequests {
                it
                    .requestMatchers(
                        "/api/auth/login",
                        
                        "/api/movies/**",
                        "/api/showtimes/**",
                        "/api/showtime-seats/**",
                        "/api/seats/**",
                        "/api/seat-types/**",
                        "/api/seat-type-pricing/**",
                        "/api/cinemas/**",
                        "/api/cinema-rooms/**",
                       
                        "/api/genres/**",
                        "/api/movie-genres/**"
                   )
                    .permitAll()
                    .requestMatchers("/api/users/**")
                    .hasRole("ADMIN")
                    .requestMatchers("/api/bookings/**")
                    .hasRole("USER")
                    .requestMatchers("/api/payments/**")
                    .hasRole("USER")
                    .requestMatchers("/api/booking-seats/**")
                    .hasRole("USER")
                    .anyRequest()
                    .authenticated()
            }

            .oauth2ResourceServer {
    it.jwt {
        it.jwtAuthenticationConverter(jwtAuthenticationConverter())
    }
}

        return http.build()
    }
}