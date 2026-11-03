package com.marcos.sistemacontabil.repository;

import java.sql.Statement;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseConfig {
	private static final String URL = "jdbc:sqlite:contabilidade.db";

	public static Connection conectar() throws SQLException {
		return DriverManager.getConnection(URL);
	}

	public static void inicializar() {
		String sql = "CREATE TABLE IF NOT EXISTS empresa("
				+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ "razao_social TEXT NOT NULL,"
				+ "cnpj TEXT NOT NULL"
				+ ")";
		try (Connection conexao = conectar();
			 Statement stmt = conexao.createStatement()){
			stmt.execute(sql);
			System.out.println("Banco de dados inicializado com sucesso");
		}catch(SQLException e) {
			System.err.println("Erro ao iniccializar o banco: " + e.getMessage());
		}
	}
	public static void verificar() {
		String sql = "PRAGMA table_info(empresa)";
		try(Connection conexao = conectar();
			Statement stm = conexao.createStatement();
			ResultSet rs = stm.executeQuery(sql)){
			while(rs.next()) {
				System.out.println(rs.getString("name") + " - " + rs.getString("type"));
			}
		}catch(SQLException e) {
			System.err.println("Erro ao verificar tabela: " + e.getMessage());
		}
	}
}
