package org.fog.entities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Random;

import org.apache.commons.math3.util.Pair;
import org.cloudbus.cloudsim.Cloudlet;
import org.cloudbus.cloudsim.Host;
import org.cloudbus.cloudsim.Log;
import org.cloudbus.cloudsim.Pe;
import org.cloudbus.cloudsim.Storage;
import org.cloudbus.cloudsim.Vm;
import org.cloudbus.cloudsim.VmAllocationPolicy;
import org.cloudbus.cloudsim.core.CloudSim;
import org.cloudbus.cloudsim.core.CloudSimTags;
import org.cloudbus.cloudsim.core.SimEvent;
import org.cloudbus.cloudsim.power.PowerDatacenter;
import org.cloudbus.cloudsim.power.PowerHost;
import org.cloudbus.cloudsim.power.models.PowerModel;
import org.cloudbus.cloudsim.provisioners.RamProvisionerSimple;
import org.cloudbus.cloudsim.sdn.overbooking.BwProvisionerOverbooking;
import org.cloudbus.cloudsim.sdn.overbooking.PeProvisionerOverbooking;
import org.fog.application.AppEdge;
import org.fog.application.AppLoop;
import org.fog.application.AppModule;
import org.fog.application.Application;
import org.fog.policy.AppModuleAllocationPolicy;
import org.fog.scheduler.StreamOperatorScheduler;
import org.fog.utils.Config;
import org.fog.utils.FogEvents;
import org.fog.utils.FogUtils;
import org.fog.utils.Logger;
import org.fog.utils.ModuleLaunchConfig;
import org.fog.utils.NetworkUsageMonitor;
import org.fog.utils.TimeKeeper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
/*import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.xmlbeans.XmlRuntimeException;*/


public class FogDevice extends PowerDatacenter {
	protected Queue<Tuple> northTupleQueue;
	protected Queue<Pair<Tuple, Integer>> southTupleQueue;
	
	protected List<String> activeApplications;
	
	protected Map<String, Application> applicationMap;
	protected Map<String, List<String>> appToModulesMap;
	protected Map<Integer, Double> childToLatencyMap;
 
	
	protected Map<Integer, Integer> cloudTrafficMap;
	
	protected double lockTime;
	
	/**	
	 * ID of the parent Fog Device
	 */
	protected int parentId;
	
	/**
	 * ID of the Controller
	 */
	protected int controllerId;
	/**
	 * IDs of the children Fog devices
	 */
	protected List<Integer> childrenIds;

	protected Map<Integer, List<String>> childToOperatorsMap;
	
	/**
	 * Flag denoting whether the link southwards from this FogDevice is busy
	 */
	protected boolean isSouthLinkBusy;
	
	/**
	 * Flag denoting whether the link northwards from this FogDevice is busy
	 */
	protected boolean isNorthLinkBusy;
	
	protected double uplinkBandwidth;
	protected double downlinkBandwidth;
	protected double uplinkLatency;
	protected List<Pair<Integer, Double>> associatedActuatorIds;
	
	protected double energyConsumption;
	protected double lastUtilizationUpdateTime;
	protected double lastUtilization;
	private int level;
	
	protected double ratePerMips;
	
	protected double totalCost;
	protected static int n;
	
	protected int count;
	
	
	static protected List<Double> users_x_temporal;
	
	static protected List<Boolean> flag;
	protected double pop_mean;

	protected double size;
	protected double cyc;
	protected double fog_cyc;
	protected double x;
	protected double threshold;
	protected static int Max_itr;
	static protected double average_x;
	static protected double average_partial_offloading_delay;
	static protected double average_local_delay;
	static protected double average_full_offloading_delay;
	static protected double average_partial_offloading_energy;
	static protected double average_local_energy;
	static protected double average_partial_offloading_payoff;
	static protected double average_local_payoff;
	
	protected double total_local_delay;
	protected double partial_local_delay;
	protected double total_offloading_delay;
	protected double partial_offloading_delay;
	protected double total_transfer_delay;
	protected double partial_transfer_delay;
	protected double total_real_offloading_delay;
	protected double total_real_local_delay;
	
	protected double total_local_energyConsumption;
	protected double partial_local_energyConsumption;
	protected double total_offloading_energyConsumption;
	protected double partial_offloading_energyConsumption;
	protected double total_transfer_energyConsumption;
	protected double partial_transfer_energyConsumption;
	protected double total_real_local_energyConsumption;
	protected double partial_real_transfer_energyConsumption;
	protected double partial_real_local_energyConsumption;
	
	protected double device_powerConsumption;
	protected double transfer_powerConsumption;
	
	protected double bw;
	
	static protected double ratePerCycle;
	
	protected double total_penalty;
	protected double partial_penalty;
	
	protected double partial_local_outcome;
	protected double partial_offloading_outcome;
	protected double real_partial_offloading_outcome;
	
	protected double total_local_outcome;
	protected double total_offloading_outcome;
	
	protected double alpha;
	protected double beta;
	protected double omega;
	
	
	protected double partial_local_payoff;
	protected double partial_offloading_payoff;
	protected double total_local_payoff;
	protected double total_offloading_payoff;
	
	protected double user_mean;
	protected int decision_n;
	protected static String population="e://sim_excel/population";
	
	static protected List<Double> users_cycles;
	protected double sum_mips;
	protected double old_x_f;
	protected double old_y_f;
//	static protected List<Double> delta_x;
//	static protected List<Double> delta_y;
	static protected List<Double> users_local_payoff;
	static protected List<Double> users_offloading_payoff;
	static protected double teta;
	protected double fog_x;
	protected List<Double> users_x;
	static protected double  n_fog;
	static protected List<Double> users_mean;
	protected double old_pop_mean;
	static protected double  n_new;
	protected double d_max;
	protected double e_max;
	
	
	protected Map<String, Map<String, Integer>> moduleInstanceCount;
	
	public FogDevice(
			String name, 
			FogDeviceCharacteristics characteristics,
			VmAllocationPolicy vmAllocationPolicy,
			List<Storage> storageList,
			double schedulingInterval,
			double uplinkBandwidth, double downlinkBandwidth, double uplinkLatency, double ratePerMips) throws Exception {
		super(name, characteristics, vmAllocationPolicy, storageList, schedulingInterval);
		setCharacteristics(characteristics);
		setVmAllocationPolicy(vmAllocationPolicy);
		setLastProcessTime(0.0);
		setStorageList(storageList);
		setVmList(new ArrayList<Vm>());
		setSchedulingInterval(schedulingInterval);
		setUplinkBandwidth(uplinkBandwidth);
		setDownlinkBandwidth(downlinkBandwidth);
		setUplinkLatency(uplinkLatency);
		setRatePerMips(ratePerMips);
		setAssociatedActuatorIds(new ArrayList<Pair<Integer, Double>>());
		for (Host host : getCharacteristics().getHostList()) {
			host.setDatacenter(this);
		}
		setActiveApplications(new ArrayList<String>());
		// If this resource doesn't have any PEs then no useful at all
		if (getCharacteristics().getNumberOfPes() == 0) {
			throw new Exception(super.getName()
					+ " : Error - this entity has no PEs. Therefore, can't process any Cloudlets.");
		}
		// stores id of this class
		getCharacteristics().setId(super.getId());
		
		applicationMap = new HashMap<String, Application>();
		appToModulesMap = new HashMap<String, List<String>>();
		northTupleQueue = new LinkedList<Tuple>();
		southTupleQueue = new LinkedList<Pair<Tuple, Integer>>();
		setNorthLinkBusy(false);
		setSouthLinkBusy(false);
		
		
		setChildrenIds(new ArrayList<Integer>());
		setChildToOperatorsMap(new HashMap<Integer, List<String>>());
		
		this.cloudTrafficMap = new HashMap<Integer, Integer>();
		
		this.lockTime = 0;
		
		this.energyConsumption = 0;
		this.lastUtilization = 0;
		setTotalCost(0);
		setModuleInstanceCount(new HashMap<String, Map<String, Integer>>());
		setChildToLatencyMap(new HashMap<Integer, Double>());
	}

