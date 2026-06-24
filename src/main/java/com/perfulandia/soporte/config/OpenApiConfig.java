package com.perfulandia.soporte.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.tags.Tag;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI soporteServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Perfulandia Soporte Service API")
                        .description("""
                                API REST del microservicio de soporte técnico para Perfulandia FullStack.

                                Funcionalidades principales:
                                - Crear tickets de soporte.
                                - Consultar tickets por usuario.
                                - Asignar responsables.
                                - Cambiar estados de atención.
                                - Registrar mensajes dentro de un ticket.
                                - Cerrar o cancelar tickets.

                                Este microservicio no almacena la entidad completa Usuario.
                                Solo utiliza idUsuario e idUsuarioAsignado para mantener independencia
                                con respecto al usuario-service.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipo Perfulandia")
                                .email("soporte@perfulandia.cl")
                                .url("https://github.com/JaAncaten/Perfulandia-Soporte-Service-"))
                        .license(new License()
                                .name("Uso académico - Perfulandia FullStack")))
                .servers(List.of(
                        new Server()
                                .url("http://localhost:8086")
                                .description("Servidor local de desarrollo")
                ))
                .tags(List.of(
                        new Tag()
                                .name("Tickets de Soporte")
                                .description("Operaciones para crear, consultar, actualizar, asignar, cerrar y cancelar tickets."),
                        new Tag()
                                .name("Mensajes de Ticket")
                                .description("Operaciones para registrar, consultar y eliminar mensajes asociados a un ticket.")
                ))
                .externalDocs(new ExternalDocumentation()
                        .description("Repositorio GitHub del microservicio")
                        .url("https://github.com/JaAncaten/Perfulandia-Soporte-Service-"));
    }
}