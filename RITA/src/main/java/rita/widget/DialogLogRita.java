package rita.widget;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Collection;
import java.util.Observable;
import java.util.Observer;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextPane;
import javax.swing.Timer;
import javax.swing.border.BevelBorder;

import renderable.RenderableBlock;
import rita.network.LogRitaObservable;
import rita.network.LogServer;
import rita.network.ServerRita;
import rita.settings.Language;
import rita.ui.component.DialogNewRobot;
import rita.widget.ScreenHelper.ScreenSize;
import workspace.Workspace;
import workspace.WorkspaceEvent;
import workspace.WorkspaceListener;
import workspace.WorkspaceWidget;

public class DialogLogRita extends JPanel implements MouseListener, ComponentListener,WorkspaceWidget, WorkspaceListener, Observer {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5420797689487948012L;

	private final SourceCodeEnlargerTimer enlarger;
	
	private static int MIN_WIDTH = 95;
	/** the default height of a source code view button */
	private static int MIN_HEIGHT = 40;
	/** this.width */
	private static int CURRENT_WIDTH = MIN_WIDTH;
	/** this.height */
	private static int CURRENT_HEIGHT = MIN_HEIGHT;
	/** button height*/
	private static int BUTTON_HEIGHT = MIN_HEIGHT;
	/** button width */
	private static int BUTTON_WIDHT = 95;
	
	private static int MAX_WIDTH;
	private static int MAX_HEIGHT;
	
	private static int X_LOCATION = 1075;
	private static int Y_LOCATION = 600;
	
	private ServerRita serverRita;
	private LogServer logServer;
	
	static {
		MAX_WIDTH = 800;
		MAX_HEIGHT = 200 + BUTTON_HEIGHT;
		/* ajustar ancho y alto con valores de acuerdo a si el tamano de la
		*  pantalla es comun o si es la de una netbook */
		if (ScreenHelper.getSize().equals(ScreenSize.SMALL)) {
			MAX_WIDTH -= MAX_WIDTH/5;
			MAX_HEIGHT -= MAX_HEIGHT/5;
		}
	}
	/* distancia del lado derecho del widget al lado derecho de la ventana */
	private static final int OFFSET_FROM_RIGHT = 30 + SourceCode.getInstance().getWidth(); // 30 + BUTTON_HEIGHT;
	/* distancia del lado inferior del widget al lado inferior de la ventana */
	private static final int OFFSET_FROM_BOTTOM = 50;

	private boolean minimized = true;
	private boolean expanded = false;
	private JButton codeButton;
	private JButton cleanTextButton;
	private Font smallButtonFont;
	private JTextPane paneJavaCode;
	private LogRitaObservable logRitaObservable;
	
	private static final class SINGLETON {
		private static final DialogLogRita INSTANCE = new DialogLogRita();
	}
	
	public static DialogLogRita getInstance() {
		return SINGLETON.INSTANCE;
	}
	
