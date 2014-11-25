package rita.ui.component;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import rita.network.CantidadConexionesObservable;
import rita.network.ClientesConectadosObservable;
import rita.network.LogRitaObservable;
import rita.network.LogServer;
import rita.network.ServerRita;
import workspace.Workspace;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JList;
import javax.swing.AbstractListModel;
import javax.swing.border.BevelBorder;

import org.eclipse.wb.swing.FocusTraversalOnArray;

import java.awt.Component;

import javax.swing.SwingConstants;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

public class DialogServerRita extends JDialog implements Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6132731169207934920L;
	private final JPanel contentPanel = new JPanel();
	private JTextField textPort;
	private JComboBox rondas;
	private ClientesConectadosObservable clientesConectadosObservable;
	private ServerRita server;
	private JButton okButton;
	private JButton btnPararServidor;
	private JLabel lblServerOn;
	private JLabel lblServerOff;
	private Workspace ws;
//	private DialogLogRita logRita;
	private LogServer logServer;
	private LogRitaObservable logRitaObservable;
	private JTextArea textAreaLog;
	
	public void dispose(){
//		logRita.getLogServer().setTexto("");
//		ws.removeWidget(logRita);
		RMenu.setDialogServerOpen(false);
		if(server != null)
			server.stopServer();
		super.dispose();
	}

	public DialogServerRita(java.awt.Frame parent, String title, boolean modal) {
		super(parent);
		setResizable(false);
		this.ws = Workspace.getInstance();
		
		/** Agrego el log PABLO */
//		logRita = DialogLogRita.getInstance();
//		logRita.setVisible(true);
//				
//		this.ws.addWorkspaceListener(logRita);
//		this.ws.addWidget(logRita, true, true);
		
		initialize(title);
	}

	private void initialize(String title) {
		try {
			this.setTitle(title);
			clientesConectadosObservable = new ClientesConectadosObservable();
			clientesConectadosObservable.addObserver(this);
			this.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			logRitaObservable = new LogRitaObservable();
			logRitaObservable.addObserver(this);
//			setLogServer(new LogServer());
//			getLogServer().setLogRitaObservable(logRitaObservable);
			this.setVisible(true);
			correr();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Launch the application.
	 */
	/*
	 * public static void main(String[] args) { try { DialogServerRita dialog =
	 * new DialogServerRita();
	 * dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
	 * dialog.setVisible(true); } catch (Exception e) { e.printStackTrace(); } }
	 */

	/**
	 * Create the dialog.
	 * 
	 * @return
	 */
	public void correr() {

		setBounds(250, 200, 515, 415);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(null);
		{
			JLabel lblPort = new JLabel("Puerto:");
			lblPort.setBounds(12, 39, 53, 15);
			contentPanel.add(lblPort);
		}
		{
			textPort = new JTextField();
			textPort.setBounds(71, 37, 53, 19);
			textPort.setText("1234");
			textPort.setColumns(10);
			contentPanel.add(textPort);
		}

		JLabel lblIpServidor = new JLabel("IP Servidor:");
		lblIpServidor.setBounds(12, 12, 91, 15);
		contentPanel.add(lblIpServidor);

		final JLabel labelIPServidorValor = new JLabel(traerIp());
		labelIPServidorValor.setBounds(103, 12, 125, 15);
		contentPanel.add(labelIPServidorValor);
		{
			JLabel lblClientesConectados = new JLabel("Clientes conectados:");
			lblClientesConectados.setBounds(12, 93, 164, 15);
			contentPanel.add(lblClientesConectados);
		}
		{
			JLabel lblClientesConectadosValor = new JLabel("0");
			lblClientesConectadosValor.setBounds(165, 93, 34, 15);
			contentPanel.add(lblClientesConectadosValor);
		}

		JButton btnEjecutarBatalla = new JButton("Ejecutar Batalla");
		btnEjecutarBatalla.setEnabled(false);
		btnEjecutarBatalla.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				server.setIniciarBatalla(true);
				try {
					Socket socket = new Socket(traerIp(), server
							.getPortNumber());
				} catch (UnknownHostException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
		btnEjecutarBatalla.setBounds(22, 312, 146, 25);
		contentPanel.add(btnEjecutarBatalla);
		{
			JList listCientesConectados = new JList();
			listCientesConectados.setBorder(new BevelBorder(BevelBorder.LOWERED, null, null, null, null));
			listCientesConectados.setModel(new AbstractListModel() {
				String[] values = new String[] {"No hay conectados..."};
				public int getSize() {
					return values.length;
				}
				public Object getElementAt(int index) {
					return values[index];
				}
			});
			listCientesConectados.setToolTipText("Clientes conectados");
			listCientesConectados.setBounds(12, 136, 164, 169);
			contentPanel.add(listCientesConectados);
		}
		
		JLabel lblUsuariosConectados = new JLabel("Listado de conectados:");
		lblUsuariosConectados.setBounds(12, 119, 173, 18);
		contentPanel.add(lblUsuariosConectados);
		
		JLabel lblRondas = new JLabel("Rondas:");
		lblRondas.setBounds(12, 66, 70, 15);
		contentPanel.add(lblRondas);
		
		JComboBox comboBoxRondas = new JComboBox();
		comboBoxRondas.setModel(new DefaultComboBoxModel(new String[] {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}));
		comboBoxRondas.setSelectedIndex(2);
		comboBoxRondas.setBounds(81, 61, 53, 24);
		contentPanel.add(comboBoxRondas);
		rondas = comboBoxRondas;
		
		JLabel lblTextAreaLog = new JLabel("Registro de Eventos:");
		lblTextAreaLog.setBounds(262, 12, 193, 15);
		contentPanel.add(lblTextAreaLog);
		
		
		textAreaLog = new JTextArea();
		textAreaLog.setEditable(false);
		textAreaLog.setBounds(200, 37, 200, 260);
		contentPanel.add(textAreaLog);

		JScrollPane scrollPaneLog = new JScrollPane(textAreaLog, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		scrollPaneLog.setBounds(200, 37, 296, 268);
		contentPanel.add(scrollPaneLog);
		
		JButton btnLimpiarRegistro = new JButton("Limpiar Registro");
		btnLimpiarRegistro.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				textAreaLog.setText("");
			}
		});
		btnLimpiarRegistro.setBounds(279, 312, 164, 25);
		contentPanel.add(btnLimpiarRegistro);
		
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				{
					okButton = new JButton("Iniciar");
					okButton.setHorizontalAlignment(SwingConstants.LEFT);
					okButton.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							server = ServerRita.getInstance(Integer.valueOf(textPort.getText()),						
									clientesConectadosObservable, logRitaObservable);
							server.setRondas(rondas.getSelectedItem().toString());
							server.start();
							//SETEO LOS BOTONES E IMAGEN DE ESTADO DEL SERVIDOR
							lblServerOn.setVisible(true);
							lblServerOff.setVisible(false);
							okButton.setEnabled(false);
							btnPararServidor.setEnabled(true);
						}
					});
					
					lblServerOn = new JLabel(new ImageIcon(DialogServerRita.class.getResource("/images/icons/serverOn.png")));
					lblServerOn.setVisible(false);
					buttonPane.add(lblServerOn);
					
					lblServerOff = new JLabel(new ImageIcon(DialogServerRita.class.getResource("/images/icons/serverOff.png")));
					lblServerOff.setHorizontalAlignment(SwingConstants.LEFT);
					lblServerOff.setVisible(true);
					buttonPane.add(lblServerOff);
					buttonPane.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{okButton, btnPararServidor}));
					okButton.setActionCommand("Correr servidor");
					buttonPane.add(okButton);
					getRootPane().setDefaultButton(okButton);
					
					btnPararServidor = new JButton("Parar");
					btnPararServidor.setEnabled(false);
					btnPararServidor.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent arg0) {
							//SETEO LOS BOTONES E IMAGEN DE ESTADO DEL SERVIDOR
							lblServerOn.setVisible(false);
							lblServerOff.setVisible(true);
							okButton.setEnabled(true);
							btnPararServidor.setEnabled(false);
							server.stopServer();
							//SE PONE EN NULL NUEVAMENTE PARA ARRANCAR DE NUEVO
							server = null;
							System.out.println("STOP Servidor");
						}
					});
					buttonPane.add(btnPararServidor);
					
					
				}
			}
		}
	}

	@Override
	public void update(Observable obs, Object data) {
		System.out.println(Thread.currentThread().getId());

		if (obs instanceof CantidadConexionesObservable) {
			JLabel lbl = (JLabel) contentPanel.getComponent(5);
			lbl.setText(data.toString());
			lbl.validate();
			lbl.repaint();
		}
		if (obs instanceof ClientesConectadosObservable) {
			//ACTUALIZO EL LISTADO DE CLIENTES
			JList lst = (JList) contentPanel.getComponent(7);
			ArrayList<String> arrayList = (ArrayList<String>) data;
			//Crear un objeto DefaultListModel
			DefaultListModel listModel = new DefaultListModel();
			//Recorrer el contenido del ArrayList
			for(int i=0; i<arrayList.size(); i++) {
			    //Añadir cada elemento del ArrayList en el modelo de la lista
			    listModel.add(i, arrayList.get(i));
			}
			
			if(arrayList.size() == 0)
				listModel.add(0, "No hay conectados...");
			
			//Asociar el modelo de lista al JList
			lst.setModel(listModel);
			
					
			lst.validate();
			lst.repaint();
			
			//ACTUALIZO EL CONTADOR DE USUARIOS
			JLabel lbl = (JLabel) contentPanel.getComponent(5);
			lbl.setText(String.valueOf(arrayList.size()));
			lbl.validate();
			lbl.repaint();
			
			//HABILITO EL BOTON DE EJECUTAR BATALLA
			JButton btnEjec = (JButton) contentPanel.getComponent(6);
			btnEjec.setEnabled((arrayList.size()>0));				
			
		}
		if (obs instanceof LogRitaObservable) {
			
			String textoEscrito = textAreaLog.getText() + "\n " + data.toString();
			// Si el String dato pasado por parametro es texto vacio significa que hay que limpiar el texto
			textAreaLog.setText(textoEscrito);
			textAreaLog.validate();
			textAreaLog.repaint();
		}
		

	}

	public String traerIp() {
		Enumeration<NetworkInterface> interfaces = null;
		String ip = "";
		try {
			interfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			e.printStackTrace();
		}
		while (interfaces.hasMoreElements()) {
			NetworkInterface current = interfaces.nextElement();
			// System.out.println("1er " + current);
			if (current.getName().startsWith("eth")
					|| current.getName().startsWith("wlan")) {
				try {
					if (!current.isUp() || current.isLoopback()
							|| current.isVirtual())
						continue;
				} catch (SocketException e) {
					e.printStackTrace();
				}
				Enumeration<InetAddress> addresses = current.getInetAddresses();
				while (addresses.hasMoreElements()) {
					InetAddress current_addr = addresses.nextElement();
					if (current_addr instanceof Inet4Address)
						ip = current_addr.getHostAddress().toString();
					if (current_addr.isLoopbackAddress())
						continue;

				}
			}
		}
		return ip;
	}

	public LogServer getLogServer() {
		return logServer;
	}

	public void setLogServer(LogServer logServer) {
		this.logServer = logServer;
	}
}
