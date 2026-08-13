package com.marcos.sistemacontabil.ui;

import com.marcos.sistemacontabil.repository.DatabaseConfig;

public class Main {
	public static void main (String[] args) {
		DatabaseConfig.inicializar();
		DatabaseConfig.verificar();
	}
}
