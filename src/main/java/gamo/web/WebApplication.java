package gamo.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

//implements WebMvcConfigurer 는 swagger 설정 때문에 static 폴더 인식 못해서 임시로 넣어놧음
@SpringBootApplication
public class WebApplication implements WebMvcConfigurer {

	public static void main(String[] args) {
		SpringApplication.run(WebApplication.class, args);
	}

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/");
    }

}
