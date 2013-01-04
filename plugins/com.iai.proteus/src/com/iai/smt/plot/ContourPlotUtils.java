package com.iai.smt.plot;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.math3.analysis.interpolation.SplineInterpolator;
import org.apache.commons.math3.analysis.polynomials.PolynomialSplineFunction;
import org.apache.log4j.Logger;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYZDataset;

import com.iai.proteus.common.LatLon;
import com.iai.proteus.common.Util;
import com.iai.proteus.common.sos.data.DSVData;
import com.iai.proteus.common.sos.data.Field;
import com.iai.proteus.common.sos.data.SensorData;

/**
 * 
 * @author Jakob Henriksson 
 *
 */
public class ContourPlotUtils {
	
	private static final Logger log = Logger.getLogger(ContourPlotUtils.class);
	
	/**
	 * 
	 * @param sensorData
	 * @param description
	 * @param title
	 * @param xVar
	 * @param yVar
	 * @param zVar
	 * @return
	 */
	public static XYZDataset 
		generateContourPlot(SensorData sensorData, 
				String description, String title, 
				Field xVar, Field yVar, Field zVar) 
	{
		
		if (sensorData instanceof DSVData) {
			
			DSVData dsv = (DSVData) sensorData;
			
			List<Field> fields = dsv.getFields();

			// timestamp
			int idxX = dsv.getFieldIndex(xVar, fields);
			// Y 
			int idxY = dsv.getFieldIndex(yVar, fields);
			// Z
			int idxZ = dsv.getFieldIndex(zVar, fields);
			
			return generateXYZDataset(sensorData, idxX, idxY, idxZ); 
		}
		
		return null;
	}	
	
	/**
	 * 
	 * 
	 * @param sensorData
	 * @param idxX
	 * @param idxY
	 * @param idxZ
	 * @return
	 */
	public static XYZDataset generateXYZDataset(SensorData sensorData, 
			final int idxX, final int idxY, final int idxZ) 
	{
		List<String[]> rawData = sensorData.getData();
		// add distance information to the data set 
		// TODO: should not hard code the lat and long indexes 
		final List<String[]> data = addDistance(rawData, 2, 3);
		
//		System.out.println("!!");
//		for (String[] row : data) {
//			System.out.println(Util.join(row, ","));
//		}
//		System.out.println("!!");
		
		if (data != null) {
			
			final int idxDistance = data.get(0).length - 1;

			/*
			 * X = timestamp
			 * Y = depth
			 * Z = salinity  
			 */
			return new XYZDataset() {

				@Override
				public int getSeriesCount() {
					return 1;
				}
				
				@Override
				public int getItemCount(int series) {
					return data.size();
				}
				
				@Override
				public Number getX(int series, int item) {
					return new Double(getXValue(series, item));
				}
				
				@Override
				public double getXValue(int series, int item) {
					String[] row = data.get(item);
					return Double.parseDouble(row[idxDistance]);
				}
				
				@Override
				public Number getY(int series, int item) {
					return new Double(getYValue(series, item));
				}
				
				@Override
				public double getYValue(int series, int item) {
					String[] row = data.get(item);
					return Double.parseDouble(row[idxY]);
				}
				
				@Override
				public Number getZ(int series, int item) {
					return new Double(getZValue(series, item));
				}
				
				@Override
				public double getZValue(int series, int item) {
					String[] row = data.get(item);
					return Double.parseDouble(row[idxZ]);   
				}
				
				@Override
				public void addChangeListener(DatasetChangeListener listener) {
					// ignore
				}
				
				@Override
				public void removeChangeListener(DatasetChangeListener listener) {
					// ignore
				}
				
				@Override
				public DatasetGroup getGroup() {
					return null;
				}
				
				@Override
				public void setGroup(DatasetGroup group) {
					// ignore
				}
				
				@Override
				public Comparable getSeriesKey(int series) {
					return "";
				}
				
				@Override
				public int indexOf(Comparable seriesKey) {
					return 0;
				}
				
				@Override
				public DomainOrder getDomainOrder() {
					return DomainOrder.ASCENDING;
				}
			};
		} 

		return null;		
		
//		return new XYZDataset() {
//
//			public int getSeriesCount() {
//				return 1;
//			}
//			
//			public int getItemCount(int series) {
//				return data.size();
//			}
//			
//			public Number getX(int series, int item) {
//				return new Double(getXValue(series, item));
//			}
//			
//			public double getXValue(int series, int item) {
//				String[] row = data.get(item);
//				Date date = TimeUtils.parseDefault(row[idxX], false);
//				return dateMap.get(date);
//			}
//			
//			public Number getY(int series, int item) {
//				return new Double(getYValue(series, item));
//			}
//			
//			public double getYValue(int series, int item) {
//				String[] row = data.get(item);
//				return Double.parseDouble(row[idxY]);
//			}
//			
//			public Number getZ(int series, int item) {
//				return new Double(getZValue(series, item));
//			}
//			
//			public double getZValue(int series, int item) {
//				String[] row = data.get(item);
//				return Double.parseDouble(row[idxZ]);   
//			}
//			
//			public void addChangeListener(DatasetChangeListener listener) {
//			}
//			
//			public void removeChangeListener(DatasetChangeListener listener) {
//			}
//			
//			public DatasetGroup getGroup() {
//				return null;
//			}
//			
//			public void setGroup(DatasetGroup group) {
//			}
//			
//			public Comparable getSeriesKey(int series) {
//				return "";
//			}
//			
//			public int indexOf(Comparable seriesKey) {
//				return 0;
//			}
//			
//			public DomainOrder getDomainOrder() {
//				return DomainOrder.ASCENDING;
//			}
//		};		
		
	}	
	
