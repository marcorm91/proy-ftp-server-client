package Vista;

import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

import Controlador.CFTP_Client;

public class VLogin extends JFrame{
	
	private CFTP_Client cFTP_Client;

	private JTextField campo_user;
	private JPasswordField campo_pass;
	private JPanel panelLogin, panelTitulo;
	private JLabel lblAccesoFtp;
	private JButton btnAcceder;

	/**
	 * Constructor de la vista VLogin.
	 * @param cFTP_Client
	 */
	public VLogin(CFTP_Client cFTP_Client) {
		initialize();
		this.cFTP_Client = cFTP_Client;
	}
	
	/**
	 * Inicializamos el JFrame de la vista Login.  Éste método será llamado desde el controlador.
	 */
	public void run() {
		setSize(350,350);
		setTitle("Acceso FTP - Login");
		setIconImage(Toolkit.getDefaultToolkit().getImage(VLogin.class.getResource("/Recursos/ftp.png")));
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
	 * Método que inicializa todos los elementos de la vista Login.
	 */
	private void initialize() {
		getContentPane().setLayout(null);
		
		panelLogin = new JPanel();
		panelLogin.setBounds(0, 12, 334, 300);
		getContentPane().add(panelLogin);
		panelLogin.setLayout(null);
		
		JLabel label_user = new JLabel("Usuario:");
		label_user.setBounds(66, 83, 70, 15);
		panelLogin.add(label_user);
		
		JLabel lblContrasea = new JLabel("Contraseña:");
		lblContrasea.setBounds(66, 157, 93, 15);
		panelLogin.add(lblContrasea);
		
		campo_user = new JTextField();
		campo_user.setBounds(66, 110, 216, 19);
		panelLogin.add(campo_user);
		campo_user.setColumns(10);
		campo_user.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					cFTP_Client.conexionLogin();
				}
			}
		});
		
		
		panelTitulo = new JPanel();
		panelTitulo.setBackground(new Color(112, 128, 144));
		panelTitulo.setBounds(12, 12, 328, 43);
		panelLogin.add(panelTitulo);
		panelTitulo.setLayout(null);
		
		lblAccesoFtp = new JLabel("ACCESO FTP");
		lblAccesoFtp.setForeground(new Color(255, 255, 255));
		lblAccesoFtp.setBackground(new Color(255, 255, 255));
		lblAccesoFtp.setHorizontalAlignment(SwingConstants.CENTER);
		lblAccesoFtp.setBounds(0, 0, 316, 43);
		lblAccesoFtp.setFont(new Font("DejaVu Sans Condensed", Font.BOLD, 16));
		panelTitulo.add(lblAccesoFtp);
		
		campo_pass = new JPasswordField();
		campo_pass.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					cFTP_Client.conexionLogin();
				}
			}
		});
		campo_pass.setBounds(66, 184, 216, 19);
		panelLogin.add(campo_pass);
		
		btnAcceder = new JButton("Acceder");
		btnAcceder.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cFTP_Client.conexionLogin();
			}
		});

		btnAcceder.setBackground(Color.LIGHT_GRAY);
		btnAcceder.setBounds(109, 244, 126, 25);
		panelLogin.add(btnAcceder);
	}
	
	
	/**
	 * Con este método comprobamos que el campo user y contraseña no están vacíos (lo controla el controlador).
	 * Si uno de los 2 está vacío pasará por este método y coloreará de rojo la entrada de texto del correspondiente
	 * y saltará un diálogo.
	 * @param tipo El tipo será user o pass.  Según sea, entrará por uno de los 'case' del switch que le espera el método.
	 */
	public void campoVacio(String tipo) {
		campo_user.setBackground(Color.WHITE);
		campo_pass.setBackground(Color.WHITE);
		
		switch(tipo){
			case "user":
				campo_user.setBackground(new Color(255,204,204));
				campo_user.requestFocus();
				JOptionPane.showMessageDialog(null, "Por favor, rellene el campo Usuario.");
			break;
			
			case "pass":
				campo_pass.setBackground(new Color(255,204,204));
				campo_pass.requestFocus();
				JOptionPane.showMessageDialog(null, "Por favor, rellene el campo Contraseña.");
			break;
		}
	}

	
	/**
	 * Si el logueo es incorrecto tras hacer la llamada al servidor pasándole ambos campos (user y pass), 
	 * volverá a colorear ambos campos de rojo y saltará un mensaje de diálogo.
	 */
	public void loginIncorrecto() {
		JOptionPane.showMessageDialog(null, "¡Nombre de usuario y/o contraseña incorrectos!");
		campo_user.setBackground(new Color(255,204,204));
		campo_user.selectAll();
		campo_user.requestFocus();
		campo_pass.setBackground(new Color(255,204,204));
		campo_pass.selectAll();
	}
	

	//------------ GETTERS AND SETTERS ------------//
	
	public JButton getBtnAcceder() {
		return btnAcceder;
	}

	public JTextField getCampo_user() {
		return campo_user;
	}

	public JPasswordField getCampo_pass() {
		return campo_pass;
	}


}
