package ROUTER;

//Dlg
import java.awt.Color;
import java.awt.Container;
import java.awt.FileDialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

public class Dlg  extends JFrame implements BaseLayer    {

	public static RoutingTable routingTable;
	public int nUpperLayerCount = 0;
	public int nUnderLayerCount = 0;
	public String pLayerName = null;
	public static ARPTable arpTable;
	public ArrayList<BaseLayer> p_aUnderLayerGUI = new ArrayList<BaseLayer>();
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	BaseLayer UnderLayer;

	private static LayerManager m_LayerMgr = new LayerManager();
	public static boolean exist = false;

	Container contentPane;

	JTextArea TotalArea;
	static JTextArea RoutingArea;

	JButton btnARPDelete;
	JButton btnRoutingDelete;
	JButton btnRoutingAdd;
	static JButton Setting_Button;

	static JComboBox<String> NICComboBox;
	JComboBox strCombo1;
	JComboBox strCombo2;

	int index1;
	int index2;


	private final JPopupMenu popupMenu = new JPopupMenu();

	public static void main(String[] args) throws IOException {

		m_LayerMgr.AddLayer(new NILayer("NI"));
		m_LayerMgr.AddLayer(new EthernetLayer("Ethernet"));
		arpTable = null;
		m_LayerMgr.AddLayer(new ARPLayer("ARP2",arpTable));
		m_LayerMgr.AddLayer(new ARPLayer("ARP",arpTable));

		m_LayerMgr.AddLayer(new IPLayer("IP"));

		m_LayerMgr.AddLayer(new NILayer("NI2"));
		m_LayerMgr.AddLayer(new EthernetLayer("Ethernet2"));

		m_LayerMgr.AddLayer(new IPLayer("IP2"));
		m_LayerMgr.AddLayer(new Dlg("GUI"));

		arpTable = new ARPTable((ARPLayer) m_LayerMgr.GetLayer("ARP"), (ARPLayer) m_LayerMgr.GetLayer("ARP2"),(Dlg) m_LayerMgr.GetLayer("GUI") );
		((ARPLayer)m_LayerMgr.GetLayer("ARP")).SetARPTable(arpTable,(Dlg) m_LayerMgr.GetLayer("GUI"));
		((ARPLayer)m_LayerMgr.GetLayer("ARP2")).SetARPTable(arpTable,(Dlg) m_LayerMgr.GetLayer("GUI"));

		m_LayerMgr.ConnectLayers(" NI ( +Ethernet ( +ARP ( +IP ( +GUI ) ) +IP ( +GUI ) ) ) ^GUI ( -IP ( -ARP ( -Ethernet ( -NI ) ) -Ethernet ( -NI ) ) )  ^NI2 ( +Ethernet2 ( +ARP2 ( +IP2 ( +GUI ) ) +IP2 ( +GUI ) ) ) ^GUI ( -IP2 ( -ARP2 ( -Ethernet2 ( -NI2 ) ) -Ethernet2 ( -NI2 ) ) )");

		routingTable = new RoutingTable();

		((IPLayer) m_LayerMgr.GetLayer("IP")).friendIPset(((IPLayer) m_LayerMgr.GetLayer("IP2")));
		((IPLayer) m_LayerMgr.GetLayer("IP2")).friendIPset(((IPLayer) m_LayerMgr.GetLayer("IP")));
		((IPLayer) m_LayerMgr.GetLayer("IP")).setRouter(routingTable);
		((IPLayer) m_LayerMgr.GetLayer("IP2")).setRouter(routingTable);
	}