	public FogDevice(
			String name, long mips, int ram, 
			double uplinkBandwidth, double downlinkBandwidth, double ratePerMips, PowerModel powerModel) throws Exception {
		super(name, null, null, new LinkedList<Storage>(), 0);
		
		List<Pe> peList = new ArrayList<Pe>();

		// 3. Create PEs and add these into a list.
		peList.add(new Pe(0, new PeProvisionerOverbooking(mips))); // need to store Pe id and MIPS Rating

		int hostId = FogUtils.generateEntityId();
		long storage = 1000000; // host storage
		int bw = 10000;

		PowerHost host = new PowerHost(
				hostId,
				new RamProvisionerSimple(ram),
				new BwProvisionerOverbooking(bw),
				storage,
				peList,
				new StreamOperatorScheduler(peList),
				powerModel
			);

		List<Host> hostList = new ArrayList<Host>();
		hostList.add(host);

		setVmAllocationPolicy(new AppModuleAllocationPolicy(hostList));
		
		String arch = Config.FOG_DEVICE_ARCH; 
		String os = Config.FOG_DEVICE_OS; 
		String vmm = Config.FOG_DEVICE_VMM;
		double time_zone = Config.FOG_DEVICE_TIMEZONE;
		double cost = Config.FOG_DEVICE_COST; 
		double costPerMem = Config.FOG_DEVICE_COST_PER_MEMORY;
		double costPerStorage = Config.FOG_DEVICE_COST_PER_STORAGE;
		double costPerBw = Config.FOG_DEVICE_COST_PER_BW;

		FogDeviceCharacteristics characteristics = new FogDeviceCharacteristics(
				arch, os, vmm, host, time_zone, cost, costPerMem,
				costPerStorage, costPerBw);

		setCharacteristics(characteristics);
		
		setLastProcessTime(0.0);
		setVmList(new ArrayList<Vm>());
		setUplinkBandwidth(uplinkBandwidth);
		setDownlinkBandwidth(downlinkBandwidth);
		setUplinkLatency(uplinkLatency);
		setAssociatedActuatorIds(new ArrayList<Pair<Integer, Double>>());
		for (Host host1 : getCharacteristics().getHostList()) {
			host1.setDatacenter(this);
		}
		setActiveApplications(new ArrayList<String>());
		if (getCharacteristics().getNumberOfPes() == 0) {
			throw new Exception(super.getName()
					+ " : Error - this entity has no PEs. Therefore, can't process any Cloudlets.");
		}
		
		
		getCharacteristics().setId(super.getId());
		
		applicationMap = new HashMap<String, Application>();
		appToModulesMap = new HashMap<String, List<String>>();
		northTupleQueue = new LinkedList<Tuple>();
		southTupleQueue = new LinkedList<Pair<Tuple, Integer>>();
		setNorthLinkBusy(false);
		setSouthLinkBusy(false);
		
		
		setChildrenIds(new ArrayList<Integer>());
		setChildToOperatorsMap(new HashMap<Integer, List<String>>());
		
		this.cloudTrafficMap = new HashMap<Integer, Integer>();
		
		this.lockTime = 0;
		
		this.energyConsumption = 0;
		this.lastUtilization = 0;
		setTotalCost(0);
		setChildToLatencyMap(new HashMap<Integer, Double>());
		setModuleInstanceCount(new HashMap<String, Map<String, Integer>>());
	}
	/**
	 * Overrides this method
	 *  when making a new and different type of resource. <br>
	 * <b>NOTE:</b> You do not need to override {@link #body()} method, if you use this method.
	 * 
	 * @pre $none
	 * @post $none
	 */
	protected void registerOtherEntity() {
		
	}
	public void startEntity()
	{
	    this.initiate();
	}
   public void processEvent(SimEvent ev)
	{
		switch(ev.getTag()){
	case FogEvents.register:
	//	System.out.println("registering");
		 register(ev);
		 break;
	case FogEvents.initial_x:
		update_user_initial_x(ev);
		break;
	case FogEvents.Decision_user:
		   update_user_x(ev);		
		//print_average();
		break;
	case FogEvents.Mean_population:
		this.set_decision(ev);
		break;
	case FogEvents.cpu_cycles:
		set_fog_cycle(ev);
//		this.set_decision();
	//	this.update_parameters();
		break;
	case FogEvents.End:
		print_x();
		break;
//	case FogEvents.initialx:
//		check();

//		this.update_parameters();
//		break;
	default:
		break;
	}		
}
   public void initiate()
	{		
//		 System.out.println("------"+"initiate-start"+"---------------"+ this.getId()+"-----------------------------------------");
		
		if(this.parentId!=-1)
		{
//			System.out.println("initiate: mobile");

			this.bw=5000;
		    this.cyc=this.getValue(400000, 800000); 
		    FogDevice.users_cycles.add(this.cyc);
		    this.size= this.cyc*0.00001;
            this.set_device_powerConsumption(Math.pow(10, -11)*Math.pow(this.cyc, 2)); 
		    this.set_transfer_powerConsumption(0.1);
		    this.alpha=1.0;
		    this.beta=1-this.alpha;
//		    this.omega=1-(this.alpha+this.beta);
		    FogDevice.ratePerCycle+=this.cyc;
		    d_max=4;
		    e_max=2000000;
		    send(parentId, 0, FogEvents.register);
		}
		else
		{
			FogDevice.n_fog=0;
			FogDevice.users_offloading_payoff=new ArrayList<Double>();
			FogDevice.users_cycles=new ArrayList<Double>();	
			FogDevice.users_local_payoff=new ArrayList<Double>();
			FogDevice.Max_itr=0;
			FogDevice.ratePerCycle=0;
			FogDevice.average_x=0.0;
			FogDevice.average_partial_offloading_delay=0;
			FogDevice.average_local_delay=0;
			FogDevice.average_full_offloading_delay=0;
			FogDevice.average_partial_offloading_energy=0;
			FogDevice.average_local_energy=0;
			FogDevice.average_partial_offloading_payoff=0;
			FogDevice.average_local_payoff=0;
			FogDevice.flag=new ArrayList<Boolean>();
			FogDevice.users_mean=new ArrayList<Double>();
			this.sum_mips=0.0;
			this.pop_mean=0.0;
//			this.threshold=0.000000000000001;
			users_x=new ArrayList<Double>();
//			FogDevice.users_mean_temporal=new ArrayList<Double>();
//			System.out.println("initiate: FOG");
			this.decision_n=FogDevice.n;
			this.fog_x=0.0;
//			old_x_f=1.0;
//			old_y_f=this.get_freeTotalMips();
			for(int i=0;i<FogDevice.n;i++)
			{
//				FogDevice.delta_x.add(0.0);
//				FogDevice.delta_y.add(this.get_freeTotalMips());
				FogDevice.users_mean.add(0.0);
				FogDevice.users_offloading_payoff.add(0.0);
				FogDevice.users_local_payoff.add(0.0);
//				FogDevice.users_cycles.add(0.0);
			}
			for(int i=0;i<FogDevice.n;i++)
			{
				FogDevice.flag.add(false);
				users_x.add(0.0);
//				FogDevice.users_mean_temporal.add(0.0);
//				FogDevice.users_mean.add(0.0);
			}  
		}
//		 System.out.println("------"+"initiate-end"+"---------------"+ this.getId()+"-----------------------------------------");
	} 
	private void register(SimEvent ev)
	{
//		 System.out.println("------"+"register-start"+"---------------"+ this.getId()+"-----------------------------------------");
		int Id;
		Id=(int) ev.getSource();
//		System.out.println("registering of "+ Id);
		this.addChild(Id);
	//	MobileDevices.add(Id);
//		System.out.println("registering :" +Id);
//		System.out.println("size="+ FogDevice.users_mean.size());
/*		for(double d: FogDevice.users_mean)
		{
//			System.out.print(d+" ");
		}*/
		if(this.childrenIds.size()==FogDevice.n)
		{		
//			System.out.println("sent freemips="+get_freeTotalMips()/n);		
			for(int id: this.childrenIds)
			{
			    send(id,0, FogEvents.cpu_cycles,get_freeTotalMips()/FogDevice.n);
			}
		}	
//		 System.out.println("------"+"register-end"+"---------------"+ this.getId()+"-----------------------------------------");
	}
	private void set_fog_cycle(SimEvent ev)
	{
		System.out.println("------"+"set_fog_cycle-start"+"---------------"+ this.getId()+"-----------------------------------------");
		this.fog_cyc=(double)ev.getData();
		this.update_total_parameters();
//		this.pop_mean=0.0;
        double delta_o_l=this.get_total_offloading_outcome()-this.get_total_local_outcome();
//		this.set_initial_decision(ev);   //with pop mean=0	
		if(delta_o_l>=0)
			this.x=1.0;
		else
		{
			while(true) 
			{
			      this.x=(double)this.getValue(1, 50)/100;
			      this.update_parameters();
			      this.set_real_partial_offloading_outcome();
			      if(this.get_real_partial_offloading_outcome()>this.get_total_local_outcome() &&
			    		  this.get_real_partial_offloading_outcome()>this.get_total_offloading_outcome())
			      {
			    	  break;
			      }
			}
			
		}
//		this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet23",this.getId()-2,0, this.x);
//		this.update_partial_parameters();
		FogDevice.teta=1.1;
//		this.set_teta();
		this.set_fog_n();
		this.set_user_mean();
	    FogDevice.users_local_payoff.set(this.getId()-3,this.get_total_local_outcome());
		FogDevice.users_offloading_payoff.set(this.getId()-3, this.get_total_offloading_outcome());
		this.send(parentId, 0, FogEvents.initial_x, this.x);
		System.out.println("set_fog_cycle: total local payoff for user "+ this.getId()+ " ="+ FogDevice.users_local_payoff.get(this.getId()-3));
		System.out.println("set_fog_cycle: total offloading payoff for user "+ this.getId()+ " ="+ FogDevice.users_offloading_payoff.get(this.getId()-3));
		System.out.println("set_fog_cycle: total cycles  for user "+ this.getId()+ " ="+ FogDevice.users_cycles.get(this.getId()-3));
		System.out.println("------"+"set_fog_cycle-end"+"---------------"+ this.getId()+"-----------------------------------------");
	    System.out.println("initial x of user "+this.getId()+"="+this.x);
	}

