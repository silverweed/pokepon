//: net/jack/server/IPClass.java

package pokepon.net.jack.server;

import static pokepon.util.MessageManager.*;

/** Class representing an IP or an IP range;
 * an IP class may be represented in the following ways:
 * <ul>
 *   <li>127.0.0.1 (single IP)</li>
 *   <li>127.0.* (all IPs starting with 127.0.)</li>
 *   <li>127.0.0.[1-255] (IP ranging from 127.0.0.1 to 127.0.0.255); in this case,
 *     a `*` is implied after the range if it's not in the last group (i.e 127.{1-3} is valid)</li>
 *   <li>* (all IPs)</li>
 *   <li>127.0.0.1/24 (subnet with base IP 127.0.0.1 and netmask 24)</li>
 * </ul>
 *
 * @author silverweed
 */
class IPClass {

	public static enum ClassType { EVERYTHING, SINGLE, RANGE, NETMASK };

	private ClassType classType = ClassType.SINGLE;
	private String startIp, endIp;
	private int netmask;
	private int ipInt;
	private String ip;
	private short[] octets = new short[4];
	/** If IP is a range, these are the starting and ending octets */
	private short[] startOctets = new short[4], endOctets = new short[4];
	private final String ipStr;

	public boolean equals(final IPClass other) {
		if(classType != other.classType) return false;
		switch(classType) {
			case EVERYTHING: return true;
			case SINGLE: return ip.equals(other.ip);
			case RANGE:
				for(int i = 0; i < 4; ++i)
					if(startOctets[i] != other.startOctets[i] || endOctets[i] != other.endOctets[i])
						return false;
				return true;
			case NETMASK:
				return ip.equals(other.ip) && netmask == other.netmask;
		}
		return false;
	}