	private DialogLogRita() {
		
		this.setBorder(BorderFactory.createBevelBorder(BevelBorder.RAISED));
		this.setLocation(X_LOCATION,Y_LOCATION);
		this.setSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));
		this.setPreferredSize(new Dimension(MIN_WIDTH, MIN_HEIGHT));

		this.setLayout(null);
		smallButtonFont = this.getFont().deriveFont(Font.PLAIN, 9.0f);

		this.enlarger = new SourceCodeEnlargerTimer();
		createHideCodeButton();
		prepareCodeRegion();
		createCleanTextButton();
		add(codeButton);
		add(cleanTextButton);
		add(paneJavaCode);
		logRitaObservable = new LogRitaObservable();
		logRitaObservable.addObserver(this);
		
		setLogServer(new LogServer());
		getLogServer().setLogRitaObservable(logRitaObservable);
		Workspace.getInstance().addComponentListener(this);
	}
	
	private void createHideCodeButton() {

		this.codeButton = new JButton(createImageIcon("/images/sourcecode/log_icon.png"));
		codeButton.addActionListener(new SourceCodeEnlargerTimer());
		codeButton.setActionCommand("CodeButton");
		codeButton.addMouseListener(this);
		codeButton.setBounds(0, 0, BUTTON_WIDHT, BUTTON_HEIGHT);
		codeButton.setFont(smallButtonFont);
		codeButton.setAlignmentX(LEFT_ALIGNMENT);
		codeButton.setText("Ver log");
		codeButton.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, Color.BLACK));
	}
	
	/** Returns an ImageIcon, or null if the path was invalid. */
	protected static ImageIcon createImageIcon(String path) {
		java.net.URL imgURL = DialogNewRobot.class.getResource(path);
		if (imgURL != null) {
			return new ImageIcon(imgURL);
		} else {
			System.err.println(Language.get("robot.error.fileNotFound") + " " + path);
			return null;
		}
	}
	
	private void createCleanTextButton(){
		this.cleanTextButton = new JButton(createImageIcon("/images/sourcecode/clear_icon.png"));
		cleanTextButton.setBounds(BUTTON_WIDHT, 0, BUTTON_WIDHT, BUTTON_HEIGHT);
		cleanTextButton.setFont(smallButtonFont);
		cleanTextButton.setAlignmentX(LEFT_ALIGNMENT);
		cleanTextButton.setText("Limpiar log");
		cleanTextButton.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0, Color.BLACK));
		cleanTextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				getLogServer().setTexto("");
			}
		});
	}
	
	private void prepareCodeRegion() {
		//paneJavaCode = new ReadOnlySourceCodePane();
		paneJavaCode = new JTextPane();
		paneJavaCode.setFont(paneJavaCode.getFont().deriveFont(12.0f));
		
		// definir la colorizacion de la sintaxis de Java, coincidiendo con los colores de los bloques de RITA
        paneJavaCode.setText("Log del server...");
		paneJavaCode.setBackground(Color.WHITE);
		paneJavaCode.setEditable(false);
        
        //paneJavaCode.getWrappingContainerWithLines().setBounds(0, BUTTON_HEIGHT, MAX_WIDTH, MAX_HEIGHT-BUTTON_HEIGHT);
		paneJavaCode.setBounds(0, BUTTON_HEIGHT, MAX_WIDTH, MAX_HEIGHT-BUTTON_HEIGHT);
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		if (minimized) {			
			codeButton.setText("Ocultar log");
			expanded = true;
			enlarger.expand();
		} else {
			codeButton.setText("Ver log");
			expanded = false;
			enlarger.shrink();
		}
		minimized = !minimized;
	}
	
	/**
	 * This animator is responsible for enlarging or shrinking the size of the
	 * MiniMap when expand() or shrink() is called, respectively.
	 */
	private class SourceCodeEnlargerTimer implements ActionListener {
		/** Growth count */
		private int count;
		/** Internal Timer used to animate the opening/closing of source view */
		private javax.swing.Timer timer;
		
		private final int NUM_STEPS = 20;
		/** absolute value of width growth */
		private final int WIDTH_GROWTH_PER_STEP = (MAX_WIDTH-MIN_WIDTH) / NUM_STEPS;
		/** absolute value of height Growth */
		private final int HEIGHT_GROWTH_PER_STEP = (MAX_HEIGHT-MIN_HEIGHT) / NUM_STEPS;
		/**
		 * Indicates whether the source code view is/was expanding (true) or shrinking
		 * (false)
		 */
		private boolean expand;

		/**
		 * Constuctors an animator that can enlarge or skrink the source code view
		 */
		public SourceCodeEnlargerTimer() {
			count = 0;
			this.expand = true;
			timer = new Timer(5, this);
		}

		/**
		 * expands/shrinks the source code view untill count is 0 or 15. At 0, the map is
		 * smallest as possible and at 15, the map is largest as possible
		 */
		public void actionPerformed(ActionEvent e) {
			
			if (count <= 0) {
				timer.stop();
			} else if(count >= NUM_STEPS) {
				timer.stop();
				maximizeSourceCode();
			} else {
				if (expand) {
					count = count + 1;
				} else {
					count = count - 1;
				}
				CURRENT_WIDTH = MIN_WIDTH + count * WIDTH_GROWTH_PER_STEP;

				// para pantalla normal
				// if (ScreenHelper.getSize().equals(ScreenSize.NORMAL))
				CURRENT_HEIGHT = MIN_HEIGHT + count * HEIGHT_GROWTH_PER_STEP;
				// para pantalla peque�a se ajusta distinto
				// if (ScreenHelper.getSize().equals(ScreenSize.SMALL))
				// MAPHEIGHT = DEFAULT_HEIGHT + (count - 5) * dy * 2
				// - (MAPHEIGHT / 5);
				repositionSourceCode(count);
				repaint();
			}			
		}

		/**
		 * enlarge this source code view
		 */
		public void expand() {
			this.expand = true;
			count++;
			this.timer.start();
		}

		/**
		 * shrinks this minimap
		 */
		public void shrink() {
			count--;
			this.expand = false;
			this.timer.start();
		}
	}
	
	private void maximizeSourceCode() {		
		if (this.getParent() != null) {
			this.setBounds(this.getParent().getWidth() - MAX_WIDTH - OFFSET_FROM_RIGHT,
						this.getParent().getHeight() - MAX_HEIGHT - OFFSET_FROM_BOTTOM,
						MAX_WIDTH, MAX_HEIGHT);
		}
	}
	
	public void repositionSourceCode(int count) {
		if (this.getParent() != null) {
			this.setBounds(this.getParent().getWidth() - CURRENT_WIDTH - OFFSET_FROM_RIGHT,
						this.getParent().getHeight() - CURRENT_HEIGHT - OFFSET_FROM_BOTTOM,
						CURRENT_WIDTH, CURRENT_HEIGHT);
		}
	}
	
	public boolean isExpanded() {
		return expanded;
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentResized(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void workspaceEventOccurred(WorkspaceEvent event) {
		// TODO Auto-generated method stub
	}

	@Override
	public void blockDropped(RenderableBlock block) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void blockDragged(RenderableBlock block) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void blockEntered(RenderableBlock block) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void blockExited(RenderableBlock block) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeBlock(RenderableBlock block) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addBlock(RenderableBlock block) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addBlocks(Collection<RenderableBlock> blocks) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public JComponent getJComponent() {		
		return this;
	}

	@Override
	public Iterable<RenderableBlock> getBlocks() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void update(Observable obs, Object data) {
		// TODO Auto-generated method stub
		if (data instanceof String){
			String dato = (String) data;
			
			String textoEscrito = paneJavaCode.getText() + "\n " + dato;
			// Si el String dato pasado por parametro es texto vacio significa que hay que limpiar el texto
			if (dato.equals("")){
				textoEscrito = "";
			}
			paneJavaCode.setText(textoEscrito);
			
		}
	}
	
	public ServerRita getServerRita() {
		return serverRita;
	}

	public void setServerRita(ServerRita serverRita) {
		this.serverRita = serverRita;
	}

	public LogServer getLogServer() {
		return logServer;
	}

	public void setLogServer(LogServer logServer) {
		this.logServer = logServer;
	}


}
