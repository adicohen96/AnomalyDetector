package test;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

public class Commands {
	
	// Default IO interface
	public interface DefaultIO{
		String readText();
		void write(String text);
		float readVal();
		void write(float val);
		default int UploadFile(String fileName,String stop){
			int rowsCounter=-1;
			try {
				String line = this.readText();
				PrintWriter out = new PrintWriter(new FileWriter(fileName));
				while (!line.equals(stop)){
					out.println(line);
					line = this.readText();
					rowsCounter++;
				}
				out.close();
				return rowsCounter;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return 0;
		}
	}
	
	// the default IO to be used in all commands
	DefaultIO dio;
	public Commands(DefaultIO dio) {
		this.dio=dio;
	}

	public class Exceptions{
		Long _exceptionStart;
		Long _exceptionEnd;
		String _features;

		public void set_exceptionStart(Long _exceptionStart) { this._exceptionStart = _exceptionStart;}
		public void set_exceptionEnd(Long _exceptionEnd) { this._exceptionEnd = _exceptionEnd;}
		public void set_features(String _features) {this._features = _features;}
	}
	// the shared state of all commands
	private class SharedState{

		float _threshold;
		long  _totalTimesteps;
		List<AnomalyReport> _reports;
		//List<String> _corFeatures;
		List<Exceptions> _reportsUnion = new ArrayList<>();

		SharedState(){
			this._threshold=(float)0.9;
			this._totalTimesteps=0;
			this._reports = new LinkedList<>();
			//this._corFeatures = new ArrayList<>();
			this._reportsUnion = new ArrayList<>();
		}
		public void set_threshold(float cor){ _threshold=cor;}
		public void set_totalTimesteps(int ts){ _totalTimesteps=ts;}
		public float get_threshold() { return _threshold;}
		public long get_totalTimesteps() { return _totalTimesteps;}
		public void set_reports(List<AnomalyReport> reports) { this._reports.addAll(reports);}
		public void set_reportsUnion(List<AnomalyReport> reports){
			int size = reports.size(),indx2 = 0;
			String features = reports.get(0).description;
			this._reportsUnion.add(new Exceptions());
			this._reportsUnion.get(0).set_exceptionStart(reports.get(0).timeStep);
			this._reportsUnion.get(0).set_features(features);
			//this._corFeatures.add(features);
			for(int i=0;i<size;i++){
				long endTime = reports.get(i).timeStep;
				while (i < size && reports.get(i).description.equals(features)) {
					endTime=reports.get(i).timeStep;
					i++;
				}
				this._reportsUnion.get(indx2++).set_exceptionEnd(endTime);
				if(i == size)
					break;
				features=reports.get(i).description;
				//this._corFeatures.add(features);
				this._reportsUnion.add(new Exceptions());
				this._reportsUnion.get(indx2).set_features(features);
				this._reportsUnion.get(indx2).set_exceptionStart(reports.get(i).timeStep);
			}
		}
		public List<AnomalyReport> get_reports() { return _reports;}
		//public List<String> get_corFeatures() { return _corFeatures;}
		public List<Exceptions> get_reportsUnion() { return _reportsUnion;}

	}
	private  SharedState sharedState=new SharedState();

	public abstract class Command{
		protected String description;
		
		public Command(String description) {
			this.description=description;
		}
		
		public abstract void execute();
	}

	public class Menu extends Command{

		public Menu() {
			super("Printing the menu");
		}

		@Override
		public void execute() {
			dio.write("Welcome to the Anomaly Detection Server.\n");
			dio.write("Please choose an option:\n");
			dio.write("1. upload a time series csv file\n");
			dio.write("2. algorithm settings\n");
			dio.write("3. detect anomalies\n");
			dio.write("4. display results\n");
			dio.write("5. upload anomalies and analyze results\n");
			dio.write("6. exit\n");
		}
	}

	public class UploadCSV extends Command{

		public UploadCSV() { super("upload a time series csv file"); }

		@Override
		public void execute() {
			dio.write("Please upload your local train CSV file.\n");
			dio.UploadFile("anomalyTrain.csv","done");  // upload the train csv file
			dio.write("Upload complete.\n");
			dio.write("Please upload your local test CSV file.\n");
			sharedState.set_totalTimesteps(dio.UploadFile("anomalyTest.csv","done")); // upload the test file and save number of time steps recorded
			dio.write("Upload complete.\n");
		}
	}

