package test;



import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class TimeSeries {


	public static HashMap<String, ArrayList<Float>> _map;
	static String [] _features;


	public static HashMap<String, ArrayList<Float>> get_map() {
		return _map;
	}
	public static String[] get_features() {return _features;}

	public TimeSeries(String csvFileName) {
		File file = new File(csvFileName);
		try{
			Scanner inputStream = new Scanner(file);
			String c = inputStream.next();
			_features = c.split(",");
			String[] values = new String[0];
			_map = new HashMap<String, ArrayList<Float>>();
			for(int i = 0; i< _features.length; i++)
				_map.put(_features[i], new ArrayList<Float>());
			while(inputStream.hasNext()){
				String data = inputStream.next();
				values= data.split(",");
				for(int i = 0; i< _features.length; i++)
					_map.get(_features[i]).add(Float.parseFloat(values[i]));
			}
			inputStream.close();
		}catch (FileNotFoundException e){
			e.printStackTrace();
		}

	}

	public Point[] getPointsArr(String col1, String col2){
		Point[] points = new Point[_map.get(col1).size()];
		for (int i = 0; i< _map.get(col1).size(); i++)
			points[i]=new Point(_map.get(col1).get(i), _map.get(col2).get(i));
		return points;
	}

	public float[] colArr(String colName){
		float[] arr = new float[_map.get(colName).size()];
		int a = _map.get(colName).size();
		for (int i = 0; i< _map.get(colName).size(); i++) {
			arr[i] = (float) _map.get(colName).get(i);
		}
		return arr;
	}
}
