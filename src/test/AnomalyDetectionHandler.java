package test;


import test.Commands.DefaultIO;
import test.Server.ClientHandler;

import java.io.*;
import java.util.Scanner;

public class AnomalyDetectionHandler implements ClientHandler{

	public void handle(InputStream inFromClient, OutputStream outToClient){
		SocketIO soc = new SocketIO(inFromClient,outToClient);
		CLI cli = new CLI(soc);
		cli.start();
		cli.dio.write("bye\n");
		soc.close();
	}


	public class SocketIO implements DefaultIO {

		BufferedReader in;
		PrintWriter out;
		public SocketIO(InputStream inputFileName,OutputStream outputFileName)  {
				in= new BufferedReader(new InputStreamReader(inputFileName));
				out= new PrintWriter(outputFileName,true);
		}

		@Override
		public String readText() {
			String s=null;
			try {
				 s =in.readLine();
			} catch (IOException e){
				e.printStackTrace();
			}
			return s;
		}

		@Override
		public void write(String text) {
			out.print(text);
			out.flush();
		}

		@Override
		public float readVal() {
			float a=0;
			return a;
		}

		@Override
		public void write(float val) {
			out.print(val);
			out.flush();
		}

		public void close() {
			try {
				in.close();
				out.close();
			} catch (IOException e){
				e.printStackTrace();
			}

		}

	}
}