		public void set_user_mean() 
		{
			System.out.println("------"+"set_user_mean-start"+"---------------"+ this.getId()+"-----------------------------------------");
		     // double delta_l_bar=this.get_total_local_payoff()-this.get_pop_mean();
		      double delta_o_l=this.get_total_offloading_outcome()-this.get_total_local_outcome();
		    System.out.println("deltal_ol="+delta_o_l);
//			this.update_parameters();
//			System.out.println("set_user_mean: n= "+ FogDevice.n_fog);
			System.out.println("set_user_mean: x for user "+this.getId()+" = "+ this.x);
			System.out.println("set_user_mean: local outcome of user "+this.getId()+"="+ this.get_total_local_outcome());
//			this.set_total_offloading_payoff();
			System.out.println("set_user_mean:total offloading outcome of user "+this.getId()+"="+ this.get_total_offloading_outcome());
            if(this.x==1)
				this.user_mean=this.get_total_offloading_outcome();
            else if(this.x==0)
            	this.user_mean=this.get_total_local_outcome();
            else
				{
				this.update_parameters();
				this.set_real_partial_offloading_outcome();  
				this.user_mean=this.get_real_partial_offloading_outcome();		
//		        this.user_mean=((2*FogDevice.n_fog-1)*delta_o_l*this.x*this.x)+(2*(1-FogDevice.n_fog)*delta_o_l*this.x)+this.get_total_local_outcome();
				}
//			this.user_mean=this.get_partial_offloading_payoff();
			FogDevice.users_mean.set(this.getId()-3, this.user_mean);	
		//	this.user_mean = x*get_partial_offloading_payoff()+(1-x)*get_partial_local_payoff();
			System.out.println("set_user_mean: partial offloaing of user "+this.getId()+"="+ FogDevice.users_mean.get(this.getId()-3));
			System.out.println("------"+"set_user_mean-end"+"---------------"+ this.getId()+"-----------------------------------------");
		}
	private void update_user_initial_x(SimEvent ev)
	{
		System.out.println("------"+"update_user_initial_x-start"+"---------------"+ this.getId()+"-----------------------------------------");
		int id;
		boolean f=false;
		double x;
		id=ev.getSource();
		x=(double) ev.getData();
		users_x.set(id-3, x);
//		System.out.println("update_user_x: update_x for user "+id+ " with x="+x);
     	users_x.set(id-3, x);
		this.count--;
		if(this.count==0)
		{
		    this.count=FogDevice.n;
		    double avg=0.0;
		    int cnt=0;
		    for(int i: this.childrenIds)
		    {
		    	int n2=(FogDevice.Max_itr)*FogDevice.n+i;
//				this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet1", n2+1, 0, (double)FogDevice.users_mean.get(i-3));
		    	if((double) users_x.get(i-3)!=0 ) {
		    	   cnt++;
		    	   avg+=(double)FogDevice.users_mean.get(i-3);
		    	}
		    }
		  
		    System.out.println("pop mean= "+this.pop_mean);
//		    this.set_teta();
//		    this.set_fog_n();
		    if(cnt==0)
		    {
			    for(int i: this.childrenIds)
			    {
                     this.send(i, 0, FogEvents.End);
			    }		    	
		    }
		    else
		    {
			    FogDevice.n_new=cnt;		    
			    avg/=cnt;
//			    this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet2",FogDevice.Max_itr+1,0,avg);
			    old_pop_mean=this.pop_mean;
			    this.pop_mean=avg;
			    System.out.println("new pop mean= "+this.pop_mean);
		    	for(int i: this.childrenIds)
			    {
//			    	if((double) users_x.get(i-3)!=1)
//			    	{
			    		send(i,0, FogEvents.Mean_population,this.pop_mean);  
//			    	}
			    }
		    }
		    System.out.println("------"+"update_user_initial_x-end"+"---------------"+ this.getId()+"-----------------------------------------");	
		}

	}
	 void set_decision(SimEvent ev)
	   { 
		    System.out.println("------"+"set_decision-start"+"---------------"+ this.getId()+"-----------------------------------------");
	        double delta_l_bar=this.get_total_local_outcome()-this.get_pop_mean();
	        double delta_o_l=this.get_total_offloading_outcome()-this.get_total_local_outcome();
	        System.out.println("deltal_ol="+delta_o_l+"  delta_lbar="+delta_l_bar);
            if(delta_o_l>=0)
	        {
	        	this.x=1;
	        }
	        else if(FogDevice.n_fog==1) {
	        	this.x=0.0;
	        }
		    else if(FogDevice.n_fog>0.5  && delta_o_l<0 && delta_l_bar>=0 &&
		    		this.pop_mean>=this.total_offloading_outcome && (delta_o_l/delta_l_bar)<
		    		((4*(2*FogDevice.n_fog-1)/(4*Math.pow(1-FogDevice.n_fog, 2)-1))))	
		    {
//		    	System.out.println("2");
		    	double temp=2*(1-FogDevice.n_fog)*delta_o_l;
//		    	System.out.println("temp="+temp);
		    	double temp2=Math.pow(temp, 2) ;
//		    	System.out.println("1:temp2="+temp2);
		    	temp2-=(4*(2*FogDevice.n_fog-1)*delta_o_l*delta_l_bar);
//		    	System.out.println("2:temp2="+temp2);
		    	temp2=Math.sqrt(temp2);
//		    	System.out.println("3:temp2="+temp2);
		    	temp=-temp;
		    	temp-=temp2;
//		    	System.out.println("1:temp="+temp);
		    	temp/=(2*(2*FogDevice.n_fog-1)*delta_o_l);
//		    	System.out.println("2:temp="+temp);
		    	this.x=temp;
		    }
		    else {
//		    	System.out.println("4");
		    	this.x=0;	    	
		    }
		    System.out.println("set_decision: x for user "+this.getId()+" ="+this.x);
			this.set_user_mean();
			this.send(parentId, 0, FogEvents.Decision_user, this.x);
			System.out.println("------"+"set_decision_x-end"+"---------------"+ this.getId()+"-----------------------------------------");
	   }

		void update_user_x(SimEvent ev)
		{
//			System.out.println("------"+"update_user_x-start"+"---------------"+ this.getId()+"-----------------------------------------");
			int id;
			boolean f=false;
			double x;
			id=ev.getSource();
			x=(double) ev.getData();
//			int n=this.childrenIds.indexOf(id);
			users_x.set(id-3, x);
//			System.out.println("update_user_x: update_x for user "+id+ " with x="+x);
//			System.out.println("count= "+this.count);
//			FogDevice.delta_x.set(id-3, x-(double)FogDevice.delta_x.get(id-3));
//			double y=this.get_freeTotalMips()-x*FogDevice.users_cycles.get(id-3);
//			FogDevice.delta_x.set(id-3, y);
	     	users_x.set(id-3, x);
			this.count--;
//			System.out.println("new_n= "+FogDevice.n_new);
			if(this.count==0)
			{
				FogDevice.Max_itr++;
				this.count=FogDevice.n;
			    double avg=0.0;
			    for(int i: this.childrenIds)
			    {
			    	int n2=(FogDevice.Max_itr)*FogDevice.n+i;
//					this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet1", n2+1, 0, (double)FogDevice.users_mean.get(i-3));
			    	if((double) users_x.get(i-3)!=0 ) {
			    	   avg+=(double)FogDevice.users_mean.get(i-3);
			    	}
			    }
			    avg/=FogDevice.n_new;
//			    this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet2",FogDevice.Max_itr+1,0,avg);			    	
//			    System.out.println("pop mean="+avg);
//			    this.set_teta();
//			    this.set_fog_n();
			    System.out.println("------"+"update_user_x-end"+"---------------"+ this.getId()+"-----------------------------------------");	
			    if((old_pop_mean-avg)==0)
			    {
			    	 System.out.println("iteation="+FogDevice.Max_itr);  
			    	for(int i: this.childrenIds)
						{
							send(i,0, FogEvents.End);
//							print();
						}
			    }
			    else
			      {
			    	 System.out.println("2");  
			    	old_pop_mean=this.pop_mean;
				    this.pop_mean=avg;
				    for(int i: this.childrenIds)
				    {
						FogDevice.teta=1.1;
						this.set_fog_n();
//				    	if((double) users_x.get(i-3)!=1)
	//			    	{
				    		send(i,0, FogEvents.Mean_population,this.pop_mean);  
		//		    	}
				    }

			      }
			}
			else if(FogDevice.n_new==0)
			{
				   for(int i: this.childrenIds)
					{
						send(i,0, FogEvents.End);
//						print();
					}				
			}

		}
   void print_x()
   {
	    System.out.println("------"+"print_x-start"+"---------------"+ this.getId()+"-----------------------------------------");
	    System.out.println("print_x:final x for mobile device "+ this.getId()+ "  ="+ this.x);
//		this.update_total_parameters();
//		this.update_partial_parameters();
		System.out.println("----------------------------------------------------------------------------------------");
		System.out.println("capacity of user "+this.getId()+"="+this.get_freeTotalMips());
//		this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet3",this.getId()-2,0, this.get_freeTotalMips());
		System.out.println("allocated capacity to user "+this.getId()+"="+this.fog_cyc);
	    System.out.println("size of user "+this.getId()+"="+this.size);
	    System.out.println("cycle of user "+this.getId()+"="+this.cyc);
//	    this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet4",this.getId()-2,0, this.cyc);
		System.out.println("send decicion:decision for user "+this.getId()+"="+this.x);
		FogDevice.average_x+=x;
//		this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet5",this.getId()-2,0, this.x);
		this.set_real_partial_local_energyConsumption();
		this.set_real_partial_transfer_energyConsumption();
		System.out.println("partial offloading energy consmption for user "+this.getId()+"="+this.get_real_partial_local_energyConsumption()
		+this.get_real_partial_transfer_energyConsumption());
//		this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet6",this.getId()-2,0, 
//				this.get_real_partial_local_energyConsumption()+this.get_real_partial_transfer_energyConsumption());
		FogDevice.average_partial_offloading_energy+=this.get_real_partial_local_energyConsumption()+this.get_real_partial_transfer_energyConsumption();
		System.out.println();
		set_total_real_local_energyConsumption();
		System.out.println("local energy consmption for user "+this.getId()+"="+this.total_real_local_energyConsumption);
		FogDevice.average_local_energy+=this.total_real_local_energyConsumption;
//		this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx","Sheet7",this.getId()-2,0,this.total_real_local_energyConsumption);
		System.out.println();
		System.out.println( "partial offloading delay saving for user "+this.getId()+"="+max(this.get_partial_local_delay(),
				this.get_partial_offloading_delay()));
		FogDevice.average_partial_offloading_delay+=max(this.get_partial_local_delay(),
				this.get_partial_offloading_delay());
//		this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet8",this.getId()-2,0,max(this.get_partial_local_delay(),
//				this.get_partial_offloading_delay()));
		System.out.println();
		this.set_total_real_local_delay();
		System.out.println("local delay saving  for user "+this.getId()+"="+this.get_total_real_local_delay());
		FogDevice.average_local_delay+=this.get_total_real_local_delay();
//		this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet9",this.getId()-2,0,this.get_total_real_local_delay());
		System.out.println();
		this.set_real_total_offloading_delay();
		System.out.println("full offloading user delay saving  for user  "+this.getId()+"="+this.get_real_total_offloading_delay());
		FogDevice.average_full_offloading_delay+=this.get_real_total_offloading_delay();
//		this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet10",this.getId()-2,0,this.get_real_total_offloading_delay());
//		System.out.println();
		System.out.println("user mean for user "+this.getId()+this.user_mean);
//		System.out.println();
//		System.out.println("pop mean="+this.pop_mean);
//		System.out.println();	
//		set_real_partial_offloading_outcome();
//		System.out.println("partial offloading user payoff for user "+this.getId()+"="+get_real_partial_offloading_outcome());
		System.out.println("partial offloaing of user "+this.getId()+"="+ this.user_mean);
		FogDevice.average_partial_offloading_payoff+=this.user_mean;
//		this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet11",this.getId()-2,0,this.user_mean);
//		System.out.println();
		System.out.println("local user payoff for user  "+this.getId()+"="+this.get_total_local_outcome());
		FogDevice.average_local_payoff+=this.get_total_local_outcome();
//		this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet12",this.getId()-2,0,this.get_total_local_outcome());
		System.out.println("full offloading payoff="+this.get_total_offloading_outcome());
//		this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx","Sheet24",this.getId()-2,0,this.get_total_offloading_outcome());
		System.out.println("----------------------------------------------------------------------------------------");
//		send(parentId, 0, FogEvents.Decision_user , decision() );
//		 System.out.println("iteration="+FogDevice.Max_itr);
//		 this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet21",this.getId()-1,0,FogDevice.Max_itr);
	    System.out.println("------"+"print_x-end"+"---------------"+ this.getId()+"-----------------------------------------");
   }

void set_fog_n()
   {
	   System.out.println("------"+"set_fog_n-start"+"---------------"+ this.getId()+"-----------------------------------------");
/*	   double sum=0.0;
	   for(int i=0; i<FogDevice.n; i++)
	   {
		   sum+= (FogDevice.users_cycles.get(i)* this.users_x.get(i));
	   }*/  
	   FogDevice.n_fog=(this.get_teta()/(1+this.get_teta()));// rest point in point n
//	   FogDevice.n_fog=1- FogDevice.n_fog;// free resource amount of fog
	   System.out.println("n=" + FogDevice.n_fog);
	   System.out.println("------"+"set_fog_n-end"+"---------------"+ this.getId()+"-----------------------------------------");
   }
   void set_teta()
   {
	   System.out.println("------"+"set_teta-start"+"---------------"+ this.getId()+"-----------------------------------------");
	   double s_x=0.0, s_y=0.0;
	   for(int j=0; j<n; j++)
	   {
//		  if((double) users_x.get(j)!=1.0)
//		  {
		   s_x+=((double) FogDevice.users_cycles.get(j)*(double) users_x.get(j));
//		  }
	   }
	   System.out.println(" old_x_f="+this.old_x_f); 
	   System.out.println(" new_x_f="+s_x);  
	   s_y=this.get_freeTotalMips()-this.sum_mips-s_x;
	   System.out.println(" old_y_f="+this.old_y_f);
	   System.out.println(" new_y_f="+s_y); 
	   teta=(s_x/this.old_x_f)/(s_y/this.old_y_f);
	   this.old_x_f=s_x;
	   this.old_y_f=s_y;
//	   teta=1.0;
	   System.out.println("set_tetta: teta="+this.teta);
	   System.out.println("------"+"set_teta-end"+"---------------"+ this.getId()+"-----------------------------------------");
   }
   double get_teta()
   {
	   return teta;
   }
   void set_fog_x()
   {
	   System.out.println("------"+"set_fog_x"+"---------------"+ this.getId()+"-----------------------------------------");
	   double s_x=0, s_total_cycle=0;
	   for(int i=0; i<n; i++)
	   {
		   s_total_cycle+=(double) FogDevice.users_cycles.get(i);
	   }
	   for(int j=0; j<n;j++)
	   {
		   s_x+=((double)users_x.get(j)* (double)FogDevice.users_cycles.get(j));
	   }
//	   fog_x=s_x/s_total_cycle;
	   fog_x= s_x;
	   System.out.println("set_fog_x: fog_x="+ this.fog_x);
	   System.out.println("------"+"set_fog_x-end"+"---------------"+ this.getId()+"-----------------------------------------");
   }
   double get_fog_x() {
	   return fog_x;
   }
  