	public Dlg(String pName) throws IOException  {

		pLayerName = pName;

		setTitle("Static Router");

		setBounds(250, 250, 980, 520);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		contentPane = this.getContentPane();
		getContentPane().setLayout(null);

		JPanel settingPanel = new JPanel();
		settingPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "setting",
				TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		settingPanel.setBounds(14, 415, 930, 50);
		contentPane.add(settingPanel);
		settingPanel.setLayout(null);

		String[] adapterna= new String[((NILayer) m_LayerMgr.GetLayer("NI")).m_pAdapterList.size()];

		for(int i=0;i<((NILayer) m_LayerMgr.GetLayer("NI")).m_pAdapterList.size();i++)
			adapterna[i] = ((NILayer) m_LayerMgr.GetLayer("NI")).m_pAdapterList.get(i).getDescription();

		JLabel select1 = new JLabel("NIC select 1: ");
		select1.setBounds(80, 20, 170, 20);
		settingPanel.add(select1);

		strCombo1= new JComboBox(adapterna);
		strCombo1.setBounds(160, 20, 220, 20);
		strCombo1.setVisible(true);
		settingPanel.add(strCombo1);

		JLabel select2 = new JLabel("NIC select 2: ");
		select2.setBounds(390, 20, 170, 20);
		settingPanel.add(select2);

		strCombo2= new JComboBox(adapterna);
		strCombo2.setBounds(470, 20, 220, 20);
		strCombo2.setVisible(true);
		settingPanel.add(strCombo2);

		Setting_Button = new JButton("setting");// setting
		Setting_Button.setBounds(720, 20, 80, 20);
		Setting_Button.addActionListener(new setAddressListener());
		settingPanel.add(Setting_Button);// setting

		JPanel Routing_Table = new JPanel();
		Routing_Table.setBounds(14, 12, 458, 402);
		Routing_Table.setBorder(new TitledBorder(null, "Static Routing Table", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		Routing_Table.setLayout(null);
		contentPane.add(Routing_Table);

		//table Label
		JTable Routing_Jtable;

		DefaultTableModel Routing_model = new DefaultTableModel(); 
		Routing_model.addColumn("Destination");
		Routing_model.addColumn("NetMask");
		Routing_model.addColumn("Gateway");
		Routing_model.addColumn("Flag");
		Routing_model.addColumn("Interface");
		Routing_model.addColumn("Metric");

		Routing_Jtable = new JTable(Routing_model); 

		Routing_Jtable.getColumnModel().getColumn(0).setPreferredWidth(80);
		Routing_Jtable.getColumnModel().getColumn(1).setPreferredWidth(80);
		Routing_Jtable.getColumnModel().getColumn(2).setPreferredWidth(80);
		Routing_Jtable.getColumnModel().getColumn(3).setPreferredWidth(20);
		Routing_Jtable.getColumnModel().getColumn(4).setPreferredWidth(40);
		Routing_Jtable.getColumnModel().getColumn(5).setPreferredWidth(20);

		JScrollPane Routing_jScrollPane=new JScrollPane(Routing_Jtable); 

		Routing_jScrollPane.setBounds(14, 30, 430, 20);

		Routing_Table.add(Routing_jScrollPane); 

		RoutingArea = new JTextArea();
		RoutingArea.setEditable(false);
		RoutingArea.setBounds(14, 50, 430, 300);
		Routing_Table.add(RoutingArea);

		btnRoutingAdd = new JButton("추가");
		btnRoutingAdd.setBounds(42, 355, 165, 35);
		Routing_Table.add(btnRoutingAdd);
		btnRoutingAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				new RoutingTableAdd(routingTable,RoutingArea);
			}
		});

		btnRoutingDelete = new JButton("삭제");

		btnRoutingDelete.setBounds(249, 355, 165, 35);
		Routing_Table.add(btnRoutingDelete);

		JPanel ARP_Cache = new JPanel();
		ARP_Cache.setBounds(486, 12, 458, 402);
		ARP_Cache.setBorder(new TitledBorder(null, "ARP Cache Table", TitledBorder.LEADING, TitledBorder.TOP, null, null));

		ARP_Cache.setLayout(null);
		contentPane.add(ARP_Cache);

		JTable ARP_table;

		DefaultTableModel ARP_model = new DefaultTableModel(); 
		ARP_model.addColumn("IP Address");
		ARP_model.addColumn("Ethernet Address");
		ARP_model.addColumn("Interface");
		ARP_model.addColumn("Flag");

		ARP_table = new JTable(ARP_model); 