	/**
	 * Add a distance column to a set of data 
     * 
     * @param data
     * @param idxLat The column index for latitude data
     * @param idxLon The column index for longitude data 
     * @return
     */
    public static List<String[]> addDistance(List<String[]> data, 
    		int idxLat, int idxLon) 
    {
    	
    	List<String[]> newData = new ArrayList<String[]>();
    	
    	LatLon lastPos = null, curPos = null;
    	
    	int total = 0; 
    	
    	for (int j = 0; j < data.size(); j++) {
    		String[] row = data.get(j);
    		
    		String[] newRow = new String[row.length + 1]; 
    		
    		// copy old data
    		for (int i = 0; i < row.length; i++) {
    			newRow[i] = row[i];
    		}
    		
    		int idxNewColumn = row.length;
    		
    		double lat = Double.parseDouble(row[idxLat]); 
    		double lon = Double.parseDouble(row[idxLon]);
    		
    		// copy the last position if there is one 
    		if (curPos != null) {
    			lastPos = new LatLon(curPos); 
    		} else {
    			lastPos = new LatLon(lat, lon); 
    		}
    		
    		// update the current position based on the data 
    		curPos = new LatLon(lat, lon);
    		
    		// update position 
    		if (!lastPos.equals(curPos)) {
    			int len = (int)Math.ceil(greatCircle(lastPos, curPos));
    			total += len;
//    			System.out.println("Len: " + len);
    		}

    		newRow[idxNewColumn] = "" + total;
    		
    		newData.add(newRow);
    	}
    	
    	return newData; 
    }
    
    /**
     * Returns the x-values from the given dataset 
     * 
     * @param dataset
     * @param series
     * @return
     */
    private static Double[] getXs(XYZDataset dataset, int series) {
    	
    	List<Double> xs = new ArrayList<Double>();
    	
    	for (int i = 0; i < dataset.getItemCount(series); i++) {
    		Double x = dataset.getXValue(series, i);
    		if (!xs.contains(x))
    			xs.add(x);
    	}
    	
    	return xs.toArray(new Double[xs.size()]); 
    }

