package ROUTER;

import java.io.File;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

//네트워크 디바이스 탐색
import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
//패킷 캡쳐
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;

public class NILayer implements BaseLayer {

	static {
		try {
			// native Library Load
			System.load(new File("jnetpcap.dll").getAbsolutePath());
			System.out.println(new File("jnetpcap.dll").getAbsolutePath());
		} catch (UnsatisfiedLinkError e) {
			System.out.println("Native code library failed to load.\n" + e);
			System.exit(1);
		}
	}
	
	public int nUpperLayerCount = 0;
	public String pLayerName = null;
	public BaseLayer p_UnderLayer = null;
	public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
	int m_iNumAdapter;
	public Pcap m_AdapterObject;
	public PcapIf device;
	public ArrayList<PcapIf> m_pAdapterList; //디바이스를 담을 변수를 ArrayList로 생성
	StringBuilder errbuf = new StringBuilder(); //에러 처리
	
	public NILayer(String pName) {
		// super(pName);
		pLayerName = pName;

		m_pAdapterList = new ArrayList<PcapIf>();
		m_iNumAdapter = 0;
		SetAdapterList();
	}

	public void PacketStartDriver() {
		int snaplen = 64 * 1024; // 65536바이트만큼 패킷 캡쳐
		int flags = Pcap.MODE_PROMISCUOUS;
		int timeout = 10 * 1000;
		m_AdapterObject = Pcap.openLive(m_pAdapterList.get(m_iNumAdapter).getName(), snaplen, flags, timeout, errbuf);
	}

	public PcapIf GetAdapterObject(int iIndex) { //index에 해당하는 네트워크 디바이스 가져오는 함수
		return m_pAdapterList.get(iIndex);
	}

	public void SetAdapterNumber(int iNum) {
		m_iNumAdapter = iNum;
		PacketStartDriver();
		Receive();
	}

	public void SetAdapterList() {
		// 현재 컴퓨터에 존재하는 모든 네트워크 어뎁터 목록 가져오기
		int r = Pcap.findAllDevs(m_pAdapterList, errbuf);
		
		// 네트워크 어뎁터가 하나도 존재하지 않을 경우 에러 처리
		if (r == Pcap.NOT_OK || m_pAdapterList.isEmpty()) {
			System.err.printf("[Error] 네트워크 어댑터를 읽지 못하였습니다. Error : ", errbuf.toString());
			return;
		}
	}

	public ArrayList<PcapIf> getAdapterList() {
		return m_pAdapterList;
	}
	
	public boolean Send(byte[] input, int length) {
		
		ByteBuffer buf = ByteBuffer.wrap(input);
		if (m_AdapterObject.sendPacket(buf) != Pcap.OK) {
			System.err.println(m_AdapterObject.getErr());
			return false;
		}
		return true;
	}

	public boolean Receive() {
		Receive_Thread thread = new Receive_Thread(m_AdapterObject, this.GetUpperLayer(0));
		Thread obj = new Thread(thread);
		obj.start();

		return false;
	}

	@Override
	public void SetUnderLayer(BaseLayer pUnderLayer) {
		// TODO Auto-generated method stub
		if (pUnderLayer == null)
			return;
		p_UnderLayer = pUnderLayer;
	}

	@Override
	public void SetUpperLayer(BaseLayer pUpperLayer) {
		// TODO Auto-generated method stub
		if (pUpperLayer == null)
			return;
		this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
		// nUpperLayerCount++;
	}

	@Override
	public String GetLayerName() {
		// TODO Auto-generated method stub
		return pLayerName;
	}

	@Override
	public BaseLayer GetUnderLayer() {
		if (p_UnderLayer == null)
			return null;
		return p_UnderLayer;
	}

	@Override
	public BaseLayer GetUpperLayer(int nindex) {
		// TODO Auto-generated method stub
		if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
			return null;
		return p_aUpperLayer.get(nindex);
	}

	@Override
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);

	}

	@Override
	public BaseLayer GetUnderLayer(int nindex) {
		// TODO Auto-generated method stub
		return null;
	}
}

class Receive_Thread implements Runnable {
	byte[] data;
	Pcap AdapterObject;
	BaseLayer UpperLayer;

	public Receive_Thread(Pcap m_AdapterObject, BaseLayer m_UpperLayer) {
		// TODO Auto-generated constructor stub
		AdapterObject = m_AdapterObject;
		UpperLayer = m_UpperLayer;
	}

	@Override
	public void run() {
		while (true) {
			PcapPacketHandler<String> jpacketHandler = new PcapPacketHandler<String>() {
				public void nextPacket(PcapPacket packet, String user) {
					data = packet.getByteArray(0, packet.size());
					UpperLayer.Receive(data);
				}
			};

			AdapterObject.loop(100000, jpacketHandler, ""); //100000개의 패킷 캡쳐
		}
	}
}
