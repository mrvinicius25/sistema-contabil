package com.marcos.sistemacontabil.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseConfig {
	private final String url;
	
	public DatabaseConfig(String url) {
		this.url = url;
	}
	
	public Connection conectar() throws SQLException {
		return DriverManager.getConnection(url);
	}

	public void inicializar() throws SQLException {
		String sql = "CREATE TABLE IF NOT EXISTS empresa("
				+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "razao_social TEXT NOT NULL,"
				+ "cnpj TEXT NOT NULL"
				+ ")";
		try (Connection conexao = conectar();
			 Statement stmt = conexao.createStatement()){
			stmt.execute(sql);
		}
	}
	public void verificar() throws SQLException {
		String sql = "PRAGMA table_info(empresa)";
		try(Connection conexao = conectar();
			Statement stm = conexao.createStatement();
			ResultSet rs = stm.executeQuery(sql)){
			while(rs.next()) {
				System.out.println(rs.getString("name") + " - " + rs.getString("type"));
			}
		}
	}
}
