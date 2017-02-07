package Controlador;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.Calendar;

import Vista.*;

public class CFTP_Client extends Thread{
	
	private VLogin vistaLogin;
	private VFTP vistaFTP;
	
	/*********************************************************/
	
	private String mensaje;
	
	private Socket cliente;
	//private String ip = "192.168.1.136";
	//private String ip = "A224-PCB";
	private String ip = "ns3034756.ip-91-121-81.eu";
	private int puerto = 2121;
	private boolean estableceComunicacion = true;
	
	/** Nombre de usuario para el acceso FTP **/
	private String user;
	
	/** Flujos de datos **/
	private InputStream is;
	private OutputStream os;
	private BufferedReader br;
	private PrintWriter pw;
		
	/*********************************************************/
	
	public CFTP_Client(){
		vistaLogin = new VLogin(this);
		user = vistaLogin.getCampo_user().getText();
		vistaFTP = new VFTP(this);
		correLogin();
		
		iniciaConexion();		
		run();
	}
	
	
	/**
	 * Inicia login tras abrir aplicación.
	 */
	public void correLogin() {
		vistaLogin.run();
	}
	
	
	/**
	 * Abre nueva ventana FTP si usuario y contraseña son correctos.
	 */
	public void accedeFTP() {
		vistaFTP.run(user);
	}


	/**
	 * Inicia el flujo de conexión con el servidor.
	 */
	private void iniciaConexion() {
		try {
			
				cliente = new Socket(ip, puerto);
				is = cliente.getInputStream();
				os = cliente.getOutputStream();
				br = new BufferedReader(new InputStreamReader(is));
				pw = new PrintWriter(os);

		} catch (IOException e){
			System.err.println("Fallo en la conexión.  Verifique que el servidor está ON.");
		}
	}
	
	
	/**
	 * Cierra el flujo de conexión con el servidor.
	 */
	private void cierraConexion(){
		try{
			
			is.close();
			os.close();
			br.close();
			pw.close();	
			cliente.close();
			System.err.println("\t("+Calendar.getInstance().getTime()+") "
							 + "... Flujo cerrado satisfactoriamente para "+cliente.getInetAddress());
			
		}catch(IOException e){
			System.err.println("Error al cerrar flujos.");
		}
	}
	
	
	@Override
	public void run() {
		
			while(estableceComunicacion){
				estableceComunicacion();
			}
		
		cierraConexion();
	}



	private void estableceComunicacion() {
		
		String escucha = null;
		
		try {
				escucha = br.readLine();

	            while (escucha != null) {
	            	
	            	switch(escucha){
	            	
	            		case "LoginOK":
	            			vistaFTP.getAreaTerminal().append(" ¡Bienvenido al servicio de FTP, "+user+"!\n");
		            		vistaFTP.getAreaTerminal().append(" Recuerda que puedes guiarte de una ayuda con /help\n");
		                	vistaLogin.setVisible(false);
		                	accedeFTP();
		                	escucha = br.readLine();
		                break;
		                
	            		case "LoginNOOK":
	            			vistaLogin.loginIncorrecto();
	            			escucha = br.readLine();
	            		break;	
	            		
	            		case "FicheroRecibido":
	            			vistaFTP.ficheroRecibido();
	            			escucha = br.readLine();
	            		break;
	            			            		
	            		case "FicheroNoEncontrado":
	            			vistaFTP.ficheroNoEncontrado();
	            			escucha = br.readLine();
	            		break;
	            		
	            		case "DirectorioNoEncontrado":
	            			vistaFTP.directorioNoEncontrado();
	            			escucha = br.readLine();
	            		break;
	            		
	            		case "FicheroEnviado":
	            			descargarFichero();
	            			escucha = br.readLine();
	            		break;
	            		
	            		case "Excesotamanio":
	            			vistaFTP.excesoTamanio();
	            			escucha = br.readLine();
	            		break;
	            		
	            		case "SeleccionaRuta":
	            			vistaFTP.seleccionRuta();	            			
	            			escucha = br.readLine();
	            		break;

	            		default:
		            		vistaFTP.getAreaTerminal().append(escucha);
		 	                vistaFTP.getAreaTerminal().append("\n");
		 	                escucha = br.readLine();
		 	                desplazaScroll();
		 	            break;
	            	
	            	}
	                
	            }

			}catch(IOException e){
				System.err.println("Error al establecer la comunicación con el servidor.");
			}
				
	}

