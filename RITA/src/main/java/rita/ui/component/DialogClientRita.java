package rita.ui.component;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import rita.network.ClienteRita;
import rita.network.EjecutarComando;
import rita.network.Mensaje;
import rita.settings.HelperEditor;
import rita.settings.Settings;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;

public class DialogClientRita extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 4883632658186677601L;
	private JTextField textFieldIP;
	private JTextField textFieldPuerto;
	
	private String miDireccion;
	private Logger log = Logger.getLogger(DialogClientRita.class);
	private static JLabel lblOn;
	private static JLabel lblOff;
	private static JButton okButton;
	public static ClienteRita clienteRita;
	
	public void dispose(){
		RMenu.setDialogClientOpen(false);
		if(clienteRita != null)
			clienteRita.setVentantaAbierta(false);
		super.dispose();
	}

	public DialogClientRita(java.awt.Frame parent, String titulo, boolean modal) {
		super(parent);
		setResizable(false);
		initialize();
	}
	
	private void initialize() {
		
		try {
			PropertyConfigurator.configure("log4j.properties");
			this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			this.setVisible(true);
			setModal(false);
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
		setBounds(100, 100, 247, 190);
		setLocation(700, 100);
		getContentPane().setLayout(new BorderLayout());
		{
			JPanel panel = new JPanel();
			getContentPane().add(panel, BorderLayout.CENTER);
			panel.setLayout(null);
			{
				JLabel lblIP = new JLabel("IP:");
				lblIP.setBounds(12, 31, 18, 15);
				panel.add(lblIP);
			}
			{
				textFieldIP = new JTextField();
				textFieldIP.setText("127.0.0.1");
				textFieldIP.setBounds(66, 27, 114, 24);
				panel.add(textFieldIP);
				textFieldIP.setColumns(15);
			}
			{
				JLabel lblPuerto = new JLabel("Puerto:");
				lblPuerto.setBounds(12, 56, 70, 15);
				panel.add(lblPuerto);
			}
			
			textFieldPuerto = new JTextField();
			textFieldPuerto.setText("1234");
			textFieldPuerto.setBounds(66, 52, 114, 24);
			panel.add(textFieldPuerto);
			textFieldPuerto.setColumns(10);
			
			JLabel lblRobot = new JLabel("Robot:");
			lblRobot.setBounds(12, 83, 70, 15);
			panel.add(lblRobot);
			
			JLabel lblRobotName = new JLabel("");
			lblRobotName.setBounds(64, 83, 114, 15);
			lblRobotName.setText(HelperEditor.currentRobotName);			
			panel.add(lblRobotName);
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				okButton = new JButton("Conectar");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent arg0) {
					
						log.info("Quieres conectarte a " + textFieldIP.getText() + " en el puerto " + textFieldPuerto.getText()
								+ " con el nombre de usuario: " + HelperEditor.currentRobotName + ".");
							try {
								if(clienteRita == null)
									clienteRita = new ClienteRita(textFieldIP.getText(), Integer.parseInt(textFieldPuerto.getText()), HelperEditor.currentRobotName);
								
								try {
									lblOn.setVisible(true);
									lblOff.setVisible(false);
									okButton.setEnabled(false);
									clienteRita.start();
								} catch (IllegalThreadStateException e) {
									JOptionPane.showMessageDialog(DialogClientRita.this, "Ya envio su robot","Error de conexion",
											JOptionPane.ERROR_MESSAGE);
									e.printStackTrace();
								}
							
							} catch (ConnectException e) {
	
								//e.printStackTrace();
								JOptionPane.showMessageDialog(DialogClientRita.this, "No se puede realizar la conexion con el servidor, verifique la IP y que este iniciado","Error de conexion",
									    JOptionPane.ERROR_MESSAGE);
							} catch (NumberFormatException e) {
								e.printStackTrace();
							} catch (UnknownHostException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
							
							
					}
				});
				{
					lblOn = new JLabel(new ImageIcon(DialogServerRita.class.getResource("/images/icons/serverOn.png")));
					lblOn.setVisible(false);
					buttonPane.add(lblOn);
				}
				{
					lblOff = new JLabel(new ImageIcon(DialogServerRita.class.getResource("/images/icons/serverOff.png")));
					lblOff.setVisible(true);
					buttonPane.add(lblOff);
				}
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				//JButton cancelButton = new JButton("Cerrar");
				JButton cancelButton = new JButton(new AbstractAction("Cerrar") {

		            @Override
		            public void actionPerformed(ActionEvent e) {
		            	dispose();
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
	
	public static void resetearBotones(){
		lblOn.setVisible(false);
		lblOff.setVisible(true);
		okButton.setEnabled(true);
	}


	public String getMiDireccion() {
		return miDireccion;
	}
	public void setMiDireccion(String miDireccion) {
		this.miDireccion = miDireccion;
	}
}