    /**
     * Returns the y-values from the given data set, given an x value 
     * 
     * @param dataset
     * @param series
     * @param x
     * @return
     */
    private static Double[] getYs(XYZDataset dataset, int series, double x) {
    	
    	List<Double> ys = new ArrayList<Double>();
    	
    	for (int i = 0; i < dataset.getItemCount(series); i++) {
    		Double xx = dataset.getXValue(series, i);
    		// check that the X value is matching 
    		if (xx.doubleValue() == x) {
    			ys.add(dataset.getYValue(series, i));
    		}
    	}
    	
    	return ys.toArray(new Double[ys.size()]); 
    }
    
    /**
     * Returns the z-value from the data set given an x and y value, 
     * null if there is no such value 
     * 
     * @param dataset
     * @param series
     * @param x
     * @param y
     * @return
     */
    private static Double getZ(XYZDataset dataset, int series, double x, double y) {
    	
    	for (int i = 0; i < dataset.getItemCount(series); i++) {
    		Double xx = dataset.getXValue(series, i);
    		// check that the X value is matching 
    		if (xx.doubleValue() == x) {
    			Double yy = dataset.getYValue(series, i);
    			if (yy.doubleValue() == y) {
    				return dataset.getZValue(series, i); 
    			}
    		}
    	}
    	
    	// if not found 
    	return null;
    } 
    
    /**
     * Returns all the z-values from the given dataset, given an x value
     * and a set of y-values  
     * 
     * @param dataset
     * @param series
     * @param x
     * @param ys
     * @return
     */
    private static Double[] getZs(XYZDataset dataset, int series, double x, Double[] ys) {

    	List<Double> zs = new ArrayList<Double>();
    	
    	for (int i = 0; i < dataset.getItemCount(series); i++) {
    		Double xx = dataset.getXValue(series, i);
    		// check that the X value is matching 
    		if (xx.doubleValue() == x) {
    			// get the Y value for the same item 
    			Double yy = dataset.getYValue(series, i);
    			// go through the Y's we're interested in
    			for (double y : ys) {
    				if (yy.doubleValue() == y)
    					// X matches, and there is a Y we want, get Z 
    					zs.add(dataset.getZValue(series, i));
    			}
    		}
    	}

    	return zs.toArray(new Double[zs.size()]); 
    }

    /**
     * Finds domain values (x-axis) corresponding to the given y value 
     * 
     * @param dataset
     * @param series
     * @param y
     * @return
     */
    private static Double[] findDomainValues(XYZDataset dataset, int series, double y) {
    	List<Double> xs = new ArrayList<Double>();
    	for (int i = 0; i < dataset.getItemCount(series); i++) {
    		Double yy = dataset.getYValue(series, i);
    		if (yy.doubleValue() == y) {
    			xs.add(dataset.getXValue(series, i));
    		}
    	}
    	return xs.toArray(new Double[xs.size()]);
    }
    
    /**
     * 
     * @param dataset
     * @return
     */
    public static XYZDataset interpolateRangeAxis(XYZDataset dataset) {
    	
    	int series = 1;

    	final List<Item> items = new ArrayList<Item>();

    	final Double[] xs = getXs(dataset, series);
    	
    	for (Double x : xs) {
    		
    		Double[] ys = getYs(dataset, series, x);
    		System.out.println("Ys (" + ys.length + "): " + 
    				Util.join(ys, ","));
//    		Arrays.sort(ys);
    		Double[] zs = getZs(dataset, series, x, ys);
    		System.out.println("Zs (" + zs.length + "): " +
    				Util.join(zs, ","));
    		
    		try {
    		
    			SplineInterpolator inter = new SplineInterpolator();
    			PolynomialSplineFunction fn = 
    					inter.interpolate(prim(ys), prim(zs));

    			int min = ys[0].intValue();
    			int max = ys[ys.length - 1].intValue();

    			for (int y = min; y < max; y++) {
    				Item item = new Item(x, y, fn.value(y));
    				items.add(item);
    			}
    			
    		} catch (org.apache.commons.math3.exception.NumberIsTooSmallException e) {
    			log.error("Interpolation exception (range; Number too small): " + 
    					e.getMessage());
    		} catch (org.apache.commons.math3.exception.DimensionMismatchException e) {
    			log.error("Interpolation exception (range; Dimension mismatch): " + 
    					e.getMessage());
    		} catch (org.apache.commons.math3.exception.NonMonotonicSequenceException e) {
    			log.error("Interpolation exception (range; Nonmonotonic sequence): " + 
    					e.getMessage());
    		}
    	}
    	
    	return constructDataset(items);
    }
    
