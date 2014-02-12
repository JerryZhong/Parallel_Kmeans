import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;  
import java.util.Vector; 


public class ClusterLoad 
{

	 private HashMap<Integer, Vector<Double> >  clusters; 
	 
	 public void initializecluster(File file,int clusternumber,int topicsize) 
	{  
		  clusters=new HashMap<Integer, Vector<Double> >();
		 
		 //not first load...
		 if(file.exists())
		 {
			 
			BufferedReader in=null;
			try
			{
				in=new BufferedReader(new InputStreamReader(new FileInputStream(file)));
				String line;
				while((line=in.readLine())!=null)
				{ 
					String[] strkey=line.split("\t");
					if(!strkey[0].trim().contains("loss")) 
					{
						Integer clusterid=Integer.parseInt(strkey[0])  ;
						String[] clusterdimension =strkey[1].split(" ");
						Vector<Double> ranlist=new Vector<Double>()    ;
						for(int i=0;i<topicsize;i++) 
							ranlist.add(Double.parseDouble(clusterdimension[i]));  
						clusters.put(clusterid,ranlist);
					} 
				}
				System.out.println("second load information...");
			}
			catch (Exception e) 
			{
				 System.out.println(e);
					// TODO: handle exception
			}
		 }
		 else//fist load, and initiate the init cluster points...
		 { 
			  for(int i=0;i<clusternumber;i++)
			 {
				Vector<Double> ranlist=new Vector<Double>(); 
				double sum=0.0;
				for(int j=0;j<topicsize;j++)
				{
					double value=Math.random();
					sum+=value;
					ranlist.add(value);
				}//for j end
				
				for(int j=0;j<topicsize;j++)
					ranlist.set(i,ranlist.get(j)/sum);
				clusters.put(i,ranlist);
			 }	//for i end 
			  
			  System.out.println("first load information..."); 
			 
		 }//else end 
	 
		 
	} 
	 
	 
	 static double EuclidDistance(Vector<Double> v1,Vector<Double> v2)
	 {
		 
		 double sum=0.0;
		 for(int i=0;i<v1.size();i++)
		 {
			 
			double tempvalue=Math.abs(v1.get(i)-v2.get(i));
			sum=sum+Math.pow(tempvalue,2); 
		 }
		 sum=Math.sqrt(sum);
		 return sum;
	 }
	 
	 public HashMap<Integer, Vector<Double> > getClusterPoints()
	 {
		 return clusters;
	 }
	 
	 
	 
	 
	 
	 

}