		ARP_table.getColumnModel().getColumn(0).setPreferredWidth(100);
		ARP_table.getColumnModel().getColumn(1).setPreferredWidth(100);
		ARP_table.getColumnModel().getColumn(2).setPreferredWidth(40);
		ARP_table.getColumnModel().getColumn(3).setPreferredWidth(10);

		JScrollPane ARP_jScrollPane = new JScrollPane(ARP_table); 
		ARP_jScrollPane.setBounds(14, 30, 430, 20);
		ARP_Cache.add(ARP_jScrollPane); 

		TotalArea = new JTextArea();
		TotalArea.setEditable(false);
		TotalArea.setBounds(14, 50, 430, 300);
		ARP_Cache.add(TotalArea);

		btnARPDelete = new JButton("삭제");

		btnARPDelete.setBounds(150, 355, 165, 35);
		ARP_Cache.add(btnARPDelete);

		btnARPDelete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				String del_ip = JOptionPane.showInputDialog("Item's IP Address");
				if (del_ip != null) {
					if (((ARPLayer) m_LayerMgr.GetLayer("ARP")).cacheTable.containsKey(del_ip)) {
						Object[] value = ((ARPLayer) m_LayerMgr.GetLayer("ARP")).cacheTable.get(del_ip);
						if (System.currentTimeMillis() - (long) value[3] / 1000 > 1) {
							((ARPLayer) m_LayerMgr.GetLayer("ARP")).cacheTable.remove(del_ip);
							arpTable.updateARPCacheTable();
						}
					}else if (((ARPLayer) m_LayerMgr.GetLayer("ARP2")).cacheTable.containsKey(del_ip)) {
						Object[] value = ((ARPLayer) m_LayerMgr.GetLayer("ARP2")).cacheTable.get(del_ip);
						if (System.currentTimeMillis() - (long) value[3] / 1000 > 1) {
							((ARPLayer) m_LayerMgr.GetLayer("ARP2")).cacheTable.remove(del_ip);
							arpTable.updateARPCacheTable();
						}
					}
				}
			}
		});

		this.exist = true;
		setVisible(true);
	}

	public boolean Receive(byte[] input) {
		byte[] data = input;
		return false;
	}

	class setAddressListener implements ActionListener  {
		@Override
		public void actionPerformed(ActionEvent e) {

			if(e.getSource() == Setting_Button) {

				Setting_Button.setEnabled(false);
				strCombo1.setEnabled(false);
				strCombo2.setEnabled(false);

				index1 = strCombo1.getSelectedIndex();
				index2 = strCombo2.getSelectedIndex();

				try {
					byte[] mac0 = ((NILayer) m_LayerMgr.GetLayer("NI")).m_pAdapterList.get(index1).getHardwareAddress();
					byte[] mac1 = ((NILayer) m_LayerMgr.GetLayer("NI2")).m_pAdapterList.get(index2).getHardwareAddress();

					final StringBuilder EthernetAddrbuf1 = new StringBuilder();
					for(byte b:mac0) {
						if(EthernetAddrbuf1.length()!=0) EthernetAddrbuf1.append(":");
						if(b>=0 && b<16) EthernetAddrbuf1.append('0');
						EthernetAddrbuf1.append(Integer.toHexString((b<0)? b+256:b).toUpperCase());
					}

					final StringBuilder EthernetAddrbuf2 = new StringBuilder();
					for(byte b:mac1) {
						if(EthernetAddrbuf2.length()!=0) EthernetAddrbuf2.append(":");
						if(b>=0 && b<16) EthernetAddrbuf2.append('0');
						EthernetAddrbuf2.append(Integer.toHexString((b<0)? b+256:b).toUpperCase());
					}

					byte[] ipSrcAddress1 = ((((NILayer)m_LayerMgr.GetLayer("NI")).m_pAdapterList.get(index1).getAddresses()).get(0)).getAddr().getData();
					final StringBuilder IPAddrbuf0 = new StringBuilder();
					for(byte b:ipSrcAddress1) {
						if(IPAddrbuf0.length()!=0) IPAddrbuf0.append(".");
						IPAddrbuf0.append(b&0xff);
					}

					byte[] ipSrcAddress2 = ((((NILayer)m_LayerMgr.GetLayer("NI")).m_pAdapterList.get(index2).getAddresses()).get(0)).getAddr().getData();
					final StringBuilder IPAddrbuf1 = new StringBuilder();
					for(byte b:ipSrcAddress2) {
						if(IPAddrbuf1.length()!=0) IPAddrbuf1.append(".");
						IPAddrbuf1.append(b&0xff);
					}

					System.out.println("NIC1: "+IPAddrbuf0.toString()+" // "+EthernetAddrbuf1.toString());
					System.out.println("NIC2: "+IPAddrbuf1.toString()+" // "+EthernetAddrbuf2.toString());

					/*IP Address 설정*/
					((IPLayer)m_LayerMgr.GetLayer("IP")).SetIPSrcAddress(ipSrcAddress1);
					((IPLayer)m_LayerMgr.GetLayer("IP2")).SetIPSrcAddress(ipSrcAddress2);

					/*IP Port 설정*/
					((IPLayer)m_LayerMgr.GetLayer("IP")).setPort(mac0, "Port1");
					((IPLayer)m_LayerMgr.GetLayer("IP2")).setPort(mac1, "Port2");

					/*ARP Address 설정*/
					((ARPLayer)m_LayerMgr.GetLayer("ARP")).SetIPAddrSrcAddr(ipSrcAddress1);
					((ARPLayer)m_LayerMgr.GetLayer("ARP2")).SetIPAddrSrcAddr(ipSrcAddress2);

					/*Ethernet Mac 주소 설정*/
					((EthernetLayer) m_LayerMgr.GetLayer("Ethernet")).SetEnetSrcAddress(mac0);
					((EthernetLayer) m_LayerMgr.GetLayer("Ethernet2")).SetEnetSrcAddress(mac1);

					/*Receive 실행*/
					((NILayer) m_LayerMgr.GetLayer("NI")).SetAdapterNumber(index1);
					((NILayer) m_LayerMgr.GetLayer("NI2")).SetAdapterNumber(index2);

				} catch (IOException e1) {
					e1.printStackTrace();
				}

			}
		}
	}

	@Override
	public void SetUnderLayer(BaseLayer pUnderLayer) {
		this.p_aUnderLayerGUI.add(nUnderLayerCount++, pUnderLayer);
	}

	@Override
	public void SetUpperLayer(BaseLayer pUpperLayer) {
		if (pUpperLayer == null)
			return;
		this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
	}

	@Override
	public String GetLayerName() {
		return pLayerName;
	}

	@Override
	public BaseLayer GetUnderLayer() {
		return null;
	}

	@Override
	public BaseLayer GetUpperLayer(int nindex) {
		if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
			return null;
		return p_aUpperLayer.get(nindex);
	}

	@Override
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer);
		this.SetUnderLayer(pUULayer);
	}

	@Override
	public BaseLayer GetUnderLayer(int nindex) {
		if (nindex < 0 || nindex > nUnderLayerCount || nUnderLayerCount < 0)
			return null;
		return p_aUnderLayerGUI.get(nindex);
	}
}

