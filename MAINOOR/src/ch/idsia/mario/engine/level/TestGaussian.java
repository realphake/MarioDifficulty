package ch.idsia.mario.engine.level;

import java.util.ArrayList;
import java.util.Arrays;
/*
import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.Covariance;*/

public class TestGaussian {
	
	public static double [][] addToArray(double [][] array, double [] element)
	{
		double [][] newarray = new double [array.length+1][array[0].length];
		
		for (int i = 0;i < array.length;i++)
		{	
			newarray[i] = array[i];
			
		}
			newarray[array.length] = element;
			return newarray;
			
	}
	
	public static double[] arrayMeans(double[][] array)
	{	
		double[] means = new double[array[0].length];
		for (int i = 0; i < array.length;i++)
		{
			for(int j = 0; j <array[i].length;j++ )
			{
				means[j] += array[i][j];
			}
		}
		for (int i = 0; i< means.length;i++)
		{
			means[i] = means[i]/array.length;
		}
		return means;
	}

	public static void main(String[] args)
    {
		  double [] means = {1,1,1};
		 
		  double [][]cov = 			{{9,3,1},
				 					{8,4,2},
		 							{9,3,1}};
		  double [][]cov3 = {
				  {4,2,0.6},
				  {4.2,2.1,0.59},
				  {3.9,2.0,0.58},
				  {4.3,2.1,0.62},
				  {4.1,2.2,0.63}
		  };
//		  double [][]covv = {{1,2,3}};
//		  double [][]covplus = addToArray(covv,means);
//		  covplus = addToArray(covplus,means);
//		  covplus = addToArray(covplus,means);
//		  covplus = addToArray(covplus,means);
//		 System.out.println(Arrays.toString(arrayMeans(covplus)));
//		 System.out.println(Arrays.deepToString(covplus));
//		  double [][] meansmeans = {means,means}; 
//		  Covariance cov2 = new Covariance(cov3,false);
//		  RealMatrix matrix = new Array2DRowRealMatrix(cov);
//			
//			//Covariance cov2 = new Covariance(cov);
//			System.out.println("aaaaaaaa");
//			System.out.println(Arrays.deepToString(cov2.getCovarianceMatrix().getData()));
//			double[][] cov2double = cov2.getCovarianceMatrix().getData();
//			cov2double[0][0] = 10;
//			cov2double[1][1] = 10;
//			cov2double[2][2] = 10;
//			
//			
//			System.out.println(Arrays.deepToString(matrix.getData()));
//		  MultivariateNormalDistribution MND = new MultivariateNormalDistribution(means,cov2double);//cov2.getCovarianceMatrix().transpose().getData());//cov2.getCovarianceMatrix().getData());
//		  
//		  System.out.println(Arrays.toString(MND.getStandardDeviations()));
//		  double[][] kaas = MND.sample(10);
//		  //double[][] haas =;
//		  
//		  System.out.println(Arrays.deepToString(kaas));//MND.getCovariances().getData()));
//		  System.out.println(cov2.getCovarianceMatrix().toString());
//		  
		  
		  ArrayList<double[]> valueArrayList = new ArrayList(0);//will contain the playvectors, no doubles
		  double [] p = {0.1, 0.2 ,0,3};
		  double [] d = {0.1, 0.2 ,0,3};
		  valueArrayList.add(p);
		  System.out.println(valueArrayList.contains(d));
		  
		  System.out.println(Arrays.equals(p, d));
    }
}
