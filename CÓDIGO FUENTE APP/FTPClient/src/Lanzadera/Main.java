package Lanzadera;

import Controlador.CFTP_Client;

public class Main {

	public static void main(String[] args) {
		CFTP_Client cFTP_Client = new CFTP_Client();
		cFTP_Client.start();
	}

}