   double get_decision()
   {
	   return this.x;
   }

   double get_fog_n()
   {
	   return FogDevice.n_fog;
   }

	void send_pop_mean()
	{
	    boolean flag=false;
		System.out.println("------"+"send_pop_mean-start"+"---------------"+ this.getId()+"-----------------------------------------");
		System.out.println("send_pop_mean: pop mean ="+ this.pop_mean);
		for(int i: this.childrenIds)
		{
			if((double) users_x.get(i-3)!=1) {
				  flag=true;
			      send(i,0, FogEvents.Mean_population,this.pop_mean);
			}
		}	
		if(flag==false) {
			for(int i: this.childrenIds)
			{
				      send(i,0, FogEvents.End,this.pop_mean);
			}				
		}
		System.out.println("------"+"send_pop_mean-end"+"---------------"+ this.getId()+"-----------------------------------------");
	}
	@Override
	protected void processOtherEvent(SimEvent ev) {
		switch(ev.getTag()){
		case FogEvents.TUPLE_ARRIVAL:
			processTupleArrival(ev);
			break;
		case FogEvents.LAUNCH_MODULE:
			processModuleArrival(ev);
			break;
		case FogEvents.RELEASE_OPERATOR:
			processOperatorRelease(ev);
			break;
		case FogEvents.SENSOR_JOINED:
			processSensorJoining(ev);
			break;
		case FogEvents.SEND_PERIODIC_TUPLE:
			sendPeriodicTuple(ev);
			break;
		case FogEvents.APP_SUBMIT:
			processAppSubmit(ev);
			break;
		case FogEvents.UPDATE_NORTH_TUPLE_QUEUE:
			updateNorthTupleQueue();
			break;
		case FogEvents.UPDATE_SOUTH_TUPLE_QUEUE:
			updateSouthTupleQueue();
			break;
		case FogEvents.ACTIVE_APP_UPDATE:
			updateActiveApplications(ev);
			break;
		case FogEvents.ACTUATOR_JOINED:
			processActuatorJoined(ev);
			break;
		case FogEvents.LAUNCH_MODULE_INSTANCE:
			updateModuleInstanceCount(ev);
			break;
		case FogEvents.RESOURCE_MGMT:
			manageResources(ev);
			break;
		default:
			break;
		}
	}
  
	/**
	 * Perform miscellaneous resource management tasks
	 * @param ev
	 */
	private void manageResources(SimEvent ev) {
		updateEnergyConsumption();
		send(getId(), Config.RESOURCE_MGMT_INTERVAL, FogEvents.RESOURCE_MGMT);
	}
/*	protected void send_cpu_cycle(SimEvent ev)
	{
		int id=ev.getSource();
		System.out.println("cycles for "+id+"="+ get_freeTotalMips()/n);
		send(id,0, FogEvents.cpu_cycles,get_freeTotalMips()/n);
	}
	*/
	
	protected double max(double a,double b)
	{
		if(a>b)
		{
			return a;
		}
		else
		{
			return b;
		}
	}
/*	private synchronized void send_decision()
	{
		this.update_total_parameters();
		this.update_partial_parameters();
		System.out.println("----------------------------------------------------------------------------------------");
		System.out.println("capacity of user "+this.getId()+"="+this.get_freeTotalMips());
		this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet3",this.getId()-2,0, this.get_freeTotalMips());
		System.out.println("allocated capacity to user "+this.getId()+"="+this.fog_cyc);
	    System.out.println("size of user "+this.getId()+"="+this.size);
	    System.out.println("cycle of user "+this.getId()+"="+this.cyc);
	    this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet4",this.getId()-2,0, this.cyc);
		System.out.println("send decicion:decision for user "+this.getId()+"="+decision());
		FogDevice.average_x+=x;
		this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet5",this.getId()-2,0, this.decision());
		System.out.printf("partial offloading energy consmption for user "+this.getId()+"=%.15f",this.get_partial_local_energyConsumption()
		+this.get_partial_transfer_energyConsumption());
		this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet6",this.getId()-2,0, 
				this.get_partial_local_energyConsumption()+this.get_partial_transfer_energyConsumption());
		FogDevice.average_partial_offloading_energy+=this.get_partial_local_energyConsumption()+this.get_partial_transfer_energyConsumption();
		System.out.println();
		System.out.printf("local energy consmption for user "+this.getId()+"=%.15f",this.total_local_energyConsumption);
		FogDevice.average_local_energy+=this.total_local_energyConsumption;
		this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx","Sheet7",this.getId()-2,0,this.total_local_energyConsumption);
		System.out.println();
		System.out.printf( "partial offloading delay for user "+this.getId()+"=%.15f",max((this.cyc*(1-this.x))/this.get_freeTotalMips(),
				this.get_partial_offloading_delay()+this.get_partial_transfer_delay()));
		FogDevice.average_partial_offloading_delay+=max((this.cyc*(1-this.x))/this.get_freeTotalMips(),
				this.get_partial_offloading_delay()+this.get_partial_transfer_delay());
		this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet8",this.getId()-2,0,max((this.cyc*(1-this.x))/this.get_freeTotalMips(),
				this.get_partial_offloading_delay()+this.get_partial_transfer_delay()));
		System.out.println();
		System.out.printf("local delay for user "+this.getId()+"=%.15f",this.get_total_local_delay());
		FogDevice.average_local_delay+=this.get_total_local_delay();
		this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet9",this.getId()-2,0,this.get_total_local_delay());
		System.out.println();
		System.out.printf("full offloading user delay for user  "+this.getId()+"=%.15f",this.get_total_offloading_delay());
		FogDevice.average_full_offloading_delay+=this.get_total_offloading_delay();
		this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet10",this.getId()-2,0,this.get_total_offloading_delay());
		System.out.println();
		System.out.printf("user mean for user "+this.getId()+"=%.15f",this.user_mean);
		System.out.println();
		System.out.printf("pop mean  "+"=%.15f",this.pop_mean);
		System.out.println();	
		System.out.printf("partial offloading user payoff for user "+this.getId()+"=%.15f",this.get_partial_local_payoff()+
				this.get_partial_offloading_payoff());
		FogDevice.average_partial_offloading_payoff+=this.get_partial_local_payoff()+this.get_partial_offloading_payoff();
		this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet11",this.getId()-2,0,this.get_partial_local_payoff()+
				this.get_partial_offloading_payoff());
		System.out.println();
		System.out.printf("local user payoff for user  "+this.getId()+"=%.15f",this.get_total_local_payoff());
		FogDevice.average_local_payoff+=this.get_total_local_payoff();
		this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet12",this.getId()-2,0,this.get_total_local_payoff());
		System.out.println("full offloading payoff="+this.get_total_offloading_payoff());
		System.out.println("----------------------------------------------------------------------------------------");
		send(parentId, 0, FogEvents.Decision_user , decision() );
		
	}
	 protected synchronized void print_average()
	   {
		   System.out.println("average of offloading="+average_x/FogDevice.n);
		   this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet13",this.getId()-1,0,average_x/FogDevice.n);
		   System.out.println("average of full offloading delay="+FogDevice.average_full_offloading_delay/FogDevice.n);
		   this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet14",this.getId()-1,0,FogDevice.average_full_offloading_delay/FogDevice.n);
		   System.out.println("average of local delay="+FogDevice.average_local_delay/FogDevice.n);
		   this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet15",this.getId()-1,0,FogDevice.average_local_delay/FogDevice.n);
		   System.out.println("average of partial offloading delay="+FogDevice.average_partial_offloading_delay/FogDevice.n);
		   this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet16",this.getId()-1,0,FogDevice.average_partial_offloading_delay/FogDevice.n);
		   System.out.println("average of local energy="+FogDevice.average_local_energy/FogDevice.n);
		   this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet17",this.getId()-1,0,FogDevice.average_local_energy/FogDevice.n);
		   System.out.println("average of partial offloading energy="+FogDevice.average_partial_offloading_energy/FogDevice.n);
		   this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet18",this.getId()-1,0,FogDevice.average_partial_offloading_energy/FogDevice.n);
		   System.out.printf("average of local payoff=%.15f",FogDevice.average_local_payoff/FogDevice.n);
		   this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet19",this.getId()-1,0,average_local_payoff/FogDevice.n);
		   System.out.println();
		   System.out.printf("average of partial offloading payoff=%.15f",FogDevice.average_partial_offloading_payoff/FogDevice.n);
		   this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet20",this.getId()-1,0,FogDevice.average_partial_offloading_payoff/FogDevice.n);
		   System.out.println();
		   System.out.println("iteration="+FogDevice.Max_itr);
		   this.AddXLSRow(FogDevice.population+FogDevice.n+".xlsx", "Sheet21",this.getId()-1,0,FogDevice.Max_itr);
	   }*/
	public double get_freeTotalMips()
	{
		double totalMipsAllocated = 0;
		for(final Vm vm : getHost().getVmList()){
			AppModule operator = (AppModule)vm;
			operator.updateVmProcessing(CloudSim.clock(), getVmAllocationPolicy().getHost(operator).getVmScheduler()
					.getAllocatedMipsForVm(operator));
			totalMipsAllocated += getHost().getTotalAllocatedMipsForVm(vm);
			
		}
		double d=(double) getHost().getTotalMips()-totalMipsAllocated;
	//	System.out.println(this.getId()+":freeTotalMips: freeTotalMips="+ d);
		return (double) getHost().getTotalMips()-totalMipsAllocated;
	}

