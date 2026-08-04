package com.huynhliem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Application {

	public static void main(String[] args) {
//		ApplicationContext ctx =
				SpringApplication.run(Application.class, args);
//		String[] names = ctx.getBeanDefinitionNames();
//		Arrays.sort(names);
//		for (String name : names) {
//			System.out.println(name);
//		}

	}

}
