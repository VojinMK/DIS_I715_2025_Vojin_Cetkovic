package apiGateway.authentication;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatcher;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import reactor.core.publisher.Mono;
import serviceLibrary.dtos.UserDto;

@Configuration
@EnableWebFluxSecurity
public class ApiGatewayAuthentication {

    @Bean
    SecurityWebFilterChain filterChain(ServerHttpSecurity http) {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeExchange(exchange -> exchange

                        // prvi admin mora da se napravi bez login-a
                        .pathMatchers(HttpMethod.POST, "/user/newAdmin").hasRole("ADMIN")

                        // gateway koristi ovaj endpoint da učita korisnika tokom login-a
                        .pathMatchers(HttpMethod.GET, "/user/email").permitAll()
                        
                        .pathMatchers(HttpMethod.GET, "/user/id").hasRole("ADMIN")

                        // samo ADMIN pravi nove korisnike
                        .pathMatchers(HttpMethod.POST, "/user/newUser").hasRole("ADMIN")

                        // samo ADMIN vidi sve korisnike
                        .pathMatchers(HttpMethod.GET, "/users").hasRole("ADMIN")

                        // samo ADMIN briše korisnike
                        .pathMatchers(HttpMethod.DELETE, "/user").hasRole("ADMIN")

                        // za sada update samo ADMIN
                        .pathMatchers(HttpMethod.PUT, "/user").hasRole("ADMIN")
                        
                        //product service
                        .pathMatchers(HttpMethod.GET, "/products").hasAnyRole("ADMIN", "USER")
                        .pathMatchers(HttpMethod.GET, "/product/**").hasAnyRole("ADMIN", "USER")
                        .pathMatchers(HttpMethod.POST, "/product").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.PUT, "/product").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/product").hasRole("ADMIN")
                        
                        //stock service
                        .pathMatchers("/stock/**").hasRole("ADMIN")
                        .pathMatchers("/stocks").hasRole("ADMIN")
                        
                        //order service
                        .pathMatchers(HttpMethod.POST, "/order").hasAnyRole("ADMIN", "USER")
                        .pathMatchers(HttpMethod.GET, "/orders").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.GET, "/order/**").hasAnyRole("ADMIN", "USER")
                        .pathMatchers(HttpMethod.PUT, "/order").hasRole("ADMIN")
                        .pathMatchers(HttpMethod.DELETE, "/order").hasRole("ADMIN")

                        .anyExchange().authenticated()
                )
                .httpBasic(Customizer.withDefaults());

        http.addFilterAfter(userHeadersFilter(), SecurityWebFiltersOrder.AUTHENTICATION);

        return http.build();
    }

    @Bean
    public WebFilter userHeadersFilter() {
        ServerWebExchangeMatcher matcher = ServerWebExchangeMatchers.matchers(
                ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, "/user/newUser"),
                ServerWebExchangeMatchers.pathMatchers(HttpMethod.PUT, "/user"),
                ServerWebExchangeMatchers.pathMatchers(HttpMethod.DELETE, "/user"),
                ServerWebExchangeMatchers.pathMatchers(HttpMethod.POST, "/order"),
                ServerWebExchangeMatchers.pathMatchers(HttpMethod.GET, "/order/email")
        );

        return (ServerWebExchange exchange, WebFilterChain chain) ->
                matcher.matches(exchange).flatMap(result -> {
                    if (!result.isMatch()) {
                        return chain.filter(exchange);
                    }

                    return exchange.getPrincipal()
                            .cast(org.springframework.security.core.Authentication.class)
                            .flatMap(auth -> {
                                String email = auth.getName();

                                String role = auth.getAuthorities()
                                        .stream()
                                        .map(GrantedAuthority::getAuthority)
                                        .findFirst()
                                        .orElse("");

                                var mutatedRequest = exchange.getRequest()
                                        .mutate()
                                        .header("X-User-Email", email)
                                        .header("X-User-Role", role)
                                        .build();

                                System.out.println("[GW] X-User-Email=" + email + ", X-User-Role=" + role);

                                return chain.filter(exchange.mutate().request(mutatedRequest).build());
                            })
                            .switchIfEmpty(chain.filter(exchange));
                });
    }

    @Bean
    ReactiveUserDetailsService reactiveUserDetailsService() {
        //WebClient client = WebClient.create("http://localhost:8770");
    	WebClient client = WebClient.create("http://user-service:8770"); // docker

        return username -> client.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/user/email")
                        .queryParam("email", username)
                        .build()
                )
                .retrieve()
                .onStatus(status -> status.value() == 404,
                        response -> Mono.error(
                                new org.springframework.security.authentication.BadCredentialsException("User not found")
                        )
                )
                .bodyToMono(UserDto.class)
                .map(dto -> User.withUsername(dto.getEmail())
                        .password(dto.getPassword())
                        .roles(dto.getRole())
                        .build()
                );
    }

    @Bean
    BCryptPasswordEncoder getEncoder() {
        return new BCryptPasswordEncoder();
    }
}