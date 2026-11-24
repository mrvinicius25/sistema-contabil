package com.marcos.sistemacontabil.repository;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class DatabaseConfigTest {
	private DatabaseConfig db;
	
	@TempDir
	Path pastaTemp;
	
	
	@BeforeEach
	void setUp() {
		db = new DatabaseConfig("jdbc:sqlite:" + pastaTemp.resolve("teste.db"));	// Arrage
	}
	
	@Test
	void inicializarBancoLegadoDeveManterDadosERegistrarVersao1() throws SQLException {
		// Arrange: simula banco da sprint 1 (com tabela  empresa e 1 registro, mas sem schema_version)
		try (Connection conexao = db.conectar();
			 Statement stmt = conexao.createStatement()) {
			stmt.executeUpdate("CREATE TABLE empresa ("
					+ "id INTEGER PRIMARY KEY AUTOINCREMENT,"
					+ "razao_social TEXT NOT NULL,"
					+ "cnpj TEXT NOT NULL)");
			stmt.executeUpdate("INSERT INTO empresa (razao_social, cnpj) VALUES ('Empresa Legada LTDA', '12345678000199')");
		}
		
		// Act: roda a inicalização com o novo sistema de migrações
		db.inicializar();
		
		// Assert: verifica se a versão virou 1 e se o dado antigo continua intacto
		assertEquals(1, lerVersao(), "A versão do schema deveria ser 1 após inicializar banco legado");

		try (Connection conexao = db.conectar();
			 Statement stmt = conexao.createStatement();
			 ResultSet rs = stmt.executeQuery("SELECT COUNT (*) FROM empresa")) {
			assertTrue(rs.next());
			assertEquals(1, rs.getInt(1), "O registro legado da tabela empresa não deveria ter sido perdido");
		}
	}
	
	@Test
	void inicializarDeveRegistrarVersao1() throws SQLException {
		// Arrange: o @BeforeEach já preparou um banco vazio
		
		// Act 
		db.inicializar();
		
		// Assert 
		assertEquals(1, lerVersao(), "A versão do schema em um banco novo deveria ser 1");
	}
	
	@Test
	void inicializarDeveCriarTabelaEmpresa() throws SQLException {
		// Act (agir)
		assertDoesNotThrow(db::inicializar);
		// Assert (verificar)
		String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='empresa'";
		try (Connection conexao = db.conectar();
				Statement stm = conexao.createStatement();
				ResultSet rs = stm.executeQuery(sql)) {
			assertTrue(rs.next(),"A tabela empresa deveria existir após inicializar()");
		}
	}
	
	@Test
	void inicializarDuasVezesNaoDeveRepetirMigracao() throws SQLException {
		
		// Act
		db.inicializar();
		db.inicializar();
		
		// Assert
		assertEquals(1, lerVersao(), "A versão deve continuar 1 mesmo ao inicializar duas vezes ");
		String sql = "SELECT COUNT (*) FROM schema_version";
		try (Connection conexao = db.conectar();
			 Statement stmt = conexao.createStatement();
			 ResultSet rs = stmt.executeQuery(sql)) {
			assertTrue(rs.next());
			assertEquals(1, rs.getInt(1));
		}
	}
	
	@Test
	void inicializarDeveCriarColunasEsperadas() throws SQLException {
		// Act
		db.inicializar();
		// Assert
		List<String> colunas = new ArrayList<String>();
		try(Connection conexao = db.conectar();
				Statement stm = conexao.createStatement();
				ResultSet rs = stm.executeQuery("PRAGMA table_info(empresa)")) {
			while (rs.next()) {
				colunas.add(rs.getString("name"));
			}	
		}
	
		assertEquals(List.of("id", "razao_social", "cnpj"), colunas);
	}
	
	// Método auxiliar
	private int lerVersao() throws SQLException {
		String  sql = "SELECT MAX(versao) FROM schema_version";
		try(Connection conexao = db.conectar();
				Statement stmt = conexao.createStatement();
				ResultSet rs = stmt.executeQuery(sql)){
			if (rs.next()) {
				return rs.getInt(1);
			}
		}
		return 0;
	}
	
}
