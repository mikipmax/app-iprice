package com.iprice;


import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;


@Configuration
public class WelcomePageRedirect implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("forward:/index.xhtml");

        registry.setOrder(Ordered.HIGHEST_PRECEDENCE);

    }

    @Bean
    public ServletContextInitializer initializer() {
        return new ServletContextInitializer() {
            @Override
            public void onStartup(ServletContext servletContext)
                    throws ServletException {

                servletContext.setInitParameter("primefaces.UPLOADER",
                        "commons");
            }
        };
    }

    @Bean
    public FilterRegistrationBean FileUploadFilter() {
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new org.primefaces.webapp.filter.FileUploadFilter());
        registration.setName("PrimeFaces FileUpload Filter");
        return registration;
    }

}