	public IPClass(final String str) throws IllegalArgumentException {
		ipStr = str;
		byte i = 0;
		char lastTok = '\000';
		StringBuilder numBuf = new StringBuilder("");

		// parse IP string passed 
		outer:
		for(int c = 0; c < str.length(); ++c) {
			if(i == 4)	// should have exited by now
				throw new IllegalArgumentException("[@pos "+c+"] Invalid IP string "+str+" (trailing characters)");

			switch(str.charAt(c)) {
				case '*': {
					// `*` must be the last character
					if(str.length() > c + 1)
						throw new IllegalArgumentException("[@pos "+c+"] Invalid IP string: "+
											str+" (`*` not last character)");

					if(c == 0) {
						classType = ClassType.EVERYTHING;
						ip = "0.0.0.0.";
						startIp = "0.0.0.0";
						endIp = "255.255.255.255";
						break outer;
					}
					if(lastTok != '.') // '*' must follow a dot if not at start of string
						throw new IllegalArgumentException("[@pos "+c+"] Invalid IP string: "+str);

					// IP class is like xxx.yyy.*: set start = xxx.yyy.0.0; end = xxx.yyy.255.255
					classType = ClassType.RANGE;
					StringBuilder sb = new StringBuilder("");
					for(byte j = 0; j < i; ++j) {
						sb.append(Short.toString(octets[j]));
						sb.append(".");
					}
					StringBuilder startsb = new StringBuilder(sb.toString()), endsb = new StringBuilder(sb.toString());
					for(byte j = 0; j < 4 - i; ++j) {
						startsb.append("0");
						endsb.append("255");
						startsb.append(".");
						endsb.append(".");
					}
					startsb.delete(startsb.length() - 1, startsb.length());
					endsb.delete(endsb.length() - 1, endsb.length());
					startIp = startsb.toString();
					endIp = endsb.toString();
					break outer;
				}
				case '.':
					if(numBuf.length() < 1) 
						throw new IllegalArgumentException("[@pos "+c+"] Invalid IP string: "+
											str+" (empty octet #"+i+")");
					octets[i++] = Short.parseShort(numBuf.toString());
					if(octets[i-1] < 0 || octets[i-1] > 255)
						throw new IllegalArgumentException("[@pos "+c+"] Invalid IP string: "+
											str+" (octet must be between 0 and 255)");
					// erase the string buffer
					numBuf.setLength(0);
					break;

				case '[': {
					if(c > 0 && lastTok != '.')
						throw new IllegalArgumentException("[@pos "+c+"] Invalid IP string: "+
											str+" (range must be at beginning or after a '.')");
					// eat up and parse the rest of the string (should be: [start-end])
					StringBuilder sb = new StringBuilder("");
					int d = c + 1;
					short start = -1, end = -1;
					inner:
					while(d < str.length()) {
						if(str.charAt(d) == '-') {
							if(start != -1)
								throw new IllegalArgumentException("[@pos "+d+"] Invalid IP string: "+
													str+" (too many `-` in IP range)");
							if(sb.length() < 1)
								throw new IllegalArgumentException("[@pos "+d+"] Invalid IP string: "+
													str+" (missing starting IP)");
							try {
								start = Short.parseShort(sb.toString());
							} catch(IllegalArgumentException e) {
								throw new IllegalArgumentException("[@pos "+d+"] Invalid IP string: "+
													str+" (invalid range start: "+sb+")");
							}
							if(start < 0 || start > 255)
								throw new IllegalArgumentException("[@pos "+d+"] Invalid IP string: "+
													str+" (octet must be between 0 and 255)");
							sb.setLength(0);

						} else if(str.charAt(d) == ']') {
							if(sb.length() < 1)
								throw new IllegalArgumentException("[@pos "+d+"] Invalid IP string: "+
													str+" (missing ending IP)");
							try {
								end = Short.parseShort(sb.toString());
							} catch(IllegalArgumentException e) {
								throw new IllegalArgumentException("[@pos "+d+"] Invalid IP string: "+
													str+" (invalid range end: "+sb+")");
							}
							if(end < 0 || end > 255)
								throw new IllegalArgumentException("[@pos "+d+"] Invalid IP string: "+
													str+" (octet must be between 0 and 255)");
							if(end < start)
								throw new IllegalArgumentException("[@pos "+d+"] Invalid IP string: "+
													str+" (end < start)");
							if(str.length() > d + 1)
								throw new IllegalArgumentException("[@pos "+d+"] Invalid IP string: "+
													str+" (trailing characters)");

						} else if(str.charAt(d) != ' ') {
							sb.append(str.charAt(d));
						}
						++d;
					}
					if(end == -1)
						throw new IllegalArgumentException("[@pos "+d+"] Invalid IP string: "+
											str+" (Unterminated range)");
					classType = ClassType.RANGE;
					sb = new StringBuilder("");
					for(byte j = 0; j < i; ++j) {
						sb.append(Short.toString(octets[j]));
						sb.append(".");
					}
					StringBuilder startsb = new StringBuilder(sb.toString()), endsb = new StringBuilder(sb.toString());
					startsb.append(Short.toString(start));
					startsb.append(".");
					endsb.append(Short.toString(end));
					endsb.append(".");
					for(byte j = 1; j < 4 - i; ++j) {
						startsb.append("0");
						endsb.append("255");
						startsb.append(".");
						endsb.append(".");
					}
					startsb.delete(startsb.length() - 1, startsb.length());
					endsb.delete(endsb.length() - 1, endsb.length());
					startIp = startsb.toString();
					endIp = endsb.toString();
					break outer;
				}
				case '/':
					if(i != 3 || numBuf.length() < 1)
						throw new IllegalArgumentException("[@pos "+c+"] Invalid IP string: "+
											str+" (netmask expected after 4th octet)");
					if(str.length() < c + 2)
						throw new IllegalArgumentException("[@pos "+c+"] Invalid IP string: "+
											str+" (empty netmask)");

					octets[i++] = Short.parseShort(numBuf.toString());
					if(octets[i-1] < 0 || octets[i-1] > 255)
						throw new IllegalArgumentException("[@pos "+c+"] Invalid IP string: "+
											str+" (octet must be between 0 and 255)");
					try {
						netmask = Short.parseShort(str.substring(c + 1));
					} catch(IllegalArgumentException e) {
						throw new IllegalArgumentException("[@pos "+c+"] Invalid IP string: "+
											str+" (non-numeric netmask)");
					}
					if(netmask < 0 || netmask > 32)
						throw new IllegalArgumentException("[@pos "+c+"] Invalid IP string: "+
											str+" (netmask must be between 0 and 32)");
					classType = ClassType.NETMASK;
					// terminate parsing
					break outer;

				case '0': case '1': case '2': case '3': case '4':
				case '5': case '6': case '7': case '8': case '9':
					numBuf.append(str.charAt(c));
					break;

				default:
					throw new IllegalArgumentException("[@pos "+c+"] Invalid IP string: "+
										str+" (invalid char: "+str.charAt(c)+")");
			}
			lastTok = str.charAt(c);
		}
		switch(classType) {
			case SINGLE:
				if(lastTok != '.') {
					try {
						octets[i] = Short.parseShort(numBuf.toString());
					} catch(IllegalArgumentException e) {
						throw new IllegalArgumentException("[@pos "+str.length()+"] Invalid IP string: "+
											str+" (invalid octet: "+numBuf+")");
					}
					if(octets[i] < 0 || octets[i] > 255)
						throw new IllegalArgumentException("[@pos "+str.length()+"] Invalid IP string: "+
											str+" (octet must be between 0 and 255)");
				}
				/* falls through */
			case NETMASK:
				for(int j = 0; j < 4; ++j)
					ipInt |= octets[j] << (8 * (3 - j));
				break;
			case RANGE: {
				String[] st = startIp.split("\\.", 4);
				String[] en = endIp.split("\\.", 4);
				for(byte j = 0; j < 4; ++j) {
					startOctets[j] = Short.parseShort(st[j]);
					endOctets[j] = Short.parseShort(en[j]);
				}
				break;
			}
		}

		ip = "";
		for(short s : octets)
			ip += Short.toString(s) + ".";
	}
	