    /**
     * 
     * 
     * @param dataset
     * @return
     */
    public static XYZDataset interpolateDomainAxis(XYZDataset dataset) {
    	
    	int series = 1;

    	final List<Item> items = new ArrayList<Item>();
    	
    	for (int y = 0; y < 500; y++) {
    		
    		Double[] xs = findDomainValues(dataset, series, y);
    		
    		if (xs.length <= 0) {
    			continue;
    		}
    		
    		List<Double> zs = new ArrayList<Double>();
    		for (Double d : xs) {
    			Double z = getZ(dataset, series, d, y);
    			if (z != null) {
    				zs.add(z);
    			}
    		}
    		
    		try {
    			
    			SplineInterpolator inter = new SplineInterpolator();
    			PolynomialSplineFunction fn = 
    					inter.interpolate(prim(xs), 
    							prim(zs.toArray(new Double[0])));

    			int min = xs[0].intValue();
    			int max = xs[xs.length - 1].intValue();

    			for (int d = min; d < max; d++) {
    				Item item = new Item(d, y, fn.value(d)); 
    				items.add(item);
    			}
    			
    		} catch (org.apache.commons.math3.exception.NumberIsTooSmallException e) {
    			log.error("Interpolation exception (domain; Number too small): " + 
    					e.getMessage());
    		} catch (org.apache.commons.math3.exception.DimensionMismatchException e) {
    			log.error("Interpolation exception (domain; Dimension mismatch): " + 
    					e.getMessage());
    		} catch (org.apache.commons.math3.exception.NonMonotonicSequenceException e) {
    			log.error("Interpolation exception (domain; Nonmonotonic sequence): " + 
    					e.getMessage());
    		}
    	}

    	return constructDataset(items);
    }    
    
    /**
     * Convenience class
     *  
     * @author Jakob Henriksson 
     *
     */
    public static class Item {
    	public double x;
    	public double y; 
    	public double z; 
    	public Item(double x, double y, double z) {
    		this.x = x;
    		this.y = y;
    		this.z = z; 
    	}
    }  
    
    /**
     * Constructs a data set from the given list of items
     * 
     * @param items
     * @return
     */
    private static XYZDataset constructDataset(final List<Item> items) {
    	return new XYZDataset() {
			
			@Override
			public int getSeriesCount() {
				return 1; 
			}
			
			@Override
			public int getItemCount(int arg0) {
				return items.size();
			}			
			
			@Override
			public double getYValue(int arg0, int arg1) {
				return items.get(arg1).y;
//				return ys[arg1];
			}
			
			@Override
			public Number getY(int arg0, int arg1) {
				return getYValue(arg0, arg1);
			}
			
			@Override
			public double getXValue(int arg0, int arg1) {
				return items.get(arg1).x;
			}
			
			@Override
			public Number getX(int arg0, int arg1) {
				return getXValue(arg0, arg1);
			}
			
			@Override
			public DomainOrder getDomainOrder() {
				return DomainOrder.ASCENDING;
			}
			
			@Override
			public double getZValue(int arg0, int arg1) {
				// TODO Auto-generated method stub
				return items.get(arg1).z;
			}
			
			@Override
			public Number getZ(int arg0, int arg1) {
				return getZValue(arg0, arg1);
			}
			
			@Override
			public int indexOf(Comparable arg0) {
				return 0;
			}
			
			@Override
			public Comparable getSeriesKey(int arg0) {
				return "";
			}
			
			@Override
			public void setGroup(DatasetGroup arg0) {
			}
			
			@Override
			public DatasetGroup getGroup() {
				return null;
			}
						
			@Override
			public void removeChangeListener(DatasetChangeListener arg0) {
			}
			
			@Override
			public void addChangeListener(DatasetChangeListener arg0) {
			}			
		};    	
    } 
    
