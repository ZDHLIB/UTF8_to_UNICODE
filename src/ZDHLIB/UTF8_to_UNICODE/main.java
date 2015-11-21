package ZDHLIB.UTF8_to_UNICODE;

public class main {

	/**
	 * ��utf-8����ת��Ϊunicode����
	 * 
	 * @param byte[] aByte: ԭ�ַ�����utf-8�����ֽ����� 
	 * @return byte[] sByte: ת�����unicode���뷽ʽ���ַ���
	 */
	public static String changeUtf8ToUnicodeBigEndian(byte[] aByte) {
		
		int sLength = aByte.length; // ԭ�ֽ����鳤��
		
		// �洢ת��Ϊunicode������StringBuffer�ַ���
		StringBuffer sUnicodeStringBuffer = new StringBuffer();
		
		char sChar; // ������ʱ���ÿ����utf-8�н���������unicode����
		
		// ѭ��ÿһ���ֽ�, �ж��ֽ��Ƿ���"1110 xxxx 10xxxxxx 10xxxxxx"����ʽ����
		for (int i = 0; i < sLength; i++) {  
			if (i + 2 < sLength) {
				/**
				 * aByte[i] & 0xF0 == 0xE0 ---> �жϵ�ǰ�ֽ��Ƿ��ԡ�1110������ʽ��ʼ�� 
				 * aByte[i + 1] & 0xC0 == 0x80 ---> �ж���һ���ֽ��Ƿ��ԡ�10������ʽ��ʼ��
				 * aByte[i + 2] & 0xC0 == 0x80 ---> �ж�����һ���ֽ��Ƿ��ԡ�10������ʽ��ʼ��
				 * �������������㣬���ʾ�˶��ֽڽ�����utf-8���룬�򽫶�����н����������ת * ��Ϊunicode���룩
				 */
				if ((aByte[i] & 0xF0) == 0xE0 && 
					(aByte[i + 1] & 0xC0) == 0x80 &&
					(aByte[i + 2] & 0xC0) == 0x80) {
					/**
					 * ����ǰ�ֽ� 1110 xxxx ת��Ϊ xxxx 000000 000000 ����ʽ��
					 * ���岽��Ϊ�� 1110 xxxx << 12 = xxxx 000000 000000 
					 *    ���磺        1110 0100 << 12 = 0100 000000 000000
					 */
					sChar = (char) (aByte[i] << 12);
					
					/**
					 * ��ǰ�����ֽ� ת��Ϊ xxxx xxxxxx 000000 ����ʽ
					 * ���岽��Ϊ�� 
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
					 * ���������ֽ�ת��Ϊ xxxx xxxxxx xxxxxx ����ʽ
					 * ���岽��Ϊ��
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
     * ��UTF-8�ֽ�����ת��Ϊunicode�ַ���
     * @param utf_data byte[] - UTF-8�����ֽ�����
     * @param len int - �ֽ����鳤��
     * @return String - �任���unicode�����ַ���
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
     * ��ָ����UTF-8�ֽ���ϳ�һ��unicode�����ַ�
     * @param utf byte[] - UTF-8�ֽ�����
     * @param sptr int - �����ֽ���ʼλ��
     * @param cntBits int - �����ֽ���
     * @return char - �任���unicode�ַ�
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
	 *  ���ݸ����ֽڼ���UTF-8�����һ���ַ���ռ�ֽ���,
	 *  UTF-8�����壬�ֽڱ��ֻ��Ϊ0��2~6
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