	public void set_n(int n)
	{
		FogDevice.n=n;
		this.count=FogDevice.n;
		this.decision_n=FogDevice.n;
		System.out.println("decision_n="+this.decision_n);
	}

	
	protected  double getValue(double min, double max)
	{
	       Random r = new Random();
	       double randomValue = min + (max - min) % r.nextDouble();
	       return randomValue;
	}
	protected  int getValue(int min, int max)
	{
	       Random r = new Random();	       
	       int randomValue =min+r.nextInt(max-min) ;
//	       System.out.println("random="+randomValue);
	       return randomValue;
	}

/*	private void set_popmean(SimEvent ev)
	{
		this.pop_mean=(double)ev.getData();

	}*/
/*	public void get_fog_cycle()
	{
		System.out.println("get_fog_cycle:parentId="+parentId+" event="+FogEvents.cpu );
		sendNow(parentId,FogEvents.cpu);
	}
*/

	protected void update_parameters()
	{
		System.out.println("------"+"update_parameters-start"+"---------------"+ this.getId()+"-----------------------------------------");
		update_total_parameters();
	    update_partial_parameters();
	    System.out.println("------"+"update_parameters-end"+"---------------"+ this.getId()+"-----------------------------------------");
//	    set_user_mean();
//	    System.out.println("update: user_mean="+this.get_user_mean()+" for user "+this.getId()+ " while popmean="+this.pop_mean);
//	    send(parentId,0,FogEvents.Mean_user,this.get_user_mean());
	}

	private void update_total_parameters()
	{
		set_total_local_delay();
		set_total_local_energyConsumption();
		set_total_offloading_delay();
		set_total_transfer_delay();
		set_total_transfer_energyConsumption();
		set_total_local_outcome(alpha,beta);
		set_total_offloading_outcome(alpha,beta);
//		set_total_penalty();
	}
	private void update_partial_parameters()
	{
//		System.out.println("update_partial_parameters:x="+ this.x+ " for mobile device="+ this.getId());
		set_partial_local_delay();
		set_partial_local_energyConsumption();
		set_partial_offloading_delay();
		set_partial_transfer_delay();
		set_partial_transfer_energyConsumption();
		set_partial_local_outcome(alpha,beta);
		set_partial_offloading_outcome(alpha,beta);
//		set_partial_penalty();	
	}
	
	public double getCycle() {
		return cyc;
	}
	public double getSize() {
		return size;
	}
	public void set_Task_cycle(int min, int max) {
		this.cyc = getValue(min,max);
		System.out.println("cyc="+ this.cyc);
	}
	public void set_Task_size(int min, int max) {
		this.size = getValue(min,max);
		System.out.println("size="+ this.size);
	}
	
	public void set_X(double x) {
		this.x = x;
	}
	public double get_X() {
		return this.x;
	}

	public void set_pop_mean(double pop_mean) {
		this.pop_mean = pop_mean;
	}
	public double get_pop_mean() {
		return this.pop_mean;
	}		
	
	public void set_ratePerCycle(double ratePerCycle) {
		FogDevice.ratePerCycle = ratePerCycle;
//		System.out.println("ratePerCycle="+ this.ratePerCycle);
	}
	public double get_ratePerCycle() {
		return FogDevice.ratePerCycle;
	} 
	
	public void set_device_powerConsumption(double power) {
		this.device_powerConsumption =power ;
	}
	public double get_device_powerConsumption() {
		return this.device_powerConsumption;
	}
	public void set_transfer_powerConsumption(double power) {
		this.transfer_powerConsumption =power ;
	}
	public double get_transfer_powerConsumption() {
		return this.transfer_powerConsumption;
	}

//**********************************************fog model****************************************************************	

	public double get_user_mean() {
		return this.user_mean;
	}	
	public void set_total_penalty() {
		this.total_penalty = (cyc)/FogDevice.ratePerCycle;
//		System.out.println("total_penalty for user "+this.getId()+"="+ this.total_penalty+" with r="+FogDevice.ratePerCycle);
	}
	public double get_total_penalty() {
		return this.total_penalty;
	}

	public void set_partial_penalty() {
		this.partial_penalty = ((x*cyc))/FogDevice.ratePerCycle;
//		System.out.println("partial_penalty for user "+this.getId()+"="+ this.partial_penalty);
	}
	public double get_partial_penalty() {
		return this.partial_penalty;
	}
	
	public void set_partial_offloading_outcome(double alpha, double beta) {
		this.partial_offloading_outcome =this.get_total_local_outcome()-this.x*(this.get_total_local_outcome()
				-this.get_total_offloading_outcome());
//		System.out.println("partial_offloading_overhead for user "+this.getId()+"="+ this.partial_offloading_overhead);
	}
	public double get_partial_offloading_outcome() {
		return this.partial_offloading_outcome;
	}
	public double get_real_partial_offloading_outcome() {
		return this.real_partial_offloading_outcome;
	}	
	public void set_real_partial_offloading_outcome() {
		this.real_partial_offloading_outcome =(alpha*(this.d_max-max(this.get_partial_local_delay(),
				this.x*this.get_partial_offloading_delay())))/(this.d_max)
				+(beta*(this.e_max-this.x*(this.get_partial_transfer_energyConsumption()+this.get_partial_local_energyConsumption())))/(this.e_max);
//		System.out.println("total_offloading_overhead for user "+this.getId()+"="+ this.total_offloading_overhead);
	}
	public void set_total_offloading_outcome(double alpha, double beta) {
		this.total_offloading_outcome =(alpha*(this.d_max-this.get_total_offloading_delay()))/(this.d_max)
				+(beta*(this.e_max-this.get_total_transfer_energyConsumption()))/(this.e_max);
//		System.out.println("total_offloading_overhead for user "+this.getId()+"="+ this.total_offloading_overhead);
	}
	public double get_total_offloading_outcome() {
		return this.total_offloading_outcome;
	}	
	public void set_partial_transfer_energyConsumption() {
		this.partial_transfer_energyConsumption = x*size*get_transfer_powerConsumption()/bw;
//		System.out.println("partial_transfer_energyConsumption for user "+this.getId()+"="+ this.partial_transfer_energyConsumption);
	}
	public double get_partial_transfer_energyConsumption() {
		return this.partial_transfer_energyConsumption;
	}
	public void set_real_partial_transfer_energyConsumption() {
		this.partial_real_transfer_energyConsumption = (this.e_max-this.get_partial_transfer_energyConsumption())/(this.e_max);
//		System.out.println("partial_transfer_energyConsumption for user "+this.getId()+"="+ this.partial_transfer_energyConsumption);
	}
	public double get_real_partial_transfer_energyConsumption() {
		return this.partial_real_transfer_energyConsumption;
	}
	public void set_total_transfer_energyConsumption() {
		this.total_transfer_energyConsumption = size*get_transfer_powerConsumption()/bw;
//		System.out.println("total_transfer_energyConsumptionn for user "+this.getId()+"="+ this.total_transfer_energyConsumption);
	}
	public double get_total_transfer_energyConsumption() {
		return this.total_transfer_energyConsumption;
	}	
	public void set_partial_transfer_delay() {
		this.partial_transfer_delay = x*size/bw;
//		System.out.println("partial_transfer_delay for "+this.getId()+"="+ this.partial_transfer_delay);
	}
	public double get_partial_transfer_delay() {
		return this.partial_transfer_delay;
	}	
	public void set_total_transfer_delay() {
		this.total_transfer_delay = size/bw;
//		System.out.println("total_transfer_delay for user "+this.getId()+"="+ this.total_transfer_delay);
	}
	public double get_total_transfer_delay() {
		return this.total_transfer_delay;
		
	}
	public void set_partial_offloading_delay() {
		this.partial_offloading_delay = (x*cyc/fog_cyc)+get_partial_transfer_delay();
//		System.out.println("partial_offloading_delay for user "+this.getId()+"="+ this.partial_offloading_delay);
	}
	public double get_partial_offloading_delay() {
		return this.partial_offloading_delay;
	}	
	public void set_total_offloading_delay() {
		this.total_offloading_delay = (cyc/fog_cyc)+this.get_total_transfer_delay();
//		System.out.println("total_offloading_delay  for user "+this.getId()+"="+ this.total_offloading_delay);
	}
	public double get_total_offloading_delay() {
		return this.total_offloading_delay;
	}
	public void set_real_total_offloading_delay() {
		this.total_real_offloading_delay = (this.d_max-this.get_total_offloading_delay())/(this.d_max);
//		System.out.println("total_offloading_delay  for user "+this.getId()+"="+ this.total_offloading_delay);
	}
	public double get_real_total_offloading_delay() {
		return this.total_real_offloading_delay;
	}
//*********************************************local model****************************************************************		
	
	public void set_partial_local_outcome(double alpha, double beta) {
		this.partial_local_outcome =(alpha*(this.d_max-this.get_partial_local_delay()))/(this.d_max)+
				(beta*(this.e_max-this.get_partial_local_energyConsumption()))/(this.e_max);
//		System.out.println("partial_local_overhead for user "+this.getId()+"="+ this.partial_local_overhead);
	}
	public double get_partial_local_outcome() {
		return this.partial_local_outcome;
	}
	public void set_total_local_outcome(double alpha, double beta) {
		this.total_local_outcome =(alpha*(this.d_max-this.get_total_local_delay()))/(this.d_max)
				+(beta*(this.e_max-this.get_total_local_energyConsumption()))/(this.e_max);
//		System.out.println("total_local_overhead  for user "+this.getId()+"="+ this.total_local_overhead);
	}
	public double get_total_local_outcome() {
		return this.total_local_outcome;
	}	
	
