package com.marcos.sistemacontabil.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class DatabaseConfig {
	private final String url;
	// Posição 0 = Versão 1, Posição 1 = Versão 2...
	// Migração já aplicada nunca é editada: erro se corrige com uma nova.
	private static final List<String> MIGRACOES = List.of(
			"CREATE TABLE IF NOT EXISTS empresa("
			+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
			+ "razao_social TEXT NOT NULL,"
			+ "cnpj TEXT NOT NULL"
			+ ")");
	
	public DatabaseConfig(String url) {
		this.url = url;
	}
	
	public Connection conectar() throws SQLException {
		return DriverManager.getConnection(url);
	}

	public void inicializar() throws SQLException {
		criarTabelaControleVersao();
		int versaoAtual = obterVersaoAtual(); // 0 num banco novo ou legado
		
		// índice i corresponde à versão i + 1; só roda as que faltam
		for (int i = versaoAtual; i < MIGRACOES.size(); i++) {
			aplicarMigracao(i + 1, MIGRACOES.get(i));
		}	
	}
	
	private void aplicarMigracao(int versao, String sql) throws SQLException {
		try (Connection conexao = conectar()){
			conexao.setAutoCommit(false); 	// começa a transação
			try (Statement stmt = conexao.createStatement();
					PreparedStatement ps = conexao.prepareStatement(
							"INSERT INTO schema_version (versao) VALUES (?)")){
				stmt.execute(sql);			// a mudança de esquema
				ps.setInt(1, versao);
				ps.executeUpdate();			// registra que esta versão foi aplicada
				conexao.commit(); 			// só aqui as duas coisas viram definitivas
			}catch (SQLException e) {
				conexao.rollback();			// desfaz as duas
				throw e;					// relança: o app não deve seguir com banco errado
			}
		}
	}
	
	private void criarTabelaControleVersao() throws SQLException {
		String sql = "CREATE TABLE IF NOT EXISTS schema_version ("
				+ "versao INTEGER PRIMARY KEY"
				+ ");";
		try(Connection conexao = conectar();
			Statement stmt = conexao.createStatement()){
			stmt.execute(sql);
		}
	}
	
	private int obterVersaoAtual() throws SQLException {
		String sql = "SELECT MAX(versao) FROM schema_version;";
		try(Connection conexao = conectar();
			Statement stmt = conexao.createStatement();
			ResultSet rs = stmt.executeQuery(sql)){
			if (rs.next()) {
				return rs.getInt(1); 
			}
		}
		return 0;
	}
	
}
