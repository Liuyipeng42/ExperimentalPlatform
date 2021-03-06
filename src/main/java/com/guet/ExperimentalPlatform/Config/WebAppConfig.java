package com.guet.ExperimentalPlatform.Config;


import com.guet.ExperimentalPlatform.Interceptor.LogInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;


import org.springframework.web.servlet.config.annotation.CorsRegistry;


@Configuration
public class WebAppConfig implements WebMvcConfigurer{

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // LogInterceptor apply to all URLs.
        registry.addInterceptor(new LogInterceptor());

    }

    //解决跨域问题
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 设置允许跨域的路径
        registry.addMapping("/**")
                // 设置允许跨域请求的域名
                .allowedOrigins("*")
                // 是否允许证书，不再默认开启
                .allowCredentials(true)
                // 设置允许的方法
                .allowedMethods("*")
                // 跨域允许时间
                .maxAge(3600);
        /*registry.addMapping("/**");*/
    }


}