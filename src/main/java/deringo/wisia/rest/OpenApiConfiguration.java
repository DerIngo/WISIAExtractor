package deringo.wisia.rest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI wisiaOpenApi() {
        return new OpenAPI().info(new Info()
                .title("WISIA Read API")
                .version("v1")
                .description("Read-only REST API fuer Arten aus der Datei alleArten.obj"));
    }
}
