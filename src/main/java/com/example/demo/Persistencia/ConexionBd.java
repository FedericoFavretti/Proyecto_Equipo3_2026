package com.example.demo.Persistencia;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;

@Configuration
public class ConexionBd {

    @Value("${spring.datasource.url}")
    private String url;

    @Value("${spring.datasource.username}")
    private String usuario;

    @Value("${spring.datasource.password}")
    private String password;

    @Bean
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setUrl(url);
        dataSource.setUsername(usuario);
        dataSource.setPassword(password);

        java.util.Properties props = new java.util.Properties();
        props.setProperty("options", "-c search_path=public");
        dataSource.setConnectionProperties(props);

        // Log de diagnóstico - borrar después
        try {
            java.sql.Connection conn = dataSource.getConnection();
            java.sql.ResultSet rs = conn.createStatement().executeQuery("SHOW search_path");
            rs.next();
            System.out.println(">>> SEARCH_PATH EN RUNTIME: " + rs.getString(1));
            java.sql.ResultSet rs2 = conn.createStatement().executeQuery("SELECT COUNT(*) FROM usuario");
            rs2.next();
            System.out.println(">>> COUNT USUARIO: " + rs2.getString(1));
            conn.close();
        } catch (Exception e) {
            System.out.println(">>> ERROR DIAGNOSTICO: " + e.getMessage());
        }

        return dataSource;
    }

}