	public void set_partial_local_energyConsumption() {
		this.partial_local_energyConsumption = (1-x)*cyc*get_device_powerConsumption();
  //  	System.out.println("partial_local_energyConsumption for user "+this.getId()+"="+ this.partial_local_energyConsumption);
	}
	public double get_real_partial_local_energyConsumption() {
		return this.partial_real_local_energyConsumption;
	}
	public void set_real_partial_local_energyConsumption() {
		this.partial_real_local_energyConsumption = (this.e_max-this.get_partial_local_energyConsumption())/(this.e_max);
  //  	System.out.println("partial_local_energyConsumption for user "+this.getId()+"="+ this.partial_local_energyConsumption);
	}
	public double get_partial_local_energyConsumption() {
		return this.partial_local_energyConsumption;
	}	
	public void set_total_local_energyConsumption() {
		this.total_local_energyConsumption = cyc*get_device_powerConsumption();
//		System.out.println("total_local_energyConsumption for user "+this.getId()+"="+ this.total_local_energyConsumption);
	}
	public double get_total_local_energyConsumption() {
		return this.total_local_energyConsumption;
	}
	public void set_total_real_local_energyConsumption() {
		this.total_real_local_energyConsumption = (this.e_max-this.get_total_local_energyConsumption())/(this.e_max);
//		System.out.println("total_local_energyConsumption for user "+this.getId()+"="+ this.total_local_energyConsumption);
	}
	public double get_total_real_local_energyConsumption() {
		return this.total_real_local_energyConsumption;
	}	
	public void set_partial_local_delay() {
		this.partial_local_delay = ((1-x)*cyc)/get_freeTotalMips();
//		System.out.println("patial_local_delay for user "+this.getId()+"="+ this.partial_local_delay);
	}
	public double get_partial_local_delay() {
		return this.partial_local_delay;
	}
	public void set_total_local_delay() {
		this.total_local_delay = cyc/get_freeTotalMips();
//		System.out.println("total_local_delay for user "+this.getId()+"="+ this.total_local_delay);
	}
	public double get_total_local_delay() {
		return this.total_local_delay;
	}
	public void set_total_real_local_delay() {
		this.total_real_local_delay = (this.d_max-this.get_total_local_delay())/(this.d_max);
//		System.out.println("total_local_delay for user "+this.getId()+"="+ this.total_local_delay);
	}
	public double get_total_real_local_delay() {
		return this.total_real_local_delay;
	}
	/**
	 * Updating the number of modules of an application module on this device
	 * @param ev instance of SimEvent containing the module and no of instances 
	 */
	private void updateModuleInstanceCount(SimEvent ev) {
		ModuleLaunchConfig config = (ModuleLaunchConfig)ev.getData();
		String appId = config.getModule().getAppId();
		if(!moduleInstanceCount.containsKey(appId))
			moduleInstanceCount.put(appId, new HashMap<String, Integer>());
		moduleInstanceCount.get(appId).put(config.getModule().getName(), config.getInstanceCount());
		System.out.println(getName()+ " Creating "+config.getInstanceCount()+" instances of module "+config.getModule().getName());
	}

	private AppModule getModuleByName(String moduleName){
		AppModule module = null;
		for(Vm vm : getHost().getVmList()){
			if(((AppModule)vm).getName().equals(moduleName)){
				module=(AppModule)vm;
				break;
			}
		}
		return module;
	}
	
	/**
	 * Sending periodic tuple for an application edge. Note that for multiple instances of a single source module, only one tuple is sent DOWN while instanceCount number of tuples are sent UP.
	 * @param ev SimEvent instance containing the edge to send tuple on
	 */
	private void sendPeriodicTuple(SimEvent ev) {
		AppEdge edge = (AppEdge)ev.getData();
		String srcModule = edge.getSource();
		AppModule module = getModuleByName(srcModule);
		
		if(module == null)
			return;
		
		int instanceCount = module.getNumInstances();
		/*
		 * Since tuples sent through a DOWN application edge are anyways broadcasted, only UP tuples are replicated
		 */
		for(int i = 0;i<((edge.getDirection()==Tuple.UP)?instanceCount:1);i++){
			//System.out.println(CloudSim.clock()+" : Sending periodic tuple "+edge.getTupleType());
			Tuple tuple = applicationMap.get(module.getAppId()).createTuple(edge, getId(), module.getId());
			updateTimingsOnSending(tuple);
			sendToSelf(tuple);			
		}
		send(getId(), edge.getPeriodicity(), FogEvents.SEND_PERIODIC_TUPLE, edge);
	}

	protected void processActuatorJoined(SimEvent ev) {
		int actuatorId = ev.getSource();
		double delay = (double)ev.getData();
		getAssociatedActuatorIds().add(new Pair<Integer, Double>(actuatorId, delay));
	}

	
	protected void updateActiveApplications(SimEvent ev) {
		Application app = (Application)ev.getData();
		getActiveApplications().add(app.getAppId());
	}

	
	public String getOperatorName(int vmId){
		for(Vm vm : this.getHost().getVmList()){
			if(vm.getId() == vmId)
				return ((AppModule)vm).getName();
		}
		return null;
	}
	
	/**
	 * Update cloudet processing without scheduling future events.
	 * 
	 * @return the double
	 */
	protected double updateCloudetProcessingWithoutSchedulingFutureEventsForce() {
		double currentTime = CloudSim.clock();
		double minTime = Double.MAX_VALUE;
		double timeDiff = currentTime - getLastProcessTime();
		double timeFrameDatacenterEnergy = 0.0;

		for (PowerHost host : this.<PowerHost> getHostList()) {
			Log.printLine();

			double time = host.updateVmsProcessing(currentTime); // inform VMs to update processing
			if (time < minTime) {
				minTime = time;
			}

			Log.formatLine(
					"%.2f: [Host #%d] utilization is %.2f%%",
					currentTime,
					host.getId(),
					host.getUtilizationOfCpu() * 100);
		}

		if (timeDiff > 0) {
			Log.formatLine(
					"\nEnergy consumption for the last time frame from %.2f to %.2f:",
					getLastProcessTime(),
					currentTime);

			for (PowerHost host : this.<PowerHost> getHostList()) {
				double previousUtilizationOfCpu = host.getPreviousUtilizationOfCpu();
				double utilizationOfCpu = host.getUtilizationOfCpu();
				double timeFrameHostEnergy = host.getEnergyLinearInterpolation(
						previousUtilizationOfCpu,
						utilizationOfCpu,
						timeDiff);
				timeFrameDatacenterEnergy += timeFrameHostEnergy;

				Log.printLine();
				Log.formatLine(
						"%.2f: [Host #%d] utilization at %.2f was %.2f%%, now is %.2f%%",
						currentTime,
						host.getId(),
						getLastProcessTime(),
						previousUtilizationOfCpu * 100,
						utilizationOfCpu * 100);
				Log.formatLine(
						"%.2f: [Host #%d] energy is %.2f W*sec",
						currentTime,
						host.getId(),
						timeFrameHostEnergy);
			}

			Log.formatLine(
					"\n%.2f: Data center's energy is %.2f W*sec\n",
					currentTime,
					timeFrameDatacenterEnergy);
		}

		setPower(getPower() + timeFrameDatacenterEnergy);

		checkCloudletCompletion();

		/** Remove completed VMs **/
		/**
		 * Change made by HARSHIT GUPTA
		 */
		/*for (PowerHost host : this.<PowerHost> getHostList()) {
			for (Vm vm : host.getCompletedVms()) {
				getVmAllocationPolicy().deallocateHostForVm(vm);
				getVmList().remove(vm);
				Log.printLine("VM #" + vm.getId() + " has been deallocated from host #" + host.getId());
			}
		}*/
		
		Log.printLine();

		setLastProcessTime(currentTime);
		return minTime;
	}


	protected void checkCloudletCompletion() {
		boolean cloudletCompleted = false;
		List<? extends Host> list = getVmAllocationPolicy().getHostList();
		for (int i = 0; i < list.size(); i++) {
			Host host = list.get(i);
			for (Vm vm : host.getVmList()) {
				while (vm.getCloudletScheduler().isFinishedCloudlets()) {
					Cloudlet cl = vm.getCloudletScheduler().getNextFinishedCloudlet();
					if (cl != null) {
						
						cloudletCompleted = true;
						Tuple tuple = (Tuple)cl;
						TimeKeeper.getInstance().tupleEndedExecution(tuple);
						Application application = getApplicationMap().get(tuple.getAppId());
						Logger.debug(getName(), "Completed execution of tuple "+tuple.getCloudletId()+"on "+tuple.getDestModuleName());
						List<Tuple> resultantTuples = application.getResultantTuples(tuple.getDestModuleName(), tuple, getId(), vm.getId());
						for(Tuple resTuple : resultantTuples){
							resTuple.setModuleCopyMap(new HashMap<String, Integer>(tuple.getModuleCopyMap()));
							resTuple.getModuleCopyMap().put(((AppModule)vm).getName(), vm.getId());
							updateTimingsOnSending(resTuple);
							sendToSelf(resTuple);
						}
						sendNow(cl.getUserId(), CloudSimTags.CLOUDLET_RETURN, cl);
					}
				}
			}
		}
		if(cloudletCompleted)
			updateAllocatedMips(null);
	}
	
	protected void updateTimingsOnSending(Tuple resTuple) {
		// TODO ADD CODE FOR UPDATING TIMINGS WHEN A TUPLE IS GENERATED FROM A PREVIOUSLY RECIEVED TUPLE. 
		// WILL NEED TO CHECK IF A NEW LOOP STARTS AND INSERT A UNIQUE TUPLE ID TO IT.
		String srcModule = resTuple.getSrcModuleName();
		String destModule = resTuple.getDestModuleName();
		for(AppLoop loop : getApplicationMap().get(resTuple.getAppId()).getLoops()){
			if(loop.hasEdge(srcModule, destModule) && loop.isStartModule(srcModule)){
				int tupleId = TimeKeeper.getInstance().getUniqueId();
				resTuple.setActualTupleId(tupleId);
				if(!TimeKeeper.getInstance().getLoopIdToTupleIds().containsKey(loop.getLoopId()))
					TimeKeeper.getInstance().getLoopIdToTupleIds().put(loop.getLoopId(), new ArrayList<Integer>());
				TimeKeeper.getInstance().getLoopIdToTupleIds().get(loop.getLoopId()).add(tupleId);
				TimeKeeper.getInstance().getEmitTimes().put(tupleId, CloudSim.clock());
				
				//Logger.debug(getName(), "\tSENDING\t"+tuple.getActualTupleId()+"\tSrc:"+srcModule+"\tDest:"+destModule);
				
			}
		}
	}

