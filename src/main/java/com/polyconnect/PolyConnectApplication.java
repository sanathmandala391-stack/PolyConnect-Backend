//package com.polyconnect;
//
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.annotation.Bean;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//
//@SpringBootApplication
//@EnableScheduling
//public class PolyConnectApplication {
//
//    public static void main(String[] args) {
//
//
//        SpringApplication.run(PolyConnectApplication.class, args);
//    }
//    @Bean
//    public void Hash_Gen(){
//        String password="admin";
//        BCryptPasswordEncoder encoder=new BCryptPasswordEncoder();
//        String Hashed_Password=encoder.encode(password);
//        System.out.println("Password:"+Hashed_Password);
//    }
//
//}





package com.polyconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableScheduling
public class PolyConnectApplication {

    public static void main(String[] args) {

        SpringApplication.run(PolyConnectApplication.class, args);

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        String password = "admin";
        String hashedPassword = encoder.encode(password);

        System.out.println("Password: " + hashedPassword);
    }


    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}