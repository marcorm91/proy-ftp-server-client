package Lanzadera;

import Controlador.CFTP_Server;

public class Main {

	public static void main(String[] args) {
		CFTP_Server controlador = new CFTP_Server();
		controlador.start();
	}

}
