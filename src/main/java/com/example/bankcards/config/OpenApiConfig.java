//package com.example.bankcards.config;
//
//import com.example.bankcards.controller.AdminUserController;
//import com.example.bankcards.controller.AuthController;
//import com.example.bankcards.controller.CardController;
//import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
//import io.swagger.v3.oas.annotations.security.SecurityScheme;
//import io.swagger.v3.oas.models.OpenAPI;
//import org.springdoc.core.models.GroupedOpenApi;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import io.swagger.v3.oas.models.info.Info;
//import io.swagger.v3.oas.models.info.Contact;
//
//@SecurityScheme(
//        name = "bearerAuth",
//        type = SecuritySchemeType.HTTP,
//        bearerFormat = "JWT",
//        scheme = "bearer"
//)
//@Configuration
//public class OpenApiConfig {
//
//    @Bean
//    public GroupedOpenApi apiV1() {
//        final var groupName = "v1";
//        return GroupedOpenApi.builder()
//                .group(groupName)
//                .pathsToMatch("/api/v1/**")
//                .packagesToScan(
//                        AdminUserController.class.getPackageName(),
//                        AuthController.class.getPackageName(),
//                        CardController.class.getPackageName()
//                )
//                .build();
//    }
//
//
//    @Bean
//    public OpenAPI customOpenAPI() {
//        return new OpenAPI()
//                .info(new Info()
//                        .title("Bank Cards API")
//                        .version("1.0")
//                        .description("API для управления банковскими картами")
//                        .contact(new Contact()
//                                .name("Support")
//                                .email("support@bank.com")));
//    }
//}
