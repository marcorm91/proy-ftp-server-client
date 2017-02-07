package Controlador;

import java.io.*;
import java.net.Socket;
import java.sql.*;
import java.util.Calendar;


import Modelo.MUsuarios;

public class Cliente implements Runnable{
	
	private MUsuarios modeloUsuarios;
	private Connection conexion;
	
	private Socket socket;
	private OutputStream os;
	private InputStream is;
	private BufferedReader br;
	private PrintWriter pw;
	private boolean estableceComunicacion = true;
	
	private boolean login = false;
	private String user = null;
	private boolean existeRuta = false;
	
	/**
	 * Constructor de Cliente.
	 * @param clientSocket Le pasamos como parámetro el socket del cliente.
	 */
	public Cliente(Socket clientSocket) {
		conexionBD();
		modeloUsuarios = new MUsuarios(conexion);
		socket = clientSocket;
		iniciaCanal();
	}
	
	/**
	 * Iniciamos la conexión sobre la BD con el cliente que se conecte.
	 */
	private void conexionBD() {
		String bd = "jdbc:postgresql://ns3034756.ip-91-121-81.eu/amromero";
        String user = "amromero";
        String pass = "amromero";
        
        try {
            conexion = DriverManager.getConnection(bd, user, pass);
        } catch (SQLException ex) {
            System.err.println(ex);
        }
	}

	
	/**
	 * Iniciamos el proceso y la ejecución del programa sobre el cliente que se haya conectado al servidor.
	 * Método sobrecargado de Thread.
	 */
	@Override
	public void run() {

		//Establecemos en continua escucha la comunicación entre el servidor - cliente.
		while(estableceComunicacion){
			estableceComunicacion();
		}
		
		//Cuando el cliente cierra la aplicación terminamos la conexión con la BD.
		try {
			conexion.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		//Y posteriormente cerramos los flujos del mismo.
		cerrarFlujos();
	}

	
	/**
	 * Cerramos los flujos entre cliente-servidor.
	 */
	private void cerrarFlujos() {
		try {
			is.close();
			os.close();
			br.close();
			pw.close();
			socket.close();
		} catch (IOException e) {
			System.err.println("Error al cerrar el flujo/conexión del cliente.");
		}
	}

	
	/**
	 * Iniciamos el canal entre cliente-servidor.
	 */
	private void iniciaCanal() {
		try {
			is = socket.getInputStream();
			os = socket.getOutputStream();
			br = new BufferedReader(new InputStreamReader(is));
			pw = new PrintWriter(os);
		} catch (IOException e) {
			System.err.println("Error al iniciar flujos del cliente.");
		}
	}
	
	
	/**
	 * Este método estará en run(), y será el encargado de mantener la comunicación entre
	 * el cliente y el servidor.
	 */
	private void estableceComunicacion(){
		
		try {
			
			String lineaEntrada = br.readLine();
			
			// Iremos jugando con la cadena que nos envía el cliente.  La cadena que nos envía puede estar
			// compuesta por más de una palabra.
			// Para interpretarla, debemos splitear esa palabra y ver que nos llega desde la otra parte.
			
			String [] escucha = lineaEntrada.split(" ");
			
			// Comprueba que el cliente envió 'exit' para hacer la salida inmediata del programa y cerrar
			// la comunicación entre servidor y cliente.
			// DETALLAR: Esta salida la hace en el intento de logueo.  Aún no está logueado.
			if(escucha[0].equalsIgnoreCase("exit")){
				System.err.println("\t("+Calendar.getInstance().getTime()+")"
						 + " Cerrando comunicación con "+socket.getInetAddress());
				estableceComunicacion = false;
			}
			
			// Mientras estemos estableciendo la comunicación con el cliente, el booleano 'login' se iniciará
			// en false. Cuando al servidor le llega user y pass interpretándolo con el método split, hará
			// el intento de login lléndose al método que tenemos en el modelo.
			// El modelo retornará true si hubo una existencia e igualidad de usuario y clave.  Cuando la haya,
			// login será true, por lo que ya no será necesario su pasada por este punto.  Además, le enviaremos
			// al cliente el acceso con la vista FTP.
			if(!login){
			
				try{
					if(!escucha[0].equalsIgnoreCase("") || !escucha[1].equalsIgnoreCase("")){
						login = modeloUsuarios.accesoLogin(escucha[0], escucha[1]);
						if(login){
							// Cuando accede por primera vez, se creará una carpeta personal con el nombre de su usuario.
							// Cuando se loguee por segunda vez, no se volverá a crear el directorio y se conservará
							// lo que tenga dentro del mismo.
							creaDirectorio_porDefecto(escucha[0]);
							
							// El nombre de usuario lo guardaremos para restringir el acceso en otros directorios.
							user = escucha[0];
							
							// Envío al cliente de que el login fue correcto.
							pw.println("LoginOK");
							pw.flush();
						}
						else{
							// El login no es correcto.
							pw.println("LoginNOOK");
							pw.flush();
						}
					}
				}catch(ArrayIndexOutOfBoundsException e){}
				
			}else{
				
				// Llegados a este punto, el usuario accedió por fin a la vista del servicio FTP, por lo que
				// podrá interactuar con una serie de comandos sobre el mismo.
				switch (escucha[0]) {
					
					// Si lo que recibe el servidor es /exit cerraremos la comunicación con dicho cliente.
					case "/exit":
						System.err.println("\t("+Calendar.getInstance().getTime()+")"
										 + " Cerrando comunicación con "+socket.getInetAddress());
						estableceComunicacion = false;
					break;
					
					// Si recibimos dir haremos un listado del directorio actual donde nos encontremos.
					// Y si recibimos 'dir + /dir/dir/...', haremos el listado sobre el último dir que se le envió
					// en la segunda cadena.
					case "dir":
						if(escucha.length != 1){
							recogeDirectorios(escucha[1]);
						}else{
							recogeDirectorioActual();
						}
					break;
					
					// Si recibimos 'del + fich/dir' eliminamos dicho elemento.
					// La condición nos sirve para que la longitud de dicha cadena sea válida
					// y no salte error.  De escribir sólo 'del' el servidor enviará un mensaje
					// de sintaxis al usuario.
					case "del":
						if(escucha.length != 1){
							eliminaDirectorio(escucha[1]);
						}else{
							pw.println("\n\n\tSintaxis 'eliminar dir|fil': del [dir | file]");
							pw.flush();
						}
					break;
					
					//Con este método creamos un directorio donde le indiquemos.  Se requiere de dos cadenas,
					//una para saber que vamos a crear un directorio (mkdir) y otra para darle nombre al mismo.
					case "mkdir":
						if(escucha.length != 1){
							creaDirectorio(escucha[1]);
						}else{
							pw.println("\n\n\tSintaxis 'crear dir': mkdir [dir]");
							pw.flush();
						}
					break;
					
					//En este caso estamos llamando al comando de subida de fichero al servidor.
					//Para ello esperamos que la primera cadena sea 'up'.
					case "u":
						existeRuta = existeDirectorio(escucha[1]);
						if(escucha.length != 1 && existeRuta){
							escribeFich(escucha[1]);
						}else{
							pw.println("DirectorioNoEncontrado");
							pw.flush();
						}
						existeRuta = false;
					break;
					
					//Si al servidor le llega 'w' sobre el primer elemento de la cadena, quiere decir
					//que se tiene que preparar para descargar lo que el usuario le envíe por consola.
					case "w":
						existeRuta = existeDirectorio(escucha[1]);
						if(escucha.length != 1 && existeRuta){
							enviaFich(escucha[1]);
						}else{
							pw.println("FicheroNoEncontrado");
							pw.flush();
						}
						existeRuta = false;
					break;
										
				}
			}
				
			}catch(IOException e){
				e.printStackTrace();
			}
		
	}
	
	
	/**
	 * Comprueba la existencia de un directorio cuando el cliente utilice la segunda cadena del comando
	 * como ruta de destino.
	 * @param directorio Directorio que le pasará el cliente.
	 * @return Devolverá true si encontró el directorio y false si no fue así.
	 */
	private boolean existeDirectorio(String directorio) {
		//String dirUsuario = System.getProperty("user.home");
		//File ruta = new File(dirUsuario+"/dFTP/"+directorio);
		File ruta = new File("dFTP/"+directorio);
				
		if(ruta.exists()){
			return true;
		}else{
			return false;
		}
	}

	
	/**
	 * Este método se encarga de enviar el fichero al usuario.
	 * @param fichero Este parámetro es lo que le proporcione el usuario por consola en la segunda cadena.
	 */
	private void enviaFich(String fichero) {
		
		//String dirUsuario = System.getProperty("user.home");
		//File ruta = new File(dirUsuario+"/dFTP/"+fichero);
		File ruta = new File("/dFTP/"+fichero);

		FileInputStream fis;
		DataOutputStream dos;
		BufferedInputStream bis;
		BufferedOutputStream bos;
		byte[] buffer;
		int tamanioFichero = (int) ruta.length();
		
		String [] split = fichero.split("/");
		
		// Si el nombre de usuario (su carpeta) o en su defecto, el usuario que está conectado es Admin 
		// procedemos con el envío del archivo al usuario.
		if(split[0].equalsIgnoreCase(user) || split[0].equalsIgnoreCase("public") || user.equalsIgnoreCase("admin")){	
		
			// Si la ruta proporcionada por el usuario existe y además es un fichero...
			if(ruta.exists() && ruta.isFile()){
				
				// Y por último, si el fichero no excede los 5 MB...
				if(tamanioFichero < 5242880){
			
					pw.println("FicheroEnviado");
					pw.flush();		
						
						try {
											
							dos = new DataOutputStream(socket.getOutputStream());
							dos.writeUTF(ruta.getName());
							dos.writeInt(tamanioFichero);
							System.err.println("\t"+socket.getInetAddress()+" descargando "+ruta.getName()+ " con tamaño "+tamanioFichero);
							
							fis = new FileInputStream(ruta);
							bis = new BufferedInputStream(fis);
							bos = new BufferedOutputStream(socket.getOutputStream());
							buffer = new byte[tamanioFichero];
							bis.read(buffer);
							
								for(int i = 0; i < buffer.length; i++){
									bos.write(buffer[i]);
								}
											
							bos.flush();
											
						}catch (IOException e) {
							e.printStackTrace();
						}
						
				}else{
					pw.println("Excesotamanio");
					pw.flush();
				}
				
			}else{
				pw.println("FicheroNoEncontrado");
				pw.flush();
			}
		
		}else{
			pw.println("\n\t ¡Acceso restringido! Sólo puedes descargar ficheros de "+user+"/ ó public/");
			pw.flush();
		}

	}

	
	/**
	 * Realiza la subida del fichero que elija el usuario.
	 * @param directorio Este parámetro es el directorio destino de subida de fichero.
	 */
	private void escribeFich(String directorio) {
		
		//String dirUsuario = System.getProperty("user.home");
				
		DataInputStream dis;
		FileOutputStream fos;
		BufferedOutputStream bos;
		String nombreFichero;
		BufferedInputStream bis;
		int tam;
		
		//Spliteamos para obtener el primer elemento de la cadena, por ejemplo, /marco/blablabla --> marco.
		String [] split = directorio.split("/");
		
		
		// Si el directorio existe y el primer elemento de split es el nombre de usuario (su carpeta) o 
		// en su defecto, el usuario que está conectado es Admin procedemos con la escritura del fichero.
		if(split[0].equalsIgnoreCase(user) || split[0].equalsIgnoreCase("public") || user.equalsIgnoreCase("admin")){			
			
			try{

				//Preparamos el canal de envío y la ruta de destino del fichero para la subida.
				dis = new DataInputStream(socket.getInputStream());
				nombreFichero = dis.readUTF().toString();
				tam = dis.readInt();
				//fos = new FileOutputStream(dirUsuario+"/dFTP/"+directorio+"/"+nombreFichero);
				fos = new FileOutputStream("dFTP/"+directorio+"/"+nombreFichero);
				bos = new BufferedOutputStream(fos);
				bis = new BufferedInputStream(socket.getInputStream());
				byte[] buffer = new byte[tam];
					
					for (int i = 0; i < buffer.length; i++){
						buffer[i] = (byte) bis.read();
					}
					
				bos.write(buffer);
				bos.flush();
					
				pw.println("FicheroRecibido");
				pw.flush();
					
			}catch(IOException e){
				System.out.println(e);
			}
		}else{
			pw.println("\n\n\t ¡Acceso restringido! Sólo puedes subir fichero en "+user+"/ ó public/");
			pw.flush();
		}

	}

	
	/**
	 * Crea el directorio por defecto tras la primera sesión del usuario en el servicio FTP.
	 * @param directorio
	 */
	private void creaDirectorio_porDefecto(String directorio) {
		//String dirUsuario = System.getProperty("user.home");
		//File dir = new File(dirUsuario+"/dFTP/"+directorio);
		File dir = new File("dFTP/"+directorio);
		dir.mkdir();
	}

	
	/**
	 * Crea directorio sobre la ruta indicada.
	 * @param directorio
	 */
	private void creaDirectorio(String directorio) {
		//String dirUsuario = System.getProperty("user.home");
		//File dir = new File(dirUsuario+"/dFTP/"+directorio);
		File dir = new File("dFTP/"+directorio);
		
		//Spliteamos para obtener el primer elemento de la cadena, por ejemplo, /marco/blablabla --> marco.
		String [] split = directorio.split("/");
		
		// Si el directorio existe y el primer elemento de split es el nombre de usuario (su carpeta) o 
		// en su defecto, el usuario que está conectado es Admin procedemos con el borrado de directorio o fichero.
		if(split[0].equalsIgnoreCase(user) || split[0].equalsIgnoreCase("public") || user.equalsIgnoreCase("admin")){
			pw.println("\n\n\t "+directorio+" creado con éxito.");
			pw.flush();
			dir.mkdir();
		
		// Sino pasa la primera condición estamos ante la situación de que el usuario está
		// intentando crear algo donde no debe.
		}else{
			pw.println("\n\n\t ¡Acceso restringido! Sólo puedes crear en "+user+"/ ó public/");
			pw.flush();
		}
	}


	/**
	 * Procedimiento que realiza la eliminación del fichero/directorio seleccionado.
	 * @param fichero/directorio: Es el segundo elemento de la cadena, refiriéndose así al propio fichero.
	 */
	private void eliminaDirectorio(String directorio) {
		
		//Nos servirá para situarnos en el /home/usuario del servidor.
		//Luego, este String nos devuelve la ruta completa del /home del servidor
		//junto con el nombre de usuario que le tenga asignado como directorio.
		//String dirUsuario = System.getProperty("user.home");
		
		//Por ejemplo, dir --> /home/servidor/dFTP/fichero.txt
		//File dir = new File(dirUsuario+"/dFTP/"+directorio);
		
		File dir = new File("dFTP/"+directorio);
		
		//Spliteamos para obtener el primer elemento de la cadena, por ejemplo, /marco/blablabla --> marco.
		String [] split = directorio.split("/");
		
		//Si el directorio existe y el usuario es el propietario, o en su defecto, admin...
		if(dir.exists() && split[0].equalsIgnoreCase(user) || user.equalsIgnoreCase("admin")){
			
			//Para empezar vamos a comprobar que dir sea directorio,
			if(dir.isDirectory()){
				
				//Si el contenido del directorio es 0, procedemos con el borrado directo del directorio.
				if(dir.list().length == 0){			
					dir.delete();
				}
				
				//De no ser así, quiere decir que hay mas de 1 elemento dentro,
				else{
					
					//Aquí emplearemos la recursividad, donde iremos recorriendo desde el último hasta primer
					//elemento del directorio en árbol.  Para ello, llamaremos de nuevo al método hasta que 
					//lleguemos al último.
					for(String temp : dir.list()){
						File fileDelete = new File(dir, temp);
						fileDelete.delete();
						//Llamada de nuevo al método donde le pasamos la ruta del último directorio / fichero encontrado.
						eliminaDirectorio(fileDelete.getPath());
					}
					
					//Si en el directorio en el que nos encontramos contiene 0 elementos, procedemos al borrado del
					//propio directorio.
					if(dir.list().length == 0){
						dir.delete();
					}
				}
			}else{
				dir.delete();
			}
			
		}else{
			
			//Si el directorio existe y no cumplió la primera condición, quiere decir que el usuario quiere borrar
			//un contenido ajeno al suyo.
			if(dir.exists()){
				pw.println("\n\n\t ¡Acceso restringido! No puedes eliminar contenido ajeno.");
				pw.flush();
			}
			
		}

	}
	

	/**
	 * Este método recibe un parámetro que será una cadena de ruta de directorios.
	 * Habrá una serie de condiciones para privar el acceso a contenido ajeno del usuario que se loguea.
	 * @param directorio
	 */
	public void recogeDirectorios(String directorio){
		
		//String dirUsuario = System.getProperty("user.home");
		//File dir = new File(dirUsuario+"/dFTP/"+directorio);	
		
		File dir = new File("dFTP/"+directorio);
			
		String [] ficheros = dir.list();
		String rutaCompleta;
		
		//Spliteamos para obtener el primer elemento de la cadena, por ejemplo, /marco/blablabla --> marco.
		String [] split = directorio.split("/");
		
		// Si el directorio existe y además, el directorio al que quiere acceder es igual a su nombre de
		// usuario o la carpeta es public, tendrá acceso al listado de directorios.
		// El usuario 'admin', tendrá acceso a todo el contenido del servicio FTP.
		if(dir.exists() && split[0].equalsIgnoreCase(user) || directorio.equalsIgnoreCase("public") || user.equalsIgnoreCase("admin")){
			
			pw.println("\n   Listado de ficheros y directorios en: dFTP/"+directorio);
			pw.flush();
			
			for(int i = 0; i < ficheros.length; i++){
				// Si el fichero comienza por . no se enviará al cliente.
				// Este tipo de ficheros son ocultos del sistema.  No nos interesa.
				if(!ficheros[i].substring(0, 1).equalsIgnoreCase(".")){
					
					rutaCompleta = dir.toString().concat("/"+ficheros[i]);
					File fil = new File(rutaCompleta);
					
					if(fil.isDirectory()){
						pw.println(" - [D] "+ficheros[i]);
						pw.flush();
					}else{
						if(fil.isFile()){
							pw.println(" - [F] "+ficheros[i]);
							pw.flush();
						}
					}
				}
			}
		
		// De lo contrario, es porque está intentando mostrar un directorio ajeno a los descrito anteriormente y por lo tanto,
		// se enviará un mensaje al usuario de acceso restringido.
		}else{
			if(dir.exists()){
				pw.println("\n\n\t ¡Acceso restringido! Sólo tienes acceso a public/ y "+user+"/");
				pw.flush();
				
			//Sino, es que no existe tal directorio.
			}else{
				pw.println("\n\n\t ¡No existe tal directorio!");
				pw.flush();
			}
		}
	}
	
	
	/**
	 * Recoge un listado de directorios en la ruta actual donde se encuentre dicho usuario.  Es como ejecutar
	 * el comando ls . en Linux.
	 */
	public void recogeDirectorioActual(){
			
		//String dirUsuario = System.getProperty("user.home");
		//File ruta = new File(dirUsuario+"/dFTP");
		
		File ruta = new File("dFTP");
		
		String [] directorios = ruta.list();
		String rutaCompleta;

		
		if(ruta.exists()){
			
			pw.println("\n   Listado de ficheros y directorios en: dFTP/");
			pw.flush();
			
			for(int i = 0; i < directorios.length; i++){
				// Si el fichero comienza por . no se enviará al cliente.
				// Este tipo de ficheros son ocultos del sistema.
				if(!directorios[i].substring(0, 1).equalsIgnoreCase(".")){
					
					rutaCompleta = ruta.toString().concat("/"+directorios[i]);
					File fil = new File(rutaCompleta);
					
					if(fil.isDirectory()){
						pw.println(" - [D] "+directorios[i]);
						pw.flush();
					}else{
						if(fil.isFile()){
							pw.println(" - [F] "+directorios[i]);
							pw.flush();
						}
					}
				}
			}
		}	
	}
	
	
}