    /**
     * Generates a color scale for a contour plot 
     * 
     * @param dataset
     * @param series 
     * @param colorStart
     * @param colorEnd 
     * @param step
     * @return
     */
    public static LookupPaintScale generateScale(XYZDataset dataset, 
    		int series, Color colorStart, Color colorEnd, double step) 
    {
    	
    	// find the min and max values for the scale 
    	double min = 0, max = 0;
    	boolean minSet = false, maxSet = false;
    	for (int i = 0; i < dataset.getItemCount(1); i++) {
    		double z = dataset.getZValue(series, i); 
    		if (!minSet || z < min) {
    			min = z;
    			minSet = true; 
    		}
    		if (!maxSet || z > max) {
    			max = z;
    			maxSet = true;
    		}
    	}
    	
    	double bottom = Math.floor(min);
    	double top = Math.ceil(max);    	

    	// create the scale 
    	LookupPaintScale ps = 
    			new LookupPaintScale(bottom, top, Color.lightGray);

    	// create the colors for the scale 
    	List<Color> colors = new ArrayList<Color>();
    	
    	double steps  = (top - bottom) / step;
//    	for (int i = 0; i < steps + step; i++) {
//    		float ratio = (float) i / (float) steps;
//    		int red = (int) (colorEnd.getRed() * ratio + colorStart.getRed() * (1 - ratio));
//    		int green = (int) (colorEnd.getGreen() * ratio + colorStart.getGreen() * (1 - ratio));
//    		int blue = (int) (colorEnd.getBlue() * ratio + colorStart.getBlue() * (1 - ratio));
//    		Color stepColor = new Color(red, green, blue);
//    		colors.add(stepColor);
//    	}
    	
        for (int i = 0; i < steps + step; i++) {
        	int val = Spectrum.RedToGreen.getColor((double)i / steps);
        	Color color = new Color(val);
        	colors.add(color);
        }    	

    	// add the colors to the scale 
    	int idx = 0; 
    	for (double d = bottom; d <= top; d += 0.5) {
    		ps.add(d, colors.get(idx++));
    	} 
    	
    	return ps;
    }
    
	/**
	 * Calculates the great circle distance between to lat-longs 
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return
	 */
	private static double greatCircle(double lat1, double lon1, 
			double lat2, double lon2) 
	{
		int R = 6371; // Radius of the earth in km
		
		double dLat = Math.toRadians(lat2-lat1);  
		double dLon = Math.toRadians(lon2-lon1);
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
				Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * 
				Math.sin(dLon/2) * Math.sin(dLon/2); 
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
		double d = R * c; // Distance in km
		return d; 
	}   
	
	/**
	 * Calculates the great circle distance between to lat-longs 
	 * 
	 * @param lat1
	 * @param lon1
	 * @param lat2
	 * @param lon2
	 * @return
	 */
	public static double greatCircle(LatLon pos1, LatLon pos2) { 
		int R = 6371; // Radius of the earth in km
		double dLat = Math.toRadians(pos2.getLat()-pos1.getLat());  
		double dLon = Math.toRadians(pos2.getLon()-pos1.getLon());
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
				Math.cos(Math.toRadians(pos1.getLat())) * 
				Math.cos(Math.toRadians(pos2.getLat())) * 
				Math.sin(dLon/2) * Math.sin(dLon/2); 
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
		double d = R * c; // Distance in km
		return d; 
	}   	
	
	/**
	 * Converts a Double array to a primitive double array 
	 * 
	 * @param values
	 * @return
	 */
	private static double[] prim(Double[] values) {
		double[] res = new double[values.length];
		for (int i = 0; i < values.length; i++) {
			res[i] = values[i].doubleValue();
		}
		return res;
	}	
	
}
