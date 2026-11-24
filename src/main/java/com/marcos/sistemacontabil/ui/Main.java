package com.marcos.sistemacontabil.ui;

import java.sql.SQLException;

import com.marcos.sistemacontabil.repository.DatabaseConfig;

public class Main {
	public static void main (String[] args) {
		DatabaseConfig db = new DatabaseConfig("jdbc:sqlite:contabilidade.db");
		try {
			db.inicializar();
		}catch (SQLException e) {
			System.err.println("Erro no banco: " + e.getMessage());
		}
	}
}
