package ZDHLIB.UTF8_to_UNICODE;

public class main {

	/**
	 * 将utf-8编码转化为unicode编码
	 * 
	 * @param byte[] aByte: 原字符串的utf-8编码字节数组 
	 * @return byte[] sByte: 转化后的unicode编码方式的字符串
	 */
	public static String changeUtf8ToUnicodeBigEndian(byte[] aByte) {
		
		int sLength = aByte.length; // 原字节数组长度
		
		// 存储转化为unicode编码后的StringBuffer字符串
		StringBuffer sUnicodeStringBuffer = new StringBuffer();
		
		char sChar; // 用于临时存放每个从utf-8中解析出来的unicode编码
		
		// 循环每一个字节, 判断字节是否以"1110 xxxx 10xxxxxx 10xxxxxx"的形式出现
		for (int i = 0; i < sLength; i++) {  
			if (i + 2 < sLength) {
				/**
				 * aByte[i] & 0xF0 == 0xE0 ---> 判断当前字节是否以“1110”的形式开始； 
				 * aByte[i + 1] & 0xC0 == 0x80 ---> 判断下一个字节是否以“10”的形式开始；
				 * aByte[i + 2] & 0xC0 == 0x80 ---> 判断下下一个字节是否以“10”的形式开始。
				 * 假如条件都满足，则表示此断字节进行了utf-8编码，则将对其进行解码操作（即转 * 化为unicode编码）
				 */
				if ((aByte[i] & 0xF0) == 0xE0 && 
					(aByte[i + 1] & 0xC0) == 0x80 &&
					(aByte[i + 2] & 0xC0) == 0x80) {
					/**
					 * 将当前字节 1110 xxxx 转化为 xxxx 000000 000000 的形式，
					 * 具体步骤为： 1110 xxxx << 12 = xxxx 000000 000000 
					 *    比如：        1110 0100 << 12 = 0100 000000 000000
					 */
					sChar = (char) (aByte[i] << 12);
					
					/**
					 * 将前两个字节 转化为 xxxx xxxxxx 000000 的形式
					 * 具体步骤为： 
					 * 10 xxxxxx & 0x003F = 0000 000000 xxxxxx 
					 * 10 111000 & 0x003F = 0000 000000 111000
					 * 
					 * 0000 000000 xxxxxx << 6 = 0000 xxxxxx 000000 
					 * 0000 000000 111000 << 6 = 0000 111000 000000
					 * 
					 * xxxx 000000 000000 | 0000 xxxxxx 000000 = xxxx xxxxxx 000000 
					 * 0100 000000 000000 | 0000 111000 000000 = 0100 111000 000000
					 */
					sChar = (char) ((((aByte[i + 1] & 0x003F) << 6) | sChar));
					
					/**
					 * 将此三个字节转化为 xxxx xxxxxx xxxxxx 的形式
					 * 具体步骤为：
					 * 10 xxxxxx & 0x003F = 0000 0000 00 xxxxxx 
					 * 10 101101 & 0x003F = 0000 0000 00 101101
					 * 
					 * xxxx xxxxxx 000000 | 0000 000000 xxxxxx = xxxx xxxxxx xxxxxx
					 * 0100 111000 000000 | 0000 000000 101101 = 0100 111000 101101
					 */
					sChar = (char) ((aByte[i + 2] & 0x003F) | sChar);
					i = i + 2;
					sUnicodeStringBuffer.append(sChar);
				} else {
					sUnicodeStringBuffer.append((char) aByte[i]);
				}
			}
		}
		return sUnicodeStringBuffer.toString();
	}
	
	
	/**
     * 将UTF-8字节数据转化为unicode字符串
     * @param utf_data byte[] - UTF-8编码字节数组
     * @param len int - 字节数组长度
     * @return String - 变换后的unicode编码字符串
     */

	public static String UTF2Uni(byte[] utf_data, int len)
	{
		StringBuffer unis = new StringBuffer();
		char unic = 0;
		int ptr = 0;
		int cntBits = 0;

		for (; ptr < len;)
		{
			cntBits = getCntBits(utf_data[ptr]);
			if (cntBits == -1){
				++ptr;
				continue;
			}else if (cntBits == 0){
				unic = UTFC2UniC(utf_data, ptr, cntBits);
				++ptr;
			}else{
				unic = UTFC2UniC(utf_data, ptr, cntBits);
				ptr += cntBits;
			}

			unis.append(unic);

		}

		return unis.toString();

	}

	
	/**	 
     * 将指定的UTF-8字节组合成一个unicode编码字符
     * @param utf byte[] - UTF-8字节数组
     * @param sptr int - 编码字节起始位置
     * @param cntBits int - 编码字节数
     * @return char - 变换后的unicode字符
     */

	public static char UTFC2UniC(byte[] utf, int sptr, int cntBits){

		/**
		 * unicode <-> UTF-8
		 * U-00000000 - U-0000007F: 0xxxxxxx
		 * U-00000080 - U-000007FF: 110xxxxx 10xxxxxx
		 * U-00000800 - U-0000FFFF: 1110xxxx 10xxxxxx 10xxxxxx
		 * U-00010000 - U-001FFFFF: 11110xxx 10xxxxxx 10xxxxxx 10xxxxxx
		 * U-00200000 - U-03FFFFFF: 111110xx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
		 * U-04000000 - U-7FFFFFFF: 1111110x 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx 10xxxxxx
		 */
		int uniC = 0; // represent the unicode char

		byte firstByte = utf[sptr];

		int ptr = 0; // pointer 0 ~ 15

		// resolve single byte UTF-8 encoding char

		if (cntBits == 0)
			return (char) firstByte;

		// resolve the first byte

		firstByte &= (1 << (7 - cntBits)) - 1;

		// resolve multiple bytes UTF-8 encoding char(except the first byte)

		for (int i = sptr + cntBits - 1; i > sptr; --i){

			byte utfb = utf[i];

			uniC |= (utfb & 0x3f) << ptr;

			ptr += 6;

		}

		uniC |= firstByte << ptr;

		return (char) uniC;

	}

	/**
	 *  根据给定字节计算UTF-8编码的一个字符所占字节数,
	 *  UTF-8规则定义，字节标记只能为0或2~6
	 * @param b
	 * @return int
	 */
	private static int getCntBits(byte b){

		int cnt = 0;
		if (b == 0)
			return -1;

		for (int i = 7; i >= 0; --i){

			if (((b >> i) & 0x1) == 1)
				++cnt;
			else
				break;
		}

		return (cnt > 6 || cnt == 1) ? -1 : cnt;
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