	protected int getChildIdWithRouteTo(int targetDeviceId){
		for(Integer childId : getChildrenIds()){
			if(targetDeviceId == childId)
				return childId;
			if(((FogDevice)CloudSim.getEntity(childId)).getChildIdWithRouteTo(targetDeviceId) != -1)
				return childId;
		}
		return -1;
	}
	
	protected int getChildIdForTuple(Tuple tuple){
		if(tuple.getDirection() == Tuple.ACTUATOR){
			int gatewayId = ((Actuator)CloudSim.getEntity(tuple.getActuatorId())).getGatewayDeviceId();
			return getChildIdWithRouteTo(gatewayId);
		}
		return -1;
	}
	
	protected void updateAllocatedMips(String incomingOperator){
		getHost().getVmScheduler().deallocatePesForAllVms();
		for(final Vm vm : getHost().getVmList()){
			if(vm.getCloudletScheduler().runningCloudlets() > 0 || ((AppModule)vm).getName().equals(incomingOperator)){
				getHost().getVmScheduler().allocatePesForVm(vm, new ArrayList<Double>(){
					protected static final long serialVersionUID = 1L;
				{add((double) getHost().getTotalMips());}});
			}else{
				getHost().getVmScheduler().allocatePesForVm(vm, new ArrayList<Double>(){
					protected static final long serialVersionUID = 1L;
				{add(0.0);}});
			}
		}
		
		updateEnergyConsumption();
		
	}
	
	private void updateEnergyConsumption() {
		double totalMipsAllocated = 0;
		for(final Vm vm : getHost().getVmList()){
			AppModule operator = (AppModule)vm;
			operator.updateVmProcessing(CloudSim.clock(), getVmAllocationPolicy().getHost(operator).getVmScheduler()
					.getAllocatedMipsForVm(operator));
			totalMipsAllocated += getHost().getTotalAllocatedMipsForVm(vm);
		}
		
		double timeNow = CloudSim.clock();
		double currentEnergyConsumption = getEnergyConsumption();
		double newEnergyConsumption = currentEnergyConsumption + (timeNow-lastUtilizationUpdateTime)*getHost().getPowerModel().getPower(lastUtilization);
		setEnergyConsumption(newEnergyConsumption);
	
		/*if(getName().equals("d-0")){
			System.out.println("------------------------");
			System.out.println("Utilization = "+lastUtilization);
			System.out.println("Power = "+getHost().getPowerModel().getPower(lastUtilization));
			System.out.println(timeNow-lastUtilizationUpdateTime);
		}*/
		
		double currentCost = getTotalCost();
		double newcost = currentCost + (timeNow-lastUtilizationUpdateTime)*getRatePerMips()*lastUtilization*getHost().getTotalMips();
		setTotalCost(newcost);
		
		lastUtilization = Math.min(1, totalMipsAllocated/getHost().getTotalMips());
		lastUtilizationUpdateTime = timeNow;
	}

	protected void processAppSubmit(SimEvent ev) {
		Application app = (Application)ev.getData();
		applicationMap.put(app.getAppId(), app);
	}

	protected void addChild(int childId){
		if(CloudSim.getEntityName(childId).toLowerCase().contains("sensor"))
			return;
		if(!getChildrenIds().contains(childId) && childId != getId())
			getChildrenIds().add(childId);
		if(!getChildToOperatorsMap().containsKey(childId))
			getChildToOperatorsMap().put(childId, new ArrayList<String>());
	}
	
	protected void updateCloudTraffic(){
		int time = (int)CloudSim.clock()/1000;
		if(!cloudTrafficMap.containsKey(time))
			cloudTrafficMap.put(time, 0);
		cloudTrafficMap.put(time, cloudTrafficMap.get(time)+1);
	}
	
	protected void sendTupleToActuator(Tuple tuple){
		/*for(Pair<Integer, Double> actuatorAssociation : getAssociatedActuatorIds()){
			int actuatorId = actuatorAssociation.getFirst();
			double delay = actuatorAssociation.getSecond();
			if(actuatorId == tuple.getActuatorId()){
				send(actuatorId, delay, FogEvents.TUPLE_ARRIVAL, tuple);
				return;
			}
		}
		int childId = getChildIdForTuple(tuple);
		if(childId != -1)
			sendDown(tuple, childId);*/
		for(Pair<Integer, Double> actuatorAssociation : getAssociatedActuatorIds()){
			int actuatorId = actuatorAssociation.getFirst();
			double delay = actuatorAssociation.getSecond();
			String actuatorType = ((Actuator)CloudSim.getEntity(actuatorId)).getActuatorType();
			if(tuple.getDestModuleName().equals(actuatorType)){
				send(actuatorId, delay, FogEvents.TUPLE_ARRIVAL, tuple);
				return;
			}
		}
		for(int childId : getChildrenIds()){
			sendDown(tuple, childId);
		}
	}
	int numClients=0;
	protected void processTupleArrival(SimEvent ev){
		Tuple tuple = (Tuple)ev.getData();
		
		if(getName().equals("cloud")){
			updateCloudTraffic();
		}
		
		/*if(getName().equals("d-0") && tuple.getTupleType().equals("_SENSOR")){
			System.out.println(++numClients);
		}*/
		Logger.debug(getName(), "Received tuple "+tuple.getCloudletId()+"with tupleType = "+tuple.getTupleType()+"\t| Source : "+
		CloudSim.getEntityName(ev.getSource())+"|Dest : "+CloudSim.getEntityName(ev.getDestination()));
		send(ev.getSource(), CloudSim.getMinTimeBetweenEvents(), FogEvents.TUPLE_ACK);
		
		if(FogUtils.appIdToGeoCoverageMap.containsKey(tuple.getAppId())){
		}
		
		if(tuple.getDirection() == Tuple.ACTUATOR){
			sendTupleToActuator(tuple);
			return;
		}
		
		if(getHost().getVmList().size() > 0){
			final AppModule operator = (AppModule)getHost().getVmList().get(0);
			if(CloudSim.clock() > 0){
				getHost().getVmScheduler().deallocatePesForVm(operator);
				getHost().getVmScheduler().allocatePesForVm(operator, new ArrayList<Double>(){
					protected static final long serialVersionUID = 1L;
				{add((double) getHost().getTotalMips());}});
			}
		}
		
		
		if(getName().equals("cloud") && tuple.getDestModuleName()==null){
			sendNow(getControllerId(), FogEvents.TUPLE_FINISHED, null);
		}
		
		if(appToModulesMap.containsKey(tuple.getAppId())){
			if(appToModulesMap.get(tuple.getAppId()).contains(tuple.getDestModuleName())){
				int vmId = -1;
				for(Vm vm : getHost().getVmList()){
					if(((AppModule)vm).getName().equals(tuple.getDestModuleName()))
						vmId = vm.getId();
				}
				if(vmId < 0
						|| (tuple.getModuleCopyMap().containsKey(tuple.getDestModuleName()) && 
								tuple.getModuleCopyMap().get(tuple.getDestModuleName())!=vmId )){
					return;
				}
				tuple.setVmId(vmId);
				//Logger.error(getName(), "Executing tuple for operator " + moduleName);
				
				updateTimingsOnReceipt(tuple);
				
				executeTuple(ev, tuple.getDestModuleName());
			}else if(tuple.getDestModuleName()!=null){
				if(tuple.getDirection() == Tuple.UP)
					sendUp(tuple);
				else if(tuple.getDirection() == Tuple.DOWN){
					for(int childId : getChildrenIds())
						sendDown(tuple, childId);
				}
			}else{
				sendUp(tuple);
			}
		}else{
			if(tuple.getDirection() == Tuple.UP)
				sendUp(tuple);
			else if(tuple.getDirection() == Tuple.DOWN){
				for(int childId : getChildrenIds())
					sendDown(tuple, childId);
			}
		}
	}

	protected void updateTimingsOnReceipt(Tuple tuple) {
		Application app = getApplicationMap().get(tuple.getAppId());
		String srcModule = tuple.getSrcModuleName();
		String destModule = tuple.getDestModuleName();
		List<AppLoop> loops = app.getLoops();
		for(AppLoop loop : loops){
			if(loop.hasEdge(srcModule, destModule) && loop.isEndModule(destModule)){				
				Double startTime = TimeKeeper.getInstance().getEmitTimes().get(tuple.getActualTupleId());
				if(startTime==null)
					break;
				if(!TimeKeeper.getInstance().getLoopIdToCurrentAverage().containsKey(loop.getLoopId())){
					TimeKeeper.getInstance().getLoopIdToCurrentAverage().put(loop.getLoopId(), 0.0);
					TimeKeeper.getInstance().getLoopIdToCurrentNum().put(loop.getLoopId(), 0);
				}
				double currentAverage = TimeKeeper.getInstance().getLoopIdToCurrentAverage().get(loop.getLoopId());
				int currentCount = TimeKeeper.getInstance().getLoopIdToCurrentNum().get(loop.getLoopId());
				double delay = CloudSim.clock()- TimeKeeper.getInstance().getEmitTimes().get(tuple.getActualTupleId());
				TimeKeeper.getInstance().getEmitTimes().remove(tuple.getActualTupleId());
				double newAverage = (currentAverage*currentCount + delay)/(currentCount+1);
				TimeKeeper.getInstance().getLoopIdToCurrentAverage().put(loop.getLoopId(), newAverage);
				TimeKeeper.getInstance().getLoopIdToCurrentNum().put(loop.getLoopId(), currentCount+1);
				break;
			}
		}
	}

	protected void processSensorJoining(SimEvent ev){
		send(ev.getSource(), CloudSim.getMinTimeBetweenEvents(), FogEvents.TUPLE_ACK);
	}
	
