package user.example.com.tozandatacollectapp.CameraRemote;

import java.util.*;
import java.io.*;
import java.util.regex.*;

public class XMLParser{

	//XML���^�O�ƒ��g���Ƃɕ���
	private String[] xmlArray;

	//�������̃C���f�b�N�X
	private int idx;

	//���݂̃^�O�̖��O
	private String tagName;
	//���݂̃^�O�̑���
	private HashMap<String, String> attrs;

	public static final int STATE_TAG_OPEN = 0, STATE_TAG_CLOSE = 1, STATE_TAG_SINGLE = 2, STATE_TEXT = 3;

	public static void main(String[] args){

		String src = "";

		try {
            //�t�@�C����ǂݍ���
            FileReader fr = new FileReader(".\\Sample.xml");
            BufferedReader br = new BufferedReader(fr);

			String line;
            int count = 0;
            while ((line = br.readLine()) != null) {
				src += line;
            }

            //�I������
            br.close();
			fr.close();

        } catch (IOException ex) {
            //��O����������
            ex.printStackTrace();
		}

		if(src.isEmpty()) return;

		XMLParser parser = new XMLParser(src);

		boolean loop = true;
		String endPointUrl = null;

		while(parser.hasNextData() && loop){
			int state = parser.nextData();
			if(state == XMLParser.STATE_TAG_OPEN){
				if(parser.getTagName().equals("av:X_ScalarWebAPI_ServiceType")){
					while(parser.nextData() != XMLParser.STATE_TEXT);
					String api = parser.getStr();
					if(true/*api.equals("camera")*/){
						while(parser.nextData() != XMLParser.STATE_TEXT);
						endPointUrl = parser.getStr() + "/" + api;
						System.out.println("endpoint:" + endPointUrl == null ? "null" : endPointUrl);
						//loop = false;
					}
				}
			}
		}

		//System.out.println("endpoint:" + endPointUrl == null ? "null" : endPointUrl);

	}

	public XMLParser(String XML){

		//>��<���Ƃɉ��s�A������̉��s����ɂ܂Ƃ߂�
		String str = XML.replaceAll("<", "\n<").replace(">", ">\n").replaceAll("\n+", "\n");
		//�J�ƌ�Ƃɕ������āA�z��Ɋi�[
		xmlArray = str.split("\\n");

		//for(String s : xmlArray) System.out.println(s);

		//�C���f�b�N�X��������
		idx = -1;

		//������������
		attrs = new HashMap<>();
	}

	public int nextData(){
		idx++;
		String str = xmlArray[idx];
		
		int state = getState(str);
		tagName = null;
		attrs.clear();
		if(state != STATE_TEXT){
			String temp = str.replaceAll("^<", "").replaceAll("/?>$", "").replaceAll("\\s+=\\s+", "=").trim();
			tagName = temp.replaceAll("\\s.*", "").trim();
			if(state == STATE_TAG_SINGLE || state == STATE_TAG_OPEN){
				for(String attr : temp.split("\\s+")){
					System.out.println("attr:[" + attr + "]");
					String[] data = attr.split("=");
					if(data.length >= 2)
						attrs.put(data[0], data[1].substring(1, data[1].length() - 1));
				}
			}
		}

		return state;
	}

	private int getState(String str){

		Pattern single = Pattern.compile("^<.+/>$");
		Pattern close = Pattern.compile("^</.+>$");
		Pattern open = Pattern.compile("^<.+>$");

		Pattern[] patterns = {single, close, open};
		int[] values = {STATE_TAG_SINGLE, STATE_TAG_CLOSE, STATE_TAG_OPEN};

		Matcher matcher;

		for(int i = 0; i < patterns.length; i++){
			matcher = patterns[i].matcher(str);
			if(matcher.find()) return values[i];
		}

		return STATE_TEXT;
	}

	public boolean hasNextData(){
		if(xmlArray == null || xmlArray.length == 0) return false;
		return (xmlArray.length - 1) > (idx + 1);
	}

	public String getStr(){
		return xmlArray[idx];
	}

	/**
	 * @return the tagName
	 */
	public String getTagName() {
		return tagName;
	}

	public Map<String, String> getAttributes(){
		return new HashMap<>(attrs);
	}

}