package test;

import java.util.LinkedList;
import java.util.List;

public class SimpleAnomalyDetector implements TimeSeriesAnomalyDetector {

	List<CorrelatedFeatures> _corFeature;
	final float  _minCor;

	public SimpleAnomalyDetector() {
		this._corFeature = new LinkedList<>() ;
		this._minCor = (float) 0.9;
	}
	public SimpleAnomalyDetector( float cor) {
		this._corFeature = new LinkedList<>() ;
		this._minCor = cor;
	}


	@Override
	public void learnNormal(TimeSeries ts) {
		for(int i = 0; i<ts._map.size(); i++){
			float max=0,pear;
			String feture=null;
			for(int j = i+1; j<ts.get_map().size(); j++) {
				pear = Math.abs(StatLib.pearson(ts.colArr(ts.get_features()[i]),ts.colArr(ts.get_features()[j])));
				if(pear>max){
					max = pear;
					feture=ts.get_features()[j];
				}
			}
			if(max>this._minCor&& feture!=null) {
				float max2=0;
				float cur2;
				Line reg= StatLib.linear_reg(ts.getPointsArr(ts.get_features()[i],feture));
				Point[] points = ts.getPointsArr(ts.get_features()[i],feture);
				for (int k=0;k<points.length;k++) {
					cur2= StatLib.dev(points[k],reg);
					if(cur2>max2)
						max2=cur2;
				}
				max2*=1.1;
				CorrelatedFeatures corFeature = new CorrelatedFeatures(ts.get_features()[i],feture,max,reg,max2);
				this._corFeature.add(corFeature);
			}
		}
	}


	@Override
	public List<AnomalyReport> detect(TimeSeries ts) {
		LinkedList<AnomalyReport> lstD= new LinkedList<AnomalyReport>();
		for(int i = 0; i<this._corFeature.size(); i++){
			Point[] points= ts.getPointsArr(this._corFeature.get(i).feature1,this._corFeature.get(i).feature2);
			for(int k = 0; k<points.length;k++){
				Line l = this._corFeature.get(i).lin_reg;
				if(StatLib.dev(points[k],l)>this._corFeature.get(i).threshold)
				{
					AnomalyReport rep = new AnomalyReport(this._corFeature.get(i).feature1+"-"+this._corFeature.get(i).feature2,k+1);
					lstD.add(rep);
				}
			}
		}
		return lstD;
	}
	
	public List<CorrelatedFeatures> getNormalModel(){
		return this._corFeature;
	}
}
