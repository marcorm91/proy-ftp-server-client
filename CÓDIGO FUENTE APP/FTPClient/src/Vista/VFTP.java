package Vista;

import Controlador.CFTP_Client;

import java.awt.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;

public class VFTP extends JFrame{

	private CFTP_Client cFTP_Client;
	private JTextField enviaTexto;
	private JTextArea areaTerminal;
	private JButton btnEnviar;
	private JScrollPane panelScroll;
	private JLabel lblLogin;
	private JLabel lblSubirFichero;
	private JTextField txtSubirFichero;
	private JLabel lblInfo1;
	private JTextField txtDescFichero;
	
	
	/**
	 * Constructor de la vista VFTP.
	 * @param cFTP_Client
	 */
	public VFTP(CFTP_Client cFTP_Client) {
		this.cFTP_Client = cFTP_Client;
		initialize();
	}

	
	/**
	 * Inicializa la aplicación con el Frame del login.
	 */
	public void run(String user) {
		setIconImage(Toolkit.getDefaultToolkit().getImage(VFTP.class.getResource("/Recursos/ftp.png")));
		setSize(1000,500);
		setTitle("Servicio FTP");
		setResizable(false);
		setLocationRelativeTo(null);
		lblLogin.setText(user);
		setVisible(true);
		
		//El mismo frame estará a la escucha de un evento sobre la X de la ventana.
		//Si el usuario hace clic sobre la X (cierra la aplicación) ésta enviará un mensaje de salida
		//al servidor para terminar la conexión entre ambos.
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				cFTP_Client.salidaApp_enLogin();
			}
		});
	}
	

	/**
	 * Inicializa componentes de la vista VFTP.
	 */
	private void initialize() {
		getContentPane().setLayout(null);
		
		areaTerminal = new JTextArea();
		areaTerminal.setFocusable(false);
		areaTerminal.setEditable(false);
		areaTerminal.setWrapStyleWord(true);
		areaTerminal.setLineWrap(true);
		
		panelScroll = new JScrollPane(areaTerminal);
		panelScroll.setRequestFocusEnabled(false);
		panelScroll.setBounds(12, 49, 595, 373);
		getContentPane().add(panelScroll);
		
		
		enviaTexto = new JTextField("");
		enviaTexto.addKeyListener(new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					cFTP_Client.escribeTerminal();
				}
			}
		});
		enviaTexto.setBounds(12, 434, 503, 23);
		enviaTexto.requestFocus();
		getContentPane().add(enviaTexto);
		enviaTexto.setColumns(10);
		
		btnEnviar = new JButton("Enviar");
		btnEnviar.setFocusable(false);
		btnEnviar.setBackground(Color.LIGHT_GRAY);
		btnEnviar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cFTP_Client.escribeTerminal();
			}
		});
		btnEnviar.setFont(new Font("Dialog", Font.BOLD, 10));
		btnEnviar.setBounds(527, 432, 80, 25);
		getContentPane().add(btnEnviar);
		
		JButton fondoNegro = new JButton("");
		fondoNegro.setFocusable(false);
		fondoNegro.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				areaTerminal.setBackground(Color.BLACK);
				areaTerminal.setForeground(Color.WHITE);
			}
		});
		fondoNegro.setBackground(Color.BLACK);
		fondoNegro.setBounds(913, 12, 23, 23);
		getContentPane().add(fondoNegro);
		
		JButton fondoBlanco = new JButton("");
		fondoBlanco.setFocusable(false);
		fondoBlanco.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				areaTerminal.setBackground(Color.WHITE);
				areaTerminal.setForeground(Color.BLACK);
			}
		});
		fondoBlanco.setBackground(Color.WHITE);
		fondoBlanco.setBounds(948, 12, 23, 23);
		getContentPane().add(fondoBlanco);
		
		JLabel lblConectadoComo = new JLabel("Conectado como:");
		lblConectadoComo.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblConectadoComo.setBounds(12, 12, 129, 15);
		getContentPane().add(lblConectadoComo);
		
		lblLogin = new JLabel("");
		lblLogin.setBounds(128, 12, 479, 15);
		getContentPane().add(lblLogin);
		
		lblSubirFichero = new JLabel("Subir fichero:");
		lblSubirFichero.setBounds(619, 75, 352, 15);
		getContentPane().add(lblSubirFichero);
		
		txtSubirFichero = new JTextField();
		txtSubirFichero.setFocusable(false);
		txtSubirFichero.setFont(new Font("Dialog", Font.PLAIN, 12));
		txtSubirFichero.setBackground(SystemColor.window);
		txtSubirFichero.setSelectionColor(Color.BLACK);
		txtSubirFichero.setEditable(false);
		txtSubirFichero.setBounds(619, 102, 230, 19);
		getContentPane().add(txtSubirFichero);
		txtSubirFichero.setColumns(10);
		
		JButton btnExaminarSubir = new JButton("Examinar...");
		btnExaminarSubir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				subirFich_FC();
			}
		});
		btnExaminarSubir.setFocusable(false);
		btnExaminarSubir.setRequestFocusEnabled(false);
		btnExaminarSubir.setBackground(Color.LIGHT_GRAY);
		btnExaminarSubir.setBounds(854, 99, 117, 25);
		getContentPane().add(btnExaminarSubir);
		
		lblInfo1 = new JLabel("<html>\n<body>\n\tIndica la ruta del fichero que quieres subir al servidor. <br>\n\tRecuerda que debes utilizar el comando <br> \n\t'up [ruta_destino_servidor]' en la terminal. <br><br>\n\n\tEl tamaño del fichero para la subida no debe exceder 5MB.\n</body>\n</html>");
		lblInfo1.setVerticalAlignment(SwingConstants.TOP);
		lblInfo1.setHorizontalAlignment(SwingConstants.LEFT);
		lblInfo1.setFont(new Font("Dialog", Font.BOLD, 10));
		lblInfo1.setBounds(619, 133, 352, 71);
		getContentPane().add(lblInfo1);
		
		JLabel lblDescargarFichero = new JLabel("Descargar fichero:");
		lblDescargarFichero.setBounds(619, 251, 352, 15);
		getContentPane().add(lblDescargarFichero);
		
		txtDescFichero = new JTextField();
		txtDescFichero.setSelectionColor(Color.BLACK);
		txtDescFichero.setFont(new Font("Dialog", Font.PLAIN, 12));
		txtDescFichero.setFocusable(false);
		txtDescFichero.setEditable(false);
		txtDescFichero.setColumns(10);
		txtDescFichero.setBackground(SystemColor.window);
		txtDescFichero.setBounds(619, 278, 230, 19);
		getContentPane().add(txtDescFichero);
		
		JButton btnDescargarFich = new JButton("Examinar...");
		btnDescargarFich.setRequestFocusEnabled(false);
		btnDescargarFich.setFocusable(false);
		btnDescargarFich.setBackground(Color.LIGHT_GRAY);
		btnDescargarFich.setBounds(854, 275, 117, 25);
		btnDescargarFich.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				descargarFich_FC();
			}
		});
		getContentPane().add(btnDescargarFich);
		
		JLabel lblInfo2 = new JLabel("<html>\n<body>\n\tIndica la ruta de tu directorio que será donde se descargue el fichero. <br>\n\tRecuerda que debes utilizar el comando <br> \n\t'down [ruta_fichero_servidor]' en la terminal. <br><br>\n\n\tEl tamaño del fichero para la descarga no debe exceder 5MB.\n</body>\n</html>");
		lblInfo2.setVerticalAlignment(SwingConstants.TOP);
		lblInfo2.setHorizontalAlignment(SwingConstants.LEFT);
		lblInfo2.setFont(new Font("Dialog", Font.BOLD, 10));
		lblInfo2.setBounds(619, 309, 352, 87);
		getContentPane().add(lblInfo2);
		
		JSeparator separator = new JSeparator();
		separator.setForeground(Color.BLACK);
		separator.setBounds(619, 224, 352, 15);
		getContentPane().add(separator);
	}

	
	/**
	 * Método que nos llevará a un FileChooser para la subida del fichero.
	 * Tras la elección del fichero, éste será mostrado como ruta absoluta en el JTextField de la subida.
	 */
	private void subirFich_FC() {
		JFileChooser fc = new JFileChooser();
		int sel = fc.showOpenDialog(getContentPane());

		File fichero = fc.getSelectedFile();
		
		if(sel == JFileChooser.APPROVE_OPTION){
			txtSubirFichero.setText(fichero.getAbsolutePath());
		}
	}
	
	
	/**
	 * Mismo proceso que el anterior, pero en este caso sólo podremos seleccionar directorios.
	 * El directorio seleccionado será el directorio de destino para la descarga del fichero desde el servidor.
	 */
	private void descargarFich_FC(){
		JFileChooser fc = new JFileChooser();
		fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

		int sel = fc.showOpenDialog(getContentPane());
		
		File archivo = fc.getSelectedFile();
		String ruta = archivo.getPath();
		
		if(sel == JFileChooser.APPROVE_OPTION){
			txtDescFichero.setText(ruta);
		}
	}

	//---------------- GETTERS AND SETTERS ---------------//
	
	public JTextArea getAreaTerminal() {
		return areaTerminal;
	}

	public void setAreaTerminal(JTextArea areaTerminal) {
		this.areaTerminal = areaTerminal;
	}

	public JTextField getEnviaTexto() {
		return enviaTexto;
	}

	public void setEnviaTexto(JTextField enviaTexto) {
		this.enviaTexto = enviaTexto;
	}

	public JScrollPane getPanelScroll() {
		return panelScroll;
	}

	public void setPanelScroll(JScrollPane panelScroll) {
		this.panelScroll = panelScroll;
	}

	public JTextField getTxtSubirFichero() {
		return txtSubirFichero;
	}

	public JTextField getTxtDescFichero() {
		return txtDescFichero;
	}
	
	//---------- MENSAJES DE ALERTA SOBRE EL USUARIO --------------//


	public void directorioNoEncontrado() {
		JOptionPane.showMessageDialog(null, "No se pudo enviar el fichero.  Comprueba que la ruta de destino es correcta.");
	}

	public void excesoTamanio() {
		JOptionPane.showMessageDialog(null, "El fichero que intentas descargar excede los 5MB.");
	}

	public void seleccionRuta() {
		JOptionPane.showMessageDialog(null, "Seleccione un fichero a subir.");
	}

	public void excesoSubida() {
		JOptionPane.showMessageDialog(null, "El fichero no debe superar 5MB.");
	}

	public void ficheroRecibido() {
		JOptionPane.showMessageDialog(null, "¡Fichero enviado con éxito!");
	}
	
	public void ficheroNoEncontrado() {
		JOptionPane.showMessageDialog(null, "¡No se pudo descargar el fichero!.  Comprueba que existe o que no es un directorio.");
	}

	public void seleccionFichero_Subida() {
		JOptionPane.showMessageDialog(null, "Debe seleccionar un fichero para subir al servidor.");
		getTxtSubirFichero().setBackground(new Color(255,204,204));
	}


	public void seleccionDirectorio_Descarga() {
		JOptionPane.showMessageDialog(null, "Debe seleccionar un directorio para descargar del servidor.");
		getTxtDescFichero().setBackground(new Color(255,204,204));
	}
	
	public void ficheroDescargado(String rutaDestino) {
		JOptionPane.showMessageDialog(null, "¡Fichero descargado con éxito en "+rutaDestino+"!");
		getTxtDescFichero().setText("");
	}

}
