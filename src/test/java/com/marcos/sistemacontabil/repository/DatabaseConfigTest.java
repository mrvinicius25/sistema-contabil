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
		db = new DatabaseConfig("jdbc:sqlite:" + pastaTemp.resolve("teste.db"));
	}
	
	@Test
	void inicializarDeveCriarTabelaEmpresa() throws SQLException {
		// Arrange (preparar)
		
		// Act (agir)
		assertDoesNotThrow(db::inicializar);
		// Assert (verificar)
		String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name='empresa'";
		try (Connection conexao = db.conectar();
				Statement stm = conexao.createStatement();
				ResultSet rs = stm.executeQuery(sql)) {
			assertTrue(rs.next(),"A tabela 'empresa deveria existir após inicializar()");
		}
	}
	
	@Test
	void inicializarDuasVezesNaoDeveFalhar() throws SQLException {
		// Act
		db.inicializar();
		// Assert
		assertDoesNotThrow(db::inicializar);
				
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
	
}