//Routing Table에 추가하기 위한 새 창
class RoutingTableAdd extends JFrame {

	JTextArea input_Destination;
	JTextArea input_Netmask;
	JTextArea input_Gateway;
	JComboBox<String> selectInterface;
	JLabel lbl_Destination;
	JLabel lbl_Netmask;
	JLabel lbl_Gateway;
	JLabel lbl_Flag;
	JLabel lbl_Interface;
	Container contentPane;
	String[] interfaceName = { "Port1", "Port2" };
	String interface0 = interfaceName[0];

	JCheckBox flagU;
	JCheckBox flagG;
	JCheckBox flagH;
	
	public static String Entry = "";

	public RoutingTableAdd(RoutingTable routerTable, JTextArea routingTable) {

		setTitle("Router Table Entry 추가");
		setSize(450, 350);
		setLocation(300, 250);
		getContentPane().setLayout(null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		contentPane = new JPanel();
		((JComponent) contentPane).setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		lbl_Destination = new JLabel("Destination");
		lbl_Destination.setBounds(50, 25, 90, 30);

		input_Destination = new JTextArea();
		input_Destination.setBounds(150, 30, 170, 20);

		contentPane.add(lbl_Destination);
		contentPane.add(input_Destination);

		lbl_Netmask = new JLabel("Netmask");
		lbl_Netmask.setBounds(50, 60, 90, 30);

		input_Netmask = new JTextArea();
		input_Netmask.setBounds(150, 65, 170, 20);

		contentPane.add(lbl_Netmask);
		contentPane.add(input_Netmask);

		lbl_Gateway = new JLabel("Gateway");
		lbl_Gateway.setBounds(50, 95, 90, 30);

		input_Gateway = new JTextArea();
		input_Gateway.setBounds(150, 100, 170, 20);

		contentPane.add(lbl_Gateway);
		contentPane.add(input_Gateway);

		lbl_Flag = new JLabel("Flag");
		lbl_Flag.setBounds(50, 130, 90, 30);
		contentPane.add(lbl_Flag);

		flagU = new JCheckBox("UP");
		flagU.setBounds(150, 135, 50, 20);
		contentPane.add(flagU);

		flagG = new JCheckBox("Gateway");
		flagG.setBounds(205, 135, 80, 20);
		contentPane.add(flagG);

		flagH = new JCheckBox("Host");
		flagH.setBounds(285, 135, 60, 20);
		contentPane.add(flagH);

		lbl_Interface = new JLabel("Interface");
		lbl_Interface.setBounds(50, 165, 90, 30);
		contentPane.add(lbl_Interface);

		selectInterface = new JComboBox<String>(interfaceName);
		selectInterface.setBounds(150, 170, 170, 20);
		selectInterface.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				interface0 = interfaceName[selectInterface.getSelectedIndex()];
			}
		});

		contentPane.add(selectInterface);

		JButton btnAdd = new JButton("추가");
		btnAdd.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!input_Destination.getText().equals("") && !input_Netmask.getText().equals("")
						&& !input_Gateway.getText().contentEquals("")) {

					StringTokenizer st = new StringTokenizer(input_Destination.getText(), ".");

					byte[] Destination = new byte[4];
					for (int i = 0; i < 4; i++) {
						String ss = st.nextToken();
						int s = Integer.parseInt(ss);
						Destination[i] = (byte) (s & 0xFF);
					}

					st = new StringTokenizer(input_Netmask.getText(), ".");

					byte[] Netmask = new byte[4];
					for (int i = 0; i < 4; i++) {
						String ss = st.nextToken();
						int s = Integer.parseInt(ss);
						Netmask[i] = (byte) (s & 0xFF);
					}

					st = new StringTokenizer(input_Gateway.getText(), ".");

					byte[] Gateway = new byte[4];
					for (int i = 0; i < 4; i++) {
						String ss = st.nextToken();
						int s = Integer.parseInt(ss);
						Gateway[i] = (byte) (s & 0xFF);
					}

					String interface_Num = interface0;

					Object[] value = new Object[7];
					value[0] = Destination;
					value[1] = Netmask;
					value[2] = Gateway;
					value[3] = flagU.isSelected();
					value[4] = flagG.isSelected();
					value[5] = flagH.isSelected();
					value[6] = interface_Num;

					routerTable.add(input_Destination.getText(), value);

					String flags = "";

					if((Boolean) value[3]) {
						flags += "U";
					}
					if((Boolean) value[4]) {
						flags += "G";
					}
					if((Boolean) value[5]) {
						flags += "H";
					}

					Entry = Entry + "     " + input_Destination.getText() + "         " + input_Netmask.getText() + "             " + input_Gateway.getText() + "           " + flags + "        " + value[6] + '\n';
					routingTable.setText(Entry);
					dispose();
				}

			}
		});

		btnAdd.setBounds(130, 250, 80, 30);
		getContentPane().add(btnAdd);

		JButton btnCancel = new JButton("취소");
		btnCancel.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent arg0) {
				dispose();
			}
		});
		btnCancel.setBounds(220, 250, 80, 30);
		getContentPane().add(btnCancel);
		setVisible(true);
	}
}


