package apiGateway.routing;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import apiGateway.filters.PasswordHashingFilter;

@Configuration
public class RoutingConfiguration {

	private final PasswordHashingFilter hashingFilter;

	public RoutingConfiguration(PasswordHashingFilter hashingFilter) {
		this.hashingFilter = hashingFilter;
	}

	@Bean
	RouteLocator gatewayRouting(RouteLocatorBuilder builder) {
		return builder.routes()

				.route(p -> p.path("/user/newUser").filters(f -> f.filter(hashingFilter.apply()))
						.uri("lb://user-service"))

				.route(p -> p.path("/user/newAdmin").filters(f -> f.filter(hashingFilter.apply()))
						.uri("lb://user-service"))
				.route(p -> p.path("/user").and().method("PUT").filters(f -> f.filter(hashingFilter.apply()))
						.uri("lb://user-service"))
				.route(p -> p.path("/user/**").uri("lb://user-service"))

				.route(p -> p.path("/users").uri("lb://user-service"))
				
				.route(p -> p.path("/products/**").uri("lb://product-service"))
				
				.route(p -> p.path("/product/**").uri("lb://product-service"))
				
				.route(p -> p.path("/stocks").uri("lb://stock-service"))
				
				.route(p -> p.path("/stock/**").uri("lb://stock-service"))

				.build();
	}
}