	public class AlgorithmSettings extends Command{

		public AlgorithmSettings() {
			super("algorithm settings");
		}

		@Override
		public void execute() {
			dio.write("The current correlation threshold is " + sharedState.get_threshold() + "\n");
			dio.write("Type a new threshold\n");
			String line = dio.readText();
			float choice = Float.parseFloat(line);
			while (choice > 1 || choice < 0){
				dio.write("please choose a value between 0 and 1.\n");
				dio.write("Type a new threshold\n");
				line = dio.readText();
				choice = Float.parseFloat(line);
			}
			sharedState.set_threshold(choice);
		}
	}

	public class DetectAnomalies extends Command{

		public DetectAnomalies() { super("detect anomalies");}

		@Override
		public void execute() {
			TimeSeries train = new TimeSeries("anomalyTrain.csv");
			SimpleAnomalyDetector ad = new SimpleAnomalyDetector(sharedState.get_threshold());
			ad.learnNormal(train);
			TimeSeries test=new TimeSeries("anomalyTest.csv");
			List<AnomalyReport> reports=ad.detect(test);
			sharedState.set_reportsUnion(reports);
			sharedState.set_reports(reports);
			dio.write("anomaly detection complete.\n");
		}

	}

	public class DisplayResults extends Command{

		public DisplayResults() {
			super("display results");
		}

		@Override
		public void execute() {
			for(AnomalyReport ar : sharedState.get_reports())
				dio.write(ar.timeStep + "\t" + ar.description + "\n");
			dio.write("Done.\n");
		}
	}

	public class UploadAndAnalyzeResults extends Command{

		public UploadAndAnalyzeResults() { super("upload anomalies and analyze results");}

		@Override
		public void execute() {
			if(sharedState.get_reports().size()==0)
				return;
			List<Exceptions> realExceptions = new ArrayList<>();
			dio.write("Please upload your local anomalies file.\n");
			UploadExceptionsToList(realExceptions);
			dio.write("Upload complete.\n");
			AlgorithmMeasurement(realExceptions);
		}
		public void UploadExceptionsToList(List<Exceptions> realExceptions){
			String line = dio.readText();
			int indx = 0 ;
			String[] timeStep;
			while (!line.equals("done")) {
				timeStep = line.split(",");
				realExceptions.add(new Exceptions());
				realExceptions.get(indx).set_exceptionStart(Long.parseLong(timeStep[0]));
				realExceptions.get(indx).set_exceptionEnd(Long.parseLong(timeStep[1]));
				line = dio.readText();
				indx++;
			}
		}
		public void AlgorithmMeasurement(List<Exceptions> realExceptions){
			long P=realExceptions.size(),N = sharedState.get_totalTimesteps(),a1,a2,b1,b2;
			float TP = 0, FP = 0;
			boolean flag =false;
			for(int i = 0 ; i < P ; i++)
				N = N - (realExceptions.get(i)._exceptionEnd - realExceptions.get(i)._exceptionStart + 1);
			for(int i = 0; i<sharedState.get_reportsUnion().size(); i++) {
				a2 = sharedState.get_reportsUnion().get(i)._exceptionEnd;
				a1 = sharedState.get_reportsUnion().get(i)._exceptionStart;
				for (int j = 0; j < realExceptions.size(); j++) {
					b2 = realExceptions.get(j)._exceptionEnd;
					b1 = realExceptions.get(j)._exceptionStart;
					if(Math.max(a2,b2) - Math.min(a1,b1) <= (a2-a1)+(b2-b1)) {
						TP++;
						flag = true;
						break;
					}
				}
				if(!flag) FP++;
				else flag = false;
			}
			dio.write("True Positive Rate: " + decimalPrint(TP/P) + "\n");
			dio.write("False Positive Rate: " + decimalPrint(FP/N) + "\n");
		}
		public float decimalPrint(float number){
			float x = number * 1000;
			int y = (int)x;
			x = (float)y / 1000;
			return x;
		}
	}

	public class Exit extends Command{

		public Exit() {
			super("exit");
		}

		@Override
		public void execute() {
			System.exit(1);

		}
	}
}