	protected void executeTuple(SimEvent ev, String moduleName){
		Logger.debug(getName(), "Executing tuple on module "+moduleName);
		Tuple tuple = (Tuple)ev.getData();
		
		AppModule module = getModuleByName(moduleName);
		
		if(tuple.getDirection() == Tuple.UP){
			String srcModule = tuple.getSrcModuleName();
			if(!module.getDownInstanceIdsMaps().containsKey(srcModule))
				module.getDownInstanceIdsMaps().put(srcModule, new ArrayList<Integer>());
			if(!module.getDownInstanceIdsMaps().get(srcModule).contains(tuple.getSourceModuleId()))
				module.getDownInstanceIdsMaps().get(srcModule).add(tuple.getSourceModuleId());
			
			int instances = -1;
			for(String _moduleName : module.getDownInstanceIdsMaps().keySet()){
				instances = Math.max(module.getDownInstanceIdsMaps().get(_moduleName).size(), instances);
			}
			module.setNumInstances(instances);
		}
		
		TimeKeeper.getInstance().tupleStartedExecution(tuple);
		updateAllocatedMips(moduleName);
		processCloudletSubmit(ev, false);
		updateAllocatedMips(moduleName);
		/*for(Vm vm : getHost().getVmList()){
			Logger.error(getName(), "MIPS allocated to "+((AppModule)vm).getName()+" = "+getHost().getTotalAllocatedMipsForVm(vm));
		}*/
	}
	
	protected void processModuleArrival(SimEvent ev){
		AppModule module = (AppModule)ev.getData();
		String appId = module.getAppId();
		if(!appToModulesMap.containsKey(appId)){
			appToModulesMap.put(appId, new ArrayList<String>());
		}
		appToModulesMap.get(appId).add(module.getName());
		processVmCreate(ev, false);
		if (module.isBeingInstantiated()) {
			module.setBeingInstantiated(false);
		}
		
		initializePeriodicTuples(module);
		
		module.updateVmProcessing(CloudSim.clock(), getVmAllocationPolicy().getHost(module).getVmScheduler()
				.getAllocatedMipsForVm(module));
	}
	
	private void initializePeriodicTuples(AppModule module) {
		String appId = module.getAppId();
		Application app = getApplicationMap().get(appId);
		List<AppEdge> periodicEdges = app.getPeriodicEdges(module.getName());
		for(AppEdge edge : periodicEdges){
			send(getId(), edge.getPeriodicity(), FogEvents.SEND_PERIODIC_TUPLE, edge);
		}
	}

	protected void processOperatorRelease(SimEvent ev){
		this.processVmMigrate(ev, false);
	}
	
	
	protected void updateNorthTupleQueue(){
		if(!getNorthTupleQueue().isEmpty()){
			Tuple tuple = getNorthTupleQueue().poll();
			sendUpFreeLink(tuple);
		}else{
			setNorthLinkBusy(false);
		}
	}
	
	protected void sendUpFreeLink(Tuple tuple){
		double networkDelay = tuple.getCloudletFileSize()/getUplinkBandwidth();
		setNorthLinkBusy(true);
		send(getId(), networkDelay, FogEvents.UPDATE_NORTH_TUPLE_QUEUE);
		send(parentId, networkDelay+getUplinkLatency(), FogEvents.TUPLE_ARRIVAL, tuple);
		NetworkUsageMonitor.sendingTuple(getUplinkLatency(), tuple.getCloudletFileSize());
	}
	
	protected void sendUp(Tuple tuple){
		if(parentId > 0){
			if(!isNorthLinkBusy()){
				sendUpFreeLink(tuple);
			}else{
				northTupleQueue.add(tuple);
			}
		}
	}
	
	
	protected void updateSouthTupleQueue(){
		if(!getSouthTupleQueue().isEmpty()){
			Pair<Tuple, Integer> pair = getSouthTupleQueue().poll(); 
			sendDownFreeLink(pair.getFirst(), pair.getSecond());
		}else{
			setSouthLinkBusy(false);
		}
	}
	
	protected void sendDownFreeLink(Tuple tuple, int childId){
		double networkDelay = tuple.getCloudletFileSize()/getDownlinkBandwidth();
		//Logger.debug(getName(), "Sending tuple with tupleType = "+tuple.getTupleType()+" DOWN");
		setSouthLinkBusy(true);
		double latency = getChildToLatencyMap().get(childId);
		send(getId(), networkDelay, FogEvents.UPDATE_SOUTH_TUPLE_QUEUE);
		send(childId, networkDelay+latency, FogEvents.TUPLE_ARRIVAL, tuple);
		NetworkUsageMonitor.sendingTuple(latency, tuple.getCloudletFileSize());
	}
	
	protected void sendDown(Tuple tuple, int childId){
		if(getChildrenIds().contains(childId)){
			if(!isSouthLinkBusy()){
				sendDownFreeLink(tuple, childId);
			}else{
				southTupleQueue.add(new Pair<Tuple, Integer>(tuple, childId));
			}
		}
	}
	
	
	protected void sendToSelf(Tuple tuple){
		send(getId(), CloudSim.getMinTimeBetweenEvents(), FogEvents.TUPLE_ARRIVAL, tuple);
	}
	public PowerHost getHost(){
		return (PowerHost) getHostList().get(0);
	}
	public int getParentId() {
		return parentId;
	}
	public void setParentId(int parentId) {
		this.parentId = parentId;
	}
	public List<Integer> getChildrenIds() {
		return childrenIds;
	}
	public void setChildrenIds(List<Integer> childrenIds) {
		this.childrenIds = childrenIds;
	}
	public double getUplinkBandwidth() {
		return uplinkBandwidth;
	}
	public void setUplinkBandwidth(double uplinkBandwidth) {
		this.uplinkBandwidth = uplinkBandwidth;
	}
	public double getUplinkLatency() {
		return uplinkLatency;
	}
	public void setUplinkLatency(double uplinkLatency) {
		this.uplinkLatency = uplinkLatency;
	}
	public boolean isSouthLinkBusy() {
		return isSouthLinkBusy;
	}
	public boolean isNorthLinkBusy() {
		return isNorthLinkBusy;
	}
	public void setSouthLinkBusy(boolean isSouthLinkBusy) {
		this.isSouthLinkBusy = isSouthLinkBusy;
	}
	public void setNorthLinkBusy(boolean isNorthLinkBusy) {
		this.isNorthLinkBusy = isNorthLinkBusy;
	}
	public int getControllerId() {
		return controllerId;
	}
	public void setControllerId(int controllerId) {
		this.controllerId = controllerId;
	}
	public List<String> getActiveApplications() {
		return activeApplications;
	}
	public void setActiveApplications(List<String> activeApplications) {
		this.activeApplications = activeApplications;
	}
	public Map<Integer, List<String>> getChildToOperatorsMap() {
		return childToOperatorsMap;
	}
	public void setChildToOperatorsMap(Map<Integer, List<String>> childToOperatorsMap) {
		this.childToOperatorsMap = childToOperatorsMap;
	}

	public Map<String, Application> getApplicationMap() {
		return applicationMap;
	}

	public void setApplicationMap(Map<String, Application> applicationMap) {
		this.applicationMap = applicationMap;
	}

	public Queue<Tuple> getNorthTupleQueue() {
		return northTupleQueue;
	}

	public void setNorthTupleQueue(Queue<Tuple> northTupleQueue) {
		this.northTupleQueue = northTupleQueue;
	}

	public Queue<Pair<Tuple, Integer>> getSouthTupleQueue() {
		return southTupleQueue;
	}

	public void setSouthTupleQueue(Queue<Pair<Tuple, Integer>> southTupleQueue) {
		this.southTupleQueue = southTupleQueue;
	}

	public double getDownlinkBandwidth() {
		return downlinkBandwidth;
	}

	public void setDownlinkBandwidth(double downlinkBandwidth) {
		this.downlinkBandwidth = downlinkBandwidth;
	}

	public List<Pair<Integer, Double>> getAssociatedActuatorIds() {
		return associatedActuatorIds;
	}

	public void setAssociatedActuatorIds(List<Pair<Integer, Double>> associatedActuatorIds) {
		this.associatedActuatorIds = associatedActuatorIds;
	}
	
	public double getEnergyConsumption() {
		return energyConsumption;
	}

	public void setEnergyConsumption(double energyConsumption) {
		this.energyConsumption = energyConsumption;
	}
	public Map<Integer, Double> getChildToLatencyMap() {
		return childToLatencyMap;
	}

	public void setChildToLatencyMap(Map<Integer, Double> childToLatencyMap) {
		this.childToLatencyMap = childToLatencyMap;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public double getRatePerMips() {
		return ratePerMips;
	}

	public void setRatePerMips(double ratePerMips) {
		this.ratePerMips = ratePerMips;
	}
	public double getTotalCost() {
		return totalCost;
	}

	public void setTotalCost(double totalCost) {
		this.totalCost = totalCost;
	}

	public Map<String, Map<String, Integer>> getModuleInstanceCount() {
		return moduleInstanceCount;
	}

	public void setModuleInstanceCount(
			Map<String, Map<String, Integer>> moduleInstanceCount) {
		this.moduleInstanceCount = moduleInstanceCount;
	}
/*	public  void AddXLSCol(String excelFileName, String sheetName, String name,int cl)
	{
		try {
			FileInputStream cFileInputStream = new FileInputStream(excelFileName);
			XSSFWorkbook cWorkbook = new XSSFWorkbook(cFileInputStream);
			XSSFSheet cWorksheet = cWorkbook.getSheet(sheetName);
			
			XSSFRow row = cWorksheet.createRow(0);	 

			XSSFCell cell1 = row.createCell(cl);
			                         
			cell1.setCellValue(name);
			 
			FileOutputStream out = new FileOutputStream(new File(excelFileName));
			cWorkbook.write(out);
			out.close();
			cWorkbook.close();
			}catch (FileNotFoundException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        } catch (IOException e) {
	            // TODO Auto-generated catch block
	            e.printStackTrace();
	        }		
	}
	public synchronized  void AddXLSRow(String excelFileName, String sheetName,int rw,int cl,double value)
	{
		try {
		FileInputStream cFileInputStream = new FileInputStream(excelFileName);
		XSSFWorkbook cWorkbook = new XSSFWorkbook(cFileInputStream);
		XSSFSheet cWorksheet = cWorkbook.getSheet(sheetName);
		
//		int n=cWorksheet.getLastRowNum()+1;
//		int n=(FogDevice.Max_itr-1)*this.n+rw;
		XSSFRow row = cWorksheet.createRow(rw);		 
		 
		XSSFCell cell1 = row.createCell(cl);
//		String name = value;
		                         
		cell1.setCellValue(value);
		 
		FileOutputStream out = new FileOutputStream(new File(excelFileName));
		cWorkbook.write(out);
		out.close();
		}catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
}*/
}