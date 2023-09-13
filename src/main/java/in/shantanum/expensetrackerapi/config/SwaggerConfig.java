package in.shantanum.expensetrackerapi.config;

import java.util.Collection;
import java.util.Collections;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import springfox.documentation.RequestHandler;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.service.Documentation;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@Configuration
@EnableSwagger2
public class SwaggerConfig {

	@Bean
	public Docket api() {
		return new Docket(DocumentationType.SWAGGER_2).apiInfo(getInfo()).select()
				.apis(RequestHandlerSelectors.basePackage("in.shantanum.expensetrackerapi")).paths(PathSelectors.any())
				.build();
	}

	private ApiInfo getInfo() {
		return new ApiInfo("Expense Manager Application", "This is a Expense  Manager API developed by Shantanum",
				"1.0", "Terms of Service", new Contact("shantanum", "http://www.shantanum.com", "shantaumali@gmail.com"),
				"License", "API license url", Collections.emptyList());
	}

}