	/**
	 * Método que envía por línea de terminal al servidor lo que el cliente escriba y envíe.
	 * Si el cliente escribe algo que no sea interpretado por los 3 primeros 'cases' del switch,
	 * por defecto se irá al método mensaje() que será el método que envíe el mensaje al servidor.
	 */
	public void escribeTerminal() {
		
		vistaFTP.getAreaTerminal().append("\n > "+vistaFTP.getEnviaTexto().getText());
		String lineaEntrada = vistaFTP.getEnviaTexto().getText();

		switch(lineaEntrada){
			
			// Si el cliente envía /help por la consola simplemente lanza un texto
			// de ayuda con una lista de comandos a utilizar por la terminal.
			// Comando local del cliente.
			case "/help":
				ayudaFTP();
			break;
			
			// No hace falta llamar al servidor para limpiar la pantalla del cliente.
			// Comando local del cliente.
			case "/clear":
				vistaFTP.getAreaTerminal().setText("");
			break;
			
			// Si el cliente envía /exit por la consola, la orden es enviada al servidor
			// para que éste finalice la conexión del cliente.
			case "/exit":
				salidaApp_enFTP();
			break;
			
			// En su defecto, enviamos al servidor cualquier comando.  El servidor está a la espera
			// de recibir algo para que cuando llegue a su controlador lo interprete de una manera
			// u otra.
			default:
				mensaje();
			break;

		}			

		vistaFTP.getEnviaTexto().setText("");
			
	}
	
	
	/**
	 * Lanza una pequeña ayuda a la terminal para que el cliente se sepe manejar por ella.
	 * Son una serie de comandos básicos de terminal.
	 */
	private void ayudaFTP(){
		vistaFTP.getAreaTerminal().append("\n\n\tBIENVENIDO/A A LA AYUDA DE LA APLICACIÓN FTP\n"
				+ "\t--------------------------------------------------------------------------\n"
				+ " /help: \t    Ayuda básica de la aplicación FTP.\n"
				+ " /exit: \t    Cierre de la aplicación. \n"
				+ " /clear:\t    Limpia el área de texto de la terminal. \n"
				+ " dir:   \t    Muestra el contenido del directorio actual. \n"
				+ " dir [dir]: \t    Muestra el contenido del directorio especificado. \n"
				+ " mkdir [dir]: \t    Crea directorio en el directorio especificado. \n"
				+ " del [dir | file]:    Elimina el directorio o fichero seleccionado.\n"
				+ " w [file]: \t    Descarga el fichero al directorio de destino seleccionado.\n"
				+ " u [dir_destino]: Envía el fichero seleccionado al directorio de destino.");
		vistaFTP.getAreaTerminal().append("\n");
		desplazaScroll();
	}
	

