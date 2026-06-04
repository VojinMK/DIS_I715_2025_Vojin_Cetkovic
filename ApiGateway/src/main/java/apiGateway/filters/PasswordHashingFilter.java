package apiGateway.filters;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.rewrite.ModifyRequestBodyGatewayFilterFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;
import serviceLibrary.dtos.UserDto;

@Component
public class PasswordHashingFilter {

    private final BCryptPasswordEncoder encoder;
    private final ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFactory;

    public PasswordHashingFilter(BCryptPasswordEncoder encoder,
                                 ModifyRequestBodyGatewayFilterFactory modifyRequestBodyFactory) {
        this.encoder = encoder;
        this.modifyRequestBodyFactory = modifyRequestBodyFactory;
    }

    public GatewayFilter apply() {
        return modifyRequestBodyFactory.apply(
                new ModifyRequestBodyGatewayFilterFactory.Config()
                        .setRewriteFunction(UserDto.class, UserDto.class, (exchange, dto) -> {
                            if (dto != null && dto.getPassword() != null) {
                                dto.setPassword(encoder.encode(dto.getPassword()));
                            }
                            return Mono.just(dto);
                        })
        );
    }
}