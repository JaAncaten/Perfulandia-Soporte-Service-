package com.perfulandia.soporte;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ConfigurableApplicationContext;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

class SoporteSeviceApplicationMainTest {

    @Test
    void main_deberiaEjecutarSpringApplicationRun() {
        String[] args = {};

        try (MockedStatic<SpringApplication> springApplicationMock = mockStatic(SpringApplication.class)) {

            springApplicationMock
                    .when(() -> SpringApplication.run(SoporteSeviceApplication.class, args))
                    .thenReturn(mock(ConfigurableApplicationContext.class));

            SoporteSeviceApplication.main(args);

            springApplicationMock.verify(
                    () -> SpringApplication.run(SoporteSeviceApplication.class, args)
            );
        }
    }
}