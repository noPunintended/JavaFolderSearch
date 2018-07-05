package th.co.msitb.dfs.service.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import java.util.List;

import com.ibm.lang.management.SysinfoCpuTime;

import th.co.msat.motor.config.SystemEnvironment;
import th.co.msitb.dfs.database.model.UploadPolicyModel;
import th.co.msitb.statement.model.AutomateSignatureVO;


public class DigiSignBatch {
	//	public static final String pathDes 				=  "/\\srv_data/Drive_X/DigiSign/Spool/Out";
	//	public static final String pathDes              =  "C:\\Users\\User\\Desktop\\AutoMatching";//general path of destinaton folder
		public static final String pathDes              =  "C:\\Users\\User\\Desktop";//general path of destinaton folder
		public static final String name                 =  "11-11-601012273";//name of the folder that need to be search
		public static final String DFS_MapTypeId 		=  "773";
		public static final String DFS_GroupId	 		=  "155";
		public static final String DFS_CatId	 		=  "68";
		public static final List<File>files             =  new ArrayList<File>();
		public static void main(String[] args) throws Exception  {
			
			findDir(pathDes, files,name);
		}
	
		private static void findDir(String directoryName, List<File> files, String name) {//This function use to find a folder in pathDes directory, that has the same name as a String "name". 
			OutputStream out = null;
			try {
				File directory = new File(directoryName);
				// Get all the files from a directory. Including Sub-folder
				File[] List = directory.listFiles();
				for (File file : List) {
					if (file.isFile()) {
						files.add(file);
						if(file.getName().equals(name)) {
							System.out.println(file.getAbsolutePath());
							// This meant that the folder is empty
							break;
						}
						
					} else if (file.isDirectory()) {
						findDir(file.getAbsolutePath(), files,name);
						if(file.getName().equals(name)) {
							System.out.println(file.getAbsolutePath());
							listDir(file.getAbsolutePath());
							break;
					}
				}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
	}	
 
	private static void listDir(String dir) {
		 OutputStream out = null;
			try {
			        File f = new File(dir);
			        File[] list = f.listFiles();
			        if (list == null) {
			            return;
		        	}
			        System.out.println("list size :"+list.length);
	        for (File file  : list) {
	            	String			policyFullNo = "" , docType = "";
					File 			streamFile	 		= new File(file.getAbsolutePath());
					 System.out.println("streamFile :"+streamFile);
					if(streamFile.exists()) {
						if(file.getName().split("_") != null && (file.getName().split("_")[0].equals("SH") || file.getName().split("_")[0].equals("DN"))){
							FileInputStream in   		 	= new FileInputStream(file.getAbsolutePath());
	        				policyFullNo 	= file.getName().split("_")[1];
	        				policyFullNo 	= policyFullNo.replaceAll("-","");
	        				 System.out.println("streamFileName :"+file.getName());
	        				 System.out.println("policyFullNo :"+policyFullNo);	        				
	        				 tranferDataToDFS(inputStreamToByteArray(in), policyFullNo, file.getName(), "BatchTest");
	        				 deleteFileAllDirectory(file.getAbsolutePath());
								}
							}		
			            }
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} catch (Exception e) {
					e.printStackTrace();
				}finally {
					try {
						if(out != null ){
							out.flush();
							out.close();
						}
					} catch (IOException e) {
						System.out.println(e.toString());
					}
				}
   			}
	private static byte[] inputStreamToByteArray(InputStream in) throws IOException{
	 	ByteArrayOutputStream out = new ByteArrayOutputStream();
	 	final int BUF_SIZE = 1 << 8; //1KiB buffer
	 	byte[] buffer = new byte[BUF_SIZE];
	 	int bytesRead = -1;
	 	
	 	while((bytesRead =in.read(buffer)) > -1) {
	 	      out.write(buffer, 0, bytesRead);
	 	}
	 	in.close();
	 	return out.toByteArray();
	}
	public static  void  deleteFileAllDirectory(String name){
		 	if( new File(name).delete()){ 
		 		System.out.println("DELETED :"+name);
		 	}
	}
	
	private static String  tranferDataToDFS(byte[] pdf,String policyFullNo , String fileName , String userLogin)throws Exception {
		 ArrayList 				modelList 	= new ArrayList();
		 UploadPolicyModel 		policyModel = new UploadPolicyModel(
				 	pdf,
					policyFullNo,
					"",
					"",
					"",
					"",
					"",
					"",
					"",
					fileName ,
					DFS_MapTypeId ,
					DFS_GroupId,
					DFS_CatId,
					"-" + System.currentTimeMillis(),
					userLogin, "0"
					);
			modelList.add(policyModel);
			byte[]  				streamFile 	= toByteArray(modelList);
			String 				 	a	 		= sendToDfsImpl(streamFile);
			System.out.println(a);
			return a ;
	}
	
	
	public static String sendToDfsImpl(byte[] data) throws Exception {
		String result = "";
		//URL url = new URL("http://it14:9080/DigitalFileSystem/UploadPolicy");
//		URL url=new URL( SystemEnvironment.getInstance().getDefaultHostName()+"DigitalFileSystem/UploadPolicy");
//		System.out.println("url :"+url);
//		URL url=new URL( "http://localhost:9080/DigitalFileSystem/UploadPolicy");
		URL url=new URL( "http://10.0.0.13/DigitalFileSystem/UploadPolicy");
		InputStream inputStream = null;
		BufferedReader reader = null;
		try { 

			URLConnection connection = url.openConnection();
			if (connection instanceof HttpURLConnection) {
				HttpURLConnection httpConnection = (HttpURLConnection) connection;
				httpConnection.setRequestMethod("PUT");
				httpConnection.setDoOutput(true);
				httpConnection.connect();

				/*
				 * Initialize output stream
				 */
				DataOutputStream outttt = new DataOutputStream(httpConnection.getOutputStream());

				/*
				 * write dataList
				 */
				outttt.write(data);
				outttt.flush();
				inputStream = httpConnection.getInputStream();
				// logger.info("inputStream=="+inputStream);
				reader = new BufferedReader(new InputStreamReader(inputStream));

				String line = null;

				while ((line = reader.readLine()) != null) {
					// logger.debug(line);
					result += line;
				}

			}
		} catch (MalformedURLException e1) {
		System.out.println(""+e1);
			
		} catch (IOException e) {
			System.out.println(""+e);
			
		} finally {
			try {
				inputStream.close();
				reader.close();
				
			} catch (IOException e) {
				System.out.println(""+e);
				
			}
		}
		return result;

	}

	public static  String  getpath(String name,String inOut) {
		String path = "";
		if(inOut != null && !"".equals(inOut)){
			if(inOut.equalsIgnoreCase("input")){
//				path = pathInputAutomate+name ;
				path = pathDes; // --- for test 
			}else if(inOut.equalsIgnoreCase("output")){
//				path = pathOutputAutomate+name ; 
				path = pathDes; // --- for test 
			}else{
//				path = pathErrorAutomate+name ; 
				path = pathDes; // --- for test 
			}
		}
		return path;
	}
	public static  AutomateSignatureVO findFile(String name,AutomateSignatureVO result)throws Exception{
		String 	path 	= getpath(name, "output");
		File 	f 		= null;
			if(!"".equals(path)){
				 f = new File(path);
				if(f.exists()) { 
					FileInputStream 	in 		= new FileInputStream(f);
										result.setPdf(inputStreamToByteArray(in));  
										result.setStatus("OK");
										result.setMessage("Complete");
										result.setFileName(name);
										
				}else{
					int k = 0 , u = 0 ;
					while(!f.exists() &&  k < 5){
						k++;
						Thread.sleep(5000);
						f = new File(path);
						System.out.println("CountDown output :"+(5-k));
					}
					if(f.exists()) { 
						FileInputStream 	in 		= new FileInputStream(f);
											result.setPdf(inputStreamToByteArray(in));  
											result.setStatus("OK");
											result.setMessage("Complete");
											result.setFileName(name);
					}else{
						path 	= getpath(name, "error");
						f 		= new File(path);
						while(!f.exists() &&  u < 5){
							u++;
							Thread.sleep(5000);
							f = new File(path);
							System.out.println("CountDown error :"+(5-u));
						}
						if(f.exists()) {
							result.setStatus("NOTOK");
							result.setMessage("Error PDF Can't Sign");
							result.setFileName(name);
							System.out.println("==== File Error Can't Sign =====");
						}else{
							result.setStatus("NOTOK");
							result.setMessage("Error File Not Found All Directory");
							result.setFileName(name);
							System.out.println("==== File Not Found All Directory =====");
						}
					}
				}
			}
		return result;
	}
	public static  byte[] toByteArray(Serializable o){
		ByteArrayOutputStream baos = null;
		try {
			baos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(baos);
			oos.writeObject(o);
			oos.flush();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return baos.toByteArray();

	}
}
