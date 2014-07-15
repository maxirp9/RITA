package rita.ui.component;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import rita.network.ClienteRita;
import rita.network.ConexionServidor;
import rita.network.EjecutarComando;
import rita.network.Mensaje;
import rita.settings.HelperEditor;
import rita.settings.Settings;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.Socket;

public class DialogClientRita extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4883632658186677601L;
	private JTextField textFieldIP;
	private JTextField textFieldPuerto;
	
	private ConexionServidor conexionServidor;
	private Socket socket;
	private String miDireccion;
	private Logger log = Logger.getLogger(DialogClientRita.class);
	private ClienteRita clienteRita;
	
	public void dispose(){
		RMenu.setDialogClientOpen(false);
		super.dispose();
	}

	public DialogClientRita(java.awt.Frame parent, String titulo, boolean modal) {
		super(parent);
		initialize();
	}
	
	private void initialize() {
		
		try {
			PropertyConfigurator.configure("log4j.properties");
			this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			this.setVisible(true);
			setModal(true);
			crearDialog();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	/**
	 * Launch the application.
	 */
	/*public static void main(String[] args) {
		try {
			PropertyConfigurator.configure("log4j.properties");
			DialogClientRita dialog = new DialogClientRita();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
*/
	/**
	 * Create the dialog.
	 */
	public void crearDialog() {
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		setTitle("Conexion con servidor");
		setBounds(100, 100, 378, 197);
		setLocation(700, 100);
		getContentPane().setLayout(new BorderLayout());
		{
			JPanel panel = new JPanel();
			getContentPane().add(panel, BorderLayout.CENTER);
			panel.setLayout(null);
			{
				JLabel lblIP = new JLabel("IP:");
				lblIP.setBounds(41, 21, 18, 15);
				panel.add(lblIP);
			}
			{
				textFieldIP = new JTextField();
				textFieldIP.setText("127.0.0.1");
				textFieldIP.setBounds(112, 21, 114, 19);
				panel.add(textFieldIP);
				textFieldIP.setColumns(15);
			}
			{
				JLabel lblPuerto = new JLabel("Puerto:");
				lblPuerto.setBounds(41, 50, 70, 15);
				panel.add(lblPuerto);
			}
			
			textFieldPuerto = new JTextField();
			textFieldPuerto.setText("1234");
			textFieldPuerto.setBounds(112, 50, 114, 19);
			panel.add(textFieldPuerto);
			textFieldPuerto.setColumns(10);
			
			JLabel lblRobot = new JLabel("Robot:");
			lblRobot.setBounds(41, 77, 70, 15);
			panel.add(lblRobot);
			
			JLabel lblRobotName = new JLabel("");
			lblRobotName.setBounds(112, 77, 114, 15);
			lblRobotName.setText(HelperEditor.currentRobotName);			
			panel.add(lblRobotName);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Conectar");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
					
						log.info("Quieres conectarte a " + textFieldIP.getText() + " en el puerto " + textFieldPuerto.getText()
								+ " con el nombre de usuario: " + HelperEditor.currentRobotName + ".");
						try {
							clienteRita = new ClienteRita(textFieldIP.getText(), Integer.parseInt(textFieldPuerto.getText()), HelperEditor.currentRobotName);
						} catch (Exception e) {

							e.printStackTrace();
							JOptionPane.showMessageDialog(null, "No se puede realizar la conexion con el servidor, verifique la IP y que este iniciado","Error de conexion",
								    JOptionPane.ERROR_MESSAGE);
						}
						clienteRita.start();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				//JButton cancelButton = new JButton("Cerrar");
				JButton cancelButton = new JButton(new AbstractAction("Cerrar") {

		            @Override
		            public void actionPerformed(ActionEvent e) {
		            	closeClient();
		                DialogClientRita.this.setVisible(false);
		                DialogClientRita.this.dispatchEvent(new WindowEvent(
		                		DialogClientRita.this, WindowEvent.WINDOW_CLOSING));
		            }
		        });
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
	
	/**
	 * Recibe los mensajes del chat reenviados por el servidor
	 */
	public void recibirMensajesServidor() {
		// Obtiene el flujo de entrada del socket
		if (this.hayPedidoRobot()){
			conexionServidor.iniciarConexionSalida();
			conexionServidor.enviarArchivo(HelperEditor.currentRobotName);
		}
		else
			log.error("Falla del pedido de robot del Cliente: "
					+ socket.getLocalAddress().getHostAddress());

		try {
			Mensaje mensajeRecibido;
			mensajeRecibido = conexionServidor.recibirMensaje();
			
			if (mensajeRecibido.accion.equals("BinGenerado")) {
				log.info("Ya esta el BinGenerado para el cliente: "
						+ getMiDireccion());

				Mensaje mensajeAEnviar = new Mensaje("DameBinFile", getMiDireccion());
				conexionServidor.enviarMensaje(mensajeAEnviar);
				log.info("Pide el binario el cliente: "
						+ getMiDireccion());
				conexionServidor.recibirArchivo("batalla.copia.bin");
			} else
				log.error("El Cliente "
						+ getMiDireccion()
						+ " espera BinGenerado y recibe mensaje incorrecto:"
						+ mensajeRecibido.accion);

			ejecutarRobocode();
		} catch (NullPointerException ex) {
			log.error("El socket no se creo correctamente. ");
		}
	}

	private boolean hayPedidoRobot() {

		boolean pedidoRobot = true;
		Mensaje mensajeRecibido;
		// Pone el mensaje recibido en mensajes para que se notifique
		// a sus observadores que hay un nuevo mensaje.
		mensajeRecibido = conexionServidor.recibirMensaje();
		if (mensajeRecibido.accion.equals("ListoHilo")) {
			log.info("Cliente: "
					+ getMiDireccion()
					+ " acepta hilo");
		} else {
			log.error("El Cliente "
					+ getMiDireccion()
					+ " espera ListoHilo y recibe mensaje incorrecto:"
					+ mensajeRecibido.accion);
			pedidoRobot = false;
		}
		return pedidoRobot;
	}

	public void ejecutarRobocode() {		
		
		String cmd = "java -Xmx512M -Dsun.io.useCanonCaches=false -cp " + Settings.getBinaryPath() + File.separator + "robocode.jar robocode.Robocode -replay " + Settings.getBinaryPath() + File.separator + "batalla.copia.bin" + " -tps 25";
		
		log.error(cmd);
		log.info("Se ejecuta Robocode en el cliente "
				+ socket.getLocalAddress().getHostAddress());
		EjecutarComando comando = new EjecutarComando(cmd);		
		
	}

	private void closeClient() {
		try {
			if (socket != null && socket.isConnected()){
				log.info("Cierro el cliente "
					+ socket.getLocalAddress().getHostAddress());
				socket.close();
			}
			this.dispose();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String getMiDireccion() {
		return miDireccion;
	}
	public void setMiDireccion(String miDireccion) {
		this.miDireccion = miDireccion;
	}
}
