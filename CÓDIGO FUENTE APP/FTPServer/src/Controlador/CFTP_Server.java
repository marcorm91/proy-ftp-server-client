package Controlador;

import java.io.*;
import java.net.*;
import java.util.Calendar;

public class CFTP_Server extends Thread{
	
	private ServerSocket socket = null;
	private int puerto = 2121;
	private boolean servidorON;
	
	/**
	 * Constructor del controlador del propio servidor.
	 * Cuando se inicia por primera vez el servidor, éste creará un directorio servidor para los clientes FTP.
	 * Ese directorio se creará si no existe, y si existe no se creará.
	 */
	public CFTP_Server(){
		creadFTP();
	}

	
	/**
	 * Método que se encargará de iniciar el servidor.
	 */
	@Override
	public void run() {
		
		// Iniciamos el servidor en el puerto especificado.
		try {
			socket = new ServerSocket(puerto);
			System.err.println("- Servidor ON. Esperando conexiones... \n");
		} catch (IOException e) {
			System.err.println("\nError al abrir el puerto. Comprueba que no esté ocupado el puerto "+puerto);
		}
			
			while (!servidorON) {
				try{
					Socket socketCliente = null;
					
						socketCliente = socket.accept();
						
						System.err.println("\t("+Calendar.getInstance().getTime()+")"
										 + " Cliente con IP: " + socketCliente.getInetAddress() + " conectado.");
						
						Cliente cliente = new Cliente(socketCliente);
						Thread hilo = new Thread(cliente);
						hilo.start();
					} catch (IOException e) {
						System.err.println("Error al intentar conectarse un cliente.");
					}
			}
	}
		
	
	/**
	 * Cuando arrancamos el servidor por primera vez, el constructor del mismo llamará a
	 * este método para la creación del directorio que sirve.
	 * Lo creará si no existe, y si ya existe, no hará ninguna creación. 
	 * Además, dentro de la misma contendrá un directorio llamado public de acceso común.
	 */
	private void creadFTP(){
		//String dirUsuario = System.getProperty("user.home");		
		//File dir = new File(dirUsuario+"/dFTP/public");
		File dir = new File("dFTP/public");
		dir.mkdirs();
	}
	
}