	public ClassType getClassType() { return classType; }

	/** @return True, if given IP is included in this IP class, False otherwise */
	public boolean includes(String ipStr) throws IllegalArgumentException {
		// ensure passed string is a valid IP
		short[] itsOctets = new short[4];
		String[] splitted = ipStr.split("\\.");
		if(splitted.length != 4)
			throw new IllegalArgumentException("[IPClass.includes("+ipStr+")] Invalid IP.");

		for(byte i = 0; i < splitted.length; ++i) {
			try {
				itsOctets[i] = Short.parseShort(splitted[i]);
			} catch(IllegalArgumentException|IndexOutOfBoundsException e) {
				throw new IllegalArgumentException("[IPClass.includes("+ipStr+")] Invalid IP.");
			}
		}
		switch(classType) {
			case EVERYTHING: return true;
			case SINGLE: 
				for(byte i = 0; i < 4; ++i)
					if(itsOctets[i] != octets[i])
						return false;
				return true;
			case RANGE: 
				for(byte i = 0; i < 4; ++i)
					if(itsOctets[i] < startOctets[i] || itsOctets[i] > endOctets[i])
						return false;
				return true;
			case NETMASK:
				int addr = 0;
				for(byte i = 0; i < 4; ++i)
					addr |= itsOctets[i] << (8 * (3 - i));
				int mask = 0;
				for(byte i = 0; i < netmask; ++i)
					mask |= 1 << (31 - i);
				int subnet = ipInt & mask;
				return ((ipInt ^ addr) & mask) == 0;
		}				
		return false;
	}

	@Override
	public String toString() {
		return ipStr;
	}

	/** Testing method */
	public static void main(String[] args) throws IllegalArgumentException {
		IPClass ip = new IPClass(args[0]);
		printMsg("Parsed:\n String = " + args[0] + "\n IP = " + ip.ip + 
				"\n classtype = "+ip.classType+"\n Netmask = "+ip.netmask+
				"\n range: {"+ip.startIp+"-"+ip.endIp+"}");
		IPClass[] testIps = new IPClass[] {
			new IPClass("*"),
			new IPClass("127.*"),
			new IPClass("[10-100]"),
			new IPClass("127.[0-1]"),
			new IPClass("127.0.0.*"),
			new IPClass("127.0.0.1"),
			new IPClass("127.0.0.0"),
			new IPClass("127.0.0.1/24"),
			new IPClass("127.0.0.1/16")
		};
		for(IPClass ipc : testIps) {
			printMsg("Is included in "+ipc+"?  "+ipc.includes(ip.ipStr));
			printMsg("Equals "+ipc+"?  "+ipc.equals(ip));
		}
	}
}
