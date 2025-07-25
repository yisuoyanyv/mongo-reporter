// MongoReporterApplication.java
// 数据报表平台后端主程序，支持动态MongoDB数据源和报表接口
package com.mongo.reporter.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MongoReporterApplication {

	public static void main(String[] args) {
		SpringApplication.run(MongoReporterApplication.class, args);
	}

}
