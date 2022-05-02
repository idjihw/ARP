package ROUTER;

import java.util.HashMap;
import java.util.Map;

public class RoutingTable {

	public static Map<String, Object[]> rountingTable;

	public RoutingTable() {
		rountingTable = new HashMap<String, Object[]>();
	}

	public static void add(String key, Object[] value) {
		rountingTable.put(key, value);
	}

	public Object[] findEntry(byte[] dstIP) {
		if(rountingTable.isEmpty())
			return null;
		
		for(Map.Entry<String, Object[]> entry : rountingTable.entrySet()) {
			byte[] netmask = (byte[])(entry.getValue()[1]);
			byte[] maskingResult = new byte[4];
			
			//subnetmask와 목적지 IP 주소 and 연산
			for(int i = 0; i < 4; i++) {
				maskingResult[i] = (byte)(dstIP[i] & netmask[i]);
			}
			
			String str_maskingResult = (maskingResult[0]&0xFF)+"."+(maskingResult[1]&0xFF)+"."+(maskingResult[2]&0xFF)+"."+(maskingResult[3]&0xFF);
			
			//목적지 IP 주소와 masking 결과가 같을 때
			if(entry.getKey().equals(str_maskingResult)) {
				return entry.getValue();
			}
		}
		return null;
	}
}