	/**
	 * Este método se encargará de obtener el mensaje a través del JTextField de la terminal y 
	 * será lo que se envíe al servidor en cualquier caso.
	 */
	private void mensaje(){
		desplazaScroll();
		mensaje = vistaFTP.getEnviaTexto().getText();
		
		if(!mensaje.equalsIgnoreCase("")){
		
			//Si el comando que se quiere enviar es 'u'[SUBIR FICHERO] ...
			if(mensaje.substring(0, 1).equalsIgnoreCase("u") ||  !vistaFTP.getTxtSubirFichero().getText().equalsIgnoreCase("")){
				
				if(mensaje.trim().length() >= 2){
					
					//Antes de enviar el mensaje tenemos que comprobar que la longitud del fichero de subida sea > 0.
					//Si el tamaño es = 0 quiere decir que no se seleccióno fichero alguno en el campo de subida.
					if(tamanioFichero() != 0){
						//Si pasa la condición anterior, comprobamos que el fichero no exceda los 5MB.
						if(tamanioFichero() < 5242880){
							pw.println(mensaje);
							pw.flush();
							subirFichero();
						}else{
							// Sino, quiere decir que se excedió en el tamaño del mismo.
							vistaFTP.excesoSubida();
						}
					}else{
						//Si el fichero es = 0, quiere decir que no se seleccionó fichero alguno para la subida.
						vistaFTP.seleccionFichero_Subida();
					}
					vistaFTP.getTxtSubirFichero().setText("");
					desplazaScroll();
				}else{
					vistaFTP.getAreaTerminal().append("\n\n\tSintaxis 'subir fich': u [ruta_destino]");
				}
			}else{
				
				// Si el comando que se quiere enviar es 'w' [DESCARGAR FICHERO] ...
				if(mensaje.substring(0, 1).equalsIgnoreCase("w")  || !vistaFTP.getTxtDescFichero().getText().equalsIgnoreCase("")){
					
					// En primer lugar comprobar que se elija una ruta de destino para guardar el fichero.
					if(vistaFTP.getTxtDescFichero().getText().equalsIgnoreCase("")){
						vistaFTP.seleccionDirectorio_Descarga();
					}else{
						
						//Si se seleccionó alguna, comprobar que haya algo que descargar del servidor poniendo 'w fich_origen'
						if(mensaje.trim().length() >= 2){
							pw.println(mensaje);
							pw.flush();
						
						//Sino fue así, enviamos un mensaje al usuario de como descargar fichero.
						}else{
							vistaFTP.getAreaTerminal().append("\n\n\tSintaxis 'descargar fich': w [fich_origen]");
						}
					}
					
				//En cualquier caso (listar, eliminar, crear...) enviar mensaje.
				}else{
					pw.println(mensaje);
					pw.flush();
				}
				
			}
		
		}
		
	}
	
	
	/**
	 * Método que se encargará de iniciar la conexión con el servicio de FTP.
	 * En primer lugar obtenemos usuario y contraseña de los respectivos campos.
	 * Hacemos una comprobación de que ambos campos estén rellenos en el mismo cliente para ahorrar 
	 * sobrecarga al servidor.
	 * Cuando ambos campos contengan algo, enviamos al servidor el usuario y contraseña y allí interpretará
	 * el sistema de login cuando lo tenga que hacer.
	 */
	public void conexionLogin() {

		user = vistaLogin.getCampo_user().getText();
		String pass = String.valueOf(vistaLogin.getCampo_pass().getPassword());
		
		if(user.equalsIgnoreCase("")){
			vistaLogin.campoVacio("user");
		}else 
			if (pass.equalsIgnoreCase("")){
				vistaLogin.campoVacio("pass");
			}else{
				pw.println(user+" "+pass);
				pw.flush();	
			}
	}
	
	
	/**
	 * Si el cliente envía '/exit', llegará al servidor y éste lo interpretará como un comando de salida.
	 * Cuando le llegue, el servidor se encargará de cerrar la comunicación con el mismo y posteriormente sea
	 * el cliente el que la cierre con él.
	 * Esta salida se hará cuando el cliente esté conectado al servicio.
	 */
	private void salidaApp_enFTP() {
		mensaje();
 		estableceComunicacion = false;
 		System.exit(0);		
	}
	
	
	/**
	 * A diferencia de la anterior, esta salida se hará sin que el cliente haya accedido aún al servicio de FTP.
	 * Es un fin de comunicación desde el login.
	 */
	public void salidaApp_enLogin(){
		pw.println("exit");
		pw.flush();
		estableceComunicacion = false;
		System.exit(0);
	}
	
	
	/**
	 * Antes de realizar el método subirFichero() hemos de comprobar que tamaño tiene el fichero que deseamos subir.
	 * Si es = 0, quiere decir que se está intentando subir algo que no es nada, es decir, el usuario lanza el comando
	 * up sobre la terminal con una ruta de destino y no seleccionó fichero alguno para la subida.
	 * @return Devuelve el tamaño de fichero que está en el campo subir.
	 */
	public int tamanioFichero(){
		File fichero = new File(vistaFTP.getTxtSubirFichero().getText());
		int tamanioFichero = (int) fichero.length();
		
		return tamanioFichero;
	}
	
	
	/**
	 * Método que hará la subida del fichero que se seleccione en el campo de subir fichero.
	 * Para que este método se ejecute es necesario pasar antes por otro método que calcula el tamaño del fichero
	 * que se quiere subir.  Si el tamaño = 0, quiere decir que no se seleccionó archivo alguno para la subida, de lo
	 * contrario procedemos con la ejecución del método.
	 */
	public void subirFichero(){

		//Para que haya un tiempo de respuesta entre servidor-cliente y la ejecución no se haga tan rápida
		//dormimos el hilo durante medio segundo.
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		vistaFTP.getTxtSubirFichero().setBackground(new Color(223,223,233));	
		
		//Seleccionamos el fichero que introdujo el usuario en el campo subida fichero.
		File fichero = new File(vistaFTP.getTxtSubirFichero().getText());
		
		FileInputStream fis;
		DataOutputStream dos;
		BufferedInputStream bis;
		BufferedOutputStream bos;
		byte[] buffer;
		int tamanioFichero = (int) fichero.length();
		
		//Si el fichero existe procedemos con el envío.
		if(fichero.exists()){
			
			try {
				dos = new DataOutputStream(cliente.getOutputStream());
				dos.writeUTF(fichero.getName());
				dos.writeInt(tamanioFichero);
				System.err.println("Enviando fichero... "+fichero.getName()+ " con tamaño "+tamanioFichero);
				
				fis = new FileInputStream(fichero);
				bis = new BufferedInputStream(fis);
				bos = new BufferedOutputStream(cliente.getOutputStream());
				buffer = new byte[tamanioFichero];
				bis.read(buffer);
				
					for(int i = 0; i < buffer.length; i++){
						bos.write(buffer[i]);
					}
				
				bos.flush();				
				
			}catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Método que se encargará de descargar el fichero que haya seleccionado previamente el usuario por terminal.
	 * Cuando el servidor le envíe la orden de 'FicheroEnviado', el cliente estará en escucha y cuando capte el mensaje
	 * procederá a la ejecución del método para la descarga.
	 */
	private void descargarFichero() {
		
		//Para que haya un tiempo de respuesta entre servidor-cliente y la ejecución no se haga tan rápida
		//dormimos el hilo durante medio segundo.
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		vistaFTP.getTxtDescFichero().setBackground(new Color(223,223,233));	
		
		String rutaDestino = vistaFTP.getTxtDescFichero().getText();
		DataInputStream dis;
		FileOutputStream fos;
		BufferedOutputStream bos;
		String nombreFichero;
		BufferedInputStream bis;
		int tam;
		
			try{

				//Preparamos el canal de envío y la ruta de destino del fichero para la subida.
				dis = new DataInputStream(cliente.getInputStream());
				nombreFichero = dis.readUTF().toString();
				tam = dis.readInt();
				fos = new FileOutputStream(rutaDestino+"/"+nombreFichero);
				bos = new BufferedOutputStream(fos);
				bis = new BufferedInputStream(cliente.getInputStream());
				byte[] buffer = new byte[tam];
					
					for (int i = 0; i < buffer.length; i++){
						buffer[i] = (byte) bis.read();
					}
					
				bos.write(buffer);
				bos.flush();
				
				vistaFTP.ficheroDescargado(rutaDestino);
			
					
			}catch(IOException e){
				System.out.println(e);
			}
	}
	
	
	/**
	 * Este método se encargará de desplazarse automáticamente por la terminal cada vez que el cliente
	 * envíe cualquier consulta.
	 */
	private void desplazaScroll(){
		 Dimension maximaAltura = vistaFTP.getAreaTerminal().getSize();
         Point p = new Point(0, maximaAltura.height);
         vistaFTP.getPanelScroll().getViewport().setViewPosition(p);
	}
}
