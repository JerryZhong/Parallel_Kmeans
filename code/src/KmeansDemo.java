import java.io.File; 
import java.util.Vector; 
import java.util.HashMap; 
import java.io.IOException;  
import org.apache.hadoop.io.Text; 
import org.apache.hadoop.fs.Path; 
import org.apache.hadoop.util.Tool;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.conf.Configured; 
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.util.ToolRunner; 
import org.apache.hadoop.mapreduce.Mapper;  
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Reducer.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

 

public class KmeansDemo extends Configured implements Tool
{
	 
	 public static class Map extends Mapper<LongWritable, Text, LongWritable, Text> 
	{   
		private ClusterLoad cLoad; 
		private HashMap<Integer,Vector<Double> >  	  clusters;  
		private Vector< Vector<Double> > 			 sumpoints;//key:cluster-point  value:[0--count][1-200--topicvalue] 
		
	 	static int clusternumber=0;
	 	static int topicnumber  =0;
		
		 protected void setup(Context context) throws IOException, InterruptedException 
		{ 
			 //super.setup(context);
			 clusternumber=   Integer.parseInt(context.getConfiguration().get("clusternumber"));
			 topicnumber  =   Integer.parseInt(context.getConfiguration().get("topicnumber")); 
			try 
			{
				cLoad=new ClusterLoad();  
				File file=new File("clusterpoints");
				cLoad.initializecluster(file,clusternumber,topicnumber);
				clusters=cLoad.getClusterPoints();  
				
				
				//initiate the parameters...
				sumpoints=new Vector<Vector<Double>>();
				for(int i=0;i<clusternumber;i++)
				{
					Vector<Double> temp=new Vector<Double>();
					for(int j=0;j<=(topicnumber+1);j++)		    //attention:apply for (topicnumber+2) free space 。
						temp.add(0.0);						   	//the last but one free space is used to count the cluser-k number
																//and the last space is used to sum the total cluster-k loss value.
					sumpoints.add(i,temp);
				} 				
			}
			catch (Exception e)
			{
				// TODO: handle exception
			}
		} 
		
		
	 
		 public void map(LongWritable key,Text value,Context context) throws IOException, InterruptedException
		{							
			double minvalue=999999;
			int clustertype=0;
			String[] fields = value.toString().split(" "); 
			 
			
			int length=fields.length ;
			String	mediaid=fields[0];
			Vector<Double> pointinfos = new Vector<Double>();
			for(int i=1;i<length;i++)
				pointinfos.add(Double.parseDouble(fields[i]));
			
			 for(int i=0;i<clusternumber;i++)
			{
				double sumx=cLoad.EuclidDistance(clusters.get(i),pointinfos); 
				if(sumx<minvalue)
				{
					minvalue=sumx;
					clustertype=i;
				}
			} 
			 
			Vector<Double> temp=sumpoints.get(clustertype);
			for(int i=0;i<topicnumber;i++)
			{
				temp.set(i,temp.get(i)+pointinfos.get(i));
			}
			temp.set(topicnumber,  temp.get(topicnumber)+1);   	 		 // clustertype=i , then count=count+1
			temp.set(topicnumber+1,temp.get(topicnumber+1)+minvalue );   // clustertype=i , then count=count+1
			sumpoints.set(clustertype,temp);  
		}


		@Override
		//cleanup:get "map" final result through the container of "sumpoints". 
		protected void cleanup(Context context) throws IOException,InterruptedException 
		{
			for(int i=0;i<clusternumber;i++)
			{
				String nameString="";
				Vector<Double> x=sumpoints.get(i);
				for(int j=0;j<=(topicnumber+1);j++)
					nameString+=","+x.get(j).toString();
				nameString=nameString.substring(1);   
				context.write(new LongWritable(i), new Text(nameString)); 
			}
			super.cleanup(context);
		}  
		
	}
	
	public static class Reduce extends Reducer<LongWritable, Text, Text, Text>
	{ 
		private final Text  reducekey   = new Text(); 
		private final Text  reducevalue = new Text();
		private ClusterLoad cLoad; 
		static int topicnumber=0 ;
		static double totalloss=0.0; 
		
		
		@Override
		protected void setup(Context context) throws IOException,InterruptedException 
		{
			// TODO Auto-generated method stub
			super.setup(context); 
			 
			
			topicnumber  =  Integer.parseInt(context.getConfiguration().get("topicnumber"));
			
		}

		public void reduce(LongWritable key, Iterable<Text> values,Context context) throws IOException, InterruptedException 
		{
			LongWritable clusterid =key;
			double[] temparray=new double[topicnumber+2]; 
			for(int i=0;i<=(topicnumber+1);i++)
				temparray[i]=0.0; 
			 
		 	
			for (Text value :values)
			{
				String[] valuestring=value.toString().split(",");
				for(int i=0;i<=(topicnumber+1);i++) 
					temparray[i]+=Double.parseDouble(valuestring[i].toString()); 
			}
			
			for(int i=0;i<topicnumber;i++)
			{
				if(temparray[topicnumber]!=0)
					temparray[i]=temparray[i]*1.0/temparray[topicnumber];
			}
			
			totalloss += temparray[topicnumber+1];


			StringBuilder sb=new StringBuilder();
			for(int j=0;j<topicnumber;j++)
				sb.append(temparray[j]+" "); 
			
			reducekey.set(key.toString());
			reducevalue.set(sb.toString());
			context.write(reducekey, reducevalue);
		}

		@Override
		protected void cleanup(Context context) throws IOException, InterruptedException 
		{
			// TODO Auto-generated method stub
			super.cleanup(context);
			context.write(new Text("loss"), new Text( String.valueOf(totalloss) )   );
		} 
		 
	}
	
	
	public int run(String[] args) throws Exception
	{
		Configuration conf = getConf(); 
	//	conf.set("topicnumber",  args[0]);
	//	conf.set("clusternumber",args[1]);
		
		conf.set("topicnumber", "5");
		conf.set("clusternumber","4");
		
		Job job = new Job(conf,"kmeancluster");
		job.setJarByClass(KmeansDemo.class);
		
		 
	 	FileInputFormat.addInputPath(job, 	new Path("input")) ; 
		FileOutputFormat.setOutputPath(job, new Path("output"));
		
///		FileInputFormat.addInputPath  (job, new Path(args[2]));
//		FileOutputFormat.setOutputPath(job, new Path(args[3])); 
		
		
		job.setMapperClass (Map.class);
	 	job.setReducerClass(Reduce.class); 
		job.setNumReduceTasks(1);
		job.setMapOutputKeyClass(LongWritable.class);
		job.setMapOutputValueClass(Text.class);
		
		job.setOutputKeyClass(Text.class);
		job.setOutputValueClass(Text.class);
		
		System.exit(job.waitForCompletion(true) ? 0 : 1);
		return 0;
	} 
	
	
	/**
	* @param args
	 * @throws Exception 
	*/
	public static void main(String[] args) throws Exception 
	{
		int res = ToolRunner.run(new Configuration(), new KmeansDemo(),args);
		System.out.println(res);
	}

	
}
